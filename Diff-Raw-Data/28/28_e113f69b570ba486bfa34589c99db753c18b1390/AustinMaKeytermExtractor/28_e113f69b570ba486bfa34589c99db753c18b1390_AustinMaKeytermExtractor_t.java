 package edu.cmu.lti.oaqa.openqa.test.team04.keyterm;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 
 public class AustinMaKeytermExtractor extends AbstractKeytermExtractor {
 
   @Override
   public void initialize(UimaContext c) throws ResourceInitializationException {
     super.initialize(c);
   }
 
   @Override
   protected List<Keyterm> getKeyterms(String text) {
    Map<Integer, Integer> geneSpans = new HashMap<Integer, Integer>(); // ner.getGeneSpans(text);
    List<Keyterm> keyTerms = new ArrayList<Keyterm>();

    for (Entry<Integer, Integer> entry : geneSpans.entrySet()) {
      int start = entry.getKey();
      int end = entry.getValue();
      String term = text.substring(start, end);
      keyTerms.add(new Keyterm(term));
     }
    return keyTerms;
   }
 }
