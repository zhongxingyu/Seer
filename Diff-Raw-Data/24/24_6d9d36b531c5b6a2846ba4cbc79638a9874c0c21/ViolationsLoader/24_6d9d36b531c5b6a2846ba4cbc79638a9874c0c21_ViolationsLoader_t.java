 package org.sonar.ide.shared;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Source;
 import org.sonar.wsclient.services.SourceQuery;
 import org.sonar.wsclient.services.Violation;
 import org.sonar.wsclient.services.ViolationQuery;
 
 import java.util.*;
 
 /**
  * Note: Violation on zero line means violation on whole file.
  *
  * @author Evgeny Mandrikov
  */
 public final class ViolationsLoader {
   private static final Logger LOG = LoggerFactory.getLogger(ViolationsLoader.class);
 
   /**
    * Returns violations from specified sonar for specified resource.
    *
    * @param sonar       sonar
    * @param resourceKey resource key
    * @return violations
    * @deprecated because returns incorrect line numbers, use
    *             {@link #getViolations(org.sonar.wsclient.Sonar, String, String[])}
    *             or {@link #getViolations(org.sonar.wsclient.Sonar, String, String)} instead
    */
   @Deprecated
   public static Collection<Violation> getViolations(Sonar sonar, String resourceKey) {
     LOG.info("Loading violations for {}", resourceKey);
     ViolationQuery query = ViolationQuery.createForResource(resourceKey);
     Collection<Violation> violations = sonar.findAll(query);
     LOG.info("Loaded {} violations", violations.size());
     return violations;
   }
 
   /**
    * Returns hash code for specified string after removing whitespaces.
    *
    * @param str string
    * @return hash code for specified string after removing whitespaces
    */
   public static int getHashCode(String str) {
     return StringUtils.deleteWhitespace(str).hashCode();
   }
 
   /**
    * Returns hash codes for specified strings after removing whitespaces.
    *
    * @param str strings
    * @return hash codes for specified strings after removing whitespaces
    */
   public static int[] getHashCodes(String[] str) {
     int[] hashCodes = new int[str.length];
     for (int i = 0; i < str.length; i++) {
       hashCodes[i] = getHashCode(str[i]);
     }
     return hashCodes;
   }
 
   private static Collection<Violation> removeSimilar(Source source, Collection<Violation> violations) {
     Set<Violation> hashSet = new TreeSet<Violation>(new ViolationComparator(source));
     for (Violation violation : violations) {
       hashSet.add(violation);
     }
     return hashSet;
   }
 
   static class ViolationComparator implements Comparator<Violation> {
     private Source source;
 
     public ViolationComparator(Source source) {
       this.source = source;
     }
 
     public int compare(Violation v1, Violation v2) {
       String line = source.getLine(v1.getLine());
       String line2 = source.getLine(v2.getLine());
       if (getHashCode(line) == getHashCode(line2)) {
         return 0;
       }
       return v1.getLine() - v2.getLine();
     }
   }
 
   /**
    * Sets proper line numbers for violations by matching modified source code with code from server.
   * Actually this is an implementation of heuristic algorithm - magic happens here.
    * <p>
    * Currently this method just compares hash codes (see {@link #getHashCode(String)}).
    * Works for O(V*L) time, where V - number of violations and L - number of lines in modified source code.
    * </p>
    *
    * @param violations violations
    * @param source     source code from server
    * @param lines      source code
    * @return violations with proper line numbers
    */
   public static List<Violation> convertLines(Collection<Violation> violations, Source source, String[] lines) {
     List<Violation> result = new ArrayList<Violation>();
     int[] hashCodes = getHashCodes(lines);
     for (Violation violation : violations) {
       int originalLine = violation.getLine();
       if (originalLine == 0) {
         // skip violation on whole file
         continue;
       }
       String originalSourceLine = source.getLine(originalLine);
       int originalHashCode = getHashCode(originalSourceLine);
       boolean found = false;
       for (int i = 0; i < hashCodes.length; i++) {
         if (hashCodes[i] == originalHashCode) {
           if (found) {
             // may be more than one match, but we take into account only first
             LOG.warn("Found more than one match for violation");
             break;
           }
           violation.setLine(i + 1);
           found = true;
         }
       }
       // skip violation, which doesn't match any line
       if (found) {
         result.add(violation);
       }
     }
     return result;
   }
 
   /**
    * Returns violations from specified sonar for specified resource and source code.
    *
    * @param sonar       sonar
    * @param resourceKey resource key
    * @param lines       source code
    * @return violations
    */
   public static List<Violation> getViolations(Sonar sonar, String resourceKey, String[] lines) {
     LOG.info("Loading violations for {}", resourceKey);
     ViolationQuery violationQuery = ViolationQuery.createForResource(resourceKey);
     Collection<Violation> violations = sonar.findAll(violationQuery);
     LOG.info("Loaded {} violations", violations.size());
     SourceQuery sourceQuery = new SourceQuery(resourceKey);
     Source source = sonar.find(sourceQuery);
     return convertLines(violations, source, lines);
   }
 
   /**
    * Returns violations from specified sonar for specified resource and source code.
    *
    * @param sonar       sonar
    * @param resourceKey resource key
    * @param text        source code
    * @return violations
    */
   public static List<Violation> getViolations(Sonar sonar, String resourceKey, String text) {
     String[] lines = text.split("\n");
     return getViolations(sonar, resourceKey, lines);
   }
 
   /**
    * Hide utility-class constructor.
    */
   private ViolationsLoader() {
   }
 }
