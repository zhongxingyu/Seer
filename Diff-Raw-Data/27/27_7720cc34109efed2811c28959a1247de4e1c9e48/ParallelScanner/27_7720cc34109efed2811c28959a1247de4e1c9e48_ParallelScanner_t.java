 /*******************************************************************************
  * Copyright (c) 2010-2011 Sonatype, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution.
  * The Eclipse Public License is available at
  *   http://www.eclipse.org/legal/epl-v10.html
  * The Apache License v2.0 is available at
  *   http://www.apache.org/licenses/LICENSE-2.0.html
  * You may elect to redistribute this code under either of these licenses.
  *******************************************************************************/
 package org.sonatype.sisu.resource.scanner.scanners;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.inject.Singleton;
 
 import org.sonatype.inject.Nullable;
 import org.sonatype.sisu.resource.scanner.Listener;
 import org.sonatype.sisu.resource.scanner.Scanner;
 
 @Named( "parallel" )
 @Singleton
 public class ParallelScanner
     implements Scanner
 {
     private final ExecutorService executor;
 
     private final Semaphore sem = new Semaphore( 1 );
 
     private final AtomicInteger count = new AtomicInteger();
 
     private final ParallelisationStrategy parallelisationStrategy;
 
     public ParallelScanner( @Named( "${scanner.parallel.threads}" ) int threads )
     {
         this( threads, EVERY_DIRECTORY );
     }
 
     @Inject
     public ParallelScanner( @Named( "${scanner.parallel.threads}" ) int threads,
                             @Nullable ParallelisationStrategy parallelisationStrategy )
     {
         this.parallelisationStrategy = parallelisationStrategy == null ? EVERY_DIRECTORY : parallelisationStrategy;
         this.executor = Executors.newFixedThreadPool( threads );
     }
 
     public void scan( File directory, Listener listener )
     {
         scan( directory, listener, null );
     }
 
     public void scan( File directory, Listener listener, FileFilter filter )
     {
         if ( listener == null )
         {
             return;
         }
         try
         {
             listener.onBegin();
             if ( directory.exists() )
             {
                 sem.acquire();
                 recurse( directory, listener, filter );
                 sem.acquire();
             }
             listener.onEnd();
             executor.shutdown();
         }
         catch ( InterruptedException e )
         {
             e.printStackTrace();
         }
     }
 
     private void recurse( File directory, final Listener listener, final FileFilter filter )
     {
         listener.onEnterDirectory( directory );
         File[] files = filter == null ? directory.listFiles() : directory.listFiles( filter );
         if ( files != null )
         {
             for ( final File file : files )
             {
                 if ( file.isDirectory() )
                 {
                     if ( parallelisationStrategy.shouldScanInParallel( file ) )
                     {
                         count.incrementAndGet();
                         executor.submit( new Runnable()
                         {
                             public void run()
                             {
                                 ParallelScanner.this.recurse( file, listener, filter );
                             }
                         } );
                     }
                     else
                     {
                         recurse( file, listener, filter );
                     }
                 }
                 else
                 {
                     listener.onFile( file );
                 }
             }
         }
         listener.onExitDirectory( directory );
 
         if ( count.decrementAndGet() < 0 )
         {
             sem.release();
         }
     }
 
     public void close()
     {
         executor.shutdown();
     }
 
     public static interface ParallelisationStrategy
     {
         boolean shouldScanInParallel( File directory );
     }
 
     public static ParallelisationStrategy EVERY_DIRECTORY = new ParallelisationStrategy()
     {
 
         public boolean shouldScanInParallel( File directory )
         {
             return true;
         }
 
     };
 
     public static ParallelisationStrategy NEVER = new ParallelisationStrategy()
     {
 
         public boolean shouldScanInParallel( File directory )
         {
            return false;
         }
 
     };
 
 }
