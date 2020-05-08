 package toybox.lexer;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import toybox.util.IO;
 import toybox.util.Location;
 
 /**
  * A base for creating simple lexers. Token rules are specified using @Token
  * annotations. There are two main strategies for creating lexers:
  * <ul>
  *   <li>Extend the <code>ToyLexer</code> class and annotate at least one method
  *       with a <code>@Token</code> annotation.</li>
  *   <li>Create a <code>ToyLexer</code> instance and add objects that contain
  *       <code>@Token</code>-annotated methods, either directly in the constructor
  *       or via the <code>add</code> method.</li>
  * </ul>
  * 
  * Note that when searching for token rules, methods in superclasses are
  * considered recursively as well.
  * 
  * @author Bruno Dufour (dufour@iro.umontreal.ca)
  *
  * @param <T> The token type
  * @see toybox.lexer.Token
  * 
  */
 public class ToyLexer<T> {
 	/*
 	 * To Do:
 	 *   - Make sure the lexer picks the longest match, rather than the first one
 	 *   - Make sure that overriden/inaccessible methods don't get registered
 	 *   - Add states  
 	 */
 	private List<TokenRule> rules = new LinkedList<TokenRule>();
 	private String input; 	
 	private int pos;
 	private int lineno;
 	private int column;
 	
 	public ToyLexer() {
 		add(this);
 	}
 	
 	public ToyLexer(Object... rules) {
 		for (Object r: rules) {
 			add(r);
 		}
 	}
 
 	public void input(File f) throws IOException {
 		input(new BufferedReader(new FileReader(f)));
 	}
 	
 	public void input(Reader r) throws IOException {
 		this.input(IO.readFully(r));
 	}
 	
 	public void input(String s) {
 		this.input = s;
 		this.pos = 0;
 		this.lineno = 1;
 		this.column = 1;
 	}
 	
 	public void add(Object obj) {
 		this.add(obj, obj.getClass());
 	}
 	
 	private void add(Object obj, Class<?> c) {
 		for (Method m: c.getDeclaredMethods()) {
 			if (m.isAnnotationPresent(Token.class)) {
 				Token token = m.getAnnotation(Token.class);
 				for (String v: token.value()) {
 					Pattern p = Pattern.compile(v);
 					register(p, obj, m);
 				}
 			}
 		}
 		
 		Class<?> sc = c.getSuperclass();
 		if (sc != null) {
 			this.add(obj, sc);
 		}
 	}
 	
 	private void register(Pattern p, Object r, Method m) {
 		checkSignature(m);
 		this.rules.add(new TokenRule(p, r, m));
 	}
 	
 	private static void checkSignature(Method m) {
 		Class<?>[] types = m.getParameterTypes();
 		if (types.length == 0 || types.length > 2) {
 			throw new IllegalStateException("Wrong number parameters for " + m.getName());
 		}
 		if (!types[0].equals(String.class)) {
 			throw new IllegalStateException("Wrong parameter type for " + m.getName());
 		}
 		if (types.length > 1 && !types[1].equals(Location.class)) {
 			throw new IllegalStateException("Wrong parameter type for " + m.getName());
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private T makeToken(TokenRule r, String val) {
 		try {
 			if (r.wantsLocation()) {
 				return (T) r.method.invoke(r.receiver, val, location());
 			} else {
 				return (T) r.method.invoke(r.receiver, val);
 			}
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (InvocationTargetException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public void skip(int count) {
 		this.pos += count;
 	}
 	
 	public int position() {
 		return this.pos;		
 	}
 	
 	public int lineno() {
 		return this.lineno;
 	}
 	
 	public int column() {
 		return this.column;
 	}
 	
 	public Location location() {
 		return new Location(this.lineno, this.column);
 	}
 	
 	private void updateLocation(String val) {
 		int i = val.length();
 		i = val.lastIndexOf('\n', i - 1);
 		if (i >= 0) {
 			column = val.length() - i;
 		} else {
 			column += val.length();
 		}
 		
 		while (i >= 0) {
 			lineno++;
 			i = val.lastIndexOf('\n', i - 1);
 		}
 	}
 	
 	public boolean eof() {
 		return this.pos >= this.input.length();
 	}
 	
 	public T next() {
 matching:
 		while (true) {
 			if (eof()) return null;
 			
 			for (TokenRule r: this.rules) {
 				Matcher m = r.pattern.matcher(this.input);
 				if (m.find(pos)) {
 					MatchResult result = m.toMatchResult();
 					if (result.start() == pos) {
 						pos = result.end();
 						String val = result.group();
 						updateLocation(val);
 						T token = makeToken(r, val);						
 						if (token == null) {
 							continue matching;			
 						}
 						return token;
 					}
 				}
 			}
 			
 			// No match
			throw new IllegalStateException("No match at " + location());
 		}
 	}
 
 	private static class TokenRule {
 		public final Pattern pattern;
 		public final Method method;
 		public final Object receiver;
 		
 		public TokenRule(Pattern p, Object r, Method m) {			
 			pattern = p;
 			receiver = r; 
 			method = m;
 		}
 		
 		public boolean wantsLocation() {
 			Class<?>[] types = method.getParameterTypes();
 			return types.length == 2;
 		}
 	}
 }
