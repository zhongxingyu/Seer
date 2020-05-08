 package togos.schemaschema.parser.ast;
 
 import java.util.Arrays;
 
 import togos.lang.BaseSourceLocation;
 import togos.lang.SourceLocation;
 
 public class Phrase extends ASTNode
 {
	public static final Phrase EMPTY = new Phrase(new Word[0]);
	
 	public final Word[] words;
 	
 	public Phrase( Word[] words ) {
 		super( words.length == 0 ? BaseSourceLocation.NONE : words[0].sLoc );
 		this.words = words;
 	}
 
 	public Phrase( Word[] words, SourceLocation sLoc ) {
 		super( sLoc );
 		this.words = words;
 	}
 	
 	public String toString() {
 		String s = "";
 		for( Word w : words ) {
 			if( s.length() > 0 ) s += " ";
 			s += w.toString();
 		}
 		return s;
 	}
 	
 	public boolean startsWithWord( String word ) {
 		return (words.length > 0 && words[0].text.equals(word));
 	}
 	
 	public Phrase tail() {
 		if( words.length <= 1 ) return EMPTY;
 		return new Phrase( Arrays.copyOfRange(words, words.length-1, 1));
 	}
 	
 	public String unquotedText() {
 		String s = "";
 		for( Word w : words ) {
 			if( s.length() > 0 ) s += " ";
 			s += w.text;
 		}
 		return s;
 	}
 }
