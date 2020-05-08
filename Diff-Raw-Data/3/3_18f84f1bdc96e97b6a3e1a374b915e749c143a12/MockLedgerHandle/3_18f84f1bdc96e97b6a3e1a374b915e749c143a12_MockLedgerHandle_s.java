 package org.apache.bookkeeper.client;
 
 import static org.apache.bookkeeper.mledger.util.VarArgs.va;
 
 import java.security.GeneralSecurityException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Queue;
 
 import org.apache.bookkeeper.client.AsyncCallback.AddCallback;
 import org.apache.bookkeeper.client.AsyncCallback.CloseCallback;
 import org.apache.bookkeeper.client.AsyncCallback.ReadCallback;
 import org.apache.bookkeeper.client.BookKeeper.DigestType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.google.inject.internal.Lists;
 
 public class MockLedgerHandle extends LedgerHandle {
 
     final ArrayList<LedgerEntry> entries = Lists.newArrayList();
     final MockBookKeeper bk;
     final long id;
     final DigestType digest;
     final byte[] passwd;
     long lastEntry = -1;
     boolean fenced = false;
 
     MockLedgerHandle(MockBookKeeper bk, long id, DigestType digest, byte[] passwd) throws GeneralSecurityException {
         super(bk, id, new LedgerMetadata(3, 3, 2, DigestType.MAC, "".getBytes()), DigestType.MAC, "".getBytes());
         this.bk = bk;
         this.id = id;
         this.digest = digest;
         this.passwd = Arrays.copyOf(passwd, passwd.length);
     }
 
     @Override
     public void asyncClose(CloseCallback cb, Object ctx) {
         if (bk.getProgrammedFailStatus()) {
             cb.closeComplete(bk.failReturnCode, this, ctx);
             return;
         }
 
         fenced = true;
         cb.closeComplete(0, this, ctx);
     }
 
     @Override
     public void asyncReadEntries(final long firstEntry, final long lastEntry, final ReadCallback cb, final Object ctx) {
         bk.executor.execute(new Runnable() {
             public void run() {
                 if (bk.getProgrammedFailStatus()) {
                     cb.readComplete(bk.failReturnCode, MockLedgerHandle.this, null, ctx);
                     return;
                 } else if (bk.isStopped()) {
                     log.debug("Bookkeeper is closed!");
                     cb.readComplete(-1, MockLedgerHandle.this, null, ctx);
                     return;
                 }
 
                 log.debug("readEntries: first={} last={} total={}", va(firstEntry, lastEntry, entries.size()));
                 final Queue<LedgerEntry> seq = new ArrayDeque<LedgerEntry>();
                 long entryId = firstEntry;
                 while (entryId <= lastEntry && entryId < entries.size()) {
                     seq.add(entries.get((int) entryId++));
                 }
 
                 log.debug("Entries read: {}", seq);
 
                 try {
                     Thread.sleep(1);
                 } catch (InterruptedException e) {
                 }
 
                 cb.readComplete(0, MockLedgerHandle.this, new Enumeration<LedgerEntry>() {
                     public boolean hasMoreElements() {
                         return !seq.isEmpty();
                     }
 
                     public LedgerEntry nextElement() {
                         return seq.remove();
                     }
 
                 }, ctx);
             }
         });
     }
 
     @Override
     public long addEntry(byte[] data) throws InterruptedException, BKException {
         bk.checkProgrammedFail();
 
         if (fenced) {
             throw BKException.create(BKException.Code.LedgerFencedException);
         }
 
         if (bk.isStopped()) {
             throw BKException.create(BKException.Code.NoBookieAvailableException);
         }
 
         lastEntry = entries.size();
         entries.add(new MockLedgerEntry(ledgerId, lastEntry, data));
         return lastEntry;
     }
 
     @Override
     public void asyncAddEntry(final byte[] data, final AddCallback cb, final Object ctx) {
         bk.executor.execute(new Runnable() {
             public void run() {
                 if (bk.getProgrammedFailStatus()) {
                     cb.addComplete(bk.failReturnCode, MockLedgerHandle.this, INVALID_ENTRY_ID, ctx);
                     return;
                 }
                 if (bk.isStopped()) {
                     cb.addComplete(-1, MockLedgerHandle.this, INVALID_ENTRY_ID, ctx);
                     return;
                 }
 
                 try {
                     Thread.sleep(1);
                 } catch (InterruptedException e) {
                 }
 
                 if (fenced) {
                     cb.addComplete(BKException.Code.LedgerFencedException, MockLedgerHandle.this,
                             LedgerHandle.INVALID_ENTRY_ID, ctx);
                 } else {
                     lastEntry = entries.size();
                     LedgerEntry entry = new MockLedgerEntry(ledgerId, lastEntry, data);
                     entries.add(entry);
                     cb.addComplete(0, MockLedgerHandle.this, lastEntry, ctx);
                 }
             }
         });
     }
 
     @Override
     public long getId() {
         return ledgerId;
     }
 
     @Override
     public long getLastAddConfirmed() {
         return lastEntry;
     }
 
     @Override
     public long getLength() {
         long length = 0;
         for (LedgerEntry entry : entries) {
             length += entry.getLength();
         }
 
         return length;
     }
 
     private static final Logger log = LoggerFactory.getLogger(MockLedgerHandle.class);
 
 }
