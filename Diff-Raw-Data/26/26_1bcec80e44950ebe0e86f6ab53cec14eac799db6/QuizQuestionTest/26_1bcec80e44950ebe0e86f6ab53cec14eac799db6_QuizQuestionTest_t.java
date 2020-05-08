 package epfl.sweng.test;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONException;
 
 import epfl.sweng.quizquestions.QuizQuestion;
 import epfl.sweng.servercomm.SwengHttpClientFactory;
 import epfl.sweng.tasks.IQuizServerCallback;
 import epfl.sweng.tasks.LoadRandomQuestion;
 
 import junit.framework.TestCase;
 
 /**
  * Unit test for JSON methods in QuizQuestion 
  */
 public class QuizQuestionTest extends TestCase {
     public static final String VALID_QUESTION_JSON = "{"
             + "question: 'What is the answer to life, the universe and everything?', "
             + "answers: ['42', '27'],"
             + "solutionIndex: 0,"
             + "tags : ['h2g2', 'trivia'],"
             + "owner : 'anonymous',"
             + "id : '123'"
             + "}";
 
     public void testQuestionOK() throws JSONException {
         String json = VALID_QUESTION_JSON;
         QuizQuestion result = new QuizQuestion(json);
         assertNotNull(result);
         
         String q = "The Question...";
         List<String> answers = new ArrayList<String>();
         answers.add("Answer 0");
         answers.add("Answer 1");
         answers.add("Answer 2");
         answers.add("Answer 3");
         answers.add("Answer 4");
         answers.add("Answer 5");
         answers.add("Answer 6");
         Set<String> tags = new HashSet<String>();
         tags.add("Tag");
         final int id=13;
         final int rightAnswer=3;
         String owner = "Anonymous";
        QuizQuestion result2 = new QuizQuestion(q, answers, rightAnswer, tags, id, owner);
         assertEquals(owner, result2.getOwner());
        result2.removeAnswerAtIndex(rightAnswer);
         assertEquals(result2.getSolutionIndex(), -1);
         result2.setSolutionIndex(2);
        result2.removeAnswerAtIndex(answers.size()-1);
         result2.removeAnswerAtIndex(0);
         assertEquals(result2.getSolutionIndex(), 1);
         
         SwengHttpClientFactory.setInstance(null);
         new LoadRandomQuestion(new IQuizServerCallback() {
         	public void onSuccess(QuizQuestion question) {
         	}
         	public void onError(Exception except) {
         	}
         }).execute();
     }
 }
