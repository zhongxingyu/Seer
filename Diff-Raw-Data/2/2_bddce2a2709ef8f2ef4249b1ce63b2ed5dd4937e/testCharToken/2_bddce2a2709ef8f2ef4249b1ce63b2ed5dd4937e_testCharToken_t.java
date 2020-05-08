 package jmathlibtests.core.tokens;
 
 import jmathlib.core.interpreter.Interpreter;
 import jmathlib.core.tokens.numbertokens.DoubleNumberToken;
 import jmathlib.tools.junit.framework.*;
 import jmathlib.core.tokens.*;
 
 public class testCharToken extends TestCase
 {
     private CharToken string1;
     private CharToken string2;
     private DoubleNumberToken number;
     protected Interpreter ml;
     
     public testCharToken(String name)
     {
         super(name);
     }
     
     public static void main(String[] args)
     {
         jmathlib.tools.junit.textui.TestRunner.run (suite());        
     }
 
     public static Test suite()
     {
         return new TestSuite(testCharToken.class);
     }
     
     public void setUp()
     {
         string1 = new CharToken("A String");
         string2 = new CharToken("Another String");
         number  = new DoubleNumberToken(1);
         ml      = new Interpreter(true);
     }
     protected void tearDown() {
         ml = null;
     }
    
     public void testAdd1()
     {
         CharToken expectedResult = new CharToken("A StringAnother String");
         Token actualResult = string1.add(string2);
         assertEquals(expectedResult.toString(), actualResult.toString());
     }
 
     public void testAdd2()
     {
         CharToken expectedResult = new CharToken("A String1");
         Token actualResult = string1.add(number);
        assertEquals(" [66 ,  33 ,  84 ,  117 ,  115 ,  106 ,  111 ,  104]\n", actualResult.toString());
     }
     
     public void testCharToken003() {
         ml.executeExpression("a='bar'+'foo'");
         assertTrue(ml.getString("a").equals("barfoo"));
     }
 
 }
 
 
 
 
 
