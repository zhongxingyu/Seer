 /**
  * CSE 403 AA
  * Project Nonogram: Backend
  * @author  HyeIn Kim (co-author Sean Wu)
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 
 package uw.cse403.nonogramfun.nonogram;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import uw.cse403.nonogramfun.enums.Difficulty;
 import uw.cse403.nonogramfun.utility.NonoUtil;
 
 
 /**
  * NonoDatabase manages all database related functionalities, such as
  * inserting data into table and getting data from table, using PostGres SQL.
  */
 public class NonoDatabase {
 	
 	/* Connection related string constants */
 	private static final String DRIVER = "org.postgresql.Driver";
 	private static final String USER_ID = "fcwlxdlohwgjtp";
 	private static final String PASSWORD = "0xpGsFrLQgSJfp-4l9GE8HXdH-";
 	private static final String SSL = "true";
 	private static final String SSL_FACTORY = "org.postgresql.ssl.NonValidatingFactory";
 	private static final String DB_URL = "jdbc:postgresql://ec2-54-225-101-126.compute-1.amazonaws.com:5432/d64in60h3dpvf2";
 	private static final String URL = DB_URL + "?" + "user=" + USER_ID + "&" + "password=" + PASSWORD + "&" + 
 			                         "ssl=" + SSL + "&" + "sslfactory=" + SSL_FACTORY;
 	
 	/* Database table related string constants */
 	private static final String PUZZLE_TABLE = "puzzles";
 	private static final String PUZZLE_ID = "id";
 	private static final String PUZZLE_DIFFICULTY = "difficulty";
 	private static final String PUZZLE_OBJECT = "puzzle";
 
 	
 	
 	// Private constructor
 	private NonoDatabase() {}
 	
 	
 	// Creates and returns a connection to the database
 	private static Connection getConnection() throws Exception {
         Class.forName(DRIVER).newInstance();
         Properties props = new Properties();
         props.setProperty("user", USER_ID);
         props.setProperty("password", PASSWORD);
         props.setProperty("ssl","true");
         Connection conn = DriverManager.getConnection(URL, props);
       	return conn;
 	}
 	
 	
 	/**
 	 * Returns a NonoPuzzle object with the given ID. Returns null if no such puzzle exists.
 	 * @param puzzleID id of a NonoPuzzle object
 	 * @return A NonoPuzzle object
 	 * @throws Exception if connection or database problem occurs
 	 */
 	public static NonoPuzzle getPuzzle(int puzzleID) throws Exception { 
 		String sql = " SELECT " + PUZZLE_OBJECT + 
 				     " FROM   " + PUZZLE_TABLE  + 
 				     " WHERE  " + PUZZLE_ID     + " = ?";
 		
 		// 1. Get connection & set up SQL statement
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		NonoPuzzle puzzle = null;
 		try {
 			conn = getConnection();
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, puzzleID);
 			
 			// 2. Execute statement & get result
 			rs = ps.executeQuery();
 			if (!rs.isBeforeFirst()) { return null; }
 			rs.next(); 
 			puzzle = (NonoPuzzle) NonoUtil.byteToObject(rs.getBytes(PUZZLE_OBJECT)); //TODO ClassNotFoundException
 			
 			// 3. Clean up & return the result
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (rs != null) {
 				rs.close();
 			}
 			if (conn != null) {
 				conn.close();
 			}
 			if (ps != null) {
 				ps.close();
 			}
 		}
 		return puzzle;
 	}
 	
 	
 	/**
 	 * Accepts a NonoPuzzle object and saves it into the database.
 	 * @param puzzle A NonoPuzzle object to be stored in database
 	 * @throws Exception if connection or database problem occurs
 	 */
 	public static void savePuzzle(NonoPuzzle puzzle) throws Exception {
 		String sql = " INSERT INTO " + PUZZLE_TABLE +
 				     " VALUES (?, ?, ?)";
 		Connection conn = null;
 		PreparedStatement ps = null;
 
 		try {
 			// 1. Get connection & set up SQL statement
 			conn = getConnection();
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, puzzle.getPuzzleID());
 			ps.setString(2, puzzle.getDifficulty().toString());
 			ps.setObject(3, NonoUtil.objecToByte(puzzle));
 			
 			System.out.println(puzzle);
 			System.out.println(puzzle.getPuzzleID());
 			System.out.println(puzzle.getDifficulty());
 			System.out.println(ps.toString());
 			// 2. Execute statement 
 			ps.executeUpdate();
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			// 3. Clean up
 			if (conn != null) {
 				conn.close();
 			}
 			if (ps != null) {
 				ps.close();
 			}
 		}
 	}
 	
 	
 	/**
 	 * Accepts Difficulty of a NonoPuzzle and returns List of puzzleIDs of 
 	 * all NonoPuzzles in database with given difficulty.
 	 * @param diff Difficulty of a NonoPuzzle
 	 * @return List of puzzleIDs of all NonoPuzzles in database with given difficulty
 	 * @throws Exception if connection or database problem occurs
 	 */
 	public static List<Integer> getPuzzleIDList(Difficulty diff) throws Exception {
 		String sql = " SELECT " + PUZZLE_ID         +
 				     " FROM   " + PUZZLE_TABLE      +
 				     " WHERE  " + PUZZLE_DIFFICULTY + " = ?";
		
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		List<Integer> idList = new ArrayList<Integer>();
 		try {
 			// 1. Get connection & set up SQL statement
 			conn = getConnection();
 			ps = conn.prepareStatement(sql);
 			ps.setString(1, diff.toString());
 			// 2. Execute statement & get result
 			rs = ps.executeQuery();
 			while(rs.next()) {
 				idList.add(rs.getInt(1));
 			}
 		} catch (Exception e) {
 			throw e;
 		} finally {	
 		// 3. Clean up & return the result
 			if (rs != null) {
 				rs.close();
 			}
 			if (conn != null) {
 				conn.close();
 			}
 			if (ps != null) {
 				ps.close();
 			}
 		}
 		return idList;
 	}
 
 	// TODO: Remove later. For connection testing
 	public static void main(String[] args) throws Exception {
 		System.out.println("MySQL Connect Example.");
 		NonoPuzzle puzzle = getPuzzle(1);
 		System.out.println(puzzle);
 		System.out.println("Connected to the database");
 	}
 }
 
