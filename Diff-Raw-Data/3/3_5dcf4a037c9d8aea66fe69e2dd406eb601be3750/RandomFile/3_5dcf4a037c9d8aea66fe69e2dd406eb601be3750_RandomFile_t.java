 /*
  * @Author Chris Niesel
  * @Purpose: Write employee database info
  * 
  */
 package InputOutput;
 
 import java.io.*;
 import java.util.*;
 import datatstructures.EmployeeRecord;
 
 public class RandomFile implements DataFile {
 
     final static int FIELD_SIZE = 50;
     private final long RECLEN;
     private String fileName;
     private RandomAccessFile raf;
     private long numRecords;
 
     public RandomFile(String fn, long length) {
         this.fileName = fn;
         this.RECLEN = length;
     }
 
     public void prepareInputFile() {
         try {
             raf = new RandomAccessFile(fileName, "rw");
         } catch (FileNotFoundException e) {
             System.out.println("Error - this RandomAccess input file does not exist");
         }
         try {
             numRecords = raf.length() / RECLEN;
         } catch (IOException e) {
             System.out.println("error=" + e.toString());
         }
     }
 
     public void prepareOutputFile() {
         try {
             raf = new RandomAccessFile(fileName, "rw");
         } catch (IOException e) {
             System.out.println("error=" + e.toString());
         } // close catch
     }
 
     public EmployeeRecord getNextRecord() {
         EmployeeRecord newRecord = new EmployeeRecord();
         try {
             raf.seek(0);
             newRecord.setEmployeeNumber(raf.readInt());
 //String Buffers allow String building
             StringBuffer fn = new StringBuffer("");
             String firstName;
             StringBuffer ln = new StringBuffer("");
             String lastName;
             StringBuffer ha = new StringBuffer("");
             String hash;
             StringBuffer ma = new StringBuffer("");
             String manager;
 //Read in first name one char at a time
             for (int i = 0; i < FIELD_SIZE; i++) {
                 fn.append(raf.readChar());
             }
 //Trims any blank space if there are empty characters
             firstName = fn.toString().trim();
 //Read in last name one char at a time
             for (int i = 0; i < FIELD_SIZE; i++) {
                 ln.append(raf.readChar());
             }
             lastName = ln.toString().trim();
             for (int i = 0; i < FIELD_SIZE; i++) {
                 ha.append(raf.readChar());
             }
             hash = ha.toString().trim();
 
             for (int i = 0; i < FIELD_SIZE; i++) {
                 ma.append(raf.readChar());
             }
             manager = ma.toString().trim();
             if (manager.equalsIgnoreCase("true")) {
                 newRecord.setManager(true);
             } else {
                 newRecord.setManager(false);
             }
             newRecord.setFirstName(firstName);
             newRecord.setLastName(lastName);
             newRecord.setPassword(hash);
             raf.close();
         } catch (IOException e) {
             System.out.println("error=" + e.toString());
         } // close catch
         return newRecord;
     }
 
     public EmployeeRecord getNextRecord(long recordNumber) {
         EmployeeRecord newRecord = new EmployeeRecord();
         try {
             raf.seek(RECLEN * recordNumber);
             newRecord.setEmployeeNumber(raf.readInt());
 //String Buffers allow String building
             StringBuffer fn = new StringBuffer("");
             String firstName;
             StringBuffer ln = new StringBuffer("");
             String lastName;
             StringBuffer ha = new StringBuffer("");
             String hash;
             StringBuffer ma = new StringBuffer("");
             String manager;
 //Read in first name one char at a time
             for (int i = 0; i < FIELD_SIZE; i++) {
                 fn.append(raf.readChar());
             }
             firstName = fn.toString().trim();
 
             for (int i = 0; i < FIELD_SIZE; i++) {
                 ln.append(raf.readChar());
             }
             lastName = ln.toString().trim();
 
             for (int i = 0; i < FIELD_SIZE; i++) {
                 ha.append(raf.readChar());
             }
             hash = ha.toString().trim();
 
             for (int i = 0; i < FIELD_SIZE; i++) {
                 ma.append(raf.readChar());
             }
             manager = ma.toString().trim();
 
             if (manager.equalsIgnoreCase("true")) {
                 newRecord.setManager(true);
             } else {
                 newRecord.setManager(false);
             }
             newRecord.setFirstName(firstName);
             newRecord.setLastName(lastName);
             newRecord.setPassword(hash);
             raf.close();
         } catch (IOException e) {
             System.out.println("error=" + e.toString());
         } // close catch
         return newRecord;
     }
 
     public void writeNextRecord(EmployeeRecord record) {
         try {
 //Write the student number
             raf.seek(raf.length());
             raf.writeInt(record.getEmployeeNumber());
             StringBuffer fn = new StringBuffer("");
             String firstName = record.getFirstName();
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < firstName.length()) {
                     raf.writeChar(firstName.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             StringBuffer ln = new StringBuffer("");
             String lastName = record.getLastName();
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < lastName.length()) {
                     raf.writeChar(lastName.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             StringBuffer ha = new StringBuffer("");
             String hash = record.getPassword();
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < hash.length()) {
                     raf.writeChar(hash.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             StringBuffer ma = new StringBuffer("");
             Boolean manager = record.isManager();
             String isManager;
            //fixed if statement, required 2 =
             if (manager == true) {
                 isManager = "true";
             } else {
                 isManager = "false";
             }
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < isManager.length()) {
                     raf.writeChar(isManager.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             isManager = "";
         }//end try
         catch (IOException e) {
             System.out.println("error=" + e.toString());
         } // close catch
     }
 
     /***************
      * Write record at specific location
      * *************/
     public void writeNextRecord(EmployeeRecord record, long recordNumber) {
         try {
 //go to specifc record
             raf.seek(RECLEN * recordNumber);
             raf.writeInt(record.getEmployeeNumber());
             StringBuffer fn = new StringBuffer("");
             String firstName = record.getFirstName();
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < firstName.length()) {
                     raf.writeChar(firstName.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             StringBuffer ln = new StringBuffer("");
             String lastName = record.getLastName();
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < lastName.length()) {
                     raf.writeChar(lastName.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             StringBuffer ha = new StringBuffer("");
             String hash = record.getPassword();
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < hash.length()) {
                     raf.writeChar(hash.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             StringBuffer ma = new StringBuffer("");
             Boolean manager = record.isManager();
             String isManager;
            //fixed if statement, required 2 =
             if (manager == true) {
                 isManager = "true";
             } else {
                 isManager = "false";
             }
             for (int j = 0; j < FIELD_SIZE; j++) {
                 if (j < isManager.length()) {
                     raf.writeChar(isManager.charAt(j));
                 } else {
                     raf.writeChar(' ');
                 }
             }//end for
             isManager = "";
         }//end try
         catch (IOException e) {
             System.out.println("error=" + e.toString());
         } // close catch
 
     }
 
     public String getNext() {
         return "";
     }
 
     public void writeNext(String field) {
     }
 //to clear file contents
 
     public void clean() {
         //delete raf file
         File file = new File(fileName);
         file.delete();
         //create new file 
         try {
             File file2 = new File(fileName);
             if (file2.createNewFile()) {
                 System.out.println("Success!");
             } else {
                 System.out.println("Error, file already exists.");
             }
         } catch (IOException ioe) {
             ioe.printStackTrace();
         }
 
     }
 
     public void close() {
         try {
             raf.close();
         } catch (IOException e) {
             System.out.println("error=" + e.toString());
         } // close catch
 
     }
 
     public void closeInput() throws IOException {
         raf.close();
     }
 }
