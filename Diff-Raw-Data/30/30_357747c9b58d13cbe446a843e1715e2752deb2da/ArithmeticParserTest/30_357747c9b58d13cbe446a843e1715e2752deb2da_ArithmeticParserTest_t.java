 package com.ninja_squad.jb.codestory.action.arithmetic;
 
 import com.google.common.collect.Lists;
 import org.junit.Test;
 
 import java.math.BigDecimal;
 import java.util.Collections;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.*;
 
 /**
  * Test for {@link com.ninja_squad.jb.codestory.action.arithmetic.ArithmeticParser}
  * @author JB
  */
 public class ArithmeticParserTest {
     @Test(expected = RuntimeException.class)
     public void emptyTokensShouldCauseException() throws Exception {
         new ArithmeticParser(Collections.<Token>emptyList()).parse();
     }
 
     @Test(expected = RuntimeException.class)
     public void incorrectTokensShouldCauseException() throws Exception {
         new ArithmeticParser(Lists .newArrayList(new Token("1.2", TokenType.FLOAT),
                                                  new Token(null, TokenType.RIGHT_PAREN))).parse();
     }
 
     @Test(expected = RuntimeException.class)
     public void correctTokensShouldReturnGoodResult() throws Exception {
         test("(-1.1+2*(3/-4)", "-2.6");
         test("((1.1+2)+3.14+4+(5+6+7)+(8+9+10)*4267387833344334647677634)/2*553344300034334349999000",
              "31878018903828899277492024491376690701584023926880");
        test("1.5*1.5",
             "2.25");
     }
 
     private void test(String expression, String expected) {
         ArithmeticLexer lexer = new ArithmeticLexer(expression);
         List<Token> tokens = lexer.parse();
         ArithmeticParser parser = new ArithmeticParser(tokens);
         BigDecimal result = parser.parse();
         assertThat(result).isEqualTo(new BigDecimal(expected));
     }
 }
