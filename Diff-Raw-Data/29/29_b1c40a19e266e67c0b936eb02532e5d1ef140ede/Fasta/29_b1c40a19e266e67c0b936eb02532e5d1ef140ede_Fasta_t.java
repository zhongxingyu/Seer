 package io.seq;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PushbackReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 
 import play.Logger;
 import util.Check;
 import util.Utils;
 import exception.QuickException;
 
 /**
  * Model the content of a FASTA file 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class Fasta extends AbstractFormat {
 	
 	
 	static public class FastaSequence extends Sequence {
 		
 		
 		void parse( PushbackReader reader, Alphabet alphabet ) {
 			StringBuilder block = new StringBuilder();   
 
 			boolean stop;
 			try {
 				/* first line is defined as the header */
 				header = readLine(reader, null);
 				/* read the sequence block */
 
 				do {
 					String line = readLine(reader,alphabet.letters());
 					if( Utils.isEmpty(line) ) { 
 						stop=true;
 						break;
 					}
 					
 					block.append(line);
 					
 					/* what's next ? */
 					int ch = reader.read();
 					stop = !alphabet.isValidChar((char)ch); 
 					if( ch != -1 ) {
 						/* pushback and continue reading */
 						reader.unread(ch);
 					}
 					
 				} 
 				while( !stop );				
 			}
 			catch( IOException e ) {
 				throw new QuickException(e, "Failure reading FASTA stream");
 			}
 			
 			if( block.length()==0 ) {
 				throw new QuickException("Empty sequence in FASTA block");
 			}
 			
 			value = block.toString();
 		}
 
 
 		/**
 	     * Reads a line of text.  A line is considered to be terminated by any one
 	     * of a line feed ('\n'), a carriage return ('\r'), or a carriage return
 	     * followed immediately by a linefeed.
 	     *
 	     *
 	     * @return     A String containing the contents of the line, not including
 	     *             any line-termination characters, or null if the end of the
 	     *             stream has been reached
 		 * @throws IOException 
 	     */
 		String readLine(PushbackReader reader, char[] validChars) throws IOException {
 			StringBuilder result = new StringBuilder();
 			int ch;
 			
 			while( (ch=reader.read()) != -1) {
 				if( ch != LINE_FEED && ch != CARRIAGE_RETURN ) {
 					/* check if the read character is valid */
 					if( validChars != null && !Utils.contains(validChars, (char)ch)) {
 						throw new QuickException("Invalid character '%c' parsing line", ch); 
 					}
 					
 					result.append((char)ch); // <-- LOOK at the cast!
 				} 
 				else {
 					/* exit the loop 
 					 * but before if we meet a CR - LF sequence */
 					if( ch == CARRIAGE_RETURN && (ch=reader.read()) != LINE_FEED ) {
 						reader.unread(ch);
 					}
 					break;
 				}
 			}
 
 			
 			return result.toString();
 		}
		
		public String toString() { 
			return ">" + this.header + "\n" + this.value;
 		}
 	}
 	
 
 	/** The default constructor */
 	public Fasta( Alphabet alphabet ) {
 		super(alphabet);
 		this.sequences = new ArrayList<FastaSequence>();
 	}
 	
 	
 	@Override
 	void parse( Reader reader ) {
 		Check.notNull(reader, "Argument reader cannot be null");
 
 		List<FastaSequence> result = new ArrayList<FastaSequence>();
 		PushbackReader input = new PushbackReader(reader); 
 		try {
 			int ch;
 			while( (ch=input.read()) != -1 ) {
 				if( ch == '>' ) {
 					FastaSequence seq = new FastaSequence();
 					seq.parse(input,alphabet);
 					result.add(seq);
 				}
 				else if( ch == '\n' || ch == '\r' ) { 
 					// do nothing just consume the char 
 				}
 				else if( ch == ';' ) { 
 					// remove all the line 
 					do { ch=input.read(); } 
 					while( ch != '\n' && ch != '\r' );
 				}
 				else { 
 					error = "Invalid FASTA format";
 					break;
 				}
 			}
 		}
 		catch( Exception e ) {
 			error = e.getMessage();
 		}
 		
 		sequences = result;
 	}
 
 
 	/**
 	 * @param file the file to be checked 
 	 * @return <code>true</code> if the specified file a valid content in FASTA format 
 	 */
 	public static boolean isValid(File file, Alphabet alphabet) {
 		try {
 			Fasta fasta = new Fasta(alphabet);
 			fasta.parse(file);
 			return fasta.isValid();
 		} 
 		catch (FileNotFoundException e) {
 			Logger.warn("Specified FASTA file does not exists: %s", file);
 			return false;
 		}
 	}
 
 	public static boolean isValid(String sequences, Alphabet alphabet) {
 		Fasta fasta = new Fasta(alphabet);
 		fasta.parse(sequences);
 		return fasta.isValid();
 	}
 
 
 
 }
