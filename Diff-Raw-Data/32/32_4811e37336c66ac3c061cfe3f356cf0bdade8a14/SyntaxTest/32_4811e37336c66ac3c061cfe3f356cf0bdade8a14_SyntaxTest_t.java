 package de.skuzzle.test.polly.parsing.parser;
 
 import org.junit.Test;
 
 import junit.framework.Assert;
 import de.skuzzle.polly.parsing.InputParser;
 import de.skuzzle.polly.parsing.ParseException;
 
 /**
  * Tests whether syntactically correct inputs are recognized correctly.
  * 
  * @author Simon Taddiken
  */
 public class SyntaxTest extends AbstractParserTest {
 
     @Test
     public void parseTest1() throws ParseException {
         final String input = ":-)";
         final InputParser parser = this.obtain(input);
         Assert.assertNull(parser.parse());
     }
     
     
     
     @Test
     public void parseTest2() throws ParseException {
         final String input = ":";
         final InputParser parser = this.obtain(input);
         Assert.assertNull(parser.parse());
     }
     
     
     
     @Test
     public void parseTest3() throws ParseException {
         final String input = ":foo";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest4() throws ParseException {
         final String input = ":foo 5+5 \"just a string\"";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest5() throws ParseException {
         final String input = ":foo \\(num x, string y: x+y)(10, \"a\")";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
 
     
     
     @Test
     public void parseTest6() throws ParseException {
         final String input = ":foo 1;2;3;4[5]";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest7() throws ParseException {
         final String input = ":foo 1..20[4] 1..10$-4";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest8() throws ParseException {
         final String input = ":foo a {1,2,3,4}";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest9() throws ParseException {
         final String input = ":foo -5 -a --7";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest10() throws ParseException {
         final String input = ":foo (5+5+(4*5+5)+9)";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest11() throws ParseException {
         final String input = ":foo del a, b,public c";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest12() throws ParseException {
         final String input = ":foo inspect abc.d inspect d inspect public a";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest13() throws ParseException {
         final String input = ":foo if true||false : 10 : 17";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest14() throws ParseException {
         final String input = ":foo 0x8:1234";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest15() throws ParseException {
         final String input = ":foo \\(list<list<num>> a: a)";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest16() throws ParseException {
         final String input = ":foo \\( (num list<num> -> list<user>) a: a)";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest17() throws ParseException {
         final String input = ":foo a.b+4*a+10-xyz[12]+1..29?-8";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest18() throws ParseException {
         final String input = ":foo abc(14,15)";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest19() throws ParseException {
         final String input = ":foo 3^5^-7";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest20() throws ParseException {
         final String input = ":foo abc->x xyz->public y";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest21() throws ParseException {
         final String input = ":foo \\( list<(num->num)> a: a)";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest22() throws ParseException {
         final String input = ":foo \\( list<((string->string)->list<string>)> a: a)";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest23() throws ParseException {
         final String input = ":foo if 5<3 : 5 : 4 10<5";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
     
     
     
     @Test
     public void parseTest24() throws ParseException {
         final String input = ":foo 1>>3";
         final InputParser parser = this.obtain(input);
         parser.parse();
     }
    
    
    
    @Test
    public void parseTest25() throws ParseException {
        final String input = ":foo 1>>>3";
        final InputParser parser = this.obtain(input);
        parser.parse();
    }
 }
