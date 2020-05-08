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
 
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 
 public class ShuLingPipeTermUpdater extends AbstractKeytermUpdater{
 
   @Override
   protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
     // TODO Auto-generated method stub
    File modelFile = new File("./ne-en-bio-genetag.HmmChunker");
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
     //List<Keyterm> KeyList = new ArrayList<Keyterm>();
     for(int j = 0 ; j < Chunkarray.length; j++){
       String str;
       str = question.substring(Chunkarray[j].start(),Chunkarray[j].end());
       System.out.println("Extract::::"+str);
       Keyterm Vterm;
       if( (Vterm = ContainsTerm(keyterms,str)) != null ){
     	  float prob = Vterm.getProbability() + (float)0.05;
     	  Vterm.setProbablity(prob);
       }else{
     	  Vterm = new Keyterm(str);
     	  Vterm.setProbablity((float)0.8);
     	  keyterms.add(Vterm);
       }
     }
     for(int i = 0 ; i < keyterms.size(); i++){
     	System.out.println(keyterms.get(i).getText() + " Prob: " + keyterms.get(i).getProbability());
     }
     
     return keyterms;
   }
   public Keyterm ContainsTerm(List <Keyterm> keyList, String str){
 	  for(int i = 0 ; i < keyList.size(); i ++){
 		  Keyterm a = keyList.get(i);
 		  if(a.getText().equals(str)){
 			  return a;
 		  }
 	  }
 	  return null;
   }
 }
