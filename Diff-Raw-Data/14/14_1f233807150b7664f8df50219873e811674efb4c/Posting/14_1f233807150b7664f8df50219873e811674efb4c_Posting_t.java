 package ru.maratyv.indexer.index;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mir
  * Date: 10/1/13
  * Time: 8:29 PM
  * Porsche is the only car
  *
  * Effectively immutable object for any class outside index package.
 * The only modifiable field is frequency, which is accessed only by synchronized methods.
  * So it is Thread-safe
  */
 public class Posting implements Comparable<Posting>{
     private int frequency;
     public final String docID;
 
     public Posting(String docID) {
         this.docID = docID;
         frequency = 1;
     }
 
     synchronized void incrementFrequency(){
         frequency++;
     }
 
     public synchronized int getFrequency(){
         return this.frequency;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Posting posting = (Posting) o;
 
         if (frequency != posting.frequency) return false;
         if (!docID.equals(posting.docID)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = docID.hashCode();
         result = 31 * result + frequency;
         return result;
     }
 
     @Override
     public String toString() {
         return  docID + ":" + frequency;
     }
 
     @Override
     public int compareTo(Posting o) {
         return docID.compareTo(o.docID);
     }
 }
