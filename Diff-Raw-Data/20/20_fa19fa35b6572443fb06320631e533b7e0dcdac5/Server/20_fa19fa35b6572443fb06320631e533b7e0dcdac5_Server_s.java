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
             
            if (!excuteCommand(command))
             {
             	break;
             }
         }
     }
     
    private boolean excuteCommand(Command command){
         
     	boolean retVal = true;
     	
     	log.info(command.getMessage());
     	
         switch(command){
                 
                 case HELP:
                    // excute stuff
                 break;
                 
                 case STOP:
                    // excute stuff
                 	retVal= false;
                 break;
                 
                 case COFFEE:
                    // excute stuff
                 break;
                 
                 case NONE:
                    // excute stuff
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
