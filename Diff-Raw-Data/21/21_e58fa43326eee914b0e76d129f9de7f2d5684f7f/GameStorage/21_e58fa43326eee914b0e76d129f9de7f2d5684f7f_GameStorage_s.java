 package com.episkipoe.common;
 
 public interface GameStorage {
 	public void saveGame() ;
 	public boolean loadGame() throws Exception ;
 	public void startup() throws Exception ;
 	public void newGame() throws Exception ;
	public void loadRooms() throws Exception ;
 	public String[] getCommonImages() ;
 }
