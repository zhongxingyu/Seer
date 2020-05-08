 package com.slugsource.steam.servers.readers;
 
 import com.slugsource.steam.serverbrowser.NotAServerException;
 import com.slugsource.steam.servers.SourceServer;
 
 /**
  *
  * @author Nathan Fearnley
  */
 public abstract class ServerReader<T extends SourceServer>
 {
 
     protected int index;
     protected byte[] data;
 
     public ServerReader()
     {
     }
 
     public abstract void readServer(byte[] rawdata, T server) throws NotAServerException;
 
     protected boolean readBoolean()
     {
         boolean result = data[index] == 0x01;
         index += 1;
         return result;
     }
 
     protected char readChar()
     {
         char result = (char) data[index];
         index += 1;
         return result;
     }
 
     protected String readNullTerminatedString()
     {
         int count = 0;
         while (data[index + count] != 00)
         {
             count++;
             if (index + count > data.length)
             {
                 throw new IndexOutOfBoundsException();
             }
         }
 
         String result = new String(data, index, count);
         index += count + 1;
         return result;
     }
 
     protected String readLengthPrefixedNullTerminatedString()
     {
         int count = readUInt8();
         String result = new String(data, index, count);
        index += count + 1;
         return result;
     }
 
     protected int readUInt8()
     {
         int result = 0xFF & data[index];
         index += 1;
         return result;
     }
 
     protected int readUInt16()
     {
         int result = readUInt8()
                 | (readUInt8() << 8);
         return result;
     }
 
     // Bug: large unsigned ints become negative signed ints
     // TODO: Add support for real unsigned 32-bit integers
     protected int readUInt32()
     {
         int result = readUInt8()
                 | (readUInt8() << 8)
                 | (readUInt8() << 16)
                 | (readUInt8() << 24);
         return result;
     }
 
     // Bug: large unsigned ints become negative signed ints
     // TODO: Add support for real unsigned 64-bit integers
     protected long readUInt64()
     {
         long result = (long) readUInt8()
                 | ((long) readUInt8() << 8)
                 | ((long) readUInt8() << 16)
                 | ((long) readUInt8() << 24)
                 | ((long) readUInt8() << 32)
                 | ((long) readUInt8() << 40)
                 | ((long) readUInt8() << 48)
                 | ((long) readUInt8() << 56);
         return result;
     }
 }
