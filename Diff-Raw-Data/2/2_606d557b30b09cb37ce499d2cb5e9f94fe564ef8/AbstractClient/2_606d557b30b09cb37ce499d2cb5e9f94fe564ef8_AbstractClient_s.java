 package net.praqma.ccanalyzer;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import net.praqma.monkit.MonKit;
 
 public abstract class AbstractClient {
 
     protected int port;
     protected String host;
     protected String clientName;
     protected MonKit monkit;
     
     public AbstractClient( int port, String host, String clientName, MonKit mk ) {
         this.port = port;
         this.host = host;
         this.clientName = clientName;
         this.monkit = mk;
         
         System.out.println( "CCAnalyzer client version " + Server.version );
     }
         
 
     public void start( ConfigurationReader counters ) throws IOException {
         Socket socket = null;
         PrintWriter out = null;
         BufferedReader in = null;
         
         System.out.print( "Trying to connect to " + host );
 
         try {
             socket = new Socket( host, port );
             out = new PrintWriter( socket.getOutputStream(), true );
 
         } catch( UnknownHostException e ) {
             System.out.println( "\rError, unkown host " + host + "\n" );
             return;
         } catch( IOException e ) {
             System.out.println( "\rError, unable to connect to " + host + "\n" );
             return;
         }
 
         in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
 
         String line = "";
 
         /* Super simple handshaking.... */
         out.println( "version " + Server.version );
         while( ( line = in.readLine() ) != null ) {
             break;
         }
        if( line.equals( Server.version ) ) {
             System.out.println( "\rError, version mismatch at " + host + "\n" );
             throw new CCAnalyzerException( "Version mismatch, got " + line + " expected " + Server.version );
         }
         
         System.out.println( "\rSuccessfully connected to " + host );
         
         /* Do the counting */
         perform( counters, out, in );
         
         out.println( "exit" );
 
         out.close();
         in.close();
 
         socket.close();
         
         System.out.println( "Disconnected\n" );
     }
     
     protected abstract void perform( ConfigurationReader counters, PrintWriter out, BufferedReader in ) throws IOException;
 
 }
