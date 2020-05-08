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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author tman
  */
 public class BillManager extends ManageData {
     private static BillManager instance = null;
 
     public static BillManager singletonBillManager(String inFileName) {
         if ( instance == null ) {
           instance = new BillManager(inFileName);
         }
 
         return instance;
 
     }
 
     private BillManager(String fileName) {
         super(fileName);
 
         readFile();
     }
 
     protected void readFile() {
 
         Scanner record = null;
 
         try {
             record = new Scanner(new FileReader(fileName));
         } catch (FileNotFoundException ex) {
             System.out.println("no bills found");
             return;
         }
 
         try {
             //first use a Scanner to get each line
             while ( record.hasNextLine() ){
                 Bill wrkBill = new Bill(record.nextLine());
                 records.add(wrkBill);
             }
         }
         finally {
             record.close();
         }
 
     }
 
     public void writeFile() throws IOException {
         Bill b;
         BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                              new FileOutputStream(fileName)));
         
         Iterator itr = records.iterator();
 
         while (itr.hasNext()) {
             b = (Bill) itr.next();
             try {
                 out.write(b.fileDataToString());
             } catch ( IOException ex ) {
                 Logger.getLogger(BillManager.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         out.close();
         
     }
 
     public List getList(Date startDate) {
         //TODO -
         records = new ArrayList();
         return records;
     }
 
     public Object search (int number) {
        Iterator itr = records.iterator();
        Member m = null;
        int k = 0;

        while (itr.hasNext()) {
            m = (Member) itr.next();
            if ( k == number ) {
                return m;
            }
            k = k+1;
        }
 
        return null;
     }
 
 }
