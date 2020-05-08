 package hack.bp.assembler;
 
import java.util.Timer;
 
 public class Assembler 
 {
 	public static void main(String[] args) 
 	{
 		if( args.length > 0 )
 		{
 			if( args[ 0 ].endsWith( ".asm" ) )
 				run( args[ 0 ] );
 			else
 				System.out.println( "Usage: <program> <fileName> " +
 					"\n\t -Can only use file with .asm extension.");
 		}
 		else
 			System.out.println( "Usage: <program> <fileName> " +
 									"\n\t -Enter an .asm file.");
 	}
 	
 	public static void run( String fileName )
 	{
 		Parser parser = new Parser( fileName );
 		Code code = new Code();
 		String output = "";
 		final int ADDRESS_LENGTH = 15;
 		
 		// Start reading the file
 		while( parser.hasMoreCommands() )
 		{
 			parser.advance();
 			
 			// Handle A_COMMAND
 			if( parser.commandType() == Parser.Commands.A_COMMAND )
 			{
 				String tempCode = "";
 				int decAddress = Integer.parseInt( parser.symbol() );
 				tempCode = Integer.toBinaryString( decAddress );
 				
 				if( ( tempCode.length() < ADDRESS_LENGTH ) && ( tempCode.length() > 0 ) )
 				{
 					int paddingCount = ADDRESS_LENGTH - tempCode.length();
 					
 					// append the A_COMMAND prefix
 					output += "0";
 					
 					for( int i = 0; i < paddingCount; i++ )
 						output += "0";
 					
 					output += tempCode;
 					output += "\n";
 				}
				
				System.out.println( output );
 			}
 		}
 		
		System.out.println( "Assembly completed!" );
 	}
 }
