 package com.jdevelopstation.l2ce.version.node.file.reader;
 
 import java.nio.ByteBuffer;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * @author VISTALL
  * @date 9:29/18.05.2011
  */
 public class ASCF implements ReadWriteType<String>
 {
 	@Override
 	public String read(ByteBuffer buff)
 	{
 		int size = buff.get() & 0xFF;
 		if(size == 0)
 			return StringUtils.EMPTY;
 
 		if(size >= 192)
 		{
 			buff.position(buff.position() - 1);
 			size = buff.getShort() & 0xFFFF;
 		}
 
 		if(size >= Short.MAX_VALUE)
 			buff.position(buff.position() + 1);
 
 		if(size >= 128)
 		{
 			if(buff.getChar(buff.position()) == 0)
 				return StringUtils.EMPTY;
 
 			StringBuilder b = new StringBuilder();
 
 			char d = 0;
 			while((d = buff.getChar()) != 0)
 				b.append(d);
 
 			return b.toString();
 		}
 		else
 		{
 			StringBuilder b = new StringBuilder();
 
 			byte d = 0;
 			while((d = buff.get()) != 0)
 				b.append((char) d);
 
 			return b.toString();
 		}
 	}
 
 	@Override
 	public void write(Object val, ByteBuffer buff)
 	{
 		if(val instanceof String)
 		{
 			String value = (String) val;
			int length = value.length() + 1;
 
 			if(!value.isEmpty())
 			{
 				char[] chars = value.toCharArray();
 
 				boolean unicode = false;
 
 				for(char c : value.toCharArray())
					if(c > 255 && c != 1081)
 						unicode = true;
 
 				if(length >= 64)
 				{
 					if(unicode)
 						buff.put((byte) (length + 128));
 					else
 						buff.put((byte) length);
 					buff.put((byte)(length >> 8));
 				}
 				else
 				{
 					// set highest bit cause to indicate it is Unicode
 					if(unicode)
 						length += 128;
 
 					buff.put((byte) length);
 				}
 
 				if(unicode)
 				{
 					for(char c : chars)
 						buff.putChar(c);
 					buff.put((byte) 0x00);
 				}
 				else
 				{
 					for(char c : chars)
 						buff.put((byte)c);
 				}
 			}
 
 			buff.put((byte) 0); //term
 		}
 	}
 }
