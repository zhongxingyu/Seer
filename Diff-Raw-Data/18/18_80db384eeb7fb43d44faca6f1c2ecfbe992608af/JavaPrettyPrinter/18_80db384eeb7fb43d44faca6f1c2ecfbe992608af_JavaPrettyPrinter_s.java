 /*
  * The JastAdd Extensible Java Compiler (http://jastadd.org) is covered
  * by the modified BSD License. You should have received a copy of the
  * modified BSD license with this compiler.
  * 
  * Copyright (c) 2005-2008, Torbjorn Ekman
  *		      2011, Jesper Öqvist <jesper.oqvist@cs.lth.se>
  * All rights reserved.
  */
 package cleanJavaTools;
  
 import AST.*;
 import java.io.*;
 import java.util.*;
 
 class JavaPrettyPrinter extends Frontend {
 	public static void main(String args[]) {
 	if(!compile(args))
 		System.exit(1);
 	}
 
 	public static boolean compile(String args[]) {
 		return new JavaPrettyPrinter().process(
 			args,
 			new BytecodeParser(),
 			new JavaParser() {
 				public CompilationUnit parse(java.io.InputStream is, String fileName) throws java.io.IOException, beaver.Parser.Exception {
 					return new parser.JavaParser().parse(is, fileName);
 				}
 			}
 		);
 	}
   
 	protected void processErrors(java.util.Collection errors, CompilationUnit unit) {
 		super.processErrors(errors, unit);
		System.out.println(unit.toString());
		System.out.println(unit.dumpTreeNoRewrite());
 	}
   
 	protected void processNoErrors(CompilationUnit unit) {
 		System.out.println(unit.toString());
 	}
 
 	protected ResourceBundle resources = null;
 	protected String resourcename = "JastAddJ";
 	protected String getString(String key) {
 		if (resources == null) {
 			try {
 				resources = ResourceBundle.getBundle(
 						resourcename);
 			} catch (MissingResourceException e) {
 				throw new Error("Could not open the resource " +
 						resourcename);
 			}
 		}
 		return resources.getString(key);
 	}
 
 	protected String name() { return getString("jastaddj.PrettyPrinter"); }
 	protected String version() { return getString("jastaddj.Version"); }
 }
