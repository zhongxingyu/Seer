 // Copyright (C) 2001, 2002, 2003, 2004 Philip Aston
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
 
 package net.grinder.engine.common;
 
 import net.grinder.common.Logger;
 import net.grinder.communication.Message;
 import net.grinder.communication.Sender;
 import net.grinder.engine.messages.ResetGrinderMessage;
 import net.grinder.engine.messages.StartGrinderMessage;
 import net.grinder.engine.messages.StopGrinderMessage;
 
 
 /**
  * Process console messages and allows them to be asynchronously
  * queried.
  *
  * <p><code>ConsoleListener</code> is passive; the message is
  * "received" through a {@link Sender} implementation obtained from
  * {@link #getSender}. Use this class in conjunction with an
  * appropriate {@link net.grinder.communication.Receiver} and a {@link
  * net.grinder.communication.MessagePump}.
  *
  * @author Philip Aston
  * @version $Revision$
  * @see net.grinder.engine.process.GrinderProcess
  */
 public final class ConsoleListener {
 
   /**
    * Constant that represents start message.
    * @see #received
    */
   public static final int START = 1 << 0;
 
   /**
    * Constant that represents a a reset message.
    * @see #received
    */
   public static final int RESET = 1 << 1;
 
   /**
    * Constant that represents a stop message.
    * @see #received
    */
   public static final int STOP =  1 << 2;
 
   /**
    * Constant that represents a communication shutdown.
    * @see #received
    */
   public static final int SHUTDOWN =  1 << 3;
 
   /**
    * Constant that represent any message.
    * @see #received
    */
   public static final int ANY = START | RESET | STOP | SHUTDOWN;
 
   private final Object m_notifyOnMessage;
   private final Logger m_logger;
   private int m_messagesReceived = 0;
   private int m_lastMessagesReceived = 0;
   private StartGrinderMessage m_lastStartGrinderMessage;
 
   /**
    * Constructor.
    *
    * @param notifyOnMessage An <code>Object</code> to notify when a
    * message arrives.
    * @param logger A {@link net.grinder.common.Logger} to log received
    * event messages to.
    */
   public ConsoleListener(Object notifyOnMessage, Logger logger) {
     m_notifyOnMessage = notifyOnMessage;
     m_logger = logger;
   }
 
   /**
    * Wait until any message is received.
    *
    * <p>After calling this method, the actual messages can be
    * determined using {@link #received}.</p>
    *
    * @throws InterruptedException If the thread is interrupted whilst
    * waiting.
    */
   public void waitForMessage() throws InterruptedException {
 
     synchronized (m_notifyOnMessage) {
       while (!checkForMessage(ConsoleListener.ANY)) {
         m_notifyOnMessage.wait();
       }
     }
   }
 
   /**
    * Check for messages matching the given mask.
    *
    * <p>After calling this method, the actual messages can be
    * determined using {@link #received}.</p>
    *
    * @param mask The messages to check for.
    * @return <code>true</code> if at least one message matches the
    * <code>mask</code> parameter has been received since the last time
    * the message was checked for, or if communications have been
    * shutdown. <code>false</code> otherwise.
    */
   public boolean checkForMessage(int mask) {
     synchronized (ConsoleListener.this) {
       final int intersection = m_messagesReceived & mask;
 
       try {
         m_lastMessagesReceived = intersection;
       }
       finally {
         m_messagesReceived ^= intersection;
       }
     }
 
     return received(mask | SHUTDOWN);
   }
 
   /**
    * Discard pending messages that match the given mask.
    *
   * @parm mask The messages to discard.
    */
   public void discardMessages(int mask) {
     m_lastMessagesReceived &= ~mask;
     m_messagesReceived &= ~mask;
   }
 
   /**
    * Query the messages set up by the last {@link #checkForMessage} or
    * {@link #waitForMessage} call.
    *
    * @param mask The messages to check for.
    * @return <code>true</code> if one or more of the received
    * messages matches <code>mask</code>.
    */
   public boolean received(int mask) {
     return (m_lastMessagesReceived & mask) != 0;
   }
 
   private void setReceived(int message) {
     synchronized (ConsoleListener.this) {
       m_messagesReceived |= message;
     }
 
     synchronized (m_notifyOnMessage) {
       m_notifyOnMessage.notifyAll();
     }
   }
 
   /**
    * Returns a {@link Sender} that can be used to pass {@link
    * Message}s to the <code>ConsoleListener</code>.
    *
    * @return The <code>Sender</code>.
    */
   public Sender getSender() {
     return new Sender() {
         public void send(Message message) {
           if (message instanceof StartGrinderMessage) {
             m_logger.output("received a start message");
             m_lastStartGrinderMessage = (StartGrinderMessage) message;
             setReceived(START);
           }
           else if (message instanceof StopGrinderMessage) {
             m_logger.output("received a stop message");
             setReceived(STOP);
           }
           else if (message instanceof ResetGrinderMessage) {
             m_logger.output("received a reset message");
             setReceived(RESET);
           }
           else {
             m_logger.error("received an unknown message: " + message);
           }
         }
 
         public void shutdown() {
           m_logger.output("communication shutdown", Logger.LOG);
           setReceived(SHUTDOWN);
         }
       };
   }
 
   /**
    * Return the last {@link StartGrinderMessage} received.
    *
    * @return The message.
    */
   public StartGrinderMessage getLastStartGrinderMessage() {
     return m_lastStartGrinderMessage;
   }
 }
