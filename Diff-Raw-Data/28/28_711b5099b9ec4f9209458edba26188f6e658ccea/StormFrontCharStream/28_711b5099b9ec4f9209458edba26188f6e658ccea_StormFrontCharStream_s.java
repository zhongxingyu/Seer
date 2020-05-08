 package cc.warlock.network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import org.antlr.runtime.CharStream;
 
 public class StormFrontCharStream implements CharStream {
 
 	private int lineOffset = 0;
 	private int position = 0;
 	private int index = 0;
 	private int curLine = 0;
 	private ArrayList<String> lines = new ArrayList<String>();
 	
 	private StormFrontConnection connection;
 	private InputStream inputStream;
 	
	private CharSequence buffer;
 	private byte[] readBuffer = new byte[4096];
 	
 	private Stack<Integer> marks = new Stack<Integer>();
 	
 	public StormFrontCharStream(InputStream inputStream) {
 		this.inputStream = inputStream;
 	}
 	
 	public int LT(int arg0) {
 		// TODO Auto-generated method stub
 		return LA(arg0);
 	}
 
 	public int getCharPositionInLine() {
 		return position;
 	}
 
 	public int getLine() {
 		return lineOffset + curLine;
 	}
 
	public void setCharPositionInLine(int arg0) {
 		// TODO make sure this is a valid position
		position = arg0;
 	}
 
 	public void setLine(int newLine) {
 		if (newLine < lineOffset) {
 			// TODO handle this
 			return;
 		}
 		
 		while(newLine > lineOffset + curLine) {
 			readLine();
 			curLine++;
 		}
 	}
 	
 	private void readLine() {
 		int bytesRead = 0;
 		
 		try {
 			int newlinePos;
 			while((newlinePos = buffer.toString().indexOf('\n')) == -1) {
 				bytesRead = inputStream.read(readBuffer);
 				if(bytesRead > 0) {
					buffer = buffer + readBuffer.toString();
 				} else if(bytesRead == 0) {
 					// TODO wait for more input
 				} else {
 					// TODO handle the error
 				}
 			}
			String newline = buffer.subSequence(0, newlinePos + 1).toString();
			buffer = buffer.subSequence(newlinePos + 1, buffer.length());
 			
 			// TODO remove excess cached lines here
 			lines.add(newline);
 		} catch(IOException e) {
 			// TODO handle it
 		}
 	}
 
 	public String substring(int arg0, int arg1) {
 		// TODO docs say we don't need this. find out if that's true
 		return null;
 	}
 
 	public int LA(int lookahead) {
 		if(position + lookahead >= 0) {
 			int relevantLine = 0;
 			int charsToRelevantLine = 0;
 			int charsInRelevantLine;
 			while(position + lookahead - charsToRelevantLine 
 					> (charsInRelevantLine = lines.get(curLine + relevantLine).length())) {
 				charsToRelevantLine += charsInRelevantLine;
 				relevantLine++;
 				if(lines.size() < curLine + relevantLine + 1) {
 					readLine();
 				}
 			}
 			return lines.get(curLine + relevantLine).charAt(position + lookahead - charsToRelevantLine);
 		}
 		
 		// TODO look in previous lines
 		return 0;
 	}
 
 	public void consume() {
 		position++;
 		index++;
 		
 		if(position >= lines.get(curLine).length()) {
 			position = 0;
 			readLine();
 			curLine++;
 		}
 	}
 
 	public int index() {
 		return index;
 	}
 
 	public int mark() {
 		marks.push(index);
 		return index;
 	}
 
 	public void release(int arg0) {
 		// TODO implement this so we don't leak memory
 	}
 
 	public void rewind() {
 		moveBack(marks.pop());
 	}
 
 	public void rewind(int newIndex) {
 		moveBack(newIndex);
 		release(index);
 	}
 
 	private void moveBack(int newIndex) {
 		while(index > newIndex) {
 			if(position == 0) {
 				curLine--;
 				position = lines.get(curLine).length() - 1;
 			} else {
 				position--;
 			}
 			index--;
 		}
 	}
 	
 	public void seek(int newIndex) {
 		if(newIndex <= index) {
 			moveBack(newIndex);
 		} else {
 			while(index < newIndex) {
 				consume();
 			}
 		}
 	}
 
 	public int size() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
