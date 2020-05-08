 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.opf.pst;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import org.apache.tika.exception.TikaException;
 import org.apache.tika.metadata.Metadata;
 import org.apache.tika.mime.MediaType;
 import org.apache.tika.parser.ParseContext;
 
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
public class PstMain {
     public static void main(String[] args) throws IOException, SAXException, TikaException{
         System.out.println("PST Tika");
         File file = new File("/Users/dm/Desktop/outlook.pst");
         PstDetector pstd = new PstDetector();
         MediaType mt = pstd.detect(new FileInputStream(file), new Metadata());
         if(mt == MediaType.application("vnd.ms-outlook")){
             System.out.println("Hooray");
             PstParser parser = new PstParser();
             
             parser.parse(new FileInputStream(file), new DefaultHandler(), new Metadata(), new ParseContext());
         }
         
     }
     
 }
