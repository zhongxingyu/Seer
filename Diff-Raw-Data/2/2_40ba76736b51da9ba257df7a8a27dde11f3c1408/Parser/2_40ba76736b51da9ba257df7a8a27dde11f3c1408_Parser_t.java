 package hack.bp.assembler;
 
 import java.io.*;
 import java.util.Scanner;
 
 /** Parser.java *************************************************************************
  *  Encapsulates access to the input code. Reads an assembly language command, parses it,
  *  and provides convenient access to the command's components (fields and symbols). In
  *  addition, removes all whitespace and comments.
  *  
  *  This version uses JDK 1.6 update 29. Please have this when attempting to run this
  *  program.
  *  
  *  @author bp
  *  
  *	@changes
  *	0.1 - 	Initial implementation of Parser class. This 
  *			version will not translate programs that contain 
  *			symbols. -bp
  *	0.2 -	Fixing access specifiers for a few methods. 
  *			Adding more useful comments -bp
  ***************************************************************************************/
 public class Parser 
 {
 	private File inputFile 	= null;
 	private Scanner scanner	= null;
 	private String currentCommand = "";
 	private int commandLength = -1;
 	
 	public Parser()
 	{
 		exit();
 	}
 	
 	public Parser( String filePath )
 	{
 		init( filePath );
 	}
 	
 	/** init() **************************************************************************
 	 *  Initializes the Parser instance with the current file and then sets up the
 	 *  reader. A scanner is initialized to allow class methods to read the file's
 	 *  contents. *** THIS MUST ALWAYS BE CALLED FIRST BEFORE USING THIS PARSER! ***
 	 *  Not doing so will keep the input file object and scanner null (therefore
 	 *  breaking the functionality of methods that depend on them).
 	 ***********************************************************************************/
 	private void init( String filePath )
 	{
 		// Initialize the file
 		if( this.inputFile == null )
 			this.inputFile = new File( filePath );
 		
 		// Create a scanner to easily check file contents
 		try 
 		{
 			this.scanner = new Scanner( inputFile );
 		} 
 		catch (FileNotFoundException e) 
 		{
 			System.out.println( "ParserError-init: Scanner couldn't find file!" );
 			e.printStackTrace();
 			System.exit( 1 );
 		}
 	}
 	
 	/************************************************************************************
 	 * 	TODO:
 	 * 	Create re-initialization function to handle when inputFile has been re-set
 	 ***********************************************************************************/
 	
 	/** exit() **************************************************************************
 	 *  It is common to not use System.exit() from within a class constructor. Instead
 	 *  it is better to let a method handle that function.
 	 ***********************************************************************************/
 	private void exit()
 	{
 		System.out.println( "ParserError-Parser(): Parser must be initialized with a filePath!" );
 		System.exit( 1 );
 	}
 	
 	/** Commands{} **********************************************************************
 	 *  Enumerations for the commands-types that exist for HACK (minus BAD_COMMAND).
 	 *  BAD_COMMAND is used strictly to set a default value for the command type in-case
 	 *  an unrecognized command is given. This, along with the L_COMMAND does not
 	 *  exist in the actual Hack Architecture specification.
 	 ***********************************************************************************/
 	public enum Commands { A_COMMAND, C_COMMAND, L_COMMAND, BAD_COMMAND }
 	
 	/** getInputFile() ******************************************************************
 	 *  Returns the current inputFile assigned to the Parser instance.  
 	 ***********************************************************************************/
 	public File getInputFile() 
 	{
 		return inputFile;
 	}
 	
 	/** setInputFile() ******************************************************************
 	 *  Sets the current inputFile assigned to the Parser instance. Once this is used,
 	 *  the instance must be reinitialized to notice the new file! 
 	 ***********************************************************************************/
 	private void setInputFile( File inputFile ) 
 	{
 		this.inputFile = inputFile;
 		/**
 		 * TODO:
 		 * Must complete proper init method for re-initialization
 		 */
 	}
 
 	/** getCurrentCommand() *************************************************************
 	 *  Returns the current command. 
 	 ***********************************************************************************/
 	public String getCurrentCommand() 
 	{
 		return currentCommand;
 	}
 	
 	/** setCurrentCommand() *************************************************************
 	 *  Sets the current command.
 	 ***********************************************************************************/
 	private void setCurrentCommand( String command ) 
 	{
 		this.currentCommand = command;
 	}
 	
 	/** getCommandLength() **************************************************************
 	 *  Returns the current command's length.
 	 ***********************************************************************************/
 	public int getCommandLength() 
 	{
 		return commandLength;
 	}
 	
 	/** setCommandLength() **************************************************************
 	 *  Sets the current command's length.
 	 ***********************************************************************************/
 	private void setCommandLength ( int commandLength )
 	{
 		this.commandLength = commandLength;
 	}
 	
 	/** hasMoreCommands() ***************************************************************
 	 *  Checks if there are more commands in the input. Stores a command
 	 ***********************************************************************************/
 	public boolean hasMoreCommands()
 	{
 		boolean hasMoreCommands = false;
 		
 		// Check if there is input in the next line, set boolean flag true if so
 		if( this.scanner.hasNextLine() )
 			hasMoreCommands = true;
 		
 		return hasMoreCommands;
 	}
 	
 	/** advance() ***********************************************************************
 	 *  Reads the next command from the input and makes it the current command. Should
 	 *  be called only if hasMoreCommands() is true. Initially there is no current
 	 *  command.
 	 ***********************************************************************************/
 	public void advance()
 	{
 		// Read in a line and set it as the current command
 		setCurrentCommand( this.scanner.nextLine() );
 		
 		// Read the current command and set the command length
 		setCommandLength( getCurrentCommand().length() );
 	}
 	
 	/** commandType() *******************************************************************
 	 *  Returns the type of the current command:
 	 *  	- A_COMMAND for @Xxx where Xxx is either a symbol or a decimal number.
 	 *  	- C_COMMAND for dest=comp;jump
 	 *  	- L_COMMAND a pseudo-command for (Xxx) where Xxx is a symbol.
 	 *  	- BAD_COMMAND is used to handle an unrecognized command.
 	 ***********************************************************************************/
 	public Commands commandType()
 	{
 		// Initialize default command
 		Commands cmd = Commands.BAD_COMMAND;
 		
 		// A-register commands always contain @ as a prefix
 		if( getCurrentCommand().contains( "@" ) )
 			cmd = Commands.A_COMMAND;
 		
 		// Computational command in form of comp;jump
 		if( getCurrentCommand().contains( ";" ) )
 			cmd = Commands.C_COMMAND;
 		
 		// Computational command in form of dest=comp
 		if( getCurrentCommand().contains( "=" ) )
 			cmd = Commands.C_COMMAND;
 		
 		return cmd;
 	}
 	
 	/** symbol() ************************************************************************
 	 *  Returns the symbol or decimal Xxx of the current command @Xxx or (Xxx). Should
 	 *  be called only when commandType() is A_COMMAND or L_COMMAND.
 	 ***********************************************************************************/
 	public String symbol()
 	{
 		// Initialize symbol and start indices
 		String symbol = "";
 		int startIndexAmp = -1;
 
 		// Grab the command starting after the @ and remove white spaces
		if( ( getCommandLength() > 0 ) && ( getCurrentCommand().contains( "@" ) ) )
 		{
 			startIndexAmp = getCurrentCommand().indexOf( "@" );
 			symbol = getCurrentCommand().substring( startIndexAmp + 1 ).trim();
 		}
 		
 		// Handle (Xxx) case
 		/**
 		 *  TODO:
 		 *  Complete basic implementation before handling the symbols for labeling 
 		 *  blocks.
 		 */
 		
 		return symbol;
 	}
 	
 	/** dest() **************************************************************************
 	 *  Returns the dest mnemonic in the current C-command (8 possibilities). Should
 	 *  be called only when commandType() is C_COMMAND.
 	 ***********************************************************************************/
 	public String dest()
 	{
 		// Initialize dest
 		String dest = "";
 		
 		// Split up the dest=comp command with the equal sign
 		String[] commandPieces = getCurrentCommand().split( "=" );
 		
 		// Take the first piece (the dest portion) and remove white spaces
 		dest = commandPieces[ 0 ].trim();
 		
 		return dest;
 	}
 	
 	/** comp() **************************************************************************
 	 *  Returns the comp mnemonic in the current C-command (28 possibilities). Should be
 	 *  called only when commandType() is C_COMMAND;
 	 ***********************************************************************************/
 	public String comp()
 	{
 		// Initialize comp
 		String comp = "";
 		
 		// Prepare the string array to hold command pieces after split
 		String[] commandPieces = null;
 		
 		// dest=comp case
 		if( getCurrentCommand().contains( "=" ) )
 		{
 			// Split up the dest=comp command with the equal sign
 			commandPieces = getCurrentCommand().split( "=" );
 			
 			// Take the second piece (comp portion) and remove white spaces
 			comp = commandPieces[ 1 ].trim();
 		}
 		
 		// comp;jump case
 		if( getCurrentCommand().contains( ";" ) )
 		{
 			// Split up the comp;jump command with the semi-colon
 			commandPieces = getCurrentCommand().split( ";" );
 			
 			// Take the first piece (comp portion) and remove white spaces
 			comp = commandPieces[ 0 ].trim();
 		}
 					
 		return comp;
 	}
 	
 	/** jump() **************************************************************************
 	 *  Returns the jump mnemonic in the current C-command (8 possibilities). Should be
 	 *  called only when the commandType() is C_COMMAND;
 	 ***********************************************************************************/
 	public String jump()
 	{
 		// Initialize jump
 		String jump = "";
 				
 		// Prepare the string array to hold command pieces after split
 		String[] commandPieces = null;
 		
 		// comp;jump case
 		if( getCurrentCommand().contains( ";") )
 		{
 			// Split up the comp;jump command with the semi-colon
 			commandPieces = getCurrentCommand().split( ";" );
 					
 			// Take the second piece (jump portion)
 			jump = commandPieces[ 1 ].trim();
 		}
 		
 		return jump;
 	}
 }
