 package com.robots.MOOS;
 
 import java.net.*;
 import java.nio.*;
 import java.io.*;
 
 import java.util.Vector;
 import java.util.Iterator;
 
 import com.robots.MOOS.JMOOSMsg;
 import com.robots.MOOS.JXPCSocket;
 import com.robots.MOOS.JMOOSGlobalHelper;
 
 
 public class JMOOSCommClient extends JMOOSCommObject implements Runnable
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
 	
 	private JMOOSGlobalHelper 	helper;
 	private	JXPCSocket	        _socket;
 	private int		            _port;		
 	private String		        _hostname;		
 	private JMOOSMSG_LIST       mOutbox;
 	private JMOOSMSG_LIST       mInbox;
 	private String		        mName;
   	private int					mReadMessages; 
 
     private Vector<String>      mPublishing;
     private Vector<String>      mSubscribing;
 
     private Thread      mThread;
 
 	private boolean 	bIsConnected;
 	public boolean		bVerbose;
     public boolean      bActive;
 
     private final int       MAX_INBOX_MESSAGES      = 1000;
     private final int       MAX_OUTBOX_MESSAGES     = 1000;
 
     private double          mFundamentalFrequency   = 10;
 
 
 
     //----------------------------------------------------------
 	public static void main( String[] args )
 	{
 		//For different android versions, we need to load up an instance of 
         //the bytebuffer for it to be stored in memory. Article related to 
         //this is referenced in the svn commit note.
 		
         ByteBuffer dummy = ByteBuffer.allocate( 0 );
 
 		JMOOSCommClient j = new JMOOSCommClient("localhost", 9000 );
 	}
 
     //----------------------------------------------------------
 	public JMOOSCommClient( String hostname, int port )
 	{
     
         _hostname = hostname;
 		_port = port;
 
         //Set up the thread
         mThread = new Thread( this );
         mThread.start();
 
 		//Initialise mOutbox
 		mOutbox = new JMOOSMSG_LIST();
 	
         mPublishing = new Vector(); 
         mSubscribing = new Vector(); 
 
 		mName = "testApp";
         bActive = true;
 	
 		helper = new JMOOSGlobalHelper();
 	}
 	static
 	{
 		System.loadLibrary("MOOS");
 	}
     
     //----------------------------------------------------------
 	public void run()
 	{
         if ( !ConnectToServer() )
             return;
 
 		if ( bVerbose )
 		{
 			System.out.println("Connected to server!" );
 			System.out.println("Handshaking...." );
 		}
 
 		if ( Handshake() )
 		{
 			//Add in the CTRL-C hook
 		 	//Runtim.getRuntime().addShutdownHook(new RunWhenShuttingDown());
 		
 			//Registrations
             //Register( "DB_TIME", 1.0 );
             //Register( "Heading", 1.0 );
 			//Register( "PUBLISH_DATA", 40.0 );
 			//Register( "PUBLISH_DATA", 10*mFundamentalFrequency );
 			Register( "PUBLISH_DATA", 0);
         
             mReadMessages = 0;
             
 			while ( bActive )
             {
 				//Test outbox size, add a blank message if we need to	
 				JMOOSMsg msg = new JMOOSMsg( MOOS_DATA, "", mName, 1.0 );
 				mOutbox.add( msg );
 
 				try
 				{
 					Thread.sleep( (int)Math.floor(1000/mFundamentalFrequency));
 				    if ( bActive )
                     {
                         Iterate();
                     } 
                     else
                     {
                         System.err.println( "Disconnected from the database..." );
                         break;
                     }
                     
                 }
                 catch( InterruptedException e )
 				{
 					System.err.println( e );
 				}
 			}
 	        
             DisconnectFromServer();
 
 			return;
 		}
 		else
 			return;
 
 	}
 
     //----------------------------------------------------------
     public boolean stop()
     {
         bActive = false;
 
         return true;
     }
 
     //----------------------------------------------------------
     public boolean Iterate()
     {
             //Send the whole mOutbox
             JMOOSCommPkt PktTx = new JMOOSCommPkt();
             PktTx.serialize( mOutbox, true );
             SendPkt( _socket , PktTx );
 
             mOutbox.Empty(); 
 
             JMOOSCommPkt PktRx = new JMOOSCommPkt();
 			ReadPkt( _socket, PktRx );
             
 			JMOOSMSG_LIST list = new JMOOSMSG_LIST();
             PktRx.serialize( list, false );
             mInbox = PktRx.tmp;
 
 			mReadMessages += mInbox.size();
 
             for ( int i =0; i < mInbox.size(); i ++ )
 			{
 					double data[] = new double[0];
 					int rows = 0;
 					int cols = 0;
 					
 					try
 					{
 						helper.MOOSValFromString( data, rows, cols, mInbox.elementAt(i).GetString(), "data" ) ;
 					}	
 					catch( NullPointerException e )
 					{
 						System.err.println( "Erroneous conversion...");
 					}
 			}
 
			PktTx.Destroy();
			PktRx.Destroy();
			list.Destroy();

			PktTx = null;
			PktRx = null;
			list = null;

             return true;
     }
 
     //----------------------------------------------------------
 	public boolean ConnectToServer()
 	{
 		if ( bIsConnected )
 		{
 			System.err.println( "Client is already connected!\n" );
 			return false;
 		}
 
 		_socket = new JXPCSocket( _hostname, _port );
 
 		if ( !_socket.Connect() )
 		{
 			System.err.println( "Unable to connect to " + _hostname + " @" + _port );
 			return false;
 		}
 
         bIsConnected = true;
 
 		return true;
 		
 	}
 
     //----------------------------------------------------------
     public boolean DisconnectFromServer()
     {
         bActive = false;
 
         if ( !bIsConnected )
         {
             System.err.println( "Client is NOT connected!" );
             return false;
         }
 
         if ( _socket.isOpen() ) 
         {
            System.out.println("Socket is still open, closing....." );
 
             if ( _socket.Disconnect() )
             {
                 System.out.println("CLOSED!");
                 return true;
             }
         }
     
         return false;
     }
 
     //----------------------------------------------------------
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
 
     //----------------------------------------------------------
 	//boolean 	Notify( String var, String val, long time )
 
     //----------------------------------------------------------
 	boolean	Register( String var, double interval )
 	{
 		JMOOSMsg MsgR = new JMOOSMsg( MOOS_REGISTER, var, interval, 1.0 ); 	
 
         mSubscribing.add( var );
 
 		Post( MsgR );
 
 		return true;
 	}
 
     //----------------------------------------------------------
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
     
 	//----------------------------------------------------------
 	boolean	Post( JMOOSMsg msg )
 	{
         if ( mOutbox.size() > MAX_OUTBOX_MESSAGES )
         {
             System.err.println( "OUTBOX IS OVERFLOWING" );
             if ( bVerbose )
                 System.out.println( "mOutbox size is " + mOutbox.size() );
 
 			return false;
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
 	
 	//----------------------------------------------------------
 	//boolean	SetOnDisconnectCallback()
 
 	//----------------------------------------------------------
     boolean SetFundamentalFrequency( double frequency )
     {
         if ( frequency < 0.0 )
             return false;
         else if ( frequency > 100 )
             return false;
         else
             mFundamentalFrequency = frequency;
         return true;
     }
 	
 	//----------------------------------------------------------
 	int GetReadMessagesCounter()
 	{
 		return mReadMessages;
 	}
 
 }
 
 
