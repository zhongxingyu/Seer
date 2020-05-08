 // JackCompiler.java
 // by Thea Gab
 // Compile: (from directory with JackCompiler folder)
 //  javac JackCompiler/*.java
 // Usage: (from directory with JackCompiler folder)
 //  java JackCompiler/JackCompiler <source>
 
 package JackCompiler;
 
 import java.io.*;
 import java.util.StringTokenizer;
 
 public class JackCompiler {
 
   public static void main(String[] args) throws IOException{
     // Check input arguments
     if (args.length != 1) {
       System.err.println("Usage: java JackCompiler <source>");
       System.exit(1);
     }
 
     // Save input file/directory name
     String source = args[0];
 
     if (source.endsWith(".jack") == true) {
       
       try {
         JackCompiler trans = new JackCompiler();
         trans.translate(source);
       } catch (IOException e){
         System.err.println("Error trying to read file");
         System.exit(1);
       }
 
     }
     else {
       // Make sure directory name ends with /
       if (! source.endsWith("/")){
         source = source + "/";
       }
 
       // Check if directory exists
       File dir = new File(source);
       if (!dir.exists()){
         System.err.println(source + ": does not exist");
         System.exit(1);
       }
 
       // Check if directory can be opened and list of files obtained
       String[] files = dir.list();
       if (files == null){
         System.err.println(source + ": is not a directory");
         System.exit(1);
       }
 	  
       // Loop through all the files in the specified directory
       for (int i=0; i < files.length; i++){
         String filename = files[i];
 
         // If the file is a .jack file, translate it
         if (filename.endsWith(".jack")){
           try {
             JackCompiler trans = new JackCompiler();
             trans.translate(source + filename);
           } catch (IOException e){
             System.err.println("Error trying to read file");
             System.exit(1);
           }
         }
       }
 	  
     } //END else
   } //END MAIN
 
   public void translate(String source) throws IOException{
   
     // Define name for output file
     String output = source.substring(0, source.length()-5) + ".vm";
 
     OutputStreamWriter vm = new OutputStreamWriter(new FileOutputStream(output));
 
     // Create file stream for input
     BufferedReader jack = null;
     try {
       jack = new BufferedReader(new FileReader(source));
     }
     catch (FileNotFoundException e) {
       System.err.println(source + ": does not exist or cannot be read");
       System.exit(1);
     }
 
     // Create a tokenizer object
     JackTokenizer tokenizer = null;
 
     try {
       tokenizer = new JackTokenizer(jack);
     }
     catch (IOException e) {
       System.err.println(source + ": error while reading file");
     }
 
     // Close file stream
     jack.close();
 
     // Create compilation engine
     CompilationEngine engine = new CompilationEngine(tokenizer, vm);
 	  
     // Compile this file
 	try {
 		engine.CompileClass();
 	} catch (IndexOutOfBoundsException e){
 		e.printStackTrace();
 		vm.close();
 		System.exit(1);
 	} catch (NullPointerException e){
 		e.printStackTrace();
 		vm.close();
 		System.exit(1);
 	}
 	  
     // Close file stream
     vm.close();
 	  
     System.out.println("Output written to " + output);
   }
 }
 
