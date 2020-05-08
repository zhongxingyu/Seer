 /**
  * @author M4rc Adam
  */
 package com.ece.superkids.testing;
 
 import com.ece.superkids.questions.entities.Question;
 import com.ece.superkids.questions.enums.QuestionCategory;
 import com.ece.superkids.questions.enums.QuestionLevel;
 import com.ece.superkids.users.FileUserManager;
 
 import org.junit.Before;
 import org.junit.After;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 
 import com.ece.superkids.users.entities.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 
 
 public class HistoryScoreTests {
 
     private User user;
     private FileUserManager fileUserManager = new FileUserManager();
     
     private ArrayList<Question> questions;
     int numberOfQuestions = 10;
     int numberOfAttempts = 5;
 
     private int totalScore;
     
 
     /* this functions generate a number of questions */
     private void generateQuestions(int n) {
         questions  = new ArrayList();
         for(int i=0; i<n; i++) {
             Question q = new Question();
             q.setQuestion("question"+ i);
             List<String> choices = new ArrayList<String>();
             for(int j=0; j<4; j++){
                 choices.add("choice" + j);
             }
             q.setChoices(choices);
             q.setAnswer("answer" + i);
             q.setExplaination("explanation" + i);
             q.setCategory(QuestionCategory.SHAPES);
             q.setLevel(QuestionLevel.LEVEL_1);
 
             questions.add(q);
         }
     }
 
     
     public void addScores() {
         user.newState(QuestionCategory.SHAPES, QuestionLevel.LEVEL_1);
 
         Random random = new Random();
         totalScore = 0;
         for (int i= 0; i < questions.size(); i++) {
             int score = Math.abs(random.nextInt()%10);
             user.saveScore(questions.get(i), score);
             totalScore += score;
         }
         System.out.println();
         int stateScore = user.getState().getTotalScore();
         user.endState();
         
         assertEquals(stateScore, totalScore);
     }
 
 
     @Before
     public void setup() {
         // create user
         user = new User("scorer");
         fileUserManager.addUser(user);
         user.getState().setCurrentCategory(QuestionCategory.SHAPES);
         user.getState().setCurrentLevel(QuestionLevel.LEVEL_1);
 
         // generate the questions
         generateQuestions(numberOfQuestions);
 
         // add scores for each of the generated questions
         for(int i=0; i<numberOfAttempts; i++){
             addScores();
         }
     }
 
     @Test
     public void testTotalScore() {
         int score = user.getState().getTotalScore();
         System.out.println(score + " " + totalScore);
         assertEquals(score, totalScore);
 
         Object[][] scores = user.getHistory(QuestionCategory.SHAPES, QuestionLevel.LEVEL_1);
         for(int i=0; i<numberOfQuestions; i++) {
             System.out.print(scores[i][0]);
            for(int j=1; j<6; j++) {
                 System.out.print(" " + scores[i][j]);
             }
             System.out.println();
         }
     }
 
 
     @After
     public void cleanUp() {
          fileUserManager.deleteUser("scorer");
     }
 }
