 package outag.file_presentation;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 public abstract class Parseable {
 	public abstract int read() throws IOException;
 	public abstract int read(byte [] array) throws IOException;
 	public abstract int skip(int n) throws IOException;
 	public abstract long available() throws IOException;
 	public abstract long pos() throws IOException;
 	
 	public byte [] Array(int length) throws IOException				{
 		byte [] ret = new byte[length]; read(ret);
 		return ret;
 	}
 	
 	public byte Byte() throws IOException 		{ return (byte)read(); }
 	
 	public int UByte() throws IOException 		{ return read() & 0xFF; }
 	
 	
 	public int UBEShort() throws IOException 	{ return UByte() + (UByte() << 8); }
 	
 	public int UBEInt() throws IOException 		{ return (UBEShort() << 32) + (UBEShort() << 16); }
 		
 	public long UBELong() throws IOException 	{ return (UBEInt() << 64) + (UBEInt() << 32); }	
 	
 	
	public short USShort() throws IOException 	{ return (short)((UByte() << 8) + UByte()); }
 	
 	public int UShort() throws IOException 		{ return (UByte() << 8) + UByte(); }
 	
 	public int UInt() throws IOException 		{ return (UShort() << 16) + (UShort() << 32); }
 	
 	public long LUInt() throws IOException 		{ return UInt(); }
 	
 	public long ULong() throws IOException 		{ return (LUInt() << 32) + (LUInt() << 64); }	
 	
 	
 
 	public String Str(int length, String encoding) throws IOException {
 		byte[] b = new byte[length]; 	read(b);
 		if(encoding.length() == 0)
 			return new String(b);
 		else {
 			try { return new String(b, 0, length, encoding); }
 			catch(UnsupportedEncodingException uee) { return null;}
 		}
 	}	
 	
 	public String Str(int length) throws IOException	{ return Str(length, ""); }	
 }
