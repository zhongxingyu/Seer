 /**
  * Created on 30.09.2010
  */
 package edu.kit.asa.alloy2relsmt;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.kit.asa.alloy2relsmt.smt.ModelException;
 import edu.kit.asa.alloy2relsmt.smt.SMTFile;
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4.parser.ParseUtil;
 import edu.mit.csail.sdg.alloy4.parser.ParsedModule;
 
 /**
  * @author Ulrich Geilmann
  * @author Jonny Best
  */
 public class Main {
 
 	public static void main(String[] args) {
 		// create program object
 		Main main = new Main(args);
 		// check object and run
 		if (main.valid)
 			// run
 			main.doIt();
 	}
 	
 	private String input;
 	private String output;
 	private boolean force;
 	private String[] finiteSigs;
 	
 	private boolean valid;
 	private boolean relationalequality = false;
 	
 	/**
 	 * parse the command line arguments and set
 	 * the instance variable appropriate.
 	 * @param args
 	 * the command line arguments
 	 */
 	private Main (String[] args) {
 		input = null;
 		output = null;
 		force = false;
 		valid = false;
 		finiteSigs = null;
 
 		// initialize the pattern for the "finite" switch
 		Pattern finPat = Pattern.compile("^--finite=([^,]*(?:,[^,]+)*)$");
 		
 		// parse all args
 		for (int i = 0; i < args.length; i++) {
 			// parse the "force" switch
 			if (Pattern.matches("^(?:-f|--force)$",args[i])) {
 				force = true;
 				continue;
 			}
 			// parse the "finite" switch
 			Matcher m = finPat.matcher(args[i]);
 			if (m.matches()) {
 				// save finite signatures
 				finiteSigs = m.group(1).split(",");
 				continue;
 			}
 			// parse the switch for enabling relational equality
 			if(Pattern.matches("^--relationalequality$", args[i]))
 			{
 				relationalequality = true;
				continue;
 			}
 			// if no other switches have been found, assign input and output
 			if (input == null)
 				input = args[i];
 			else if (output == null)
 				output = args[i];
 		}
 		// probably does nothing
 		valid = true;
 	}
 	
 	/**
 	 * @return
 	 * true on success
 	 */
 	private boolean doIt() {
 		// parse our inputs from class var
 		ParsedModule mod = parse(input);
 		// unsuccessful parse will return null
 		if (mod == null)
 			return false;
 		// notify user about successful parse
 		System.out.println ("Successfully parsed "+input+" as "+mod.getModelName());
 		// translate the parsed file into key model
 		SMTFile key = translate (mod);
 		// unsuccessful translation will result in null
 		if (key == null)
 			return false;
 		System.out.println ("Model successfully translated.");
 		// write the translated model to disk
 		return write(key,output,input);
 	}
 	
 	/**
 	 * translate an Alloy model
 	 * @param module
 	 * the parsed Alloy model
 	 * @return
 	 * null if translation fails
 	 */
 	private SMTFile translate (ParsedModule module) {
 		// create and set up a new translator
 		Translator translator = new Translator(module);
 		// assume all signatures to be finite 
 		for (int i = 0; i < ((finiteSigs == null) ? 0 : finiteSigs.length); ++i) {
 			// finitizer may return false for invalid signatures
 			if (!translator.finitize(finiteSigs[i]))
 				System.err.println ("WARNING: Could not find signature "+finiteSigs[i]+" in the model.");
 		}
 		if (relationalequality) {
 			// if the user has told us to use relational Equality instead of '=', set it here  
 			translator.setUseRelationalEquality(relationalequality);
 			System.out.println("Using relational equality instead of '=' for relational formulas.");
 		}
 		// return the key model
 		try {
 			return translator.translate();
 		} catch (ModelException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * Parse an Alloy model
 	 * @param filename
 	 * the file name of the model to parse
 	 * @return
 	 * the parsed module on success, null otherwise
 	 */
 	private ParsedModule parse (String filename) {
 		// prevent empty file names
 		if(filename == null) {
 			System.err.println ("Parameter cannot be empty!");
 			return null;
 		}
 		// careful: only create files from real file names!
 		File f = new File (filename);
 		// always false on windows
 		if (!f.exists()) {
 			System.err.println (filename + " does not exist!");
 			return null;
 		}
 		// check if user selected a directory
 		if (!f.isFile()) {
 			System.err.println (filename + " does not denote a regular file!");
 			return null;
 		}
 		// check if we have a readable file (again, shouldn't be neccessary on windows)
 		if (!f.canRead()) {
 			System.err.println ("Can not read "+filename+"! Check permissions.");
 			return null;
 		}
 		// do some real work
 		try {
 			// use the alloy parser to get a model from this file
 			return ParseUtil.parseEverything_fromFile(null, null, filename);
 		} catch (Err e) {
 			// handle errors if any
 			System.err.println (e.dump());
 			return null;
 		}
 	}
 	
 	/**
 	 * write everything to file. I.e. the translation,
 	 * all referenced modules, and the theory files.
 	 * @param key
 	 * the translation
 	 * @param dest
 	 * the destination to write to. If this denotes a
 	 * directory, the same name as for the input used
 	 * (but with different suffix), otherwise the KeY
 	 * translation will be written to dest, and the
 	 * theory gets put in the same directory.
 	 * if dest is null, the directory of src is used.
 	 * @param src
 	 * the original model. used to determine filename
 	 * when dest denotes a directory.
 	 * @return
 	 * true on success, false otherwise
 	 */
 	private boolean write (SMTFile key, String dest, String src) {
 		// use the source file's parent directory or create a new destination file
 		File destFile = (dest == null) ? (new File(src).getAbsoluteFile().getParentFile()) : (new File (dest));
 		// write to this directory
 		File destDir;
 		// set destination directory and file if needed
 		if (destFile.isDirectory()) {
 			destDir = destFile;
 			String n = new File (src).getName();
 			destFile = new File (destDir,n.substring(0, n.length() > 4 && n.contains(".") ? n.length()- 4 : n.length())+".smt2");
 			if (destFile.isDirectory()) {
 				System.err.println (destFile + " is a directory!");
 				return false;
 			}
 		} else {
 			destDir = destFile.getParentFile();
 			if (destDir == null) {
 				System.out.println ("Failed to write model.");
 				return false;
 			}
 		}
 		// don't overwrite existing files if force switch off
 		if (destFile.exists() && !force) {
 			System.err.println ("Destination "+destFile+" already exists! Use -f to force overwrite.");
 			return false;
 		}
 		// handle IO permission error for file
 		if (destFile.exists() && !destFile.canWrite()) {
 			System.err.println ("Can not write "+destFile);
 			return false;
 		}
 		// handle IO permission error for dir
 		if (!destDir.canWrite()) {
 			System.err.println ("Can not write to "+destDir);
 			return false;			
 		}
 		
 		// do the real outputting
 		try {
 			// write KeY file
 			key.output(new FileOutputStream(destFile));
 			System.out.println ("Output written to "+destFile);
 			
 			return true;
 		} catch (IOException ioe) {
 			System.err.println (ioe.getMessage());
 			return false;
 		}
 	}
 
 }
