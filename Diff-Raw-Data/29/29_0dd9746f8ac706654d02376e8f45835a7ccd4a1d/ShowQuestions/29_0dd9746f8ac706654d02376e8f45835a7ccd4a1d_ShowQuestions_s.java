 package com.forum.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository
 public class ShowQuestions {
     private static final int BEGIN_INDEX = 0;
     private static final int CHARACTER_LIMIT = 50;
     private static final String TRAILING_CHARACTERS = "...?";
 
     @Autowired
     private JdbcTemplate jdbcTemplate;
 
     @Autowired
     private DataSource dataSource;
 
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public List<Question> show(int pageNumber, int questionsPerPage) {
         int endIndex = pageNumber * questionsPerPage;
         List<Question> resultQuestions = new ArrayList<Question>(questionsPerPage);
         List<Question> questions = getQuestions();
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
 
     public List<Question> getQuestions() {
         SqlRowSet questions = jdbcTemplate.queryForRowSet("select * from questions ORDER BY post_date DESC");
 
         List<Question> questionsList = new ArrayList<Question>();
         while (questions.next()) {
             questionsList.add(new Question(questions.getString(1), truncateQuestionToCharacterLimit(questions.getString(2)), questions.getString(3), questions.getString(4)));
         }
         return questionsList;
     }
 
     String truncateQuestionToCharacterLimit(String question) {
        return (question.length() <= CHARACTER_LIMIT) ? question : question.substring(BEGIN_INDEX, CHARACTER_LIMIT).concat(TRAILING_CHARACTERS);
     }
 
     public String nextButtonStatus(int pageNumber, int questionsPerPage) {
         int totalNumberOfQuestions = jdbcTemplate.queryForInt("select count(*) from questions");
         int maxPages = (totalNumberOfQuestions % questionsPerPage == 0) ? totalNumberOfQuestions / questionsPerPage : totalNumberOfQuestions / questionsPerPage + 1;
         return (pageNumber >= maxPages || totalNumberOfQuestions <= questionsPerPage) ? "disabled" : "enabled";
     }
 
     public String previousButtonStatus(int pageNumber) {
         return (pageNumber <= 1) ? "disabled" : "enabled";
     }

 }
