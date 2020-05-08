 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class BD_Utils {
 	private static  Connection connection;
 	private static final String URL = "jdbc:mysql://localhost/films";
 	private static final String NAME = "root";
 	private static final String  PASSWORD = "";
 	private static List<Record> list;
 	static {
 		try {
 			connection = DriverManager.getConnection(URL,NAME,PASSWORD);
 			list = new ArrayList<>();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	private static ResultSet getResult(String s) throws SQLException {
 		Statement statement = connection.createStatement();
 		ResultSet result = statement.executeQuery(s);
 		return result;
 	}
 	private static void addFilms(String query,boolean flag) throws SQLException {
 		ResultSet rs = getResult(query);
 		addToArrayList(rs,flag);
 	}
 	private static Float avg(Float ratingOne, Float ratingTwo) {
 		return new BigDecimal((ratingOne + ratingTwo)/2).setScale(2,RoundingMode.UP).floatValue();
 	 
 	}
 	//flag = true - performs choosing films that contained in two tables concurrently
 	//type = false - performs choosing films that not contained in two tables concurrently
 	private static void addToArrayList(ResultSet rs,boolean flag) throws  SQLException {
 		if(flag) {	
 			while(rs.next()) {
 				list.add(new Record(rs.getString(1)+" ("+rs.getString(2)+")",avg(Float.parseFloat(rs.getString(3)),Float.parseFloat(rs.getString(4)))));
 			}
 		}
 		else {
 			while(rs.next()) {
				list.add(new Record(rs.getString(1)+" ("+rs.getString(2)+")",Float.parseFloat(rs.getString(3))/2));
 			}
 		}
 	}
 	public static String ratingCalculation(int ratingNumber) throws SQLException  {
 		String s =  "SELECT imdb.name FROM imdb INNER JOIN kinopoisk ON imdb.name=kinopoisk.name AND imdb.year=kinopoisk.year";
 		
 		addFilms("SELECT i.name,i.year,i.rating,k.rating FROM imdb AS i "
 				+ "INNER JOIN kinopoisk AS k ON i.name=k.name AND i.year=k.year",true);
 		
 		addFilms("SELECT imdb.name,imdb.year,imdb.rating FROM imdb "
 				+ "WHERE imdb.name NOT IN( " + s + " )",false);
 		
 		addFilms("SELECT kinopoisk.name,kinopoisk.year,kinopoisk.rating FROM kinopoisk "
 				+ "WHERE kinopoisk.name NOT IN( " + s + " )",false);
 				
 		Collections.sort(list);
 		connection.close();
 		return list.get(ratingNumber-1).toString();
 	}
 }
