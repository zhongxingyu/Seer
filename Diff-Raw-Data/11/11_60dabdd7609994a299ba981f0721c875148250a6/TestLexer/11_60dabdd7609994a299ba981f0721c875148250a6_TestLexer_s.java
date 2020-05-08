 package uk.co.overstory.xquery.test;
 
 import com.intellij.psi.tree.IElementType;
 import uk.co.overstory.xquery.parser.XqyLexer;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ron
  * Date: 1/10/12
  * Time: 2:26 PM
  */
 @SuppressWarnings("UseOfSystemOutOrSystemErr")
 public class TestLexer
 {
 	private static final String XQUERY=
 		"xquery version '1.0-ml';\n" +
 			"\n" +
 			"(: embedded (parens) :)\n" +
 			"\n" +
 			"(# myqname this is a pragma #)\n" +
 			"" +
 			"<?target This is a processing instruction ?>\n" +
 			"" +
 			"(: This is a comment:with a semicolon :)\n" +
 			"(: This is a comment :)\n" +
 			"\n" +
 			"declare variable $data := <![CDATA[ foo bar baz ]]>;\n" +
 			"\n" +
 			"declare variable $hello as xs:string := ('Hello world');\n" +
 			"declare variable $good-bye := <hello>goodbye</hello>;\n" +
 			"declare variable $toodles := <ta-ta/>;\n" +
 			"declare variable $fare_well := 1.0e+5;\n" +
 			"declare variable $pi := <?foo bar?>;\n" +
 			"declare variable $xml-with-comment :=\n" +
 			"     <foo>\n" +
 			"        <!-- xml comment -->\n" +
 			"        <? target blah blah ?>\n" +
 			"        Some content\n" +
 			"     </foo>\n" +
 			"\n" +
 			"declare variable $foo := 'foobar';\n\n" +
 			"declare function myfunc ($arg1 as xs:string, $arg2 as element(foo)*) as empty-sequence()" +
 			"{" +
 			"	xdmp:current-dateTime()" +
 			"};" +
 			"\n" +
 			"($hello, $good-bye, $fare_well)[1]\n";
 
 	private TestLexer ()
 	{
 	}
 
 
 	public static void main (String[] args)
 	{
 		XqyLexer lexer = new XqyLexer ();
 		IElementType tokenType = null;
 
 		lexer.start (XQUERY);
 
 		while ((tokenType = lexer.getTokenType()) != null)  {
			String token = lexer.getTokenText ();
 
			System.out.println ("-->" + token + "<-- " + tokenType);
			lexer.advance ();
 		}
 
 	}
 }
