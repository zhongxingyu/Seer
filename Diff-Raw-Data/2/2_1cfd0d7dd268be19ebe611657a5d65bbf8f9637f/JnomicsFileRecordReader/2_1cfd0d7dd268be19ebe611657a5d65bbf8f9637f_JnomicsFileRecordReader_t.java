 /*
  * Copyright (C) 2011 Matthew A. Titmus
  * 
  * Last modified: $Date$ (revision $Revision$)
  */
 
 package edu.cshl.schatz.jnomics.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 import org.apache.hadoop.util.LineReader;
 
 import edu.cshl.schatz.jnomics.ob.IUPACCodeException;
 import edu.cshl.schatz.jnomics.ob.writable.QueryTemplate;
 import edu.cshl.schatz.jnomics.util.TextCutter;
 
 /**
  * This record reader reads short-read sequence files, and breaks the data into
  * key/value pairs for input to the Mapper.
  */
 public abstract class JnomicsFileRecordReader extends RecordReader<Writable, QueryTemplate> {
     public static final int DEFAULT_BUFFER_SIZE = 64 * 1024;
 
     protected static final byte[] SLASH_FIRST = { '/', '1' };
 
     protected static final byte[] SLASH_LAST = { '/', '2' };
 
     protected Text key = null;
 
     protected long pos, splitStart, splitEnd;
 
     protected QueryTemplate value = null;
 
     protected LineReader in;
 
     public static boolean hasTrailingSlashNumber(Text text) {
         int len = text.getLength();
         byte[] bytes = text.getBytes();
 
        return (len > 2 && bytes[len - 2] == (byte) '/' && Character.isDigit(bytes[len - 1]));
     }
 
     public static void trimTrailingSlashNumber(Text text) {
         int len = text.getLength();
         byte[] bytes = text.getBytes();
 
         if (bytes[len - 2] == (byte) '/' && Character.isDigit(bytes[len - 1])) {
             text.set(bytes, 0, len - 2);
         }
     }
 
     /*
      * @see org.apache.hadoop.mapreduce.RecordReader#close()
      */
     @Override
     public synchronized void close() throws IOException {
         if (in != null) {
             in.close();
         }
     }
 
     /*
      * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentKey()
      */
     @Override
     public Text getCurrentKey() {
         return key;
     }
 
     /*
      * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentValue()
      */
     @Override
     public QueryTemplate getCurrentValue() {
         return value;
     }
 
     /**
      * Get the progress within the split
      */
     @Override
     public float getProgress() {
         if (splitStart == splitEnd) {
             return 0.0f;
         } else {
             return Math.min(1.0f, (pos - splitStart) / (float) (splitEnd - splitStart));
         }
     }
 
     /**
      * @param split
      * @param conf
      * @throws IOException
      */
     public abstract void initialize(FileSplit split, Configuration conf) throws IOException;
 
     /*
      * @see
      * org.apache.hadoop.mapreduce.RecordReader#initialize(org.apache.hadoop
      * .mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)
      */
     @Override
     public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {
         initialize((FileSplit) genericSplit, context.getConfiguration());
     }
 
     /**
      * Allows a reader to be constructed from only a {@link Path} and
      * {@link Configuration}.
      */
     public void initialize(Path file, Configuration conf) throws IOException {
         FileSystem fs = file.getFileSystem(conf);
         FileSplit split = new FileSplit(file, 0, fs.getFileStatus(file).getLen(), null);
 
         initialize(split, conf);
     }
 
     /**
      * This reads a short read DNA sequence from a text file, one line at a
      * time.
      */
     public static abstract class RecordLineReader extends LineReader {
         protected Text line = new Text();
 
         protected TextCutter textCutter = new TextCutter().setDelimiter('\t');
 
         /**
          * Create a reader that reads from the given stream using the default
          * buffer-size (64k).
          * 
          * @param in The input stream
          * @throws IOException
          */
         public RecordLineReader(InputStream in) {
             this(in, DEFAULT_BUFFER_SIZE);
         }
 
         /**
          * Create a reader that reads from the given stream using the
          * <code>io.file.buffer.size</code> specified in the given
          * {@link Configuration}.
          * 
          * @param in The input stream
          * @param conf The configuration
          */
         public RecordLineReader(InputStream in, Configuration conf) {
             this(in, conf.getInt("io.file.buffer.size", DEFAULT_BUFFER_SIZE));
         }
 
         /**
          * Create a line reader that reads from the given stream using the given
          * buffer-size.
          * 
          * @param in The input stream
          * @param bufferSize Size of the read buffer
          * @throws IOException
          */
         public RecordLineReader(InputStream in, int bufferSize) {
             super(in, bufferSize);
         }
 
         /**
          * Reads one or more lines from the InputStream into the given
          * {@link QueryTemplate}. A line can be terminated by one of the
          * following: '\n' (LF) , '\r' (CR), or '\r\n' (CR+LF). EOF also
          * terminates an otherwise unterminated line.
          * 
          * @param queryTemplate the object to store the next one or more
          *            records.
          * @return the number of bytes read including the (longest) newline
          *         found.
          * @throws IOException if thrown by the underlying stream.
          * @throws IUPACCodeException
          */
         public int readRecord(QueryTemplate queryTemplate) throws IOException {
             return readRecord(queryTemplate, Integer.MAX_VALUE, Integer.MAX_VALUE);
         }
 
         /**
          * Reads one or more lines from the InputStream into the given
          * {@link QueryTemplate}. A line can be terminated by one of the
          * following: '\n' (LF) , '\r' (CR), or '\r\n' (CR+LF). EOF also
          * terminates an otherwise unterminated line.
          * 
          * @param queryTemplate the object to store the next one or more
          *            records.
          * @throws IOException if thrown by the underlying stream.
          * @throws IUPACCodeException
          */
         public abstract int readRecord(QueryTemplate queryTemplate, int maxLineLength,
             int maxBytesToConsume) throws IOException;
     }
 }
