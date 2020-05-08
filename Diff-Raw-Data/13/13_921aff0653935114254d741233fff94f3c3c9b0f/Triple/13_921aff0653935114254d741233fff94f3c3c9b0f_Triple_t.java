 package store;
 
 import format.Word;
 
 /**
  * User: buren
  * Date: 10/1/13
  * Time: 5:03 PM
  */
 public class Triple {
     private Word subject, verb, wObject;
     private int frequency = 1;
 
     public Triple(){}
 
 
 
     public Triple(Word subject, Word verb) {
         this.subject = subject;
         this.verb = verb;
     }
 
     public Triple(Word subject, Word verb, Word wObject) {
         this.subject = subject;
         this.verb = verb;
         this.wObject = wObject;
     }
 
     public Word getSubject() {
         return subject;
     }
 
     public Word getVerb() {
         return verb;
     }
 
     public Word getwObject() {
         return wObject;
     }
 
     public int getFrequency(){
         return frequency;
     }
 
     public void setVerb(Word verb) {
         this.verb = verb;
     }
 
     public void setSubject(Word subject) {
         this.subject = subject;
     }
 
     public void setwObject(Word wObject) {
         this.wObject = wObject;
     }
 
    public void addOccurrence() {
        this.frequency++;
    }

 
     public boolean isTriplet(){
         return subject != null && verb != null && wObject  != null;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Triple triple = (Triple) o;
 
         if (!subject.getForm().equals(triple.subject.getForm())) return false;
         if (!verb.getForm().equals(triple.verb.getForm())) return false;
         if (!wObject.getForm().equals(triple.wObject.getForm())) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = subject.getForm().hashCode();
         result = 31 * result + verb.getForm().hashCode();
         result = 31 * result + wObject.getForm().hashCode();
         return result;
     }
 }
