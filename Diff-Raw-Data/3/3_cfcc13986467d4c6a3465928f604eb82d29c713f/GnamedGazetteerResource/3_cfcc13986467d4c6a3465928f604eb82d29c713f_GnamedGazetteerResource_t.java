 /* Created on Feb 4, 2013 by Florian Leitner.
  * Copyright 2013. All rights reserved. */
 package txtfnnl.uima.resource;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.uima.util.Level;
 
 import org.uimafit.descriptor.ConfigurationParameter;
 
 import txtfnnl.utils.stringsim.LeitnerLevenshtein;
 
 /**
  * The JdbcGazetteerResource uses a {@link JdbcConnectionResource#PARAM_DRIVER_CLASS JDBC database}
  * to retrieve the ID, name values used to populate the Gazetteer. It can use any user-defined
  * {@link GnamedGazetteerResource#PARAM_QUERY_SQL query} that selects these ID, name values and
  * uses regular expressions matching for those names.
  * 
  * @author Florian Leitner
  */
 public class GnamedGazetteerResource extends JdbcGazetteerResource {
   public static final String PARAM_NO_GREEK_MAPPING = "DisableGreekMapping";
   @ConfigurationParameter(name = PARAM_NO_GREEK_MAPPING,
       mandatory = false,
       description = "Disable mapping of Latin names of Greek letters ('alpha', 'beta', ...) to their actual characters",
       defaultValue = "false")
   private boolean disableGreekMapping;
   /** Mappings of gene IDs to their taxon IDs. */
   private Map<String, String> taxonMap = new HashMap<String, String>();
 
   public static class Builder extends JdbcGazetteerResource.Builder {
     Builder(String url, String driverClass, String querySql) {
       super(GnamedGazetteerResource.class, url, driverClass, querySql);
     }
 
     /**
      * Disable the mapping of Latin names of Greek letters to the actual Greek letters.
      * <p>
      * The mapping replaces the Strings "alpha" and "ALPHA" in any gene name with the lower-case
      * Greek char for alpha, while "Alpha" will be replaced with the upper-case char. Any other,
      * mixed case spelling will not trigger a mapping. Idem for all other Greek letters.
      */
     public Builder disableGreekMapping() {
       setOptionalParameter(PARAM_NO_GREEK_MAPPING, true);
       return this;
     }
   }
 
   /**
    * Configure a resource for transaction-less, read-write JDBC connections.
    * 
    * @param databaseUrl a JDBC database URL
    * @param driverClassName a fully qualified JDBC driver class name
    * @param query that will retrieve ID, taxon ID, gene name triplets from the database
    */
   public static Builder configure(String databaseUrl, String driverClassName, String query) {
     return new Builder(databaseUrl, driverClassName, query);
   }
 
   public static final char[] GREEK_NAMES_FIRST = { 'A', 'B', 'C', 'D', 'E', 'G', 'I', 'K', 'L',
       'M', 'N', 'O', 'P', 'R', 'S', 'T', 'U', 'X', 'Z', 'a', 'b', 'c', 'd', 'e', 'g', 'i', 'k',
       'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'x', 'z' };
   public static final String[][] GREEK_NAMES = { { "Alpha" }, { "Beta" }, { "Chi" }, { "Delta" },
       { "Epsilon", "Eta" }, { "Gamma" }, { "Iota" }, { "Kappa" }, { "Lambda" }, { "Mu" },
       { "Nu" }, { "Omega", "Omicron" }, { "Pi", "Psi", "Phi" }, { "Rho" }, { "Sigma" },
       { "Tau", "Theta" }, { "Upsilon" }, { "Xi" }, { "Zeta" }, { "alpha" }, { "beta" }, { "chi" },
       { "delta" }, { "epsilon" }, { "eta" }, { "gamma" }, { "iota" }, { "kappa" }, { "lambda" },
       { "mu" }, { "nu" }, { "omicron", "omega" }, { "pi", "phi", "psi" }, { "rho" }, { "sigma" },
       { "tau", "theta" }, { "upsilon" }, { "xi" }, { "zeta" } };
 
   /** Generate the keys, the trie and the key-to-ID mappings. */
   @Override
   public void afterResourcesInitialized() {
     initializeJdbc();
     // fetch a process the mappings
     // uses "key = makeKey(name) && if (key != null) processMapping(dbId, name, key)"
     try {
       Connection conn = getConnection();
       Statement stmt = conn.createStatement();
       logger.log(Level.INFO, "running SQL query: ''{0}''", querySql);
       ResultSet result = stmt.executeQuery(querySql);
       while (result.next()) {
         final String geneId = result.getString(1);
         final String taxId = result.getString(2);
         final String name = result.getString(3);
         put(geneId, name);
         if (!disableGreekMapping) {
           final String nameWithGreekLetters = mapLatinNamesOfGreekLetters(name);
           if (nameWithGreekLetters != null) put(geneId, nameWithGreekLetters);
         }
         taxonMap.put(geneId, taxId);
       }
       conn.close();
     } catch (SQLException e) {
       logger.log(Level.SEVERE, "SQL error", e);
       throw new RuntimeException(e);
     } catch (Exception e) {
       logger.log(Level.SEVERE, "unknown error", e);
       throw new RuntimeException(e);
     }
   }
 
   /**
    * Return a String with the Latin names of Greek letters replaced with actual Greek characters.
    * 
    * @param str to map Latin Strings of Greek letters to Greek characters
    * @return the mapped String or <code>null</code> if no letter was mapped.
    */
   private String mapLatinNamesOfGreekLetters(String str) {
     int len = str.length();
     int last = 0;
     StringBuilder normal = new StringBuilder();
     for (int offset = 0; offset < len - 1; ++offset) {
       char c = str.charAt(offset);
       if (c <= 'z' && c >= 'A') {
         int idx = Arrays.binarySearch(GREEK_NAMES_FIRST, c);
         if (idx > -1) {
           SCAN:
           for (String latin : GREEK_NAMES[idx]) {
             for (int ext = 1; ext < latin.length(); ++ext)
              if (offset + ext >= len || latin.charAt(ext) != str.charAt(offset + ext))
                continue SCAN;
             normal.append(str.subSequence(last, offset));
             normal.appendCodePoint(greekLetterFor(latin));
             last = offset + latin.length();
             offset = last - 1;
             break SCAN;
           }
         }
       }
     }
     if (last > 0) {
       normal.append(str.subSequence(last, len));
       return normal.toString();
     } else {
       return null;
     }
   }
 
   /**
    * Get the Unicode code-point value for the Latin name of a Greek letter.
    * 
    * @param latin the Latin name of a Greek letter
    * @throws IllegalArgumentException if the Latin name is unknown
    */
   private int greekLetterFor(String latin) {
     if (Character.isLowerCase(latin.charAt(0))) {
       for (int i = 0; i < LeitnerLevenshtein.GREEK_LOWER.length; ++i) {
         if (latin.equals(LeitnerLevenshtein.GREEK_LOWER_NAMES[i]))
           return LeitnerLevenshtein.GREEK_LOWER[i];
       }
     } else {
       for (int i = 0; i < LeitnerLevenshtein.GREEK_UPPER.length; ++i) {
         if (latin.equals(LeitnerLevenshtein.GREEK_UPPER_NAMES[i]))
           return LeitnerLevenshtein.GREEK_UPPER[i];
       }
     }
     throw new IllegalArgumentException("unknown Greek letter name " + latin);
   }
 
   /** Return the associated taxon ID for the given gene ID. */
   public String getTaxId(String geneId) {
     return taxonMap.get(geneId);
   }
 }
