 package org.emamotor.javase.io;
 
import java.io.*;
 
 /**
  * @author Yoshimasa Tanabe
  */
 public class FileViewerExample {
 
     public static void main(String[] args) {
 
        try (PushbackReader reader = new PushbackReader(new FileReader("README.md"), 8);) {

            char[] buffer = new char[3];
            int length;

            while ((length = reader.read(buffer)) != -1) {
                for (int i = 0; i < length; i++) {
                    System.out.println(buffer[i]);
                }
                System.out.println();

                if (length == 3) {
                    reader.unread(buffer, 1, 2);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
 }
