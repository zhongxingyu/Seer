 package com.forum.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository
 public class BasicTextSearch {
     private static final String FETCH_QUESTIONS_QUERY = "select question from questions order by q_id desc";
     @Autowired
     private JdbcTemplate jdbcTemplate;
     @Autowired
     private DataSource dataSource;
     private List<Question> searchedQuestions;
 
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public List<Question> getQuestionsPerPage(int pageNumber, int questionsPerPage, String searchText) {
         int endIndex = pageNumber * questionsPerPage;
         List<Question> resultQuestions = new ArrayList<Question>(questionsPerPage);
         List<Question> questions = searchAll(searchText);
 
         for (int startIndex = (pageNumber - 1) * questionsPerPage; startIndex < endIndex; startIndex++) {
             if (startIndex < questions.size()) {
                 try {
                     resultQuestions.add(questions.get(startIndex));
                 } catch (Exception e) {
                 }
             }
         }
         return resultQuestions;
     }
 
     private List<Question> fetchQuestions(String searchText) {
         SqlRowSet result = jdbcTemplate.queryForRowSet("SELECT q_id,question,post_date,user_name," +
                 " ts_rank(question_tsvector, plainto_tsquery('english_nostop','" + searchText + "'), 1 ) AS rank" +
                 " FROM questions WHERE to_tsvector('english_nostop', COALESCE(question,'') || ' ' || COALESCE(question,''))" +
                " @@ to_tsquery('english_nostop','" + searchText + "') order by rank, post_date desc;");
 
         while (result.next()) {
             searchedQuestions.add(new Question(result.getString(1), result.getString(2), result.getString(3), result.getString(4)));
         }
 
         return searchedQuestions;
     }
 
     public List<Question> searchAll(String searchText) {
         QuestionValidation questionValidation = new QuestionValidation();
         searchedQuestions = new ArrayList<Question>();
         searchText = questionValidation.trimSpecialSymbolsAndSpaces(searchText);
         searchText = convertToKeyWords(searchText);
         if (searchText.equals("")) return searchedQuestions;
         return fetchQuestions(searchText);
     }
 
     private String convertToKeyWords(String searchText) {
         searchText = searchText.replaceAll(" ", " | ");
         return searchText;
     }
 
     public String nextButtonStatus(int pageNumber, int questionsPerPage, String question) {
         int totalNumberOfQuestions = searchAll(question).size();
         int maxPages = (totalNumberOfQuestions % questionsPerPage == 0) ? totalNumberOfQuestions / questionsPerPage : totalNumberOfQuestions / questionsPerPage + 1;
         return (pageNumber >= maxPages || totalNumberOfQuestions <= questionsPerPage) ? "disabled" : "enabled";
     }
 }
