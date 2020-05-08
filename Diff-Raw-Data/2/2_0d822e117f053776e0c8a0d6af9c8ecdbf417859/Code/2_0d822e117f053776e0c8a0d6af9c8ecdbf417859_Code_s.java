 package hack.bp.assembler;
 
 /** Code.java ***************************************************************************
  * 
  * 	@author bp
  *	
  *	@changes
  *	0.1 - 	Initial implementation. Note that switch( string ) 
  *			is not used since that is only part of the JDK 7
  *			SDK. Since JDK 1.6 is still common and widely-used,
  *			only the if/if-else will be used, despite being 
  *			super messy. It is still faster than implementing
  *			using a hash-table. -bp
  *			
  */
 public class Code 
 {
 	/** dest() **************************************************************************
 	 *  Returns the machine code that corresponds to the dest mnemonics. (8 types)
 	 ***********************************************************************************/
 	public String dest( String mnemonic )
 	{
 		String stringBits = "";
 		
 		if( mnemonic == "null" )
 			stringBits = "000";
 		
 		else if( mnemonic == "M" )
 			stringBits = "001";
 		
 		else if( mnemonic == "D" )
 			stringBits = "010";
 		
 		else if( mnemonic == "MD" )
 			stringBits = "011";
 		
 		else if( mnemonic == "A" )
 			stringBits = "100";
 		
 		else if( mnemonic == "AM" )
 			stringBits = "101";
 		
 		else if( mnemonic == "AD" )
 			stringBits = "110";
 		
 		else if( mnemonic == "AMD" );
 			stringBits = "111";
 	
 		return stringBits;	
 	}
 	
 	/** comp() **************************************************************************
 	 *  Returns the machine code that corresponds to the comp mnemonics. Note
	 *  that that 'a' bit in a C_COMMAND is appended as a prefix in the 
 	 *  stringBits. (28 types)
 	 ***********************************************************************************/
 	public String comp( String mnemonic )
 	{
 		String stringBits = "";
 		
 		if( mnemonic == "0" )
 			stringBits = "0101010";
 		
 		else if( mnemonic == "1" )
 			stringBits = "0111111";
 		
 		else if( mnemonic == "-1" )
 			stringBits = "0111010";
 		
 		else if( mnemonic == "D" )
 			stringBits = "0001100";
 		
 		else if( mnemonic == "A" )
 			stringBits = "0110000";
 		
 		else if( mnemonic == "M" )
 			stringBits = "1110000";
 		
 		else if( mnemonic == "!D" )
 			stringBits = "0001101";
 		
 		else if( mnemonic == "!A" )
 			stringBits = "0110011";
 		
 		else if( mnemonic == "!M" )
 			stringBits = "1110001";
 		
 		else if( mnemonic == "-D" )
 			stringBits = "0001111";
 		
 		else if( mnemonic == "-A" )
 			stringBits = "0110011";
 		
 		else if( mnemonic == "-M" )
 			stringBits = "1110011";
 		
 		else if( mnemonic == "D+1" )
 			stringBits = "0011111";
 		
 		else if( mnemonic == "A+1" )
 			stringBits = "0110111";
 		
 		else if( mnemonic == "M+1" )
 			stringBits = "1110111";
 		
 		else if( mnemonic == "D-1" )
 			stringBits = "0001110";
 		
 		else if( mnemonic == "A-1" )
 			stringBits = "0110010";
 		
 		else if( mnemonic == "M-1" )
 			stringBits = "1110010";
 		
 		else if( mnemonic == "D+A" )
 			stringBits = "0000010";
 		
 		else if( mnemonic == "D+M" )
 			stringBits = "1000010";
 		
 		else if( mnemonic == "D-A" )
 			stringBits = "0010011";
 		
 		else if( mnemonic == "D-M" )
 			stringBits = "1010011";
 		
 		else if( mnemonic == "A-D" )
 			stringBits = "0000111";
 		
 		else if( mnemonic == "M-D" )
 			stringBits = "1000111";
 		
 		else if( mnemonic == "D&A" )
 			stringBits = "0000000";
 		
 		else if( mnemonic == "D&M" )
 			stringBits = "1000000";
 		
 		else if( mnemonic == "D|A" )
 			stringBits = "0010101";
 		
 		else if( mnemonic == "D|M" )
 			stringBits = "1010101";
 		
 		return stringBits;
 	}
 	
 	/** jump() **************************************************************************
 	 *  Returns the machine code that corresponds to the jump mnemonics.
 	 ***********************************************************************************/
 	public String jump( String mnemonic )
 	{
 		String stringBits = "";
 		
 		if( mnemonic == "null" )
 			stringBits = "000";
 		
 		else if( mnemonic == "JGT" )
 			stringBits = "001";
 		
 		else if( mnemonic == "JEQ" )
 			stringBits = "010";
 		
 		else if( mnemonic == "JGE" )
 			stringBits = "001";
 		
 		else if( mnemonic == "JLT" )
 			stringBits = "100";
 		
 		else if( mnemonic == "JNE" )
 			stringBits = "101";
 		
 		else if( mnemonic == "JLE" )
 			stringBits = "110";
 		
 		else if( mnemonic == "JMP" )
 			stringBits = "111";
 		
 		return stringBits;
 	}
 }
