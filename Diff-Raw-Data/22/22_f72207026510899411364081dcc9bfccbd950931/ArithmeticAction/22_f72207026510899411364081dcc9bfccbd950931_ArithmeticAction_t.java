 package com.ninja_squad.jb.codestory.action;
 
 import com.ninja_squad.jb.codestory.Action;
 import com.ninja_squad.jb.codestory.HttpRequest;
 import com.ninja_squad.jb.codestory.HttpResponse;
 import org.antlr.runtime.ANTLRReaderStream;
 import org.antlr.runtime.CommonTokenStream;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Locale;
 
 /**
  * Action used to parse and answer to non-URL-encoded arithmetic expression in the <code>q</code> parameter
  * @author JB
  */
 public class ArithmeticAction implements Action {
 
     @Override
     public HttpResponse execute(HttpRequest request) throws IOException {
         String encodedExpression =
             request.getPathAndQueryString().substring(request.getPathAndQueryString().indexOf('=') + 1);
         try {
             ANTLRReaderStream input = new ANTLRReaderStream(new StringReader(encodedExpression.replace(',', '.')));
             MathLexer lexer = new MathLexer(input);
             CommonTokenStream tokens = new CommonTokenStream(lexer);
             MathParser parser = new MathParser(tokens);
             BigDecimal result = parser.expr();
 
             DecimalFormat decimalFormat = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.FRENCH));
             String formattedResult = decimalFormat.format(result);
             return HttpResponse.ok(formattedResult);
         }
         catch (Exception e) {
             return HttpResponse.badRequest("Invalid expression: " + encodedExpression);
         }
     }
 }
