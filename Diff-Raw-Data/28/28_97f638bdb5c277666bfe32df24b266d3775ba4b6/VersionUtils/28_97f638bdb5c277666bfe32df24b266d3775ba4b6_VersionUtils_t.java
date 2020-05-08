 package net.zeroinstall.pom2feed.core;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.common.collect.ImmutableMap;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 /**
  * Utility class for converting Maven versions to Zero Install versions.
  */
 final class VersionUtils {
 
     /**
      * A regular expression describing dotted lists.
      */
     private static final Pattern dottedListPattern = Pattern.compile("^[0-9]+(\\.[0-9]+)*$");
 
     private VersionUtils() {
     }
 
     /**
      * Converts a Maven version string into a Zero Install version string.
      */
     public static String convertVersion(String mavenVersion) {
         String[] parts = checkNotNull(mavenVersion.trim()).split("-");
         StringBuilder ziVersion = new StringBuilder();
 
         if (!isDottedList(parts[0])) {
             ziVersion.append("1-");
         }
 
         for (int i = 0; i < parts.length; i++) {
             ziVersion.append(pom2feedVersionPart(parts[i]));
 
             if (i < parts.length - 1) {
                 ziVersion.append("-");
             }
         }
 
         return ziVersion.toString();
     }
 
     private static boolean isDottedList(String part) {
         return dottedListPattern.matcher(part).matches();
     }
     /**
      * Maps Maven version qualifiers to Zero Install version qualifiers.
      */
     private static final ImmutableMap<String, String> QUALIFIER_MAP = ImmutableMap.<String, String>builder()
             .put("snapshot", "pre-")
             .put("milestone", "pre1-")
             .put("m", "pre1-")
             .put("alpha", "pre2-")
             .put("a", "pre2-")
             .put("beta", "pre3-")
             .put("b", "pre3-")
             .put("rc", "rc")
             .put("cr", "rc")
             .put("ga", "0")
             .put("final", "0")
             .put("rev", "post-")
             .put("sp", "post")
             .build();
 
     private static String pom2feedVersionPart(String part) {
         String prefix = "";
         for (final Entry<String, String> pair : QUALIFIER_MAP.entrySet()) {
             if (part.toLowerCase(Locale.ENGLISH).startsWith(pair.getKey())) {
                 part = part.substring(pair.getKey().length());
                 prefix = pair.getValue();
                 break;
             }
         }
 
         if (!isDottedList(part)) {
             long number = convertToAsciiNumbers(part);
             part = (number == 0) ? "" : Long.toString(convertToAsciiNumbers(part));
         }
 
         if (prefix.endsWith("-") && part.equals("")) {
             return prefix.substring(0, prefix.length() - 1);
         } else {
             return prefix + part;
         }
     }
 
     static long convertToAsciiNumbers(String value) {
         if (value.length() >= 9) {
             return Long.MAX_VALUE;
         }
 
         long result = 0;
        for (char c : value.toCharArray()) {
            result = (result << 7) + (byte) c;
         }
         return result;
     }
 
     /**
      * Determines whether a string might be a Maven version range.
      */
     public static boolean isMavenRange(String value) {
         return value.contains("(") || value.contains(")")
                 || value.contains("[") || value.contains("]");
     }
 
     /**
      * Converts a Maven version range string into a Zero Install version range
      * string.
      */
     public static String convertRange(String pomVersionRange) {
         StringBuilder result = new StringBuilder();
 
         boolean inInterval = false;
         boolean leftOpen = false;
         StringBuilder version = new StringBuilder();
         for (char c : pomVersionRange.toCharArray()) {
             if (c == '[') {
                 inInterval = true;
                 leftOpen = false;
             } else if (c == '(') {
                 inInterval = true;
                 leftOpen = true;
             } else if (c == ')') {
                 inInterval = false;
                 if (version.length() > 0) {
                     result.append("!").append(convertVersion(version.toString()));
                     version = new StringBuilder();
                 }
             } else if (c == ']') {
                 inInterval = false;
                 if (version.length() > 0) {
                     result.append("!").append(convertVersion(version.toString())).append("-post");
                     version = new StringBuilder();
                 }
             } else if (c == ',') {
                 if (inInterval) {
                     if (version.length() > 0) {
                         result.append(convertVersion(version.toString()));
                         version = new StringBuilder();
                         if (leftOpen) {
                             result.append("-post");
                         }
                     }
                     result.append("..");
                 } else {
                     if (version.length() > 0) {
                         result.append(convertVersion(version.toString()));
                         version = new StringBuilder();
                     }
                     result.append("|");
                 }
             } else {
                 version.append(c);
             }
         }
         if (version.length() > 0) {
             result.append(convertVersion(version.toString()));
         }
 
         return result.toString();
     }
 }
