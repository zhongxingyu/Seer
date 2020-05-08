 import java.util.*;
 import java.io.*;
 
 /*This is the main class of the program*/
 
 public class FileSystem {
 	
 	/*Constructors*/
 	public FileSystem() {
 		mCurrentDirectory = new Directory("root", null);
 		mCurrentAction = "";
 		mInput = new Scanner(System.in);
 		mRunning = true;	
 		mPaths = new ArrayList<CommandPath>();
 		mActions = new String[19];
 		mBlocks = new ArrayList<byte[]>();
 		 
 		mActions[0] = "format"; 	//bygger upp ett tomt system (“formatterar skivan”)
 		mActions[1] = "quit"; 		//lämnar körningen
 		mActions[2] = "exit";		//lämnar körningen
 		mActions[3] = "save";		//sparar systemet som en vanlig UNIX-fil
 		mActions[4] = "read";		//återställer ett filsystem från en UNIX-fil
 		mActions[5] = "create";		//skapar en fil (lägger in text till extra tom rad)
 		mActions[6] = "cat";		//skriver ut innehållet i en fil
 		mActions[7] = "ls";			//listar innehållsförteckningen I en catalog
 		mActions[8] = "dir";		//listar innehållsförteckningen I en catalog
 		mActions[9] = "copy";		//kopierar en fil
 		mActions[10] = "append";	//lägger till på slutet av filen
 		mActions[11] = "rename";	//ändrar namn på fil
 		mActions[12] = "rn";		//ändrar namn på fil
 		mActions[13] = "mkdir"; 	//skapar ny tom catalog
 		mActions[14] = "cd"; 		//ändrar aktuell catalog
 		mActions[15] = "pwd";		//skriver namn på aktuell katalog (print working directory)
 		mActions[16] = "rm";		//tar bort en fil
 		mActions[17] = "remove";	//tar bort en fil
 		mActions[18] = "help";
 		
 		start();
 	}
 	
 	/*Members*/
 	private boolean mRunning;
 	private Directory mCurrentDirectory;
 	private Scanner mInput;
 	private ArrayList<CommandPath> mPaths;
 	private String mCurrentAction;
 	private String[] mActions;
 	private ArrayList<byte[]> mBlocks;
 	
 	/*Methods*/
 	private void start() {
 		while(mRunning) {
 			if(validCommand()) {
 				executeCommand();
 			}
 		}
 	}
 	private boolean validCommand() {
 			//In order to avoid inconsistencies - reset relevant command variables.
 			resetCommandVariables();
 			//Print complete path for convenience.
 			printWorkingDirectory();
 		
 			/*Get input*/
 			String currentInput;
 			currentInput = mInput.nextLine();
 			
 			/*Interpet action*/
 			String[] command;
 			command = currentInput.split(" ", 100);
 			
 			boolean validCommand = false;
 			if(command.length > 0) {
 				//Assumption: The action is always specified firsthand.
 				mCurrentAction = command[0];
 				
 				for(int i = 0; i < mActions.length; i++) {
 					if(mCurrentAction.equals(mActions[i])) {
 						validCommand = true;
 					}
 				}
 			}
 			else {
 				System.out.println("Blank command input detected. No action performed.");
 			}
 			
 			if(validCommand) {
 					if(command.length > 1) {	//e.g. one or more paths specified
 						for(int i = 1; i < command.length; i++) {
 							/*Interpret command paths*/
 							String localPath = command[i];
 							String[] localCommand = localPath.split("/");
 							mPaths.add(new CommandPath(localCommand));
 						}
 					}
 					return true;
 			}
 			else {
 				System.out.println("No valid input. Enter 'help' for a list of valid commands.");
 			}
 			
 			return false;
 	}
 	private void resetCommandVariables() {
 		mCurrentAction = "";
 		mPaths = new ArrayList<CommandPath>();
 	}
 	private void executeCommand() {
 		if(mCurrentAction.equals("format")) {
 			format();
 		}
 		else if(mCurrentAction.equals("quit") || mCurrentAction.equals("exit")) {
 			mRunning = false;
 		}
 		else if(mCurrentAction.equals("copy")) {
 			copyFile();
 		}
 		else if(mCurrentAction.equals("append")) {
 			append();
 		}
 		else if(mCurrentAction.equals("rn") || mCurrentAction.equals("rename")) {
 			renameComponent();
 		}
 		else if(mCurrentAction.equals("mkdir")) {
 			makeDirectory();
 		}
 		else if(mCurrentAction.equals("cd")) {
 			changeDirectory();
 		}
 		else if(mCurrentAction.equals("pwd")) {
 			printWorkingDirectory();
 		}
 		else if(mCurrentAction.equals("rm") || mCurrentAction.equals("remove")) {
 			removeComponent();
 		}
 		else if(mCurrentAction.equals("help")) {
 			printHelp();
 		}
 		else if(mCurrentAction.equals("save")) {
 			save();
 		}
 		else if(mCurrentAction.equals("read")) {
 		}
 		else if(mCurrentAction.equals("create")) {
 			create();
 		}
 		else if(mCurrentAction.equals("cat")) {
 			cat();
 		}
 		else if(mCurrentAction.equals("ls") || mCurrentAction.equals("dir")) {
 			list();
 		}
 	}
 
 	private void printHelp() {
 		System.out.println("\nValid commands:");
 		for(int i = 0; i < mActions.length; i++) {
 			System.out.println(" - " + mActions[i]);
 		}
 	}
 	private void changeDirectory() {
 		if(mPaths.size() > 0) {
 			CommandPath targetPath = mPaths.get(0);
 			int numPath = targetPath.getSize();
 			
 			Directory tempDirectory = mCurrentDirectory;
 			String step; boolean validTarget = true;
 			for(int i = 0; i < numPath; i++) {	
 				step = targetPath.getNext();
 				tempDirectory = (Directory)tempDirectory.getComponent(step);
 				if(tempDirectory == null) {
 					validTarget = false;
 					break;
 				}
 			}
 			if(validTarget) {
 				mCurrentDirectory = tempDirectory;
 			}
 		}
 		else {
 			System.out.println("No path specified. No action taken.");
 		}
 	}
 	private void list() {
 		mCurrentDirectory.print();
 	}
 	private void makeDirectory() {
 		if(mPaths.size() > 0) {
 			CommandPath relevantPath = mPaths.get(0);
 			int numPaths = relevantPath.getSize();
 			
 			Directory tempDirectory = mCurrentDirectory;
 			boolean validDir = true; String step;
 			for(int i = 0; i < numPaths - 1; i++) {
 				step = relevantPath.getNext();
 				tempDirectory = (Directory)tempDirectory.getComponent(step);
 				if(tempDirectory == null) {
 					System.out.println("'" + step + "' does not exist.");
 					validDir = false;
 					break;
 				}
 			}
 			if(validDir) {
 				step = relevantPath.getNext();
 				tempDirectory.addComponent(new Directory(step, tempDirectory));
 			}
 		}
 	}
 
 	private void append() {
 		
 		if(mPaths.size() > 0) {
 			CommandPath relevantPath = mPaths.get(0);
 			int numPaths = relevantPath.getSize();
 			
 			Directory tempDirectory = mCurrentDirectory;
 			boolean validDir = true; String step;
 			for(int i = 0; i < numPaths - 1; i++) {
 				step = relevantPath.getNext();
 				tempDirectory = (Directory)tempDirectory.getComponent(step);
 				if(tempDirectory == null) {
 					System.out.println("'" + step + "' does not exist.");
 					validDir = false;
 					break;
 				}
 			}
 			
 			if(validDir) {
 				step = relevantPath.getNext();
 				File file = null;
 				if(tempDirectory.getComponent(step) instanceof File) {
 					file = (File)tempDirectory.getComponent(step);
 					
 					if(file != null) {
 						
 						String input = mInput.nextLine();
 						byte[] bytes = input.getBytes();
 						
 						copyBytes(file, bytes); 
 						
 					}
 					else {
 						System.out.println("File does not exist");
 					}
 				}
 				else {
 					System.out.println("'" + step + "'" + " is not a file");
 				}
 			}
 		}
 		
 	}
 	
 	private void copyBytes(File file, byte[] bytes) {
 	
 		int currentByte = 0;
 		int currentBlock = file.getCurrentBlockIndex();
 		if(currentBlock == -1) {
 			mBlocks.add(new byte[File.BLOCK_SIZE]);
 			file.addBlockIndex(mBlocks.size()-1);
 			file.setBlockPosition(0);
 			
 			currentBlock = file.getCurrentBlockIndex();
 		}
 		
 		int blockPosition = file.getBlockPosition();
 		
 		while(currentByte < bytes.length) {
 		
 			
 			for(int i=blockPosition; i<File.BLOCK_SIZE; i++) {
 			
 				mBlocks.get(currentBlock)[i] = bytes[currentByte];
			
 				currentByte++;
 				if(currentByte >= bytes.length) {
 					break;
 				}
 				if(i >= File.BLOCK_SIZE) {
 					mBlocks.add(new byte[File.BLOCK_SIZE]);
 					file.addBlockIndex(mBlocks.size()-1);
 					file.setBlockPosition(0);
 				}
 			}
 		}
 		
 		
 	}
 	
 
 	private void printWorkingDirectory() {
 		Directory tempDirectory = mCurrentDirectory;
 		
 		Stack<String> wd = new Stack<String>();
 		wd.push(tempDirectory.name());
 		while(tempDirectory.getParent() != null) {
 			tempDirectory = (Directory)tempDirectory.getParent();
 			wd.push(tempDirectory.name());
 		}
 		while(!wd.empty()) {
 			System.out.print(wd.pop() + "/");
 		} System.out.print("\n");
 	}
 	private void format() {
 		System.out.println("Formatting disc.\n\nSHUT\nDOWN\nEVERYTHING\n");
 		
 		mCurrentDirectory = new Directory("root", null);
 		mCurrentAction = "";	
 		mPaths = new ArrayList<CommandPath>();
 		mBlocks = new ArrayList<byte[]>();
 	}
 	
 	private void save() {
 	
 		/*
 		 * Find root directory
 		 */
 		Directory root = (Directory)mCurrentDirectory.getComponent(".");
 		
 		/*
 		 * Save root which will save everything under root
 		 */
 		 try {
 			 
 			FileOutputStream fileStream = new FileOutputStream("../resources/fileSystem.ffs"); //ffs - Futuristic File System
 			ObjectOutputStream objectStream = new ObjectOutputStream(fileStream);
 			objectStream.writeObject(root);
 			objectStream.close();
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	private void renameComponent() {
 		if(mPaths.size() < 2) {
 			System.out.println("Not enough parameters. rn/rename directory newName");
 		}
 		else {
 			CommandPath targetPath = mPaths.get(0);
 			CommandPath nameParameter = mPaths.get(1);
 			if(nameParameter.getSize() > 1) {
 				System.out.println("Name parameter cannot be a directory path. rn/rename directory newName");
 			}
 			else {
 				String newName = mPaths.get(1).getNext();
 				
 				String step; boolean validPath = true;
 				Directory tempDirectory = mCurrentDirectory;
 				for(int i = 0; i < targetPath.getSize(); i++) {
 					step = targetPath.getNext();
 					tempDirectory = (Directory)tempDirectory.getComponent(step);
 					if(tempDirectory == null) {
 						validPath = false;
 						break;
 					}
 				}
 				if(validPath) {
 					tempDirectory.rename(newName);
 				}
 			}
 		}
 	}
 
 	private void create() {
 		
 		if(mPaths.size() > 0) {
 			CommandPath relevantPath = mPaths.get(0);
 			int numPaths = relevantPath.getSize();
 			
 			Directory tempDirectory = mCurrentDirectory;
 			boolean validDir = true; String step;
 			for(int i = 0; i < numPaths - 1; i++) {
 				step = relevantPath.getNext();
 				tempDirectory = (Directory)tempDirectory.getComponent(step);
 				if(tempDirectory == null) {
 					System.out.println("'" + step + "' does not exist.");
 					validDir = false;
 					break;
 				}
 			}
 			
 			if(validDir) {
 				step = relevantPath.getNext();
 				
 				tempDirectory.addComponent(new File(step));
 			}
 		}
 	} 
 	private void cat() {
 		
 		if(mPaths.size() > 0) {
 			CommandPath relevantPath = mPaths.get(0);
 			int numPaths = relevantPath.getSize();
 			
 			Directory tempDirectory = mCurrentDirectory;
 			boolean validDir = true; String step;
 			for(int i = 0; i < numPaths - 1; i++) {
 				step = relevantPath.getNext();
 				tempDirectory = (Directory)tempDirectory.getComponent(step);
 				if(tempDirectory == null) {
 					System.out.println("'" + step + "' does not exist.");
 					validDir = false;
 					break;
 				}
 			}
 			
 			if(validDir) {
 				step = relevantPath.getNext();
 				File file = null;
 				if(tempDirectory.getComponent(step) instanceof File) {
 					file = (File)tempDirectory.getComponent(step);
 					
 					if(file != null) {
 						
 						ArrayList<Integer> blockIndexes = file.getDataPointer();
 						for(int i=0; i<blockIndexes.size(); i++) {
						
							for(int j=0; j<File.BLOCK_SIZE; j++) {
								System.out.print(mBlocks.get(blockIndexes.get(i))[j] + "  ");
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	private void removeComponent() {
 		if(mPaths.size() > 0) {
 			Directory tempDirectory = mCurrentDirectory;
 			CommandPath targetPath = mPaths.get(0);
 			int numSteps = targetPath.getSize();
 			boolean validPath = true; boolean returnToRoot = false;
 			String step;
 			for(int i = 0; i < numSteps - 1; i++) {
 				step = targetPath.getNext();
 				if(step.equals("..") || step.equals(".")) {
 					System.out.println("System recognized '..' or '.' in delete statement. Returning to /root for system-safety.");
 					while(mCurrentDirectory.getParent() != null) {
 					mCurrentDirectory = (Directory)mCurrentDirectory.getParent();
 					}
 				}
 				tempDirectory = (Directory)tempDirectory.getComponent(step);
 				if(tempDirectory == null) {
 					System.out.println("Errounous file path.");
 					validPath = false;
 					break;
 				}
 			}
 			if(validPath) {
 				String toBeRemoved = targetPath.getNext();
 				if(tempDirectory.removeComponent(toBeRemoved)) {
 					System.out.println(toBeRemoved + " was removed sucessfully!");
 				}
 				else {
 					System.out.println("Could not find " + toBeRemoved + ".");
 				}
 			}
 		}
 		else {
 			System.out.println("Please specify path: rm/remove path");
 		}
 	}
 	private void copyFile() {
 		if(mPaths.size() < 2) {
 			System.out.println("Not enough specified paths. Please specify: copy origin destination");
 		}
 		else {
 			CommandPath origin = mPaths.get(0);
 			CommandPath destination = mPaths.get(1);
 			
 			/*Find Origin*/
 			int numSteps = origin.getSize();
 			String step; boolean validOrigin = true;
 			Component org = mCurrentDirectory;
 			for(int i = 0; i  < numSteps - 1; i++) {
 				step = origin.getNext();
 				org = ((Directory)org).getComponent(step);
 				if(org == null) {
 					validOrigin = false;
 					System.out.println("False origin detected - '" + step + "'.");
 					break;
 				}
 			}
 			if(validOrigin) {
 				String originFileName = origin.getNext();
 				org = ((Directory)org).getComponent(originFileName);
 				if(org != null) {
 					if(org instanceof File) {
 						System.out.println("Origin is a file.");
 						
 						numSteps = destination.getSize();
 						boolean validDestination = true;
 						Directory dir = mCurrentDirectory;
 						for(int i = 0; i < numSteps; i++) {
 							step = destination.getNext();
 							dir = (Directory)dir.getComponent(step);
 							if(dir == null) {
 								validDestination = false;
 								System.out.println("False destination detected.");
 								break;
 							}
 						}
 						if(validDestination) {
 							/*COPY DATA*/
 							dir.addComponent(new File(org.name()));
 							
 						}
 					}
 					else {
 						/*Implement code for folders here*/
 						System.out.println("Origin is a folder. Aborting.");
 					}
 				}
 				else {
 					System.out.println("org = null");
 				}
 			}
 		}
 	}
 }
