 package com.ninja_squad.jb.codestory.action;
 
 import com.ninja_squad.jb.codestory.Action;
 import com.ninja_squad.jb.codestory.HttpRequest;
 import com.ninja_squad.jb.codestory.HttpResponse;
 import com.ninja_squad.jb.codestory.action.arithmetic.ArithmeticLexer;
 import com.ninja_squad.jb.codestory.action.arithmetic.ArithmeticParser;
 
 import java.math.BigDecimal;
 
 /**
  * Action used to parse and answer to non-URL-encoded arithmetic expression in the <code>q</code> parameter
  * @author JB
  */
 public class ArithmeticAction implements Action {
 
     @Override
     public HttpResponse execute(HttpRequest request) {
         String encodedExpression =
             request.getPathAndQueryString().substring(request.getPathAndQueryString().indexOf('=') + 1);
         try {
             ArithmeticLexer lexer = new ArithmeticLexer(encodedExpression.replace(',', '.'));
             ArithmeticParser parser = new ArithmeticParser(lexer.parse());
             BigDecimal result = parser.parse();
 
            String formattedResult = result.toPlainString().replace('.', ',');
            if (formattedResult.indexOf(',') >= 0) {
                while (formattedResult.endsWith("0")) {
                    formattedResult = formattedResult.substring(0, formattedResult.length() - 1);
                }
            }
            if (formattedResult.endsWith(",")) {
                formattedResult = formattedResult.substring(0, formattedResult.length() - 1);
            }
             return StandardResponses.ok(formattedResult);
         }
         catch (Exception e) {
             return StandardResponses.badRequest("Invalid expression: " + encodedExpression);
         }
     }
 }
