 package multimil;
 import java.io.*;
 /**
  * Main class for Multimil.
  * 
  * @author angelstam
  */
 public class Main
 {
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		System.out.println("Multimil disassembler for SY6502.");
 
 		opHandler handler;
		if (args.length > 0 && args[0].equals("smooth"))
 		    handler = new opHandler("../codematrix",2);
 		else
 		    handler = new opHandler("../codematrix",1);
 		    
 		handler.generateInstructions("../doc/multimil.bin");
 	}
 	
 }
