 /*
  * $Id$
  * $Revision$
  * $Date$
  * $Author$
  *
  * The DOMS project.
  * Copyright (C) 2007-2010  The State and University Library
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package dk.statsbiblioteket.doms.ingesters.radiotv;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.UUID;
 
 /**
  * @author tsh
  */
 public class TestHotFolderScanner {
 
     private HotFolderScanner hotFolderScanner;
     private File clientFeedbackAddedFile;
 
     private File tempTestDir;
     private File tempTestFile;
     private File stopFolder;
 
     //TODO: Add tests for the detection of modified and deleted files. 
     @SuppressWarnings("unused")
     private File clientFeedbackModifiedFile;
     @SuppressWarnings("unused")
     private File clientFeedbackDeletedFile;
 
     //TODO: Also test file modification and deletion.
 
     private final HotFolderScannerClient hotFolderScannerClient = new HotFolderScannerClient() {
         @Override
         public void fileAdded(File addedFile) {
             clientFeedbackAddedFile = addedFile;
         }
 
         @Override
         public void fileDeleted(File deletedFile) {
             clientFeedbackDeletedFile = deletedFile;
         }
 
         @Override
         public void fileModified(File modifiedFile) {
             clientFeedbackModifiedFile = modifiedFile;
         }
     };
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
         hotFolderScanner = new HotFolderScanner();
         clientFeedbackAddedFile = null;
     }
 
     @After
     public void tearDown() throws Exception {
         if (tempTestFile != null) {
             tempTestFile.delete();
         }
 
         if (tempTestDir != null) {
             tempTestDir.delete();
         }
         if (stopFolder != null) {
             stopFolder.delete();
         }
     }
 
     /**
      * Test method for
     * {@link dk.statsbiblioteket.doms.ingesters.radiotv.HotFolderScanner#startScanning(java.io.File, dk.statsbiblioteket.doms.ingesters.radiotv.HotFolderScannerClient)}
      * .
      */
     @Test
     public void testStartScanning() throws IOException {
         // Create a private temp. dir in the system temp. dir.
         tempTestDir = new File(System.getProperty("java.io.tmpdir") +
                 File.pathSeparator + UUID.randomUUID());
         stopFolder = new File(System.getProperty("java.io.tmpdir") +
                 File.pathSeparator + UUID.randomUUID());
 
         assertTrue("Failed creating temp. test dir: " + tempTestDir.toString(),
                 tempTestDir.mkdirs());
 
         // Start scanning.
         final long scanDelay = 5000;
         hotFolderScanner.setInitialScannerDelay(scanDelay);
         hotFolderScanner.setScannerPeriod(scanDelay);
         hotFolderScanner.startScanning(tempTestDir, stopFolder, hotFolderScannerClient);
 
         // Create a test file.
         tempTestFile = new File(tempTestDir, UUID.randomUUID().toString());
         assertTrue("Failed creating test file: " + tempTestFile.toString(),
                 tempTestFile.createNewFile());
 
         // Wait for the scanner to detect the change.
         try {
             Thread.sleep(scanDelay * 2);
         } catch (InterruptedException ir) {
             // Never mind that....
         }
 
         assertEquals(
                 "The created test file was not detected by the hot folder scanner.",
                 tempTestFile, clientFeedbackAddedFile);
 
         // TODO: It wouldn't hurt testing the scanner with more than one file...
     }
 }
