 /* Lists dependencies and classes for input java file. Calling function
  * responsible for all recursion and cyclical dependency resolution.
  *
  * Needs to handle both import statements and all members of package.
  *
  * (C) 2010 P.Hammer, A.Krebs, L. Pelka, P.Ponzeka
  */
 package xtc.oop;
 
 
 import java.util.ArrayList;
 import java.io.File;
 import java.io.IOException;
 							
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class DependencyFinder {
 
     public boolean verbose = false;
 
    /**
        *  filePaths holds canonical paths to file's dependencies
        *  fileClasses holds ClassStructs with info about each class in file
        */
     private ArrayList<FileDependency> fileDependencies = new ArrayList<FileDependency>();
     private ArrayList<ClassStruct> fileClasses = new ArrayList<ClassStruct>();
     private String currentPackage, currentSuperClass, currentFilePath, currentParentDirectory;
 
     // ignores all dependencies from following top-level superpackages:
     private final java.util.List<String> excludedPackages = java.util.Arrays.asList(new String[]
                     {"java","javax"});
 
 
     public DependencyFinder(Node n, String filePath) {
 
         currentParentDirectory = (new File(filePath)).getParent(); // all dependencies are relative to the translated file
         currentFilePath = filePath;
         currentPackage = "";
         currentSuperClass = "";
 
         // good idea / bad idea?
         gatherDirectoryFiles(currentParentDirectory, DependencyOrigin.CURRENTDIRECTORY); // since current directory is treated almost like a package
 
         new Visitor() {
 
             public void visitPackageDeclaration(GNode g) {
 
                 // list all files in the directory, and add them to our paths list
 
                 Node n = g.getNode(1);
 
                 /* support for wildcards... later
                                if ( n.getString(n.size() -1 ).equals("*") )
                                n.remove(n.size());
                              */
 
 
                 StringBuilder pathbuilder = new StringBuilder();
 
                 for (int i = 0; i < n.size(); i++) {
                     String breadcrumb = n.getString(i);
 
                     // if still part of the path
                     if (pathbuilder.length() > 0)
                         pathbuilder.append("/");
 
                     pathbuilder.append(breadcrumb);
                 }
 
                 String pathname = pathbuilder.toString();
                 currentPackage = pathname.replace("/",".");
 
                 gatherDirectoryFiles(pathname, DependencyOrigin.PACKAGE);
             }
 
             public void visitImportDeclaration(GNode g) {
 
                 Node n = g.getNode(1);
 
                 if (excludedPackages.contains(n.getString(0))) {
                     return; // don't convert anything in java.*, javax.*, etc.
                 }
 
                 StringBuilder pathbuilder = new StringBuilder(); // build filename from recursing tree through its children
 
                 /*
                             // if using the wildcard operator, visit the folder instead
                             try {
                                     if (n.getString(n.getNode(1).size() -1 ).equals("*"))
                                             gatherDirectoryFiles(pathBuilder.toString());
                             return;
                             } catch (Exception e) {
                                 e.printStackTrace();
                             }
                              */
 
                 for (int i = 0; i < n.size(); i++) {
                     String breadcrumb = n.getString(i);
 
                     // if still part of the path
                     if (pathbuilder.length() > 0) {
                         pathbuilder.append("/");
                     }
 
                     pathbuilder.append(breadcrumb);
 
                     if (i == (n.size() - 1)) // || n.getNode(1).getString(n.getNode(1).size() -1).equals("java") ) EVENTUALLY NEEDS .java filename SUPPORT
                     {
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
     }
 
     /* Adds all files in directory to filePaths  */
     private void gatherDirectoryFiles(String dirPath, DependencyOrigin origin) {
         File dir = null;
 
         try {
             dir = new File(currentParentDirectory, dirPath);
         } catch (NullPointerException e) {
             System.out.println(dirPath + " could not be found. Exists?");
             Object.class.getProtectionDomain().getCodeSource().getLocation(); // failsafe
         }
 
         if (dir.exists()) {
             for (String fileName : dir.list()) {
                 if (fileName.endsWith(".java")) {
                     addPath(dirPath + '/' + fileName, origin);
                 }
             }
         }
     }
 
     /*  add file path (as string) to file dependencies Map, using canonical path
         *  rather than absolute to avoid collisions                    */
        private void addPath (String path, DependencyOrigin origin) {
 
            try {
               path = (new File(currentParentDirectory, path)).getCanonicalPath();
            } catch (IOException e) { }
 
            fileDependencies.add(new FileDependency(path, origin));
        }
 
     /**
         *   add class names (as ClassStruct object) to both class
         *   lists (all classes and file-specific classes)
         */
        void addClass (String className, GNode n) {
 
            ClassStruct c = new ClassStruct(currentFilePath, currentPackage,
                             className, currentSuperClass, fileDependencies, n);
            fileClasses.add(c);
        }
 
 
       /**
            * @return all paths to each dependent file of the current file
            */
         public ArrayList<String> getFileDependencyPaths() {
             ArrayList<String> paths = new ArrayList<String>();
 
             for (FileDependency d : fileDependencies) {
                 paths.add(d.fullPath);
             }
 
             return paths;
         }
 
         public String getPackageName() {
             return currentPackage;
         }
 
         /* returns list of cpp-formatted dependencies, including
                * proper syntax for easy printing */
         public ArrayList<String> getCppDependencies() {
 
             ArrayList<String> files = new ArrayList<String>();
 
             for (FileDependency d : fileDependencies) {
                 switch (d.origin) {
                     case IMPORT:
                         files.add("#include " + d.cppFileName());
 								break;
                     case PACKAGE:
                         files.add("namespace " + currentPackage);
 								break;
                     /* case CURRENTDIRECTORY:
                                         files.add(d.cppFileName()); // needed? If no package specified, default to same "" package
 													 break; */
                 }
             }
 
             return files;
         }
 
         /* Get dependices sorted by specific origin (i.e. get just package
                * imports or just includes) */
         public ArrayList<String> getCppDependencies(DependencyOrigin origin) {
 
             ArrayList<String> files = new ArrayList<String>();
             
             for (FileDependency d : fileDependencies) {
                 switch (origin) {
                     case IMPORT:
                         files.add("#include \"" + d.hFileName() + "\"");
 								break;
                     case PACKAGE:
                         files.add("namespace " + "package name"); // FIX THIS
 								break;
                 }
             }
 
             return files;
         }
 
         /** use static method to return all imports */
         public ArrayList<String> getImports(ArrayList<ClassStruct> classes) {
             return DependencyFinder.getImports(classes, currentFilePath);
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
            return new ArrayList<String>(java.util.Arrays.asList(currentPackage.split("\\.")));
         }
 
         /** allows us to use Set .contains() method, compare by file path only */
         public boolean equals (DependencyFinder other) {
             return this.currentFilePath.equals(other.currentFilePath);
         }
 
         public boolean equals (String otherPath) {
             return this.currentFilePath.equals(otherPath);
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
 
         /**
                * @return package name as using directory
                * i.e. package xtc.oop.B; becomes using namespace xtc::oop::B;
                */
         public static String getNamespace(ArrayList<ClassStruct> classes, String filename) {
             for (ClassStruct c : classes) {
                 if (c.filePath.equals(filename)) {
                     return "using namespace " + c.packageName.replaceAll(".", "::") + ";";
                 }
             }
             assert false; // should never reach here
             return "";
         }
 
         public static ArrayList<String> getUsingDeclarations(ArrayList<ClassStruct> classes, String filename) {
             ArrayList<String> usings = new ArrayList<String>();
 
             for (ClassStruct c : classes) {
                 if (c.filePath.equals(filename)) {
                     usings.add( c.packageName.replaceAll(".", "::")
                             + "::" + c.className + ";" );
                 }
             }
             return usings;
         }
 }
