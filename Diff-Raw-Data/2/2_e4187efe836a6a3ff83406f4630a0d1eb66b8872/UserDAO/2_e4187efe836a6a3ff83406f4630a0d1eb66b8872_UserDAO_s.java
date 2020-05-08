 /**
  * JMBS: Java Micro Blogging System
  *
  * Copyright (C) 2012  
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY.
  * See the GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * @author Younes CHEIKH http://cyounes.com
  * @author Benjamin Babic http://bbabic.com
  * 
  */
 
 package jmbs.server;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import jmbs.common.Project;
 import jmbs.common.User;
 
 public class UserDAO extends DAO {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1371248166710891652L;
 
 	
 	public UserDAO(Connection c) {
 		super(c);
 	}
 
 	/**
 	 * Find a user using his database id.
 	 * 
 	 * @param userid
 	 *            database id of the user u want to find.
 	 */
 	public User getUser(int userid) {
 
 		User u = null;
 		set("SELECT * FROM users WHERE iduser=?;");
 		setInt(1, userid);
 		ResultSet res = executeQuery();
 
 		try {
 			u = new User(res.getString("name"), res.getString("forename"), res.getString("email"), userid);
 		} catch (SQLException e) {
 			System.out.println("No users with id equal to " + userid + " !");
 		}
 
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 		return u;
 	}
 
 	/**
 	 * Finds a user using his email.
 	 * 
 	 * @param em
 	 *            email of the user.
 	 * @throws SQLException 
 	 */
 	public User getUser(String em) {
 		User u = null;
 		int userid = 0;
 		ResultSet res = null;
 		
 		set("SELECT * FROM users WHERE email=? ;");
 		setString(1,em);
 		res = executeQuery();
 		
 		try {
 			userid = res.getInt("iduser");
 			u = new User(res.getString("name"), res.getString("forename"), res.getString("email"), userid);
 		} catch (SQLException e) {
 			System.err.println("No user with " + em + " as email adress !\n");
 		}
 
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 		
 		return u;
 	}
 
 	
 	/**
 	 * Returns all the projects a user is involved in.
 	 * 
 	 * @return Array of Projects
 	 */
 	public ArrayList<Project> getProjects(int userid) {
 		ArrayList<Project> p = new ArrayList<Project>();
 		
 		set("SELECT partiNamecipate.idproject,name FROM participate,project WHERE participate.iduser=? AND participate.idproject=project.idproject;");
 		setInt(1,userid);
 		ResultSet res = executeQuery();
 
 		try {
 			do {	
 				p.add(new Project(res.getString("name"), res.getInt("idproject"), this.getUser(res.getInt("idowner"))));
 			} while (res.next());
 
 		} catch (SQLException e) {
 			System.err.println("This user has no projects/n ");
 			// TODO determine if this error is due to a wrong user name or a
 			// lack of projects.
 		}
 
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 		
 		
 		return p;
 	}
 	
 	/**
 	 * Returns all the projects a user is involved in.
 	 * 
 	 * @return Array of Projects
 	 */
 	public ArrayList<Project> getProjects(User u) {
 		return getProjects(u.getId());
 	}
 
 	// TODO add an option to list Users by second names...
 	// TODO don't consider upper case...
 	/**
 	 * Find all users which names are containing uName.
 	 * 
 	 * @param uName
 	 *            part of the searched name.
 	 * @return Array of User
 	 */
 	public ArrayList<User> findUsers(String uName, int param) {
 		ArrayList<User> u = new ArrayList<User>();
 		int userid = 0;
 		String errorMsg = new String();
 		
 		if (param == BY_NAME) 
 		{
 			set("SELECT * FROM users WHERE name LIKE ?;");
 			setString(1,"%"+uName+"%");
 			errorMsg="No users found with name containing  \"" + uName +"\"";
 		}
 		if (param == BY_FORNAME)
 		{
 			set("SELECT * FROM users WHERE forename LIKE ?;");
 			setString(1,"%"+uName+"%");
 			errorMsg="No users found with second name containing  \"" + uName +"\"";
 		}
 		if (param == BY_BOTH)
 		{
 			set("SELECT * FROM users WHERE name LIKE ? OR forename LIKE ?;");
 			setString(1,"%"+uName+"%");
 			setString(2,"%"+uName+"%");
 			errorMsg="No users found with name or second name containing  \"" + uName +"\"";
 		}
 		ResultSet res = executeQuery();
 
 		try {
 			 do {
 					userid = res.getInt("iduser");
 					u.add(new User(res.getString("name"), res.getString("forename"), res.getString("email"), userid));
 			} while (res.next());
 		} catch (SQLException e) {
 			System.err.println(errorMsg);
 		}
 
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 
 		return u;
 	}
 
 	/**
 	 * Check if the password matches with the db one.
 	 * 
 	 * @param u
 	 *            User
 	 * @param pass
 	 *            String containing password
 	 * @return true - if the password matches
 	 * 
 	 */
 	public boolean checkPassword(User u, String pass) {
 		return checkPassword(u.getId(), pass);
 	}
 	
 	/**
 	 * Check if the password matches with the db one.
 	 * 
 	 * @param u
 	 *            User
 	 * @param pass
 	 *            String containing password
 	 * @return true - if the password matches
 	 * 
 	 */
 	public boolean checkPassword(int iduser, String pass) {
 		boolean ret = false;
 		
 		set("SELECT pass FROM users WHERE iduser =?;");
 		setInt(1,iduser);
 		ResultSet res = executeQuery();
 
 		try {
 			ret = res.getString("pass").equals(pass);
 			// TODO add connection log.
 		} catch (SQLException e) {
 			System.err.println("Invalid User.\n");
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Check if the email is already in use.
 	 * 
 	 * @param em
 	 *            String containing the email.
 	 * 
 	 * @return true if the email is used.
 	 */
 	public boolean checkMail(String em) {
 		boolean ret = true;
 		
 		set("SELECT email FROM users WHERE email =?;");
 		setString(1,em);
 		ResultSet res = executeQuery();
 
 		try {
 			res.getString("email");
 		} catch (SQLException e) { // Unused email
 			ret = false;
 		}
 
 		return ret;
 	}
 
 	//TODO: check if not useless.
 	/**
 	 * Says if the user exists in the database.
 	 * 
 	 * @return true - if the user is registered in the database.
 	 */
 	public boolean exists(User u) {
 		boolean ret = false;
 		
 		set("SELECT * FROM users WHERE iduser=?;");
 		setInt(1,u.getId());
 		ResultSet res = executeQuery();
 
 		try {
 			ret = res.getString("email").equals(u.getMail());
 		} catch (SQLException e) { // user does not exist
 			ret = false;
 		}
 
 		return ret;
 	}
 	
 	public boolean exists(int iduser){
 		set("SELECT email FROM users WHERE iduser=?");
 		setInt(1,iduser);
 		ResultSet res = executeQuery();
 		boolean ret = false;
 		
 		try {
 			res.getString("email");
 			ret = true;
 		} catch (SQLException e) { // user does not exist we can do something here if we really want to waste time ...
 			
 		}
 		
 		return ret;
 	}
 
 	/**
 	 * Adds a new user in the Database.
 	 * 
 	 * @param u
 	 *            the new user
 	 * @param pass
 	 *            the hashed password
 	 * @return true if editing DB succeeded
 	 */
 	public boolean addUser(User u, String pass) {
 		if (!checkMail(u.getMail()))
 		{
 			set("INSERT INTO users(name, forename, email, pass) VALUES (?,?,?,?);");
 			setString(1,u.getName());
 			setString(2,u.getFname());
 			setString(3,u.getMail());
 			setString(4,pass);
 			return executeUpdate();
 		}
 		System.err.println("Email already used.");
 
 		return false;
 	}
 
 	/**
 	 * Set an user to follow an other user.
 	 * 
 	 * @param idFollower
 	 * @param idFollowed
 	 * @return true if DB was editing DB succeeded
 	 */
 	public boolean follows(int idFollower, int idFollowed) {
 		if (this.exists(idFollower) && this.exists(idFollowed)){
 			set("INSERT INTO follows(follower, followed) VALUES (?,?);");
 			setInt(1,idFollower);
 			setInt(2,idFollowed);
 			boolean res = executeUpdate();
 			
 			return (res);
 		} else return false	;
 	}
 
 	/**
 	 * Set a user to stop following an other user.
 	 * 
 	 * @param idFollower
 	 * @param idFollowed
 	 * @return true if DB was editing DB succeeded
 	 */
 	public boolean unFollow(int idFollower, int idFollowed) {
 			set("DELETE FROM follows WHERE follower=? and followed=?;");
 			setInt(1,idFollower);
 			setInt(2,idFollowed);
 			boolean res = executeUpdate();
 			
 			return (res);
 	}
 
 	/**
 	 * Returns all the users a user is following
 	 * 
 	 * @param user
 	 * @return list of users
 	 */
 	public ArrayList<User> getFollowed(User user) {
 		ArrayList<User> u = new ArrayList<User>();
 		
 		set("SELECT iduser,name,forename,email FROM users,follows WHERE follows.follower =? and follows.followed=users.iduser;");
 		setInt(1,user.getId());
 		ResultSet res = executeQuery();
 
 		try {
 			do {
 				u.add(new User(res.getString("name"), res.getString("forename"), res.getString("email"), res.getInt("iduser")));
 			} while (res.next());
 		} catch (SQLException e) {
 			System.err.println(user.getFname() + " does not follow anyone yet!");
 		}
 
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 		
 		return u;
 	}
 
 	/**
 	 * Returns all the user followers.
 	 * 
 	 * @param user
 	 * @return ArrayList<User>
 	 */
 	public ArrayList<User> getFollowers(User user) {
 		ArrayList<User> u = new ArrayList<User>();
 		
 		set("SELECT iduser,name,forename,email FROM users,follows WHERE follows.followed =? and follows.follower=users.iduser;");
 		setInt(1,user.getId());
 		ResultSet res = executeQuery();
 		
 		try {
 			do {
 				u.add(new User(res.getString("name"), res.getString("forename"), res.getString("email"), res.getInt("iduser")));
 			}while (res.next());
 		} catch (SQLException e) {
 			System.err.println(user.getFname() + " is not followed by anyone yet!");
 		}
 		try {
 			res.close();
 		} catch (SQLException e) {
 			System.err.println("Database acess error !\n Unable to close connection !");
 		}
 		
 		return u;
 	}
 	
 	
 	public boolean participate (int iduser, int idproject, int auth){
 		if (this.exists(iduser) && (new ProjectDAO(super.con)).exists(idproject)){ //if the project and the user exists.
 			set("INSERT INTO participate (iduser,idproject,authlvl) VALUES (?,?,?);");
 			setInt(1,iduser);
 			setInt(2,idproject);
 			setInt(3,auth);
 			boolean res = executeUpdate();
 		
 			return res;
 		} else return false;
 	}
 	
 	public boolean participate (int iduser, int idproject){
 		return this.participate(iduser, idproject,User.DEFAULT_AUTHORISATION_LEVEL);
 	}
 	
 	public boolean unParticipate (int iduser, int idproject){
 		set("DELETE FROM participate WHERE iduser=? and idproject=?;");
 		setInt(1,iduser);
 		setInt(2,idproject);
 		boolean res = executeUpdate();
 		
 		return (res);
 	}
 	
 	public int getAccessLevel (int iduser, int idproject){
 		int ret = -1;
 		if (this.exists(iduser) && (new ProjectDAO(super.con)).exists(idproject)) {
 			set("SELECT authlvl FROM participate WHERE iduser=? AND idproject=?");
 			setInt(1,iduser);
 			setInt(2,idproject);
 			ResultSet res = executeQuery();
 		
 			try {
 				ret = res.getInt("authlvl");
 			} catch (SQLException e) {
 				System.err.println("Unexcepted error !");
 			}
 		}
 		
 		return ret;
 	}
 	
 	public int getAccessLevel (int iduser){
 		int ret = -1;
 		if (this.exists(iduser)) {
			set("SELECT authlvl FROM user WHERE iduser=?");
 			setInt(1,iduser);
 			ResultSet res = executeQuery();
 		
 			try {
 				ret = res.getInt("authlvl");
 			} catch (SQLException e) {
 				System.err.println("Unexcepted error !");
 			}
 		}
 		
 		return ret;
 	}
 	
 
 	public boolean changePassword(int userid, String oldPass, String newPass) throws SQLException{
 		boolean b = false;
 		if (checkPassword(userid, oldPass)){
 			set("UPDATE users SET pass=? WERE iduser = ?");
 			setString(1,newPass);
 			setInt(2,userid);
 			b = executeUpdate();
 			if (!b) throw new SQLException("Unable to change password in database for user id "+userid );
 		}
 		
 		return b;
 	}
 }
