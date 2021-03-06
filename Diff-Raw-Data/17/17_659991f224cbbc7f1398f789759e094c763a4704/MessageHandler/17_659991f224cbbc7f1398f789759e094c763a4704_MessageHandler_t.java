 /* $Id$ */
 
 package ibis.satin.impl;
 
 import ibis.ipl.IbisIdentifier;
 import ibis.ipl.ReadMessage;
 import ibis.ipl.SendPortIdentifier;
 import ibis.ipl.Upcall;
 import ibis.ipl.WriteMessage;
 import ibis.satin.ActiveTuple;
 import ibis.satin.SharedObject;
 import ibis.util.Timer;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 
 final class MessageHandler implements Upcall, Protocol, Config {
     Satin satin;
 
     MessageHandler(Satin s) {
 	satin = s;
     }
 
     void handleAbort(ReadMessage m) {
         int stamp = -1;
         IbisIdentifier owner = null;
         try {
             stamp = m.readInt();
             owner = (IbisIdentifier) m.readObject();
             synchronized (satin) {
                 satin.addToAbortList(stamp, owner);
             }
             // m.finish();
         } catch (IOException e) {
             abortLogger.error("SATIN '" + satin.ident
                     + "': got exception while reading job result: " + e, e);
         } catch (ClassNotFoundException e1) {
             abortLogger.error("SATIN '" + satin.ident
                     + "': got exception while reading job result: " + e1, e1);
         }
     }
 
     /**
      * Used for fault tolerance
      */
     void handleAbortAndStore(ReadMessage m) {
         int stamp = -1;
         IbisIdentifier owner = null;
         try {
             stamp = m.readInt();
             owner = (IbisIdentifier) m.readObject();
             synchronized (satin) {
                 satin.addToAbortAndStoreList(stamp, owner);
             }
             // m.finish();
         } catch (IOException e) {
             grtLogger.error("SATIN '" + satin.ident
                     + "': got exception while reading abort_and_store: " + e,
                     e);
         } catch (ClassNotFoundException e1) {
             grtLogger.error("SATIN '" + satin.ident
                     + "': got exception while reading abort_and_store: " + e1,
                     e1);
         }
     }
 
     void handleJobResult(ReadMessage m, int opcode) {
         ReturnRecord rr = null;
         SendPortIdentifier sender = m.origin();
         IbisIdentifier i = null;
         int stamp = -666;
         Throwable eek = null;
         Timer returnRecordReadTimer = null;
         boolean gotException = false;
 
         // This upcall may run in parallel with other upcalls.
         // Therefore, we cannot directly use the timer in Satin.
         // Use our own local timer, and add the result to the global timer
         // later.
 
         if (STEAL_TIMING) {
             returnRecordReadTimer = satin.createTimer();
             returnRecordReadTimer.start();
         }
         try {
             i = (IbisIdentifier) m.readObject();
             if (opcode == JOB_RESULT_NORMAL) {
                 rr = (ReturnRecord) m.readObject();
                 stamp = rr.stamp;
                 eek = rr.eek;
             } else {
                 eek = (Throwable) m.readObject();
                 stamp = m.readInt();
             }
             // m.finish();
         } catch (IOException e) {
             spawnLogger.error("SATIN '" + satin.ident
                     + "': got exception while reading job result: " + e
                     + opcode, e);
             gotException = true;
         } catch (ClassNotFoundException e1) {
             spawnLogger.error("SATIN '" + satin.ident
                     + "': got exception while reading job result: " + e1
                     + opcode, e1);
             gotException = true;
         }
 
         if (STEAL_TIMING) {
             returnRecordReadTimer.stop();
             synchronized (satin) {
                 satin.returnRecordReadTimer.add(returnRecordReadTimer);
             }
         }
 
         if (gotException) {
             return;
         }
 
         if (stealLogger.isDebugEnabled()) {
             if (eek != null) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': handleJobResult: exception result: " + eek, eek);
             } else {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': handleJobResult: normal result");
             }
         }
 
         satin.addJobResult(rr, sender, i, eek, stamp);
     }
 
     /*
      * Just make this method synchronized, than all the methods we call don't
      * have to be.
      */
     // Furthermore, this makes sure that nobody changes tables while I am busy.
     // But, it is scary, then we are sending a reply while holding a lock...
     // --Rob
     void handleStealRequest(SendPortIdentifier ident, int opcode) {
         Victim v = null;
         Map table = null;
         Timer handleStealTimer = null;
         Timer invocationRecordWriteTimer = null;
 
         // This upcall may run in parallel with other upcalls.
         // Therefore, we cannot directly use the handleSteal timer in Satin.
         // Use our own local timer, and add the result to the global timer
         // later.
 
         if (STEAL_TIMING) {
             handleStealTimer = satin.createTimer();
             invocationRecordWriteTimer = satin.createTimer();
 
             /*
              * handleStealTimer = Timer.createTimer();
              * invocationRecordWriteTimer = Timer.createTimer();
              * if (handleStealTimer == null) {
              *     handleStealTimer = new Timer();
              *     invocationRecordWriteTimer = new Timer();
              * }
              */
 
             handleStealTimer.start();
         }
 
         if (STEAL_STATS) {
             satin.stealRequests++;
         }
 
         if (stealLogger.isDebugEnabled()) {
             if (opcode == STEAL_REQUEST) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got steal request from " + ident.ibis());
             } else if (opcode == ASYNC_STEAL_REQUEST) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got ASYNC steal request from "
                         + ident.ibis());
             } else if (opcode == STEAL_AND_TABLE_REQUEST) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got steal and table request from "
                         + ident.ibis());
             } else if (opcode == ASYNC_STEAL_AND_TABLE_REQUEST) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got ASYNC steal and table request from "
                         + ident.ibis());
             }
         }
 
         InvocationRecord result = null;
 
         synchronized (satin) {
 
             if (satin.deadIbises.contains(ident.ibis())) {
                 //this message arrived after the crash of its sender was
                 // detected
                 //is it anyhow possible?
                 stealLogger.warn("SATIN '" + satin.ident
                         + "': EEK!! got steal request from a dead ibis: "
                         + ident.ibis());
                 if (STEAL_TIMING) {
                     handleStealTimer.stop();
                     satin.handleStealTimer.add(handleStealTimer);
                 }
                 return;
             }
 
             v = satin.getVictimWait(ident.ibis());
 
             while (true) {
                 result = satin.q.getFromTail();
                 if (result != null) {
                     result.stealer = ident.ibis();
 
                     /* store the job in the outstanding list */
                     satin.addToOutstandingJobList(result);
                 }
 
                 if (opcode != BLOCKING_STEAL_REQUEST || satin.exiting
                         || result != null) {
                     break;
                 }
                 try {
                     satin.wait();
                 } catch (Exception e) {
                     // Ignore.
                 }
                 v = satin.getVictimNoWait(ident.ibis());
                 if (v == null) {
                     // This ibis died.
                     return;
                 }
             }
 
             if (FAULT_TOLERANCE
                     && !FT_NAIVE
                     && (opcode == STEAL_AND_TABLE_REQUEST || opcode == ASYNC_STEAL_AND_TABLE_REQUEST)) {
 
                 if (!satin.getTable) {
                     table = satin.globalResultTable.getContents();
                     //temporary
                     // tupleSpace = satin.getContents();
                 }
             }
         }
 
         if (result == null) {
             if (stealLogger.isDebugEnabled()) {
                 if (opcode == ASYNC_STEAL_REQUEST) {
                     stealLogger.debug("SATIN '" + satin.ident
                             + "': sending FAILED back to "
                             + ident.ibis());
                 }
 
                 if (opcode == ASYNC_STEAL_AND_TABLE_REQUEST) {
                     stealLogger.debug("SATIN '" + satin.ident
                             + "': sending FAILED_TABLE back to "
                             + ident.ibis());
                 }
             }
 
             try {
                 WriteMessage m = v.newMessage();
                 if (opcode == STEAL_REQUEST
                         || opcode == BLOCKING_STEAL_REQUEST) {
                     m.writeByte(STEAL_REPLY_FAILED);
                 } else if (opcode == ASYNC_STEAL_REQUEST) {
                     m.writeByte(ASYNC_STEAL_REPLY_FAILED);
                 } else if (opcode == STEAL_AND_TABLE_REQUEST) {
                     if (table != null) {
                         m.writeByte(STEAL_REPLY_FAILED_TABLE);
                         m.writeObject(table);
                         //temporary
                         /* if (tupleSpace == null) {
                          *     tupleLogger.fatal("SATIN '" + satin.ident 
                          *             + "': EEK i have the table but not the "
                          *             + "tuplespace!!");
                          *     System.exit(1);
                          * }
                          * m.writeObject(tupleSpace);
                          */
                     } else {
                         m.writeByte(STEAL_REPLY_FAILED);
                     }
                 } else if (opcode == ASYNC_STEAL_AND_TABLE_REQUEST) {
                     if (table != null) {
                         m.writeByte(ASYNC_STEAL_REPLY_FAILED_TABLE);
                         m.writeObject(table);
                         //temporary
                         /* if (tupleSpace == null) {
                          *     tupleLogger.fatal("SATIN '" + satin.ident 
                          *             + "': EEK i have the table but not the "
                          *             + "tuplespace!!");
                          *     System.exit(1);
                          * }
                          * m.writeObject(tupleSpace);
                          */
                     } else {
                         m.writeByte(ASYNC_STEAL_REPLY_FAILED);
                     }
                 } else {
                     stealLogger.error(
                             "UNHANDLED opcode " + opcode
                             + " in handleStealRequest");
                     // System.exit(1);
                 }
 
                 long cnt = m.finish();
                 if (STEAL_STATS) {
                     if (satin.inDifferentCluster(ident.ibis())) {
                         satin.interClusterMessages++;
                         satin.interClusterBytes += cnt;
                     } else {
                         satin.intraClusterMessages++;
                         satin.intraClusterBytes += cnt;
                     }
                 }
 
                 if (stealLogger.isDebugEnabled()) {
                     stealLogger.debug("SATIN '" + satin.ident
                             + "': sending FAILED back to "
                             + ident.ibis() + " DONE");
                 }
 
             } catch (IOException e) {
                 stealLogger.warn("SATIN '" + satin.ident
                         + "': trying to send FAILURE back, but got exception: "
                         + e, e);
             }
 
             if (STEAL_TIMING) {
                 handleStealTimer.stop();
                 satin.handleStealTimer.add(handleStealTimer);
             }
 
             return;
         }
 
         /* else */
 
         if (ASSERTS && result.aborted) {
             stealLogger.warn("SATIN '" + satin.ident
                     + ": trying to send aborted job!");
         }
 
         if (STEAL_STATS) {
             satin.stolenJobs++;
         }
 
         if (stealLogger.isDebugEnabled()) {
             stealLogger.debug("SATIN '" + satin.ident
                     + "': sending SUCCESS and #" + result.stamp + " back to "
                     + ident.ibis());
 
             if (opcode == ASYNC_STEAL_REQUEST) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': sending SUCCESS back to " + ident.ibis());
             }
 
             if (opcode == ASYNC_STEAL_AND_TABLE_REQUEST) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': sending SUCCESS_TABLE back to "
                         + ident.ibis());
             }
         }
 
         try {
             if (STEAL_TIMING) {
                 invocationRecordWriteTimer.start();
             }
             WriteMessage m = v.newMessage();
             if (opcode == STEAL_REQUEST || opcode == BLOCKING_STEAL_REQUEST) {
                 m.writeByte(STEAL_REPLY_SUCCESS);
             } else if (opcode == ASYNC_STEAL_REQUEST) {
                 m.writeByte(ASYNC_STEAL_REPLY_SUCCESS);
             } else if (opcode == STEAL_AND_TABLE_REQUEST) {
                 if (table != null) {
                     m.writeByte(STEAL_REPLY_SUCCESS_TABLE);
                     m.writeObject(table);
                     //temporary
                     /* if (tupleSpace == null) {
                      *     tupleLogger.fatal("SATIN '" + satin.ident 
                      *             + "': EEK i have the table but not the "
                      *             + "tuplespace!!");
                      *     System.exit(1);
                      * }
                      * m.writeObject(tupleSpace);
                      */
                 } else {
                     stealLogger.warn("SATIN '" + satin.ident
                             + "': EEK!! sending a job but not a table !?");
                 }
             } else if (opcode == ASYNC_STEAL_AND_TABLE_REQUEST) {
                 if (table != null) {
                     m.writeByte(ASYNC_STEAL_REPLY_SUCCESS_TABLE);
                     m.writeObject(table);
                     //temporary
                     /* if (tupleSpace == null) {
                      *     tupleLogger.fatal("SATIN '" + satin.ident 
                      *             + "': EEK i have the table but not the "
                      *             + "tuplespace!!");
                      *     System.exit(1);
                      * }
                      * m.writeObject(tupleSpace);
                      */
                 } else {
                     stealLogger.warn("SATIN '" + satin.ident
                             + "': EEK!! sending a job but not a table !?");
                 }
             } else {
                 stealLogger.error("UNHANDLED opcode " + opcode
                         + " in handleStealRequest");
                 // System.exit(1);
             }
 
             if (TupleSpace.use_seq) { // ordered communication
                 m.writeLong(satin.expected_seqno);
             }
 
             m.writeObject(result);
 
             long cnt = m.finish();
             if (STEAL_TIMING) {
                 invocationRecordWriteTimer.stop();
                 satin.invocationRecordWriteTimer.add(
                         invocationRecordWriteTimer);
             }
             if (STEAL_STATS) {
                 if (satin.inDifferentCluster(ident.ibis())) {
                     satin.interClusterMessages++;
                     satin.interClusterBytes += cnt;
                 } else {
                     satin.intraClusterMessages++;
                     satin.intraClusterBytes += cnt;
                 }
             }
         } catch (IOException e) {
             stealLogger.warn("SATIN '" + satin.ident
                     + "': trying to send a job back, but got exception: " + e,
                     e);
         }
 
         if (STEAL_TIMING) {
             handleStealTimer.stop();
             satin.handleStealTimer.add(handleStealTimer);
         }
     }
 
     // Here, the timing code is OK, the upcall cannot run in parallel
     // (readmessage is not finished).
     void handleReply(ReadMessage m, int opcode) {
         SendPortIdentifier ident = m.origin();
         InvocationRecord tmp = null;
         Map table = null;
         if (stealLogger.isDebugEnabled()) {
             if (opcode == STEAL_REPLY_SUCCESS) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got steal reply message from "
                         + ident.ibis() + ": SUCCESS");
             } else if (opcode == STEAL_REPLY_FAILED) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got steal reply message from "
                         + ident.ibis() + ": FAILED");
             }
             if (opcode == ASYNC_STEAL_REPLY_SUCCESS) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got ASYNC steal reply message from "
                         + ident.ibis() + ": SUCCESS");
             } else if (opcode == ASYNC_STEAL_REPLY_FAILED) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got ASYNC steal reply message from "
                         + ident.ibis() + ": FAILED");
             }
             if (opcode == STEAL_REPLY_SUCCESS_TABLE) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got steal reply message from "
                         + ident.ibis() + ": SUCCESS_TABLE");
             } else if (opcode == STEAL_REPLY_FAILED_TABLE) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got steal reply message from "
                         + ident.ibis() + ": FAILED_TABLE");
             }
             if (opcode == ASYNC_STEAL_REPLY_SUCCESS_TABLE) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got ASYNC steal reply message from "
                         + ident.ibis() + ": SUCCESS_TABLE");
             } else if (opcode == ASYNC_STEAL_REPLY_FAILED_TABLE) {
                 stealLogger.debug("SATIN '" + satin.ident
                         + "': got ASYNC steal reply message from "
                         + ident.ibis() + ": FAILED_TABLE");
             }
         }
 
         switch (opcode) {
         case BARRIER_REPLY:
             if (commLogger.isDebugEnabled()) {
                 commLogger.debug("SATIN '" + satin.ident
                         + "': got barrier reply message from "
                         + ident.ibis());
             }
 
             synchronized (satin) {
                 if (ASSERTS && satin.gotBarrierReply) {
                     commLogger.fatal("Got barrier reply while I already got "
                             + "one.");
                     System.exit(1);     // Failed assertion
                 }
                 satin.gotBarrierReply = true;
                 satin.notifyAll();
             }
             break;
 
         case STEAL_REPLY_SUCCESS_TABLE:
         case ASYNC_STEAL_REPLY_SUCCESS_TABLE:
             try {
                 table = (Map) m.readObject();
                 //temporary
                 //tupleSpace = (Map) m.readObject();
             } catch (IOException e) {
                 stealLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading steal "
                         + "reply from " + ident + ", opcode:" + +opcode
                         + ", exception: " + e, e);
             } catch (ClassNotFoundException e1) {
                 stealLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading steal "
                         + "reply from " + ident + ", opcode:" + +opcode
                         + ", exception: " + e1, e1);
             }
             if (table != null) {
                 synchronized (satin) {
                     satin.getTable = false;
                     satin.globalResultTable.addContents(table);
                 }
             }
             //satin.addContents(tupleSpace);
             if (ADD_REPLICA_TIMING) {
                 satin.addReplicaTimer.stop();
             }
         //fall through
         case STEAL_REPLY_SUCCESS:
         case ASYNC_STEAL_REPLY_SUCCESS:
 
             if (IDLE_TIMING && satin.idleStarted) {
                 satin.idleStarted = false;
                 if (idleLogger.isDebugEnabled()) {
                     idleLogger.debug("SATIN '" + ident + "': idle stop");
                 }
                 satin.idleTimer.stop();
             }
 
             try {
                 if (STEAL_TIMING) {
                     satin.invocationRecordReadTimer.start();
                 }
                 if (TupleSpace.use_seq) { // ordered communication
                     satin.stealReplySeqNr = m.readLong();
                 }
                 tmp = (InvocationRecord) m.readObject();
                 if (STEAL_TIMING) {
                     satin.invocationRecordReadTimer.stop();
                 }
 
                 if (ASSERTS && tmp.aborted) {
                     stealLogger.warn("SATIN '" + satin.ident
                             + ": stole aborted job!");
                 }
             } catch (IOException e) {
                 stealLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading steal "
                         + "reply from " + ident + ", opcode:" + +opcode
                         + ", exception: " + e, e);
             } catch (ClassNotFoundException e1) {
                 stealLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading steal "
                         + "reply from " + ident + ", opcode:" + +opcode
                         + ", exception: " + e1, e1);
             }
 
             synchronized (satin) {
                 if (satin.deadIbises.contains(ident)) {
                     //this message arrived after the crash of its sender
                     // was detected
                     //is it anyhow possible?
                     stealLogger.error("SATIN '" + satin.ident
                             + "': got reply from dead ibis??? Ignored");
                     break;
                 }
             }
 
             satin.algorithm.stealReplyHandler(tmp, opcode);
             break;
 
         case STEAL_REPLY_FAILED_TABLE:
         case ASYNC_STEAL_REPLY_FAILED_TABLE:
             try {
                 table = (Map) m.readObject();
             } catch (IOException e) {
                 stealLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading steal "
                         + "reply from " + ident + ", opcode:" + +opcode
                         + ", exception: " + e, e);
             } catch (ClassNotFoundException e1) {
                 stealLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading steal "
                         + "reply from " + ident + ", opcode:" + +opcode
                         + ", exception: " + e1, e1);
             }
             if (table != null) {
                 synchronized (satin) {
                     satin.getTable = false;
                     satin.globalResultTable.addContents(table);
                 }
             }
             if (ADD_REPLICA_TIMING) {
                 satin.addReplicaTimer.stop();
             }
         //fall through
         case STEAL_REPLY_FAILED:
         case ASYNC_STEAL_REPLY_FAILED:
             satin.algorithm.stealReplyHandler(null, opcode);
             break;
 
         default:
             stealLogger.error("INTERNAL ERROR, opcode = " + opcode);
             // System.exit(1);
             break;
         }
 
     }
 
     private static class tuple_command {
         byte command;
 
         String key;
 
         Serializable data;
 
         SendPortIdentifier sender;
 
         tuple_command(byte c, String k, Serializable s, SendPortIdentifier se) {
             command = c;
             key = k;
             data = s;
             sender = se;
         }
     }
 
     private HashMap saved_tuple_commands = null;
 
     private void add_to_queue(long seqno, String key, Serializable data,
             SendPortIdentifier s, byte command) {
         if (saved_tuple_commands == null) {
             saved_tuple_commands = new HashMap();
         }
         saved_tuple_commands.put(new Long(seqno), new tuple_command(command,
                 key, data, s));
     }
 
     private void scan_queue() {
 
         if (saved_tuple_commands == null) {
             return;
         }
 
         Long i = new Long(satin.expected_seqno);
         tuple_command t = (tuple_command) saved_tuple_commands.remove(i);
         while (t != null) {
             switch (t.command) {
             case TUPLE_ADD:
                 if (t.data instanceof ActiveTuple) {
                     synchronized (satin) {
                         satin.addToActiveTupleList(t.key, t.data);
                         satin.gotActiveTuples = true;
                     }
                 } else {
                     TupleSpace.remoteAdd(t.key, t.data);
                 }
                 break;
             case TUPLE_DEL:
                 TupleSpace.remoteDel(t.key);
                 break;
             }
             satin.expected_seqno++;
 
             if (t.sender.equals(satin.tuplePort.identifier())) {
                 synchronized (satin) {
                     satin.tuple_message_sent = false;
                     satin.notifyAll();
                 }
             }
 
             i = new Long(satin.expected_seqno);
             t = (tuple_command) saved_tuple_commands.remove(i);
         }
     }
 
     private void handleTupleAdd(ReadMessage m) {
         long seqno = 0;
 
 	/*	if (TUPLE_TIMING) {
 	    satin.handleTupleTimer.start();
 	    }*/
 
         try {
             if (TupleSpace.use_seq) {
                 seqno = m.sequenceNumber();
             }
             String key = m.readString();
             Serializable data = (Serializable) m.readObject();
             SendPortIdentifier s = m.origin();
 
             if (TupleSpace.use_seq && seqno > satin.expected_seqno) {
                 add_to_queue(seqno, key, data, s, TUPLE_ADD);
             } else {
                 if (data instanceof ActiveTuple) {
                     synchronized (satin) {
                         satin.addToActiveTupleList(key, data);
                         satin.gotActiveTuples = true;
                     }
                 } else {
                     TupleSpace.remoteAdd(key, data);
                 }
                 if (TupleSpace.use_seq) {
                     satin.expected_seqno++;
                     scan_queue();
 
                     synchronized (satin) {
                         if (s.equals(satin.tuplePort.identifier())) {
                             satin.tuple_message_sent = false;
                         }
 
                         satin.notifyAll();
                     }
                 }
             }
 
             m.finish(); // @@@ is this one needed ? --Rob
 
         } catch (Exception e) {
             tupleLogger.error("SATIN '" + satin.ident
                     + "': Got Exception while reading tuple update: " + e, e);
         }
 
 	/*	if (TUPLE_TIMING) {
 	    satin.handleTupleTimer.stop();
 	    }*/
     }
 
     private void handleTupleDel(ReadMessage m) {
         long seqno = 0;
         boolean done = false;
         try {
             if (TupleSpace.use_seq) {
                 seqno = m.sequenceNumber();
             }
             String key = m.readString();
             if (TupleSpace.use_seq && seqno > satin.expected_seqno) {
                 add_to_queue(seqno, key, null, m.origin(), TUPLE_DEL);
             } else {
                 TupleSpace.remoteDel(key);
                 if (TupleSpace.use_seq) {
                     satin.expected_seqno++;
                     scan_queue();
                 }
                 done = true;
                 SendPortIdentifier s = m.origin();
                 if (s.equals(satin.tuplePort.identifier())) {
                     synchronized (satin) {
                         satin.tuple_message_sent = false;
                         satin.notifyAll();
                     }
                 }
             }
             m.finish(); // @@@ is this one needed ? --Rob
         } catch (Exception e) {
             tupleLogger.error("SATIN '" + satin.ident
                     + "': Got Exception while reading tuple remove: " + e, e);
             // System.exit(1);
         }
         if (TupleSpace.use_seq) {
             if (done) {
                 synchronized (satin) {
                     satin.notifyAll();
                 }
             }
         }
     }
 
     private void handleResultRequest(ReadMessage m) {
         Victim v = null;
         GlobalResultTable.Value value = null;
         Timer handleLookupTimer = null;
 
         try {
             if (GRT_TIMING) {
                 handleLookupTimer = satin.createTimer();
                 handleLookupTimer.start();
             }
 
             GlobalResultTable.Key key = (GlobalResultTable.Key) m.readObject();
 
             int stamp = m.readInt();
 
             //leave it out if you make globally unique stamps
             IbisIdentifier owner = (IbisIdentifier) m.readObject();
 
             IbisIdentifier ident = m.origin().ibis();
 
             m.finish();
 
             // if (grtLogger.isInfoEnabled()) {
             //     grtLogger.info("SATIN '" + satin.ident
             //             + "': got result request " + key + " from " + ident);
             // }
 
             synchronized (satin) {
                 value = satin.globalResultTable.lookup(key, false);
                 if (value == null && ASSERTS) {
                     grtLogger.fatal("SATIN '" + satin.ident
                             + "': EEK!!! no requested result in the table: "
                             + key);
                     System.exit(1);     // Failed assertion
                 }
                 if (value.type == GlobalResultTable.Value.TYPE_POINTER
                         && ASSERTS) {
                     grtLogger.fatal("SATIN '" + satin.ident
                             + "': EEK!!! " + ident
                             + " requested a result: " + key
                             + " which is stored on another node: " + value);
                     System.exit(1);     // Failed assertion
                 }
 
                 if (FT_WITHOUT_ABORTS) {
                     if (value.type == GlobalResultTable.Value.TYPE_LOCK) {
                         //job not finished yet, set the owner of the job
                         //to the sender of this message and return
                         //i'm writing an invocation record here, is that ok?
                         value.sendTo = ident;
                         if (GRT_TIMING) {
                             handleLookupTimer.stop();
                             satin.handleLookupTimer.add(handleLookupTimer);
                         }
                         return;
                     }
                 }
 
                 v = satin.getVictimNoWait(ident);
             }
             if (v == null) {
                 if (commLogger.isDebugEnabled()) {
                     commLogger.debug("SATIN '" + satin.ident
                             + "': the node requesting a result died");
                 }
                 if (GRT_TIMING) {
                     handleLookupTimer.stop();
                     satin.handleLookupTimer.add(handleLookupTimer);
                 }
                 return;
             }
             value.result.stamp = stamp;
             WriteMessage w = v.newMessage();
             w.writeByte(Protocol.JOB_RESULT_NORMAL);
             w.writeObject(owner);
             //leave it out if you make globally unique stamps
             w.writeObject(value.result);
             w.finish();
         } catch (IOException e) {
             grtLogger.error("SATIN '" + satin.ident
                     + "': trying to send result back, but got exception: " + e,
                     e);
         } catch (ClassNotFoundException e) {
             grtLogger.error("SATIN '" + satin.ident
                     + "': trying to send result back, but got exception: " + e,
                     e);
         }
         if (GRT_TIMING) {
             handleLookupTimer.stop();
             satin.handleLookupTimer.add(handleLookupTimer);
         }
 
     }
 
     private void handleResultPush(ReadMessage m) {
 
         if (grtLogger.isInfoEnabled()) {
             grtLogger.info("SATIN '" + satin.ident
                     + ": handle result push");
         }
 
         try {
 
             Map results = (Map) m.readObject();
 
             synchronized (satin) {
 
                 satin.globalResultTable.updateAll(results);
             }
 
         } catch (IOException e) {
             grtLogger.error("SATIN '" + satin.ident
                     + "': trying to read result push, but got exception: " + e,
                     e);
         } catch (ClassNotFoundException e) {
             grtLogger.error("SATIN '" + satin.ident
                     + "': trying to read result push, but got exception: " + e,
                     e);
         }
 
         if (grtLogger.isInfoEnabled()) {
             grtLogger.info("SATIN '" + satin.ident
                     + ": handle result push finished");
         }
 
     }
 
     private void handleExitReply(ReadMessage m) {
 
         SendPortIdentifier ident = m.origin();
 
         if (commLogger.isDebugEnabled()) {
             commLogger.debug("SATIN '" + satin.ident
                     + "': got exit ACK message from " + ident.ibis());
         }
 
         if (satin.stats) {
             try {
                 StatsMessage s = (StatsMessage) m.readObject();
                 satin.totalStats.add(s);
             } catch (Exception e) {
                 commLogger.error("SATIN '" + satin.ident
                         + "': Got Exception while reading stats: " + e, e);
                 // System.exit(1);
             }
         }
 
         try {
             m.finish();
         } catch (Exception e) {
             /* ignore */
         }
 
         synchronized (satin) {
             satin.exitReplies++;
             satin.notifyAll();
         }
     }
 
     public void handleSORequest(ReadMessage m) {
 	String objid = null;
 //	SharedObject so = null;
 //	Victim v = null;	
//	Timer handleSOTransferTimer = null;
 //	Timer soSerializationTimer = null;
 //	long size = 0;
 //	WriteMessage wm = null;
 	IbisIdentifier origin = m.origin().ibis();
 
         soLogger.info("SATIN '" + satin.ident.name() + "': got so request");
 
 	try {
 	    objid = m.readString();
 
 	    // we must finish the message before doing communication. --Rob
 	    m.finish();
 	} catch (IOException e) {
 	    soLogger.warn("SATIN '" + satin.ident.name() 
 			       + "': got exception while reading"
 			       + " shared object request: " + e.getMessage());
 	}
     
     synchronized (satin) {
         satin.addToSORequestList(origin, objid);
     }
     
 /*
 	//	System.err.println("read objid: " + objid);
 	synchronized (satin) {
 	    so = satin.getSOReference(objid);
 	    //System.err.println("got object");
 	    v = satin.getVictimWait(origin);
 	    //System.err.println("got reply port");
        
 	    if (ASSERTS && so == null) {
 		soLogger.fatal("SATIN '" + satin.ident.name() 
 				   + "': EEEK, requested shared object: "
 				   + objid
 				   + " not found! Exiting..");
 		System.exit(1); // Failed assertion
 	    }
 
 	    if (SO_TIMING) {
 		soSerializationTimer = satin.createTimer();
 		soSerializationTimer.start();
 	    }
 
 	    //we need to hold the lock while writing the object
 	    //otherwise some update might change the state of the object
 	    //what's worse: the update might be executed only partially
 	    //before the object is sent
 	    try {
 		wm = v.newMessage();
 		wm.writeByte(SO_TRANSFER);
 		wm.writeObject(so);
 	    } catch (IOException e) {
 		soLogger.error("SATIN '" + satin.ident.name()
 				   + "': got exception while writing"
 				   + " shared object", e);
 	    }
 	}
 	try {
 	    size = wm.finish();
 	    //	    System.err.println("sent object");
 	} catch (IOException e) {
 	    soLogger.error("SATIN '" + satin.ident.name() 
 			       + "': got exception while sending"
 			       + " shared object", e);
 	}
 	//stats
 	satin.soTransfers++;
 	satin.soTransfersBytes += size;
 	if (SO_TIMING) {
 	    handleSOTransferTimer.stop();
 	    satin.handleSOTransferTimer.add(handleSOTransferTimer);
 	    soSerializationTimer.stop();
 	    satin.soSerializationTimer.add(soSerializationTimer);
 	}
     */
     }
 
     public void handleSOTransfer(ReadMessage m) { // normal so transfer (not exportObject)
 	SharedObject obj = null;
 	Timer soDeserializationTimer = null;
 
 	if (SO_TIMING) {
 	    soDeserializationTimer = satin.createTimer();
 	    soDeserializationTimer.start();
 	}
 	try {
 	    obj = (SharedObject) m.readObject();
 	} catch (IOException e) {
 	    soLogger.error("SATIN '" + satin.ident.name() 
 			       + "': got exception while reading"
 			       + " shared object", e);
 	} catch (ClassNotFoundException e) {
 	    soLogger.error("SATIN '" + satin.ident.name() 
 			       + "': got exception while reading"
 			       + " shared object", e);
 	}
 	if (SO_TIMING) {
 	    soDeserializationTimer.stop();
 	    satin.soDeserializationTimer.add(soDeserializationTimer);
 	}
 
         // no need to finish the read message here. 
         // We don't block and don't do any communication
 
 	satin.receiveObject(obj);
     }
 
     public void upcall(ReadMessage m) {
         SendPortIdentifier ident = m.origin();
 
         try {
             byte opcode = m.readByte();
 
             switch (opcode) {
             case EXIT:
                 if (commLogger.isDebugEnabled()) {
                     commLogger.debug("SATIN '" + satin.ident
                             + "': got exit message from "
                             + ident.ibis());
                 }
                 m.finish();
                 synchronized (satin) {
                     satin.exiting = true;
                     satin.notifyAll();
                 }
                 break;
             case EXIT_STAGE2:
                 if (commLogger.isDebugEnabled()) {
                     commLogger.debug("SATIN '" + satin.ident
                             + "': got exit2 message from "
                             + ident.ibis());
                 }
                 m.finish();
                 synchronized (satin) {
                     satin.exitStageTwo = true;
                     satin.notifyAll();
                 }
                 break;
             case BARRIER_REQUEST:
                 m.finish();
                 synchronized (satin) {
                     satin.barrierRequests++;
                     satin.notifyAll();
                 }
                 break;
             case EXIT_REPLY:
                 handleExitReply(m);
                 break;
             case STEAL_AND_TABLE_REQUEST:
             case ASYNC_STEAL_AND_TABLE_REQUEST:
             case STEAL_REQUEST:
             case ASYNC_STEAL_REQUEST:
             case BLOCKING_STEAL_REQUEST:
                 if (commLogger.isDebugEnabled() &&
                         (opcode == STEAL_AND_TABLE_REQUEST
                             || opcode == ASYNC_STEAL_AND_TABLE_REQUEST)) {
                     if (commLogger.isDebugEnabled()) {
                         commLogger.debug("SATIN '" + satin.ident
                                 + "': got table request from "
                                 + ident.ibis());
                     }
                 }
                 m.finish(); // must finish, we will send back a reply.
                 handleStealRequest(ident, opcode);
                 break;
             case STEAL_REPLY_FAILED:
             case STEAL_REPLY_SUCCESS:
             case ASYNC_STEAL_REPLY_FAILED:
             case ASYNC_STEAL_REPLY_SUCCESS:
             case STEAL_REPLY_FAILED_TABLE:
             case STEAL_REPLY_SUCCESS_TABLE:
             case ASYNC_STEAL_REPLY_FAILED_TABLE:
             case ASYNC_STEAL_REPLY_SUCCESS_TABLE:
             case BARRIER_REPLY:
                 handleReply(m, opcode);
                 break;
             case JOB_RESULT_NORMAL:
             case JOB_RESULT_EXCEPTION:
                 if (stealLogger.isDebugEnabled()) {
                     stealLogger.debug("SATIN '" + satin.ident
                             + "': got job result message from "
                             + ident.ibis());
                 }
 
                 handleJobResult(m, opcode);
                 break;
             case ABORT:
                 handleAbort(m);
                 break;
             case ABORT_AND_STORE:
                 handleAbortAndStore(m);
                 break;
             case TUPLE_ADD:
                 handleTupleAdd(m);
                 break;
             case TUPLE_DEL:
                 handleTupleDel(m);
                 break;
             case RESULT_REQUEST:
                 handleResultRequest(m);
                 break;
             case RESULT_PUSH:
                 handleResultPush(m);
                 break;
 	    case SO_REQUEST:
 		System.err.print("r");
 		handleSORequest(m);
 		break;
 	    case SO_TRANSFER:
 		System.err.print("t");
 		handleSOTransfer(m);
 		break;
             default:
                 commLogger.error("SATIN '" + satin.ident
                         + "': Illegal opcode " + opcode + " in MessageHandler");
                 // System.exit(1);
             }
         } catch (IOException e) {
             commLogger.warn("satin msgHandler upcall: " + e, e);
             // Ignore.
         }
     }
 }
