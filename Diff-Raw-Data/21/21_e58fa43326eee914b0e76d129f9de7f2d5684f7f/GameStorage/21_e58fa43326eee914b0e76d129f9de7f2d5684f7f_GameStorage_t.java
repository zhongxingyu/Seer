 package com.episkipoe.common;
 
/**
 *  Save/Load/Start a new game
 */
 public interface GameStorage {
 	public void saveGame() ;
 	public boolean loadGame() throws Exception ;
 	public void startup() throws Exception ;
 	public void newGame() throws Exception ;
 	public String[] getCommonImages() ;
 }
