 package database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import entities.Countdown;
 import entities.Group;
 import entities.Post;
 import entities.Round;
 import entities.Score;
 import entities.Team;
 import entities.Title;
 import entities.User;
 
 public class Database {
 	private Connection connection;
 	private static Database instance;
 
 	private Database() {
 
 	}
 
 	private Connection createConnection() {
 		if (connection == null) {
 			// String url =
 			// "jdbc:mysql://127.13.81.130:3306/MEETAlumniCompetition2013";
 			// String username = "adminUjVsItP";
 			// String password = "qxCxPNB43QKx";
 			String url = "jdbc:mysql://localhost/MEETAlumniCompetition2013";
 			String username = "root";
 			String password = "";
 			try {
 				Class.forName("com.mysql.jdbc.Driver");
 				connection = DriverManager.getConnection(url, username, password);
 				System.out.println("Connection is established");
 			} catch (ClassNotFoundException e) {
 				System.out.println("JDBC Class not found");
 			} catch (SQLException e) {
 				e.printStackTrace();
 				System.out.println("failed in connecting");
 			}
 		}
 		return connection;
 	}
 
 	public static Database getInstance() {
 		if (instance == null) {
 			instance = new Database();
 		}
 		instance.createConnection();
 		return instance;
 	}
 
 	/**
 	 * 
 	 * @param user
 	 * @return returns true if the siginin was successful otherwise returns
 	 *         false
 	 */
 	public boolean signup(User user) {
 		String query = "INSERT INTO user(`fname`,`lname`,`username`,`password`,`img`,`status`,`email`)VALUES(?,?,?,?,?,?,?)";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setString(1, user.getfName());
 			ps.setString(2, user.getlName());
 			ps.setString(3, user.getUsername());
 			ps.setString(4, user.getPassword());
 			ps.setString(5, user.getImg());
 			ps.setString(6, "Pending");
 			ps.setString(7, user.getEmail());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean isValid(User user) {
 		String query = "SELECT * FROM user WHERE `username`=? OR `email`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setString(1, user.getUsername());
 			ps.setString(2, user.getEmail());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				return false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	public User signIn(String username, String password) {
 		String query = "SELECT * FROM user WHERE `username`=? AND `status`='Approved'";
 		PreparedStatement ps;
 		User user = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setString(1, username);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				if (rs.getString("password").equals(password)) {
 					user = new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), rs.getString("password"), rs.getString("img"), rs.getString("status"), rs.getString("email"));
 				}
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return user;
 	}
 
 	public boolean approveUser(User user) {
 		String query = "UPDATE user SET `status`='Approved' WHERE `id`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean disableUser(User user) {
 		String query = "UPDATE user SET `status`='Disabled' WHERE `id`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean approveTeam(Team team) {
 		String query = "UPDATE team SET `status`='Approved' WHERE `id`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, team.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean disableTeam(Team team) {
 		String query = "UPDATE team SET `status`='Disabled' WHERE `id`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, team.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean openRound(Round round) {
 		String query = "UPDATE `round` SET `status`='Opened' WHERE `id`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, round.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean closeRound(Round round) {
 		String query = "UPDATE `round` SET `status`='Closed' WHERE `id`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, round.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public Team createTeam(Team team) {
 		String query = "INSERT INTO team(`name`,`description`,`status`)VALUES(?,?)";
 		PreparedStatement ps;
 		ResultSet generatedKeys;
 		try {
 			ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
 			ps.setString(1, team.getName());
 			ps.setString(2, team.getDescription());
 			ps.setString(3, team.getStatus());
 			ps.execute();
 			generatedKeys = ps.getGeneratedKeys();
 			if (generatedKeys.next())
 				team.setId(generatedKeys.getInt(1));
 			else
 				throw new SQLException("Creating score failed, no generated key obtained.");
 			return team;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return team;
 	}
 
 	public Group createGroup(Group group) {
 		String query = "INSERT INTO `group`(`name`,`description`)VALUES(?,?)";
 		PreparedStatement ps;
 		ResultSet generatedKeys;
 		try {
 			ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
 			ps.setString(1, group.getName());
 			ps.setString(2, group.getDescription());
 			ps.execute();
 			generatedKeys = ps.getGeneratedKeys();
 			if (generatedKeys.next())
 				group.setId(generatedKeys.getInt(1));
 			else
 				throw new SQLException("Creating score failed, no generated key obtained.");
 			return group;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return group;
 	}
 
 	public boolean createRound(Round round) {
 		String query = "INSERT INTO `round`(`title`,`description`,`status`)VALUES(?,?,?)";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setString(1, round.getTitle());
 			ps.setString(2, round.getDescription());
 			ps.setString(3, round.getStatus());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean addUserToTeam(User user, Team team) {
 		String query = "INSERT INTO user_team(`userid`,`teamid`)VALUES(?,?)";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.setInt(2, team.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean removeUserFromTeam(User user, Team team) {
 		String query = "DELETE FROM `user_team` WHERE `userid`=? AND `teamid`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.setInt(2, team.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean addUserToGroup(User user, Group group) {
 		String query = "INSERT INTO user_group(`userid`,`groupid`)VALUES(?,?)";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.setInt(2, group.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean removeUserFromGroup(User user, Group group) {
 		String query = "DELETE FROM `user_group` WHERE `userid`=? AND `groupid`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.setInt(2, group.getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean isInGroup(User user, Group group) {
 		String query = "SELECT * FROM user_group WHERE `userid`=? AND `groupid`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.setInt(2, group.getId());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean isInTeam(User user, Team team) {
 		String query = "SELECT * FROM user_team WHERE `userid`=? AND `teamid`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ps.setInt(2, team.getId());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public ArrayList<Score> getScoresPerTeamAndRound(Team team, Round round) {
 		String query = "SELECT * FROM score WHERE `id` IN (SELECT `scoreid` FROM round_team_score WHERE `teamid`=? AND `roundid`=?)";
 		PreparedStatement ps;
 		ArrayList<Score> scores = new ArrayList<Score>();
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, team.getId());
 			ps.setInt(2, round.getId());
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				scores.add(new Score(rs.getInt("id"), rs.getDouble("value"), rs.getString("description"), getUser(rs.getInt("userid"))));
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return scores;
 	}
 
 	public User getUser(int id) {
 		String query = "SELECT * FROM user WHERE `id`=?";
 		PreparedStatement ps;
 		User user = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				user = new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return user;
 	}
 
 	public Score getScore(int id) {
 		String query = "SELECT * FROM score WHERE `id`=?";
 		PreparedStatement ps;
 		Score score = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				score = new Score(rs.getInt("id"), rs.getDouble("value"), rs.getString("description"), getUser(rs.getInt("userid")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return score;
 	}
 
 	public Team getTeam(int id) {
 		String query = "SELECT * FROM team WHERE `id`=?";
 		PreparedStatement ps;
 		Team team = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				team = new Team(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return team;
 	}
 
 	public Round getRound(int id) {
 		String query = "SELECT * FROM `round` WHERE `id`=?";
 		PreparedStatement ps;
 		Round round = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				round = new Round(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("status"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return round;
 	}
 
 	public Group getGroup(int id) {
 		String query = "SELECT * FROM `group` WHERE `id`=?";
 		PreparedStatement ps;
 		Group group = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				group = new Group(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return group;
 	}
 
 	public ArrayList<Team> getAllTeams() {
 		String query = "SELECT * FROM team";
 		PreparedStatement ps;
 		ArrayList<Team> teams = new ArrayList<Team>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				teams.add(new Team(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return teams;
 	}
 
 	public ArrayList<Group> getAllGroups() {
 		String query = "SELECT * FROM `group`";
 		PreparedStatement ps;
 		ArrayList<Group> groups = new ArrayList<Group>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				groups.add(new Group(rs.getInt("id"), rs.getString("name"), rs.getString("description")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return groups;
 	}
 
 	public ArrayList<Round> getAllRounds() {
 		String query = "SELECT * FROM round";
 		PreparedStatement ps;
 		ArrayList<Round> rounds = new ArrayList<Round>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				rounds.add(new Round(rs.getInt("id"), rs.getString("title"), rs.getString("description"), rs.getString("status")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return rounds;
 	}
 
 	public ArrayList<User> getAllPendingUsers() {
 		String query = "SELECT * FROM user WHERE `status`='Pending'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public ArrayList<User> getAllApprovedUsers() {
 		String query = "SELECT * FROM user WHERE `status`='Approved'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public ArrayList<User> getAllDisabledUsers() {
 		String query = "SELECT * FROM user WHERE `status`='Disabled'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public ArrayList<Team> getAllPendingTeams() {
 		String query = "SELECT * FROM team WHERE `status`='Pending'";
 		PreparedStatement ps;
 		ArrayList<Team> teams = new ArrayList<Team>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				teams.add(new Team(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return teams;
 	}
 
 	public ArrayList<Team> getAllApprovedTeams() {
 		String query = "SELECT * FROM team WHERE `status`='Approved'";
 		PreparedStatement ps;
 		ArrayList<Team> teams = new ArrayList<Team>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				teams.add(new Team(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return teams;
 	}
 
 	public ArrayList<Team> getAllDisabledTeams() {
 		String query = "SELECT * FROM team WHERE `status`='Disabled'";
 		PreparedStatement ps;
 		ArrayList<Team> teams = new ArrayList<Team>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next())
 				teams.add(new Team(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status")));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return teams;
 	}
 
 	public boolean addScore(Score score, User creator, Team team, Round round) {
 		String query = "INSERT INTO score(`value`,`description`,`userid`)VALUES(?,?,?)";
 		PreparedStatement ps;
 		ResultSet generatedKeys;
 		try {
 			connection.setAutoCommit(false);
 			ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
 			ps.setDouble(1, score.getValue());
 			ps.setString(2, score.getDescription());
 			ps.setInt(3, creator.getId());
 			ps.execute();
 			generatedKeys = ps.getGeneratedKeys();
 			if (generatedKeys.next())
 				score.setId(generatedKeys.getInt(1));
 			else
 				throw new SQLException("Creating score failed, no generated key obtained.");
 			query = "INSERT INTO round_team_score(`teamid`,`roundid`,`scoreid`)VALUES(?,?,?)";
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, team.getId());
 			ps.setInt(2, round.getId());
 			ps.setInt(3, score.getId());
 			ps.execute();
 			connection.commit();
 			connection.setAutoCommit(true);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean addPost(Post post) {
 		String query = "INSERT INTO post(`title`,`description`,`roundid`,`userid`)VALUES(?,?,?,?)";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
 			ps.setString(1, post.getTitle());
 			ps.setString(2, post.getDescription());
 			ps.setInt(3, post.getRound().getId());
 			ps.setInt(4, post.getCreator().getId());
 			ps.execute();
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public Countdown getCountdown(Round round) {
 		String query = "SELECT * FROM countdowns WHERE id = (SELECT `countdownid` FROM `countdown_round` WHERE `roundid`=?)";
 		PreparedStatement ps;
 		Countdown countdown = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, round.getId());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				countdown = new Countdown(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getDate("enddate"), rs.getTime("endtime"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return countdown;
 	}
 	
 
 	public Countdown getCountdown(int id) {
 		String query = "SELECT * FROM countdowns WHERE id = ?";
 		PreparedStatement ps;
 		Countdown countdown = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				countdown = new Countdown(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getDate("enddate"), rs.getTime("endtime"));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return countdown;
 	}
 	public boolean isCountdownDone(int id) {
 		String query = "SELECT * FROM `countdowns` WHERE `id`=? AND (`enddate`>CURDATE() OR (`enddate` = CURDATE() AND `endtime`>NOW()))";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				return false;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	public ArrayList<User> getMembers(Team team) {
 		String query = "SELECT * from `user` WHERE `id` IN(SELECT `userid` FROM `user_team` WHERE `teamid` = ?) AND `status`='Approved'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, team.getId());
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public ArrayList<User> getAllUsersInGroup(Group group) {
 		String query = "SELECT * from `user` WHERE `id` IN(SELECT `userid` FROM `user_group` WHERE `groupid` = ?) AND `status`='Approved'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, group.getId());
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public ArrayList<User> getUsersNotInTeams() {
 		String query = "SELECT * from `user` WHERE `id` NOT IN(SELECT `userid` from `user_team`) AND `status`='Approved'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public ArrayList<User> getUsersNotInGroup(Group group) {
 		String query = "SELECT * from `user` WHERE `id` NOT IN(SELECT `userid` from `user_group` WHERE `groupid`=?) AND `status`='Approved'";
 		PreparedStatement ps;
 		ArrayList<User> users = new ArrayList<User>();
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, group.getId());
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				users.add(new User(rs.getInt("id"), rs.getString("fname"), rs.getString("lname"), rs.getString("username"), "", rs.getString("img"), rs.getString("status"), rs.getString("email")));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return users;
 	}
 
 	public Team getTeam(User user) {
 		String query = "SELECT * FROM `team` WHERE `id`=(SELECT `teamid` from `user_team` WHERE `userid`=?)";
 		PreparedStatement ps;
 		Team team = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				team = new Team(rs.getInt("id"), rs.getString("name"), rs.getString("description"), rs.getString("status"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return team;
 	}
 
 	public boolean isInTeam(User user) {
 		String query = "SELECT * FROM user_team WHERE `userid`=?";
 		PreparedStatement ps;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, user.getId());
 			ResultSet rs = ps.executeQuery();
 			if (rs.next())
 				return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public ArrayList<Post> getAllPosts(Round round) {
 		String query = "SELECT * from `post` WHERE `roundid` =? ";
 		PreparedStatement ps;
 		ArrayList<Post> posts = new ArrayList<Post>();
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, round.getId());
 			ResultSet rs = ps.executeQuery();
 			while (rs.next()) {
 				posts.add(new Post(rs.getInt("id"), rs.getString("title"), rs.getString("description"), getUser(rs.getInt("userid")), round));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return posts;
 	}
 
 	public static void main(String[] args) {
 		User user = new User(1, "firstname", "lastname", "username", "password", "img url", "Approved", "email");
 		// Team team = new Team(1, "name", "description");
 		// Round round = new Round(2, "Roundtitle", "RoundDescription",
 		// "Closed");
 		// Score score = new Score(1, 70.7, "description", user);
 		// Group group = new Group(1, "name", "description");
 		// Database.getInstance().signup(user);
 		// Database.getInstance().approveUser(user);
 		// System.out.println(Database.getInstance().signIn("username",
 		// "password").getfName());
 		// Database.getInstance().createGroup(group);
 		// Database.getInstance().addUserToGroup(user, group);
 		// Database.getInstance().addUserToTeam(user, team);
 		// System.out.println(Database.getInstance().isInGroup(user, group));
 		// System.out.println(Database.getInstance().isInTeam(user, team));
 		// Database.getInstance().createRound(round);
 		// Database.getInstance().addScore(score, user, team, round);
 		// System.out.println(Database.getInstance().getScore(1).getValue());
 		Team team = Database.getInstance().getTeam(user);
 		System.out.print(team.getName());
 	}
 	
 	public Title getTitle(int id) {
 		String query = "SELECT * FROM titles WHERE `id`=?";
 		PreparedStatement ps;
 		Title title = null;
 		try {
 			ps = connection.prepareStatement(query);
 			ps.setInt(1, id);
 			ResultSet rs = ps.executeQuery();
 			if (rs.next()) {
 				title = new Title(rs.getInt("id"), rs.getString("title"), rs.getString("description"));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return title;
 	}
 }
