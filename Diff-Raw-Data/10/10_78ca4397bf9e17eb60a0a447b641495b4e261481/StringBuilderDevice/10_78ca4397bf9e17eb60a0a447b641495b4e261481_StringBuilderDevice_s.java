 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.io;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 
 /**
  * A Device encapsulating a StringBuilder
  *
  * @author <a href="mailto:ole@freiheit.com">Ole Langbehn</a>
  */
 public final class StringBuilderDevice implements Device, Serializable {
     private StringBuilder builder;
     private transient ByteArrayOutputStream byteStream = null;
 
     public StringBuilderDevice() {
         builder = new StringBuilder(4 * 1024);
     }
 
     public StringBuilderDevice(int capacity) {
         builder = new StringBuilder(capacity);
     }
 
     public String toString() {
         flush();
         return builder.toString();
     }
 
    public boolean isSizePreserving() { return true; }
 
     /**
      * Flush this Stream.
      */
     public void flush() {
         if (byteStream != null) {
             try {
                builder.append(byteStream.toString(IOUtil.getIOEncoding()));
             } catch (UnsupportedEncodingException e) {
                 throw new RuntimeException(e);
             }
             byteStream = null;
         }
     }
 
     public void close() {
         flush();
     }
 
     public void reset() {
         flush();
         builder.setLength(0);
     }
 
     private ByteArrayOutputStream getStream() {
         if (byteStream != null)
             return byteStream;
         byteStream = new ByteArrayOutputStream();
         return byteStream;
     }
 
     /**
      * Print a String.
      */
     public Device print(String s) {
         if (byteStream != null) flush();
         builder.append(s);
         return this;
     }
 
     /**
      * Print a character.
      */
     public Device print(char c) {
         if (byteStream != null) flush();
         builder.append(c);
         return this;
     }
 
     /**
      * Print a character array.
      */
     public Device print(char[] c) {
         if (byteStream != null) flush();
         builder.append(c);
         return this;
     }
 
     /**
      * Print a character array.
      */
     public Device print(char[] c, int start, int len) {
         if (byteStream != null) flush();
         builder.append(c, start, len);
         return this;
     }
 
     /**
      * Print an integer.
      */
     public Device print(int i) {
         if (byteStream != null) flush();
         builder.append(i);
         return this;
     }
 
     /**
      * Print any Object
      */
     public Device print(Object o) {
         if (byteStream != null) flush();
         builder.append(o);
         return this;
     }
 
     /**
      * Writes the specified byte to this data output stream.
      */
     public Device write(int c) {
         getStream().write(c);
         return this;
     }
 
     /**
      * Writes b.length bytes from the specified byte array to this
      * output stream.
      */
     public Device write(byte b[]) throws IOException {
         getStream().write(b);
         return this;
     }
 
     /**
      * Writes len bytes from the specified byte array starting at offset
      * off to this output stream.
      */
     public Device write(byte b[], int off, int len) {
         getStream().write(b, off, len);
         return this;
     }
 }
 
 
