 package com.totalchange.bunman.cddb.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.pool.ObjectPool;
 
 import com.samskivert.net.cddb.CDDB;
 import com.samskivert.net.cddb.CDDBException;
 import com.samskivert.net.cddb.CDDB.Detail;
 import com.totalchange.bunman.cddb.CddbQuerier;
 import com.totalchange.bunman.cddb.CddbQuerier.Listener;
 import com.totalchange.bunman.cddb.CddbResult;
 
 final class CddbRunnable implements Runnable {
     private ObjectPool<CDDB> cddbPool;
     private String id;
     private CddbQuerier.Listener listener;
 
    CddbRunnable(ObjectPool<CDDB> cddbPool, String id,
            Listener listener) {
         super();
         this.cddbPool = cddbPool;
         this.id = id;
         this.listener = listener;
     }
 
     public void run() {
         List<CddbResult> results = new ArrayList<CddbResult>();
         try {
             CDDB cddb = cddbPool.borrowObject();
             try {
                 String[] categories = cddb.lscat();
                 for (String category : categories) {
                     try {
                         Detail detail = cddb.read(category, id);
                         results.add(new CddbResultImpl(detail));
                     } catch (CDDBException cddbEx) {
                         // 401 indicates not found which is ok - we'll go onto
                         // the next category. Anything else and we'll fail.
                         if (cddbEx.getCode() != 401) {
                             throw cddbEx;
                         }
                     }
                 }
             } catch (IOException ioEx) {
                 cddbPool.invalidateObject(cddb);
                 throw ioEx;
             } finally {
                 cddbPool.returnObject(cddb);
             }
         } catch (IOException ioEx) {
             listener.error(ioEx);
         } catch (Throwable th) {
            listener.error(new IOException(th));
         }
     }
 }
