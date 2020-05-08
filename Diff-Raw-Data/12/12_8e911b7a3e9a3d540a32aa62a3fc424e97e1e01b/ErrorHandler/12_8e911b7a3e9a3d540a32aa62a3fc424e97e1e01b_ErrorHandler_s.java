 package com.galaxyx.utils;
 
 import java.util.LinkedList;
 import java.util.List;
 import org.antlr.runtime.Token;
 
 public class ErrorHandler {
 
     private List<Error> errors;
     private List<Warning> warnings;
     
     public static int ERROR_COUNT = 0;
     public static int WARNING_COUNT = 0;
     
     public ErrorHandler(){
         errors = new LinkedList<Error>();
         warnings = new LinkedList<Warning>();
     }
     
     public void reportError(Error e){
         errors.add(e);
     }
     
     public void reportWarning(Warning w){
         warnings.add(w);
     }
     
     public boolean errorsOccured(){
         return errors.size() > 0;
     }
     
     public boolean warnignsOccured(){
         return warnings.size() > 0;
     }
     
     public List<Error> retrieveErrorMessages(){
         return errors;
     }
     
     public List<Warning> retrieveWarningMessages(){
         return warnings;
     }
     
     public static class Error{
         private String msg;
         private int line, pos;
         
         /**
          * Reference token for line and position is the first.
          * 
          * @param msg
          * @param tokens 
          */
         public Error(String msg, Token ... tokens){
             for (int i = 0; i < tokens.length; i++) {
                 msg = msg.replaceAll("\\$" + (i + 1), tokens[i].getText());
             }
             this.msg = "line " + tokens[0].getLine() + ":" + tokens[0].getCharPositionInLine() + " " + msg;
         }
         
         public Error(String msg, int line, int pos){
             this.msg = msg;
             this.line = line;
             this.pos = pos;
         }
         
         public int getLine(){
             return line;
         }
         
         public int getPos(){
             return pos;
         }
         
         @Override
         public String toString(){
             return msg;
         }
     }
     
     public static class Warning{
         private String msg;
         private int line, pos;
         
         public Warning(String msg, int line, int pos){
             this.msg = msg;
             this.line = line;
             this.pos = pos;
         }
         
         public int getLine(){
             return line;
         }
         
         public int getPos(){
             return pos;
         }
         
         @Override
         public String toString(){
             return msg;
         }
     }
 }
