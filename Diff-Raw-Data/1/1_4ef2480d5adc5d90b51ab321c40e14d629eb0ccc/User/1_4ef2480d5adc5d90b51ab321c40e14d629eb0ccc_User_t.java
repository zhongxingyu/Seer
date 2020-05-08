 /**
  * @author M4rc Adam
  */
 package com.ece.superkids.users.entities;
 
 import com.ece.superkids.achievements.entities.Achievements;
 import com.ece.superkids.questions.entities.Question;
 import com.ece.superkids.questions.enums.QuestionCategory;
 import com.ece.superkids.questions.enums.QuestionLevel;
 import com.ece.superkids.users.UserDatabaseFactory;
 import com.ece.superkids.users.FileUserManager;
 import com.ece.superkids.users.UserManager;
 import java.io.Serializable;
 
 public class User implements Serializable {
 
     static final long serialVersionUID = 1L;
 
     private int id;
     private String name;
     private State state;
     private History history;
     private String image;
     private Achievements achievements;
 
     public User(String name) {
         this.name = name;
         state = new State();
         history = new History();
         achievements = new Achievements();
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
         saveUser();
     }
 
     public void copyState(State state) {
         this.state = state;
     }
 
     public void setImage(String image) {
         this.image = image;
     }
     public String getImage() {
         return image;
     }
 
     public Achievements getAchievements() {
         return achievements;
     }
 
     public void setAchievements(final Achievements achievements) {
         this.achievements = achievements;
     }
 
     public History getHistory() {
         return history;
     }
 
     public void setHistory(final History history) {
         this.history = history;
     }
 
     public void setGameOn(boolean set){
         this.history.setGameStarted();
         saveUser();
     }
     public void setGameOn() {
         this.history.setGameStarted();
         saveUser();
     }
 
     public boolean isGameOn() {
         return history.getGameOn();
     }
 
     /* Use this to create a new game for the user, warning: this clears everything! */
     public void newGame() {
         state = new State();
         history = new History();
         setGameOn();
     }
     
     public boolean isCurrentLevelFinished(){
         return history.isLevelFinished(this.state.getCurrentLevel());
     }
     public boolean isLevelFinished(QuestionLevel level) {
         return history.isLevelFinished(level);
     }
 
     public void setCurrentQuestion(Question question) {
         history.setGameStarted();
         state.setCurrentQuestion(question);
         state.setCurrentLevel(question.getLevel());
         state.setCurrentCategory(question.getCategory());
         saveUser();
     }
 
     public void saveScore(Question question, Integer score) {
         state.addScore(question, score);
     }
     
     /* call this function whenever you click on a new category and level */
     public void newState(QuestionCategory category, QuestionLevel level) {
         /* clear the old state and create a new one */
         state = new State();
 
         /* set category and level to the new state */
         state.setCurrentCategory(category);
         state.setCurrentLevel(level);
 
         /* save the user into the ser file */
         saveUser();
     }
 
     /* call this function when you're done with a category and level (after the stars screen) */
     public void endState() {
         /* save the state to the history */
         history.saveToHistory(state);
         /* clear the category so that whenever the game loads again, the user goes to category selection */
         state.setCurrentCategory(null);
         /* save the user in the ser file */
         saveUser();
     }
 
     /* adapters for the state functions */
     public QuestionCategory getCurrentCategory() {
         return state.getCurrentCategory();
     }
     public void setCurrentCategory(QuestionCategory currentCategory) {
         state.setCurrentCategory(currentCategory);
     }
 
     public QuestionLevel getCurrentLevel() {
         return state.getCurrentLevel();
     }
     public void setCurrentLevel(QuestionLevel currentLevel) {
         state.setCurrentLevel(currentLevel);
     }
 
     /* call this function to save user in database, this is automatically called when endState is called */
     public void saveUser() {
         (new FileUserManager()).addUser(this);
         (new FileUserManager()).updateUser(this, this);
     }
 
     /* deletes the serializable file for the user */
     public void deleteUser() {
         (new FileUserManager()).deleteUser(name);
     }
 
     public Object[][] getHistory(QuestionCategory questionCategory, QuestionLevel questionLevel) {
         return history.getHistory(questionCategory, questionLevel);
     }
 
     public Object[][] getHistoryTest() {
         return history.getHistoryTest();
     }
 
     public State getMaximumScoreState(QuestionCategory questionCategory, QuestionLevel questionLevel) {
         return history.getMaximumScoreState(questionCategory, questionLevel);
     }
 
     public int getTotalScore() {
         return history.getTotalScore();
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
 
