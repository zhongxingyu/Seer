 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.ows.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * Utility class for reading Key Value Pairs from a http query string.
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @author Gabriel Rold?n, Axios
  * @author Justin Deoliveira, TOPP
  *
  * @version $Id$
  */
 public class KvpUtils {
     /** Class logger */
     private static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.vfny.geoserver.requests.readers");
 
     /**
      * Defines how to tokenize a string by using some sort of delimiter.
      * <p>
      * Default implementation uses {@link String#split(String)} with the
      * regular expression provided at the constructor. More specialized
      * subclasses may just override <code>readFlat(String)</code>.
      * </p>
      * @author Gabriel Roldan
      * @since 1.6.0
      */
     public static class Tokenizer {
         private String regExp;
 
         public Tokenizer(String regExp) {
             this.regExp = regExp;
         }
 
         private String getRegExp() {
             return regExp;
         }
 
         public String toString() {
             return getRegExp();
         }
         
         public List readFlat(final String rawList){
             if ((rawList == null || rawList.trim().equals(""))) {
                 return Collections.EMPTY_LIST;
             } else if (rawList.equals("*")) {
                 // handles explicit unconstrained case
                 return Collections.EMPTY_LIST;
             }
             // -1 keeps trailing empty strings in the pack
             String[] split = rawList.split(getRegExp(), -1);
             return new ArrayList(Arrays.asList(split));
         }
     }
     /** Delimeter for KVPs in the raw string */
     public static final Tokenizer KEYWORD_DELIMITER = new Tokenizer("&");
 
     /** Delimeter that seperates keywords from values */
     public static final Tokenizer VALUE_DELIMITER = new Tokenizer("=");
 
     /** Delimeter for outer value lists in the KVPs */
     public static final Tokenizer OUTER_DELIMETER = new Tokenizer("\\)\\(") {
         public List readFlat(final String rawList) {
             List list = new ArrayList(super.readFlat(rawList));
             final int len = list.size();
             if (len > 0) {
                 String first = (String) list.get(0);
                String last = (String) list.get(len - 1);
                 if (first.startsWith("(")) {
                     list.set(0, first.substring(1));
                 }
                 if (last.endsWith(")")) {
                     list.set(len - 1, last.substring(0, last.length() - 1));
                 }
             }
             return list;
         }
     };
 
     /** Delimeter for inner value lists in the KVPs */
     public static final Tokenizer INNER_DELIMETER = new Tokenizer(",");
 
     /** Delimeter for multiple filters in a CQL filter list (<code>";"</code>) */
     public static final Tokenizer CQL_DELIMITER = new Tokenizer(";");
 
     /**
      * Attempts to parse out the proper typeNames from the FeatureId filters.
      * It simply uses the value before the '.' character.
      *
      * @param rawFidList the strings after the FEATUREID url component.  Should
      *        be found using kvpPairs.get("FEATUREID") in this class or one of
      *        its children
      *
      * @return A list of typenames, made from the featureId filters.
      *
      * @throws WfsException If the structure can not be read.
      */
     public static List getTypesFromFids(String rawFidList) {
         List typeList = new ArrayList();
         List unparsed = readNested(rawFidList);
         Iterator i = unparsed.listIterator();
 
         while (i.hasNext()) {
             List ids = (List) i.next();
             ListIterator innerIterator = ids.listIterator();
 
             while (innerIterator.hasNext()) {
                 String fid = innerIterator.next().toString();
                 LOGGER.finer("looking at featureId" + fid);
 
                 String typeName = fid.substring(0, fid.lastIndexOf("."));
                 LOGGER.finer("adding typename: " + typeName + " from fid");
                 typeList.add(typeName);
             }
         }
 
         return typeList;
     }
 
     /**
      * Calls {@link #readFlat(String)} with the {@link #INNER_DELIMETER}.
      *
      */
     public static List readFlat(String rawList) {
         return readFlat(rawList, INNER_DELIMETER);
     }
     
     /**
      * Reads a tokenized string and turns it into a list.
      * <p>
      * In this method, the tokenizer is actually responsible to scan the string,
      * so this method is just a convenience to maintain backwards compatibility
      * with the old {@link #readFlat(String, String)} and to easy the use of the
      * default tokenizers {@link #KEYWORD_DELIMITER}, {@link #INNER_DELIMETER},
      * {@link #OUTER_DELIMETER} and {@value #VALUE_DELIMITER}.
      * </p>
      * <p>
      * Note that if the list is unspecified (ie. is null) or is unconstrained
      * (ie. is ''), then the method returns an empty list.
      * </p>
      * 
      * @param rawList
      *            The tokenized string.
      * @param tokenizer
      *            The delimeter for the string tokens.
      * 
      * @return A list of the tokenized string.
      * @see Tokenizer
      */
     public static List readFlat(final String rawList, final Tokenizer tokenizer) {
         return tokenizer.readFlat(rawList);
     }
     
     /**
      * Reads a tokenized string and turns it into a list. In this method, the
      * tokenizer is quite flexible. Note that if the list is unspecified (ie. is
      * null) or is unconstrained (ie. is ''), then the method returns an empty
      * list.
      * <p>
      * If possible, use the method version that receives a well known
      * {@link #readFlat(String, org.geoserver.ows.util.KvpUtils.Tokenizer) Tokenizer},
      * as there might be special cases to catch out, like for the
      * {@link #OUTER_DELIMETER outer delimiter "()"}. If this method delimiter
      * argument does not match a well known Tokenizer, it'll use a simple string
      * tokenization based on splitting out the strings with the raw passed in
      * delimiter.
      * </p>
      * 
      * @param rawList
      *            The tokenized string.
      * @param delimiter
      *            The delimeter for the string tokens.
      * 
      * @return A list of the tokenized string.
      * 
      * @see #readFlat(String, org.geoserver.ows.util.KvpUtils.Tokenizer)
      */
     public static List readFlat(String rawList, String delimiter) {
         Tokenizer delim;
         if (KEYWORD_DELIMITER.getRegExp().equals(delimiter)) {
             delim = KEYWORD_DELIMITER;
         } else if (VALUE_DELIMITER.getRegExp().equals(delimiter)) {
             delim = VALUE_DELIMITER;
         } else if (OUTER_DELIMETER.getRegExp().equals(delimiter)) {
             delim = OUTER_DELIMETER;
         } else if (INNER_DELIMETER.getRegExp().equals(delimiter)) {
             delim = INNER_DELIMETER;
         }else if(CQL_DELIMITER.getRegExp().equals(delimiter)){
             delim = CQL_DELIMITER;
         } else {
             LOGGER.fine("Using not a well known kvp tokenization delimiter: " + delimiter);
             delim = new Tokenizer(delimiter);
         }
         return readFlat(rawList, delim);
     }
 
     /**
      * Reads a nested tokenized string and turns it into a list. This method is
      * much more specific to the KVP get request syntax than the more general
      * readFlat method. In this case, the outer tokenizer '()' and inner
      * tokenizer ',' are both from the specification. Returns a list of lists.
      * 
      * @param rawList
      *            The tokenized string.
      * 
      * @return A list of lists, containing outer and inner elements.
      * 
      * @throws WfsException
      *             When the string structure cannot be read.
      */
     public static List readNested(String rawList) {
         if (LOGGER.isLoggable(Level.FINEST)) {
             LOGGER.finest("reading nested: " + rawList);
         }
 
         List kvpList = new ArrayList(10);
 
         // handles implicit unconstrained case
         if (rawList == null) {
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.finest("found implicit all requested");
             }
 
             kvpList.add(Collections.EMPTY_LIST);
             return kvpList;
 
             // handles explicit unconstrained case
         } else if (rawList.equals("*")) {
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.finest("found explicit all requested");
             }
 
             kvpList.add(Collections.EMPTY_LIST);
             return kvpList;
 
             // handles explicit, constrained element lists
         } else {
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.finest("found explicit requested");
             }
 
             // handles multiple elements list case
             if (rawList.startsWith("(")) {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.finest("reading complex list");
                 }
 
                 List outerList = readFlat(rawList, OUTER_DELIMETER);
                 Iterator i = outerList.listIterator();
 
                 while (i.hasNext()) {
                     kvpList.add(readFlat((String) i.next(), INNER_DELIMETER));
                 }
 
                 // handles single element list case
             } else {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.finest("reading simple list");
                 }
 
                 kvpList.add(readFlat(rawList, INNER_DELIMETER));
             }
 
             return kvpList;
         }
     }
 
     /**
      * creates a Map of key/value pairs from a HTTP style query String
      *
      * @param qString DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      * @deprecated not being used code wise
      */
     public static Map parseKvpSet(String qString) {
         // uses the request cleaner to remove HTTP junk
         String cleanRequest = clean(qString);
         LOGGER.fine("clean request is " + cleanRequest);
 
         Map kvps = new HashMap();
 
         // parses initial request sream into KVPs
         StringTokenizer requestKeywords = new StringTokenizer(cleanRequest.trim(), KEYWORD_DELIMITER.getRegExp());
 
         // parses KVPs into values and keywords and puts them in a HashTable
         while (requestKeywords.hasMoreTokens()) {
             String kvpPair = requestKeywords.nextToken();
             String key;
             String value;
 
             // a bit of a horrible hack for filters, which handles problems of
             //  delimeters, which may appear in XML (such as '=' for
             //  attributes.  unavoidable and illustrates the problems with
             //  mixing nasty KVP Get syntax and pure XML syntax!
             if (kvpPair.toUpperCase().startsWith("FILTER")) {
                 String filterVal = kvpPair.substring(7);
 
                 //int index = filterVal.lastIndexOf("</Filter>");
                 //String filt2 = kvpPair.subString
                 LOGGER.finest("putting filter value " + filterVal);
                 kvps.put("FILTER", filterVal);
             } else {
                 // handles all other standard cases by looking for the correct
                 //  delimeter and then sticking the KVPs into the hash table
                 StringTokenizer requestValues = new StringTokenizer(kvpPair, VALUE_DELIMITER.getRegExp());
 
                 // make sure that there is a key token
                 if (requestValues.hasMoreTokens()) {
                     // assign key as uppercase to eliminate case conflict
                     key = requestValues.nextToken().toUpperCase();
 
                     // make sure that there is a value token
                     if (requestValues.hasMoreTokens()) {
                         // assign value and store in hash with key
                         value = requestValues.nextToken();
                         LOGGER.finest("putting kvp pair: " + key + ": " + value);
                         kvps.put(key, value);
                     }
                 }
             }
         }
 
         LOGGER.fine("returning parsed " + kvps);
 
         return kvps;
     }
 
     /**
      * Cleans an HTTP string and returns pure ASCII as a string.
      *
      * @param raw The HTTP-encoded string.
      *
      * @return The string with the url escape characters replaced.
      */
     public static String clean(String raw) {
         LOGGER.finest("raw request: " + raw);
 
         String clean = null;
 
         if (raw != null) {
             try {
                 clean = java.net.URLDecoder.decode(raw, "UTF-8");
             } catch (java.io.UnsupportedEncodingException e) {
                 LOGGER.finer("Bad encoding for decoder " + e);
             }
         } else {
             return "";
         }
 
         LOGGER.finest("cleaned request: " + raw);
 
         return clean;
     }
 }
