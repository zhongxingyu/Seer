 /*
  * Copyright 2011 Josh Beitelspacher
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
 
 package com.netbeetle.reboot.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class ZipFileSystem implements RebootFileSystem
 {
     private final ZipFile zipFile;
 
     private class ZipDirectory extends RebootDirectory
     {
         public ZipDirectory(String name)
         {
             super(name);
         }
 
         @Override
         public Collection<RebootFile> list(boolean recursive)
         {
             List<RebootFile> contents = new ArrayList<RebootFile>();
             Enumeration<? extends ZipEntry> entries = zipFile.entries();
             while (entries.hasMoreElements())
             {
                 ZipEntry entry = entries.nextElement();
                 String name = entry.getName();
                 if (name.startsWith(getName()) && name.length() > getName().length())
                 {
                     if (recursive || name.indexOf('/', getName().length()) == -1)
                     {
                         if (entry.isDirectory())
                         {
                             contents.add(new ZipDirectory(entry.getName()));
                         }
                         else
                         {
                             contents.add(new ZipEntryFile(entry));
                         }
                     }
                 }
             }
             return contents;
         }
     }
 
     private class ZipEntryFile extends RebootStreamFile
     {
         private final ZipEntry entry;
 
         public ZipEntryFile(ZipEntry entry)
         {
             super(entry.getName());
             this.entry = entry;
         }
 
         @Override
         public InputStream getInputStream() throws IOException
         {
             return zipFile.getInputStream(entry);
         }
 
         @Override
         public long getSize()
         {
             return entry.getSize();
         }
     }
 
     public ZipFileSystem(ZipFile zipFile)
     {
         this.zipFile = zipFile;
     }
 
     @Override
     public RebootFile getFile(String name)
     {
         if (name.isEmpty())
         {
             return new ZipDirectory(name);
         }
 
         ZipEntry entry = zipFile.getEntry(name);
         if (entry == null)
         {
            // some zip files don't contain directory entries, so look for files
            // inside the directory instead
            if (name.endsWith("/"))
            {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements())
                {
                    ZipEntry nextEntry = entries.nextElement();
                    String nextName = nextEntry.getName();
                    if (nextName.endsWith("/"))
                    {
                        // the zip file contain directories, don't bother
                        // scanning any further through it to find the directory
                        // in question

                        // this may fail to find directories inside zip files
                        // that use directory entries inconsistently, but
                        // preventing long scans through valid zip files is more
                        // important than handling inconsistent zip files
                        break;
                    }
                    else if (nextName.startsWith(name))
                    {
                        // found a file inside the directory
                        return new ZipDirectory(name);
                    }
                }
            }
             return null;
         }
         else if (entry.isDirectory())
         {
             return new ZipDirectory(name);
         }
         else
         {
             return new ZipEntryFile(entry);
         }
     }
 
     @Override
     public String fingerprint() throws IOException
     {
         try
         {
             return HashUtil.hash(new File(zipFile.getName()));
         }
         catch (NoSuchAlgorithmException e)
         {
             throw new IOException("Unable to compute hash", e);
         }
     }
 }
