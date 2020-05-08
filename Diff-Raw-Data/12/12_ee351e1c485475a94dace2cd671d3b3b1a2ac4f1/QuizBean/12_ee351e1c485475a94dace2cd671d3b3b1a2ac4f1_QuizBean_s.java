 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.jus.trems.estagioaprendizado.numberquiz.controller;
 
 import br.jus.trems.estagioaprendizado.numberquiz.daoimpl.ProblemDaoImpl;
 import br.jus.trems.estagioaprendizado.numberquiz.daoimpl.QuizDaoImpl;
 import br.jus.trems.estagioaprendizado.numberquiz.entities.Problem;
 import br.jus.trems.estagioaprendizado.numberquiz.entities.Quiz;
 import br.jus.trems.estagioaprendizado.numberquiz.entities.User;
 import br.jus.trems.estagioaprendizado.numberquiz.utils.Constants;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 /**
  *
  *
  *
  * @author heverson.vasconcelos
  */
 @ManagedBean(name = "quizBean")
 @SessionScoped
 public class QuizBean implements Serializable {
 
     private List<Problem> problems;
     private int currentIndex;
     private int score;
     private Quiz quiz;
    private User authenticatedUser;
     /* DAOs */
     private ProblemDaoImpl problemDaoImpl;
     private QuizDaoImpl quizDaoImpl;
 
     public QuizBean() {
         problemDaoImpl = new ProblemDaoImpl();
         quizDaoImpl = new QuizDaoImpl();
     }
 
     @PostConstruct
    public String init() {
         quiz = new Quiz();
 
         problems = problemDaoImpl.list();
         score = 0;
         currentIndex = 0;
         Collections.shuffle(problems);
         quiz.setProblems((ArrayList<Problem>) problems);
         quiz.setScore(score);
 
        quiz.setUser(authenticatedUser);

        return null;
     }
 
     public int getScore() {
         return score;
     }
 
     public Problem getCurrent() {
         return problems.get(currentIndex);
     }
 
     public String getAnswer() {
         return "";
     }
 
     public void setAnswer(String newValue) {
         try {
             int answer = Integer.parseInt(newValue.trim());
             if (getCurrent().getSolution() == answer) {
                 score++;
             }
             currentIndex = (currentIndex + 1) % problems.size();
         } catch (NumberFormatException ex) {
         }
     }
 
     /**
      * Mtodo para armazenar o score atual no banco. Este score ser atrelado
      * a um jogo
      */
     public void saveScore() {
         if (score > 0) {
             quiz.setScore(score);
             quizDaoImpl.create(quiz);
         }
     }
 
     /**
      * Mtodo para iniciar um novo jogo. Inicialmente ser armazenado o score atual
      * e ento sero 
      */
     public String newGame() {
         init();
 
         return Constants.PAGE_NUMBERQUIZ;
     }
 
     public String showScore() {
         saveScore();
 
         return Constants.PAGE_STATS;
     }
 
     public List<Quiz> getTopScores() {
         return quizDaoImpl.getTopScores();
     }
 }
