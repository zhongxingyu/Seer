 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * This class represents a Question in the Muldvarp application domain.
  * 
  * @author johan
  */
 public class Question extends Domain implements Serializable{
     public enum QuestionType {
         SINGLE("Single"),
         MULTIPLE("Multiple"),
         TEXT("Text");
         private String questionType;        
         private QuestionType(String questionType){        
             this.questionType = questionType;
         }
         public String getName() {            
             return questionType;
         }
     }
     //Global variables
     QuestionType questionType;    
     List<Alternative> alternatives;
     boolean shuffleAlternatives;
     
     public Question(){        
     }
     
     public Question(JSONObject json) throws JSONException{
         id = json.getInt("id");
         name = json.getString("name");        
         if (json.getString("shuffleAlternatives").equals("true")) {
             this.shuffleAlternatives = true;
         } else {
             this.shuffleAlternatives = false;
         }
        if(json.getString("questionType").equalsIgnoreCase(Question.QuestionType.SINGLE.getName())){
             questionType = QuestionType.SINGLE;
        } else if (json.getString("questionType").equalsIgnoreCase(Question.QuestionType.MULTIPLE.getName())) {
             questionType = QuestionType.MULTIPLE;
         }
         List<Alternative> aList= new ArrayList<Alternative>();
         JSONArray array = json.getJSONArray("alternatives");
         for (int i = 0; i < array.length(); i++) {
             aList.add(new Alternative(array.getJSONObject(i)));
         }
         this.alternatives = aList;
     }
     
     public Question(String name, List<Alternative> alternatives, Question.QuestionType quType){
         this.name = name;
         this.alternatives = alternatives;
         this.questionType = quType;
     }
     
     public Question(String name, List<Alternative> alternatives, Question.QuestionType quType, boolean shuffleAlts){
         this.name = name;
         this.alternatives = alternatives;
         this.questionType = quType;        
         this.shuffleAlternatives = shuffleAlts;
         if (shuffleAlts) {
             Collections.shuffle(this.alternatives);
         }
     }
     
     public Question(String name, List<Alternative> alternatives, boolean shuffleAlts) {
         this.name = name;
         this.alternatives = alternatives;
         this.shuffleAlternatives = shuffleAlts;
         if (shuffleAlts) {
             Collections.shuffle(this.alternatives);
         }
     }
 
     public Question(String name, List<Alternative> alternatives) {
         this.name = name;
         this.alternatives = alternatives;
     }
     
     /**
      * This method validates the format of the Questions class, checking if the essential parameters are set.
      * Currently these are QuestionType, name, ArrayList<Alternative>, and all the alternatives must also be formatted properly. 
      * Returns false if the format is not properly set, and returns true if else.
      * @return boolean 
      */
     @Override
     public boolean validateFormat(){
         if(questionType == null){
             System.out.println("DOMAINQUESTION: VALIDATION FAILED - QuestionType not set.");
             return false;
         } else if(alternatives == null ) {
             System.out.println("DOMAINQUESTION: VALIDATION FAILED - Alternatives not set.");
             return false;
         } else if (name == null){
             System.out.println("DOMAINQUESTION: VALIDATION FAILED - Name not set.");
             return false;
         } 
         for(Alternative a : alternatives){
             if(!a.validateFormat()){
                 return false;
             }
         }
         System.out.println("QUESTIONDOMAIN: VALIDATION OK");
         return true;
     }
 
     public QuestionType getQuestionType() {
         return questionType;
     }
 
     public void setQuestionType(QuestionType questionType) {
         this.questionType = questionType;
     }
     
 
     public List<Alternative> getAlternatives() {
         return alternatives;
     }
 
     public void setAlternatives(List<Alternative> alternatives) {
         this.alternatives = alternatives;
         if (shuffleAlternatives) {
             Collections.shuffle(this.alternatives);
         }
     }
     
     public void addAlternative(Alternative newAlternative) {
         alternatives.add(newAlternative);
     }
 
     public void removeAlternative(Alternative a) {
         alternatives.remove(a);
     }
     
     public Alternative getAlternative(int id){      
         return alternatives.get(id);
     }
 
     public boolean isShuffleAlternatives() {
         return shuffleAlternatives;
     }
 
     public void setShuffleAlternatives(boolean shuffleAlternatives) {
         this.shuffleAlternatives = shuffleAlternatives;
     }
     
     public void shuffleQuestions(){
         if (alternatives != null) {
             Collections.shuffle(alternatives);
         }
     }
 }
