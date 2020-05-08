 package team.kyb.extragames;
 
 import java.util.Random;
 
 import android.database.Cursor;
 
 public class ScriptureForGameHelper {
 
 	Cursor cursor;
 	String randomMissingWord = " ";
 	int id, chapter, verse;
 	String passageFull, passageMissing, book;
 
 	public ScriptureForGameHelper(Cursor cursor) {
 		this.cursor = cursor;
 		this.id = cursor.getInt(0);
 		this.passageFull = cursor.getString(1);
 		this.book = cursor.getString(2);
 		this.chapter = cursor.getInt(3);
 		this.verse = cursor.getInt(4);
 
 		int posOfWord = -1;
 		Random rd = new Random();
 		while (randomMissingWord.length() < 4) {
 			int rdint = rd.nextInt(passageFull.length());
 			posOfWord = passageFull.indexOf(" ", rdint);
 			posOfWord++;
 
 			// Normal case
 			int endPos = passageFull.indexOf(" ", posOfWord);
 
 			// Case words at the end
 			if (endPos < 0) {
 				endPos = passageFull.indexOf(".", posOfWord);
 			}
			
			if (endPos < 0) {
				endPos = passageFull.indexOf("!", posOfWord);
			}
			
			if (endPos < 0) {
				endPos = passageFull.length();
			}
 
 			// Case word follow by a comma
 			if (passageFull.charAt(endPos - 1) == ',') {
 				endPos--;
 			}
 
 			randomMissingWord = passageFull.substring(posOfWord, endPos);
 		}
 		String dashes = "";
 		for (int i = 0; i < randomMissingWord.length(); i++) {
 			dashes += "-";
 		}
 
 		passageMissing = passageFull.replaceFirst(randomMissingWord, dashes);
 	}
 
 	public int getID() {
 		return id;
 	}
 
 	public String getPassage() {
 		return passageFull;
 	}
 
 	public String getPassageMissing() {
 		return passageMissing;
 	}
 
 	public String getBook() {
 		return book;
 	}
 
 	public int getChapter() {
 		return chapter;
 	}
 
 	public int getVerse() {
 		return verse;
 	}
 
 	public String getRandomMissingWord() {
 
 		return randomMissingWord;
 	}
 
 	public String getScriptureFull() {
 		String full = getPassage() + " " + getBook() + " " + getChapter()
 				+ " : " + getVerse();
 		return full;
 	}
 
 	public String getScriptureMissing() {
 		String retVal = passageMissing + " " + getBook() + " " + getChapter()
 				+ " : " + getVerse();
 		return retVal;
 	}
 
 }
