 /**
  * Copyright (c) 2011 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.common.utils.zip;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.concurrent.Callable;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import org.apache.commons.io.IOUtils;
 
 public class EntryCountBasedZipPartitioner implements ZipPartitioner
 {
    public Collection<Callable<Integer>> computePartitions(ZipInputStreamFactory streamFactory,
       ZipEntryHandler entryHandler, int numberOfThreads) throws IOException
    {
       final int numberOfBytes = streamFactory.getNumberOfBytes();
       final int numberOfEntries = getNumberOfEntries(streamFactory);
 
       final int numberOfPartitions = computeNumberOfPartitions(numberOfThreads, numberOfBytes, numberOfEntries);
 
       int jobsSize = numberOfEntries / numberOfPartitions;
      int leftover = numberOfPartitions % numberOfPartitions;
      
       final Collection<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
       if (jobsSize > 0)
       {
          int startIdx = 0;
          int endIdx = -1;
          for (int i = 0; i < numberOfPartitions; i++)
          {
             startIdx = endIdx + 1;
             endIdx += jobsSize;
             if (leftover > 0)
             {
                endIdx++;
                leftover--;
             }
             RangedZipPartitionProcessor task = new RangedZipPartitionProcessor(streamFactory, entryHandler, startIdx,
                endIdx);
             tasks.add(task);
          }
       }
       return tasks;
    }
 
    public int computeNumberOfPartitions(int numberOfThreads, int numberOfBytes, int numberOfEntries)
    {
       if (numberOfEntries < 2)
       {
          return 1;
       }
 
       if (numberOfThreads < 1)
       {
          return 1;
       }
 
       final int minBytes = 128000;
       final int minEntries = 60;
 
       if (numberOfBytes < minBytes && numberOfEntries < minEntries)
       {
          return 1;
       }
 
       final int maxPartitions = numberOfThreads * 2;
 
       final int recommendedByBytes = Math.min(
          numberOfBytes < 0 ? -1 : (int) Math.ceil(numberOfBytes / (float) minBytes), numberOfEntries);
 
       final int recommendedByEntries = (int) Math.ceil(numberOfEntries / (float) minEntries);
 
       int recommendedByContent = Math.min(Math.max(recommendedByBytes, recommendedByEntries), maxPartitions);
 
       final int recommendedByThreads = Math.min(numberOfThreads + (numberOfThreads % 2), numberOfEntries);
 
       if (recommendedByContent < recommendedByThreads)
       {
          return Math.min(recommendedByContent * 2, recommendedByThreads);
       }
 
       return recommendedByContent;
    }
 
    private int getNumberOfEntries(ZipInputStreamFactory streamFactory) throws IOException
    {
       final ZipInputStream zipStream = streamFactory.newZipInputStream();
       try
       {
          int nr = 0;
          ZipEntry zipEntry = zipStream.getNextEntry();
          while (zipEntry != null)
          {
             zipEntry = zipStream.getNextEntry();
             nr++;
          }
          return nr;
       }
       finally
       {
          IOUtils.closeQuietly(zipStream);
       }
    }
 }
