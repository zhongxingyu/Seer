 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package CalmPortal.services;
 
 import CalmPortal.model.Question;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Jinn
  */
 public class QuestionRepository {
 
     public List<Question> GetQuestion() {
         List<Question> questions = new ArrayList<Question>();
         questions.add(new Question(1, "Le boss", "My boss is crazy and mad"));
        questions.add(new Question(2, "Le girl friend", "Overly attached girlfriend"));
         questions.add(new Question(3, "Le work", "I am overloaded with work"));
         questions.add(new Question(4, "Le family", "My family is a mess"));
         
         return questions;
     }
 }
 
