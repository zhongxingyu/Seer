 package org.droidstack.stackapi;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Stats {
 	
 	public final long totalQuestions;
 	public final long totalUnanswered;
 	public final long totalAnswers;
 	public final long totalComments;
 	public final long totalVotes;
 	public final long totalBadges;
 	public final long totalUsers;
 	public final double questionsPerMinute;
 	public final double answersPerMinute;
 	public final double badgesPerMinute;
 	public final String apiVersion;
 	public final String apiRevision;
	public final String displayName;
 	
 	public Stats(JSONObject json) throws JSONException {
 		json = json.getJSONArray("statistics").getJSONObject(0);
 		totalQuestions = json.getLong("total_questions");
 		totalUnanswered = json.getLong("total_unanswered");
 		totalAnswers = json.getLong("total_answers");
 		totalComments = json.getLong("total_comments");
 		totalVotes = json.getLong("total_votes");
 		totalBadges = json.getLong("total_badges");
 		totalUsers = json.getLong("total_users");
 		
 		questionsPerMinute = json.getDouble("questions_per_minute");
 		answersPerMinute = json.getDouble("answers_per_minute");
 		badgesPerMinute = json.getDouble("badges_per_minute");
 		
 		apiVersion = json.getJSONObject("api_version").getString("version");
 		apiRevision = json.getJSONObject("api_version").getString("revision");
 		
		displayName = json.getString("display_name");
 	}
 	
 }
