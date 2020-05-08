 package com.dubbelepuntf.airsocketserver.cli;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.log4j.Logger;
 
 /**
  * Stuff enzo
  * 
  * @author niels
  *
  */
 public class Server { 
 	
    private final Logger log = Logger.getLogger(this.getClass());
     
     private boolean isAppelTaartLekker = true;
     
     /**
      * Public method. Moet commentaar bij jong.
      */
     public void startCLI() {
         
         while(isAppelTaartLekker){
             
             log.info("Enter a command:");
             
             String input = getInput();
             
             Command command = Command.find(input);
             
            if (!executeCommand(command))
             {
             	break;
             }
         }
     }
     
    private boolean executeCommand(Command command){
         
     	boolean retVal = true;
     	
     	log.info(command.getMessage());
     	
         switch(command){
                 
                 case HELP:
                    // execute stuff
                 break;
                 
                 case STOP:
                    // execute stuff
                 	retVal= false;
                 break;
                 
                 case COFFEE:
                    // execute stuff
                 break;
                 
                 case NONE:
                    // execute stuff
                 break;
         }
 		return retVal;
     }
     
     private String getInput() {
 
         String input = "";
         BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(System.in));
         
         try {
             log.info("$ ");
                 input = inputBuffer.readLine();
                 
         }
         
         catch (IOException e) {
             
                log.error(e.getMessage(), e);
                 
         }
 
         return input;
         
     }
 }
