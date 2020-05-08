 package edu.neumont.csc380.models;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlRootElement(name="board")
 public class Board 
 {
<<<<<<< HEAD
 	@XmlElement(name="tiles")
 	private String[][] tiles;
=======
 	@XmlElement(name="board")
 	private String[][] board;
 	public Board() {}
>>>>>>> Changed GameStatusMessage class
 	
 	public Board(String[][] board)
 	{
<<<<<<< HEAD
 		tiles = new String[3][3];
 	}
 	
 	public String[][] getBoard() {
 		return tiles;
 	}
 	
 	public void setBoard(String[][] board) {
 		this.tiles = board;
=======
 		this.board = board;
>>>>>>> Changed GameStatusMessage class
 	}
 	
 	@Override
 	public String toString()
 	{
 		String result = "";
 		// Example toString(): (xoEMPTY),(xEMPTYo),(oEMPTYx)
 		for(int row = 0; row < tiles.length; row++) {
 			result += "(";
 			for(int column = 0; column < tiles.length; column++) {
 				result += tiles[row][column];
 			}
 			result += "),";
 		}
 		// Take out the extra comma at the end of result
 		result.substring(result.length()).replace(",", "");
 		
 		return result;
 	}
 }
