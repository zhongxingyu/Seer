 package se.l4.aurochs.serialization.format;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * Input for binary format.
  * 
  * @author Andreas Holstenson
  *
  */
 public class BinaryInput
 	extends AbstractStreamingInput
 {
 	private static final int CHARS_SIZE = 1024;
 	private static final ThreadLocal<char[]> CHARS = new ThreadLocal<char[]>()
 	{
 		@Override
 		protected char[] initialValue()
 		{
 			return new char[1024];
 		}
 	};
 	
 	private final InputStream in;
 	
 	private final byte[] buffer;
 	
 	private int peekedByte;
 	
 	public BinaryInput(InputStream in)
 	{
 		this.in = in;
 		buffer = new byte[8];
 		
 		peekedByte = -2;
 	}
 
 	@Override
 	public Token peek()
 		throws IOException
 	{
 		if(peekedByte == -2)
 		{
 			peekedByte = in.read();
 		}
 		
 		switch(peekedByte)
 		{
 			case -1:
 				return null;
 			case BinaryOutput.TAG_KEY:
 				return Token.KEY;
 			case BinaryOutput.TAG_OBJECT_START:
 				return Token.OBJECT_START;
 			case BinaryOutput.TAG_OBJECT_END:
 				return Token.OBJECT_END;
 			case BinaryOutput.TAG_LIST_START:
 				return Token.LIST_START;
 			case BinaryOutput.TAG_LIST_END:
 				return Token.LIST_END;
 			case BinaryOutput.TAG_NULL:
 				return Token.NULL;
 			default:
 				return Token.VALUE;
 		}
 	}
 
 	@Override
 	protected Token next0()
 		throws IOException
 	{
 		Token current = peek();
 		if(current == Token.KEY || current == Token.VALUE || current == Token.NULL)
 		{
 			// Read actual data of keys and values
 			readValue();
 		}
 		
 		peekedByte = in.read();
 		
 		return current;
 	}
 	
 	private void readBuffer(int len)
 		throws IOException
 	{
 		int read = in.read(buffer, 0, len);
 		if(read != len)
 		{
			throw new EOFException("Expected to read " + len + " bytes, but could only read " + read);
 		}
 	}
 	
 	private double readDouble()
 		throws IOException
 	{
 		readBuffer(8);
 		long value = ((long) buffer[0] & 0xff) |
 			((long) buffer[1] & 0xff) << 8 |
 			((long) buffer[2] & 0xff) << 16 |
 			((long) buffer[3] & 0xff) << 24 |
 			((long) buffer[4] & 0xff) << 32 |
 			((long) buffer[5] & 0xff) << 40 |
 			((long) buffer[6] & 0xff) << 48 |
 			((long) buffer[7] & 0xff) << 56;
 		
 		return Double.longBitsToDouble(value);
 	}
 	
 	private float readFloat()
 		throws IOException
 	{
 		readBuffer(4);
 		int value = (buffer[0] & 0xff) |
 			(buffer[1] & 0xff) << 8 |
 			(buffer[2] & 0xff) << 16 |
 			(buffer[3] & 0xff) << 24;
 		
 		return Float.intBitsToFloat(value);
 	}
 	
 	private int readInteger()
 		throws IOException
 	{
 		int shift = 0;
 		int result = 0;
 		while(shift < 32)
 		{
 			final byte b = (byte) in.read();
 			result |= (int) (b & 0x7F) << shift;
 			if((b & 0x80) == 0) return result;
 			
 			shift += 7;
 		}
 		
 		throw new EOFException("Invalid integer");
 	}
 	
 	private long readLong()
 		throws IOException
 	{
 		int shift = 0;
 		long result = 0;
 		while(shift < 64)
 		{
 			final byte b = (byte) in.read();
 			result |= (long) (b & 0x7F) << shift;
 			if((b & 0x80) == 0) return result;
 			
 			shift += 7;
 		}
 		
 		throw new EOFException("Invalid long");
 	}
 	
 	private String readString()
 		throws IOException
 	{
 		int length = readInteger();
 		char[] chars = length < CHARS_SIZE ? CHARS.get() : new char[length];
 		
 		for(int i=0; i<length; i++)
 		{
 			int c = in.read() & 0xff;
 			int t = c >> 4;
 			if(t > -1 && t < 8)
 			{
 				chars[i] = (char) c;
 			}
 			else if(t == 12 || t == 13)
 			{
 				chars[i] = (char) ((c & 0x1f) << 6 | in.read() & 0x3f);
 			}
 			else if(t == 14)
 			{
 				chars[i] = (char) ((c & 0x0f) << 12 
 					| (in.read() & 0x3f) << 6
 					| (in.read() & 0x3f) << 0);
 			}
 		}
 		
 		return new String(chars, 0, length);
 	}
 
 	private byte[] readByteArray()
 		throws IOException
 	{
 		int length = readInteger();
 		byte[] buffer = new byte[length];
 		int read = in.read(buffer);
 		
 		if(read != length)
 		{
 			throw new EOFException("Stream ended before entire byte array was sent");
 		}
 		
 		return buffer;
 	}
 	
 	private void readValue()
 		throws IOException
 	{
 		switch(peekedByte)
 		{
 			case BinaryOutput.TAG_BOOLEAN:
 				int b = in.read();
 				setValue(b == 1);
 				break;
 			case BinaryOutput.TAG_DOUBLE:
 				setValue(readDouble());
 				break;
 			case BinaryOutput.TAG_FLOAT:
 				setValue(readFloat());
 				break;
 			case BinaryOutput.TAG_INT:
 				int i = readInteger();
 				i = (i >>> 1) ^ -(i & 1);
 				setValue(i);
 				break;
 			case BinaryOutput.TAG_LONG:
 				long l = readLong();
 				l = (l >>> 1) ^ -(l & 1);
 				setValue(l);
 				break;
 			case BinaryOutput.TAG_NULL:
 				setValue(null);
 				break;
 			case BinaryOutput.TAG_KEY:
 			case BinaryOutput.TAG_STRING:
 				setValue(readString());
 				break;
 			case BinaryOutput.TAG_BYTE_ARRAY:
 				setValue(readByteArray());
 				break;
 			default:
 				throw new IOException("Unexpected value type, no idea what to do (type was " + peekedByte + ")");
 		}
 		
 	}
 }
