 import java.util.List;
 import org.junit.*;
 import play.test.*;
 import models.*;
 import play.db.jpa.GenericModel.JPAQuery;
 
 public class BasicTest extends UnitTest {
 
     @Before
     public void setup() {
         Fixtures.deleteAllModels();
     }
  
     @Test
     public void createAndRetrieveUser() {
         TykmihUser user = new TykmihUser("test@chrysostom-family.com", "pass123", "Sam Winchester");
         user.save();
         
         TykmihUser retrieveUser = TykmihUser.find("byEmail", "test@chrysostom-family.com").first();
         
         assertNotNull(retrieveUser);
         assertEquals("Sam Winchester", retrieveUser.fullname);
     }
     
     @Test
     public void createAndRetrieveQuestion() {
        Question q = new Question("Phalanx", "What maneuver did Sparta...", "What maneuver did Sparta use with the Greeks?");
         q.save();
         
         Question quesRetr = Question.find("byTitle", "Phalanx").first();
         
         assertNotNull(quesRetr);
         assertEquals("Phalanx", quesRetr.title);
     }
     
     @Test
     public void createAndRetrieveAnswer() {
        Question q = new Question("Question 2", "Short display q", "Long display question here.");
         q.save();
         
         Answer a = new Answer(q, "A", "Short answer.", "The very long answer with explanation.", true);
         a.save();
         
         Answer answerRetr = Answer.find("byQuestion", q).first();
         
         assertNotNull(answerRetr);
         assertEquals("Short answer.", answerRetr.shortText);
         assertEquals("Question 2", answerRetr.question.title);
         
     }
     
     @Test
     public void createMultiAnswers() {
        Question q = new Question("Question 3", "Short display q3", "Long display question three here.");
         q.save();
         
         Answer a = new Answer(q, "A", "Short answer A.", "The very long answer with explanation for A", true);
         a.save();
         Answer b = new Answer(q, "B", "Short answer B.", "The very long answer with explanation for B", false);
         b.save();
         
         JPAQuery query = Answer.find("byQuestion", q);
         List<Answer> answerListRetr = query.fetch();
         
         assertNotNull(answerListRetr);
         assertEquals(2, answerListRetr.size());
         assertTrue(answerListRetr.get(1).label.equals("A") || answerListRetr.get(1).label.equals("B"));
         
     }
 
 }
