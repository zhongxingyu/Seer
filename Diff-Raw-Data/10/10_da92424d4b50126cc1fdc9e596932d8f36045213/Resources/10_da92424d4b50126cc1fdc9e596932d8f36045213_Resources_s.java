 package db;
 
 import java.text.SimpleDateFormat;
 
 public abstract class Resources {
 	public static final String dbUser = "postgres";
 	public static final String dbPassword = "scr4mbled";
 	public static final String dbUrl = "jdbc:postgresql://142.104.21.212/";
 	public static final SimpleDateFormat DBDateFormat = new SimpleDateFormat("yyyy-mm-dd kk:mm:ss Z");
 	
 	public enum ChangeType {
 		MODIFYINSERT, MODIFYDELETE, DELETE, ADD, MODIFY, MOVE, RENAME
 	}
 	
 	public static int convertLineEndToCharEnd(int lineEnd, String file) {
 		int index = -1;
 		int lineCount = 1;
 		
 		for(char c: file.toCharArray()) {
 			if(c == '\n' && lineCount == lineEnd) {
 				index++;
 				break;
 			}
 			if(c == '\n')
 				lineCount++;
 			index++;
 		}
 		
 		return index;
 	}
 	
 	public static int convertCharToLine(int charNum, String file) {
 		int index = -1;
 		int lineCount = 1;
 		
 		for(char c: file.toCharArray()) {
 			index++;
 			if(index == charNum) 
 				break;
 			if(c == '\n')
 				lineCount++;
 		}
 		
 		return lineCount;
 	}
 	
 	public static int convertLineStartToCharStart(int lineStart, String file) {
 		int index = -1;
 		int lineCount = 1;
		
 		for(char c: file.toCharArray()) {
			if(c == '\n')
				lineCount++;
			else if(lineCount == lineStart) {
 				index++;
 				break;
 			}
 			index++;
 		}
 		
 		return index;
 	}
 }
