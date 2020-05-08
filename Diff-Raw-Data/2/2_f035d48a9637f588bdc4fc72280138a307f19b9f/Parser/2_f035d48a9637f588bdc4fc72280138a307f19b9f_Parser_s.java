 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import Token.Type;
 
 
 public class Parser
 {
 	private InputSource _in;
 	private String  	_line;
 	
 	private Map<String, Token.Type> _tokens;
 	
 	public Parser(InputSource source)
 	{
 		if (source == null)
 			throw new IllegalArgumentException("InputSource can't be null");
 		_in = source;
 		_line = _in.readLine();
 		this.initTokens();
 	}
 	
 	private void initTokens()
 	{
 		_tokens = new HashMap<String, Token.Type>();
 		_tokens.put("(", Token.Type.OPEN_PARENTHESIS);
 		_tokens.put(")", Token.Type.CLOSE_PARENTHESIS);
 		_tokens.put("&", Token.Type.BIN_AND_OPERATOR);
 		_tokens.put("|", Token.Type.BIN_OR_OPERATOR);
 		_tokens.put("=>", Token.Type.EQUALS_OPERATOR);
 		_tokens.put("~", Token.Type.UNARY_NOT_OPERATOR);
 	}
 	
 	public Token nextToken()
 	{
 		if (_line.length() == 0)
 			_line = _in.readLine();
 		if (_line.length() == 0)
 			return null;
 		Iterator<Entry<String, Token.Type>> it = _tokens.entrySet().iterator(); 
 		while (it.hasNext())
 		{
 			Entry<String, Token.Type> entry = it.next();
 			if (_line.startsWith(entry.getKey()))
 			{
 				String value = _line.substring(0, entry.getKey().length());
 				_line = _line.substring(value.length());
 				return new Token(entry.getValue(), value);
 			}
 		}
 		Character c = _line.charAt(0);
 		if (!Character.isLowerCase(c))
 			throw new IllegalLineException("`" + c + "' is not a valid token");
		return new Token(Token.Type.VARIABLE, c);
 	}
 }
