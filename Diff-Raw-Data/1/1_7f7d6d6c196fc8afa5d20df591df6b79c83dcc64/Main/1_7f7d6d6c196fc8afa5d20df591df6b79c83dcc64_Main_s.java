 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Main.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Author: Younghoon Jeon <yj2231@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Main
  */
 package edu.columbia.mipl;
 
 import java.io.*;
 import java.util.*;
 
 import edu.columbia.mipl.conf.*;
 import edu.columbia.mipl.syntax.*;
 import edu.columbia.mipl.codegen.*;
 import edu.columbia.mipl.runtime.*;
 import edu.columbia.mipl.runtime.execute.*;
 import edu.columbia.mipl.runtime.traverse.*;
 
 public class Main {
 	private static final String optString = "help;h version;v syntax;s interactive;i config;c: server;s: writer;w: output;o: remote;r";
 
 	public static void main(String[] args) {
 		//Parser parser = new Parser(new Program(new SemanticChecker(), new ProgramExecutor())); // Interactive Mode
 		//Parser parser = new Parser("test/input/multireturn.mipl", new Program(new SemanticChecker(), new ProgramExecutor())); // Interpreter Mode
 		//Parser parser = new Parser("test/input/multireturn.mipl", new SemanticChecker()); // CheckingOnly mode
 		
 		Configuration conf = Configuration.getInstance();
 
 		Map<String, String> optMap = new HashMap<String, String>();
 		int index = getOpt(args, optString, optMap);
 		if (index < 0) {
 			System.exit(1);
 		}
 		
 		// show usage
 		if (optMap.containsKey("help")) {
 			showUsage();
 			return;
 		}
 		
 		// show version
 		if (optMap.containsKey("version")) {
 			showVersion();
 			return;
 		}		
 		
 		// read configuration file
 		if (optMap.containsKey("config")) {			
 		}
 		
 		if (optMap.containsKey("server")) {
 			conf.addServer(optMap.get("server"));
 		}
 
 		if (optMap.containsKey("remote")) {
 			conf.setMode(Configuration.MODE_REMOTE);
 		}
 
 		if (optMap.containsKey("interactive")) {
 			// interactive mode
 			Parser parser = new Parser(new Program(new SemanticChecker(), new ProgramExecutor()));
 		}
 		else {
 			if (index >= args.length) {
 				System.out.println("index =" + index + " length = " + args.length);
 				showUsage();
 				System.exit(1);
 			}
 			
 			String srcName = args[index];
 			
 			Parser parser = new Parser(srcName);
 			if (parser.getNumError() != 0) {
				System.out.println("Parser Error");
 				System.exit(1);
 			}
 			
 			if (!parser.getProgram().traverse(new SemanticChecker())) {
 				System.out.println("SemanticChecker Error");
 				System.exit(1);
 			}
 			
 			// check only
 			if (optMap.containsKey("syntax")) {
 				// return here without doing something
 				return;
 			}
 		
 			if (!optMap.containsKey("output")) {
 				// interpreter mode
 				parser.getProgram().traverse(new ProgramExecutor());
 			}
 			else {
 				// compile mode				
 				String outName = optMap.get("output");
 				
 				// set writer
 				if (optMap.containsKey("writer"))
 					conf.setWriter(optMap.get("writer"));									
 				
 				parser.getProgram().traverse(new CodeGenerator("build", outName));
 			}
 		}
 
 		/*
 		// Compiling Mode
 		Parser parser = new Parser(args[0]);
 		if (parser.getNumError() != 0) {
 			System.out.println("Error on parsing input!");
 			return;
 		}
 		
 		if (!parser.getProgram().traverse(new SemanticChecker())) {
 			System.out.println("There are semantic errors!");
 			return;
 		}
 		
 		parser.getProgram().traverse(new CodeGenerator("build", "MiplProgram"));
 		*/
 		
 	}
 	
 	public static int getOpt(String[] args, String optStr, Map<String, String> optMap) {		
 		Set<String> optSingle = new HashSet<String>();
 		Set<String> optPair = new HashSet<String>();
 		
 		// (shortName, longName)
 		Map<String, String> shortNameTab = new HashMap<String, String>();
 		
 		String[] optStrList = optStr.split("[ \t]+");
 		for (String s : optStrList) {
 			Set<String> optTarget = optSingle;
 			if (s.endsWith(":")) {
 				optTarget = optPair;
 				s = s.substring(0, s.length() - 1);
 			}
 			
 			String[] tokens = s.split(";");
 			optTarget.add(tokens[0]);			
 			if (tokens.length == 2)
 				shortNameTab.put(tokens[1], tokens[0]);
 		}
 		
 		int i = 0;
 		while (i < args.length) {
 			String s = args[i];		
 			if (!s.startsWith("-"))
 				break;
 			
 			s = s.substring(1);
 			if (shortNameTab.containsKey(s))
 				s = shortNameTab.get(s);
 			
 			if (optSingle.contains(s))
 				optMap.put(s, null);
 			else if (optPair.contains(s)) {
 				i++;
 				if (i >= args.length)
 					return -1;
 				optMap.put(s, args[i]);				
 			}
 			else
 				return -1;
 			
 			i++;
 		}
 				
 		return i;
 	}
 	
 	public static void showUsage() {
 		System.err.println("Usage: java edu.columbia.cs.mipl.Mipl [options] input.mipl");
 		System.err.println("      [options] : " + optString);
 	}
 	
 	public static void showVersion() {
 		System.err.println("MIPL version 1.0.0");
 	}
 }
