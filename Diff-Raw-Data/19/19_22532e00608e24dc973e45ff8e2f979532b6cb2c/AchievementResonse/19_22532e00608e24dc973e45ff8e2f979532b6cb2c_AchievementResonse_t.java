 package de.kile.zapfmaster2000.rest.api.achievements;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class AchievementResonse {
 
	private long achievementId;
 
 	private String achievementName;
 
 	private String achievementDescription;
 
 	private String achievementImage;
 
 	private final List<UserThatGained> users = new ArrayList<>();
 
	public long getAchievementId() {
		return achievementId;
 	}
 
	public void setAchievementId(long achivementId) {
		this.achievementId = achivementId;
 	}
 
 	public String getAchievementName() {
 		return achievementName;
 	}
 
 	public void setAchievementName(String achievementName) {
 		this.achievementName = achievementName;
 	}
 
 	public String getAchievementDescription() {
 		return achievementDescription;
 	}
 
 	public void setAchievementDescription(String achievementDescription) {
 		this.achievementDescription = achievementDescription;
 	}
 
 	public String getAchievementImage() {
 		return achievementImage;
 	}
 
 	public void setAchievementImage(String achievementImage) {
 		this.achievementImage = achievementImage;
 	}
 
 	public List<UserThatGained> getUsers() {
 		return users;
 	}
 
 	public static class UserThatGained {
 
 		private long userId;
 
 		private String userName;
 
 		private String userImage;
 
 		public long getUserId() {
 			return userId;
 		}
 
 		public void setUserId(long userId) {
 			this.userId = userId;
 		}
 
 		public String getUserName() {
 			return userName;
 		}
 
 		public void setUserName(String userName) {
 			this.userName = userName;
 		}
 
 		public String getUserImage() {
 			return userImage;
 		}
 
 		public void setUserImage(String userImage) {
 			this.userImage = userImage;
 		}
 
 	}
 
 }
