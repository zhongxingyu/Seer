 /*
  * xtc - The eXTensible Compiler
  * Copyright (C) 2012 Robert Grimm
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
 
 package qimpp;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.PrintWriter;
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 import xtc.tree.Printer;
 
 import xtc.util.Tool;
 
 /**
  * A translator from (a subset of) Java to (a subset of) C++.
  *
  * @author QIMPP
  * @version 0.1
  */
 public class QimppTranslator extends Tool {
   
   Printer fileout;
   
   /** Create a new translator. */
   public QimppTranslator() {
     try {
       fileout = new Printer(new PrintWriter("testfiles/out.cc"));
     } catch(Exception e) {
       System.err.println("Couldn't open file to write out!");
       System.exit(1); 
     }
   }
 
   public String getName() {
     return "Java to C++ Translator";
   }
 
   public String getCopy() {
     return "(C) 2012 qimpp";
   }
 
   public void init() {
     super.init();
   }
 
   public void prepare() {
     super.prepare();
 
     // Perform consistency checks on command line arguments.
   }
 
   public File locate(String name) throws IOException {
     File file = super.locate(name);
     if (Integer.MAX_VALUE < file.length()) {
       throw new IllegalArgumentException(file + ": file too large");
     }
     return file;
   }
 
   public Node parse(Reader in, File file) throws IOException, ParseException {
     JavaFiveParser parser =
       new JavaFiveParser(in, file.toString(), (int)file.length());
     Result result = parser.pCompilationUnit(0);
     return (Node)parser.value(result);
   }
 
   public void process(Node node) {
     new Visitor() {
     
       public void visitCompilationUnit(GNode n) {
         fileout.pln("#include \"java_lang.h\"")
               .pln("#include <iostream>")
               .pln("using namespace java::lang;").pln().flush();
             visit(n);
       }
     
       public void visitNewClassExpression(GNode n) {
        if (n.getGeneric(2) != null) {
           if (n.getGeneric(2).getString(0).equals("Object")) {
            fileout.p("new __").p(n.getGeneric(2).getString(0)).p("(")
                   .flush();                
             }
           }
           visit(n);
           fileout.p(")").flush();
        }  
       
       public void visitDeclarators(GNode n) {
         fileout.p(n.getGeneric(0).getString(0)).p(" = ").flush();
         visit(n);
       }
       
       public void visitFieldDeclaration(GNode n) {
         if (n.getGeneric(1).getGeneric(0).getString(0).equals("Object")) {
           fileout.p("Object ").flush();
           visit(n);
           fileout.pln(";").flush();
         }
       }
     
       public void visitMethodDeclaration(GNode n) {
         if (n.getString(3) != null && n.getString(3).equals("main")) {
           fileout.p("int main(int argc, char **argv) {").pln().flush();
           visit(n);
           fileout.indent().pln("return 0;").pln("}").flush();
         }
         else {
           visit(n);
         }
       }
       
       public void visitCallExpression(GNode n) {
        if (n.getString(2) != null && n.getString(2).equals("println")) {
          fileout.indent().p("std::cout << ").flush();
           GNode args = n.getGeneric(3);
           GNode string_literal = args.getGeneric(0);
          String str = string_literal.getString(0);
           fileout.p(str).p(";").pln().flush();
         }
         visit(n);
       }
         
      public void visitClassDeclaration(GNode n) {
         // Send the class declaration to our header file - this is a hack, as we actually need to collect all the 
         // classes, and then send them to print out
         InheritanceManager i = new InheritanceManager();
         GNode qimppFormattedClassDeclaration = i.getQimppClassDeclaration(n);
           
         HeaderWriter w = new HeaderWriter();
         GNode[] classesForHeader = {qimppFormattedClassDeclaration};
         w.generateHeader(classesForHeader);
           
         visit(n);
       }
 
       public void visit(Node n) {
         for (Object o : n) if (o instanceof Node) dispatch((Node)o);
       }
 
     }.dispatch(node);
   }
 
   /**
    * Run the translator with the specified command line arguments.
    *
    * @param args The command line arguments.
    */
   public static void main(String[] args) {
     new QimppTranslator().run(args);
   }
 
 }
