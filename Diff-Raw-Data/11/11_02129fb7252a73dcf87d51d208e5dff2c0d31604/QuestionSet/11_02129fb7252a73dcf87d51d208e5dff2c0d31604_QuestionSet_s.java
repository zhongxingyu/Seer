 package models.question;
 
 import java.util.*;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.avaje.ebean.Ebean;
 import models.competition.Competition;
 import models.data.Difficulty;
 import models.data.Grade;
 import models.data.Language;
 import models.dbentities.QuestionSetModel;
 import models.dbentities.QuestionSetQuestion;
 
 /**
  * Logical implementation question set
  *
  * @author Ruben Taelman
  * @author Kevin Stobbelaar
  *
  */
 public class QuestionSet{
 
     private List<Question> questions = new LinkedList<Question>();
     private boolean active;
     private Competition competition;
     private Grade grade;
     private Map<Question, Difficulty> difficulties;
     private QuestionSetModel data;
 
     /**
      * Constructor for question set
      * @param data question set database model
      */
     public QuestionSet(QuestionSetModel data){
         this.data = data;
         this.competition = new Competition(data.contest);
         this.active = data.active;
 
         // setting the questions for this question set
         List<QuestionSetQuestion> questionSetQuestions = Ebean.find(QuestionSetQuestion.class).where().ieq("qsid", data.id).findList();
         for (QuestionSetQuestion questionSetQuestion : questionSetQuestions){
             try {
                 Question question = Question.fetch("" + questionSetQuestion.qid);
                 questions.add(question);
             } catch (QuestionBuilderException e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * Returns the list of languages supported in this question set
      * @return list of supported languages
      */
     public List<Language> getLanguages() {
         List<Language> languages = new ArrayList<Language>();
         for (Question q : questions){
             for (Language l : q.getLanguages()){
                 if (!languages.contains(l)){
                     languages.add(l);
                 }
             }
         }
         return languages;
     }
 
     /**
      * Returns true if this list can be activated
      * @return true is this list can be activated
      */
     public boolean canActivate() {
         return true;
     }
 
     public boolean isActive() {
         return active;
     }
 
     public void setActive(boolean active) {
         this.active = active;
         data.active = active;
     }
 
     public Competition getCompetition() {
         return competition;
     }
 
     public void setCompetition(Competition competition) {
         this.competition = competition;
         data.contest = competition.getCompetitionModel();
     }
 
     public Grade getGrade() {
         return null;
     }
 
     public void setGrade(Grade grade) {
         this.grade = grade;
     }
     
     public List<Question> getQuestions() {
         return this.questions;
     }
 
 }
