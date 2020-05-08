 /**
  * 
  */
 package com.sjsu.siquoia.model;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 /**
  * @author Parnit Sainion
  * @since 28 November 2013
  * Description: This class obtains the JSON and parsers it accordingly for subjects,topics,subtopics or the quiz itself.
  */
 public class SiQuoiaJSONParser {
 
 	/**
 	 * 
 	 */
 	public SiQuoiaJSONParser() {
 		// TODO Auto-generated constructor stub
 	}
 
 	
 	/**
 	 * parses JSON into User object
 	 * @param json JSON contain user information
 	 */
 	public static User parseUser(String json)
 	{
 		User user = new User();
 		JSONObject jsonObj = new JSONObject();
 		
 		try {		
 			//convert JSON string to JSONArrya
 			JSONArray jsonArray = new JSONArray(json);
 			
 			//get User JSON Object
 			jsonObj = jsonArray.getJSONObject(0);
 			
 			//set user information
 			user.setEmail(jsonObj.get("email").toString());
 			user.setCurrentQuiz(jsonObj.getString("currentQuiz").toString());
 			user.setAnswers(jsonObj.get("currentAns").toString());
 			user.setPacketType(jsonObj.get("packetType").toString());
 			user.setSequoiaBucks(Integer.parseInt(jsonObj.getString("siquoiaPoints").toString()));
 			user.setPacketsBought(Integer.parseInt(jsonObj.getString("packetsBought").toString()));
 			user.setMemorabiliaBought(Integer.parseInt(jsonObj.getString("memorabilia").toString()));
 			user.setTotalPointsSpent(Integer.parseInt(jsonObj.getString("totalPointsSpent").toString()));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return user;
 	}
 	
 	/**
 	 * parses JSON into User object
 	 * @param json JSON contain user information
 	 */
 	public static ArrayList<String> parseSubject(String json)
 	{
 		//declare variables
 		ArrayList<String> subjects = new ArrayList<String>();
 		subjects.add("Any");
 		JSONObject jsonObj = new JSONObject();
 		int size =0;
 		
 		try {		
 			//convert JSON string to JSONArrya
 			JSONArray jsonArray = new JSONArray(json);
 			
 			size = jsonArray.length();
 			
 			for( int count =0; count < size; count++)
 			{
 				jsonObj = (JSONObject) jsonArray.get(count);
 				subjects.add(jsonObj.getString("subjectText").toString());
 			}			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return subjects;
 	}
 	
 	/**
 	 * parses JSON into topics list
 	 * @param json JSON contain topics
 	 */
 	public static ArrayList<String> parseTopics(String json)
 	{
 		//declare variables
 		ArrayList<String> topics = new ArrayList<String>();
 		topics.add("Any");
 		JSONObject jsonObj = new JSONObject();
 		int size =0;
 		
 		try {		
 			//convert JSON string to JSONArrya
 			JSONArray jsonArray = new JSONArray(json);
 			
 			size = jsonArray.length();
 			
 			for( int count =0; count < size; count++)
 			{
 				jsonObj = (JSONObject) jsonArray.get(count);
 				topics.add(jsonObj.getString("topic").toString());
 			}			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return topics;
 	}
 	
 	/**
 	 * parses JSON into subtopics list
 	 * @param json JSON contain subtopics
 	 */
 	public static ArrayList<String> parseSubtopics(String json)
 	{
 		//declare variables
 		ArrayList<String> subtopics = new ArrayList<String>();
 		subtopics.add("Any");
 		JSONObject jsonObj = new JSONObject();
 		int size =0;
 		
 		try {		
 			//convert JSON string to JSONArrya
 			JSONArray jsonArray = new JSONArray(json);
 			
 			size = jsonArray.length();
 			
 			for( int count =0; count < size; count++)
 			{
 				jsonObj = (JSONObject) jsonArray.get(count);
 				subtopics.add(jsonObj.getString("subtopic").toString());
 			}			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return subtopics;
 	}
 	
 	
 	/**
 	 * parses JSON into quiz list
 	 * @param json JSON contain quiz
 	 */
 	public static ArrayList<Question> parseQuiz(String json, String currentAnswers, String packetType)
 	{
 		//declare variables
 		ArrayList<Question> quiz = new ArrayList<Question>();
 		JSONObject jsonObj = new JSONObject();
 		int size;
 		Question question;
 		int numOfAnswers = currentAnswers.length();
 		boolean isNormal = packetType.equalsIgnoreCase("normal");
 		
 		try {		
 			//convert JSON string to JSONArray
 			JSONArray jsonArray = new JSONArray(json);
 			
 			size = jsonArray.length();
 			
 			for( int count =0; count < size; )
 			{
 				question = new Question();
 				jsonObj = (JSONObject) jsonArray.get(count);
 				question.setQuestion(jsonObj.getString("questionText").toString());		
 				
 				question.addChoice(jsonObj.getString("answerOne").toString());
 				question.addChoice(jsonObj.getString("answerTwo").toString());
 				question.addChoice(jsonObj.getString("answerThree").toString());
 				question.addChoice(jsonObj.getString("answerFour").toString());
 				question.setCorrectChoice(jsonObj.getInt("correctAns") -1);
 				
 				if(isNormal)
 					question.setRank(jsonObj.getInt("rank"));				
 				
 				if(count < numOfAnswers)
 				{
 					String answer = currentAnswers.charAt(count)+"";
 					question.setStatus(Integer.parseInt(answer));
 				}
 				
 				//increment count and set question title
 				question.setTitle("Question "+ (++count));
 				
 				quiz.add(question);
 			}			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return quiz;
 	}
 	
 	/**
 	 * parse the leader board JSON
 	 * @param json JSON representing leader board
 	 * @return ArrayList of questions for leader board
 	 */
 	public static ArrayList<Question> parseLeaderboard(String json)
 	{
 		//declare variables
 		ArrayList<Question> questions = new ArrayList<Question>();
 		JSONObject jsonObj = new JSONObject();
 		int size;
 		Question question;
 		
 		try {		
 			//convert JSON string to JSONArray
 			JSONArray jsonArray = new JSONArray(json);
 			
 			size = jsonArray.length();
 			
 			for( int count =0; count < size;)
 			{
 				question = new Question();
 				jsonObj = (JSONObject) jsonArray.get(count);
 				question.setQuestion(jsonObj.getString("questionText").toString());
 				question.setRank(jsonObj.getInt("rank"));
 				
 				//increment count and set title (so it starts with #1)
 				question.setTitle(++count +". ");
 				questions.add(question);
 			}			
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return questions;
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
