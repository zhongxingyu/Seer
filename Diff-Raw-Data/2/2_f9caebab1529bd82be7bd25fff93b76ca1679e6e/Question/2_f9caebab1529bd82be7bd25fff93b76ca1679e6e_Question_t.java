 package com.webquiz.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class Question implements Serializable {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
 
     public enum Type {
         UNKNOWN, FILL_IN_THE_BLANK, MULTIPLE_CHOICE, TRUE_FALSE;
         public String getString() {
             return this.name();
         }
     }
 
     private int id = -1;
     private Type type = Type.UNKNOWN;
     private String text = "";
     private ArrayList<Answer> answers = new ArrayList<Answer>();
     private ArrayList<String> userAnswers = new ArrayList<String>();
     boolean answeredCorrectly = false;
 
     public Question(int id, Type type, String text) {
         this.id = id;
         this.type = type;
         this.text = text;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     public String getText() {
         return text;
     }
 
     public void setText(String name) {
         this.text = name;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public ArrayList<Answer> getAnswers() {
         return answers;
     }
 
     public void setAnswers(ArrayList<Answer> answers) {
         this.answers = answers;
     }
 
     public ArrayList<String> getUserAnswers() {
         return userAnswers;
     }
 
     public void setUserAnswers(ArrayList<String> userAnswers) {
         this.userAnswers = userAnswers;
     }
 
     public ArrayList<Answer> getCorrectAnswers() {
         ArrayList<Answer> correctAnswers = new ArrayList<Answer>();
 
         for (Answer answer : getAnswers())
             if (answer.getCorrect())
                 correctAnswers.add(answer);
 
         return correctAnswers;
     }
 
     public boolean isAnsweredCorrectly() {
         return answeredCorrectly;
     }
 
     public void setAnsweredCorrectly(boolean answeredCorrectly) {
         this.answeredCorrectly = answeredCorrectly;
     }
 
     public void grade() {
         ArrayList<Answer> correctAnswers = getCorrectAnswers();
         int userCorrectAnswerCount = 0;
         int userWrongAnswerCount = 0;
 
         for (String userAnswer : getUserAnswers()) {
             boolean foundMatch = false;
             for (Answer correctAnswer : correctAnswers) {
                if (correctAnswer.matches(userAnswer.trim())) {
                     foundMatch = true;
                     break;
                 }
             }
             if (foundMatch)
                 ++userCorrectAnswerCount;
             else
                 ++userWrongAnswerCount;
 
         }
 
         setAnsweredCorrectly(correctAnswers.size() == userCorrectAnswerCount && userWrongAnswerCount == 0);
     }
 }
