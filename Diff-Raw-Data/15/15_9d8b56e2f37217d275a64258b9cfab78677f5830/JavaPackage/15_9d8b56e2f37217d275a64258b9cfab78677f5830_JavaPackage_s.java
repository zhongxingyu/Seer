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
 import xtc.tree.Printer;
 import xtc.tree.Visitor;
 
 /**
  * The path to a package; operates on
  * both <code>PackageDeclaration</code> and
  * <code>ImportDeclaration</code> nodes as
  * we need all classes from a package even if
  * the file only explicitly imports one.
  *
  * @author Nabil Hassein
  * @author Thomas Huston
  * @author Mike Morreale
  * @author Marta Wilgan
  *
  * @version 2.1.1
  */
 public class JavaPackage implements Translatable {
 
   // The default package name
   final private static String DEFAULT = "$main";  
 
   // Mapping of package paths to the corresponding JavaPackage objects
   private static Map<String, JavaPackage> packages = new HashMap<String, JavaPackage>();
   
   // The parts of the package name
   private List<String> pkg;
 
   // Caches of the filename, namespace, and path
   private String filename, namespace, packagename, path;
 
   // The files in the package
   private List<JavaFile> files;
 
   // The main file for the package
   private JavaFile main;
 
 
   // =========================== Constructors =======================
   
   /**
    * Constructs the default package.
    */
   public JavaPackage() {
     // Set the filename to the default
     filename = DEFAULT;
 
     // Initialize the empty package set
     pkg = new ArrayList<String>();
 
     // Initialize the file list
     files = new ArrayList<JavaFile>();
   }
   
   /**
    * Constructs the package using a GNode.
    * 
    * @param n The package or import declaration node.
    */
   public JavaPackage(GNode n) {
     // Parse the package name
     pkg = new ArrayList<String>();
     int size;
     if (n.hasName("ImportDeclaration")) {
       if (null == n.get(2))
         size = n.getNode(1).size() - 1;
       else
         size = n.getNode(1).size();
     } else {
       size = n.getNode(1).size();
     }
     for (int i = 0; i < size; i++) {
       pkg.add(n.getNode(1).getString(i));
     }
 
     // Initialize the file list
     files = new ArrayList<JavaFile>();
   }
 
   /**
    * Creates the package using a list.
    *
    * @param s The package path as a list.
    */
   public JavaPackage(List<String> s) {
     // Use the list s as the package
     pkg = s;
 
     // Initialize the file list
     files = new ArrayList<JavaFile>();
   }
 
 
   // ============================ Get Methods =======================
 
   /**
    * Gets the package as a filename.
    *
    * @return The filename.
    */
   public String getFilename() {
     // Only create the filename string if it doesn't already exist
     if (null == filename)
       filename = getString("_");
     return filename;
   }
 
   /**
    * Gets the files in the package.
    *
    * @return The files.
    */
   public List<JavaFile> getFiles() {
     return files;
   }
 
   /**
    * Gets the package as a namespace.
    *
    * @return The namespace.
    */
   public String getNamespace() {
     // Only create the namespace string if it doesn't already exist
     if (null == namespace)
       namespace = getString("::");
     return namespace;
   }
   
   /**
    * Gets the package identifier.
    *
    * @return The package.
    */
   public List<String> getPackage() {
     return pkg;
   }
 
   /**
    * Gets the package as a Java package.
    *
    * @return The package name.
    */
   public String getPackagename() {
     // Only create the package string if it doesn't already exist
     if (null == packagename)
       packagename = getString(".");
     return packagename;
   }
 
   /**
    * Gets the package as a path.
    *
    * @return The package path.
    */
   public String getPath() {
     // Only create the path string if it doesn't already exist
     if (null == path)
       path = getString("/");
     return path;
   }
 
   /**
    * Joins together the parts of the package name
    * using the specified separator.
    *
    * @param sep The separator.
    *
    * @return The package string.
    */
   private String getString(String sep) {
     StringBuilder p = new StringBuilder();
     int size = pkg.size();
     for (int i = 0; i < size; i++) {
       p.append(pkg.get(i));
       if (i < size - 1)
         p.append(sep);
     }
     return p.toString();
   }
 
 
   // ============================ Set Methods =======================
 
   /**
    * Adds the specified file to the package.
    *
    * @param file The JavaFile to add.
    */
   public void addFile(JavaFile file) {
     files.add(file);
   }
 
   /**
    * Orders the files based on dependencies.
    */
   public void orderFiles() {
     List<JavaFile> old = files;
     files = new ArrayList<JavaFile>();
     for (JavaFile file : old) {
       order(file);
     }
   }
 
   /**
    * Recursively orders files based on dependencies.
    *
    * @param file The file to add to the list.
    */
   private void order(JavaFile file) {
     if (file.isMain())
       main = file;
     if (files.contains(file))
       return;
     if (file.getPublicClass().hasParent())
       order(file.getPublicClass().getParent().getFile());
     files.add(file);
   }
 
   // =========================== Other Methods ======================
 
   /**
    * Checks if the path of two packages is the same.
    *
    * @return <code>True</code> if the path is the same;
    * <code>false</code> otherwise.
    */
   public boolean equals(JavaPackage o) {
     return o.getPath().equals(getPath());
   }
 
 
   // ======================= Translation Methods ====================
 
   /**
    * Writes the C++ header for the package to
    * the specified output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translateHeader(Printer out) {
     // Get any imports
     Set<JavaPackage> using = new HashSet<JavaPackage>();
     for (JavaFile file : files) {
       Set<JavaPackage> imports = file.getImports();
       for (JavaPackage i : imports) {
         if (!i.getPath().equals(getPath()))
           using.add(i);
       }
     }
 
     // Include any imported headers
     out.pln("#pragma once").pln();
     out.pln("#include <iostream>");
     out.pln("#include <sstream>").pln();
     out.pln("#include \"include/java_lang.h\"");
     for (JavaPackage i : using) {
       out.p("#include \"").p(i.getFilename()).pln(".h\"");
     }
 
     // Declare namespaces being used
     out.pln().pln("using namespace java::lang;");
     for (JavaPackage i : using) {
       out.p("using namespace ").p(i.getNamespace()).pln(";");
     }
     out.pln();
 
     // Add the namespace
     for (String part : pkg) {
       out.indent().p("namespace ").p(part).pln(" {").incr();
     }
 
     // Declare the class structs
     for (JavaFile file : files) {
       for (JavaClass cls : file.getClasses()) {
         String className = cls.getName();
         out.indent().p("struct __").p(className).pln(";");
         out.indent().p("struct __").p(className).pln("_VT;");
         out.pln().indent().p("typedef __rt::Ptr<__").p(className).p("> ")
           .p(className).pln(";").pln();
       }
     }
 
     // Print header structs
     for (JavaFile file : files) {
       for (JavaClass cls : file.getClasses()) {
         cls.translateHeader(out).pln();
       }
     } 
 
     // Close the namespace
     for (int i = 0; i < pkg.size(); i++) {
       out.decr().indent().pln("}");
     }
 
     return out;
   }
 
   /**
    * Translates the body of the package and
    * writes it to the output stream.
    *
    * @param out The output stream.
    *
    * @return The output stream.
    */
   public Printer translate(Printer out) {
     // Include the header file
     out.p("#include \"").p(getFilename()).pln(".h\"").pln();
 
     // Print the array template specializations for the classes in this package
     out.pln("namespace __rt {").incr();
     for (JavaFile f : files) {
       for (JavaClass cls : f.getClasses()) {
         cls.translateArrayTemplate(out);
       }
     }
     out.decr().pln("}").pln();
 
     // Add the namespace
     for (String part : pkg) {
       out.indent().p("namespace ").p(part).pln(" {").incr();
     }
 
     // Print all the files in the package
     for (JavaFile f : files) {
      f.translate(out);
       out.pln();
     }
 
     // Close the namespace
     for (int i = 0; i < pkg.size(); i++) {
       out.decr().indent().pln("}");
     }
 
     // If this package contains the main file, print the main method here
     if (null != main) {
       out.pln("int main(int argc, char *argv[]) {").incr();
       out.indent().pln("__rt::Ptr<__rt::Array<String> > args = new __rt::Array<String>(argc-1);");
       out.indent().pln("for (int i = 1; i < argc; i++) {").incr();
       out.indent().pln("(*args)[i-1] = __rt::literal(argv[i]);");
       out.decr().indent().pln("}");
       out.indent();
       if (!getNamespace().equals(""))
         out.p(getNamespace()).p("::");
       out.p("__").p(main.getPublicClass().getName()).pln("::main$array1_String(args);");
       out.decr().pln("}");
     }
 
     return out;
   }
 
 
   // ========================== Static Methods ======================
 
   /**
    * Adds a package to the packages map.
    *
    * @param path The path to the package.
    * @param pkg The corresponding JavaPackage object.
    */
   public static void addPackage(String path, JavaPackage pkg) {
     packages.put(path, pkg);
   }
 
   /**
    * Gets the JavaPackage specified by the path.
    *
    * @param path The path to the package.
    * 
    * @return The corresponding JavaPackage object.
    */
   public static JavaPackage getJavaPackage(String path) {
     return packages.get(path);
   }
 
   /**
    * Gets the list of Java packages.
    *
    * @return The list of Java packages.
    */
   public static Set<String> getJavaPackageList() {
     return packages.keySet();
   }
 
 }
