 /*
  * Copyright (C) 2012 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jvss.git;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  *
  */
 public class IoUtil
 {
    public static boolean move(String source, String destination)
    {
       // File (or directory) to be moved
      File file = new File(source);
 
       // Destination directory
      File dir = new File(destination);
 
       // Move file to new directory
      return file.renameTo(new File(dir, file.getName()));
 
    }
 
    public static boolean isExists(String path)
    {
       return new File(path).exists();
    }
 
    public static void delete(String path)
    {
       delete(new File(path));
    }
 
    public static void writeStream(InputStream inputStream, OutputStream out)
    {
       // write the inputStream to a FileOutputStream
       try
       {
 
          int read = 0;
          byte[] bytes = new byte[1024];
 
          while ((read = inputStream.read(bytes)) > 0)
          {
             out.write(bytes, 0, read);
          }
 
          inputStream.close();
          out.flush();
          out.close();
       }
       catch (FileNotFoundException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
 
    public static void writeStream(InputStream inputStream, String path)
    {
 
       try
       {
          File folder = new File(path).getParentFile();
          if (!folder.exists())
          {
             folder.mkdirs();
          }
          OutputStream out = new FileOutputStream(new File(path));
          writeStream(inputStream, out);
       }
       catch (FileNotFoundException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
       catch (IOException e)
       {
          // TODO Auto-generated catch block
          e.printStackTrace();
       }
    }
 
    public static void delete(File f)
    {
       if (f.exists())
       {
          if (f.isDirectory())
          {
             for (File c : f.listFiles())
             {
                delete(c);
             }
          }
          if (!f.delete())
          {
             throw new RuntimeException("Failed to delete file: " + f);
          }
       }
    }
 }
