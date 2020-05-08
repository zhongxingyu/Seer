 package com.geeksong.agricolascorer.model;
 
 public class Score {
 	private int fieldScore;
 	private int pastureScore;
 	private int grainScore;
 	private int vegetableScore;
 	private int sheepScore;
 	private int boarScore;
 	private int cattleScore;
 	private int unusedSpacesScore;
 	private int fencedStablesScore;
 	private int roomsScore;
 	private RoomType roomType;
 	private int familyMemberScore;
 	private int pointsForCards;
 	private int bonusPoints;
 	private int beggingCardsScore;
 
 	public void setFieldScore(int fieldScore) {
 		this.fieldScore = fieldScore;
 	}
 
 	public void setPastureScore(int pastureScore) {
 		this.pastureScore = pastureScore;
 	}
 
 	public void setGrainScore(int grainScore) {
 		this.grainScore = grainScore;
 	}
 
 	public void setVegetableScore(int vegetableScore) {
 		this.vegetableScore = vegetableScore;
 	}
 
 	public void setSheepScore(int sheepScore) {
 		this.sheepScore = sheepScore;
 	}
 
 	public void setBoarScore(int boarScore) {
 		this.boarScore = boarScore;
 	}
 
 	public void setCattleScore(int cattleScore) {
 		this.cattleScore = cattleScore;
 	}
 
 	public void setUnusedSpacesScore(int unusedSpacesScore) {
 		this.unusedSpacesScore = unusedSpacesScore;
 	}
 
 	public void setFencedStablesScore(int fencedStablesScore) {
 		this.fencedStablesScore = fencedStablesScore;
 	}
 
 	public void setRoomsScore(int roomsScore) {
 		this.roomsScore = roomsScore;
 	}
 
 	public void setRoomType(RoomType roomType) {
 		this.roomType = roomType;
 	}
 
 	public void setFamilyMemberScore(int familyMemberScore) {
 		this.familyMemberScore = familyMemberScore;
 	}
 
 	public void setPointsForCards(int pointsForCards) {
 		this.pointsForCards = pointsForCards;
 	}
 
 	public void setBonusPoints(int bonusPoints) {
 		this.bonusPoints = bonusPoints;
 	}
 
 	public void setBeggingCardsScore(int beggingCardsScore) {
 		this.beggingCardsScore = beggingCardsScore;
 	}
 
 	public int getTotalScore() {
		return this.fieldScore + this.pastureScore + this.grainScore + this.vegetableScore + this.sheepScore + this.boarScore + this.cattleScore;
 	}
 }
