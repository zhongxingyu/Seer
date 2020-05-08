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
 
 @Repository
 public class AdviceRepository {
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
 
     public void insert(int question_id,String answer) {
         QuestionValidation validation = new QuestionValidation();
         answer = validation.insertApostrophe(answer);
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         String dateformat = dateFormat.format(date);
         jdbcTemplate.execute("insert into answers(q_id,answer) values(" + question_id + ",'" + answer + "')");
     }
 
 
     public List<Advice> getAdvices(int questionId) {
        SqlRowSet advice = jdbcTemplate.queryForRowSet("select q_id,answer,post_date from answers where q_id=" + questionId);
         List<Advice> advices = new ArrayList<Advice>();
         while (advice.next()) {
             advices.add(new Advice(advice.getString(1),advice.getString(2),advice.getString(3)));
         }
         return advices;
     }
 }
