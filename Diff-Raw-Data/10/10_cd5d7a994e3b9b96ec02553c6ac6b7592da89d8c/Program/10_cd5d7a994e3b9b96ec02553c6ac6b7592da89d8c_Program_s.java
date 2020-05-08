 package welch.brainmess;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 
 /**
  * Represents a Brain Mess program
  * @author Michael Welch
  *
  */
 public class Program
 {
 	
 	private final String program;
 	private  int position;
 
 	/**
 	 * Creates a new instance of a Program by reading instructions
 	 * from the specified file.
 	 * @param fileName
 	 */
 	public Program(String fileName)
 	{
 		try
 		{
 			File file = new File(fileName);
 			
 			FileReader reader = new FileReader(file);
 			BufferedReader breader = new BufferedReader(reader);
 			int value;
 			StringBuilder builder = new StringBuilder();
 			while((value = breader.read()) != -1)
 			{
 				builder.append((char)value);
 			}
 			program = builder.toString();
 		} 
 		catch (Exception e)
 		{
 			throw new RuntimeException("Could not read file", e);
 		}
 
 		
 	}
 	
 	/**
 	 * Returns true if the last instruction has been read.
 	 * @return
 	 */
 	public boolean isEndOfProgram()
 	{
 		return position == program.length();
 		
 	}
 
 	/**
 	 * Retrieves the next instruction.
 	 * @return
 	 */
 	public Instruction fetch()
 	{
 		char value = getNextChar();
 		
 		return Instruction.parseInstruction(value);
 	}
 
 	/**
 	 * A helper function to retrieve the next character in the
 	 * program.
 	 * @return
 	 */
 	private char getNextChar()
 	{
 		char value = program.charAt(position);
 		position++;
 		return value;
 	}
 	
 	/**
 	 * A helper function to retrieve the previous character in the
 	 * program.
 	 * @return
 	 */
 	private char getPreviousChar()
 	{
 		position--;
 		return program.charAt(position);
 	}
 	
 
 	/**
 	 * Seeks in a forward direction until it finds a matching ']' and then
 	 * positions the 'cursor' at instruction that follows it.
 	 */
 	public void jumpForward()
 	{
 		// look for matching "]" - keep track of nesting level to know when match is found
 		int nestingLevel = 0;
 		
		do
		{
 			char nextChar = getNextChar();
 			if (nextChar == ']')
 			{
 				if (nestingLevel == 0)
 				{
 					return;
 				}
 				else
 				{
 					nestingLevel--;
 				}
 			}
			else if (nextChar == ']')
 			{
 				nestingLevel++;
 			}
 		} while(!isEndOfProgram());
 
 		
 	}
 	
 	/**
 	 * Seeks in a backward direction until it finds a matching '[' and then positions
 	 * the cursor at the instruction that follows it.
 	 */
 	public void jumpBackward()
 	{
 		// look for matching '[' - using nesting level to know when match is found
 		int nestingLevel = -1;
		do
		{
 			char prevChar = getPreviousChar();
 			if (prevChar == '[')
 			{
 				if (nestingLevel == 0)
 				{
 					getNextChar();
 					return;
 				}
 				else
 				{
 					nestingLevel--;
 				}
 			}
 			else if (prevChar == ']')
 			{
 				nestingLevel++;
 			}
 			
 		} while(position >= 0);
 		
 	}
 	
 	
 }
