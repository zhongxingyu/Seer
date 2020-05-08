 // Copyright 2007-2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.json;
 
 import static org.ref_send.promise.Fulfilled.ref;
 
 import java.io.Reader;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.joe_e.array.BooleanArray;
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.CharArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.DoubleArray;
 import org.joe_e.array.FloatArray;
 import org.joe_e.array.ImmutableArray;
 import org.joe_e.array.IntArray;
 import org.joe_e.array.LongArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.array.ShortArray;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 import org.ref_send.promise.Rejected;
 import org.ref_send.promise.Volatile;
 import org.ref_send.scope.Layout;
 import org.ref_send.scope.Scope;
 import org.ref_send.type.Typedef;
 import org.waterken.syntax.Importer;
 
 /**
  * Deserializes a JSON text stream to an array of Java objects.
  */
 public final class
 JSONParser {
     
     private final String base;
     private final Importer connect;
     private final ClassLoader code;
     private final JSONLexer lexer;
     
     private
     JSONParser(final String base, final Importer connect,
                final ClassLoader code, final Reader in) {
         this.base = base;
         this.connect = connect;
         this.code = code;
         lexer = new JSONLexer(in);
     }
 
     /**
      * Parses an argument list.
      * @param base          base URL
      * @param connect       reference importer
     * @param parameters    each expected type
      * @param code          class loader
      * @param in            UTF-8 JSON text stream
      * @return parsed argument list
      * @throws Exception    any exception
      */
     static public ConstArray<?>
     parse(final String base, final Importer connect,
            final ConstArray<Type> parameters, final ClassLoader code,
            final Reader in) throws Exception {
         final JSONParser parser = new JSONParser(base, connect, code, in);
         try {
             if (!"[".equals(parser.lexer.next())) { throw new Exception(); }
             final ConstArray<?> r = parser.parseArguments(parameters);
             if (null != parser.lexer.getHead()) { throw new Exception(); }
             parser.lexer.close();
             return r;
         } catch (final Exception e) {
             try { parser.lexer.close(); } catch (final Exception e2) {}
             throw new Exception("<" + parser.base + "> ( " +
                                 parser.lexer.getStartLine() + ", " +
                                 parser.lexer.getStartColumn() + " ) : ", e);           
         }
     }
     
     private ConstArray<?>
     parseArguments(final ConstArray<Type> parameters) throws Exception {
         if (!"[".equals(lexer.getHead())) { throw new Exception(); }
         final ConstArray.Builder<Object> r =
             ConstArray.builder(parameters.length());
         if (!"]".equals(lexer.next())) {
             while (true) {
                 r.append(parseValue(r.length() < parameters.length()
                                ? parameters.get(r.length()) : Object.class));
                 if ("]".equals(lexer.getHead())) { break; }
                 if (!",".equals(lexer.getHead())) { throw new Exception(); }
                 lexer.next();
             }
         }
         lexer.next();   // skip past the closing bracket 
         return r.snapshot();
     }
 
     static private final TypeVariable<?> R = Typedef.name(Volatile.class, "T");
     static private final TypeVariable<?> T = Typedef.name(Iterable.class, "T");
     
     private Object
     parseValue(final Type required) throws Exception {
         final String token = lexer.getHead();
         return "[".equals(token) ? parseArray(required)
         : "{".equals(token) ? parseObject(required)
         : token.startsWith("\"") ? parseString(required)
         : parseKeyword(required);
     }
     
     private Object
     parseKeyword(final Type required) throws Exception {
         final String token = lexer.getHead();
         final Type promised = Typedef.value(R, required);
         final Type expected = null != promised ? promised : required;
         final Object value;
         if ("null".equals(token)) {
             value = null;
         } else if ("false".equals(token)) {
             value = Boolean.FALSE;
         } else if ("true".equals(token)) {
             value = Boolean.TRUE;
         } else if (int.class == expected || Integer.class == expected) {
             value = Integer.parseInt(token);
         } else if (long.class == expected || Long.class == expected) {
             value = Long.parseLong(token);
         } else if (byte.class == expected || Byte.class == expected) {
             value = Byte.parseByte(token);
         } else if (short.class == expected || Short.class == expected) {
             value = Short.parseShort(token);
         } else if (BigInteger.class == expected) {
             value = new BigInteger(token);
         } else if (double.class == expected || Double.class == expected) {
             value = Double.valueOf(token);  // accepts a superset of JSON
         } else if (float.class == expected || Float.class == expected) {
             value = Float.valueOf(token);   // accepts a superset of JSON
         } else if (BigDecimal.class == expected) {
             value = new BigDecimal(token);
         } else if (token.indexOf('.') != -1 ||
                    token.indexOf('e') != -1 || token.indexOf('E') != -1) {
             value = Double.valueOf(token);  // accepts a superset of JSON
         } else {
             final BigInteger x = new BigInteger(token);
             int bits = x.bitLength();
             if (x.signum() > 0) { ++bits; }
             if (bits <= Integer.SIZE) {
                 value = x.intValue();
             } else if (bits <= Long.SIZE) {
                 value = x.longValue();
             } else {
                 value = x;
             }
         }
         lexer.next();   // pop the keyword from the stream
         return null != promised ? ref(value) : value;
     }
     
     private Object
     parseString(final Type required) throws Exception {
         final String text = string(lexer.getHead());
         final Type promised = Typedef.value(R, required);
         final Type expected = null != promised ? promised : required;
         final Object value;
         if (char.class == expected || Character.class == expected) {
             if (1 != text.length()) { throw new Exception(); }
             value = text.charAt(0);
         } else {
             value = text;
         }
         lexer.next();   // pop the string from the stream
         return null != promised ? ref(value) : value;
     }
     
     private @SuppressWarnings("unchecked") Object
     parseArray(final Type required) throws Exception {
         if (!"[".equals(lexer.getHead())) { throw new Exception(); }
         final Type promised = Typedef.value(R, required);
         final Type expected = null != promised ? promised : required;
         final Class<?> rawExpected = Typedef.raw(expected);
         final ConstArray.Builder builder = 
             BooleanArray.class == rawExpected ? BooleanArray.builder()
             : CharArray.class == rawExpected ? CharArray.builder()
             : ByteArray.class == rawExpected ? ByteArray.builder()
             : ShortArray.class == rawExpected ? ShortArray.builder()
             : IntArray.class == rawExpected ? IntArray.builder()
             : LongArray.class == rawExpected ? LongArray.builder()
             : FloatArray.class == rawExpected ? FloatArray.builder()
             : DoubleArray.class == rawExpected ? DoubleArray.builder()
             : PowerlessArray.class.isAssignableFrom(rawExpected)
                 ? PowerlessArray.builder()
             : ImmutableArray.class.isAssignableFrom(rawExpected)
                 ? ImmutableArray.builder()
             : ConstArray.builder();
         if (!"]".equals(lexer.next())) {
             final Type elementT = Typedef.value(T, expected);
             while (true) {
                 builder.append(parseValue(elementT));
                 if ("]".equals(lexer.getHead())) { break; }
                 if (!",".equals(lexer.getHead())) { throw new Exception(); }
                 lexer.next();
             }
         }
         lexer.next();   // skip past the closing bracket
         final ConstArray<?> value = builder.snapshot();
         return null != promised ? ref(value) : value;
     }
     
     private Object
     parseObject(final Type required) throws Exception {
         if (!"{".equals(lexer.getHead())) { throw new Exception(); }
         if ("\"@\"".equals(lexer.next())) {
             // connect to linked reference
             if (!":".equals(lexer.next())) { throw new Exception(); }
             final String href = string(lexer.next());
             if (!"}".equals(lexer.next())) { throw new Exception(); }
             final Object value = connect.run(href, base, required);
             lexer.next();    // skip past the closing curly
             return value;
         }
         final Type promised = Typedef.value(R, required);
         final Type expected = null != promised ? promised : required;
         Class<?> actual = Typedef.raw(expected);
         PowerlessArray<?> declaration = null;
         if ("\"$\"".equals(lexer.getHead())) {
             // try to find a corresponding local class
             if (!":".equals(lexer.next())) { throw new Exception(); }
             if (!"[".equals(lexer.next())) { throw new Exception(); }
             declaration = (PowerlessArray<?>)parseArray(PowerlessArray.class);
             for (final Object name : declaration) {
                 try {
                     actual = Java.load(code, (String)name);
                     break;
                 } catch (final ClassNotFoundException e) {}
             }
             if (",".equals(lexer.getHead())) {
                 if ("}".equals(lexer.next())) { throw new Exception(); }
             } else {
                 if (!"}".equals(lexer.getHead())) { throw new Exception(); }
             }
         }
         if (Object.class == actual || Scope.class == actual) {
             // just hold onto the members in a generic JSON container
             final PowerlessArray.Builder<String> names=PowerlessArray.builder();
             final ConstArray.Builder<Object> values = ConstArray.builder();
             if (null != declaration) {
                 names.append("$");
                 values.append(declaration);
             }
             if (!"}".equals(lexer.getHead())) {
                 while (true) {
                     names.append(string(lexer.getHead()));
                     if (!":".equals(lexer.next())) { throw new Exception(); }
                     lexer.next();
                     values.append(parseValue(Object.class));
                     if ("}".equals(lexer.getHead())) { break; }
                     if (!",".equals(lexer.getHead())) { throw new Exception(); }
                     lexer.next();
                 }
             }
             final Scope x = new Scope(new Layout(names.snapshot()),
                                       values.snapshot());  
             lexer.next();    // skip past the closing curly
             return null != promised ? ref(x) : x;
         }
         
         // use reflection to construct an object of the specified type
         Constructor<?> make = null;
         for (final Constructor<?> c : Reflection.constructors(actual)) {
             if (c.isAnnotationPresent(deserializer.class)) {
                 make = c;
                 break;
             }
         }
         if (null == make && Throwable.class.isAssignableFrom(actual)) {
             make = Reflection.constructor(actual);
         }
         final Type[] paramv = make.getGenericParameterTypes(); {
             int i = 0;
             for (final Type p : paramv) {
                 paramv[i++] = Typedef.bound(p, expected);
             }
         }
         final String[] namev = new String[paramv.length]; {
             int i = 0;
             for (final Annotation[] as : make.getParameterAnnotations()) {
                 for (final Annotation a : as) {
                     if (a instanceof name) {
                         namev[i] = ((name)a).value();
                         break;
                     }
                 }
                 ++i;
             }
         }
         final Object[] argv = new Object[paramv.length];
         final boolean[] donev = new boolean[paramv.length];
         if (!"}".equals(lexer.getHead())) {
             while (true) {
                 final String name = string(lexer.getHead());
                 int slot = namev.length;
                 while (0 != slot-- && !name.equals(namev[slot])) {}
                 if (-1 != slot) {
                     if (donev[slot]) { throw new Exception(); }
                     donev[slot] = true;
                 }
                 if (!":".equals(lexer.next())) { throw new Exception(); }
                 lexer.next();
                 final Type type = -1 != slot ? paramv[slot] : Object.class;
                 final Object value = parseValue(type);
                 if (-1 != slot) {
                     argv[slot] = value;
                 }
                 if ("}".equals(lexer.getHead())) { break; }
                 if (!",".equals(lexer.getHead())) { throw new Exception(); }
                 lexer.next();
             }
         }
         for (int i = donev.length; 0 != i--;) {
             if (!donev[i]) {
                 argv[i] = defaultValue(paramv[i]);
                 donev[i] = true;
             }
         }
         Object value = Reflection.construct(make, argv);
         if (value instanceof Rejected) {
             value = ((Rejected<?>)value)._(Typedef.raw(required));
         } else if (null != promised) {
             value = ref(value);
         }
         lexer.next();    // skip past the closing curly
         return value;
     }
     
     static private String
     string(final String token) throws Exception {
         if (!token.startsWith("\"")) { throw new Exception(); }
         return token.substring(1, token.length() - 1);
     }
     
     /**
      * Gets the default value of a specified type.
      * @param required  expected type
      * @return default value
      */
     static private Object
     defaultValue(final Type required) {
         final Type promised = Typedef.value(R, required);
         final Type expected = null != promised ? promised : required;
         final Object value =
             boolean.class == expected ? Boolean.FALSE
             : char.class == expected ? Character.valueOf('\0')
             : byte.class == expected ? Byte.valueOf((byte)0)
             : short.class == expected ? Short.valueOf((short)0)
             : int.class == expected ? Integer.valueOf(0)
             : long.class == expected ? Long.valueOf(0)
             : float.class == expected ? Float.valueOf(0.0f)
             : double.class == expected ? Double.valueOf(0.0)
             : (Object)null;
         return null != promised ? ref(value) : value;
     }
 }
