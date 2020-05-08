 package xtc.oop;
 
 
 import java.io.*;
 import java.util.*;
 /**@author Paige Ponzeka */
 
 /**MUST BE RUN FROM OOP ROOT FOLDER!
  Implements Runs some Test Cases based on assigned level
   Currently has no error checking... Ignores anything that is not a java file*/
 public class Tester
 {
 	private final String folderName= "TestCases"; //the default location for all the test cases
 	private String[][] test={
 	{"0","Type Zero To Exit"},
 	{"1","Primitive,Strings,Print,Arrays"},
 	{"2","If/Else,Switch,Loops etc"},
 	{"3","Returning Method,Method Calls"},
 	{"4","Instances,Recievers,Arrays of Objects"},
 	{"5","MethodChaining"},
 	{"6","Dependency"},
 	{"7","Inheritance"}
 
 	};
 	/**Calling This constructor will prompt you for all the test cases*/	
 	public Tester()
 	{
 		//print out the array of test Cases
 		System.out.println("Here are the Current Test Levels \n");
 		for(int r=0;r<test.length;r++)
 		{
 			System.out.print("\t\t");
 			int rlength=test[r].length;
 			for(int k=0;k<rlength;k++)
 			{
 				System.out.print(test[r][k]);
 				if(k!=rlength-1)
 				{
 					System.out.print(" - ");
 				}
 			}
 			System.out.println();
 		}
 		//print out the user message
 		System.out.print("Type a Number to Pick a Level (0 To Exit): ");
 		//  open up standard input
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		
 		String levelAsString = null;
 		
 		//  read the level from the command-line
 		try {
 			levelAsString = br.readLine();
 		} catch (IOException ioe) {
 			System.out.println("IO error!");
 			System.exit(1);
 		}
 		//try to convert to number if fails or zero exit
 		try
 		{
 			// the String to int conversion
 			int level = Integer.parseInt(levelAsString.trim());
 			
 			
 			//check to see if its Zero
 			if(level==0)
 			{
 				System.out.println("Operation Cancelled");
 			}
 			else { // call the level
 				
 				//print all the files in that level and let a user choose the file to call translator on
 				chooseFile(level);
 			}
 
 		}
 		catch (NumberFormatException nfe)
 		{
 			System.out.println("ERROR: " + nfe.getMessage());
 			System.exit(1);
 		}
 	}
 	/**For every File in that level folder print then out*/
 	public void chooseFile(int level)
 	{
 		String currentFolder= folderName+"/"+level+"/";
 
 		//calls a file reader to get all the files in a folder with the ending .java
 		String[] filenames= getFilesInLevel(level);
 		
 		//User Messages
 		System.out.println("Choose a File To Translate:");
 		System.out.println("0 - Exit");
 		for(int i=0;i<filenames.length;i++)
 		{
 			System.out.println(""+(i+1)+" - "+filenames[i]);
 			
 		}
 		//  open up standard input
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		String fileNumberAsString  = null;
 		//  read the file number from the command-line
 		try {
 			fileNumberAsString = br.readLine();
 		} catch (IOException ioe) {
 			System.out.println("IO error!");
 			System.exit(1);
 		}
 		//try to convert to number if fails or zero exit
 		try
 		{
 			// the String to int conversion
 			int fileNumber = Integer.parseInt(fileNumberAsString.trim());
 			//check to see if its Zero
 			if(level==0)
 			{
 				System.out.println("Operation Cancelled");
 			}
 			else { // call the level
 				
 				compileJava(currentFolder,filenames[(fileNumber-1)]);
 			}
 		}
 		catch (NumberFormatException nfe)
 		{
 			System.out.println("ERROR: " + nfe.getMessage());
 			System.exit(1);
 		}
 		
 		
 	}
 	/**runs the test on the given level calling translator on each file in the level*/
 	public void runTest(String location, String FileName)
 	{
 			//for each file call translator .java making sure to send the file name as an argument
 			String[] arguments= new String[2];
 			arguments[0]="-printJavaAST";
 			arguments[1]=location+FileName;
 					
 			//Translator t=new Translator();
 			new Translator().run(arguments);
 			//System.out.println("Args ran");
 	}
 	/**@return String[] of all the files in the given subdirectory, 
 	 filter to make sure only files and end in .java are included */
 	public String[] getFilesInLevel(int level)
 	{
 		File dir=new File(folderName+"/"+level);
 		  FilenameFilter onlyJava = new FileListFilter("", "java");
 		String[] files = dir.list(onlyJava);
 		return files;
 	}
 	public void runTranslation(String folder, String filename)
 	{
		String cmd="Java xtc.oop.Translator -translate "folder +" " filename+;
 		runCommand(cmd);
 		
 	}
 	/**Calls Terminal Command to compile Java Code based on given file name*/
 	public void compileJava(String folder,String fileName)
 	{
 		System.out.println("Compiling Java...");
 		String cmd = "javac "+folder+fileName;
 		runCommand(cmd);
 		runJava(folder, fileName);
 	}
 	/**Runs Compiled Java*/
 	public void runJava(String folder, String fileName)
 	{
 		//remove .java
 		System.out.println("Running Java...");
 		String className= removeJava(fileName);
 		String cmd = "java -cp "+folder+" " +className;
 		runCommand(cmd);
 	}
 	public String removeJava(String fileName)
 	{
 		StringTokenizer st = new StringTokenizer(fileName, ".");
 		return st.nextToken();
 	}
 	public void runCommand(String command)
 	{
 		try
         {            
             Runtime rt = Runtime.getRuntime();
             Process proc = rt.exec(command);
             InputStream stderr = proc.getErrorStream();
             InputStreamReader isr = new InputStreamReader(stderr);
             BufferedReader br = new BufferedReader(isr);
             String line = null;
             while ( (line = br.readLine()) != null)
                 System.out.println(line);
             int exitVal = proc.waitFor();
 			InputStream istrm = proc.getInputStream();
 			InputStreamReader istrmrdr = new InputStreamReader(istrm);
 			BufferedReader buffrdr = new BufferedReader(istrmrdr);
 			String data;
 			while ((data = buffrdr.readLine()) != null) {
 				System.out.println(data);
 			}
 			
 		} catch (Throwable t)
 		{
             t.printStackTrace();
 		}
 	}
 	/**Calls Terminal Command to Compile c++ Generated Code, Prints it to the terminal*/
 	public void compileCpp(String fileName)
 	{
 	}
 	/**Runs cpp files*/
 	public void runCpp(String fileName)
 	{
 	}
 	/**compares the results of the java and Cpp files and prints out messages*/
 	public void compareResults()
 	{
 		//print java string
 		//print cpp string
 		//append the results to a continuing log, TimeStamp : file.java Output, file.cpp Output
 		//compare the two result strings
 		//prints out message based on results
 	}
 }
 
 /** // Define a filter for java source files beginning with F
  */
 class FileListFilter implements FilenameFilter {
 	private String name; 
 	
 	private String extension; 
 	
 	public FileListFilter(String name, String extension) {
 		this.name = name;
 		this.extension = extension;
 	}
 	
 	public boolean accept(File directory, String filename) {
 		boolean fileOK = true;
 		
 		if (name != null) {
 			fileOK &= filename.startsWith(name);
 		}
 		
 		if (extension != null) {
 			fileOK &= filename.endsWith('.' + extension);
 		}
 		return fileOK;
 	}
 } 
