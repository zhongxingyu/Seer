 package se.chalmers.chessfeud.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 /**
  * A class to handle the communication between the phone and the server. Handles
  * all the logic like login, register new user and the status of the games.
  * 
  * @author Simon Almgren Henrik Alburg
  * 
  *         Copyright (c) 2012 Simon Almgren, Henrik Alburg
  * 
  */
 
 public class DbHandler {
 
 	private HttpClient client;
 	private HttpPost httpPost;
 	private InputStream is;
 	private List<BasicNameValuePair> pairs;
 
 	private static final String TAG = "tag";
 	private static final String USERNAME = "username";
 	private static final String PASSWORD = "password";
 	private static final String EMAIL = "email";
 
 	private static DbHandler instance;
 	private PlayerInfo player;
 
 	/**
 	 * Empty constructor following the Singleton-pattern.
 	 */
 	protected DbHandler() {
 	}
 
 	/**
 	 * Returns a new instance of DbHandler if there isn't already an instance,
 	 * otherwise returns the instance.
 	 * 
 	 * @return an instance of the DbHandler
 	 */
 	public static DbHandler getInstance() {
 		if (instance == null) {
 			instance = new DbHandler();
 			instance.init();
 		}
 		return instance;
 	}
 
 	/**
 	 * Initiates the DbHandler and connects it to the database.
 	 */
 	private void init() {
 		client = new DefaultHttpClient();
 		httpPost = new HttpPost(
 				"http://90.227.18.175:8080/ChessFeudServer/DbHandler/*");
 		is = null;
 		pairs = new ArrayList<BasicNameValuePair>();
 		player = PlayerInfo.getInstance();
 	}
 
 	/**
 	 * Tries to authenticate the specified username and password with an
 	 * existing user on the server, returns false if it wasn't there or if
 	 * something went wrong otherwise true.
 	 * 
 	 * @param userName
 	 *            The username to try and log in
 	 * @param password
 	 *            The password to try and log in
 	 * @return boolean is login sucseeded
 	 */
 	public boolean login(String userName, String password) {
 		pairs.clear();
 		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
 		String enc;
 		try {
 			enc = encrypt(password);
 			list.add(new BasicNameValuePair(TAG, "login"));
 			list.add(new BasicNameValuePair(USERNAME, userName));
 			list.add(new BasicNameValuePair(PASSWORD, enc));
 		} catch (NoSuchAlgorithmException e) {
 			return false;
 		}
 		return updateDatabase(list);
 	}
 
 	/**
 	 * Contacts the server and tells it to save a new user, returns false if the
 	 * player couldnt be added.
 	 * 
 	 * @param email
 	 *            The email of the new user
 	 * @param userName
 	 *            The username of the new user
 	 * @param password
 	 *            The password of the new user
 	 * @return true if the server was successful when creating the user
 	 * @throws NoSuchAlgorithmException
 	 *             If the MD5 fails to crypt.
 	 */
 	public boolean addUser(String email, String userName, String password) {
 		pairs.clear();
 		String enc;
 		try {
 			enc = encrypt(password);
 			pairs.add(new BasicNameValuePair(TAG, "addUser"));
 			pairs.add(new BasicNameValuePair(EMAIL, email));
 			pairs.add(new BasicNameValuePair(USERNAME, userName));
 			pairs.add(new BasicNameValuePair(PASSWORD, enc));
 		} catch (NoSuchAlgorithmException e) {
 			return false;
 		}
 		return (updateDatabase());
 	}
 
 	/**
 	 * Deletes a user from the database.
 	 * 
 	 * @param userName
 	 *            The name of the user to be deleted.
 	 * @return true if the server operation was successful
 	 */
 	public boolean deleteUser(String userName) {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "deleteUser"));
 		pairs.add(new BasicNameValuePair(USERNAME, userName));
 		return (updateDatabase());
 	}
 
 	/**
 	 * Contacts the server and checks if the specified username exists in the
 	 * database.
 	 * 
 	 * @param userName
 	 *            The username to check
 	 * @return true if the server operation was successful
 	 */
 	public boolean userExists(String userName) {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "userExists"));
 		pairs.add(new BasicNameValuePair(USERNAME, userName));
 		return updateDatabase();
 	}
 
 	/**
 	 * Sets moves a finished game from the gamedatabase to finishedgames.
 	 * 
 	 * @param target
 	 *            The one you lost or won against.
 	 * @return if everyhting went as expected.
 	 */
 	public boolean setGameFinished(String target) {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "setGameFinished"));
 		pairs.add(new BasicNameValuePair("user1", player.getUserName()));
 		pairs.add(new BasicNameValuePair("user2", target));
 		return updateDatabase();
 	}
 
 	/**
 	 * Contacts the database and tells it to create a new game, returns false if
 	 * it failed.
 	 * 
 	 * @param user1
 	 *            The username of the white player
 	 * @param user2
 	 *            The username of the black player
 	 * @param board
 	 *            The starting board for this game
 	 * @return true if the server operation was successful
 	 */
 	public boolean newGame(String target, String board) {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "newGame"));
 		pairs.add(new BasicNameValuePair("user1", player.getUserName()));
 		pairs.add(new BasicNameValuePair("user2", target));
 		pairs.add(new BasicNameValuePair("board", board));
 		return updateDatabase();
 	}
 
 	/**
 	 * Contacts the database with a new gameboard for the existing game between
 	 * two players.
 	 * 
 	 * @param target
 	 *            The opponents username
 	 * @param newBoard
 	 *            The new chess board
 	 * @return true if the server operation was successful
 	 */
 	public boolean newMove(String target, String newModel) {
 		List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>();
 		list.add(new BasicNameValuePair(TAG, "newMove"));
 		list.add(new BasicNameValuePair("user1", player.getUserName()));
 		list.add(new BasicNameValuePair("user2", target));
 		list.add(new BasicNameValuePair("board", newModel));
 		return updateDatabase(list);
 	}
 
 	/**
 	 * Contacts the server and tells it to save a new inquire, returns false if
 	 * it couldn't be saved or if something went wrong.
 	 * 
 	 * @param target
 	 *            The username of the opponent who has been challanged
 	 * @return boolean true if the server operation was successful
 	 */
 	public boolean addInquirie(String target) {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "addInquirie"));
 		pairs.add(new BasicNameValuePair("user", player.getUserName()));
 		pairs.add(new BasicNameValuePair("target", target));
 		return updateDatabase();
 	}
 
 	/**
 	 * Gives the server a user name and returns the current statistics from that
 	 * user in a String with / between all the different stats, which is
 	 * w/l/d/numberofmoves. Returns null of something went wrong when contacting
 	 * the database.
 	 * 
 	 * @return returns the stats of that user.
 	 */
 	public String getStats() {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "getStats"));
 		pairs.add(new BasicNameValuePair(USERNAME, player.getUserName()));
 		return getFromDatabase();
 	}
 
 	/**
 	 * Contacts the database and request a list of all ongoing games, returns a
 	 * list of all the games, an empty list if there are no games and null if
 	 * something went wrong.
 	 * 
 	 * @return a list of games of the currently logged in player.
 	 */
 	public List<String> getGames() {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "getGames"));
 		pairs.add(new BasicNameValuePair(USERNAME, player.getUserName()));
 		String s = getFromDatabase();
 		if (s == null) {
 			return new ArrayList<String>();
 		} else if (s.equals("") || s.equals("true") || s.equals("false")) { // If something went wrong when contacting
 									// the database.
 			return new ArrayList<String>();
 		}
 		List<String> games = new ArrayList<String>();
 		String[] dbGames = s.split(";");
 		for (String i : dbGames) {
 			games.add(i);
 		}
 		return games;
 	}
 
 	/**
 	 * Contacts the database and request a list of all finished games played in
 	 * the closest 72 hours.
 	 * 
 	 * @return a list of the finished games.
 	 */
 	public List<String> getFinishedGames() {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "getFinishedGames"));
 		pairs.add(new BasicNameValuePair(USERNAME, player.getUserName()));
 		String s = getFromDatabase();
 		if (s == null) {
 			return null;
 		} else if (s.equals("")) { // If something went wrong when contacting
 									// the database.
 			return new ArrayList<String>();
 		}
 		List<String> finishedgames = new ArrayList<String>();
 		String[] dbFinishedGames = s.split(";");
 		for (String i : dbFinishedGames) {
 			finishedgames.add(i);
 		}
 		return finishedgames;
 	}
 
 	/**
 	 * Increments the wins for a user in the database, returns false if
 	 * something went wrong.
 	 * 
 	 * @return true if the server operation was successful
 	 */
 	public boolean incWins() {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "incWins"));
 		pairs.add(new BasicNameValuePair(USERNAME, player.getUserName()));
 		return updateDatabase();
 
 	}
 
 	/**
 	 * Increments the draws for a user in the databse, returns false if
 	 * something went wrong.
 	 * 
 	 * @return true if the server operation was successful
 	 */
 	public boolean incDraws() {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "incDraws"));
 		pairs.add(new BasicNameValuePair(USERNAME, player.getUserName()));
 		return updateDatabase();
 	}
 
 	/**
 	 * Increments the losses for a user in the database, returns false if
 	 * something went wrong.
 	 * 
 	 * @return true if the server operation was successful
 	 */
 	public boolean incLosses() {
 		pairs.clear();
 		pairs.add(new BasicNameValuePair(TAG, "incLosses"));
 		pairs.add(new BasicNameValuePair(USERNAME, player.getUserName()));
 
 		return updateDatabase();
 	}
 
 	/**
 	 * Contacts the database with an update and returns true if it sucseeded or
 	 * false if something went wrong along the way.
 	 * 
 	 * @return true if nothing went wrong, true otherwise.
 	 */
 	public boolean updateDatabase() {
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 			HttpResponse response = client.execute(httpPost);
 			HttpEntity entity = response.getEntity();
 			InputStream iStream = entity.getContent();
 			Scanner sc = new Scanner(iStream);
 			if (sc.hasNext()) {
 				String s = sc.next();
 				if (s.equals("true")) {
 					return true;
 				}
 			}
 			return false;
 		} catch (UnsupportedEncodingException e) {
 			return false;
 		} catch (ClientProtocolException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Contacts the database with an update and returns true if it sucseeded or
 	 * false if something went wrong along the way.
 	 * 
 	 * @return true if nothing went wrong, true otherwise.
 	 */
 	public boolean updateDatabase(List<BasicNameValuePair> l) {
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(l));
 			HttpResponse response = client.execute(httpPost);
 			HttpEntity entity = response.getEntity();
 			InputStream iStream = entity.getContent();
 			Scanner sc = new Scanner(iStream);
 			if (sc.hasNext()) {
 				String s = sc.next();
 					if (s.equals("true")) {
 					return true;
 				}
 			}
 			return false;
 		} catch (UnsupportedEncodingException e) {
 			return false;
 		} catch (ClientProtocolException e) {
 			return false;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Requests a String from the database, returns null if something went
 	 * wrong.
 	 * 
 	 * @return The String requested from the database.
 	 */
 	public String getFromDatabase() {
 		try {
 			httpPost.setEntity(new UrlEncodedFormEntity(pairs));
 			HttpResponse response = client.execute(httpPost);
 			HttpEntity entity = response.getEntity();
 			is = entity.getContent();
 			Scanner sc = new Scanner(is);
 			StringBuilder sb = new StringBuilder();
 			while (sc.hasNext()) {
 				sb.append(sc.next());
 			}
 			return sb.toString();
 		} catch (UnsupportedEncodingException e) {
 			return null;
 		} catch (ClientProtocolException e) {
 			return null;
 		} catch (IllegalStateException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	/**
 	 * A method for encrypting a string using the common md5-encryption pattern.
 	 * 
 	 * @param s
 	 *            The String to encrypt
 	 * @return The encrypted string.
 	 */
 	public String encrypt(String s) throws NoSuchAlgorithmException {
 		MessageDigest md5enc = MessageDigest.getInstance("MD5");
 		md5enc.update(s.getBytes(), 0, s.length());
 		return new BigInteger(1, md5enc.digest()).toString(16);
 	}
 
 }
