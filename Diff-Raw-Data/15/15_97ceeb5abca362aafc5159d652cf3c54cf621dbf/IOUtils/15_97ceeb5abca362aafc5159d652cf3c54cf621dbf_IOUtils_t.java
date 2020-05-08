 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.utils;
 
 import org.slf4j.Logger;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 /**
  * I/O Utils
  */
 public final class IOUtils {
     private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
     private static final String DEFAULT_ENCODING = "utf-8";
 
     private IOUtils() {
     }
 
     /**
      * Close I/O object quietly without exception
      *
      * @param input object to close
      */
     public static void closeQuietly(Closeable input) {
         try {
             if (input != null) {
                 input.close();
             }
         } catch (IOException ioe) {
             // ignore, do nothing
         }
     }
 
     /**
      * Close I/O object quietly without exception but using external logger for logging errors if any
      *
      * @param input object to close
      * @param log   external logger to use
      */
     public static void closeQuietly(Closeable input, Logger log) {
         try {
             if (input != null) {
                 input.close();
             }
         } catch (IOException ioe) {
             log.warn("'close' invocation exception for resource: " + input.getClass().getName(), ioe);
         }
     }
 
     /**
      * Copy content from one stream to another
      *
      * @param input  source stream
      * @param output destination stream
      * @return bytes copied
      * @throws IOException if I/O error occurs
      */
     public static long copy(InputStream input, OutputStream output) throws IOException {
         byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
         long count = 0;
         int n = 0;
         while (-1 != (n = input.read(buffer))) {
             output.write(buffer, 0, n);
             count += n;
         }
         return count;
     }
 
     /**
      * Copy content from stream to writer
      *
      * @param input  source stream
      * @param output destination writer
      * @return bytes copied
      * @throws IOException if I/O error occurs
      */
     public static long copy(InputStream input, Writer output) throws IOException {
        return copy(new InputStreamReader(input,"utf-8"), output);
     }
 
     /**
      * Copy content from stream to writer using specified encoding for stream reading
      *
      * @param input  source stream
      * @param output destination writer
      * @return bytes copied
      * @throws IOException if I/O error occurs
      */
     public static long copy(InputStream input, Writer output, String encoding) throws IOException {
         if (encoding == null) {
             return copy(input, output, DEFAULT_ENCODING);
         } else {
             return copy(new InputStreamReader(input, encoding), output);
         }
     }
 
     /**
      * Copy content from reader to writer
      *
      * @param input  source reader
      * @param output destination writer
      * @return bytes copied
      * @throws IOException if I/O error occurs
      */
     public static long copy(Reader input, Writer output) throws IOException {
         char[] buffer = new char[DEFAULT_BUFFER_SIZE];
         long count = 0;
         int n = 0;
         while (-1 != (n = input.read(buffer))) {
             output.write(buffer, 0, n);
             count += n;
         }
         return count;
     }
 
     /**
      * Prints reader content to output string
      *
      * @param input reader to use
      * @return reader content as string
      * @throws IOException if I/O error occurs
      */
     public static String toString(Reader input) throws IOException {
         StringWriter sw = new StringWriter();
         copy(input, sw);
         return sw.toString();
     }
 
     /**
      * Prints stream content to output string
      *
      * @param input reader to use
      * @return reader content as string
      * @throws IOException if I/O error occurs
      */
     public static String toString(InputStream input) throws IOException {
         StringWriter sw = new StringWriter();
         copy(input, sw, DEFAULT_ENCODING);
         return sw.toString();
     }
 
     /**
      * Prints stream content to output string using specified encoding
      *
      * @param input reader to use
      * @return reader content as string
      * @throws IOException if I/O error occurs
      */
     public static String toString(InputStream input, String encoding)
             throws IOException {
         StringWriter sw = new StringWriter();
         copy(input, sw, encoding);
         return sw.toString();
     }
 }
