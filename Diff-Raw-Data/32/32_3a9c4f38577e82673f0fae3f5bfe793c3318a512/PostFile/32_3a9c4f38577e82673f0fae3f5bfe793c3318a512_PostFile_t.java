 /*
  * Copyright 2010 Kazuyoshi Aizawa
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cafeform.iumfs.twitterfs.files;
 
 import com.cafeform.iumfs.IumfsFile;
 import com.cafeform.iumfs.NotADirectoryException;
 import com.cafeform.iumfs.twitterfs.Account;
 import com.cafeform.iumfs.twitterfs.TwitterFactoryAdapter;
 import com.cafeform.iumfs.twitterfs.MessageSeparator;
 import java.io.FileNotFoundException;
 import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
 import twitter4j.Status;
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 
 public class PostFile extends TwitterFsFile 
 {
     protected static final int MAX_LENGTH = 140;
     private String prefix;
 
     public PostFile (Account account, String pathname)
     {
         this(account, pathname, "");
     }
     
     public PostFile (Account account, String pathname, String prefix)
     {
         super(account, pathname);
         this.prefix = prefix;
     }
 
     @Override
     public long getPermission() 
     {
         return (long) 0100666; // can write
     }
 
     @Override
     public long read (ByteBuffer buf, long size, long offset) 
     {
         /* Nothing can be read from this file. */
         return 0;
     }
     
     @Override
     public long write (byte[] buf, long size, long offset)
             throws FileNotFoundException 
     {
         try 
         {
             Status status = null;
             Twitter twitter = TwitterFactoryAdapter.getInstance(getUsername());            
 
             /*
              * Get a strings written as a file data.
              */
             String wholeMessage = new String(buf);
             logger.finer ("Orig Text:" + wholeMessage);
             logger.finest ("whole_msg.length() = " + wholeMessage.length());
             int left = wholeMessage.length();
             /*
              * Post strings to twitter every 140 characters.
              */
             MessageSeparator sep = new MessageSeparator(
                     wholeMessage, 
                     prefix);
             while (sep.hasNext()) 
             {
                 String msg = (String) sep.next();
 
                 status = twitter.updateStatus(msg);
                 logger.finest("Text: " + msg);
                 logger.fine("Status updated");
             }
            Date now = new Date();
            setAtime(now.getTime());            
            setMtime(now.getTime());
            setCtime(now.getTime());            
             return 0;
         } 
         catch (TwitterException ex) 
         {
            if (ex.getErrorCode() == 187)
            {
                // Code 187: Status is a duplicate.
                // The message seems to have written already.
                logger.log(Level.INFO, "Status is a duplicate. Ignored.");
                return 0;
            }
             /* 
              * Convert TwitterException to FileNotFoundException 
              */
            logger.log(Level.SEVERE, "TwitterException when writing", ex);
             throw new FileNotFoundException();
         } 
     }    
 
     @Override
     public void addFile (IumfsFile file) throws NotADirectoryException
     {
         throw new NotADirectoryException();
     }
 
     @Override
     public IumfsFile[] listFiles ()
     {
         throw new NotADirectoryException();
     }
 }
