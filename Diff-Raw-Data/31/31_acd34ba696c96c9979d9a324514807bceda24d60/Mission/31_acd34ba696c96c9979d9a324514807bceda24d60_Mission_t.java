 package ua.nevis.rainyday.data;
 
 public class Mission {
 	private boolean active;
 	private int missionNumber;
 	private int scoreValue;
 	private int starCount;
 	private int maxDropCount;
 	private int completeDropCount;
 
 	public Mission() {
 		this(false, 0, 0, 0);
 	}
 
 	public Mission(boolean active, int missionNumber, int maxDropCount, int completeDropCount) {
 		this.active = active;
 		this.missionNumber = missionNumber;
 		this.maxDropCount = maxDropCount;
 		this.completeDropCount = completeDropCount;
 		this.scoreValue = 0;
 		this.starCount = 0;
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}
 
 	public int getMissionNumber() {
 		return missionNumber;
 	}
 
 	public void setMissionNumber(int missionNumber) {
 		this.missionNumber = missionNumber;
 	}
 
 	public int getScoreValue() {
 		return scoreValue;
 	}
 
 	public void setScoreValue(int scoreValue) {
 		this.scoreValue = scoreValue;
 	}
 
 	public int getStarCount() {
 		return starCount;
 	}
 
 	public void setStarCount(int starCount) {
		if (starCount > 3) {
			this.starCount = 3;
		} else {
			this.starCount = starCount;
		}
 	}
 
 	public int getMaxDropCount() {
 		return maxDropCount;
 	}
 
 	public void setMaxDropCount(int maxDropCount) {
 		this.maxDropCount = maxDropCount;
 	}
 
 	public int getCompleteDropCount() {
 		return completeDropCount;
 	}
 
 	public void setCompleteDropCount(int completeDropCount) {
 		this.completeDropCount = completeDropCount;
 	}
 }
