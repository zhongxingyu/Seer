 /*
  * Object-Oriented Programming
  * Copyright (C) 2010 Robert Grimm
  * edits (C) 2010 P.Hammer, A.Krebs, L. Pelka, P.Ponzeka
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * version 2 as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  */
 package xtc.oop;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 import java.util.HashMap;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.util.Tool;
 
 /**
  * A translator from (a subset of) Java to (a subset of) C++.
  *
  * @author P.Hammer
  * @author A.Krebs
  * @author L. Pelka
  * @author P.Ponzeka
  * @version 1
  */
 public class Translator extends Tool {
 
 	File inputFile = null;
         HashMap<FileDependency,Boolean> allDependencies;
         HashMap<ClassStruct,Boolean> classes;
         String rootPackage;
 
 	/** Create a new translator. */
 	public Translator() {
 		
 	}
 
         public Translator (HashMap<FileDependency,Boolean> dependencies,
                     HashMap<ClassStruct,Boolean> classes) {
             this();
             this.allDependencies = dependencies;
             this.classes = classes;
         }
 
 	public String getCopy() {
 		return "(C) 2010 P.Hammer, A.Krebs, L. Pelka, P.Ponzeka";
 	}
 
 	public String getName() {
 		return "Java to C++ Translator";
 	}
 
 	public String getExplanation() {
 		return "This tool translates a subset of Java to a subset of C++.";
 	}
 
 	public void init() {
 		super.init();
 		
 		// Declare command line arguments.
 		runtime.
 			bool("printJavaAST", "printJavaAST", false,
 				 "Print the Java AST at the end of a translation.").
 			bool("countMethods", "optionCountMethods", false,
 				 "Print the number of method declarations.").
 			bool("translate", "translate", false,
 				 "Translate .java file to c++.").
 			bool("finddependencies", "finddependencies", false,
 				 "find all classes we need to translate").	
 			bool("testing","testing",false,"Run some Test cases.").
 			bool("tester","tester",false,"Run tester Class").
 			bool("test","test",false,"Run some Test cases.");	}
 
 	public void prepare() {
 		super.prepare();
 
 		// Perform consistency checks on command line arguments.
 	}
 
 	public File locate(String name) throws IOException {
 		File file = super.locate(name);
 		if (Integer.MAX_VALUE < file.length()) {
 			throw new IllegalArgumentException(file + ": file too large");
 		}
 		inputFile = file;
 		//System.out.println("using this method");
 		return file;
 	}
 
 	public Node parse(Reader in, File file) throws IOException, ParseException {
 		JavaFiveParser parser =
 			new JavaFiveParser(in, file.toString(), (int)file.length());
 		Result result = parser.pCompilationUnit(0);
 		return (Node)parser.value(result);
 	}
 	/**
 	 * helper method test how many classes left to translate at any moment
 	 */
 	public int numFalse() {
 		int numRemaining = 0;
 		for (Boolean b : classes.values()) {
 			if (!b)numRemaining++;
 		}
 		return numRemaining;
 	}
 
 	//-----------------------------------------------------------------------
 	public void process(Node node) {
 		final boolean VERBOSE = runtime.test("optionVerbose");
 		final boolean SILENT  = runtime.test("optionSilent");
 		
 		//Some Testing Environments
 		if(runtime.test("testing"))
 			{
 				runtime.console().p("Testing...").pln().flush();
 			
 				/*Create a new visitor to visit the CompilationUnit */
 				new Visitor(){
 					public void visitBlock(GNode n)
 					{
 						CppPrinter print= new CppPrinter(n,true);
 						//print.DEBUG=true;
 						
 					}
 					public void visit(Node n)
 					{
 						for(Object o:n) {
 							if(o instanceof Node) dispatch((Node) o);
 						
 						}
 					}
 				}.dispatch(node);
 			
 			}
 		/*
 		 * runtime option TRANSLATE 
 		 *
 		 */
 		if (runtime.test("translate")) {
 
 				
 			if (VERBOSE) {
 					runtime.console().p("Begining translation...").pln().flush();
 				}
                  
 			String fullPathName = "";
 			try { fullPathName = inputFile.getCanonicalPath(); }
 			catch (IOException e) { }
 
          // need the original file to avoid circular imports and give it base import file
 			allDependencies.put(new FileDependency(fullPathName, DependencyOrigin.ROOTFILE), true);
                         
 			// recursively find dependencies from input file
 			Translator t = new Translator(allDependencies, classes);
 			t.run(new String[]{"-no-exit", "-finddependencies", fullPathName});
 			classes = t.classes;
 			allDependencies = t.allDependencies;
 
                         // set all ClassStruct's root packages to root package
                         for (ClassStruct c : classes.keySet()) {
                             c.rootPackage = t.rootPackage;
                         }
 
 			for(FileDependency d: allDependencies.keySet()) System.out.println(d+" "+d.fullPath);
 			for(ClassStruct c: classes.keySet()) System.out.println(c+" "+c.className);
 					  
 			//creates tree root a.k.a. the Object class
 			final InheritanceTree Object = new InheritanceTree();
 
 			//creates the Class class as subclass of Object class
 			final InheritanceTree Class = new InheritanceTree(Object,"Class");
 
 			//creates the String class as a subclass of Object class
 			final InheritanceTree String = new InheritanceTree(Object,"String");
 					 
 			//---- creates all InheritanceTrees ----
 			int leftTotranslate = classes.size();
                         while (classes.containsValue(false)) {
                             for (ClassStruct c : classes.keySet()) {
                                 if (classes.get(c) == false) {
                                     if (c.superClass.equals("")) {//*** extends object
                                         new InheritanceTree(c.getPackage(), c.classNode, Object);
                                         classes.put(c, true);
                                     } else {
                                         InheritanceTree superclass = Object.search(c.getPackage(), c.superClass);
                                         if (superclass != null) {//**extends an already translated class
                                             new InheritanceTree(c.getPackage(), c.classNode, superclass);
                                             classes.put(c, true);
                                         }
                                     }
                                 }
                             }
 				
 				if (leftTotranslate == numFalse()) System.out.println("infiniteloop");//**infiniteloop test
 				leftTotranslate = classes.size();//**update for infiniteloop
 			}
 					 
 			//----- creates all InheritanceBuilders
                         InheritanceBuilder inherit;
                         boolean superiswritten =true;
                         LinkedList<ClassStruct> editablelist;
                         int index;
                         for (FileDependency d: allDependencies.keySet()){
 							DependencyFinder dep = new DependencyFinder(getNodeFromFilename(d.fullPath), d.fullPath);
 							editablelist = new LinkedList<ClassStruct>(dep.getFileClasses());
 							//inheritancebuilder takes the Files dependencyfinder and arraylist of the ClassStructs
 							inherit = new InheritanceBuilder(dep, new ArrayList<ClassStruct>(classes.keySet()));
 							int num_classes = editablelist.size();
 							while(num_classes!=0){
 									for (int i=0;i< num_classes;i++){
 										ClassStruct c = editablelist.get(i);
 										superiswritten =true;
 										if( c.superClass.equals("")){//*** extends object
 												inherit.addClassdef(Object.search(c.getPackage(),c.className));
 												editablelist.remove(c);
 										}//end of if check
 										else{
 											for (ClassStruct check: editablelist){
 												if(c.superClass.equals(check.className))
 													superiswritten = false;
 											}
 											if (superiswritten){//**extends an already written class
 												inherit.addClassdef(Object.search(c.getPackage(),c.className));
 												editablelist.remove(c);
 											}
 										}//end of else check
 										num_classes = editablelist.size();
 									}//end of for loop
 									
 								}//end of while
 								try{inherit.close();}
 								catch(Exception e){}
 						}
 						
                    
						if (runtime.test("printJavaAST")) {
 							runtime.console().format(node).pln().flush();
 						}
 		}//end of runtime.test("Translate") test
 //-----------------------------------------------------------------------
                 /* find dependencies of a single file, recursively calling until whole dependency topology is filled */
 		if (runtime.test("finddependencies")) {
                     String fullPathName = "";
                     try { fullPathName = inputFile.getCanonicalPath(); }
                     catch (IOException e) { }
 
                     DependencyFinder depend = new DependencyFinder(node, fullPathName);
                     if (allDependencies.containsKey(new FileDependency(fullPathName, DependencyOrigin.ROOTFILE))) { // if we're translating the root file, set its package as the root package
                         rootPackage = depend.getPackageName();
                     }
                     
                     for (ClassStruct c : depend.getFileClasses()) {
                         classes.put(c, false);
                     }
 
                     Translator t = null;
 
                     // add all explicitly imported files first before adding package to maintain precedence
                     for ( FileDependency f : depend.getFileDependencies() ) {
                         // only translate if not translated. get returns boolean
                         // telling whether the file has been translated
                         if ( !containsKey(allDependencies, f.fullPath) || !get(allDependencies, f.fullPath) ) {
                             allDependencies.put(f, true);
                             t = new Translator(allDependencies, classes);
                             t.run( new String[] {"-no-exit", "-finddependencies", f.fullPath});
                             allDependencies.putAll(t.allDependencies);
                         }
                     }
 		}
 	}//end of process method
 
         /** helper methods for translator */
 
         /** @return arbitrary node from any class belonging to filename,   */
         public Node getNodeFromFilename(String filename) {
             for (ClassStruct c : classes.keySet()) {
                 if (c.filePath.equals(filename))
                     return c.fileNode;
             }
             throw new RuntimeException("can't find any classes belonging to " + filename);
         }
 
         /** stripped-down version of HashMap containsKey() and get() so we can search by file name  */
         public static boolean containsKey (HashMap<FileDependency,Boolean> allDependencies,
                                     String filename) {
             for (FileDependency f : allDependencies.keySet()) {
                 if (f.fullPath.equals(filename))
                     return true;
             }
             return false;
         }
 
         public static boolean get (HashMap<FileDependency,Boolean> allDependencies, String filename) {
             for (FileDependency f : allDependencies.keySet()) {
                 if (f.fullPath.equals(filename))
                     return allDependencies.get(f);
             }
             throw new RuntimeException("Not in dependencies list. Bad");
         }
 
     /**
 	 * Run the translator with the specified command line arguments.
 	 *
 	 * Uses xtc.util.tool run();
 	 * @param args The command line arguments.
 	 */
 	public static void main(String[] args) {
 
 		HashMap<FileDependency,Boolean> dependencies = new HashMap<FileDependency,Boolean>();
 		HashMap<ClassStruct,Boolean> classes = new HashMap<ClassStruct,Boolean>(); 
 		new Translator(dependencies, classes).run(args);
 
 	}	
 }//end of Translator.java
 
