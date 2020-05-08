 package quizsite.models;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import quizsite.util.DatabaseConnection;
 import quizsite.util.PersistentModel;
 
 public abstract class Question extends PersistentModel {
 	
 	protected Set<String> answers;
 	protected String text;
 	private String type;
 	
 	public enum Type{
 		CHECKBOX("checkbox"), 
 		FILL_BLANK("fill_blank"), 
 		PICTURE("picture"), 
 		RADIO("radio"), 
 		RESPONSE("response");
 		
 		private final String repr;
 		Type(String repr) {
 			this.repr = repr;
 		}
 		@Override
 		public String toString() {
 			return getRepr();
 		}
 		private String getRepr() {
 			return this.repr;
 		}		
 	};
 	
 	public static String TABLE_NAME = "Question";
	public static String[][] SCHEMA = { {"quiz_id", "INTEGER"}, {"body", "TEXT"}, {"type", "TINYTEXT"} };
 	public static String[][] FOREIGN_KEYS = { {"quiz_id", "Quiz", "id"} };
 //	public static String[] INDEX = {"type"}; /** Which columns should be indexed for faster search? */
 	
 	@Override
 	public Object[] getFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Question(String text, Set<String> answers) throws SQLException
 	{
 		super(TABLE_NAME, SCHEMA, FOREIGN_KEYS);
 		this.text 	 = text;
 		this.answers = answers;
 	}
 
 	
 	public static List<Question> index() throws SQLException {
 		List<List<String> > allRows = DatabaseConnection.index(TABLE_NAME);
 		
 		return null;
 	}
 	
 	@Override
 	public int save() throws SQLException {
 		return 0;
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	 * Given user's answers as a set of strings, the total score will be an intersection of two sets   
 	 * @param userAnswers Set of user answers
 	 * @return number of matched answers
 	 */
 	public int getScore(Set<String> userAnswers) {
 		int score = 0;
 		for (Iterator<String> itr = userAnswers.iterator(); itr.hasNext();) {
 			String answ = (String) itr.next();
 			if (answers.contains(answ))
 				score++;
 		}
 		return score;
 	}
 	
 	public String getText()
 	{ return text; }
 
 
 	public static Question get(int id) throws SQLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void parse(List<String> dbEntry) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 	
 	public void setType(Type type) {
 		setType(type.toString());
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	
 }
