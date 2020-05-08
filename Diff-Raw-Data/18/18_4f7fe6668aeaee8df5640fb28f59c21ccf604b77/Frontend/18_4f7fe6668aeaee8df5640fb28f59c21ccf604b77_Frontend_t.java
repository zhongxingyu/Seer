 /*******************************************************************************
  * Copyright (c) 2012 Cesar Yeep.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the BSD 3-Clause License
  * ("New BSD" or "BSD Simplified") which accompanies this distribution,
  * and is available at
  * http://opensource.org/licenses/BSD-3-Clause
  * 
  * Contributors:
  *     Cesar Yeep - initial API and implementation
  ******************************************************************************/
 package edu.utep.cs.jasg;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Scanner;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 import edu.utep.cs.jasg.apiGenerator.MainAPIGenerator;
 import edu.utep.cs.jasg.specificationGenerator.XMLParser;
 
 /** Main program of JASG. */
 public class Frontend {
 	public static Scanner scanner = new Scanner( System.in );
 	private String frameworkNameProperty, versionProperty, urlProperty;
 	private String workspaceProperty, targetModuleProperty;
 	private String workspace, targetModule;
 
 	//TODO: convert string workspace to path
 	public static void main(String[] args){
 		new Frontend();
 	}
 
 	public Frontend(){
 		String option = "";
 
 		//Load configuration properties
 		getProperties();
 
 		printVersion();
 
 		//Check if there exists a workspace property
 		if(!workspaceProperty.equals("")){
 			System.out.println("A previous workspace \"" + workspaceProperty + "\" was identifed");
 			System.out.println("Do you want to use the same workspace? (yes, or no to specify a new one)");
 			String workspaceOption = scanner.nextLine();
 			if(workspaceOption.equals("yes"))
 				workspace = workspaceProperty;			
 		}
 
 		//Set workspace
 		if(workspace == null){
 			do{
 				System.out.print( "Please specify a worskapce: " );
 				workspace = scanner.nextLine();
 			}while(!setWorkspace(workspace));
 		}
 
 		//Check if there exists a target module property
 		if(!targetModuleProperty.equals("")){
 			System.out.println("A previous target module \"" + targetModuleProperty + "\" was identifed");
 			System.out.println("Do you want to use the same target module? (yes, or no to specify a new one)");
 			String workspaceOption = scanner.nextLine();
 			if(workspaceOption.equals("yes"))
 				targetModule = targetModuleProperty;			
 		}
 
 		//Set target module
 		if(targetModule == null){
 			do{
 				System.out.print( "Please specify a target module: " );
 				targetModule = scanner.nextLine();
 			}while(!setTargetModule(targetModule));
 		}
 
 		System.out.println("Using workspace \"" + getWorkspace() + "\"");
 		System.out.println("Using  target module \"" + getTargetModule() + "\"");
 
 		printOptions();
 
 		//Enable command prompt
 		while(true){
 			System.out.print("JASG> ");
 			option = scanner.nextLine();
 			if(option.equals("exit"))
 				break;
 			processOptions(option);
 		}
 	}
 
 	/** Constructor used for testing purposes. */
 	public Frontend(String workspace, String targetModule){
 
 		//Load configuration properties
 		getProperties();
 
 		printVersion();
 
 		setWorkspace(workspace);
 
 		setTargetModule(targetModule);
 
 	}
 
 
 	/** Get properties from properties file. */
 	private void getProperties(){
 
 		try {
 			Properties defaultProps = new Properties();
 			//load a properties file
 			defaultProps.load(new FileInputStream("properties"+File.separator+"JASG.properties"));
 
 			Properties workspaceProps = new Properties(defaultProps);
 			workspaceProps.load(new FileInputStream("properties"+File.separator+"Workspace.properties"));
 
 			Properties targetmoduleProps = new Properties(workspaceProps);
 			targetmoduleProps.load(new FileInputStream("properties"+File.separator+"TargetModule.properties"));
 
 			//get the property value and print it out
 			frameworkNameProperty = targetmoduleProps.getProperty("jasg.JASG");
 			urlProperty = targetmoduleProps.getProperty("jasg.URL");
 			versionProperty  = targetmoduleProps.getProperty("jasg.Version");
 
 			workspaceProperty = targetmoduleProps.getProperty("jasg.Workspace");
 
 			targetModuleProperty = targetmoduleProps.getProperty("jasg.TargetModule");
 
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 
 	/** Set workspace path. */
 	public boolean setWorkspace(String workspace){
 		Path path = Paths.get(workspace);
 		if(!Files.exists(path))
 		{
 			System.out.println("Workspace " + path.toString() + " doesn't exist");
 			return false;
 		}
 		else
 		{
 			this.workspace = path.toString();
 			setWorkspaceProperty(this.workspace);
 			System.out.println("Workspace " + "\"" + path.toString() + "\"" + " set");
 			return true;
 		}
 	}
 
 	/** Store workspace in properties. */
 	private void setWorkspaceProperty(String workspace){
 		Properties prop = new Properties();
 		try {
 			prop.setProperty("jasg.Workspace", workspace);
 
 			prop.store(new FileOutputStream("properties"+File.separator+"Workspace.properties"), null);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/** Set target module path. */
 	public boolean setTargetModule(String targetModule){
 		Path path = Paths.get(targetModule);
 		if(!Files.exists(path))
 		{
 			System.out.println("Target module " + path.toString() + " doesn't exist");
 			return false;
 		}
 		else
 		{
 			this.targetModule = path.toString();
 			setTargetModuleProperty(this.targetModule);
 			System.out.println("Target module " + "\"" + path.toString() + "\"" + " set");
 			return true;
 		}
 	}
 
 
 	/** Store targetModule in properties. */
 	private void setTargetModuleProperty(String targetModule){
 		Properties prop = new Properties();
 		try {
 			prop.setProperty("jasg.TargetModule", targetModule);
 
 			prop.store(new FileOutputStream("properties"+File.separator+"TargetModule.properties"), null);
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}	
 
 
 	/** Process options. */
 	private void processOptions(String arg){
 		String[] args = arg.split(" ");
 		switch(args[0]){
 		case "parse":
 			parseXMLSpec(processArgument(args));
 			break;
 		case "doc":
 			createParserDoc();
 			break;
 		case "set-workspace":
 			setWorkspace(processArgument(args));			
 			break;
 		case "get-workspace":
 			System.out.println(getWorkspace());
 			break;
 		case "clean-workspace-property":
 			setWorkspaceProperty("");
 			System.out.println("Workspace property cleared");
 			break;
 		case "set-target-module":
 			setTargetModule(processArgument(args));			
 			break;
 		case "get-target-module":
 			System.out.println(getTargetModule());
 			break;
 		case "clean-target-module-property":
 			setTargetModuleProperty("");
 			System.out.println("Target module property cleared");
 			break;
 
 		case "help":
 			printOptions();
 			break;
 		case "version":
 			printVersion();
 			break;
 
 		default: 
 			System.out.println("Invalid option");
 			printOptions();
 			break;
 		}
 	}
 
 	/** Process option argument. */
 	private String processArgument(String[] args){
 		String optionArg = "";
 		if(args.length > 1){
 			if(args[1] != null){
 				optionArg = args[1];
 			}
 		}
 
 		if(optionArg.equals("")){
 			System.out.print("Specify " + args[0] + " option argument: " );
 			optionArg = scanner.nextLine();
 		}
 		return optionArg;
 	}
 
 	/** Parse XML spec. */
 	private void parseXMLSpec(String fileName){
 		while(!checkFileExists(fileName)){
 			System.out.print( "Provide location of JASG XML specification file or type \"cancel\": " );
 			fileName = scanner.nextLine();
 			if(fileName.equals("cancel"))
 				return;
 		}
 		XMLParser xmlParser = new XMLParser(workspace);
 		xmlParser.parse(fileName);
 	}
 
 	/** Create documentation. */
 	public void createParserDoc(){
 		MainAPIGenerator apiGenerator = new MainAPIGenerator(workspace);
 		String[] parseResult = parseBuildFile(targetModule+File.separator+"build.xml");
 		String moduleName = parseResult[0];
 		String scannerName = parseResult[1];
 		String parserName = parseResult[2];
 
		scannerName = targetModule+File.separator+"scanner"+File.separator+scannerName+".flex";
		parserName = targetModule+File.separator+"parser"+File.separator+parserName+".all";
		
 		//Check if scanner name property was define in the target module's build.xml
		while(!checkFileExists(scannerName)){
 				System.out.print( "Provide name of target module's main scanner file (e.g. scanner"+File.separator+"<Name>.flex) or type \"cancel\": " );
 				scannerName = scanner.nextLine();
 				if(scannerName.equals("cancel"))
 					return;
 				scannerName = targetModule+File.separator+"scanner"+File.separator+scannerName+".flex";
 		}
 
 		//Check if parser name property was define in the target module's build.xml
		while(!checkFileExists(parserName)){
 				System.out.print( "Provide name of target module's main parser file (e.g. parser"+File.separator+"<Name>.all) or type \"cancel\": " );
 				parserName = scanner.nextLine();
 				if(parserName.equals("cancel"))
 					return;
 				parserName = targetModule+File.separator+"parser"+File.separator+parserName+".all";
 		}
 
 		//Check if name attribute was define in the target module's build.xml root
 		if(moduleName.equals("")){
 			System.out.print( "Provide a name for the xml generated file (output file located in <workspace>"+File.separator+"doc): " );
 			moduleName = scanner.nextLine();
 		}
		
 		apiGenerator.generateDoc(moduleName,scannerName,parserName);
 	}
 
 	/** Check if given file exists. */
 	public boolean checkFileExists(String fileName){
 		Path path = Paths.get(fileName);
 		if(!Files.exists(path))
 		{
 			System.out.println("File " + path.toString() + " doesn't exist");
 			return false;
 		}
 		else
 		{
 			return true;
 		}
 	}
 
 	/** Return workspace location. */
 	public String getWorkspace(){
 		return workspace;
 	}
 
 	/** Return target module location. */
 	public String getTargetModule(){
 		return targetModule;
 	}
 
 	/** Tool information. */
 	public void printVersion() {
 		System.out.println("\n"+frameworkNameProperty  + " " + urlProperty + " Version " + versionProperty +"\n");
 	}
 
 	/** Print basic tool usage. */
 	public void printOptions() {
 		System.out.println(
 				"Options:\n" +
 						"  -parse <JASGXMLFile.xml>\t\tParse a JASG specification file\n" +
 						"  -doc\t\t\t\t\tGenerate documentation from AST and parser files\n" +
 						//"  -import <JastAdd module path>\t\tImport a JastAdd module into current workspace\n" +
 						"  -set-workspace <workspace path> \tSet project workspace\n" +
 						"  -get-workspace\t\t\tView current workspace\n" +
 						"  -clean-workspace-property\t\tResets workspace property file\n" +
 						"  -set-target-module <workspace path> \tSet target module\n" +
 						"  -get-target-module\t\t\tView current target module\n" +
 						"  -clean-target-module-property\t\tResets target module property file\n" +
 						"  -help\t\t\t\t\tPrint a synopsis of standard options\n" +
 						"  -version\t\t\t\tPrint version information\n\n" +
 						"  -exit\t\t\t\t\tExit application\n"
 				);
 	}
 
 	/** Parse build XML file. Returns a String array with the following content:
 	 * 		[0] = target module name
 	 * 		[1] = target module main scanner specification file
 	 * 		[2] = target module main parser specification file
 	 * */
 	public String[] parseBuildFile(String filePath){
 		String[] result = {"","",""};
 
 		if(Files.exists(Paths.get(filePath))){
 
 			try {
 				SAXBuilder builder = new SAXBuilder();
 				Document doc = (Document) builder.build(filePath);
 				Element root = doc.getRootElement();
 
 				if(root != null){
 					List<Element> propertyList = root.getChildren("property");		
 					Iterator<Element> propertiesIterator = propertyList.iterator();
 					
 					//Get module name
 					String moduleName = root.getAttributeValue("name");
 					if(moduleName != null)
 						result[0] = moduleName;
 
 					//Get specification name properties
 					while(propertiesIterator.hasNext()){
 						Element property = propertiesIterator.next();
 						String propertyName = property.getAttributeValue("name");
 						String propertyValue = property.getAttributeValue("value");
 
 
 						if(propertyName != null){
 							//Get scanner name
 							if(propertyName.equals("scannerName")){
 								if(propertyValue != null)
 									result[1] = propertyValue;
 							}
 							//Get scanner name
 							else if(propertyName.equals("parserName")){
 								if(propertyValue != null)
 									result[2] = propertyValue;
 							}
 						}
 					}
 				}
 			} catch (IOException io) {
 				System.out.println(io.getMessage());
 			} catch (JDOMException jdomex) {
 				System.out.println(jdomex.getMessage());
 			}
 		}
 		return result;
 	}
 
 }
