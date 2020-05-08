 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.communication;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
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
  **/
 final class SocketSet {
   private static final int PURGE_FREQUENCY = 1000;
 
   private final Object m_mutex = new Object();
   private List m_handles = new ArrayList();
   private int m_lastHandle = 0;
   private int m_nextPurge = 0;
 
   public SocketSet() {
     m_handles.add(new SentinelHandle());
   }
 
   public final void add(Socket socket) throws IOException {
     final Handle handle = new HandleImplementation(socket);
 
     synchronized (m_mutex) {
       m_handles.add(handle);
       m_mutex.notifyAll();
     }
   }
 
   public final Handle reserveNextHandle() throws InterruptedException {
     synchronized (m_mutex) {
       purgeZombieHandles();
 
       int checked = 0;
 
       while (true) {
 	if (++m_lastHandle >= m_handles.size()) {
 	  m_lastHandle = 0;
 	}
 
 	if (checked++ >= m_handles.size()) {
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
 
   public final void close() {
     synchronized (m_mutex) {
       final Iterator iterator = m_handles.iterator();
 
       while (iterator.hasNext()) {
 	final Handle handle = (Handle)iterator.next();
 	handle.close();
       }
     }
   }
 
   private final void purgeZombieHandles() {
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
 
   public interface Handle {
     boolean isSentinel();
 
     Message pollForMessage() throws ClassNotFoundException, IOException;
 
     boolean reserve();
     void free();	
 
     void close();
     boolean isClosed();
   }
 
   private static final class SentinelHandle implements Handle {
     public final boolean isSentinel() {
       return true;
     }
 
     public final Message pollForMessage() {
       throw new RuntimeException("Assertion failure");
     }
 
     public final boolean reserve() {
       return true;
     }
 
     public final void free() {
     }
 
     public final void close() {
     }
 
     public final boolean isClosed() {
       return false;
     }
   }
 
   private static final class HandleImplementation implements Handle {
     private final Socket m_socket;
     private final InputStream m_inputStream;
     private ObjectInputStream m_objectStream;
     private boolean m_busy = false;
     private boolean m_closed = false;
 
     HandleImplementation(Socket socket) throws IOException {
       m_socket = socket;
       m_inputStream = new BufferedInputStream(m_socket.getInputStream());
     }
 
     public final boolean isSentinel() {
       return false;
     }
 
     public final Message pollForMessage()
       throws ClassNotFoundException, IOException {
 
       // Don't synchronise, assume caller has correctly reserved
       // this Handle.
       if (m_inputStream.available() == 0) {
 	return null;
       }
 
       m_objectStream = new ObjectInputStream(m_inputStream);
 
       return (Message)m_objectStream.readObject();
     }
 
     public final synchronized boolean reserve() {
       if (m_busy || m_closed) {
 	return false;
       }
 	    
       m_busy = true;
 
       return true;
     }
 
     public final synchronized void free() {
       m_busy = false;
     }
 
     public final synchronized void close() {
       if (!m_closed) {
 	m_closed = true;
 
 	try {
 	  m_socket.close();
 	}
 	catch (IOException e) {
 	  // Ignore.
 	}
       }
     }
 
     public final synchronized boolean isClosed() {
       return m_closed;
     }
   }
 }
