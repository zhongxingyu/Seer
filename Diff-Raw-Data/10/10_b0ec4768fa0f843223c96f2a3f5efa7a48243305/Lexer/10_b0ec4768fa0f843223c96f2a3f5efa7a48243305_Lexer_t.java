 package infixpp.ast.parser;
 
 import infixpp.ast.parser.exception.LexerException;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Lexer
 {
 	private static Map<String, Kind> keywords = new HashMap<String, Kind>();
 
 	static
 	{
 		keywords.put("Define", Kind.KW_DEF);
 		keywords.put("Order", Kind.KW_ORDER);
 		keywords.put("Translate", Kind.KW_TRANSLATE);
 		keywords.put("End", Kind.KW_END);
 		keywords.put("left", Kind.KW_LEFT);
 		keywords.put("right", Kind.KW_RIGHT);
 	}
 
 	private char[] cs;
 	private int p;
 	private int line;
 	private int column;
 	private int startColumn;
 	private StringBuilder buf = new StringBuilder();
 
 	public Lexer(String text)
 	{
 		cs = text.toCharArray();
 		line = 1;
 		column = 1;
 	}
 
 	public Token lex(boolean inTranslate) throws LexerException
 	{
 		skipws();
 
 		if (end())
 		{
 			return new Token(Kind.END, "$", Location.of(line, p + 1));
 		}
 
 		startColumn = column;
 
 		switch (peek())
 		{
 		case '(':
 			succ();
 			return token(Kind.L_PAREN, "(");
 		case ')':
 			succ();
 			return token(Kind.R_PAREN, ")");
 		case '.':
 			if (!inTranslate)
 			{
 				succ();
 				return token(Kind.DOT, ".");
 			}
 			break;
 		case ',':
 			if (!inTranslate)
 			{
 				succ();
 				return token(Kind.COMMA, ",");
 			}
 			break;
 		case '"':
 			return lexLiteral();
 		case '\'':
 			return lexOperatorSymbol();
 		case ':':
 			if (!inTranslate)
 			{
 				succ();
 				if (peek() == '=')
 				{
 					succ();
 					return token(Kind.DEF, ":=");
 				}
 				throw new LexerException("missing '='", Location.of(line, column));
 			}
 			break;
 		case '<':
 			if (!inTranslate)
 			{
 				succ();
 				return token(Kind.WEAKER, "<");
 			}
 			break;
 		case '#':
 			if (!inTranslate)
 			{
 				succ();
 				while (!end() && peek() != '\n') succ();
 				return lex(inTranslate);
 			}
 			break;
 		}
 
 		if (Character.isDigit(peek()))
 		{
 			return lexInteger();
 		}
 		else if (isAlpha(peek()))
 		{
 			buf.setLength(0);
 			while (isAlpha(peek()))
 			{
 				buf.append(peek());
 				succ();
 			}
 			String s = buf.toString();
 			Kind kind = keywords.get(s);
 			if (kind != null)
 			{
 				return token(kind, s);
 			}
 			throw new LexerException("unknown keyword '" + s + "'.", Location.of(line, column));
 		}
 
 		buf.setLength(0);
 		while (!end() && !Character.isWhitespace(peek()))
 		{
 			buf.append(peek());
 			succ();
 		}
 		return token(Kind.OPERATOR, buf.toString());
 	}
 
 	private Token lexInteger() throws LexerException
 	{
 		int value = 0;
 		while (Character.isDigit(peek()))
 		{
 			value = 10 * value + (peek() - '0');
 			succ();
 		}
 		return new Token(Kind.INTEGER, value, Location.of(line, column));
 	}
 
 	private Token lexLiteral() throws LexerException
 	{
 		buf.setLength(0);
 		if (peek() == '"')
 		{
 			succ();
 			while (!end() && peek() != '"' && peek() != '\n')
 			{
 				buf.append(peek());
 				succ();
 			}
 			if (peek() == '"')
 			{
 				succ();
 				return token(Kind.LITERAL, buf.toString());
 			}
 		}
 		throw new LexerException("invalid literal.", Location.of(line, startColumn));
 	}
 
 	private Token lexOperatorSymbol() throws LexerException
 	{
 		buf.setLength(0);
 		if (peek() == '\'')
 		{
 			succ();
 			skipws();
			while (!end() && peek() != '\'' && !Character.isWhitespace(peek()))
 			{
 				buf.append(peek());
 				succ();
 			}
 			skipws();
 			if (peek() == '\'')
 			{
 				succ();
 				return token(Kind.OPERATOR_SYMBOL, buf.toString());
 			}
 		}
 		throw new LexerException("invalid operator symbol.", Location.of(line, column));
 	}
 
 	private Token token(Kind kind, String text)
 	{
		return new Token(kind, text, Location.of(line, startColumn));
 	}
 
 	private void skipws()
 	{
 		while (!end() && Character.isWhitespace(peek())) succ();
 	}
 
 	private char peek()
 	{
 		return !end() ? cs[p] : 0;
 	}
 
 	private void succ()
 	{
 		if (!end())
 		{
 			if (peek() == '\n')
 			{
 				++line;
 				column = 1;
 			}
 			else
 			{
 				++column;
 			}
 			++p;
 		}
 	}
 
 	private boolean end()
 	{
 		return p >= cs.length;
 	}
 
 	private static boolean isAlpha(char c)
 	{
 		return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z';
 	}
 }
