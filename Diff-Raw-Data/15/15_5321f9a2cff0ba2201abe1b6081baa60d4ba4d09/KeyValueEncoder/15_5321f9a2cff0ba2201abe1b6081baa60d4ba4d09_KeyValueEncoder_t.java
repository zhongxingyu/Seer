 package com.blendlabsinc.avrokeyvalue;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Collection;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.BitSet;
 import org.apache.commons.lang3.StringUtils;
 
 import org.apache.avro.AvroTypeException;
 import org.apache.avro.Schema;
 import org.apache.avro.AvroTypeException;
 import org.apache.avro.Schema;
 import org.apache.avro.Schema.Field;
 import org.apache.avro.generic.GenericDatumWriter;
 import org.apache.avro.generic.GenericFixed;
 import org.apache.avro.io.DatumWriter;
 import org.apache.avro.io.Encoder;
 import org.apache.avro.io.ParsingEncoder;
 import org.apache.avro.io.parsing.JsonGrammarGenerator;
 import org.apache.avro.io.parsing.Parser;
 import org.apache.avro.io.parsing.Symbol;
 import org.apache.avro.util.Utf8;
 
 public class KeyValueEncoder extends ParsingEncoder implements Parser.ActionHandler {
   final Parser parser;
 
  /**
    * Has anything been written into the collections?
    */
   protected BitSet isEmpty = new BitSet();
 
   private java.util.Map<String, String> out;
 
   KeyValueEncoder(Schema sc, java.util.Map<String, String> out) throws IOException {
     configure(out);
     this.parser =
       new Parser(new JsonGrammarGenerator().generate(sc), this);
   }
 
   @Override
   public void flush() throws IOException {
     // Do nothing
   }
 
   public KeyValueEncoder configure(java.util.Map<String, String> newOut) throws IOException {
     this.out = newOut;
     return this;
   }
   
   /////////////////////////////////////////////////////////////////////////////
 
   @Override
   public void writeNull() throws IOException {
     parser.advance(Symbol.NULL);
     out.put(getKeyPathString(), "");
   }
 
   @Override
   public void writeBoolean(boolean b) throws IOException {
     parser.advance(Symbol.BOOLEAN);
     out.put(getKeyPathString(), Boolean.toString(b));
   }
 
   @Override
   public void writeInt(int n) throws IOException {
     parser.advance(Symbol.INT);
     out.put(getKeyPathString(), Integer.toString(n));
   }
 
   @Override
   public void writeLong(long n) throws IOException {
     parser.advance(Symbol.LONG);
     out.put(getKeyPathString(), Long.toString(n));
   }
 
   @Override
   public void writeFloat(float f) throws IOException {
     parser.advance(Symbol.FLOAT);
     out.put(getKeyPathString(), Float.toString(f));
   }
 
   @Override
   public void writeDouble(double d) throws IOException {
     parser.advance(Symbol.DOUBLE);
     out.put(getKeyPathString(), Double.toString(d));
   }
 
   @Override
   public void writeString(Utf8 utf8) throws IOException {
     writeString(utf8.toString());
   }
   
   @Override 
   public void writeString(String str) throws IOException {
     parser.advance(Symbol.STRING);
     trace("writeString(" + str + ")");
     if (parser.topSymbol() == Symbol.MAP_KEY_MARKER) {
       parser.advance(Symbol.MAP_KEY_MARKER);
       pushKeyPathComponent(str);
       // out.writeFieldName(str);
     } else {
       out.put(getKeyPathString(), str);
     }
   }
 
   @Override
   public void writeBytes(ByteBuffer bytes) throws IOException {
     if (bytes.hasArray()) {
       writeBytes(bytes.array(), bytes.position(), bytes.remaining());
     } else {
       byte[] b = new byte[bytes.remaining()];
       for (int i = 0; i < b.length; i++) {
         b[i] = bytes.get();
       }
       writeBytes(b);
     }
   }
 
   @Override
   public void writeBytes(byte[] bytes, int start, int len) throws IOException {
     parser.advance(Symbol.BYTES);
     writeByteArray(bytes, start, len);
   }
 
   private void writeByteArray(byte[] bytes, int start, int len)
     throws IOException {
     // out.writeString(
         new String(bytes, start, len, "UTF-8");
   }
 
   @Override
   public void writeFixed(byte[] bytes, int start, int len) throws IOException {
    throw new IOException("Fixed encoding is not implemented");
   }
 
   @Override
   public void writeEnum(int e) throws IOException {
     parser.advance(Symbol.ENUM);
     Symbol.EnumLabelsAction top = (Symbol.EnumLabelsAction) parser.popSymbol();
     trace("writeEnum(" + e + " : " + top.getLabel(e) + ")");
     if (e < 0 || e >= top.size) {
       throw new AvroTypeException(
           "Enumeration out of range: max is " +
           top.size + " but received " + e);
     }
     out.put(getKeyPathString(), top.getLabel(e));
   }
 
   @Override
   public void writeArrayStart() throws IOException {
    throw new IOException("Array encoding is not implemented");
   }
 
   @Override
   public void writeArrayEnd() throws IOException {
     if (! isEmpty.get(pos)) {
       parser.advance(Symbol.ITEM_END);
     }
     pop();
     parser.advance(Symbol.ARRAY_END);
     // out.writeEndArray();
   }
 
   @Override
   public void writeMapStart() throws IOException {
     push();
     isEmpty.set(depth());
 
     parser.advance(Symbol.MAP_START);
     // out.writeStartObject();
   }
 
   @Override
   public void writeMapEnd() throws IOException {
     if (! isEmpty.get(pos)) {
       parser.advance(Symbol.ITEM_END);
       popKeyPathComponent();
     }
     pop();
 
     parser.advance(Symbol.MAP_END);
     // out.writeEndObject();
   }
 
   @Override
   public void startItem() throws IOException {
     trace("startItem");
     if (! isEmpty.get(pos)) {
       parser.advance(Symbol.ITEM_END);
       popKeyPathComponent();
     }
     super.startItem();
     isEmpty.clear(depth());
   }
 
   @Override
   public void writeIndex(int unionIndex) throws IOException {
     parser.advance(Symbol.UNION);
     Symbol.Alternative top = (Symbol.Alternative) parser.popSymbol();
     Symbol symbol = top.getSymbol(unionIndex);
     if (symbol != Symbol.NULL) {
       pushKeyPathComponent(top.getLabel(unionIndex));
       parser.pushSymbol(Symbol.UNION_END);
     }
     parser.pushSymbol(symbol);
   }
 
   @Override
   public Symbol doAction(Symbol input, Symbol top) throws IOException {
     if (top instanceof Symbol.FieldAdjustAction) {
       Symbol.FieldAdjustAction fa = (Symbol.FieldAdjustAction) top;
       pushKeyPathComponent(fa.fname);
     } else if (top == Symbol.RECORD_START) {
       // out.writeStartObject();
     } else if (top == Symbol.RECORD_END || top == Symbol.UNION_END) {
       // out.writeEndObject();
     } else if (top == Symbol.FIELD_END) {
       popKeyPathComponent();
     } else {
       throw new AvroTypeException("Unknown action symbol " + top);
     }
     return null;
   }
 
   /////////////////////////////////////////////////////////////////////////////
   // Key path
 
   private java.util.ArrayList<String> keyPath = new java.util.ArrayList();
 
   private void pushKeyPathComponent(String component) {
     keyPath.add(component);
   }
 
   private void popKeyPathComponent() {
     keyPath.remove(keyPath.size() - 1);
   }
 
   private String getKeyPathString() {
     return StringUtils.join(keyPath, '|');
   }
 
   /////////////////////////////////////////////////////////////////////////////
 
   private void trace(String s) {
     System.out.println(s + ":\t topSymbol=" + parser.topSymbol() + " keyPath=" + getKeyPathString());
   }
 }
 
