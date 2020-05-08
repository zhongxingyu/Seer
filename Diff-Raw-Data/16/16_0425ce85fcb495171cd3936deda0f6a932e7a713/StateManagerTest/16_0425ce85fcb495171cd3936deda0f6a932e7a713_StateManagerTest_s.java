 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tests.tcf.filesystem.utils;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.tcf.te.runtime.callback.Callback;
 import org.eclipse.tcf.te.tcf.core.concurrent.Rendezvous;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.operations.OpCacheUpdate;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.CacheManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.FileState;
 import org.eclipse.tcf.te.tcf.filesystem.core.internal.utils.PersistenceManager;
 import org.eclipse.tcf.te.tcf.filesystem.core.model.CacheState;
 
 public class StateManagerTest extends UtilsTestBase {
 
 	public void testCacheStateConsistent() throws Exception {
 		writeFileContent("hello,world!"); //$NON-NLS-1$
 	    OpCacheUpdate update = new OpCacheUpdate(testFile);
 	    update.run(new NullProgressMonitor());
 		CacheState cacheState = testFile.getCacheState();
 		assertEquals(CacheState.consistent, cacheState);
 	}
 	
 	public void testCacheStateModified() throws Exception {
 		writeFileContent("hello,world!"); //$NON-NLS-1$
 	    OpCacheUpdate update = new OpCacheUpdate(testFile);
 	    update.run(new NullProgressMonitor());
 	    File file = CacheManager.getCacheFile(testFile);
 	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 	    writer.write("hello, test!"); //$NON-NLS-1$
 	    writer.close();
 		updateCacheState();
 		CacheState cacheState = testFile.getCacheState();
 	    assertEquals(CacheState.modified, cacheState);
 	}
 
 	private void updateCacheState() throws TimeoutException {
 	    FileState digest = PersistenceManager.getInstance().getFileDigest(testFile);
 		final Rendezvous rendezvous = new Rendezvous();
 		digest.updateState(new Callback(){
 			@Override
             protected void internalDone(Object caller, IStatus status) {
 				rendezvous.arrive();
             }
 		});
 		rendezvous.waiting(10000);
     }
 	
 	private void refreshCacheState() throws TimeoutException {
 		final Rendezvous rendezvous = new Rendezvous();
 		testFile.refresh(new Callback(){
 			@Override
             protected void internalDone(Object caller, IStatus status) {
 				rendezvous.arrive();
             }
 		});
 		rendezvous.waiting(10000);
 	}
 	 
 	
 	public void testCacheStateOutdated() throws Exception {
 		writeFileContent("hello,world!"); //$NON-NLS-1$
 	    OpCacheUpdate update = new OpCacheUpdate(testFile);
 	    update.run(new NullProgressMonitor());
 		writeFileContent("hello,test!"); //$NON-NLS-1$
 		refreshCacheState();
 		CacheState cacheState = testFile.getCacheState();
 		assertEquals(CacheState.outdated, cacheState);
 	}
 	
 	public void testCacheStateConflict() throws Exception {
 		writeFileContent("hello,world!"); //$NON-NLS-1$
 	    OpCacheUpdate update = new OpCacheUpdate(testFile);
 	    update.run(new NullProgressMonitor());
 		writeFileContent("hello,test!"); //$NON-NLS-1$
 		refreshCacheState();
 	    File file = CacheManager.getCacheFile(testFile);
 	    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 	    writer.write("hello, earth!"); //$NON-NLS-1$
 	    writer.close();
 		updateCacheState();
 		CacheState cacheState = testFile.getCacheState();
 		assertEquals(CacheState.conflict, cacheState);
 	}
 }
