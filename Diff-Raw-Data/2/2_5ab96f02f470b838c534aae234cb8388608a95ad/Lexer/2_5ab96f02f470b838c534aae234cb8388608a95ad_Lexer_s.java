 package kaygan;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Lexer
 {
 	private final CharReader reader;
 	
 	private final List<Token> buffer = new ArrayList<Token>();
 	
 	private int beginOffset;
 	
 	private final StringBuilder contentBuffer = new StringBuilder();
 	
 	/**
 	 * ignore carriage returns in token offsets, since Java
 	 * text editors ignore that when calculating character offsets
 	 */
 	int crCount = 0;
 	
 	public Lexer(Reader reader)
 	{
 		this.reader = new CharReader(reader, 2); // LA(2)
 	}
 	
 	public Token peek()
 	{
 		return peek(1);
 	}
 	
 	public Token peek(int lookAhead)
 	{
 		// populate the buffer with as many token as we need
 		for( int i=buffer.size(); i<=lookAhead+1; i++ )
 		{
 			buffer.add( read() );
 		}
 		
 		return buffer.get(lookAhead - 1);
 	}
 	
 	protected Token end(TokenType type)
 	{
 		String value = contentBuffer.toString();
 		
 		Token token = new Token(	type, 
 									beginOffset,
 									beginOffset + value.length(), 
 									value);
 		
 		// reset token state
 		contentBuffer.setLength(0);
 		
 		return token;
 	}
 	
 	protected void consume() throws IOException
 	{
 		contentBuffer.appendCodePoint( reader.read() );
 	}
 	
 	protected int peekChar() throws IOException
 	{
 		return reader.peek();
 	}
 	
 	protected void pushChar(int c) throws IOException
 	{
 		contentBuffer.setLength( contentBuffer.length() -1 );
 		reader.unread(c);
 	}
 	
 	protected boolean isWS(int c)
 	{
 		return c == ' ' || c == '\t' || c == '\r' || c == '\n';
 	}
 	
 	protected boolean isEOF(int c)
 	{
 		return c == -1 || c == 65535;
 	}
 	
 	protected boolean isBinaryDigit(int c)
 	{
 		return c == '0' || c == '1';
 	}
 	
 	protected boolean isHexDigit(int c)
 	{
 		return (c >= '0' && c <= '9')
 				|| (c >= 'a' && c <= 'f')
 				|| (c >= 'A' && c <= 'F');
 	}
 	
 	protected boolean isDigit(int c)
 	{
 		return c >= '0' && c <= '9';
 	}
 	
 	protected boolean isControl(int c)
 	{
 		return c == '{' || c == '}'
 			|| c == '(' || c == ')'
 			|| c == '[' || c == ']'
 			|| c == ':' || c == '.'
 			|| c == '|';
 	}
 	
 	protected boolean isSymbol(int c)
 	{
 		return !(isControl(c) || isEOF(c) || isWS(c));
 	}
 	
 	
 	public Token next()
 	{
 		if( buffer.size() > 0 )
 		{
 			return buffer.remove(0);
 		}
 		
 		return read();
 	}
 	
 	
 	protected Token read()
 	{
 		try
 		{
 			int c = peekChar();
 			
 			// eat any whitespace
 			if( isWS(c) )
 			{
 				do
 				{
 					int ws = reader.read();
 					if( ws == '\r' )
 					{
 						crCount++;
 					}
 				}
 				while( isWS( peekChar() ) );
 				
 				c = peekChar();
 			}
 			
 			beginOffset = reader.getOffset() - crCount;
 			
 			switch(c)
 			{
 			case '(': consume(); return end(TokenType.OPEN_PAREN);
 			case ')': consume(); return end(TokenType.CLOSE_PAREN);
 			
 			case '[': consume(); return end(TokenType.OPEN_BRACKET);
 			case ']': consume(); return end(TokenType.CLOSE_BRACKET);
 			
 			case '{': consume(); return end(TokenType.OPEN_BRACE);
 			case '}': consume(); return end(TokenType.CLOSE_BRACE);
 			
 			case '|': consume(); return end(TokenType.PIPE);
 			
 			case ':': consume(); return end(TokenType.COLON);
 			case '.':
 				consume();
 				if( peekChar() == '.' )
 				{
 					consume();
 					return end(TokenType.BETWEEN);
 				}
 				return end(TokenType.FULL_STOP);
 			}
 			
 			if( c == '"' )
 			{
 				do
 				{
 					// TODO escaping
 					consume();
 				}
				while( peekChar() != '"' );
 				
 				consume();
 				return end(TokenType.String);
 			}
 			
 			if( c == '/' )
 			{
 				consume();
 				
 				if( peekChar() == '*' )
 				{
 					// comment
 					consume_comment:
 					while(true)
 					{
 						consume();
 						
 						int peek = peekChar();
 						if( peek == '*' )
 						{
 							consume();
 							if( peekChar() == '/' )
 							{
 								consume();
 								break consume_comment;
 							}
 						}
 						else if( isEOF(peek) )
 						{
 							throw new IOException("EOF reached before end /*");
 						}
 					}
 					
 					return end(TokenType.Comment);
 				}
 			}
 			
 			if( c == '0' )
 			{
 				consume();
 				
 				int peek = peekChar();
 				if( peek == 'b' )
 				{
 					consume();
 					while( isBinaryDigit( peekChar() ) )
 					{
 						consume();
 					}
 					return end(TokenType.Binary);
 				}
 				else if( peek == 'x' )
 				{
 					consume();
 					while( isHexDigit( peekChar() ) )
 					{
 						consume();
 					}
 					return end(TokenType.Hex);
 				}
 				
 				// we already have a zero, so fall through
 				// to the number lexer (put back the zero we consumed)
 				pushChar(c);
 			}
 			
 			if( isDigit(c) )
 			{
 				do
 				{
 					consume();
 				}
 				while( isDigit( peekChar() ) );
 				
 				// we've read up to a non-digit,
 				// if we find a decimal point here read a real
 				
 				if( peekChar() == '.' && reader.peek2() != '.' )
 				{
 					do
 					{
 						consume();
 					}
 					while( isDigit( peekChar() ) );
 					
 					return end(TokenType.Real);
 				}
 	
 				// no decimal point, just an int
 				return end(TokenType.Int);
 			}
 			
 			if( isSymbol(c) )
 			{
 				// symbol part
 				int peek = peekChar();
 				while( isSymbol(peek) )
 				{
 					consume();
 					peek = peekChar();
 					if( peek == '.' && isSymbol(reader.peek2()) )
 					{
 						// symbolpart1.symbolpart2
 						consume(); // consume the '.'
 						peek = peekChar();
 					}
 				}
 				return end(TokenType.SymbolPart);
 			}
 			
 			return end(TokenType.EOF);
 		}
 		catch(IOException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 }
