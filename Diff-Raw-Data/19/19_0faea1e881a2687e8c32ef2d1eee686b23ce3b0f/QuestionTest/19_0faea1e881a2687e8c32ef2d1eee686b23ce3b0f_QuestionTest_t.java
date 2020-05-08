 package quizweb.question;
 
 import static org.junit.Assert.*;
 
 import java.util.*;
 
 import org.junit.*;
 
 import quizweb.*;
 
 public class QuestionTest {
 
 	@Before
 	public void setUp() throws Exception {
 		XMLReader.loadAllXML();
 	}
 	
 	@Test
 	public void testQuestion() {
 		String[] stringArray = {"foo", "bar", "test", "temp"};
 		ArrayList<String> stringList = new ArrayList<String>();
 		Collections.addAll(stringList, stringArray);
 		ArrayList<String> retList = Question.getParsedStrings(Question.getConcatedString(stringList));
 		assertEquals(stringList.size(), retList.size());
 		for (int i = 0; i < retList.size(); i++) {
 			assertEquals(stringList.get(i), retList.get(i));
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testQuestion2() {
 		Quiz quiz = Quiz.getQuizByQuizName("Bunny Quiz");
 		ArrayList<Question> questions = Question.getQuestionsByQuizID(quiz.quizID);
 		assertEquals(questions.size(), 7);
 		for (int i = 0; i < questions.size(); i++) {
 			Question question = questions.get(i);
 			assertEquals(question.position, i);
 			if (question instanceof ResponseQuestion) {
 				assertEquals(((ArrayList<String>)question.answer).get(0), "Leporidae");
 				String userAnswer1 = "Leporidae";
 				String userAnswer2 = "I don't know";
 				assertEquals(question.getScore(userAnswer1), 10, 1e-6);
 				assertEquals(question.getScore(userAnswer2), 0, 1e-6);
 			} else if (question instanceof FillInBlankQuestion) {
 				assertEquals(((ArrayList<String>)question.answer).get(0), "Watership");
 				String userAnswer1 = "Watership";
 				String userAnswer2 = "Watergate";
 				assertEquals(question.getScore(userAnswer1), 10, 1e-6);
 				assertEquals(question.getScore(userAnswer2), 0, 1e-6);				
 			} else if (question instanceof MultipleChoiceQuestion) {
 				assertTrue(((String)question.answer).startsWith("In Stanford tradition"));
 				assertEquals(((ArrayList<String>)question.question).size()-1, 3);
 				String userAnswer1 = "In Stanford tradition, rubbing the nose of a rabbit guarantees an A on the CS108 class project";
 				String userAnswer2 = "Aztec mythology includes a rabbit pantheon.";
 				assertEquals(question.getScore(userAnswer1), 10, 1e-6);
 				assertEquals(question.getScore(userAnswer2), 0, 1e-6);
 			} else if (question instanceof PictureQuestion) {
 				assertEquals(((ArrayList<String>)question.answer).size(), 2);
 				assertTrue(((PictureQuestion)question).questionURL.endsWith("Bugs_Bunny_Pose.PNG"));
 				String userAnswer1 = "Bugs";
 				String userAnswer2 = "Bugs Bunny";
 				String userAnswer3 = "Computer";
 				assertEquals(question.getScore(userAnswer1), 10, 1e-6);
 				assertEquals(question.getScore(userAnswer2), 10, 1e-6);
 				assertEquals(question.getScore(userAnswer3), 0, 1e-6);				
 			} else if (question instanceof MatchingQuestion) {
 				ArrayList<Integer> indices = ((MatchingQuestion) question).getCorrectChoiceIndexes();
 				assertEquals(indices.get(0).intValue(), 3);
 				assertEquals(indices.get(1).intValue(), 4);
 				assertEquals(indices.get(2).intValue(), 1);
 				assertEquals(indices.get(3).intValue(), 2);	
 				String[] userAnswerArray1 = {"Bunny answer 1", "Bunny answer 2", "Bunny answer 4", "Bunny answer 3"};
 				String[] userAnswerArray2 = {"Bunny answer 3", "Bunny answer 2", "Bunny answer 4", "Bunny answer 1"};
 				String[] userAnswerArray3 = {"Bunny answer 2", "Bunny answer 3", "Bunny answer 1"};
 				assertEquals(question.getScore(ArrayToList(userAnswerArray1)), 6, 1e-6);
 				assertEquals(question.getScore(ArrayToList(userAnswerArray2)), 3, 1e-6);
 				assertEquals(question.getScore(ArrayToList(userAnswerArray3)), 0, 1e-6);					
 			} else if (question instanceof MultiAnswerQuestion) {
 				assertEquals(((MultiAnswerQuestion)question).answerNumber, 3);
 				assertEquals(((ArrayList<String>)question.answer).size(), 4);
 				String[] userAnswerArray1 = {"B", "C", "D"};
 				String[] userAnswerArray2 = {"U", "Y", "N"};
 				String[] userAnswerArray3 = {"B", "U", "Y"};
 				assertEquals(question.getScore(ArrayToList(userAnswerArray1)), 5.0/3, 1e-6);
 				assertEquals(question.getScore(ArrayToList(userAnswerArray2)), 5, 1e-6);
 				assertEquals(question.getScore(ArrayToList(userAnswerArray3)), 5, 1e-6);					
 			} else if (question instanceof MultiChoiceMultiAnswerQuestion) {
				assertEquals(((ArrayList<String>)question.question).size(), 6);
 				assertEquals(((ArrayList<String>)question.answer).size(), 2);
 				String[] userAnswerArray1 = {"This is a correct answer about Bunny.", "This is another correct answer about Bunny."};
 				String[] userAnswerArray2 = {"This is a correct answer about Bunny.", "This is another correct answer about Bunny.", "This is a wrong answer about Bunny."};
 				String[] userAnswerArray3 = {"This is a correct answer about Bunny.", "This is a third wrong answer about Bunny."};
 				assertEquals(question.getScore(ArrayToList(userAnswerArray1)), 15, 1e-6);
 				assertEquals(question.getScore(ArrayToList(userAnswerArray2)), 12, 1e-6);
 				assertEquals(question.getScore(ArrayToList(userAnswerArray3)), 9, 1e-6);				
 			} else {
 				assertTrue(false);
 			}
 		}
 		quiz = Quiz.getQuizByQuizName("Cities");
 		questions = Question.getQuestionsByQuizID(quiz.quizID);
 		assertEquals(questions.size(), 6);
 	}
 
 	private ArrayList<String> ArrayToList(String[] userAnswerArray) {
 		ArrayList<String> answerList = new ArrayList<String>();
 		Collections.addAll(answerList, userAnswerArray);
 		return answerList;
 	}
 
 }
