 package directoryencryption;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author scottl
  */
 public class DirectoryEncryption {
     private static final boolean debug = false;
     private static final boolean showStatistics = true;
     private long bytesProcessed = 0;
     private long filesProcessed = 0;
     private static Date startTime;
     
    
     public DirectoryEncryption()
     {
         startTime = new Date();
     }
     
    
     public boolean encryptDirectory(File directory)
     {
         try
         {
             if (!directory.isDirectory())
                 return false;
             for (File file : directory.listFiles())
                 if (!file.isDirectory())
                     encryptFile(file);
                 else
                     encryptDirectory(file);
         }
         catch(IOException ex)
         {    
             if (debug)
                 ex.printStackTrace();
             return false;
         }
         return true;
     }
     
     public void encryptFile(File file) throws IOException
     {invertFile(file);}
     
     public void decryptFile(File file) throws IOException
     {invertFile(file);}
     
     /**
      * Flips ever bit in a file.
      * @param path Path to file to invert.
      * @throws IOException Throws IOException if there is an error during read or write.
      */
     public void invertFile(File file) throws IOException
     {
         checkFile(file);
         if (file.isDirectory())
             return;
         
         // read in file and flip the bits
         BufferedInputStream inputStream = null;
         ArrayList<Byte> destination = new ArrayList((int)file.length());
         try
         {
             inputStream = new BufferedInputStream(new FileInputStream(file));
             byte[] bytes = new byte[4096];
             int numBytesRead = 0;
             // flip each byte and put it to the destination list
             while((numBytesRead = inputStream.read(bytes)) > -1)
                 for(int i = 0; i<numBytesRead; i++)
                     destination.add((byte) ~bytes[i]);
         }
         catch(IOException e)
         {throw e;}
         finally
         {
             if (inputStream != null)
                 inputStream.close();
         }
         
         // write flipped bytes back to the file
         // this is the slowest way to do this, but we're just testing.
         BufferedOutputStream outputStream = null;
         try
         {
             outputStream = new BufferedOutputStream(new FileOutputStream(file));
             for (byte b : destination)
                 outputStream.write(b);
         }
         catch(FileNotFoundException e)
         {
             if (debug)
                 e.printStackTrace();
         }
         finally
         {
             if (outputStream != null)
                 outputStream.close();
             bytesProcessed += file.length();
             filesProcessed++;
         }
         
         // show current stats
         if (showStatistics)
             if (filesProcessed % 250 == 0)
                 showCurrentStats();
     }
     
     private void checkFile(File file) throws IOException, NullPointerException
     {
         if (file == null)
             throw new NullPointerException("Null pointer in file check");
         if (!file.exists())
             throw new IOException("File does not exist: " + file.getAbsolutePath());
         if (!file.canRead() || !file.canWrite())
             throw new IOException("Cannot read or write file");
     }
     
     
     public void showFile(File file) throws IOException
     {
         checkFile(file);
         BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
         byte[] bytes = new byte[4096];
         
         System.out.println("File: " + file.getAbsolutePath());
         while(inputStream.read(bytes) > 0)
             for(byte b : bytes)
                 System.out.print(b);
     }
         
     public final void showCurrentStats()
     {
         long duration = new Date().getTime() - startTime.getTime();
         System.out.println("Bytes processed: " + bytesProcessed);
         System.out.println("Files processed : " + filesProcessed);
 
         double kBytesProcessed = bytesProcessed / 1024;
         double mBytesProcessed = kBytesProcessed / 1024;
 //        double kBytesProcessedPerMS = kBytesProcessed / duration;
         double durationInSeconds = duration / 1000;
         double mBytesProcessedPerSecond = mBytesProcessed / durationInSeconds;
 
 //        System.out.println("Kbytes processed per MS : " + kBytesProcessedPerMS);
         System.out.println("Mbytes processed per second : " + mBytesProcessedPerSecond);
         
     }
     
     /**
      * @param args directory to encrypt.
      */
     public static void main(String[] args) {
         String path = "C:\\TEMP";
         DirectoryEncryption de = new DirectoryEncryption();
         
         try
         {
             de.encryptDirectory(new File(path));
         }
         catch(Exception e)
         {e.printStackTrace();}
         Date stopTime = new Date();
         
         if (showStatistics)
         {
             long duration = stopTime.getTime() - de.startTime.getTime();
             System.out.println("Run time: " + duration + " MS");
         }
         
     }
 }
