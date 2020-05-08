 package codechicken.obfuscator.fs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 
 public class DataBuffer
 {
     private class DataEntry
     {
         final int len;
         final byte[] buf;
         
         public DataEntry(InputStream in) throws IOException
         {
             buf = new byte[blocksize];
            len = in.read(buf);
         }
     }
     private LinkedList<DataEntry> data = new LinkedList<DataEntry>();
     private int length;
     private int blocksize = 4096;
     
     private boolean locked;
     
     public DataBuffer(long size) throws IOException
     {
         if(size > 16777216)
             throw new IOException("Please don't send us files > 16MB. You're doing it wrong");
         if(size > 0)
             blocksize = (int)size;
     }
     
     public void lock()
     {
         locked = true;
     }
     
     public void read(InputStream in) throws IOException
     {
         if(locked)
             throw new IOException("Cannot read to a locked DataBuffer");
         
         while(true)
         {
             DataEntry e = new DataEntry(in);
             if(e.len <= 0)
                 break;
             
             data.add(e);
             length+=e.len;
         }
     }
     
     public int getLength()
     {
         return length;
     }
     
     public void write(OutputStream out) throws IOException
     {
         lock();
         
         for(DataEntry e : data)
             out.write(e.buf, 0, e.len);
     }
 }
