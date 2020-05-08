 /*
  * Copyright (C) 2010 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.io;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Utility methods related to {@link File}.
  * 
  * @author Herve Quiroz
  */
 public final class Files
 {
     private Files()
     {
         // No instantiation
     }
 
     public static FileInputStream newFileInputStream(final File file)
     {
         try
         {
             return new FileInputStream(file);
         }
         catch (final FileNotFoundException e)
         {
             throw new IllegalStateException(file.toString(), e);
         }
     }
 
     public static FileOutputStream newFileOutputStream(final File file)
     {
         try
         {
             return new FileOutputStream(file);
         }
         catch (final FileNotFoundException e)
         {
             throw new IllegalStateException(file.toString(), e);
         }
     }
 
     public static List<File> listDirectories(final File directory)
     {
         Preconditions.checkNotNull(directory);
         return ImmutableList.copyOf(directory.listFiles(FileFilters.isDirectory()));
     }
 
     public static File createTempFile(final Object caller)
     {
         return createTempFile(caller.getClass());
     }
 
    public static File createTempFile(final Class<?> callerClass)
     {
         try
         {
             return File.createTempFile(callerClass.getSimpleName(), "");
         }
         catch (final IOException e)
         {
             throw new IllegalStateException(e);
         }
     }
 }
