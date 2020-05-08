 /*
  *  PDOSCClient.java
  *  PDOSCClient
  *
  *  Copyright (c) 2010 Hanns Holger Rutz. All rights reserved.
  *
  *	This library is free software; you can redistribute it and/or
  *	modify it under the terms of the GNU Lesser General Public
  *	License as published by the Free Software Foundation; either
  *	version 2.1 of the License, or (at your option) any later version.
  *
  *	This library is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *	Lesser General Public License for more details.
  *
  *	You should have received a copy of the GNU Lesser General Public
  *	License along with this library; if not, write to the Free Software
  *	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  *	For further information, please contact Hanns Holger Rutz at
  *	contact@sciss.de
  *
  *
  *  Changelog:
  */
 
 package de.sciss.net;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 
 import com.cycling74.max.Atom;
 import com.cycling74.max.MaxObject;
 import de.sciss.net.OSCListener;
 import de.sciss.net.OSCMessage;
 
 /**
  *  A subclass of <code>com.cycling74.max.MaxObject</code>
 *  that allows bidirectional OSC communication via UDP.
  *
  *  @author		Hanns Holger Rutz
 *  @version	0.12, 11-Sep-06
  */
 public class PDOSCClient
 extends MaxObject
 implements OSCListener
 {
     public static final double 	VERSION			= 0.11;
     private static final int	NUM_INLETS		= 2;
     private static final int	NUM_OUTLETS		= 3;
 
     public static final String USAGE = "PDOSCPort arguments: [<(int) port> [<String protocol>]]";
     private static final String[] INLET_ASSIST = {
         "Control messages", "OSC messages (Lists) to send"
     };
     private static final String[] OUTLET_ASSIST = {
         "Received OSC messages", "Source OSC address (host port)", "OSC bundle time" // , "Query Replies"
     };
 
     private static final int	INLET_MSG			= 0;
     private static final int	INLET_OSCMSG		= 1;
     private static final int	OUTLET_OSCMSG		= 0;
     private static final int	OUTLET_OSCADDR		= 1;
     private static final int	OUTLET_OSCTIME		= 2;
     private static final int	OUTLET_INFO			= 3;
 
     private static final String ERR_ARGTYPE		= "Wrong argument type: ";
 //    private static final String ERR_NOADDRESS   = "No target socket address (use \"host\" and \"port\")";
     private static final String ERR_NOCLIENT 	= "Client not running (maybe failed to bind to requested local port)";
     private static final String ERR_NOTARGET	= "No target socket address (use \"target\" to set it)";
     private static final String ERR_CONNECT		= "While connecting";
 //    private static final String ERR_DISCONNECT  = "While disconnecting";
     private static final String ERR_SENDING		= "While sending OSC packet";
 //    private static final String ERR_ADDRESS		= "While setting target socket";
 //    private static final String ERR_LOCALADDRESS = "Could not retrieve local address";
 	private static final String ERR_BUNDLENEST	= "Cannot nest bundles";
 	private static final String ERR_NOBUNDLE	= "No open bundle";
 
 	private OSCClient			client			= null;
 	private final int			ourPort;
 	private int					ourResolvedPort	= 0;
 //	private InetSocketAddress	addr			= null;
 
 	private final String		protocol;
 	private int					port			= 0;
 	private String				host			= "127.0.0.1";
 	private OSCBundle			bndl			= null;
 	private long				bndlTimeOffset	= 0L;
 	private boolean				targetSet		= false;
 	
 	private int					dumpInMode		= OSCChannel.kDumpOff;
 	private int					dumpOutMode		= OSCChannel.kDumpOff;
 	
 	private static final PrintStream	dumpStream	= System.out; // goes to Max/PD console
 	
     public static void main( String args[] )
 	{
         System.out.println( "This is a java external to use with Max/MSP mxj or PD pdj object." );
 		System.exit( 1 );
     }
 
     public PDOSCClient()
 	{
 		this( null );
 	}
 
     public PDOSCClient( Atom[] args )
     {
     	final int numArgs = args == null ? 0 : args.length;
     	if( numArgs == 0 ) {
     		protocol	= OSCChannel.UDP;
     		ourPort		= 0;
     	} else if( numArgs == 1 && args[ 0 ].isInt() ) {
     		protocol	= OSCChannel.UDP;
     		ourPort		= args[ 0 ].toInt();
     	} else if( numArgs == 1 && args[ 0 ].isString() ) {
     		protocol	= args[ 0 ].toString();
     		ourPort		= 0;
     	} else if( numArgs == 2 && args[ 0 ].isString() && args[ 1 ].isInt() ) {
     		protocol	= args[ 0 ].toString();
     		ourPort		= args[ 1 ].toInt();
     	} else {
     		bail( USAGE );
     		protocol	= null;
     		ourPort		= 0;
     	}
 	
         declareIO( NUM_INLETS, NUM_OUTLETS );
         setInletAssist( INLET_ASSIST );
         setOutletAssist( OUTLET_ASSIST );
 //		declareAttribute( "port" ); //, "getPort", "setPort" );
 //		declareAttribute( "host" ); // , "getHost", "setHost" );
 //		declareAttribute( "protocol" ); // , "getProtocl", "setProtocl" );
 //		declareAttribute( "localhost", "getLocalHost", "setLocalHost" );
 //		declareAttribute( "localport" ); // , "getLocalPort", "setLocalPort" );
     }
     
     private boolean createClient() {
     	if( client != null ) return true;
     	if( !targetSet ) {
     		error( ERR_NOTARGET );
     		return false;
     	}
     	try {
 			final InetSocketAddress addr = new InetSocketAddress( host, port );
 			final boolean loopBack = addr.getAddress().isLoopbackAddress();
         	client	= OSCClient.newUsing( protocol, ourPort, loopBack );
             client.addOSCListener( this );
             client.setTarget( addr );
             client.start();
             dumpOSC( dumpInMode, dumpOutMode );
             ourResolvedPort = (ourPort == 0) ? client.getLocalAddress().getPort() : ourPort;
     	} catch( IOException e1 ) {
     		showException( ERR_CONNECT, e1 );
     	}
 		return true;
     }
 
     /**
      *  Called by Max when this java external
      *  has been removed from the patcher.
      *  We use it to free resources, close datagram
      *  channels etc.
      */
     public void notifyDeleted()
     {
         cleanUp();
     }
 
     private void cleanUp()
     {
         if( client != null ) {
         	client.dispose();
         	client = null;
         	ourResolvedPort = ourPort;
         }
     }
     
     public void disconnect()
     {
     	cleanUp();
     	targetSet = false;
     }
 
 	public void dumpOSC( int mode )
 	{
 		dumpInMode 	= mode;
 		dumpOutMode = mode;
 		if( client != null ) client.dumpOSC( mode, System.out );	// System.out appears in the max window
 	}
 
 	public void dumpOSC( int incomingMode, int outgoingMode  )
 	{
 		dumpInMode 	= incomingMode;
 		dumpOutMode = outgoingMode;
 		if( client != null ) {
 			client.dumpIncomingOSC( incomingMode, dumpStream );
 			client.dumpOutgoingOSC( outgoingMode, dumpStream );
 		}
 	}
 
 	public void getlocalport()
 	{
 		outlet( OUTLET_INFO, new Atom[] { Atom.newAtom( ourResolvedPort )});
 	}
 
 	public void gettarget()
 	{
     	if( !targetSet ) {
     		error( ERR_NOTARGET );
     		return;
     	}
     	outlet( OUTLET_INFO, new Atom[] { Atom.newAtom( host ), Atom.newAtom( port )});
 	}
 	
 	public void target( String host, int port )
 	{
 		this.host	= host;
 		this.port	= port;
 		cleanUp();
 		targetSet	= true;
     }
 
 	private void illegalMessage( String message )
 	{
 		error( getName() + " doesn't understand \"" + message +"\"" );
 	}
 
 	private void illegalMessageArgs( String message )
 	{
 		error( getName() + " : illegal message args for \"" + message +"\"" );
 	}
     
 	public void openbundle()
 	{
 		if( bndl != null ) {
 			error( getName() + " " + ERR_BUNDLENEST );
 			return;
 		}
 		bndl = new OSCBundle();
 	}
 	
 	public void openbundle( int time )
 	{
 		if( bndl != null ) {
 			error( getName() + " " + ERR_BUNDLENEST );
 			return;
 		}
 		bndl = new OSCBundle( time + (bndlTimeOffset == 0L ? System.currentTimeMillis() : bndlTimeOffset ));
 	}
 
 	public void closebundle()
 	{
 		if( bndl == null ) {
 			error( getName() + " " + ERR_NOBUNDLE );
 			return;
 		}
 		
 		send( bndl );
 		bndl = null;
 	}
 	
 // max can't handle 64bit precision properly
 //	public void getsystemmillis()
 //	{
 ////		outlet( getInfoIdx(), "systemmillis", System.currentTimeMillis() );
 //		outlet( getInfoIdx(), "systemmillis", (double) System.currentTimeMillis() );
 //	}
 
 	public void freezetime()
 	{
 		bndlTimeOffset = System.currentTimeMillis();
 	}
 	
 	public void unfreezetime()
 	{
 		bndlTimeOffset = 0L;
 	}
 	
 	private void send( OSCPacket p )
 	{
 		if( !createClient() ) return;
 		if( client == null ) {
 			error( ERR_NOCLIENT );
 			return;
 		}
 		try {
 			client.send( p );
 		}
 		catch( IOException e1 ) {
 			showException( ERR_SENDING, e1 );
 		}
 	}
 	
 	private void msgInlet( String message, Atom[] args ) {
 		final int numArgs = args == null ? 0 : args.length;
 		if( message.equals( "target" )) {
 			if( numArgs == 1 && args[ 0 ].isInt() ) {
 				target( "127.0.0.1", args[ 0 ].toInt() );
 			} else if( numArgs == 2 && args[ 0 ].isString() && args[ 1 ].isInt() ) {
 				target( args[ 0 ].toString(), args[ 1 ].toInt() );
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "gettarget" )) {
 			if( numArgs == 0 ) {
 				gettarget();
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "getlocalport" )) {
 			if( numArgs == 0 ) {
 				getlocalport();
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "dumpOSC" )) {
 			if( numArgs == 1 && args[ 0 ].isInt() ) {
 				dumpOSC( args[ 0 ].toInt() );
 			} else if( args.length == 2 && args[ 0 ].isInt() && args[ 1 ].isInt() ) {
 				dumpOSC( args[ 0 ].toInt(), args[ 1 ].toInt() );
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "openbundle" )) {
 			if( numArgs == 0 ) {
 				openbundle();
 			} else if( numArgs == 1 && args[ 0 ].isInt() ) {
 				openbundle( args[ 0 ].toInt() );
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "closebundle" )) {
 			if( numArgs == 0 ) {
 				closebundle();
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "freezetime" )) {
 			if( numArgs == 0 ) {
 				freezetime();
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "unfreezetime" )) {
 			if( numArgs == 0 ) {
 				unfreezetime();
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else if( message.equals( "disconnect" )) {
 			if( numArgs == 0 ) {
 				cleanUp();
 			} else {
 				illegalMessageArgs( message );
 			}
 		} else {
 			illegalMessage( message );
 		}
 	}
 	
     protected void anything( String message, Atom[] args )
     {
     	switch( getInlet() ) {
 			case INLET_MSG: msgInlet( message, args );
 				break;
     		case INLET_OSCMSG: oscMsgInlet( message, args );
     			break;
 		}
     }		
 
 	private void oscMsgInlet( String message, Atom[] args ) {
 		final Object[] javaArgs = new Object[ args == null ? 0 : args.length ];
 		
 		// assemble OSC message
 		for( int i = 0; i < javaArgs.length; i++ ) {
 			final Atom atom = args[ i ];
 			if( atom.isInt() ) {
				javaArgs[ i ] = new Integer( atom.toInt() );
 			} else if( atom.isFloat() ) {
				javaArgs[ i ] = new Float( atom.toFloat() );
 			} else if( atom.isString() ) {
				javaArgs[ i ] = atom.toString();
 			} else {
 				error( getName() + " " + ERR_ARGTYPE + atom.getClass().getName() );
 				return;
 			}
 		}
 		
 		final OSCMessage msg = new OSCMessage( message, javaArgs );
 		
 		if( bndl == null ) {
 			send( msg );
 		} else {
 			bndl.addPacket( msg );
 		}
     }
 	
 	// -------------------- OSCListener interface --------------------
 
     public void messageReceived( OSCMessage msg, SocketAddress sender, long when )
     {
         final Atom[]	hiroshima = new Atom[ msg.getArgCount() + 1 ];
         Object			o;
 		
         hiroshima[ 0 ] = Atom.newAtom( msg.getName() );
                 
         for( int i = msg.getArgCount(); i > 0; i-- ) {
             o = msg.getArg( i - 1 );
             if( o instanceof Integer ) {
                 hiroshima[ i ] = Atom.newAtom( ((Integer) o).intValue() );
             } else if( o instanceof Float ) {
                 hiroshima[ i ] = Atom.newAtom( ((Float) o).floatValue() );
             } else if( o instanceof String ) {
                 hiroshima[ i ] = Atom.newAtom( (String) o );
             } else if( o instanceof Double ) {
                 hiroshima[ i ] = Atom.newAtom( ((Double) o).doubleValue() );
             } else if( o instanceof Long ) {
                 hiroshima[ i ] = Atom.newAtom( ((Long) o).longValue() );
             } else {
                 hiroshima[ i ] = Atom.newAtom( false );
             }
         }
         
 		outlet( OUTLET_OSCTIME, when );
 		if( sender instanceof InetSocketAddress ) {
 			final InetSocketAddress iaddr = (InetSocketAddress) sender;
 			outlet( OUTLET_OSCADDR, new Atom[] { Atom.newAtom( iaddr.getHostName() ), Atom.newAtom( iaddr.getPort() )});
 		}
         outlet( OUTLET_OSCMSG, hiroshima );
     }
 }
