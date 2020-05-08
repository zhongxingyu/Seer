 package com.monopoly.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Board {
 
 	private List<Square> squares = new ArrayList<Square>();
 	
 	public Board() {
 		for (int i = 0; i < 40; i++) {
 			squares.add(new Square("Square" + i))
			
 		}
 	}
 	
 	public List<Square> getSquares() {
 		return squares;
 	}
 }
