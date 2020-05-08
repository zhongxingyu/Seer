 package org.apache.felix.ipojo.everest.services;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 
 /**
  * An object that is used to locate a resource.
  */
 public class Path implements Iterable<String> {
 
     /**
      * The root path.
      */
     private static final Path ROOT = new Path(new String[0], "/");
 
     /**
      * The path separator
      */
     public static final String SEPARATOR = "/";
 
     /**
      * The number of elements in this path.
      */
     private final int m_count;
 
     /**
      * The elements of this path.
      */
     private final String[] m_elements;
 
     /**
      * The string representation of this path.
      */
     private final String m_string;
 
     /**
      * Create a new path from the given elements.
      *
      * @param elements the elements of the path
      */
     private Path(String[] elements) {
         this(elements, toString(elements));
     }
 
     /**
      * Create a new path from the given elements and string representation.
      *
      * @param elements the elements of the path
      * @param string   the string representation of the path
      */
     private Path(String[] elements, String string) {
         m_elements = elements;
         m_count = m_elements.length;
         m_string = string;
     }
 
     // SIMPLE GETTERS
 
     /**
      * @return all the elements of this path
      */
     public String[] getElements() {
         return m_elements.clone();
     }
 
     /**
      * @return the number of elements in the path
      */
     public int getCount() {
         return m_count;
     }
 
     /**
      * Returns an element of this path.
      *
      * @param index the index of the element
      * @return the number of elements in the path
      * @throws IndexOutOfBoundsException if {@code index} is negative, or {@code index}  is greater than or equal to the number of elements
      */
     public String getElement(int index) throws IndexOutOfBoundsException {
         return m_elements[index];
     }
 
     /**
      * @return an iterator over the elements of this path.
      */
     public Iterator<String> iterator() {
         return Collections.unmodifiableList(Arrays.asList(m_elements)).iterator();
     }
 
     // STRUCTURE GETTERS
 
     /**
      * @return the parent of this path, or {@code null} if this path is the root path.
      */
     public Path getParent() {
         if (m_count == 0) {
             return null;
         }
         return getHead(m_count -1);
     }
 
     /**
      * @return the first element of this path
      * @throws IndexOutOfBoundsException if this path is the root path
      */
     public String getFirst() {
         return m_elements[0];
     }
 
     /**
      * @return the last element of this path
      * @throws IndexOutOfBoundsException if this path is the root path
      */
     public String getLast() {
         return m_elements[m_count-1];
     }
 
     /**
      * Get the head of this path.
      *
      * @param count the number of head elements to return
      * @return TODO
      * @throws IndexOutOfBoundsException if this path contains less than {@code level} elements
      */
     public Path getHead(int count) {
         if (count == 0) {
             return ROOT;
         }
         String[] elements = new String[count];
         int endIndex = 1;
         for (int i = 0; i < count; i++) {
             elements[i] = m_elements[i];
             endIndex += elements[i].length() + 1;
         }
         return new Path(elements, m_string.substring(0, endIndex));
     }
 
     /**
      * Get the tail of this path.
      *
      * @param count the number of tail elements to return
      * @return TODO
      * @throws IndexOutOfBoundsException if this path contains less than {@code level} elements
      */
     public Path getTail(int count) {
         if (count == 0) {
             return ROOT;
         }
         String[] elements = new String[count];
         int beginIndex = m_string.length();
         for (int i = 0; i < count; i++) {
             elements[i] = m_elements[m_count - count + i];
             beginIndex -= elements[i].length() + 1;
         }
         return new Path(elements, m_string.substring(beginIndex, m_string.length()));
     }
 
 
     /**
      * Create the string representation for the given list of elements.
      *
      * @param elements the list of elements
      * @return the string representing the list of elements
      */
     private static String toString(String[] elements) {
         if (elements.length == 0) {
             return SEPARATOR;
         }
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < elements.length; i++) {
             sb.append(SEPARATOR);
             sb.append(elements[i]);
         }
         return sb.toString();
     }
 
 
 
     public static Path from(String pathName) {
         if (pathName == null) {
             throw new NullPointerException("null pathName");
         }
 
         // A path is absolute iff it begins with a slash
         boolean isAbsolute = pathName.startsWith(SEPARATOR);
        String tmp = pathName;
         if (isAbsolute) {
             // Remove the leading slash so it won't disturb the path analysis.
            tmp = pathName.substring(1);
            if (tmp.isEmpty()) {
                 // This is the root.
                 return ROOT;
             }
         }
 
         // Cut the path into elements.
        String[] elements = tmp.split(SEPARATOR);
 
         // Check that there are no empty element (caused by double slash) and that there is no trailing slash
        if (tmp.endsWith(SEPARATOR) || Arrays.asList(elements).contains("")) {
             throw new IllegalArgumentException("invalid pathName: " + pathName);
         }
 
         return new Path(elements, pathName);
     }
 
 
     // UTILITY METHODS
 
     @Override
     public String toString() {
         return m_string;
     }
 
     @Override
     public int hashCode() {
         return m_string.hashCode();
     }
 
     @Override
     public boolean equals(Object that) {
         if (this == that) {
             return true;
         } else if (!(that instanceof Path)) {
             return false;
         }
         return m_string.equals(((Path) that).m_string);
     }
 }
