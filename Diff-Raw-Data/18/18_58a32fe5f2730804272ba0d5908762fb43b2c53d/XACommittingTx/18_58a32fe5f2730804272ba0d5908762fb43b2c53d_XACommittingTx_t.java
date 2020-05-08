 /*
  * JOnAS: Java(TM) Open Application Server
  * Copyright (C) 2004 Bull S.A.
  * All rights reserved.
  * 
  * Contact: howl@objectweb.org
  * 
  * This software is licensed under the BSD license.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 
  *   * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *     
  *   * Redistributions in binary form must reproduce the above copyright
  *     notice, this list of conditions and the following disclaimer in the
  *     documentation and/or other materials provided with the distribution.
  *     
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * ------------------------------------------------------------------------------
 * $Id: XACommittingTx.java,v 1.7 2005-08-18 22:29:55 girouxm Exp $
  * ------------------------------------------------------------------------------
  */
 package org.objectweb.howl.log.xa;
 
 import java.nio.ByteBuffer;
 
 /**
  * XA Transaction Managers write log records using the XALogger subclass of
  * the basic HOWL Logger.  The XALogger methods keep track of transactions
  * that are in the COMMITTING state using XACommittingTx objects. 
  * 
  * @author Michael Giroux
  */
 public class XACommittingTx {
   
   /**
    * reference to the COMMIT data record the TM wrote to the
    * log.
    * <p>TM must <b> not </b> change this record until the DONE
    * record has been logged.  Specifically, once the TM calls
    * XALogger.putCommit() it must not change the byte[][]
    * that was logged.
    */
   private byte[][] record = null;
   
   /**
    * returns the byte[][] containing the COMMIT record data.
    * <p>This method is used by the log overflow notification
    * processor to retrieve the original record data and write
    * a new copy of the COMMIT record.
    * 
    * @return the byte[][] containing the COMMIT record data. 
    */
   public final byte[][] getRecord() { return record; }
   
   /**
    * saves a reference to the byte[][] containing the COMMIT
    * record data.
    * 
    * @param record the byte[][] that was passed to the putCommit()
    * routine of XALogger.
    */
   final void setRecord(byte[][] record) { this.record = record; }
   
   
   /**
    * workerID into the activeTx[] that this entry will be stored.
    * <p>Each XACommittingTx instance is assigned a fixed slot
    * in the activeTx[].  Entries in the activeTx[] are set
    * to null when not in use.
    * <p>When the XACommittingTx object is constructed, it
    * is stored into the availableTx[] using <i> workerID </i>.
    * The entry is removed 
    */
   private final int index;
   
   /**
    * returns an workerID into an array of XACommittingTx
    * objects that holds a reference to this XACommittingTx.
    * 
    * @return an integer used as an workerID into an array of XATransactions.
    */
   final int getIndex() { return this.index; }
   
   /**
    * the log key associated with the COMMIT record for
    * this transaction
    */
   private long logKey = 0L;
   
   /**
    * @return the log key associated with the COMMIT record
    * for this transaction. 
    */
   public final long getLogKey() { return logKey; }
   
   /**
    * sets the log key associated with the COMMIT record
    * for this transaction.
    * 
    * <p>also updates LogKeyBytes with byte[] form of logKey
    * for subsequent recording in XADONE record.
    * 
    * @param logKey a log key returned by Logger.put()
    * 
    */
   final void setLogKey(long logKey)
   {
     this.logKey = logKey;
     LogKeyBB.clear();
     LogKeyBB.putLong(logKey);
   }
   
   /**
    * Flag indicating that the TM has called XALogger.putDone().
    * <p>initialized to <b> false <b> in XALogger.putCommit()
    * and set to <b> true </b> in XALogger.putDone().
    * <p>Examined by logOverflowNotification processor to
    * determine if the entry needs to be moved.  Entries
    * that are marked 
    */
   private boolean done = false;
   
   /**
    * 
    * @param done <b>false</b> while transaction is in COMMITTING state.
    * <b>true</b> when two-phase-commit completes.  
    */
   final void setDone(boolean done) {this.done = done; };
   
   /**
    * returns <i> done </i> as maintained by XALogger.putCommit()
    * and XALogger.putDone().
    * 
    * @return boolean indicating whether the two-phase-commit processing
    * is complete.
    */
   public final boolean isDone() { return this.done; };
   
   /**
    * Flag indicating that the log overflow notification routine
    * is in the process of moving this record.
    * 
    * <p>To assure that the DONE record is always
    * recorded <b> after </b> the COMMIT record,
    * XALogger.putDone() must wait until moving is false
    * before it writes the DONE record to the log.
    */
   private boolean moving = false;
   
   /**
    * used by logOverflowNotification to mark XACommittingTx entries
    * in the process of being moved.
    * 
    * <p>The XALogger.putDone() method waits until moving is false.
    * 
    * @param moving boolean indicating that entry is being moved by
    * logOverflowNotification routine.
    */
   final void setMoving(boolean moving) { this.moving = moving; } 
   
   /**
    * @return <b>true</b> if logOverflowNotification routine is
    * moving the record.
    * 
    * <p>The XALogger.putDone() method waits until moving is false.
    */
   public final boolean isMoving() { return this.moving; }
   
   /**
    * constructs a new XACommittingTx instance.
    */
   XACommittingTx(int index)
   {
     this.index = index;
    
    // BUG 303907 - add index to XADONE records to aid debugging journal problems.
    ByteBuffer indexBB = ByteBuffer.wrap(this.indexBytes);
    indexBB.putInt(index);
   }
   
   /**
    * byte[] representation of logKey.
    * <p>Recorded to the log by XALogger#putDone
    * as the record data of the XADONE record.
    */
   private byte[] logKeyBytes = new byte[8];
   
   /**
    * Used to putLong(logKey) into logKeyBytes.
    */
   private ByteBuffer LogKeyBB = ByteBuffer.wrap(logKeyBytes);
   
   /**
   * byte[] representation of this.index.
   * <p>Recorded to the log by XALogger#putDone
   * as a diagnostic aid.
   */
  private byte[] indexBytes = new byte[4];
  
  /**
    * data record for XADONE record generated
    * by XALogger#putDone().
    */
  byte[][] logKeyData = new byte[][] { logKeyBytes, indexBytes };
   
 }
