 /**
  * Copyright 2010 Sematext International
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.sematext.hbase.hut;
 
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * HBaseHUT {@link org.apache.hadoop.hbase.client.ResultScanner} implementation.
  * Use it when scanning records written as {@link com.sematext.hbase.hut.HutPut}s
  * TODO: needs refactoring: extract base class ResultScanner "wrapper" independent on the actual data source
  */
 public class HutResultScanner implements ResultScanner {
   private final ResultScanner resultScanner;
   private final ResultsAccessor resultsAccessor;
   private Result nonConsumed = null;
   private final UpdateProcessor updateProcessor;
   private boolean storeProcessedUpdates;
   private final HTable hTable;
   // Can be converted to local variable, but we want to reuse iterable instance
   private IterableRecords iterableRecords = new IterableRecords();
   // Can be converted to local variable, but we want to reuse processingResult instance
   private UpdateProcessingResultImpl processingResult = new UpdateProcessingResultImpl();
 
   private int minRecordsToProcess = 1;
 
   public HutResultScanner(ResultScanner resultScanner, UpdateProcessor updateProcessor) {
     this(resultScanner, updateProcessor, null, false);
   }
 
   public HutResultScanner(ResultScanner resultScanner, UpdateProcessor updateProcessor,
                           HTable hTable, boolean storeProcessedUpdates) {
     verifyInitParams(resultScanner, updateProcessor, hTable, storeProcessedUpdates);
     this.resultScanner = resultScanner;
     this.resultsAccessor = new ResultsAccessor();
     this.updateProcessor = updateProcessor;
     this.storeProcessedUpdates = storeProcessedUpdates;
     this.hTable = hTable;
   }
 
   public void setMinRecordsToProcess(int minRecordsToProcess) {
     this.minRecordsToProcess = minRecordsToProcess;
   }
 
   void verifyInitParams(ResultScanner resultScanner, UpdateProcessor updateProcessor, HTable hTable, boolean storeProcessedUpdates) {
     if (resultScanner == null) {
       throw new IllegalArgumentException("ResultScanner should NOT be null.");
     }
     if (updateProcessor == null) {
       throw new IllegalArgumentException("UpdateProcessor should NOT be null.");
     }
     if (storeProcessedUpdates && hTable == null) {
       throw new IllegalArgumentException("HTable is null, but access to it required for storing processed updates back.");
     }
   }
 
   @Override
   public Result next() throws IOException {
     Result firstResult = nonConsumed != null ? nonConsumed : resultsAccessor.next();
     nonConsumed = null;
 
     if (firstResult == null) {
       return firstResult;
     }
 
     Result nextToFirstResult = resultsAccessor.next();
     if (nextToFirstResult == null) {
       return firstResult;
     }
 
     boolean mergeNeeded = isMergeNeeded(firstResult.getRow(), nextToFirstResult.getRow());
     if (mergeNeeded && minRecordsToProcess > 2) {
       // fetching (minRecordsToProcess)th to see if there are at least minRecordsToProcess records to merge
       Result nth = resultsAccessor.getNth(minRecordsToProcess - 2);
       if (nth == null || !isMergeNeeded(firstResult.getRow(), nth.getRow())) {
         resultsAccessor.advanceThruPrefetchedWhileOriginalKeySame(minRecordsToProcess - 2, firstResult.getRow());
         return firstResult;
       }
     }
 
     if (!mergeNeeded) {  // nothing to process
       nonConsumed = nextToFirstResult;
       return firstResult;
     }
 
     iterableRecords.init(firstResult, nextToFirstResult);
     // TODO: allow decide to skip merging at processing time
 
     Result result;
     // hook for fastforwarding thru records with particular original key in case no need to merge them
     // TODO: modify API of processor to return true/false instead of extra method?
     // TODO: adjust API of updateProcessor.isMergeNeeded method (add offset/length params) to avoid creating extra objects
     if (updateProcessor.isMergeNeeded(HutRowKeyUtil.getOriginalKey(firstResult.getRow()))) {
       processingResult.init(firstResult.getRow());
       updateProcessor.process(iterableRecords, processingResult);
 
       result = processingResult.getResult();
       // TODO: handle empty processing result better, code refactoring needed
       if (storeProcessedUpdates && !processingResult.isEmpty()) {
         storeProcessedUpdates(result, iterableRecords.iterator.lastRead);
       }
     } else {
       // actually this should be ignored, as the hook for skipping processing is for compaction job only
       result = firstResult;
     }
 
     // TODO: allow client code specify skipping this?
     // Reading records of this group to the end
     while (iterableRecords.iterator.hasNext()) {
       iterableRecords.iterator.next();
     }
 
     // TODO: if result is empty we should not return it, but rather fetch next
     return result;
   }
 
   protected boolean isMergeNeeded(byte[] firstKey, byte[] secondKey) {
     return HutRowKeyUtil.sameOriginalKeys(firstKey, secondKey);
   }
 
   Result fetchNext() throws IOException {
     return resultScanner.next(); // TODO: make sure caching is used to reduce RPC requests number
   }
 
   @Override
   public Result[] next(int nbRows) throws IOException {
     // Identical to HTable.ClientScanner implementation
     // Collect values to be returned here
     ArrayList<Result> resultSets = new ArrayList<Result>(nbRows);
     for(int i = 0; i < nbRows; i++) {
       Result next = next();
       if (next != null) {
         resultSets.add(next);
       } else {
         break;
       }
     }
     return resultSets.toArray(new Result[resultSets.size()]);
 
   }
   
   @Override
   public Iterator<Result> iterator() {
     // Identical to HTable.ClientScanner implementation
     return new Iterator<Result>() {
       // The next RowResult, possibly pre-read
       Result next = null;
 
       // return true if there is another item pending, false if there isn't.
       // this method is where the actual advancing takes place, but you need
       // to call next() to consume it. hasNext() will only advance if there
       // isn't a pending next().
       public boolean hasNext() {
         if (next == null) {
           try {
             next = HutResultScanner.this.next();
             return next != null;
           } catch (IOException e) {
             throw new RuntimeException(e);
           }
         }
         return true;
       }
 
       // get the pending next item and advance the iterator. returns null if
       // there is no next item.
       public Result next() {
         // since hasNext() does the real advancing, we call this to determine
         // if there is a next before proceeding.
         if (!hasNext()) {
           return null;
         }
 
         // if we get to here, then hasNext() has given us an item to return.
         // we want to return the item and then null out the next pointer, so
         // we use a temporary variable.
         Result temp = next;
         next = null;
         return temp;
       }
 
       public void remove() {
         throw new UnsupportedOperationException();
       }
     };
   }
 
   @Override
   public void close() {
     resultScanner.close();
   }
 
   static class UpdateProcessingResultImpl implements UpdateProcessingResult {
     private KeyValue[] kvs; // TODO: consider using a List
     private byte[] row;
 
     public void init(byte[] row) {
       this.row = row;
       this.kvs = new KeyValue[0];
     }
 
     @Override
     public void add(KeyValue[] kvs) {
       // TODO: not efficient! underlying kvs array will be expanded multiple times instead of onces
       for (KeyValue kv : kvs) {
         add(kv);
       }
     }
 
     // TODO: revise this method implementation, it can be done better (and/or more efficient)
     // TODO: compare with add(byte[] colFam, byte[] qualifier, byte[] value) method, possible option for refactoring
     @Override
     public void add(KeyValue kvToAdd) {
       overrideRow(kvToAdd, row);
 
       // TODO: do we really need doing merge here? Won't it override things automatically? Also: we compromise support of multiple versions here
       boolean found = false;
       for (int i = 0; i < kvs.length; i++) {
         KeyValue kv = kvs[i];
         // TODO: make use of timestamp value when comparing?
         // TODO: use KVComparator?
         if( Bytes.equals(kvToAdd.getFamily(), kv.getFamily()) && Bytes.equals(kvToAdd.getQualifier(), kv.getQualifier())) {
           kvs[i] = kvToAdd;
           found = true;
           break; // TODO: do we need to update here other KeyValues (or just most recent one)?
         }
       }
       if (!found) {
         kvs = Arrays.copyOf(kvs, kvs.length + 1); // TODO: looks like not vey optimal
         kvs[kvs.length - 1] = kvToAdd;
       }
     }
 
     // TODO: revise this method implementation, it can be done better (and/or more efficient)
     @Override
     public void add(byte[] colFam, byte[] qualifier, byte[] value) {
       // TODO: do we really need doing merge here? Won't it override things automatically? Also: we compromise support of multiple versions here
       // TODO: Defer merging to getResult method?
       boolean found = false;
       for (int i = 0; i < kvs.length; i++) {
         KeyValue kv = kvs[i];
         // TODO: make use of timestamp value when comparing?
         if(Bytes.equals(colFam, kv.getFamily()) && Bytes.equals(qualifier, kv.getQualifier())) {
           KeyValue merged = new KeyValue(row, colFam, qualifier, kv.getTimestamp(), value);
           kvs[i] = merged;
           found = true;
           break; // TODO: do we need to update here other KeyValues (or just most recent one)?
         }
       }
       if (!found) {
         kvs = Arrays.copyOf(kvs, kvs.length + 1); // TODO: looks like not vey optimal
         kvs[kvs.length - 1] = new KeyValue(row, colFam, qualifier, value);
       }
     }
 
     // TODO: revise this method implementation, it can be done better (and/or more efficient)
     @Override
     public void delete(byte[] colFam, byte[] qualifier) {
       // TODO: Defer merging to getResult method?
       for (int i = 0; i < kvs.length; i++) {
         KeyValue kv = kvs[i];
         // TODO: make use of timestamp value when comparing?
         if(Bytes.equals(colFam, kv.getFamily()) && Bytes.equals(qualifier, kv.getQualifier())) {
           // TODO: looks like not vey optimal
           KeyValue[] newKvs = new KeyValue[kvs.length - 1];
           System.arraycopy(kvs, 0, newKvs, 0, i);
           System.arraycopy(kvs, i, newKvs, i + 1, newKvs.length - i);
           kvs = newKvs;
           break; // TODO: do we need to update here other KeyValues (or just most recent one)?
         }
       }
     }
 
     public boolean isEmpty() {
       // TODO: can row be actualy null?
       return kvs == null || kvs.length == 0 || row == null;
     }
 
     public Result getResult() {
      // Result object relies on kvs to be sorted
      Arrays.sort(kvs, KeyValue.COMPARATOR);
       return new Result(kvs);
     }
   }
 
   private class ResultsAccessor {
     private LinkedList<Result> prefetched = new LinkedList<Result>();
 
     public Result next() throws IOException {
       if (prefetched.size() == 0) {
         return fetchNext();
       }
 
       return prefetched.pollFirst();
     }
 
     // TODO: rename to smth like "get (but don't fetch) Nth from ahead"
     public Result getNth(int i) throws IOException {
       if (prefetched.size() < i) {
         for (int k = 0; k < i - prefetched.size(); k++) {
           Result next = fetchNext();
           if (next == null) {
             break;
           }
           prefetched.add(next);
         }
       }
 
       if (prefetched.size() < i) {
         return null;
       }
 
       return (prefetched.get(i - 1));
     }
 
     public void advanceThruPrefetchedWhileOriginalKeySame(int maxToAdvance, byte[] hutKey) {
       // TODO: any faster way?
       for (int i = 0; i < maxToAdvance; i++) {
         Result res = prefetched.pollFirst();
         if (res == null) {
           return;
         }
 
         if (!HutRowKeyUtil.sameOriginalKeys(hutKey, res.getRow())) {
           // stop and put back the element
           prefetched.addFirst(res);
           return;
         }
       }
     }
   }
 
   private class IterableRecords implements Iterable<Result> {
     // reusing iterator instance
     private IteratorImpl iterator = new IteratorImpl();
 
     // Accepts at least two records: no point in starting processing unless we have more than one
     public void init(Result first, Result nextToFirst) {
       this.iterator.firstRecordKey = first.getRow();
       this.iterator.next = null;
       this.iterator.exhausted = false;
       this.iterator.lastRead = null;
       this.iterator.doFirstHasNext(first, nextToFirst);
     }
 
     private class IteratorImpl implements Iterator<Result> {
       private byte[] firstRecordKey;
       private Result next; // next record prepared for fetching
       private Result lastRead;
       boolean exhausted;
 
       private void doFirstHasNext(Result first, Result nextToFirst) {
         try {
           next = first;
           // we start directly with the last steps of hasNext as all other conditions were checked in resultScanner's next()
           // Skipping those which were processed but haven't deleted yet (very small chance to face this) -
           // skipping records that are stored before processing result
           while (HutRowKeyUtil.sameRecords(first.getRow(), nextToFirst.getRow())) {
             next = nextToFirst;
             nextToFirst = resultsAccessor.next();
             if (nextToFirst == null) {
               return;
             }
           }
 
           nonConsumed = nextToFirst;
 
         } catch (IOException e) {
           throw new RuntimeException(e);
         }
       }
 
       // return true if there is another item pending, false if there isn't.
       // this method is where the actual advancing takes place, but you need
       // to call next() to consume it. hasNext() will only advance if there
       // isn't a pending next().
       @Override
       public boolean hasNext() {
         if (exhausted) {
           return false;
         }
         if (next != null) {
           return true;
         }
         try {
           Result nextCandidate = nonConsumed != null ? nonConsumed : resultsAccessor.next();
           if (nextCandidate == null) {
             exhausted = true;
             return false;
           }
 
           // TODO: nextCandidate.getRow() reads all fields (HBase internal implementation), but we actually may need only row here
           boolean mergeNeeded = isMergeNeeded(firstRecordKey, nextCandidate.getRow());
 
           if (!mergeNeeded) {
             nonConsumed = nextCandidate;
             exhausted = true;
             return false;
           }
 
           // Skipping those which where already processed but hasn't
           // been deleted yet to keep results consistent.
           // There's tiny chance for that: may occur because writing processed interval's data
           // and deleting all processed records in the interval is not atomic.
           // Also allows not to delete records at all during compaction
           // (in case we want and able to process them more than once).
           while (lastRead != null && mergeNeeded && !HutRowKeyUtil.isAfter(nextCandidate.getRow(), lastRead.getRow())) {
             nextCandidate = resultsAccessor.next();
             if (nextCandidate == null) {
               exhausted = true;
               return false;
             }
             mergeNeeded = isMergeNeeded(firstRecordKey, nextCandidate.getRow());
           }
 
           if (!mergeNeeded) {
             nonConsumed = nextCandidate;
             exhausted = true;
             return false;
           }
 
           next = nextCandidate;
           nonConsumed = null;
 
           // Skipping those which were processed but haven't deleted yet (very small chance to face this)
           // skipping records that are stored before processing result
           Result afterNextCandidate = resultsAccessor.next();
           if (afterNextCandidate == null) {
             return true;
           }
 
           while (HutRowKeyUtil.sameRecords(nextCandidate.getRow(), afterNextCandidate.getRow())) {
             next = afterNextCandidate;
             afterNextCandidate = resultsAccessor.next();
             if (afterNextCandidate == null) {
               return true;
             }
           }
 
           nonConsumed = afterNextCandidate;
 
           return true;
         } catch (IOException e) {
           throw new RuntimeException(e);
         }
       }
 
       // get the pending next item and advance the iterator. returns null if
       // there is no next item.
       public Result next() {
         // since hasNext() does the real advancing, we call this to determine
         // if there is a next before proceeding.
         if (!hasNext()) {
           return null;
         }
 
         // if we get to here, then hasNext() has given us an item to return.
         // we want to return the item and then null out the next pointer, so
         // we use a temporary variable.
         lastRead = next;
         next = null;
         return lastRead;
       }
 
       @Override
       public void remove() {
         throw new UnsupportedOperationException();
       }
     }
 
     @Override
     public Iterator<Result> iterator() {
       return iterator;
     }
   }
 
   private void storeProcessedUpdates(Result processingResult, Result last) throws IOException {
     // processing result was stored in the first record of the processed interval,
     // hence we can utilize its write time as start time for the compressed interval
     byte[] firstRow = processingResult.getRow();
     byte[] lastRow = last.getRow();
     Put put = createPutWithProcessedResult(processingResult, firstRow, lastRow);
 
 
     store(put);
     deleteProcessedRecords(processingResult.getRow(), lastRow, put.getRow());
   }
 
   // TODO: move this method out of this class? (looks like utility method)
   static Put createPutWithProcessedResult(Result processingResult, byte[] firstRow, byte[] lastRow) throws IOException {
     // adjusting row, so that it "covers" interval from first record to last record
     byte[] row = Arrays.copyOf(firstRow, firstRow.length);
     HutRowKeyUtil.setIntervalEnd(row, lastRow); // can row here remain the same?
 
     return createPutWithProcessedResult(processingResult, row);
   }
 
   private static Put createPutWithProcessedResult(Result processingResult, byte[] row) throws IOException {
     Put put = new Put(row);
     for (KeyValue kv : processingResult.raw()) {
       // using copying here, otherwise processingResult is affected when its
       // keyvalues are changed. TODO: think over better approach? Previously same kv was used and things went well
       byte[] kvBytes = Arrays.copyOfRange(kv.getBuffer(), kv.getOffset(), kv.getOffset() + kv.getLength());
       KeyValue toWrite = new KeyValue(kvBytes);
       overrideRow(toWrite, row);
       put.add(toWrite);
     }
     return put;
   }
 
   void store(Put put) throws IOException {
     hTable.put(put);
   }
 
   // NOTE: this works only when rows has the same length, and doesn't invalidate row cache
   static void overrideRow(KeyValue kv, byte[] row) {
     // TODO: Does it makes sense to check if there's need for overriding first? Will it be more efficient?
     System.arraycopy(row, 0, kv.getBuffer(), kv.getRowOffset(), row.length);
 
   }
 
   void deleteProcessedRecords(byte[] firstInclusive, byte[] lastInclusive, byte[] processingResultToLeave) throws IOException {
     HTableUtil.deleteRange(hTable, firstInclusive, lastInclusive, processingResultToLeave);
   }
 
 }
