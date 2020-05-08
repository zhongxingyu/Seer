 package model;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class PictureResponse implements Question {
 
 	public static final int type=4;
 	private String url;
 	private Set<String> answers;
 	private int qID;
 	private ArrayList<String> userAnswers;
 	
 	public static String getHTMLInputString(){
 		
 		StringBuilder html = new StringBuilder();
 		html.append("<br />Insert picture url: <br /><input type=\"text\" name=\"url\" required />");
 		html.append("<br />Insert All Possible Answers, one on each line:");
 		html.append("<br /><textarea name=\"answers\" cols=\"20\" rows=\"10\" required></textarea>");
 		
 		return html.toString();
 	}
 
 
 	public int getqID() {
 		return qID;
 	}
 
 	public void setqID(int qID) {
 		this.qID = qID;
 	}
 
 	public PictureResponse(String url, Set<String> ans) { // pushes to database
 		this.url = url;
 		this.answers = ans;
 	}
 
 	public PictureResponse(int id, Connection con) { // pulls from database
 		generate(id, con);
 	}
 	
 	public void generate(int id, Connection con) {
 		setqID(id);
 		try {
 			
 			PreparedStatement ps = con.prepareStatement("select * from picture_response_question where question_id = ?");
 			ps.setInt(1, id);
 			ResultSet resultSet = ps.executeQuery();
 			
 			
 			String ans = new String();
 			while (resultSet.next()) {
 				url = resultSet.getString("url");
 				ans = resultSet.getString("answer");
 				
 			}
			
 			String[] strings = ans.split(Pattern.quote(" &&& "));
 			answers = new HashSet<String>();
 			for (String string : strings) {
 				answers.add(string);
 			}
				
			
			
 		}catch(Exception e){
 			
 		}
 		
 	}
 
 	public String getURL() {
 		return url;
 	}
 
 	public void setURL(String url) {
 		this.url = url;
 	}
 
 	public Set<String> getAnswers() {
 		return answers;
 	}
 
 	public void setAnswers(Set<String> answers) {
 		this.answers = answers;
 	}
 
 	public int solve(ArrayList<String> ans) {
 		if (ans.size()!=1) {
 			return 0; // input cleansing
 		}
 		
 		for (String a : ans) {
 			if (answers.contains(a)) {
 				return 1;
 			}
 		}
 		return 0;
 	}
 
 	@Override
 	public String toHTMLString() {
 		StringBuilder html = new StringBuilder();
 		
 		html.append("<img src=\"" + url + "\" width=\"200\" height=\"200\" />" );
         html.append("<br /><input type=\"text\" name=\"");
 		html.append(type);
 		html.append("_");
         html.append(qID);
         html.append("\" id=\"");
 		html.append(type);
 		html.append("_");
 		html.append(qID);
         html.append("\" />");
 		
 		return html.toString();
 	}
 
 	@Override
 	public String getCorrectAnswers() {
 		StringBuilder correctAnswers = new StringBuilder();
 		
 		for(String s : answers) {
 			s=s.trim();
 			correctAnswers.append(s);
 			correctAnswers.append(",  ");
 		}
 		correctAnswers.replace(correctAnswers.length()-3, correctAnswers.length(), "");
 		
 		return correctAnswers.toString();
 	}
 	public int getType(){
 		return type;
 	}
 
 	@Override
 	public void pushToDB(Connection con) {
 		Statement stmt;
 		
 		try {
 			stmt = con.createStatement();
 			StringBuilder sqlString = new StringBuilder("INSERT INTO picture_response_question VALUES(null,\"");
 			url = url.trim();
 			sqlString.append(url);
			sqlString.append("\",\" ");
 			for (String string : answers) {
 				string = string.trim();
 				sqlString.append(string);
 				sqlString.append(" &&& ");
 			}
 			sqlString.replace(sqlString.length()-5, sqlString.length(), "");
 			sqlString.append("\") ");
 			
 			System.out.print(sqlString.toString());
 			stmt.executeUpdate(sqlString.toString());
 			
 			stmt = con.createStatement();
 			sqlString = new StringBuilder("SELECT * FROM picture_response_question WHERE url=\"");
 			sqlString.append(url);
 			sqlString.append("\" ");
 			
 			System.out.print(sqlString.toString());
 			ResultSet resultSet = stmt.executeQuery(sqlString.toString());
 			
 			
 			while (resultSet.next()) {
 				this.setqID(resultSet.getInt("question_id")); // will always be the last one
 			}
 		}catch(Exception e){
 			
 		}		
 	}
 
 	@Override
 	public void setUserAnswers(ArrayList<String> ans) {
 		userAnswers = new ArrayList<String>();
 		
 		for(String s : ans) {
 			userAnswers.add(s);
 		}
 	}
 
 	@Override
 	public String getUserAnswers() {
 		StringBuilder userAns = new StringBuilder();
 		
 		for(String s : userAnswers) {
 			userAns.append(s);
 			userAns.append(", ");
 		}
 		userAns.replace(userAns.length()-2, userAns.length(), "");
 		
 		return userAns.toString();
 	}
 
 
 	@Override
 	public int getTotalQScore() {
 		return 1;
 	}
 
 
 	@Override
 	public int getNumAnswers() {
 		return 1;
 	}
 }
