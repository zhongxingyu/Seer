 package org.emamotor.javase.io;
 
import java.io.FileReader;
import java.io.PushbackReader;
 
 /**
  * @author Yoshimasa Tanabe
  */
 public class FileViewerExample {
 
     public static void main(String[] args) {
 
        PushbackReader reader = new PushbackReader(new FileReader());
     }
 }
