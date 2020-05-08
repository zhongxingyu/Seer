 package cybion.annotator_utils.annotator;
 
 import cybion.annotator_utils.AnnotatorUtils;
 import cybion.annotator_utils.annotator.exception.UncertainCaseException;
 import cybion.uima.ts.BandoEntity;
 import org.apache.commons.lang.StringUtils;
 import org.apache.uima.SentenceAnnotation;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.Feature;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.util.Level;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * default annotator that first looks for simple patterns to automatically extract features and then
  * lets each subclass implement its own specific algorithm
  *
  * @author tommaso
  */
 public abstract class CybionAbstractAnnotator extends JCasAnnotator_ImplBase {
   private SimpleStructurePatternProvider simpleStructurePatternProvider = new SimpleStructurePatternProvider();
 
   private static final String MORE_THAN_ONE_MATCH = "More than one match of the same key-value structure found";
 
   protected BandoEntity getBando(JCas cas) {
     return AnnotatorUtils.getBandoEntityWithCreation(cas);
   }
 
   @Override
   public void process(JCas jCas) throws AnalysisEngineProcessException {
     boolean extracted = false;
 //    this.getContext().getLogger().log(Level.INFO, "Processing document" + jCas.getDocumentText());
//    jCas.getDocumentText().replaceAll("", "a").replaceAll("", "e").replaceAll("", "o")
//            .replaceAll("", "u").replaceAll("", "i").replaceAll("", "e");
     try {
       extracted = extractWithSimpleStructure(jCas);
     } catch (Exception e) {
       this.getContext().getLogger().log(Level.WARNING,
               "a problem arose trying to extract information with simple structure");
     } finally {
       if (!extracted)
         extractWithCustomAlgorithm(jCas);
     }
   }
 
   private boolean extractWithSimpleStructure(JCas jCas) throws Exception {
     boolean successfullExtraction = false;
     String documentText = jCas.getDocumentText();
     // iterate over patterns keys to find "key : value" structures
     for (String key : getFeatureKeys(jCas.getDocumentLanguage())) {
       // look first if the key appears in the document text
       if (StringUtils.containsIgnoreCase(documentText, key)) {
         // if it appears then count the matches
         int countMatches = StringUtils.countMatches(documentText, key);
         // build the pattern
         Pattern p = simpleStructurePatternProvider.buildPattern(key);
         if (countMatches == 1) {
           /*
            * if there is only one match then look for the pattern structure inside the sentence
            * containing the key
            */
           SentenceAnnotation sentenceAnnotation = AnnotatorUtils.getSentenceAtPosition(documentText
                   .indexOf(key), jCas);
           Matcher m = p.matcher(sentenceAnnotation.getCoveredText());
           if (m.find()) {
             // it's a good sentence
             BandoEntity bandoEntity = AnnotatorUtils.getBandoEntityWithCreation(jCas);
             Feature feature = getFeature(jCas);
             bandoEntity.setFeatureValue(feature, sentenceAnnotation);
             successfullExtraction = true;
             this.getContext().getLogger().log(
                     Level.INFO,
                     new StringBuilder("feature ").append(feature.getName()).append(
                             " extracted with key - value structure").toString());
             break;
           }
         } else { // more than one match
           this.getContext().getLogger().log(Level.WARNING, MORE_THAN_ONE_MATCH);
           throw new UncertainCaseException(MORE_THAN_ONE_MATCH);
         }
       }
     }
     return successfullExtraction;
   }
 
   protected abstract Feature getFeature(JCas cas) throws CASException;
 
   protected abstract String[] getFeatureKeys(String language);
 
   protected abstract void extractWithCustomAlgorithm(JCas jCas);
 
 }
