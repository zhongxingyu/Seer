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
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
  * Class that manages the a set of TCP sockets. Currently only alows
 * for polling the sockets for received {@link Message}s, but might be
  * extended in the future to support broadcast of a {@link Message} to
  * all the sockets.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class SocketSet
 {
     private static final int PURGE_FREQUENCY = 1000;
 
     private final Object m_mutex = new Object();
     private List m_handles = new ArrayList();
     private int m_lastHandle = 0;
     private int m_nextPurge = 0;
 
     public final void add(Handle handle)
     {
 	synchronized (m_mutex) {
 	    m_handles.add(handle);
 	    m_mutex.notifyAll();
 	}
     }
 
     public final Handle reserveNextHandle() throws InterruptedException
     {
 	synchronized (m_mutex) {
 	    purgeZombieHandles();
 
 	    int checked = 0;
 
 	    while (true) {
 		if (++m_lastHandle >= m_handles.size()) {
 		    m_lastHandle = 0;
 		}
 
 		if (checked++ >= m_handles.size())
 		{
 		    // All current Handles are busy => too many
 		    // threads. Put this one to sleep until we have
 		    // more work.
 		    m_mutex.wait();
 
 		    checked = 0;
 		}
 		else {
 		    final Handle handle = (Handle)m_handles.get(m_lastHandle);
 
 		    if (handle.reserve()) {
 			return handle;
 		    }
 		}
 	    }
 	}
     }
 
     public final void close()
     {
 	synchronized (m_mutex) {
 	    final Iterator iterator = m_handles.iterator();
 
 	    while (iterator.hasNext()) {
 		final Handle handle = (Handle)iterator.next();
 		handle.close();
 	    }
 	}
     }
 
     private final void purgeZombieHandles()
     {
 	synchronized (m_mutex) {
 	    if (++m_nextPurge > PURGE_FREQUENCY) {
 		m_nextPurge = 0;
 
 		final List newHandles = new ArrayList(m_handles.size());
 
 		final Iterator iterator = m_handles.iterator();
 
 		while (iterator.hasNext()) {
 		    final Handle handle = (Handle)iterator.next();
 
 		    if (!handle.isClosed()) {
 			newHandles.add(handle);
 		    }
 		}
 
 		m_handles = newHandles;
 		m_lastHandle = 0;
 	    }
 	}
     }
     
     public final static class Handle
     {
 	private final Socket m_socket;
 	private final InputStream m_inputStream;
 	private ObjectInputStream m_objectStream;
 	private boolean m_busy = false;
 	private boolean m_closed = false;
 
 	public Handle(Socket socket) throws IOException
 	{
 	    m_socket = socket;
 	    m_inputStream = new BufferedInputStream(m_socket.getInputStream());
 	}
 
 	public final Message pollForMessage()
 	    throws ClassNotFoundException, IOException
 	{
 	    // Don't synchronise, assume caller has correctly reserved
 	    // this Handle.
 
 	    if (m_inputStream.available() == 0) {
 		return null;
 	    }
 
 	    m_objectStream = new ObjectInputStream(m_inputStream);
 
 	    return (Message)m_objectStream.readObject();
 	}
 
 	public final synchronized boolean reserve()
 	{
 	    if (m_busy || m_closed) {
 		return false;
 	    }
 	    
 	    m_busy = true;
 
 	    return true;
 	}
 
 	public final synchronized void free()
 	{
 	    m_busy = false;
 	}
 
 	public final synchronized void close()
 	{
 	    if (!m_closed) {
 		m_closed = true;
 
 		try {
 		    m_socket.close();
 		}
 		catch (IOException e){
 		}
 	    }
 	}
 
 	public final synchronized boolean isClosed()
 	{
 	    return m_closed;
 	}
     }
 
     private static void log(String s)
     {
 	System.err.println(Thread.currentThread().getName() + ": " + s);
     }
 }
