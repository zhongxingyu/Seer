 package domain;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 /**
  * VoterDB 
  * 
  * @author 
  * @version 2012.10.23
  *
  */
 
 import javax.swing.table.DefaultTableModel;
 
 /**
  * VoterDB actually likes controller of a System by controlling between UI and Database.
  * @author Wasupol Tungsakultong
  * @version 1.0
  */
 public class VoterDB extends RecordLog {
 
 	private static Connection con;
 	private PreparedStatement pstmt;
 	private static Statement stmt;
 	private String messageLog;
 	private Voter voter;
 
 	public void LoadDriver() {
 
 		// Load the JDBC-ODBC bridge driver
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
  	 * connect to database server
  	 */
 	public void connect() {
 
 		try {
 			con = DriverManager.getConnection(
 					"jdbc:mysql://192.168.1.5:3306/exceed_vote", "root", "Peepo");
 			stmt = con.createStatement();
 			// turn on autocommit
 			con.setAutoCommit(true);
 			record(messageLog = "Connected to database");
 		} catch (SQLException e) {
 			record(messageLog = "Can not connect to database!");
 		}
 
 	}
 
 	/**
  	 * access and get question from database server 
  	 * @return arraylist of question
  	 */
 	public ArrayList<Question> getQuestion() {
 		String query; // SQL select string
 		ResultSet rs; // SQL query results
 		ArrayList<Question> arrQuestion = new ArrayList<Question>();
 		query = "SELECT * FROM Question ";
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				arrQuestion.add(new Question(rs.getInt("ID"), rs
 						.getNString("questions")));
 			}
 			record("Access Question Database");
 			return arrQuestion;
 			// Loop through the rows retrieved from the query
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
  	 * access and get project from database server 
  	 * @return arraylist of project
  	 */
 	public ArrayList<Project> getProject() {
 		String query; // SQL select string
 		ResultSet rs; // SQL query results
 		ArrayList<Project> arrProject = new ArrayList<Project>();
 		query = "SELECT * FROM Project ";
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				arrProject.add(new Project(rs.getInt("ID"), rs
 						.getNString("name"), rs.getNString("teamname")));
 			}
 			record("Access Project Database");
 			return arrProject;
 			// Loop through the rows retrieved from the query
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
  	 * check username and password of voter from database server that exist in database or not
  	 * @param user - username of voter
  	 * @param pass - password of voter 
  	 * @return Voter object of voter that contain all information of voter from database
  	 * Voter object = null - if username and password not exist in database
  	 */
 	public Voter getVoter(String user, String pass) {
 		String query; // SQL select string
 		ResultSet rs; // SQL query results
 		query = "SELECT * FROM Voter ";
 		try {
 			rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				if (user.equalsIgnoreCase(rs.getString("username"))
 						&& pass.equals(rs.getString("password"))) {
 					return new Voter(rs.getInt("ID"), user, pass);
 				}
 			}
 			// Loop through the rows retrieved from the query
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		record("User: " + user + "- Login fail");
 		return null;
 	}
 
 	/**
  	 * check login with Voter object that login success or fail
  	 * @param voter - Voter object
  	 * @return false - if Voter object = null
  	 * true - otherwise 
  	 */
 	public boolean logIn(Voter voter) {
 		if (voter == null) {
 			messageLog = "Invalid Username or Password";
 			return false;
 		} else {
 			this.voter = voter;
 			messageLog = "Login success!";
 			record("IDNo: " + voter.getId() + "| Username" + ":"
 					+ voter.getName() + " - " + messageLog);
 			return true;
 		}
 	}
 
 	/**
  	 * submit result that voter select (username , project Id, question Id ,nuymber of score , time of vote)
  	 * @param user_id - user Id of voter
  	 * @param project_id - project Id that user vote
  	 * @param question_id - question Id that user select
  	 * @param score - number of score that voter vote
  	 * @param voteTime - time that submit to server
  	 */
 	public void insertVoteDB(int user_id, int project_id, int question_id,
 			int score, Timestamp voteTime) {
 		try {
 			String queryin = "INSERT INTO Vote (user_id, project_id, question_id, score, datetime) VALUES (?,?,?,?,?)";
 			pstmt = con.prepareStatement(queryin);
 			pstmt.setInt(1, user_id);
 			pstmt.setInt(2, project_id);
 			pstmt.setInt(3, question_id);
 			pstmt.setInt(4, score);
 			pstmt.setTimestamp(5, voteTime);
 			pstmt.executeUpdate();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
  	 * check that voter had voted the same thing(same prject and question) before or not
  	 * @param user_id - user Id of voter
  	 * @param project_id - project Id that user vote
  	 * @param question_id - question Id that user select
  	 * @param score - number of score that voter vote
  	 * @param voteTime - time that submit to server
  	 * @return false - if voter had voted this thing(same prject and question) before
  	 * true - otherwise
  	 */
 	public boolean canInsert(int user_id, int project_id, int question_id,
 			int score, Timestamp votetime) {
 		boolean unique = true;
 		try {
 			ResultSet resultset = stmt.executeQuery("SELECT * FROM Vote");
 			while (resultset.next()) {
 				if (resultset.getInt("user_id") == user_id
 						&& resultset.getInt("project_id") == project_id
 						&& resultset.getInt("question_id") == question_id) {
 					unique = false;
 				}
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return unique;
 
 	}
 	
 	
 	/**
 	 * create a table model of database to show as a result to user in form of JTable.
 	 * @param model - TableModel object which create from instance of database of voting system.
 	 * @return model of containing data of which project have been voted.
 	 * @throws SQLException
 	 */
 	public DefaultTableModel voteResult(DefaultTableModel model)
 			throws SQLException {
 		ResultSet row = stmt
 				.executeQuery("SELECT project.teamname,sum(vote.score) as total_vote_score, question.questions FROM vote,voter,question,project WHERE voter.ID = vote.user_id and project.ID = vote.project_id and question.ID = vote.question_id group by question.questions");
 		ResultSetMetaData meta = row.getMetaData();
 		if (model == null)
 			model = new DefaultTableModel();
 		String cols[] = new String[meta.getColumnCount()];
 		for (int i = 0; i < cols.length; ++i) {
 			cols[i] = meta.getColumnLabel(i + 1);
 		}
 		model.setColumnIdentifiers(cols);
 		while (row.next()) {
 			Object data[] = new Object[cols.length];
 			for (int i = 0; i < data.length; ++i) {
 				data[i] = row.getObject(i + 1);
 			}
 			model.addRow(data);
 		}
 		return model;
 	}
 
 	/**
  	 * return Voter object (voter that current login)
  	 * @return voter - Voter object
  	 */
 	public Voter getVoter() {
 		return voter;
 	}
 
 	/**
  	 * return message log for display in UI
  	 * @return messageLog
  	 */
 	public String getMessage() {
 		return messageLog;
 	}
 
 	/**
  	 * disconnect from database sever
  	 */
 	public void close() throws SQLException {
 		if (stmt != null) {
 			stmt.close();
 			con.close();
 			record(messageLog = "exits");
 		}
 	}
 
 }
