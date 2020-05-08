 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.deployer.hot;
 
 import java.io.File;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.eclipse.virgo.util.io.FileSystemChecker;
 import org.eclipse.virgo.util.io.FileSystemListener;
 
 /**
  * Task that monitors a given directory and notifies configured {@link FileSystemListener FileSystemListeners}.
  * <p/>
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Threadsafe.
  * 
  */
 final class WatchTask implements Runnable {
 
     private final Logger logger = LoggerFactory.getLogger(this.getClass());
 
     private final int scanIntervalMillis = 1000;
 
     private final FileSystemChecker checker;
 
     private final File watchDir;
 
     WatchTask(FileSystemChecker checker, File watchDir) {
         this.checker = checker;
         this.watchDir = watchDir;
     }
 
     /**
      * Watches the configured directory for modifications.
      */
     public void run() {
         while (!Thread.currentThread().isInterrupted()) {
             try {
                 Thread.sleep(this.scanIntervalMillis);
             } catch (InterruptedException e) {
                 break;
             }
 
             try {
                 this.checker.check();
            } catch (Throwable e) {
                 logger.error("Error watching directory '{}'", e, this.watchDir.getAbsolutePath());
             }
         }
     }
 }
