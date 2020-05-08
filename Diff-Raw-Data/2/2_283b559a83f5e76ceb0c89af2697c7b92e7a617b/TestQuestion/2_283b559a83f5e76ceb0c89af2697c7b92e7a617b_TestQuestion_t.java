 package test;
 
 import junit.framework.TestCase;
 import domain.Question;
 
 /**
  * This class is about testing a Question Object
  * @author Metas Pongmetha
  * @version 2012/10/24
  */
 public class TestQuestion extends TestCase {
 	// relate attribute	to test
 	private Question q  = new Question(1,"What your Name?");
 	private String newQuestion = "What is your name?";
 
 	/**
 	 *  Test object have to be created
 	 */
 	public void testQuestion() {
 		Question tempq = q;
 		assertSame(tempq, q);
 		Question temp = null;
 		assertNotSame(q, temp);
 		assertNotNull(q);
 	}
 
 	/**
 	 * Test setQuestion method of Question Object.
 	 */
 	public void testSetQuestion() {
 		Question tempqq = q;
 		tempqq.setQuestion(newQuestion);
 		assertFalse(q.getQuestion() != tempqq.getQuestion());
		assertEquals("What is your name?", tempqq.getQuestion());
 	}
 
 	/**
 	 *  Test GetQuestionID method of Question Object.
 	 */
 	public void testGetQuestionID() {
 		assertEquals(1, q.getQuestionID());
 	}
 
 	/**
 	 * Test GetQuestion method of Question Object.
 	 */
 	public void testGetQuestion() {
 		assertEquals("What your Name?", q.getQuestion());
 	}
 
 	/**
 	 *  Test toString method of Question Object.
 	 */
 	public void testToString() {
 		assertEquals("What your Name?",q.toString());
 	}
 
 }
