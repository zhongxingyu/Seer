 package com.basinc.golfminus.view.teetime;
 
 import com.basinc.golfminus.domain.User;
 
 public class EnteredScore {
 
 	private User user;
 	private Integer grossScore;
	private Integer adjustedScore;
 	
 	public EnteredScore() {};
 	public EnteredScore(User user) {
 		setUser(user);
 	}
 	
 	public User getUser() {
 		return user;
 	}
 	public void setUser(User user) {
 		this.user = user;
 	}
 	public Integer getGrossScore() {
 		return grossScore;
 	}
 	public void setGrossScore(Integer grossScore) {
 		this.grossScore = grossScore;
 	}
	public Integer getAdjustedScore() {
		return adjustedScore;
 	}
	public void setAdjustedScore(Integer netScore) {
		this.adjustedScore = netScore;
 	}
 }
