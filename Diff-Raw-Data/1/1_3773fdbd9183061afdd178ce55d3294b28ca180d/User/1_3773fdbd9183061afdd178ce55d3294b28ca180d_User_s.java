 /**
  * @author M4rc Adam
  */
 package com.ece.superkids.users.entities;
 
 import com.ece.superkids.questions.entities.Question;
 import com.ece.superkids.questions.enums.QuestionCategory;
 import com.ece.superkids.questions.enums.QuestionLevel;
 import java.io.Serializable;
 
 public class User implements Serializable {
 
     static final long serialVersionUID = -6618469841127325812L;
 
     private int id;
     private String name;
     private State state;
     private History history;
 
     public User(String name) {
         this.name = name;
         state = new State();
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public State getState() {
         return state;
     }
 
     public void setState(State state) {
         this.state = state;
     }
 
     public void setCurrentLevel(QuestionLevel level) {
         state.setCurrentLevel(level);
     }
 
     public void setCurrentQuestion(Question question) {
         state.setCurrentQuestion(question);
     }
 
     public void saveScore(Question question, Integer score) {
         state.addScore(question, score);
     }
 
     public void newState(QuestionCategory category, QuestionLevel level) {
         history.saveToHistory(category, level, state);
         state = new State();
     }
 
     public Object[][] getHistory() {
         return history.getHistoryTest();
     }
     
     public Question getCurrentQuestion() {
         return state.getCurrentQuestion();
     }
     
     public QuestionLevel getCurrentLevel() {
         return state.getCurrentLevel();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (!(obj instanceof User)) {
            return false;
         }
         User user2 = (User)obj;
         if (!(id == user2.id))  return false;
         if (!name.equals(user2.name))  return false;
 
         return true;
     }
 
 
 
 }
