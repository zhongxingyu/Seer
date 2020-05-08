 package edu.ai;
 
 public class UtilityBlocker implements IUtility {
 
 	private int[] nLocked;
 	
 	@Override
 	public double getUtility(PylosEnvironment e, int c) {
 		if(e.isTerminal()) {
 			if(e.getWinner() == c) return 1000;
 			else return -1000;
 		}
 		nLocked = new int[3];
 		nLocked[PylosColour.WHITE]=nLocked[PylosColour.BLACK]=0;
 		for(int z = 0; z < 4; z++) {
 			for(int x = 0; x < 4-z; x++) {
 				for(int y = 0; y < 4-z; y++) {
 					if(e.isLocked(x,y,z)) {
 						nLocked[e.board[z][x][y]]++;
 					}
 				}
 			}
 		}
		return nLocked[c]-nLocked[PylosEnvironment.changeCurrent(c)];
 	}
 
 }
