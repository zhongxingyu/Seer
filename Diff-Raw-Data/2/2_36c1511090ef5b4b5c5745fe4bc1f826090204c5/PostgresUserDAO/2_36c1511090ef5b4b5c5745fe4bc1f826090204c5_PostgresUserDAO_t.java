 package edu.wm.werewolf.dao;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 import edu.wm.werewolf.domain.Player;
 import edu.wm.werewolf.domain.Score;
 import edu.wm.werewolf.domain.User;
 import edu.wm.werewolf.exceptions.PlayerNotFoundException;
 import edu.wm.werewolf.exceptions.UserNotFoundException;
 
 public class PostgresUserDAO extends PostgresDAO implements IUserDAO {
 
 	@Override
 	public boolean insertUser(User u) {
 		
 		Connection connection = establishConnection();
 		return execUpdate(connection, "insert into user_account(first_name, last_name, imageurl, hashed_password, username, score) values ('" + u.getFirstname() + "','" + u.getLastname() + "','" + u.getImageURL() + "','" + u.getHashedPassword() + "','" + u.getUsername() + "'," + u.getScore() + ");");
 		
 	}
 
 	@Override
 	public boolean setPicture(String filepath) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public User getUserByUsername(String username) throws UsernameNotFoundException {
 		
 		Connection connection = establishConnection();
 		ResultSet r = execQuery(connection, "select * from user_account where username='" + username + "';");
 		
 		try {
 			if (r.next())
 				return new User(r.getString("first_name"), r.getString("last_name"), r.getString("imageurl"), r.getString("hashed_password"), r.getString("username"), r.getInt("score"));
 			else
 				throw new UsernameNotFoundException(username);
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 
 	@Override
 	public boolean removeUserByUsername(String username) {
 		
 		Connection connection = establishConnection();
 		return execUpdate(connection, "delete from user_account where username=" + username + ";");
 		
 	}
 
 	@Override
 	public List<User> getAllUsers() {
 		
 		Connection connection = establishConnection();
 		List<User> users = new ArrayList<User>();
 		ResultSet r = execQuery(connection, "select * from user_account;");
 		
 		try {
 			while (r.next()) {
 				users.add(new User(r.getString("first_name"), r.getString("last_name"), r.getString("imageurl"), r.getString("hashed_password"), r.getString("username"), r.getInt("score")));
 			}
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return users;
 		
 	}
 
 	@Override
 	public List<Score> getScores() {
 		Connection connection = establishConnection();
 		ResultSet r = execQuery(connection, "select username, score from user_account order by score;");
 		
 		List<Score> scores = new ArrayList<Score>();
 		
 		try {
 		
 			while (r.next()) {
 				scores.add(new Score(r.getString("username"), r.getInt("score")));
 			}
 		
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return scores;
 		
 	}
 
 	@Override
 	public boolean logWin(User u) {
 
 		Connection connection = establishConnection();
		return execUpdate(connection, "update user_account set score=score+1 where username='" + u.getUsername() + "';");
 		
 	}
 
 }
