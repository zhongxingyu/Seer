 package io.seq;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.List;
 
 import play.Logger;
 
 /**
  * An abstract MSA format reader 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public abstract class AbstractFormat {
 
 	protected static final int LINE_FEED = '\n';
 	
 	protected static final int CARRIAGE_RETURN = '\r';
 	
 	final Alphabet alphabet;
 	
 	/** The list of sequences */
	List<? extends Sequence> sequences;
 	
 	/** The error message if parsing fail */
 	String error; 
 	
 	protected AbstractFormat( Alphabet alpha ) { 
 		this.alphabet = alpha;
 	}
 	
 	public boolean isValid() {
		boolean result = !isEmpty() && minLength()>0;
 		
 		/* check that all sequences have a non-empty name */
 		if( result && sequences != null ) for( Sequence seq : sequences )  { 
 
 			if( seq.header == null || "".equals(seq.header.trim()) ) { 
 				// on first sequence with empty name return false
 				error = "All sequence must have a valid non-empty name (ID)";
 				return false;
 			}
 		}
 		
 		return result;
 	} 	
 	
 	public boolean isEmpty() {
 		return sequences == null || sequences.size() == 0;
 	}
 	
 	public int count() {
 		return sequences != null ? sequences.size() : 0;
 	} 
 
 	public int minLength() {
 		if( sequences==null ) {
 			return 0;
 		}
 		
 		int min = Integer.MAX_VALUE;
 		for( Sequence seq : sequences ) {
 			if( seq.value!=null && min > seq.value.length() ) {
 				min = seq.value.length();
 			} 
 		}
 		
 		return min!=Integer.MAX_VALUE ? min : 0;
 	}
 		
 	public int maxLength() {
 		if( sequences==null ) {
 			return 0;
 		}
 		
 		int max = 0;
 		for( Sequence seq : sequences ) {
 			if( seq.value!=null && max<seq.value.length() ) {
 				max = seq.value.length();
 			} 
 		}
 		
 		return max;
 	}
 	
 	public void parse( File file ) throws FileNotFoundException { 
 		try {
 			parse( new FileReader(file) );
 		} 
 		catch (IOException e) {
 			error = "File is not Clustal format";
 			Logger.error(e, "The following is not in Clustal format: '%s'", file);
 		}
 	}
 
 	public void parse( String sequences )  { 
 		try {
 			parse( new StringReader(sequences) );
 		} 
 		catch (IOException e) {
 			error = "The sequences provided are not in Clustal format";
 			Logger.error(e, "The sequences are not in Clustal format:\n'%s'", sequences);
 		}
 	}
 	
 	
 	abstract void parse( Reader reader ) throws IOException ;
 	
 	
 }
