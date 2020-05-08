 package com.techtalk4geeks.blogspot;
 
 enum Rank
 {
 	LILYPAD, LEECH, FLY, POND_SKATER, POND_SNAIL, TADPOLE, KOI, FROG, DUCK, TURTLE
 }
 
 public class User
 {
 	String myName = "User.getName()";
 	int myLevel = 1;
 	Rank myRank;
 	int myBirthYear;
 	private int myHP;
 	private int mySP;
 	private int myPOW;
 	private int myDEF;
 	private int mySPEED;
 
 	public User(String name, Rank rank, int birthYear)
 	{
		myName = name;
 		myBirthYear = birthYear;
 		myRank = rank;
		myLevel = (int)(myBirthYear / 500);
 		mySP = (int)(myLevel * 1.5);
 		myHP = (int)(myBirthYear * mySP / 1000.0);
 	}
 
 	public String getName()
 	{
 		return myName;
 	}
 
 	public void setName(String name)
 	{
 		myName = name;
 	}
 
 	public int getLevel()
 	{
 		return myLevel;
 	}
 
 	int getHP()
 	{
 		return myHP;
 	}
 
 	void setHP(int myHP)
 	{
 		this.myHP = myHP;
 	}
 
 	int getSP()
 	{
 		return mySP;
 	}
 
 	void setSP(int mySP)
 	{
 		this.mySP = mySP;
 	}
 
 	int getPOW()
 	{
 		return myPOW;
 	}
 
 	void setPOW(int myPOW)
 	{
 		this.myPOW = myPOW;
 	}
 
 	int getDEF()
 	{
 		return myDEF;
 	}
 
 	void setDEF(int myDEF)
 	{
 		this.myDEF = myDEF;
 	}
 
 	int getSPEED()
 	{
 		return mySPEED;
 	}
 
 	void setSPEED(int mySPEED)
 	{
 		this.mySPEED = mySPEED;
 	}
 }
