 package interpreter;
 import tokenizer.Token;
 
 import java.util.ArrayList;
 import java.util.TreeSet;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  * A List of strings and their metadata - the files and locations in those files
  * where they were found.
  */
 public class StringList {
 
     /**
      * Converts the given Arraylist of tokens to a StringList
      *
      * @param aList An ArrayList of tokens
      * @return a StringList of all the tokens's fields in the given ArrayList
      */
     public static StringList toStringList(ArrayList<Token> aList) {
         StringList sl = new StringList();
 
         if (aList != null) {
             while (aList.size() > 0) {
                 sl.add(aList.remove(0));
             }
         }
         return sl;
     }
 
     /**
      * Performs the union operation on the 2 given StringLists
      *
      * @param a A StringList
      * @param b A StringList
      * @return a StringList of all the strings in a and all the tokens in b,
      *          with all metadata from the original StringLists
      */
     public static StringList union(StringList a, StringList b) {
         if (b == null)
             return a;
 
         String[] bStrings = b.strings();
 
         for (int i=0; i < bStrings.length; i++) {
             a.put(bStrings[i], b.get(bStrings[i]));
         }
         return a;
     }
 
     /**
      * Performs the inters operation on the 2 given StringLists
      *
      * @param a A StringList
      * @param b A StringList
      * @return a StringList containing only the strings present in both a and b,
      *          with all the metadata for those strings
      */
     public static StringList inters(StringList a, StringList b) {
         StringList sl = new StringList();
         String[] aStrings = a.strings();
             
         for (int i=0; i < aStrings.length; i++) {
             if (b.contains(aStrings[i])) {
                 sl.put(aStrings[i], a.get(aStrings[i]));
                 sl.put(aStrings[i], b.get(aStrings[i]));
             }
         }
         return sl;
     }
 
     /**
      * Performs the diff operation on the 2 given StringLists, as a diff b
      *
      * @param a A StringList
      * @param b A StringList
      * @return a StringList containing only the strings present in a but not b,
      *          with all the metadata for those strings that are in a and b
      *          removed
      */
     public static StringList diff(StringList a, StringList b) {
         String[] bStrings = b.strings();
                 
         for (int i=0; i < bStrings.length; i++) {
             if (a.contains(bStrings[i])) {
                 a.remove(bStrings[i]);
             }
         }
         return a;
     }
 
     /**
      * Returns the number of strings in the given StringList
      *
      * @param a A StringList
      * @return the number of strings in a
      */
     public static int length(StringList a) {
         return a.length();
     }
 
     /**
      * Returns a string that has a maximal # of locations associated with it
      *
      * @param a A StringList
      * @return a string that has a maximal # of locations associated with it
      */
     public static String maxfreqstring(StringList a) {
         return a.maxfreqstring();
     }
 
 
     /**
      * Maps tokens to files to the locations of the tokens in the files.
      */
     private HashMap<String, HashMap<String, TreeSet<Location>>> list;
 
     /**
      * Constructs a new StringList to represent a string-match list
      */
     public StringList() {
         list = new HashMap<String, HashMap<String, TreeSet<Location>>>();
     }
 
     /**
      * Adds the string and metadata in the given token to this StringList
      * 
      * @param t A token
      */
     public void add(Token t) {
         String literal = t.getString();
         String file = t.getFile();
         Location index = new Location(t.getRow(), t.getStart(), t.getEnd());
         HashMap<String, TreeSet<Location>> fileList;
         TreeSet<Location> indices;
 
         if (literal == null || file == null || index == null) {
             return;
         }
         if (list.containsKey(literal)) {
             // The token's string has been found before
             fileList = list.get(literal);
             if (fileList.containsKey(file)) {
                 // The token's string has been found in this file before
                 indices = fileList.get(file);
                 indices.add(index);
                 fileList.put(file, indices);
             } else {
                 // The token's string has not been found in this file before
                 indices = new TreeSet<Location>();
                 indices.add(index);
                 fileList.put(file, indices);
             }
         } else {
             // The token's string has not been found before
             indices = new TreeSet<Location>();
             indices.add(index);
             fileList = new HashMap<String, TreeSet<Location>>();
             fileList.put(file, indices);
         }
         list.put(literal, fileList);
     }
 
     /**
      * Adds the given string and its metadata in the hashmap to this StringList
      *
      * @param s A string to add to this StringList
      * @param hm A mapping of files that s appears in to the locations in the
      *          files where it appears
      */
     public void put(String s, HashMap<String, TreeSet<Location>> hm) {
         HashMap<String, TreeSet<Location>> fileList;
         TreeSet<Location> oldIndices, newIndices;
         String[] files;
 
         if (s == null || hm == null || hm.size() < 1) {
             // do nothing
         } else {
             if (list.containsKey(s)) {
                 files = hm.keySet().toArray(new String[0]);
                 fileList = list.get(s);
                 
                 for (int i=0; i<files.length; i++) {
                     if (fileList.containsKey(files[i])) {
                         newIndices = fileList.get(files[i]);
                         oldIndices = hm.get(files[i]);
 
                         while (oldIndices.size() > 0) {
                             newIndices.add(oldIndices.pollFirst());
                         }
                         fileList.put(files[i], newIndices);
                     } else {
                         fileList.put(files[i], hm.get(files[i]));
                     }
                 }
                 list.put(s, fileList);
             } else {
                 list.put(s, hm);
             }
         }
     }
 
     /**
      * Removes the given string and all its metadata from this StringList
      *
      * @param s A string to remove from this StringList
      * @return the metadata for s that was removed
      */
     public HashMap<String, TreeSet<Location>> remove(String s) {
         return list.remove(s);
     }
 
     /**
      * Returns true if and only if the given string appears in this StringList
      *
      * @param s A string to search this StringList for
      * @return true if and only if the given string appears in this StringList
      */
     public boolean contains(String s) {
         return list.containsKey(s);
     }
 
     /**
      * Returns the metadata for the given string from this StringList
      *
      * @param s A string to in this StringList
      * @return the metadata for s
      */
     public HashMap<String, TreeSet<Location>> get(String s) {
         return list.get(s);
     }
 
     /**
      * Returns the list of strings in this StringList
      *
      * @return all strings in this StringList
      */
     public String[] strings() {
         return list.keySet().toArray(new String[0]);
     }
 
     /**
      * Returns the number of strings in this StringList
      *
      * @return the number of strings in this StringList
      */
     public int length() {
         return list.size();
     }
 
     /**
      * Returns a string that has a maximal # of locations associated with it
      *
      * @param a A StringList
      * @return a string that has a maximal # of locations associated with it
      */
     public String maxfreqstring() {
         String maxString = "\"\"";
         int max = -1, count;
 
         if (list.size() > 0) {
             for (String string : list.keySet()) {
                 count = 0;
                 for (String file : list.get(string).keySet()) {
                     count += list.get(string).get(file).size();
                 }
                 if (count >= max) {
                     max = count;
                     maxString = "\"" + string + "\"";
                 }
             }
         }
         return maxString;
     }
 
     /**
      * Overrides the generic toString method. Displays the string and all its
      * metadata
      * 
      * @return every string in this StringList and all its metadata
      */
     public String toString() {
         HashMap<String, TreeSet<Location>> files;
         Iterator<Location> it;
         String out = "";
 
         if (list.keySet().size() == 0) {
             return "[]";
         }
        out += "[ ";
         for (String string : list.keySet()) {
             out += "(\"" + string;
             files = list.get(string);
 
             for (String file : files.keySet()) {
                 out += "\" <" + file + "";
                 it = files.get(file).iterator();
 
                 while (it.hasNext()) {
                     out += "; " + it.next();
                 }
                 out += ">";
                 out += "), ";
             }
         }
        if (out.length() > 2) {
            out = out.substring(0,out.length()-2);
        }
        out +=" ]";
 
 
 
         return out;
     }
 
     public ArrayList<String> print() {
         ArrayList<String> strings = new ArrayList<java.lang.String>();
         for (String string : list.keySet()) {
             strings.add(string);
         }
         return strings;
     }
 }
 
 
 /**
  * Represents the line, start index, and end index of a string in a file; All 3
  * are 1-indexed.
  */
 class Location implements Comparable<Location> {
 
     private int line, start, end;
 
     /**
      * Constructs a new Location; note it has no direct ties to any strings
      *
      * @param line a line in a file
      * @param start a column on a line in a file
      * @param end a column on a line in a file
      */
     public Location(int line, int start, int end) {
         this.line = line;
         this.start = start;
         this.end = end;
     }
 
     /**
      * Allows Locations to be sorted. Greater Locations occur later on in files
      *
      * @param that Another Location to compare with this one
      * @return a positive number if this Location occurs later in a file than
      *          that Location, a negative number if this Location occurs earlier
      *          in a file than that Location, or 0 otherwise
      */
     public int compareTo(Location that) {
         int comp;
 
         if (this.line == that.getLine()) {
             if (this.start == that.getStart()) {
                 comp = this.end - that.getEnd();
             } else {
                 comp = this.start - that.getStart();
             }
         } else {
             comp = this.line - that.getLine();
         }
         return comp;
     }
 
     /**
      * Overrides the generic toString method. Displays all information contained
      * in this Location object
      *
      * @return all fields of this Location in a readable format
      */
     public String toString() {
         return "Ln " + line + ", Col " + start + "-" + end;
     }
 
     /**
      * Returns this Location's line
      *
      * @return this Location's line
      */
     public int getLine() {
         return line;
     }
 
     /**
      * Returns this Location's start index within its line
      *
      * @return this Location's start index
      */
     public int getStart() {
         return start;
     }
 
     /**
      * Returns this Location's end index
      *
      * @return this Location's end index
      */
     public int getEnd() {
         return end;
     }
 }
 
 
 
 
 
 
