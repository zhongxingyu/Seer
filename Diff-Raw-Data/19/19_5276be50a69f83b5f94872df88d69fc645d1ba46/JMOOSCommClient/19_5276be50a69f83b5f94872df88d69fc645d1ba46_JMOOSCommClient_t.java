 package com.robots.MOOS;
 
 import java.net.*;
 import java.nio.*;
 import java.io.*;
 
 import java.util.Vector;
 import java.util.Iterator;
 
 import com.robots.MOOS.JMOOSMsg;
 import com.robots.MOOS.JXPCSocket;
 
 class heartbeat implements Runnable
 {
     
     public void run()
     {
 
 
     }
 
 }
 
 
 public class JMOOSCommClient extends JMOOSCommObject
 {
 	//Core data types
 	final char	MOOS_NOTIFY 	='N';
 	final char	MOOS_REGISTER 	='R';
 	final char	MOOS_UNREGISTER ='U';
 	final char	MOOS_NOT_SET 	='~';
 	final char	MOOS_COMMAND 	='C';
 	final char	MOOS_ANONYMOUS 	='A';
 	final char	MOOS_NULL_MSG 	='.';
 	final char	MOOS_DATA 	    ='i';
 	final char	MOOS_POISON  	='K';
 	final char	MOOS_WELCOME 	='W';
 	final char	MOOS_SERVER_REQUEST = 'Q';
 
 	//MESSAGE DATA TYPES
 	final char 	MOOS_DOUBLE 	='D';
 	final char 	MOOS_STRING    	='S';
 	
 	private	JXPCSocket	        _socket;
 	private int		            _port;		
 	private String		        _hostname;		
 	private JMOOSMSG_LIST       mOutbox;
 	private JMOOSMSG_LIST       mInbox;
 	private String		        mName;
     
     private Vector<String>      mPublishing;
     private Vector<String>      mSubscribing;
 
 
 	private boolean 	bIsConnected;
 	public boolean		bVerbose;
 
     private final int      MAX_INBOX_MESSAGES      =1000;
     private final int      MAX_OUTBOX_MESSAGES     =1000;
 
 
 
 	public static void main( String[] args )
 	{
 		JMOOSCommClient j = new JMOOSCommClient();
 
 		j.bVerbose	= true;
 		
 		j.Run( "localhost", 9000 );
 
 	}
 
 	public JMOOSCommClient()
 	{
 		//Initialise mOutbox
 		mOutbox = new JMOOSMSG_LIST();
 	
         mPublishing = new Vector(); 
         mSubscribing = new Vector(); 
 
 		mName = "testApp";
 	}
 	static
 	{
 		System.loadLibrary("MOOS");
 	}
 
 	public boolean Run( String hostname, int port )
 	{
 
 		_hostname = hostname;
 		_port = port;
 
 		if ( !ConnectToServer() )
 			return false;
 
 		if ( bVerbose )
 		{
 			System.out.println("Connected to server!" );
 			System.out.println("Handshaking...." );
 		}
 
 		if ( Handshake() )
 		{
 			//Add in the CTRL-C hook
 		 	//Runtim.getRuntime().addShutdownHook(new RunWhenShuttingDown());
 			
             Register( "DB_TIME", 1.0 );
             Register( "Heading", 1.0 );
         
 			//Run for X seconds
 			long init = System.currentTimeMillis();
 
             while ( ( System.currentTimeMillis() - init ) < 300000 )
 			{
 				JMOOSMsg msg = new JMOOSMsg( MOOS_DATA, "", mName, 1.0 );
 				mOutbox.add( msg );
 
 				try
 				{
 					Thread.sleep( 1000 );
 					
                     //Send the whole mOutbox
                     JMOOSCommPkt PktTx = new JMOOSCommPkt();
                     PktTx.serialize( mOutbox, true );
                     SendPkt( _socket , PktTx );
 
                     JMOOSCommPkt PktRx = new JMOOSCommPkt();
                     ReadPkt( _socket, PktRx );
                     JMOOSMSG_LIST list = new JMOOSMSG_LIST();
                     
                     PktRx.serialize( list, false );
                     mInbox = PktRx.tmp;
                     PktRx = null;
 
                     //TESTING
                    System.out.println( "------------------------------" );
                     //System.out.println( mInbox.front().Trace() );
                     //System.out.println( mInbox.front().GetTime() );
                     //System.out.println( mInbox.front().GetDouble() );
                     //System.out.println( mInbox.front().GetKey() );
                     //System.out.println( mInbox.front().GetString() );
                     mInbox.Trace();
 
                     if ( bVerbose )	
 						System.out.println( System.currentTimeMillis() );
                     
                     
                     //DisconnectFromServer();
                 }
 				catch( InterruptedException e )
 				{
 					System.err.println( e );
 				}
 			}
 		
 			return true;
 		}
 		else
 			return false;
 
 	}
 
 	public boolean ConnectToServer()
 	{
 		if ( bIsConnected )
 		{
 			System.err.println( "Client is already connected!\n" );
 			return false;
 		}
 
 		_socket = new JXPCSocket( _hostname, _port );
 
 		//Try to connect
 		if ( !_socket.Connect() )
 		{
 			System.err.println( "Unable to connect to " + _hostname + " @" + _port );
 			return false;
 		}
 
         bIsConnected = true;
 
 		return true;
 		
 	}
 
     public boolean DisconnectFromServer()
     {
         if ( !bIsConnected )
         {
             System.err.println( "Client is NOT connected!" );
             return false;
         }
 
         if ( _socket.isOpen() ) 
         {
             if ( _socket.Disconnect() )
             {
                 return true;
             }
         }
     
         return false;
     }
 
 
 	public boolean Handshake()
 	{
 
 		//Send a blank message
 		JMOOSMsg smsg = new JMOOSMsg( MOOS_DATA, "", mName, 1.0 );
 		SendMsg( _socket, smsg );
 	
 		//Receive a blank message
         ReadMsg( _socket );
         JMOOSMsg rmsg = lastRead;
 
         if ( rmsg.IsType( "W" )  )
         {
             return true;
         }
         else
         {
 			System.err.println( "HANDSHAKING INCOMPLETE" );
             return false;
         }
 	}
 
 	//Functions to implement
 	//boolean 	Notify( String var, String val, long time )
 
 	boolean	Register( String var, double interval )
 	{
 		JMOOSMsg MsgR = new JMOOSMsg( MOOS_REGISTER, var, 2.0, 1.0 ); 	
 
         mSubscribing.add( var );
 
 		Post( MsgR );
 
 		return true;
 	}
 
 	boolean	Unregister( String var )
 	{
         //Check if we are subscribing to it
         Iterator it = mSubscribing.iterator();
         while ( it.hasNext() )
         {
             if ( var == it.next() )
             {
                 JMOOSMsg MsgUR = new JMOOSMsg( MOOS_UNREGISTER, var, 0.0, 0.0 ); 	
 		        Post( MsgUR );
 				return true;
             }
 
         }
 
         return false;
 
 	}
 
     //----------------------------------------------------------
 
 	//boolean	Fetch( MOOSMSG_LIST )
 	boolean	Post( JMOOSMsg msg )
 	{
         if ( mOutbox.size() > MAX_OUTBOX_MESSAGES )
         {
             System.err.println( "OUTBOX IS OVERFLOWING" );
             if ( bVerbose )
                 System.out.println( "mOutbox size is " + mOutbox.size() );
         }
 
         //Set the client name
         msg.setClientName( mName );
        
         if ( mOutbox.add( msg ) )
             return true;
         else
             return false;
 	}
 	//----------------------------------------------------------
 	
 	//boolean	SetOnConnectCallback()
 	//boolean	SetOnDisconnectCallback()
 
 }
 
 
