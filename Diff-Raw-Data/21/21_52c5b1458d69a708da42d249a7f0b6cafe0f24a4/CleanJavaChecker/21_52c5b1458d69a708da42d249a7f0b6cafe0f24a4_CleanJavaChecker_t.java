
 /*******************************************************************************
 * Copyright (c) 2013 Cesar Yeep.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the BSD 3-Clause License
  * ("New BSD" or "BSD Simplified") which accompanies this distribution,
  * and is available at
  * http://opensource.org/licenses/BSD-3-Clause
  * 
 * Contributors of CleanJavaChecker (frontend):
  *     Cesar Yeep - initial API and implementation
 *
 * The CleanJava checker frontend is a modified and extended version of the 
 * original JastAddJ Compiler that includes the following license statement:
 *
 * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
 * by the modified BSD License. You should have received a copy of the
 * modified BSD license with this compiler.
 * 
 * Copyright (c) 2005-2008, Torbjorn Ekman
 *		 2011	    Jesper qvist <jesper.oqvist@cs.lth.se>
 * All rights reserved.
 *******************************************************************************/
 package cleanJavaTools;
 
 import AST.*;
 import java.util.*;
 
 public class CleanJavaChecker extends Frontend {
 
 	/** ArrayList containing compilation error messages (simple version for testing purposes). */
 	private ArrayList<Problem> errorList = new ArrayList<Problem>();
 	
 	public static void main(String args[]) {
 		CleanJavaChecker checker = new CleanJavaChecker(args);
 		checker.getErrors();
 	}
 	
 	/** Main checker constructor. */
 	public CleanJavaChecker(String[] args)
 	{
 		checker(args);
 	}
 	
 	/** Get error list (used for testing purposes). */
 	public ArrayList<Problem> getErrors()
 	{
 		return errorList;
 	}
 	
 	/** Get error list in a string object (used for testing purposes). */
 	public String getErrorString()
 	{
 		String errorString = "";
 		
 		for(Iterator<Problem> it = errorList.iterator(); it.hasNext(); ){
 			errorString += it.next().toSimpleString();
 			if(it.hasNext())
 				errorString += "\n";
 		}
 		return errorString;
 	}
 	
 	/** Process compiler errors. */
 	protected void processErrors(Collection errors, CompilationUnit unit) {
 		System.err.println("Errors:");
 		for(Iterator<Problem> iter2 = errors.iterator(); iter2.hasNext(); ) {
 			Problem problem = iter2.next();
 			System.err.println(problem.toString());
 			errorList.add(problem);
 		}
     }
 	/** Process input file. Creates a new compilation unit. */
 	public void checker(String[] args) {
 		process(
 			args,
 			new BytecodeParser(),
 			new JavaParser() {
 				parser.JavaParser parser = new parser.JavaParser();
 
 				public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
 					return parser.parse(is, fileName);
 				}
 			}
 		);
 	}
 	
 	//Properties configuration from JastAddJ7
 	protected ResourceBundle resources = null;
 	protected String resourcename = "JastAddJ";
 	protected String getString(String key) {
 		if (resources == null) {
 			try {
 				resources = ResourceBundle.getBundle(resourcename);
 			} catch (MissingResourceException e) {
 				throw new Error("Could not open the resource " +
 						resourcename);
 			}
 		}
 		return resources.getString(key);
 	}
 	
 	//Version printing extension from Frontend
 	
 	public void printVersion() {
       System.out.println(name() + " Version " + version() + " Build " + build());
     }
 	
 	public void printLongVersion() {
       System.out.println(longName() + " " + url() + " Version " + version() + " Build " + build());
     }
 	
 	public void printUsage() {
       printLongVersion();
       System.out.println(
           "\nUsage: java " + name() + " <options> <source files>\n" +
           "  -verbose                  Output messages about what the compiler is doing\n" +
           "  -classpath <path>         Specify where to find user class files\n" +
           "  -sourcepath <path>        Specify where to find input source files\n" + 
           "  -bootclasspath <path>     Override location of bootstrap class files\n" + 
           "  -extdirs <dirs>           Override location of installed extensions\n" +
           "  -d <directory>            Specify where to place generated class files\n" +
           "  -help                     Print a synopsis of standard options\n" +
           "  -version                  Print version information\n"
           );
     }
 
 	public String name() { return getString("cjc.Name"); }
 	public String longName() { return getString("cjc.LongName"); }
 	public String version() { return getString("cjc.Version"); }
 	public String build() { return getString("cjc.Build"); }
 	public String url() { return getString("cjc.URL"); }
 }
