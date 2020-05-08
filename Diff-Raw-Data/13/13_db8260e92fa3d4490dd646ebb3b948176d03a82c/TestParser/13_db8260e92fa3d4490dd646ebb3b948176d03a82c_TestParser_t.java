 package jescript.parser;
 
 import static org.testng.Assert.*;
 import java.io.PushbackReader;
 import java.io.StringReader;
 import jescript.analysis.DepthFirstAdapter;
 import jescript.lexer.Lexer;
 import jescript.node.Node;
 import jescript.node.Start;
 import org.testng.annotations.Test;
 import org.testng.log4testng.Logger;
 
 @Test
 public class TestParser {
 
 	private static Logger LOG = Logger.getLogger(TestParser.class);
 
 	public void emptyModule() {
 		testInput("-module(m).");
 		testInvalidInput("-module(X).");
 	}
 	
 	public void fileAttributes() {
 		testInput("-file(\"f\", 1).-module(x).");
 		testInput("-file(\"f\", 1).\n-file(\"g\", 2).\n-module(x).");
 	}
 	
 	public void headerAttributes() {
 		testModule("-export([main/1]).");
 		testModule("-import(io, [fwrite/1]).");
 		testModule("-file(\"f\", 1).");
 		testModule("-compile([]).\n-compile(export_all).\n-compile([export_all, 'E']).");
 		testModule("-custom(1.1).");
 		testModule("-custom(f/1).");
 		testModule("-custom({}).");
 		testModule("-custom([]).");
 		testModule("-custom((x)).");
 		testModule("-custom({[1,2], x}).");
 		testInvalidModule("-custom(1.1, x).");
 		testInvalidModule("-custom(1+2).");
 	}
 	
 	public void anywhereAttributes() {
 		testModule("main(_) -> ok.\n-record(empty, {}).\nf() -> f.");
 	}
 
 	public void records() {
 		testModule("-record(empty, {}).");
 		testModule("-record(person, {name, phone, address}).");
 		testModule("-record(person, {name = \"\", phone = [], address}).");
 	}
 	
 	public void simpleFunArgs() {
 		testModule("main() -> true.");
 		testModule("main(X) -> X.");
 		testModule("main(x) -> x.");
 		testModule("main(1) -> x.");
 		testModule("main($\\^c) -> x.");
 		testModule("main(\"ab\" \"c\") -> x.");
 		testModule("main(_) -> x.");
 		testModule("main([]) -> x.");
 		testModule("main([1,2]) -> x.");
 		testModule("main({a,b,c}) -> x.");
 		testModule("main(#person{name=\"Robert\"}) -> x.");
 
 		testModule("main(X, 1, atom) -> X.");
 		testInvalidModule("main(,) -> x.");
 		testInvalidModule("fun() -> true.");
 	}
 	
	public void complexFunArgs() {
		testModule("g(X=2 bsr 1) -> X.");
		testModule("g(+1*-2) -> X.");
		testModule("g([32]++\"abc\") -> X.");
	}
	
	public void invalidFunArgs() {
		testInvalidModule("g(X = (2>1)) -> X.");
		testInvalidModule("g(X+2)) -> true.");
		testInvalidModule("g(3++[1]) -> X.");
		testInvalidModule("g(2==2)) -> true.");
	}
	
 	public void multipleFuns() {
 		testModule("main(_) -> f(x).\nf(X) -> X.");
 	}
 
 	public void funClauses() {
 		testModule("f(x) -> x; f(_) -> err.");
 	}
 	
 	public void applyExpr() {
 		testModule("main(_) -> f(x).");
 		testModule("main(_) -> mod:f(x).");
 	}
 	
 	public void simpleExpr() {
 		testExpr("x");
 		testExpr("'x x'");
 		testExpr("X");
 		testExpr("{x}");
 		testExpr("[x]");
 		testExpr("1");
 		testExpr("1.2");
 		testExpr("$x");
 		testExpr("\"x\"");
 		testExpr("(x)");
 		testExpr("[X || X <- [1]]");
 		testExpr("[X || X <- [1], X =:= 1]");
 	}
 	
 	public void matchExpr() {
 		testModule("f([H|T]) -> T.");
 		testExpr("{_,_,x,X} = Y");
 	}
 	
 	public void blocks() {
 		testExpr("begin a, b, X end");
 	}
 
 	public void ifExpr() {
 		testExpr("if x -> x; true -> y end");
 		testExpr("if\nX == 1, X < 2 -> X\nend");
 		testInvalidExpr("if X = 1 -> X end"); // illegal guard
 	}
 
 	public void caseExpr() {
 		testExpr("case X of _ -> x end");
 		testExpr("case X of _ when X > 1 -> x end");
 		testExpr("case X of\n  {X,X} -> x;\n  2 -> y;\n  _ -> z \nend");
 	}
 	
 	public void receiveExpr() {
 		testExpr("receive X -> X end");
 		testExpr("receive after 100 -> true end");
 		testExpr("receive {X,_} -> X end");
 		testExpr("receive {X,_} -> X; stop -> true end");
 		testExpr("receive {X,_} -> X after 5 -> true end");
 		testExpr("receive {X,_} -> X after (1+2)*3 -> true end");
 	}
 	
 	public void funExpr() {
 		testExpr("fun x/1");
 		testExpr("fun x:x / 1");
 		testExpr("fun(x) -> x end");
 		testExpr("fun(x,y) -> x; (_,{}) -> y end");
 		testInvalidExpr("fun (x):x / 1");
 	}
 
 	public void guards() {
 		testModule("f(X) when true -> X.");
 		testModule("f(X) when true, x -> X.");
 		testModule("f(X) when is_atom(X) -> X.");
 		testModule("f(X) when is_number(X), X =/= x -> X.");
 		testModule("f(X) when (X+1 == 2) -> X.");
 	}
 	
 	private void testInput(String input) { assertValid(parse(input)); }
 	private void testModule(String input) { testInput("-module(m).\n" + input); }
 	private void testExpr(String input) { testInput("-module(m).\nmain(_)->\n" + input + "."); }
 	private void testInvalidInput(String input) { assertInvalid(parse(input)); } 
 	private void testInvalidModule(String input) { testInvalidInput("-module(m).\n" + input); }
 	private void testInvalidExpr(String input) { testInvalidInput("-module(m).\nmain(_)->\n" + input + "."); }
 
 	private void assertValid(Node parseResult) {
 		if (parseResult instanceof ParseFailed) {
 			ParseFailed r = (ParseFailed) parseResult;
 			fail(r.getMessage(), r.getException());
 		}
 	}
 	
 	private void assertInvalid(Node parseResult) {
 		assertInvalid(parseResult, -1, -1, -1, -1);
 	}
 
 	private void assertInvalid(Node parseResult, int linestart, int lineend, int colstart, int colend) {
 		if (!(parseResult instanceof ParseFailed)) {
 			fail("Expected parse error but succeeded");
 		}
 		ParseFailed r = (ParseFailed) parseResult;
 		if (!(r.getException() instanceof ParserException)) {
 			fail("Expected a parse exception, but got " + r.getException().getMessage(), r.getException());
 		}
 		String[] pos = r.getException().getMessage().split("\\[|\\]|,");
 		int line = Integer.parseInt(pos[1]), col = Integer.parseInt(pos[2]);
 		assertTrue(linestart < 0 || (linestart <= line && lineend >= line), "\n    Error at unexpected position: " + r.getMessage());
 		assertTrue(colstart < 0 || (colstart <= col && colend >= col),      "\n    Error at unexpected position: " + r.getMessage());
 	}
 
 	private Node parse(String input) {
 		try {
 			Lexer l = new Lexer(new PushbackReader(new StringReader(input), 1024));
 			Parser p = new Parser(l);
 			Start s = p.parse();
 			LOG.debug(input + " = \n" + nodeToString(s));
 			return s;
 		} catch (Exception e) {
 			return new ParseFailed(input, e);
 		}
 	}
 
 	private String nodeToString(Node s) {
 		final StringBuilder ast = new StringBuilder();
 		s.apply(new DepthFirstAdapter() {
 			int indent = 0;
 			void append(Node n) {
 				StringBuilder s = new StringBuilder();
 				for (int i = 0; i < indent; i++) {
 					s.append("|  ");
 				}
 				s.append(n.getClass().toString().replaceFirst("class jescript.node.A?", "")).append(getText(n));
 				ast.append(s).append("\n");
 			}
 			String getText(Node n) {
 //				String s = null;
 //				if (n instanceof ANumberExpr || n instanceof ABoolExpr || n instanceof ACharExpr || n instanceof AStringExpr || n instanceof AVarExpr) {
 //					s = n.toString();
 //				} else if (n instanceof AClassDef) {
 //					s = ((AClassDef) n).getId().toString();
 //				} else if (n instanceof AFunDef) {
 //					s = ((AFunDef) n).getId().toString().trim() + "(" + ((AFunDef) n).getFormal().toString() + ")";
 //				}
 //				if (s != null) {
 //					return "(" + s.trim() + ")";
 //				}
 				return "";
 			}
 			@Override public void defaultIn(Node node) {
 				append(node);
 				indent++;
 			}
 			@Override public void defaultOut(Node node) {
 				indent--;
 			}
 		});
 		return ast.toString();
 	}
 }
