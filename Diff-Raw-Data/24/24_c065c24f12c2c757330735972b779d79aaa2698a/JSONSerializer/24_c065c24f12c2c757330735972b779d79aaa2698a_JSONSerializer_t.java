 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.json;
 
 import static org.ref_send.promise.Fulfilled.ref;
 
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.lang.reflect.TypeVariable;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 
 import org.joe_e.Struct;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.UTF8;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.Record;
 import org.ref_send.deserializer;
 import org.ref_send.promise.Inline;
 import org.ref_send.promise.Volatile;
 import org.ref_send.type.Typedef;
 import org.waterken.id.Exporter;
 import org.waterken.io.Content;
 import org.waterken.io.open.Open;
 import org.waterken.syntax.Serializer;
 
 /**
  * <a href="http://www.json.org/">JSON</a> serialization.
  */
 public final class
 JSONSerializer extends Struct implements Serializer, Record, Serializable {
     static private final long serialVersionUID = 1L;
     
     /**
      * Constructs an instance.
      */
     public @deserializer
     JSONSerializer() {}
 
     // org.waterken.syntax.Serializer interface
 
     static private final String newLine = "\r\n";
     
     public Content
     run(final boolean mode, final Exporter export, final ConstArray<?> object) {
         return new Content() {
             public void
             writeTo(final OutputStream out) throws Exception {
                 final Writer text = UTF8.output(Open.output(out));
                 serializeArray(mode, ConstArray.class, object, export, "",text);
                 text.write(newLine);
                 text.flush();
                 text.close();
             }
         };
     }
 
     /**
      * {@link Volatile} expected type
      */
     static private final TypeVariable R = Typedef.name(Volatile.class, "T");
     
     static private void
     serialize(final boolean mode, final Type implicit,
               final Object object, final Exporter export,
               final String indent, final Writer out) throws Exception {
         final Class actual = null != object ? object.getClass() : Void.class;
         if (Inline.class == actual) {
             final Type r = Typedef.value(R, implicit);
             serialize(mode, null != r ? r : Object.class,
                       ((Inline)object).cast(), export, indent, out);
         } else if (String.class == actual) {
             out.write("\"");
             final String text = (String)object;
             final int len = text.length();
             for (int i = 0; i != len; ++i) {
                 final char c = text.charAt(i);
                 switch (c) {
                 case '\"':
                     out.write("\\\"");
                     break;
                 case '\\':
                     out.write("\\\\");
                     break;
                 case '\b':
                     out.write("\\b");
                     break;
                 case '\f':
                     out.write("\\f");
                     break;
                 case '\n':
                     out.write("\\n");
                     break;
                 case '\r':
                     out.write("\\r");
                     break;
                 case '\t':
                     out.write("\\t");
                     break;
                 default:
                     if (Character.isISOControl(c)) {
                         out.write("\\u");
                         final int u = c;
                         for (int shift = 16; 0 != shift;) {
                             shift -= 4;
                             final int h = (u >> shift) & 0x0F;
                             out.write(h < 10 ? '0' + h : 'A' + (h - 10));
                         }
                     } else {
                         out.write(c);
                     }
                 }
             }
             out.write("\"");
         } else if (Void.class == actual) {
             out.write("null");
         } else if (Integer.class == actual) {
             out.write(((Integer)object).toString());
         } else if (Long.class == actual) {
             out.write(((Long)object).toString());
         } else if (BigInteger.class == actual) {
             out.write(((BigInteger)object).toString());
         } else if (Byte.class == actual) {
             out.write(((Byte)object).toString());
         } else if (Short.class == actual) {
             out.write(((Short)object).toString());
         } else if (Boolean.class == actual) {
             if (Boolean.TRUE.equals(object)) {
                 out.write("true");
             } else {
                 out.write("false");
             }
         } else if (Character.class == actual) {
             serialize(mode, implicit, ((Character)object).toString(),
                       export, indent, out);
         } else if (Double.class == actual) {
             final Volatile<Double> pd = ref((Double)object);
             if (pd instanceof Inline) {
                 out.write(((Inline<Double>)pd).cast().toString());
             } else {
                 serialize(mode, implicit, pd, export, indent, out);
             }
         } else if (Float.class == actual) {
             final Volatile<Float> pf = ref((Float)object);
             if (pf instanceof Inline) {
                 out.write(((Inline<Float>)pf).cast().toString());
             } else {
                 serialize(mode, implicit, pf, export, indent, out);
             }
         } else if (BigDecimal.class == actual) {
             out.write(((BigDecimal)object).toPlainString());
         } else if (object instanceof ConstArray) {
             serializeArray(render, implicit, (ConstArray)object,
                            export, indent, out);
         } else if (object instanceof Record || object instanceof Throwable) {
             out.write("{");
             String separator = newLine;
             final String comma = "," + newLine;
             final String inset = indent + "  ";
             final Class top = Typedef.raw(implicit);
             if (actual != top) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"$\" : ");
                 serialize(render, PowerlessArray.class, upto(actual, top),
                           export, inset, out);
                 separator = comma;
             }
             for (final Field f : Reflection.fields(actual)) {
                 final int flags = f.getModifiers();
                 if (!Modifier.isStatic(flags) && Modifier.isFinal(flags) &&
                     Modifier.isPublic(f.getDeclaringClass().getModifiers())) {
                     final Object value = Reflection.get(f, object);
                     if (null != value) {
                         out.write(separator);
                         out.write(inset);
                         out.write("\"");
                         out.write(f.getName());
                         out.write("\" : ");
                         serialize(render,
                                   Typedef.bound(f.getGenericType(), implicit),
                                   value, export, inset, out);
                         separator = comma;
                     }
                 }
             }
             out.write(newLine);
             out.write(indent);
             out.write("}");
         } else if (render == mode || object instanceof Volatile) {
             out.write("{ \"@\" : ");
             serialize(render, implicit, export.run(object), export, indent,out);
             out.write(" }");
         // rest is introspection support not used in normal messaging
         } else if (Field.class == actual) {
             final Field f = (Field)object;
             final String inset = indent + "  ";
             final String comma = "," + newLine;
             String separator = newLine;
             
             out.write("{");
             
             if (Field.class != implicit) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"$\" : [ \"function\" ]");
                 separator = comma;
             }
 
             // output the field type
             out.write(separator);
             out.write(inset);
             out.write("\"out\" : ");
             serialize(render, Class.class, jsonType(f.getGenericType()),
                       export, indent, out);
 
             out.write(newLine);
             out.write(indent);
             out.write("}");
         } else if (Method.class == actual) {
             final Method m = (Method)object;
             final String inset = indent + "  ";
             final String comma = "," + newLine;
             String separator = newLine;
 
             out.write("{");
             
             if (Method.class != implicit) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"$\" : [ \"function\" ]");
                 separator = comma;
             }
 
             // output the return type
             final Class outClass = jsonType(m.getGenericReturnType());
             if (void.class != outClass && Void.class != outClass) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"out\" : ");
                 serialize(render, Class.class, outClass, export, indent, out);
                 separator = comma;
             }
 
             // output the parameter types
             if (null == Java.property(m)) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"in\" : [ ");
                 String sp = "";
                 for (final Type p : m.getGenericParameterTypes()) {
                     out.write(sp);
                     serialize(render,Class.class,jsonType(p),export,indent,out);
                     sp = ", ";
                 }
                 out.write(" ]");
                 separator = comma;
             }
 
             // output the error types
             out.write(separator);
             out.write(inset);
             out.write("\"error\" : [ ");
             String sp = "";
             for (final Type e : m.getGenericExceptionTypes()) {
                 out.write(sp);
                 serialize(render, Class.class, jsonType(e), export, indent,out);
                 sp = ", ";
             }
             out.write(" ]");
             separator = comma;
 
             out.write(newLine);
             out.write(indent);
             out.write("}");
         } else if (Class.class == actual) {
             final Class c = (Class)object;
             String separator = newLine;
             final String comma = "," + newLine;
             final String inset = indent + "  ";
 
             out.write("{");
             
             if (Class.class != implicit) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"$\" : [ \"class\" ]");
                 separator = comma;
             }
 
             for (final Field f : Reflection.fields(c)) {
                 if (!Modifier.isStatic(f.getModifiers())) {
                     out.write(separator);
                     out.write(inset);
                     out.write("\"");
                     out.write(f.getName());
                     out.write("\" : ");
                     serialize(describe, Field.class, f, export, inset, out);
                     separator = comma;
                 }
             }
             if (!Java.isPBC(c)) {
                 for (Method m : Reflection.methods(c)) {
                     final int flags = m.getModifiers();
                     if (!Modifier.isStatic(flags) && !Java.isSynthetic(flags)) {
                         final Method spec = Java.bubble(m);
                         if (null != spec) {
                             m = spec;
                         }
                         out.write(separator);
                         out.write(inset);
                         out.write("\"");
                         String name = Java.property(m);
                         if (null == name) { name = m.getName(); }
                         out.write(name);
                         out.write("\" : ");
                         serialize(describe, Method.class, m, export, inset,out);
                         separator = comma;
                     }
                 }
             }
             out.write(newLine);
             out.write(indent);
             out.write("}");
         } else {
             out.write("{");
             String separator = newLine;
             final String comma = "," + newLine;
             final String inset = indent + "  ";
             final Class top = Typedef.raw(implicit);
             if (actual != top) {
                 out.write(separator);
                 out.write(inset);
                 out.write("\"$\" : ");
                 final Class<?> end = Struct.class.isAssignableFrom(actual)
                     ? Struct.class
                 : Object.class;
                 final ArrayList<String> r = new ArrayList<String>(4);
                 for (Class<?> i=actual; end!=i; i=i.getSuperclass()) {all(i,r);}
                 serialize(render, PowerlessArray.class,
                           PowerlessArray.array(r.toArray(new String[r.size()])),
                           export, inset, out);
                 separator = comma;
             }
             out.write(newLine);
             out.write(indent);
             out.write("}");
         }
     }
     
     static private Class
     jsonType(final Type p) {
         final Type pR = Typedef.value(R, p);
         final Class pC = Typedef.raw(null != pR ? pR : p);
         return Boolean.class == pC
             ? boolean.class
         : (byte.class == pC || Byte.class == pC ||
            short.class == pC || Short.class == pC ||
            int.class == pC || Integer.class == pC ||
            long.class == pC || Long.class == pC ||
            float.class == pC || Float.class == pC ||
            double.class == pC || Double.class == pC ||
            java.math.BigInteger.class == pC ||
            java.math.BigDecimal.class == pC
             ? Number.class
         : (char.class == pC || Character.class == pC
             ? String.class
         : (Class.class == pC
             ? Type.class
         : (Field.class == pC || Member.class == pC || Constructor.class == pC
              ? Method.class
         : (Exception.class == pC
             ? RuntimeException.class
         : pC)))));
     }
     
     static private final TypeVariable T = Typedef.name(Iterable.class, "T");
     
     static private void
     serializeArray(final boolean describe, final Type implicit,
                    final ConstArray<?> object, final Exporter export,
                    final String indent, final Writer out) throws Exception {
         out.write("[");
         String separator = " ";
         final String comma = ", ";
         final String inset = indent + "  ";
         final Type valueType = Typedef.bound(T, implicit);
         for (final Object value : (ConstArray)object) {
             out.write(separator);
             serialize(describe, valueType, value, export, inset, out);
             separator = comma;
         }
         out.write(" ]");
     }
     
     static private PowerlessArray<String>
     upto(final Class<?> bottom, final Class<?> top) {
        final Class<?> limit = Struct.class.isAssignableFrom(bottom)
            ? Struct.class
        : RuntimeException.class.isAssignableFrom(bottom)
            ? Exception.class
        : Exception.class.isAssignableFrom(bottom)
            ? Throwable.class
        : Object.class;
         final ArrayList<String> r = new ArrayList<String>(4);
        for (Class<?> i = bottom; top != i && limit != i; i=i.getSuperclass()) {
             if (Modifier.isPublic(i.getModifiers())) { r.add(Java.name(i)); }
         }
         return PowerlessArray.array(r.toArray(new String[r.size()]));
     }
     
     
     static private void
     all(final Class<?> type, final ArrayList<String> r) {
         if (type == Serializable.class) { return; }
         if (Modifier.isPublic(type.getModifiers())) { r.add(Java.name(type)); }
         for (final Class<?> i : type.getInterfaces()) { all(i, r); }
     }
 }
