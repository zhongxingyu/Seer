 package io.seq;
 
 import io.seq.Alphabet.AminoAcid;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PushbackReader;
 import java.io.Reader;
 import java.io.StringReader;
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
 	
 	
 	public class FastaSequence extends Sequence {
 		
 		Integer length;
 		
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
 					line = line.replace(" ", "");
 					
 					block.append(line);
 					
 					// track the detected length
 					if( length == null && line.length()>=20 ) { 
 						length = line.length();
 					}
 					
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
 				throw new QuickException(e, "Failure reading FASTA sequences around line: %s", lineCount);
 			}
 			
 			if( block.length()==0 ) {
				throw new QuickException("Empty sequence in FASTA block around line: %s", lineCount);
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
 			lineCount++; 
 			
 			StringBuilder result = new StringBuilder();
 			int ch;
 			int col=0;
 
 			while( (ch=reader.read()) != -1) {
 				col++;
 			
 				if( ch == ' ' && validChars != null ) {
 					// just skip 
 				} 
 				else if( ch != LINE_FEED && ch != CARRIAGE_RETURN ) {
 					/* check if the read character is valid */
 					if( validChars != null && !Utils.contains(validChars, (char)ch)) {
 						throw new QuickException("Invalid character '%c' (0x%s) reading FASTA sequences at line: %s, column: %s ", ch, Integer.toHexString(ch), lineCount, col); 
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
 		
 		/**
 		 * Reformat the sequence in fasta format
 		 */
 		public String toString() { 
 			/* 
 			 * 1. the header 
 			 */
 			StringBuilder result = new StringBuilder()
 				.append(">") .append(this.header).append("\n");
 
 			
 			/*
 			 * 2. format the sequence using has max block width the value defined in #length attribute
 			 */
 			char[] buffer = new char[ length != null ? length : 70 ]; // <-- default 70  
 			StringReader reader = new StringReader(this.value);
 			int len;
 			try { 
 				while( (len=reader.read(buffer)) != -1 ) { 
 					result.append(buffer,0,len) .append("\n");
 				}
 			} catch( IOException e ) { 
 				throw new RuntimeException(e);
 			}
 			
 			/* 3. return the result */
 			return result.toString();
  		}
 	}
 	
 
 	/** The default constructor */
 	public Fasta() { 
 		this(AminoAcid.INSTANCE);
 	}
 	
 	public Fasta( Alphabet alphabet ) {
 		super(alphabet);
 		this.sequences = new ArrayList<FastaSequence>();
 	}
 	
 
 	int lineCount=0;
 	
 	@Override
 	void parse( Reader reader ) {
 		Check.notNull(reader, "Argument reader cannot be null");
 
 		List<FastaSequence> result = new ArrayList<FastaSequence>();
 		PushbackReader input = new PushbackReader(reader); 
 		try {
 			int ch, prev=0;
 
 			while( (ch=input.read()) != -1 ) {
 				if( ch == '>' ) {
 					FastaSequence seq = new FastaSequence();
 					seq.parse(input,alphabet);
 					result.add(seq);
 				}
 				else if( ch == '\n' || ch == '\r' ) { 
 					// do nothing just consume the char 
 					if( (ch=='\n' && prev != '\r') || ch=='\r' ) { // <-- count \r\n sequence (windows line termination) only one time
 						lineCount++;
 					}
 				}
 				else if( ch == ';' ) { 
 					// remove all the line 
 					do { ch=input.read(); } 
 					while( ch != '\n' && ch != '\r' );
 					lineCount++;
 				}
 				else { 
 					error = String.format("Unrecognized character '%c' (0x%s) in FASTA sequences starting line: %s", ch, Integer.toHexString(ch), lineCount+1);
 					break;
 				}
 				
 				prev=ch;
 			}
 		}
 		catch( Exception e ) {
 			error = e.getMessage();
 		}
 		
 		sequences = result;
 	}
 	
 	public String toString() { 
 		if( sequences == null ) return null;
 		
 		StringBuilder result = new StringBuilder();
 		for( Sequence seq : sequences ) { 
 			result.append( seq.toString() );
 		}
 		
 		return result.toString();
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
 
 	public static Fasta read(File file) throws FileNotFoundException { 
 		Fasta fasta = new Fasta();
 		fasta.parse(file);
 		return fasta;
 	}
 
 	public static Fasta read(File file, Alphabet alphabet) throws FileNotFoundException { 
 		Fasta fasta = new Fasta(alphabet);
 		fasta.parse(file);
 		return fasta;
 	}
 
 }
