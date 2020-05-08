 package ch.x42.terye.mk.hbase;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.hadoop.hbase.DoNotRetryIOException;
 import org.apache.hadoop.hbase.TableNotFoundException;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.jackrabbit.mk.api.MicroKernelException;
 
 import ch.x42.terye.mk.hbase.HBaseMicroKernelSchema.JournalTable;
 
 public class Journal {
 
     // grace period for long-taking tries of revisions (a commit, where the
     // successful try took longer than the grace period might not be seen by
     // other microkernels)
     public static final int GRACE_PERIOD = 800;
 
     private HTable table;
     private final int timeout;
     public LinkedHashSet<Long> journal;
     private long headRevisionId;
     private List<Long> newRevisionIds;
 
     private Thread thread;
     private boolean done = false;
     private Object timeoutMonitor;
     private Object updateMonitor;
     private boolean locked;
 
     public Journal(HTable journalTable, int timeout) throws IOException {
         table = journalTable;
         this.timeout = timeout;
         journal = new LinkedHashSet<Long>();
         journal.add(0L);
         headRevisionId = 0L;
         newRevisionIds = new LinkedList<Long>();
         timeoutMonitor = new Object();
         updateMonitor = new Object();
         locked = false;
 
         // start update thread
         Updater updater = new Updater();
         updater.updateJournal();
         thread = new Thread(updater);
         thread.setDaemon(true);
         thread.start();
     }
 
     /**
      * Synchronous update of the journal.
      */
     public void update() {
         try {
             synchronized (updateMonitor) {
                 synchronized (timeoutMonitor) {
                     // wake thread up in case it sleeps
                     timeoutMonitor.notify();
                 }
                 // wait to be notified
                 updateMonitor.wait();
             }
         } catch (InterruptedException e) {
             // thread has been notified
         }
     }
 
     public long getHeadRevisionId() {
         synchronized (journal) {
             return headRevisionId;
         }
     }
 
     /**
      * Returns the complete journal.
      */
     public LinkedList<Long> get() {
         synchronized (journal) {
             return new LinkedList<Long>(journal);
         }
     }
 
     /**
      * Returns the journal up to and including the specified revision id.
      * 
      * @param revisionId a revision id
      * @return the journal up to and including the specified revision id
      * @throws MicroKernelException when specified revision id is not present in
      *             journal
      */
     public LinkedList<Long> get(long revisionId) {
         // get current journal
         LinkedList<Long> revisionIds = get();
         // assemble all revision ids up to and including 'revisionId'
         LinkedList<Long> journal = new LinkedList<Long>();
         boolean found = false;
         Iterator<Long> iterator = revisionIds.iterator();
         while (iterator.hasNext()) {
             Long id = iterator.next();
             journal.add(id);
             if (id == revisionId) {
                 found = true;
                 break;
             }
         }
         if (!found) {
             throw new MicroKernelException("Unknown revision id " + revisionId);
         }
         return journal;
     }
 
     public void addRevisionId(long revisionId) {
         synchronized (journal) {
             if (!journal.contains(revisionId)) {
                 journal.add(revisionId);
                 headRevisionId = revisionId;
             }
         }
     }
 
     /**
      * Locks the journal. The update thread might still fetch new revisions,
      * however they are not added to journal until unlocked.
      */
     public void lock() {
         if (locked) {
             throw new IllegalStateException("Journal is already locked");
         }
         synchronized (newRevisionIds) {
             locked = true;
         }
     }
 
     public void unlock() {
         if (!locked) {
             throw new IllegalStateException("Journal is not locked");
         }
         synchronized (newRevisionIds) {
             for (Long id : newRevisionIds) {
                 addRevisionId(id);
             }
             locked = false;
         }
     }
 
     public void dispose() throws IOException {
         done = true;
         synchronized (timeoutMonitor) {
             timeoutMonitor.notify();
         }
         try {
             // wait for thread to die
             thread.join();
         } catch (InterruptedException e) {
             // ignore
         }
         table.close();
     }
 
     private class Updater implements Runnable {
 
         private Long lastTimeRead;
 
         private void updateJournal() throws IOException {
             Scan scan = new Scan();
             if (lastTimeRead != null) {
                 // only scan what hasn't been scanned yet (giving potential
                 // long-taking revisions a grace period of GRACE_PERIOD ms)
                 long timestamp = lastTimeRead - RevisionIdGenerator.EPOCH
                         - GRACE_PERIOD;
                 scan.setStartRow(Bytes.toBytes(timestamp << 24));
             }
             lastTimeRead = System.currentTimeMillis();
            List<Long> revisionIds = new LinkedList<Long>();
             ResultScanner scanner = table.getScanner(scan);
             Iterator<Result> iterator = scanner.iterator();
             while (iterator.hasNext()) {
                 Result result = iterator.next();
                 // discard uncommitted revisions
                 if (!Bytes.toBoolean(result.getValue(
                         JournalTable.CF_DATA.toBytes(),
                         JournalTable.COL_COMMITTED.toBytes()))) {
                     continue;
                 }
                revisionIds.add(Bytes.toLong(result.getRow()));
             }
             scanner.close();
             synchronized (newRevisionIds) {
                 if (locked) {
                    newRevisionIds.addAll(revisionIds);
                 } else {
                    for (Long id : revisionIds) {
                         addRevisionId(id);
                     }
                 }
             }
         }
 
         @Override
         public void run() {
             while (!done) {
                 try {
                     updateJournal();
                     synchronized (updateMonitor) {
                         // interrupt thread waiting in update method
                         updateMonitor.notify();
                     }
                     synchronized (timeoutMonitor) {
                         // sleep
                         timeoutMonitor.wait(timeout);
                     }
                 } catch (InterruptedException e) {
                     // thread has been interrupted
                 } catch (TableNotFoundException e) {
                     // might happen if journal table is being dropped
                     return;
                 } catch (DoNotRetryIOException e) {
                     // might happen if journal table is being dropped
                     return;
                 } catch (IOException e) {
                     // XXX: log exception
                 }
             }
         }
     }
 
 }
