 package database;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 import quiz.*;
 
 public class RatingBank {
 
 	private DBConnection connection;    //Singleton DBConnection instance
 
 	/*
 	 * Initializes the database Connection for later use.
 	 */
 	public RatingBank() {
 		connection = DBConnection.getDBConnection();
 	}
 	
 	public List<Rating> getRatings(int quizID) {
 		List<Rating> ratings = new ArrayList<Rating>();
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM rating WHERE quizid=" + quizID + " ORDER BY date DESC LIMIT 3");
 		try {
 			while (rs.next()) {
 				int user = rs.getInt("userid");
 				int quiz = rs.getInt("quizid");
 				int score = rs.getInt("value");
 				String date = rs.getString("date");
 				String review = rs.getString("review");
 				Rating rating = new Rating(user, quiz, score, review);
 				rating.setDate(date);
 				ratings.add(rating);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ratings;
 	}
 
 	public void addRating(Rating rating) {
 		String update;
 		if (RatingExists(rating.getQuizID(), rating.getUserID())) {
 			update = ("DELETE FROM rating WHERE  userid=" + rating.getUserID() + " AND quizid=" + rating.getQuizID());
 			connection.ExecuteUpdate(update);
		} 
		update = ("INSERT INTO rating VALUES (" + rating.getUserID() + ", " + rating.getQuizID() + ", current_timestamp, " + rating.getValue() + ", '" + rating.getReview() + "')"); 
		connection.ExecuteUpdate(update);
 	}
 
 	private boolean RatingExists(int quiz_id, int user_id) {
 		ResultSet rs = connection.ExecuteQuery("SELECT COUNT(*) FROM rating WHERE userid=" + user_id + " AND quizid=" + quiz_id);
 		int size = 0;
 		try {
 			rs.next();
 			size = rs.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return (size == 1);
 	}
 	
 	public static void main(String[] args) {
 		Rating rate = new Rating(9, 9, 5, "Fun quiz, would take again!");
 		RatingBank ratingbank = new RatingBank();
 		ratingbank.addRating(rate);
 	}
 }
