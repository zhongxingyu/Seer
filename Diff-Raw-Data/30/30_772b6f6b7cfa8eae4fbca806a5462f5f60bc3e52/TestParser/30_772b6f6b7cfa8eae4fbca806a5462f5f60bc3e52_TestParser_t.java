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

    public void testMultipleText() throws ParseException
    {
        p.ReInit("one/two/3");

        List<Ast> l = p.UrlDeclaration();

        assertEquals("[AST{value:one}, AST{value:two}, AST{value:3}]", l.toString());
    }

    public void testMultipleMixed() throws ParseException
    {
        p.ReInit("one/$two:Integer/3/$flag:Boolean");

        List<Ast> l = p.UrlDeclaration();

        assertEquals("[AST{value:one}, AST{name:two,type:class java.lang.Integer}, AST{value:3}, AST{name:flag,type:class java.lang.Boolean}]", l.toString());
    }

    public void testDeclaringTypeForNormalTestShouldFail()
    {
        try {
            p.ReInit("two:Integer");
            List<Ast> l = p.UrlDeclaration();
            fail("Allowing type definition for normal text");
        } catch (ParseException ex) {
        }
    }


 }
