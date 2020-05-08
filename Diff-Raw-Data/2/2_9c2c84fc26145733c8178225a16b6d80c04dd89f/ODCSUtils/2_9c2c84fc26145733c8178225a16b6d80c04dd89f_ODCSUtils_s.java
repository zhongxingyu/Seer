 package cz.cuni.mff.odcleanstore.core;
 
 import org.openrdf.model.BNode;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 
 import java.text.Normalizer;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 
 /**
  * Various utility methods.
  *
  * @author Jan Michelfeit
  */
 public final class ODCSUtils {
     /* Simplified patterns for IRIs and prefixed names  based on
      * specification at http://www.w3.org/TR/rdf-sparql-query/#QSynIRI
      */
     private static final String PN_CHARS_BASE = "A-Za-z\\xC0-\\xFF";
     private static final String PN_CHARS_U = PN_CHARS_BASE + "_";
     private static final String PN_CHARS = PN_CHARS_U + "\\-0-9\\xB7";
     private static final String PN_PREFIX =
             "[" + PN_CHARS_BASE + "](?:[" + PN_CHARS + ".]*[" + PN_CHARS + "])?";
     private static final String PN_LOCAL =
             "[" + PN_CHARS_U + "0-9](?:[" + PN_CHARS + ".]*[" + PN_CHARS + "])?";
 
     private static final Pattern PREFIX_PATTERN = Pattern.compile("^" + PN_PREFIX + "$");
     private static final Pattern IRI_PATTERN = Pattern.compile("^[^<>\"{}|^`\\x00-\\x20']*$");
     private static final Pattern PREFIXED_NAME_PATTERN =
             Pattern.compile("^(" + PN_PREFIX + ")?:(" + PN_LOCAL + ")?$");
     private static final Pattern VAR_PATTERN =
             Pattern.compile("^([" + PN_CHARS_U + "]|[0-9])([" + PN_CHARS_U + "]|[0-9]|\\xB7)*$");
 
     private static final Pattern UUID_PATTERN = 
             Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
 
     /** Milliseconds in a second. */
     public static final long MILLISECONDS = 1000L;
     
     /** Time unit 60. */
     public static final long TIME_UNIT_60 = 60L;
     
     /** Time unit 60. */
     public static final int TIME_UNIT_60_INT = 60;
     
     /** Number of hours in a day. */
     public static final long DAY_HOURS = 24L;
     
     /** Jdbc driver class. */
     public static final String JDBC_DRIVER = "virtuoso.jdbc3.Driver";
     
     /** Default encoding - UTF-8. */
     public static final String DEFAULT_ENCODING = "UTF-8";
     
     /** Pattern matching characters to be removed from a literal when being escaped for a SPARQL query. */ 
     public static final Pattern ESCAPE_LITERAL_CHARS_TO_REMOVE = Pattern.compile("[\\x00-\\x09\\x0E-\\x1F]");
     
     /** Pattern matching characters to be escaped in a literal when being escaped for a SPARQL query. */
     public static final Pattern ESCAPE_LITERAL_CHARS_TO_ESCAPE = Pattern.compile("([\"'`\\\\])");
 
     /**
      * Compare two values which may be null. Null is considered less than all non-null values.
      * @param o1 first compared value or null
      * @param o2 second compared value or null
      * @param <T> type of compared values
      * @return a negative integer, zero, or a positive integer as o1 is less than, equal to, or greater than o2
      */
     public static <T> int nullProofCompare(Comparable<T> o1, T o2) {
         if (o1 != null && o2 != null) {
             return o1.compareTo(o2);
         } else if (o1 != null) {
             return 1;
         } else if (o2 != null) {
             return -1;
         } else {
             assert o1 == null && o2 == null;
             return 0;
         }
     }
     
     /**
      * Compare two values which may be null. Null is considered less than all non-null values.
      * @param o1 first compared value or null
      * @param o2 second compared value or null
      * @param comparator comparator used to perform the comparison.
      * @param <T> type of compared values
      * @return a negative integer, zero, or a positive integer as o1 is less than, equal to, or greater than o2
      */
     public static <T> int nullProofCompare(T o1, T o2, Comparator<T> comparator) {
         if (o1 != null && o2 != null) {
             return comparator.compare(o1, o2);
         } else if (o1 != null) {
             return 1;
         } else if (o2 != null) {
             return -1;
         } else {
             assert o1 == null && o2 == null;
             return 0;
         }
     }
     
     /**
      * Compare two strings which may be null using case-ignoring comparison.
      * Null is considered less than all non-null values.
      * @param s1 first compared value or null
      * @param s2 second compared value or null
      * @param <T> type of compared values
      * @return a negative integer, zero, or a positive integer as o1 is less than, equal to, or greater than o2
      */
     public static <T> int nullProofCompareIgnoreCase(String s1, String s2) {
         if (s1 != null && s2 != null) {
             return s1.compareToIgnoreCase(s2);
         } else if (s1 != null) {
             return 1;
         } else if (s2 != null) {
             return -1;
         } else {
             assert s1 == null && s2 == null;
             return 0;
         }
     }
     
     /**
      * Compare two values which may be null for equality.
      * @param o1 first compared value or null
      * @param o2 second compared value or null
      * @return return true iff both values are null or if they satisfy equals()
      */
     public static boolean nullProofEquals(Object o1, Object o2) {
         if (o1 == null) {
             return o1 == o2;
         } else {
             return o1.equals(o2);
         }
     }
 
     /**
      * Checks whether the given URI is a valid IRI.
      * See http://www.w3.org/TR/rdf-sparql-query/#QSynIRI
      * @param uri the string to check
      * @return true iff the given string is a valid IRI
      */
     public static boolean isValidIRI(String uri) {
        return !uri.isEmpty() && IRI_PATTERN.matcher(uri).matches();
     }
     
     /**
      * Checks whether the given string is a valid SPARQL variable name.
      * See http://www.w3.org/TR/rdf-sparql-query/
      * @param varName the string to check
      * @return true iff the given string is a valid SPARQL variable name
      */
     public static boolean isValidSparqlVar(String varName) {
         return !varName.isEmpty() && VAR_PATTERN.matcher(varName).matches();
     }
     
     
     /**
      * Checks whether the given URI is a prefixed name.
      * See http://www.w3.org/TR/rdf-sparql-query/#QSynIRI.
      * @param uri the string to check
      * @return true iff the given string is a valid IRI
      */
     public static boolean isPrefixedName(String uri) {
         return !uri.isEmpty() && PREFIXED_NAME_PATTERN.matcher(uri).matches();
     }
 
     /**
      * Checks whether the given string is a valid namespace prefix.
      * See http://www.w3.org/TR/rdf-sparql-query/#QSynIRI
      * @param prefix the string to check
      * @return true iff the given string is a valid namespace prefix
      */
     public static boolean isValidNamespacePrefix(String prefix) {
         return !prefix.isEmpty() && PREFIX_PATTERN.matcher(prefix).matches();
     }
     
     /**
      * Checks if a string is null or an empty string.
      * @param s tested string
      * @return true iff s is null or an empty string
      */
     public static boolean isNullOrEmpty(String s) {
         return s == null || s.length() == 0;
     }
     
     /**
      * Extracts the UUID part from a data named graph URI.
      * @param namedGraphURI URI of a payload/metadata/provenanceMetadata named graph
      * @return the UUID part or null if it the named graph doesn't have the correct format
      */
     public static String extractUUID(String namedGraphURI) {
         if (ODCSUtils.isNullOrEmpty(namedGraphURI)) {
             return null;
         }
         Matcher matcher = UUID_PATTERN.matcher(namedGraphURI);
         return matcher.find() ? matcher.group() : null;
     }
     
     /**
      * Convert the given string to ASCII characters, removing diacritical marks.
      * @param str string to convert
      * @return string containing only ASCII characters
      */
     public static String toAscii(CharSequence str) {
         final int asciiSize = 128;
         String decomposed = Normalizer.normalize(str, Normalizer.Form.NFKD);
         /* Build a new String with only ASCII characters. */
         StringBuilder buf = new StringBuilder(str.length());
         for (int idx = 0; idx < decomposed.length(); ++idx) {
             char ch = decomposed.charAt(idx);
             if (ch < asciiSize) {
                 buf.append(ch);
             }
         }
         return buf.toString();
     }
     
     /**
      * Escapes a literal for use in a SPARQL query.
      * @param literalValue value to be escaped
      * @return escaped value
      */
     public static String escapeSPARQLLiteral(String literalValue) {
         if (literalValue == null) {
             return "";
         }
         
         String escapedValue = literalValue;
         escapedValue = ESCAPE_LITERAL_CHARS_TO_REMOVE.matcher(escapedValue).replaceAll("");
         escapedValue = ESCAPE_LITERAL_CHARS_TO_ESCAPE.matcher(escapedValue).replaceAll("\\\\$1");
         
         return escapedValue;
     }
     
     /**
      * Return the URI identifying a blank node in Virtuoso.
      * @param bNode blank node
      * @return URI identifying bNode in Virtuoso
      * throws UnsupportedOperationException bNode is not a blank node
      */
     public static String getVirtuosoURIForBlankNode(BNode bNode) {
         return "nodeID://" + bNode.getID();
     }
     
     /**
      * Returns an URI representing the given node or null if it is not a resource.
      * For blank nodes returns the Virtuoso blank node identifier.
      * This function only works in conjunction with Virtuoso database.
      * @param value RDF node
      * @return URI representing
      */
     public static String getVirtuosoNodeURI(Value value) {
         if (value instanceof URI) {
             return value.stringValue();
         } else if (value instanceof BNode) {
             return ODCSUtils.getVirtuosoURIForBlankNode((BNode) value);
         } else {
             return null;
         }
     }
     
     /**
      * Converts an object or null reference to a string (null is converted to the empty string).
      * @param obj object to stringify
      * @return string representation of obj
      */
     public static String toStringNullProof(Object obj) {
         return obj == null ? "" : obj.toString();
     }
     
     /**
      * Add a value to the set given in parameter and return modified set; if set is null, create new instance.
      * @param value value to add to the set
      * @param set set to add to or null
      * @return set containing the given value
      * @param <T> item type
      */
     public static <T> Set<T> addToSetNullProof(T value, Set<T> set) {
         Set<T> result = set;
         if (result == null) {
             result = new HashSet<T>();
         }
         result.add(value);
         return result;
     }
     
     /**
      * Add a value to the list given in parameter and return modified list; if list is null, create new instance.
      * New Lists are instances of ArrayList.
      * @param value value to add to the list
      * @param list list to add to or null
      * @return list containing the given value
      * @param <T> item type
      */
     public static <T> List<T> addToListNullProof(T value, List<T> list) {
         final int defaultListSize = 1;
         List<T> result = list;
         if (result == null) {
             result = new ArrayList<T>(defaultListSize);
         }
         result.add(value);
         return result;
     }
     
     /**
      * Returns {@link Value#stringValue() stringValue()} of the given {@link Value} or null if the value is null.
      * @param value value to convert to string
      * @return {@link Value#stringValue() stringValue()} of <code>value</code> {@link Value} or null if <code>value</code> 
      *      is null
      */
     public static String valueToString(Value value) {
         return value == null
                 ? null
                 : value.stringValue();
     }
     
     /**
      * Searches model for triples having the given subject and predicate and returns the object
      * of first such triple, or null if there is no such triple.
      * @param subject subject of searched triples
      * @param predicate predicated of searched triples
      * @param model RDF model to search
      * @return object of the first matching triple in model or null if there is none
      */
     public static Value getSingleObjectValue(Resource subject, URI predicate, Model model) {
         Iterator<Statement> modelIt = model.filter(subject, predicate, null).iterator();
         if (modelIt.hasNext()) {
             return modelIt.next().getObject();
         } else {
             return null;
         }
     }
         
     /** Disable constructor for a utility class. */
     private ODCSUtils() {
     }
 }
