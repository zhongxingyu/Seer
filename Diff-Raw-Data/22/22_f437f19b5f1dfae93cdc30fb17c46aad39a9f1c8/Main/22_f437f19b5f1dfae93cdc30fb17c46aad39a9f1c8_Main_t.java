 import java.io.*;
 import tree.*;
 import error.*;
 import output.*;
 import preprocessor.*;
 import semantic.*;
 
 public class Main {
 	public static void main(String[] args) {
 		String supportPath = null;
 		String installPath = null;
 		File cyborgFile = new File(args[0]).getAbsoluteFile();
 		for (int i=0; i<args.length-1; i++) {
 			if (args[i].equalsIgnoreCase("-support") || args[i].equalsIgnoreCase("-s")) {
 				supportPath = args[i+1];
 			}
 			if (args[i].equalsIgnoreCase("-install") || args[i].equalsIgnoreCase("-i")) {
 				installPath = args[i+1];
 			}
 		}
 		if (supportPath == null)
 			supportPath = "../support/"; // TODO this will change
 		if (installPath == null)
 			installPath = cyborgFile.getParent() + "/";
 		
 		System.out.println("cyborg file: " + cyborgFile);
 		System.out.println("config path: " + supportPath);
 		System.out.println("install path: " + installPath);
 		
 		if (!cyborgFile.exists()) {
 			System.err.println(cyborgFile.toString() + " does not exist.");
 			System.exit(0);
 		}
 
 		// Create SemanticManager to initialize members
 		SemanticManager sm = new SemanticManager(supportPath);
 		
 		// Preprocessing
 		Preprocessor preprocessor = new Preprocessor(cyborgFile);
 		try {
 			preprocessor.processFile();
 		} catch (IOException e) {
 			System.out.println("Error preprocessing file: " + e.getMessage());
 		}
 		// Tester System.out.println(preprocessor.getStringFile());
 		
 		// Lexer
 		Lexer scanner = null;
 		AppNode root = null;
 		try {
 			scanner = new Lexer(new ByteArrayInputStream(preprocessor.getStringFile().getBytes()));
 		} catch (Exception e) {
 			System.err.println("Error on input stream.");
 			System.exit(1);
 		}
 		try {
 			parser p = new parser(scanner); // We need parser.java
 			root = (AppNode)p.parse().value;
 			System.out.println(root);
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 			System.exit(1);
 		}
 
 		TreePrinter tp = new TreePrinter(root, "../outputTree");
 		tp.print();
 		
 		// Front End
 		root.traverse(0, null);
 		SemanticManager.analysisDone();
 		root.traverse(0, null);
 		
 		// Convert to .apk
 		
 		// Install to phone path
 	}
 }
