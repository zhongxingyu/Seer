 package ussr.aGui.tabs.controllers;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import ussr.aGui.tabs.views.ConsoleTab;
 
 /**
  * Directs output streams to Console tab.
  * @author Konstantinas
  */
 public class ConsoleTabController {
 	
 	/**
 	 * Directs output stream to GUI component called console(tab). Stream runs in separate thread.
 	 * @param prefix, the prefix to use.
 	 * @param stream, the stream to direct.
 	 */
 	public static void appendStreamToConsole(final String prefix, final InputStream stream){
 		 new Thread() {
 	            public void run() {
 	                BufferedReader input = new BufferedReader(new InputStreamReader(stream));
 	                while(true) {
 	                    String line;
 	                    try {
 	                        line = input.readLine();
 	                        if(line==null) break;
 	                        
 	                        ConsoleTab.getJTextAreaConsole().append(prefix +": "+line+"\n" );
 	                    } catch (IOException e) {
 	                        throw new Error("Unable to dump stream: "+e); 
 	                    }
 	                }
 	            }
 	        }.start();
 	}	
 }
