 package edu.cmu.lti.oaqa.openqa.test.team06.keyterm;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunker;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.util.AbstractExternalizable;
 
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 
 public class ShuLingPipeTermExtractor extends AbstractKeytermExtractor{
 
   @Override
   protected List<Keyterm> getKeyterms(String question) {
     // TODO Auto-generated method stub
    File modelFile = new File("src/ne-en-bio-genetag.HmmChunker");
     Chunker chunker = null;
     Chunking chunking;
     Chunk[] Chunkarray;
     try {
       chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
     } catch (ClassNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     chunking  = chunker.chunk(question);
     Chunkarray = chunking.chunkSet().toArray(new Chunk[0]);
     //List<Keyterm> KeyList;
     List<Keyterm> KeyList = new ArrayList<Keyterm>();
     for(int j = 0 ; j < Chunkarray.length; j++){
       String str;
       str = question.substring(Chunkarray[j].start(),Chunkarray[j].end());
       System.out.println("Extract::::"+str);
       KeyList.add(new Keyterm(str));
     }
     
     FileWriter fstream = null;
 	try {
 		fstream = new FileWriter("LingpipeHMM.txt",true);
 	} catch (IOException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
     BufferedWriter out = new BufferedWriter(fstream);
     PrintWriter pw = new PrintWriter(out,false);
     pw.println(KeyList);
     pw.close();    
     
     return KeyList;
   }
 }
