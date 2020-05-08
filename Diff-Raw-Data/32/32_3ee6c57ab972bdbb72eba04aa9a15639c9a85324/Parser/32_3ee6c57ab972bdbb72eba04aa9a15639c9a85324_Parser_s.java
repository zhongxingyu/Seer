 /*
  * Parser.java
  * Joe Smith
  * Algorithms - Spring 2009
  * Erik Linstead
  * Chapman University
  * Released under GPLv2 or later.
  */
 
 import java.io.*;
 import java.util.*;
 
 public class Parser{
 	public Parser(String filename){
 		BufferedReader in;
 		String line = "";
 		FiniteStateAutomata FSA = new FiniteStateAutomata();
 
 		try 
 		{
 			in = new BufferedReader(new FileReader(filename));
 
 			/*
 			 * This algorithm (hopefully) doesn't need the list of states
 			 */
 
 			line = in.readLine();
 
                         /*
 			 * To find the characters
 			 */
 			line = in.readLine();
 			FSA.Alphabet = parseAlphabet(line);
 				
 			line = in.readLine();
 			FSA.startState = parseStartState(line);
 
 			line = in.readLine();
 			FSA.acceptState = parseAcceptState(line);
 
 			line = in.readLine();
 			while  (line != null) 
 			{        
				line = parseTransitionState(line);
				System.out.println(line);
 
                                Transition tran = new Transition();

				//tran = parseTrans(line);

				//FSA.transitions.add(line);
 
 				line = in.readLine();
 			} 
 		}
  		catch (FileNotFoundException e)
  		{
 	 		e.printStackTrace();
  		}
  		catch (IOException e) 
  		{
 	 		e.printStackTrace();
  		}
 	}
 
 	private String[] parseAlphabet(String line)
 	{
 		String[] alpha = {};
                 alpha = line.split("\t");
 
 		System.out.println("The alphabet: ");
 		for (int i = 0; i < alpha.length; ++i)
 		{
 			System.out.println(alpha[i]);
 		}
 
 		return alpha;
 	}
 
 	private String parseStartState(String line)
 	{
                 line = line.replace("{", "");
                 line = line.replace("}", "");
 
 		System.out.println("The start state: ");
 		System.out.println(line);
 		return line;
 	}
 
 	private String parseAcceptState(String line)
 	{
                 line = line.replace("{", "");
                 line = line.replace("}", "");
 
 		System.out.println("The accept state: ");
 		System.out.println(line);
 		return line;
 	}
 
	private String parseTransitionState(String line)
 	{
                 line = line.replace("{", "");
                 line = line.replace("}", "");
 
 		//System.out.println(line);
		return line;
 	}
 }
