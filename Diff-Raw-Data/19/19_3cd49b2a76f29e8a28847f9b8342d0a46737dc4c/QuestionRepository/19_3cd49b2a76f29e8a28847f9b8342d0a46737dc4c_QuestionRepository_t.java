 package com.forum.repository;
 
 import com.forum.domain.Question;
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
 public class QuestionRepository {
    @Autowired
    private ShowQuestions showQuestion;
     private JdbcTemplate jdbcTemplate;
     private DataSource dataSource;
 
     @Autowired
     public QuestionRepository(DataSource dataSource) {
         this.jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public void insert(String question) {
         QuestionValidation validation = new QuestionValidation();
         question = validation.insertApostrophe(question);
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         String dateformat = dateFormat.format(date);
         jdbcTemplate.execute("insert into questions(question,post_date) values('" + question + "','" + dateformat + "')");
     }
 
     public Question getQuestionById(int questionId) {
         SqlRowSet question = jdbcTemplate.queryForRowSet("select * from questions where q_id=" + questionId);
         question.next();
         return new Question(question.getString(1), question.getString(2), question.getString(3), question.getString(4),question.getString(5));
     }
 
     public List<Question> getQuestions(List questionIds) {
         List<Question> questions = new ArrayList<Question>();
         if (questionIds.equals(new ArrayList())) return questions;

        SqlRowSet advisedQuestions = jdbcTemplate.queryForRowSet("select DISTINCT  q.q_id ,q.post_date,q.question,array_to_string(array_agg(t.tag_name), ' ') as tags from questions q LEFT OUTER JOIN questions_tags qt on q.q_id = qt.q_id LEFT OUTER JOIN tags t on t.t_id=qt.t_id join answers a on q.q_id=a.q_id where q.q_id in (" + convertArrayToString(questionIds) + ") group by q.q_id,q.post_date,q.question order by post_date desc;");
        SqlRowSet QuestionUserNameAndDate = jdbcTemplate.queryForRowSet("select post_date,user_name from questions;");
        List<Question> questionsList = new ArrayList<Question>();
        while (advisedQuestions.next() && QuestionUserNameAndDate.next()) {
            questionsList.add(new Question(advisedQuestions.getString("q_id"),showQuestion.truncateQuestionToCharacterLimit(advisedQuestions.getString("question")),QuestionUserNameAndDate.getString("post_date"),QuestionUserNameAndDate.getString("user_name"),advisedQuestions.getString("tags") ));
         }
        return questionsList;
     }
 
     private String convertArrayToString(List questionIds) {
         String questionIdString = "";
         for (int i = 0; i < questionIds.size(); i++) {
             questionIdString = questionIdString + questionIds.get(i);
             if (i != questionIds.size() - 1) {
                 questionIdString = questionIdString + ",";
             }
         }
         return questionIdString;
     }
 }
