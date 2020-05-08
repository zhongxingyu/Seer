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
 
 
 /**
  * Thread-safe queue of {@link Message}s.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 class MessageQueue {
 
   private final ThreadSafeQueue m_queue = new ThreadSafeQueue();
   private final boolean m_passExceptions;
 
   /**
    * Creates a new <code>MessageQueue</code> instance.
    *
    * @param passExceptions <code>true</code> => allow exceptionss to
    * be inserted into the queue and rethrown to callers of {@link
    * #dequeue}.
    **/
   public MessageQueue(boolean passExceptions) {
     m_passExceptions = passExceptions;
   }
 
   /**
    * Queue the given message.
    *
    * @param message A {@link Message}.
    * @exception ThreadSafeQueue.ShutdownException If the queue has
    * been shutdown.
    * @see #shutdown
    **/
   public final void queue(Message message)
     throws ThreadSafeQueue.ShutdownException {
 
     m_queue.queue(message);
   }
 
   /**
    * Queue the given exception.
    *
    * @param exception An exception.
    * @exception RuntimeException If the queue does not allow
    * exceptions to be propagated..
    * @exception ThreadSafeQueue.ShutdownException If the queue has
    * been shutdown.
    * @see #shutdown
    **/
   public final void queue(Exception exception)
     throws ThreadSafeQueue.ShutdownException {
 
     if (!m_passExceptions) {
       // Assertion failure.
       throw new RuntimeException(
         "This MessageQueue does not allow Exceptions to be queued");
     }
 
     m_queue.queue(exception);
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
    * @exception ThreadSafeQueue.ShutdownException If the queue has
    * been shutdown.
    * @see #shutdown
    **/
   public final Message dequeue(boolean block)
     throws CommunicationException, ThreadSafeQueue.ShutdownException {
 
    final Object result = (Message)m_queue.dequeue(block);
 
     if (m_passExceptions && result instanceof Exception) {
       throw new CommunicationException("Queued exception", (Exception) result);
     }
 
     return (Message) result;
   }
 
   /**
    * Shutdown the <code>MessageQueue</code>. Any {@link Message}s in
    * the queue are discarded.
    **/
   public final void shutdown() {
     m_queue.shutdown();
   }
 
   public final Object getMutex() {
     return m_queue.getMutex();
   }
 }
