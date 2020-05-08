 package jscl.text;
 
 import jscl.math.ExpressionVariable;
 import jscl.math.Generic;
 import jscl.math.Variable;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * User: serso
  * Date: 10/27/11
  * Time: 2:45 PM
  */
 public class PrimaryExpressionParser implements Parser<Generic> {
 
 	public static final Parser<Generic> parser = new PrimaryExpressionParser();
 
 	private static final List<Parser<Generic>> parsers = Arrays.asList(
 			new VariableConverter<Variable>(DoubleVariableParser.parser),
 			JSCLIntegerParser.parser,
 			new VariableConverter<Variable>(VariableParser.parser),
 			new VariableConverter<Variable>(MatrixVariableParser.parser),
 			new VariableConverter<Variable>(VectorVariableParser.parser),
 			new VariableConverter<ExpressionVariable>(BracketedExpression.parser));
 
	private static final Parser<Generic> multiTryParser = new MultiTryParser<Generic>(parsers);

 	private PrimaryExpressionParser() {
 	}
 
 	public Generic parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		return multiTryParser.parse(string, position);
 	}
 
 
 }
