 // The Grinder
 // Copyright (C) 2000, 2001  Paco Gomez
 // Copyright (C) 2000, 2001  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.communication;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.LinkedList;
 
 
 /**
  * Class that manages the sending of unicast messages.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 public class UnicastSender extends AbstractSender
 {
     private OutputStream m_outputStream;
     private final Socket m_socket;
 
     /**
      * Constructor.
      *
      * @param grinderID A string describing our Grinder process.
      * @param addressString TCP address to send to.
      * @param port TCP port to send to.
      * @throws CommunicationException If failed to bind to socket or
      * failed to generate a unique process identifer.
      **/    
     public UnicastSender(String grinderID, String addressString, int port)
 	throws CommunicationException
     {
 	super(grinderID);
 
 	final String localHost;
 	final int localPort;
 
 	try {
 	    // Our socket - bind to any local port.
 	    m_socket = new Socket(addressString, port);
 
 	    m_outputStream =
 		new BufferedOutputStream(m_socket.getOutputStream());
 
 	    localHost = InetAddress.getLocalHost().getHostName();
 	    localPort = m_socket.getLocalPort();
 	}
 	catch (IOException e) {
 	    throw new CommunicationException(
 		"Could not bind to TCP address " + addressString + ":" + port,
 		e);
 	}
 
 	// Calculate a globally unique string for this sender. We
 	// avoid calling addressString.toString() since this involves
 	// a DNS lookup.
 	setSenderID(
 	    addressString + ":" + port + ":" + localHost + ":" +
 	    localPort + ":" + System.currentTimeMillis());
     }
 
     protected final void writeMessage(Message message) throws IOException
     {
 	// I tried the model of using a single ObjectOutputStream for
 	// the lifetime of the socket, but the corresponding
 	// ObjectInputStream would get occasional EOF's during
 	// readObject. Seems like voodoo to me, but creating a new
 	// ObjectOutputStream for every message fixes this.
 
 	final ObjectOutputStream objectStream =
 	    new ObjectOutputStream(m_outputStream);
 	
 	objectStream.writeObject(message);
 	objectStream.flush();
     }
 
     /**
     * Cleanly shutdown the <code>Sender</code>. Ignore most errors.
     * Connection has probably been reset by peer.
      *
      * @exception CommunicationException If an error occurs.
      **/
     public void shutdown() throws CommunicationException
     {
	try {
	    send(new CloseCommunicationMessage());
	    flush();
	}
	catch (CommunicationException e) {
	}
 
 	super.shutdown();
 
 	try {
 	    m_socket.close();
 	}
 	catch (IOException e) {
 	}
 
 	try {
 	    m_socket.close();
 	}
 	catch (IOException e) {
 	}
     }
 }
