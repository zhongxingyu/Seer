 package com.aconex.scrutineer.elasticsearch;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import com.aconex.scrutineer.IdAndVersion;
 import com.aconex.scrutineer.LogUtils;
 import com.fasterxml.sort.Sorter;
 import com.google.common.io.CountingInputStream;
 import org.apache.log4j.Logger;
 
 public class ElasticSearchSorter {
 
     private static final Logger LOG = LogUtils.loggerForThisClass();
 
     private final Sorter<IdAndVersion> sorter;
 
     public ElasticSearchSorter(Sorter<IdAndVersion> sorter) {
         this.sorter = sorter;
     }
 
     public void sort(InputStream inputStream, OutputStream outputStream) {
         long begin = System.currentTimeMillis();
         CountingInputStream countingInputStream = new CountingInputStream(inputStream);
        doSort(inputStream, outputStream);
         LogUtils.infoTimeTaken(LOG, begin, countingInputStream.getCount(), "Sorted stream of %d bytes", countingInputStream.getCount());
     }
 
     private void doSort(InputStream inputStream, OutputStream outputStream ) {
         try {
             sorter.sort(inputStream,outputStream);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 }
