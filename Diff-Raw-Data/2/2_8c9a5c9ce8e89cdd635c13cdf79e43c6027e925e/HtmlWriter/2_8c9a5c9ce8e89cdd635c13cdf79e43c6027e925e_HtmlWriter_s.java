 package com.psddev.dari.util;
 
 import java.lang.reflect.Array;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayDeque;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Map;
 
 /** Writer implementation that adds basic HTML formatting. */
 public class HtmlWriter extends Writer {
 
     private final Writer writer;
     private final Map<Class<?>, HtmlFormatter<Object>> defaultFormatters = new HashMap<Class<?>, HtmlFormatter<Object>>();
     private final Map<Class<?>, HtmlFormatter<Object>> overrideFormatters = new HashMap<Class<?>, HtmlFormatter<Object>>();
     private final Deque<String> tags = new ArrayDeque<String>();
 
     /** Creates an instance that writes to the given {@code writer}. */
     public HtmlWriter(Writer writer) {
         this.writer = writer;
     }
 
     @SuppressWarnings("unchecked")
     public <T> void putDefault(Class<T> objectClass, HtmlFormatter<? super T> formatter) {
         defaultFormatters.put(objectClass, (HtmlFormatter<Object>) formatter);
     }
 
     @SuppressWarnings("unchecked")
     public <T> void putOverride(Class<T> objectClass, HtmlFormatter<? super T> formatter) {
         overrideFormatters.put(objectClass, (HtmlFormatter<Object>) formatter);
     }
 
     public void putAllStandardDefaults() {
         putDefault(null, HtmlFormatter.NULL);
         putDefault(Class.class, HtmlFormatter.CLASS);
         putDefault(Collection.class, HtmlFormatter.COLLECTION);
         putDefault(Date.class, HtmlFormatter.DATE);
         putDefault(Double.class, HtmlFormatter.FLOATING_POINT);
         putDefault(Enum.class, HtmlFormatter.ENUM);
         putDefault(Float.class, HtmlFormatter.FLOATING_POINT);
         putDefault(Map.class, HtmlFormatter.MAP);
         putDefault(Number.class, HtmlFormatter.NUMBER);
         putDefault(PaginatedResult.class, HtmlFormatter.PAGINATED_RESULT);
         putDefault(StackTraceElement.class, HtmlFormatter.STACK_TRACE_ELEMENT);
         putDefault(Throwable.class, HtmlFormatter.THROWABLE);
 
         // Optional.
         if (HtmlFormatter.JASPER_EXCEPTION_CLASS != null) {
             putDefault(HtmlFormatter.JASPER_EXCEPTION_CLASS, HtmlFormatter.JASPER_EXCEPTION);
         }
     }
 
     public void removeDefault(Class<?> objectClass) {
         defaultFormatters.remove(objectClass);
     }
 
     public void removeOverride(Class<?> objectClass) {
         overrideFormatters.remove(objectClass);
     }
 
     /**
      * Escapes the given {@code string} so that it's safe to use in
      * an HTML page.
      */
     protected String escapeHtml(String string) {
         return StringUtils.escapeHtml(string);
     }
 
     private void writeAttribute(Object name, Object value) throws IOException {
         if (!(ObjectUtils.isBlank(name) || value == null)) {
             writer.write(' ');
             writer.write(escapeHtml(name.toString()));
             writer.write("=\"");
             writer.write(escapeHtml(value.toString()));
             writer.write('"');
         }
     }
 
     /**
      * Writes the given {@code tag} with the given {@code attributes}.
      *
      * <p>This method doesn't keep state, so it should be used with doctype
      * declaration and self-closing tags like {@code img}.</p>
      */
     public HtmlWriter tag(String tag, Object... attributes) throws IOException {
         if (tag == null) {
             throw new IllegalArgumentException("Tag can't be null!");
         }
 
         writer.write('<');
         writer.write(tag);
 
         if (attributes != null) {
             for (int i = 0, length = attributes.length; i < length; ++ i) {
                 Object name = attributes[i];
 
                 if (name instanceof Map) {
                     for (Map.Entry<?, ?> entry : ((Map<?, ?>) name).entrySet()) {
                         writeAttribute(entry.getKey(), entry.getValue());
                     }
 
                 } else {
                     ++ i;
                     Object value = i < length ? attributes[i] : null;
                     writeAttribute(name, value);
                 }
             }
         }
 
         writer.write('>');
         return this;
     }
 
     /**
      * Writes the given start {@code tag} with the given {@code attributes}.
      *
      * <p>This method keeps state, so there should be a matching {@link #end}
      * call afterwards.</p>
      */
     public HtmlWriter start(String tag, Object... attributes) throws IOException {
         tag(tag, attributes);
         tags.addFirst(tag);
         return this;
     }
 
     /** Writes the end tag previously started with {@link #start}. */
     public HtmlWriter end() throws IOException {
         String tag = tags.removeFirst();
 
         if (tag == null) {
             throw new IllegalStateException("No more tags!");
         }
 
         writer.write("</");
         writer.write(tag);
         writer.write('>');
 
         return this;
     }
 
     /**
      * Escapes and writes the given {@code unescapedHtml}, or if it's
      * {@code null}, the given {@code defaultUnescapedHtml}.
      */
     public HtmlWriter htmlOrDefault(Object unescapedHtml, String defaultUnescapedHtml) throws IOException {
         writer.write(escapeHtml(unescapedHtml == null ? defaultUnescapedHtml : unescapedHtml.toString()));
         return this;
     }
 
     /**
      * Escapes and writes the given {@code unescapedHtml}, or if it's
      * {@code null}, nothing.
      */
     public HtmlWriter html(Object unescapedHtml) throws IOException {
         htmlOrDefault(unescapedHtml, "");
         return this;
     }
 
     /** Formats and writes the given {@code object}. */
     public HtmlWriter object(Object object) throws IOException {
         HtmlFormatter<Object> formatter;
 
         if (object == null) {
             formatter = overrideFormatters.get(null);
             if (formatter == null) {
                 formatter = defaultFormatters.get(null);
             }
             if (formatter != null) {
                 formatter.format(this, null);
                 return this;
             }
 
         } else {
             if (formatWithMap(overrideFormatters, object)) {
                 return this;
             }
 
             if (object instanceof HtmlObject) {
                 ((HtmlObject) object).format(this);
                 return this;
             }
 
             if (formatWithMap(defaultFormatters, object)) {
                 return this;
             }
         }
 
        if (object.getClass().isArray()) {
             start("ul");
                 for (int i = 0, length = Array.getLength(object); i < length; ++ i) {
                     start("li").object(Array.get(object, i)).end();
                 }
             end();
             return this;
         }
 
         return html(object);
     }
 
     private boolean formatWithMap(
             Map<Class<?>, HtmlFormatter<Object>> formatters,
             Object object)
             throws IOException {
 
         HtmlFormatter<Object> formatter;
 
         for (Class<?> objectClass = object.getClass();
                 objectClass != null;
                 objectClass = objectClass.getSuperclass()) {
 
             formatter = formatters.get(objectClass);
             if (formatter != null) {
                 formatter.format(this, object);
                 return true;
             }
 
             for (Class<?> interfaceClass : objectClass.getInterfaces()) {
                 formatter = formatters.get(interfaceClass);
                 if (formatter != null) {
                     formatter.format(this, object);
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     // --- Writer support ---
 
     @Override
     public Writer append(char letter) throws IOException {
         writer.write(letter);
         return this;
     }
 
     @Override
     public Writer append(CharSequence text) throws IOException {
         writer.append(text);
         return this;
     }
 
     @Override
     public Writer append(CharSequence text, int start, int end) throws IOException {
         writer.append(text, start, end);
         return this;
     }
 
     @Override
     public void close() throws IOException {
         writer.close();
     }
 
     @Override
     public void flush() throws IOException {
         writer.flush();
     }
 
     @Override
     public void write(char[] buffer) throws IOException {
         writer.write(buffer);
     }
 
     @Override
     public void write(char[] buffer, int offset, int length) throws IOException {
         writer.write(buffer, offset, length);
     }
 
     @Override
     public void write(int letter) throws IOException {
         writer.write(letter);
     }
 
     @Override
     public void write(String text) throws IOException {
         writer.write(text);
     }
 
     @Override
     public void write(String text, int offset, int length) throws IOException {
         writer.write(text, offset, length);
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link #htmlOrDefault} instead. */
     @Deprecated
     public HtmlWriter stringOrDefault(Object string, String defaultString) throws IOException {
         return htmlOrDefault(string, defaultString);
     }
 
     /** @deprecated Use {@link #html} instead. */
     @Deprecated
     public HtmlWriter string(Object string) throws IOException {
         return html(string);
     }
 }
