 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package datext;
 
 import datext.io.StreamHandler;
 import java.io.IOException;
 import java.io.Reader;
 
 /**
  * Uses a FSM to parse a character stream to a DaText tree.
  * @author  Christopher Collin Hall
  */
 public class DefaultDaTextParser extends DaTextParser{
 
 	
 	
 	@Override
 	public DaTextObject parse(Reader in) throws IOException {
 		Parser p = new Parser(in);
 		try{return p.parse();}finally{in.close();}
 	}
 	
 	
 	
 	
 	/** These are the states of the FSM for parsing the file */
 	protected enum ReadState {
 		/** between &#47;* and *&#47; */
 		MULTILINE_COMMENT,
 		/** between &#47;&#47; and the end of the line */
 		LINE_COMMENT,
 		/** between # and the end of the line */
 		ANNOTATION,
 		/** reading a keyname */
 		KEY,
 		/** skipping whitespace */
 		WHITESPACE,
 		/** reading a quoted string */
 		QUOTE,
 		/** reading a quoted string */
 		SEMIQUOTE,
 		/** reading an object */
 		CURLY_BRACKET,
 		/** reading a list */
 		SQUARE_BRACKET,
 		/** reading the value of a key */
 		VALUE
 	}
 	
 	/**
 	 * This class does the heavy-lifting for the parsing. The FSM actually 
 	 * resides inside this class to avoid thread problems.
 	 */
 	protected class Parser {
 
 		/**
 		 * FSM state
 		 */
 		ReadState state = ReadState.WHITESPACE; // starting FSM state
 		/** input stream */
 		final StreamHandler input;
 		
 		Parser(Reader r) {
 			input = new StreamHandler(r);
 		}
 		private Parser(StreamHandler r) {
 			input = (r);
 		}
 		
 		
 
 		StringBuilder annotBuffer = new StringBuilder();
 		StringBuilder keynameBuffer = new StringBuilder();
 		StringBuilder valueBuffer = new StringBuilder();
 		
 		DaTextObject parse() throws IOException, IllegalArgumentException {
 			return parse(1);
 		}
 		
 		DaTextObject parse(int line) throws IOException, IllegalArgumentException {
 			DefaultObject obj = new DefaultObject();
 			ReadState stateBeforeComment = null;
			// TODO: remove debugging stuff
 			ReadState old = null;
 			
 			boolean done = false;
 			do{
 				
 				if(old != state){
 	//				System.out.println("\n\t"+state.name());
 					old = state;
 				}
 	//			System.out.print(input.peekCurrent());
 				
 				Character c = input.readNextChar();
 				
 				if(c == null){
 					// pad the end of the stream with a new-line
 					done = true;
 					c = '\n';
 				}
 				if (c == '\n') {
 					line++;
 				}
 				
 				
 				// comment handling
 				if (c == '/' && input.peekNext() == '/') {
 					stateBeforeComment = state;
 					state = ReadState.LINE_COMMENT;
 				} else if (c == '/' && input.peekNext() == '*') {
 					stateBeforeComment = state;
 					state = ReadState.MULTILINE_COMMENT;
 					// skip past the comment-start string
 					input.readNextChar();
 					input.readNextChar(); 
 				}
 				switch(state){
 					// Finite state machine (FSM)
 					case WHITESPACE:
 						// skip whitespace;
 						if(!Character.isWhitespace(c)){
 							if(c == '#'){
 								state = ReadState.ANNOTATION;
 							} else {
 								keynameBuffer = new StringBuilder();
 								state = ReadState.KEY;
 								keynameBuffer.append(c);
 							}
 						}
 						break;
 					case ANNOTATION:
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == '\n' ){
 							state = ReadState.WHITESPACE;
 						}
 						
 						annotBuffer.append(c);
 						break;
 					case KEY:
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == '='){
 							state = ReadState.VALUE;
 							break;
 						} else if(c == '\n' ){
 							// value-less kay
 							state = ReadState.WHITESPACE;
 							obj.put(keynameBuffer.toString().trim());
 							break;
 						}
 						keynameBuffer.append(c);
 						
 						break;
 					case VALUE:
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == '"'){
 							state = ReadState.QUOTE;
 							break;
 						} else if(c == '\''){
 							state = ReadState.SEMIQUOTE;
 							break;
 						} else if(c == '['){
 							valueBuffer.append(c);
 							state = ReadState.SQUARE_BRACKET;
 							break;
 						} else if(c == '{'){
 							valueBuffer.append(c);
 							state = ReadState.CURLY_BRACKET;
 							break;
 						} else if(c == '\n'){
 							// store the value
 							if(annotBuffer.length() > 0){
 								obj.put(keynameBuffer.toString().trim(), new DefaultVariable(valueBuffer.toString().trim(), annotBuffer.toString().trim()));
 							} else {
 								obj.put(keynameBuffer.toString().trim(), new DefaultVariable(valueBuffer.toString().trim()));
 							}
 							state = ReadState.WHITESPACE;
 							annotBuffer = new StringBuilder();
 							keynameBuffer = new StringBuilder();
 							valueBuffer = new StringBuilder();
 							break;
 						} 
 						valueBuffer.append(c);
 						
 						break;
 					case QUOTE:
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == '"'){
 							state = ReadState.VALUE;
 						}
 						valueBuffer.append(c);
 						break;
 					case SEMIQUOTE:
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == '\''){
 							state = ReadState.VALUE;
 						}
 						valueBuffer.append(c);
 						break;
 					case SQUARE_BRACKET:
 						// ignore new-lines when in list mode
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == ']'){
 							state = ReadState.VALUE;
 						}
 						valueBuffer.append(c);
 						break;
 					case CURLY_BRACKET:
 						// ignore new-lines when in list mode
 						if(c == '\\'){
 							c = input.readNextChar();
 							if(c == null){
 								throw new IllegalArgumentException("DaText Format Error on line ["+line+"]: Unexpected end-of-file after escape character \\");
 							}
 						} else if(c == '}'){
 							state = ReadState.VALUE;
 						}
 						valueBuffer.append(c);
 						break;
 					case LINE_COMMENT:
 						if(input.peekNext() == '\n' || input.peekNext() == null){
 							// preserve end-line behavior of previous state
 							state = stateBeforeComment;
 							stateBeforeComment = null;
 						}
 						break;
 					case MULTILINE_COMMENT:
 						if(c == '*' && input.peekNext() == '/'){
 							state = stateBeforeComment;
 							stateBeforeComment = null;
 						}
 						break;
 					default:
 						// Injection of additional enums?!
 						throw new UnsupportedOperationException("WTF!!! Where did '" + state.name() + "' come from!");
 				}
 			} while(!done);
 			return obj;
 		}
 	}
 	
 }
