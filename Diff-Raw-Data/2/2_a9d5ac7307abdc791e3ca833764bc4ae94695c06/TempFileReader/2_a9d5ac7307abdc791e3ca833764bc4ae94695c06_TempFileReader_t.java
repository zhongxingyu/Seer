 
 package com.randomhumans.svnindex.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /** An input stream reader that attempts to delete the file on close */
 
 public class TempFileReader extends InputStreamReader
 {
     File file;
 
     public TempFileReader(final File in) throws FileNotFoundException
     {
         super(new FileInputStream(in));
         this.file = in;
     }
 
     @Override
     public void close() throws IOException
     {
         try
         {
             super.close();
         }
         finally
        {  //best effort attempt to ensure the file is deleted.
             if (!this.file.delete())
             {
                 this.file.deleteOnExit();
             }
         }
     }
 
 }
