 /*
  * $Id: PatternType.java,v 1.26 2007/08/27 11:18:21 agoubard Exp $
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.types;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.text.TextUtils;
 
 /**
  * Abstract base class for pattern types. A pattern type only accepts values
  * that match a certain regular expression.
  *
  * @version $Revision: 1.26 $ $Date: 2007/08/27 11:18:21 $
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.0.0
  */
 public abstract class PatternType extends Type {
 
    /**
     * Pattern string. This is the uncompiled version of {@link #_pattern}.
     * This field cannot be <code>null</code>.
     */
    private final String _patternString;
 
    /**
     * Compiled pattern. This is the compiled version of
     * {@link #_patternString}. This field cannot be <code>null</code>.
     */
    private final Pattern _pattern;
 
    /**
     * Creates a new <code>PatternType</code> instance. The name of the type
     * needs to be specified. The value class (see
     * {@link Type#getValueClass()}) is set to {@link String String.class}.
     *
     * @param name
     *    the name of the type, not <code>null</code>.
     *
     * @param pattern
     *    the regular expression the values must match, not <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null || pattern == null</code>.
     *
     * @throws PatternCompileException
     *    if the specified pattern is considered invalid.
     */
    protected PatternType(String name, String pattern)
    throws IllegalArgumentException, PatternCompileException {
 
       // Explicitly invoke superclass constructor
       super(name, String.class);
 
       // Check preconditions
       MandatoryArgumentChecker.check("pattern", pattern);
 
       // Compile the regular expression to a Pattern object
       try {
          _pattern = Pattern.compile(pattern);
 
       // Handle pattern compilation error
       } catch (PatternSyntaxException cause) {
          PatternCompileException e = new PatternCompileException(pattern);
          e.initCause(cause);
          throw e;
       }
 
       // Store the original pattern string
       _patternString = pattern;
    }
 
    @Override
    protected final void checkValueImpl(String value) throws TypeValueException {
 
       // Determine if the value matches the pattern
      boolean found;
       try {
          Matcher patternMatcher = _pattern.matcher(value);
         found = patternMatcher.find();
 
       // If the call causes an exception, then log that exception and assume
       // the value does not match the pattern
       } catch (Throwable cause) {
          String detail = "Assuming the value \"" + value + "\" is invalid for the pattern \"" + _patternString + "\".";
         Utils.logProgrammingError(PatternType.class.getName(), "checkValueImpl()", null, null, detail, cause);
          throw new TypeValueException(this, value, "Pattern matching caused an exception.", cause);
       }

      if (! found) {
         throw new TypeValueException(this, value, "String does not match pattern.");
      }
    }
 
    @Override
    protected final Object fromStringImpl(String value) {
       return value;
    }
 
    @Override
    public final String toString(Object value)
    throws IllegalArgumentException, ClassCastException, TypeValueException {
       MandatoryArgumentChecker.check("value", value);
       String s = (String) value;
       checkValue(s);
       return s;
    }
 
    @Override
    public String getDescription() {
       return "Text that matches the regular expression " + TextUtils.quote(getPattern()) + '.';
    }
 
    /**
     * Returns the pattern.
     *
     * @return
     *    the pattern, not <code>null</code>.
     */
    public String getPattern() {
       return _patternString;
    }
 }
