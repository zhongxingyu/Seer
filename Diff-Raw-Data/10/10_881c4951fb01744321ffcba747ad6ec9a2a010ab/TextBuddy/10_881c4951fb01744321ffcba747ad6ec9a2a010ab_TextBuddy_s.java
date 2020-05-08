 /* TextBuddy by Arnold Christopher Koroa
  * A0092101Y
  * 
  * This is a simple text editor as specified in NUS CS2103T CE1 problem statement
  * 
  * It is assumed that the input will be from the keyboard or a redirected text file. 
  * If input comes from a redirected text file, the displayed messages might not look 
  * exactly like the given example as the commands entered will not be shown.
  * 
  * It is also assumed that the argument passed into the program will be a valid plain
  * text format file. 
  * 
  * The text file used is assumed to be small as some of the implementations might
  * not be very efficient for large files.
  */
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 
 public class TextBuddy {
     // a comparator that compares 2 strings while ignoring case
     static class IgnoreCaseStringComparator implements Comparator<String> {
 	public int compare(String s1, String s2) {
 	    return s1.compareToIgnoreCase(s2);
 	}
     }
 
     public static void main(String args[]) {
 	String fileName = args[0], command, feedback;
 
 	Scanner sc = new Scanner(System.in);
 
 	File currentFile = openFile(fileName);
 	showToUser(generateWelcomeMessage(fileName));
 
 	while (true) {
 	    System.out.print("command: ");
 	    command = sc.nextLine();
 	    feedback = processCommand(command, currentFile);
 	    showToUser(feedback);
 	}
     }
 
     private static String generateWelcomeMessage(String fileName) {
 	return ("Welcome to TextBuddy. " + fileName + " is ready to use.");
     }
 
     private static File openFile(String fileName) {
 	File file;
 	file = new File(fileName);
 
 	try {
 	    if (!file.exists())
 		file.createNewFile();
 	} catch (IOException e) {
 	    System.out.println("Error opening/creating file");
 	    e.printStackTrace();
 	    System.exit(1);
 	}
 	return file;
     }
 
     public static String processCommand(String commandLine, File currentFile) {
 	String commandWord = getFirstWord(commandLine);
 
 	if (commandWord.equals("display"))
 	    return display(currentFile);
 	else if (commandWord.equals("clear"))
 	    return clear(currentFile);
 	else if (commandWord.equals("add"))
 	    return add(commandLine, currentFile);
 	else if (commandWord.equals("delete"))
 	    return delete(commandLine, currentFile);
 	else if (commandWord.equals("sort"))
 	    return sort(currentFile);
 	else if (commandWord.equals("search"))
 	    return search(commandLine, currentFile);
 	else if (commandWord.equals("exit"))
 	    System.exit(0);
 
 	return (commandWord + " is not a valid command");
     }
 
     // shows text to user
     private static void showToUser(String text) {
 	System.out.println(text);
     }
 
     // Displays the contents of the file or a message if file is empty
     private static String display(File file) {
 	Scanner inputFile;
 	int i = 0;
 	StringBuilder feedbackBuilder = new StringBuilder();
 
 	if (isEmpty(file)) {
 	    return (file.getName() + " is empty");
 	} else {
 	    try {
 		inputFile = new Scanner(file);
 
 		while (inputFile.hasNext()) {
 		    i++;
 		    feedbackBuilder.append(i + ". " + inputFile.nextLine()
 			    + "\n");
 		}
 
 		inputFile.close();
 	    } catch (FileNotFoundException e) {
 		return "File for display not found.";
 	    }
 	}
 	return feedbackBuilder.toString();
     }
 
     // Performs the clear command's operation
     private static String clear(File file) {
 	clearFile(file);
 	return ("All contents deleted from " + file.getName());
     }
 
     // The actual clearing of file
     private static void clearFile(File file) {
 	BufferedWriter outputFile;
 
 	try {
 	    outputFile = new BufferedWriter(new FileWriter(file.getName(),
 		    false));
 	    outputFile.write("");
 	    outputFile.close();
 	} catch (IOException e) {
 	    System.out.println("IOException during clear");
 	    e.printStackTrace();
 	}
     }
 
     // Performs the add command's operation
     private static String add(String commandLine, File file) {
 	String inputText = removeFirstWord(commandLine);
 
 	writeToFile(inputText, file);
 
 	return ("Added to " + file.getName() + ": \"" + inputText + "\"");
     }
 
     // The actual Writing of text to file
     private static void writeToFile(String inputText, File file) {
 	BufferedWriter outputFile;
 
 	try {
 	    outputFile = new BufferedWriter(
 		    new FileWriter(file.getName(), true));
 
 	    if (!isEmpty(file))
 		outputFile.newLine();
 	    outputFile.write(inputText);
 
 	    outputFile.close();
 	} catch (IOException e) {
 	    System.out.println("Error writing to file");
 	    e.printStackTrace();
 	}
     }
 
     // Performs the delete command's operation
     private static String delete(String commandLine, File file) {
 	int deleteParameter;
 	String deletedText;
 
 	String deleteString = removeFirstWord(commandLine);
 
 	if (validDeleteParameter(deleteString, file)) {
 	    deleteParameter = Integer.parseInt(deleteString);
 	    deletedText = removeLine(deleteParameter, file);
 	    return ("Deleted from " + file.getName() + ": \"" + deletedText + "\"");
 	} else {
 	    return ("\"" + deleteString + "\" is not a valid parameter");
 	}
     }
 
     // The actual deletion of indicated line from file
     // Returns the deleted line
     private static String removeLine(int deleteParameter, File file) {
 	String tempString, deletedString = "";
 	Queue<String> lines = new LinkedList<String>();
 
 	deletedString = insertUndeletedLinesToQueue(deleteParameter, file,
 		lines);
 	clearFile(file);
 
 	while (!lines.isEmpty()) {
 	    tempString = lines.poll();
 	    writeToFile(tempString, file);
 	}
 
 	return deletedString;
     }
 
     // Copies file content to lines Queue, except for the deleted line
     // Returns the deleted line
     private static String insertUndeletedLinesToQueue(int deleteParameter,
 	    File file, Queue<String> lines) {
 	int lineCount = 0;
 	String tempString, deletedString = "";
 	Scanner inputFile;
 
 	try {
 	    inputFile = new Scanner(file);
 
 	    while (inputFile.hasNext()) {
 		lineCount++;
 		tempString = inputFile.nextLine();
 
 		if (lineCount != deleteParameter)
 		    lines.offer(tempString);
 		else
 		    deletedString = tempString;
 	    }
 	    inputFile.close();
 	} catch (FileNotFoundException e) {
 	    System.out.println("File not found during delete");
 	    e.printStackTrace();
 	}
 
 	return deletedString;
     }
 
     // Check if delete's parameter is valid (a number and not out of bound)
     private static boolean validDeleteParameter(String deleteString, File file) {
 	boolean lineIsInFile;
 
 	if (areDigits(deleteString) && (numberOfWords(deleteString) == 1)) {
 	    lineIsInFile = Integer.valueOf(deleteString) <= numberOfLines(file);
 	    return lineIsInFile;
 	}
 	return false;
     }
 
     // perform the sort command's operation
     private static String sort(File file) {
 	PriorityQueue<String> lineSorter;
 	lineSorter = insertLinesToLineSorter(file);
 
 	clearFile(file);
 	while (!lineSorter.isEmpty()) {
 	    writeToFile(lineSorter.poll(), file);
 	}
 
 	return ("Sorted alphabetically: " + file.getName());
     }
 
     private static PriorityQueue<String> insertLinesToLineSorter(File file) {
 	PriorityQueue<String> lineSorter = new PriorityQueue<String>(
 		numberOfLines(file), new IgnoreCaseStringComparator());
 
 	Scanner inputFile;
 
 	try {
 	    inputFile = new Scanner(file);
 
 	    while (inputFile.hasNext())
 		lineSorter.add(inputFile.nextLine());
 
 	    inputFile.close();
 	} catch (FileNotFoundException e) {
 	    System.out.println("File not found during delete");
 	    e.printStackTrace();
 	}
 
 	return lineSorter;
     }
 
     // performs the search command's operation
     private static String search(String commandLine, File file) {
 	String searchString = removeFirstWord(commandLine), currString;
 	StringBuilder feedbackBuilder = new StringBuilder();
 	int lineCount = 0;
 
 	if (!validSearchParameter(searchString))
 	    return ("\"" + searchString + "\" is not a valid search parameter");
 	else {
 	    Scanner inputFile;
 
 	    try {
 		inputFile = new Scanner(file);
 
 		while (inputFile.hasNext()) {
 		    lineCount++;
 		    currString = inputFile.nextLine();
		    if (currString.contains(searchString)) {
 			feedbackBuilder.append(lineCount + ". " + currString
 				+ "\n");
 		    }
 		}
 	
 		inputFile.close();
 	    } catch (FileNotFoundException e) {
 		System.out.println("Cannot read file during search");
 		e.printStackTrace();
 	    }
 	}
 
 	return feedbackBuilder.toString();
     }
 
     private static boolean validSearchParameter(String searchString) {
 	return numberOfWords(searchString) == 1;
     }
 
     private static int numberOfLines(File file) {
 	int count = 0;
 	Scanner inputFile;
 
 	try {
 	    inputFile = new Scanner(file);
 
 	    while (inputFile.hasNext()) {
 		count++;
 		inputFile.nextLine();
 	    }
 	    inputFile.close();
 	} catch (FileNotFoundException e) {
 	    System.out.println("File not found while counting lines");
 	    e.printStackTrace();
 	}
 
 	return count;
     }
 
     private static int numberOfWords(String s) {
 	StringTokenizer st = new StringTokenizer(s);
 	return st.countTokens();
     }
 
     private static boolean areDigits(String deleteString) {
 	boolean areDigits;
 
 	areDigits = true;
 	for (char c : deleteString.toCharArray()) {
 	    if (!Character.isDigit(c)) {
 		areDigits = false;
 		break;
 	    }
 	}
 	return areDigits;
     }
 
     private static boolean isEmpty(File file) {
 	return file.length() <= 0;
     }
 
     // Remove commandWord from commandLine. Adapted from
     // CityConnectForRefactoring,java
     private static String removeFirstWord(String commandLine) {
 	return commandLine.replace(getFirstWord(commandLine), "").trim();
     }
 
     // Extracts commandWord from the commandLine
     private static String getFirstWord(String commandLine) {
 	StringTokenizer tokenizedCommand = new StringTokenizer(commandLine);
 	String commandWord = tokenizedCommand.nextToken();
 
 	return commandWord;
     }
 }
