 package bisbat;
 
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class Being {
 	
 	public String name, shortDesc, longDesc;
 	public int hp, maxhp;
 	public Vector<Integer> damageHistory;
 	public Vector<Room> seenIn;
 	public int guessLocation = 0;
 	
 	/**
 	 * Four modes:
 	 * 0. single character guesses
 	 * 1. entire word guesses
 	 * 2. guesses from room title
 	 * 3. guesses from room description (unimplmeented yet)
 	 */
 	public int guessMode = 0;
 	
 	public Being(String string) {
 		longDesc = string;
 	}
 	public boolean equals(Being other) {
 		//Bisbat.debug("checking to see if mob is equal to another.");
 		return longDesc.equalsIgnoreCase(other.longDesc);
 	}
 	public boolean equals(Object other) {
 		if(other instanceof Being) {
 			return equals((Being)other);
 		} else {
 			return false;
 		}
 	}
 	public boolean isSureOfName() {
 		return (name!=null && name.length() > 2);
 	}
 	
 	public String guessName() {
 		String[] names = longDesc.split(" ");
 		if(guessMode == 0) {
 			return names[guessLocation].substring(0,1);
 		} else {
 			if(guessLocation >= names.length) {
 				Bisbat.debug("We couldn't figure out the name of the mobile.  There is a chance she left us.");
 			}
 			return names[guessLocation];
 		}
 	}
 	public boolean setGuessResult(String result) {
 		if(result.startsWith("You don't see that here.")) {
 			setGuessResult(false);
 			return false;
 		} else {
 			Pattern p = Pattern.compile("(.*) looks much tougher than you\\..*", Pattern.MULTILINE | Pattern.DOTALL);
 			Pattern p2 = Pattern.compile("You are much tougher than (.*)\\..*");
 			Matcher m = p.matcher(result);
 			if(m.matches()) {
 				//Bisbat.debug("Someone looks much tougher than us, lets grab '" + m.group(1) + "'.");
 				shortDesc = m.group(1);
 				name = stripOfPronouns(shortDesc);
 			} else if(p2.matcher(result).matches()) {
 				shortDesc = p2.matcher(result).group(1);
 				name = stripOfPronouns(shortDesc);
 				
 			} else {
 				Bisbat.debug("Neither thing matched");
 			}
 			setGuessResult(true);
 			return true;
 		}
 	}
 	public void setGuessResult(boolean success) {
 		if(success) {
 			if(guessMode == 0) {
 				guessMode = 1;
 			} else if(guessMode == 1) {
 				name = longDesc.split(" ")[guessLocation];
 				if(name.length() < 4) {
 					guessMode = 0;
 					guessLocation++;
 				}
 			}
 		} else {
 			if(guessMode == 0) {
 				guessLocation++;
 				if(guessLocation >= longDesc.length()) {
 					guessMode = 2;
 				}
 			} else if(guessMode == 1) {
 				guessLocation++;
 				if(guessLocation >= longDesc.length()) {
 					guessMode = 2;
 				}
 			}
 		}
 	}
 
 	public static String stripOfPronouns(String string) {
 		String result = string.replaceAll("the ", "");
 		result = result.replaceAll("a ", "");
 		return result;
 	}
 	
 	public String toString() {
 		return (shortDesc == null ? longDesc : shortDesc);
 	}
 }
