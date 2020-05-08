 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002 Philip Aston
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
 
 import java.util.LinkedList;
 
 import net.grinder.common.GrinderException;
 
 
 /**
  * Thread-safe queue of {@link Message}s.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 class MessageQueue
 {
     private final boolean m_passExceptions;
     private LinkedList m_messages = new LinkedList();
     private boolean m_shutdown = false;
 
     /**
      * Creates a new <code>MessageQueue</code> instance.
      *
      * @param passExceptions <code>true</code> => allow exceptionss to
      * be inserted into the queue and rethrown to callers of {@link
      * #dequeue}.
      **/
     public MessageQueue(boolean passExceptions)
     {
 	m_passExceptions = passExceptions;
     }
 
     /**
      * Queue the given message.
      *
      * @param message A {@link Message}.
      * @exception ShutdownException If the queue has been shutdown.
      * @see #shutdown
      **/
     public final void queue(Message message) throws ShutdownException
     {
 	doQueue(message);
     }
 
     /**
      * Queue the given exception.
      *
      * @param exception An exception.
      * @exception RuntimeException If the queue does not allow
      * exceptions to be propagated..
      * @exception ShutdownException If the queue has been shutdown.
      * @see #shutdown
      **/
     public final void queue(Exception exception)
 	throws ShutdownException
     {
 	if (!m_passExceptions) {
 	    // Assertion failure.
 	    throw new RuntimeException(
 		"This MessageQueue does not allow Exceptions to be queued");
 	}
 
 	doQueue(exception);
     }
 
     private final void doQueue(Object o) throws ShutdownException
     {
 	synchronized (getMutex()) {
 	    assertNotShutdown();
 	    m_messages.add(o);
 	    m_messages.notifyAll();
 	}
     }
 
     /**
      * Dequeue a message.
      *
      * @param block <code>true</code> => block until message is
      * available, <code>false</code => return <code>null</code> if no
      * message is available.
      * @exception CommunicationException If the queue allows
      * exceptions to be propagated, queued CommunicationExceptions are
      * rethrown to callers of this method.
      * @exception ShutdownException If the queue has been shutdown.
      * @see #shutdown
      **/
     public final Message dequeue(boolean block)
 	throws CommunicationException, ShutdownException
     {
 	synchronized (getMutex()) {
 	    while (!m_shutdown && block && m_messages.size() == 0) {
 		try {
		    m_messages.wait();
 		}
 		catch (InterruptedException e) {
 		}
 	    }
 
 	    assertNotShutdown();
 
 	    if (m_messages.size() == 0) {
 		return null;
 	    }
 	    else {
 		final Object o = m_messages.removeFirst();
 
 		if (m_passExceptions && o instanceof Exception) {
 		    throw new CommunicationException(
 			"Queued exception", (Exception)o);
 		}
 		return (Message)o;
 	    }
 	}
     }
 
     /**
      * Shutdown the <code>MessageQueue</code>. Any {@link Message}s in
      * the queue are discarded.
      **/
     public final void shutdown()
     {
 	synchronized (getMutex()) {
 	    m_shutdown = true;
 	    m_messages.clear();
 	    m_messages.notifyAll();
 	}
     }
 
     public final Object getMutex()
     {
 	return m_messages;
     }
 
     private final void assertNotShutdown() throws ShutdownException
     {
 	if (m_shutdown) {
 	    throw new ShutdownException("MessageQueue shutdown");
 	}
     }
 
     /**
      * Exception that indicates <code>MessageQueue</code> has been
      * shutdown. It doesn't extend {@link CommunicationException}
      * because typically callers want to propagate
      * <code>ShutdownException</code>s but handle
      * <code>CommunicationException</code>s locally.
      **/
     final static class ShutdownException extends GrinderException
     {
 	public ShutdownException(String s)
 	{
 	    super(s);
 	}
 
 	public ShutdownException(String s, Exception e)
 	{
 	    super(s, e);
 	}
     }
 }
