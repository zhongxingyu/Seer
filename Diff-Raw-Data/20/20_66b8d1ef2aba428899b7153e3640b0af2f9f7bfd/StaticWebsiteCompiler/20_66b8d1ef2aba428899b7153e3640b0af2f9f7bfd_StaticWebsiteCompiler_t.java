 package com.rudyrigot.staticwebsitecompiler;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.rudyrigot.staticwebsitecompiler.batch.BatchScripter;
 import com.rudyrigot.staticwebsitecompiler.batch.DefaultBatchScripter;
 import com.rudyrigot.staticwebsitecompiler.parsers.WebpageParser;
 import com.rudyrigot.staticwebsitecompiler.parsers.WebsiteParser;
import com.rudyrigot.staticwebsitecompiler.websiteversion.WebsiteVersion;
 
 public class StaticWebsiteCompiler {
 	
 	private static String filename;
 	private static int version;
 	private static BatchScripter batchScripter;
 	
 	public static void main (String [] args) {
 		System.out.println("**************************************************");
 		System.out.println("* StaticWebsiteCompiler is your new best friend! *");
 		System.out.println("**************************************************");
 		System.out.println();
 
 		// parsing the command line
 		if (args.length%2!=0) usage(null);
 		int i=0;
 		while (i<args.length) {
 			String operation = args[i];
 			String argument = args[i+1];
 			if (!operation.startsWith("-")) usage(operation+" is not a valid operation (should start with '-')");
 			operation = operation.substring(1);
 			if (operation.equals("f")) checkFilename(argument);
 			else if (operation.equals("v")) checkVersion(argument);
 			else if (operation.equals("b")) checkBatchScripter(argument);
 			else usage(operation+" is not a valid operation : only -f, -v, and -b exist");
 			i+=2;
 		}
 		
 		// Getting the default BatchScripter is none was specified
 		if (batchScripter==null) batchScripter = new DefaultBatchScripter();
 		
 		// Checking the version
		if (version>batchScripter.getWebsiteVersions().size()) usage("The BatchScripter doesn't have a version "+version+", poor guy only has "+batchScripter.getWebsiteVersions().size());
 		
 		// Checking the website root
 		File websiteRoot = new File(batchScripter.getWebsiteRoot());
 		if (!websiteRoot.exists()) usage("The directory "+batchScripter.getWebsiteRoot()+" does not exist.");
 		// It exists alright, but does it contain all it should?
 		File websiteRootSources = new File(batchScripter.getWebsiteRoot()+"/sources");
 		if (!websiteRootSources.exists()) websiteRootSources.mkdir();
 		File websiteRootSourcesRootpages = new File(batchScripter.getWebsiteRoot()+"/sources/rootpages");
 		if (!websiteRootSourcesRootpages.exists()) websiteRootSourcesRootpages.mkdir();
 		File websiteRootSourcesModules = new File(batchScripter.getWebsiteRoot()+"/sources/modules");
 		if (!websiteRootSourcesModules.exists()) websiteRootSourcesModules.mkdir();
 		File websiteRootWebcontent = new File(batchScripter.getWebsiteRoot()+"/WebContent");
 		if (!websiteRootWebcontent.exists()) websiteRootWebcontent.mkdir();
 		
 		// Building the filenames list
 		List<String> filenamesList = null;
 		if (filename!=null) {
 			filenamesList = new ArrayList<String>();
 			filenamesList.add(filename);
 		}
 		
		if (version!=0) // if version is set --> one parse
			new WebsiteParser(batchScripter.getWebsiteRoot(), filenamesList, batchScripter.getWebsiteVersions().get(version-1), batchScripter.getCompilingActions()).parse();
		else { // otherwise --> parsing every version
			for (WebsiteVersion websiteVersion : batchScripter.getWebsiteVersions())
				new WebsiteParser(batchScripter.getWebsiteRoot(), filenamesList, websiteVersion, batchScripter.getCompilingActions()).parse();
		}
 	}
 	
 	/**
 	 * Checking and storing the argument passed as a filename 
 	 * 
 	 * @param argument
 	 */
 	private static void checkFilename(String argument) {
 		filename = argument;
 		System.out.println("File: "+filename+(!filename.endsWith(".html")?"  (warning: doesn't end in .html, but i guess you know that)":""));
 	}
 	
 	/**
 	 * Checking and storing the argument passed as a version
 	 * 
 	 * @param argument
 	 */
 	private static void checkVersion(String argument) {
 		try {
 			version = Integer.parseInt(argument);
 			System.out.println("Version: "+version);
 		}
 		catch (Exception e) {
 			usage("Problem with version number parsing (must be a number, eh?)");
 		}
 	}
 	
 	/**
 	 * Checking and storing the argument passed as BatchScripter classname
 	 * 
 	 * @param argument
 	 */
 	private static void checkBatchScripter(String argument) {
 		Class batchScripterClass=null;
 		try {
 			batchScripterClass = Class.forName(argument);
 		}
 		catch (ClassNotFoundException e) {
 			usage("The class "+argument+" does not exist");
 		}
 		BatchScripter batchScripter = null;
 		try {
 			Constructor batchScripterConstructor = batchScripterClass.getConstructor();
 			Object batchScripterObject = batchScripterConstructor.newInstance();
 			batchScripter = (BatchScripter) batchScripterObject;
 		}
 		catch(Exception e) {
 			usage("Problem with the constructor: "+e.getMessage());
 		}
 		StaticWebsiteCompiler.batchScripter = batchScripter;
 		System.out.println("BatchScripter built: "+batchScripter.toString());
 	}
 	
 	private static void usage(String message) {
 		if (message!=null) System.err.println("Error : "+message);
 		System.out.println("Usage : three options");
 		System.out.println(" * -f : if for a file in particular (parses all of them otherwise)");
 		System.out.println(" * -v : if for a version in particular (parses all of them otherwise)");
 		System.out.println(" * -b : for the BatchScripter location (uses the default one otherwise)");
 		System.out.println();
 		System.out.println("Example :");
 		System.out.println("java -jar StaticWebsiteCompiler.jar -f index.html -v 1 -b com.rudyrigot.staticwebsitecompiler.TenorBatchScripter");
 		System.exit(-1);
 	}
 
 }
