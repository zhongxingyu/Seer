 package com.techtalk4geeks.blogspot;
 
 import java.util.ArrayList;
 
 enum SpecialRank
 {
	DO_NOT_CARE, DORK, GEEK, NERD, JOCK, BLONDE, TEACHER, HIPPIE, ANIMAL_LOVER, SHORT_PERSON, STRECH, HOBBIT, DWARF, VAMPIRE, NINJA, GANGSTER, 
 	EMO, PROFESSOR, CODER, FOX, WEREWOLF, ZOMBIE, NARWHAL, ALIEN, SWAG_MASTER, PRINCESS, CRAFTER, GAMER, VLOGGER, PREDATOR, WRESTLER, 
 	SUPER_HERO, RICH_GUY, BABY, GRANDPARENT, BIKER
 }
 
 public class User
 {
 	String myName = "User.getName()";
 	int myLevel = 1;
 	SpecialRank myRank;
 //	Date myBirthYear;
 	protected int myHP;
 	protected int mySP;
 	protected int myPOW;
 	protected int myDEF;
 	protected int mySPEED;
 	int myAge;
 	ArrayList<Item> inventory = new ArrayList<Item>();
 
 	public User(String name, String rank, int age)
 	{
 //		myBirthYear = birthYear;
 //		Date date = new Date(System.currentTimeMillis()) + 2100;
 		myAge = age;
 		myName = name;
 		myRank = SpecialRank.valueOf(rank);
 		myLevel = (int)(age * 2);
 		mySP = (int)(myLevel * 1.5 / 5);
 		myHP = (int)(age * mySP);
 		myPOW = (int)(mySP * 0.4);
 		myDEF = (int)(mySP * 0.6);
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
