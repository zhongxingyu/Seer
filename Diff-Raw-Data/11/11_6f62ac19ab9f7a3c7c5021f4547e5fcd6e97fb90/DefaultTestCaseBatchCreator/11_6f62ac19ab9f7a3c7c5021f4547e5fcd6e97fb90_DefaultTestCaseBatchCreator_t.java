 /**
  *    This module represents an engine IMPL for the load testing framework
  *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.smartitengineering.loadtest.engine.impl.management;
 
 import com.smartitengineering.loadtest.engine.TestCase;
 import com.smartitengineering.loadtest.engine.events.BatchEvent;
 import com.smartitengineering.loadtest.engine.management.TestCaseBatchCreator.Batch;
 import java.lang.ref.WeakReference;
 import java.util.AbstractMap;
import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  *
  * @author imyousuf
  */
 public class DefaultTestCaseBatchCreator
     extends AbstracltTestCaseBatchCreator {
 
     private Set<Batch> batches;
     private Batch currentBatch;
 
     public DefaultTestCaseBatchCreator() {
         batches = new HashSet<Batch>();
     }
 
     public void start()
         throws IllegalStateException {
         if (!isInitialized()) {
             throw new IllegalStateException();
         }
         Thread creatorThread = new Thread(new BatchCreatorThread());
         creatorThread.start();
     }
 
     public Batch getNextBatch()
         throws IllegalStateException {
        if (!isInitialized() || !isNextBatchAvailable()) {
             throw new IllegalStateException();
         }
         return currentBatch;
     }
 
     protected class BatchCreatorThread
         implements Runnable {
 
         public void run() {
             int batchCount = 0;
             while (getDelayProvider().hasNext()) {
                 try {
                     Thread.sleep(getDelayProvider().next());
                 }
                 catch (InterruptedException ex) {
                     ex.printStackTrace();
                 }
                 batchCount++;
                 int nextStepSize = getNextStepSizeProvider().next();
                 final StringBuilder nameBuilder =
                     new StringBuilder(name).append('-').append(batchCount);
                 ThreadGroup group = new ThreadGroup(nameBuilder.toString());
                 Map<Thread, TestCase> threadMap =
                     new HashMap<Thread, TestCase>();
                 for (int stepCount = 0; stepCount < nextStepSize; ++stepCount) {
                     TestCase testCase = getTestCaseCreationFactory().getTestCase(
                         initalProperties);
                    testCase.initTestCase();
                     if (testCase == null) {
                         continue;
                     }
                     nameBuilder.append("-Thread-").append(stepCount);
                     Thread thread = new Thread(group, testCase, nameBuilder.
                         toString());
                     threadMap.put(thread, testCase);
                 }
                 currentBatch = getBatch(
                     new AbstractMap.SimpleEntry<ThreadGroup, Map<Thread, TestCase>>(
                     group, threadMap));
                 fireBatchEvent();
                 batches.add(new WeakReference<Batch>(getNextBatch()).get());
                 setNextBatchAvailable(false);
                 currentBatch = null;
             }
             Batch batch =
                 getBatch(new AbstractMap.SimpleEntry<ThreadGroup, Map<Thread, TestCase>>(
                new ThreadGroup(""), Collections.<Thread, TestCase>emptyMap()));
            fireBatchEndedEvent(new BatchEvent(batch, name));
         }
     }
 }
