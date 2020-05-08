 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package memsim_java;
 /**
  *
  * @author zalatnaicsongor
  */
 public class CacheLine {
 
     private int sequence = 0;
     private int line;
     CacheRow[] cacheRowArray;
 
     public int getLine() {
         return line;
     }
 
     public void destroyAll() {
         for (CacheRow cr: cacheRowArray) {
             cr.discard();
         }
     }
 
     public void resetSequence() {
         this.sequence = 0;
     }
 
     public void resetUsageData() {
         for (CacheRow cr: cacheRowArray) {
             cr.resetUsageData();
         }
     }
 
     public int getNextSequence() {
         int retval = this.sequence;
         this.sequence++;
         return retval;
     }
 
     public CacheLine(int line, int associativity) {
         this.line = line;
         this.cacheRowArray = new CacheRow[associativity];
         //for (int i = 0; i < Cache.getInstance().getAssociativity(); i++) {
         //    this.cacheRowArray.set(i, null);
         //}
     }
 
     public CacheRow getRowByTag(int tag) throws CacheRowNotFoundException {
         CacheRow retval = null;
         for (CacheRow cr: this.cacheRowArray) {
             if (cr.getTag() == tag) {
                 retval = cr;
                 break;
             }
         }
         if (retval == null) {
             throw new CacheRowNotFoundException("Nem találtam meg a sort");
         } else if (!retval.isValid()) {
             throw new CacheRowNotFoundException("A sor nem valid");
         }
         return retval;
     }
 
     public CacheRow createRow(int tag) {
         CacheRow retval = null;
         while (this.getFreeRowsCount() <= 0) {
             Cache.getInstance().getRowDiscardStrategy().findRow(this).discard();
             resetUsageData();
         }
         retval = new CacheRow(tag, this);
         this.cacheRowArray[this.findFreeIndex()] = retval;
         return retval;
     }
 
     public int findFreeIndex() {
         int index = -1;
         for (int i = 0; i < Cache.getInstance().getAssociativity(); i++) {
             if (this.cacheRowArray[i] == null || this.cacheRowArray[i].isValid()) {
                 return i;
             }
         }
         return index;
     }
 
     public int getFreeRowsCount() {
         int retval = 0;
         for (CacheRow cr: this.cacheRowArray) {
             if (cr == null || !cr.isValid()) {
                 retval++;
             }
         }
         return retval;
     }
 
     public CacheRow getMinUsageCountCacheRow() {
         CacheRow retval = this.cacheRowArray[0];
         for (CacheRow cr: this.cacheRowArray) {
             if (cr.getUseCount() < retval.getUseCount()) {
                 retval = cr;
             }
         }
         return retval;
     }
 
     public CacheRow getMaxUsageCountCacheRow() {
         CacheRow retval = this.cacheRowArray[0];
         for (CacheRow cr: this.cacheRowArray) {
             if (cr.getUseCount() > retval.getUseCount()) {
                 retval = cr;
             }
         }
         return retval;
     }
 
     public CacheRow getMinUsageSequenceCacheRow() {
         CacheRow retval = this.cacheRowArray[0];
         for (CacheRow cr: this.cacheRowArray) {
             if (cr.getUseSequence() < retval.getUseSequence()) {
                 retval = cr;
             }
         }
         return retval;
     }
 
    public CacheRow getMaxUsageSequenceCacheRow() {
         CacheRow retval = this.cacheRowArray[0];
         for (CacheRow cr: this.cacheRowArray) {
             if (cr.getUseSequence() > retval.getUseSequence()) {
                 retval = cr;
             }
         }
         return retval;
     }
 
 }
