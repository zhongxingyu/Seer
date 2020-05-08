 package org.amplafi.flow.shell;
 
 import java.io.Console;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.amplafi.flow.utils.AdminTool;
 
 /**
  * @author Tuan Nguyen
  */
 public class GScript extends Action {
     static String USAGE = 
         "Usage: \n"+
         "  gscript" ;
     
     public void exec(Console c, ShellContext context, String argsLine) throws Exception {
         if(argsLine == null) {
             argsLine = "" ;
         }
        argsLine = "-key " + context.getApiKey() + " -host " + context.getHost() + " " + argsLine;
         //TODO: should handle the string in " case
         String[] args = argsLine.split(" ") ;
         AdminTool adminTool = new AdminTool();
         List<String> argHolder = new ArrayList<String>() ;
         for (String arg : args) {
             arg = arg.trim();
             if(arg.length() > 0) {
                 argHolder.add(arg) ;
             }
         }
         args = argHolder.toArray(new String[argHolder.size()]) ;
         adminTool.processCommandLine(args);
     }
 
     public String getHelpInstruction() {
         return "Run a groovy script" ;
     }
 }
