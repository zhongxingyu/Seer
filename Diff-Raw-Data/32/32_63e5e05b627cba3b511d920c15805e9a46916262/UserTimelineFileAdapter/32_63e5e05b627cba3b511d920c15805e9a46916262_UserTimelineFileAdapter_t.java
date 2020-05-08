 package com.cafeform.iumfs.twitterfs.files;
 
 import com.cafeform.iumfs.FileExistException;
 import com.cafeform.iumfs.FileType;
 import com.cafeform.iumfs.IumfsFile;
 import com.cafeform.iumfs.NotADirectoryException;
 import com.cafeform.iumfs.NotSupportedException;
import com.cafeform.iumfs.twitterfs.Account;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import twitter4j.TwitterException;
 
 /**
  * Adapter of UserTimelineFile.
  * All methods but getPathname call apapteee's method.
  */
 public class UserTimelineFileAdapter implements NormalTimelineFile
 {
     private final NormalTimelineFile file;
     private final String pathname;
     
     public UserTimelineFileAdapter (String pathname, NormalTimelineFile file)
     {
         if (!(file instanceof UserTimelineFileImpl))
         {
             throw new IllegalArgumentException(
                     "not an instance of UserTimelineFile");
         }
         this.file = file;
         this.pathname = pathname;
     }
 
     @Override
     public void setLength (long length)
     {
         file.setLength(length);
     }
 
     @Override
     public long length ()
     {
         return file.length();
     }
 
     @Override
     public long read (ByteBuffer buf, long size, long offset) 
             throws FileNotFoundException, IOException, NotSupportedException
     {
         return file.read(buf, size, offset);
     }
 
     @Override
     public long write (byte[] buf, long size, long offset) 
             throws NotSupportedException, FileNotFoundException
     {
         return file.write(buf, size, 0);
     }
 
     @Override
     public boolean delete ()
     {
         return file.delete();
     }
 
     @Override
     public boolean mkdir () throws FileExistException
     {
         return file.mkdir();
     }
 
     @Override
     public IumfsFile[] listFiles ()
     {
         return file.listFiles();
     }
 
     @Override
     public long getAtime ()
     {
         return file.getAtime();
     }
 
     @Override
     public void setAtime (long atime)
     {
         file.setAtime(atime);
     }
 
     @Override
     public long getCtime ()
     {
         return file.getCtime();
     }
 
     @Override
     public void setCtime (long ctime)
     {
         file.setCtime(ctime);
     }
 
     @Override
     public long getMtime ()
     {
         return file.getMtime();
     }
 
     @Override
     public void setMtime (long mtime)
     {
         file.setMtime(mtime);
     }
 
     @Override
     public FileType getFileType ()
     {
         return file.getFileType();
     }
 
     @Override
     public long getPermission ()
     {
         return file.getPermission();
     }
 
     @Override
     public boolean isDirectory ()
     {
         return file.isDirectory();
     }
 
     @Override
     public void create () throws IOException
     {
         file.create();
     }
 
     @Override
     public boolean exists () throws SecurityException
     {
         return file.exists();
     }
 
     @Override
     public boolean isFile ()
     {
         return file.isFile();
     }
 
     @Override
     public String getName ()
     {
         return file.getName();
     }
 
     @Override
     public String getPath ()
     {
         return pathname;
     }
 
     @Override
     public void addFile (IumfsFile file) throws NotADirectoryException
     {
         addFile(file);
     }
 
     @Override
     public void getTimeline () throws TwitterException
     {
         file.getTimeline();
     }

    @Override
    public Account getAccount ()
    {
        return file.getAccount();
    }

    @Override
    public void setAccount (Account account)
    {
        file.setAccount(account);
    }

    @Override
    public boolean isTimeline ()
    {
        return file.isTimeline();
    }

    @Override
    public String getUsername ()
    {
        return file.getUsername();
    }

    @Override
    public void setDirectory (boolean directory)
    {
        file.setDirectory(directory);
    }
 }
