 /*
  * Copyright 2010 JBoss, a divison Red Hat, Inc.                              
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.jboss.errai.bus.server.io;
 
 import org.mvel2.CompileException;
 import org.mvel2.util.StringAppender;
 
 import java.io.*;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import static org.jboss.errai.bus.server.io.TypeDemarshallHelper._demarshallAll;
 import static org.jboss.errai.common.client.protocols.SerializationParts.ENCODED_TYPE;
 
 /**
  * High-performance stream JSON parser. Provides the decoding algorithm to interpret the Errai Wire Protcol,
  * including serializable types.  This parser always assumes the outer payload is a Map. So it probably shouldn't
  * be used as a general parser.
  *
  * @author Mike Brock
  * @since 1.1
  */
 public class JSONStreamDecoder {
     private CharBuffer buffer;
     private BufferedReader reader;
 
     private char carry;
 
     private int read;
     private boolean initial = true;
 
     public JSONStreamDecoder(InputStream inStream) {
         this.buffer = CharBuffer.allocate(25);
         try {
             this.reader = new BufferedReader(
                     new InputStreamReader(inStream, "UTF-8")
             );
         } catch (UnsupportedEncodingException e) {
             throw new Error("UTF-8 is not supported by this JVM?", e);
         }
     }
 
     public static Object decode(InputStream instream) throws IOException {
         return new JSONStreamDecoder(instream).parse();
     }
 
     public char read() throws IOException {
         if (carry != 0) {
             char c = carry;
             carry = 0;
             return c;
         }
         if (read <= 0) {
             if (!initial) buffer.rewind();
             initial = false;
             if ((read = reader.read(buffer)) <= 0) {
                 return 0;
             }
             buffer.rewind();
         }
         read--;
         return buffer.get();
     }
 
     public Object parse() {
         try {
             return _parse(new Context(), null, false);
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private Object _parse(Context ctx, Object collection, boolean map) throws IOException {
         char c;
         StringAppender appender;
         while ((c = read()) != 0) {
             switch (c) {
                 case '[':
                     ctx.addValue(_parse(new Context(), new ArrayList(), false));
                     break;
 
                 case '{':
                     ctx.addValue(_parse(new Context(), new HashMap(), true));
                     break;
 
                 case ']':
                 case '}':
                     if (map && ctx.encodedType) {
                         ctx.encodedType = false;
                         try {
                             return _demarshallAll(ctx.record(collection));
                         }
                         catch (Exception e) {
                             throw new RuntimeException("Could not demarshall object", e);
                         }
                     } else {
                         return ctx.record(collection);
                     }
 
                 case ',':
                     ctx.record(collection);
                     break;
 
                 case '"':
                 case '\'':
                     char term = c;
                     appender = new StringAppender();
                     StrCapture:
                     while ((c = read()) != 0) {
                         switch (c) {
                             case '\\':
                                 appender.append(handleEscapeSequence());
                                 break;
                             case '"':
                             case '\'':
                                 if (c == term) {
                                     ctx.addValue(appender.toString());
                                     term = 0;
                                     break StrCapture;
                                 }
                                 break;
                             default:
                                 appender.append(c);
                         }
                     }
 
                     if (term != 0) {
                         throw new RuntimeException("unterminated string literal");
                     }
 
                     break;
 
                 case ':':
                     continue;
 
                 default:
                     if (isValidNumberPart(c)) {
                         ctx.addValue(parseNumber(c));
                         break;
                     } else if (Character.isJavaIdentifierPart(c)) {
                         appender = new StringAppender().append(c);
 
                         while (((c = read()) != 0) && Character.isJavaIdentifierPart(c)) {
                             appender.append(c);
                         }
 
                         String s = appender.toString();
                        if ("true".equals(s) || "false".equals(s)) {
                            ctx.addValue("true".equals(s) ? Boolean.TRUE : Boolean.FALSE);
                        } else if ("null".equals(s)) {
                             ctx.addValue(null);
                        } else {
                             ctx.addValue(s);
                         }

                        if (c != 0) {
                            //       carry1 = c;
                        }
                     }
             }
         }
 
         return ctx.record(collection);
     }
 
     public char handleEscapeSequence() throws IOException {
         char c;
         switch (c = read()) {
             case '\\':
                 return '\\';
             case 'b':
                 return '\b';
             case 'f':
                 return '\f';
             case 't':
                 return '\t';
             case 'r':
                 return '\r';
             case 'n':
                 return '\n';
             case '\'':
                 return '\'';
             case '"':
                 return '\"';
             default:
                 throw new CompileException("illegal escape sequence: " + c);
         }
     }
 
     public Number parseNumber(char c) throws IOException {
         long val = 0;
         double dVal = 0;
 
         int factor = 1;
         boolean dbl = false;
 
         char[] buf = new char[21];
         int len = 0;
         do {
             buf[len++] = c;
         } while ((c = read()) != 0 && isValidNumberPart(c));
 
         if (c != 0) {
             carry = c;
         }
 
         if (len == 1 && buf[0] == '-') return null;
 
         for (int i = len - 1; i != -1; i--) {
             switch (buf[i]) {
                 case '.':
                     dVal = ((double) val) / factor;
                     val = 0;
                     factor = 1;
                     dbl = true;
                     continue;
                 case '-':
                     if (i != 0) {
                         throw new NumberFormatException(new String(buf));
                     }
                     val = -val;
                     break;
                 case '1':
                     val += factor;
                     break;
                 case '2':
                     val += 2 * factor;
                     break;
                 case '3':
                     val += 3 * factor;
                     break;
                 case '4':
                     val += 4 * factor;
                     break;
                 case '5':
                     val += 5 * factor;
                     break;
                 case '6':
                     val += 6 * factor;
                     break;
                 case '7':
                     val += 7 * factor;
                     break;
                 case '8':
                     val += 8 * factor;
                     break;
                 case '9':
                     val += 9 * factor;
                     break;
             }
 
             factor *= 10;
         }
         if (dbl) {
             return dVal + val;
         } else {
             return val;
         }
     }
 
     private static boolean isValidNumberPart(char c) {
         switch (c) {
             case '.':
             case '-':
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
                 return true;
             default:
                 return false;
         }
     }
 
     private static class Context {
         Object lhs;
         Object rhs;
         boolean encodedType = false;
 
         private Context() {
         }
 
         private void addValue(Object val) {
             if (lhs == null) {
                 lhs = val;
             } else {
                 rhs = val;
             }
         }
 
         private Object record(Object collection) {
             try {
                 if (lhs != null) {
                     if (collection instanceof Map) {
                         if (!encodedType) encodedType = ENCODED_TYPE.equals(lhs);
                         //noinspection unchecked
                         ((Map) collection).put(lhs, rhs);
 
                     } else {
                         if (collection == null) return lhs;
                         //noinspection unchecked
                         ((Collection) collection).add(lhs);
                     }
                 }
                 return collection;
             }
             catch (ClassCastException e) {
                 throw new RuntimeException("error building collection", e);
             }
             finally {
                 lhs = rhs = null;
             }
         }
     }
 }
