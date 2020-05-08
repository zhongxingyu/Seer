 package com.robots.MOOS;
 
 //MOOS
 import com.robots.MOOS.JMOOSCommPkt;
 //ANDROID
 //import android.util.Log;
 
 import java.net.Socket;
 import java.nio.channels.SocketChannel;
 import java.net.UnknownHostException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.OutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.Channels;
 import java.nio.channels.WritableByteChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.io.BufferedReader;
 
 import java.net.SocketAddress;
 
 import java.util.*;
 import java.io.*;
 import java.nio.*;
 
 
 
 
 public class JMOOSCommObject
 {
 	public boolean			bVerbose;
     public JMOOSMsg         lastRead;
 
 	private int CHUNK_SIZE  = 1024;	
 	
 
 	public JMOOSCommObject()
 	{
     }
 
 	public boolean SendPkt( JXPCSocket sock, JMOOSCommPkt Pkt )
 	{
 		//Get the bytes required for this packet
 		int bytesRequired = Pkt.getBytesRequired();
 
 		byte[] bytes = Pkt.getBytes();
 			
 		if ( sock.write( bytes, 0, bytes.length ) )
         {
 			return true;
 	    }
 
 		return false;
 	}
 
 	public boolean ReadPkt( JXPCSocket sock, JMOOSCommPkt Pkt )
 	{
 			int bytesRequired = Pkt.getBytesRequired();
 
 			if ( bVerbose )
 					System.out.println( "___" + bytesRequired + " bytesRequired" );
 
 			byte[] bytes = new byte[ bytesRequired ];	
 			sock.read( bytes, 0 , bytesRequired );
 
 			Pkt.fill( bytes, bytes.length );
 			bytes = null;
 
 			if ( bVerbose )	
 					System.out.println( "_x_" + Pkt.getBytesRequired() + " bytesRequired" );
 
 			int required = Pkt.getBytesRequired();
 			int read = 0;
 
 			while ( read != required )
 			{
 					long read_bytes;
 
					bytes = null;

 					if ( required < CHUNK_SIZE )
 					{
 						bytes = new byte[ required ];
 						read_bytes = sock.read( bytes, 0 , required );
 					}
 					else
 					{
 						bytes = new byte[ CHUNK_SIZE ];
 						read_bytes = sock.read( bytes, 0 , CHUNK_SIZE );
 					}
 			
 					Pkt.fill( bytes, bytes.length );
 				
 					read += read_bytes;
 			}
 
 			return true;
 	}
 
 	//Send a single message to the server
 	public boolean SendMsg( JXPCSocket sock, JMOOSMsg msg )
 	{
 		//Create a packet with a single message
 		JMOOSCommPkt Pkt = new JMOOSCommPkt();	
 
 		Vector vList = new Vector();
 		vList.add( msg );
 
 		JMOOSMSG_LIST list = new JMOOSMSG_LIST( vList );
 
 		if ( Pkt.serialize( list, true ) )
 		{
 			if ( SendPkt( sock, Pkt ) )
 			{
 				return true;
 			}	
 		}	
 
 		return false;
 	}
 
 	//Read a single message from the server
 	public boolean ReadMsg( JXPCSocket sock )
 	{
 		//Create a MSG_LIST 
 		JMOOSMSG_LIST list = new JMOOSMSG_LIST();
 		JMOOSCommPkt Pkt = new JMOOSCommPkt();	
 
         if ( ReadPkt( sock, Pkt ) )
         {
             Pkt.serialize( list , false );
    
             //THIS IS REALLY CONFUSING
             list = null;
             list = Pkt.tmp; 
 
             lastRead = list.front();
         }
 		return true;
 	}
 
 
     /*
      *Helper Functions	
      */
 	public static int byteArrayToInt( byte[] b )
 	{
 		assert ( b.length == 4 );
 
 		long l = (long)b[0] & 0xff;
 		l += ((long)b[1] & 0xff) << 8;	
 		l += ((long)b[2] & 0xff) << 16;	
 		l += ((long)b[3] & 0xff) << 24;	
 
 		return (int)l;
 	}
 
 }
 
