 package com.forum.repository;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
import java.util.Map;
 
 @Repository
 public class QuestionRepository {
 
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
 
     public void insert(String question) {
         QuestionValidation validation = new QuestionValidation();
         question = validation.insertApostrophe(question);
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         String dateformat = dateFormat.format(date);
        jdbcTemplate.execute("insert into questions(question,post_date) values('" + question + "','"+ dateformat +"')");
     }
 
     public Question getQuestionById(int questionId) {
         SqlRowSet question = jdbcTemplate.queryForRowSet("select * from questions where q_id=" + questionId);
         question.next();
        return new Question(question.getString(1), question.getString(2), question.getString(3),question.getString(4));
     }
 
     public List<Question> getQuestions(List questionIds) {
        List<Question> questions=new ArrayList<Question>();
              String query = " select DISTINCT q.q_id, q.question, max(a.post_date) as post_date, q.user_name from questions q" +
                   " join answers a on q.q_id=a.q_id where q.q_id in ("+convertArrayToString(questionIds)+") group by q.q_id, q.question, q.user_name order by post_date DESC" ;
        SqlRowSet results=jdbcTemplate.queryForRowSet(query);
        while(results.next()){
             questions.add(new Question(results.getString("q_id"), results.getString("question"),
                     results.getString("post_date"), results.getString("user_name")));
         }
         return questions;
     }
 
     private String convertArrayToString(List questionIds) {
        String questionIdString="";
        for(int i=0; i< questionIds.size(); i++){
             questionIdString = questionIdString + questionIds.get(i);
            if(i != questionIds.size()-1) {
            questionIdString = questionIdString + "," ;
             }
         }
         return questionIdString;
     }
 }
