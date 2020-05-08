 package cl.own.usi.gateway.utils;
 
 import java.util.List;
 
 import cl.own.usi.gateway.client.UserInfoAndScore;
 
 public class ScoresHelper {
 
 	public static void appendUsersScores(List<UserInfoAndScore> users,
 			StringBuilder sb) {
 		StringBuilder scoresMails = new StringBuilder("\"mail\":[");
		StringBuilder scoresScores = new StringBuilder("\"scores\":[");
 		StringBuilder scoresFirstName = new StringBuilder(
 				"\"firstname\":[");
		StringBuilder scoresLastname = new StringBuilder("\"lastname\":[");
 		boolean first = true;
 		for (UserInfoAndScore user : users) {
 			if (!first) {
 				scoresMails.append(",");
 			}
 			scoresMails.append("\"").append(user.getEmail()).append("\"");
 			if (!first) {
 				scoresScores.append(",");
 			}
 			scoresScores.append("\"").append(user.getScore()).append("\"");
 			if (!first) {
 				scoresFirstName.append(",");
 			}
 			scoresFirstName.append("\"").append(user.getFirstname())
 					.append("\"");
 			if (!first) {
 				scoresLastname.append(",");
 			}
 			scoresLastname.append("\"").append(user.getLastname())
 					.append("\"");
 			first = false;
 		}
 		sb.append(scoresMails).append("],");
 		sb.append(scoresScores).append("],");
 		sb.append(scoresFirstName).append("],");
 		sb.append(scoresLastname).append("]");
 	}
 	
 }
