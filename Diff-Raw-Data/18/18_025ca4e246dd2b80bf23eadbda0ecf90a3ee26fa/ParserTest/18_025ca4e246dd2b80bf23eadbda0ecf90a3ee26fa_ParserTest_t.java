 package org.seventyeight.markup;
 
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * @author cwolfgang
  */
 public class ParserTest {
 
     protected String sample1 = "1 2 =3 4= 5 6";
     protected String sample2 = "1 2 ==3 4== 5 6";
     protected String sample3 = "=1 2= ==3 4== 5 6";
 
     protected String sample101 = "1 2 ''3 4'' 5 6";
     protected String sample102 = "1 2 '''3 4''' 5 6";
     protected String sample103 = "1 2 '''''3 4''''' 5 6";
 
     protected String sample1001 = "*1\n*2";
     protected String sample1002 = "*1\n*2\n**3";
 
     protected String sample1000001 = "Yeah";
     protected String sample1000002 =  "* 1\r\n* 2\r\n** 3\r\n** 4\r\n* 5";
 
    protected String MARKUP5 = "=First day=\n\r\n\r=second day=";

     @Test
     public void test() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample1, output );
         System.out.println("OUT: " + output.toString());
 
         assertThat( output.toString(), is( "1 2 <h1>3 4</h1> 5 6" ) );
     }
 
     @Test
     public void test2() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample2, output );
         System.out.println("OUT: " + output.toString());
 
         assertThat( output.toString(), is( "1 2 <h2>3 4</h2> 5 6" ) );
     }
 
     @Test
     public void test3() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample3, output );
         System.out.println("OUT: " + output.toString());
 
         assertThat( output.toString(), is( "<h1>1 2</h1> <h2>3 4</h2> 5 6" ) );
     }
 
 
     @Test
     public void test101() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample101, output );
         System.out.println("OUT: " + output.toString());
 
         assertThat( output.toString(), is( "1 2 <span style=\"font-style: italic\">3 4</span> 5 6" ) );
     }
 
     @Test
     public void test102() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample102, output );
         System.out.println("OUT: " + output.toString());
 
         assertThat( output.toString(), is( "1 2 <span style=\"font-weight: bold\">3 4</span> 5 6" ) );
     }
 
     @Test
     public void test103() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample103, output );
         System.out.println("OUT: " + output.toString());
 
         assertThat( output.toString(), is( "1 2 <span style=\"font-style: italic; font-weight: bold\">3 4</span> 5 6" ) );
     }
 
     @Test
     public void test1001() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample1001, output );
         System.out.println("OUT: " + output.toString());
     }
 
     @Test
     public void test1002() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample1002, output );
         System.out.println("OUT: " + output.toString());
     }
 
 
 
 
     @Test
     public void test1000001() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample1000001, output );
         System.out.println("OUT: " + output.toString());
     }
 
 
     @Test
     public void test1000002() {
         SimpleParser parser = new SimpleParser( new HtmlGenerator() );
         StringBuilder output = new StringBuilder();
         parser.parse( sample1000002, output );
         System.out.println("OUT: " + output.toString());
     }

    @Test
    public void testMarkUp5() {
        SimpleParser parser = new SimpleParser( new HtmlGenerator() );
        StringBuilder output = new StringBuilder();
        parser.parse( MARKUP5, output );
        System.out.println("OUT: " + output.toString());
    }
 }
