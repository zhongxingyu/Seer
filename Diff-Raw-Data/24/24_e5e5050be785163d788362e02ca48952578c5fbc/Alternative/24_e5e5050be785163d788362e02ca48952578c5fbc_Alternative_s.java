 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.domain;
 
 import java.io.Serializable;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class represents an Alternative in the Quiz-context of the Muldvarp application domain.
  * 
  * @author johan
  */
 public class Alternative extends Domain implements Serializable {
     public enum AlternativeType {
         CHOICE("Choice"),
         TEXT("Text");
         private String alternativeType;        
         private AlternativeType(String quizType){        
             this.alternativeType = quizType;
         }
         public String getName() {            
             return alternativeType;
         }
     }
     AlternativeType alternativeType;
     boolean isCorrect;
     boolean isChoosen;
     String answerText; //This text is the text that can be set by a user
 
     public Alternative(){        
     }
     
     public Alternative(JSONObject json) throws JSONException{
         id = json.getInt("id");
         name = json.getString("name");        
         if (json.getString("isCorrect").equals("true")) {
             this.isCorrect = true;
         } 
         
     }
     
     public Alternative(String name) {
         super(name);
         isChoosen = false;
     }
     
     public Alternative(String name, boolean isCorrect) {
         super(name);
         this.isCorrect = isCorrect;
         isChoosen = false;
     }
     
     public Alternative(String name, boolean isCorrect, boolean shuffleAlternatives) {
         super(name);
         this.isCorrect = isCorrect;
         isChoosen = false;
         
     }
     
     /**
      * This method validates the format of the Alternative class, checking if the essential parameters are set.
      * Currently these are enum AlternativeType and String name.
      * Returns false if the format is not properly set, and returns true if else.
      * @return boolean 
      */
     @Override
    public boolean validateFormat(){
        if(alternativeType == null){
            System.out.println("DOMAINALTERNATIVE: VALIDATION FAILED - AlternativeType not set.");
            return false;
        } else if(name == null){
             System.out.println("DOMAINALTERNATIVE: VALIDATION FAILED - Name not set.");
             return false;
        } 
         return true;
     }
 
     public boolean isIsCorrect() {
         return isCorrect;
     }
 
     public void setIsCorrect(boolean isCorrect) {
         this.isCorrect = isCorrect;
     }
 
     public String getAnswerText() {
         return answerText;
     }
 
     public void setAnswerText(String answerText) {
         this.answerText = answerText;
     }
 
     public boolean isIsChoosen() {
         return isChoosen;
     }
 
     public void setIsChoosen(boolean isChoosen) {
         this.isChoosen = isChoosen;
     }
     
     public void toggleChosen(){
         isChoosen = !isChoosen;
     }
 
     public AlternativeType getAlternativeType() {
         return alternativeType;
     }
 
     public void setAlternativeType(AlternativeType alternativeType) {
         this.alternativeType = alternativeType;
     }
     
     
 }
