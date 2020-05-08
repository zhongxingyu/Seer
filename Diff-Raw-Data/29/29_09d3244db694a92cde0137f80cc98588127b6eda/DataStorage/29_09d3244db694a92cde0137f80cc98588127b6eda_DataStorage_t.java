 package com.socaldevs.jeopardy;
 
 public interface DataStorage {
 
 	public Question getQuestion(int col, int row);
 	
	public void put(String question, String answer, int col, int row, int value);
	public void put(Question question);
 	
 }
