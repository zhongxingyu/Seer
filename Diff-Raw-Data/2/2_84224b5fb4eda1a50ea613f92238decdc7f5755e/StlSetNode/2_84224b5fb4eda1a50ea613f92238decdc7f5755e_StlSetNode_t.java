 package smartest;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 /**
  * The Class StlSetNode.
  * StlSetNode  : '[' OptionalQuestionList ']'
  * Example: set s = [q1, q2, q3]
  * Semantics: OptionalQuestionList must be of type question
  * @author Parth
  */
 public class StlSetNode {
 	
 	/** The Question array list. */
 	ArrayList<Question> QuestionArrayList;
 	
 	
 	//hashmap to store info about a question is asked or not
 	/** The Question is asked hm. */
 	HashMap<Question, Boolean> QuestionIsAskedHM = new HashMap<Question, Boolean>();
 
 	
         public StlSetNode()
         {
                 QuestionArrayList = new ArrayList<Question>();
         }
 
         public StlSetNode(QuestionList ql)
         {
                 QuestionArrayList = ql.getQuestions();
         }
 	/**
 	 * Gets the question array list.
 	 *
 	 * @return the question array list
 	 */
 	public ArrayList<Question> getQuestionArrayList() {
 		return QuestionArrayList;
 	}
 
 	/**
 	 * Sets the question array list.
 	 *
 	 * @param questionArrayList the new question array list
 	 */
	public void setQuestionArrayList(ArrayList<Question> questionArrayList) {
 		QuestionArrayList = questionArrayList;
 	}
 
 	/**
 	 * Gets the question is asked hash map
 	 *
 	 * @return the question is asked hash map
 	 */
 	public HashMap<Question, Boolean> getQuestionIsAskedHM() {
 		return QuestionIsAskedHM;
 	}
 
 	/**
 	 * Sets the question is asked hash map.
 	 *
 	 * @param questionIsAskedHM the question is asked hash map
 	 */
 	public void setQuestionIsAskedHM(
 			HashMap<Question, Boolean> questionIsAskedHM) {
 		QuestionIsAskedHM = questionIsAskedHM;
 	}
 
 	/**
 	 * Adds the question.
 	 * Add question literal node to set
 	 * @param q the question literal node
 	 */
 	public StlSetNode addQuestion(Question q){
 		this.getQuestionArrayList().add(q);
                 return this;
 	}
 		
 	//mark if the question is asked from the set or not
 	/**
 	 * Mark is checked true.
 	 *
 	 * @param s the stlset node
 	 * @param q the question literal node
 	 */
 	public void markIsCheckedTrue(StlSetNode s, Question q){
 		s.getQuestionIsAskedHM().put(q, true);
 		
 	}
 	
 }
