 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CalmPortal.model;
 
 /**
  *
  * @author Jinn
  */
 public class Question {
     private int questionId;
     private String title;
     private String description;
     private String fullText;
     
    public Question(int id, String title, String description)
     {
         this.questionId = id;
         this.title = title;
        this.description = description;
     }
 
     public int getQuestionId() {
         return questionId;
     }
 
     public void setQuestionId(int questionId) {
         this.questionId = questionId;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getFullText() {
         return fullText;
     }
 
     public void setFullText(String fullText) {
         this.fullText = fullText;
     }
     
     
     
     
 
 }
