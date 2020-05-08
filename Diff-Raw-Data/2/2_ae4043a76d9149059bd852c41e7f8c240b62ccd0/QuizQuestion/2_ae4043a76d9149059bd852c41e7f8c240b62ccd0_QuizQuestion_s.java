 package epfl.sweng.quizquestions;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * Simple data structure holding the data retrieved from the web service
  */
 public class QuizQuestion {
 
 	private final static int MAX_NUMBER_OF_ANSWERS = 10;
 	private final static int MIN_NUMBER_OF_ANSWERS = 2;
 	private final static int MAX_LENGTH_OF_TAGS = 20;
 	private final static int MAX_LENGTH_OF_STRINGS = 500;
     private String mQuestion;
     private List<String> mAnswers;
     private int mSolutionIndex;
     private Set<String> mTags;
     private String mOwner;
     private int mId;
     
     /**
      * Possible QuizQuestion Parameters
      */
     public enum QuizQuestionParam {
     	QUESTION, ANSWER, SOLUTION_INDEX, TAGS, OWNER, ID
     }
     
 	/** The constructor for quiz questions received as JSON strings from the Sweng2012QuizApp server, as in homework #1
 	* @param json The JSON string received from the Sweng2012QuizApp server, as in homework #1
 	*/
 
 	/** The constructor for quiz questions defined by the user
 	* @param text The body of the question, as input by the user
 	* @param answers The list of possible answers of the question, as input by the user
 	* @param solutionIndex The index identifying the correct answer, as input by the user
 	* @param tags The set of tags of the question, as input by the user
 	* @param id The id of the question
 	* @param owner The owner of the question
 	*/
	public QuizQuestion(String txt, List<String> answers, int solutionIdx, Set<String> tags, String id, String owner) {
 		setQuestion(txt);
 		setId(id);
 		setSolutionIndex(solutionIdx);
 		setAnswers(answers);
 		setTags(tags);
 		setOwner(owner);
 	}
 	
     /**
      * Constructor setting default values for the question and the answers
      */
     public QuizQuestion() {
         super();
         mQuestion = "";
         mAnswers = new ArrayList<String>();
         mAnswers.add("");
         mTags = new HashSet<String>();
         mSolutionIndex = -1;
         mOwner = "anonymous";
         mId = 1;
     	
     }
 	
 	/** The constructor for a quiz questions received as a JSON strings.
 	* @param json The JSON string received from the Sweng2012QuizApp server
 	*/
 	public QuizQuestion(String json) throws JSONException {
 		JSONObject responseJson = new JSONObject(json);
 		
 		JSONArray answersJSON = responseJson.getJSONArray("answers");
 		List<String> answers = new ArrayList<String>();
 		for (int i=0; i<answersJSON.length(); i++) {
 			answers.add(answersJSON.getString(i));
 		}
 
 		JSONArray tagsJSON = responseJson.getJSONArray("tags");
 		Set<String> tags = new HashSet<String>();
 		for (int i=0; i<tagsJSON.length(); i++) {
 			tags.add(tagsJSON.getString(i));
 		}
 		
 		setQuestion(responseJson.getString("question"));
 		setId(responseJson.getString("id"));
 		setSolutionIndex(responseJson.getInt("solutionIndex"));
 		setAnswers(answers);
 		setTags(tags);
 		setOwner(responseJson.getString("owner"));
 	
 	}
 	
 	/** Returns the number of rep invariant violations*/
 	public int auditErrors(int depth) {
 		
 		int errorCount = 0;
 
 		//Check that no string is larger than 500 characters nor empty or only whitespaces
 		if (!checkString(mQuestion)) {
 			errorCount++;
 		}
 		Iterator<String> iter1 = mAnswers.iterator();
 		while (iter1.hasNext()) {
 	    	String answer = iter1.next();
 	    	if (!checkString(answer)) {
 	    		errorCount++;
 	    	}
 	    }
 		Iterator<String> iter2 = mTags.iterator();
 		while (iter2.hasNext()) {
 	    	String tag = iter2.next();
 	    	if (!checkString(tag)) {
 	    		errorCount++;
 	    	}
 	    }
 		if (!checkString(mOwner)) {
 			errorCount++;
 		}
 		
 //		NOT NEEDED ANYMORE SINCE ID IS AN INT /DANA		
 //		if (!checkString(mId)) {
 //			errorCount++;
 //		}
 		
 		
 		// Check the number of answers
 		if (!checkNbAnswers()) {
 			errorCount++;
 		}
 		
 		// Check the tags
 		Iterator<String> iter3 = mTags.iterator();
 	    while (iter3.hasNext()) {
 	    	String tag = iter3.next();
 	    	if (!checkTag(tag)) {
 	    		errorCount++;
 	    	}
 	    }
 		
 
 		// Check the index of the right answer
 	    if (mSolutionIndex>=mAnswers.size() || mSolutionIndex<0) {
 	    	errorCount++;
 	    }
 	    
 	    // Check owner
     	boolean ownerOK = true;
 	    for (int i = 0; i < mOwner.length(); i++) {
     	    if (!Character.isLetterOrDigit(mOwner.charAt(i))) {
     	    	ownerOK = false;
     	    	break;
     	    }
     	}
 	    
 	    if (!ownerOK) {
     		errorCount++;
     	}
 		
 	    // Check the ID
 //    	boolean idOK = true;
 //	    for (int i = 0; i < mId.length(); i++) {
 //    	    if (!Character.isLetterOrDigit(mId.charAt(i))) {
 //    	    	idOK = false;
 //    	    	break;
 //    	    }
 //    	}
 //		NOT NEEDED ANYMORE SINCE ID IS AN INT /DANA	    	    
 	    if (mId<=0) {
     		errorCount++;
     	}
 		
 	    
 	    
 	    return errorCount;
 			
 	}
 	
 	/**
 	 * Checks that a String is non empty nor only whitespaces, nor longer than MAX_LENGTH_OF_STRINGS
 	 * @param string The string
 	 * @return true if non empty nor too long
 	 */
 	public boolean checkString(String string) {
 		
 		if (string == null || string.length() == 0) {
 			return false;
 		} else {
 			boolean onlyWhiteSpace = true;
 			for (int i = 0; i < string.length(); i++) {
 				onlyWhiteSpace = onlyWhiteSpace && Character.isWhitespace(string.charAt(i));
 			}
 			return string.length() <= MAX_LENGTH_OF_STRINGS && !onlyWhiteSpace;
 		}
 	}
 	
 	/**
 	 * Checks that the number of answers is between MIN_NUMBER_OF_ANSWERS and MAX_NUMBER_OF_ANSWERS
 	 * @return true if between the bounds
 	 */
 	public boolean checkNbAnswers() {
 		return mAnswers.size()>=MIN_NUMBER_OF_ANSWERS && mAnswers.size()<=MAX_NUMBER_OF_ANSWERS;
 	}
 	
 	/**
 	 * Checks that a tag is shorter than MAX_LENGTH_OF_TAGS and made of alphanumerics characters
 	 * @param tag The tag
 	 * @return true if short enough and alphanumeric
 	 */
 	public boolean checkTag(String tag) {
 		boolean tagOK = true;
 		
     	if (tag.length()>MAX_LENGTH_OF_TAGS) {
     		tagOK = false;
     	
     	} else {
 	    
     		for (int i = 0; i < tag.length(); i++) {
 	    	    if (!Character.isLetterOrDigit(tag.charAt(i))) {
 	    	    	tagOK = false;
 	    	    	break;
 	    	    }
 	    	}
     	}
     	
 	    return tagOK;
 	}
 	
     /**
      * Get multiple choice question
      * @return String The question
      */
     public String getQuestion() {
     	return mQuestion;
     }
     
     /**
      * Get owner of the dataset
      * @return String The owner
      */
     public String getOwner() {
     	return mOwner;
     } 
     
     /**
      * Set owner of the dataset
      * @param String owner
      */
     public void setOwner(String owner) {
     	mOwner = owner;
     }
     
     /**
      * Get an array of possible answers
      * @return String[] string holding the possible answers
      */
     public String[] getAnswers() {
     	return mAnswers.toArray(new String[mAnswers.size()]);
     }
 
     /**
      * Get an array of tags
      * @return String[] string holding the possible answers
      */
     public String[] getTags() {
     	return mTags.toArray(new String[mTags.size()]);
     }
     
     /**
      * Get the index of the right solution in the given array of the possible answers
      * @return int the index of the correct answer
      */
     public int getSolutionIndex() {
     	return mSolutionIndex;
     }
     
     /**
      * Get id of the question-answer dataset
      * @return
      */
     public int getId() {
     	return mId;
     }
     
     /**
      * Set the possible answers
      * @param String[] answers the answers to be set
      */
    /* public void setAnswers(String[] answers) {
     	mAnswers = Arrays.asList(answers);	
     }*/
     
     /**
      * Set the possible answers
      * @param List<String> answers the answers to be set
      */
     public void setAnswers(List<String> answers) {
     	mAnswers = answers;	
     }
     
     /**
      * Add an answer or modify an existing one
      * @param answer Answer to be added/modified
      * @param index The index of the answer
      */
     public void addAnswerAtIndex(String answer, int index) {
     	// Replace an existing answer
     	if (mAnswers.size() >= index+1) {
     		mAnswers.set(index, answer);
     	// Push a new answer
     	} else if (mAnswers.size() == index) {
     		mAnswers.add(answer);
     	// Add empty answers and push new one
     	} else {
     		while (mAnswers.size() < index) {
     			mAnswers.add("");
     		}
     		mAnswers.add(answer);
     	}
     	System.out.println("Size of mAnswers: " + mAnswers.size());
     	
     }
     
     /**
      * Removes the answer at a given index from mEditedQuestion 
      * @param index The index of the answer to be removed
      */
     public void removeAnswerAtIndex(int index) {
 
     	if (mSolutionIndex < index) {
     		mAnswers.remove(index);
     	} else if (mSolutionIndex == index) {
     		mSolutionIndex = -1;
     		mAnswers.remove(index);
     	} else {
     		mSolutionIndex--;
     		mAnswers.remove(index);
     	}
     }
     
     /**
      * Set the possible tags
      * @param Set<String> tags the tags to be set
      */
     public void setTags(Set<String> tags) {
     	mTags = tags;
     	for (String tag : mTags) {
     		if (tag.equals("")) {
     			mTags.remove(tag);
     		}
     	}
     }
 
     /**
      * Set the question
      * @param String question the question to be set
      */
     public void setQuestion(String question) {
     	mQuestion = question;
     }
     
     /**
      * Set the index of the right solution in the given array of the possible answers
      * @param int solutionIndex the index of the correct answer
      */
     public void setSolutionIndex(int solutionIndex) {
     	mSolutionIndex = solutionIndex;
     }
     
     /**
      * Set the id of the question-answers dataset
      * @param String id the id of the dataset
      */
     public void setId(String id) {
     	mId = Integer.parseInt(id);
     }
 
     /**
      * Set the id of the question-answers dataset
      * @param int id the id of the dataset
      */
     public void setId(int id) {
     	mId = id;
     }
     
     /**
      * 
      * @return String the question json string as understood by the Sweng Quiz Server
      * @throws JSONException
      */
     public String getJSONString() throws JSONException {
 
     	JSONObject json = new JSONObject();
     	JSONArray answersArray = new JSONArray();
     	JSONArray tagsArray = new JSONArray();
     	
     	
     	
 
     	for (String answer : mAnswers) {
 	    	answersArray.put(answer);
     	}
     	json.put("answers", answersArray);
     	
 
     	for (String tag : mTags) {
         	tagsArray.put(tag);
         }
     	json.put("tags", tagsArray);
     	
     	json.put("question", mQuestion); 
     	json.put("solutionIndex", mSolutionIndex); 
     	
     	return json.toString();
 
     }
     
 }
