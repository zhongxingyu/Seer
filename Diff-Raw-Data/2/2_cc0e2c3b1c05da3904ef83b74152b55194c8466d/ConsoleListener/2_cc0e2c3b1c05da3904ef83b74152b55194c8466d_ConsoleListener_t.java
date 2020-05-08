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
 
 package net.grinder.engine.process;
 
 import net.grinder.common.Logger;
 import net.grinder.communication.CommunicationException;
 import net.grinder.communication.Message;
 import net.grinder.communication.Receiver;
 import net.grinder.communication.ResetGrinderMessage;
 import net.grinder.communication.StartGrinderMessage;
 import net.grinder.communication.StopGrinderMessage;
 
 
 /**
  * Active object which listens for console messages.
  *
  * @author Philip Aston
  * @version $Revision$
  * @see net.grinder.engine.process.GrinderProcess
  */
 final class ConsoleListener {
 
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
 
   private final Monitor m_notifyOnMessage;
   private final Logger m_logger;
   private final ReceiverThread m_receiverThread;
   private int m_messagesReceived = 0;
 
   /**
    * Constructor.
    *
    * @param receiver Receiver connected to the console.
    * @param notifyOnMessage A {@link Monitor} to notify when a
    * message arrives.
    * @param logger A {@link net.grinder.common.Logger} to log received
    * event messages to.
    */
   public ConsoleListener(Receiver receiver, Monitor notifyOnMessage,
                          Logger logger) {
     m_notifyOnMessage = notifyOnMessage;
     m_logger = logger;
 
     m_receiverThread = new ReceiverThread(receiver);
     m_receiverThread.start();
   }
 
   /**
    * The <code>ConsoleListener</code> has a bit mask representing
    * messages received but not acknowledged. This method returns a
    * bit mask representing the messages received that match the
    * <code>mask</code> parameter and acknowledges the messages
    * represented by <code>mask</code>.
    *
    * @param mask The messages to check for.
    * @return The subset of <code>mask</code> received.
    */
   public synchronized int received(int mask) {
     final int intersection = m_messagesReceived & mask;
 
     try {
       return intersection;
     }
     finally {
       m_messagesReceived ^= intersection;
     }
   }
 
   /**
    * Thread that uses a {@link net.grinder.communication.Receiver}
    * to receive console messages.
    */
   private final class ReceiverThread extends Thread {
     private final Receiver m_receiver;
 
     /**
      * Creates a new <code>ReceiverThread</code> instance.
      *
      * @param receiver The receiver.
      */
     public ReceiverThread(Receiver receiver) {
       super("Console Listener");
       m_receiver = receiver;
       setDaemon(true);
     }
 
     /**
      * Event loop that receives messages from the console.
      */
     public void run() {
       while (true) {
         final Message message;
 
         try {
           message = m_receiver.waitForMessage();
         }
         catch (CommunicationException e) {
           m_logger.error("error receiving console signal: " + e,
                          Logger.LOG | Logger.TERMINAL);
           continue;
         }
 
         if (message == null) {
           m_logger.output("communication shutdown",
                           Logger.LOG | Logger.TERMINAL);
           setReceived(SHUTDOWN);
           break;
         }
         else if (message instanceof StartGrinderMessage) {
           m_logger.output("received a start message");
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
          m_logger.error("received an unknown message");
         }
       }
     }
 
     private void setReceived(int message) {
       synchronized (ConsoleListener.this) {
         m_messagesReceived |= message;
       }
 
       synchronized (m_notifyOnMessage) {
         m_notifyOnMessage.notifyAll();
       }
     }
   }
 }
 
