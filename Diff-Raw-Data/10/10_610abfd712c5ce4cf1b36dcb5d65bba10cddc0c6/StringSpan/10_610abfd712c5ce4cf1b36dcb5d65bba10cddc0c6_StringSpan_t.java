 /*
  * StringSpan.java
  *
  * Copyright (c) 2009 Operational Dynamics Consulting Pty Ltd
  * 
  * The code in this file, and the program it is a part of, are made available
  * to you by its authors under the terms of the "GNU General Public Licence,
  * version 2" See the LICENCE file for the terms governing usage and
  * redistribution.
  */
 package quill.textbase;
 
 /**
  * A Span multiple characters wide built by reusing an existing String.
  * 
  * @author Andrew Cowie
  */
 /*
  * Relies on Span.createSpan() to ensure there are no surrogates in the
  * String; we could perhaps put a check for that here.
  */
 public class StringSpan extends Span
 {
     private final String data;
 
     StringSpan(String str, Markup markup) {
         super(markup);
 
         if (str.length() == 0) {
             throw new IllegalArgumentException();
         }
 
         data = str;
     }
 
     protected Span copy(Markup markup) {
         return new StringSpan(this.data, markup);
     }
 
     /*
      * The OpenJava implementation reuses the underlying character array, so
      * this is all good.
      */
 
     private StringSpan(Span span, int begin, int end) {
         super(span.getMarkup());
 
         if (begin == end) {
             throw new IllegalArgumentException();
         }
 
         data = span.getText().substring(begin, end);
     }
 
     public String getText() {
         return data;
     }
 
     /*
      * If there are surrogate pairs lurking in the UTF-16 encoded char[], then
      * this will properly reduce them to character count. Maybe we should
      * cache this?
      */
     public int getWidth() {
         return data.length();
     }
 
     public int getChar(int position) {
         return data.charAt(position);
     }
 
     Span split(int begin, int end) {
         if ((end - begin) == 1) {
            return new CharacterSpan((char) getChar(begin), getMarkup());
         } else {
             return new StringSpan(this, begin, end);
         }
     }
 }
