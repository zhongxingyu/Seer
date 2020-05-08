 package vu.kafkybot;
 
 import eu.kyotoproject.kaf.*;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kyoto
  * Date: 2/3/13
  * Time: 2:43 PM
  * To change this template use File | Settings | File Templates.
  */
 public class KafKybot {
 
 
     static public void main (String[] args) {
         HashMap<String, ArrayList<KafResult>> kafResultOverviewMap = new HashMap<String, ArrayList<KafResult>>();
         String pathToKafFile = "/Code/vu/kyotoproject/vu.kafkybot.KafKybot/release/kafkybot.v.0.1/example/bus-accident.ont.dep.kaf";
         String extension = "";
         boolean overview = false;
         String pathToProfiles = "/Code/vu/kyotoproject/vu.kafkybot.KafKybot/release/kafkybot.v.0.1/profiles/profiles.txt";
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
             if ((arg.equals("--kaf-file")) && args.length>i+1) {
                 pathToKafFile = args[i+1];
             }
             else if ((arg.equals("--profiles")) && args.length>i+1) {
                 pathToProfiles = args[i+1];
             }
             else if ((arg.equals("--extension")) && args.length>i+1) {
                 extension = args[i+1];
             }
             else if (arg.equals("--overview")) {
                 overview = true;
             }
         }
         File kafFile = new File(pathToKafFile);
         if (kafFile.isDirectory()) {
            ArrayList<String> files = Util.makeRecursiveFileListAll(pathToKafFile, extension);
             for (int i = 0; i < files.size(); i++) {
                 File file = new File(files.get(i));
                 if (!file.isDirectory()) {
                     try {
                         FileOutputStream fos = new FileOutputStream(file.getAbsolutePath()+".tpl");
                         HashMap<String, ArrayList<KafResult>> kafResultMap = ApplyProfilesToKafFile(file, pathToProfiles);
                        SerializeKafResults.writeMapToStream(kafResultMap, fos);
                        fos.close();
                        if (overview) {
                             kafResultOverviewMap.putAll(kafResultMap);
                         }
                     } catch (IOException e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         }
         else {
                 kafResultOverviewMap = ApplyProfilesToKafFile(kafFile, pathToProfiles);
                 //System.out.println("kafResultOverviewMap = " + kafResultOverviewMap.size());
                 SerializeKafResults.writeMapToStream(kafResultOverviewMap, System.out);
         }
       //  System.out.println("FINAL kafResultMap.size() = " + kafResultMap.size());
 
         try {
             if (overview) {
                 FileOutputStream fos = new FileOutputStream(kafFile+"-"+new File(pathToProfiles).getName()+".overview.xls");
                 KafResultOverview.makeOverviewFile(kafResultOverviewMap, fos);
                 fos.close();
             }
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /**
      * @TODO Sentence based processing should be extended to cover any range sentence
      * @param pathToKafFile
      * @param pathToProfiles
      * @return
      */
 
 
     static public HashMap<String, ArrayList<KafResult>> ApplyProfilesToKafFile (File pathToKafFile, String pathToProfiles) {
         HashMap<String, ArrayList<KafResult>> kafResultMap = new HashMap<String, ArrayList<KafResult>>();
         ArrayList<Profile> profiles = ProfileReader.readProfiles(pathToProfiles);
         // System.out.println("profiles.size() = " + profiles.size());
 
         if (pathToKafFile.exists()) {
             int nResults = 0;
             ArrayList<KafResult> tuples = new ArrayList<KafResult>();
             KafSaxParser kafSaxParser = new KafSaxParser();
            // System.out.println("pathToKafFile = " + pathToKafFile.getName());
             kafSaxParser.parseFile(pathToKafFile);
 
 
             /// We process the kaf file sentence by sentence
             Set keySet = kafSaxParser.SentenceToTerm.keySet();
             Iterator keys = keySet.iterator();
             while (keys.hasNext()) {
                 String sentenceId = (String) keys.next();
                 ArrayList<KafResult> sentenceResults = new ArrayList<KafResult>();
                 ArrayList<String> termIds = kafSaxParser.SentenceToTerm.get(sentenceId);
                 ArrayList<String> tokenIds = kafSaxParser.SentenceToWord.get(sentenceId);
                 String comment = "";
                 for (int i = 0; i < tokenIds.size(); i++) {
                     String tokenId = tokenIds.get(i);
                     KafWordForm kafWordForm = kafSaxParser.getWordForm(tokenId);
                     comment += kafWordForm.getWf()+" ";
                 }
                // System.out.println("comment = " + comment);
                 ///// all profiles are applied to each sentence
                 for (int p = 0; p < profiles.size(); p++) {
                     Profile profile = profiles.get(p);
                    // System.out.println("\nPROFILE = " + profile.toString()+"\n");
                     if (profile.getOrder().equals("free")) {
                        // sentenceResults = processAnyOrderSentence(kafSaxParser, profile, sentenceId, termIds, comment);
                     }
                     else if (profile.getOrder().equals("fixed")) {
                         sentenceResults = processInOrderSentence(kafSaxParser, profile, sentenceId, termIds, comment);
                     }
                     else {
                         sentenceResults = processInOrderSentence(kafSaxParser, profile, sentenceId, termIds, comment);
                     }
                    // System.out.println("sentenceResults = " + sentenceResults.size());
                     for (int i = 0; i < sentenceResults.size(); i++) {
                         KafResult kafResult = sentenceResults.get(i);
                         nResults++;
                         kafResult.setDateAndTime(kafSaxParser, "event");
                         kafResult.setId(new Integer(nResults).toString());
                         tuples.add(kafResult);
                     }
                 }/// end of for profiles
             } /// end of sentenceIds
            // System.out.println("pathToKafFile = " + pathToKafFile);
            // System.out.println("tuples.size() = " + tuples.size());
             kafResultMap.put(pathToKafFile.getName(), tuples);
            // System.out.println("kafResultMap.size() = " + kafResultMap.size());
             //writeKafToStream(tuples, System.out, pathToKafFile.getName());
         }
         return kafResultMap;
     }
 
     static boolean depRelationMatch (KafDep kafDep, TupleElement element) {
         boolean match = false;
         String relation = element.getDepRelation();
         if (relation.endsWith("*")) {
            relation = relation.substring(0, relation.length()-1);
            match = kafDep.getRfunc().startsWith(relation);
         }
         else {
             match = kafDep.getRfunc().equals(relation);
         }
         return match;
     }
 
     static boolean checkDependencyTargets (KafSaxParser kafSaxParser, ArrayList<TupleElement> tupleElements) {
         for (int i = 0; i < tupleElements.size(); i++) {
             TupleElement element = tupleElements.get(i);
             if (!element.getDepTo().isEmpty()) {
 /*
                 System.out.println("element.getDepTo() = " + element.getDepTo());
                 System.out.println("element.getDepRelation() = " + element.getDepRelation());
 */
                 boolean hasTarget = false;
                 if (kafSaxParser.TermToDeps.containsKey(element.getMention())) {
                     ArrayList<KafDep> deps = kafSaxParser.TermToDeps.get(element.getMention());
                     for (int j = 0; j < tupleElements.size(); j++) {
                         if (j!=i) {
                             TupleElement tupleElement = tupleElements.get(j);
                             if (tupleElement.getProfileId().equals(element.getDepTo())) {
                                 for (int k = 0; k < deps.size(); k++) {
                                     KafDep kafDep = deps.get(k);
                                     if (kafDep.getFrom().equals(tupleElement.getMention()) ||
                                          kafDep.getTo().equals(tupleElement.getMention())) {
 /*
                                         System.out.println("kafDep.getFrom() = " + kafDep.getFrom());
                                         System.out.println("kafDep.getTo() = " + kafDep.getTo());
                                         System.out.println("kafDep.getRfunc() = " + kafDep.getRfunc());
 */
                                         if (depRelationMatch(kafDep,element)) {
                                             hasTarget = true;
                                             break;
                                         }
                                     }
                                 }
                             }
                         }
                         if (hasTarget) {
                             break;
                         }
                     }
                     if (!hasTarget) {
                         return false;
                     }
                 }
             }
         }
         return true;
     }
 
     static ArrayList<KafResult> processInOrderSentence (KafSaxParser kafSaxParser,
                                                         Profile profile,
                                                         String sentenceId,
                                                         ArrayList<String> termIds,
                                                         String comment) {
         ArrayList<KafResult> tuples = new ArrayList<KafResult>();
       //  System.out.println("termIds.size() = " + termIds.size());
         ArrayList<TermProfile> termProfiles = profile.getTermProfiles();
         int nRequiredMatches = 0;
         for (int i = 0; i < termProfiles.size(); i++) {
             TermProfile termProfile = termProfiles.get(i);
             if (!termProfile.getType().equalsIgnoreCase("stop")) {
                 nRequiredMatches++;
             }
         }
         ResultTree resultTree = new ResultTree();
         processTermProfile(kafSaxParser,
                 termIds, 0, termIds.size(), termProfiles, 0, resultTree);
         ArrayList<ArrayList<TupleElement>> tupleElements = resultTree.getTupleElements();
       //  System.out.println("final resultTree="+resultTree.printTree(0));
       //  System.out.println("tupleElements.size() = " + tupleElements.size());
         for (int i = 0; i < tupleElements.size(); i++) {
             ArrayList<TupleElement> result = tupleElements.get(i);
            // System.out.println("result.size() = " + result.size());
 
             /// we need to check the dependencies separately because they are relations between results.
             /// we first need to have all the result elements to be able to test it
             if ((nRequiredMatches==result.size()) && checkDependencyTargets(kafSaxParser, result)) {
                 //// pattern is valid!
                 KafResult kafResult = new KafResult(profile.getName());
                 if (profile.getConfidence()>0) {
                     kafResult.setProfileConfidence(profile.getConfidence());
                 }
                 kafResult.setComment(comment);
                 kafResult.setSentenceId(sentenceId);
                 for (int j = 0; j < result.size(); j++) {
                     TupleElement element = result.get(j);
                     if (!element.getName().equals("skip")) {
                         kafResult.addChildren(element);
                     }
                 }
                 if (!Util.hasTuple(tuples, kafResult)) {
                     tuples.add(kafResult);
                 }
                 //  System.out.println("tuples = " + tuples.size()+"\n");
             }
             else {
                 /// the patterns was not valid.
             }
         }
         return tuples;
     }
 
     static void processTermProfile (KafSaxParser kafSaxParser,
                                     ArrayList<String> termIds,
                                     int termStartPosition,
                                     int termEndPosition,
                                     ArrayList<TermProfile> termProfiles,
                                     int profileId,
                                     ResultTree parentNode) {
         String tab = "";
         for (int i = 0; i < profileId; i++) {
              tab+="  ";
         }
         TermProfile termProfile = termProfiles.get(profileId);
      //   System.out.println("\n"+tab+profileId+": termProfile.toString() = " + termProfile.toString());
      //   System.out.println(tab+"termStartPosition = " + termStartPosition);
      //   System.out.println(tab+"termEndPosition = " + termEndPosition);
         boolean match = false;
         for (int j = termStartPosition; j < termEndPosition; j++) {
             String termId = termIds.get(j);
        //     System.out.println(tab+"termId = " + termId);
             KafTerm kafTerm = kafSaxParser.getTerm(termId);
        //     System.out.println(tab+"kafTerm.getLemma() = " + kafTerm.getLemma());
             if (termProfile.hasMatchingProperties(kafSaxParser, kafTerm)) {
                 match = true;
                 if (termProfile.getType().equalsIgnoreCase("stop")) {
        //             System.out.println(tab+"NEGATIVE MATCH: "+kafTerm.getTid()+":"+kafTerm.getLemma());
                     termEndPosition = j;
                     // we restrict the end but not the start,
                     // the next profile should run from current start position
                     // and not the negated element
        //             System.out.println(tab+"limiting termEndPosition for the next term profile = " + termEndPosition);
                     int nextProfile = profileId+1;
                     if (nextProfile<termProfiles.size()) {
                         processTermProfile(kafSaxParser,
                                 termIds,
                                 termStartPosition,
                                 termEndPosition,
                                 termProfiles,
                                 nextProfile,
                                 parentNode);
                     }                }
                 else {
                     ResultTree childNode = new ResultTree();
                     termStartPosition = j+1;
                    // System.out.println(tab+"POSITIVE MATCH: "+kafTerm.getTid()+":"+kafTerm.getLemma());
                     TupleElement element = new TupleElement(termProfile.getType(), kafTerm, termProfile);
                     childNode.setElement(element);
                     parentNode.addChild(childNode);
                     int endPosition = termEndPosition;
                     if (termProfile.getNext()>0) {
                        endPosition = j+termProfile.getNext();
                         if (endPosition>termIds.size()) {
                             endPosition = termIds.size();
                         }
                     //    System.out.println("\tprofile:"+profileId+" restricted the termEndPosition to " + endPosition);
                     }
                     int nextProfile = profileId+1;
                     if (nextProfile<termProfiles.size()) {
                         processTermProfile(kafSaxParser,
                                 termIds,
                                 termStartPosition,
                                 endPosition,
                                 termProfiles,
                                 nextProfile,
                                 childNode);
                     }
                 }
 
             }
             else {
                 ////// this is not a good term
             }
         }
 
         if ((!match) && (termProfile.getType().equalsIgnoreCase("stop"))) {
             int nextProfile = profileId+1;
             if (nextProfile<termProfiles.size()) {
                 processTermProfile(kafSaxParser,
                         termIds,
                         termStartPosition,
                         termEndPosition,
                         termProfiles,
                         nextProfile,
                         parentNode);
             }
         }
        // System.out.println("children parentNode = " + parentNode.getChildren().size());
        // System.out.println(parentNode.printTree(0));
     }
 
 
     static public void writeKafToStream(ArrayList<KafResult> events, OutputStream stream, String fileName)
     {
         try
         {
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             DocumentBuilder builder = factory.newDocumentBuilder();
             DOMImplementation impl = builder.getDOMImplementation();
 
             Document xmldoc = impl.createDocument(null, "tuples", null);
             xmldoc.setXmlStandalone(true);
             Element root = xmldoc.getDocumentElement();
             root.setAttribute("source", fileName);
             for (int i = 0; i < events.size(); i++) {
                 KafResult kafResult = events.get(i);
                 root.appendChild(kafResult.toXML(xmldoc));
             }
             // Serialisation through Tranform.
             DOMSource domSource = new DOMSource(xmldoc);
             TransformerFactory tf = TransformerFactory.newInstance();
             //tf.setAttribute("indent-number", 4);
             Transformer serializer = tf.newTransformer();
             serializer.setOutputProperty(OutputKeys.INDENT,"yes");
             serializer.setOutputProperty(OutputKeys.ENCODING,"UTF-8");
             serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
             // serializer.setParameter("format-pretty-print", Boolean.TRUE);
             serializer.setOutputProperty(OutputKeys.INDENT, "yes");
             serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
 
             StreamResult streamResult = new StreamResult(new OutputStreamWriter(stream,"UTF-8"));
             serializer.transform(domSource, streamResult);
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
     }
 
 
 }
 
