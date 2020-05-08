 package com.techtalk4geeks.blogspot;
 
 import java.sql.Date;
 
 enum Rank
 {
 	LILYPAD, LEECH, FLY, POND_SKATER, POND_SNAIL, TADPOLE, KOI, FROG, DUCK, TURTLE
 }
 
 enum SpecialRank
 {
	DORK, GEEK, NERD, JOCK, BLONDE, TEACHER, HIPPIE, ANIMAL-lOVER, SHORT-PERSON, STRECH, HOBBIT, DWARF, VAMPIRE, NINJA, GANGSTER, EMO, PROFESSOR, CODER
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
 	int myAge;
 
 	public User(String name, Rank rank, int birthYear)
 	{
 		myBirthYear = birthYear;
 //		Date date = new Date(System.currentTimeMillis()) + 2100;
 //		myAge = date.getYear() - myBirthYear;
 		myName = name;
 		myRank = rank;
 		myLevel = (int)(myBirthYear / 5);
 		mySP = (int)(myLevel * 1.5 / 5);
 		myHP = (int)(myBirthYear * mySP / 10000.0);
 		myPOW = (int)(mySP * 0.4 / 1.5);
 		myDEF = (int)(mySP * 0.6 / 2);
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
