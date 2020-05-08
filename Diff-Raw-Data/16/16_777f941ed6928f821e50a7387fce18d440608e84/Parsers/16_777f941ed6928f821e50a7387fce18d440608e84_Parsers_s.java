 package pl.edu.mimuw.cloudatlas.query;
 
 import java.io.StringReader;
 import java.util.List;
 import java_cup.runtime.Symbol; 
 
 public class Parsers {
 
 	private Parsers() {	
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static List<Stmt> parseQuery(String source) throws ParseException  {
 		Parser parser = new Parser(new Lexer(new StringReader(source)));
 		Symbol symbol;
 		try {
 			symbol = parser.parse();
 		} catch (Exception e) {
			throw new ParseException(e);
 		}
 		return (List<Stmt>) symbol.value;
 	}
 }
