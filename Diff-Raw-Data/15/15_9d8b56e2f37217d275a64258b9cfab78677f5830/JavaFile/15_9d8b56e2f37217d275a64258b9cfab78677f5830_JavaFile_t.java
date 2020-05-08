 /*
  * pcp - The Producer of C++ Programs
  * Copyright (C) 2011 Nabil Hassein, Thomas Huston, Mike Morreale, Marta Wilgan
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package pcp.translator;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Printer;
 import xtc.tree.Visitor;
 
 /**
  * A Java file.
  *
  * @author Nabil Hassein
  * @author Thomas Huston
  * @author Mike Morreale
  * @author Marta Wilgan
  *
  * @version 2.1.1
  */
 public class JavaFile extends Visitor implements Translatable {
   
   // A map from file names to the corresponding file objects
   private static Map<String, JavaFile> files = new HashMap<String, JavaFile>();
 
   // A list of all classes in this file
   private List<JavaClass> allClasses;
 
   // The imported packages
   private Set<JavaPackage> imports;
 
   // Whether this is the main file specified by the user
   private boolean isMain;
 
   // The package this file is in
   private JavaPackage pkg;
 
   // The public class in this file
   private JavaClass publicClass;
 
 
   // =========================== Constructors =======================
   
   /**
    * Create a new Java file.
    * 
    * @param n The AST node.
    */
   public JavaFile(GNode n) {
     // Initialize the classes list
     allClasses = new ArrayList<JavaClass>();
 
     // Initialize the imports set
     imports = new HashSet<JavaPackage>();
 
     // Dispatch over the nodes in the file
     for (Object o : n) {
       if (o instanceof Node) {
         dispatch((Node) o);
       }
     }
 
     // If the file has no package, set it to the default package
     if (null == pkg) {
       if (null != JavaPackage.getJavaPackage(""))
         pkg = JavaPackage.getJavaPackage("");
       else
         JavaPackage.addPackage("", pkg = new JavaPackage());
     }
 
     // Add this file to its package
     pkg.addFile(this);
 
     // Add the public class to the classes map
     for (JavaClass c : allClasses) {
       JavaClass.addClass(pkg.getPath() + c.getName(), c);
     }
   }
 
 
   // ============================ Get Methods =======================
   
   /**
    * Gets a list of all the classes in the file.
    *
    * @return The classes.
    */
   public List<JavaClass> getClasses() {
     return allClasses;
   }
 
   /**
    * Gets a list of the import declarations.
    *
    * @return The imports.
    */
   public Set<JavaPackage> getImports() {
     return imports;
   }
 
   /**
    * Gets the package.
    * 
    * @return The package.
    */
   public JavaPackage getPackage() {
     return pkg;
   }
   
   /**
    * Gets the public class.
    *
    * @return The main class.
    */
   public JavaClass getPublicClass() {
     return publicClass;
   }
 
   /**
    * Checks if this file contains the main method
    * for the program.
    *
    * @return <code>True</code> if it contains the main method;
    * <code>false</code> otherwise.
    */
   public boolean isMain() {
     return isMain;
   }
 
 
   // ============================ Set Methods =======================
 
   /**
    * Orders classes in by dependencies.
    */
   public void orderClasses() {
     List<JavaClass> old = allClasses;
     allClasses = new ArrayList<JavaClass>();
     for (JavaClass cls : old) {
       order(cls);
     }
   }
 
   /**
    * Recursively orders classes based on dependencies.
    *
    * @param cls The class to add to the list.
    */
   private void order(JavaClass cls) {
     if (allClasses.contains(cls))
       return;
    if (cls.hasParent() && cls.getParent().getFile() == this)
       order(cls.getParent());
     allClasses.add(cls);
   }
 
   /**
    * Marks this as the main file for the program.
    */
   public void setMain() {
     isMain = true;
   }
 
 
   // ======================== Visit Methods =========================
   
   /**
    * Visits a ClassDeclaration node and adds it to the
    * list in the classes hash for its visibility.
    * 
    * @param n The AST node to visit.
    */
   public void visitClassDeclaration(GNode n) {
     // Create the class
     JavaClass c = new JavaClass(n, this);
 
     // Add the class to the list
     allClasses.add(c);
 
     // Check if this is the public class
     JavaVisibility v = c.getVisibility();
     if (v == JavaVisibility.PUBLIC) {
       if (null == publicClass)
         publicClass = c;
       else
         pcp.Translator.errConsole.pln("Multiple public classes in one file").flush();
     }
   }
   
   /**
    * Visits an ImportDeclaration node and adds it to
    * the imports list.
    *
    * @param n The AST node to visit.
    */
   public void visitImportDeclaration(GNode n) {
     // Parse the import declaration
     JavaPackage imp = new JavaPackage(n);
     String pkgpath = imp.getPath();
 
     // If the package has already been created, use the existing one
     if (null != JavaPackage.getJavaPackage(pkgpath))
       imp = JavaPackage.getJavaPackage(pkgpath);
 
     // Otherwise add the package to the packages hash
     else
       JavaPackage.addPackage(pkgpath, imp);
 
     // Add the package to the imports set
     imports.add(imp);
   }
   
   /**
    * Visits a PackageDeclaration node and sets it as
    * the unit package.
    * 
    * @param n The AST node to visit.
    */
   public void visitPackageDeclaration(GNode n) {
     // Parse the package
     pkg = new JavaPackage(n);
     String pkgpath = pkg.getPath();
 
     // If the package has already been created, use the existing one instead
     if (null != JavaPackage.getJavaPackage(pkgpath))
       pkg = JavaPackage.getJavaPackage(pkgpath);
 
     // Otherwise add the package to the hash
     else
       JavaPackage.addPackage(pkgpath, pkg);
   }
 
 
   // ===================== Translation Methods ======================
 
   /**
    * Translates the classes in the file and
    * writes them to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translate(Printer out) {
     for (JavaClass cls : allClasses) {
       cls.translate(out).pln();
     }
     return out;
   }
 
 
   // ========================== Static Methods ======================
 
   /**
    * Adds a file to the files map.
    *
    * @param path The path to the file.
    * @param file The corresponding JavaFile object.
    */
   public static void addFile(String path, JavaFile file) {
     files.put(path, file);
   }
 
   /**
    * Gets the JavaFile specified by the filepath.
    *
    * @param path The path to the file.
    * 
    * @return The corresponding JavaFile object.
    */
   public static JavaFile getJavaFile(String path) {
     return files.get(path);
   }
 
   /**
    * Gets the list of Java files.
    *
    * @return The list of Java files.
    */
   public static Set<String> getJavaFileList() {
     return files.keySet();
   }
   
 }
