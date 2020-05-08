 package com.example.skidrow;
 
 /**
  * This class will contain the players information -- information holder.
  * @author Michael Underwood
  * @version 0.1
  */
 
 public class Player {
 	
 	private String name;
 	private int communicationSkills,fighterSkills, driverSkills, dealerSkills;
	private String difficultyLevel;
 
	public Player(String name, int communicationSkills, int fighterSkills, int driverSkills, int dealerSkills, String  difficultyLevel)
 	{
 		this.name = name;
 		this.communicationSkills = communicationSkills;
 		this.fighterSkills = fighterSkills;
 		this.driverSkills = driverSkills;
 		this.dealerSkills = dealerSkills;
		this.difficultyLevel= difficultyLevel;
 		System.out.println("Name: " + name + " ComSkills: " + communicationSkills + 
 				" FightSkills: " + fighterSkills + " DriveSkills: " + driverSkills
				+ " DealSkills: " + dealerSkills + "difficultyLevel"+ difficultyLevel);
 	}
 	
 
 }
