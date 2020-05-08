 /**
  * @author M4rc Adam
  */
 package com.ece.superkids.users.entities;
 
 import com.ece.superkids.questions.entities.Question;
 import com.ece.superkids.questions.enums.QuestionCategory;
 import com.ece.superkids.questions.enums.QuestionLevel;
import com.ece.superkids.users.UserDatabaseFactory;
import com.ece.superkids.users.UserManager;
 import java.io.Serializable;
 
 public class User implements Serializable {
 
     static final long serialVersionUID = -6618469841127325812L;
 
     private int id;
     private String name;
     private State state;
     private History history;
 
    static private UserManager userManager = UserDatabaseFactory.aUserManager();
 
     public User(String name) {
         this.name = name;
         state = new State();
         history = new History();
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
 
     private void setState(State state) {
         this.state = state;
         saveState();
     }
 
     public void setCurrentQuestion(Question question) {
         state.setCurrentQuestion(question);
         saveState();
     }
 
     public void saveScore(Question question, Integer score) {
         state.addScore(question, score);
     }
 
     public void newState(QuestionCategory category, QuestionLevel level) {
         history.saveToHistory(state);
         state = new State();
         saveState();
     }
 
     public void saveState() {
        userManager.addUser(this);
        userManager.updateUser(this, this);
     }
 
     public Object[][] getHistory(QuestionCategory questionCategory, QuestionLevel questionLevel) {
         return history.getHistory(questionCategory, questionLevel);
     }
 
     public Object[][] getHistoryTest() {
         return history.getHistoryTest();
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
 
