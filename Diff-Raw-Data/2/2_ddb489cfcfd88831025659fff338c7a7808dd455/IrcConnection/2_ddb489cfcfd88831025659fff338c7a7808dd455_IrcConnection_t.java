 /**
  * Copyright (C) 2013 Alexander Szczuczko
  *
  * This file may be modified and distributed under the terms
  * of the MIT license. See the LICENSE file for details.
  */
 package ca.szc.keratin.core.net;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 
 import javax.net.SocketFactory;
 import javax.net.ssl.SSLSocketFactory;
 
 import net.engio.mbassy.bus.BusConfiguration;
 import net.engio.mbassy.bus.MBassador;
 
 import org.pmw.tinylog.Logger;
 
 import ca.szc.keratin.core.event.IrcEvent;
 import ca.szc.keratin.core.event.connection.IrcDisconnect;
 import ca.szc.keratin.core.net.io.InputThread;
 import ca.szc.keratin.core.net.io.IrcOutputHandler;
 
 public class IrcConnection
 {
     private final SocketFactory socketFactory;
 
     private InetSocketAddress endpoint;
 
     private MBassador<IrcEvent> bus;
 
     private Thread inputWorkerThread;
 
     /**
      * Create an IRC connection with a String address and SSL disabled. The connection is not active until connect is
      * called.
      * 
      * @param address A string representing the address to connect to
      * @param port The port number within the valid range to connect to
      * @throws UnknownHostException If no IP address for the host could be found, or if a scope_id was specified for a
      *             global IPv6 address.
      * @throws InvalidPortException If the port parameter is outside the specified range of valid port values.
      */
     public IrcConnection( String address, int port )
         throws UnknownHostException, InvalidPortException
     {
         this( InetAddress.getByName( address ), port, false );
     }
 
     /**
      * Create an IRC connection with SSL disabled. The connection is not active until connect is called.
      * 
      * @param address A InetAddress representing the address to connect to
      * @param port The port number within the valid range to connect to
      * @throws InvalidPortException If the port parameter is outside the specified range of valid port values.
      */
     public IrcConnection( InetAddress address, int port )
         throws InvalidPortException
     {
         this( address, port, false );
     }
 
     /**
      * Create an IRC connection with a String address. The connection is not active until connect is called.
      * 
      * @param address A string representing the address to connect to
      * @param port The port number within the valid range to connect to
      * @throws UnknownHostException If no IP address for the host could be found, or if a scope_id was specified for a
      *             global IPv6 address.
      * @throws InvalidPortException If the port parameter is outside the specified range of valid port values.
      */
     public IrcConnection( String address, int port, boolean ssl )
         throws UnknownHostException, InvalidPortException
     {
        this( InetAddress.getByName( address ), port, ssl );
     }
 
     /**
      * Create an IRC connection. The connection is not active until connect is called.
      * 
      * @param address A InetAddress representing the address to connect to
      * @param port The port number within the valid range to connect to
      * @throws InvalidPortException If the port parameter is outside the specified range of valid port values.
      */
     public IrcConnection( InetAddress address, int port, boolean ssl )
         throws InvalidPortException
     {
         Logger.trace( "IrcConnection instantiation" );
         try
         {
             endpoint = new InetSocketAddress( address, port );
         }
         catch ( IllegalArgumentException e )
         {
             throw new InvalidPortException( e );
         }
 
         bus = new MBassador<IrcEvent>( BusConfiguration.Default() );
 
         if ( ssl )
             socketFactory = SSLSocketFactory.getDefault();
         else
             socketFactory = SocketFactory.getDefault();
     }
 
     /**
      * Get the IrcEvent bus for the connection
      * 
      * @return the central MBassador bus
      */
     public MBassador<IrcEvent> getEventBus()
     {
         return bus;
     }
 
     /**
      * Activate the connection. Will block until the first connection is established.
      */
     public void connect()
     {
         Logger.info( "Connecting" );
 
         Logger.trace( "Subscribing to event bus" );
         bus.subscribe( new IrcConnectionHandlers( endpoint, socketFactory ) );
         bus.addErrorHandler( new BusErrorHandler() );
         bus.subscribe( new DeadMessageHandler() );
 
         Logger.trace( "Creating/starting input thread" );
         inputWorkerThread = new InputThread( bus );
         inputWorkerThread.start();
 
         Logger.trace( "Registering output handler" );
         new IrcOutputHandler( bus );
 
         Logger.trace( "Sending event to establish first connection" );
         bus.publish( new IrcDisconnect( bus, null ) );
 
         Logger.trace( "Done set up" );
     }
 
     /**
      * Deactivate the connection.
      */
     public void disconnect()
     {
         Logger.info( "Disconnecting" );
 
         Logger.trace( "Stopping worker thread" );
         inputWorkerThread.interrupt();
         try
         {
             inputWorkerThread.join();
         }
         catch ( InterruptedException e )
         {
         }
 
         Logger.trace( "Shutting down event bus" );
         bus.shutdown();
         bus = null;
 
         Logger.trace( "Done shut down" );
     }
 
     /**
      * Put the IrcConnection in a state to be reused after calling disconnect().
      */
     public void reuse()
     {
         bus = new MBassador<IrcEvent>( BusConfiguration.Default() );
     }
 }
