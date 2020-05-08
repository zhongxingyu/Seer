 /*
  *  Wittren by Guanyu Wang, andrew ID: guanyuw
  *  For homework 1 of 11791
  */
 
 import java.io.File;
 import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
 import org.apache.uima.jcas.JCas;
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.util.AbstractExternalizable;
 
 /**
  * The Name Entity Recognizer. It use the Lingpipe tool kit to do the tagging task. 
  */
 
 public class NErAnnotatorLingPipe extends JCasAnnotator_ImplBase {
  //private File modelFile = new File("src/main/resources/Lingpipe/ne-en-bio-genetag.HmmChunker");
  
  ObjectInputStream ois = null;
  URL url = this.getClass().getClassLoader().getResource("ne-en-bio-genetag.HmmChunker");
   private Chunker chunker;
 
   /**
    * @see JCasAnnotator_ImplBase#process(JCas)
    */
   public void process(JCas aJCas) {
     // get document text
     String docText = aJCas.getDocumentText();
     String[] lines;
     String word;
     String lineindex;
     String linewoindex;
     int firstblank;
     Chunk chunk;
     try {
      //chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
      chunker = (Chunker)ois.readObject();
     } catch (IOException e) {
       e.printStackTrace();
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
     }
 
     lines = docText.split("\n");
     for (String s : lines) {
       firstblank = s.indexOf(" ");
       lineindex = s.substring(0, firstblank);
       linewoindex = s.substring(firstblank + 1, s.length());
 
       Chunking chunking = chunker.chunk(linewoindex);
       Set<Chunk> chunkresult = chunking.chunkSet();
       Iterator<Chunk> it = chunkresult.iterator();
 
       // count the number of " ".
       int parsepos, beforestart, beforeend;
       parsepos = beforestart = beforeend = 0;
       while (it.hasNext()) {
         chunk = it.next();
         GenTag annotation = new GenTag(aJCas);
         annotation.setLineindex(lineindex);
 
         for (int i = parsepos; i < chunk.start(); i++)
           if (linewoindex.charAt(i) == ' ')
             beforestart++;
         for (int i = chunk.start(); i < chunk.end(); i++)
           if (linewoindex.charAt(i) == ' ')
             beforeend++;
         parsepos = chunk.end();
         
         //compute the start and end value
         word = linewoindex.substring(chunk.start(), chunk.end());
         annotation.setBegin(chunk.start() - beforestart);
         annotation.setEnd(chunk.end() - beforestart - beforeend - 1);
         annotation.setMSofa(word);
         annotation.addToIndexes();
         
         beforestart = beforestart + beforeend;
         beforeend = 0;
       }
     }
   }
 }
