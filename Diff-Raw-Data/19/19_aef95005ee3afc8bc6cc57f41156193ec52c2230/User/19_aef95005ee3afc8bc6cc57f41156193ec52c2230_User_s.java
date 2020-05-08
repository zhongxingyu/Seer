 package frontend;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 
 
 import sql.DB;
 
 public class User {
 	private final String id;
 	private final DB db;
 	private final String hash;
 	private static final int historyLimit = 5;
 	private String profPicture;
 	
 	public User(String user, String hash, boolean isAdmin, DB db, String profPicture){
 		this.db = db;
 		this.id = user;
 		this.hash = hash;
 		this.profPicture = profPicture; //default user picture
 		//String hash = hash(password);
 		//db.addUser(user, hash, isAdmin);
 	}
 	
 	// takes a user id and returns a link to their profile page
 	static public String getUserProfileLink(String userId){
 		return "<a href='ProfileViewServlet?user=" + userId + "'>" + userId + "</a>" ;
 	}
 	
 	public String getHash(){
 		return hash;
 	}
 	
 	public String getID(){
 		return id;
 	}
 	
 	// returns a html link to this user's profile
 	public String getProfileLink(){
 		return getUserProfileLink(id);
 	}
 	
 	public ArrayList<Quiz> getCreatedQuizzes(){
 		return db.getCreatedQuizzes(this.id, historyLimit);
 	}
 	
 
 
 	
 	private static String hexToString(byte[] bytes) {
 		StringBuffer buff = new StringBuffer();
 		for (int i=0; i<bytes.length; i++) {
 			int val = bytes[i];
 			val = val & 0xff;  // remove higher bits, sign
 			if (val<16) buff.append('0'); // leading 0
 			buff.append(Integer.toString(val, 16));
 		}
 		return buff.toString();
 	}
 	
 	private String hash(String raw){
 		String hash = "password";
 		try {
 			MessageDigest md = MessageDigest.getInstance("SHA");
 			byte[] array = md.digest(raw.getBytes());
 			hash = hexToString(array);
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		return hash;
 	}
 	
 	/**
 	 * returns a string of all 'id2' values in the friends table 
 	 * where 'id1' is this user
 	 * note: this has nothing to do with the requests table
 	 * @return
 	 */
 	public ArrayList<String> getFriends(){
 		return db.getFriends(this.id);
 	}
 	
 	
 	public History getHistory(){
 		return db.getHistory(this.id, historyLimit);
 	}
 	
 	public ArrayList<Achievement> getAchievements(){
 		return db.getAchievements(this.id);
 	}
 	
 	public ArrayList<Challenge> getChallenges(){
 		return db.getChallenges(this.id);
 	}
 	
 	public ArrayList<Message> getNotes(){
 		return db.getNotes(this.id);
 	}
 	
 	public boolean isAdmin(){
 		return db.getIsAdmin(this.id);
 	}
 	
 	public void addAchievement(Achievement achievement){
 		db.addAchievement(this.id, achievement);
 	}
 	
 	// returns the list of result objects of a specific quiz,
 	public ArrayList<Result> getResult(String quizId){
 		return db.getQuizResults(quizId, id);
 	}
 	
 	// gets results for all quizes taken
 	public ArrayList<Result> getResults(){
 		return db.getResults(this.id, Integer.MAX_VALUE);
 	}
 	
 	// returns the best score a user has gotten on a quiz
 	// in terms of percent correct
 	// and 0 if they haven't taken it
 	public int getBestScore(String quizId){
 		ArrayList<Result> results = getResult(quizId);
 		int bestScore = 0;
 		for(Result r : results){
 			if(r.getPercentCorrect() > bestScore) bestScore = (int) r.getPercentCorrect();
 		}
 		
 		return bestScore;
 	}
 	
 	public int getWorstScore(String quizId){
		//TODO
		return 999;
 	}
 	
 	public int getAverageScore(String quizId){
		//TODO
		return 999;
 	}
 	
 	
 	/**
 	 * adds a row to the friends table with this user and id
 	 * as id1 and id2, respectively
 	 * note: this has nothing to do with the requests table
 	 * @param id
 	 */
 	public void addFriend(String id){
 		db.addFriend(this.id, id);
 	}
 	
 	/**
 	 * This removes any row in the friends table containing these
 	 * two id's, regardless of order. That is, removeFriend(John, Stu)
 	 * will remove instances of both John, Stu and Stu, John in the 
 	 * friends table.
 	 * It ALSO removes any and all friend requests between these two
 	 * users in the requests table, regardless of isConfirmed's value
 	 * @param id
 	 */
 	public void removeFriend(String id){
 		db.removeFriend(this.id, id);
 	}
 	/**
 	 * returns an array of friend requests, each of which contains 
 	 * a src, dest, body, time (all strings) and isConfirmed (boolean)
 	 * values
 	 * @return
 	 */
 	public ArrayList<FriendRequest> getFriendRequests(){
 		return db.getFriendRequests(this.id);
 	}
 	
 	public void addResult(Result result){
 		db.addResult(this.id, result);
 	}
 	
 	public void setAdminStatus(boolean status){
 		db.setAdminStatus(this.id, status);
 	}
 	
 	// returns true if the user has a pending friend request from "username"
 	public boolean hasRequestFrom(String username){
 		ArrayList<FriendRequest> requests = getFriendRequests();
 		
 		// check all requests
 		for(FriendRequest r : requests){
 			if(r.getSrc().equals(username)) return true;
 		}
 		
 		return false;
 	}
 	
 	public ArrayList<Quiz> getQuizzes(){
 		return db.getCreatedQuizzes(id, Integer.MAX_VALUE);
 	}
 	
 	
 	
 	public String getProfPicture() {
 		return profPicture;
 	}
 	
 	public void setProfPicture(String url) {
 		this.profPicture = url;
 		db.setUserImage(this.id, url);
 	}
 	
 }
 
 
 
 
