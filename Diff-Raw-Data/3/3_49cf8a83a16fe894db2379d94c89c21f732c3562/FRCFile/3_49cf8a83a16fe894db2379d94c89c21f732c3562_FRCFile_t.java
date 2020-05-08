 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import com.sun.squawk.microedition.io.FileConnection;
 import java.io.DataInputStream;
 import javax.microedition.io.Connector;
 
 /**
  *
  * @author First1
  */
 public class FRCFile {
     
     private static DataInputStream inFile;
     private static FileConnection fc;
     
     public static boolean runAuton;
     
     public static void openFile(String Filename){
         try {
            fc = (FileConnection)Connector.open("file:///"+Filename, 
                    Connector.READ);
             fc.create();
             inFile = fc.openDataInputStream();
         } catch (Exception e){
             System.out.println("Could not open file" + Filename);
         }
     }
     
     public static void closeFile(){
         try {
             inFile.close();
             fc.close();
         }
         catch (Exception e){
             System.out.println("Could not close file");
         }
     }
     public static void readFile(String Filename){
         openFile(Filename);
         int lineNumber = 0;
         try {
             while(inFile.available() != 0){                
                 switch(lineNumber){
                     case 0:
                         runAuton= inFile.readBoolean();
                         ++lineNumber;
                         break;
                     default:
                         System.out.println("unknown line number");
                         break;
                 }    
                 
             }
         } catch (Exception e) {
             System.out.println("Could not read file " + Filename);
             
         }
         
         closeFile();
         
     }
     
     
 }
