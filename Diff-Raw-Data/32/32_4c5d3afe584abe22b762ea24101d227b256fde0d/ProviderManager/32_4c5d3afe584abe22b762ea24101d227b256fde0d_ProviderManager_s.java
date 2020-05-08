 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package chocansimulator.datamangement;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Iterator;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tman
  */
 public class ProviderManager extends ManageData {
     private static ProviderManager instance = null;
 
     public static ProviderManager singletonProviderManager(String inFileName) {
         if ( instance == null ) {
           instance = new ProviderManager(inFileName);
         }
 
         return instance;
 
     }
 
     private ProviderManager(String fileName) {
         super(fileName);
 
         readFile();
     }
 
     protected void readFile() {
         //Scanner record = new Scanner(new FileReader(this.fileName));
         Scanner record = null;
 
         try {
             record = new Scanner(new FileReader(fileName));
         } catch (FileNotFoundException ex) {
             System.out.println("no providers found");
             return;
         }
 
         try {
             //first use a Scanner to get each line
             while ( record.hasNextLine() ){
                Member wrkMem = new Member(record.nextLine());
                 records.add(wrkMem);
             }
         }
         finally {
             record.close();
         }
 
     }
 
     public void writeFile() throws IOException {
         Provider p;
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                              new FileOutputStream(fileName)));
         
         Iterator itr = records.iterator();
 
         while (itr.hasNext()) {
             p = (Provider) itr.next();
             try {
                 out.write(p.fileDataToString());
             } catch ( IOException ex ) {
                 Logger.getLogger(ProviderManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         out.close();
         
     }
 
     public Object search(int number) {
         Iterator itr = records.iterator();
         Provider p = null;
 
         while (itr.hasNext()) {
             p = (Provider) itr.next();
             if ( p.getNumber() == number ) {
                 return p;
             }
         }
 
         return null;
     }
 

 }
