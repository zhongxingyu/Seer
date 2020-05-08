 package org.eclipse.uomo.util.numbers;
 
 import static org.eclipse.uomo.core.UOMoNumberFormatException.Kind.*;
 
 import java.math.BigDecimal;
 import org.eclipse.uomo.core.UOMoNumberFormatException;
 import org.eclipse.uomo.util.Messages;
 
 /**
  * Double: a mantissa followed, optionally, by the character "E" or "e",
 * followed by an exponent. The exponent must be an integer. The mantissa must
  * be a decimal number. The representations for exponent and mantissa must
  * follow the lexical rules for integer and decimal. If the "E" or "e" and the
  * following exponent are omitted, an exponent value of 0 is assumed.
  * 
  * The special values positive and negative zero, positive and negative infinity
  * and not-a-number have lexical representations 0, -0, INF, -INF and NaN,
  * respectively.
  * 
  * Decimal: decimal has a lexical representation consisting of a finite-length
  * sequence of decimal digits (#x30-#x39) separated by a period as a decimal
  * indicator. An optional leading sign is allowed. If the sign is omitted, "+"
  * is assumed. Leading and trailing zeroes are optional. If the fractional part
  * is zero, the period and following zero(es) can be omitted.
  * 
  * @author Grahame Grieve
  */
 
 class NumberValidator {
 
 	private String source;
 	private int cursor;
 	private DecimalFormatOptions options;
 
 	private String whole;
 	private String decimals;
 	private String exponent;
 
 	/**
 	 * @param source
 	 * @param cursor
 	 * @param options
 	 */
 	protected NumberValidator(String source, DecimalFormatOptions options) {
 		super();
 		this.source = source;
 		this.cursor = 0;
 		this.options = options;
 	}
 
 	private boolean optionsBanExponent() {
 		return options != null && options instanceof RealFormatOptions
 				&& ((RealFormatOptions) options).getExponent() != null
 				&& !((RealFormatOptions) options).getExponent().booleanValue();
 	}
 
 	private boolean optionsRequireExponent() {
 		return options != null && options instanceof RealFormatOptions
 				&& ((RealFormatOptions) options).getExponent() != null
 				&& ((RealFormatOptions) options).getExponent().booleanValue();
 	}
 
 	private boolean optionsAllowSpecial() {
 		return options != null && options instanceof RealFormatOptions
 				&& ((RealFormatOptions) options).isAllowSpecial();
 	}
 
 	private boolean more() {
 		return cursor < source.length();
 	}
 
 	private char peek() {
 		if (!more())
 			return ' ';
 		else
 			return source.charAt(cursor);
 	}
 
 	private char next() {
 		char ch = peek();
 		cursor++;
 		return ch;
 	}
 
 	private String pos() {
 		return Integer.toString(cursor);
 	}
 
 	private void start() throws UOMoNumberFormatException {
 		if (source == null || source.equals("")) //$NON-NLS-1$
 			throw new UOMoNumberFormatException(TEXT_FORMAT,
 					Messages.NumberValidator_Number_empty);
 
 		whole = null;
 		decimals = null;
 		exponent = null;
 	}
 
 	public long parseInteger() throws UOMoNumberFormatException {
 		validateInteger();
 		try {
 			return Long.parseLong(whole);
 		} catch (Exception e) {
 			// can get to here if the number is too big
 			throw new UOMoNumberFormatException(SIZE, e.getMessage());
 		}
 	}
 
 	public BigDecimal parseDecimal() throws UOMoNumberFormatException {
 		validateDecimal();
 		try {
 			return new BigDecimal(whole
 					+ (decimals == null ? "" : Messages.NumberValidator_DOT + decimals)); //$NON-NLS-1$
 		} catch (NumberFormatException e) {
 			// can get to here if the number is too big (for example,
 			// 1.0e10000000000000000000000000000000000000000000000000000000)
 			throw new UOMoNumberFormatException(SIZE, e.getMessage());
 		}
 	}
 
 	public BigDecimal parseReal() throws UOMoNumberFormatException {
 		validateReal();
 		if (whole.equals(NaN))
 			throw new UOMoNumberFormatException(NaN, Messages.NumberValidator_Value_not_a_number);
 		else if (whole.equals(Messages.NumberValidator_INF) || whole.equals(Messages.NumberValidator_plusINF))
 			throw new UOMoNumberFormatException(PINF, Messages.NumberValidator_Value_Infinity);
 		else if (whole.equals(Messages.NumberValidator_minusINF))
 			throw new UOMoNumberFormatException(NINF,
 					Messages.NumberValidator_Value_negative_Infinity);
 		else
 			try {
 				return new BigDecimal(whole
 						+ (decimals == null ? "" : "." + decimals) //$NON-NLS-1$
 						+ (exponent == null ? "" : Messages.NumberValidator_E + exponent)); //$NON-NLS-1$
 			} catch (NumberFormatException e) {
 				throw new UOMoNumberFormatException(SIZE,
 						e.getLocalizedMessage());
 			}
 	}
 
 	public void validateInteger() throws UOMoNumberFormatException {
 		start();
 		whole = processInteger(true, false, Messages.NumberValidator_an_Integer);
 		if (more())
 			throw new UOMoNumberFormatException(TEXT_FORMAT,
 					Messages.NumberValidator_Unexpected_Content_after_Parsing_Int + peek() + "' at character " + pos() //$NON-NLS-2$
 							+ " after parsing integer"); //$NON-NLS-1$
 	}
 
 	public void validateReal() throws UOMoNumberFormatException {
 		start();
 
 		processDecimal();
 		if (more()) {
 			if (peek() != 'e' && peek() != 'E')
 				throw new UOMoNumberFormatException(TEXT_FORMAT,
 						Messages.NumberValidator_Unexpected_Content_at_Char_expecting_E + peek() + "' at character " //$NON-NLS-2$
 								+ pos() + " expecting e or E"); //$NON-NLS-1$
 			if (optionsBanExponent())
 				throw new UOMoNumberFormatException(RULE,
 						Messages.NumberValidator_Exponent_Format_not_allowed_in_Context);
 			next();
 			exponent = processInteger(true, false, Messages.NumberValidator_an_Exponent);
 			if (more()) {
 				throw new UOMoNumberFormatException(TEXT_FORMAT,
 						Messages.NumberValidator_Unexpected_Content_after_Parsing_Exponent + peek() + "' at character " //$NON-NLS-2$
 								+ pos() + " after parsing exponent"); //$NON-NLS-1$
 			}
 		} else if (optionsRequireExponent())
 			throw new UOMoNumberFormatException(
 					UOMoNumberFormatException.Kind.RULE,
 					Messages.NumberValidator_Exponent_Format_required_in_Context);
 
 		checkDigits();
 	}
 
 	public void validateDecimal() throws UOMoNumberFormatException {
 		start();
 
 		processDecimal();
 		if (more())
 			throw new UOMoNumberFormatException(TEXT_FORMAT,
 					Messages.NumberValidator_Unexpected_Content_after_Parsing_Decimal + peek() + "' at character " + pos() //$NON-NLS-2$
 							+ " after parsing decimal"); //$NON-NLS-1$
 
 		checkDigits();
 	}
 
 	private void checkDigits() throws UOMoNumberFormatException {
 		if (options != null) {
 			if (options.getFractionDigits() != DecimalFormatOptions.ANY_DIGITS
 					&& decimals != null) {
 				if (decimals.length() > options.getFractionDigits())
 					throw new UOMoNumberFormatException(
 							UOMoNumberFormatException.Kind.RULE,
 							Messages.NumberValidator_Only_Digits_after_Decimal_allowed_but_found
 									+ Integer.toString(options
 											.getFractionDigits())
 									+ " digits after the decimal are allowed, but found " //$NON-NLS-1$
 									+ Integer.toString(decimals.length()));
 			}
 			if (options.getTotalDigits() != DecimalFormatOptions.ANY_DIGITS) {
 				int len = whole.length()
 						+ (decimals == null ? 0 : decimals.length());
 				if (len > options.getTotalDigits())
 					throw new UOMoNumberFormatException(
 							UOMoNumberFormatException.Kind.RULE,
 							"Only " //$NON-NLS-1$
 									+ Integer.toString(options.getTotalDigits())
 									+ " digits after the decimal are allowed, but found " //$NON-NLS-1$
 									+ Integer.toString(len));
 			}
 		}
 	}
 
 	private String processInteger(boolean allowSign, boolean allowComplex,
 			String context) throws UOMoNumberFormatException {
 		if (!more())
 			throw new UOMoNumberFormatException(TEXT_FORMAT,
 					Messages.NumberValidator_Unexpected_End_of_Source_looking_for + context);
 		StringBuffer b = new StringBuffer();
 		if (allowSign && (peek() == '+' || peek() == '-')) {
 			if (peek() == '+')
 				next();
 			else
 				b.append(next());
 		}
 		if (!more())
 			throw new UOMoNumberFormatException(TEXT_FORMAT,
 					Messages.NumberValidator_Unexpected_End_of_Source_after_Sign_looking_for
 							+ context);
 		if (allowComplex && peek() == 'N') {
 			processSequence(Messages.NumberValidator_NaN, context);
 			b.append("NaN"); //$NON-NLS-1$
 		} else if (allowComplex && peek() == 'I') {
 			processSequence("INF", context);
 			b.append("INF");
 		} else {
 			if (peek() < '0' || peek() > '9')
 				throw new UOMoNumberFormatException(TEXT_FORMAT,
 						Messages.NumberValidator_Unexpected_Content_expecting_Start_of + peek() + "' at character " //$NON-NLS-2$
 								+ pos() + " expecting the start of " + context); //$NON-NLS-1$
 			while (more() && peek() >= '0' && peek() <= '9')
 				b.append(next());
 		}
 		return b.toString();
 	}
 
 	private void processSequence(String mask, String context)
 			throws UOMoNumberFormatException {
 		for (int i = 0; i < mask.length(); i++) {
 			if (!more())
 				throw new UOMoNumberFormatException(TEXT_FORMAT,
 						Messages.NumberValidator_Unexpected_End_of_Source + mask
 								+ "' in " + context); //$NON-NLS-1$
 			char ch = next();
 			if (ch != mask.charAt(i))
 				throw new UOMoNumberFormatException(TEXT_FORMAT,
 						Messages.NumberValidator_Unexpected_Content_NaN + ch + "' at character " + pos() //$NON-NLS-2$
 								+ " attempting to read 'NaN' in " + context); //$NON-NLS-1$
 		}
 		if (more())
 			throw new UOMoNumberFormatException(TEXT_FORMAT,
 					Messages.NumberValidator_Unexpected_Content_following_in_Decimal + peek() + "' at character " + pos() //$NON-NLS-2$
 							+ " following '" + mask + "' in " + context); //$NON-NLS-1$ //$NON-NLS-2$
 
 	}
 
 	private void processDecimal() throws UOMoNumberFormatException {
 		whole = processInteger(true, options == null || optionsAllowSpecial(),
 				Messages.NumberValidator_a_number);
 		if (more() && peek() == '.') {
 			next();
 			decimals = processInteger(false, false, Messages.NumberValidator_decimal_portion);
 		}
 
 	}
 
 }
