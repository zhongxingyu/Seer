 package ner;
 
import java.io.File;
 
import ner.types.InputSentence;
 import ner.types.GeneAnnotation;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;
 
 /**
  * Analysis Engine which produces {@link GeneAnnotation} instances from {@link InputSentence} instances
  * by running the HMM chunker. 
  */
 
 public class GeneAnnotator extends JCasAnnotator_ImplBase {
   
   /**
    * HMM chunker instance.
    */
   private Chunker chunker;
   
   /**
    * Initializer for the Analysis Engine.
    * Reads the model from the file provided as a parameter in the descriptor
    * 
    * @args context The UIMA context.
    * @throws ResourceInitializationException when the model file cannot be read.
    */
   public void initialize(UimaContext context) throws ResourceInitializationException {
    String modelFile = (String) context.getConfigParameterValue("model");
     try {
      chunker = (Chunker) AbstractExternalizable.readObject(new File(modelFile));
     } catch (Exception e) {
       throw new ResourceInitializationException(e);
     }
   }
 
   /**
    * Process the CAS to annotate gene mentions in sentences.
    * 
    * @args jcas The Java CAS in which the input sentences are stored.
    */
   @Override
   public void process(JCas jcas) throws AnalysisEngineProcessException {
     FSIterator<Annotation> it = jcas.getAnnotationIndex(InputSentence.type).iterator();
     while(it.hasNext()) {
       InputSentence sentence = (InputSentence) it.next();
       String text = jcas.getDocumentText();
       // Compute the offsets without whitespace and stores them.
       int[] offsetMap = new int[text.length()];
       int offset = 0;
       for(int i = 0; i < text.length(); i++) {
         offsetMap[i] = offset;
         if(!Character.isWhitespace(text.charAt(i)))
           offset++;
       }
       // Calls the chunker to produce annotation.
       Chunking chunking = chunker.chunk(text);
       for(Chunk chunk: chunking.chunkSet()) {
         GeneAnnotation annotation = new GeneAnnotation(jcas);
         annotation.setBegin(offsetMap[chunk.start()]);
         annotation.setEnd(offsetMap[chunk.end()-1]);
         annotation.setSentenceId(sentence.getSentenceId());
         annotation.setName(text.substring(chunk.start(), chunk.end()));        
         annotation.addToIndexes();
       } 
     }
   }
 }
