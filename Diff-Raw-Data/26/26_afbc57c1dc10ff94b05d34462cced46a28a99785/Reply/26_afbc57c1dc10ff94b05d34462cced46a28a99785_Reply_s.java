 
 /*
 * Reply.java            $Revision: 1.7 $ $Date: 2003/04/23 15:23:02 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.beepcore.org/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.lib;
 
 
 import java.lang.InterruptedException;
 
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 
 import org.beepcore.beep.core.BEEPInterruptedException;
 import org.beepcore.beep.core.Message;
 import org.beepcore.beep.core.ReplyListener;
 
 
 /**
  * Provides a synchronous abstraction for receiving BEEP reply messages.
  * The caller may block using <code>getNextReply</code> when as it waits
  * for incoming messages.
  *
  * <code>Reply</code> is produced by the <code>Channel.sendMSG</code>
  * method.
  *
  * Please note that the other Channel send operations do NOT
  * return this class as a result.
  *
  * @see org.beepcore.beep.core.Channel#sendMSG
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision, $Date: 2003/04/23 15:23:02 $
  */
 public class Reply implements ReplyListener {
 
     // Constants
     private static final int DEFAULT_ARRAY_LIST_SIZE = 4;
 
     // Data
     private boolean complete = false;
     private LinkedList replies = new LinkedList();
 
     /**
      * Returns the reply corresponding to a <code>Channel.sendMSG</code>
      * call as a <code>Message</code>.  Always call <code>hasNext</code>
      * previous to calling <code>getNextReply</code> to discover whether or not
      * <code>getNextReply</code> should be called again.
      * If all messages for this reply have been returned, a subsequent call
      * will throw a <code>NoSuchElementException</code>.
      *
      *
      * @return Message Contains the reply to the previous request.
      *
      * @see Message
      * @see #hasNext
      *
      * @throws BEEPException
      * @throws NoSuchElementException If the reply is complete and no more
      * <code>Message</code>s can be returned.
      *
      */
     synchronized public Message getNextReply() throws BEEPInterruptedException
     {
         if (this.replies.size() == 0) {
             if (this.complete) {
                 throw new NoSuchElementException();
             }
 
             try {
                 this.wait();
             } catch (InterruptedException x) {
                 throw new BEEPInterruptedException(x.getMessage());
             }
         }
 
         Message message = (Message) this.replies.removeFirst();
 
         if (message.getMessageType() != Message.MESSAGE_TYPE_ANS) {
             this.complete = true;
         }
 
         return message;
     }
 
     /**
      * Indicates if there are more messages to retrive.  While
      * <code>hasNext</code> returns true the reply to the previous
      * <code>sendMSG</code> is not complete.  Call <code>getNextReply</code>
      * to return unretrieved messages.
      *
      * @see #getNextReply
      *
      */
     synchronized public boolean hasNext() throws BEEPInterruptedException
     {
        if (this.replies.size() != 0) {
            return true;
        }

        if (this.complete) {
            return false;
        }

         try {
            this.wait();
         } catch (InterruptedException x) {
             throw new BEEPInterruptedException(x.getMessage());
         }
 
        if (complete) {
            return false;
        }

        return true;
     }
 
     private synchronized void setMessage(Message message)
     {
         this.replies.add(message);
         notify();
     }
 
     // Implementation of method declared in ReplyListener
     public void receiveRPY(Message message)
     {
         setMessage(message);
     }
 
     // Implementation of method declared in ReplyListener
     public void receiveERR(Message message)
     {
         setMessage(message);
     }
 
     // Implementation of method declared in ReplyListener
     public void receiveANS(Message message)
     {
         setMessage(message);
     }
 
     // Implementation of method declared in ReplyListener
     public synchronized void receiveNUL(Message message)
     {
         this.complete = true;
         notify();
     }
 }
