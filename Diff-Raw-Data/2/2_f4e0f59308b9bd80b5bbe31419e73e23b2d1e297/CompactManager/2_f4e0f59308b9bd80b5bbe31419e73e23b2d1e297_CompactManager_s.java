 package ru.compscicenter.lidis;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 
 /**
  * Author: Vasiliy Khomutov
  * Date: 14.10.2013
  */
 public class CompactManager implements Runnable {
 
     public static final double MAX_DISK_TABLES = 5;
 
     private final Lock changeTablesLock;
     private final DiskTableAccessor diskTableAccessor;
 
     private volatile boolean stopSignal = false;
 
     public CompactManager(DiskTableAccessor diskTableAccessor, Lock changeTablesLock) {
         this.changeTablesLock = changeTablesLock;
         this.diskTableAccessor = diskTableAccessor;
     }
 
 
     private void compactTables() {
         List<DiskTable> diskTables = takeSmallest(diskTableAccessor.diskTables(), 2);
 
 
         DiskTable compact;
         try {
             Path newDiskTablePAth = diskTableAccessor.newPathForTable();
             System.out.println("NODE: INFO: Compacting " + diskTables.size() + " tables to new " + newDiskTablePAth);
             Compactor compactor = new Compactor(newDiskTablePAth, diskTables, false);
             compact = compactor.compact();
         } catch (IOException e) {
             System.out.println("NODE: EXCEPTION: " + e);
             throw new RuntimeException(e);
         }
 
         changeTablesLock.lock();
         try {
             diskTableAccessor.replaceDiskTables(diskTables, compact);
         } finally {
             changeTablesLock.unlock();
         }
 
         try {
             for (DiskTable diskTable : diskTables) {
                 diskTable.close();
                 Files.delete(diskTable.file());
             }
         } catch (IOException e) {
             System.out.println("NODE: EXCEPTION: " + e);
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public void run() {
         System.out.println("NODE: INFO: Starting CompactManager");
 
         while (!stopSignal) {
             if (needsCompaction()) {
                 compactTables();
             }
         }
     }
 
     public void stop() {
         stopSignal = true;
     }
 
     private boolean needsCompaction() {
         return diskTableAccessor.tablesCount() > MAX_DISK_TABLES;
     }
 
     private List<DiskTable> takeSmallest(List<DiskTable> tableList, int number) {
         List<DiskTable> sortedBySizeTables = new ArrayList<>(tableList);
         Collections.sort(sortedBySizeTables, new Comparator<DiskTable>() {
             @Override
             public int compare(DiskTable o1, DiskTable o2) {
                 if (o1.dataSize() < o2.dataSize()) {
                     return -1;
                 } else if (o1.dataSize() > o2.dataSize()) {
                     return 1;
                 } else {
                     return 0;
                 }
             }
         });
 
         List<DiskTable> smallestTables = new ArrayList<>();
        for (int index = 0; index < number; index++) {
             smallestTables.add(sortedBySizeTables.get(index));
         }
 
         return smallestTables;
     }
 }
