 /*
  * Copyright 2012 Joseph Spencer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.xforj;
 
 import com.xforj.arguments.XforJArguments;
 import com.xforj.arguments.XforJTerminal;
 import com.xforj.javascript.*;
 import com.xforj.productions.ProductionContext;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 
 /**
  *
  * @author Joseph Spencer
  */
 public class XforJ extends LOGGER implements Characters {
    //Exit codes
    public static final int UNABLE_TO_PARSE_FILE=1;
    public static final int IO_Error=2;
 
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
       try {
          XforJArguments arguments = XforJTerminal.getArguments(args);
 
          //We don't compile if they want the lib.
          if(arguments.hasOutputlibrary()){
             JavascriptBuilder jsBuilder = JavascriptBuilder.getInstance(arguments);
             File output = arguments.getOutputlibrary();
             MainUtil.putString(output, jsBuilder.getXforJLib());
 
          } else {
             if((!arguments.hasInputfile() || !arguments.hasOutputfile())){
                //If either are null throw an exception, unless they specify the output of external library.
                throw new Exception("Both an input file and an output file must be given.");
             }
             long before = new Date().getTime();
             startCompiling(arguments);
             out("Time taken: "+Long.toString(new Date().getTime() - before) + "ms");
          }
       } catch(Throwable exc) {
          handleGeneralError(exc);
       }
 
    }
 
    private static void handleGeneralError(Throwable exc){
       out("===============================");
       out("FAILED FOR THE FOLLOWING REASON");
       out("===============================");
       out(exc.getMessage());
       debug(exc.getClass().getSimpleName());
       System.exit(UNABLE_TO_PARSE_FILE);
    }
 
 
    /**
     * This is the main entry point to XforJ from the ant task.
     * 
     * @param arguments
     */
    public static void startCompiling(XforJArguments arguments) {
       try {
          if(arguments.getDebug()){
             debug = true;
          }
          if(arguments.getWarn()){
             warn = true;
          }
 
 
          debug("startCompiling called.");
 
          if(//make sure there is input to process before proceeding.
             arguments.hasInputfile()
             || arguments.hasInputfiles()
          ){
             if(arguments.hasInputfile()){
                File input = arguments.getInputfile();
                if(arguments.hasOutputfile()){
                   compileNewFile(input, arguments.getOutputfile(), arguments);
                } else if(arguments.hasDestdir()){
                   String outputDir = arguments.getDestdir().getCanonicalPath();
                   if(!outputDir.endsWith(File.separator)){
                      outputDir+=File.separator;
                   }
                   String outputFilePath = outputDir+input.getName();
                   File outputFile = new File(outputFilePath);
 
                   compileNewFile(input, outputFile, arguments);
                } else {
                   throw new IllegalArgumentException("No target given for input file.");
                }
 
             }
 
             if(arguments.hasInputfiles()){
                if(arguments.hasDestdir() && arguments.hasSrcdir()){
                   String inputDirectoryPath = arguments.getSrcdir().getCanonicalPath();
                   String outputDirectoryPath = arguments.getDestdir().getCanonicalPath();
 
                   if(!inputDirectoryPath.endsWith(File.separator)){
                      inputDirectoryPath+=File.separator;
                   }
                   if(!outputDirectoryPath.endsWith(File.separator)){
                      outputDirectoryPath+=File.separator;
                   }
 
                   debug("InputDirectoryPath:\n   "+inputDirectoryPath);
                   debug("OutputDirectoryPath:\n   "+outputDirectoryPath);
 
                   Iterator<File> files = arguments.getInputfiles().iterator(); 
                   while(files.hasNext()){
                      File next = files.next();
                      String pathOfFileToCompile = next.getCanonicalPath();
                      String pathOfTargetFile = pathOfFileToCompile.replace(
                            inputDirectoryPath, 
                            outputDirectoryPath
                      ).replaceFirst("\\.xforj$", extension_js);
 
                      debug("File to compile:\n   "+pathOfFileToCompile);
                      debug("Target File:\n   "+pathOfTargetFile);
 
                      if(!pathOfFileToCompile.startsWith(inputDirectoryPath)){
                         throw new IOException(
                            "The following file does not belong to the input path specified:\n"
                            +pathOfFileToCompile
                         );
                      }
 
                      compileNewFile(next, new File(pathOfTargetFile), arguments);
                   }
                } else {
                   throw new IllegalArgumentException("Both destdir and srcdir must be given as attributes when using filesets.");
                }
             }
          } else {
             out("No input file[s] were given.  Exiting early.");
          }
       } catch(Exception exc) {
          handleGeneralError(exc);
       }
    }
 
    private static Set<String> compiledNewFiles = new HashSet(Arrays.asList(new String[]{}));
    /**
     * Compiles one file at a time.  This method relies on the compiledNewFiles set
     * to avoid compiling a file more than once.
     * 
     * @param input
     * @param outFile
     * @param arguments
     * @throws Exception 
     */
    private static void compileNewFile(File input, File outFile, XforJArguments arguments) throws Exception {
       debug("compileNewFile called.");
       String inputFilePath = input.getCanonicalPath();
       String outputFilePath = outFile.getCanonicalPath();
       
       String compiledKey = inputFilePath+":"+outputFilePath;//used to avoid redundant building
 
 
       //Make sure the input file exists
       if(!input.exists()){
          throw new IOException("The following input file does not exist:\n"+inputFilePath);
       }
 
       if(!arguments.getOverwrite() && outFile.exists()){
          out("Can't overwrite the following file: "+outputFilePath);
          out("Specify overwrite in the arguments to change this.");
          return;
       }
 
       //Now check to make sure that the appropriate file extensions are used
       if(!inputFilePath.endsWith(extension_xforj)){
          throw new IOException("The following input file does not end with a .xforj extension:\n"+inputFilePath);
       }
       if(!outputFilePath.endsWith(extension_js)){
          throw new IOException("The following target file does not end with a .js extension:\n"+outputFilePath);
       }
 
       //Create any directories leading up to the file.
       File parentDir = outFile.getParentFile();
       if(parentDir != null && !parentDir.exists()){
          parentDir.mkdirs();
       }
 
       if(compiledNewFiles.contains(compiledKey)){
          out("Ignoring:\n   "+compiledKey+"\n   It has already been built.");
       } else {
          compiledNewFiles.add(compiledKey);
          debug("Initializing JavascriptBuilder.");
          JavascriptBuilder jsBuilder = JavascriptBuilder.getInstance(arguments);
          debug("Initializing new ProductionContext.");
          ProductionContext context = new ProductionContext(input, arguments, jsBuilder);
 
          String output = compileFile(
             input, 
             context
          ).toString();
          MainUtil.putString(outFile, output);
 
          out("Compiled:\n   "+inputFilePath);
          out("To:\n   "+outputFilePath);
       }
    }
 
    /**
     * This is for internal use by the compiler.
     * 
     * @param path Passed by import statements
     * @param previousContext
     * @return
     */
    public static Output compileFile(File fileToCompile, ProductionContext context) {
       CharWrapper wrapper=null;
       debug("compileFile called.");
 
       try{
          char[] chars=MainUtil.getChars(fileToCompile);
 
          CharWrapper characters = new CharWrapper(chars);
          wrapper=characters;
          while(characters.length() > 0){
             context.executeCurrent(characters);
          }
          context.close();
          return context.output;
       } catch(IOException exc){
          logException(fileToCompile, exc, null);
          System.exit(IO_Error);
       } catch(Exception exc){
          logException(fileToCompile, exc, wrapper);
          System.exit(UNABLE_TO_PARSE_FILE);
       }
       return null;
    }
 
    private static void logException(File file, Exception exc, CharWrapper wrapper){
       String message= "Unable to parse:\n"+
             file.getAbsolutePath()+
             "\nFor the following reason:\n"+
             exc.getMessage();
       if(wrapper!=null){
          message+="\n"+wrapper.getErrorLocation();
       }
       out(message);
    }
 }
