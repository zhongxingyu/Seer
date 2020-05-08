 /*
  * UnicodeSpan.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.textbase;
 
 import java.lang.ref.WeakReference;
 import java.util.WeakHashMap;
 
 /**
  * A contigiously formatted span of unicode text.
  * 
  * @author Andrew Cowie
  */
 public class UnicodeSpan extends Span
 {
     /**
      * Cached copy of the String this Span came from.
      */
     private final String data;
 
     /**
      * Because Strings can contain Unicode surrogate pairs, we also need the
      * backing data in code point form.
      */
     private final int[] points;
 
     private final int start;
 
     private final int length;
 
     /**
      * Construct a new UnicodeSpan based on the given UTF-16 String,
      * previously calculated to be width characters wide. If it is width 1, a
      * cached String reference will be used instead.
      */
     /*
      * Assumes width in characters was correctly calculated by the calling
      * factory method up in Span.
      */
     UnicodeSpan(String str, int length, int width, Markup markup) {
         super(markup);
 
         int i, j;
         char ch;
 
         if (width == 1) {
             this.data = lookupString(str);
             this.points = lookupPoints(str);
         } else {
             this.data = str;
             this.points = new int[width];
         }
 
         this.start = 0;
         this.length = width;
 
         j = 0;
         for (i = 0; i < length; i++) {
             ch = str.charAt(i);
 
             if (Character.isHighSurrogate(ch)) {
                 this.points[j] = str.codePointAt(i);
                 i++;
             } else if (Character.isLowSurrogate(ch)) {
                 throw new IllegalStateException();
             } else {
                 this.points[j] = ch;
             }
             j++;
         }
     }
 
     /**
      * Create a copy of this Span but with different Markup applying to it.
      */
     private UnicodeSpan(String data, int[] points, int start, int length, Markup markup) {
         super(markup);
         this.data = data;
         this.points = points;
         this.start = start;
         this.length = length;
     }
 
     private static WeakHashMap<String, WeakReference<int[]>> cachePoints;
 
     private static WeakHashMap<String, WeakReference<String>> cacheString;
 
     static {
         cachePoints = new WeakHashMap<String, WeakReference<int[]>>(8);
         cacheString = new WeakHashMap<String, WeakReference<String>>(8);
     }
 
     private static synchronized int[] lookupPoints(final String str) {
         WeakReference<int[]> ref;
         int[] result;
 
         result = null;
 
         ref = cachePoints.get(str);
         if (ref != null) {
             result = ref.get();
         }
 
         if (result == null) {
             result = new int[1];
             ref = new WeakReference<int[]>(result);
             cachePoints.put(str, ref);
             return result;
         } else {
             return result;
         }
     }
 
     private static synchronized String lookupString(final String str) {
         WeakReference<String> ref;
         String result;
 
         result = null;
 
         ref = cacheString.get(str);
         if (ref != null) {
             result = ref.get();
         }
 
         if (result == null) {
             ref = new WeakReference<String>(str);
             cacheString.put(str, ref);
             return str;
         } else {
             return result;
         }
     }
 
     Span copy(Markup markup) {
         return new UnicodeSpan(this.data, this.points, this.start, this.length, markup);
     }
 
     /**
      * Get the character this Span represents, if it is only one character
      * wide. Position is from 0 to width.
      */
     public int getChar(int position) {
         return this.points[position];
     }
 
     /**
      * Get the text behind this Span for representation in the GUI. Since many
      * spans are only one character wide, use {@link #getChar(int) getChar()}
      * directly if building up Strings to pass populate Element bodies.
      */
     public String getText() {
         return this.data;
     }
 
     /**
      * Get the number of <b>characters</b> in this span.
      */
     public int getWidth() {
        return this.points.length;
     }
 
     /**
      * Create a new String by taking a subset of the existing one.
      * <code>begin</code> and <code>end</code> are character offsets.
      */
     Span split(int begin, int end) {
         final int origin, width, first;
         final StringBuilder str;
         int i, point;
         boolean high;
 
         width = end - begin;
         origin = start + begin;
 
         if (width == 1) {
             first = points[origin];
             if (!(Character.isSupplementaryCodePoint(first))) {
                 return new CharacterSpan((char) first, getMarkup());
             }
         }
 
         /*
          * We need to create a new Java String to be cached by the Span. TODO
          * calculate range in UTF-16 chars and reuse via String's substring().
          */
 
         str = new StringBuilder();
         high = false;
 
         for (i = start + begin; i < start + end; i++) {
             point = points[i];
 
             if (Character.isSupplementaryCodePoint(point)) {
                 high = true;
             }
 
             str.appendCodePoint(point);
         }
 
         if (high) {
             return new UnicodeSpan(str.toString(), points, start + begin, width, this.getMarkup());
         } else {
             return new StringSpan(str.toString(), this.getMarkup());
         }
     }
 }
