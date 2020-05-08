 package com.forum.repository;
 
 import com.forum.domain.Question;
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
         postQuestion.insert("java", "this is first question for testing which should be trimmed", "Anil");
         postQuestion.insert("java", "this is second question", "Anil");
     }
 
     @After
     public void tearDown() {
         template.execute("delete from answers;");
         template.execute("delete from questions;");
     }
 
     @Test
     public void shouldGiveNewlyInsertedQuestionsOfAGivenPageNumberAccordingToQuestionsPerPage() {
         int questionsPerPage = 2, pageNumber = 1;
         List<Question> result = questions.show(pageNumber, questionsPerPage);
         List<String> actual = new ArrayList<String>();
         for (Question resultedQuestion : result) {
             actual.add(resultedQuestion.getQuestion());
         }
        List<String> expected = Arrays.asList("this is first question for testing which should be...?</p>", "this is second question");
         assertTrue(expected.equals(actual));
     }
 
     @Test
     public void shouldReturnFirst50CharactersOfAQuestionAlongWithThreeTrailingDotsAndQuestionMark() {
         String string = "It should return a new String till the specified Characters";
        String expected = "It should return a new String till the specified C...?</p>";
         assertThat(questions.truncateQuestionToCharacterLimit(string), IsEqual.equalTo(expected));
     }
 
     @Test
     public void shouldReturnFullQuestionIfQuestionIsLessThan50Characters() {
         String question = "Don't trim me";
         assertThat(questions.truncateQuestionToCharacterLimit(question), IsEqual.equalTo(question));
     }
 
     @Test
     public void shouldReturnFullQuestionIfQuestionContainsTags() {
         String question = "Don't trim me <html> <tr> <p> </tr></html> yahoo";
         String expected = "Don't trim me <html> <tr> <p> </tr></html> yahoo";
         assertThat(questions.truncateQuestionToCharacterLimit(question), IsEqual.equalTo(expected));
     }
 }
