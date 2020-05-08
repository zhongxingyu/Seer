 package wavtools;
 import java.util.Scanner;
 import java.io.File;
 
 public class Main
 {
 	static Scanner keyboardinput = new Scanner(System.in);
     static SoundProcessor s = new SoundProcessor();
     static String filename = null;
    
     public static void main (String[] args)
     {
    	new SoundProcessor();
         Boolean moretasks = null;
         
         do
         {    
         	printMenu();
         	
             int decision = Integer.parseInt(getKeyboardInput());
             
             if (1 <= decision && decision <=7) {
                 System.out.println(processMenuChoice(decision, getInputFile(), getOutputFile()));
                 
             	System.out.println("Perform another task? (y/n)");
         		moretasks = checkAnswer();
             }
             
             else if (decision == 8) {
             	moretasks = false;
             }
             
         } while (moretasks);
     }
     
     
     
     public static void printMenu()
     {
 	  System.out.println("Please select from the following menu options:");
       System.out.println("1 - Reduce Volume");
       System.out.println("2 - Combine two clips");
       System.out.println("3 - Reverse Audio");
       System.out.println("4 - Increase Speed");
       System.out.println("5 - Remove Silence");
       System.out.println("6 - Add Echo");
       System.out.println("7 - Trim");
       System.out.println("8 - Exit");
     }
     
     public static String getKeyboardInput()
     {
         Scanner keyboardinput = new Scanner(System.in);
         return keyboardinput.next();
     }
 
     public static String processMenuChoice(int decision, String inputpath, String outputpath)
     {
     	s.setup(inputpath);
     	
     	switch (decision) {
 	        case 1: s.quieter(outputpath);
 	            return "Done!";
 	        case 2: System.out.print("Choose a second wave file. ");
 	            s.combine(getInputFile(), outputpath);
 	            return "Done!";
 	        case 3: s.reverse(outputpath);
 	        	return "Done!";
 	        case 4: s.speedUp(outputpath);
 	            return "Done!";
 	        case 5: s.removeSilence(outputpath);
 	            return "Done!";
 	        case 6: s.addEcho(outputpath);
 	            return "Done!";
 	        case 7: processTrim(filename, outputpath);
 	        	return "Done!";
 	        case 8:
 	        	return "Goodbye!";
 	        default: return("Not a valid choice.");
     	}
     }
     
     public static String getInputFile()
     {
     System.out.println("Choose input wav file.");
     String inputfilepath = null;
     File inputfile = null;
     
         do
         {
             System.out.print("Enter path to file: ");
             inputfilepath = getKeyboardInput();
             inputfile = new File(inputfilepath);
             if (!inputfile.canRead()) {
                 System.out.println("File does not exist. Please try again.");
             }
         }
         while (!inputfile.canRead());
         return inputfilepath;
 
     }
     
     public static String getOutputFile()
     {
     	
             boolean overwrite = false;
             String outputpath = null;
             
             do {
             	 System.out.print("Please enter an output filename: ");
             	 outputpath = getKeyboardInput();
             	 File outputfile = new File(outputpath);
             	 
                 if (outputfile.canRead()) {
                 	
                     System.out.println("File already exists. Would you like to overwrite? (y/n)");                    
                     overwrite = checkAnswer();
                 }
                     
                 else if(!outputfile.canRead()) {
                 	overwrite = true;
                 }
                 
             } while (overwrite == false);       
             return outputpath;
             
         }
     
     
     public static boolean checkAnswer()
     {
     Boolean inputvalidity = null;
     Boolean yesorno = null;    
     
     	do { 
     		// Check if the answer is valid
         	
     		String inputtocheck = getKeyboardInput();
     		inputtocheck = inputtocheck.intern();
 	    		// Contents of string are not known until runtime, so it must be interned
 	    		// in order to use the '==' operator.
 	    		
         	if (inputtocheck == "y"
     			|| inputtocheck == "Y"
     			|| inputtocheck == "n" 
     			|| inputtocheck == "N") {
         		// Input is valid, breaks this loop 
         		
         		inputvalidity = true; 
 
         		if (inputtocheck == "y" || inputtocheck == "Y") {
         			yesorno = true;
         		}
         		
         		else if (inputtocheck == "n" || inputtocheck == "N") {
         			yesorno = false;
         		}
         		
         	}
         	
         	else { 
         		// Loops back to get a valid response
         		
         		inputvalidity = false;
         		System.out.println("Not a valid response, please try again.");
         	} 
         	
         		
     	} while (inputvalidity == false);
     	
     	return yesorno;
     }
     
     
     
     
     public static void processTrim(String input, String output)
     {
         
         System.out.print("When to start trimming? ");
         String start = getKeyboardInput();
         double startTime = Double.parseDouble(start);
         
         System.out.print("How long? ");
         String length = getKeyboardInput();
         double lengthTime = Double.parseDouble(length);
         
         s.trimFile(input, output, startTime, lengthTime);
         System.out.println("Done!");
     }
 
 }
