 package org.hackystat.sensorbase.uripattern;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 
 
 /**
  * Implements a UriPattern, such as "file://foo/*.java", which can then be matched against a 
  * concrete URI string, such as "file://foo/Bar.java".  UriPatterns can be "atomic" or "compound".
  * <p> 
  * An atomic UriPattern matches against a single string that can contain wildcard characters
  * like "*", "**", or "?".  
  * <p>
  * A compound UriPattern consists of atomic UriPatterns that are composed
  * together using the "+" and "-" operators. 
  * For example, (UriPattern1) + (UriPattern2) means (UriPattern1 OR UriPattern2). 
  * (UriPattern1) - (UriPattern2) means (UriPattern1 AND (NOT UriPattern2)).
  * Note that in compound UriPatterns, all atomic patterns must be enclosed in parentheses, and
  * only one level of parentheses is supported. 
  * <p>
  * Note: Matching is case-sensitive, and only the forward slash is supported as a path separator.
  * So, Windows-based sensors must convert their file paths before sending them!
  * 
  * @author Philip Johnson (adapted from code originally written for Hackystat 7 by Qin Zhang).
  *
  */
  
 public class UriPattern implements Comparable<UriPattern> {
 
   /** The string provided by the User defining this UriPattern. */
   private String rawPattern; 
 
   /** The processed Pattern instance created from the rawPattern. */
   private Pattern pattern = null;
 
   /** The pattern used to split a rawPattern representing a compound pattern into its components.*/
   private java.util.regex.Pattern splitPattern = java.util.regex.Pattern
       .compile("\\(([^\\(\\)]*)\\)");
   
   /** True if this UriPattern is a "top-level" UriPattern, which simplifies the matching process. */
   private boolean isTopLevel; 
   
   /** True if this UriPattern is the "matchAll" UriPattern ("**"). */
   private boolean isMatchAll;
 
   /**
    * Create a UriPattern instance. There are three possible wildcard characters:
    * <ul>
    * <li>'**': matches all directories.</li>
    * <li>'*': zero or more characters. </li>
    * <li>'?': one and only one character.</li>
    * </ul>
    * 
    * @param pattern The UriPattern. If null is passed, the pattern defaults to "**".
    */
   public UriPattern(String pattern) {
     this.rawPattern = (pattern == null) ? "**" : pattern;
     this.isTopLevel = determineTopLevel();
     this.isMatchAll = "**".equals(pattern);
     
     Matcher matcher = this.splitPattern.matcher(this.rawPattern);
     int searchStartIndex = 0;
     while (matcher.find()) {
 
       if (this.pattern == null) {
         if (this.rawPattern.substring(searchStartIndex, matcher.start()).trim().length() != 0) {
           throw new RuntimeException("Illegal pattern.");
         }
         this.pattern = new AtomicPattern(matcher.group(1));
       }
       else {
         String strOperator = this.rawPattern.substring(searchStartIndex, matcher.start()).trim();
         if ("+".equals(strOperator)) {
           this.pattern = new CompoundPattern(Operator.OR, new Pattern[] { this.pattern,
               new AtomicPattern(matcher.group(1)) });
         }
         else if ("-".equals(strOperator)) {
           Pattern second = new AtomicPattern(matcher.group(1));
           CompoundPattern temp = new CompoundPattern(Operator.NOT, new Pattern[] { second });
           this.pattern = new CompoundPattern(Operator.AND, new Pattern[] { this.pattern, temp });
         }
         else {
           throw new RuntimeException("Illegal pattern.");
         }
       }
 
       searchStartIndex = matcher.end();
     }
 
     if (this.pattern == null) {
       this.pattern = new AtomicPattern(this.rawPattern);
     }
     else {
       if (this.rawPattern.substring(searchStartIndex).trim().length() != 0) {
         throw new RuntimeException("Illegal pattern.");
       }
     }
   }
   
   /**
    * Returns true if resource matches any of the UriPatterns.
    * @param resource The resource of interest. 
    * @param uriPatterns The list of UriPatterns.
    * @return True if there is a match. 
    */
   public static boolean matches(String resource, List<UriPattern> uriPatterns) {
     for (UriPattern pattern : uriPatterns) {
       if (pattern.matches(resource)) {
         return true;
       }
     }
     return false;
   }
 
   /**
    * Returns a List of UriPatterns extracted from the passed Project.
    * @param project The project containing a list of UriPattern strings. 
    * @return The List of UriPattern instances. 
    */
   public static List<UriPattern> getPatterns(Project project) {
     List<UriPattern> patterns = new ArrayList<UriPattern>();
     for (String uriPatternString : project.getUriPatterns().getUriPattern()) {
       patterns.add(new UriPattern(uriPatternString));
     }
     return patterns;
   }
 
   /**
    * Returns true if the passed path matches this UriPattern.
    * <p>
    * Matching is case sensitive.
    * <p>
    *  
    * This implemementation is optimized for contexts in which a high percentage of the
    * UriPatterns in use are "top-level". A "top-level" UriPattern is a UriPattern like
    * "file://hackyCore_Kernel/**", where the only wildcard is a trailing "/**".
    * This implementation tests to see if this UriPattern is a top-level, and if so
    * determines the match without recourse to the underlying Ant-based pattern matching machinery.
    * The overhead of checking for top-level is not high, but the performance advantages of this 
    * implementation are significant whenthere are a high number of calls to "matches()" with
    * top-level UriPatterns.
    * <p>
    * For interesting information on File: URLs, see http://www.cs.tut.fi/~jkorpela/fileurl.html.
    * 
    * 
    * @param path The path to be tested against this UriPattern.
    * @return True if it matches, false otherwise.
    */
   public boolean matches(String path) {
     // Take care of the case where this UriPattern is "**" right away.
     if (this.isMatchAll) {
       return true;
     }
 
     // Top-level processing is a little complicated, so I'm surrounding this with a try-catch block 
     // just in case there's a weird boundary condition I didn't think of.
     try {
       if (this.isTopLevel) {
         // The path has to be at least as long as the top-level UriPattern w/o wildcard.
         if (path.length() < this.rawPattern.length() - 3) {
           return false;
         }
 
         // If UriPattern is top-level, then the path must match exactly except for last 3 chars
         for (int i = 0; i < this.rawPattern.length() - 3; i++) {
           if (path.charAt(i) != this.rawPattern.charAt(i)) {
             return false;
           }
         }
         // Now make sure that either the path ends at the end of the top-level UriPattern
         // or else that it has a separator right then.
         return ((path.length() == (this.rawPattern.length() - 3))
             || (path.charAt(this.rawPattern.length() - 3) == '/')); 
       }
       // Else this UriPattern is not top-level, so do it the normal way.
       else {
         // If not top-level, do the match the hard way.
         return this.pattern.matches(path);
       }
     }
     catch (Exception e) {
       // OK, something bad happened, so try it again the normal way.
       return this.pattern.matches(path);
     }
   }
 
   /**
    * Returns true if this UriPattern is "top-level", such as "file://hackyCore_Kernel/**". 
    * UriPatterns in which the characters * or ? appear before the final three characters
    * are not considered "top-level". The final three characters must be "/**" for the UriPattern
    * to be "top-level".
    * Called by the constructor and cached in this.isTopLevel.
    * 
    * @return True if the UriPattern is "top-level", false otherwise.
    */
   private boolean determineTopLevel() {
     int length = this.rawPattern.length();
     if (length < 4) {
       return false;
     }
     // A top-level file pattern does not have * or ? until the final three characters.
     for (int i = 0; i < length - 3; i++) {
       if ((this.rawPattern.charAt(i) == '*') || (this.rawPattern.charAt(i) == '?')) {
         return false;
       }
     }
     if (this.rawPattern.charAt(length - 1) == '*' && 
         this.rawPattern.charAt(length - 2) == '*' && 
         this.rawPattern.charAt(length - 3) == '/') {
       return true;
     }
     return false;
   }
   
   /**
    * Returns true if this UriPattern is top-level.
    * Package private because this method exists for testing purposes only.
    * @return True if the UriPattern is top-level.
    */
   boolean isTopLevel() {
     return this.isTopLevel;
   }
 
   /**
    * Compares two objects.
    * 
   * @param o The other object.
   * 
    * @return An integer value indicates the relative magnitude of two objects compared.
    */
   public int compareTo(UriPattern another) {
     return this.rawPattern.compareTo(another.rawPattern);
   }
 
   /**
    * Tests whether two objects contain the same pattern.
    * 
    * @param o The other object.
    * 
    * @return True if they are equal.
    */
   @Override
   public boolean equals(Object o) {
     if (!(o instanceof UriPattern)) {
       return false;
     }
     UriPattern another = (UriPattern) o;
     return this.rawPattern.equals(another.rawPattern);
   }
 
   /**
    * Gets the hash code of this object.
    * 
    * @return The hash code.
    */
   @Override
   public int hashCode() {
     return this.rawPattern.hashCode();
   }
 
   /**
    * Returns the 'raw' pattern, which some clients may find a better string representation.
    * 
    * @return The 'raw' pattern as a string.
    */
   public String getRawPattern() {
     return this.rawPattern;
   }
 
   /**
    * Gets the string representation of this file path pattern.
    * 
    * @return The string representation.
    */
   @Override
   public String toString() {
     return "<UriPattern: " + this.rawPattern + ">";
   }
 
 }
