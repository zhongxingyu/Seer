 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.json;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 import org.joe_e.Struct;
 import org.ref_send.promise.Infinity;
 import org.ref_send.promise.NaN;
 import org.ref_send.promise.NegativeInfinity;
 import org.ref_send.promise.PositiveInfinity;
 
 /**
  * A JSON writer.
  * <p>
  * A client can only output a syntactically correct JSON value, or leave the
  * {@link ValueWriter} in a {@linkplain #isWritten detectable} error state.
  * </p>
  */
 /* package */ final class
 ValueWriter extends Struct {
     static protected final String newLine = "\r\n";
     static private   final String tab = "  ";
 
     static protected final class
     Prize<T> {
         private T value;
 
         protected
         Prize(final T value) {
             this.value = value;
         }
 
         protected T
         claim() {
             final T r = value;
             value = null;
             return r;
         }
     }
 
     static protected final class
     Milestone {
         private boolean marked;
 
         protected
         Milestone(final boolean marked) {
             this.marked = marked;
         }
 
         protected boolean
         is() { return marked; }
 
         protected void
         mark() {
             marked = true;
         }
     }
 
     private final String indent;        // indentation for this JSON value
     private final Prize<Writer> output;
     private final Milestone written;
 
     protected
     ValueWriter(final String indent, final Writer out) {
         this.indent = indent;
         output = new Prize<Writer>(out);
         written = new Milestone(null == out);
     }
 
     /**
      * @return <code>true</code> if a single JSON value was successfully
      *         written, else <code>false</code>
      */
     protected boolean
     isWritten() { return written.is(); }
 
     protected ObjectWriter
     startObject() throws IOException {
         final Writer out = output.claim();
         out.write('{');
         return new ObjectWriter(out);
     }
 
     protected final class
     ObjectWriter {
         static private final String comma = "," + newLine;
 
         private final String inset;         // indentation for each member
         private final Writer out;
         private       String prefix;        // current member separator prefix
         private       ValueWriter member;   // most recent member started
 
         protected
         ObjectWriter(final Writer out) {
             inset = indent + tab;
             this.out = out;
             prefix = newLine;
             member = new ValueWriter(inset, null);
         }
 
         protected void
         close() throws IOException {
             if (!member.isWritten()) { throw new NullPointerException(); }
 
             member = null;      // prevent future calls to this object
             out.write(newLine);
             out.write(indent);
             out.write('}');
             written.mark();     // mark the containing value successful
         }
 
         protected ValueWriter
         startMember(final String name) throws IOException {
             if (!member.isWritten()) { throw new NullPointerException(); }
 
             member = new ValueWriter(inset, out); // prevent calls until after
                                                   // new member is complete
             out.write(prefix);
             out.write(inset);
             writeStringTo(name, out);
             out.write(" : ");
             prefix = comma;
             return member;
         }
     }
 
     protected ArrayWriter
     startArray() throws IOException {
         final Writer out = output.claim();
         out.write('[');
         return new ArrayWriter(out);
     }
 
     protected final class
     ArrayWriter {
         static private final String comma = ", ";
 
         private final String inset;         // indentation for each element
         private final Writer out;
         private       String prefix;        // current element separator prefix
         private       ValueWriter element;  // most recent element started
 
         protected
         ArrayWriter(final Writer out) {
             inset = indent + tab;
             this.out = out;
             prefix = " ";
             element = new ValueWriter(inset, null);
         }
 
         protected void
         close() throws IOException {
             if (!element.isWritten()) { throw new NullPointerException(); }
 
             element = null;     // prevent future calls to this object
             out.write(" ]");
             written.mark();     // mark the containing value successful
         }
 
         protected ValueWriter
         startElement() throws IOException {
             if (!element.isWritten()) { throw new NullPointerException(); }
 
             element = new ValueWriter(inset, out); // prevent calls until after
                                                    // new element is complete
             out.write(prefix);
             prefix = comma;
             return element;
         }
     }
 
     protected void
     writeLink(final String URL) throws IOException {
         final Writer out = output.claim();
         out.write("{ \"@\" : ");
         writeStringTo(URL, out);
         out.write(" }");
         written.mark();
     }
 
     protected void
     writeNull() throws IOException {
         output.claim().write("null");
         written.mark();
     }
 
     protected void
     writeBoolean(final boolean value) throws IOException {
         output.claim().write(value ? "true" : "false");
         written.mark();
     }
 
     protected void
     writeByte(final byte value) throws IOException {
         output.claim().write(Byte.toString(value));
         written.mark();
     }
 
     protected void
     writeShort(final short value) throws IOException {
         output.claim().write(Short.toString(value));
         written.mark();
     }
 
     protected void
     writeInt(final int value) throws IOException {
         output.claim().write(Integer.toString(value));
         written.mark();
     }
 
     protected void
     writeLong(final long value) throws IOException {
         output.claim().write(Long.toString(value));
         written.mark();
     }
 
     protected void
     writeInteger(final BigInteger value) throws IOException {
         output.claim().write(value.toString());
         written.mark();
     }
 
     protected void
     writeFloat(final float value) throws Infinity, NaN, IOException {
     	if (Float.isNaN(value)) { throw new NaN(); }
     	if (Float.isInfinite(value)) {
     		if (Float.NEGATIVE_INFINITY == value) {
     			throw new NegativeInfinity();
     		} else {
     			throw new PositiveInfinity();
     		}
     	}
         output.claim().write(Float.toString(value));
         written.mark();
     }
 
     protected void
     writeDouble(final double value) throws Infinity, NaN, IOException {
     	if (Double.isNaN(value)) { throw new NaN(); }
     	if (Double.isInfinite(value)) {
     		if (Double.NEGATIVE_INFINITY == value) {
     			throw new NegativeInfinity();
     		} else {
     			throw new PositiveInfinity();
     		}
     	}
         output.claim().write(Double.toString(value));
         written.mark();
     }
 
     protected void
     writeDecimal(final BigDecimal value) throws IOException {
         output.claim().write(value.toString());
         written.mark();
     }
 
     protected void
     writeString(final String value) throws IOException {
         writeStringTo(value, output.claim());
         written.mark();
     }
 
     static private void
     writeStringTo(final String value, final Writer out) throws IOException {
         out.write('\"');
         final int len = value.length();
         for (int i = 0; i != len; ++i) {
             final char c = value.charAt(i);
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
             case ' ':
                 out.write(c);
                 break;
             default:
                 switch (Character.getType(c)) {
                 case Character.UPPERCASE_LETTER:
                 case Character.LOWERCASE_LETTER:
                 case Character.TITLECASE_LETTER:
                 case Character.MODIFIER_LETTER:
                 case Character.OTHER_LETTER:
                 case Character.NON_SPACING_MARK:
                 case Character.ENCLOSING_MARK:
                 case Character.COMBINING_SPACING_MARK:
                 case Character.DECIMAL_DIGIT_NUMBER:
                 case Character.LETTER_NUMBER:
                 case Character.OTHER_NUMBER:
                 case Character.DASH_PUNCTUATION:
                 case Character.START_PUNCTUATION:
                 case Character.END_PUNCTUATION:
                 case Character.CONNECTOR_PUNCTUATION:
                 case Character.OTHER_PUNCTUATION:
                 case Character.MATH_SYMBOL:
                 case Character.CURRENCY_SYMBOL:
                 case Character.MODIFIER_SYMBOL:
                 case Character.INITIAL_QUOTE_PUNCTUATION:
                 case Character.FINAL_QUOTE_PUNCTUATION:
                     out.write(c);
                     break;
                 default:
                     out.write("\\u");
                     final int unicode = c;
                     for (int shift = 16; 0 != shift;) {
                         shift -= 4;
                         final int hex = (unicode >> shift) & 0x0F;
                         out.write(hex < 10 ? '0' + hex : 'A' + (hex - 10));
                     }
                 }
             }
         }
         out.write('\"');
     }
 }
