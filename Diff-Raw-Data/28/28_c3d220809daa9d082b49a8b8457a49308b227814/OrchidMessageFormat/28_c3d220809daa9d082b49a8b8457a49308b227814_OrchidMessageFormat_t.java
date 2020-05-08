 /**
  * Copyright (c) 2011, Mikael Svahn, Softhouse Consulting AB
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so:
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package se.softhouse.garden.orchid.commons.text;
 
 import java.text.AttributedCharacterIterator;
 import java.text.FieldPosition;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * OrchidMessageFormat is an extension of the JDK class MessageFormat and
  * provides a means to produce concatenated messages in a language-neutral way.
  * The OrchidMessageFormat provides the possibility to use variable names
  * instead of attribute indexes. This do however requires that the arguments are
  * added to the format method using the {@link OrchidMessageArguments}. <br>
  * Exmaple usage:
  * 
  * <pre>
  * OrchidMessageFormat.format(&quot;Test message {id}&quot;, OrchidMessage.arg(&quot;ID&quot;, 1));
  * </pre>
  * 
  * @author Mikael Svahn
  * 
  */
 public class OrchidMessageFormat extends MessageFormat {
 
 	private static final long serialVersionUID = -7270065959883891187L;
 
 	protected List<Object> argMap;
 	protected OrchidMessageFormatLookup orchidMessageFormatLookup;
 	protected OrchidMessageFormatFunctionExecutorResolver functionResolver = new OrchidMessageFormatFunctionExecutorResolver();
 
 	/**
 	 * Constructs a MessageFormat for the default locale and without any
 	 * pattern.
 	 */
 	public OrchidMessageFormat() {
 		super("");
 	}
 
 	/**
 	 * Constructs a OrchidMessageFormat for the default locale and the specified
 	 * enhanced pattern.
 	 * 
 	 * @param pattern
 	 *            the pattern for this message format
 	 * @exception IllegalArgumentException
 	 *                if the pattern is invalid
 	 */
 	public OrchidMessageFormat(String pattern) {
 		super(pattern);
 	}
 
 	/**
 	 * Constructs a MessageFormat for the specified locale and pattern.
 	 * 
 	 * @param pattern
 	 *            the pattern for this message format
 	 * @param locale
 	 *            the locale for this message format
 	 * @exception IllegalArgumentException
 	 *                if the pattern is invalid
 	 */
 	public OrchidMessageFormat(String pattern, Locale locale) {
 		super(pattern, locale);
 	}
 
 	/**
 	 * Constructs a OrchidMessageFormat for the default locale and the specified
 	 * enhanced pattern code.
 	 * 
 	 * @param pattern
 	 *            the pattern for this message format specified as an enum
 	 * @exception IllegalArgumentException
 	 *                if the pattern is invalid
 	 */
 	public OrchidMessageFormat(OrchidMessageCode pattern) {
 		super(pattern.getPattern());
 	}
 
 	/**
 	 * Constructs a MessageFormat for the specified locale and pattern.
 	 * 
 	 * @param pattern
 	 *            the pattern for this message format specified as an enum
 	 * @param locale
 	 *            the locale for this message format
 	 * @exception IllegalArgumentException
 	 *                if the pattern is invalid
 	 */
 	public OrchidMessageFormat(OrchidMessageCode pattern, Locale locale) {
 		super(pattern.getPattern(), locale);
 	}
 
 	/**
 	 * Creates a OrchidMessageFormat with the given pattern and uses it to
 	 * format the given arguments.
 	 * 
 	 * @param pattern
 	 *            the pattern for this message format
 	 * @param argsBuilder
 	 *            the arguments specified as {@link OrchidMessageArguments}
 	 * @exception IllegalArgumentException
 	 *                if the pattern is invalid, or if an argument in the
 	 *                <code>arguments</code> array is not of the type expected
 	 *                by the format element(s) that use it.
 	 */
 	public static String format(String pattern, OrchidMessageArguments argsBuilder) {
 		OrchidMessageFormat temp = new OrchidMessageFormat(pattern);
 		return temp.format(argsBuilder);
 	}
 
 	/**
 	 * Creates a OrchidMessageFormat with the given pattern and uses it to
 	 * format the given arguments.
 	 * 
 	 * @param pattern
 	 *            the pattern for this message format specified as an enum
 	 * @param argsBuilder
 	 *            the arguments specified as {@link OrchidMessageArguments}
 	 * @exception IllegalArgumentException
 	 *                if the pattern is invalid, or if an argument in the
 	 *                <code>arguments</code> array is not of the type expected
 	 *                by the format element(s) that use it.
 	 */
 	public static String format(OrchidMessageCode pattern, OrchidMessageArguments argsBuilder) {
 		OrchidMessageFormat temp = new OrchidMessageFormat(pattern);
 		return temp.format(argsBuilder);
 	}
 
 	/**
 	 * Formats the arguments to a string using pattern specified for this
 	 * instance.
 	 * 
 	 * @param argsBuilder
 	 *            the arguments specified as {@link OrchidMessageArguments}
 	 * @exception IllegalArgumentException
 	 *                if the Format cannot format the given object
 	 */
 	public String format(OrchidMessageArguments argsBuilder) {
 		return super.format(createArgsArray(argsBuilder));
 	}
 
 	/**
 	 * Formats the arguments and appends the <code>OrchidMessageFormat</code>'s
 	 * pattern, with format elements replaced by the formatted objects, to the
 	 * provided <code>StringBuffer</code>.
 	 * 
 	 * @param argsBuilder
 	 *            the arguments specified as {@link OrchidMessageArguments}
 	 * @exception IllegalArgumentException
 	 *                if the Format cannot format the given object
 	 */
 	public StringBuffer format(OrchidMessageArguments argsBuilder, StringBuffer result, FieldPosition pos) {
 		return super.format(createArgsArray(argsBuilder), result, pos);
 	}
 
 	/**
 	 * See {@link java.text.MessageFormat#formatToCharacterIterator(Object)}
 	 * 
 	 * @param args
 	 *            the arguments specified as {@link OrchidMessageArguments}
 	 * @return
 	 */
 	public AttributedCharacterIterator formatToCharacterIterator(OrchidMessageArguments args) {
 		return super.formatToCharacterIterator(createArgsArray(createArgsArray(args)));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.text.MessageFormat#applyPattern(java.lang.String)
 	 */
 	@Override
 	public void applyPattern(String pattern) {
		if (pattern != null) {
 			super.applyPattern(parseEnhancedPattern(pattern));
 		}
 	}
 
 	/**
 	 * Set the function resolver
 	 * 
 	 * @param functionResolver
 	 *            The functionResolver to set
 	 */
 	public void setFunctionResolver(OrchidMessageFormatFunctionExecutorResolver functionResolver) {
 		this.functionResolver = functionResolver;
 	}
 
 	/**
 	 * Parse the enhanced pattern and converts it to a pattern supported by
 	 * {@link MessageFormat}. The argument map is initialized to handle
 	 * conversion from named arguments to numbered arguments.
 	 * 
 	 * @param pattern
 	 *            The pattern to parse
 	 * @return The converted string
 	 */
 	protected String parseEnhancedPattern(String pattern) {
 		this.argMap = new ArrayList<Object>();
 
 		StringBuilder b = new StringBuilder();
 		StringBuilder arg = new StringBuilder();
 		int currentArgNo = 0;
 		boolean inArg = false;
 		boolean inQuote = false;
 		for (int i = 0; i < pattern.length(); ++i) {
 			char ch = pattern.charAt(i);
 			if (!inArg) {
 				if (ch == '\'') {
 					if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '\'') {
						b.append(ch);
 						++i;
 					} else {
 						inQuote = !inQuote;
 					}
 				} else if (ch == '{' && !inQuote) {
 					inArg = true;
 				}
 				b.append(ch);
 			} else {
 				if (ch == '}' || ch == ',') {
 					String argNo = Integer.toString(currentArgNo++);
 					String name = arg.toString();
 					int idx = name.indexOf(':');
 					if (idx > 0) {
 						this.argMap.add(resolveFunction(name.substring(0, idx), name.substring(idx + 1)));
 					} else {
 						this.argMap.add(name);
 					}
 					b.append(argNo);
 					b.append(ch);
 					arg.delete(0, arg.length());
 					inArg = false;
 				} else {
 					arg.append(ch);
 				}
 			}
 		}
 		return b.toString();
 
 	}
 
 	/**
 	 * Resolves the function and returns an object to insert as an argument
 	 * 
 	 * @param function
 	 *            The function name
 	 * @param value
 	 *            The value associated with the function
 	 * 
 	 * @return The object to insert as an argument
 	 */
 	protected Object resolveFunction(String function, String value) {
 		if (this.functionResolver != null) {
 			return this.functionResolver.resolveFunction(function, value, this.orchidMessageFormatLookup);
 		}
 		return new OrchidMessageFormatFunctionExecutor(function, value, null);
 	}
 
 	/**
 	 * Creates an argument array from named arguments specified in an
 	 * {@link OrchidMessageArguments}.
 	 * 
 	 * @param arguments
 	 *            An array of arguments which if it only contains an
 	 *            {@link OrchidMessageArguments} is resolved to an argument
 	 *            array.
 	 * @return The resolved argument array
 	 */
 	protected Object[] createArgsArray(Object[] arguments) {
 		if (arguments.length == 1 && arguments[0] instanceof OrchidMessageArguments) {
 			return createArgsArray((OrchidMessageArguments) arguments[0]);
 		}
 		return arguments;
 	}
 
 	/**
 	 * Creates an argument array from named arguments specified in an
 	 * {@link OrchidMessageArguments}.
 	 * 
 	 * @param arguments
 	 *            The arguments which will be resolved to an argument array.
 	 * @return The resolved argument array
 	 */
 	public Object[] createArgsArray(OrchidMessageArguments arguments) {
 		Object[] argArray = new Object[this.argMap.size()];
 		for (int i = 0; i < this.argMap.size(); i++) {
 			resolveArg(arguments, argArray, i);
 		}
 		return argArray;
 	}
 
 	/**
 	 * Resolve the argument with number i
 	 * 
 	 * @param args
 	 *            The arguments to use
 	 * @param argArray
 	 *            The resulting argument array to write to
 	 * @param i
 	 *            The number of the argument
 	 */
 	private void resolveArg(OrchidMessageArguments args, Object[] argArray, int i) {
 		Object key = this.argMap.get(i);
 		if (key instanceof OrchidMessageFormatFunctionExecutor) {
 			argArray[i] = ((OrchidMessageFormatFunctionExecutor) key).execute(args, getLocale());
 		} else {
 			argArray[i] = args.getArgs().get(key);
 		}
 	}
 
 	/**
 	 * Set the lookup instance to use when resolving messages within messages
 	 */
 	public void setOrchidMessageFormatLookup(OrchidMessageFormatLookup orchidMessageFormatLookup) {
 		this.orchidMessageFormatLookup = orchidMessageFormatLookup;
 	}
 
 	/**
 	 * Returns the lookup instance that is used when resolving messages within
 	 * messages
 	 */
 	public OrchidMessageFormatLookup getOrchidMessageFormatLookup() {
 		return this.orchidMessageFormatLookup;
 	}
 
 	/**
 	 * Class used to resolve functions found in the pattern
 	 */
 	public static class OrchidMessageFormatFunctionExecutorResolver {
 		/**
 		 * Resolves the function and returns an object to insert as an argument
 		 * 
 		 * @param function
 		 *            The function name
 		 * @param value
 		 *            The value associated with the function
 		 * 
 		 * @return The object to insert as an argument
 		 */
 		protected Object resolveFunction(String function, String value, OrchidMessageFormatLookup lookup) {
 			return new OrchidMessageFormatFunctionExecutor(function, value, lookup);
 		}
 	}
 }
