 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package simpleformat;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author sinan
  */
 public class Tokenizer {
 
     private static String SEPARATOR = " -> ";
     private File file;
     private List<Class> classList;
     private int edgeCount;
 
     public Tokenizer() {
         classList = new ArrayList<Class>();
         edgeCount = 0;
     }
 
     public Tokenizer(File file) {
         this.file = file;
         classList = new ArrayList<Class>();
         edgeCount = 0;
     }
 
     /**
      * @return the file
      */
     public File getFile() {
         return file;
     }
 
     /**
      * @param file the file to set
      */
     public void setFile(File file) {
         this.file = file;
     }
 
     public List<Class> tokenize() throws FileNotFoundException,
             IOException, FileFormatNotSupportedException {
        
        if (classList.size() > 0)
            return classList;
 
         FileInputStream fstream = new FileInputStream(file);
         DataInputStream in = new DataInputStream(fstream);
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
         String strLine;
         while ((strLine = br.readLine()) != null) {
 
             if (!strLine.contains(SEPARATOR)) {
                 throw new FileFormatNotSupportedException();
             }
 
             String[] klasses = strLine.split(SEPARATOR);
             if (klasses.length != 2) {
                 throw new FileFormatNotSupportedException();
             }
 
             Class c1 = new Class(klasses[0]);
             Class c2 = new Class(klasses[1]);
             if (classList.contains(c1)) {
                 c1 = classList.get(classList.indexOf(c1));
             }
             if (classList.contains(c2)) {
                 c2 = classList.get(classList.indexOf(c2));
             }
 
             boolean newEdge = false;
             if (c1.addOutClass(c2))
                 newEdge = true;
             if (c2.addInClass(c1))
                 newEdge = true;
             if (newEdge)
                 edgeCount++;
 
             if (!classList.contains(c1)) {
                 classList.add(c1);
             }
             if (!classList.contains(c2)) {
                 classList.add(c2);
             }
         }
 
         in.close();
         return classList;
     }
 
     /**
      * @return the edgeCount
      */
     public int getEdgeCount() {
         return edgeCount;
     }
 
 }
