 import junit.framework.*;
 
 import java.util.*;
 
 import dit.url.*;
 import dit.url.ast.*;
 import dit.url.parser.*;
 
 
 public class TestParser extends TestCase
 {
     private Parser p;
 
     public TestParser(String s)
     {
         super(s);
     }
 
     public void setUp()
     {
         p = new Parser(System.in);
     }
 
     public void testSingleText() throws ParseException
     {
         p.ReInit("one");
 
         List<Ast> l = p.UrlDeclaration();
 
         assertEquals("[AST{value:one}]", l.toString());
     }
 
     public void testSingleUntypedVariable() throws ParseException
     {
         p.ReInit("$one");
 
         List<Ast> l = p.UrlDeclaration();
 
         assertEquals("[AST{name:one,type:class java.lang.String}]", l.toString());
     }
 
     public void testSingleTypedVariable() throws ParseException
     {
         p.ReInit("$one:Boolean");
 
         List<Ast> l = p.UrlDeclaration();
 
         assertEquals("[AST{name:one,type:class java.lang.Boolean}]", l.toString());
     }
 }
