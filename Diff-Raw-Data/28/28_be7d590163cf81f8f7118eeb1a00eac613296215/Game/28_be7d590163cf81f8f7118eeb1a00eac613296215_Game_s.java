 package com.example.skidrow;
 
 public class Game {
 	private Player player;
 	
 	/**
 	 * Constructor for the game class
 	 */
 	public Game(){
 	}
 	
 	/**
 	 * Method used to create a new instance of player in the game.
 	 * 
 	 * @param name Name of the Player
 	 * @param communicationSkills Communications skill points
 	 * @param fighterSkills Fighter skill points
 	 * @param driverSkills Driver skill points
 	 * @param dealerSkills Dealer skill Points
 	 */
	public void createPlayer(String name, int communicationSkills, int fighterSkills, int driverSkills, int dealerSkills){
		player = new Player(name, communicationSkills, fighterSkills, driverSkills, dealerSkills);
 	}
 }
