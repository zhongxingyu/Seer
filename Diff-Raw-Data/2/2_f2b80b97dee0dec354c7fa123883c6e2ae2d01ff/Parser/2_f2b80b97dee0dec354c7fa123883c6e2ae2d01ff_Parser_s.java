 package ash.parser;
 
 import java.io.Serializable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import bruce.common.utils.CommonUtils;
 
 public final class Parser {
 	private static final Pattern getFirstPlainTextPattern = Pattern.compile("(\\S+)\\s*");
 	private static final char ESCAPE_CHAR = '\\';
 	private static final char STRING_WRAPPING_CHAR = '\"';
 	private static final char QUOTE_CHAR = '\'';
 
 	private Parser() {}
 	
 	protected static Serializable createAst(String readIn) {
 		if (readIn.charAt(0) == '(') {
 			String unWrapped = unWrapped(readIn);
 			return CommonUtils.isStringNullOrWriteSpace(unWrapped) ? Node.NIL : split(unWrapped);
 		} else
 			return readIn;
 	}
 
 	private static String unWrapped(String exp) {
 		if (exp.charAt(0) == '(' && exp.charAt(exp.length() - 1) == ')')
 			return exp.substring(1, exp.length() - 1);
 		throw new UnsupportedOperationException("Can not Unwrap:" + exp);
 	}
 
 	public static Node split(String str) {
 		String trim = str.trim();
 		boolean quoteSugar = trim.charAt(0) == QUOTE_CHAR;
 		String first = quoteSugar ? getFirst(trim.substring(1)) : getFirst(trim);
 		String rest = getRest(trim, quoteSugar ? first.length() + 1 : first.length());
 		
		Serializable ast = quoteSugar ? new Node("quote", new Node(createAst(first))) : createAst(first);
 		if (CommonUtils.isStringNullOrWriteSpace(rest))
 			return new Node(ast);
 		else
 			return new Node(ast, split(rest));
 	}
 
 	private static String getRest(String str, int firstStrLen) {
 		return str.substring(firstStrLen);
 	}
 
 	private static String getFirst(String str) {
 		char headCh = str.charAt(0);
 		return headCh == '(' || headCh == STRING_WRAPPING_CHAR
 				? str.substring(0, getFirstElemLen(str, 0, 0, '\0'))
 				: getHeadPlainText(str);
 	}
 
 	protected static String getHeadPlainText(String str) {
 		Matcher m = getFirstPlainTextPattern.matcher(str);
 		m.find();
 		return m.group(1);
 	}
 
 	private static int getFirstElemLen(String src, int balance, int elemLen, char spanChar) {
 		if (elemLen != 0 && balance == 0 && spanChar == '\0') return elemLen;
 		
 		final char c = src.charAt(elemLen);
 		return getFirstElemLen(src,
 				spanChar == STRING_WRAPPING_CHAR
 					? balance
 					: balance + (c == '(' ? 1 : (c == ')' ? -1 : 0)),
 				elemLen + (c == ESCAPE_CHAR ? 2 : 1),
 				STRING_WRAPPING_CHAR == c ? (spanChar == c ? '\0' : c) : spanChar);
 	}
 }
