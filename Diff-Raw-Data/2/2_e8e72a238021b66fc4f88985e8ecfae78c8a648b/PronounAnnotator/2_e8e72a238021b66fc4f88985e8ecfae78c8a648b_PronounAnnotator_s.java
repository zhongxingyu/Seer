 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.philgooch;
 
 import gate.*;
 import gate.creole.*;
 import gate.creole.metadata.*;
 import gate.util.*;
 
 import gate.Annotation;
 import gate.AnnotationSet;
 import gate.Factory;
 import gate.FeatureMap;
 import gate.event.ProgressListener;
 import gate.event.StatusListener;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 import com.jenkov.*;
 
 /**
  *
  * @author philipgooch
  */
 @CreoleResource(name = "Pronoun Annotator",
 helpURL = "",
 comment = "Plugin that annotates pronouns according to anaphoricity/pleonasticity, number, case, gender")
 public class PronounAnnotator extends AbstractLanguageAnalyser implements ProgressListener,
         ProcessingResource,
         Serializable {
 
     private LanguageAnalyser japeTransducer = null;     // the JAPE transducer we will instantiate
     private String inputASName;     //  Input AnnotationSet name
     private String outputASName;    // Output AnnotationSet set name
     private URL japeURL;      // URL to JAPE main file
     private URL configFileURL;      // URL to configuration file that defines base annotation types
     private String pronoun;
     private String relativePronoun;
     private String tokenName;                   // default to Token
     private String verbGroupName;               // default to VG
     private String sentenceName;               // default to Sentence
     private String quotedTextName;              // Annotation type that holds quoted text
     private Protagonist personPronoun2nd;       // Allow linkage of second person pronouns (you, your) to a protagonist
     private Protagonist personPronoun1st;       // Allow linkage of first person pronouns (I, me) to a protagonist
     private Protagonist quotedPP3rd;            // Default linkage of third person pronouns in quoted text to a protagonist
     private ArrayList<String> personTypes;      // Person types to match against personal pronouns
     private ArrayList<String> locationTypes;    // Location types to match against here, there, where
     private ArrayList<String> otherTypes;
     private ArrayList<String> excludeIfWithin;      // don't process if any input annotation occurs inside one of these annots, e.g. quoted text
     private String corefIdFeature;              // Feature that will hold coref id
     private String backrefIdFeature;            // Feature that will hold antecedent backref id
     private String backrefTextFeature;            // Feature that will hold antecedent backref text
     private String corefTextFeature;          // Feature that will hold coreferring text
     private Integer maxSentenceDistance;        // Maximum sentence distance between antecedent and anaphor
     private Boolean corefSortalMentions;		// Flag to determine whether to do pronominal coref on sortal mentions
     // Exit gracefully if exception caught on init()
     private boolean gracefulExit;
     private ConfigParser config;                // config file
     private static final String properties = "org.philgooch.configProperties";  // config properties
 
     // allow selection of core protagonist for matching first and second person pronouns
     public enum Protagonist {
 
         None, FirstPersonMentioned, LastPersonMentioned, MostFrequentPersonMentioned, PreviousPersonMentioned
     }
 
     /**
      *
      * @param inputAS           some input Annotation Set
      * @param inputAnnExpr      some Annotation or Annotation.feature == value expression
      * @return  inputAS filtered according to inputAnnExpr
      */
     private AnnotationSet getFilteredAS(AnnotationSet inputAS, String inputAnnExpr) {
         // We allow inputAnnExpr of the form
         // Annotation or Annotation.feature == value
         String annFeature;
         String annFeatureValue;
 
         // Assume a simple ann name unless we have a feature and feature value present
         AnnotationSet filteredAS = inputAS.get(inputAnnExpr);
 
         // Check if we have an expression of the form Annotation.feature == value
         String[] inputAnnArr = inputAnnExpr.split("\\s*==\\s*");
 
         if (inputAnnArr.length == 2) {
             String base = inputAnnArr[0];
             int dot = base.lastIndexOf(".");
             if (dot > 0 && dot < base.length() - 1) {
                 String annName = base.substring(0, dot);
                 annFeature = base.substring(dot + 1);
                 annFeatureValue = inputAnnArr[1];
                 FeatureMap annFeats = Factory.newFeatureMap();
                 annFeats.put(annFeature, annFeatureValue);
                 filteredAS = inputAS.get(annName, annFeats);
             }
         }
         return filteredAS;
     }
 
     /**
      * Pluralisation of mentions:
      *  For prepositional phrases, take the number of the last noun before the (first) preposition (e.g. a severe pain in both legs -> singular)
      *  For non-prepositional phrases, take the number of the last noun
      *  For mentions containing a conjunction, treat as plural.
      * @param inputAS   Input Annotation Set
      * @param ann       Annotation candidate for pronominal coreference
      * 
      */
     private void setMentionPlurality(AnnotationSet inputAS, Annotation ann) {
         Long annStart = ann.getStartNode().getOffset();
         Long annEnd = ann.getEndNode().getOffset();
         FeatureMap annFeats = ann.getFeatures();
 
         String plurality = "singular";
         List<Annotation> innerToks = new ArrayList<Annotation>(inputAS.getContained(annStart, annEnd).get(tokenName));
         Collections.sort(innerToks, new OffsetComparator());
 
         int numToks = innerToks.size();
         for (int i = 0; i < numToks; i++) {
             Annotation tok = innerToks.get(i);
             String word = (String) tok.getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME);
             String cat = (String) tok.getFeatures().get(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
             if (i > 0 && word != null && word.matches("(?i)and|or")) {
                 plurality = "plural";
                 break;
             }
             if (cat != null) {
                 if (i > 0 && cat.matches("IN|TO")) {        // Prepositional phrase
                     Annotation prevTok = innerToks.get(i - 1);
                     String prevTokCat = (String) prevTok.getFeatures().get(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
                     if (prevTokCat != null && prevTokCat.matches("NNS|NNPS|NPS")) {
                         plurality = "plural";
                         break;
                     }
                 } else if (i == numToks - 1 && cat.matches("NNS|NNPS|NPS")) { // Normal phrase, if last word is plural noun, then term is plural
                     plurality = "plural";
                     break;
                 }
             }
         }
         if (annFeats.get("number") == null) {
             annFeats.put("number", plurality);
         }
     }
 
     /**
      * Quick and dirty way of assigning case to the annotation based on its position relative to the main verb of the sentence
      * @param inputAS           Input Annotation Set
      * @param sentence          Sentence annotation that wraps the candidate annotation
      * @param ann               Candidate annotation
      */
     private void setMentionCase(AnnotationSet inputAS, Annotation ann) {
         String mentionCase = "nominative";
 
         Long annStart = ann.getStartNode().getOffset();
         Long annEnd = ann.getEndNode().getOffset();
         FeatureMap annFeats = ann.getFeatures();
 
         AnnotationSet sentenceAS = inputAS.getCovering(sentenceName, annStart, annEnd);
         if (sentenceAS.isEmpty()) {
             return;
         }
 
         Annotation sentence = sentenceAS.iterator().next();
         Long sentStart = sentence.getStartNode().getOffset();
         Long sentEnd = sentence.getEndNode().getOffset();
 
         List<Annotation> sentenceToks = new ArrayList<Annotation>(inputAS.getContained(sentStart, sentEnd).get(tokenName));
         sentenceToks.add(ann);
         Collections.sort(sentenceToks, new OffsetComparator());
 
         int annPosition = sentenceToks.indexOf(ann);
 
         List<Annotation> sentenceVGs = new ArrayList<Annotation>(inputAS.getContained(sentStart, sentEnd).get(verbGroupName));
         if (!sentenceVGs.isEmpty()) {
             // If we follow the main verb, set case to objective (could be direct or indirect object)
             Collections.sort(sentenceVGs, new OffsetComparator());
             Annotation firstVG = sentenceVGs.get(0);
             if (annStart > firstVG.getEndNode().getOffset()) {
                 mentionCase = "objective";
             }
         }
         // Check if we are preceded by a preposition or a conjunction
         if (annPosition > 0) {
             Annotation prevTok;
             do {
                 prevTok = sentenceToks.get(annPosition - 1);
                 annPosition--;
             } while (prevTok.getStartNode().getOffset() == annStart && annPosition > 0);
 
             String cat = (String) prevTok.getFeatures().get(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
             String str = (String) prevTok.getFeatures().get(ANNIEConstants.TOKEN_STRING_FEATURE_NAME);
             if (cat != null) {
                 if (cat.matches("IN|TO")) {
                     mentionCase = "objective";
                     // New clause, e.g. look for CC or : | ; before ann => nominative
                 } else if (cat.equals("CC") || str.matches(":|;")) {
                     mentionCase = "nominative";
                 }
             }
         }
         annFeats.put("case", mentionCase);
         // Default to third person unless previously set by Protagonist
         if (annFeats.get("person") == null) {
             annFeats.put("person", "third");
         }
     }
 
     /**
      *
      * @param inputAS               input Annotation Set
      * @param mentionTypes          ArrayList<String> of annotation names or expressions (Annotation.feat == value) for coreference
      * @param inputAnnsList         List of lists that will hold all annotation types to be coreferenced
      */
     private void populateMentionList(AnnotationSet inputAS, List<String> mentionTypes, List<List<Annotation>> inputAnnsList) {
         List<Annotation> mentionList = new ArrayList<Annotation>();
         for (String mentionType : mentionTypes) {
             AnnotationSet mentionAS = getFilteredAS(inputAS, mentionType);
             mentionList.addAll(mentionAS);
         }
         Collections.sort(mentionList, new OffsetComparator());
         if (!corefSortalMentions) {
             // remove mentions with a sortal feature
             List<Annotation> removeList = new ArrayList<Annotation>();
             for (Annotation ann : mentionList) {
                 FeatureMap fm = ann.getFeatures();
                 if (fm.containsKey("sortal")) {
                     removeList.add(ann);
                 }
             }
             mentionList.removeAll(removeList);
         }
         inputAnnsList.add(mentionList);
     }
 
     /**
      *
      * @param expression    an expression of the form Annotation or Annotation.feature == value
      * @return              string containing the annotation name from the input expression
      */
     private String getAnnNameFromExpression(String expression) {
         String[] inputAnnArr = expression.split("\\s*==\\s*");
         return inputAnnArr[0];
     }
 
     /**
      *
      * @param inputAS           input Annotation Set
      * @param currStart         annotation start offset
      * @param currEnd           annotation end offset
      * @return                  true if annotation occurs within exclusion region
      */
     private boolean isInExclusionRegion(AnnotationSet inputAS, Long currStart, Long currEnd) {
         // Don't process this annotation if it occurs within a defined exclusion zone
         if (excludeIfWithin != null && !(excludeIfWithin.isEmpty())) {
             for (String excludeAnnExpr : excludeIfWithin) {
                 String excludeAnnName = getAnnNameFromExpression(excludeAnnExpr);
                 AnnotationSet tempAS = inputAS.getCovering(excludeAnnName, currStart, currEnd);
                 AnnotationSet excludeAS = getFilteredAS(tempAS, excludeAnnExpr);
                 if (!excludeAS.isEmpty()) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      *
      * @param inputAS           input Annotation Set
      * @param currStart         annotation start offset
      * @param currEnd           annotation end offset
      * @return                  true if annotation occurs within quoted text
      */
     private boolean isInQuotedText(AnnotationSet inputAS, Long currStart, Long currEnd) {
         if (quotedTextName != null && !(quotedTextName.isEmpty())) {
             String excludeAnnName = getAnnNameFromExpression(quotedTextName);
             AnnotationSet tempAS = inputAS.getCovering(excludeAnnName, currStart, currEnd);
             AnnotationSet quoteAS = getFilteredAS(tempAS, quotedTextName);
             if (!quoteAS.isEmpty()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Get all pronouns that occur within range
      * @param inputAS           Input annotation set
      * @param pronounsList      List of pronouns to be populated
      * @param startPoint        Range start offset
      * @param endPoint          Range end offset
      */
     private void populatePronounListInRange(AnnotationSet inputAS, List<Annotation> pronounsList, Long startOffset, Long endOffset, String cls, String matchPP) {
         FeatureMap anaphorFilter = Factory.newFeatureMap();
         anaphorFilter.put("type", "anaphoric");
         if (matchPP != null) {
         	anaphorFilter.put("person", matchPP);
         }
         anaphorFilter.put("cls", cls);
         AnnotationSet currSentencePronouns = inputAS.getContained(startOffset, endOffset).get(pronoun, anaphorFilter);
         AnnotationSet currSentenceRelativePronouns = inputAS.getContained(startOffset, endOffset).get(relativePronoun, anaphorFilter);
         pronounsList.addAll(currSentencePronouns);
         pronounsList.addAll(currSentenceRelativePronouns);
         Collections.sort(pronounsList, new OffsetComparator());
     }
 
     /**
      * Check for potential gender ambiguity in range. I.e. if there is only one male or one female mention, pronominal coreference will be deterministic
      * @param personAnns        List of 'Person' type annotations in the document
      * @param startOffset       Start offset of range
      * @param endOffset         End offset of range
      * @return                  True if more than one male or female mention in the range
      */
     private boolean hasPotentialGenderAmbiguityInRange(List<Annotation> personAnns, Long startOffset, Long endOffset) {
         int numMales = 0;
         int numFemales = 0;
         for (Annotation ann : personAnns) {
             Long annStart = ann.getStartNode().getOffset();
             Long annEnd = ann.getEndNode().getOffset();
             String gender = (String) ann.getFeatures().get("gender");
             if (annStart >= startOffset && annEnd <= endOffset && gender != null) {
                 if (gender.equals("male")) {
                     numMales++;
                 } else if (gender.equals("female")) {
                     numFemales++;
                 } else if (gender.equals("either")) {
                     numMales++;
                     numFemales++;
                 }
             }
         }
         if (numMales == 1 || numFemales == 1) {
             return false;
         }
         return true;
     }
 
     /**
      *
      * @param mentionList       List of mentions
      * @param startOffset       Range start offset
      * @param endOffset         Range end offset
      * @param filter            FeatureMap to filter mentions
      * @return                  True if a mention in mentionList occurs in the range
      */
     private boolean hasMentionInRange(List<Annotation> mentionList, Long startOffset, Long endOffset, FeatureMap filter) {
         int numIntervening = 0;
         for (Annotation ann : mentionList) {
             Long annStart = ann.getStartNode().getOffset();
             Long annEnd = ann.getEndNode().getOffset();
             FeatureMap feats = ann.getFeatures();
             if (annStart >= startOffset && annEnd <= endOffset) {
                 boolean filterMatch = true;
                 if (filter != null) {
                     for (Map.Entry<Object, Object> entry : filter.entrySet()) {
                         String key = (String) entry.getKey();
                         Object value = entry.getValue();
                         Object featValue = feats.get(key);
                         if (!(featValue != null && featValue.equals(value))) {
                             filterMatch = false;
                             break;
                         }
                     }
                 }
                 if (filterMatch) {
                     numIntervening++;
                 }
             }
         }
         return (numIntervening > 0);
     }
 
     @Override
     public Resource init() throws ResourceInstantiationException {
         gracefulExit = false;
 
         if (configFileURL == null) {
             gracefulExit = true;
             gate.util.Err.println("No configuration file provided!");
         }
 
 
         config = new ConfigParser(configFileURL);
         boolean configOK = false;
         try {
             configOK = config.populateOptions(properties);
         } catch (Exception e) {
             gracefulExit = true;
             gate.util.Err.println(e);
         } finally {
             if (!(configOK)) {
                 gracefulExit = true;
                 gate.util.Err.println("Missing or incomplete configuration data in " + configFileURL);
             }
         }
 
         // Get base annotation configuration information
         HashMap<String, String> options = config.getOptions();
 
         // Set defaults
         personTypes = new ArrayList<String>();
         String option = options.get("basePersonType");
         personTypes.add(option == null || option.isEmpty() ? ANNIEConstants.PERSON_ANNOTATION_TYPE : option);
 
         locationTypes = new ArrayList<String>();
         option = options.get("baseLocationType");
         locationTypes.add(option == null || option.isEmpty() ? ANNIEConstants.LOCATION_ANNOTATION_TYPE : option);
 
         otherTypes = new ArrayList<String>();
         option = options.get("baseOrganizationType");
         otherTypes.add(option == null || option.isEmpty() ? ANNIEConstants.ORGANIZATION_ANNOTATION_TYPE : option);
         option = options.get("baseOtherType");
         otherTypes.add(option == null || option.isEmpty() ? "Unknown" : option);
 
         BufferedReader in = null;
         String inputLine = null;
         File f = new File(japeURL.getPath());
         String path = f.getParent() + "/";
 
         // copy japeURL file to temporary file
         File p = new File(path);
         File tempJapeMain = null;
         FileWriter fw = null;
         try {
             tempJapeMain = File.createTempFile("pr_", ".jape", p);
             tempJapeMain.deleteOnExit();
             fw = new FileWriter(tempJapeMain);
         } catch (IOException ie) {
             gracefulExit = true;
             gate.util.Err.println("Unable to create temp JAPE file.");
         }
 
 
         List<String> japeList = new ArrayList<String>();
         try {
             in = new BufferedReader(new InputStreamReader(japeURL.openStream()));
             boolean startReading = false;
             while ((inputLine = in.readLine()) != null) {
                 if (startReading) {
                     japeList.add(inputLine);
                 } else {
                     fw.write(inputLine);
                     fw.write("\n");
                 }
                 if (inputLine.startsWith("Phases:")) {
                     startReading = true;
                 }
             }
         } catch (FileNotFoundException e) {
             gracefulExit = true;
             gate.util.Err.println("Unable to locate main JAPE file.");
         } catch (IOException ie) {
             gracefulExit = true;
             gate.util.Err.println("Unable to read file " + japeURL);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException iee) {
                     gracefulExit = true;
                 }
             }
         }
 
         // Slight problem is that these base types get populated at init() time, but these may be overridden before execution.
         Map<String, String> tokens = new HashMap<String, String>();
         tokens.put("PRONOUN", pronoun);
         tokens.put("RELATIVE_PRONOUN", relativePronoun);
         tokens.put("PERSON", personTypes.get(0));
         tokens.put("LOCATION", locationTypes.get(0));
         tokens.put("ORGANIZATION", otherTypes.get(0));
 
         // Sortal instances of JobTitle will be converted to Person annots by the transducer
         option = options.get("baseJobTitleType");
         tokens.put("JOBTITLE", option == null || option.isEmpty() ? "JobTitle" : option);
         // Interested in Person Title to determine role (e.g. clinical for Dr, MD etc)
         option = options.get("baseTitleType");
         tokens.put("TITLE", option == null || option.isEmpty() ? "Title" : option);
         // To annotate other sortal references and add them to the list of otherTypes for pronominal coref
         option = options.get("baseSortalType");
         if (option == null || option.isEmpty()) {
             option = "Unknown";
         }
         tokens.put("SORTAL", option);
         if (!otherTypes.contains(option)) {
             otherTypes.add(option);
         }
         option = options.get("baseDiseaseSortalType");
         if (option == null || option.isEmpty()) {
             option = "Unknown";
         }
         tokens.put("DISEASE", option);
         if (!otherTypes.contains(option)) {
             otherTypes.add(option);
         }
         option = options.get("baseProcedureSortalType");
         if (option == null || option.isEmpty()) {
             option = "Unknown";
         }
         tokens.put("PROCEDURE", option);
         if (!otherTypes.contains(option)) {
             otherTypes.add(option);
         }
         option = options.get("baseTestSortalType");
         if (option == null || option.isEmpty()) {
             option = "Unknown";
         }
         tokens.put("TEST", option);
         if (!otherTypes.contains(option)) {
             otherTypes.add(option);
         }
         option = options.get("baseSymptomSortalType");
         if (option == null || option.isEmpty()) {
             option = "Unknown";
         }
         tokens.put("SYMPTOM", option);
         if (!otherTypes.contains(option)) {
             otherTypes.add(option);
         }
         option = options.get("quotedTextType");
         if (option == null || option.isEmpty()) {
             option = "QuotedText";
         }
         tokens.put("QUOTED_TEXT", option);
 
         MapTokenResolver resolver = new MapTokenResolver(tokens);
 
         int i = 0;
         for (String jape : japeList) {
             i++;
             Reader reader = null;
             FileWriter fstream = null;
             BufferedWriter out = null;
 
             try {
                 reader = new TokenReplacingReader(new FileReader(new File(path + jape + ".template")), resolver);
             } catch (IOException e) {
                 gate.util.Err.println("Missing file " + path + jape + ".template");
                 gracefulExit = true;
             }
             if (reader != null) {
                 try {
                     File tempJape = File.createTempFile("pr" + i, ".jape", p);
                     tempJape.deleteOnExit();
                     String tmpName = tempJape.getName();
                     tmpName = tmpName.substring(0, tmpName.lastIndexOf("."));
                     //fstream = new FileWriter(path + jape + ".jape");
                     fw.write(tmpName);
                     fw.write("\n");
                     fstream = new FileWriter(tempJape);
                     out = new BufferedWriter(fstream);
                     int data = reader.read();
                     while (data != -1) {
                         out.write((char) data);
                         data = reader.read();
                     }
                 } catch (IOException e) {
                     gracefulExit = true;
                     gate.util.Err.println("Unable to create file " + path + jape + ".jape");
                 } finally {
                     if (out != null) {
                         try {
                             out.close();
                         } catch (IOException iee) {
                             gracefulExit = true;
                         }
 
                     }
                 } // end finally
             } // end if
         } // end for
 
         if (fw != null) {
             try {
                 fw.close();
             } catch (IOException ie) {
                 gracefulExit = true;
             }
         }
 
         // Now need to instantiate the JAPE transducer that has been generated
         FeatureMap hidden = Factory.newFeatureMap();
         Gate.setHiddenAttribute(hidden, true);
         FeatureMap params = Factory.newFeatureMap();
         URL tempJapeMainURL = null;
         try {
             tempJapeMainURL = tempJapeMain.toURI().toURL();
         } catch (MalformedURLException me) {
             gracefulExit = true;
         }
         //params.put("grammarURL", japeURL);
         params.put(Transducer.TRANSD_GRAMMAR_URL_PARAMETER_NAME, tempJapeMainURL);
         if (japeTransducer == null) {
             japeTransducer = (LanguageAnalyser) Factory.createResource("gate.creole.Transducer",
                     params, hidden);
         } else {
             japeTransducer.setParameterValues(params);
             japeTransducer.reInit();
         }
 
         return this;
     } // end init()
 
     @Override
     public void execute() throws ExecutionException {
         interrupted = false;
         // quit if setup failed
         if (gracefulExit) {
             gate.util.Err.println("Plugin was not initialised correctly. Exiting gracefully ... ");
             cleanup();
             fireProcessFinished();
             return;
         }
 
         // Run the JAPE transducer
         try {
             japeTransducer.setDocument(document);
             japeTransducer.setParameterValue(Transducer.TRANSD_INPUT_AS_PARAMETER_NAME, inputASName);
             japeTransducer.setParameterValue(Transducer.TRANSD_OUTPUT_AS_PARAMETER_NAME, outputASName);
 
             japeTransducer.execute();
         } catch (ResourceInstantiationException re) {
             gate.util.Err.println("Unable to run " + japeURL);
             gracefulExit = true;
         } finally {
             japeTransducer.setDocument(null);
         }
 
         // Do some coreferencing
         AnnotationSet inputAS = (inputASName == null || inputASName.trim().length() == 0) ? document.getAnnotations() : document.getAnnotations(inputASName);
         AnnotationSet outputAS = (outputASName == null || outputASName.trim().length() == 0) ? document.getAnnotations() : document.getAnnotations(outputASName);
 
         // Document content
         String docContent = document.getContent().toString();
 		int docLen = docContent.length();
 		
         // Sorted list of all sentences in document
         List<Annotation> sentenceList = new ArrayList<Annotation>(inputAS.get(sentenceName));
         int numSentences = sentenceList.size();
         Collections.sort(sentenceList, new OffsetComparator());
 		Annotation firstSentence = sentenceList.get(0);
         Annotation lastSentence = sentenceList.get(numSentences - 1);
         
         List<List<Annotation>> inputAnnsList = new ArrayList<List<Annotation>>();
 
         this.populateMentionList(inputAS, personTypes, inputAnnsList);
         this.populateMentionList(inputAS, locationTypes, inputAnnsList);
         this.populateMentionList(inputAS, otherTypes, inputAnnsList);
 
         // May wish to calculate last or first appearing person as the Protagonist
         //Annotation lastPerson = null;
         //Annotation firstPerson = null;
 		
 		fireStatusChanged("Performing pronominal coreference in " + document.getName());
         fireProgressChanged(0);
         int progress = 0;
         
         for (int i = 0; i < inputAnnsList.size(); i++) {
         	progress++;
         	fireProgressChanged(progress / docLen);
         	
             int anaphorId = -1;
 
             // inputAnns is a list of annotations of the same type (Person, Location, Other)
             List<Annotation> inputAnns = inputAnnsList.get(i);
 
             // Will make a copy of the list of Person annotations to keep track of male/female mentions
             List<Annotation> personAnns = new ArrayList<Annotation>();
             
             // Collections.sort(inputAnns, new OffsetComparator()); already sorted
             // Document may not contain any of the inputAnns we are interested in
             if (inputAnns == null || inputAnns.isEmpty()) {
                 continue;
             }
 
             Annotation lastPerson = inputAnns.get(inputAnns.size() - 1);
             Annotation firstPerson = inputAnns.get(0);
 
             Annotation curr;
             if (i == 0 && personPronoun1st == Protagonist.LastPersonMentioned || personPronoun2nd == Protagonist.LastPersonMentioned) {
                 curr = lastPerson;
             } else {
                 curr = inputAnns.iterator().next();
             }
             //Annotation curr = inputAnns.iterator().next();
 
             // Shouldn't happen but if document has been modified, it can occur
             if (curr == null) {
                 continue;
             }
 
             // Reset coref features
             FeatureMap f = curr.getFeatures();
             f.remove(corefIdFeature);
             f.remove(corefTextFeature);
             f.remove(backrefTextFeature);
             f.remove(backrefIdFeature);
             f.remove("person");
             f.remove("case");
             f.remove("isFirst");
             f.remove("isLast");
             f.remove("isMostFrequent");
             f.remove("matchPP");
 
             boolean matchedPair = false;
             boolean forceCaseMatch = false;
 
             // Which type of annotation are we processing - Person, Location, or Other
             String cls = "";
             // i = 0: Person; i = 1: Location; i = 2: Everything else
             switch (i) {
                 case 0:
                     cls = "Person";
                     break;
                 case 1:
                     cls = "Location";
                     break;
                 default:
                     cls = "Thing";
                     break;
             }
 
             // Get most frequently occurring person for possible Protagonist
             // Will probably need find other strings that match Surname (or Firstname)
             // Surname only will probably be tagged as Unknown. Or make use of Orthomatcher features?
             if (i == 0) {
                 // lastPerson = inputAnns.get(inputAnns.size() - 1);
                 // firstPerson = inputAnns.get(0);
                 firstPerson.getFeatures().put("isFirst", true);
                 lastPerson.getFeatures().put("isLast", true);
                 if (personPronoun1st == Protagonist.LastPersonMentioned) {
                     lastPerson.getFeatures().put("matchPP", "first");
                 } else if (personPronoun1st == Protagonist.FirstPersonMentioned) {
                     firstPerson.getFeatures().put("matchPP", "first");
                 }
                 if (personPronoun2nd == Protagonist.LastPersonMentioned) {
                     lastPerson.getFeatures().put("matchPP", "second");
                 } else if (personPronoun2nd == Protagonist.FirstPersonMentioned) {
                     firstPerson.getFeatures().put("matchPP", "second");
                 }
 
                 int max = 0;
                 for (Annotation person : inputAnns) {
                     personAnns.add(person);
                     List<Integer> matchesList = new ArrayList<Integer>();
                     FeatureMap fm = person.getFeatures();
                     if (fm.containsKey(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME)) {
                         try {
                             matchesList = (List<Integer>) fm.get(ANNIEConstants.ANNOTATION_COREF_FEATURE_NAME);
                             int sz = matchesList.size();
                             if (sz > max) {
                                 max = sz;
                                 fm.put("isMostFrequent", true);
                                 if (personPronoun2nd == Protagonist.MostFrequentPersonMentioned) {
                                     fm.put("matchPP", "second");
                                 } else if (personPronoun1st == Protagonist.MostFrequentPersonMentioned) {
                                     fm.put("matchPP", "first");
                                 }
                             }
                         } catch (ClassCastException ce) {
                             // ignore, no matches
                         }
                     }
                 }
             }
 
 
             // Iterate over annotations of the same type
             while (!inputAnns.isEmpty()) {
                 // Shouldn't happen but if document has been modified, it can occur
                 if (curr == null) {
                     break;
                 }
 
                 int currId = curr.getId();
                 String currType = curr.getType();
                 Long currStart = curr.getStartNode().getOffset();
                 Long currEnd = curr.getEndNode().getOffset();
                 String currString = docContent.substring(currStart.intValue(), currEnd.intValue()).trim();
                 // Are we processing the last person and are we coreferencing first person pronouns against them?
                 if (cls.equals("Person") && personPronoun1st == Protagonist.LastPersonMentioned && curr.equals(lastPerson)) {
                     currStart = firstPerson.getStartNode().getOffset();
                     currEnd = firstPerson.getEndNode().getOffset();
                 }
                 matchedPair = false;
 
                 // Progress bar
                 fireProgressChanged(100 * currId / inputAnns.size());
                 if (isInterrupted()) {
                     throw new ExecutionException("Execution of pronominal coreference was interrupted.");
                 }
 
                 // inputAnns.remove(curr);
 
                 // Get sentences that cover this candidate antecedent
                 AnnotationSet currSentenceAS = inputAS.getCovering(sentenceName, currStart, currEnd);
 
                 // Don't process this antecedent if it occurs within a defined exclusion zone
                 if (isInExclusionRegion(inputAS, currStart, currEnd) || currSentenceAS.isEmpty()) {
                     inputAnns.remove(curr);
                     if (inputAnns.iterator().hasNext()) {
                         curr = inputAnns.iterator().next();
                     }
                     continue;
                 }
 
 				// Get the features of the current antecedent
                 FeatureMap currFeats = curr.getFeatures();
                 Integer backrefId = (Integer) currFeats.get(backrefIdFeature);
                 if (backrefId == null) {    // Not a cloned Person annotation from previous antecedent
                     this.setMentionPlurality(inputAS, curr);
                     this.setMentionCase(inputAS, curr);
                     forceCaseMatch = false;
                 }
 				String currCase = (String) currFeats.get("case");
                 String currNumber = (String) currFeats.get("number");
                 String currGender = (String) currFeats.get("gender");
                 String currPerson = (String) currFeats.get("person");
                 String matchPP = (String) currFeats.get("matchPP");
                 Boolean isFirstPerson = (Boolean) currFeats.get("isFirst");
                 Boolean isLastPerson = (Boolean) currFeats.get("isLast");
                 Boolean isMostFrequentPerson = (Boolean) currFeats.get("isMostFrequent");
                 
 				// For non-Person mentions, we really want to be in the same sentence or 1 sentence apart
 				// Person mentions tend to have a wider coreference scope (protagonist theory)
 				int sentenceDistance = 1;
 				if ( cls.equals("Person") ) {
 					sentenceDistance = maxSentenceDistance;
 				}
                 Annotation currSentence = currSentenceAS.iterator().next();
                 int currSentencePos = sentenceList.indexOf(currSentence);
                 int endSentencePos = currSentencePos + sentenceDistance;
                 if (currSentencePos >= numSentences - 1) {
                     endSentencePos = currSentencePos;
                 } else if (endSentencePos >= numSentences - 1) {
                     endSentencePos = numSentences - 1;
                 }
                 Annotation endSentence = sentenceList.get(endSentencePos);
                 
                 // Span the whole document if processing first person pronouns against the last person mentioned
                 if (isLastPerson != null) {
                 	endSentence = lastSentence;
                 }
                 Long currSentenceStart = currSentence.getStartNode().getOffset();
                 Long currSentenceEnd = currSentence.getEndNode().getOffset();
                 Long endSentenceStart = endSentence.getStartNode().getOffset();
                 Long endSentenceEnd = endSentence.getEndNode().getOffset();
 
                 // Get all anaphors that appear after the antecedent within maxSentenceDistance and of the same class as the antecedent
                 List<Annotation> pronounsList = new ArrayList<Annotation>();
                 populatePronounListInRange(inputAS, pronounsList, currEnd, endSentenceEnd, cls, matchPP);
 
                 // Check if we have potential gender ambiguity for coreference, i.e. more than one male or female to match
                 boolean genderCorefAmbiguity = false;
                 if (cls.equals("Person")) {
                     // Boolean isMostFrequentPerson = (Boolean)currFeats.remove("isMostFrequent");
                     // Do we want to coreference the first person mentioned with you, your pronouns?
                     if (isFirstPerson != null 
                             && (personPronoun2nd == Protagonist.FirstPersonMentioned || personPronoun1st == Protagonist.FirstPersonMentioned)) {
                         forceCaseMatch = true;
                     } // Do we want to coreference the most frequent person mentioned with with you, your, pronouns?
                     else if (isMostFrequentPerson != null 
                             && (personPronoun2nd == Protagonist.MostFrequentPersonMentioned || personPronoun1st == Protagonist.MostFrequentPersonMentioned)) {
                         forceCaseMatch = true;
                     }
                     genderCorefAmbiguity = hasPotentialGenderAmbiguityInRange(personAnns, currSentenceStart, endSentenceEnd);
                 }
 
                 inputAnns.remove(curr); // remove current iteration from the list so we don't check it again
 
                 // Iterate over pronouns in range
                 for (Iterator<Annotation> itr = pronounsList.iterator(); itr.hasNext();) {
                     // Flags for determining closeness of match according to gender, case, distance, number
                     boolean numberMatch = false;
                     boolean proximityMatch = true;
                     boolean genderMatch = true;
                     boolean caseMatch = true;
                     boolean personMatch = true;
                     boolean contextMatch = false;
                     boolean inQuotedText = false;
                     boolean prInQuotedText = false;
                     boolean currInQuotedText = false;
 
 					if (isInterrupted()) {
                     	throw new ExecutionException("Execution of pronominal coreference was interrupted.");
                 	}
                 
                     Annotation pronounAnn = itr.next();
                     Long prStart = pronounAnn.getStartNode().getOffset();
                     Long prEnd = pronounAnn.getEndNode().getOffset();
 
                     boolean inSameSentence = (currStart >= currSentenceStart && prEnd <= currSentenceEnd);
 
                     // Don't process this anaphor if it occurs within a defined exclusion zone
                     if (isInExclusionRegion(inputAS, prStart, prEnd)) {
                         continue;
                     }
 
                     FeatureMap pronounFeats = pronounAnn.getFeatures();
                     String prType = pronounAnn.getType();
                     String prNumber = (String) pronounFeats.get("number");
                     String prCase = (String) pronounFeats.get("case");
                     String prGender = (String) pronounFeats.get("gender");
                     String prPerson = (String) pronounFeats.get("person");
                     String prMatchPP = null;
                     
                     Set<String> tmpFs = new HashSet<String>();
                     tmpFs.add(backrefIdFeature);
                     AnnotationSet tmpAS = outputAS.get(prStart, prEnd).get(currType, tmpFs);
 
 					if (! tmpAS.isEmpty()) {
 						Annotation tmpAnn = tmpAS.iterator().next();
 						FeatureMap tmpFeats = tmpAnn.getFeatures();
                     	// Don't reprocess first or second person pronouns that were linked on a previous pass
                     	prMatchPP = (String)tmpFeats.get("matchPP");
 						if (prMatchPP != null) {
 							continue;
 						}
 					}
 					
 					// Might want to think about whether we want to force a case match for non-Person pronouns, maybe make it an option
 					caseMatch = prCase.contains(currCase);
 					
                     try {
                         // Primarily concerned with gender and case match for personal pronouns
                         if (cls.equals("Person")) {
                             genderMatch = false;
                             //contextMatch = false;
                             personMatch = false;
                             caseMatch = false;
                             if (isInQuotedText(inputAS, prStart, prEnd)) {
                                 prInQuotedText = true;
                             }
                             if (isInQuotedText(inputAS, currStart, currEnd)) {
                                 currInQuotedText = true;
                             }
                             if ((prGender != null && currGender == null)
                                     || (prGender != null && currGender != null && currGender.equals("either"))
                                     || (prGender != null && currGender != null && (prGender.equals(currGender) || prGender.equals("either")))) {
                                 genderMatch = true;
                             } 
                             // Pronouns in quoted text don't corefer with Persons outside the quote unless certain rules are met
                             if (currInQuotedText && !prInQuotedText) { // antecedent is in quote and anaphor is after the quote; need definite gender and person match
                                 if (currPerson.equals("first") && prGender != null && currGender != null && prGender.equals(currGender)) {
                                     contextMatch = true;
                                     inQuotedText = true;
                                 } 
                             } else if (prInQuotedText && !currInQuotedText) {
                                 if (currEnd < prStart) { // anaphor is in quote and antecedent is before the quote
                                     inQuotedText = true;
                                     if ((quotedPP3rd == Protagonist.FirstPersonMentioned && isFirstPerson != null)
                                             || (quotedPP3rd == Protagonist.MostFrequentPersonMentioned && isMostFrequentPerson != null)
                                             || (quotedPP3rd == Protagonist.PreviousPersonMentioned)
                                             || prPerson.equals("first")) {
                                         contextMatch = true;
                                     }
                                 }
                             }
                             
                             if (!genderCorefAmbiguity || (inQuotedText && contextMatch) || forceCaseMatch || prCase.contains(currCase) || prCase.matches("possessive|reflexive")) {
                                 caseMatch = true;
                             } 
                             if (prPerson.equals(currPerson) || (inQuotedText && (prPerson.equals("first") || currPerson.equals("first"))) || (matchPP != null && matchPP.equals(prPerson))) {
                                 personMatch = true;
                             } 
                         }
                      
                         if (prType.equals(relativePronoun)) { // relative pronoun must be in same sentence with no intervening mentions of the same type
                             if (!inSameSentence || hasMentionInRange(inputAnns, currEnd, prStart, null)) {
                                 proximityMatch = false;
                             }
                         }
                         if (currNumber.equals(prNumber) || prNumber.equals("either") || (inQuotedText && prPerson != null && prPerson.equals("first") && prNumber.equals("plural"))) {
                             numberMatch = true;
                         }
                         // For Things, there must be no other of the same number, between candidate antecedent and pronoun
                         if (cls.equals("Thing")) {
                             FeatureMap numberFilter = Factory.newFeatureMap();
                             numberFilter.put("number", currNumber);
                             if (hasMentionInRange(inputAnns, currEnd, prStart, numberFilter)) {
                                 proximityMatch = false;
                             }
                         }
 
                         // TODO - if we have Thing1... Thing2 IN PRN then Thing2 does not coref with PRN but Thing1 does
                         if (proximityMatch && genderMatch && caseMatch && personMatch && numberMatch) {
                             matchedPair = true;
                         }
                     } catch (NullPointerException ne) {
                         // feature absent or null
                         // System.out.println("NPE for pronoun: " + pronounAnn + " and antecedent: " + curr);
                     }
 
                     if (matchedPair) {
                         try {
                             // Create a feature map to hold a copy of the antecedent features
                             FeatureMap feats = Factory.newFeatureMap();
                             // TODO - if there was already a lower-weighted coref back to a different antecedent, override it
                             Set<String> fs = new HashSet<String>();
                             fs.add(backrefIdFeature);
                             AnnotationSet tempAS = outputAS.get(prStart, prEnd).get(currType, fs);
 
                             if (tempAS.isEmpty()) {
                                 anaphorId = outputAS.add(prStart, prEnd, currType, feats);
                             } else {
                                 Annotation tmp = tempAS.iterator().next();
                                 anaphorId = tmp.getId();
                                 feats = tmp.getFeatures();
                                 // remove added features
                                 feats.remove("person");
                                 feats.remove("number");
                                 feats.remove("case");
                                 feats.remove("isFirst");
                                 feats.remove("isLast");
                                 feats.remove("isMostFrequent");
                                 int backRefId = (Integer) feats.get(backrefIdFeature);
                                 Annotation prevAnt = outputAS.get(backRefId);
                                 if (prevAnt != null) {
                                     FeatureMap prevAntFeats = prevAnt.getFeatures();
                                     prevAntFeats.remove(corefIdFeature);
                                     prevAntFeats.remove(corefTextFeature);
                                 }
                             }
 
                             // Propagate the back reference text to the first antecedent matched
                             feats.put(backrefTextFeature, currString);
                             feats.putAll(currFeats);
                             // Mark the back reference id of the current antecedent
                             feats.put(backrefIdFeature, currId);
 
                             // remove coref features copied from antecedent
                             feats.remove(corefIdFeature);
                             feats.remove(corefTextFeature);
 
                             // Inherit the pronoun's gender if the antecedent had no gender or ambiguous gender
                             if ((currGender == null || currGender.equals("either")) && prGender != null && !prGender.equals("either")) {
                                 feats.put("gender", prGender);
                             }
                             // inherit pronoun's person if not null
                             if (prPerson != null) {
                                 feats.put("person", prPerson);
                             }
 
 
                             // Mark the coref on the antecedent
                             currFeats.put(corefIdFeature, anaphorId);
                             currFeats.put(corefTextFeature, pronounFeats.get("string"));		// coreferent text
 
                             Annotation newAntecedent = outputAS.get(anaphorId);
                             inputAnns.add(newAntecedent);
                             curr = newAntecedent;
                         } catch (InvalidOffsetException ie) {
                             // shouldn't happen
                             // System.out.println("InvalidOffset for: " + pronounAnn);
                         } finally {
                             itr.remove();
                             break;
                         }
                     } // end if matchedPair
                 } // end for
 
                 if (inputAnns.iterator().hasNext()) {
                     Annotation next = inputAnns.iterator().next();
                     if (!matchedPair) {
                         curr = next;        // move top iterator onto the next mention
                     }
                 }
             } // end while
         } // end for over set of annotations
 
         fireProcessFinished();
     } // end execute()
 
     @Override
     public void cleanup() {
         Factory.deleteResource(japeTransducer);
     }
     
     @Override
     public void progressChanged(int i) {
         fireProgressChanged(i);
     }
 
     @Override
     public void processFinished() {
         fireProcessFinished();
     }
     
 
     @Optional
     @RunTime
     @CreoleParameter(comment = "Input Annotation Set Name")
     public void setInputASName(String inputASName) {
         this.inputASName = inputASName;
     }
 
     public String getInputASName() {
         return inputASName;
     }
 
     @Optional
     @RunTime
     @CreoleParameter(comment = "Output Annotation Set Name")
     public void setOutputASName(String outputASName) {
         this.outputASName = outputASName;
     }
 
     public String getOutputASName() {
         return outputASName;
     }
 
     @CreoleParameter(defaultValue = "PRN",
     comment = "Annotation name for non-personal pronouns")
     public void setPronoun(String pronoun) {
         this.pronoun = pronoun;
     }
 
     public String getPronoun() {
         return pronoun;
     }
 
     @CreoleParameter(defaultValue = "WHICH",
     comment = "Annotation name for relative pronouns")
     public void setRelativePronoun(String relativePronoun) {
         this.relativePronoun = relativePronoun;
     }
 
     public String getRelativePronoun() {
         return relativePronoun;
     }
 
     @CreoleParameter(defaultValue = "resources/org_city_pronoun_annotator.jape",
     comment = "Location of main JAPE file")
     public void setJapeURL(URL japeURL) {
         this.japeURL = japeURL;
     }
 
     public URL getJapeURL() {
         return japeURL;
     }
 
     @RunTime
     @CreoleParameter(comment = "List of Location-type annotations for here, there, where coreference")
     public void setLocationTypes(ArrayList<String> locationTypes) {
         this.locationTypes = locationTypes;
     }
 
     public ArrayList<String> getLocationTypes() {
         return locationTypes;
     }
 
     @RunTime
     @CreoleParameter(comment = "List of other annotations for pronominal coreference")
     public void setOtherTypes(ArrayList<String> otherTypes) {
         this.otherTypes = otherTypes;
     }
 
     public ArrayList<String> getOtherTypes() {
         return otherTypes;
     }
 
     @RunTime
     @CreoleParameter(comment = "List of Person-type annotations for personal pronoun coreference")
     public void setPersonTypes(ArrayList<String> personTypes) {
         this.personTypes = personTypes;
     }
 
     public ArrayList<String> getPersonTypes() {
         return personTypes;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = ANNIEConstants.TOKEN_ANNOTATION_TYPE,
     comment = "Name of Token annotation")
     public void setTokenName(String tokenName) {
         this.tokenName = tokenName;
     }
 
     public String getTokenName() {
         return tokenName;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "VG",
     comment = "VerbGroup Chunker annotation name")
     public void setVerbGroupName(String verbGroupName) {
         this.verbGroupName = verbGroupName;
     }
 
     public String getVerbGroupName() {
         return verbGroupName;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = ANNIEConstants.SENTENCE_ANNOTATION_TYPE,
     comment = "Sentence annotation name")
     public void setSentenceName(String sentenceName) {
         this.sentenceName = sentenceName;
     }
 
     public String getSentenceName() {
         return sentenceName;
     }
 
     @Optional
     @RunTime
     @CreoleParameter(defaultValue = "QuotedText",
     comment = "Annotation that holds quoted text")
     public void setQuotedTextName(String quotedTextName) {
         this.quotedTextName = quotedTextName;
     }
 
     public String getQuotedTextName() {
         return quotedTextName;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "backRefId", comment = "Feature name for antecedent back reference id")
     public void setBackrefIdFeature(String backrefIdFeature) {
         this.backrefIdFeature = backrefIdFeature;
     }
 
     public String getBackrefIdFeature() {
         return backrefIdFeature;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "backRefText", comment = "Feature name for antecedent back reference text")
     public void setBackrefTextFeature(String backrefTextFeature) {
         this.backrefTextFeature = backrefTextFeature;
     }
 
     public String getBackrefTextFeature() {
         return backrefTextFeature;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "corefId", comment = "Feature name for coreference id")
     public void setCorefIdFeature(String corefIdFeature) {
         this.corefIdFeature = corefIdFeature;
     }
 
     public String getCorefIdFeature() {
         return corefIdFeature;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "corefText", comment = "Feature name for coreferring text")
     public void setCorefTextFeature(String corefTextFeature) {
         this.corefTextFeature = corefTextFeature;
     }
 
     public String getCorefTextFeature() {
         return corefTextFeature;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "2", comment = "Maximum sentence distance between antecedent and anaphor")
     public void setMaxSentenceDistance(Integer maxSentenceDistance) {
         this.maxSentenceDistance = maxSentenceDistance;
     }
 
     public Integer getMaxSentenceDistance() {
         return maxSentenceDistance;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "false", comment = "Should definite descriptors be pronominally coreferenced?")
     public void setCorefSortalMentions(Boolean corefSortalMentions) {
         this.corefSortalMentions = corefSortalMentions;
     }
 
     public Boolean getCorefSortalMentions() {
         return corefSortalMentions;
     }
 
     @Optional
     @RunTime
     @CreoleParameter(comment = "Don't attempt to coreference terms that are within these annotations")
     public void setExcludeIfWithin(ArrayList<String> excludeIfWithin) {
         this.excludeIfWithin = excludeIfWithin;
     }
 
     public ArrayList<String> getExcludeIfWithin() {
         return excludeIfWithin;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "None",
     comment = "Link first person pronouns to a protagonist")
     public void setPersonPronoun1st(Protagonist personPronoun1st) {
         this.personPronoun1st = personPronoun1st;
     }
 
     public Protagonist getPersonPronoun1st() {
         return personPronoun1st;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "None",
     comment = "Link second person pronouns to a protagonist")
     public void setPersonPronoun2nd(Protagonist personPronoun2nd) {
         this.personPronoun2nd = personPronoun2nd;
     }
 
     public Protagonist getPersonPronoun2nd() {
         return personPronoun2nd;
     }
 
     @RunTime
     @CreoleParameter(defaultValue = "PreviousPersonMentioned",
     comment = "Link third person pronouns in quoted text to a protagonist")
     public void setQuotedPP3rd(Protagonist quotedPP3rd) {
         this.quotedPP3rd = quotedPP3rd;
     }
 
     public Protagonist getQuotedPP3rd() {
         return quotedPP3rd;
     }
 
     public URL getConfigFileURL() {
         return configFileURL;
     }
 
     @CreoleParameter(defaultValue = "resources/config.txt",
     comment = "Location of configuration file")
     public void setConfigFileURL(URL configFileURL) {
         this.configFileURL = configFileURL;
     }
 }
