 package tk.jlowe.smla.compiler;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 public class SMLACompiler {
 	private HashMap<Symbol, Integer> symbolTable;
 	private HashMap<String, Integer> commandTable;
 	private final String[] usableCommands = {"rem", "input", "print", "let", "goto", "if", "end"};
 	private int commandPointer = 0;
 	private int memoryPointer = 99;
 	private String machineCodeContents;

 	public SMLACompiler() {
 		symbolTable = new HashMap<Symbol, Integer>();
 		machineCodeContents = "";
 		commandTable.put(usableCommands[1], 10);
 		commandTable.put(usableCommands[2], 11);
 		commandTable.put(usableCommands[6], 43);
 	}

 	public void compileFile(File file) throws IOException {
 		Scanner reader = new Scanner(file);
 		String[][] lines = new String[99][];
 		int linePlace = 0;
 		while(reader.hasNextLine()) {
 			String line = reader.nextLine();
 			String[] lineComponents = line.split(" ");
 			String lineNumber = "";
 			String command = "";
 			String parameter = "";
 			try {
 				int number = Integer.parseInt(lineComponents[0]);
 				lineNumber = lineComponents[0];
 			} catch(NumberFormatException e) {
 				//TODO what to do if a line number isn't the first thing
 			}
 			command = lineComponents[1];
 			if(command.equals(usableCommands[0]) 
					|| command.equals(usableCommands[1]) 
					|| command.equals(usableCommands[2]) 
					//|| command.equals(usableCommands[3]) 
					//|| command.equals(usableCommands[4]) 
					//|| command.equals(usableCommands[5]) 
					|| command.equals(usableCommands[6])
			  ) {
 				int ii = 2;
 				if(ii < lineComponents.length) {
 					parameter += lineComponents[ii] + " ";
 				}
 			} else { //Not a valid command
 				System.out.println("Error: Invalid command on line " + 0); //TODO Replace 0 with line number variable
 				System.exit(0);
 			} 
 			String[] code = {command, lineNumber, parameter};
 			lines[linePlace] = code;
 			linePlace++;
 		}

 		compileCode(lines);
 		String path = file.getParent();
 		PrintWriter writer = new PrintWriter(new FileWriter(path + file.getName() + ".jsc"));
 		writer.println(machineCodeContents);
 		writer.close();
 	}

 	private void compileCode(String[][] code) {
 		//Object[][] newCode = new Object[code.length][3];
 		ArrayList<Object[]> temp = new ArrayList<Object[]>();
 		for(String[] line : code) {
 			//ArrayList<Symbol> newLine = new ArrayList<Symbol>();
 			Object[] objectList = new Object[3];
 			if(line[0].equals(usableCommands[0])) {
 				break;
 			} else if(!line[0].equals(usableCommands[6])){
 				//int lineNumber = Integer.parseInt(line[1]);
 				if(!symbolTable.containsKey(line[1])) {
 					LineNumber lineNumber = new LineNumber(line[1]);
 					symbolTable.put(lineNumber, commandPointer);
 					commandPointer++;
 					//commandTable.put(lineNumber, line[0]);
 					//newLine.add(lineNumber);
 					objectList[0] = lineNumber;
 					objectList[1] = line[0];
 					if(line.length > 2 && !symbolTable.containsKey(line[2])) {
 						Variable variable = new Variable(line[2]);
 						symbolTable.put(variable, memoryPointer);
 						//newLine.add(variable);
 						objectList[2] = variable;
 					}
 				}
 			}
 			temp.add(objectList);
 		}
 		for(Object[] objectList : temp) {
 			machineCodeContents += commandTable.get(objectList[1]);
 			if(!((String) objectList[1]).equals(usableCommands[6])) {
 				machineCodeContents += symbolTable.get(objectList[2]) + "\n";
 			} else {
 				machineCodeContents += "00";
 			}
 		}
 	}
 }
