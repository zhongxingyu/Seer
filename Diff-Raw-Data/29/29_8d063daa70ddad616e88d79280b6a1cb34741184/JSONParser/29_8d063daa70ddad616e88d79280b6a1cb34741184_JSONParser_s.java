 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.json;
 
 import static org.ref_send.promise.Fulfilled.ref;
 
 import java.io.Reader;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 
 import org.joe_e.Equatable;
 import org.joe_e.array.BooleanArray;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.CharArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.DoubleArray;
 import org.joe_e.array.FloatArray;
 import org.joe_e.array.IntArray;
 import org.joe_e.array.LongArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.array.ShortArray;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 import org.ref_send.promise.NegativeInfinity;
 import org.ref_send.promise.PositiveInfinity;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.Volatile;
 import org.ref_send.promise.eventual.Do;
 import org.ref_send.type.Typedef;
 import org.waterken.id.Importer;
 import org.waterken.uri.URI;
 
 final class
 JSONParser {
     
     static private final String whitespace = " \n\r\t";
     
     static private void
     eatWhitespace(final char c) throws Exception {
         if (whitespace.indexOf(c) == -1) {
             throw new Exception("0x" + Integer.toHexString(c));
         }
     }
     
     static private interface
     State extends Equatable {
         void
         run(char c) throws Exception;
     }
 
     private final String base;
     private final Importer connect;
     private final ClassLoader code;
     private final ArrayList<State> state;
     
     JSONParser(final String base,final Importer connect,final ClassLoader code){
         this.base = base;
         this.connect = connect;
         this.code = code;
         state = new ArrayList<State>();
     }
     
     private State
     current() { return state.get(state.size() - 1); }
     
     private State
     pop() { return state.remove(state.size() - 1); }
     
     private void
     push(final State child) { state.add(child); }
     
     private void
     to(final State next) { state.set(state.size() - 1, next); }
     
     ConstArray<?>
     parse(final Reader in,
           final PowerlessArray<Type> parameters) throws Exception {
         final State done = new State() {
             public void
             run(final char c) throws Exception { eatWhitespace(c); }
         };
         push(done);
        final ConstArray[] root = { null };
         push(parseStart(parameters, new Do<ConstArray<?>,Void>() {
             public Void
             fulfill(final ConstArray<?> referent) {
                 root[0] = referent;
                 return null;
             }
         }));
         int line = 1;
         int column = 1;
         try {
             for (int i = in.read(); -1 != i; i = in.read()) {
                 current().run((char)i);
                 if ('\n' == i) {
                     ++line;
                     column = 1;
                 } else {
                     ++column;
                 }
             }
             if (done != pop()) { throw new Exception(); }
         } catch (final Exception e) {
             try { in.close(); } catch (final Exception e2) {}
             throw new Exception("( " + line + ", " + column + " ) : ", e);
         }
         in.close();
         return root[0];
     }
     
     private State
     parseStart(final PowerlessArray<Type> parameters,
                final Do<ConstArray<?>,?> out) {
         return new State() {
             public void
             run(final char c) throws Exception {
                 if ('[' == c) {
                     to(parseTuple(parameters, out));
                 } else {
                     eatWhitespace(c);
                 }
             }
         };
     }
     
     private State
     parseTuple(final PowerlessArray<Type> parameters,
                final Do<ConstArray<?>,?> out) {
         final Object[] arg = new Object[parameters.length()];
         return new State() {
             private int i = 0;
             
             public void
             run(final char c) throws Exception {
                 if (']' == c) {
                     pop();
                     for (; i != arg.length; ++i) {
                         arg[i] = defaultValue(parameters.get(i));
                     }
                     out.fulfill(ConstArray.array(arg));
                 } else if (whitespace.indexOf(c) != -1) {
                     // ignore whitespace
                 } else {
                     push(parseContinuation(']'));
                     push(parseValue(i<arg.length?parameters.get(i):Object.class,
                                     new Do<Object,Void>() {
                         public Void
                         fulfill(final Object x) {
                             if (i < arg.length) { arg[i] = x; }
                             ++i;
                             return null;
                         }
                     }));
                     current().run(c);
                 }
             }
         };
     }
     
     static private Object
     defaultValue(final Type type) {
         final Object NULL = null;
         return boolean.class == type
             ? Boolean.FALSE
         : char.class == type
             ? Character.valueOf('\0')
         : byte.class == type
             ? Byte.valueOf((byte)0)
         : short.class == type
             ? Short.valueOf((short)0)
         : int.class == type
             ? Integer.valueOf(0)
         : long.class == type
             ? Long.valueOf(0)
         : float.class == type
             ? Float.valueOf(0.0f)
         : double.class == type
             ? Double.valueOf(0.0)
         : NULL;
     }
     
     private State
     parseValue(final Type implicit, final Do<Object,?> out) {
         return new State() {
             public void
             run(final char c) throws Exception {
                 switch (c) {
                 case '\"':
                     to(parseString(implicit, out));
                     break;
                 case '{':
                     to(parseObject(implicit, out));
                     break;
                 case '[':
                     to(parseArray(implicit, out));
                     break;
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
                     to(parseNumber(implicit, out));
                     current().run(c);
                     break;
                 case 't':
                     to(parseToken(implicit, "true", Boolean.TRUE, out));
                     break;
                 case 'f':
                     to(parseToken(implicit, "false", Boolean.FALSE, out));
                     break;
                 case 'n':
                     to(parseToken(implicit, "null", null, out));
                     break;
                 default:
                     eatWhitespace(c);
                 }
             }
         };
     }
 
     /**
      * {@link Volatile} expected type
      */
    static private final TypeVariable T = Typedef.name(Volatile.class, "T");
     
     private State
     parseObject(final Type implicit, final Do<Object,?> out) {
         return new State() {
            private Class explicit;     // type declared in JSON data
             private Type promised;      // T if implicit is Volatile, else null
             
             // deserializer information for the determined type
            private Constructor make;
             private Type[] paramv;
             private String[] namev;
             private Object[] argv;
             private boolean[] donev;
             
             /**
              * Determine the deserialization constructor.
              */
             private void
             determine() throws NoSuchMethodException {
                 promised = Typedef.value(T, implicit);
                 final Type expected = null != promised ? promised : implicit;
                final Class actual =
                     null != explicit ? explicit : Typedef.raw(expected);
                for (final Constructor c : Reflection.constructors(actual)) {
                     if (c.isAnnotationPresent(deserializer.class)) {
                         make = c;
                         break;
                     }
                 }
                 if (null == make) {
                     make = Reflection.constructor(actual);
                 }
                 paramv = make.getGenericParameterTypes();
                 int i = 0;
                 for (final Type p : paramv) {
                     paramv[i++] = Typedef.bound(p, expected);
                 }
                 namev = new String[paramv.length];
                 i = 0;
                 for (final Annotation[] as : make.getParameterAnnotations()) {
                     for (final Annotation a : as) {
                         if (a instanceof name) {
                             namev[i] = ((name)a).value();
                             break;
                         }
                     }
                     ++i;
                 }
                 argv = new Object[paramv.length];
                 donev = new boolean[paramv.length];
             }
             
             public void
             run(final char c) throws Exception {
                 if ('}' == c) {
                     pop();
                     if (null == make) { determine(); }
                     for (int i = donev.length; 0 != i--;) {
                         if (!donev[i]) {
                             argv[i] = defaultValue(paramv[i]);
                             donev[i] = true;
                         }
                     }
                     final Object r = Reflection.construct(make, argv);
                     if (r instanceof Rejected) {
                        final Rejected p = (Rejected)r;
                         if (Double.class==implicit || double.class==implicit) {
                             if (p.reason instanceof NegativeInfinity) {
                                 out.fulfill(Double.NEGATIVE_INFINITY);
                             } else if (p.reason instanceof PositiveInfinity) {
                                 out.fulfill(Double.POSITIVE_INFINITY);
                             } else {
                                 out.fulfill(Double.NaN);
                             }
                         } else if(Float.class==implicit||float.class==implicit){
                             if (p.reason instanceof NegativeInfinity) {
                                 out.fulfill(Float.NEGATIVE_INFINITY);
                             } else if (p.reason instanceof PositiveInfinity) {
                                 out.fulfill(Float.POSITIVE_INFINITY);
                             } else {
                                 out.fulfill(Float.NaN);
                             }
                         } else {
                             out.fulfill(p._(Typedef.raw(implicit)));
                         }
                     } else {
                         out.fulfill(null != promised ? ref(r) : r);
                     }
                 } else if ('\"' == c) {
                     push(parseString(String.class, new Do<Object,Void>() {
                         public Void
                         fulfill(final Object name) throws Exception {
                             if ("$".equals(name)) {
                                 if (null != make) { throw new Exception(); }
                                 push(parseContinuation('}'));
                                 push(parsePairValue(ConstArray.class, 
                                                     new Do<Object,Void>() {
                                     public Void
                                     fulfill(final Object x) throws Exception {
                                        for (final Object i : (ConstArray)x) {
                                             try {
                                                 explicit =
                                                     Java.load(code, (String)i);
                                                 break;
                                             } catch(ClassNotFoundException e) {}
                                         }
                                         determine();
                                         return null;
                                     }
                                 }));
                             } else if ("@".equals(name)) {
                                 if (null != make) { throw new Exception(); }
                                 pop();
                                 push(new State() {
                                     public void
                                     run(final char c) throws Exception {
                                         if ('}' == c) {
                                             pop();
                                         } else {
                                             eatWhitespace(c);
                                         }
                                     }
                                 });
                                 push(parsePairValue(String.class, 
                                                     new Do<Object,Void>() {
                                     public Void
                                     fulfill(final Object x) throws Exception {
                                         out.fulfill(connect.run(
                                             Typedef.raw(implicit),
                                             URI.resolve(base, (String)x)));
                                         return null;
                                     }
                                 }));
                             } else {
                                 if (null == make) { determine(); }
                                 int i = namev.length;
                                 while (0 != i-- && !name.equals(namev[i])) {}
                                 if (-1 != i) {
                                     if (donev[i]) {throw new Exception("dup");}
                                     donev[i] = true;
                                 }
                                 final int position = i;
                                 push(parseContinuation('}'));
                                 push(parsePairValue(
                                         -1 != i ? paramv[i] : Object.class,
                                         new Do<Object,Void>() {
                                     public Void
                                     fulfill(final Object x) throws Exception {
                                         if (-1 != position) {
                                             argv[position] = x;
                                         }
                                         return null;
                                     }
                                 }));
                             }
                             return null;
                         }
                     }));
                 } else {
                     eatWhitespace(c);
                 }
             }
         };
     }
     
     private State
     parseContinuation(final char bracket) {
         return new State() {
             public void
             run(final char c) throws Exception {
                 if (',' == c) {
                     pop();
                 } else if (bracket == c) {
                     pop();
                     current().run(c);
                 } else {
                     eatWhitespace(c);
                 }
             }
         };
     }
     
     private State
     parsePairValue(final Type implicit, final Do<Object,?> out) {
         return new State() {
             public void
             run(final char c) throws Exception {
                 if (':' == c) {
                     to(parseValue(implicit, out));
                 } else {
                     eatWhitespace(c);
                 }
             }
         };
     }
 
    static private final TypeVariable E = Typedef.name(ConstArray.class, "E");
     
     private State
     parseArray(final Type implicit, final Do<Object,?> out) {
         final Type promised = Typedef.value(T, implicit);
         final Type expected = null != promised ? promised : implicit;
         final Type valueType = Typedef.bound(E, expected);
         final ArrayList<Object> values = new ArrayList<Object>(); 
         return new State() {
             public void
             run(final char c) throws Exception {
                 if (']' == c) {
                     pop();
 
                     // determine the array element type
                     final Class<?> t;
                     final Class<?> vt;
                    final Class actual = Typedef.raw(expected);
                     if (ByteArray.class == actual) {
                         t = byte.class;
                         vt = byte[].class;
                     } else if (ShortArray.class == actual) {
                         t = short.class;
                         vt = short[].class;
                     } else if (IntArray.class == actual) {
                         t = int.class;
                         vt = int[].class;
                     } else if (LongArray.class == actual) {
                         t = long.class;
                         vt = long[].class;
                     } else if (FloatArray.class == actual) {
                         t = float.class;
                         vt = float[].class;
                     } else if (DoubleArray.class == actual) {
                         t = double.class;
                         vt = double[].class;
                     } else if (BooleanArray.class == actual) {
                         t = boolean.class;
                         vt = boolean[].class;
                     } else if (CharArray.class == actual) {
                         t = char.class;
                         vt = char[].class;
                     } else {
                         t = Typedef.raw(valueType);
                         vt = Object[].class;
                     }
 
                     // fill out an array
                     final Object v = Array.newInstance(t, values.size());
                     if (v instanceof Object[]) {
                         values.toArray((Object[])v);
                     } else {
                         int i = 0;
                         for (final Object x : values) {
                             Array.set(v, i++, x);
                         }
                     }
 
                     // determine the constructor
                     final Method make = Reflection.method(
                         ConstArray.class.isAssignableFrom(actual)
                             ? actual
                         : ConstArray.class, "array", vt);
                     final Object r = Reflection.invoke(make, null, v);
                     out.fulfill(null != promised ? ref(r) : r);
                 } else if (whitespace.indexOf(c) != -1) {
                     // ignore whitespace
                 } else {
                     push(parseContinuation(']'));
                     push(parseValue(valueType, new Do<Object,Void>() {
                         public Void
                         fulfill(final Object x) {
                             values.add(x);
                             return null;
                         }
                     }));
                     current().run(c);
                 }
             }
         };
     }
     
     private State
     parseNumber(final Type implicit, final Do<Object,?> out) {
         final StringBuilder buffer = new StringBuilder();
         return new State() {
             public void
             run(final char c) throws Exception {
                 if ('-' == c) {
                     if (buffer.length() != 0) { throw new Exception(); }
                     buffer.append(c);
                 } else if ('0' <= c && '9' >= c) {
                     buffer.append(c);
                 } else if ('.' == c || 'e' == c || 'E' == c) {
                     to(parseDecimal(implicit, buffer, out));
                     buffer.append(c);
                 } else {
                     pop();
                     final String text = buffer.toString();
                     final Type promised = Typedef.value(T, implicit);
                     final Type expected= null != promised ? promised : implicit;
                     final Number r;
                     if (int.class == expected || Integer.class == expected) {
                         r = Integer.parseInt(text);
                     } else if (long.class==expected || Long.class == expected) {
                         r = Long.parseLong(text);
                     } else if (byte.class==expected || Byte.class == expected) {
                         r = Byte.parseByte(text);
                     } else if (short.class==expected || Short.class==expected) {
                         r = Short.parseShort(text);
                     } else if (BigInteger.class == expected) {
                         r = new BigInteger(text);
                     } else if (double.class==expected||Double.class==expected) {
                         r = Double.valueOf(text);
                     } else if (float.class==expected || Float.class==expected) {
                         r = Float.valueOf(text);
                     } else if (BigDecimal.class == expected) {
                         r = new BigDecimal(text);
                     } else {
                         final BigInteger x = new BigInteger(text);
                         int bits = x.bitLength();
                         if (x.signum() > 0) { ++bits; }
                         if (bits <= Byte.SIZE) {
                             r = x.byteValue();
                         } else if (bits <= Short.SIZE) {
                             r = x.shortValue();
                         } else if (bits <= Integer.SIZE) {
                             r = x.intValue();
                         } else if (bits <= Long.SIZE) {
                             r = x.longValue();
                         } else {
                             r = x;
                         }
                     }
                     out.fulfill(null != promised ? ref(r) : r);
                     current().run(c);
                 }
             }
         };
     }
     
     private State
     parseDecimal(final Type implicit, final StringBuilder buffer,
                  final Do<Object,?> out) {
         return new State() {
             public void
             run(final char c) throws Exception {
                 if (('0' <= c && '9' >= c) || "eE+-".indexOf(c) != -1) {
                     buffer.append(c);
                 } else {
                     pop();
                     final String text = buffer.toString();
                     final Type promised = Typedef.value(T, implicit);
                     final Type expected= null != promised ? promised : implicit;
                     final Number r;
                     if (double.class == expected || Double.class == expected) {
                         r = Double.valueOf(text);
                     } else if (float.class==expected || Float.class==expected) {
                         r = Float.valueOf(text);
                     } else {
                         r = new BigDecimal(text);
                     }
                     out.fulfill(null != promised ? ref(r) : r);
                     current().run(c);
                 }
             }
         };
     }
     
     private State
     parseToken(final Type implicit, final String name,
                final Object value, final Do<Object,?> out) {
         return new State() {
             private int i = 1;
             
             public void
             run(final char c) throws Exception {
                 if (name.charAt(i) != c) { throw new Exception(); }
                 if (++i == name.length()) {
                     pop();
                     final Type promised = Typedef.value(T, implicit);
                     out.fulfill(null != promised ? ref(value) : value);
                 }
             }
         };
     }
     
     private State
     parseString(final Type implicit, final Do<Object,?> out) {
         final StringBuilder buffer = new StringBuilder();
         return new State() {
             public void
             run(final char c) throws Exception {
                 if ('\"' == c) {
                     pop();
                     final Type promised = Typedef.value(T, implicit);
                     final Type expected= null != promised ? promised : implicit;
                     final Object r;
                     if (char.class == expected || Character.class == expected) {
                         if (1 != buffer.length()) { throw new Exception(); }
                         r = buffer.charAt(0);
                     } else {
                         r = buffer.toString();
                     }
                     out.fulfill(null != promised ? ref(r) : r);
                 } else if ('\\' == c) {
                     push(parseEscape(buffer));
                 } else {
                     buffer.append(c);
                 }
             }
         };
     }
     
     private State
     parseEscape(final StringBuilder out) {
         return new State() {
             public void
             run(final char c) throws Exception {
                 switch (c) {
                 case '\"':
                     pop();
                     out.append('\"');
                     break;
                 case '\\':
                     pop();
                     out.append('\\');
                     break;
                 case '/':
                     pop();
                     out.append('/');
                     break;
                 case 'b':
                     pop();
                     out.append('\b');
                     break;
                 case 'f':
                     pop();
                     out.append('\f');
                     break;
                 case 'n':
                     pop();
                     out.append('\n');
                     break;
                 case 'r':
                     pop();
                     out.append('\r');
                     break;
                 case 't':
                     pop();
                     out.append('\t');
                     break;
                 case 'u':
                     to(parseUnicode(out));
                     break;
                 default:
                     throw new Exception("0x" + Integer.toHexString(c));
                 }
             }            
         };
     }
     
     private State
     parseUnicode(final StringBuilder out) {
         return new State() {
             private int u = 0;
             private int i = 4;
             
             public void
             run(final char c) throws Exception {
                 u <<= 4;
                 if ('0' <= c && '9' >= c) {
                     u |= (c - '0') & 0x0F;
                 } else if ('A' <= c && 'F' >= c) {
                     u |= (c - 'A' + 10) & 0x0F;
                 } else if ('a' <= c && 'f' >= c) {
                     u |= (c - 'a' + 10) & 0x0F;
                 } else {
                     throw new Exception("0x" + Integer.toHexString(c));
                 }
                 if (--i == 0) {
                     pop();
                     out.append((char)u);
                 }
             }
         };
     }
 }
