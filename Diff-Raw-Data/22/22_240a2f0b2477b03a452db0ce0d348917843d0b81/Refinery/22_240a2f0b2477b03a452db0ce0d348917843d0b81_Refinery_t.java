 /*
  * Copyright 2006 Sony Computer Entertainment Inc.
  *
  * Licensed under the SCEA Shared Source License, Version 1.0 (the "License"); you may not use this 
  * file except in compliance with the License. You may obtain a copy of the License at:
  * http://research.scea.com/scea_shared_source_license.html
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License 
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions and limitations under the 
  * License. 
  */
 package refinery;
 
 import java.io.*;
 import java.util.Iterator;
 import java.net.*;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.DateFormat;
 
 import javax.swing.UIManager;
 import javax.swing.JOptionPane;
 import javax.xml.XMLConstants;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.xml.sax.SAXParseException;
 
 import refinery.display.DisplayAreaDummy;
 import refinery.display.DisplayAreaInterface;
 import refinery.display.MainGUI;
 import refinery.document.ConditionerWrapper;
 import refinery.document.FileLoader;
 import refinery.document.Pipeline;
 import refinery.document.PipelineMacro;
 import refinery.history.Mark;
 import refinery.output.Console;
 import refinery.output.ErrorAndWarningReport;
 import refinery.output.LogOutput;
 import refinery.output.StandardOutput;
 
 
 public class Refinery {
 	private final String VERSION_NUMBER = new String("2.0.0");
 
 	public static Refinery instance;					// We will have only one instance of Refinery in this progam.
 														// This will allow easy access from anywhere in the code.
 														// (to add log for example)
 	
 	// The different behavior we can have depending on command line options :
 	public enum Mode {Normal, ExLib, ExMacro, ExFile, Help, HelpLib, ListLibs, Error};	
 	public enum LogLevel {NORMAL, DEBUG};
 	private MainGUI gui;								// A reference to our main gui...
 	private DisplayAreaInterface displayArea;			// and it's display area
 	private AvailableConditionersList availableCondList;// This list is setup at init with detected conditioners
 	private AvailableMacrosList availableMacroList;		// List of macros detected
 	private LogOutput console;							// 
 	private LogOutput exWin;								// Potential other place where we want to log
 	//private ExternalLibs extLibs;						// Our external lib (conditioner) loader
 	private LibLoader extLibs;
 
 	private Pipeline document;							// Our root document.
 	public ErrorAndWarningReport errorAndWarnings;
 	public Preferences preferences;
 
	public static String RefineryDirectory;    // The top-level directory of the Refinery project and its data files,
	                                           //   e.g. "C:\Program Files\COLLADA Refinery"
	public static String ApplicationDirectory; // The actual bin path that the Refinery was run from,
	                                           //   e.g. "C:\Program Files\COLLADA Refinery\bin\vc8\release"
 	
 //	public static final String CONDITIONER_DIRECTORY = "lib";
 //	public static final String MACRO_DIRECTORY = "macros";
 //	public static final String TEMP_DIRECTORY = "tmp";
 	
 	public Refinery() {
 		instance = this;
 		availableCondList = new AvailableConditionersList();
 		availableMacroList = new AvailableMacrosList();
 		gui = null;
 		displayArea = new DisplayAreaDummy();
 		console = new StandardOutput();
 		exWin = null;
 		
 		preferences = new Preferences();
 		preferences.load();
 		
 		newWorkSpace();
 		errorAndWarnings = new ErrorAndWarningReport();
 		extLibs = new LibLoader(availableCondList, this);
 
		String mainConditionerDir = Refinery.ApplicationDirectory + File.separator + "conditioners";
 		availableCondList.load(extLibs, mainConditionerDir);
 		availableCondList.load(extLibs, RefineryDirectory + File.separator + preferences.getValue("DIR_USER_LIB"));
 
 		if(errorAndWarnings.hasErrors() || errorAndWarnings.hasWarnings()){
 			errorAndWarnings.popupWindow();
 		}
 	}
 	
 	public void loadMacros(){
 		errorAndWarnings = new ErrorAndWarningReport();
 		availableMacroList.loadFromDirectory(RefineryDirectory + File.separator + preferences.getValue("DIR_MACRO"));
 		if(errorAndWarnings.hasErrors() || errorAndWarnings.hasWarnings()){
 			errorAndWarnings.popupWindow();
 		}
 	}
 	
 	public void startGUI(){
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			System.err.println("Could not load custom look & feel");
 		}
 		gui = new MainGUI (this, availableCondList, availableMacroList, document);
 		displayArea = gui.getBoxesArea();
 		console = new Console(this);
 	}
 	
 	
 	public AvailableConditionersList getAvailableCondList() {
 		return availableCondList;
 	}
 	
 	public AvailableMacrosList getAvailableMacroList(){
 		return availableMacroList;
 	}
 	
 
 	public MainGUI getGUI(){
 		return gui;
 	}
 	
 	public void setTempLoger(LogOutput loger){
 		exWin = loger;
 	}
 	
 	public void unsetTempLoger(){
 		exWin = null;
 	}
 	
 	public void setConsoleVisible(boolean newValue){
 		if(console instanceof Console){
 			((Console) console).setVisible(newValue);
 		}
 	}
 	
 	/**
 	 * @return Returns the document.
 	 */
 	public Pipeline getDocument() {
 		return document;
 	}
 	
 	public void addNormalMessage(String message){
 		addLog(message, LogLevel.NORMAL);
 	}
 	
 	public void addDebugMessage(String message){
 		addLog(message, LogLevel.DEBUG);
 	}
 	
 	public void addLog(String message, LogLevel level){
 		switch(level){
 		case NORMAL:
 			console.addLog(message);
 			if(exWin != null){
 				exWin.addLog(message);
 			}
 			break;
 		case DEBUG:
 			console.addLog(message);
 			break;
 		}
 	}
 	
 	public LibLoader getLibLoader(){
 		return extLibs;
 	}
 	
 	
 	public void newWorkSpace(){
 		newDocument();
 		document.addInputBox();
 		document.addOuputBox();
 		
 		
 	}
 	
 	public void newDocument(){
 		document = new Pipeline(this);
 		if (gui != null){
 			gui.setDocument(document);
 		}
 		displayArea.setDocument(document);
 	}
 	
 	
 	public void loadFile(String filename) throws SAXParseException, Exception{
 		newDocument();
 		Mark historyMark = document.getHistory().addMark();
 		//System.out.println("1###############");
 		FileLoader contentHandler = new FileLoader(this, document, displayArea);
 		//System.out.println("2###############");
 		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		//System.out.println("3###############");
 		Schema schema = factory.newSchema(new StreamSource(new File(Refinery.RefineryDirectory + File.separator + "savefiles.xsd")));
 		//System.out.println("4###############");
 		SAXParserFactory spf = SAXParserFactory.newInstance();
 		//System.out.println("5###############");
 		spf.setSchema(schema);
 		//System.out.println("6###############");
 		SAXParser saxParser = spf.newSAXParser();
 		//System.out.println("7###############");
 		//System.out.println(filename);
 		saxParser.parse(new File(filename), contentHandler);
 		//System.out.println("8###############");
 
 		document.setSaveFileName(filename);
         //System.out.println("9###############");
 		displayArea.checkDisplaySize();
 		//System.out.println("10###############");
         displayArea.repaint();
 		//System.out.println("11###############");
         document.getHistory().packSinceMark(historyMark);
 		//System.out.println("12###############");
 	}
 
 	private void checkForUpdates()
 	{
 		try
 		{
 			String update = preferences.getOption("UPDATE").value;
 			Calendar currDate = Calendar.getInstance();
 			DateFormat df = DateFormat.getDateTimeInstance();
 			if (update.equals("YES") || currDate.getTime().after( df.parse( update ) ) ) 
 			{
 				URL url = new URL("http://www.collada.org/refineryupdates/version.ini");
 				URLConnection uconn = url.openConnection();
 				//System.out.println("Type = " + uconn.getContentType());
 				//System.out.println("Content = " + uconn.getContent());
 				BufferedReader br = new BufferedReader(new InputStreamReader(uconn.getInputStream()));
 				String ln = br.readLine();
 				//System.out.println("Version = " + ln);
 				if (!ln.equals(VERSION_NUMBER))
 				{
 					String[] options = { "Update", "Update later", "Remind me in 1 week", "Update never" };
 					String message = "Newer version of COLLADA Refinery available.\nUpdate now?\n\nnew version: " + ln + "\ncurrent version: " + VERSION_NUMBER;
 					Object selected = JOptionPane.showOptionDialog(null, message, "Update?", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[1]);
 					if (selected.equals(0))
 					{
 						//System.out.println("Chose to update now");
 						ProcessBuilder pb = new ProcessBuilder("update.exe");
 						Process p = pb.start();
 						System.exit(0);
 					}
 					else if (selected.equals(1) || selected == null)
 					{
 						//System.out.println("Chose to update later");
 					}
 					else if (selected.equals(2))
 					{
 						//System.out.println("Chose to remind");
 						currDate.add(Calendar.DAY_OF_MONTH, 7);
 						preferences.setValue("UPDATE", df.format(currDate.getTime()));
 						preferences.save();
 					}
 					else if (selected.equals(3))
 					{
 						//System.out.println("Chose to not update");
 						preferences.setValue("UPDATE", "NO");
 						preferences.save();
 					}
 					else
 					{
 						//System.out.println("NOTHING!!");
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			//System.out.println(e.getMessage());
 		}
 	}
 
 	private static String path;
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Mode mode;
 		CommandLineParser clp;
 		Refinery main;

		// Get the Refinery and bin locations as absolute paths
 		try {
			File appPath = new File(System.getProperty("java.class.path")).getCanonicalFile().getParentFile();
			Refinery.ApplicationDirectory = appPath.getCanonicalPath();
			Refinery.RefineryDirectory = appPath.getParentFile().getParentFile().getParentFile().getCanonicalPath();
 		}
 		catch (IOException e) {
 		}
 
 		if (Refinery.RefineryDirectory == null)
 		{
 			System.err.println("Failed to determine the location of the COLLADA Refinery!");
 			System.exit(-1);
 		}
 
 		try {
 			clp = new CommandLineParser(args);
 		} catch (ParsingErrorException e){
 			System.err.println(e.getMessage());
 			//e.printStackTrace();
 			System.err.println("Aborting");
 			return;
 		}
 		
 		mode = clp.getMode();
 		//System.out.println(clp.getMode());
 		//System.out.println("############");
 
 		main = new Refinery();
 		Pipeline document = main.document;
 		ConditionerWrapper lib;
 
 		switch(mode){
 		case Normal:
 			main.loadMacros();
 			main.startGUI();
 			main.checkForUpdates();
 			break;
 		case ExLib:
 			// To execute this conditioner, we add to the document and create links.
 			// One from input and one to ouput. 
 			String libName = clp.getLibName();
 			ConditionerTemplate cb = main.availableCondList.getConditionerBase(libName);
 			if (cb == null){
 				System.err.println("Error: Lib "+ libName + " not found, aborting.");
 				return;
 			}
 			lib = document.addConditioner(cb);
 			
 			if(clp.getInputFileNumber() != lib.getInputNumber()){
 				if(lib.isVariableInputNumber()){
 					lib.setInputNumber(clp.getInputFileNumber());
 				} else {
 					System.err.println("Error: you gave " + clp.getInputFileNumber() + " input files but " + lib.getName() + " takes " + lib.getInputNumber());
 					break;
 				}
 			}
 			Iterator<String> inFilesIt = clp.getInputFiles();
 			int fileN = 0;
 			while(inFilesIt.hasNext()){
 				String input = inFilesIt.next();
 				lib.setInput( fileN, input);
 				fileN++;
 
 				LibLoader.loadDocument(input);
 			}
 
 			Iterator<String> outFilesIt = clp.getOutputFiles();
 			fileN = 0;
 			while (outFilesIt.hasNext())
 			{
 				lib.setOutput(fileN, outFilesIt.next());
 				fileN++;
 			}
 			//lib.setOutput(0, clp.getOutputFile());
 			
 			try {
 				lib.executeSolo(clp.getLibArgs());
 			} catch (ParsingErrorException e1) {
 				//e1.printStackTrace();
 				System.err.println(e1.getMessage());
 			}
 
 			outFilesIt = clp.getOutputFiles();
 			fileN = 0;
 			while (outFilesIt.hasNext())
 			{
 				LibLoader.saveDocument(lib.getOutput(fileN), outFilesIt.next());
 				fileN++;
 			}
 			break;
 		case ExMacro:
 			//System.out.println("ExMacro #1");
 			main.loadMacros();
 			//System.out.println("ExMacro #2");
 			try {
 
 				MacroTemplate mb = main.availableMacroList.getMacroBaseFromLibName(clp.getMacroName());
 				if (mb == null)
 				{
 					mb = new MacroTemplate(new File(clp.getMacroName()));
 				}
 			    //System.out.println("ExMacro #3");
 				PipelineMacro macro = new PipelineMacro(main, mb);
 			    //System.out.println("ExMacro #4");
 
 				if (clp.getInputFileNumber() != macro.getInputNumber())
 				{
 					System.err.println("Error: you gave " + clp.getInputFileNumber() + " input files but " + macro.getName() + " takes " + macro.getInputNumber());
 					break;
 				}
 				Iterator<String> inIt = clp.getInputFiles();
 				int mfileN = 0;
 				while (inIt.hasNext())
 				{
 					String input = inIt.next();
 					macro.setInput(mfileN, input);
 					mfileN++;
 
 					LibLoader.loadDocument(input);
 				}
 
 				macro.executeSolo(clp.getLibArgs());
 				//System.out.println("after execute");
 				Iterator<String> outIt = clp.getOutputFiles();
 				mfileN = 0;
 				while (outIt.hasNext())
 				{
 					String fname = outIt.next();
 					//System.out.println("Output to be saved: " + fname);
 					LibLoader.saveDocument(fname, fname);
 					mfileN++;
 				}
 			} catch (Exception e1) {
 				System.err.println("Error : " + e1.getMessage());
 			}
 			break;
 		case ExFile:
 			try{
 				//System.out.println("before load macro");
 				//System.out.println("###############");
 				main.loadMacros();
 				//System.out.println("before load file");
                 //System.out.println(clp.getFileName());
 				//System.out.println("###############");
 				main.loadFile(clp.getFileName());
 				//System.out.println("after load file");
 				//System.out.println("###############");
 				main.document.execute();
 			} catch (Exception e){
 				System.err.println(e.getMessage());
 			}
 			break;
 		case HelpLib:
 			cb = main.getAvailableCondList().getConditionerBase(clp.getLibName());
 			if (cb == null){
 				System.err.println("Lib "+clp.getLibName()+ " not found. Aborting\n");
 			} else {
 				lib = document.addConditioner(cb);
 				System.out.println(lib.getUsage());
 			}
 			break;
 		case ListLibs:
 			main.loadMacros();
 			main.availableCondList.printList();
 			System.out.println();
 			main.availableMacroList.printList();
 			break;
 		case Help:
 		case Error:
 			// Nothing else to do
 			break;
 		}	
 
 	}
 
 }
