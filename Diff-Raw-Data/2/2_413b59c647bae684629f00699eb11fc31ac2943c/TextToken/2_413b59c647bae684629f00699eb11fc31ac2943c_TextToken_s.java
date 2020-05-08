 package name.kazennikov.tokens;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 public class TextToken extends AbstractToken {
 
 	
 	public TextToken(String src, int start, int end, TokenType type) {
 		super(src, start, end, type);
 	}
 	
 	public static TextToken valueOf(String src, int start, int end, TokenType type) {
 		return new TextToken(src, start, end, type);
 	}
 	
 	//public static TextToken makeSpace = TextToken.valueOf(" ", BaseTokenType.SPACE);
 	//public static final TextToken NEWLINE = TextToken.valueOf("\n", BaseTokenType.SPACE);
 	//public static final TextToken COMMA = TextToken.valueOf(",", BaseTokenType.PUNC);
 	//public static final TextToken DOT = TextToken.valueOf(".", BaseTokenType.PUNC);
 	//public static final TextToken EMPTY = TextToken.valueOf("", BaseTokenType.SPACE);
	public static final TextToken NULL = TextToken.valueOf(null, 0, 0, BaseTokenType.NULL);
 
 	@Override
 	public String toString() {
 		return String.format("#text:'%s'%s,%s", text(), type, properties);
 	}
 
 	@Override
 	public int size() {
 		return 0;
 	}
 
 	@Override
 	public AbstractToken getChild(int index) {
 		return null;
 	}
 
 }
