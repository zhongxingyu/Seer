 package ca2;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 public class StudentRandomFile {
        
     private RandomAccessFile studentsFile = null;
     //Code , Index
     private HashMap<Integer, Integer> studentCodes = null;
     
     //Actual length allowed for those strings.
     public static final int F_NAME_LENGTH = 20;
     public static final int S_NAME_LENGTH = 20;
     
     public final boolean AGE_TYPE = false;
     public final boolean GPA_TYPE = true;
     
     //Size in bytes for all the fields
     private final int F_NAME_SIZE = F_NAME_LENGTH + 2;      // Length + 2
     private final int S_NAME_SIZE = S_NAME_LENGTH + 2;      // Length + 2
     private final int ID_SIZE = 4;                          // Int
     private final int SEX_SIZE = 1;                         // Boolean
     private final int CURRENT_GPA_SIZE = 8;                 // Double
     private final int AGE_SIZE = 4;                         // Int
     
     private final int RECORD_SIZE =  F_NAME_SIZE
                                    + S_NAME_SIZE
                                    + ID_SIZE
                                    + SEX_SIZE
                                    + CURRENT_GPA_SIZE
                                    + AGE_SIZE;
     
 
 
     
     public StudentRandomFile(String fileName) {
         try
         {
             studentsFile = new RandomAccessFile(fileName, "rw");
             studentCodes = this.getCodes();
         }
         catch(FileNotFoundException fnfe)
         {
             System.out.println("FileNotFoundException in StudentRandomFile::Constructor");
         }
     }
     
     public void finalize()
     {
         try
         {
             if(studentsFile != null) {
                 studentsFile.close();
             }
         }
         catch(IOException ioe)
         {
             System.out.println("FileNotFoundException in StudentRandomFile::finalize()");
         }   
     }
     
     //**********************
     //  Private  ::  Reading Students
     //**********************
     
     private int getRecordNumber(int studentCode)
     {
         if(studentCodes.containsKey(studentCode))
             return studentCodes.get(studentCode);
         else
             return -1;
     }
     
     private Student getRecord(int recordNumber) throws IOException
     {
         if(recordNumber >= 0 && recordNumber < this.getRecordCount())
         {
             studentsFile.seek(recordNumber * RECORD_SIZE);
 
             int id = studentsFile.readInt();
             String fName = IOStringUtils.readFixedString(studentsFile, F_NAME_SIZE);
             String sName = IOStringUtils.readFixedString(studentsFile, S_NAME_SIZE);
             int age = studentsFile.readInt();
             boolean sex = studentsFile.readBoolean();
             double gpa = studentsFile.readDouble();
 
 
             Student student = new Student(id, fName, sName, age, sex, gpa);
 
             return student;
         }
         else
         {
             return null;
         }
     }
     
     private HashMap<Integer, Integer> getCodes()
     {
         try
         {
             HashMap<Integer, Integer> codes = new HashMap<Integer, Integer>();
             for(int i = 0;i < this.getRecordCount(); i++)
             {
                 studentsFile.seek(i * RECORD_SIZE);
                 codes.put(studentsFile.readInt(), i);
             }
             return codes;
         }
         catch(IOException ioe)
         {
             ioe.printStackTrace();
             return null;
         }
     }
     
     
     //**********************
     //  Public  ::  Reading Students
     //**********************
     
     public int getRecordCount()
     {
         int recordCount = 0;
         try {
             recordCount = (int) studentsFile.length() / RECORD_SIZE;
         } catch (IOException ex) {
             Logger.getLogger(StudentRandomFile.class.getName()).log(Level.SEVERE, null, ex);
         }
         return recordCount;
     }
     
     
     public void printAllStudents()
     {
         try
         {
             if(getRecordCount() > 0)
             {
                 for(int i = 0; i < getRecordCount(); i++)
                 {
                     Student s = this.getRecord(i);
                     System.out.println("**********");
                     System.out.println(s);
                 }
             }
             else
                 System.out.println("No entries found!");
         }
         catch(IOException ioe)
         {
             //report error
             
         }
     }
     
     
     public Student getStudent(int studentCode)
     {
         try
         {
             Student student;
             int recordNumber = this.getRecordNumber(studentCode);
             if(recordNumber != -1)
             {
             student = this.getRecord(recordNumber);
             }
             else
             {
                 student = null;
             }
             
             return student;
         }
         catch(IOException ioe)
         {
             //ERROR
             return null;
         }
     }
     
     public ArrayList<Student> searchByRange(int hi, int low, boolean type) throws IOException
     {
         //The key principle outlined here is to never read in the full set of data ever.
         ArrayList<Student> results = new ArrayList<Student>();
         
         for(int i = 0; i < getRecordCount(); i++)
         {
             if(type == AGE_TYPE)
             {
                 studentsFile.seek((i * RECORD_SIZE) + ID_SIZE + F_NAME_SIZE + S_NAME_SIZE); //48, age is last field.
                 int field = studentsFile.readInt();
                if(field <= hi || field >= low)
                     results.add(getRecord(i));
             }
             else //GPA_TYPE
             {
                 studentsFile.seek((i * RECORD_SIZE) + ID_SIZE + F_NAME_SIZE + S_NAME_SIZE + AGE_SIZE + SEX_SIZE); //53, 2nd last field
                 double field = studentsFile.readDouble();
                if(field <= hi || field >= low)
                     results.add(getRecord(i));
             }
             
         }
         return results;
     }
     
     /*
      *  Trying to enforce a good practise of leaving as much of the data out of memory as you can.
      *  Not saving additional list of information in memory, simply accessing that information straight from the file when we need it.
      */
     public ArrayList<Student> searchByRegex(String regex) throws IOException
     {
         //The key principle outlined here is to never read in the full set of data ever.
         ArrayList<Student> results = new ArrayList<Student>();
         
         for(int i = 0; i < getRecordCount(); i++)
         {
             studentsFile.seek((i * RECORD_SIZE) + ID_SIZE); //4, f_name is 2nd field
             String field = IOStringUtils.readFixedString(studentsFile, F_NAME_LENGTH);
             if(Pattern.matches(regex, field))
                 results.add(getRecord(i));
             else //if first name didn't match
                 field = IOStringUtils.readFixedString(studentsFile, S_NAME_LENGTH);
             if(Pattern.matches(regex, field))
                 results.add(getRecord(i));
             
         }
         return results;
     }
     
     //**********************
     //  Private  ::  Writing Students
     //**********************
     
     private boolean writeStudent(Student student, int recordNumber)
     {
         try 
         {    
             studentsFile.seek(recordNumber * RECORD_SIZE);
             studentsFile.writeInt(student.getID());
             IOStringUtils.writeFixedString(studentsFile,F_NAME_LENGTH, student.getFirstName());
             IOStringUtils.writeFixedString(studentsFile,S_NAME_LENGTH, student.getLastName());
             studentsFile.writeInt(student.getAge());
             studentsFile.writeBoolean(student.isSex());
             studentsFile.writeDouble(student.getCurrentGPA());
             return true;
         } catch (IOException ex) {
             Logger.getLogger(StudentRandomFile.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
         
     }
     
     //**********************
     //  Public  ::  Writing Students
     //**********************
     
     public boolean addStudent(Student student)
     {
         
         try {
             if(!studentCodes.containsKey(student.getID()))
             {
                 int recordNumber = (int)studentsFile.length() / RECORD_SIZE;
                 studentCodes.put(student.getID(), recordNumber);
                 return this.writeStudent(student, recordNumber);                
             }
             else
                 return false;
         } catch (IOException ex) {
             Logger.getLogger(StudentRandomFile.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
     }
     
     //USED FOR EDITING
     public boolean updateStudent(Student student)
     {
         int recordNumber = getRecordNumber(student.getID());
         if(recordNumber != -1)
         {
             return this.writeStudent(student, recordNumber);
         }
         else
             return false;
         
     }
     
     /*
      * Quick Delete.
      * As the order is not important in the file, we can simply delete any index, and then fill it with the one on the end.
      */
     public boolean deleteStudent(Student student)
     {
         try
         {
             if(getRecordNumber(student.getID()) != -1)
             {
                 int deletionIndex = student.getID();
                 Student lastStudent = getRecord((int)studentsFile.length()/ RECORD_SIZE - RECORD_SIZE); //Last item in the file NOT SURE IF RIGHT
 
                 //Remove last from hash map
                 studentCodes.remove(lastStudent.getID());
                 //add with same value as the one to be deleted
                 studentCodes.put(lastStudent.getID(), getRecordNumber(student.getID()));
                 //Remove k,v to be deleted.
                 studentCodes.remove(student.getID());
 
                 //Update student, will get written into the space you want removed
                 updateStudent(lastStudent);
                 //set the file to be one file shorter, removing the duplicate.
                 studentsFile.setLength(studentsFile.length() - RECORD_SIZE);
 
                 return true;
             }
             else
                 return false;
         }
         catch(IOException ioe)
         {
             //report error
             return false;
         }
     }
     
     public boolean deleteAll()
     {
         try {
             studentsFile.setLength(0);
             studentCodes.clear();
             return true;
         } catch (IOException ex) {
             Logger.getLogger(StudentRandomFile.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
     }
 }
