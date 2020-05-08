 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package io.seqware.queryengine.sandbox.testing;
 
 import io.seqware.queryengine.sandbox.testing.impl.GATKBackendTest;
 import io.seqware.queryengine.sandbox.testing.utils.Global;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import org.junit.Test;
 
 /**
  *
  * @author dyuen
  */
 public class GATKTest {
     @Test
     public void testGATK() throws IOException{
     	 ReturnValue returned = new ReturnValue();
          GATKBackendTest testb = new GATKBackendTest();
          BufferedReader in;
          String line = new String();
          String temp = new String();
          
          //Point to local VCF file to be read
         testb.loadFeatureSet("src/main/resources/testdata/exampleVCFinput.vcf");   
 
          //Point to local JSON text file to be read
         in = new BufferedReader(new FileReader("src/main/resources/testdata/exampleJSONQuery.txt"));
          while ((line = in.readLine()) != null){
          	temp = temp.concat(line);
          }
  		
          //Point to TSV output file to be written to
          Global.outputFilePath = "/Users/bso/output.txt";
          
  		//Obtain matched features
          returned = testb.getFeatures(temp);    	
     }
 }
