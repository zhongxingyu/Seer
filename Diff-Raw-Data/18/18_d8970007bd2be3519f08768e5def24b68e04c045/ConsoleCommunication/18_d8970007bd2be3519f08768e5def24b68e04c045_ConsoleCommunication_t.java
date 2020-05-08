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
 
 package net.grinder.console;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.GrinderProperties;
 import net.grinder.communication.CommunicationException;
 import net.grinder.communication.Message;
 import net.grinder.communication.Receiver;
 import net.grinder.communication.ResetGrinderMessage;
 import net.grinder.communication.Sender;
 import net.grinder.communication.StartGrinderMessage;
 import net.grinder.communication.StopGrinderMessage;
import net.grinder.console.common.ConsoleException;
 import net.grinder.console.common.ConsoleExceptionHandler;
 import net.grinder.console.common.DisplayMessageConsoleException;
 import net.grinder.console.model.ConsoleProperties;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
  */
 class ConsoleCommunication
 {
     private final ConsoleProperties m_properties;
     private final ConsoleExceptionHandler m_exceptionHandler;
 
     private Receiver m_receiver = null;
     private Sender m_sender = null;
     private boolean m_deaf = true;
 
     ConsoleCommunication(ConsoleProperties properties,
 			 ConsoleExceptionHandler exceptionHandler)
     {
 	m_properties = properties;
 	m_exceptionHandler = exceptionHandler;
 
 	resetReceiver();
 	resetSender();
 
 	properties.addPropertyChangeListener(
 	    new PropertyChangeListener() 
 	    {
 		public void propertyChange(PropertyChangeEvent event) 
 		{
 		    final String property = event.getPropertyName();
 
 		    if (property.equals(
 			    ConsoleProperties.MULTICAST_ADDRESS_PROPERTY)) {
 			resetReceiver();
 			resetSender();
 		    }
 		    else if (property.equals(
 				 ConsoleProperties.CONSOLE_PORT_PROPERTY)) {
 			resetReceiver();
 		    }
 		    else if (property.equals(
 				 ConsoleProperties.GRINDER_PORT_PROPERTY)) {
 			resetSender();
 		    }
 		}
 	    });
     }
 
     private void resetReceiver()
     {
 	try {
 	    if (m_receiver != null) {
 		m_receiver.shutdown();
 	    }
 
	    while (!m_deaf) {
		try {
		    synchronized(this) {
			wait();
		    }
		}
		catch (InterruptedException e) {
		}
	    }

 	    m_receiver =
 		new Receiver(m_properties.getMulticastAddress(),
 			     m_properties.getConsolePort());
 
 	    synchronized(this) {
 		m_deaf = false;
 		notifyAll();
 	    }
 	}
 	catch(CommunicationException e) {
 	    handleBindException();
 	}
     }
 
     private void resetSender()
     {
 	String host;
 
 	try {
 	    host = InetAddress.getLocalHost().getHostName();
 	}
 	catch (UnknownHostException e) {
 	    host = "UNNAMED HOST";
 	}
 
 	try {
 	    m_sender = new Sender("Console (" + host + ")",
 				  m_properties.getMulticastAddress(),
 				  m_properties.getGrinderPort());
 	}
 	catch(CommunicationException e) {
 	    handleBindException();
 	}
     }
 
     private void handleBindException()
     {
 	m_exceptionHandler.consoleExceptionOccurred(
 	    new DisplayMessageConsoleException(
 		"bindError.text", "Failed to bind to multicast address"));
     }
 
     void sendStartMessage()
 	throws CommunicationException
     {
 	m_sender.send(new StartGrinderMessage());
     }
 
     void sendResetMessage()
 	throws CommunicationException
     {
 	m_sender.send(new ResetGrinderMessage());
     }
 
     void sendStopMessage()
 	throws CommunicationException
     {
 	m_sender.send(new StopGrinderMessage());
     }
 
     /**
      * @return The message.
      **/
     Message waitForMessage()
     {
 	while (true)
 	{
 	    while (m_deaf) {
 		try {
 		    synchronized(this) {
 			wait();
 		    }
 		}
 		catch (InterruptedException e) {
 		}
 	    }
 
 	    try {
 		final Message message = m_receiver.waitForMessage();
 
 		if (message == null) {
 		    // Current receiver has been shutdown.
 		    synchronized (this) {
 			m_deaf = true;
			notifyAll();
 		    }
 		}
 
 		return message;
 	    }
 	    catch (CommunicationException e) {
		m_exceptionHandler.consoleExceptionOccurred(
		    new ConsoleException(e.getMessage(), e));
 	    }
 	}
     }
 }
