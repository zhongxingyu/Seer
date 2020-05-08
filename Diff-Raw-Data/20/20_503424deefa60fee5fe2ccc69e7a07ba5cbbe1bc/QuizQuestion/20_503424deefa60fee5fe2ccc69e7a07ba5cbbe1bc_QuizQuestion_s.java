 package epfl.sweng.quizquestions;
 
 import java.util.Arrays;
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
	private final static int MAX_NUMBER_OF_ANSWERS = 2;
	private final static int MIN_NUMBER_OF_ANSWERS = 10;
	private final static int MAX_NUMER_OF_TAGS = 20;
     private String mQuestion;
     private List<String> mAnswers;
     private int mSolutionIndex;
     private Set<String> mTags;
     private String mOwner;
     private String mId;
     
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
         /*mQuestion = "Choose an answer:";
         mAnswers = new String[] {"Answer 1", "Answer 2", "Answer 3", "Answer 4"};
         mTags = new String[] {"Answer 1", "Answer 2", "Answer 3", "Answer 4"};
         mSolutionIndex = 1;*/
     	
     }
 	
 	/** The constructor for a quiz questions received as a JSON strings.
 	* @param json The JSON string received from the Sweng2012QuizApp server
 	*/
 	public QuizQuestion(String json) throws JSONException {
 		JSONObject responseJson = new JSONObject(json);
 		
 		JSONArray answersJSON = responseJson.getJSONArray("answers");
 		String[] answers = new String[answersJSON.length()];
 		for (int i=0; i<answersJSON.length(); i++) {
 			answers[i]=answersJSON.getString(i);
 		}
 
 		JSONArray tagsJSON = responseJson.getJSONArray("tags");
 		String[] tags = new String[tagsJSON.length()];
 		for (int i=0; i<tagsJSON.length(); i++) {
 			tags[i]=tagsJSON.getString(i);
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
 
 		//Check that no string is larger than 500 characters
 		
 		
 		// Check the number of answers
 		if (mAnswers.size()<MIN_NUMBER_OF_ANSWERS || mAnswers.size()>MAX_NUMBER_OF_ANSWERS) {
 			errorCount++;
 		}
 		
 		// Check the tags
 		Iterator<String> iter = mTags.iterator();
 	    while (iter.hasNext()) {
 	    	boolean tagOK = true;
 	    	String tag = iter.next();
 	    	
	    	if (tag.length()>MAX_NUMER_OF_TAGS) {
 	    		tagOK = false;
 	    	
 	    	} else {
 		    
 	    		for (int i = 0; i < tag.length(); i++) {
 		    	    if (Character.isLetterOrDigit(tag.charAt(i))) {
 		    	    	tagOK = false;
 		    	    	break;
 		    	    }
 		    	}
 	    	}
 	    	
 	    	if (!tagOK) {
 	    		errorCount++;
 	    		break;
 	    	}
 	    }
 		
 
 		// Check the id of the right answer
 	    if (mSolutionIndex>=mAnswers.size()) {
 	    	errorCount++;
 	    }
 	    
 	    // Check owner
     	boolean ownerOK = true;
 	    for (int i = 0; i < mOwner.length(); i++) {
     	    if (Character.isLetterOrDigit(mOwner.charAt(i))) {
     	    	ownerOK = false;
     	    	break;
     	    }
     	}
 	    
 	    if (!ownerOK) {
     		errorCount++;
     	}
 		
 	    // Check the ID
     	boolean idOK = true;
 	    for (int i = 0; i < mId.length(); i++) {
     	    if (Character.isLetterOrDigit(mId.charAt(i))) {
     	    	idOK = false;
     	    	break;
     	    }
     	}
 	    
 	    if (!idOK) {
     		errorCount++;
     	}
 		
 	    
 	    
 	    return errorCount;
 			
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
     public String getId() {
     	return mId;
     }
     
     /**
      * Set the possible answers
      * @param String[] answers the answers to be set
      */
     public void setAnswers(String[] answers) {
     	mAnswers = Arrays.asList(answers);	
     }
     
     /**
      * Set the possible answers
      * @param List<String> answers the answers to be set
      */
     public void setAnswers(List<String> answers) {
     	mAnswers = answers;	
     }
     
     /**
      * Set the possible tags
      * @param String[] tags the tags to be set
      */
     public void setTags(String[] tags) {
     	mTags = new HashSet<String>(Arrays.asList(tags));
     }
     
     /**
      * Set the possible tags
      * @param Set<String> tags the tags to be set
      */
     public void setTags(Set<String> tags) {
     	mTags = tags;
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
      * @param int id the id of the dataset
      */
     public void setId(String id) {
     	mId = id;
     }
 
     public String getJSONString() throws JSONException {
 
     	JSONObject json = new JSONObject();
     	
     	json.put("question", mQuestion); 
     	
     	for (String answer : mAnswers) {
     		json.accumulate("answers", answer);
     	}
     	json.put("solutionIndex", mSolutionIndex); 
 
     	for (String tag : mTags) {
     		json.accumulate("tags", tag);
     	}
     	
     	return json.toString();
 
     }
     
 }
