 package main;
 
 import grammar.JavaLexer;
 import grammar.JavaParser;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.StringTokenizer;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 
 import walkers.CommonTreePackage;
 
 /**
  * This class reads all java source files in the folder "javaprojectsources"
  * and generates a collection of CommonTreePackages 
  */
 public class GenerateTreePackages {
 
 	private BufferedInputStream inputStream;
 	private JavaLexer lexer;
 	private JavaParser parser;
 	
 	private static final String PATH = "javaprojectsources";
 	
 	public Collection<CommonTreePackage> generate(){
 		File parent = new File(PATH);
 		
 		return searchInFolder(parent);
 	}
 	
 	private Collection<CommonTreePackage> searchInFolder(File parent){
 		Collection<CommonTreePackage> trees = new HashSet<CommonTreePackage>();
 		File[] files = parent.listFiles();
 		
 		if(files == null){
 			try {
 				if(!parent.getName().endsWith(".java")){
 					return trees;
 				}
 				
 				trees.add(generateTreeFromFile(parent));
 			} catch (Exception e) { System.err.println("Error: " + e.getMessage());}
 		} else{
 			for(File file : files){
 				trees.addAll(searchInFolder(file));
 			}
 		}
 		
 		return trees;
 	}
 
 	private CommonTreePackage generateTreeFromFile(File parent) throws Exception {
 		this.inputStream = new BufferedInputStream(new FileInputStream(parent));
 		this.lexer = new JavaLexer(new ANTLRInputStream(this.inputStream ));
 		this.parser = new JavaParser(new CommonTokenStream(this.lexer));
 		CommonTree tree = (CommonTree) (this.parser.compilationUnit().getTree());
 		return new CommonTreePackage(tree, getProjectName(parent), parent);
 	}
 
 	private String getProjectName(File parent) { 
         String path = parent.getAbsolutePath(); 
         String name = ""; 
        StringTokenizer tokenizer = new StringTokenizer(path, "\\"); 
         
         while(tokenizer.hasMoreTokens()){ 
              if(tokenizer.nextToken().equals(PATH)){ 
                   break; 
              } 
         } 
         
         if(tokenizer.hasMoreElements()){ 
              name = tokenizer.nextToken(); 
         } 
         
         return name; 
    }
 	
 }
