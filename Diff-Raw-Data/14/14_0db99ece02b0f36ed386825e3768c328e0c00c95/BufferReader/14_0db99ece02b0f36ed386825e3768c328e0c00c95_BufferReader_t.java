 /*
  * Copyright 2009 HPDI, LLC
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jvss.physical;
 
 import org.jvss.hash.VssCRC32;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 import java.util.Date;
 
 /**
  * Reads VSS data types from a byte buffer.
  */
 public class BufferReader
 {
 
    private final String encoding;
 
    private final byte[] data;
 
    private int offset;
 
    private final int limit;
 
    public BufferReader(String encoding, byte[] data)
    {
       this(encoding, data, 0, data.length);
    }
 
    public BufferReader(String encoding, byte[] data, int offset, int limit)
    {
       this.encoding = encoding;
       this.data = data;
       this.offset = offset;
       this.limit = limit;
    }
 
    /**
     * @param offset
     *           the offset to set
     */
    public void setOffset(int offset)
    {
       this.offset = offset;
    }
 
    public int getOffset()
    {
       return offset;
    }
 
    public int getRemaining()
    {
       return limit - offset;
    }
 
    public short checksum16()
    {
       short sum = 0;
       for (int i = offset; i < limit; ++i)
       {
          sum += data[i];
       }
       return sum;
    }
 
    //private static Hash16 crc16 = new XorHash32To16(new Crc32(Crc32.IEEE));
 
    public short crc16()
    {
       return VssCRC32.VssCrc16(data, offset, limit);//crc16.compute(data, offset, limit);
    }
 
    public short crc16(int bytes)
    {
       CheckRead(bytes);
       return VssCRC32.VssCrc16(data, offset, offset + bytes); //crc16.compute(data, offset, offset + bytes)
    }
 
    public void skip(int bytes)
    {
       CheckRead(bytes);
       offset += bytes;
    }
 
    public short readInt16()
    {
       CheckRead(2);
       //return (short)(data[offset++] | data[offset++] << 8);
       short result = ByteBuffer.wrap(data, offset, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
       //short result2 = (short)(data[offset++] | data[offset++] << 8);
       short result2 = (short)(data[offset++] & 0xff | data[offset++] << 8);
 
       //System.out.println("int16->" + result + "=" + result2);
 
       //offset += 2;
       return result;
    }
 
    public int readInt32()
    {
       CheckRead(4);
       int offsetOrig = offset;
       int result = data[offset++] & 0xff | data[offset++] << 8 | data[offset++] << 16 | data[offset++] << 24;
       //System.out.println(result);
       int result2 = ByteBuffer.wrap(data, offsetOrig, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
      //System.out.println("int32->" + result + "=" + result2);
       return result2;
    }
 
    public Date readDateTime()
    {
       return new Date(readInt32());
    }
 
    public String readSignature(int length)
    {
       CheckRead(length);
       StringBuilder buf = new StringBuilder(length);
       for (int i = 0; i < length; ++i)
       {
          buf.append((char)data[offset++]);
       }
       return buf.toString();
    }
 
    public VssName readName()
    {
       CheckRead(2 + 34 + 4);
       return new VssName(readInt16(), readString(34), readInt32());
    }
 
    public String readString(int fieldSize)
    {
       CheckRead(fieldSize);
 
       int count = 0;
       for (int i = 0; i < fieldSize; ++i)
       {
          if (data[offset + i] == 0)
          {
             break;
          }
          ++count;
       }
       String str;
       try
       {
          str = new String(data, offset, count, encoding);
       }
       catch (UnsupportedEncodingException e)
       {
          throw new RuntimeException(e.getLocalizedMessage(), e);
       }
 
       offset += fieldSize;
 
       return str;
    }
 
    public String readByteString(int bytes)
    {
       CheckRead(bytes);
       String result = formatBytes(bytes);
       offset += bytes;
       return result;
    }
 
    public BufferReader extract(int bytes)
    {
       CheckRead(bytes);
       return new BufferReader(encoding, data, offset, offset += bytes);
    }
 
    public byte[] getBytes(int bytes)
    {
       CheckRead(bytes);
 
       byte[] result = Arrays.copyOfRange(data, offset, bytes);
       //var result = new ArraySegment<byte>(data, offset, bytes);
       offset += bytes;
       return result;
    }
 
    public String formatBytes(int bytes)
    {
       int formatLimit = Math.min(limit, offset + bytes);
       StringBuilder buf = new StringBuilder((formatLimit - offset) * 3);
       for (int i = offset; i < formatLimit; ++i)
       {
          buf.append(String.format("{0:X2} ", data[i]));
       }
       return buf.toString();
    }
 
    public String formatRemaining()
    {
       return formatBytes(getRemaining());
    }
 
    private void CheckRead(int bytes)
    {
       if (offset + bytes > limit)
       {
          throw new EndOfBufferException(String.format(
             "Attempted read of {0} bytes with only {1} bytes remaining in buffer", bytes, getRemaining()));
       }
    }
 }
