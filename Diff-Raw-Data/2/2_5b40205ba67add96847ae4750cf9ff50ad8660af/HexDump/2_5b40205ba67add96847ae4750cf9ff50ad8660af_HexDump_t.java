 package org.uli.hexdump;
 
 public class HexDump {
     static private final int DEFAULT_BYTES_PER_LINE=16;
     static private final String LINE_SEPARATOR=System.getProperty("line.separator");
     private int bytesPerLine;
 
     static public String hexDump(byte[] array) {
 	return hexDump(array, 0, array.length);
     }
 
     static public String hexDump(byte[] array, int offset, int length) {
 	HexDump hd = new HexDump();
 	return hd.dump(array, offset, length);
     }
 
     public HexDump() {
 	this(DEFAULT_BYTES_PER_LINE);
     }
 
     public HexDump(int bytesPerLine) {
 	this.bytesPerLine = bytesPerLine;
     }
 
     private String dump(byte[] array, int offset, int length) {
 	StringBuffer sb = new StringBuffer(length*3+100);
 	int remainingLength = length;
 	int thisLineOffset   = offset;
 	while (remainingLength > 0) {
 	    StringBuffer hexBuffer = new StringBuffer(3*this.bytesPerLine);
 	    StringBuffer textBuffer = new StringBuffer(this.bytesPerLine);
 	    sb.append(address(thisLineOffset));
 	    sb.append(":");
 	    int i=0;
 	    for (; i<remainingLength && i<this.bytesPerLine; i++) {
 		byte b = array[thisLineOffset+i];
 		append(hexBuffer, String.format("%02x", b), i);
 		textBuffer.append(text(b));
 	    }
 	    for ( ; i<this.bytesPerLine; i++) {
 		append(hexBuffer, "  ", i);
 	    }
 	    sb.append(hexBuffer);
 	    sb.append("  '");
 	    sb.append(textBuffer);
 	    sb.append("'");
 	    sb.append(LINE_SEPARATOR);
 	    thisLineOffset += this.bytesPerLine;
 	    remainingLength -= this.bytesPerLine;
 	}
 	return sb.toString();
     }
 
     private StringBuffer append(StringBuffer sb, String a, int i) {
 	if (i%4 == 0) {
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
 	    result = new String(new byte[]{n});
 	} else {
 	    result = ".";
 	}
 	return result;
     }
 
     private boolean isPrintable(byte n) {
 	return n>=32 && n<127;
     }
 
     public static void main(String[] args) {
 	System.out.println(HexDump.hexDump(args[0].getBytes()));
     }
 }
