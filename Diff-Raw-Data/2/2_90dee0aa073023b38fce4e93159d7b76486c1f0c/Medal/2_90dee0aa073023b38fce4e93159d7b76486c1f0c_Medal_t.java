 package com.csoft.clinelympics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Medal implements Comparable<Medal>{
 	private HashMap<String, MedalScore> scores;
 	private ArrayList<String> medalNames;
 	private String rawMedals;
 	private String displayName;
 	private boolean scoreType; // True = high score wins
 	
 	public Medal() {
 		scores = new HashMap<String, MedalScore>();
 		medalNames = new ArrayList<String>();
 		setScoreType(true);
 		setRawMedals("Gold,Silver,Bronze");
 		setMedalNames();	
 		zeroScores();
 		setDisplayName("");
 	}
 	
 	private void setMedalNames() {
 		String[] names = rawMedals.split(",");
 		for (String n : names) {
 			medalNames.add(n);
 		}
 	}
 
 	public Medal(String medals, boolean t) {
 		scores = new HashMap<String,MedalScore>();
 		medalNames = new ArrayList<String>();
 		setRawMedals(medals);
 		setScoreType(t);
 		setMedalNames();
 		zeroScores();
 		setDisplayName("");
 	}
 	
 	public Medal(String medals, boolean t, String n) {
 		scores = new HashMap<String,MedalScore>();
 		medalNames = new ArrayList<String>();
 		setRawMedals(medals);
 		setScoreType(t);
 		setMedalNames();
 		zeroScores();
 		setDisplayName(n);
 	}
 	
 	public void addScore(MedalScore s) {
 		if ((s.score == Integer.MAX_VALUE) || (s.score == Integer.MIN_VALUE)) return;
 		for (String n : medalNames) {
 			if (s.score == scores.get(n).score) {
 				if (!scores.get(n).displayName.contains(s.displayName)) {
 					s.displayName = s.displayName + ", " + scores.get(n).displayName;
 					scores.put(n, s);
 					return;
 				}
 				
 			} else {
 				if (scoreType) {
 					if (s.score > scores.get(n).score) {
 						MedalScore o = scores.get(n);
 						scores.put(n, s);
 						for (int i=(medalNames.indexOf(n)+1);i<medalNames.size();i++) {
 							s = scores.get(medalNames.get(i));
 							scores.put(medalNames.get(i), o);
 							o = s;
 						}
 						return;
 					}
 				} else {
 					if (s.score < scores.get(n).score) {
 						MedalScore o = scores.get(n);
 						scores.put(n, s);
 						for (int i=(medalNames.indexOf(n)+1);i<medalNames.size();i++) {
 							s = scores.get(medalNames.get(i));
 							scores.put(medalNames.get(i), o);
 							o = s;
 						}
 						return;
 					}
 				}
 			}
 		}
 	}
 	
 	public void addScore(String type, MedalScore s) {
 		if ((s.score == Integer.MAX_VALUE) || (s.score == Integer.MIN_VALUE)) return;
 		if (isValidType(type)) {
 			if (s.score > scores.get(type).score) {
 				scores.put(type, s);
 			} else if (s.score == scores.get(type).score) {
 				if (!scores.get(type).displayName.contains(s.displayName)) {
 					s.displayName = s.displayName + ", " + scores.get(type).displayName;
 					scores.put(type, s);
 				}
 			}
 		}
 	}
 	
 	public void addScore(String n, int s) {
 		MedalScore m = new MedalScore(n, s);
 		addScore(m);
 	}
 	
 	public void addScore(String type, String name, int score) {
 		MedalScore m = new MedalScore(name, score);
 		addScore(type, m);
 	}
 	
 	public boolean containsScore(String type) {
 		if (isValidType(type)) {
 			if (scores.containsKey(type)) {
 				return true;
 			} else {
 				if ((scores.get(type).score == Integer.MAX_VALUE) || (scores.get(type).score == Integer.MIN_VALUE)) {
 					return false;
 				} else {
 					return true;
 				}
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	public MedalScore getScore(String type) {
 		if (isValidType(type)) {
 			return scores.get(type);
 		} else {
 			return null;
 		}
 	}
 	
 	public boolean isValidType(String type) {
 		return medalNames.contains(type);
 	}
 
 	public void setScoreType(boolean scoreType) {
 		this.scoreType = scoreType;
 	}
 
 	public boolean getScoreType() {
 		return scoreType;
 	}
 	
 	private void zeroScores() {
 		for (String n : medalNames) {
 			if (scoreType) {
 				scores.put(n, new MedalScore("", Integer.MIN_VALUE));
 			} else {
 				scores.put(n, new MedalScore("", Integer.MAX_VALUE));
 			}
 		}
 	}
 
 	public void setMedalNames(ArrayList<String> medalNames) {
 		this.medalNames = medalNames;
 	}
 
 	public ArrayList<String> getMedalNames() {
 		return medalNames;
 	}
 	
 	public String getRawMedals() {
 		return rawMedals;
 	}
 
 	public void setRawMedals(String rawMedals) {
 		this.rawMedals = rawMedals;
 	}
 	
 	public int getTotalMedals() {
 		return medalNames.size();
 	}
 
 	@Override
 	public int compareTo(Medal o) {
 		return compareRecur(o, 0);
 	}
 	
 	private int compareRecur(Medal o, int val) {
 		if ((val >= medalNames.size()) || (val >= o.medalNames.size()))
 			return o.displayName.compareToIgnoreCase(displayName);
 		else {
 			if (scores.get(medalNames.get(val)).score == o.getScore(medalNames.get(val)).score) {
 				return compareRecur(o, val + 1);
 			} else {
				return Integer.valueOf(o.getScore(medalNames.get(val)).score).compareTo(Integer.valueOf(scores.get(medalNames.get(val)).score));
 			}
 		}
 	}
 
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 
 	public String getDisplayName() {
 		return displayName;
 	}
 }
