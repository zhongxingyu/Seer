 /*******************************************************************************
  * Copyright (c) 2000, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.tests.internal.localstore;
 
 import java.io.*;
 import java.util.*;
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.eclipse.core.internal.indexing.*;
 import org.eclipse.core.internal.localstore.*;
 import org.eclipse.core.internal.properties.IndexedStoreWrapper;
 import org.eclipse.core.internal.resources.*;
 import org.eclipse.core.internal.utils.Policy;
 import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
 
 /**
  * This class defines all tests for the HistoryStore Class.
  */
 
 public class HistoryStoreTest extends EclipseWorkspaceTest {
 
 	class VerificationFailedException extends Exception {
 		VerificationFailedException(String message) {
 			super(message);
 		}
 	}
 
 	class HistoryStoreVisitorVerifier implements IHistoryStoreVisitor {
 		Set expected = new HashSet();
 		Set actual = new HashSet();
 
 		public boolean visit(HistoryStoreEntry state) {
 			actual.add(state.getPath());
 			return true;
 		}
 
 		public void reset() {
 			expected = new HashSet();
 			actual = new HashSet();
 		}
 
 		public void addExpected(IPath path) {
 			expected.add(path);
 		}
 
 		public void verify() throws VerificationFailedException {
 			if (expected.size() != actual.size())
 				throw new VerificationFailedException("Expected size (" + expected.size() + ") and actual size (" + actual.size() + ") differ.");
 			for (Iterator i = expected.iterator(); i.hasNext();) {
 				IPath path = (IPath) i.next();
 				if (!actual.contains(path))
 					throw new VerificationFailedException("Did not visit expected path: " + path);
 			}
 		}
 	}
 
 	class LogListenerVerifier implements ILogListener {
 		List expected = new ArrayList();
 		List actual = new ArrayList();
 
 		void addExpected(int statusCode) {
 			expected.add(new Integer(statusCode));
 		}
 
 		void verify() throws VerificationFailedException {
 			String message;
 			if (expected.size() != actual.size()) {
 				message = "Expected size: " + expected.size() + " does not equal actual size: " + actual.size() + "\n";
 				message += dump();
 				throw new VerificationFailedException(message);
 			}
 			for (Iterator i = expected.iterator(); i.hasNext();) {
 				Integer status = (Integer) i.next();
 				if (!actual.contains(status)) {
 					message = "Expected and actual results differ.\n";
 					message += dump();
 					throw new VerificationFailedException(message);
 				}
 			}
 		}
 
 		void reset() {
 			expected = new ArrayList();
 			actual = new ArrayList();
 		}
 
 		String dump() {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("Expected:\n");
 			for (Iterator i = expected.iterator(); i.hasNext();)
 				buffer.append("\t" + i.next() + "\n");
 			buffer.append("Actual:\n");
 			for (Iterator i = actual.iterator(); i.hasNext();)
 				buffer.append("\t" + i.next() + "\n");
 			return buffer.toString();
 		}
 
 		/**
 		 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
 		 */
 		public void logging(IStatus status, String plugin) {
 			actual.add(new Integer(status.getCode()));
 		}
 
 	}
 
 	public HistoryStoreTest() {
 		super();
 	}
 
 	public HistoryStoreTest(String name) {
 		super(name);
 	}
 
 	public static Test suite() {
 		//	TestSuite suite = new TestSuite();
 		//	suite.addTest(new HistoryStoreTest("testBug28238"));
 		//	suite.addTest(new HistoryStoreTest("testIndexOrdering2"));
 		//	return suite;
 		return new TestSuite(HistoryStoreTest.class);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		wipeHistoryStore();
 	}
 
 	private int numBytes(InputStream input) {
 		int i = 0;
 		int c = -1;
 		try {
 			c = input.read();
 			while (c != -1) {
 				i++;
 				c = input.read();
 			}
 		} catch (IOException e) {
 			i = 0;
 		}
 		if (c != -1)
 			i = 0;
 		return i;
 	}
 
 	/**
 	 * Test the various policies in place to ensure that the history store 
 	 * does not grow to unmanageable size.  The policies currently in place
 	 * include:
 	 * - store only a maximum number of states for each file
 	 * - do not store files greater than some stated size
 	 * - consider history store information stale after some specified period
 	 *   of time and discard stale data
 	 * 
 	 * History store states are always stored in order from the newest state to
 	 * the oldest state.  This will be tested as well
 	 *
 	 * Scenario:
 	 *   1. Create project					AddStateAndPoliciesProject
 	 *   2. Create file	(file.txt)			random contents
 	 *   3. Set policy information in the workspace description as follows:
 	 * 			- don't store states older than 1 day
 	 * 			- keep a maximum of 5 states per file
 	 * 			- file states must be less than 1 Mb
 	 *   4. Make 8 modifications to file.txt (causing 8 states to be created)
 	 *   5. Ensure only 5 states were kept.
 	 *   6. Ensure states are in order from newest to oldest.
 	 *   7. Set policy such that file states must be no greater than 7 bytes.
 	 *   8. Create a new file file1.txt
 	 *   9. Add 10 bytes of data to this file.
 	 *  10. Check each of the states for this file and ensure they are not
 	 *      greater than 7 bytes.
 	 *  11. Revert to policy in #3
 	 *  12. Make sure we still have 5 states for file.txt (the first file we 
 	 *      worked with)
 	 *  13. Change the policy so that data older than 10 seconds is stale.
 	 *  14. Wait 12 seconds (make it longer than 10 seconds to ensure we don't
 	 *      encounter granularity issues).
 	 *  15. Check file states.  There should be none left.
 	 */
 	public void testAddStateAndPolicies() {
 
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		IFile file = project.getFile("file.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		/* set local history policies */
 		// keep orignal
 		IWorkspaceDescription originalDescription = getWorkspace().getDescription();
 		// get another copy for changes
 		IWorkspaceDescription description = getWorkspace().getDescription();
 		// longevity set to 1 day
 		description.setFileStateLongevity(1000 * 3600 * 24);
 		// keep a max of 5 file states
 		description.setMaxFileStates(5);
 		// max size of file = 1 Mb
 		description.setMaxFileStateSize(1024 * 1024);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("0.1", e);
 		}
 
 		/* test max file states */
 		for (int i = 0; i < 8; i++) {
 			try {
 				ensureOutOfSync(file);
 				file.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
 				file.setContents(getRandomContents(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("1.0", e);
 			}
 		}
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.01", e);
 		}
 		// Make sure we have 8 states as we haven't trimmed yet.
 		assertEquals("1.02", 8, states.length);
 
 		try {
 			getWorkspace().save(true, null);
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 		// We added 8 states.  Make sure we only have 5 (the max).
 		assertEquals("1.2", description.getMaxFileStates(), states.length);
 
 		// assert that states are in the correct order (newer ones first)
 		long lastModified = states[0].getModificationTime();
 		for (int i = 1; i < states.length; i++) {
 			assertTrue("1.3", lastModified > states[i].getModificationTime());
 			lastModified = states[i].getModificationTime();
 		}
 
 		/* test max file state size */
 		description.setMaxFileStates(15);
 		// max size of file = 7 bytes
 		description.setMaxFileStateSize(7);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("2.0.0", e);
 		}
 		file = project.getFile("file1.txt");
 		try {
 			file.create(new ByteArrayInputStream(new byte[0]), true, getMonitor());
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 		// Add 10 bytes to exceed the max file state size.
 		for (int i = 0; i < 10; i++) {
 			try {
 				file.appendContents(getContents("a"), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("2.1", e);
 			}
 		}
 		try {
 			getWorkspace().save(true, null);
 			states = file.getHistory(getMonitor());
 			// #states = size + 1 for the 0 byte length file to begin with.
 			for (int i = 0; i < states.length; i++) {
 				int bytesRead = numBytes(states[i].getContents());
 				assertTrue("2.2." + i, bytesRead <= description.getMaxFileStateSize());
 			}
 		} catch (CoreException e) {
 			fail("2.3", e);
 		}
 
 		/* test max file longevity */
 		// use the file of the first test
 		file = project.getFile("file.txt");
 		// 1 day
 		description.setFileStateLongevity(1000 * 3600 * 24);
 		description.setMaxFileStates(5);
 		// 1 Mb
 		description.setMaxFileStateSize(1024 * 1024);
 		// the description should be the same as the first test
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 		try {
 			states = file.getHistory(getMonitor());
 			// Make sure we have 5 states for file file.txt
 			assertEquals("3.1", description.getMaxFileStates(), states.length);
 		} catch (CoreException e) {
 			fail("3.2", e);
 		}
 		// change policies
 		// 10 seconds
 		description.setFileStateLongevity(1000 * 10);
 		// 1 Mb
 		description.setMaxFileStateSize(1024 * 1024);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("3.3", e);
 		}
 		try {
 			// sleep for more than 10 seconds (the granularity varies on
 			// some machines so we will sleep for 12 seconds)
 			Thread.sleep(1000 * 12);
 		} catch (InterruptedException e) {
 			fail("3.4", e);
 		}
 		try {
 			getWorkspace().save(true, null);
 			states = file.getHistory(getMonitor());
 			// The 5 states for file.txt should have exceeded their longevity
 			// and been removed.  Make sure we have 0 states left.
 			assertEquals("3.5", 0, states.length);
 		} catch (CoreException e) {
 			fail("3.6", e);
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 		try {
 			getWorkspace().setDescription(originalDescription);
 		} catch (CoreException e) {
 			fail("20.1", e);
 		}
 	}
 
 	/*
 	 * Test the functionality in store.clean() which is called to ensure
 	 * the history store to ensure that the history store does not grow to
 	 * unmanageable size.  The policies currently in place include:
 	 * - store only a maximum number of states for each file
 	 * - do not store files greater than some stated size
 	 * - consider history store information stale after some specified period
 	 *   of time and discard stale data
 	 * 
 	 */
 	public void testClean() {
 
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		IFile file = project.getFile("file.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		IHistoryStore store = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		// keep orignal
 		IWorkspaceDescription originalDescription = getWorkspace().getDescription();
 		// get another copy for changes
 		IWorkspaceDescription description = getWorkspace().getDescription();
 
 		/* test max file states */
 		// 1 day
 		description.setFileStateLongevity(1000 * 3600 * 24);
 		// 500 states per file max.
 		description.setMaxFileStates(500);
 		// 1Mb max size
 		description.setMaxFileStateSize(1024 * 1024);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("0.1", e);
 		}
 
 		// Set up 8 file states for this file when 500 are allowed
 		for (int i = 0; i < 8; i++) {
 			try {
 				ensureOutOfSync(file);
 				file.refreshLocal(IResource.DEPTH_ZERO, getMonitor());
 				//			try {
 				//				Thread.sleep(5000); // necessary because of lastmodified granularity in some file systems
 				//			} catch (InterruptedException e) {
 				//			}
 				file.setContents(getRandomContents(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("1.0", e);
 			}
 		}
 		// All 8 states should exist.
 		long oldLastModTimes[] = new long[8];
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.1", 8, states.length);
 			for (int i = 0; i < 8; i++) {
 				oldLastModTimes[i] = states[i].getModificationTime();
 			}
 		} catch (CoreException e) {
 			fail("1.2", e);
 		}
 
 		// Set max. number of file states to be 3
 		description.setMaxFileStates(3);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 		// Run 'clean' - should cause 5 of 8 states to be removed
 		store.clean(getMonitor());
 		// Restore max. number of states/file to 500
 		description.setMaxFileStates(500);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("2.2", e);
 		}
 
 		// Check to ensure only 3 states remain.  Make sure these are the 3
 		// newer states.
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("2.3", 3, states.length);
 			// assert that states are in the correct order (newer ones first)
 			long lastModified = states[0].getModificationTime();
 			for (int i = 1; i < states.length; i++) {
 				assertTrue("2.4." + i, lastModified > states[i].getModificationTime());
 				lastModified = states[i].getModificationTime();
 			}
 			// Make sure we kept the 3 newer states.
 			for (int i = 0; i < states.length; i++) {
 				assertTrue("2.5." + i, oldLastModTimes[i] == states[i].getModificationTime());
 			}
 		} catch (CoreException e) {
 			fail("2.6", e);
 		}
 
 		/* test max file longevity */
 		file = project.getFile("file.txt"); // use the file of the first test
 		description.setFileStateLongevity(1000 * 3600 * 24); // 1 day
 		description.setMaxFileStates(500);
 		description.setMaxFileStateSize(1024 * 1024); // 1 Mb
 		// the description should be the same as the first test
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("3.1", 3, states.length);
 		} catch (CoreException e) {
 			fail("3.2", e);
 		}
 		// change policies
 		// 10 seconds
 		description.setFileStateLongevity(1000 * 10);
 		// 1 Mb
 		description.setMaxFileStateSize(1024 * 1024);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("4.0", e);
 		}
 		try {
 			// sleep for 12 seconds (must exceed 10 seconds).  This should
 			// cause all 3 states for file.txt to be considered stale.
 			Thread.sleep(1000 * 12);
 		} catch (InterruptedException e) {
 			fail("4.1", e);
 		}
 		store.clean(getMonitor());
 		// change policies - restore to original values
 		// 1 day
 		description.setFileStateLongevity(1000 * 3600 * 24);
 		// 1 Mb
 		description.setMaxFileStateSize(1024 * 1024);
 		try {
 			getWorkspace().setDescription(description);
 		} catch (CoreException e) {
 			fail("5.0", e);
 		}
 		// Ensure we have no state information left.  It should have been 
 		// considered stale.
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("5.1", 0, states.length);
 		} catch (CoreException e) {
 			fail("5.2", e);
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 		try {
 			getWorkspace().setDescription(originalDescription);
 		} catch (CoreException e) {
 			fail("20.1", e);
 		}
 	}
 
 	/**
 	 * Test for existence of file states in the HistoryStore.
 	 */
 	public void testExists() throws Throwable {
 
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		IFile file = project.getFile("removeAllStatesFile.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		// Constant for the number of states we will create
 		final int ITERATIONS = 20;
 
 		/* Add multiple states for one file location. */
 		for (int i = 0; i < ITERATIONS; i++) {
 			try {
 				file.setContents(getRandomContents(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("3.0." + i, e);
 			}
 		}
 
 		/* Valid Case: Test retrieved values. */
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("5.0", e);
 		}
 		// Make sure we have ITERATIONS number of states
 		assertEquals("5.1", ITERATIONS, states.length);
 		// Make sure that each of these states really exists in the filesystem.
 		for (int i = 0; i < states.length; i++)
 			assertTrue("5.2." + i, states[i].exists());
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 	}
 
 	/**
 	 * Test for retrieving contents of files with states logged in the HistoryStore.
 	 */
 	public void testGetContents() throws Throwable {
 
 		final int ITERATIONS = 20;
 
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		/* Create files. */
 		IFile file = project.getFile("getContentsFile.txt");
 		String contents = "This file has some contents in testGetContents.";
 		ensureExistsInWorkspace(file, contents);
 
 		IFile secondValidFile = project.getFile("secondGetContentsFile.txt");
 		contents = "A file with some other contents in testGetContents.";
 		ensureExistsInWorkspace(secondValidFile, contents);
 
 		IHistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 
 		/* Simulated date -- Increment once for each edition added. */
 		long myLong = 0L;
 
 		/* Add multiple editions for one file location. */
 		for (int i = 0; i < ITERATIONS; i++, myLong++) {
 			historyStore.addState(file.getFullPath(), file.getLocation().toFile(), myLong, true);
 			try {
 				contents = "This file has some contents in testGetContents.";
 				InputStream is = new ByteArrayInputStream(contents.getBytes());
 				createFileInFileSystem(file.getLocation(), is);
 				file.refreshLocal(IResource.DEPTH_INFINITE, null);
 			} catch (CoreException e) {
 				fail("1.1." + i, e);
 			} catch (IOException e) {
 				fail("1.2." + i, e);
 			}
 		}
 
 		/* Add multiple editions for second file location. */
 		for (int i = 0; i < ITERATIONS; i++, myLong++) {
 			historyStore.addState(secondValidFile.getFullPath(), secondValidFile.getLocation().toFile(), myLong, true);
 			try {
 				contents = "A file with some other contents in testGetContents.";
 				InputStream is = new ByteArrayInputStream(contents.getBytes());
 				createFileInFileSystem(secondValidFile.getLocation(), is);
 				secondValidFile.refreshLocal(IResource.DEPTH_INFINITE, null);
 			} catch (CoreException e) {
 				fail("2.1." + i, e);
 			} catch (IOException e) {
 				fail("2.2." + i, e);
 			}
 		}
 
 		/* Ensure contents of file and retrieved resource are identical.
 		 Does not check timestamps. Timestamp checks are performed in a separate test. */
 		DataInputStream inFile = null;
 		DataInputStream inContents = null;
 		IFileState[] stateArray = null;
 		stateArray = historyStore.getStates(file.getFullPath(), getMonitor());
 		for (int i = 0; i < stateArray.length; i++, myLong++) {
 			inFile = new DataInputStream(file.getContents(false));
 			try {
 				inContents = new DataInputStream(historyStore.getContents(stateArray[i]));
 			} catch (CoreException e) {
 				fail("3.1." + i, e);
 			}
 			if (!compareContent(inFile, inContents))
 				fail("3.2." + i + " No match, files are not identical.");
 		}
 
 		stateArray = historyStore.getStates(secondValidFile.getFullPath(), getMonitor());
 		for (int i = 0; i < stateArray.length; i++, myLong++) {
 			inFile = new DataInputStream(secondValidFile.getContents(false));
 			try {
 				inContents = new DataInputStream(historyStore.getContents(stateArray[i]));
 			} catch (CoreException e) {
 				fail("4.1." + i, e);
 			}
 			if (!compareContent(inFile, inContents))
 				fail("4.2." + i + " No match, files are not identical.");
 		}
 
 		/* Test getting an invalid file state. */
 		for (int i = 0; i < ITERATIONS; i++) {
 			// Create bogus FileState using invalid uuid.
 			try {
 				InputStream in = historyStore.getContents(new FileState(historyStore, Path.ROOT, myLong, new UniversalUniqueIdentifier()));
 				in.close();
 				fail("6." + i + " Edition should be invalid.");
 			} catch (CoreException e) {
 				// expected
 			}
 		}
 
 		/* Test verification using null file state. */
 		for (int i = 0; i < ITERATIONS; i++) {
 			try {
 				historyStore.getContents(null);
 				fail("7." + i + " Null edition should be invalid.");
 			} catch (RuntimeException e) {
 				// expected
 			}
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 	}
 
 	public void testRemoveAll() {
 
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		IFile file = project.getFile("removeAllStatesFile.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		final int ITERATIONS = 20;
 
 		/* test remove in a file */
 		for (int i = 0; i < ITERATIONS; i++) {
 			try {
 				file.setContents(getRandomContents(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("3.0." + i, e);
 			}
 		}
 
 		/* Valid Case: Ensure correct number of states available. */
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("4.0", e);
 		}
 		assertTrue("4.1", states.length == ITERATIONS);
 
 		/* Remove all states, and verify that no states remain. */
 		try {
 			file.clearHistory(getMonitor());
 			states = file.getHistory(getMonitor());
 			assertEquals("5.0", 0, states.length);
 		} catch (CoreException e) {
 			fail("5.1", e);
 		}
 
 		/* test remove in a folder -- make sure it does not affect other resources' states*/
 		IFolder folder = project.getFolder("folder");
 		IFile anotherOne = folder.getFile("anotherOne");
 		try {
 			folder.create(true, true, getMonitor());
 			anotherOne.create(getRandomContents(), true, getMonitor());
 		} catch (CoreException e) {
 			fail("6.0", e);
 		}
 		for (int i = 0; i < ITERATIONS; i++) {
 			try {
 				file.setContents(getRandomContents(), true, true, getMonitor());
 				anotherOne.setContents(getRandomContents(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("6.1." + i, e);
 			}
 		}
 
 		try {
 			states = file.getHistory(getMonitor());
 			assertEquals("6.2", ITERATIONS, states.length);
 			states = anotherOne.getHistory(getMonitor());
 			assertEquals("6.3", ITERATIONS, states.length);
 		} catch (CoreException e) {
 			fail("6.4", e);
 		}
 
 		/* Remove all states, and verify that no states remain. */
 		try {
 			project.clearHistory(getMonitor());
 			states = file.getHistory(getMonitor());
 			assertEquals("7.0", 0, states.length);
 			states = anotherOne.getHistory(getMonitor());
 			assertEquals("7.1", 0, states.length);
 		} catch (CoreException e) {
 			fail("7.2", e);
 		}
 
 		/* test remove in a folder -- make sure it does not affect other resources' states*/
 		IFile aaa = project.getFile("aaa");
 		IFolder bbb = project.getFolder("bbb");
 		anotherOne = bbb.getFile("anotherOne");
 		IFile ccc = project.getFile("ccc");
 		try {
 			bbb.create(true, true, getMonitor());
 			anotherOne.create(getRandomContents(), true, getMonitor());
 			aaa.create(getRandomContents(), true, getMonitor());
 			ccc.create(getRandomContents(), true, getMonitor());
 		} catch (CoreException e) {
 			fail("8.0", e);
 		}
 		for (int i = 0; i < ITERATIONS; i++) {
 			try {
 				anotherOne.setContents(getRandomContents(), true, true, getMonitor());
 				aaa.setContents(getRandomContents(), true, true, getMonitor());
 				ccc.setContents(getRandomContents(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("8.1." + i, e);
 			}
 		}
 
 		try {
 			states = anotherOne.getHistory(getMonitor());
 			assertEquals("8.3", ITERATIONS, states.length);
 			states = aaa.getHistory(getMonitor());
 			assertEquals("8.4", ITERATIONS, states.length);
 			states = ccc.getHistory(getMonitor());
 			assertEquals("8.5", ITERATIONS, states.length);
 		} catch (CoreException e) {
 			fail("8.6", e);
 		}
 
 		/* Remove all states, and verify that no states remain. aaa and ccc should not be affected. */
 		try {
 			bbb.clearHistory(getMonitor());
 			states = anotherOne.getHistory(getMonitor());
 			assertEquals("9.1", 0, states.length);
 			states = aaa.getHistory(getMonitor());
 			assertEquals("9.2", ITERATIONS, states.length);
 			states = ccc.getHistory(getMonitor());
 			assertEquals("9.3", ITERATIONS, states.length);
 		} catch (CoreException e) {
 			fail("9.4", e);
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 	}
 
 	/**
 	 * Simple use case for History Store.
 	 *
 	 * Scenario:									   # Editions
 	 *   1. Create file					"content 1"			0		
 	 *   2. Set new content				"content 2"			1	
 	 *   3. Set new content				"content 3"			2
 	 *   4. Delete file										3
 	 *   5. Roll back to first version  "content 1"			3
 	 *   6. Set new content				"content 2"			4
 	 *   7. Roll back to third version  "content 3"			5
 	 */
 	public void testSimpleUse() {
 
 		/* Initialize common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		String[] contents = {"content1", "content2", "content3"};
 		IFile file = project.getFile("file");
 
 		/* Create the file. */
 		try {
 			file.create(getContents(contents[0]), true, getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 
 		/* Set new contents on the file. Should add two entries to the store. */
 		try {
 			for (int i = 0; i < 2; i++)
 				file.setContents(getContents(contents[i + 1]), true, true, getMonitor());
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 
 		/* Ensure two entries are available for the file, and that content matches. */
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("3.0", 2, states.length);
 			assertTrue("3.1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("3.1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 		} catch (CoreException e) {
 			fail("3.2", e);
 		}
 
 		/* Delete the file. Should add an entry to the store. */
 		try {
 			file.delete(true, true, getMonitor());
 		} catch (CoreException e) {
 			fail("4.0", e);
 		}
 
 		/* Ensure three entries are available for the file, and that content matches. */
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("5.0", 3, states.length);
 			assertTrue("5.1.1", compareContent(getContents(contents[2]), states[0].getContents()));
 			assertTrue("5.1.2", compareContent(getContents(contents[1]), states[1].getContents()));
 			assertTrue("5.1.3", compareContent(getContents(contents[0]), states[2].getContents()));
 		} catch (CoreException e) {
 			fail("5.2", e);
 		}
 
 		/* Roll file back to first version, and ensure that content matches. */
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			// Create the file with the contents from one of the states. 
 			// Won't add another entry to the store.
 			file.create(states[0].getContents(), false, getMonitor());
 
 			// Check history store.
 			states = file.getHistory(getMonitor());
 			assertEquals("6.0", 3, states.length);
 			assertTrue("6.1.1", compareContent(getContents(contents[2]), states[0].getContents()));
 			assertTrue("6.1.2", compareContent(getContents(contents[1]), states[1].getContents()));
 			assertTrue("6.1.3", compareContent(getContents(contents[0]), states[2].getContents()));
 
 			// Check file contents.
 			assertTrue("6.2", compareContent(getContents(contents[2]), file.getContents(false)));
 
 		} catch (CoreException e) {
 			fail("6.3", e);
 		}
 
 		/* Set new contents on the file. Should add an entry to the history store. */
 		try {
 			file.setContents(getContents(contents[1]), true, true, null);
 		} catch (CoreException e) {
 			fail("7.0", e);
 		}
 
 		/* Ensure four entries are available for the file, and that entries match. */
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("8.0", 4, states.length);
 			assertTrue("8.1.1", compareContent(getContents(contents[2]), states[0].getContents()));
 			assertTrue("8.1.2", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("8.1.3", compareContent(getContents(contents[1]), states[2].getContents()));
 			assertTrue("8.1.4", compareContent(getContents(contents[0]), states[3].getContents()));
 		} catch (CoreException e) {
 			fail("8.2", e);
 		}
 
 		/* Roll file back to third version, and ensure that content matches. */
 		try {
 			IFileState[] states = file.getHistory(getMonitor());
 			// Will add another entry to log.
 			file.setContents(states[2], true, true, getMonitor());
 
 			// Check history log.
 			states = file.getHistory(getMonitor());
 			assertEquals("9.0", 5, states.length);
 			assertTrue("9.1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("9.1.2", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("9.1.3", compareContent(getContents(contents[2]), states[2].getContents()));
 			assertTrue("9.1.4", compareContent(getContents(contents[1]), states[3].getContents()));
 			assertTrue("9.1.5", compareContent(getContents(contents[0]), states[4].getContents()));
 
 			// Check file contents.
 			assertTrue("9.2", compareContent(getContents(contents[1]), file.getContents(false)));
 
 		} catch (CoreException e) {
 			fail("9.3", e);
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 	}
 
 	public void testDelete() {
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("MyProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		// test file
 		IFile file = project.getFile("file.txt");
 		try {
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), true, true, getMonitor());
 			file.setContents(getRandomContents(), true, true, getMonitor());
 
 			// Check to see that there are only 2 states before the deletion
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 
 			// Delete the file.  This should add a state to the history store.
 			file.delete(true, true, getMonitor());
 			states = file.getHistory(getMonitor());
 			assertEquals("1.1", 3, states.length);
 
 			// Re-create the file.  This should not affect the history store.
 			file.create(getRandomContents(), true, getMonitor());
 			states = file.getHistory(getMonitor());
 			assertEquals("1.2", 3, states.length);
 		} catch (CoreException e) {
 			fail("1.20", e);
 		}
 
 		// test folder
 		IFolder folder = project.getFolder("folder");
 		// Make sure this has a different name as the history store information
 		// for the first 'file.txt' is likely still around.
 		file = folder.getFile("file2.txt");
 		try {
 			folder.create(true, true, getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), true, true, getMonitor());
 			file.setContents(getRandomContents(), true, true, getMonitor());
 
 			// There should only be 2 history store entries.
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("2.0", 2, states.length);
 
 			// Delete the folder.  This should cause one more history store entry.
 			folder.delete(true, true, getMonitor());
 			states = file.getHistory(getMonitor());
 			assertEquals("2.1", 3, states.length);
 
 			// Re-create the folder.  There should be no new history store entries.
 			folder.create(true, true, getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			states = file.getHistory(getMonitor());
 			assertEquals("2.2", 3, states.length);
 		} catch (CoreException e) {
 			fail("2.99", e);
 		}
 
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("20.0", e);
 		}
 	}
 
 	public void testFindDeleted() {
 		// create common objects
 		IWorkspaceRoot root = getWorkspace().getRoot();
 		IProject project = root.getProject("MyProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 
 			IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("0.1", 0, df.length);
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		// test that a deleted file can be found
 		IFile pfile = project.getFile("findDeletedFile.txt");
 		try {
 			// create and delete a file
 			pfile.create(getRandomContents(), true, getMonitor());
 			pfile.delete(true, true, getMonitor());
 
 			// the deleted file should show up as a deleted member of project
 			IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("0.1", 1, df.length);
 			assertEquals("0.2", pfile, df[0]);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("0.3", 1, df.length);
 			assertEquals("0.4", pfile, df[0]);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("0.5", 0, df.length);
 
 			// the deleted file should show up as a deleted member of workspace root
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("0.5.1", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("0.5.2", 1, df.length);
 			assertEquals("0.5.3", pfile, df[0]);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("0.5.4", 0, df.length);
 
 			// recreate the file
 			pfile.create(getRandomContents(), true, getMonitor());
 
 			// the deleted file should no longer show up as a deleted member of project
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("0.6", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("0.7", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("0.8", 0, df.length);
 
 			// the deleted file should no longer show up as a deleted member of ws root
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("0.8.1", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("0.8.2", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("0.8.3", 0, df.length);
 
 		} catch (CoreException e) {
 			fail("0.00", e);
 		}
 
 		// scrub the project
 		try {
 			project.delete(true, getMonitor());
 			project.create(getMonitor());
 			project.open(getMonitor());
 
 			IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("0.9", 0, df.length);
 		} catch (CoreException e) {
 			fail("0.10", e);
 		}
 
 		// test folder
 		IFolder folder = project.getFolder("folder");
 		IFile file = folder.getFile("filex.txt");
 		IFile folderAsFile = project.getFile(folder.getProjectRelativePath());
 		try {
 			// create and delete a file in a folder
 			folder.create(true, true, getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.delete(true, true, getMonitor());
 
 			// the deleted file should show up as a deleted member
 			IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("1.1", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("1.2", 1, df.length);
 			assertEquals("1.3", file, df[0]);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("1.4", 0, df.length);
 
 			// recreate the file
 			file.create(getRandomContents(), true, getMonitor());
 
 			// the deleted file should no longer show up as a deleted member
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("1.5", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("1.6", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("1.7", 0, df.length);
 
 			// deleting the folder should bring it back
 			folder.delete(true, true, getMonitor());
 
 			// the deleted file should show up as a deleted member of project
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("1.8", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("1.9", 1, df.length);
 			assertEquals("1.10", file, df[0]);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("1.11", 0, df.length);
 
 			// create and delete a file where the folder was
 			folderAsFile.create(getRandomContents(), true, getMonitor());
 			folderAsFile.delete(true, true, getMonitor());
 			folder.create(true, true, getMonitor());
 
 			// the deleted file should show up as a deleted member of folder
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("1.12", 1, df.length);
 			assertEquals("1.13", folderAsFile, df[0]);
 
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("1.14", 2, df.length);
 			List dfList = Arrays.asList(df);
 			assertTrue("1.15", dfList.contains(file));
 			assertTrue("1.16", dfList.contains(folderAsFile));
 
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("1.17", 2, df.length);
 			dfList = Arrays.asList(df);
 			assertTrue("1.18", dfList.contains(file));
 			assertTrue("1.19", dfList.contains(folderAsFile));
 
 		} catch (CoreException e) {
 			fail("1.00", e);
 		}
 
 		// scrub the project
 		try {
 			project.delete(true, getMonitor());
 			project.create(getMonitor());
 			project.open(getMonitor());
 
 			IFile[] df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("1.50", 0, df.length);
 		} catch (CoreException e) {
 			fail("1.51", e);
 		}
 
 		// test a bunch of deletes
 		folder = project.getFolder("folder");
 		IFile file1 = folder.getFile("file1.txt");
 		IFile file2 = folder.getFile("file2.txt");
 		IFolder folder2 = folder.getFolder("folder2");
 		IFile file3 = folder2.getFile("file3.txt");
 		try {
 			// create and delete a file in a folder
 			folder.create(true, true, getMonitor());
 			folder2.create(true, true, getMonitor());
 			file1.create(getRandomContents(), true, getMonitor());
 			file2.create(getRandomContents(), true, getMonitor());
 			file3.create(getRandomContents(), true, getMonitor());
 			folder.delete(true, true, getMonitor());
 
 			// under root
 			IFile[] df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("3.1", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("3.2", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("3.3", 3, df.length);
 			List dfList = Arrays.asList(df);
 			assertTrue("3.3.1", dfList.contains(file1));
 			assertTrue("3.3.2", dfList.contains(file2));
 			assertTrue("3.3.3", dfList.contains(file3));
 
 			// under project
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("3.4", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("3.5", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("3.6", 3, df.length);
 			dfList = Arrays.asList(df);
 			assertTrue("3.6.1", dfList.contains(file1));
 			assertTrue("3.6.2", dfList.contains(file2));
 			assertTrue("3.6.3", dfList.contains(file3));
 
 			// under folder
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("3.7", 0, df.length);
 
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("3.8", 2, df.length);
 
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("3.9", 3, df.length);
 
 			// under folder2
 			df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("3.10", 0, df.length);
 
 			df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("3.11", 1, df.length);
 
 			df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("3.12", 1, df.length);
 
 		} catch (CoreException e) {
 			fail("3.00", e);
 		}
 
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.5", e);
 		}
 
 		// once the project is gone, so is all the history for that project	
 		try {
 			// under root
 			IFile[] df = root.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("4.1", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("4.2", 0, df.length);
 
 			df = root.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("4.3", 0, df.length);
 
 			// under project
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("4.4", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("4.5", 0, df.length);
 
 			df = project.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("4.6", 0, df.length);
 
 			// under folder
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("4.7", 0, df.length);
 
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("4.8", 0, df.length);
 
 			df = folder.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("4.9", 0, df.length);
 
 			// under folder2
 			df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ZERO, getMonitor());
 			assertEquals("4.10", 0, df.length);
 
 			df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_ONE, getMonitor());
 			assertEquals("4.11", 0, df.length);
 
 			df = folder2.findDeletedMembersWithHistory(IResource.DEPTH_INFINITE, getMonitor());
 			assertEquals("4.12", 0, df.length);
 
 		} catch (CoreException e) {
 			fail("4.00", e);
 		}
 	}
 
 	/**
 	 * Simple move case for History Store when the local history is being
 	 * copied.
 	 *
 	 * Scenario:
 	 *   1. Create file						"content 1"
 	 *   2. Set new content					"content 2"
 	 *   3. Set new content					"content 3"
 	 *   4. Move file
 	 *   5. Set new content	to moved file	"content 4"
 	 *   6. Set new content to moved file	"content 5"
 	 *
 	 * The original file should have two states available.
 	 * But the moved file should have 4 states as it retains the states from
 	 * before the move took place as well.
 	 */
 	public void testSimpleMove() {
 
 		/* Initialize common objects. */
 		IProject project = getWorkspace().getRoot().getProject("SimpleMoveProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		String[] contents = {"content1", "content2", "content3", "content4", "content5"};
 		IFile file = project.getFile("simpleMoveFileWithCopy");
 		IFile moveFile = project.getFile("copyOfSimpleMoveFileWithCopy");
 
 		/* Create first file. */
 		try {
 			file.create(getContents(contents[0]), true, null);
 		} catch (CoreException e) {
 			fail("1.2", e);
 		}
 
 		/* Set new contents on source file. Should add two entries to the history store. */
 		try {
 			file.setContents(getContents(contents[1]), true, true, null);
 			file.setContents(getContents(contents[2]), true, true, null);
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 
 		/* Move source file to second location. 
 		 * Moved files should have the history of the original file.
 		 */
 		try {
 			file.move(moveFile.getFullPath(), true, null);
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 
 		/* Check history for both files. */
 		try {
 			IFileState[] states = file.getHistory(null);
 			assertEquals("4.0", 2, states.length);
 			states = moveFile.getHistory(null);
 			assertEquals("4.1", 2, states.length);
 		} catch (CoreException e) {
 			fail("4.2", e);
 		}
 
 		/* Set new contents on moved file. Should add two entries to the history store. */
 		try {
 			moveFile.setContents(getContents(contents[3]), true, true, null);
 			moveFile.setContents(getContents(contents[4]), true, true, null);
 		} catch (CoreException e) {
 			fail("5.0", e);
 		}
 
 		/* Check history for both files. */
 		try {
 			// Check log for original file.
 			IFileState[] states = file.getHistory(null);
 			assertEquals("6.0", 2, states.length);
 			assertTrue("6.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("6.2", compareContent(getContents(contents[0]), states[1].getContents()));
 
 			// Check log for moved file.
 			states = moveFile.getHistory(null);
 			assertEquals("6.3", 4, states.length);
 			assertTrue("6.4", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("6.5", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("6.6", compareContent(getContents(contents[1]), states[2].getContents()));
 			assertTrue("6.7", compareContent(getContents(contents[0]), states[3].getContents()));
 
 		} catch (CoreException e) {
 			fail("6.8", e);
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("7.0", e);
 		}
 	}
 
 	/**
 	 * Simple copy case for History Store when the local history is being
 	 * copied.
 	 *
 	 * Scenario:
 	 *   1. Create file						"content 1"
 	 *   2. Set new content					"content 2"
 	 *   3. Set new content					"content 3"
 	 *   4. Move file
 	 *   5. Set new content	to copied file	"content 4"
 	 *   6. Set new content to copied file	"content 5"
 	 *
 	 * The original file should have two states available.
 	 * But the copied file should have 4 states as it retains the states from
 	 * before the copy took place as well.
 	 */
 	public void testSimpleCopy() {
 
 		/* Initialize common objects. */
 		IProject project = getWorkspace().getRoot().getProject("SimpleCopyProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		String[] contents = {"content1", "content2", "content3", "content4", "content5"};
 		IFile file = project.getFile("simpleCopyFileWithHistoryCopy");
 		IFile copyFile = project.getFile("copyOfSimpleCopyFileWithHistoryCopy");
 
 		/* Create first file. */
 		try {
 			file.create(getContents(contents[0]), true, null);
 		} catch (CoreException e) {
 			fail("1.2", e);
 		}
 
 		/* Set new contents on first file. Should add two entries to the history store. */
 		try {
 			file.setContents(getContents(contents[1]), true, true, null);
 			file.setContents(getContents(contents[2]), true, true, null);
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 
 		/* Copy first file to the second. Second file should have no history. */
 		try {
 			file.copy(copyFile.getFullPath(), true, null);
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 
 		/* Check history for both files. */
 		try {
 			IFileState[] states = file.getHistory(null);
 			assertEquals("4.0", 2, states.length);
 			states = copyFile.getHistory(null);
 			assertEquals("4.1", 2, states.length);
 		} catch (CoreException e) {
 			fail("4.2", e);
 		}
 
 		/* Set new contents on second file. Should add two entries to the history store. */
 		try {
 			copyFile.setContents(getContents(contents[3]), true, true, null);
 			copyFile.setContents(getContents(contents[4]), true, true, null);
 		} catch (CoreException e) {
 			fail("5.0", e);
 		}
 
 		/* Check history for both files. */
 		try {
 			// Check log for original file.
 			IFileState[] states = file.getHistory(null);
 			assertEquals("6.0", 2, states.length);
 			assertTrue("6.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("6.2", compareContent(getContents(contents[0]), states[1].getContents()));
 
 			// Check log for copy.
 			states = copyFile.getHistory(null);
 			assertEquals("6.3", 4, states.length);
 			assertTrue("6.4", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("6.5", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("6.6", compareContent(getContents(contents[1]), states[2].getContents()));
 			assertTrue("6.7", compareContent(getContents(contents[0]), states[3].getContents()));
 
 		} catch (CoreException e) {
 			fail("6.8", e);
 		}
 
 		/* remove garbage */
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("7.0", e);
 		}
 	}
 
 	/**
 	 * Move case for History Store of folder when the local history is being
 	 * copied.
 	 *
 	 * Scenario:
 	 *   1. Create folder (folder1)
 	 *   2. Create file						"content 1"
 	 *   3. Set new content					"content 2"
 	 *   4. Set new content					"content 3"
 	 *   5. Move folder
 	 *   6. Set new content	to moved file	"content 4"
 	 *   7. Set new content to moved file	"content 5"
 	 *
 	 * The original file should have two states available.
 	 * But the moved file should have 4 states as it retains the states from
 	 * before the move took place as well.
 	 */
 	public void testMoveFolder() {
 		String[] contents = {"content1", "content2", "content3", "content4", "content5"};
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("MyProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		IFile file = project.getFile("file1.txt");
 
 		IFolder folder = project.getFolder("folder1");
 		IFolder folder2 = project.getFolder("folder2");
 		file = folder.getFile("file1.txt");
 		try {
 			// Setup folder1 and file1.txt with some local history
 			folder.create(true, true, getMonitor());
 			file.create(getContents(contents[0]), true, getMonitor());
 			file.setContents(getContents(contents[1]), true, true, getMonitor());
 			file.setContents(getContents(contents[2]), true, true, getMonitor());
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 			assertTrue("1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 
 			// Now do the move
 			folder.move(folder2.getFullPath(), true, getMonitor());
 
 			// Check to make sure the file has been moved
 			IFile file2 = folder2.getFile("file1.txt");
 			assertTrue("1.3", file2.getFullPath().toString().endsWith("folder2/file1.txt"));
 
 			// Give the new (moved file) some new contents
 			file2.setContents(getContents(contents[3]), true, true, getMonitor());
 			file2.setContents(getContents(contents[4]), true, true, getMonitor());
 
 			// Check the local history of both files
 			states = file.getHistory(getMonitor());
 			assertEquals("2.0", 2, states.length);
 			assertTrue("2.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("2.2", compareContent(getContents(contents[0]), states[1].getContents()));
 			states = file2.getHistory(getMonitor());
 			assertEquals("2.3", 4, states.length);
 			assertTrue("2.4", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("2.5", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("2.6", compareContent(getContents(contents[1]), states[2].getContents()));
 			assertTrue("2.7", compareContent(getContents(contents[0]), states[3].getContents()));
 		} catch (CoreException e) {
 			fail("2.8", e);
 		}
 
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 	}
 
 	/**
 	 * Copy case for History Store of folder when the local history is being
 	 * copied.
 	 *
 	 * Scenario:
 	 *   1. Create folder (folder1)
 	 *   2. Create file						"content 1"
 	 *   3. Set new content					"content 2"
 	 *   4. Set new content					"content 3"
 	 *   5. Copy folder
 	 *   6. Set new content	to moved file	"content 4"
 	 *   7. Set new content to moved file	"content 5"
 	 *
 	 * The original file should have two states available.
 	 * But the copied file should have 4 states as it retains the states from
 	 * before the copy took place as well.
 	 */
 	public void testCopyFolder() {
 		String[] contents = {"content1", "content2", "content3", "content4", "content5"};
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("CopyFolderProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		IFile file = project.getFile("file1.txt");
 
 		IFolder folder = project.getFolder("folder1");
 		IFolder folder2 = project.getFolder("folder2");
 		file = folder.getFile("file1.txt");
 		try {
 			// Setup folder1 and file1.txt with some local history
 			folder.create(true, true, getMonitor());
 			file.create(getContents(contents[0]), true, getMonitor());
 			file.setContents(getContents(contents[1]), true, true, getMonitor());
 			file.setContents(getContents(contents[2]), true, true, getMonitor());
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 			assertTrue("1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 
 			// Now do the move
 			folder.copy(folder2.getFullPath(), true, getMonitor());
 
 			// Check to make sure the file has been copied
 			IFile file2 = folder2.getFile("file1.txt");
 			assertTrue("1.3", file2.getFullPath().toString().endsWith("folder2/file1.txt"));
 
 			// Give the new (copied file) some new contents
 			file2.setContents(getContents(contents[3]), true, true, getMonitor());
 			file2.setContents(getContents(contents[4]), true, true, getMonitor());
 
 			// Check the local history of both files
 			states = file.getHistory(getMonitor());
 			assertEquals("2.0", 2, states.length);
 			assertTrue("2.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("2.2", compareContent(getContents(contents[0]), states[1].getContents()));
 			states = file2.getHistory(getMonitor());
 			assertEquals("2.3", 4, states.length);
 			assertTrue("2.4", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("2.5", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("2.6", compareContent(getContents(contents[1]), states[2].getContents()));
 			assertTrue("2.7", compareContent(getContents(contents[0]), states[3].getContents()));
 		} catch (CoreException e) {
 			fail("2.8", e);
 		}
 
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 	}
 
 	/**
 	 * Move case for History Store of project.  Note that local history is
 	 * NOT copied for a project move.
 	 *
 	 * Scenario:
 	 *   1. Create folder (folder1)
 	 *   2. Create file						"content 1"
 	 *   2. Set new content					"content 2"
 	 *   3. Set new content					"content 3"
 	 *   4. Copy folder
 	 *   5. Set new content	to moved file	"content 4"
 	 *   6. Set new content to moved file	"content 5"
 	 *
 	 * The original file should have two states available.
 	 * But the copied file should have 4 states as it retains the states from
 	 * before the copy took place as well.
 	 */
 	public void testMoveProject() {
 		String[] contents = {"content1", "content2", "content3", "content4", "content5"};
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("MoveProjectProject");
 		IProject project2 = getWorkspace().getRoot().getProject("SecondMoveProjectProject");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 
 		IFile file = project.getFile("file1.txt");
 
 		IFolder folder = project.getFolder("folder1");
 		file = folder.getFile("file1.txt");
 		try {
 			// Setup folder1 and file1.txt with some local history
 			folder.create(true, true, getMonitor());
 			file.create(getContents(contents[0]), true, getMonitor());
 			file.setContents(getContents(contents[1]), true, true, getMonitor());
 			file.setContents(getContents(contents[2]), true, true, getMonitor());
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 			assertTrue("1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 
 			// Now do the move
 			project.move(new Path("SecondMoveProjectProject"), true, getMonitor());
 
 			// Check to make sure the file has been moved
 			IFile file2 = project2.getFile("folder1/file1.txt");
 			assertTrue("1.3", file2.getFullPath().toString().endsWith("SecondMoveProjectProject/folder1/file1.txt"));
 
 			// Give the new (copied file) some new contents
 			file2.setContents(getContents(contents[3]), true, true, getMonitor());
 			file2.setContents(getContents(contents[4]), true, true, getMonitor());
 
 			// Check the local history of both files
 			states = file.getHistory(getMonitor());
 			assertEquals("2.0", 2, states.length);
 			assertTrue("2.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("2.2", compareContent(getContents(contents[0]), states[1].getContents()));
 			states = file2.getHistory(getMonitor());
 			assertEquals("2.3", 4, states.length);
 			assertTrue("2.4", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("2.5", compareContent(getContents(contents[2]), states[1].getContents()));
 			assertTrue("2.6", compareContent(getContents(contents[1]), states[2].getContents()));
 			assertTrue("2.7", compareContent(getContents(contents[0]), states[3].getContents()));
 		} catch (CoreException e) {
 			fail("2.9", e);
 		}
 
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 	}
 
 	/*
 	 * This little helper method makes sure that the history store is
 	 * completely clean after it is invoked.  If a history store entry or
 	 * a file is left, it may become part of the history for another file in
 	 * another test (if this file has the same name).
 	 */
 	private void wipeHistoryStore() {
 		IHistoryStore store = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		// Remove all the entries from the history store index.  Note that
 		// this does not cause the history store states to be removed.
 		store.remove(Path.ROOT, getMonitor());
 		// Now make sure all the states are really removed.
 		if (store instanceof HistoryStore)
 			org.eclipse.core.internal.localstore.TestingSupport.removeGarbage((HistoryStore) store);
 	}
 
 	/*
 	 * This test is designed to exercise the public API method
 	 * HistoryStore.copyHistory().  The following tests will be performed:
 	 * - give a null source path
 	 * - give a null destination path
 	 * - give the same path for source and destination
 	 * - give an invalid source path but a valid destination path
 	 * - give an invalid destination path but a valid source path
 	 */
 	public void testCopyHistoryFile() {
 		// Create a project, folder and file so we have some history store
 		// Should have a project that appears as follows:
 		// - project name TestCopyHistoryProject
 		// - has one folder called folder1
 		// - folder1 has one file called file1.txt
 		// - file1.txt was created with initial data "content1"
 		// - change data in file1.txt to be "content2"
 		// - change data in file1.txt to be "content3"
 		// As a result of the above, there should be 2 history store states for
 		// file1.txt (one with "contents1" and the other with "contents2".
 
 		String[] contents = {"content0", "content1", "content2", "content3", "content4"};
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("TestCopyHistoryProject");
 		ensureExistsInWorkspace(project, true);
 
 		IFolder folder = project.getFolder("folder1");
 		IFile file = folder.getFile("file1.txt");
 		IFile file2 = folder.getFile("file2.txt");
 		try {
 			// Setup folder1 and file1.txt with some local history
 			folder.create(true, true, getMonitor());
 			file.create(getContents(contents[0]), true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.0", e);
 			}
 			file.setContents(getContents(contents[1]), true, true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.1", e);
 			}
 			file.setContents(getContents(contents[2]), true, true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.2", e);
 			}
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 			assertTrue("1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 			file2.create(getContents(contents[3]), true, getMonitor());
 			file2.setContents(getContents(contents[4]), true, true, getMonitor());
 		} catch (CoreException e) {
 			fail("1.3", e);
 		}
 
 		// Run some tests with illegal arguments
 		LogListenerVerifier verifier = new LogListenerVerifier();
 		ILog log = ResourcesPlugin.getPlugin().getLog();
 		log.addLogListener(verifier);
 
 		// Test with null source and/or destination
 		IHistoryStore store = ((Resource) file).getLocalManager().getHistoryStore();
 		verifier.addExpected(IResourceStatus.INTERNAL_ERROR);
 		store.copyHistory(null, null);
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("1.4 ", e);
 		}
 		verifier.reset();
 
 		verifier.addExpected(IResourceStatus.INTERNAL_ERROR);
 		store.copyHistory(null, file2.getLocation());
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("1.5 ", e);
 		}
 		verifier.reset();
 
 		verifier.addExpected(IResourceStatus.INTERNAL_ERROR);
 		store.copyHistory(file.getLocation(), null);
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("1.6 ", e);
 		}
 		verifier.reset();
 
 		// Try to copy the history store stuff to the same location
 		verifier.addExpected(IResourceStatus.INTERNAL_ERROR);
 		store.copyHistory(file.getLocation(), file.getLocation());
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("1.7 ", e);
 		}
 		verifier.reset();
 
 		// Remember to remove the log listener now that we are done
 		// testing illegal arguments.
 		log.removeLogListener(verifier);
 
 		// Test a valid copy of a file
 		store.copyHistory(file.getFullPath(), file2.getFullPath());
 		IFileState[] states = null;
 		try {
 			states = file2.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("2.3");
 		}
 		assertEquals("2.4", 3, states.length);
 		try {
 			assertTrue("2.5", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("2.6", compareContent(getContents(contents[1]), states[1].getContents()));
 			assertTrue("2.7", compareContent(getContents(contents[0]), states[2].getContents()));
 		} catch (CoreException e) {
 			fail("2.8");
 		}
 	}
 
 	public void testCopyHistoryFolder() {
 		String[] contents = {"content0", "content1", "content2", "content3", "content4"};
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("TestCopyHistoryProject");
 		ensureExistsInWorkspace(project, true);
 
 		IFolder folder = project.getFolder("folder1");
 		IFolder folder2 = project.getFolder("folder2");
 		IFile file = folder.getFile("file1.txt");
 		IFile file2 = folder2.getFile("file1.txt");
 		try {
 			// Setup folder1 and file1.txt with some local history
 			folder.create(true, true, getMonitor());
 			file.create(getContents(contents[0]), true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.0", e);
 			}
 			file.setContents(getContents(contents[1]), true, true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.1", e);
 			}
 			file.setContents(getContents(contents[2]), true, true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.2", e);
 			}
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 			assertTrue("1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 			folder2.create(true, true, getMonitor());
 			file2.create(getContents(contents[3]), true, getMonitor());
 			file2.setContents(getContents(contents[4]), true, true, getMonitor());
 		} catch (CoreException e) {
 			fail("1.9", e);
 		}
 
 		// Test a valid copy of a folder
 		IHistoryStore store = ((Resource) file).getLocalManager().getHistoryStore();
 		store.copyHistory(folder.getFullPath(), folder2.getFullPath());
 		IFileState[] states = null;
 		try {
 			states = file2.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("2.3");
 		}
 		assertEquals("2.4", 3, states.length);
 		try {
 			assertTrue("2.5", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("2.6", compareContent(getContents(contents[1]), states[1].getContents()));
 			assertTrue("2.7", compareContent(getContents(contents[0]), states[2].getContents()));
 		} catch (CoreException e) {
 			fail("2.8");
 		}
 	}
 
 	public void testCopyHistoryProject() {
 		String[] contents = {"content0", "content1", "content2", "content3", "content4"};
 		// create common objects
 		IProject project = getWorkspace().getRoot().getProject("TestCopyHistoryProject");
 		IProject project2 = getWorkspace().getRoot().getProject("TestCopyHistoryProject2");
 		ensureExistsInWorkspace(new IResource[] {project, project2}, true);
 
 		IFolder folder = project.getFolder("folder1");
 		IFolder folder2 = project2.getFolder("folder1");
 		IFile file = folder.getFile("file1.txt");
 		IFile file2 = folder2.getFile("file1.txt");
 		try {
 			// Setup folder1 and file1.txt with some local history
 			folder.create(true, true, getMonitor());
 			file.create(getContents(contents[0]), true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.0", e);
 			}
 			file.setContents(getContents(contents[1]), true, true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.1", e);
 			}
 			file.setContents(getContents(contents[2]), true, true, getMonitor());
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				fail("0.2", e);
 			}
 			IFileState[] states = file.getHistory(getMonitor());
 			assertEquals("1.0", 2, states.length);
 			assertTrue("1.1", compareContent(getContents(contents[1]), states[0].getContents()));
 			assertTrue("1.2", compareContent(getContents(contents[0]), states[1].getContents()));
 			folder2.create(true, true, getMonitor());
 			file2.create(getContents(contents[3]), true, getMonitor());
 			file2.setContents(getContents(contents[4]), true, true, getMonitor());
 		} catch (CoreException e) {
 			fail("1.9", e);
 		}
 
 		// Test a valid copy of a folder
 		IHistoryStore store = ((Resource) file).getLocalManager().getHistoryStore();
 		store.copyHistory(project.getFullPath(), project2.getFullPath());
 		IFileState[] states = null;
 		try {
 			states = file2.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("2.3");
 		}
 		assertEquals("2.4", 3, states.length);
 		try {
 			assertTrue("2.5", compareContent(getContents(contents[3]), states[0].getContents()));
 			assertTrue("2.6", compareContent(getContents(contents[1]), states[1].getContents()));
 			assertTrue("2.7", compareContent(getContents(contents[0]), states[2].getContents()));
 		} catch (CoreException e) {
 			fail("2.8");
 		}
 	}
 
 	public void testBug28238() {
 		// paths to mimic files in the workspace
 		IProject project = getWorkspace().getRoot().getProject("myproject");
 		IFolder folder = project.getFolder("myfolder");
 		IFolder destinationFolder = project.getFolder("myfolder2");
 		IFile file = folder.getFile("myfile.txt");
 		IFile destinationFile = destinationFolder.getFile(file.getName());
 
 		IHistoryStore store = ((Resource) getWorkspace().getRoot()).getLocalManager().getHistoryStore();
 
 		// location of the data on disk
 		IPath path = getRandomLocation();
 		try {
 			createFileInFileSystem(path, getRandomContents());
 		} catch (IOException e) {
 			fail("1.0", e);
 		}
 
 		// add the data to the history store
 		store.addState(file.getFullPath(), path.toFile(), System.currentTimeMillis(), true);
 		IFileState[] states = store.getStates(file.getFullPath(), getMonitor());
 		assertEquals("2.0", 1, states.length);
 
 		// copy the data
 		store.copyHistory(folder.getFullPath(), destinationFolder.getFullPath());
 
 		states = store.getStates(destinationFile.getFullPath(), getMonitor());
 		assertEquals("3.0", 1, states.length);
 
 		// Cleanup
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("4.0", e);
 		}
 	}
 
 	public void testBug28603() {
 		// paths to mimic files in the workspace
 		IProject project = getWorkspace().getRoot().getProject("myproject");
 		IFolder folder1 = project.getFolder("myfolder1");
 		IFolder folder2 = project.getFolder("myfolder2");
 		IFile file1 = folder1.getFile("myfile.txt");
 		IFile file2 = folder2.getFile(file1.getName());
 
 		ensureExistsInWorkspace(new IResource[] {project, folder1, folder2}, true);
 		try {
 			file1.create(getRandomContents(), IResource.FORCE, getMonitor());
 			file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 			file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 			file1.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		int maxStates = ResourcesPlugin.getWorkspace().getDescription().getMaxFileStates();
 
 		IFileState[] states = null;
 		try {
 			states = file1.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 		assertEquals("1.1", 3, states.length);
 		int currentStates = 3;
 
 		for (int i = 0; i < maxStates + 10; i++) {
 			try {
 				states = file1.getHistory(getMonitor());
 			} catch (CoreException e) {
 				fail("2.0", e);
 			}
 			assertEquals("2.1 file1 states", currentStates, states.length);
 			try {
 				file1.move(file2.getFullPath(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("2.2", e);
 			}
 
 			try {
 				states = file2.getHistory(getMonitor());
 			} catch (CoreException e) {
 				fail("2.3", e);
 			}
 			currentStates = currentStates < maxStates ? currentStates + 1 : maxStates;
 			assertEquals("2.4 file2 states", currentStates, states.length);
 			try {
 				file2.move(file1.getFullPath(), true, true, getMonitor());
 			} catch (CoreException e) {
 				fail("2.5", e);
 			}
 			try {
 				states = file1.getHistory(getMonitor());
 			} catch (CoreException e) {
 				fail("2.6", e);
 			}
 			currentStates = currentStates < maxStates ? currentStates + 1 : maxStates;
 			assertEquals("2.7 file1 states", currentStates, states.length);
 		}
 	}
 
 	protected void addToHistory(String message, IHistoryStore store, IPath path, InputStream input) {
 		IPath localLocation = getRandomLocation();
 		try {
 			createFileInFileSystem(localLocation, input);
 		} catch (IOException e) {
 			fail(message, e);
 		}
 		store.addState(path, localLocation.toFile(), System.currentTimeMillis(), true);
 	}
 
 	public void testAccept() {
 		IHistoryStore hs = ((Resource) getWorkspace().getRoot()).getLocalManager().getHistoryStore();
 		if (!(hs instanceof HistoryStore))
 			return;
 		HistoryStore store = (HistoryStore) hs;
 		IPath a = new Path("/a");
 		IPath ab = a.append("b");
 		IPath abc = ab.append("c");
 
 		IPath a1 = new Path("/a1");
 
 		addToHistory("1.0", store, a, getRandomContents());
 		addToHistory("1.1", store, ab, getRandomContents());
 		addToHistory("1.2", store, abc, getRandomContents());
 		addToHistory("1.3", store, a1, getRandomContents());
 
 		// visit only /a
 		HistoryStoreVisitorVerifier verifier = new HistoryStoreVisitorVerifier();
 		verifier.addExpected(a);
 		org.eclipse.core.internal.localstore.TestingSupport.accept(store, a, verifier, false);
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("2.0", e);
 		}
 
 		// visit /a and all its children. Ensure that we don't visit /a1
 		verifier.reset();
 		verifier.addExpected(a);
 		verifier.addExpected(ab);
 		verifier.addExpected(abc);
 		org.eclipse.core.internal.localstore.TestingSupport.accept(store, a, verifier, true);
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("2.1", e);
 		}
 
 		// visit starting at the root. Should visit all entries
 		verifier.reset();
 		verifier.addExpected(a);
 		verifier.addExpected(ab);
 		verifier.addExpected(abc);
 		verifier.addExpected(a1);
 		org.eclipse.core.internal.localstore.TestingSupport.accept(store, Path.ROOT, verifier, true);
 		try {
 			verifier.verify();
 		} catch (VerificationFailedException e) {
 			fail("2.2", e);
 		}
 	}
 
 	public void testSimultaneousStates() {
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		IFile file = project.getFile("file.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 
 		IFileState[] states = null;
 		try {
 			getWorkspace().save(true, null);
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 		assertEquals("1.2", 1, states.length);
 
 		long lastModifiedTime = states[0].getModificationTime();
 
 		// Create more states for this file with the same path
 		// and last modification time
 		IHistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.3", e);
 		}
 		assertEquals("1.4", 3, states.length);
 
 		// Now get all the HistoryStoreEntry's that match these
 		// states and make sure the counter is different for
 		// each one.
 		final String INDEX_FILE = ".index"; //$NON-NLS-1$
 		IPath location = ((Workspace) getWorkspace()).getMetaArea().getHistoryStoreLocation();
 		IndexedStoreWrapper store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 
 		boolean[] counts = {false, false, false};
 
 		store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 		byte[] key = HistoryStoreEntry.keyPrefixToBytes(file.getFullPath(), lastModifiedTime);
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.find(key);
 			// Check for a prefix match.
 			while (cursor.keyMatches(key)) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				if (storedKey.length - ILocalStoreConstants.SIZE_COUNTER == key.length) {
 					HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 					int counter = storedEntry.getCount();
 					assertTrue("1.5", counter < 3);
 					// Make sure we haven't already seen this
 					// counter.
 					assertFalse("1.6", counts[counter]);
 					counts[counter] = true;
 				}
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.7");
 		}
 		// Make sure we saw each of the counts once
 		for (int i = 0; i < counts.length; i++) {
 			assertTrue("1.8" + i, counts[i]);
 		}
 
 		// Clean up
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("1.9", e);
 		}
 	}
 
 	public void testTooManySimultaneousStates() {
 		// What happens if we have so many simultaneous states, that we
 		// can't store them?
 		class checkErrorListener implements ILogListener {
 			private String expectedMessage = null;
 			private boolean triggered = false;
 
 			public void logging(IStatus status, String plugin) {
 				if (plugin.equals(ResourcesPlugin.PI_RESOURCES)) {
 					if (status.getCode() == IResourceStatus.FAILED_WRITE_LOCAL) {
 						assertTrue("1.4", expectedMessage.equals(status.getMessage()));
 						triggered = true;
 					}
 				}
 			}
 
 			public void setExpectedMessage(String message) {
 				expectedMessage = message;
 			}
 
 			public boolean hasBeenTriggered() {
 				return triggered;
 			}
 		}
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project");
 		IFile file = project.getFile("file.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 		assertEquals("1.2", 1, states.length);
 
 		long lastModifiedTime = states[0].getModificationTime();
 
 		// Create more states for this file with the same path
 		// and last modification time
 		IHistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		checkErrorListener myListener = new checkErrorListener();
 		String message = Policy.bind("history.tooManySimUpdates", file.getFullPath().toString(), new Date(lastModifiedTime).toString()); //$NON-NLS-1$
 		myListener.setExpectedMessage(message);
 		ResourcesPlugin.getPlugin().getLog().addLogListener(myListener);
 		for (int i = 0; i <= Byte.MAX_VALUE; i++) {
 			historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		}
 		// Make sure the we did hit the error message
 		assertTrue("1.3", myListener.hasBeenTriggered());
 	}
 
 	private void removeHistoryStoreEntry(IndexedStoreWrapper store, HistoryStoreEntry entry) throws IndexedStoreException {
 		// This method provided as a convenience for removing a HistoryStoreEntry.
 		// It is pirated directly from HistoryStore.remove(HistoryStoreEntry)
 		// which is a protected method.
 		try {
 			Vector objectIds = store.getIndex().getObjectIdentifiersMatching(entry.getKey());
 			if (objectIds.size() == 1) {
 				store.removeObject((ObjectID) objectIds.get(0));
 			} else if (objectIds.size() > 1) {
 				// There is a problem with more than one entry having the same
 				// key.
 				String message = Policy.bind("history.tooManySimUpdates", entry.getPath().toString(), new Date(entry.getLastModified()).toString()); //$NON-NLS-1$
 				ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, entry.getPath(), message, null);
 				ResourcesPlugin.getPlugin().getLog().log(status);
 			}
 		} catch (Exception e) {
 			String[] messageArgs = {entry.getPath().toString(), new Date(entry.getLastModified()).toString(), entry.getUUID().toString()};
 			String message = Policy.bind("history.specificProblemsCleaning", messageArgs); //$NON-NLS-1$
 			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, null, message, e);
 			ResourcesPlugin.getPlugin().getLog().log(status);
 		}
 		entry.remove();
 	}
 
 	public void testIndexOrdering1() {
 		/*
 		 * This test will create a file and give it a local history state.
 		 * It will then create 128 states for this file with the same last
 		 * modified time (this is currently the maximum number of states you
 		 * can have with the same filename and last modified time).  After this,
 		 * 3 states in the middle of the ordering of these 128 states will be
 		 * removed.  And finally a new state will be added (with the same 
 		 * filename and last modified time).  This last state should cause all
 		 * existing states to be reordered so that the new state is ordered last
 		 * (i.e., close the holes where the 3 states were removed and add the
 		 * new state on the end).
 		 */
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project2");
 		IFile file = project.getFile("file2.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 
 		// Cause some new states to be added to this file with the same
 		// timestamp
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 		assertEquals("1.2", 1, states.length);
 		long lastModifiedTime = states[0].getModificationTime();
 
 		IHistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		for (int i = states.length; i <= Byte.MAX_VALUE; i++) {
 			historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		}
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.3", e);
 		}
 		assertEquals("1.4", Byte.MAX_VALUE + 1, states.length);
 
 		final String INDEX_FILE = ".index"; //$NON-NLS-1$
 		IPath location = ((Workspace) getWorkspace()).getMetaArea().getHistoryStoreLocation();
 		IndexedStoreWrapper store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 
 		store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 		// Remove the states with counts 2, 3, and 4
 		try {
 			IndexCursor cursor = store.getCursor();
 			byte[] keyPrefix = HistoryStoreEntry.keyPrefixToBytes(file.getFullPath(), lastModifiedTime);
 			byte[] key = new byte[keyPrefix.length + 1];
 			// Copy all values into full key.
 			int destPosition = 0;
 			System.arraycopy(keyPrefix, 0, key, destPosition, keyPrefix.length);
 			destPosition += keyPrefix.length;
 			for (byte i = 2; i <= 4; i++) {
 				key[destPosition] = i;
 				cursor.find(key);
 				// Check for a prefix match.
 				if (cursor.keyMatches(key)) {
 					byte[] storedKey = cursor.getKey();
 					HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 					int counter = storedEntry.getCount();
 					removeHistoryStoreEntry(store, storedEntry);
 					cursor.reset();
 				}
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.5");
 		}
 		// Make sure that the states with counter 2, 3 or 4 are missing
 		BitSet counters = new BitSet();
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.findFirstEntry();
 			// Check for a prefix match.
 			while (cursor.isSet()) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 				byte counter = storedEntry.getCount();
 				counters.set(counter);
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.6");
 		}
 
 		assertEquals("1.7", Byte.MAX_VALUE + 1, counters.length());
 		assertFalse("1.8", counters.get(2));
 		assertFalse("1.9", counters.get(3));
 		assertFalse("1.9", counters.get(4));
 		assertTrue("1.10", counters.get(Byte.MAX_VALUE));
 
 		// This should cause all the counts to be shifted and the new state
 		// added at the end.
 		historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		counters.clear();
 
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.findFirstEntry();
 			// Check for a prefix match.
 			while (cursor.isSet()) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 				int counter = storedEntry.getCount();
 				counters.set(counter);
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.11");
 		}
 		assertEquals("1.12", Byte.MAX_VALUE - 1, counters.length());
 		assertTrue("1.13", counters.get(2));
 		assertTrue("1.14", counters.get(3));
 		assertTrue("1.15", counters.get(4));
 		assertFalse("1.16", counters.get(Byte.MAX_VALUE - 1));
 		assertFalse("1.17", counters.get(Byte.MAX_VALUE));
 
 		// cleanup
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 	}
 
 	public void testIndexOrdering2() {
 		/*
 		 * This test will create a file and give it a local history state.
 		 * It will then create 128 states for this file with the same last
 		 * modified time (this is currently the maximum number of states you
 		 * can have with the same filename and last modified time).  After this,
 		 * 3 states at the beginning of the ordering of these 128 states will be
 		 * removed.  And finally a new state will be added (with the same 
 		 * filename and last modified time).  This last state should cause all
 		 * existing states to be reordered so that the new state is ordered last
 		 * (i.e., close the holes where the 3 states were removed and add the
 		 * new state on the end).
 		 */
 
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project11");
 		IFile file = project.getFile("file17.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 
 		// Cause some new states to be added to this file with the same
 		// timestamp
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 		assertEquals("1.2", 1, states.length);
 		long lastModifiedTime = states[0].getModificationTime();
 
 		IHistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		for (int i = states.length; i <= Byte.MAX_VALUE; i++) {
 			historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		}
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.3", e);
 		}
 		assertEquals("1.4", Byte.MAX_VALUE + 1, states.length);
 
 		final String INDEX_FILE = ".index"; //$NON-NLS-1$
 		IPath location = ((Workspace) getWorkspace()).getMetaArea().getHistoryStoreLocation();
 		IndexedStoreWrapper store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 
 		store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 		// Remove the states with counts 0, 1, and 2
 		try {
 			IndexCursor cursor = store.getCursor();
 			byte[] keyPrefix = HistoryStoreEntry.keyPrefixToBytes(file.getFullPath(), lastModifiedTime);
 			byte[] key = new byte[keyPrefix.length + 1];
 			// Copy all values into full key.
 			int destPosition = 0;
 			System.arraycopy(keyPrefix, 0, key, destPosition, keyPrefix.length);
 			destPosition += keyPrefix.length;
 			for (byte i = 0; i <= 2; i++) {
 				key[destPosition] = i;
 				cursor.find(key);
 				// Check for a prefix match.
 				if (cursor.keyMatches(key)) {
 					byte[] storedKey = cursor.getKey();
 					HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 					removeHistoryStoreEntry(store, storedEntry);
 					cursor.reset();
 				}
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.5");
 		}
 		// Make sure that the states with counter 0, 1 or 2 are missing
 		BitSet counters = new BitSet();
 		counters.clear();
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.findFirstEntry();
 			// Check for a prefix match.
 			while (cursor.isSet()) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 				byte stateCount = storedEntry.getCount();
 				counters.set(stateCount);
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.6");
 		}
 
 		assertEquals("1.7", Byte.MAX_VALUE + 1, counters.length());
 		assertFalse("1.8", counters.get(0));
 		assertFalse("1.9.1", counters.get(1));
 		assertFalse("1.9.2", counters.get(2));
 		assertTrue("1.10", counters.get(Byte.MAX_VALUE));
 
 		// This should cause all the counts to be shifted and the new state
 		// added at the end.
 		historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		counters.clear();
 
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.findFirstEntry();
 			// Check for a prefix match.
 			while (cursor.isSet()) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 				int counter = storedEntry.getCount();
 				counters.set(counter);
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.11");
 		}
 		assertEquals("1.12", Byte.MAX_VALUE - 1, counters.length());
 		assertTrue("1.13", counters.get(0));
 		assertTrue("1.14", counters.get(1));
 		assertTrue("1.15", counters.get(2));
 		assertFalse("1.16", counters.get(Byte.MAX_VALUE - 1));
 		assertFalse("1.17", counters.get(Byte.MAX_VALUE));
 
 		// cleanup
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 	}
 
 	public void testIndexOrdering3() {
 		/*
 		 * This test will create a file and give it a local history state.
 		 * It will then create 128 states for this file with the same last
 		 * modified time (this is currently the maximum number of states you
 		 * can have with the same filename and last modified time).  After this,
 		 * 3 states at the end of the ordering of these 128 states will be
 		 * removed.  And finally a new state will be added (with the same 
 		 * filename and last modified time).  This last state should not need to
 		 * do any reordering of existing states and the new state should be
 		 * added at the end of the ordering.
 		 */
 		/* Create common objects. */
 		IProject project = getWorkspace().getRoot().getProject("Project2");
 		IFile file = project.getFile("file2.txt");
 		try {
 			project.create(getMonitor());
 			project.open(getMonitor());
 			file.create(getRandomContents(), true, getMonitor());
 			file.setContents(getRandomContents(), IResource.FORCE | IResource.KEEP_HISTORY, getMonitor());
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 
 		// Cause some new states to be added to this file with the same
 		// timestamp
 		IFileState[] states = null;
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 		assertEquals("1.2", 1, states.length);
 		long lastModifiedTime = states[0].getModificationTime();
 
 		IHistoryStore historyStore = ((Workspace) getWorkspace()).getFileSystemManager().getHistoryStore();
 		for (int i = states.length; i <= Byte.MAX_VALUE; i++) {
 			historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		}
 		try {
 			states = file.getHistory(getMonitor());
 		} catch (CoreException e) {
 			fail("1.3", e);
 		}
 		assertEquals("1.4", Byte.MAX_VALUE + 1, states.length);
 
 		final String INDEX_FILE = ".index"; //$NON-NLS-1$
 		IPath location = ((Workspace) getWorkspace()).getMetaArea().getHistoryStoreLocation();
 		IndexedStoreWrapper store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 
 		store = new IndexedStoreWrapper(location.append(INDEX_FILE));
 		// Remove the states with counts 126, 127, and 128
 		try {
 			IndexCursor cursor = store.getCursor();
 			byte[] keyPrefix = HistoryStoreEntry.keyPrefixToBytes(file.getFullPath(), lastModifiedTime);
 			byte[] key = new byte[keyPrefix.length + 1];
 			// Copy all values into full key.
 			int destPosition = 0;
 			System.arraycopy(keyPrefix, 0, key, destPosition, keyPrefix.length);
 			destPosition += keyPrefix.length;
 			for (byte i = Byte.MAX_VALUE - 2; i <= Byte.MAX_VALUE && i >= 0; i++) {
 				key[destPosition] = i;
 				cursor.find(key);
 				// Check for a prefix match.
 				if (cursor.keyMatches(key)) {
 					byte[] storedKey = cursor.getKey();
 					HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 					int counter = storedEntry.getCount();
 					removeHistoryStoreEntry(store, storedEntry);
 					cursor.reset();
 				}
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.5");
 		}
 		// Make sure that the states with counter 2, 3 or 4 are missing
 		BitSet counters = new BitSet();
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.findFirstEntry();
 			// Check for a prefix match.
 			while (cursor.isSet()) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 				byte counter = storedEntry.getCount();
 				counters.set(counter);
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.6");
 		}
 
 		assertEquals("1.7", Byte.MAX_VALUE - 2, counters.length());
 		assertFalse("1.8", counters.get(Byte.MAX_VALUE - 2));
 		assertFalse("1.9.1", counters.get(Byte.MAX_VALUE - 1));
 		assertFalse("1.9.2", counters.get(Byte.MAX_VALUE));
 		assertTrue("1.10", counters.get(Byte.MAX_VALUE - 3));
 
 		// This should cause all the counts to be shifted and the new state
 		// added at the end.
 		historyStore.addState(file.getFullPath(), file.getLocation().toFile(), lastModifiedTime, false);
 		counters.clear();
 
 		try {
 			IndexCursor cursor = store.getCursor();
 			cursor.findFirstEntry();
 			// Check for a prefix match.
 			while (cursor.isSet()) {
 				byte[] storedKey = cursor.getKey();
 				// set the boolean for the appropriate counter
 				HistoryStoreEntry storedEntry = HistoryStoreEntry.create(store, cursor);
 				int counter = storedEntry.getCount();
 				counters.set(counter);
 				cursor.next();
 			}
 			cursor.close();
 		} catch (Exception e) {
 			fail("1.11");
 		}
 		assertEquals("1.12", Byte.MAX_VALUE - 1, counters.length());
 		assertTrue("1.13", counters.get(Byte.MAX_VALUE - 2));
 		assertFalse("1.14", counters.get(Byte.MAX_VALUE - 1));
 		assertFalse("1.15", counters.get(Byte.MAX_VALUE));
 
 		// cleanup
 		try {
 			project.delete(true, getMonitor());
 		} catch (CoreException e) {
 			fail("3.0", e);
 		}
 	}
 }
