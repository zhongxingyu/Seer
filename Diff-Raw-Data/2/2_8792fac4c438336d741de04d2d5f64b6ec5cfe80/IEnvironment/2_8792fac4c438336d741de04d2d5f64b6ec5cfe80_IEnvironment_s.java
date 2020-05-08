 package dk.itu.ecdar.text.generator.environment;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import dk.itu.ecdar.text.generator.framework.AutomatonTimer;
 import dk.itu.ecdar.text.generator.framework.IController;
 
 /**
  * Base class for environment implementations.
  * 
  * Use this to test the implemented ECDAR specifications.
  * A text file provided can specify the input the environment
  * receives while executing.
  */
 public abstract class IEnvironment {
 	
 	
 	protected IController controller;
 	AutomatonTimer timer;
     ArrayList<SimpleEntry<Long, String>> inputs;
 	
     /**
      * Needs to be implemented for every implemented ECDAR specification.
      */
     public abstract void generateController();
     
 	public IEnvironment() {
 		generateController();
 		timer = new AutomatonTimer();
 		inputs = new ArrayList<SimpleEntry<Long, String>>();
 	}
     
 	/**
 	 * Parses a file that specifies a number of timed inputs in the format
 	 * 
 	 * 1  COF
 	 * 9  PUB
 	 * 20 TEA
 	 * 
 	 * where the number of spaces between time and
 	 * input can be greater than one. The instructions
 	 * will be handled top to bottom. It is therefore
 	 * required, that the instructions are ordered after
 	 * time. Otherwise, the environment will end up in
 	 * a deadlock.
 	 * 
 	 * @param file Path to the file to parse.
 	 */
 	public void parse(String file) {
 		QuickLog.print("Parsing \"" + file + "\"...");
 		Scanner scanner;
 		
 		try {
 			scanner = new Scanner(new File(file));
 			
 			while (scanner.hasNext()) {
 				String line = scanner.nextLine();
 				
 				// allow comments
 				if (line.startsWith("#"))
 					continue;
 				
 				String[] input = line.split("\\s+");
 				inputs.add(new SimpleEntry<Long, String>(Long.parseLong(input[0]), input[1]));
 			}
 			
 			scanner.close();
 			
 		} catch (FileNotFoundException e) {
 			QuickLog.print("Could not open file \"" + file + "\", will exit now.");
 			System.exit(-1);
 		}
 	}
 	
 	/**
 	 * Executes the environment.
 	 */
 	public void run() {
 		QuickLog.print("Starting controller...");
 		controller.run();
 		
 		timer.reset();
 		
 		while(!inputs.isEmpty()) {
 			if (inputs.get(0).getKey() <= timer.getTime()) {
 				QuickLog.log(toString(), timer.getTime(), "Signaling " + inputs.get(0).getValue());
 				controller.notify(inputs.get(0).getValue());
 				inputs.remove(0);
 			}
 		}
 		
 		QuickLog.print("All instructions sent.");
 		
 		try {
			Thread.sleep(100);
 		} catch (InterruptedException e) {
 			QuickLog.print(e.getMessage());
 		}
 		
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 }
