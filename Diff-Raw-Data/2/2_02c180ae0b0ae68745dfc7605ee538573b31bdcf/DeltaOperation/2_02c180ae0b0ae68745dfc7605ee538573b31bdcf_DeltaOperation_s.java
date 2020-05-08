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
 
 import java.io.PrintStream;
 import java.util.Arrays;
 
 /**
  * Represents a single delta operation for a file revision.
  */
 public class DeltaOperation
 {
    /**
     * Enumeration of file revision delta commands.
     * 
     */
    public static enum DeltaCommand {
       WriteLog(0), // write data from the log file
       WriteSuccessor(1), // write data from the subsequent revision
       Stop(2); // indicates the last operation
 
       public static DeltaCommand valueOf(int value)
       {
          switch (value)
          {
             case 0 :
                return WriteLog;
             case 1 :
                return WriteSuccessor;
             case 2 :
                return Stop;
             default :
                throw new IllegalArgumentException(value + " is not a valid DeltaCommand");
          }
       }
 
       private final int value;
 
       private DeltaCommand(int value)
       {
          this.value = value;
       }
 
       /**
        * @return the value
        */
       public int getValue()
       {
          return value;
       }
 
    }
 
    public static DeltaOperation writeLog(byte[] data, int offset, int length)
    {
       DeltaOperation result = new DeltaOperation();
       result.command = DeltaCommand.WriteLog;
       result.length = length;
      result.data = Arrays.copyOfRange(data, offset, length);//TODO check this
       return result;
    }
 
    public static DeltaOperation writeSuccessor(int offset, int length)
    {
       DeltaOperation result = new DeltaOperation();
       result.command = DeltaCommand.WriteSuccessor;
       result.offset = offset;
       result.length = length;
       return result;
    }
 
    private DeltaCommand command;
 
    private int offset; // meaningful for WriteSuccessor only
 
    private int length;
 
    private byte[] data; // WriteLog only
 
    public void dump(PrintStream writer)
    {
       final int MAX_DATA_DUMP = 40;
       writer.print(String.format("  %s: Offset=%d, Length=%d", command, offset, length));
       if (data != null)
       {
          int dumpLength = data.length;
          boolean truncated = false;
          if (dumpLength > MAX_DATA_DUMP)
          {
             dumpLength = MAX_DATA_DUMP;
             truncated = true;
          }
 
          StringBuilder buf = new StringBuilder(dumpLength);
          for (int i = 0; i < dumpLength; ++i)
          {
             //byte b = data[data.Offset + i];
             byte b = data[i];
             buf.append(b >= 0x20 && b <= 0x7E ? (char)b : '.');
          }
          writer.print(String.format(", Data: %s%s", buf.toString(), truncated ? "..." : ""));
       }
       writer.println();
    }
 
    /**
     * @return the command
     */
    public DeltaCommand getCommand()
    {
       return command;
    }
 
    /**
     * @return the data
     */
    public byte[] getData()
    {
       return data;
    }
 
    /**
     * @return the length
     */
    public int getLength()
    {
       return length;
    }
 
    /**
     * @return the offset
     */
    public int getOffset()
    {
       return offset;
    }
 
    public void read(BufferReader reader)
    {
       command = DeltaCommand.valueOf(reader.readInt16());
       reader.skip(2); // unknown
       offset = reader.readInt32();
       length = reader.readInt32();
       if (command == DeltaCommand.WriteLog)
       {
          data = reader.getBytes(length);
       }
    }
 
 }
