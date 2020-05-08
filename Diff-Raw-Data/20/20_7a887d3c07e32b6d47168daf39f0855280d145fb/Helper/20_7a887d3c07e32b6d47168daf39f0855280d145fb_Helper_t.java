 package cs213.chess.utils;
 
 import cs213.chess.exceptions.IllegalCoordsException;
 import cs213.chess.exceptions.IllegalFileRankException;
 
 /**
  * @author Bilal Quadri
  *
  */
 public class Helper {
 
 	public static int[] fileRankToCoords(String fileRank) throws IllegalFileRankException {
 		if(fileRank.length() < 2) {
 			throw new IllegalFileRankException("Invalid rank file.");
 		}
 		return fileRankToCoords(fileRank.charAt(0), Character.getNumericValue(fileRank.charAt(1)));
 	}
 	
 	public static int[] fileRankToCoords(char file, int rank) throws IllegalFileRankException {
 		int i = file - 'a';
 		int j = rank - 1;
 		
 		if (i < 0 || i > 7 || j < 0 || j > 7) {
 			throw new IllegalFileRankException("File: " + i + " ; Rank: " + j);
 		}
 
 		int[] coords = {i, j};
 		return coords;
 	}
 	
 	public static String coordsTofileRank(int i, int j) throws IllegalCoordsException {
 		if (i < 0 || j < 0 || i > 7 || j > 7) {
 			throw new IllegalCoordsException("Invalid coordinates passed");
 		}
 		
 		String fileRank = ((char) ('a' + i)) + "" + (j + 1);
 		return fileRank;
 	}
 	
 	public static String getSquareRepresentation(int i, int j) throws IllegalCoordsException {
 		if (i < 0 || j < 0 || i > 7 || j > 7) {
 			throw new IllegalCoordsException("Invalid coordinates passed");
 		}
 		if (i % 2 == 0 && j % 2 == 0) {
 			return "##";
 		} else if (i % 2 != 0 && j % 2 != 0) {
 			return "##";
 		} else {
 			return "  ";
 		}
 	}
 	
	public static String getSquareRepresentation(char file, int rank) throws IllegalFileRankException {
		int[] coords = fileRankToCoords(file, rank);
 		try {
 			return getSquareRepresentation(coords[0], coords[1]);
 		} catch (IllegalCoordsException e) {
 			throw new IllegalFileRankException("Invalid rank file.");
 		}
 	}
 	
	public static String getSquareRepresentation(String fileRank) throws IllegalFileRankException {
		int[] coords = fileRankToCoords(fileRank);
 		try {
 			return getSquareRepresentation(coords[0], coords[1]);
 		} catch (IllegalCoordsException e) {
 			throw new IllegalFileRankException("Invalid rank file.");
 		}
 	}
 }
