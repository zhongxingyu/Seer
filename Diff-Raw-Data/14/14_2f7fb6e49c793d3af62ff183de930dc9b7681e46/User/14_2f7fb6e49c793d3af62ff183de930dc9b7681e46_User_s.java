 package com.techtalk4geeks.blogspot;
 
 enum Rank {
	LILYPAD, FLY, TADPOLE, KOI, FROG
 }
 
 public class User
 {
 String myName = "User.getName()";
 int myLevel = 1;
int myRank = 1;
 int myBirthYear;
 int myHP;
 int mySP;
 int myPOW;
 int myDEF;
 int mySPEED;
 
	public User(String name, int birthYear, int SP) {
 		myName = name;
 		myBirthYear = birthYear;
 		mySP = SP;
 	}
 	
 	public String getName() {
 		return myName;
 	}
 	
 	public int getLevel() {
 		return myLevel;
 	}
	

	
 }
