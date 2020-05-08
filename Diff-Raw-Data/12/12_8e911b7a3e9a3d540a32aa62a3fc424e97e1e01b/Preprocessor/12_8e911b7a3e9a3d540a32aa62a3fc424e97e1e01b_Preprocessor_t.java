 package com.galaxyx;
 
 import com.galaxyx.utils.ErrorHandler;
 import com.galaxyx.utils.FileLoader;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.antlr.runtime.Token;
 
 /**
  *
  * @author Timo Hanisch (timohanisch@gmail.com)
  */
 public class Preprocessor {
 
     private Map<String, String> defineTable;
     private ErrorHandler handler;
     private String input, filePath;
     private boolean includePhase = false;
     private List<File> includeList;
 
     public Preprocessor(String input, String filePath) {
         this.input = input;
         this.filePath = filePath;
         handler = new ErrorHandler();
         defineTable = new HashMap<String, String>();
         includeList = new ArrayList<File>();
         includeList.add(new File(filePath));
     }
 
     private void processDefines() {
         for (String id : defineTable.keySet()) {
             input = input.replaceAll(id, defineTable.get(id));
         }
     }
     
     private void clean(){
         String [] inputLines = input.split(System.getProperty("line.separator"));
         for(int i = 0; i < inputLines.length; i++){
             String line = inputLines[i];
             line = line.trim();
             if(line.startsWith("#")){
                 inputLines[i] = null;
             }
         }
         StringBuilder builder = new StringBuilder();
         for(int i = 0; i < inputLines.length; i++){
             String line = inputLines[i];
             if(line != null){
                 builder.append(line);
                 builder.append(System.getProperty("line.separator"));
             }
         }
         input = builder.toString();
     }
     
     public void reportError(String msg, Token ... tokens){
         handler.reportError(new ErrorHandler.Error(msg, tokens));
     }
     
     public void reportError(String msg, int line, int pos){
         handler.reportError(new ErrorHandler.Error(msg,line,pos));
     }
 
     public ErrorHandler getErrorHandler() {
         return handler;
     }
 
     public boolean define(String id, String definition) {
         return defineTable.put(id, definition) == null;
     }
 
     public boolean undef(String id) {
         return defineTable.remove(id) != null;
     }
     
     public void processInclude(Token path){
         if(!includePhase){
             String includePath = path.getText().substring(1, path.getText().length() - 1);
             File f = new File(includePath);
             if(includeList.contains(f)){
                 reportError("Cyclic dependency for include",path);
             }else if(!f.exists()){
                 reportError("Could not find file '"+includePath+"'",path);
             }else{
                 String includeText = FileLoader.loadFile(f.getAbsolutePath());
                 String [] inputLines = input.split(System.getProperty("line.separator"));
                 StringBuilder builder = new StringBuilder();
                 for(int i = 0; i < inputLines.length; i++){
                     String line = inputLines[i];
                     if(i == path.getLine() - 1){
                         builder.append(includeText);
                         builder.append(System.getProperty("line.separator"));
                     }else{
                         builder.append(line);
                         builder.append(System.getProperty("line.separator"));
                     }
                 }
                 input = builder.toString();
                 includePhase = true;
             }
         }
     }
 
     public boolean canProcess() {
         return !handler.errorsOccured();
     }
     
     public String includePhase(){
         if(includePhase){
             includePhase = false;
             defineTable.clear();
             return input;
         }
         return null;
     }
 
     public void printErrors() {
         for (ErrorHandler.Error e : handler.retrieveErrorMessages()) {
             System.err.println(e);
         }
        handler.clear();
     }
 
     public String process() {
         processDefines();
         clean();
         return input;
     }
 }
