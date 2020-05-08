 package com.brweber2.parser;
 
 import com.brweber2.term.Atom;
 import com.brweber2.term.ComplexTerm;
 import com.brweber2.term.Numeric;
 import com.brweber2.term.Variable;
 import com.brweber2.term.rule.Rule;
 import com.brweber2.term.rule.RuleAnd;
 import com.creativewidgetworks.goldparser.parser.GOLDParser;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.io.StringReader;
 
 /**
  * @author brweber2
  *         Copyright: 2012
  */
 public class ParserTest {
     @Test
     public void parseTerm()
     {
         String source = "abc .";
         Atom expected = new Atom("abc");
 
         Atom atom = parseForMe(Atom.class,source);
         Assert.assertEquals(atom,expected);
     }
 
     @Test
     public void parseQuotedTerm()
     {
         String source = "'How about that?' .";
        Atom expected = new Atom("How about that?");
 
         Atom atom = parseForMe(Atom.class,source);
         Assert.assertEquals(atom,expected);
     }
 
     @Test
     public void parseNumber()
     {
         String source = "123.";
         Numeric expected = new Numeric("123");
 
         Numeric Numeric = parseForMe(Numeric.class,source);
         Assert.assertEquals(Numeric,expected);
     }
 
     @Test
     public void parseDecimal()
     {
         String source = "123.45.";
         Numeric expected = new Numeric("123.45");
 
         Numeric numeric = parseForMe(Numeric.class,source);
         Assert.assertEquals(numeric,expected);
     }
 
     @Test
     public void parseVariable()
     {
         String source = "@foo.";
         Variable expected = new Variable("foo");
 
         Variable variable = parseForMe(Variable.class,source);
         Assert.assertEquals(variable,expected);
     }
 
     @Test
     public void parseComplexTerm()
     {
         String source = "foo(bar,42).";
         ComplexTerm expected = new ComplexTerm("foo",new Atom("bar"),new Numeric("42"));
 
         ComplexTerm complexTerm = parseForMe(ComplexTerm.class,source);
         Assert.assertEquals(complexTerm,expected);
     }
 
     @Test
     public void parseRule()
     {
         String source = "foo(@x,@y) :- bar(@x,@z), foo(@z,@y).";
         Rule expected = new Rule(new ComplexTerm("foo",new Variable("x"),new Variable("y")), new RuleAnd(new ComplexTerm("bar",new Variable("x"),new Variable("z")), new ComplexTerm("foo",new Variable("z"),new Variable("y"))));
 
         Rule rule = parseForMe(Rule.class,source);
         Assert.assertEquals(rule,expected);
     }
 
     @Test(enabled = false)
     public void shouldNotParse()
     {
         String source = "123&abc .";
         Atom expected = new Atom("abc");
 
         Atom atom = parseForMe(Atom.class,source);
         Assert.assertEquals(atom,expected);
     }
     
     private Object parse(String source)
     {
         CompileGrammar grammar = new CompileGrammar();
         GOLDParser parser = grammar.parser();
         
         Assert.assertTrue(parser.parseSourceStatements(new StringReader(source)));
         parser.getCurrentReduction().execute();
         Object value = parser.getCurrentReduction().getValue().asObject();
         return value;
     }
     
     private <T> T parseForMe(Class<T> clazz,String source)
     {
         Object value = parse(source);
         T result = gimme(clazz,value);
         return result;
     }
     
     private <T> T gimme(Class<T> clazz, Object o)
     {
         Assert.assertNotNull(o);
         Assert.assertTrue(clazz.isAssignableFrom(o.getClass()));
         return (T) o;
     }
 }
