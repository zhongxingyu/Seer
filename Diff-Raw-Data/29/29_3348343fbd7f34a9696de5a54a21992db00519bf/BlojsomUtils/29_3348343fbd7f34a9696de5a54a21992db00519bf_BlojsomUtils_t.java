 package org.ignition.blojsom.util;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.*;
 
 /**
  * BlojsomUtils
  *
  * @author David Czarnecki
  */
 public class BlojsomUtils {
 
     private BlojsomUtils() {
     }
 
     /**
      * Return a file filter which only returns directories
      *
      * @return File filter appropriate for filtering only directories
      */
     public static FileFilter getDirectoryFilter() {
         return new FileFilter() {
             public boolean accept(File pathname) {
                 return (pathname.isDirectory());
             }
         };
     }
 
     /**
      * Return a file filter which takes a list of file extensions to look for
      *
      * @param extensions List of file extensions
      * @return File filter appropriate for filtering out a set of file extensions
      */
     public static FileFilter getExtensionsFilter(final String[] extensions) {
         return new FileFilter() {
             public boolean accept(File pathname) {
                 for (int i = 0; i < extensions.length; i++) {
                     if (pathname.isDirectory()) {
                         return false;
                     }
                     String extension = extensions[i];
                     if (pathname.getName().endsWith(extension)) {
                         return true;
                     }
                 }
                 return false;
             }
         };
     }
 
     /**
      * Parse a comma-separated list of values; also parses over internal spaces
      *
      * @param commaList Comma-separated list
      * @return Individual strings from the comma-separated list
      */
     public static String[] parseCommaList(String commaList) {
         StringTokenizer tokenizer = new StringTokenizer(commaList, ", ");
         ArrayList list = new ArrayList();
         while (tokenizer.hasMoreTokens()) {
             list.add(tokenizer.nextToken());
         }
         if (list.size() == 0) {
             return new String[]{};
         }
         return (String[]) list.toArray(new String[list.size()]);
     }
 
     /**
      * Strip off the blog home directory for a requested blog category
      *
      * @param blogHome Blog home value
      * @param requestedCategory Requested blog category
      * @return Blog category only
      */
     public static String getBlogCategory(String blogHome,
                                          String requestedCategory) {
         requestedCategory = requestedCategory.replace('\\', '/');
         int indexOfBlogHome = requestedCategory.indexOf(blogHome);
         if (indexOfBlogHome == -1) {
             return "";
         }
         indexOfBlogHome += blogHome.length();
         String returnCategory = requestedCategory.substring(indexOfBlogHome);
         returnCategory = removeInitialSlash(returnCategory);
         return "/" + returnCategory;
     }
 
     /**
      * Return a URL to the main blog site without the servlet path requested
      *
      * @param blogURL URL for the blog
      * @param servletPath Servlet path under which the blog is placed
      * @return URL to the blog up to the servlet path
      */
     public static String getBlogSiteURL(String blogURL, String servletPath) {
         int servletPathIndex = blogURL.indexOf(servletPath, 6);
         if (servletPathIndex == -1) {
             return blogURL;
         }
 
         return blogURL.substring(0, servletPathIndex);
     }
 
     /**
      * Return an escaped string where &amp;, &lt;, and &gt; are converted to their HTML equivalents
      *
      * @param input Unescaped string
      * @return Escaped string containing HTML equivalents for &amp;, &lt;, and &gt;
      */
     public static String escapeString(String input) {
         if (input == null) {
             return null;
         }
 
        String unescaped = replace(input, "&", "&amp;");
        unescaped = replace(unescaped, "<", "&lt;");
        unescaped = replace(unescaped, ">", "&gt;");
         return unescaped;
     }
 
     /**
     * Replace any occurances of a string pattern within a string with a different string.
     *
     * @param str The source string.  This is the string that will be searched and have the replacements
     * @param pattern The pattern to look for in str
     * @param replace The string to insert in the place of <i>pattern</i>
     * @return String with replace occurences
     */
    public static String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();

        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    /**
      * Return the file extension for a given filename or <code>null</code> if no file extension
      * is present
      *
      * @param filename Filename
      * @return File extension without the . or <code>null</code> if no file extension is present
      */
     public static String getFileExtension(String filename) {
         int dotIndex = filename.lastIndexOf(".");
         if (dotIndex == -1) {
             return null;
         } else {
             return filename.substring(dotIndex + 1);
         }
     }
 
     /**
      * Return a string of "YYYYMD"
      *
      * @param date Date from which to extract "key"
      * @return String of "YYYYMD"
      */
     public static String getDateKey(Date date) {
         StringBuffer value = new StringBuffer();
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(date);
         value.append(calendar.get(Calendar.YEAR));
         value.append(calendar.get(Calendar.MONTH) + 1);
         value.append(calendar.get(Calendar.DAY_OF_MONTH));
         return value.toString();
     }
 
     /**
      * Remove the initial "/" from a string
      *
      * @param input Input string
      * @return Input string without initial "/" removed or <code>null</code> if the input was null
      */
     public static String removeInitialSlash(String input) {
         if (input == null) {
             return null;
         }
 
         if (!input.startsWith("/")) {
             return input;
         } else {
             return input.substring(1);
         }
     }
 
     /**
      * Return a comparator that uses a file's last modified time to order the files. If the
      * files have the same last modified time, the file's names are compared to order the
      * files.
      */
     public static Comparator FILE_TIME_COMPARATOR = new Comparator() {
         public int compare(Object o1, Object o2) {
             File f1 = (File) o1;
             File f2 = (File) o2;
 
             if (f1.lastModified() > f2.lastModified()) {
                 return -1;
             } else if (f1.lastModified() < f2.lastModified()) {
                 return 1;
             } else {
                 return f1.getName().compareTo(f2.getName());
             }
         }
     };
 
     /**
      * Return a comparator to sort by name
      */
     public static Comparator FILE_NAME_COMPARATOR = new Comparator() {
         public int compare(Object o1, Object o2) {
             String s1 = (String) o1;
             String s2 = (String) o2;
 
             return s1.compareTo(s2);
         }
     };
 }
