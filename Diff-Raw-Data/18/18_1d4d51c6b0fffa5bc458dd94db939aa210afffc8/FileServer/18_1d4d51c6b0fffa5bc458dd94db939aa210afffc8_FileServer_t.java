 /**
  * Has a listing of files that must be processed.  It will send these files
  * out to any client that connects, and wait for a returned file from the
  * client.
  */
 
 package parallel.cluster;
 
 import java.util.*;
 import java.util.concurrent.*;
 import java.io.*;
 import java.net.*;
 
 public class FileServer {
     // begin constants
     public static final int DEFAULT_PORT = 8675;
     // end constants
 
     // begin instance variables
     private final Queue< String > files;
     private final Set< String > inFlight;
     private final Set< String > complete;
     private final ServerSocket server;
     private final ToOutputFile toOutputFile;
     // end instance variables
 
     public FileServer( Collection< String > toProcess,
 		       ToOutputFile toOutputFile ) throws IOException {
 	this.toOutputFile = toOutputFile;
 	files = new ConcurrentLinkedQueue< String >( toProcess );
 	inFlight = Collections.synchronizedSet( new HashSet< String >() ); 
 	complete = Collections.synchronizedSet( new HashSet< String >() );
 	server = new ServerSocket( DEFAULT_PORT );
     }
 
     public void mainLoop() {
 	while( true ) {
 	    accept();
 	}
     }
 	    
     public void accept() {
 	try {
 	    new FileServerThread( this, 
 				  server.accept(),
 				  toOutputFile ).start();
 	} catch ( SocketException e ) {
 	    // we are terminating - this is normal
 	} catch ( IOException e ) {
 	    e.printStackTrace();
	    System.err.println( "Client failed to finish making the initial " +
				"connection." );
 	}
     }
 
     // if we don't have any files left, it only means that
     // all messages have been taken.  Clients could have gone
     // down or be straggling. It's possible that the same file could
     // be analyzed twice, which is ok given that analysis should
     // be idempotent
     protected synchronized void resetInFlight() {
 	files.addAll( inFlight );
     }
 
     public synchronized void checkDone() throws IOException {
 	if ( isDone() ) {
 	    server.close(); // makes server throw a SocketException to interrupt mainLoop
 	}
     }
 
     public synchronized boolean isDone() {
 	return files.isEmpty() && inFlight.isEmpty();
     }
 
     // called when a file has been finished being processed
     public synchronized void fileComplete( String file ) {
 	inFlight.remove( file );
 	complete.add( file );
     }
 
     // tries to get the next file.  Returns one of the following:
     // -NO_MORE_FILES_MSG if we are out of files
     // -null if we should call it again (we tried to give a file that
     //  we know has already been processed)
     // -filename otherwise
     protected synchronized String tryNextFile() {
 	String retval = files.poll();
 	if ( retval == null ) {
 	    resetInFlight();
 	    retval = files.poll();
 	}
 
 	if ( retval == null ) {
 	    // we can't grab any more files - we are done
 	    retval = FileTransfer.NO_MORE_FILES_MSG;
 	} else if ( complete.contains( retval ) ) {
 	    // processed before
 	    retval = null;
 	}
 
 	return retval;
     }
 
     // returns a filename or NO_MORE_FILES_MSG
     public synchronized String nextFile() {
 	String retval;
 	do {
 	    retval = tryNextFile();
 	} while ( retval == null );
 	return retval;
     }
 
     // takes one optional parameter: "temp" to make temporary output
     // files. Useful for debugging
    public static void main( String[] args ) {
 	ToOutputFile toOutputFile =
 	    ( args.length == 1 && args[ 0 ].equals( "temp" ) ) ?
 	    new MakeTempFile() :
 	    new MakePostfixFile();
	try {
	    new FileServer( Arrays.asList( args ),
			    toOutputFile ).mainLoop();
	} catch ( IOException e ) {
	    e.printStackTrace();
	    System.err.println( "Couldn't start up server." );
	}
     }
 }
 
