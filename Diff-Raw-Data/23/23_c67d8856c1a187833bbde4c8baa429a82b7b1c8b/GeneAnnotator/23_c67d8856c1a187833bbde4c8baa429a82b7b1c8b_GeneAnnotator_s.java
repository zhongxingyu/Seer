 package com.orii.uima.annotator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.cas.FSIndex;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.util.AbstractExternalizable;
 import com.orii.uima.Gene;
 import com.orii.uima.Sentence;
 
 /**
  * Annotates {@link com.orii.uima.Gene}s given {@link com.orii.uima.Sentence}s.
  * Uses the LingPipe Chunker.
  * 
  * @author Naoki Orii
  */
 public class GeneAnnotator extends JCasAnnotator_ImplBase {
   
   public static final String PARAM_MODELFILE = "ModelFile";
   
   private Chunker chunker;
   
   @Override
   public void initialize(UimaContext aContext) throws ResourceInitializationException {
     super.initialize(aContext);
     
    File modelFile = new File(((String) getContext().getConfigParameterValue(PARAM_MODELFILE)).trim());
    
    if (!modelFile.exists()) {
      throw new ResourceInitializationException();
    }
    
     try {
       chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
     } catch (IOException e) {
      throw new ResourceInitializationException(e);
     } catch (ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
     }
   }
 
   @Override
   public void process(JCas aJCas) throws AnalysisEngineProcessException {
     aJCas.getDocumentText();
     FSIndex sentenceIndex = aJCas.getAnnotationIndex(Sentence.type);
     Iterator sentenceIter = sentenceIndex.iterator();
     
     while (sentenceIter.hasNext()) {
       Sentence sentence = (Sentence) sentenceIter.next();
       String id = sentence.getSentenceID();
       String rawString = sentence.getRawString();
       
       Chunking chunking = chunker.chunk(rawString);
       
       for (Chunk chunk : chunking.chunkSet()) {
         Gene gene = new Gene(aJCas);
         gene.setSentenceID(id);
 
         String geneRawString = rawString.substring(chunk.start(), chunk.end());
         gene.setRawString(geneRawString);
 
         int geneLocation = chunk.start();
         // Start offset is the number of NON-whitespace characters in the sentence
         // preceding the first character of the mention
         int countWhitespaceBefore = StringUtils.countMatches(rawString.substring(0, geneLocation), " ");
         // End offset is the number of NON-whitespace characters in the sentence
         // preceding the last character of the mention
         int countWhitespaceIn = StringUtils.countMatches(geneRawString, " ");
         gene.setBegin(chunk.start() - countWhitespaceBefore);
         gene.setEnd(chunk.end() - 1 - countWhitespaceBefore - countWhitespaceIn);
         
         gene.addToIndexes();
       }
     }
   }
 }
