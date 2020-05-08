 package greenrubber.scanner;
 
 
 public class StringReader {
 	public static final int END = -1;
 	
 	private String input;
 	private int currentPosition;
 
 	public StringReader(String input) {
 		this.input = input;
 	}
 	
 	public int getCurrentPosition() {
 		return currentPosition;
 	}
 	
 	public void setCurrentPosition(int currentPosition) {
 		this.currentPosition = currentPosition;
 	}
 	
 	public String getInput() {
 		return input;
 	}
 
 	public int read() {
 		if (currentPosition == input.length()) {
 			return END;
 		}
 
 		return input.charAt(currentPosition++);
 	}
 
 	public void unread(char c) {
 		currentPosition--;
 	}
 
 	public boolean reachedEnd() {
 		return (currentPosition == input.length());
 	}
 	
 	public void skip(String theseCharacters) {
 		while (currentPosition < input.length() &&
				theseCharacters.indexOf(input.charAt(currentPosition)) > 0) {
 			++currentPosition;
 		}
 	}
 }
