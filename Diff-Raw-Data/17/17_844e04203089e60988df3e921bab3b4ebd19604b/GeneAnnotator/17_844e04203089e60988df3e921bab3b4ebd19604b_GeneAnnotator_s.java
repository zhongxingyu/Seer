 package hw1.kmandavi;
 
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 
 import hw1.kmandavi.sentence;
 
 import hw1.kmandavi.GeneTag;
 
 import hw1.kmandavi.PosTagNamedEntityRecognizer;
 
 public class GeneAnnotator extends JCasAnnotator_ImplBase {
   
 
   @Override
   public void process(JCas aJCas) throws AnalysisEngineProcessException {
     // TODO Auto-generated method stub
     // get document text
     JCas jcas = aJCas;
     
     String sentenceIdentifier = "";
     String sentenceText = "";
     System.out.println("Annotating Genes");
     PosTagNamedEntityRecognizer Tagger = null;
     try {
       Tagger = new PosTagNamedEntityRecognizer();
     } catch (ResourceInitializationException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     CSVToHashMap genes = new CSVToHashMap();
     
 
     // retrieve the filename of the input file from the CAS
     FSIterator it = jcas.getAnnotationIndex(sentence.type).iterator();
     while (it.hasNext()) {
 
       sentence annotation = (sentence) it.next();
       sentenceIdentifier = annotation.getIdentifier();
       sentenceText = annotation.getText();
       Map<Integer, Integer> occurences = Tagger.getGeneSpans(sentenceText);
       int begin;
       int end;
       String gene;
       for (Map.Entry<Integer, Integer> entry : occurences.entrySet())
       {
           begin = entry.getKey();
           end = entry.getValue();
           gene = sentenceText.substring(begin, end);
           if(genes.findGene(gene) == true ){
             GeneTag ann = new GeneTag(jcas);
             ann.setBegin(begin);
             ann.setEnd(end);
             ann.setIdentifier(sentenceIdentifier);
             ann.setName(gene);
             ann.addToIndexes();
           }
       }
       
     }       
     
   }
 
 }
