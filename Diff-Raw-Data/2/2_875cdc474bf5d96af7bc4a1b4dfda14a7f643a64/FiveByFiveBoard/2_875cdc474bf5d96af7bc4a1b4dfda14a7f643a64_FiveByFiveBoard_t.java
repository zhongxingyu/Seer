 package com.soartech.bolt.evaluation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 public class FiveByFiveBoard {
 	private HashMap<String, BoardLocation> lastLocation = new HashMap<String, BoardLocation>();
 	private LinkedHashMap<BoardLocation, ThreeByThreeConfig> locToConfig = new LinkedHashMap<BoardLocation, ThreeByThreeConfig>();
 	
 	public FiveByFiveBoard() {
 		for(Preposition p : Preposition.values()) {
 			lastLocation.put(p.toString(), new BoardLocation(-1, -1));
 		}
 		locToConfig.put(new BoardLocation(1, 1), new ThreeByThreeConfig(1,2,0,1));
 		locToConfig.put(new BoardLocation(1, 2), new ThreeByThreeConfig(1,2,0,2));
 		locToConfig.put(new BoardLocation(1, 3), new ThreeByThreeConfig(1,0,0,1));
 		locToConfig.put(new BoardLocation(2, 3), new ThreeByThreeConfig(0,1,0,2));
 		locToConfig.put(new BoardLocation(3, 3), new ThreeByThreeConfig(0,1,1,2));
 		locToConfig.put(new BoardLocation(3, 2), new ThreeByThreeConfig(0,2,1,2));
 		locToConfig.put(new BoardLocation(3, 1), new ThreeByThreeConfig(0,1,1,0));
 		locToConfig.put(new BoardLocation(2, 1), new ThreeByThreeConfig(0,1,0,0));
 		for(int r=0; r<=2; r++)
 			locToConfig.put(new BoardLocation(r, 0), new ThreeByThreeConfig(2,2,r,0));
 		for(int c=1; c<=2; c++) 
 			locToConfig.put(new BoardLocation(0, c), new ThreeByThreeConfig(2,2,0,c));
 		locToConfig.put(new BoardLocation(0, 3), new ThreeByThreeConfig(2,0,0,1));
 		for(int r=0; r<=2; r++)
 			locToConfig.put(new BoardLocation(r, 4), new ThreeByThreeConfig(2,0,r,2));
 		for(int r=3; r<=4; r++)
 			locToConfig.put(new BoardLocation(r, 4), new ThreeByThreeConfig(0,0,r-2,2));
 		locToConfig.put(new BoardLocation(4, 3), new ThreeByThreeConfig(0,1,2,2));
 		locToConfig.put(new BoardLocation(4, 2), new ThreeByThreeConfig(0,0,2,0));
 		locToConfig.put(new BoardLocation(4, 1), new ThreeByThreeConfig(0,1,2,0));
 		for(int r=3; r<=4; r++)
 			locToConfig.put(new BoardLocation(r, 0), new ThreeByThreeConfig(0,2,r-2,0));
 	}
 	
 	public List<ThreeByThreeConfig> getLocationList() {
 		List<ThreeByThreeConfig> locs = new LinkedList<ThreeByThreeConfig>();
 		for(ThreeByThreeConfig conf : locToConfig.values()) {
 			locs.add(conf);
 		}
 		return locs;
 	}
 	
 	public List<BoardLocation> getRandom5x5LocationOrder() {
 		ArrayList<BoardLocation> locs = new ArrayList<BoardLocation>();
 		for(BoardLocation bl : locToConfig.keySet()) {
 			locs.add(bl);
 		}
 		Collections.shuffle(locs);
 		return locs;
 	}
 	
 	public ThreeByThreeConfig getRandomLocation(String prep) {
 		boolean valid = false;
 		BoardLocation loc = BoardLocation.getRandomLocation();
 		BoardLocation lastLoc = lastLocation.get(prep);
 		while(!valid) {
 			loc = BoardLocation.getRandomLocation();
 			valid = checkLocation(prep, loc);
 			// no column constraint on behind since all squares are in column 2
 			if( prep.equals(Preposition.BEHIND.toString()) ) {
 				if( lastLoc.getRow() == loc.getRow() ) {
 					valid = false;
 				}
 				continue;
 			}
 			if(lastLoc.getColumn() == loc.getColumn() || lastLoc.getRow() == loc.getRow()) {
 				valid = false;
 			}
 		}
 		lastLocation.put(prep, loc);
 		return locToConfig.get(loc);
 	}
 	
 	public boolean checkLocation(String prep, BoardLocation loc) {
 		if(loc.getRow() == 2 && loc.getColumn() == 2) {
 			return false;
 		}
 		if(prep.equals(Preposition.BEHIND.toString())) {
 			if(loc.getRow() > 2 && loc.getColumn() == 2) {
 				return true;
 			}
 		} else if ( prep.equals(Preposition.FAR_FROM.toString())) {
 			int r = loc.getRow();
 			int c = loc.getColumn();
 			if(c == 0 || c == 4 || r == 0 || r == 4) {
				if( (r == 2 && c == 0) || (r == 0 && c == 2) || 
 						(r == 2 && c == 4) || (r == 4 && c == 2) ) {
 					return false;
 				}
 				return true;
 			} 
 		} else if (prep.equals(Preposition.IN_FRONT_OF.toString())) {
 			if(loc.getRow() < 2) {
 				return true;
 			} 
 		} else if (prep.equals(Preposition.LEFT_OF.toString())) {
 			if(loc.getColumn() < 2) {
 				return true;
 			} 
 		} else if (prep.equals(Preposition.NEAR.toString())) {
 			int r = loc.getRow();
 			int c = loc.getColumn();
 			if(!(c == 0 || c == 4 || r == 0 || r == 4)) {
 				return true;
 			} 
 		} else if (prep.equals(Preposition.RIGHT_OF.toString())) {
 			if(loc.getColumn() > 2) {
 				return true;
 			} 
 		}
 		return false;
 	}
 }
