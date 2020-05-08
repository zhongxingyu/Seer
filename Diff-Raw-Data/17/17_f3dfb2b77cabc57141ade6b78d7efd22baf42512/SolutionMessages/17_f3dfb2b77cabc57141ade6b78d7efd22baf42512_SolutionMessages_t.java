 /* *****************************************************************************
  * SolutionMessages.java
 * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2006, 2009 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.compiler;
 import java.io.*;
 import java.lang.*;
 import java.util.*;
 
 // TODO [hqm 3/24/2003] This 3rd-party regexp package can be replaced
 // by the Java language built-in regexp stuff in Java 1.4, when we
 // move up to Java 1.4.
 import org.apache.oro.text.regex.*;
 
 /**  
  * Looks up error messages in a database of known errors, and suggests solutions
  *
  * @author Henry Minsky
  */
 
 class SolutionMessages {
 
     static final Perl5Matcher sMatcher = new Perl5Matcher();
     static final Perl5Compiler compiler = new Perl5Compiler();
     static final Perl5Substitution sSubst = new Perl5Substitution();
 
     /** Perform string substitution using pattern matching.
      * @param str the source string
      * @param pattern pattern to look for
      * @param subst the string to replace <i>pattern</i> with. Perl5 references to matches are allowed. See
      * <a href="http://jakarta.apache.org/oro/api/org/apache/oro/text/regex/Perl5Substitution.html">http://jakarta.apache.org/oro/api/org/apache/oro/text/regex/Perl5Substitution.html</a>
      * @return the string with the substitution made for ALL occurences of the pattern
      */
 
     public static String regsub (String  str,
                           String pattern,
                           String subst) {
         return regsub(str, pattern, subst, Util.SUBSTITUTE_ALL);
     }
 
     /** Perform string substitution using pattern matching.
      * @param str the source string
      * @param p pattern to look for
      * @param s the string to replace <i>pattern</i> with. Perl5 references to matches are allowed. See
      * <a href="http://jakarta.apache.org/oro/api/org/apache/oro/text/regex/Perl5Substitution.html">http://jakarta.apache.org/oro/api/org/apache/oro/text/regex/Perl5Substitution.html</a>
      * @param numSubs number of substitutions to perform, Util.SUBSTITUTE_ALL will cause all occurences to be replaced
      * @return the string with the substitution made for numSubs occurences of the pattern
      */
 
     public static String regsub (String  str, String p, String s, int numSubs) {
         try {
             Pattern pattern = compiler.compile(p);
             Perl5Substitution subst = new Perl5Substitution(s);
 
             String result = Util.substitute(sMatcher, pattern, subst, str, numSubs);
             return result;
         } catch (MalformedPatternException e) {
             throw new CompilationError(e);
         }
     }
 
     /**
      * Return true if the regexp pattern is found to occur in the input string.
      * @param input the input string
      * @param p the pattern
      */
 
     public static  boolean regexp (String input, String p) {
         try {
             Pattern pattern = compiler.compile(p);
             return sMatcher.contains(input, pattern);
         } catch (MalformedPatternException e) {
             // We should probably print something to a debug log or something to mention that the pattern we tested
             // threw an error and probably has some bogus regexp syntax.
             return false;
         }
     }
 
 
 
     // We classify the error messages into several areas, to make it
     // easier to give a specific solution that might apply.
     static final String PARSER       = "PARSER";
     static final String VALIDATOR    = "VALIDATOR";
     static final String VIEWCOMPILER = "VIEWCOMPILER";
 
     static final ArrayList errs = new ArrayList();
 
     static {
         errs.add(new SolutionMessage(PARSER,
                                      // This error message indicates a stray XML escape character
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The content of elements must consist of well-formed character data or markup"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-101")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Look for stray or unmatched XML escape chars ( <, >, or & ) in your source code. When using '<' in a Script, XML requires wrapping the Script content with '<![CDATA[' and ']]>'. "
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-108")
                                                                         ));
 
         errs.add(new SolutionMessage(PARSER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="entity name must immediately follow the '&' in the entity reference"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-117")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Look for unescaped '&' characters in your source code."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-124")
                                                                         ));
                                      
         errs.add(new SolutionMessage(VIEWCOMPILER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Was expecting one of: instanceof"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-133")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The element and attribute names in .lzx files are case-sensitive; Look for a mistake in the upper/lower case spelling of attribute names, i.e., \"onClick\" instead of \"onclick\""
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-140")
                                                                         ));
 
 
         errs.add(new SolutionMessage(PARSER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="\"\" is not a valid local name"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-150")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Make sure that every <class> and <attribute> tag element contains a non-empty 'name' attribute, and each <method> element contains a non-empty 'name' or 'event' attribute."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-157")
                                                                         ));
 
         errs.add(new SolutionMessage(PARSER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The root element is required in a well-formed document"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-166")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Check for invalid UTF8 characters in your source file. LZX XML files may contain only legal UTF-8 character codes. If you see a character with an accent over it, it might be the problem."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-173")
                                                                         ));
 
         errs.add(new SolutionMessage(PARSER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Content is not allowed in prolog"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-182")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Some text editors may insert a Byte-Order Mark (the sequence of characters 0xEFBBBF) at the start of your source file without your knowledge. Please remove any non-whitespace characters before the start of the first '<' character."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-189")
                                                                         ));
 
         errs.add(new SolutionMessage(PARSER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="The reference to entity.*must end with the"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-198")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Look for a misplaced or unescaped ampersand ('&') character in your source code."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-205")
                                                                         ));
 
 
        errs.add(new SolutionMessage(VIEWCOMPILER, 
                                     "unable to build font",
                                     "The font may be a bold or italic style, try adding style=\"bold\" or \"italic\" attribute to font tag"));

         errs.add(new SolutionMessage(VIEWCOMPILER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Lexical error.  The source location is for the element that contains the erroneous script"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-215")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Examine the compiler warnings for warnings about undefined class or attribute names."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-222")
                                      ));
 
         errs.add(new SolutionMessage(PARSER,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Error in building"
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-231")
 ,
 /* (non-Javadoc)
  * @i18n.test
  * @org-mes="Check for invalid UTF8 characters in your source file. LZX XML files may contain only legal UTF-8 character codes. If you see a character with an accent over it, it might be the problem."
  */
                         org.openlaszlo.i18n.LaszloMessages.getMessage(
                                 SolutionMessages.class.getName(),"051018-238")
                                                                         ));
 
 
 
 
     }
 
 
     /** Look through the list of known error message strings, looking for a match,
      * and return the suggested solution.
      *
      * @param err an error message you want to look up the solution for
      * @param type the class of error (parser, validator, viewcompiler), or null for any match
      *
      * @return a solution message if available, otherwise the empty string
     */
     static String findSolution (String err, String type) {
         for (int i = 0; i < errs.size(); i++) {
             SolutionMessage msg = (SolutionMessage) errs.get(i);
             // Look for match of the err message within our error string
             if ((type == null || msg.errType.equals(type)) && regexp(err, msg.errMessage)) {
                 return msg.solution;
             }
         }
         return "";
     }
 
     /** Find matching solution from the first  matching solution pattern */
     static String findSolution (String err) {
         return findSolution(err, null);
     }
 
 }
 
 class SolutionMessage {
         String errType;
         String errMessage;
         String solution;
 
         SolutionMessage(String type, String err, String sol) {
             errType = type;
             errMessage = err;
             solution = sol;
         }
 
     }
 
