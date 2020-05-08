 package com.socaldevs.jeopardy;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class SQLiteStorage implements DataStorage{
 
 	private static final String TABLE = "jeopardy";
 	private static final String QUESTION = "question";
 	private static final String ANSWER = "answer";
 	private static final String ROW = "row";
 	private static final String COLUMN = "column";
 	private static final String VALUE = "value";
 
 	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+TABLE+" (" +
 			//"id INTEGER PRIMARY KEY," +
 			QUESTION+" TEXT," +
 			ANSWER+" TEXT," +
 			COLUMN+" INTEGER," +
 			ROW+" INTEGER," +
 			VALUE+" INTEGER" +
 			");";
 
 	private static final String SELECT_BY_POS = "SELECT %s FROM "+TABLE+" WHERE "+COLUMN+"=%d AND "+ROW+"=%d";
 
 	private static final String INSERT_QUESTION = "INSERT INTO "+TABLE+
 			" VALUES('%s','%s',%d,%d,%d);";
 	
 	private Connection conn = null;
 
 	public SQLiteStorage(File database){
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}//load sqlite driver
 
 		try {
 			conn = DriverManager.getConnection("jdbc:sqlite:"+database.getAbsolutePath(),"","");
 			Statement createStatement = conn.createStatement();
 			createStatement.execute(CREATE_TABLE);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public SQLiteStorage(String database){
 		this(new File(database));
 	}
 	
 	@Override
 	public Question getQuestion(int col, int row){
 		Question q = new Question();
 		q.setCol(col);
 		q.setRow(row);
 		q.setAnswer(getAnswer(col, row));
 		q.setQuestion(getQuestionString(col, row));
 		q.setValue(getValue(col, row));
 		return q;
 	}
 
 	public String getQuestionString(int col, int row) {
 		try{
 			Statement query = conn.createStatement();
 			ResultSet result = query.executeQuery(String.format(SELECT_BY_POS, QUESTION, col, row));
 			return result.getString(QUESTION);
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public String getAnswer(int col, int row) {
 		try{
 			Statement query = conn.createStatement();
 			ResultSet result = query.executeQuery(String.format(SELECT_BY_POS, ANSWER, col, row));
 			return result.getString(ANSWER);
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	public int getValue(int col, int row) {
 		try{
 			Statement query = conn.createStatement();
 			ResultSet result = query.executeQuery(String.format(SELECT_BY_POS, VALUE, col, row));
 			return result.getInt(VALUE);
 		}catch(SQLException e){
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	@Override
	public void put(String question, String answer, int col, int row, int value) {
 		try {
 			Statement statement = conn.createStatement();
 			statement.execute(String.format(INSERT_QUESTION, question, answer, row, col, value));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
	@Override
	public void put(Question question) {
		put(question.getQuestion(), question.getAnswer(), question.getCol(), question.getRow(), question.getValue());
	}

 
 }
