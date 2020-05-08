 package biz.ixxi.analysis.wordending;
 
 import java.io.IOException;
 
 import java.util.ArrayList; 
 
 import org.apache.lucene.analysis.TokenFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
 import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
 import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
 import org.apache.lucene.util.AttributeSource;
 
 public class WordEndingFilter extends TokenFilter {
 
 	private AttributeSource.State current;
 	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
 	private char delimiter = '$';
 	private ArrayList<String> tokens = new ArrayList<String>();
 	private String mode;
 
 	public WordEndingFilter(final TokenStream input, final String mode) {
 		super(input);
 		this.mode = mode;
 	}
 
 	@Override
 	final public boolean incrementToken() throws IOException {
		if (mode == "autocomplete") {
 			return autocompleteMode(); // Slower
 		} else {
 			return defaultMode();
 		}
 	}
 
 	final private boolean defaultMode() throws IOException {
 		final boolean increment = input.incrementToken();
 		if (increment) {
 			termAtt.append(this.delimiter);
 		}
 		return increment;
 	}
 
 
 	/*
 	* Do not add boundary to last token.
 	*/
 	final private boolean autocompleteMode() throws IOException {
 		// First call, loop, store data, but do not return
 		while (input.incrementToken()) {
 			tokens.add(termAtt.toString());
 		}
         if (tokens.size() > 0) {
 			String token = tokens.get(0);
 			tokens.remove(0);
 			restoreState(current);
 			clearAttributes();
 			termAtt.copyBuffer(token.toCharArray(), 0, token.length());
 			termAtt.setLength(token.length());
 			if (tokens.size() > 0) {
 				termAtt.append(this.delimiter);
 			}
 			current = captureState();
 			return true;
         }
 		return false;
 	}
 }
