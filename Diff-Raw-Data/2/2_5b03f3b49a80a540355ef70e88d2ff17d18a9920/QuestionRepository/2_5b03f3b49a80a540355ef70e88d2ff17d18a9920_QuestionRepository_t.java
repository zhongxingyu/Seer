 package com.forum.repository;
 
 import com.forum.domain.Question;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Map;
 
 @Repository
 public class QuestionRepository {
 
     private JdbcTemplate jdbcTemplate;
 
     @Autowired
     public QuestionRepository(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public Question getById(Integer questionId) {
         QuestionRowMapper rowMapper = new QuestionRowMapper();
         String query = "SELECT Q.ID AS QUESTION_ID, Q.TITLE, Q.DESCRIPTION, " +
                 "Q.CREATED_AT, U.* FROM QUESTION Q JOIN USER U WHERE Q.USER_ID=U.ID AND Q.ID = ?";
         return (Question) jdbcTemplate.queryForObject(query, new Object[]{questionId}, rowMapper);
 
     }
 
     public int createQuestion(Map<String, String> params) {
         java.util.Date date= new java.util.Date();
         Timestamp timestamp = new Timestamp(date.getTime());
 
         return jdbcTemplate.update("INSERT INTO QUESTION (TITLE, DESCRIPTION, CREATED_AT, USER_ID) VALUES (?, ?, ?, ?)",
                new Object[]{params.get("questionTitle"), params.get("editor"), timestamp.toString(), 1});
 //        return true;
     }
 
     public List<Question> latestQuestion(int pageNum, int pageSize) {
         int pageStart = (pageNum - 1) * pageSize;
         QuestionRowMapper rowMapper = new QuestionRowMapper();
         String query = "SELECT Q.ID AS QUESTION_ID, Q.TITLE, Q.DESCRIPTION, "
                 + "Q.CREATED_AT, U.* FROM QUESTION Q JOIN USER U WHERE Q.USER_ID=U.ID "
                 + "ORDER BY Q.CREATED_AT DESC LIMIT ?,?";
         return jdbcTemplate.query(query,
                 new Object[]{pageStart, pageSize}, rowMapper);
     }
 }
