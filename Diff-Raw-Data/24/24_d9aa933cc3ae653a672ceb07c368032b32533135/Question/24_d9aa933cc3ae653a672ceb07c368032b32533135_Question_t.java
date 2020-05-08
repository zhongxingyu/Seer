 package question;
 
 import java.sql.Connection;
 import java.util.HashMap;
 import java.util.Map;
 
 import quiz.DataBaseObject;
 
 public abstract class Question extends DataBaseObject{
 	
 	private int quizID;
 	private int questionNumber;
 	private QUESTION_TYPE type;
 	protected Map<String, String> attributes;
 	
 	
 	public static enum QUESTION_TYPE {
 		MULTI_CHOICE,
 		FILL_IN,
 		PICTURE;
 	}
 	
 	public static enum QUESTION_ATTRIBUTE {
 		
 	}
 	
 	/**
 	 * Factory constructor. Parses the attributes to determine the type of question
 	 * we want, then calls the proper constructor and returns the result.
 	 * @param attrs
 	 * @param conn
 	 * @return a new Question object
 	 */
 	public static Question build(String[] attrs, Connection conn){
 		QUESTION_TYPE type = serializeType(attrs[3]);
 		switch(type){
 		case MULTI_CHOICE: return new MultiChoiceQuestion(attrs, conn);
 		case FILL_IN: return new FillInQuestion(attrs, conn);
 		case PICTURE: return new PictureQuestion(attrs, conn);
 		}
 		return null;
 	}
 
 	/**
 	 * DataBaseObject constructor.
 	 * Initializes fields from database, and loads attributes map from the attributes table.
 	 * TODO: create constructor for creating new (ie, not yet in database) questions
 	 * @param attrs
 	 * @param conn
 	 */
 	
 	protected Question(String[] attrs, Connection conn) {
 		super(attrs, conn);
 		quizID = Integer.parseInt(attrs[1]);
 		questionNumber = Integer.parseInt(attrs[2]);
 		type = serializeType(attrs[3]);
 		
 		attributes = new HashMap<String, String>();
 		//TODO: SELECT * FROM Question_Attribute WHERE question_id = dbID
 		//then put <attr_type, attr value> pairs into attributes map
 	}
 	
	private static QUESTION_TYPE serializeType(String attr){ //small note: pretty sure it starts with 1, not 0
 		int val = Integer.parseInt(attr);
		switch(val){ 
 		case 0: return QUESTION_TYPE.MULTI_CHOICE; 
 		case 1: return QUESTION_TYPE.FILL_IN;
 		case 2: return QUESTION_TYPE.PICTURE;
 		}
 		return null;
 	}
 
 
 	@Override
 	public void saveToDataBase(Connection conn) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public int getQuizID() {
 		return quizID;
 	}
 
 	public void setQuizID(int quizID) {
 		this.quizID = quizID;
 	}
 
 	public int getQuestionNumber() {
 		return questionNumber;
 	}
 
 	public void setQuestionNumber(int questionNumber) {
 		this.questionNumber = questionNumber;
 	}
 
 	public QUESTION_TYPE getType() {
 		return type;
 	}
 
 	public void setType(QUESTION_TYPE type) {
 		this.type = type;
 	}
 
 }
