 package se.chalmers.chessfeudserver;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation for handling request from the andriod-app.
  */
 public class DbHandler extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private Connection dbConnection;
 
 	private ResultSet rs;
 	private Statement s;
 	private HashMap tags;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public DbHandler() {
 		super();
 
 	}
 
 	/**
 	 * Loads the jdbc driver.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		String loginUser = "root";
 		String loginPw = "awesomeness";
 		String loginURL = "jdbc:mysql://localhost/cfdb";
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			dbConnection = DriverManager.getConnection(loginURL, loginUser,
 					loginPw);
 			s = dbConnection.createStatement();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		tags = new HashMap();
 		tags.put("login", 0);
 		tags.put("newmove", 1);
 		tags.put("getGames", 2);
 		tags.put("getStats", 3);
 		tags.put("addUser", 4);
 		tags.put("incWins", 5);
 		tags.put("incLosses", 6);
 		tags.put("incDraws", 7);
 		tags.put("newGame", 8);
 		tags.put("deleteGame", 9);
 		tags.put("newInquirie", 10);
 		tags.put("userExists", 11);
 		tags.put("getFinishedGames", 12);
 
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		this.init();
 		response.setContentType("text");
 		final ServletOutputStream out = response.getOutputStream();
 
 		try {
 			switch ((Integer) tags.get(request.getParameter("tag"))) {
 			case 0:
 				out.print(authenticate(request.getParameter("username"),
 						request.getParameter("password")));
 				break;
 			case 1:
 				newMove(request.getParameter("user1"),
 						request.getParameter("user2"),
 						request.getParameter("gameBoard"));
 				break;
 			case 2:
 				List<String> games = getGamesInProgress(request
 						.getParameter("username"));
 				StringBuilder sb = new StringBuilder();
 				for (String s : games) {
 					sb.append(s);
 					sb.append(";");
 				}
 				out.print(sb.toString());
 				break;
 			case 3:
 				out.print(getStatistics(request.getParameter("username")));
 				break;
 			case 4:
 				out.print(addUser(request.getParameter("email"),
 						request.getParameter("username"),
 						request.getParameter("password")));
 				break;
 			case 5:
 				incWins(request.getParameter("username"));
 				break;
 			case 6:
 				incLosses(request.getParameter("username"));
 				break;
 			case 7:
 				incDraws(request.getParameter("username"));
 				break;
 			case 8:
 				newGame(request.getParameter("user1"),
 						request.getParameter("user2"),
 						request.getParameter("gameBoard"));
 				break;
 			case 9:
 				deleteGame(request.getParameter("user1"),
 						request.getParameter("user2"));
 				break;
 			case 10:
 				newInquirie(request.getParameter("user"),
 						request.getParameter("target"));
 			case 11:
 				out.print(userExists(request.getParameter("username")));
 			case 12:
 				List<String> finishedGames = getGamesInProgress(request
 						.getParameter("username"));
 				StringBuilder strbuilder = new StringBuilder();
 				for (String s : finishedGames) {
 					strbuilder.append(s);
 					strbuilder.append(";");
 				}
 				out.print(strbuilder.toString());
 			}
 
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		out.flush();
 		out.close();
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		this.doGet(request, response);
 
 	}
 
 	private void writeResultSet(ResultSet resultSet) throws SQLException {
 		while (resultSet.next()) {
 
 			System.out.println(resultSet.getString(1));
 			System.out.println(resultSet.getString(2));
 			System.out.println(resultSet.getString(3));
 		}
 	}
 
 	private boolean authenticate(String userName, String password)
 			throws SQLException {
		if(userExists(userName)) {
			rs = s.executeQuery("select password from auth where userName='"
					+ userName + "'");
			rs.first();
			System.out.println(rs.getString(1));
			if (rs.getString(1).equals(password)) {
				return true;
			}			 			
 		}
 		return false;
 	}
 
 	/**
 	 * Checks if the username currently is registred in the database.
 	 * 
 	 * @param userName
 	 * @return
 	 * @throws SQLException
 	 */
 	private boolean userExists(String userName) throws SQLException {
 		rs = s.executeQuery("select count(*) from auth where userName='"
 				+ userName + "'");
		rs.first();
		if(rs.getInt(1)>0) {
			return true;
		}
		return false;
 	}
 
 	/**
 	 * Checks for games in the database where the user is participating and adds
 	 * them to an arraylist to return.
 	 * 
 	 * @param userName
 	 * @return
 	 * @throws SQLException
 	 */
 	private List<String> getGamesInProgress(String userName)
 			throws SQLException {
 		List<String> games = new ArrayList<String>();
 		rs = s.executeQuery("select * from game where user1='" + userName
 				+ "' or user2='" + userName + "'");
 		while (rs.next()) {
 			games.add(rs.getString(1) + "/" + rs.getString(2) + "/"
 					+ rs.getString(3) + "/" + rs.getString(4) + "/"
 					+ rs.getString(5));
 		}
 		return games;
 	}
 
 	/**
 	 * Checks for games in the table for finishedgames in the database where the
 	 * user is playing and returns an arraylist with them.
 	 * 
 	 * @param userName
 	 * @return an arraylist with all the finished games.
 	 * @throws SQLException
 	 */
 	private List<String> getFinishedGames(String userName) throws SQLException {
 		List<String> games = new ArrayList<String>();
 		rs = s.executeQuery("select * from finishedgames where user1='"
 				+ userName + "' or user2='" + userName + "'");
 		while (rs.next()) {
 			games.add(rs.getString(1) + "/" + rs.getString(2) + "/"
 					+ rs.getString(3) + "/" + rs.getString(4) + "/"
 					+ rs.getString(5));
 		}
 		return games;
 	}
 
 	/**
 	 * Returns wins losses and draws as well as number of moves done all with a
 	 * / between each other.
 	 * 
 	 * @param userName
 	 * @return
 	 * @throws SQLException
 	 */
 	private String getStatistics(String userName) throws SQLException {
 		rs = s.executeQuery("select * from statistics where username='"
 				+ userName + "'");
 		return (rs.getString(2) + "/" + rs.getString(3));
 	}
 
 	/**
 	 * Adds a user to the database.
 	 * 
 	 * @param email
 	 * @param userName
 	 * @param password
 	 * @throws SQLException
 	 */
 	private boolean addUser(String email, String userName, String password)
 			throws SQLException {
		if(!userExists(userName)) {
			int i = s.executeUpdate("insert into auth(email, username, password) values('"
					+ email + "','" + userName + "','" + password + "')");
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Increments wins in the database for the username specified.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private void incWins(String userName) throws SQLException {
 		rs = s.executeQuery("select wld from statistics where username='"
 				+ userName + "'");
 		String[] wld = rs.getString(1).split("/");
 		int w = Integer.parseInt(wld[0]) + 1;
 		s.executeUpdate("update statistics set wld='" + w + wld[1] + wld[2]
 				+ "' where username='" + userName + "'");
 	}
 
 	/**
 	 * Increments losses in the database for the username specified.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private void incLosses(String userName) throws SQLException {
 		rs = s.executeQuery("select wld from statistics where username='"
 				+ userName + "'");
 		String[] wld = rs.getString(1).split("/");
 		int l = Integer.parseInt(wld[1]) + 1;
 		s.executeUpdate("update statistics set wld='" + wld[0] + l + wld[2]
 				+ "' where username='" + userName + "'");
 	}
 
 	/**
 	 * Increments draws in the database for the username specified.
 	 * 
 	 * @param userName
 	 * @throws SQLException
 	 */
 	private void incDraws(String userName) throws SQLException {
 		rs = s.executeQuery("select wld from statistics where username='"
 				+ userName + "'");
 		String[] wld = rs.getString(1).split("/");
 		int d = Integer.parseInt(wld[2]) + 1;
 		s.executeUpdate("update statistics set wld='" + wld[0] + wld[1] + d
 				+ "' where username='" + userName + "'");
 	}
 
 	/**
 	 * Adds a new game to the database.
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @param gameBoard
 	 * @throws SQLException
 	 */
 	private void newGame(String user1, String user2, String gameBoard)
 			throws SQLException {
 		s.executeUpdate("insert into game(user1, user2, board, turns, timestamp) values('"
 				+ user1
 				+ "','"
 				+ user2
 				+ "','"
 				+ gameBoard
 				+ "','"
 				+ "0"
 				+ "','" + "CURRENT_TIMESTAMP)");
 	}
 
 	/**
 	 * A new gameboard is inserted into the database replacing the old one.
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @param newBoard
 	 * @throws SQLException
 	 */
 	private void newMove(String user1, String user2, String newModel)
 			throws SQLException {
 		String[] boardandturns = newModel.split("/");
 		String newBoard = boardandturns[0];
 		int turns = Integer.parseInt(boardandturns[1]);
 		s.executeUpdate("update game set board='" + newBoard + "'where user1='"
 				+ user1 + "' and user2='" + user2 + "'");
 		s.executeUpdate("update game set board='" + newBoard + "'where user2='"
 				+ user1 + "' and user2='" + user1 + "'");
 		s.executeUpdate("update game set turns='" + turns + "'where user1='"
 				+ user1 + "' and user2='" + user2 + "'");
 		s.executeUpdate("update game set turns='" + turns + "'where user2='"
 				+ user1 + "' and user2='" + user1 + "'");
 	}
 
 	/**
 	 * Adds a new inquirie to the database.
 	 * 
 	 * @param user
 	 * @param target
 	 * @throws SQLException
 	 */
 	private void newInquirie(String user, String target) throws SQLException {
 		s.executeUpdate("insert into inquiries(user, target, timestamp) values('"
 				+ user + "','" + target + "',CURRENT_TIMESTAMP)");
 	}
 
 	/**
 	 * Deletes a game between two users from the database.
 	 * 
 	 * @param user1
 	 * @param user2
 	 * @throws SQLException
 	 */
 	private void deleteGame(String user1, String user2) throws SQLException {
 		s.executeUpdate("delete from game where user1='" + user1
 				+ "' and user2='" + user2 + "'");
 		s.executeUpdate("delete from game where user1='" + user2
 				+ "' and user2='" + user1 + "'");
 	}
 
 }
