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
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileChannel.MapMode;
 
 /**
  * Represents a file containing VSS records.
  */
 public class VssRecordFile
 {
    private final String filename;
 
    protected final BufferReader reader;
 
    public String filename()
    {
       return filename;
    }
 
    public VssRecordFile(String filename, String encoding)
    {
       this.filename = filename;
       reader = new BufferReader(encoding, readFile(filename));
    }
 
    public void readRecord(VssRecord record)
    {
       try
       {
          RecordHeader recordHeader = new RecordHeader();
          recordHeader.Read(reader);
 
          BufferReader recordReader = reader.extract(recordHeader.getLength());
 
          // comment records always seem to have a zero CRC
          if (recordHeader.getSignature() != CommentRecord.SIGNATURE)
          {
             recordHeader.CheckCrc();
          }
 
          recordHeader.CheckSignature(record.getSignature());
 
          record.read(recordReader, recordHeader);
       }
       catch (EndOfBufferException e)
       {
          throw new RecordTruncatedException(e.getLocalizedMessage());
       }
    }
 
    public void ReadRecord(VssRecord record, int offset)
    {
       reader.setOffset(offset);
       readRecord(record);
    }
 
    public boolean ReadNextRecord(VssRecord record)
    {
       while (reader.getRemaining() > RecordHeader.LENGTH)
       {
          try
          {
             RecordHeader recordHeader = new RecordHeader();
             recordHeader.Read(reader);
 
             BufferReader recordReader = reader.extract(recordHeader.getLength());
 
             // comment records always seem to have a zero CRC
             if (!recordHeader.getSignature().equals(CommentRecord.SIGNATURE))
             {
                recordHeader.CheckCrc();
             }
 
             if (recordHeader.getSignature().equals(record.getSignature()))
             {
                record.read(recordReader, recordHeader);
                return true;
             }
          }
          catch (EndOfBufferException e)
          {
             throw new RecordTruncatedException(e.getLocalizedMessage());
          }
       }
       return false;
    }
 
    protected <T extends VssRecord> T getRecord(RecordCreator<T> creationCallback, boolean ignoreUnknown)
    {
       RecordHeader recordHeader = new RecordHeader();
       recordHeader.Read(reader);
 
       BufferReader recordReader = reader.extract(recordHeader.getLength());
 
       // comment records always seem to have a zero CRC
      if (recordHeader.getSignature() != CommentRecord.SIGNATURE)
       {
          recordHeader.CheckCrc();
       }
 
       T record = creationCallback.createRecord(recordReader, recordHeader);
 
       if (record != null)
       {
          // double-check that the object signature matches the file
          recordHeader.CheckSignature(record.getSignature());
          record.read(recordReader, recordHeader);
       }
       else if (!ignoreUnknown)
       {
          throw new UnrecognizedRecordException(recordHeader, String.format(
             "Unrecognized record signature {0} in item file", recordHeader.getSignature()));
       }
       return record;
    }
 
    protected <T extends VssRecord> T getRecord(RecordCreator<T> creationCallback, boolean ignoreUnknown, int offset)
    {
       reader.setOffset(offset);
       return getRecord(creationCallback, ignoreUnknown);
    }
 
    protected <T extends VssRecord> T getNextRecord(RecordCreator<T> creationCallback, boolean skipUnknown)
 
    {
       while (reader.getRemaining() > RecordHeader.LENGTH)
       {
          T record = getRecord(creationCallback, skipUnknown);
          if (record != null)
          {
             return record;
          }
       }
       return null;
    }
 
    public static interface RecordCreator<T extends VssRecord>
    {
       T createRecord(BufferReader reader, RecordHeader header);
    }
 
    private static byte[] readFile(String filename)
    {
       //       byte[] data;
       //       using (var stream = new FileStream(filename,
       //           FileMode.Open, FileAccess.Read, FileShare.Read))
       //       {
       //           data = new byte[stream.Length];
       //           stream.Read(data, 0, data.Length);
       //       }
       //       return data;
 
       File f = new File(filename.toLowerCase());
       FileInputStream fin = null;
       FileChannel ch = null;
       try
       {
          fin = new FileInputStream(f);
          ch = fin.getChannel();
          int size = (int)ch.size();
          MappedByteBuffer buf = ch.map(MapMode.READ_ONLY, 0, size);
          byte[] bytes = new byte[size];
          buf.get(bytes);
          return bytes;
 
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
       finally
       {
          try
          {
             if (fin != null)
             {
                fin.close();
             }
             if (ch != null)
             {
                ch.close();
             }
          }
          catch (IOException e)
          {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
       return new byte[0];
    }
 }
