 package org.lenition.singleton;
 
 import com.google.gson.Gson;
 import static org.junit.Assert.assertTrue;
 
 public class QuizTest {
 
     @org.junit.Test
     public void testGetQuestions() {
         Gson gson = new Gson();
        for(int i = 1; i <= 20; i++) {
             String json = Quiz.INSTANCE.getQuestions(i);
             System.out.println(json);
             org.lenition.domain.Quiz quiz = gson.fromJson(json, org.lenition.domain.Quiz.class);
             assertTrue(quiz.questions.length == i);
         }
 
     }
 }
