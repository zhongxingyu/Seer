 package suite.parser;
 
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.junit.Test;
 
 import suite.util.To;
 
 public class EbnfTest {
 
 	@Test
 	public void testId() throws IOException {
		Ebnf ebnf = new Ebnf(new FileReader("src/main/ebnf/java.ebnf"));
 		System.out.println(ebnf.parse("abc", 0, "<IDENTIFIER>"));
 	}
 
 	@Test
 	public void testExpression() throws IOException {
		Ebnf ebnf = new Ebnf(new FileReader("src/main/ebnf/java.ebnf"));
 		System.out.println(ebnf.parse("\"1\" + \"2\"", 0, "Expression"));
 	}
 
 	@Test
 	public void testJava0() throws IOException {
		Ebnf ebnf = new Ebnf(new FileReader("src/main/ebnf/java.ebnf"));
 		System.out.println(ebnf.parse("public class C { public void f() { int a; } }"));
 	}
 
 	@Test
 	public void testJava() throws IOException {
		Ebnf ebnf = new Ebnf(new FileReader("src/main/ebnf/java.ebnf"));
 		System.out.println(ebnf.parse(To.string(new FileReader("src/test/java/suite/parser/EbnfTest.java"))));
 	}
 
 }
