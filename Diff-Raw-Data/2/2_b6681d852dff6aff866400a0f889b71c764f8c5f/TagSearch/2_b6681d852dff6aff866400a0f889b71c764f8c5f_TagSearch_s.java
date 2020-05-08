 package com.forum.repository;
 
 import com.forum.domain.Question;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.util.ArrayList;
 import java.util.List;
 
 @Repository
 public class TagSearch {
     private JdbcTemplate jdbcTemplate;
     private DataSource dataSource;
 
     @Autowired
     public TagSearch(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public List<Question> getQuestionsPerPage(int pageNumber, int questionsPerPage, String tagText) {
         int endIndex = pageNumber * questionsPerPage;
         List<Question> resultQuestions = new ArrayList<Question>(questionsPerPage);
         List<Question> questions = fetchTag(tagText);
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
 
     private List<Question> fetchTag(String searchTag) {
         List<Question> searchedTag= new ArrayList<Question>();
        SqlRowSet result = jdbcTemplate.queryForRowSet("select q.q_id,q.question,q.post_date,q.user_name,t.tag_name from questions q join questions_tags qt on q.q_id=qt.q_id join tags t on t.t_id=qt.t_id where t.tag_name='"+searchTag+"';");
 
         while (result.next()) {
             searchedTag.add(new Question(result.getString(1), result.getString(2), result.getString(3), result.getString(4), result.getString(5)));
         }
 
         return searchedTag;
     }
 
     public String nextButtonStatus(int pageNumber, int questionsPerPage, String tag) {
         int totalNumberOfQuestions = fetchTag(tag).size();
         int maxPages = (totalNumberOfQuestions % questionsPerPage == 0) ? totalNumberOfQuestions / questionsPerPage : totalNumberOfQuestions / questionsPerPage + 1;
         return (pageNumber >= maxPages || totalNumberOfQuestions <= questionsPerPage) ? "disabled" : "enabled";
     }
 }
