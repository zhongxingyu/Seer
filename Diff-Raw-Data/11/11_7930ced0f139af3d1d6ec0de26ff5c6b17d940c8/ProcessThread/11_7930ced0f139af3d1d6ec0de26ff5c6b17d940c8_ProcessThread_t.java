 package tinylayeraroundgit.utils;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 
 public class ProcessThread extends Thread {
     
     private ByteArrayOutputStream stdout = new ByteArrayOutputStream();
     private ByteArrayOutputStream stderr = new ByteArrayOutputStream();
     
     private boolean writeOutput = true;
 
     private String commandLine;
     private Process process;
 
     private int exitCode;
 
 	private File path;
 
     public ProcessThread( String commandLine, File path ) {
         this.commandLine = commandLine;
         this.path = path;
        
        correctPath();
     }
     
    private void correctPath() {
    	if( path.isFile() ) {
    		path = path.getParentFile();
    	}
	}

	/**
      * Without this class the process on Windows hangs...
      * 
      * @author Tomo Krajina
      */
     class StreamGobbler extends Thread {
 
         private InputStream is;
 
 		private OutputStream out;
         
         StreamGobbler( InputStream is, OutputStream out ) {
             this.is = is;
             this.out = out;
         }
 
         public void run() {
             try {
                 InputStreamReader isr = new InputStreamReader( is );
                 BufferedReader br = new BufferedReader( isr );
                 String line = null;
                 while ( ( line = br.readLine() ) != null ) {
                     if( writeOutput ) {
                         out.write( line.getBytes() );
                         out.write( '\n' );
                     }
                 }
             }
             catch ( IOException e ) {
             	// TODO
             	e.printStackTrace();
             }
         }
 
     }
 
     @Override
     public void run() {
         try {
             this.process = Runtime.getRuntime().exec( commandLine, null, path );
 
             StreamGobbler outputGobbler = new StreamGobbler( this.process.getInputStream(), this.stdout );
             StreamGobbler errorGobbler = new StreamGobbler( this.process.getErrorStream(), this.stderr );
 
             errorGobbler.start();
             outputGobbler.start();
 
             this.setExitCode( this.process.waitFor() );
         }
         catch ( Exception e ) {
             System.err.println( e.getMessage() );
         }
     }
     
     public Process getProcess() {
         return this.process;
     }
 
     public boolean started() {
         return this.process != null;
     }
 
     public String getStdout() {
     	return new String( this.stdout.toByteArray() );
     }
 
     public String getStderr() {
     	return new String( this.stderr.toByteArray() );
     }
 
     /** Kills the process. */
     public void kill() {
         if( this.process != null ) {
             this.process.destroy();
         }
     }
     
     public int waitToEnd() throws InterruptedException {
     	while( this.process == null ) {
     		Thread.sleep( 100 );
     	}
     	
         this.process.waitFor();
         
         return getExitCode();
     }
     
     public void setWriteOutput( boolean writeOutput ) {
         this.writeOutput = writeOutput;
     }
 
     public boolean isWriteOutput() {
         return writeOutput;
     }
 
     private void setExitCode( int exitValue ) {
         this.exitCode = exitValue;
     }
 
     public int getExitCode() {
         return exitCode;
     }
     
 }
