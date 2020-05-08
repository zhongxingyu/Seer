 package wap;
 
 import iggs.JAVA_tools.StringTools.Check_Test;
 import iggs.JAVA_tools.console.IGGS_console;
 import iggs.JAVA_tools.utilities.SafeAppShutdown;
 import iggs.JAVA_tools.utilities.SysTools;
 import java.util.Calendar;
 
 /** Copyright (c) 2010, Goffredo Marocchi
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the author nor the
  *       names of any contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY GOFFREDO MAROCCHI "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL GOFFREDO MAROCCHI BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/
 
 public class Record_simple_exploration implements SafeAppShutdown{
 
 	static String fname = null;
 
 	public static final String crlf = System.getProperty("line.separator");
 	private static int steps_performed = 0;
 	//
 
 	private static void records_download(String fileName) {
 
 		String delim = "\"";
 		steps_performed = 0;
 		String tt = null;
 		
 		boolean mobileWebTesting = false;
 		
 		Calendar tStartTest;
 		tStartTest = null;
 		tStartTest = Calendar.getInstance();
 		
 		String check = "quit,undo,mobileWeb";
 		String welcome = "> Press Enter when you start the content testing step\n" +
 		"or type either \"quit\" to exit, \"mobileWeb\" to switch to web testing, or \"undo\" to roll-back one operation...";
 		String err_msg = "Invalid input, please try again...\n";
 
 		while (true) {
 			if (mobileWebTesting) {
 				check = "quit,undo";
 				welcome = "> Press Enter when you start the content testing step\n" +
 				"or type either \"quit\" to exit or \"undo\" to roll-back one operation...";
 			}
 			else {
 				check = "quit,undo,mobileWeb";
 				welcome = "> Press Enter when you start the content testing step\n" +
 				"or type either \"quit\" to exit, \"mobileWeb\" to switch to web testing, or \"undo\" to roll-back one operation...";
 			}
 			
 			IGGS_console console = new IGGS_console(null, null);
 			Calendar t1,t2;
 
 			int result = Check_Test.check_input (welcome, check, delim, err_msg, console, true);	
 			if(1 == result) {
 				SysTools.writeFileFlushBuffer(fileName);
 				break;
 			}
 			if (2 == result) {
 				console.println ("(!) Last step was deleted (!)");
 				SysTools.undoStep();
 				if (steps_performed >0) steps_performed--;
 				console.println ("Contents downloaded: " + Integer.toString(steps_performed));
 				if (mobileWebTesting) {
 					welcome = "> Press Enter when you have started the content testing step\n" +
 					"or type either \"quit\" to exit...";
 					check = "quit";
 				}
 				else {
 					welcome = "> Press Enter when you have started the content testing step\n" +
 					"or type either \"quit\" to exit or \"mobileWeb\" to switch to web testing...";
 					check = "quit,mobileWeb";
 				}
 				result = Check_Test.check_input(welcome, check, delim, err_msg, console, true);	
 				if(result == 1 || result > 2 || (result > 0 && mobileWebTesting)) {
 					break;
 				}
 				else if (result == 2) {
 					if (steps_performed > 0) {
 						mobileWebTesting = true;
 						SysTools.write_time_interval(fileName, tStartTest);
 						tStartTest = Calendar.getInstance();
 					}
 					else {
 						console.println ("(!) Error: not enough downloads performed to switch to mobileWeb testing mode (!)");
 						break;
 					}
 				}
 			}
 			if (3 == result) {
 				if (steps_performed > 0) {
 					mobileWebTesting = true;
 					SysTools.write_time_interval(fileName, tStartTest);
 					tStartTest = Calendar.getInstance();
 				}
 				else {
 					console.println ("(!) Error: not enough downloads performed to switch to mobileWeb testing mode (!)");
 					break;
 				}
 			}
 			else {
 				SysTools.writeFileFlushBuffer(fileName);
 			}
 
 			t1 = Calendar.getInstance();
 
 			console.print("> Press Enter when you have completed the content testing step");
 			console.readLine();
 			t2 = Calendar.getInstance();
 
 			SysTools.writeFile(fileName, SysTools.getDateString() + " ");
 			SysTools.write_time_HHMMSS_AMPM(fileName, t1);
 			SysTools.writeFile (fileName, ",");
 			SysTools.write_time_HHMMSS_AMPM(fileName, t2);
 			SysTools.writeFile (fileName, ",");
 			
 			long time = (t2.getTimeInMillis()/1000) - (t1.getTimeInMillis()/1000);
 
 			tt = Long.toString(time) + " s";
 
 			SysTools.writeFile (fileName,  tt);
 			SysTools.writeFile (fileName, "\n");
 
 			steps_performed++;
 			console.println ("Contents downloaded: " + Integer.toString(steps_performed) + "\n");
 		}
 		
 		if (steps_performed > 0) {
 			SysTools.write_time_interval(fileName, tStartTest);
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		/*Safe shutdown*/
 		SysTools.enableSafeShutdown((SafeAppShutdown) new Record_simple_exploration());
 
 		if (!SysTools.isWindows()) SysTools.install(new SysTools(), "TSTP");
 		SysTools.install(new SysTools(), "TERM");
 		SysTools.install(new SysTools(), "INT");
 		SysTools.install(new SysTools(), "ABRT");
		SysTools.install(new SysTools(), "SEGV");
 
 		IGGS_console console = new IGGS_console(null, null);
 		String delim = "\"";
 
 		String welcome = "Welcome to the WAP simple test helper tool, " +
 		"press Enter to go ahead " +
 		"or type \"quit\" and press Enter to exit...";
 		String err_msg = "Invalid input, please try again...\n";
 
 		int result = Check_Test.check_input(welcome, "quit", delim, err_msg, console, true);	
 		if(result > 0) {
 			SysTools.delayLoop (1);
 			System.exit(0);
 		}
 
 		console.println();
 		
 		do {
 			console.println("Please enter network company (example: TIM, Vodafone, WIND, etc.)");
 		} while ((fname = new String (console.readLine())).compareTo("") == 0);
 
 		fname = "outDownloads_" + fname + ".csv";
 		
 		SysTools.addFname(fname);
 
 		while(!SysTools.create_reset_File(fname));
 		records_download (fname);
 		SysTools.delayLoop (1);
 		System.exit(0);
 		
 	}
 
 	@Override
 	public void shutDown() {
 		// TODO Auto-generated method stub
 		SysTools.cleanShutdown();
 		IGGS_console console = new IGGS_console(null, null);
 		console.println ("The Application is shutting down...");
 	}
 
 	@Override
 	public void start() {
 		// TODO Auto-generated method stub
 		try {
 			Thread.sleep(250);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
