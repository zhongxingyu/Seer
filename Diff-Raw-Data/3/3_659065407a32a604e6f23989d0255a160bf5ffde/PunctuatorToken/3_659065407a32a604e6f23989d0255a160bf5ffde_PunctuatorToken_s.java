 package titocc.tokenizer;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Operator or other punctuator.
  */
 public class PunctuatorToken extends Token
 {
 	// List of all punctuators. An important fact is that all multi-character
 	// punctuators start with some other punctuator (e.g. "<<=" contains "<<"
 	// which contains "<"). This means that when an unmatching character is
 	// encountered only that character needs to be unread back into the reader.
 	private static final String[] punctuatorList = {
 		"+",
 		"++",
 		"+=",
 		"-",
 		"--",
 		"-=",
 		"*",
 		"*=",
 		"/",
 		"/=",
 		"%",
 		"%=",
 		"|",
 		"||",
 		"&",
 		"&&",
 		"~",
 		"~=",
 		"^",
 		"^=",
 		"!",
 		"=",
 		">>",
 		">>=",
 		"<<",
 		"<<=",
 		"==",
 		"!=",
 		"<",
 		"<=",
 		">",
 		">=",
 		"{",
 		"}",
 		"(",
 		")",
 		";",
 		","
 	};
 	private static Set<String> punctuators = new HashSet<String>(Arrays.asList(punctuatorList));
 
 	private PunctuatorToken(String string, int line, int column)
 	{
 		super(string, line, column);
 	}
 
 	public static PunctuatorToken parse(CodeReader reader) throws IOException
 	{
 		PunctuatorToken token = null;
 		int line = reader.getLineNumber(), column = reader.getColumn();
 
 		StringBuilder tokenString = new StringBuilder();
 
 		char c = reader.read();
 		while (punctuators.contains(tokenString.toString() + c)) {
 			tokenString.append(c);
 			c = reader.read();
 		}
 
 		if (tokenString.length() > 0)
 			token = new PunctuatorToken(tokenString.toString(), line, column);
 
 		if (c != '\0')
 			reader.unread();
 
 		return token;
 	}
 }
