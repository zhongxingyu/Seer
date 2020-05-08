 package com.raverun.coreutil.api;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.RandomAccessFile;
 import java.nio.charset.Charset;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.Validate;
 
 
 /**
  * API for clients.
  * FileTailer uses a bounded buffer to store lines from a file.
  * <p>
  * Example code:
  * <pre>
  * {@code
  *   FileTailer.Builder builder = new FileTailer.Builder( new File( "filename" ) );
  * builder = builder.numberOfLines( 15 )
  *                  .debug( true );
  * final FileTailer tailer = builder.build();
  * tailer..addObserver( new ContentObserver() { .... } );
  * tailer.turnOn();
  * }
  * </pre>
  *
  * @author Gavin Bong
  */
 public final class FileTailer {
 
     /**
      * Add a listener for receive the lines from the file
      *
      * @param observer - an observer
      * @throws IllegalArgumentException if {@code observer} is null
      */
     public void addObserver( ContentObserver observer ) {
         Validate.notNull( observer, "an observer is mandatory" );
         _observers.add( observer );
     }
 
     /**
      * Not implemented yet
      *
      * @throws UnsupportedOperationException
      */
     public void removeObserver( ContentObserver observer ) {
         throw new UnsupportedOperationException(); 
     }
 
     public void waitInterruptibly() throws InterruptedException {
         if( _flag.get() ) {
             info( TAG_FILETAILER, "joining" );
             if( _producerTask != null )
                 _producerTask.join();
             if( _consumerTask != null )
                 _consumerTask.join();
         }
         info( TAG_FILETAILER, "returning from waitInterruptibly" );
     }
 
     /**
      * Notes: 
      * <ul>
      * <li>this method can be called once</li>
      * <li>thread safe</li>
      * </ul>
      * <p>
      * @throws RuntimeException if unable to start
      */
     public synchronized void turnOn() {
         if( _flag.compareAndSet( false, true) ) {
             info( TAG_FILETAILER, "turning on" );
             _consumerTask = new ObserverNotifierTask();
             _producerTask = new RetrieveLinesTask();
             _executorPool.execute( _producerTask );
             _executorPool.execute( _consumerTask );
         }
     }
 
     /**
      * Notes: 
      * <ul>
      * <li>thread safe
      * </ul>
      */
     public synchronized void turnOff() {
         if( _producerTask != null )
             _producerTask.cancel();
 
         if( _consumerTask != null )
             _consumerTask.cancel();
 
         _executorPool.shutdown();
 
 //        _producerTask.join();
 //        _consumerTask.join();
     }
 
     private void notifyObservers( String line ) {
         if( line != null ) {
             for( ContentObserver anObserver : _observers ) {
                 anObserver.onNewLine( line );
             }
         }
     }
 
     private void notifyObserversFinish( boolean normal, String error ) {
         for( ContentObserver anObserver : _observers ) {
             if( normal )
                 anObserver.onFinishNormal();
             else
                 anObserver.onFinishWithException( error );
         }
     }
 
     /**
      * Consumer of lines & responsible for notifying all observers
      */
     private class ObserverNotifierTask extends Thread {
         private boolean _running = true;
 
         public ObserverNotifierTask() {
             setName( TAG_TASK );
             setDaemon( true );
         }
 
         @Override
         public void run() {
             try {
                 while( _running ) {
                     String line = _queue.take();
                     //FileTailer.info( TAG_TASK, "received -> " + line );
 
                     if( line == POISON_MESSAGE ) {
                         notifyObserversFinish( true, null );
                         Thread.sleep( 100 );
                         break;
                     }
 
                     notifyObservers( line );
                 }
             } 
             catch( InterruptedException e ) {
                 e.printStackTrace();
                 Thread.currentThread().interrupt();
             }
             finally {
                 debug( TAG_TASK, "dying" );
             }
         }
 
         public void cancel() {
             _running = false;
             interrupt();
         }
 
         private final String TAG_TASK = "ObserverNotifierTask";
     }
 
     /**
      * Producer of lines
      */
     private class RetrieveLinesTask extends Thread {
         public RetrieveLinesTask() {
             setName( TAG_TASK );
             setDaemon( true );
         }
     
         @Override
         public void run() {
             try {
                 if( _numberOfLines == 0 ) {
                     debug( TAG_TASK, "user wanted to read 0 lines" );
                     sendPoison();
                     return;
                 }
 
                 if( isEmptyFile() ) {
                     debug( TAG_TASK, "file is empty" );
                     sendPoison();
                     return;
                 }
 
                 Deque<String> lines = readFile();
                 for( String line : lines ) {
                     _queue.put( line );
                     debug( TAG_TASK, "added -> " + line );
                     Thread.sleep( 10 );
                 }
 
                 // NOTE: this is not the poison message (for testing)
                 //_queue.put( "poison" ); 
 
             // --- notify observers that EOF has been reached ---
                 sendPoison();
 
             }
             catch( IOException ioe ) {
                 ioe.printStackTrace();
                 notifyObserversFinish( false, ioe.getMessage() + "" );
             }
             catch( InterruptedException e ) {
                 Thread.currentThread().interrupt();
             }
             catch( Exception e ) {
                 notifyObserversFinish( false, e.getMessage() + "" );
             }
             finally {
                 debug( TAG_TASK, "dying" );
             }
         }
 
         private void sendPoison() throws InterruptedException {
             _queue.put( POISON_MESSAGE );
             info( TAG_TASK, "sent POISON" );
         }
 
         private boolean isEmptyFile() throws IOException {
             RandomAccessFile file = new RandomAccessFile( _file, "r" );
             try {
                 debug( TAG_TASK, "isEmptyFile" );
                 return( file.length() == 0 );
             }
             finally {
                 IOUtils.closeQuietly( file );
             }
         }
 
         private LineReaderResult readFully( RandomAccessFile file, long startFilePointer, int bufferSize ) throws IOException {
             debug( TAG_TASK, "readFully - seek to pointer " + startFilePointer );
             file.seek( startFilePointer );
 
             byte[] b = new byte[bufferSize];
             final int[] read = {0};
 
             read[0] = file.read( b );
             debug( TAG_TASK, "readFully - read bytes " + read[0] );
 
             if( read[0] <= 0 )
                 return new LineReaderResult() {
                     public Deque<String> lines() { return new ArrayDeque<String>(0); }
                     public int numberOfBytesRead() { return 0; }
                 };
 
             BufferedReader reader = new BufferedReader( 
                 new InputStreamReader( 
                    new ByteArrayInputStream( b ), Charset.forName( UTF8 ) 
                 )
             );
 
             final Deque<String> result = new ArrayDeque<String>();
             String line = null;
             while( (line = reader.readLine()) != null ) {
                 result.addLast( line );
                 debug( TAG_TASK, "readFully - line=[ " + line + " ]" );
             }
 
        // push back the top-most line
            if( result.peekFirst() != null && result.size() > 1 ) {
                String first   = result.pollFirst();
                byte[] firstba = first.getBytes( UTF8 );
                if( firstba.length > 0 ) {
                    read[0] = read[0] - (firstba.length + 1);
                }
            }

             return new LineReaderResult() {
                 public Deque<String> lines() { return result; }
                 public int numberOfBytesRead() { return read[0]; }
             };
         }
 
         private Deque<String> readFile() throws IOException {
            final Deque<String> sink = new ArrayDeque<String>();
 
            RandomAccessFile file = new RandomAccessFile( _file, "r" );
            int targetNoOfLines = _numberOfLines;
            debug( TAG_TASK, "readFile: searching for last " + _numberOfLines + " lines" );
 
            try {
                debug( TAG_TASK, "file size=" + file.length() );
                file.seek( file.length() );
                debug( TAG_TASK, "starting filepointer after seek to length=" + file.getFilePointer() );
 
                /**
                 * will keep changing to reflect how far back from the EOF we need to seek to
                 */
                long offset = CAPACITY;
 
                long size = file.length();
                debug( TAG_TASK, "offset" + offset );
 
                while( targetNoOfLines > 0 ) {
                    if( size < offset )
                        offset = size;
 
                    long newPointer = size - (offset);
                    final LineReaderResult result = readFully( file, newPointer, CAPACITY );
                    Deque<String> lines = result.lines();
                    for( String aLine : lines )
                        debug( TAG_TASK, "\tline={ " + aLine + " }" );
 
                    if( targetNoOfLines > 0  ) {
                        if( targetNoOfLines > lines.size() ) {
                        // -- continue to read some more after this
                            Iterator<String> iter = lines.descendingIterator();
                            while( iter.hasNext() )
                                sink.addFirst( iter.next() );
 
                            targetNoOfLines -= lines.size();
                            debug( TAG_TASK, "--->targetNoOfLines=" + targetNoOfLines );
                        } 
                        else {
                        // we have enough
                            Iterator<String> iter = lines.descendingIterator();
                            while( iter.hasNext() ) {
                                debug( TAG_TASK, "\ttargetNoOfLines=" + targetNoOfLines );
                                if( targetNoOfLines == 0 )
                                    break;
 
                                sink.addFirst( iter.next() );
                                targetNoOfLines--;
                            }
                            break;
                        }
                    }
 
                    debug( TAG_TASK, "new filepointer=" + file.getFilePointer() );
 
                    if( offset == size )
                        break;
 
                    offset += result.numberOfBytesRead();
                    debug( TAG_TASK, "===========offset= " + offset + "===================" );
               }//while
 
                return sink;
            }
            finally {
                IOUtils.closeQuietly( file );
            }
         }
 
         public void cancel() {
             interrupt();
         }
 
         private final String TAG_TASK = "RetrieveLinesTask";
     }
 
     static interface LineReaderResult {
         Deque<String> lines();
         int numberOfBytesRead();
     }
 
     /*
      * Junk this soon
      */
     @SuppressWarnings("unused")
     private List<String> readLines( RandomAccessFile file, int offset ) throws IOException {
 //        read_str = f.read(offset)
 //        # Remove newline at the end
 //        if read_str[offset - 1] == '\n':
 //          read_str = read_str[:-1]
 //        lines = read_str.split('\n')
         return null;
     }
 
     private void debug( String what, String s ) {
         if( _debug )
             System.out.println( "[" + what + "] " + s );
     }
 
     private void info( String what, String s ) {
         //System.out.println( "[" + what + "] " + s );
     }
 
     public static class Builder {
         /**
          * @param file - a file
          * @throws IllegalArgumentException if file is null
          * @throws FileNotFoundException if invalid {@code file}
          */
         public Builder( File file ) throws FileNotFoundException {
             Validate.notNull( file, "file must not be null" );
             new RandomAccessFile( file, "r" ); // fail early
             builderFile = file;
         }
 
         /**
          * 
          * @param lines - the number of lines
          * @return the same instance of {@code Builder}
          */
         public Builder numberOfLines( int lines ) {
             if( lines >= 0 )
                 builderNoOfLines = lines;
 
             return this;
         }
 
         /**
          *
          * @param debug - set to true to enable debugging logs
          * @return the same instance of {@code Builder}
          */
         public Builder debug( boolean debug ) {
             builderDebug = debug;
             return this;
         }
 
         /**
          * Currently this is not used internally 
          */
         public Builder follow( boolean shouldFollow ) {
             builderFollow = shouldFollow;
             return this;
         }
 
         public FileTailer build() {
             return new FileTailer( builderFile, builderNoOfLines, builderFollow, builderDebug );
         }
 
         private File builderFile;
         private int builderNoOfLines = 10;
         private boolean builderFollow = false;
         private boolean builderDebug = false;
     }
 
     private FileTailer( File file, int numberOfLines, boolean follow, boolean debug ) {
         _file = file;
         _numberOfLines = numberOfLines;
         _follow = follow;
         _debug = debug;
 
         _queue = new ArrayBlockingQueue<String>( CAPACITY );
         _observers = new ArrayList<ContentObserver>(2);
         _executorPool = Executors.newCachedThreadPool();
     }
 
     private volatile RetrieveLinesTask _producerTask;
     private volatile ObserverNotifierTask _consumerTask;
 
     private final File _file;
     private final int _numberOfLines;
     private final boolean _debug;
 
     /**
      * Not implemented yet
      */
     @SuppressWarnings("unused")
     private final boolean _follow;
 
    private final static String UTF8 = "UTF-8";
     private final static int CAPACITY = 1024;
     private final static String TAG_FILETAILER = "FileTailer";
     private final static String POISON_MESSAGE = new String("poison");
 
     private final ExecutorService _executorPool;
     private final BlockingQueue<String> _queue;
     private final List<ContentObserver> _observers;
     private final AtomicBoolean _flag = new AtomicBoolean( false );
 }
