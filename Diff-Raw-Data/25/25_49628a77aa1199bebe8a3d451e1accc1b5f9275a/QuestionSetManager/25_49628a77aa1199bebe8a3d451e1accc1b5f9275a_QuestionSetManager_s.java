 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package QuestionsAndAnswers;
 
 import java.util.ArrayList;
 import persistance.PersistanceRepositoryQuestions;
 
 /**
  *
  * @author neil + darkstar
  */
 public class QuestionSetManager {
 
     PersistanceRepositoryQuestions persistance;
     ArrayList<QuestionSet> questionSets;
 
     public QuestionSetManager(PersistanceRepositoryQuestions persistance) {
         this.persistance = persistance;
         this.init();
     }
 
     public QuestionSet[] getAllQuestionSets() {
         return this.questionSets.toArray(new QuestionSet[0]);
     }
 
     public synchronized boolean addQuestionSet(String name) {
 
         if (persistance.addNewQuestionSet(name)) {
             this.init();
             return true;
         }
         return false;
     }
 
     public synchronized boolean deleteQuestionSet(int questionSetId) {
 
         QuestionSet qs = this.getQuestionSetById(questionSetId);
         if (persistance.removeQuestionSet(qs)) {
             this.questionSets.remove(qs);
             return true;
         }
         return false;
     }
 
     public synchronized boolean addQuestion(Question question, int questionSetId) {
 
         if (persistance.addQuestionToExistingQuestionSet(question, questionSetId)) {
             this.init();
             return true;
         }
         return false;
     }
 
     public synchronized boolean removeQuestionFromQuestionSet(int questionId, int questionSetId) {
 
         QuestionSet qs = this.getQuestionSetById(questionSetId);
         
         if (qs != null 
                 && qs.questionIdExistsInQs(questionId) 
                 && persistance.removeQuestion(questionId)) {
             qs.removeQuestion(questionId);
             return true;
         }
         return false;
     }
     
     public synchronized int answerQuestionSet(int questionSetId, int[] answers){
         
         QuestionSet qs = this.getQuestionSetById(questionSetId);
         if (qs != null){
             return qs.getScoreForAnswers(answers);
         }
         return 0;
     }
 
     private QuestionSet getQuestionSetById(int id) {
 
         for (QuestionSet qs : this.questionSets) {
             if (id == qs.getId()) {                
                 return qs;
             }
         }
         return null;
     }
 
     private synchronized void init() {
         this.questionSets = persistance.getAllQuestionSets();
     }
 }
