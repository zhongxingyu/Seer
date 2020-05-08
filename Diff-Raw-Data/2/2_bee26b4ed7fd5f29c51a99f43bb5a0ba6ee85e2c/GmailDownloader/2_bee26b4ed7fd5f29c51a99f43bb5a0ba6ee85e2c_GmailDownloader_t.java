 /*
  * GmailDownloader.java
  * (c) staktrace systems, 2009.
  * This code is BSD-licensed.
  */
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 import javax.net.*;
 import javax.net.ssl.*;
 
 public class GmailDownloader extends ImapDownloader {
     protected void run( boolean delete, int startIx, int endIx ) throws IOException {
        super.run( "imap.gmail.com", 993, delete, "\"[Gmail]/All Mail\"", startIx, endIx );
     }
 
     @Override protected void deleteMessages( int start, int end ) throws IOException {
         scanForExists( imapSend( "COPY " + start + ":" + end + " \"[Gmail]/Trash\"" ) );
         System.out.println( "Deletion completed." );
     }
 
     public static void usage( PrintStream out ) {
         out.println( "Usage: java GmailDownloader [-s <start>] [-e <end>]" );
         out.println( "           This will open a secure socket to GMail and prompt you for credentials." );
         out.println( "           The credentials will be used to authenticate the IMAP connection, and then all the" );
         out.println( "           messages in the specified range will be downloaded as RFC822-encoded messages and saved" );
         out.println( "           to plaintext files." );
         out.println();
         out.println( "       java GmailDownloader -d [-s <start>] [-e <end>]" );
         out.println( "           This will open a secure socket to GMail and prompt you for credentials." );
         out.println( "           The credentials will be used to authenticate the IMAP connection, and then all the" );
         out.println( "           messages in the specified range will be DELETED!" );
         out.println();
     }
 
     public static void main( String[] args ) throws IOException {
         int startIx = -1;
         int endIx = -1;
         boolean delete = false;
 
         try {
             for (int argIx = 0; argIx < args.length; argIx++) {
                 if (args[ argIx ].equals( "-s" )) {
                     startIx = Integer.parseInt( args[ ++argIx ] );
                 } else if (args[ argIx ].equals( "-e" )) {
                     endIx = Integer.parseInt( args[ ++argIx ] );
                 } else if (args[ argIx ].equals( "-d" )) {
                     delete = true;
                 } else {
                     throw new IllegalArgumentException();
                 }
             }
         } catch (Exception e) {
             usage( System.out );
             return;
         }
 
         GmailDownloader downloader = new GmailDownloader();
         downloader.run( delete, startIx, endIx );
     }
 }
