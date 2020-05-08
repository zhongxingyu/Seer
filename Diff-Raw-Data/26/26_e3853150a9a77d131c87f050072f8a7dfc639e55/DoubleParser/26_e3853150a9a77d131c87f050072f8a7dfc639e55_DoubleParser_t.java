 package jscl.text;
 
 import jscl.math.ExpressionVariable;
 import jscl.math.Generic;
 import jscl.math.NumericWrapper;
 import jscl.math.Variable;
 import jscl.math.numeric.JSCLDouble;
 import org.apache.commons.lang.mutable.MutableInt;
 import org.jetbrains.annotations.NotNull;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class DoubleParser implements Parser<NumericWrapper> {
 
 	public static final Parser<NumericWrapper> parser = new DoubleParser();
 
 	private static final List<Parser<Double>> parsers = Arrays.asList(
 			Singularity.parser,
 			FloatingPointLiteral.parser);
 
 	private DoubleParser() {
 	}
 
 	@NotNull
 	public NumericWrapper parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
		final Parser<Double> multiTryParser = new MultiTryParser<Double>(new ArrayList<Parser<Double>>(parsers));
 		return new NumericWrapper(JSCLDouble.valueOf(multiTryParser.parse(string, position)));
 	}
 }
 
 class Singularity implements Parser<Double> {
 
 	public static final Parser<Double> parser = new Singularity();
 
 	private Singularity() {
 	}
 
 	@NotNull
 	public Double parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 
 		final double result;
 
 		String s = Identifier.parser.parse(string, position);
 		if (s.equals("NaN")) {
 			result = Double.NaN;
 		} else if (s.equals("Infinity")) {
 			result = Double.POSITIVE_INFINITY;
 		} else {
 			position.setValue(pos0);
 			throw new ParseException();
 		}
 
 		return result;
 	}
 }
 
 class FloatingPointLiteral implements Parser<Double> {
 
 	public static final Parser<Double> parser = new FloatingPointLiteral();
 
 	private FloatingPointLiteral() {
 	}
 
 	public Double parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 
 		final StringBuilder result = new StringBuilder();
 
 		boolean digits = false;
 		boolean point = false;
 
 		try {
 			result.append(Digits.parser.parse(string, position));
 			digits = true;
 		} catch (ParseException e) {
 		}
 
 		try {
 			DecimalPoint.parser.parse(string, position);
 			result.append(".");
 			point = true;
 		} catch (ParseException e) {
 			if (!digits) {
 				position.setValue(pos0);
 				throw e;
 			}
 		}
 		try {
 			String s = (String) Digits.parser.parse(string, position);
 			result.append(s);
 		} catch (ParseException e) {
 			if (!digits) {
 				position.setValue(pos0);
 				throw e;
 			}
 		}
 		try {
 			String s = (String) ExponentPart.parser.parse(string, position);
 			result.append(s);
 		} catch (ParseException e) {
 			if (!point) {
 				position.setValue(pos0);
 				throw e;
 			}
 		}
 		return new Double(result.toString());
 	}
 }
 
 class DecimalPoint implements Parser {
 	public static final Parser parser = new DecimalPoint();
 
 	private DecimalPoint() {
 	}
 
 	public Object parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 		ParserUtils.skipWhitespaces(string, position);
 		if (position.intValue() < string.length() && string.charAt(position.intValue()) == '.') {
 			string.charAt(position.intValue());
 			position.increment();
 		} else {
 			position.setValue(pos0);
 			throw new ParseException();
 		}
 		return null;
 	}
 }
 
 class ExponentPart implements Parser {
 	public static final Parser parser = new ExponentPart();
 
 	private ExponentPart() {
 	}
 
 	public Object parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 		StringBuffer buffer = new StringBuffer();
 		ParserUtils.skipWhitespaces(string, position);
 		if (position.intValue() < string.length() && (string.charAt(position.intValue()) == 'e' || string.charAt(position.intValue()) == 'E')) {
 			char c = string.charAt(position.intValue());
 			position.increment();
 			buffer.append(c);
 		} else {
 			position.setValue(pos0);
 			throw new ParseException();
 		}
 		try {
 			String s = (String) SignedInteger.parser.parse(string, position);
 			buffer.append(s);
 		} catch (ParseException e) {
 			position.setValue(pos0);
 			throw e;
 		}
 		return buffer.toString();
 	}
 }
 
 class SignedInteger implements Parser {
 	public static final Parser parser = new SignedInteger();
 
 	private SignedInteger() {
 	}
 
 	public Object parse(@NotNull String string, @NotNull MutableInt position) throws ParseException {
 		int pos0 = position.intValue();
 		StringBuffer buffer = new StringBuffer();
 		ParserUtils.skipWhitespaces(string, position);
 		if (position.intValue() < string.length() && (string.charAt(position.intValue()) == '+' || string.charAt(position.intValue()) == '-')) {
 			char c = string.charAt(position.intValue());
 			position.increment();
 			buffer.append(c);
 		}
 		try {
 			int n = ((Integer) IntegerParser.parser.parse(string, position)).intValue();
 			buffer.append(n);
 		} catch (ParseException e) {
 			position.setValue(pos0);
 			throw e;
 		}
 		return buffer.toString();
 	}
 }
