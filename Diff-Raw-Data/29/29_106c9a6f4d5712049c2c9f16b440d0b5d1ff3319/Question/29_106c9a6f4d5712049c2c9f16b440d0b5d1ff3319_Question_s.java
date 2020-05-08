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
 public class Question implements Serializable{
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
     long id;
     String name;
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
 
     public void setId(long id) {
         this.id = id;
     }
 
     public long getId() {
         return id;
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
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
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
