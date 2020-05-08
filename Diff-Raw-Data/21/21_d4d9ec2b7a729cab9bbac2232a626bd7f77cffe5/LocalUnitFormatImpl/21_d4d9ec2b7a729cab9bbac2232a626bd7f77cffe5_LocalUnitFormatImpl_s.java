 /**
  * Copyright (c) 2005, 2011, Werner Keil, Ikayzo and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil, Eric Russell - initial API and implementation
  */
 package org.eclipse.uomo.units.impl.format;
 
 import static org.eclipse.uomo.units.SI.CUBIC_METRE;
 import static org.eclipse.uomo.units.SI.GRAM;
 import static org.eclipse.uomo.units.SI.KILOGRAM;
 import static org.eclipse.uomo.units.USCustomary.LITER;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.math.BigInteger;
 import java.text.FieldPosition;
 import java.text.ParsePosition;
 import java.util.Formattable;
 import java.util.Formatter;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import org.eclipse.uomo.units.AbstractConverter;
 import org.eclipse.uomo.units.AbstractConverter.Compound;
 import org.eclipse.uomo.units.AbstractFormat;
 import org.eclipse.uomo.units.AbstractUnit;
 import org.eclipse.uomo.units.SymbolMap;
 import org.eclipse.uomo.units.impl.AddConverter;
 import org.eclipse.uomo.units.impl.AnnotatedUnit;
 import org.eclipse.uomo.units.impl.BaseUnit;
 import org.eclipse.uomo.units.impl.ExpConverter;
 import org.eclipse.uomo.units.impl.LogConverter;
 import org.eclipse.uomo.units.impl.MultiplyConverter;
 import org.eclipse.uomo.units.impl.RationalConverter;
 import org.eclipse.uomo.units.impl.TransformedUnit;
 import org.eclipse.uomo.units.Messages;
 import org.unitsofmeasurement.unit.Unit;
 import org.unitsofmeasurement.unit.UnitConverter;
 
 import com.ibm.icu.util.ULocale;
 
 /**
  * <p>
  * This class represents the local sensitive format.
  * </p>
  * 
  * <h3>Here is the grammar for Units in Extended Backus-Naur Form (EBNF)</h3>
  * <p>
  * Note that the grammar has been left-factored to be suitable for use by a
  * top-down parser generator such as <a
  * href="https://javacc.dev.java.net/">JavaCC</a>
  * </p>
  * <table width="90%" align="center">
  * <tr>
  * <th colspan="3" align="left">Lexical Entities:</th>
  * </tr>
  * <tr valign="top">
  * <td>&lt;sign&gt;</td>
  * <td>:=</td>
  * <td>"+" | "-"</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;digit&gt;</td>
  * <td>:=</td>
  * <td>"0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;superscript_digit&gt;</td>
  * <td>:=</td>
  * <td>"⁰" | "¹" | "²" | "³" | "⁴" | "⁵" | "⁶" | "⁷" | "⁸" | "⁹"</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;integer&gt;</td>
  * <td>:=</td>
  * <td>(&lt;digit&gt;)+</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;number&gt;</td>
  * <td>:=</td>
  * <td>(&lt;sign&gt;)? (&lt;digit&gt;)* (".")? (&lt;digit&gt;)+ (("e" | "E")
  * (&lt;sign&gt;)? (&lt;digit&gt;)+)?</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;exponent&gt;</td>
  * <td>:=</td>
  * <td>( "^" ( &lt;sign&gt; )? &lt;integer&gt; ) <br>
  * | ( "^(" (&lt;sign&gt;)? &lt;integer&gt; ( "/" (&lt;sign&gt;)?
  * &lt;integer&gt; )? ")" ) <br>
  * | ( &lt;superscript_digit&gt; )+</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;initial_char&gt;</td>
  * <td>:=</td>
  * <td>? Any Unicode character excluding the following: ASCII control &
  * whitespace (&#92;u0000 - &#92;u0020), decimal digits '0'-'9', '('
  * (&#92;u0028), ')' (&#92;u0029), '*' (&#92;u002A), '+' (&#92;u002B), '-'
  * (&#92;u002D), '.' (&#92;u002E), '/' (&#92;u005C), ':' (&#92;u003A), '^'
  * (&#92;u005E), '²' (&#92;u00B2), '³' (&#92;u00B3), '·' (&#92;u00B7), '¹'
  * (&#92;u00B9), '⁰' (&#92;u2070), '⁴' (&#92;u2074), '⁵' (&#92;u2075), '⁶'
  * (&#92;u2076), '⁷' (&#92;u2077), '⁸' (&#92;u2078), '⁹' (&#92;u2079) ?</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;unit_identifier&gt;</td>
  * <td>:=</td>
  * <td>&lt;initial_char&gt; ( &lt;initial_char&gt; | &lt;digit&gt; )*</td>
  * </tr>
  * <tr>
  * <th colspan="3" align="left">Non-Terminals:</th>
  * </tr>
  * <tr valign="top">
  * <td>&lt;unit_expr&gt;</td>
  * <td>:=</td>
  * <td>&lt;compound_expr&gt;</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;compound_expr&gt;</td>
  * <td>:=</td>
  * <td>&lt;add_expr&gt; ( ":" &lt;add_expr&gt; )*</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;add_expr&gt;</td>
  * <td>:=</td>
  * <td>( &lt;number&gt; &lt;sign&gt; )? &lt;mul_expr&gt; ( &lt;sign&gt;
  * &lt;number&gt; )?</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;mul_expr&gt;</td>
  * <td>:=</td>
  * <td>&lt;exponent_expr&gt; ( ( ( "*" | "·" ) &lt;exponent_expr&gt; ) | ( "/"
  * &lt;exponent_expr&gt; ) )*</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;exponent_expr&gt;</td>
  * <td>:=</td>
  * <td>( &lt;atomic_expr&gt; ( &lt;exponent&gt; )? ) <br>
  * | (&lt;integer&gt; "^" &lt;atomic_expr&gt;) <br>
  * | ( ( "log" ( &lt;integer&gt; )? ) | "ln" ) "(" &lt;add_expr&gt; ")" )</td>
  * </tr>
  * <tr valign="top">
  * <td>&lt;atomic_expr&gt;</td>
  * <td>:=</td>
  * <td>&lt;number&gt; <br>
  * | &lt;unit_identifier&gt; <br>
  * | ( "(" &lt;add_expr&gt; ")" )</td>
  * </tr>
  * </table>
  * 
  * @author <a href="mailto:eric-r@northwestern.edu">Eric Russell</a>
  * @author <a href="mailto:uomo@catmedia.us">Werner Keil</a>
  * @version 1.4.1 ($Revision: 232 $), $Date: 2010-10-13 17:13:18 +0200 (Mi, 13 Okt 2010) $
  */
 public class LocalUnitFormatImpl extends AbstractFormat {
 
 	// ////////////////////////////////////////////////////
 	// Class variables //
 	// ////////////////////////////////////////////////////
 	/**
 	 * serialVersionUID
 	 */
 	private static final long serialVersionUID = -2046025264383639924L;
 
 	/**
 	 * Name of the resource bundle
 	 */
 	private static final String BUNDLE_NAME = LocalUnitFormatImpl.class.getPackage()
 			.getName() + ".messages"; //$NON-NLS-1$
 
 	/**
 	 * Default locale instance. If the default locale is changed after the class
 	 * is initialized, this instance will no longer be used.
 	 */
 	private static final LocalUnitFormatImpl DEFAULT_INSTANCE = new LocalUnitFormatImpl(
 			new SymbolMapImpl(ResourceBundle.getBundle(BUNDLE_NAME)), Locale.getDefault());
 
 	/** Multiplicand character */
 	private static final char MIDDLE_DOT = '\u00b7'; //$NON-NLS-1$
 	
 	/** Exponent 1 character */
 	private static final char EXPONENT_1 = '\u00b9'; //$NON-NLS-1$
 	
 	/** Exponent 2 character */
 	private static final char EXPONENT_2 = '\u00b2'; //$NON-NLS-1$
 	
 	/** Operator precedence for the addition and subtraction operations */
 	private static final int ADDITION_PRECEDENCE = 0;
 
 	/** Operator precedence for the multiplication and division operations */
 	private static final int PRODUCT_PRECEDENCE = ADDITION_PRECEDENCE + 2;
 
 	/** Operator precedence for the exponentiation and logarithm operations */
 	private static final int EXPONENT_PRECEDENCE = PRODUCT_PRECEDENCE + 2;
 
 
 	
 	/**
 	 * Operator precedence for a unit identifier containing no mathematical
 	 * operations (i.e., consisting exclusively of an identifier and possibly a
 	 * prefix). Defined to be <code>Integer.MAX_VALUE</code> so that no operator
 	 * can have a higher precedence.
 	 */
 	private static final int NOOP_PRECEDENCE = Integer.MAX_VALUE;
 
 	private final ULocale uLocale;
 	
 	// /////////////////
 	// Class methods //
 	// /////////////////
 	/**
 	 * Returns the instance for the current default locale (non-ascii characters
 	 * are allowed)
 	 */
 	public static LocalUnitFormatImpl getInstance() {
 		return DEFAULT_INSTANCE;
 	}
 
 	/**
 	 * Returns an instance for the given locale.
 	 * 
 	 * @param locale
 	 */
 	public static LocalUnitFormatImpl getInstance(Locale locale) {
 		return new LocalUnitFormatImpl(new SymbolMapImpl(ResourceBundle.getBundle(
 				BUNDLE_NAME, locale)), locale);
 	}
 
 	/** Returns an instance for the given symbol map. */
 	protected static LocalUnitFormatImpl getInstance(SymbolMapImpl symbols, Locale locale) {
 		return new LocalUnitFormatImpl(symbols, locale);
 	}
 
 	// //////////////////////
 	// Instance variables //
 	// //////////////////////
 	/**
 	 * The symbol map used by this instance to map between
 	 * {@link org.unitsofmeasure.Unit Unit}s and <code>String</code>s, etc...
 	 */
 	private transient SymbolMapImpl symbolMap;
 
 	// ////////////////
 	// Constructors //
 	// ////////////////
 	/**
 	 * Base constructor.
 	 * 
 	 */
 	public LocalUnitFormatImpl() {
 		this(new SymbolMapImpl(ResourceBundle.getBundle(
 				BUNDLE_NAME, Locale.getDefault())), Locale.getDefault());
 	}
 	
 	/**
 	 * Private constructor.
 	 * 
 	 * @param symbols
 	 *            the symbol mapping.
 	 */
 	private LocalUnitFormatImpl(SymbolMapImpl symbols, Locale loc) {
 		symbolMap = symbols;
 		uLocale = ULocale.forLocale(loc);
 	}
 
 	// //////////////////////
 	// Instance methods //
 	// //////////////////////
 	/**
 	 * Get the symbol map used by this instance to map between
 	 * {@link org.unitsofmeasure.Unit Unit}s and <code>String</code>s, etc...
 	 * 
 	 * @return SymbolMap the current symbol map
 	 */
 	public SymbolMap getSymbolMap() {
 		return symbolMap;
 	}
 
 	// //////////////
 	// Formatting //
 	// //////////////
 	public Appendable format(Unit<?> unit, Appendable appendable)
 			throws IOException {
 		formatInternal(unit, appendable);
 		if (unit instanceof AnnotatedUnit<?>) {
 			AnnotatedUnit<?> annotatedUnit = (AnnotatedUnit<?>) unit;
 			if (annotatedUnit.getAnnotation() != null) {
 				appendable.append('{');
 				appendable.append(annotatedUnit.getAnnotation());
 				appendable.append('}');
 			}
 		}
 		return appendable;
 	}
 
 	public Unit<?> parse(CharSequence csq, ParsePosition cursor)
 			throws IllegalArgumentException {
 		// Parsing reads the whole character sequence from the parse position.
 		int start = cursor != null ? cursor.getIndex() : 0;
 		int end = csq.length();
 		if (end <= start) {
 			return AbstractUnit.ONE;
 		}
 		String source = csq.subSequence(start, end).toString().trim();
 		if (source.length() == 0) {
 			return AbstractUnit.ONE;
 		}
 		try {
 			UnitParser parser = new UnitParser(symbolMap, new StringReader(
 					source));
 			Unit<?> result = parser.parseUnit();
 			if (cursor != null)
 				cursor.setIndex(end);
 			return result;
 		} catch (ParseException e) {
 			if (cursor != null) {
 				if (e.currentToken != null) {
 					cursor.setErrorIndex(start + e.currentToken.endColumn);
 				} else {
 					cursor.setErrorIndex(start);
 				}
 			}
 			throw new IllegalArgumentException(e.getMessage());
 		} catch (TokenMgrError e) {
 			cursor.setErrorIndex(start);
 			throw new IllegalArgumentException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Format the given unit to the given StringBuffer, then return the operator
 	 * precedence of the outermost operator in the unit expression that was
 	 * formatted. See {@link ConverterFormat} for the constants that define the
 	 * various precedence values.
 	 * 
 	 * @param unit
 	 *            the unit to be formatted
 	 * @param buffer
 	 *            the <code>StringBuffer</code> to be written to
 	 * @return the operator precedence of the outermost operator in the unit
 	 *         expression that was output
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private int formatInternal(Unit<?> unit, Appendable buffer)
 			throws IOException {
 		if (unit instanceof AnnotatedUnit<?>) {
 			unit = ((AnnotatedUnit<?>) unit).getActualUnit();
 			// } else if (unit instanceof ProductUnit<?>) {
 			// ProductUnit<?> p = (ProductUnit<?>)unit;
 		}
 		String symbol = symbolMap.getSymbol(unit);
 		if (symbol != null) {
 			buffer.append(symbol);
 			return NOOP_PRECEDENCE;
 		} else if (unit.getProductUnits() != null) {
 			Map<Unit<?>, Integer> productUnits = (Map<Unit<?>, Integer>) unit.getProductUnits();
 			int negativeExponentCount = 0;
 			// Write positive exponents first...
 			boolean start = true;
 			for (Map.Entry<Unit<?>, Integer> e : productUnits.entrySet()) {
 				int pow = e.getValue();
 				if (pow >= 0) {
 					formatExponent(e.getKey(), pow, 1, !start, buffer);
 					start = false;
 				} else {
 					negativeExponentCount += 1;
 				}
 			}
 			// ..then write negative exponents.
 			if (negativeExponentCount > 0) {
 				if (start) {
 					buffer.append('1');
 				}
 				buffer.append('/');
 				if (negativeExponentCount > 1) {
 					buffer.append('(');
 				}
 				start = true;
 				for (Map.Entry<Unit<?>, Integer> e : productUnits.entrySet()) {
 					int pow = e.getValue();
 					if (pow < 0) {
 						formatExponent(e.getKey(), -pow, 1, !start, buffer);
 						start = false;
 					}
 				}
 				if (negativeExponentCount > 1) {
 					buffer.append(')');
 				}
 			}
 			return PRODUCT_PRECEDENCE;
 		} else if (unit instanceof BaseUnit<?>) {
 			buffer.append(((BaseUnit<?>) unit).getSymbol());
 			return NOOP_PRECEDENCE;
 		} else if (unit.getSymbol() != null) { // Alternate unit.
 			buffer.append(unit.getSymbol());
 			return NOOP_PRECEDENCE;
 		} else { // A transformed unit or new unit type!
 			UnitConverter converter = null;
 			boolean printSeparator = false;
 			StringBuilder temp = new StringBuilder();
 			int unitPrecedence = NOOP_PRECEDENCE;
 			Unit<?> parentUnit = unit.getSystemUnit();
 			converter = ((AbstractUnit<?>) unit).getConverterToMetric();
 			if (KILOGRAM.equals(parentUnit)) {
 				// More special-case hackery to work around gram/kilogram
 				// incosistency
 				if (unit.equals(GRAM)) {
 					buffer.append(symbolMap.getSymbol(GRAM));
 					return NOOP_PRECEDENCE;
 				}
 				parentUnit = GRAM;
 				if (unit instanceof TransformedUnit<?>) {
 					converter = ((TransformedUnit<?>) unit).toParentUnit();
 				} else {
 					converter = unit.getConverterTo((Unit) GRAM);
 				}
 			} else if (CUBIC_METRE.equals(parentUnit)) {
 				if (converter != null) {
 					parentUnit = LITER;
 				}
 			}
 
			if (unit instanceof TransformedUnit<?>) {
				TransformedUnit<?> transUnit = (TransformedUnit<?>) unit;
 				parentUnit = transUnit.getParentUnit();
 //				String x = parentUnit.toString();
 			}
 
 			unitPrecedence = formatInternal(parentUnit, temp);
 			printSeparator = !parentUnit.equals(AbstractUnit.ONE);
 			int result = formatConverter(converter, printSeparator,
 					unitPrecedence, temp);
 			buffer.append(temp);
 			return result;
 		}
 	}
 
 	/**
 	 * Format the given unit raised to the given fractional power to the given
 	 * <code>StringBuffer</code>.
 	 * 
 	 * @param unit
 	 *            Unit the unit to be formatted
 	 * @param pow
 	 *            int the numerator of the fractional power
 	 * @param root
 	 *            int the denominator of the fractional power
 	 * @param continued
 	 *            boolean <code>true</code> if the converter expression should
 	 *            begin with an operator, otherwise <code>false</code>. This
 	 *            will always be true unless the unit being modified is equal to
 	 *            Unit.ONE.
 	 * @param buffer
 	 *            StringBuffer the buffer to append to. No assumptions should be
 	 *            made about its content.
 	 */
 	private void formatExponent(Unit<?> unit, int pow, int root,
 			boolean continued, Appendable buffer) throws IOException {
 		if (continued) {
 			buffer.append(MIDDLE_DOT);
 		}
 		StringBuilder temp = new StringBuilder();
 		int unitPrecedence = formatInternal(unit, temp);
 
 		if (unitPrecedence < PRODUCT_PRECEDENCE) {
 			temp.insert(0, '('); //$NON-NLS-1$
 			temp.append(')'); //$NON-NLS-1$
 		}
 		buffer.append(temp);
 		if ((root == 1) && (pow == 1)) {
 			// do nothing
 		} else if ((root == 1) && (pow > 1)) {
 			String powStr = Integer.toString(pow);
 			for (int i = 0; i < powStr.length(); i += 1) {
 				char c = powStr.charAt(i);
 				switch (c) {
 				case '0':
 					buffer.append('\u2070'); //$NON-NLS-1$
 					break;
 				case '1':
 					buffer.append(EXPONENT_1); //$NON-NLS-1$
 					break;
 				case '2':
 					buffer.append(EXPONENT_2);
 					break;
 				case '3':
 					buffer.append('\u00b3'); //$NON-NLS-1$
 					break;
 				case '4':
 					buffer.append('\u2074'); //$NON-NLS-1$
 					break;
 				case '5':
 					buffer.append('\u2075'); //$NON-NLS-1$
 					break;
 				case '6':
 					buffer.append('\u2076'); //$NON-NLS-1$
 					break;
 				case '7':
 					buffer.append('\u2077'); //$NON-NLS-1$
 					break;
 				case '8':
 					buffer.append('\u2078'); //$NON-NLS-1$
 					break;
 				case '9':
 					buffer.append('\u2079'); //$NON-NLS-1$
 					break;
 				}
 			}
 		} else if (root == 1) {
 			buffer.append('^'); //$NON-NLS-1$
 			buffer.append(String.valueOf(pow));
 		} else {
 			buffer.append("^("); //$NON-NLS-1$
 			buffer.append(String.valueOf(pow));
 			buffer.append('/'); //$NON-NLS-1$
 			buffer.append(String.valueOf(root));
 			buffer.append(')'); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Formats the given converter to the given StringBuffer and returns the
 	 * operator precedence of the converter's mathematical operation. This is
 	 * the default implementation, which supports all built-in UnitConverter
 	 * implementations. Note that it recursively calls itself in the case of a
 	 * {@link org.eclipse.uomo.units.internal.AbstractConverter.converter.UnitConverter.Compound
 	 * Compound} converter.
 	 * 
 	 * @param converter
 	 *            the converter to be formatted
 	 * @param continued
 	 *            <code>true</code> if the converter expression should begin
 	 *            with an operator, otherwise <code>false</code>.
 	 * @param unitPrecedence
 	 *            the operator precedence of the operation expressed by the unit
 	 *            being modified by the given converter.
 	 * @param buffer
 	 *            the <code>StringBuffer</code> to append to.
 	 * @return the operator precedence of the given UnitConverter
 	 */
 	private int formatConverter(UnitConverter converter, boolean continued,
 			int unitPrecedence, StringBuilder buffer) {
 		ParsePrefix prefix = symbolMap
 				.getPrefixObject((AbstractConverter) converter);
 		if ((prefix != null) && (unitPrecedence == NOOP_PRECEDENCE)) {
 			buffer.insert(0, symbolMap.getSymbol(prefix));
 			return NOOP_PRECEDENCE;
 		} else if (converter instanceof AddConverter) {
 			if (unitPrecedence < ADDITION_PRECEDENCE) {
 				buffer.insert(0, '(');
 				buffer.append(')');
 			}
 			double offset = ((AddConverter) converter).getOffset();
 			if (offset < 0) {
 				buffer.append("-"); //$NON-NLS-1$
 				offset = -offset;
 			} else if (continued) {
 				buffer.append("+"); //$NON-NLS-1$
 			}
 			long lOffset = (long) offset;
 			if (lOffset == offset) {
 				buffer.append(lOffset);
 			} else {
 				buffer.append(offset);
 			}
 			return ADDITION_PRECEDENCE;
 		} else if (converter instanceof LogConverter) {
 			double base = ((LogConverter) converter).getBase();
 			StringBuffer expr = new StringBuffer();
 			if (base == StrictMath.E) {
 				expr.append("ln"); //$NON-NLS-1$
 			} else {
 				expr.append("log"); //$NON-NLS-1$
 				if (base != 10) {
 					expr.append((int) base);
 				}
 			}
 			expr.append("("); //$NON-NLS-1$
 			buffer.insert(0, expr);
 			buffer.append(")"); //$NON-NLS-1$
 			return EXPONENT_PRECEDENCE;
 		} else if (converter instanceof ExpConverter) {
 			if (unitPrecedence < EXPONENT_PRECEDENCE) {
 				buffer.insert(0, '(');
 				buffer.append(')');
 			}
 			StringBuffer expr = new StringBuffer();
 			double base = ((ExpConverter) converter).getBase();
 			if (base == StrictMath.E) {
 				expr.append('e');
 			} else {
 				expr.append((int) base);
 			}
 			expr.append('^'); 
 			buffer.insert(0, expr);
 			return EXPONENT_PRECEDENCE;
 		} else if (converter instanceof MultiplyConverter) {
 			if (unitPrecedence < PRODUCT_PRECEDENCE) {
 				buffer.insert(0, '(');
 				buffer.append(')');
 			}
 			if (continued) {
 				buffer.append(MIDDLE_DOT);
 			}
 			double factor = ((MultiplyConverter) converter).getFactor();
 			long lFactor = (long) factor;
 			if (lFactor == factor) {
 				buffer.append(lFactor);
 			} else {
 				buffer.append(factor);
 			}
 			return PRODUCT_PRECEDENCE;
 		} else if (converter instanceof RationalConverter) {
 			if (unitPrecedence < PRODUCT_PRECEDENCE) {
 				buffer.insert(0, '(');
 				buffer.append(')');
 			}
 			RationalConverter rationalConverter = (RationalConverter) converter;
 			if (!rationalConverter.getDividend().equals(BigInteger.ONE)) {
 				if (continued) {
 					buffer.append(MIDDLE_DOT);
 				}
 				buffer.append(rationalConverter.getDividend());
 			}
 			if (!rationalConverter.getDivisor().equals(BigInteger.ONE)) {
 				buffer.append('/');
 				buffer.append(rationalConverter.getDivisor());
 			}
 			return PRODUCT_PRECEDENCE;
 		} else if (converter instanceof AbstractConverter.Compound) {
 			AbstractConverter.Compound compound = (Compound) converter;
 			if (compound.getLeft() == AbstractConverter.IDENTITY) {
 				return formatConverter(compound.getRight(), true,
 						unitPrecedence, buffer);
 			} else {				
 		    	if (compound.getLeft() instanceof Formattable) {
 		    		return formatFormattable((Formattable)compound.getLeft(), unitPrecedence, buffer);
 		    	} else if (compound.getRight() instanceof Formattable) {
 		    		return formatFormattable((Formattable)compound.getRight(), unitPrecedence, buffer);	    	
 				} else {
 					return formatConverter(compound.getLeft(), true,
 							unitPrecedence, buffer);
 				}
 			}
 				// return formatConverter(compound.getRight(), true,
 				// unitPrecedence, buffer);
 			
 		} else {
 			if (converter != null) {
 				throw new IllegalArgumentException(
 						"Unable to format the given UnitConverter: " + converter.getClass()); //$NON-NLS-1$
 			} else
 				throw new IllegalArgumentException(
 						"Unable to format, no UnitConverter given"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Formats the given <code>Formattable</code> to the given StringBuffer and returns the
 	 * given precedence of the converter's mathematical operation.
 	 * 
 	 * @param f
 	 *            the formattable to be formatted
 	 * @param unitPrecedence
 	 *            the operator precedence of the operation expressed by the unit
 	 *            being modified by the given converter.
 	 * @param buffer
 	 *            the <code>StringBuffer</code> to append to.
 	 * @return the given operator precedence
 	 */
 	private int formatFormattable(Formattable f, int unitPrecedence, StringBuilder buffer) {
 		Formatter fmt = new Formatter();
 		fmt.format(Messages.LocalFormat_Pattern, f);
 		buffer.replace(0, 1, fmt.toString());
 		return unitPrecedence;
 	}
 		
 	/**
 	 * Override Format.format().
 	 * 
 	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer,
 	 *      java.text.FieldPosition)
 	 */
 	@Override
 	public StringBuffer format(Object obj, StringBuffer toAppendTo,
 			FieldPosition pos) {
 		try {
 			Unit<?> unit = (Unit<?>) obj;
 			// fmt.setCurrency(currency.getCurrency());
 			return (StringBuffer) format(unit, toAppendTo);
 		} catch (IOException ie) {
 			throw new IllegalArgumentException(
 					"Invalid type: " + obj.getClass().getName()); //$NON-NLS-1$
 		} catch (ClassCastException e) {
 			throw new IllegalArgumentException(
 					"Invalid type: " + obj.getClass().getName()); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Override Format.parseObject().
 	 * 
 	 * @see java.text.Format#parseObject(java.lang.String,
 	 *      java.text.ParsePosition)
 	 */
 	public Object parseObject(String source, ParsePosition pos) {
 		return parse(source, pos);
 	}
 
 	public ULocale getLocale() {
 		return uLocale;
 	}
 }
