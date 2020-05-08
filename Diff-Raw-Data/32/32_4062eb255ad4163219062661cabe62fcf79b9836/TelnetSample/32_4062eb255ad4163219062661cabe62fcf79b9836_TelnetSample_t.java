 package com.mettadore.lightdroid;
 
 import org.apache.commons.net.telnet.*;
 import java.io.*;
import java.net.InetAddress;
 
 public class TelnetSample
 {
   private TelnetClient telnet = new TelnetClient();
   private InputStream in;
   private PrintStream out;
   private char prompt = '>';
 
  public TelnetSample( InetAddress server) {
    try {
 	 // Connect to the specified server
	 telnet.connect( server );
 	 
 	 // Get input and output stream references
 	 in = telnet.getInputStream();
 	 out = new PrintStream( telnet.getOutputStream() );
 
 	 // Advance to a prompt
 //	 readUntil( prompt + " " );
    }
   catch( IOException e ) {
 	 e.printStackTrace();
    }
   }
 
   public String readUntil( String pattern ) {
    try {
 	 char lastChar = pattern.charAt( pattern.length() - 1 );
 	 StringBuffer sb = new StringBuffer();
 	 char ch = ( char )in.read();
 	 while( true ) {
 	  System.out.print( ch );
 	  sb.append( ch );
 	  if( ch == lastChar ) {
 	    if( sb.toString().endsWith( pattern ) ) {
 		 return sb.toString();
 	    }
 	  }
 	  ch = ( char )in.read();
 	 }
    }
    catch( Exception e ) {
 	 e.printStackTrace();
    }
    return null;
   }
 
   public void write( String value ) {
    try {
 	 out.println( value );
 	 out.flush();
 	 System.out.println( value );
    }
    catch( Exception e ) {
 	 e.printStackTrace();
    }
   }
 
   public void sendCommand( String command ) {
    try {
 	 write( command );
 //	 return readUntil( prompt + " " );
    }
    catch( Exception e ) {
 	 e.printStackTrace();
    }
 //   return null;
   }
 
   public void disconnect() {
    try {
 	 telnet.disconnect();
    }
    catch( Exception e ) {
 	 e.printStackTrace();
    }
   }
 /*
   public static void main( String[] args ) {
    try {
 	 TelnetSample telnet = new TelnetSample( "192.168.2.9", 3100);
 	 telnet.sendCommand( "1-36 @ 0" );
    }
    catch( Exception e ) {
 	 e.printStackTrace();
    }
   }*/
 }
