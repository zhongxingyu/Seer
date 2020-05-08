 package edu.cmu.lti.oaqa.openqa.test.team15.keyterm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.apache.uima.UimaContext;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.util.AbstractExternalizable;
 
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 import edu.cmu.lti.oaqa.openqa.test.team15.keyterm.expand.KeytermExpander;
 
 public class LuzhengsLingpipeKeytermExtractor extends AbstractKeytermExtractor {
   private KeytermExpander expander1;
 
   private KeytermExpander expander2;
 
  private File modelFile;

   private Logger logger = Logger.getLogger(LuzhengsLingpipeKeytermExtractor.class);
 
   @Override
   public void initialize(UimaContext c) throws ResourceInitializationException {
     super.initialize(c);
     DOMConfigurator configurator = new DOMConfigurator();
     InputStream log4jConfigIn = this.getClass().getClassLoader()
             .getResourceAsStream("configuration/log4j.xml");
     configurator.doConfigure(log4jConfigIn, LogManager.getLoggerRepository());
     String expanderClassName1 = (String) c.getConfigParameterValue("expander1");
     String expanderClassName2 = (String) c.getConfigParameterValue("expander2");
     try {
       @SuppressWarnings("unchecked")
       Class<KeytermExpander> clz1 = (Class<KeytermExpander>) Class.forName(expanderClassName1);
       expander1 = clz1.getConstructor(new Class[] { UimaContext.class }).newInstance(c);
       @SuppressWarnings("unchecked")
       Class<KeytermExpander> clz2 = (Class<KeytermExpander>) Class.forName(expanderClassName2);
       expander2 = clz2.getConstructor(new Class[] { UimaContext.class }).newInstance(c);
     } catch (Exception e) {
      logger.error("", e);
    }
    URL modelFileUrl = this.getClass().getClassLoader()
            .getResource("model/ne-en-bio-genia.TokenShapeChunker");
    try {
      modelFile = new File(modelFileUrl.toURI());
    } catch (URISyntaxException e) {
      logger.error("", e);
     }
   }
 
   @Override
   /*
    * exploit lingpipe to extract gene-related key terms
    * 
    * @see edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor#getKeyterms
    * (java.lang.String)
    */
   protected List<Keyterm> getKeyterms(String question) {

    // File modelFile = new File("src/main/resources/model/ne-en-bio-genia.TokenShapeChunker");

     List<Keyterm> keyterms = new ArrayList<Keyterm>();
    if (modelFile == null) {
      logger.error("model file is null!");
      return keyterms;
    }
 
     String[] questionParts = question.split("\\(|\\)");
     for (String questionPart : questionParts) {
       try {
         Chunker chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
         Chunking chunking = chunker.chunk(questionPart);
         Set<Chunk> chunkSet = chunking.chunkSet();
         Iterator<Chunk> it = chunkSet.iterator();
 
         while (it.hasNext()) {
           Chunk presentChunk = it.next();
           String presentTerm = questionPart.substring(presentChunk.start(), presentChunk.end());
 
           // do term extension
           Set<String> extendStrSet = new HashSet<String>();
           extendStrSet.addAll(expander1.expandKeyterm(presentTerm, "NN"));
           extendStrSet.addAll(expander2.expandKeyterm(presentTerm, "NN"));
           for (String extendedKeyterm : extendStrSet) {
             Keyterm kt = new Keyterm(extendedKeyterm);
             if (extendedKeyterm.equals(presentTerm))
               kt.setProbablity(1f);
             else
               kt.setProbablity(0.6f);
             keyterms.add(kt);
           }
         }
       } catch (ClassNotFoundException e) {
         System.err.println("No definition for the class has been found.");
         e.printStackTrace();
         return null;
       } catch (IOException e) {
         System.err.println("Error reading model files.");
         e.printStackTrace();
         return null;
       }
     }
 
     // add in verbs except the word "do"
     try {
       PosTagNamedEntityRecognizer posTaggerAnno = new PosTagNamedEntityRecognizer();
       String[] words = question.split(" ");
       for (String word : words) {
         if (word == null)
           continue;
         String pos = posTaggerAnno.getPOS(word);
         if (pos.matches("VBP|VB")) {
           if (word.equals("do"))
             continue;
           // TODO expand keyterm
 
           // do term extension
           Set<String> extendStrSet = new HashSet<String>();
           extendStrSet.addAll(expander1.expandKeyterm(word, pos));
           extendStrSet.addAll(expander2.expandKeyterm(word, pos));
 
           // logger.debug("key term: " + word);
           // logger.debug("extended key terms: " + Arrays.toString(extendStrSet.toArray()));
           for (String extendedKeyterm : extendStrSet) {
             Keyterm kt = new Keyterm(extendedKeyterm);
             if (extendedKeyterm.equals(word))
               kt.setProbablity(0.6f);
             else
               kt.setProbablity(0.36f);
             keyterms.add(kt);
           }
         }
       }
     } catch (ResourceInitializationException e) {
       e.printStackTrace();
     }
 
     if (logger.isDebugEnabled()) {
       Iterator<Keyterm> it = keyterms.iterator();
       StringBuilder sb;
       while (it.hasNext()) {
         sb = new StringBuilder();
         Keyterm presentKt = it.next();
         sb.append(presentKt.toString() + presentKt.getProbability() + "\t");
         // logger.debug(sb.toString());
       }
     }
     return keyterms;
   }
 }
