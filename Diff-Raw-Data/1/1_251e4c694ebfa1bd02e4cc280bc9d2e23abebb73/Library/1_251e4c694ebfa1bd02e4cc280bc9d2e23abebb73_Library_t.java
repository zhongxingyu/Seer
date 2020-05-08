 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import org.xins.common.Utils;
 
 /**
  * Class that represents the XINS/Java Server Framework library.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class Library extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = Library.class.getName();
 
    /**
     * Perl 5 pattern compiler.
     */
    private static final Perl5Compiler PATTERN_COMPILER = new Perl5Compiler();
 
    /**
     * Regular expression that production release versions of XINS match, and
     * non-production release version do not.
     */
    private static final String PRODUCTION_RELEASE_PATTERN_STRING = "[1-9][0-9]*\\.[0-9]+\\.[0-9]+";
 
    /**
     * The pattern for a URL.
     */
    private static final Pattern PRODUCTION_RELEASE_PATTERN;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this class. This function compiles
     * {@link #PRODUCTION_RELEASE_PATTERN_STRING}
     * to a {@link Pattern} and then stores that in
     * {@link #PRODUCTION_RELEASE_PATTERN}.
     */
    static {
       final String THIS_METHOD = "<clinit>()";
       try {
          PRODUCTION_RELEASE_PATTERN = PATTERN_COMPILER.compile(
             PRODUCTION_RELEASE_PATTERN_STRING,
             Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.CASE_INSENSITIVE_MASK);
 
       } catch (MalformedPatternException exception) {
          final String SUBJECT_CLASS = PATTERN_COMPILER.getClass().getName();
          final String SUBJECT_METHOD = "compile(java.lang.String,int)";
          final String DETAIL = "The pattern \""
                              + PRODUCTION_RELEASE_PATTERN_STRING
                              + "\" is considered malformed.";
 
          throw Utils.logProgrammingError(CLASSNAME,
                                          THIS_METHOD,
                                          SUBJECT_CLASS,
                                          SUBJECT_METHOD,
                                          DETAIL,
                                          exception);
       }
    }
 
    /**
     * Returns the version of this library.
     *
     * @return
     *    the version of this library, for example <code>"1.0.0"</code>,
     *    never <code>null</code>.
     */
    public static final String getVersion() {
       return "%%VERSION%%";
    }
 
    /**
     * Checks if the specified version indicates a production release of XINS.
     *
     * @param version
     *    the XINS version to check, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> is the specified XINS version identifies a
     *    production release of XINS, <code>false</code> if it does not.
     *
     * @throws NullPointerException
     *    if <code>version == null</code>.
     */
    static final boolean isProductionRelease(String version)
    throws NullPointerException {
       Perl5Matcher patternMatcher = new Perl5Matcher();
       return patternMatcher.matches(version, PRODUCTION_RELEASE_PATTERN);
    }
 
    /**
     * Checks if the specified version is a more recent version as this version.
     *
     * @param buildVersion
     *    the XINS version to check, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> is the given version is the same version or more
     *    recent, false is the given version is not a production released or is
     *    less recent or the current version is not a production released.
     *
     * @throws NullPointerException
     *    if <code>version == null</code>.
     */
    static final boolean isMoreRecent(String buildVersion)
    throws NullPointerException {
 
       if (!isProductionRelease(buildVersion) ||
           isProductionRelease(getVersion())) {
          return false;
       }
       if (buildVersion.equals(getVersion())) {
          return true;
       }
       int buildFirstDot = buildVersion.indexOf('.');
       int currentFirstDot = getVersion().indexOf('.');
       int buildMajor = Integer.parseInt(buildVersion.substring(0, buildFirstDot));
       int currentMajor = Integer.parseInt(getVersion().substring(0, currentFirstDot));
       if (buildMajor < currentMajor) {
          return false;
       } else if (buildMajor > currentMajor) {
          return true;
       } else {
          int buildSecondDot = buildVersion.indexOf('.', buildFirstDot + 1);
          int currentSecondDot = getVersion().indexOf('.', currentFirstDot + 1);
          int buildMinor = Integer.parseInt(buildVersion.substring(0, buildSecondDot));
          int currentMinor = Integer.parseInt(getVersion().substring(0, currentSecondDot));
          if (buildMinor < currentMinor) {
             return false;
          } else if (buildMinor > currentMinor) {
             return true;
          } else {
             int buildThirdDot = buildVersion.indexOf('.', buildFirstDot + 1);
             int currentThirdDot = getVersion().indexOf('.', currentFirstDot + 1);
             int buildMinor2 = Integer.parseInt(buildVersion.substring(0, buildThirdDot));
             int currentMinor2 = Integer.parseInt(getVersion().substring(0, currentThirdDot));
             if (buildMinor2 < currentMinor2) {
                return false;
             } else {
                return true;
             }
          }
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Library</code> object.
     */
    private Library() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
