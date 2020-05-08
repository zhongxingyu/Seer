 package edu.berkeley.gamesman;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.prefs.Preferences;
 
 import org.python.core.PyObject;
 import org.python.util.InteractiveConsole;
 import org.python.util.JLineConsole;
 import org.python.util.ReadlineConsole;
 
 import edu.berkeley.gamesman.util.Util;
 
 public final class JythonInterface {
 	
 	private enum Consoles {
 		DUMB,READLINE,JLINE
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException something bad happened
 	 */
 	public static void main(String[] args) throws Exception {
 		Preferences prefs = Preferences.userNodeForPackage(JythonInterface.class);
 		
 		String consoleName = prefs.get("console", null);
 		Consoles console = null;
 		try {
 			console = Consoles.valueOf(consoleName);
 		} catch(IllegalArgumentException e) {
 			System.out.println(consoleName + " isn't one of " + Arrays.toString(Consoles.values()));
 		}
 		while(console == null){
 			System.out.println("Available consoles:");
 			for(int i = 0; i < Consoles.values().length; i++)
 				System.out.printf("\t%d. %s\n", i, Consoles.values()[i].toString());
 			System.out.print("What console would you like to use? ");
 			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 			int i;
 			try {
 				i = Integer.parseInt(br.readLine());
 			} catch(NumberFormatException e) {
 				System.out.println("Please input a number");
 				continue;
 			}
 			if(i < 0 || i >= Consoles.values().length) {
 				System.out.println(i + " is out of range");
 				continue;
 			}
 			console = Consoles.values()[i];
 		}
 		prefs.put("console", console.name());
 		
 		InteractiveConsole rc = null;
 		switch(console){
 		case DUMB:
 			rc = new InteractiveConsole() {
 				@Override
 				public String raw_input(PyObject prompt) {
 					//eclipse sometimes interleaves the error messages 
 					exec("import sys; sys.stderr.flush()");
 					return super.raw_input(prompt);
 				}
 			};
 			break;
 		case JLINE:
 			rc = new JLineConsole();
 			break;
 		case READLINE:
 			rc = new ReadlineConsole();
 			break;
 		default:
 			Util.fatalError("Need to pick a console");
 		}
 		
 		//this will let us put .py files in the junk directory, and things will just work =)
 		rc.exec("import sys");
 		addpath(rc, "jython_lib");
 		addpath(rc, "junk");
 		addpath(rc, "jobs");
 	
		rc.exec("from Play import *");
 		rc.interact();
 	}
 	
 	private static void addpath(InteractiveConsole ic, String what){
 		ic.exec(String.format("sys.path.append('%1$s/%2$s'); sys.path.append('%1$s/../%2$s');", 
 				System.getProperty("user.dir"), what));
 	}
 
 }
