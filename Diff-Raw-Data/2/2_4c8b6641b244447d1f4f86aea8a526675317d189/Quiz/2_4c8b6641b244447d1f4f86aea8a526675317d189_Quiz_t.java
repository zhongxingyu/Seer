 package quizsite.models;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Collections;
 
 import quizsite.models.Message.Type;
 import quizsite.util.Activity;
 import quizsite.util.DatabaseConnection;
 import quizsite.util.PersistentModel;
 
 public class Quiz extends PersistentModel{
 
 	/* Quiz settings */
 	private boolean onePage;
 	private boolean practice;
 	private boolean immediateCheck;
 	private boolean randomized;
 	private String title;
 	private String descr; 
 	private String category;
 
 	private int creatorID;
 
 	private String url;	// url to access quiz
 
 	public static String TABLE_NAME = "Quiz";
 	public static String[][] SCHEMA = {{"creator_id", "INTEGER"}, {"random", "BOOL"}, 
 		{"practice", "BOOL"}, {"immediate", "BOOL"}, {"one_page", "BOOL"}, {"title", "TEXT"}, {"descr", "TEXT"}, {"category", "TEXT"}};
 	public final static int I_CREATORID = 2, I_RANDOM = 3, I_PRAC = 4, I_IMMED = 5, I_ONEPAGE = 6, I_TITLE = 7, I_DESCR = 8, I_CATEGORY = 9; // TODO: @makazone - replace magic numbers by N_PRE_COL
 	public static String[][] FOREIGN_KEYS = {{"creator_id", "User", "id"}};
 
 	public Quiz(String title, String descr, String category, boolean onePage, boolean practice, boolean immediateCheck, boolean random, int creatorID) throws SQLException
 	{
 		super(TABLE_NAME, SCHEMA, FOREIGN_KEYS);
 		this.title			= title;
 		this.descr			= descr;
 		this.category		= category;
 		this.randomized		= random;
 		this.onePage	 	= onePage;
 		this.practice 	 	= practice;
 		this.immediateCheck = immediateCheck;
 		this.creatorID 		= creatorID;
 		if(creatorID == 0)
 			System.err.println(" THIS IS A ZERO USER MADE ON QUIZ: " + title);
 
 	}
 	
 	private Quiz() throws SQLException
 	{
 		super(TABLE_NAME, SCHEMA, FOREIGN_KEYS);
 	}
 
 	/** Overriding so that an update achievements trigger can be */
 	@Override
 	public int save() throws SQLException {
 		int id = super.save();
 		User.updateCreatorAchievements(creatorID);
 		return id;
 	}
 	
 	/**
 	 * Depending on the randomized flag, either shuffles an ArrayList of Questions 
 	 * or returns them in the order they've been added to a DB. 
 	 * @return ArrayList of Questions
 	 * @throws SQLException 
 	 */
 	public List<Question> getQuestions() throws SQLException
 	{
 		List<Question> questions = Question.indexByQuizID(getId());
 		if (isRandomized())
 			Collections.shuffle(questions);
 		return questions;
 	}
 	
 	
 	public static List<Quiz> index() throws SQLException {
 		List<List<String> > allRows = DatabaseConnection.index(TABLE_NAME);
 		List<Quiz> quizzes			= new ArrayList<Quiz>();
 		
 		for (List<String> row : allRows) {
 			Quiz currQuiz = new Quiz();
 			currQuiz.parse(row);
 			quizzes.add(currQuiz);
 		}
 		return quizzes;
 	}
 	
 	/**
 	 * Leverages index() to return an index of the quizzes by creation time. Uses
 	 * an anonymous Comparator class.
 	 */
 	public static List<Quiz> indexByCreationTime() throws SQLException {
 		List<Quiz> quizList = Quiz.index();
 		Collections.sort(quizList, 
 			new Comparator<Quiz>() {
 				@Override
 				public int compare(Quiz q1, Quiz q2) {
 					return q1.getCreatedAt().compareTo(q2.getCreatedAt());
 				}
 			});
 
 //TESTING Code
 //	for(Quiz quiz : quizList) {
 //			System.err.println(quiz.getCreatedAt());
 //		}
 		
 		return quizList;
 	}
 	
 	public static List<Quiz> indexByNumberOfAttempts() throws SQLException {
 		List<Quiz> quizList = Quiz.index();
 		Collections.sort(quizList,
 			new Comparator<Quiz>() {
 				@Override
 				public int compare(Quiz q1, Quiz q2) {
 					try {
 						return q1.getNumberOfAttempts() - q2.getNumberOfAttempts();
 					} catch (Exception e) {
 						System.err.println("Error finding quiz num attempts in indexByNumberOfAttempts");
 						e.printStackTrace();
 						return 0;
 					}
 				}
 			});
 		
 //TESTING Code
 //		for(Quiz quiz : quizList) {
 //			System.err.println(quiz.getNumberOfAttempts());
 //		}
 		
 		return quizList;
 	}
 	
 	public static List<Quiz> parseRows(List<List<String> > rows) throws SQLException {
 		List<Quiz> ret = new ArrayList<Quiz>();
 		for (List<String> row : rows) {
 			if (row != null) {
 				Quiz curr = new Quiz();
 				curr.parse(row);
 				ret.add(curr);
 			} else {
 				ret.add(null);
 			}
 		}
 		return ret;
 	}
 	
 	/** Returns a list of quizzes created by a user */
 	public static List<Quiz> indexCreatedBy(User user) throws SQLException {
 		String[][] conditions = {{"creator_id", "=", "" + user.getId()}};
 		return parseRows(DatabaseConnection.indexWhere(TABLE_NAME, conditions));
 	}
 
 	/** Returns a list of quizzes created by a user and in order of date */
 	public static List<Quiz> indexCreatedByUserAndByDate(User user) throws SQLException {
 		List<Quiz> quizzes = Quiz.indexCreatedBy(user);
 		Collections.sort(quizzes,
 			new Comparator<Quiz>() {
 				@Override
 				public int compare(Quiz q1, Quiz q2) {
 					return q1.getCreatedAt().compareTo(q2.getCreatedAt());
 				}
 			});
 		return quizzes;
 	}
 
 	@Override
 	public Object[] getFields() {
 		Object[] objs = new Object[] {getCreatorID(), setBool(isRandomized()), setBool(isPracticeEnabled()), setBool(isImmediate()), setBool(isOnePage()), getTitle(), getDescr(), getCategory()};
 		return objs;
 	}
 
 	public static Quiz get(int id) throws SQLException {
 		List<String> entry = DatabaseConnection.get(TABLE_NAME, id);
 		Quiz obj = new Quiz();
 		obj.parse(entry);
 		return obj;
 	}
 
 	@Override
 	public void parse(List<String> dbEntry) throws IllegalArgumentException, SQLException {
 		super.parse(dbEntry);
 		
 		setCreatorID(Integer.parseInt(dbEntry.get(I_CREATORID)));
 		setImmediateCheckEnab(getBool(dbEntry.get(I_IMMED)));
 		setRandom(getBool(dbEntry.get(I_RANDOM)));
 		setOnePage(getBool(dbEntry.get(I_ONEPAGE)));
 		setPractiseEnab(getBool(dbEntry.get(I_PRAC)));
 		setTitle(dbEntry.get(I_TITLE));
 		setDescr(dbEntry.get(I_DESCR));
 		setCategory(dbEntry.get(I_CATEGORY));
 	}
 	
 	private boolean getBool(String bstr)
 	{ return (bstr.equals("1"))?true:false; }
 	
 	private int setBool(boolean b)
 	{ return (b)?1:0; }
 
 	/*G&S*/
 	public boolean isOnePage()
 	{ return onePage; }
 	
 	public boolean isPracticeEnabled()
 	{ return practice; }
 	
 	public boolean isImmediate()
 	{ return immediateCheck; }
 	
 	public boolean isRandomized()
 	{ return randomized; }
 	
 	public int getCreatorID() { 
 		if(creatorID == 0) {
 			System.err.print("Quiz.java:217 Zero Creator ID error: " + creatorID + "QuizId: " + this.getId());
 		}
 		return creatorID; }
 	public void setCreatorID(int creatorId) {
 		this.creatorID = creatorId;
 	}
 
 	// URL to access quiz
 	public String getURL() 
 	{ return "takeQuiz.jsp?quizid=" + getId(); } 
 	
 	public void setOnePage(boolean prop)
 	{ onePage = prop; }
 	
 	public void setRandom(boolean prop)
 	{ randomized = prop; }
 	
 	public void setPractiseEnab(boolean prop)
 	{ practice = prop; }
 	
 	public void setImmediateCheckEnab(boolean prop)
 	{ immediateCheck = prop; }
 	
 	public void setUrl(String newUrl)
 	{ url = newUrl; }
 	
 	public String getTitle() { return title; }
 	
 	public void setTitle(String newTitle) { title = newTitle; }
 	
 	public String getDescr() { return descr; }
 	
 	public void setDescr(String newDescr) { descr = newDescr; }
 	
 	public String getCategory() { return category; }
 	
 	public void setCategory(String newCategory) { category = newCategory; }
 
 	public List<Attempt> getAttempts() throws SQLException {
 		return Attempt.atQuiz(this);
 	}
 	
 	public int getNumberOfAttempts() throws SQLException {
 		return getAttempts().size();
 	}
 
 	@Override
 	public Activity getActivity() {
 		try {
 			System.out.println("MAKING QUIZ ACTIVITY in Quiz: line 260");
 			System.out.println("creatorID " + creatorID);
 			return new Activity(this.getCreatorID(), User.get(this.getCreatorID()).getName(), this.getCreatedAt(), "created a new Quiz", title);
 		} catch (SQLException e) {
 			System.err.println("SQLException looking up user");
 			e.printStackTrace();
 			return new Activity(0, "", "", "", "");
 		}
 	}
 
 	/**
 	 * Gets an action print string for the quiz. Similar to the getActivity followed
 	 * by getActivityPrintString but with subtle differences that are object centric (like
 	 * the link) versus the more user centric activity.
 	 */
 	public String getQuizTitleLink() {
 		StringBuilder sb = new StringBuilder();
		sb.append("<a href='displayQuiz.jsp?quizId="+this.getId()+"'>"+this.getTitle()+"</a>");
 		return sb.toString();
 	}
 }
