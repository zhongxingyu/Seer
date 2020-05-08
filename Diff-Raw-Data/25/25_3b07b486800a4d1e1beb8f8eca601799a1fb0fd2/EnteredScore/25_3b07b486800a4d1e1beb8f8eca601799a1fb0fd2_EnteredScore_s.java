 package com.basinc.golfminus.view.teetime;
 
 import com.basinc.golfminus.domain.User;
 
 public class EnteredScore {
 
 	private User user;
 	private Integer grossScore;
	private Integer netScore;
 	
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
	public Integer getNetScore() {
		return netScore;
 	}
	public void setNetScore(Integer netScore) {
		this.netScore = netScore;
 	}
 }
