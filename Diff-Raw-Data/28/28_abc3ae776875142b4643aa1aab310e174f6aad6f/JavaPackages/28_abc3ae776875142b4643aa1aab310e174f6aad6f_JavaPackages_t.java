 package translator;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.parser.ParseException;
 import xtc.util.Runtime;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.Reader;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 import java.util.Hashtable;
 import java.util.HashSet;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Provides a nice interface to Translator.  Rather than having everything in Translator, we come here
  * and do all of the processing.  This serves as a bridge from what the user wants to what needs to happen:
  * here is where the hard work begins.  We are given files to translate by Translator, and this takes care of
  * it.
  * 
  * Rather than creating a tree-like structure for our class heirachy, I'm going to
  * store everything in Hashtables so that we can do quick lookups based on names
  * alone, rather than having to traverse and find.  Everything in java (as with
  * any language), is represented by a string, so this is the quickest and easiest
  * way to store all our information.
  */
 class JavaPackages {
 	/**
 	 * The list of all known classes.
 	 * Class name -> Class Object
 	 */
 	private Hashtable<String, JavaClass> classes = new Hashtable<String, JavaClass>();
 	
 	/**
 	 * The list of all known files.
 	 * Filename -> File Object
 	 */
 	private Hashtable<String, JavaFile> files = new Hashtable<String, JavaFile>();
 	
 	/**
 	 * The list of all imported packages.
 	 * Name of package -> list of classes in that package
 	 */
 	private Hashtable<String, HashSet<String>> packages = new Hashtable<String, HashSet<String>>(); 
 	
 	/**
 	 * All of the packages that we have loaded in their entirety.
 	 * We can't rely on packages for this information because it is populated when even 1 class
 	 * is loaded, not when all are.
 	 */
 	public HashSet<String> loadedPackages = new HashSet<String>();
 	
 	/**
 	 * Don't allow creation of the class anywhere but inside itself.
 	 */
 	private JavaPackages() {
 		JavaStatic.pkgs = this;
 	}
 	
 	/**
 	 * Singleton so that only 1 can exist.
 	 */
 	private static JavaPackages instance = null;
 	
 	/**
 	 * Get the only instance of this class.
 	 */
 	public static JavaPackages getInstance() {
 		if (instance == null)
 			instance = new JavaPackages();
 		
 		return instance;
 	}
 	
 	/**
 	 * =============================================================================================
 	 * Methods for adding classes / files to our structure.
 	 */
 	 
 	/**
 	 * Adds a class (and its package) to the registry.
 	 *
 	 * @param cls The class to add to our registry.
 	 */
 	public void addClass(JavaClass cls) {
 		this.classes.put(cls.getName(), cls);
 		
 		//add the class to our package
 		if (!this.packageExists(cls.getPackageName()))
 			this.packages.put(cls.getPackageName(), new HashSet<String>());
 		
 		this.packages.get(cls.getPackageName()).add(cls.getName(false)); 
 	}
 	
 	/**
 	 * Adds a file to the registry.
 	 *
 	 * @param file The file to be added.
 	 */
 	public void addFile(JavaFile file) {
 		this.files.put(file.getName(), file);
 	}
 	
 	/**
 	 * =============================================================================================
 	 * Checkers to see if things exist.
 	 */
 	
 	/**
 	 * Tests if the class exists.
 	 *
 	 * @param cls The name of a class, in the "java.lang.Object" form.
 	 */
 	public boolean classExists(String cls) {
 		return this.classes.containsKey(cls);
 	}
 	
 	/**
 	 * Tests if the given file is registered (ie. exists).
 	 *
 	 * @param file The name of a file, in the "java.lang.Object" form.
 	 */
 	public boolean fileExists(String file) {
 		return this.files.containsKey(file);
 	}
 	
 	/**
 	 * Tests if the package exists.
 	 *
 	 * @param pkg The name of the package, in the "java.lang" form.
 	 */
 	public boolean packageExists(String pkg) {
 		return this.packages.containsKey(pkg);
 	}
 	
 	/**
 	 * =============================================================================================
 	 * Nice accessor methods for classes and files.
 	 */
 	
 	/**
 	 * Get a file object by a class it contains.
 	 *
 	 * @param path The name of a file, in the "java.lang.Object" form.
 	 */
 	public JavaFile getFile(String path) {
 		return this.files.get(path);
 	}
 	
 	/**
 	 * Get a class object by the name of a class.
 	 *
 	 * @param cls The name of a class, in the "java.lang.Object" form.
 	 */
 	public JavaClass getClass(String cls) {
 		return this.classes.get(cls);
 	}
 	
 	/**
 	 * Get a list of all the defined classes (as strings) contained in the given package.
 	 *
 	 * @param pkg The name of the package, in the "java.lang" form.
 	 */
 	public HashSet<String> getPackage(String pkg) {
 		return this.packages.get(pkg);
 	}
 	
 	/**
 	 * =============================================================================================
 	 * The printer.
 	 */
 	 
 	/**
 	 * Instructs every file to print itself.
 	 */
 	public void print() {
 		for (JavaFile f : this.files.values())
 			f.print();
 	}
 	
 	/**
 	 * =============================================================================================
 	 * Some extra utility stuff.
 	 */
 	
 	/**
 	 * Stuff to execute when we're done translating.
 	 */
 	public void wrapUp() {
 		/*
 		for (String cls : this.classes.keySet()) {
 			System.out.println("JavaPackges Class: " + cls + " -- " + this.classes.get(cls).getName());
 		}
 		
 		for (String f : this.files.keySet()) {
 			System.out.println("JavaPackges File: " + f + " -- " + this.files.get(f).getName());
 		}
 		*/
 	}
 	
 	/**
 	 * Given a package file name, it does its best to import that package file and add it to the translation.
 	 *
 	 * @param file A string in the format java.lang.Object for the file to import.
 	 *
 	 * @TODO - handle * imports
 	 */
 	public JavaFile importFile(String file) {
 		if (this.fileExists(file))
 			return this.getFile(file);
 		
 		//get a file path from our package name
		String jFile = file.replace("default.", "").replace(".", "/") + ".java";
 		
 		//see if we can find the file in our search path (given by the "-in" flag)
 		try {
 			File f = JavaStatic.runtime.locate(jFile);
 			
 			//if we get here, then we found our file, so let's throw it to the parser to make sure he gets imported properly
 			//so, we're going to restart the process, from the bottom-up, on the file we just found
			
 			//run through the imported file and everything it has
 			JavaFile javaFile = new JavaFile(f.toString(), JavaStatic.translator.parse(JavaStatic.runtime.getReader(f), f));
 			
 			return javaFile;
 		} catch (FileNotFoundException e) {
 			JavaStatic.runtime.error("File could not be located for import (in JavaPackages.importFile): " + file);
 			JavaStatic.runtime.exit(); //abort, we can't possibly go any further
 		} catch (IOException x) {
 			if (null == x.getMessage())
 				JavaStatic.runtime.error("I/O error");
 			else
 				JavaStatic.runtime.error(x.getMessage());
 		} catch (ParseException x) {
 			JavaStatic.runtime.error();
 			System.err.print(x.getMessage());
 		}
 		
 		return null; //silence, Java!
 	}
 	
 	/**
 	 * Loads a package when we're sure that it will be in the search path.
 	 *
 	 * @see translator#importMany(String, String)
 	 * 
 	 * @param pkg The name of the package to import, in "java.lang.Object" form.
 	 */
 	public void importMany(String pkg) {
 		this.importMany(pkg, null);
 	}
 	
 	/**
 	 * Loads a full package.  When we're loading the default package, we need the filename that caused the package
 	 * to load as "default" is not actually a path that we can just load.  If it's not the default package, then it 
 	 * should be able to find itself in the package search path.
 	 * 
 	 * Done with file as an extra parameter so as not to compromise look-ups for all imports (if we just passed in the 
 	 * file and hoped that it could resolve, it would fail: how do we resolve java.lang from some random test.pkg?).
 	 *
 	 * @param pkg The name of the package to import, in "java.lang.Object" form.
 	 * @param file The path to the file that caused the import (only important for "default" packages).
 	 */
 	public void importMany(String pkg, String file) {
 		//this is kinda janky, but "packages" can contain partially-loaded packages, so we can't rely on it
 		//thus, we needed another data structure to confirm when something has been through here (ie. completely loaded)
 		if (this.loadedPackages.contains(pkg))
 			return;
 		
 		//make sure we don't load the package again
 		this.loadedPackages.add(pkg);
 		
 		//the directory that contains the package
 		File dir = null;
 		
 		//if we're loading the default package
 		if (pkg == "default") {
 			file = new File(file).getAbsolutePath();
 			//attempt to remove the file name from the path
 			dir = new File(file.substring(0, file.lastIndexOf("/")));
 			
 			//if we failed at finding the package root
 			if (!dir.isDirectory())
 				dir = null;
 		} else {
 			//we need to look for a directory, and xtc.runtime.locate() doesn't do that
 			List<File> dirs = JavaStatic.runtime.getFileList(Runtime.INPUT_DIRECTORY); 
 		
 			//if no source dirs were specified on the command line
 			if (dirs == null)
 				return;
 			
 			//prepare to go to the filesystem
 			String jPkg = pkg.replace(".", "/");
 		
 			for (File root : dirs) {
 				dir = new File(root, jPkg);
 				if (dir.exists() && dir.isDirectory())
 					break;
 			}
 		}
 		
 		//we couldn't find the import directory...surely this is an error
 		if (dir == null) {
 			JavaStatic.runtime.error("Package could not be located for import (in JavaPackages.importMany): " + pkg);
 			JavaStatic.runtime.exit(); //abort, we can't possibly go any further
 		}
 		
 		//glob for our .java files to import
 		File[] jFiles = dir.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.indexOf(".java") > -1;
 			}
 		});
 		
 		//if we don't have any files...w/e, just return
 		if (jFiles == null)
 			return;
 		
 		//and import all our of files in this package
 		for (File f : jFiles) {
 			this.importFile(pkg + "." + f.getName().replace(".java", ""));
 		}
 	}
 }
