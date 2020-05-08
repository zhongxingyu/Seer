 package cybion.annotator_utils.annotator;
 
 import cybion.uima.ts.ValueAnnotation;
 import org.apache.uima.SentenceAnnotation;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 
import java.util.ArrayList;
import java.util.List;

 public class ValueAnnotator extends JCasAnnotator_ImplBase {
 
  private static final String[] currencies = {"Euro", "euro", "Dollar", "dollar", "dollars",
          "Dollars", "Pound", "Pounds", "pound", "pounds", "$", "£", "€"};
 
   private List<SentenceAnnotation> sentenceCandidates;
 
   @Override
   public void initialize(UimaContext aContext) throws ResourceInitializationException {
     this.sentenceCandidates = new ArrayList<SentenceAnnotation>();
     super.initialize(aContext);
   }
 
   @Override
   public void process(JCas aJCas) throws AnalysisEngineProcessException {
 
     String document = aJCas.getDocumentText();
     // iterate over currencies
     for (String currencyWord : currencies) {
       boolean finished = false;
       String copyOfDoc = new String(document);
       String dummyWord = buildDummyWord(currencyWord);
       while (!finished) {
         if (copyOfDoc.contains(currencyWord)) {
           ValueAnnotation valueAnnotation = new ValueAnnotation(aJCas);
           valueAnnotation.setBegin(copyOfDoc.indexOf(currencyWord));
           valueAnnotation.setEnd(copyOfDoc.indexOf(currencyWord) + currencyWord.length());
           valueAnnotation.addToIndexes();
           copyOfDoc = copyOfDoc.replace(currencyWord, dummyWord);
         } else {
           finished = true;
         }
       }
     }
 
   }
 
   private String buildDummyWord(String currencyWord) {
     StringBuffer dummyWord = new StringBuffer();
     for (int i = 0; i < currencyWord.length(); i++) {
       dummyWord.append("X");
     }
     return dummyWord.toString();
   }
 
   @Deprecated
   public void oldProcess(JCas cas) throws AnalysisEngineProcessException {
     // find if some currency is defined inside the doc
     List<String> currenciesFound = new ArrayList<String>();
     int numberOfCurrs = 0;
 
     for (String currency : currencies) {
       if (cas.getDocumentText().contains(currency)) {
         numberOfCurrs++;
         currenciesFound.add(currency);
       }
     }
 
     String doc = cas.getDocumentText();
 
     // find the sentence containing each currency
     for (String currencyFound : currenciesFound) {
 
       int index = doc.indexOf(currencyFound);
       FSIterator<Annotation> it = cas.getAnnotationIndex(SentenceAnnotation.type).iterator();
 
       while (it.hasNext()) {
         SentenceAnnotation sentence = (SentenceAnnotation) it.next();
         // if a sentence has begin before and end after the index of the found currency then it
         // contains the currency string
         if (sentence.getBegin() < index && sentence.getEnd() > index) {
           this.sentenceCandidates.add(sentence);
           doc = doc.substring(sentence.getEnd()); // parsing the document
         }
       }
 
     }
 
     // TODO: parse the sentence candidates
     for (SentenceAnnotation sentence : sentenceCandidates) {
       System.err.println(sentence.getCoveredText());
     }
 
   }
 
 }
