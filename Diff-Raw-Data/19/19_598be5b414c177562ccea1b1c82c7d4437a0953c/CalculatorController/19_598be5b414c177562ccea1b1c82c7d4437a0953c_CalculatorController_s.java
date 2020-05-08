 package calculator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import calculator.common.ValidationError;
 import calculator.parser.CalcParser;
 import calculator.parser.CalcLexer;
 
 /**
  * Handles requests for the application home page.
  */
 @Controller
 public class CalculatorController {
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home() {
 		return "home";
 	}
 
 	/**
 	 * Simply selects the home view to render by returning its name.
 	 */
 	@RequestMapping(value = "/calculate", method = RequestMethod.POST)
 	public String calculate(HttpServletRequest request, Model model) {
 		String expressions = request.getParameter("expressions");
 		String[] lines = expressions.split("\n");
 
 		List<String> results = new ArrayList<String>(lines.length);
 		for (String eachExpression : lines) {
 			eachExpression = clearPreviouslyCalculatedExpression(eachExpression);
 			calculateAndAddResult(results, eachExpression);
 		}
 
 		model.addAttribute("results", formatOneExpressionOneLine(results));
 		return "home";
 	}
 
 	private void calculateAndAddResult(List<String> results, String eachExpression) {
 		String result;
 		try {
 			CalcLexer lexer = new CalcLexer(eachExpression);
 			CalcParser parser = new CalcParser(lexer);
 			result = "" + parser.expr().evaluate();
 		} catch (ValidationError error) {
 			result = error.getMessage();
 		}
 		results.add(eachExpression.replaceAll("\r", "") + " = " + result);
 	}
 
 	private String formatOneExpressionOneLine(List<String> results) {
 		StringBuilder formattedResult = new StringBuilder();
 
 		for (String eachResult : results) {
 			formattedResult.append(eachResult).append("\n");
 		}
 		return formattedResult.substring(0, formattedResult.length() - 1);
 	}
 
 	private String clearPreviouslyCalculatedExpression(String eachExpression) {
 		if (eachExpression.contains("=")) {
			eachExpression = eachExpression.substring(0, eachExpression.indexOf('=') - 1);
 		}
 		return eachExpression;
 	}
 
 }
