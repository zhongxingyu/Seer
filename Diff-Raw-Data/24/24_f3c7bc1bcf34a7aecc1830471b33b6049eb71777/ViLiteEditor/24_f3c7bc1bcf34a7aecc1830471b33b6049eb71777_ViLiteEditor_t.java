 package vilite;
 
 import java.io.IOException;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 public class ViLiteEditor implements IEditor {
 
 	Scanner input;
 
 	String command;
 
 	int currentLine;
 
 	int numLines;
 
 	MyLinkedList list;
 
 	public ViLiteEditor() {
 		input = new Scanner(System.in);
 		command = null;
 		currentLine = 0;
 		numLines = 0;
 		list = new MyLinkedList();
 	}
 
 	@Override
 	public void run() {
 		System.out.println("\nCommand:");
 		command = input.nextLine();
 		processCommand(command);
 	}
 
 	@Override
 	public void processCommand(String command) {
 
 		Scanner scanner = new Scanner(command);
 
 		String cmd = null;
 		String param1 = null;
 		String param2 = null;
 
 		try {
 			cmd = scanner.next();
 		} catch(NoSuchElementException e) {
 			cmd = param1 = param2 = null;
 			return;
 		}
 
 		//Check for b, i, and e, which are a command followed by a sentence
 
 		if(cmd.equals("b")) {
 
 			String sentence = null;
 
 			try {
 				sentence = scanner.nextLine();
 			} catch(NoSuchElementException e) {
 				System.out.println("Invalid parameter, try: b <sentence>");
 				return;
 			}
 
 			if(list.isEmpty()) {
 				insertEnd(sentence);
 				currentLine++;
 			}
 			else {
 				insertBefore(sentence);
 				//No need to decrement currentLine
 			}
 
 			numLines++;
 
 			return;
 		}
 
 		if(cmd.equals("i")) {
 
 			String sentence = null;
 
 			try {
 				sentence = scanner.nextLine();
 			} catch(NoSuchElementException e) {
 				System.out.println("Invalid parameter, try: i <sentence>");
 				return;
 			}
 
 			if(list.isEmpty()) {
 				insertEnd(sentence);
 				currentLine++;
 			}
 			else {
 				insertAfter(sentence);
 				currentLine++;
 			}
 
 			numLines++;
 
 			return;
 		}
 
 		if(cmd.equals("e")) {
 
 			String sentence = null;
 
 			try {
 				sentence = scanner.nextLine();
 			} catch(NoSuchElementException e) {
 				System.out.println("Invalid parameter, try: e <sentence>");
 				return;
 			}
 
 			insertEnd(sentence);
 
 			numLines++;
 			currentLine = numLines;
 
 			return;
 		}
 
 		try {
 			param1 = scanner.next();
 		} catch(NoSuchElementException e) {
 			param1 = param2 = null;
 		}
 
 		try {
 			param2 = scanner.next();
 		} catch(NoSuchElementException e) {
 			param2 = null;
 		}
 
 		if(cmd.equals("m")) {
 			if(param1 != null) {
 				try {
 					int num = Integer.parseInt(param1);
 
 					moveDown(num);
 					return;
 
 				} catch(NumberFormatException e) {
 					System.out.println("Invalid parameters, try: m #");
 					return;
 				}
 			}
 
 			else {
 				moveDown(1);
 				return;
 			}
 		}
 
 		if(cmd.equals("u")) {
 			if(param1 != null) {
 				try {
 					int num = Integer.parseInt(param1);
 
 					moveUp(num);
 					return;
 
 				} catch(NumberFormatException e) {
 					System.out.println("Invalid parameters, try: u #");
 					return;
 				}
 			}
 
 			else {
 				moveUp(1);
 				return;
 			}
 		}
 
 		if(cmd.equals("r")) {
 
 			if(!list.isEmpty()) {
 				if(param1 != null) {
 					try {
 						int num = Integer.parseInt(param1);
 						
 						remove(num);
						return;
 						
 					} catch(NumberFormatException e) {
 						System.out.println("Invalid parameters, try: r #");
 						return;
 					}
 				}
 				
 				else {
 					remove(1);
					return;
 				}
 			}
			
 			else
 				return;
 
 		}
 
 		if(cmd.equals("d")) {
 
 			if(param1 == null && param2 == null) {
 				display(1, numLines);
 				return;
 			}
 
 			else {
 
 				if(param1.equals("c") && param2 == null) {
 					if(list.isEmpty())
 						System.out.println("No data to display!");
 					else
 						System.out.println("==> \t" + list.get(currentLine));
 
 					return;
 				}
 
 				try {
 					int start = Integer.parseInt(param1);
 					int end = Integer.parseInt(param2);
 
 					display(start, end);
 
 					return;
 				} catch(NumberFormatException e) {
 					System.out.println("Invalid parameters, try: d # *");
 
 					return;
 				}
 
 			}
 		}
 
 		if(cmd.equals("c")) {
 			clear();
 			numLines = 0;
 			currentLine = 0;
 			return;
 		}
 
 		if(cmd.equals("s")) {
 
 		}
 
 		if(cmd.equals("l")) {
 
 		}
 
 		if(cmd.equals("h")) {
 			displayHelp();
 			return;
 		}
 
 		if(cmd.equals("x")) {
 			//Check if saved or not before exiting
 			System.out.println("Goodbye!");
 			System.exit(0);
 		}
 
 		if(cmd.equals("mc")) {
 
 		}
 
 		if(cmd.equals("fr")) {
 
 		}
 
 		if(cmd.equals("sw")) {
 
 		}
 
 		if(cmd.equals("ud")) {
 
 		}
 		
 		if(cmd != null)
 			System.out.println("Command " + cmd + " not recognized.  Type 'h' for help");
 	}
 
 	@Override
 	public int getNbrLines() {
 		return numLines;
 	}
 
 	@Override
 	public int getCurrentLineNbr() {
 		return currentLine;
 	}
 
 	@Override
 	public String getLine(int lineNbr) {
 		return list.get(lineNbr);
 	}
 
 	@Override
 	public String[] getLines(int beginPos, int endPos) {
 		String[] lines = {null};
 
 		for(int i = beginPos; i <= endPos; i++)
 			lines[i] = list.get(i);
 
 		return lines;
 	}
 
 	@Override
 	public void insertBefore(String line) {
 		list.add(currentLine, line);
 	}
 
 	@Override
 	public void insertAfter(String line) {
 		list.add((currentLine + 1), line);
 	}
 
 	@Override
 	public void insertEnd(String line) {
 		list.add(line);
 	}
 
 	@Override
 	public void moveUp(int nbrOfPositions) {
 		for(int i = 0; i < nbrOfPositions; i++)
 			if(currentLine != 1)
 				currentLine--;
 	}
 
 	@Override
 	public void moveDown(int nbrOfPositions) {
 		for(int i = 0; i < nbrOfPositions; i++)
 			if(currentLine != numLines)
 				currentLine++;
 	}
 
 	@Override
 	public void remove(int nbrLinesToRemove) {
		for(int i = 0; i < nbrLinesToRemove; i++) {		
			//System.out.println("" + i + " " + currentLine);
 			
 			if(currentLine == numLines) {
 				list.remove(currentLine);
 				currentLine--;
 				numLines--;
 				return;
 			}
 		
 			else {
 				list.remove(currentLine);
				//currentLine++;
 				numLines --;
 			}
 		}
 	}
 
 	@Override
 	public void clear() {
 		list.clear();
 	}
 
 	@Override
 	public void save(String filename) throws IOException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void load(String filename) throws IOException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int findReplace(String str1, String str2) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void moveCurrentLineTo(int lineNbr) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void switchCurrentLineWith(int lineNbr) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void undo() {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void display(int start, int end) {
 		for(int i = start; i <= end; i++) {
 
 			if(i > numLines)
 				return;
 
 			if(list.get(i) != null) {
 				if(i == currentLine)
 					System.out.println("==> \t" + list.get(i));
 				else
 					System.out.println("\t" + list.get(i));
 			}
 		}
 	}
 
 	public void displayHelp() {
 		System.out.println("The following are possible commands:\n" +
 				"b <sentence> = insert line before the current line\n" + 
 				"i <sentence> = insert after the current line\n" +
 				"e <sentence> = insert after the last line\n" +
 				"m # = move the current line down # positions\n" +
 				"u # = move the current line up # positions\n" +
 				"r = remove the current line\n" +
 				"r # = remove # lines starting at the current line\n" +
 				"s <filename> = save the contents to a text file\n" + 
 				"l <filename> = load contents of a text file\n" +
 				"d = display the buffer contents\n" +
 				"d c = display current line\n" +
 				"d # * = display line # to *(inclusive)\n" +
 				"c = clear the buffer contents\n" +
 				"h = show a help menu of editor commands\n" +
 				"fr string1 string2 = find/search and replace\n" +
 				"mc # = move current line to another location\n" +
 				"mc $ = move current line to the last line\n" +
 				"sw # = switch the current line with line #\n" +
 				"sw $ = switch current line with last line\n" +
 				"ud = undo operation\n" +
 				"x = exit the editor (if saved)");
 	}
 
 	public static void main (String[] args) {
 		ViLiteEditor vilite = new ViLiteEditor();
 
 		while(true) {
 			vilite.run();
 		}
 	}
 }
