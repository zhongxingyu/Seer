 package com.forum.repository;
 
 import org.hamcrest.core.IsEqual;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import javax.sql.DataSource;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class ShowQuestionsTest {
     private ApplicationContext context = new ClassPathXmlApplicationContext("file:./config.xml");
     private ShowQuestions questions;
     private PostQuestion postQuestion;
     private JdbcTemplate template;
 
     @Before
     public void setup() {
         questions = (ShowQuestions) context.getBean("showQuestions");
         postQuestion = (PostQuestion) context.getBean("post");
         template = new JdbcTemplate((DataSource) context.getBean("dataSource"));
         postQuestion.insert("this is first question for testing which should be trimmed","Anil");
         postQuestion.insert("this is second question","Anil");
     }
 
     @After
     public void tearDown() {
         template.execute("delete from questions;");
     }
 
     @Test
     public void shouldGiveNewlyInsertedQuestionsOfAGivenPageNumberAccordingToQuestionsPerPage() {
         int questionsPerPage = 2, pageNumber = 1;
         List<Question> result = questions.show(pageNumber, questionsPerPage);
        List<String> expected = Arrays.asList("this is first question for testing which should be...?", "this is second question");
         List<String> actual = new ArrayList<String>();
        actual.add(result.get(0).getQuestion());
        actual.add(result.get(1).getQuestion());
         assertTrue(expected.equals(actual));
     }
 
     @Test
     public void shouldReturnFirst50CharactersOfAQuestionAlongWithThreeTrailingDotsAndQuestionMark() {
         String string = "It should return a new String till the specified Characters";
         String expected = "It should return a new String till the specified C...?";
         assertThat(questions.truncateQuestionToCharacterLimit(string), IsEqual.equalTo(expected));
     }
 
     @Test
     public void shouldReturnFullQuestionIfQuestionIsLessThan50Characters() {
         String question = "Don't trim me";
         assertThat(questions.truncateQuestionToCharacterLimit(question), IsEqual.equalTo(question));
     }
 }
