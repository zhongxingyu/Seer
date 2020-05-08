 package org.uli.tcpmon;
 
import java.nio.charset.Charset;
 import java.util.LinkedList;
 import java.util.List;
 
 public class HexDump {
 
     static private final int DEFAULT_BYTES_PER_LINE = 16;
     static private final boolean DEFAULT_DUMP_HEX   = true;
     static private final boolean DEFAULT_DUMP_TEXT  = true;
     
     static private final String LINE_SEPARATOR = System.getProperty("line.separator");
     private final int bytesPerLine;
     private final boolean fDumpHex;
     private final boolean fDumpText;
 
     public static abstract class Builder<T extends Builder<T>> {
         private int bytesPerLine = DEFAULT_BYTES_PER_LINE;
         private boolean fDumpHex = DEFAULT_DUMP_HEX;
         private boolean fDumpText = DEFAULT_DUMP_TEXT;
 
         protected abstract T self();
 
         public T bytesPerLine(int bytesPerLine) {
             this.bytesPerLine = bytesPerLine;
             return self();
         }
 
         public T dumpHex(boolean fDumpHex) {
             this.fDumpHex = fDumpHex;
             return self();
         }
 
         public T dumpText(boolean fDumpText) {
             this.fDumpText = fDumpText;
             return self();
         }
 
         public HexDump build() {
             return new HexDump(this);
         }
     }
 
     private static class Builder2 extends Builder<Builder2> {
         @Override
         protected Builder2 self() {
             return this;
         }
     }
 
     public static Builder<?> builder() {
         return new Builder2();
     }
 
     protected HexDump(Builder<?> builder) {
         this.bytesPerLine = builder.bytesPerLine;
         this.fDumpHex = builder.fDumpHex;
         this.fDumpText = builder.fDumpText;
     }
 
     static public String hexDump(byte[] array) {
         return hexDump(array, 0, array.length);
     }
 
     static public String hexDump(byte[] array, int offset, int length) {
         HexDump hd = HexDump.builder().build();
         return hd.dump(array, offset, length);
     }
 
     public String dump(byte[] array) {
         return this.dump(array, 0, array.length);
     }
 
     public String dump(byte[] array, int offset, int length) {
         StringBuffer sb = new StringBuffer(length * 3 + 100);
         int remainingLength = length;
         int thisLineOffset = offset;
         while (remainingLength > 0) {
             dumpLineResult dlr = this.dumpLine(array, thisLineOffset, remainingLength);
             sb.append(dlr.line);
             sb.append(LINE_SEPARATOR);
             remainingLength = dlr.remainingLength;
             thisLineOffset = dlr.nextOffset;
         }
         return sb.toString();
     }
 
     public List<String> dumpToList(byte[] array) {
         return this.dumpToList(array, 0, array.length);
     }
 
     public List<String> dumpToList(byte[] array, int offset, int length) {
         List<String> l = new LinkedList<String>();
         int remainingLength = length;
         int thisLineOffset = offset;
         while (remainingLength > 0) {
             dumpLineResult dlr = this.dumpLine(array, thisLineOffset, remainingLength);
             l.add(dlr.line);
             remainingLength = dlr.remainingLength;
             thisLineOffset = dlr.nextOffset;
         }
         return l;
     }
 
     private StringBuffer append(StringBuffer sb, String a, int i) {
         if (i % 4 == 0) {
             sb.append(" ");
         }
         sb.append(a);
         return sb;
     }
 
     private String address(int offset) {
         return String.format("%08x", offset);
     }
 
     private String text(byte n) {
         String result;
         if (isPrintable(n)) {
            result = new String(new byte[] { n }, Charset.forName("US-ASCII"));
         } else {
             result = ".";
         }
         return result;
     }
 
     private boolean isPrintable(byte n) {
         return n >= 32 && n < 127;
     }
 
     public static void main(String[] args) {
        System.out.println(HexDump.hexDump(args[0].getBytes(Charset.defaultCharset())));
     }
 
     private dumpLineResult dumpLine(byte[] array, int offset, int length) {
         dumpLineResult dlr = new dumpLineResult("", offset, length);
         StringBuffer sb = new StringBuffer();
         if (length > 0) {
             StringBuffer hexBuffer = new StringBuffer(3 * this.bytesPerLine);
             StringBuffer textBuffer = new StringBuffer(this.bytesPerLine);
             sb.append(address(offset));
             sb.append(":");
             int i = 0;
             for (; i < length && i < this.bytesPerLine; i++) {
                 byte b = array[offset + i];
                 if (this.fDumpHex) {
                     append(hexBuffer, String.format("%02x", b), i);
                 }
                 if (this.fDumpText) {
                     textBuffer.append(text(b));
                 }
             }
             if (this.fDumpHex && this.fDumpText) {
                 for (; i < this.bytesPerLine; i++) {
                     append(hexBuffer, "  ", i);
                 }
             }
             if (this.fDumpHex) {
                 sb.append(hexBuffer);
             }
             if (this.fDumpText) {
                 sb.append("  '");
                 sb.append(textBuffer);
                 sb.append("'");
             }
             dlr.line       = sb.toString();
             dlr.nextOffset = offset + this.bytesPerLine;
             dlr.remainingLength = length - this.bytesPerLine;
         }
         return dlr;
     }
 
     private static class dumpLineResult {
         private String line;
         private int nextOffset;
         private int remainingLength;
 
         private dumpLineResult(String line, int nextOffset, int remainingLength) {
             this.line = line;
             this.nextOffset = nextOffset;
             this.remainingLength = remainingLength;
         }
     }
 }
