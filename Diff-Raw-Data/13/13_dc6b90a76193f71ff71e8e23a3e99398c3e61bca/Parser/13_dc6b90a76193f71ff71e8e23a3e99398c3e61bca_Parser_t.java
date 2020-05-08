 package no.truben.uhs.format;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Scanner;
 
 public class Parser {
 	
 	public static Hunk parse(File file) throws IOException {
 		Scanner s = new Scanner(file);
 		
 		oldStuff(s);
 		
 		return readHunk(s, null, 1);
 		
 	}
 	
 	private static Hunk readHunk(Scanner s, int[] key, int currentLine) {
 		
 		Hunk thisHunk = new Hunk();
 		
 		String[] firstLine = s.nextLine().split(" ");
 		int lines = Integer.parseInt(firstLine[0]);
 		HunkType type = HunkType.valueOf(firstLine[0].toUpperCase());
 		
 		thisHunk.setLines(lines);
 		thisHunk.setHunkType(type);
 		thisHunk.setMainLabel(s.nextLine());
 		thisHunk.setLineNumber(currentLine);
		System.out.println(thisHunk.getMainLabel());
 		if(key == null)
 			key = Encryption.getKey(thisHunk.getMainLabel());
 		
 		String buffer = "";
 		
 		for(int line = 3; line <= lines; line++) {
 			
 			switch(thisHunk.getHunkType()) {
 			
 			case HINT:
 				String hintLine = s.nextLine();
 				if(hintLine.equals("-") || line == lines) {
 					thisHunk.addHint(Encryption.decryptString(buffer, key));
 					buffer = "";
 				}
 				else
 					buffer += " " + hintLine;
 				
 				break;
 			case TEXT:
 				
 				break;
 			case LINK:
 				thisHunk.setLink(Integer.parseInt(s.nextLine()));
 				break;
 			case SUBJECT:
 				Hunk newHunk = readHunk(s, key, line);
 				line += newHunk.getLines();
 				thisHunk.addHunk(newHunk);
 				break;
 			
 			
 			}
 		}
 		
 		return thisHunk;
 		
 	}
 
 	private static void oldStuff(Scanner s) {
 		final String TERMINATE = "** END OF 88A FORMAT **";
		while(s.hasNextLine()) {
			if(s.nextLine().equals(TERMINATE))
 				break;
 		}
 	}
 	
 	
 
 }
