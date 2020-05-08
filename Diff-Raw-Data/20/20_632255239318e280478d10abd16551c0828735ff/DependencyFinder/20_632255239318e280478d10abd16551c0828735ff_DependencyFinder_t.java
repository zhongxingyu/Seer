 /* Lists dependencies and classes for input java file. Calling function
  * responsible for all recursion and cyclical dependency resolution.
  *
  * Needs to handle both import statements and all members of package.
  *
  * (C) 2010 P.Hammer, A.Krebs, L. Pelka, P.Ponzeka
  */
 package xtc.oop;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.io.File;
 import java.io.IOException;
 							
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class DependencyFinder {
 
     public boolean verbose = true;
 
    /**
        *  filePaths holds canonical paths to file's dependencies
        *  fileClasses holds ClassStructs with info about each class in file
        */
     private ArrayList<FileDependency> fileDependencies = new ArrayList<FileDependency>();
     private ArrayList<ClassStruct> fileClasses = new ArrayList<ClassStruct>();
     private String currentPackage, currentSuperClass, currentFilePath, rootDirectory;
     private DependencyOrigin origin;
     private Node fileNode;
 
     // ignores all dependencies from following top-level superpackages:
     private final java.util.List<String> excludedPackages = Arrays.asList(new String[]
                     {"java","javax"});
 
 
     public DependencyFinder(Node n, String filePath) {
 
         currentFilePath = filePath;
         rootDirectory = (new File(currentFilePath)).getParent(); // all dependencies are relative to the translated file
         fileNode = n;
         currentPackage = "";
         currentSuperClass = "";
         origin = null;
 
         new Visitor() {
 
             public void visitPackageDeclaration(GNode g) {
 
                 // list all files in the directory, and add them to our paths list
                 Node n = g.getNode(1);
 
                 StringBuilder pathbuilder = new StringBuilder();
 
                 for (int i = 0; i < n.size(); i++) {
                     String breadcrumb = n.getString(i);
 
                     // if still part of the path
                     if (pathbuilder.length() > 0)
                         pathbuilder.append("/");
 
                     pathbuilder.append(breadcrumb);
                 }
 
                 String path  = pathbuilder.toString();
                 currentPackage = path.replace("/",".");
 
                 try {
                     for (int i=0; i<n.size(); i++) { // get ancestor directory until matches with package classpath
                         rootDirectory = (new File(rootDirectory)).getParent();
                     }
                     String fullPath = (new File(rootDirectory, path)).getCanonicalPath();
                     gatherDirectoryFiles(fullPath, DependencyOrigin.CURRENTPACKAGE);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
 
             public void visitImportDeclaration(GNode g) {
 
                 Node n = g.getNode(1);
 
                 if (excludedPackages.contains(n.getString(0))) {
                     return; // don't convert anything in java.*, javax.*, etc.
                 }
 
                 StringBuilder pathbuilder = new StringBuilder(); // build filename from recursing tree through its children
 
                 for (int i = 0; i < n.size(); i++) {
                     String breadcrumb = n.getString(i);
 
                     // if still part of the path
                     if (pathbuilder.length() > 0) {
                         pathbuilder.append("/");
                     }
 
                     pathbuilder.append(breadcrumb);
 
                     if (i == (n.size() - 1)) {
 						  
 			// if using the wildcard operator, visit the folder instead
                         // e.g. import A.B.*;
                         if (null != g.get(2) && g.getString(2).equals("*")) {
                             try {
                                 String fullPath = (new File(rootDirectory, pathbuilder.toString())).getCanonicalPath();
                                 gatherDirectoryFiles(fullPath, DependencyOrigin.IMPORTEDPACKAGE);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             return;
                         }
 								
                        	pathbuilder.append(".java");
                     }
                 }
 
                 String filename = pathbuilder.toString();
 
                 addPath(filename, DependencyOrigin.IMPORT);
             }
             public void visitClassDeclaration(GNode n){
                 String className = n.getString(1);
 				//currentSuperClass = "Object";
 		visit(n);
                 addClass(className, n);
 
                 // reset superclass to blank
                 currentSuperClass = "";
             }
 
             public void visitExtension(GNode n){
 		currentSuperClass = n.getNode(0).getNode(0).getString(0);
             }
 
 
             public void visit(Node n) {
                 for (Object o : n) {
                     if (o instanceof Node) {
                         dispatch((Node) o);
                     }
                 }
             } //end of visit method
         }.dispatch(n);
 
 		/* don't import current directory files... for now
         if (currentPackage.equals("")) {
             gatherDirectoryFiles((new File(currentFilePath)).getParent(), DependencyOrigin.CURRENTPACKAGE);
         
 		 }*/
     } // end of DependencyFinder
 
     /** used for second time we call DepFinder (in -translate) when we know the file's origin. */
     public DependencyFinder(Node n, FileDependency inputFile) {
         this(n, inputFile.fullPath);
         this.origin = inputFile.origin;
     }
 
     /* Adds all files in directory to filePaths  */
     private void gatherDirectoryFiles(String dirFullPath, DependencyOrigin origin) {
         File dir = new File(dirFullPath);
 
         /*
                 if (!dir.exists()) {
                     try {
                         String failsafe = Class.forName(fileClasses.get(0).className).getProtectionDomain().getCodeSource().getLocation().toString();
                         dir = new File(failsafe);
                     } catch (ClassNotFoundException x) { }
                 }
                 */
         if (dir.isDirectory()) {
             for (String fileName : dir.list()) {
                 try {
                     if (fileName.endsWith(".java")) {
                         //addPath(dirPath + '/' + fileName, origin);
                         addPath((new File(dir, fileName)).getCanonicalPath(), origin);
                     } /* else if (origin != DependencyOrigin.CURRENTDIRECTORY && // AK 12/18/10 8:00 don't recurse directories
                             !fileName.contains(".")) { // support for recursively calling subdirectories
                         try {
                             fileName = fileName.replaceAll("\\s", "\\ "); // escape all spaces so we don't make a bunch of new directories
                             File subDir = new File(dir, fileName);
                             if (subDir.isDirectory()) {
                                 gatherDirectoryFiles(subDir.getCanonicalPath(), origin);
                             }
                                 } catch (NullPointerException e) {
                                 }
                             } */
                 } catch (IOException i) { }
             }
         }
     }
 
     /*  add file path (as string) to file dependencies Map, using canonical path
         *  rather than absolute to avoid collisions                    */
        private void addPath (String fullPath, DependencyOrigin origin) {
            if (!fullPath.equals(currentFilePath)) // don't add self to dependencies
                fileDependencies.add(new FileDependency(fullPath, origin));
        }
 
     /**
         *   add class names (as ClassStruct object) to both class
         *   lists (all classes and file-specific classes)
         */
        void addClass (String className, GNode n) {
 
            ClassStruct c = new ClassStruct(currentFilePath, currentPackage,
                    className, currentSuperClass, fileDependencies, n, fileNode);
            fileClasses.add(c);
        }
 
 
         /**
            * @return all called dependent files of the current file including package and import
            */
         public ArrayList<FileDependency> getFileDependencies() {
             ArrayList<FileDependency> paths = new ArrayList<FileDependency>();
 
             for (FileDependency d : fileDependencies) {
                 // the FileDependency .equals() method only compares by file paths, which allows us to use the .contains() method of ArrayList
                 // we can then compare the dependency precedence to make sure we have dependencies correctly ordered and no duplicates
                 if (!paths.contains(d) ) {
                     paths.add(d);
                 } else {
                     switch (d.origin) {
                         case IMPORT:
                             if (paths.get(paths.indexOf(d)).origin.compareTo(DependencyOrigin.IMPORT) > 0) {
                                 paths.remove(d);
                                 paths.add(d);
                             }
                             break;
                         case IMPORTEDPACKAGE:
                             if (paths.get(paths.indexOf(d)).origin.compareTo(DependencyOrigin.IMPORTEDPACKAGE) > 0) {
                                 paths.remove(d);
                                 paths.add(d);
                             }
                             break;
                         case CURRENTPACKAGE:
                             if (paths.get(paths.indexOf(d)).origin.compareTo(DependencyOrigin.IMPORTEDPACKAGE) > 0) {
                                 paths.remove(d);
                                 paths.add(d);
                             }
                             break;
                         case CURRENTDIRECTORY:
                             break; // don't add it if we've explicitly added it already
                         case ROOTFILE:
                             throw new RuntimeException("at the root file, shouldn't be here??");
                         default:
                             throw new RuntimeException("in default. Should not happen!");
                     }
                 }
             }
 
             return paths;
         }
 
       /**
            * @return all paths to each explicitly called dependent file of the current file
            */
         public ArrayList<FileDependency> getFileImportDepPaths() {
             ArrayList<FileDependency> paths = new ArrayList<FileDependency>();
 
             for (FileDependency d : fileDependencies) {
                 if (d.origin == DependencyOrigin.IMPORT || d.origin == DependencyOrigin.IMPORTEDPACKAGE) {
                     paths.add(d);
                 }
             }
 
             return paths;
         }
 
         /**
            * @return all paths to each package-defined dependent file of the current file
            */
         public ArrayList<FileDependency> getFilePkgDepPaths() {
             ArrayList<FileDependency> paths = new ArrayList<FileDependency>();
 
             for (FileDependency d : fileDependencies) {
                 if (d.origin == DependencyOrigin.CURRENTPACKAGE || d.origin == DependencyOrigin.CURRENTDIRECTORY) {
                     paths.add(d);
                 }
             }
             return paths;
         }
 
         public String getPackageName() {
             return currentPackage;
         }
 	
 	/**
 	 * @return package in cpp syntax
 	 */
 	public String getNamespace(){
 		return currentPackage.replace("\\.","::");
 	}
 
         /** Get dependices sorted by specific origin (i.e. get just package
                 * imports or just includes) */
         public ArrayList<String> getCppIncludeDecs(ArrayList<ClassStruct> allClasses, DependencyOrigin importorigin) {
 
             ArrayList<String> files = new ArrayList<String>();
 
             if (origin != DependencyOrigin.ROOTFILE) { // add java_lang.h to all except origin file
                 files.add("#include \"java_lang.h\"");
             }
 
             for (FileDependency d : fileDependencies) {
                 /*if (d.importorigin == importorigin) {
                     switch (origin) {
                         case IMPORT:
                         case IMPORTEDPACKAGE: // we're importing all files in directory now, so these two checks not needed until we update Inheritancebuilder to handle implicit imports
                         case CURRENTPACKAGE: */
                if (d.origin != DependencyOrigin.CURRENTDIRECTORY &&
							 d.origin != DependencyOrigin.CURRENTPACKAGE ) { // we've updated so all files from same package are in one file in a namespace
 					 		String pack = getPackageName(allClasses, d.fullPath);
							if (!files.contains("#include \"" + hFileName(allClasses, d.fullPath, pack) + "\""))
	                    files.add("#include \"" + hFileName(allClasses, d.fullPath, pack) + "\"");
                 }
                     /*        break;
                         /*case IMPORTEDPACKAGE:
 	                          files.add("using " + currentPackage.replaceAll("\\.", "::") + ";");
 	                          break; 
                         case CURRENTPACKAGE:
                             files.add("namespace " + currentPackage.replaceAll("\\.", "::") + ";");
                             break;
                          */
 								/* case CURRENTDIRECTORY: 
 									????
 									break;
 								*/
                   /*  }
                 } */
             }
 
             if (files.size() == 0) { // if root file and no imports
                 files.add("#include \"java_lang.h\"");
             }
 
             return files;
         }
 
         /** Get using declarations for each explicitly imported dependency
                   * e.g. through import A.B.Foo;            */
         public ArrayList<String> getCppUsingDeclarations(ArrayList<ClassStruct> allClasses) {
 
             ArrayList<String> files = new ArrayList<String>();
 
             for (FileDependency d : fileDependencies) {
                 if (d.origin != DependencyOrigin.CURRENTDIRECTORY) { // for now, don't import from CURRENTDIRECTORY
                     // only add using if from different namespaces
                     if (!this.currentPackage.equals(getPackageName(allClasses, d.fullPath))) {
                         String pack = getPackageName(allClasses, d.fullPath);
                         for (ClassStruct c : getAllClassesInPackage(allClasses, pack)) {
									if (!files.contains("using " + qualifiedName(allClasses, c.className, d.fullPath, false) + ";"))
	                            files.add("using " + qualifiedName(allClasses, c.className, d.fullPath, false) + ";");
	                            files.add("using " + qualifiedName(allClasses, c.className, d.fullPath, true) + ";");
                         }
                     }
                 }
             }
 
             if (origin != DependencyOrigin.ROOTFILE || files.isEmpty()) { // if the root file but no included files
                 files.add("using java::lang::Object;");
                 files.add("using java::lang::__Object;");
                 files.add("using java::lang::Class;");
                 files.add("using java::lang::__Class;");
                 files.add("using java::lang::String;");
                 files.add("using java::lang::__String;");
                 files.add("using java::lang::__Array;");
                 files.add("using java::lang::ArrayOfInt;");
                 files.add("using java::lang::ArrayOfObject;");
                 files.add("using java::lang::ArrayOfClass;");
 				files.add("using java::lang::ArrayOfString;");
                 files.add("using java::lang::ArrayOfBoolean;");
                 files.add("using java::lang::ArrayOfFloat;");
                 files.add("using java::lang::ArrayOfDouble;");
                 files.add("using java::lang::ArrayOfShort;");
                 files.add("using java::lang::ArrayOfLong;");
                 files.add("using java::lang::ArrayOfChar;");
                 files.add("using __rt::java_cast;");
 
 				
             } else {
                 System.out.println("do something different for origin file?");
             }
 
             return files;
         }
 
         /** Get using declarations for each explicitly imported package dependency  *
                 *  e.g. through import A.B.*;       */
         public ArrayList<String> getCppUsingDirectives() {
 
             ArrayList<String> files = new ArrayList<String>();
             for (FileDependency d : fileDependencies) {
                 String namespace = getNamespace(fileClasses, d.fullPath);
                 if (d.origin == DependencyOrigin.IMPORTEDPACKAGE && 
                         !files.contains(namespace)) { // don't add duplicates
                     files.add(namespace);
                 }
             }
 
             return files;
         }
 
         public String getFilePath() {
             return currentFilePath;
         }
 
       /**
             * @return classes in the current file
             */
         public ArrayList<ClassStruct> getFileClasses() {
             return fileClasses;
         }
 
         /**
                 * @return "xtc.oop.Foo" --> ArrayList of "xtc", "oop", "Foo"
                 */
         public ArrayList<String> getPackageToNamespace() {	
 
             ArrayList<String> pack = new ArrayList<String>(Arrays.asList(currentPackage.split("\\.")));
             if (pack.get(0).equals("")) {
                 pack.remove(0);
             }//test for empty string element!!!
             return pack;
         }
 
         /** allows us to use Set .contains() method, compare by file path only */
         @Override
         public boolean equals (Object o) {
             if (o instanceof DependencyFinder) {
                 DependencyFinder other = (DependencyFinder)o;
                 return currentFilePath.equals(other.currentFilePath);
             }
             else {
                 Thread.dumpStack();
                 throw new RuntimeException("Trying to compare DependencyFinder with wrong object type");
             }
         }
 
         @Override
         public int hashCode() {
             return currentFilePath.hashCode();
         }
 
         public static String javaFileName(String fullPath, String packName) {
             String namespace = packName.replaceAll("\\.","_");
             return namespace; //+"_"+(new File(fullPath)).getName();
         }
 
         /** @return just name of file (ie ImportFile from ImportFile.java,
                       * used for cpp import headers */
         public static String cppFileName(ArrayList<ClassStruct> allClasses, String fullPath, String currentPackage) {
             String directory = getNamespaceDirectory(allClasses, fullPath, currentPackage);
             String basename = javaFileName(fullPath, currentPackage)+"_methoddef.cpp";
 
             if (directory.equals(""))
                 return basename;
             else
                 return directory + "/" + basename;
         }
         public String cppFileName(ArrayList<ClassStruct> allClasses){
             return cppFileName(allClasses, currentFilePath, currentPackage);
         }
         public String hFileName(ArrayList<ClassStruct> allClasses) {
             return hFileName(allClasses, currentFilePath, currentPackage);
         }
         public static String hFileName(ArrayList<ClassStruct> allClasses, String fullPath, String currentPackage) {
             String directory = getNamespaceDirectory(allClasses, fullPath, currentPackage);
             String basename = javaFileName(fullPath, currentPackage)+"_dataLayout.h";
 
             if (directory.equals(""))
                 return basename;
             else
                 return directory + "/" + basename;
         }
         public static String qualifiedName(ArrayList<ClassStruct> allClasses, String className, String fullPath, boolean useunderscores) {
             String namespace = getNamespace(allClasses, fullPath);
             //String basename = javaFileName(fullPath, packName).replace(".java","");
             String basename = className;
             if (useunderscores)
                 return namespace + "::__" + basename;
             else
                 return namespace + "::" + basename;
         }
 
         /** @return classes belonging to packname */
         public static ArrayList<ClassStruct> getAllClassesInPackage(ArrayList<ClassStruct> classes, String packname) {
             ArrayList<ClassStruct> packageClasses = new ArrayList<ClassStruct>();
 			if(classes ==null){
 				for (ClassStruct c : Translator.classes.keySet()) {
 					if (c.packageName.equals(packname))
 						packageClasses.add(c);
 				}
 			}
 			else{
 				for (ClassStruct c : classes) {
 					if (c.packageName.equals(packname))
 						packageClasses.add(c);
 				}
 			}
             return packageClasses;
         }
 
         public static ArrayList<String> getImports(ArrayList<ClassStruct> classes, String filename) {
             ArrayList<String> imports = new ArrayList<String>();
 
             // really ugly, but works....
             for (ClassStruct c : classes) {
                 if (c.filePath.equals(filename)) {
                     for (FileDependency f : c.fileDependencies)
                         if (!imports.contains(f.fullPath))
                             imports.add(f.fullPath);
                 }
             }
             return imports;
         }
 
         /** @return the package name for a specific file */
         public static String getPackageName(ArrayList<ClassStruct> classes, String filename) {
             for (ClassStruct c : classes) {
                 if (c.filePath.equals(filename)) {
                     return c.packageName;
                 }
             }
             throw new RuntimeException("no namespace found for " + filename);
         }
 
        /**
                * @return package name as using directive
                * i.e. package xtc.oop.B; becomes using namespace xtc::oop::B;
                */
         public static String getNamespace(ArrayList<ClassStruct> classes, String filename) {
             return getPackageName(classes,filename).replaceAll("\\.", "::");
         }
 
         /**
                * @return package name as relative directory location
                * i.e. package xtc.oop.B; becomes xtc/oop/B;,
                * or if e.g. xtc is rootPackage, becomes oop/B
                */
         public static String getNamespaceDirectory(ArrayList<ClassStruct> classes, String filename, String currentPackage) {
             for (ClassStruct c : classes) {
                 if (c.filePath.equals(filename)) {
                     String r = "";
                    if (!c.packageName.equals(c.rootPackage)) {
								r = c.packageName.replaceAll("\\.", "/");
						      r = r.replace(c.rootPackage + "/", "");
                     }
                     return r;
                 }
             }
             throw new RuntimeException("no namespace found for " + filename);
         }
 }
