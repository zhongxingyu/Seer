 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package persistance;
 
 import QuestionsAndAnswers.Answer;
 import QuestionsAndAnswers.Question;
 import QuestionsAndAnswers.QuestionSet;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author neil + darkstar
  */
 public class PersistanceRepositoryQuestions {
 
     private DatabaseQueryExecutor executor;
 
     public PersistanceRepositoryQuestions(DatabaseQueryExecutor executor) {
         this.executor = executor;
     }
 
     public synchronized ArrayList<QuestionSet> getAllQuestionSets() {
 
         String sql = "SELECT * FROM"
                 + "`questionset` `qs`";
 
         ResultSet rs = executor.executeStatement(sql);
 
         return this.generateQuestionSetListFromResults(rs);
     }
 
     // <editor-fold defaultstate="collapsed" desc="Section of methods which get items by id ">
     public synchronized QuestionSet getQuestionSet(int id) {
 
         String sql = "SELECT * FROM"
                 + "`questionset` `qs`"
                 + "WHERE `qs`.`id` =" + id;
 
         ResultSet rs = executor.executeStatement(sql);
 
         return this.generateQuestionSetListFromResults(rs).get(0);
 
     }
 
     private synchronized ArrayList<Question> getQuestionSetQuestions(int id) {
 
         String sql = "SELECT * FROM"
                 + "`question` `q`"
                 + "WHERE `q`.`questionSetId` = " + id;
 
         ResultSet rs = executor.executeStatement(sql);
         return this.generateQuestionsSetQuestionsFromResults(rs);
     }
 
     private synchronized ArrayList<Answer> getQuestionAnswers(int id) {
 
         String sql = "SELECT * FROM"
                 + "`answer` `a`"
                 + "WHERE `a`.`questionId` =" + id;
 
         ResultSet rs = executor.executeStatement(sql);
         return this.generateAnswersForQuestion(rs);
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Section of methods which return a list of items from a result set">
     private synchronized ArrayList<QuestionSet> generateQuestionSetListFromResults(ResultSet rs) {
 
         ArrayList<QuestionSet> questionSetList = new ArrayList();
 
         try {
             while (rs.next()) {
                 questionSetList.add(this.getQuestionSetFromResultSet(rs));
             }
         } catch (SQLException ex) {
             Logger.getLogger(PersistanceRepositoryQuestions.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return questionSetList;
     }
    
     private synchronized ArrayList<Question> generateQuestionsSetQuestionsFromResults(ResultSet rs) {
 
         ArrayList<Question> questionList = new ArrayList();
         try {
             while (rs.next()) {
                 questionList.add(this.getQuestionFromResultSet(rs));
             }
         } catch (SQLException ex) {
             Logger.getLogger(PersistanceRepositoryQuestions.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return questionList;
     }
 
     private synchronized ArrayList<Answer> generateAnswersForQuestion(ResultSet rs) {
 
         ArrayList<Answer> answerList = new ArrayList();
         try {
             while (rs.next()) {
                 answerList.add(this.getAnswerFromResultSet(rs));
             }
         } catch (SQLException ex) {
             Logger.getLogger(PersistanceRepositoryQuestions.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         
         System.out.println("yarrr!!!");
         
         return answerList;
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Section of methods which can pull a single item from a result set. Note, you have to catch an exception">
     private synchronized QuestionSet getQuestionSetFromResultSet(ResultSet rs) throws SQLException {
 
         int id = rs.getInt("id");
         String name = rs.getString("title");
         ArrayList<Question> questionList = this.getQuestionSetQuestions(id);
         return new QuestionSet(id, name, questionList);
     }
 
     private synchronized Question getQuestionFromResultSet(ResultSet rs) throws SQLException {
 
         int id = rs.getInt("id");
         String questionText = rs.getString("text");
         ArrayList<Answer> questionAnswers = this.getQuestionAnswers(id);
         return new Question(id, questionText, questionAnswers);
     }
 
     private synchronized Answer getAnswerFromResultSet(ResultSet rs) throws SQLException {
 
         int id = rs.getInt("id");
         String text = rs.getString("text");
         int value = rs.getInt("value");
         return new Answer(text, value);
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Methods which save questionsets/questions to the database">
     public synchronized boolean addNewQuestionSet(String name) {
 
         String sql = "INSERT INTO"
                 + "`questionset` (`title`)"
                 + "VALUES ('" + name + "')";
 
         return executor.executeUpdate(sql);
     }
 
     public synchronized boolean updateQuestionSetName(QuestionSet questionSet) {
 
         String sql = "UPDATE `questionset` `qs`"
                 + "SET `qs`.`title`='" + questionSet.getName() + "'"
                 + "WHERE `qs`.`id` = " + questionSet.getId() + "";
 
         return executor.executeUpdate(sql);
     }
 
     public synchronized boolean addQuestionToExistingQuestionSet(Question question, int questionSetId) {
 
         String questionText = question.getQuestionText();
 
         String sql = "INSERT INTO"
                 + "`question` (`text`, `questionSetId`)"
                 + "VALUES ('" + questionText + "'," + questionSetId + ")";
         
         return addAnswerListToQuestion(question, executor.executeUpdate(sql));
     }
 
     private synchronized boolean addAnswerToQuestion(Answer answer) {
 
         String sql = "Insert INTO"
                 + "`answer` (`text`, `questionId`, `value`)"
                 + "VALUES ('" + answer.answerText + "', (SELECT MAX(`id`) FROM `question`), " + answer.value + " )";
 
         return executor.executeUpdate(sql);
     }
 
     private synchronized boolean addAnswerListToQuestion(Question question, boolean success) {
 
         if (!success) {
             return false;
         }
 
         for (Answer answer : question.getAnswerListAsArray()) {
             success = this.addAnswerToQuestion(answer);
         }
         return success;
     }
     // </editor-fold>
 
     public boolean removeQuestionSet(QuestionSet questionSet) {
 
                 
         System.out.println("==============================");
                 System.out.println(questionSet.getId());
                 System.out.println("==============================");
         
         
         String sql = "DELETE FROM `questionset` "
                 + "WHERE `id` = " + questionSet.getId();
 
         return executor.executeUpdate(sql);
     }
 
     public boolean removeQuestion(int questionId) {
 
         String sql = "DELETE FROM `question` "
                 + "WHERE `id` = " + questionId;
 
         return executor.executeUpdate(sql);
     }
     
    public synchronized ArrayList<Question> getAllQuestions() {
        String sql = "SELECT * FROM"
                + "`question`";

        ResultSet rs = executor.executeStatement(sql);

        return this.generateQuestionsSetQuestionsFromResults(rs);
    }
    
     
 }
