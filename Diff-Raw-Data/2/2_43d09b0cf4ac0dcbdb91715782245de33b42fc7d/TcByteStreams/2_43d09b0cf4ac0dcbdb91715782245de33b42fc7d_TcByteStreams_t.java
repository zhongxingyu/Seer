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
 
 import com.google.common.base.Supplier;
 import com.google.common.io.ByteStreams;
 import com.google.common.io.Closeables;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 
 import org.trancecode.concurrent.TcExecutors;
 import org.trancecode.lang.TcThreads;
 
 /**
  * Utility methods related to {@link InputStream} and {@link OutputStream}.
  * 
  * @author Herve Quiroz
  */
 public final class TcByteStreams
 {
     private TcByteStreams()
     {
         // No instantiation
     }
 
     public static long copy(final InputStream in, final OutputStream out, final boolean close)
     {
         try
         {
             final long bytes = ByteStreams.copy(in, out);
             if (close)
             {
                 Closeables.close(in, false);
                 Closeables.close(out, false);
             }
             return bytes;
         }
         catch (final IOException e)
         {
             throw new RuntimeIOException(e);
         }
         finally
         {
             if (close)
             {
                 Closeables.closeQuietly(in);
                 Closeables.closeQuietly(out);
             }
         }
     }
 
     public static Future<Long> concurrentCopy(final InputStream in, final OutputStream out, final boolean close)
     {
         return TcExecutors.concurrentExecute(new Callable<Long>()
         {
             @Override
             public Long call() throws Exception
             {
                 return copy(in, out, close);
             }
         });
     }
 
     public static Supplier<File> copyToTempFile(final InputStream in)
     {
        final File file = Files.createTempFile(TcByteStreams.class);
         final Thread thread = new Thread(new Runnable()
         {
             @Override
             public void run()
             {
                 TcByteStreams.copy(in, Files.newFileOutputStream(file), true);
             }
         });
         thread.start();
         return new Supplier<File>()
         {
             @Override
             public File get()
             {
                 TcThreads.join(thread);
                 return file;
             }
         };
     }
 }
