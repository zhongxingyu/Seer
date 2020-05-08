 package compiler;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import compiler.Settings;
 import compiler.UnitTests.*;
 import boxcode.Compiler;
 
 
 /**
  * Interface object, menu object (starts with label, add options, draw)
  * All interface elements are objects internally, options are added to menues at program start-up.
  */
 
 public class CLI
 {
 	static BufferedReader br = new BufferedReader (new InputStreamReader(System.in)); //Pulled straight from Mr. Carin's grade 11 class.
 
 	
 	public static interface Option
 	{public String getName ();  //Name of the option that appears in menu.
 	public byte getWidth();  //Width of the name.
 	public void select ();}  //Called when the user selects this option.  Represents selected behaviour.
 
 
 
 	
 	/** Standard menu object.  Draws a
 	 * _________
 	 * |Menu   |
 	 * |_______|
 	 * 
 	 * automagically.  Options (including other menus) are added to these as needed, the menu renders
 	 * them with itself.
 	 */
 	public static class Menu implements Option
 	{
 		private String name;
 		private String [] label;  //Displays at the top of a menu.
 		private byte width;  //Displays the width of the table.  Not related to getWidth.
 		private HashMap<String,Option> options;
 		private Integer size; //Size of 'options'
 
 		
 		/**Menu object.  Assumes the label is narrow enough to fit within the menu border.
 		 * @param title displayed at the top of the menu
 		 * @param width width of the menu border.
 		 */
 		public Menu (String [] label, String name, byte width)
 		{
 			this.label = label;
 			this.name = name;
 			this.width = width;
 			size = new Integer(0);  //Number of elements in options.
 			options = new HashMap<String,Option>();
 			
 			ErrorLog.debug("Built " + name);
 		}
 		
 		public void addOption (Option element)
 		{
 			size++;  //Increment size to account for the new list element.
 			options.put(size.toString(), element);
 			
 			ErrorLog.debug("Added " + element.getName() + " to " + name);
 		}
 		
 		public String getName()	{
 			return name;		}
 		public byte getWidth()	{
 			return (byte) name.length();}
 		
 		public void select() {  //Display menu, select option.
 			String sinput = "";
 			Option selected = null;
 			ErrorLog.debug(name + " selected.");
 			
 			while (true) {
 				System.out.println(toString()); //Print menu.
 				sinput = read(); //Get user selection.
 				if (!sinput.equalsIgnoreCase("exit")) {
 					selected = options.get(sinput); //Find corresponding option.
 
 					if (selected != null) //Sinput corresponds to an extant option.
 						selected.select(); //Select the selected.
 					else
 						print(sinput + " is not a valid option");
 				} else
 					{print("Returning to previous menu."); break;}
 			}
 			
 			ErrorLog.debug ("Quitting " + name + ". Sinput:"+sinput);
 			
 			return;
 		}
 		
 		
 		public String toString ()  //Generates menu.
 		{
 			StringBuffer table = new StringBuffer (width*size);			
 
 			drawTopBorder(table);
 			ErrorLog.debug ("Printed top border.");
 			
 			for (int i=0; i<label.length; i++)
 			{
 				draw (label[i], table);  //Draw the label.
 				ErrorLog.debug ("Printed " + label[i]);
 			}
 			draw ("   Please select an option:", table);
 			for (Integer i=new Integer(1); i <= size; i++)
 			{
 				ErrorLog.debug ("Printing " + i.toString());
 				draw (i.toString() + ": " + options.get(i.toString()).getName() , table);  //Draw the options.
 				// **When not calling .getName on the returned option, it defaults to the toString() which it is called from
 				// (recursively)
 			}
 			draw ("Exit", table);
 
 			drawBottomBorder(table);
 			
 			return table.toString();  //To stop Eclipse from complaining.
 		}
 		
 		
 		private void drawTopBorder(StringBuffer canvas) { //Assumes printable is empty.
 			for (int i=0; i<width; i++)
 				canvas.append('_');  //Top bar.
 			canvas.append('\n');}  //The screen is my canvas.
 		
 		
 		private void draw (String printable, StringBuffer canvas) {
 			canvas.append("| ");
 			
 			canvas.append(printable);
 			for (int i=printable.length(); i<width-4; i++) //Fill in  the spaces.
 			{
 				canvas.append(' ');
 			}
 			canvas.append(" |\n");
 		}
 		
 		
 		private void drawBottomBorder(StringBuffer canvas){
 			if (width >= 2){
 				canvas.append('|');
 				for (int i=0; i<width-2; i++)
 					canvas.append('_');
 				canvas.append("|\n");
 			}
 			else
 				canvas.append('|');
 		}
 	}
 
 	
 	
 	/*This class, PickNumber and Editor extend the command-line system to modifying global settings.
 	 * I haven't yet decided if these would be better off in the Settings class.  Same goes for system-tests.
 	 * Should those be in here?  Or in the system test file?*/
 	/**
 	 * Toggle class for changing a boolean setting.	 *
 	 */
 	public static class Toggle implements Option
 	{
 		private Boolean setting;
 		private String name;
 		private String trueName;
 		private String falseName;
 		
 		
 		/**
 		 * Constructor for a setting toggle option.
 		 * @param setting The user is given the option to change this value.
 		 * @param name This is the name of the value that the user sees.
 		 */
 		public Toggle (Boolean setting, String name){
 			this.setting = setting;
 			this.name = name;
 			trueName = "enabled"; //Default names.
 			falseName = "disabled";
 		}
 		
 		/**
 		 * Constructor for a setting toggle option.
 		 * @param setting The user is given the option to change this value.
 		 * @param name This is the name of the value that the user sees.
 		 * @param trueName setting is in this state while "true"
 		 * @param falseName setting is in this state while "false"
 		 */
 		public Toggle (Boolean setting, String name, String trueName, String falseName){
 			this.setting = setting;
 			this.name = name;
 			this.trueName = trueName;
 			this.falseName = falseName;
 		}
 		
 		public String getName (){
 			return name;}
 		public byte getWidth (){
 			return (byte)name.length();}
 		
 				
 		public void select (){
 			String selection;
 			
 			print (displaySetting());
 			print ("Would you like to change this? (Y/N)");
 			
 			selection = read();
 			
 			if (selection.equalsIgnoreCase("Y")){
 				setting = !setting; //Reverse setting.
 				print (displaySetting());
 			}
 			else{
 				print ("Setting kept.");
 			}			
 		}
 		
 				
 		private String displaySetting (){
 			if (setting)
 				return name + " is: " + trueName; //Imaginative, I know...
 			else
 				return name + " is: " + falseName;
 		}
 	}
 	
 
 	
 	
 	/**
 	 * Settings interface for numeric variables.
 	 */
 	public static class PickNumber implements Option
 	{
 		private Short setting;
 		private int size; //May or may not represent the type passed to it...
 		private String name;
 		private String type;
 		
 		private final String yes = "Y"; //Input used to accept an option.
 		private final String no = "N";  //Input used to decline an option.
 		//Should these be moved to program settings?
 		
 			PickNumber (Short setting, String name, String type){
 			this.setting = setting;
 			//size = setting.SIZE;
 			this.name = name;
 			this.type = type;}
 		
 		
 		public String getName ()
 		{return name;}
 		public byte getWidth ()
 		{return (byte)name.length();}
 		public int getSize ()
 		{return size;}
 		
 		
 		
 		/**
 		 * The user invokes this to modify a numeric setting value.
 		 * This method should really be cleaned up (made more generic)
 		 */
 		public void select ()
 		{
 			String sinput;			
 			
 			do {
 				ErrorLog.debug ("Setting size: " + getSize());  //Need to check if Short values
 				//Can be passed in.
 				
 				print ("The current " + name + " is: " + setting + type);
 				print ("Would you like to change this? ("+yes+"/"+no+")");
 				sinput = read ();
 				
 				if (sinput.equalsIgnoreCase(yes)){
 					print ("Please enter the " + name + " in " + type);
 					sinput = read ();
 					
 					try {
 						setting = Short.parseShort(sinput);
 						print (name + " changed.");
 					}
 					catch (NumberFormatException e){					
 						print ("That's not a valid number!");
 					}					
 				}
 				else if (sinput.equalsIgnoreCase(no)){
 					print ("Setting kept.");
 					break;
 				}
 				else {
 					print ("That's not an option.");
 				}
 				
 			} while (true);
 		}		
 	}
 	
 	
 	
 	/**
 	 * User interface for editing String[]
 	 */
 	public static class Editor implements Option {
 
 		private String [] text;
 		private String name;
 		
 		public Editor (String[] text, String name){
 			this.text = text;
 			this.name = name;
 		}
 		public String getName() {
 			return name;}
 		public byte getWidth() {
 			return (byte)name.length();}
 		
 		public void select() {
 			print ("Editing "+name);  //Wraps old editor function.  I'm not going to rewrite that.
 			editor(text);  //This could be expanded to call the default system text editor, and
 			//default to this editor function.
 		}		
 	}
 	
 	
 	
 	/**
 	 * Shallow wrapper of System.out.println.  Could be expanded to include, say, the minecraft interface.
 	 * @param output
 	 */
 	static void print (Object output){
 		System.out.println(output);
 	}
 	static void print (){  //Stops Eclipse from giving me annoying errors while typing...
 		System.out.println();
 	}
 
 
 	/**
 	 * Text reader.  Calls br.readLine
 	 * @return user input.  Null on error.
 	 */
 	static String read ()
 	{
 		String sinput;
 
 		try
 		{
 			sinput = br.readLine();
 		}
 		catch (IOException e)
 		{
 			ErrorLog.addError("Input exception occured in command line interface!");
 			return null;  //Menu will exit when fed a null
 		}
 		return sinput;
 	}
 
 
 	
 	
 	/**
 	 * Command-line menu for various submenues.
 	 */
 	static void mainMenu ()
 	{
 		final byte width = 45;  //Menu is 45 characters wide.
 		String label [] = {"Welcome to my RedGame 2 compiler"};
 		
 		Menu front = new Menu(label, "Main Menu", (byte) width);
 		
 		Menu systemTests = systemTests(); //Gen a test object.
 		Menu settings = settings(); //Gen a setting object.
 		
 		front.addOption (systemTests);
 		front.addOption (settings);
 		
 		front.select();
 		//The program should exit after this menu has returned.  CLI-specific
 		//exit operations should be here:
 		
 		print ("\nThank you for using my program.");
 	}
 	
 	
 	
 	/**
 	 * Command-line menu for various program testing tasks.
 	 */
 	static Menu systemTests ()
 	{
 		final byte width = 40;
 		Option commentRemoval, findBinary, literal, next, defineSettings, isBinary, split,
 		programROM, templateROM, schematicSample, RCPRead;  //List of all system tests.
 		String label [] = {"What test would you like to run?"};
 		Menu tests = new Menu (label, "Run system tests.", width);
 		
 		//Initialising options:
 		commentRemoval = new TestInterface ("Comment Removal", new CommentRemoval());
 		findBinary = new TestInterface ("Find Binary", new FindBinary());
 		literal = new TestInterface ("Hardware Literals", new Literal());
 		next = new TestInterface ("Next line", new Next());
 		defineSettings = new TestInterface ("Define Settings", new DefineSettings());
 		isBinary = new TestInterface ("[string] Is Binary", new IsBinary());
 		split = new TestInterface ("Split", new Split());
 		programROM = new TestInterface ("Generate program ROM", new ProgramROM());
 		templateROM = new TestInterface ("Generate ROM template", new TemplateROM());
 		schematicSample = new TestInterface ("Generate sample schematic", new SchematicSample());
 		RCPRead = new TestInterface ("Read sample RCP file", new RCPReader());
 		
 		//Adding options to test menu:
 		tests.addOption (commentRemoval);
 		tests.addOption (findBinary);
 		tests.addOption (literal);
 		tests.addOption (next);
 		tests.addOption (defineSettings);
 		tests.addOption (isBinary);
 		tests.addOption (split);
 		tests.addOption (programROM);
 		tests.addOption (templateROM);
 		tests.addOption (schematicSample);
 		tests.addOption (RCPRead);
 		//These options will be displayed in the order they were added here.
 		
 		return tests;
 	}
 
 	
 	/**
 	 * Generates a menu for program settings.
 	 * @return menu object for this settings menu.
 	 */
 	static Menu settings ()
 	{
 		final byte width = 45;
 		Option ram, rightHanded, dumpState, trackTime, assembly, compression;
 		String label [] = {"Which settings field would you",
 		"like to modify?"};
 		Menu settings = new Menu (label, "Change program settings.", width);
 		
 		//Initialising options:
 		assembly = new Editor (Settings.assembly, "Assembly Definitions");
 		rightHanded = new Toggle (Settings.rightHanded, "RAM select", "Right", "Left"); //True stands for right;
 		//False for left.
 		dumpState = new Toggle (Settings.dumpState, "Data Recording");
 		trackTime = new Toggle (Settings.trackTime, "Time Tracking");
 		ram = new PickNumber (Settings.rAMallowed, "RAM Allowed", "bytes");
 		compression = new Toggle (Settings.compressNBT, "Schematic compression");
 		
 		//Adding options to setting menu:
 		settings.addOption(assembly);
 		settings.addOption(rightHanded);
 		settings.addOption(dumpState);
 		settings.addOption(trackTime);
 		settings.addOption(ram);
		settings.addOption(compression);
 		//Settings menu will display these options in the order they are added here.
 		
 		//TODO option for program name, target and programming language settings.
 		
 		return settings;
 	}
 	
 	
 	
 //	static void unitTests ()
 //	{
 //		String selection;
 //		String passable;  //Argument to most test methods.
 //
 //		do {
 //			print("_____________________________________________");
 //			print("| What test would you like to run?          |");
 //			print("| 1:  Remove comments                       |");
 //			print("| 2:  RAM                                   |");
 //			print("| 3:  Hardware literal                      |");
 //			print("| 4:  Next i                                |");
 //			print("| 5:  Next Word                             |");
 //			print("| 6:  Define settings                       |");
 //			print("| 7:  Is binary                             |");
 //			print("| 8:  Split                                 |");
 //			print("| 9:  Assemble (SASM)                       |");
 //			print("| 10: Find Binary                           |");
 //			print("| 11: Radix                                 |");
 //			print("| 12: Assemble (RCV)                        |");
 //			print("| 13: Build schematic					   |");
 //			print("| E:  Exit menu.                            |");
 //			print("|___________________________________________|");
 //
 //			selection = read(); //Select test.
 //
 //			if (selection.equals("1"))  //Comment removal test.
 //			{
 //				UnitTests.testCommentRemoval();  //Test method uses separate output.
 //			}
 //			else if (selection.equals("3"))  //Hardware literal test.
 //			{
 //				UnitTests.testLiteral();
 //			}
 //			else if (selection.equals("4"))   //Next i test.
 //			{
 //				UnitTests.testNext();
 //			}			
 //			else if (selection.equals("6"))  //Define settings.
 //			{
 //				UnitTests.testDefineSettings();
 //			}
 //			else if (selection.equals("7"))  //Test IsBinary method.
 //			{
 //				UnitTests.testIsBinary();
 //			}
 //			else if (selection.equals("8"))   //Test split string method.
 //			{
 //				UnitTests.testSplit();
 //			}
 //			else if (selection.equals("9"))  //Test SASM assembler.
 //			{
 //				print ("Line to assemble:");
 //				passable = read();
 //
 //				if (true)
 //				{
 //					SourceCode temp = new SourceCode(1);  //Assumes parsing 1 line of assembly.
 //					temp.line[0] = passable;
 //					print (ParseASM.assembleSASM(temp).command[0].toString(true));
 //				}
 //			}
 //			else if (selection.equals("10"))   //Test Find Binary.
 //			{
 //				UnitTests.testFindBinary ();
 //			}
 //			else if (selection.equals("11"))   //Test radix method.
 //			{
 //				print ("Find radix of:");
 //				passable = read ();
 //				System.out.println(boxcode.Compiler.Radix(passable));
 //			}
 //			else if (selection.equals("12"))  //Test RCV assembler.
 //			{
 //				print ("Line to assemble:");
 //				passable = read();
 //
 //				if (true)
 //				{
 //					SourceCode temp = new SourceCode(1);  //Assumes parsing 1 line of assembly.
 //					temp.line[0] = passable;
 //					print (ParseASM.assembleRCV(temp).command[0].toString(true));
 //				}
 //			}
 //			else if (selection.equals("13"))
 //			{
 //				UnitTests.makeROM(0);
 //			}
 //			else if (selection.equalsIgnoreCase("E"))
 //			{
 //				print ("Returning to main menu.");
 //				break;
 //			}
 //			else
 //			{
 //				print ("That's not a valid input!");
 //			}
 //
 //
 //		} while (!selection.equals(""));  //Null string exits.
 //	}
 
 
 
 
 	/**
 	 * Command line text document editor.
 	 * @param text
 	 */
 
 	static void editor (String[] text)
 	{
 		int length = text.length;
 		int start = 0;
 		int end = 0;
 		boolean complete = false;
 		boolean invalidEntry = true;
 		String sinput;
 
 		do
 		{
 			print (Settings.newLine + "Editing document.");
 			print ("The document is " + length + " lines long.");
 			print ("Press 1 to view the complete document.");
 			print ("Press 2 to view a section of the document.");
 			print ("Press 3 to view one line.");
 			print ("Type \"Cancel\" to not view document.");
 
 			sinput = read();
 
 			if (sinput.equals("1"))  //Select entire document...
 			{
 				start = 0;  //From start
 				end = length - 1; //To end.
 			}
 			else if (sinput.equals("2"))  //Select finite range.
 			{
 				do
 				{
 					print ("Please select the first line:");
 					sinput = read();
 
 					try
 					{
 						start = Integer.parseInt(sinput) - 1;
 						if (0 <= start && start <= length)						
 							invalidEntry = false;						
 					}
 					catch (NumberFormatException e){
 						print ("Cannot parse that number.");
 						invalidEntry = true;
 					}
 				}
 				while (invalidEntry);
 
 				do
 				{
 					print ("Please select the last line:");
 					sinput = read ();
 
 					try
 					{
 						end = Integer.parseInt(sinput) - 1;
 						if (0 <= end && end <= length)						
 							invalidEntry = false;						
 					} 
 					catch (NumberFormatException e){
 						print ("Cannot parse that number.");
 						invalidEntry = true;
 					}
 				}
 				while (invalidEntry);
 			}
 			else if (sinput.equals("3"))  //Select a line.
 			{
 				do
 				{
 					print ("Please select a line to view:");
 					sinput = read();
 
 					try
 					{
 						start = Integer.parseInt(sinput) - 1;
 						end = start;
 						if (0 <= start && start < length)						
 							invalidEntry = false;						
 					}
 					catch (NumberFormatException e){
 						print ("Cannot parse that number.");
 						invalidEntry = true;
 					}
 				}
 				while (invalidEntry);
 			}
 
 			// *Start and end points selected.
 
 
 			if (!sinput.equals("Cancel"))
 			{
 				print ("The selected text is:" + Settings.newLine);
 
 				for (int i = start; i <= end; i++)				
 					print ((i+1) + " " + text[i]);  //Print selected lines.
 				
 				//Edit text
 
 				print (Settings.newLine + "Would you like to edit this selection? (Y/N)");
 				sinput = read();
 
 				if (sinput.equalsIgnoreCase("Y"))
 				{
 					for (int i = start; i <= end; i++)					
 						text[i] = read();  //Directly set line i to input.  No undo available.
 					
 					print ("Editing complete.");
 				}
 			}
 			else			
 				complete = true;
 			
 		}
 		while (!complete);
 	}
 
 }
