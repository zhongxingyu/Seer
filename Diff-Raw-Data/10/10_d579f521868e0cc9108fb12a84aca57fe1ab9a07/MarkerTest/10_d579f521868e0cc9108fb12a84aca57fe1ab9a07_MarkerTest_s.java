 /**********************************************************************
  * Copyright (c) 2000, 2002 IBM Corporation and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors: 
  * IBM - Initial API and implementation
  **********************************************************************/
 package org.eclipse.core.tests.resources;
 
 import java.io.*;
 import java.io.File;
 import java.util.*;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.eclipse.core.internal.resources.*;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.tests.harness.EclipseWorkspaceTest;
 
 public class MarkerTest extends EclipseWorkspaceTest {
 
 	public static final String TRANSIENT_MARKER = "org.eclipse.core.tests.resources.transientmarker";
 
 	/** The collection of resources used for testing. */
 	IResource[] resources;
 
 /**
  * Need a zero argument constructor to satisfy the test harness.
  * This constructor should not do any real work nor should it be
  * called by user code.
  */
 public MarkerTest() {
 }
 /**
  * Creates a new markers test.
  */
 public MarkerTest(String name) {
 	super(name);
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void _testMarkerChangesInDelta3() {
 	log("TestMarkerChangesInDelta3");
 
 	// Create and register a listener.
 	final MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		IResource resource = getWorkspace().getRoot().findMember("1");
 		IResource destinationResource = null;
 		IResource child;
 		IResource destinationChild;
 		IPath destination;
 		IMarker[] markers = new IMarker[4];
 
 		// MOVE the resource
 		try {
 			destination = resource.getFullPath().removeLastSegments(1).append(resource.getFullPath().lastSegment() + "new");
 			markers[0] = resource.createMarker(IMarker.BOOKMARK);
 			child = ((IProject) resource).members()[0];
 			markers[1] = child.createMarker(IMarker.TASK);
 			listener.reset();
 			resource.move(destination, false, getMonitor());
 			destinationResource = getWorkspace().getRoot().findMember(destination);
 			markers[2] = destinationResource.getMarker(0);
 			destinationChild = ((IProject) destinationResource).findMember(child.getName());
 			markers[3] = destinationChild.getMarker(0);
 			assertEquals("1.1." + resource.getFullPath(), 4, listener.numAffectedResources());
 			assertTrue("1.2." + resource.getFullPath(), listener.checkChanges(resource, null, new IMarker[] { markers[0] }, null));
 			assertTrue("1.3." + resource.getFullPath(), listener.checkChanges(child, null, new IMarker[] { markers[1] }, null));
 			assertTrue("1.4." + destinationResource.getFullPath(), listener.checkChanges(destinationResource, new IMarker[] { markers[2] }, null, null));
 			assertTrue("1.5." + destinationResource.getFullPath(), listener.checkChanges(destinationChild, new IMarker[] { markers[3] }, null, null));
 		} catch (CoreException e) {
 			fail("1.99", e);
 		}
 
 		// COPY the resource and look at the deltas
 		try {
 			resource = getWorkspace().getRoot().findMember("2");
 			destination = resource.getFullPath().removeLastSegments(1).append(resource.getFullPath().lastSegment() + "copy");
 			markers[0] = resource.createMarker(IMarker.BOOKMARK);
 			listener.reset();
 			resource.copy(destination, false, getMonitor());
 			destinationResource = getWorkspace().getRoot().findMember(destination);
 			markers[1] = destinationResource.getMarker(0);
 			assertEquals("2.1." + resource.getFullPath(), 1, listener.numAffectedResources());
 			assertTrue("2.3." + destinationResource.getFullPath(), listener.checkChanges(destinationResource, new IMarker[] { markers[1] }, null, null));
 		} catch (CoreException e) {
 			fail("2.99", e);
 		}
 
 		// delete all markers for a clean run next time
 		try {
 			getWorkspace().getRoot().deleteMarkers(null, true, IResource.DEPTH_INFINITE);
 		} catch (CoreException e) {
 			fail("99.99", e);
 		}
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 public void _testPerformanceManyResources() {
 	log("testPerformanceManyResources");
 	long start;
 	long stop;
 
 	// cleanup old resources and create our own
 	IResource[] resources = null;
 	try {
 		getWorkspace().getRoot().delete(false, getMonitor());
 		resources = createLargeHierarchy();
 	} catch (CoreException e) {
 		fail("0.0", e);
 	}
 
 	// header info
 	final int markersPerResource = 20;
 	final int numMarkers = resources.length * markersPerResource;
 	display("\nNumber of resources: " + resources.length);
 	display("Markers per resource: " + markersPerResource);
 	display("Total Number of Markers: " + numMarkers);
 
 	// Create an array with a bunch of markers.
 	IWorkspaceRunnable body = new IWorkspaceRunnable() {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			IResourceVisitor visitor = new IResourceVisitor() {
 				public boolean visit(IResource resource) throws CoreException {
 					for (int i = 0; i < markersPerResource; i++)
 						resource.createMarker(IMarker.PROBLEM);
 					return true;
 				}
 			};
 			getWorkspace().getRoot().accept(visitor);
 		}
 	};
 	try {
 		start = System.currentTimeMillis();
 		getWorkspace().run(body, getMonitor());
 		stop = System.currentTimeMillis();
 		display("Task: creating markers");
 		display(start, stop);
 	} catch (CoreException e) {
 		fail("0.0", e);
 	}
 
 	// gather the markers for use. don't time this one.
 	final IMarker[] markers = new IMarker[numMarkers];
 	try {
 		IMarker[] temp = getWorkspace().getRoot().findMarkers(null, true, IResource.DEPTH_INFINITE);
 		assertEquals("0.1", numMarkers, temp.length);
 		for (int i = 0; i < temp.length; i++)
 			markers[i] = temp[i];
 	} catch (CoreException e) {
 		fail("0.2", e);
 	}
 
 	// create attributes on each marker
 	body = new IWorkspaceRunnable() {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			for (int i = 0; i < markers.length; i++)
 				markers[i].setAttribute(IMarker.MESSAGE, getRandomString());
 		}
 	};
 	try {
 		start = System.currentTimeMillis();
 		getWorkspace().run(body, getMonitor());
 		stop = System.currentTimeMillis();
 		display("Task: setting an attribute on each marker");
 		display(start, stop);
 	} catch (CoreException e) {
 		fail("1.0", e);
 	}
 
 	// get the attribute from each marker
 	body = new IWorkspaceRunnable() {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			for (int i = 0; i < markers.length; i++)
 				markers[i].getAttribute(IMarker.MESSAGE);
 		}
 	};
 	try {
 		start = System.currentTimeMillis();
 		getWorkspace().run(body, getMonitor());
 		stop = System.currentTimeMillis();
 		display("Task: getting an attribute on each marker");
 		display(start, stop);
 	} catch (CoreException e) {
 		fail("2.0", e);
 	}
 }
 public void _testPerformanceOneResource() {
 	log("testPerformanceOneResource");
 	long start;
 	long stop;
 	final int numMarkers = 4000;
 
 	// header info
 	display("Number of resources: 1");
 	display("Number of Markers: " + numMarkers);
 
 	// Create an array with a bunch of markers.
 	final IMarker markers[] = new IMarker[numMarkers];
 	IWorkspaceRunnable body = new IWorkspaceRunnable() {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			IResource resource = getWorkspace().getRoot();
 			for (int i = 0; i < markers.length; i++) {
 				markers[i] = resource.createMarker(IMarker.PROBLEM);
 			}
 		}
 	};
 	try {
 		start = System.currentTimeMillis();
 		getWorkspace().run(body, getMonitor());
 		stop = System.currentTimeMillis();
 		display("Task: creating markers");
 		display(start, stop);
 	} catch (CoreException e) {
 		fail("0.0", e);
 	}
 
 	// create attributes on each marker
 	body = new IWorkspaceRunnable() {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			for (int i = 0; i < markers.length; i++)
 				markers[i].setAttribute(IMarker.MESSAGE, getRandomString());
 		}
 	};
 	try {
 		start = System.currentTimeMillis();
 		getWorkspace().run(body, getMonitor());
 		stop = System.currentTimeMillis();
 		display("Task: setting an attribute on each marker");
 		display(start, stop);
 	} catch (CoreException e) {
 		fail("1.0", e);
 	}
 
 	java.util.Comparator c = new java.util.Comparator() {
 		public int compare(Object o1, Object o2) {
 			try {
 				String name1 = (String) ((IMarker) o1).getAttribute(IMarker.MESSAGE);
 				String name2 = (String) ((IMarker) o2).getAttribute(IMarker.MESSAGE);
 				if (name1 == null)
 					name1 = "";
 				if (name2 == null)
 					name2 = "";
 				int result = name1.compareToIgnoreCase(name2);
 				return result;
 			} catch (CoreException e) {
 				fail("2.0", e);
 			}
 			// avoid compiler error
 			return -1;
 		}
 	};
 	start = System.currentTimeMillis();
 	Arrays.sort(markers, c);
 	stop = System.currentTimeMillis();
 	display("Task: sort arrays based on MESSAGE attribute");
 	display(start, stop);
 }
 /**
  * Test a specific PR case.
  */
 public void _testPR() {
 	log("_testPR");
 
 	try {
 		IWorkspaceRunnable body = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				IResource resource = getWorkspace().getRoot().getProjects()[0].members()[0];
 				resource.createMarker(IMarker.BOOKMARK);
 				IPath destination = resource.getFullPath().removeLastSegments(1).append(resource.getName() + "new");
 				resource.move(destination, true, getMonitor());
 				IResource dest = getWorkspace().getRoot().findMember(destination);
 				dest.move(resource.getFullPath(), true, getMonitor());
 			}
 		};
 		getWorkspace().run(body, getMonitor());
 	} catch (CoreException e) {
 		fail("99.9", e);
 	}
 
 }
 protected void addChildren(ArrayList result, IPath root, int breadth, int depth) {
 	for (int i = 1; i < breadth + 1; i++) {
 		IPath child = root.append(i + "");
 		if (depth == 0) {
 			result.add(child.toString());
 			return;
 		}
 		child = child.addTrailingSeparator();
 		result.add(child.toString());
 		addChildren(result, child, breadth, depth - 1);
 	}
 }
 protected void assertDoesNotExist(String message, IMarker[] markers) {
 	for (int i = 0; i < markers.length; i++)
 		assertDoesNotExist(message, markers[i]);
 }
 protected void assertDoesNotExist(String message, IMarker marker) {
 	assertTrue(message, !marker.exists());
 }
 
 /**
  * Asserts that the given collection of expected markers contains
  * the same markers as the given collection of actual markers.  The
  * markers do not have to be in the same order.
  */
 protected void assertEquals(String message, IMarker[] expectedMarkers, IMarker[] actualMarkers) {
 	int n = expectedMarkers.length;
 	if (n != actualMarkers.length)
 		fail(message);
 	boolean[] seen = new boolean[n];
 	for (int i = 0; i < n; ++i) {
 		boolean found = false;
 		for (int j = 0; j < n; ++j) {
 			if (!seen[j] && equals(expectedMarkers[i], actualMarkers[j])) {
 				found = true;
 				seen[j] = true;
 				break;
 			}
 		}
 		if (!found) {
 			fail(message);
 		}
 	}
 }
 protected void assertEquals(String message, Map map, Object[] keys, Object[] values) {
 	assertEquals(message, keys.length, values.length);
 	assertEquals(message, keys.length, map.size());
 	for (Iterator i = map.keySet().iterator(); i.hasNext();) {
 		Object key = i.next();
 		Object value = map.get(key);
 		boolean found = false;
 		for (int j = 0; !found && j < keys.length; j++) {
 			if (keys[j].equals(key)) {
 				found = true;
 				if (!values[j].equals(value))
 					fail(message);
 			}
 		}
 		if (!found)
 			assertTrue(message, false);
 	}
 }
 protected void assertExists(String message, IMarker[] markers) {
 	for (int i = 0; i < markers.length; i++)
 		assertExists(message, markers[i]);
 }
 protected void assertExists(String message, IMarker marker) {
 	assertTrue(message, marker.exists());
 }
 public IResource[] createLargeHierarchy() throws CoreException {
 	ArrayList result = new ArrayList();
 	result.add("/");
 	new MarkerTest().addChildren(result, Path.ROOT, 3, 4);
 	String[] names = (String[]) result.toArray(new String[result.size()]);
 	IResource[] resources = buildResources(getWorkspace().getRoot(), names);
 	ensureExistsInWorkspace(resources, true);
 	return resources;
 }
 protected IMarker[] createMarkers(IResource[] hosts, String type) throws CoreException {
 	IMarker[] result = new IMarker[hosts.length];
 	for (int i = 0; i < hosts.length; i++) {
 		result[i] = hosts[i].createMarker(type);
 	}
 	return result;
 }
 /**
  * Return a string array which defines the hierarchy of a tree.
  * Folder resources must have a trailing slash.
  */
 public String[] defineHierarchy() {
 	return new String[] { "/", "1/", "1/1", "1/2/", "1/2/1", "1/2/2/", "2/", "2/1", "2/2/", "2/2/1", "2/2/2/" };
 }
 public void display(long start, long stop) {
 	display("Start: " + start);
 	display("Stop: " + stop);
 	display("Duration: " + (stop - start));
 }
 public void display(String message) {
 	System.out.println(message);
 }
 /**
  * Returns whether the given markers are equal.
  */
 public static boolean equals(IMarker a, IMarker b) {
 	try {
 		if (a.getType() != b.getType())
 			return false;
 		if (a.getId() != b.getId())
 			return false;
 		return true;
 	} catch (CoreException e) {
 		return false;
 	}
 }
 /**
  * Returns whether two object are both null, or both non-null and equal.
  */
 protected static boolean equalsOrNull(Object a, Object b) {
 	return a == b || (a != null && b != null && a.equals(b));
 }
 public void setUp() throws Exception {
 	super.setUp();
 	try {
 		resources = createHierarchy();
 	} catch (CoreException e) {
 		fail("#setUp", e);
 	}
 }
 /**
  * Configures the markers test suite.
  */
 public static Test suite() {
 	return new TestSuite(MarkerTest.class);
 
 //	TestSuite suite = new TestSuite();
 //	suite.addTest(new MarkerTest("testMarkerDeltasMove"));
 //	return suite;
 }
 public void tearDown() throws Exception {
 	super.tearDown();
 	try {
 		getWorkspace().getRoot().delete(true, null);
 	} catch (CoreException e) {
 		fail("#tearDown", e);
 	}
 }
 /**
  * Tests whether markers correctly copy with resources.
  */
 public void testCopyResource() {
 	log("TestCopyResource");
 }
 public void testCreateMarker() {
 	log("TestCreateMarker");
 
 	// Create and register a listener.
 	MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	// create markers on our hierarchy of resources
 	for (int i = 0; i < resources.length; i++) {
 		listener.reset();
 		IResource resource = resources[i];
 		IMarker[] markers = new IMarker[3];
 		try {
 			markers[0] = resource.createMarker(IMarker.PROBLEM);
 			markers[1] = resource.createMarker(IMarker.BOOKMARK);
 			markers[2] = resource.createMarker(IMarker.TASK);
 			assertExists("1.0." + resource.getFullPath(), markers);
 		} catch (CoreException e) {
 			fail("1.1." + resource.getFullPath(), e);
 		}
 		assertEquals("1.2." + resource.getFullPath(), 1, listener.numAffectedResources());
 		assertTrue("1.3." + resource.getFullPath(), listener.checkChanges(resource, markers, null, null));
 
 		// expect an AssertionFailedException from a null type
 		try {
 			resource.createMarker(null);
 			fail("2.0." + resource.getFullPath());
 		} catch (CoreException e) {
 			fail("2.1." + resource.getFullPath(), e);
 		} catch (RuntimeException e) {
 		}
 	}
 
 	// try creating a marker on a resource which does't exist
 	IResource testResource = getWorkspace().getRoot().getFile(new Path("non/existant/resource"));
 	assertTrue("3.0", !testResource.exists());
 	try {
 		testResource.createMarker(IMarker.PROBLEM);
 		fail("3.1");
 	} catch (CoreException e) {
 	}
 
 	// cleanup
 	getWorkspace().removeResourceChangeListener(listener);
 }
 public void testCreationTime() {
 
 	for (int i=0; i<resources.length; i++) {
 		IMarker marker = null;
 		try {
 			marker = resources[i].createMarker(IMarker.PROBLEM);
 		} catch (CoreException e) {
 			fail("0.0", e);
 		}
 		
 		// make sure the marker has a non-zero creation time
 		try {
 			assertTrue("1.0." + i, 0 != marker.getCreationTime());
 		} catch (CoreException e) {
 			fail("1.1", e);
 		}
 	}
 }
 public void testDeleteMarker() {
 	log("TestDeleteMarker");
 	IMarker marker = null;
 
 	// Create and register a listener.
 	MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	// for each resource in the hierarchy do...
 	for (int i = 0; i < resources.length; i++) {
 		IResource resource = resources[i];
 
 		// create the marker, assert that it exists, and then remove it
 		try {
 			listener.reset();
 			marker = resource.createMarker(IMarker.PROBLEM);
 			assertEquals("2.0." + resource.getFullPath(), 1, listener.numAffectedResources());
 			assertTrue("2.1." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { marker }, null, null));
 		} catch (CoreException e) {
 			fail("2.2." + resource.getFullPath(), e);
 		}
 		assertExists("2.3." + resource.getFullPath(), marker);
 		try {
 			listener.reset();
 			marker.delete();
 			assertDoesNotExist("2.4." + resource.getFullPath(), marker);
 			assertEquals("2.5." + resource.getFullPath(), 1, listener.numAffectedResources());
 			assertTrue("2.6." + resource.getFullPath(), listener.checkChanges(resource, null, new IMarker[] { marker }, null));
 		} catch (CoreException e) {
 			fail("2.7." + resource.getFullPath(), e);
 		}
 
 		// Check that a non-existant marker can be removed 
 		try {
 			marker.delete();
 		} catch (CoreException e) {
 			fail("3.0." + resource.getFullPath(), e);
 		}
 
 		// create multiple markers, assert they exist, and then remove them.
 		IMarker[] markers = new IMarker[3];
 		try {
 			markers[0] = resource.createMarker(IMarker.BOOKMARK);
 			assertExists("4.0." + resource.getFullPath(), markers[0]);
 			markers[1] = resource.createMarker(IMarker.TASK);
 			assertExists("4.1." + resource.getFullPath(), markers[1]);
 			markers[2] = resource.createMarker(IMarker.PROBLEM);
 			assertExists("4.2." + resource.getFullPath(), markers[2]);
 		} catch (CoreException e) {
 			fail("4.3." + resource.getFullPath(), e);
 		}
 		try {
 			listener.reset();
 			getWorkspace().deleteMarkers(markers);
 			assertEquals("4.4." + resource.getFullPath(), 1, listener.numAffectedResources());
 			assertTrue("4.5." + resource.getFullPath(), listener.checkChanges(resource, null, markers, null));
 		} catch (CoreException e) {
 			fail("4.6." + resource.getFullPath(), e);
 		}
 		assertDoesNotExist("4.7." + resource.getFullPath(), markers);
 	}
 
 	// cleanup
 	getWorkspace().removeResourceChangeListener(listener);
 }
 public void testDeleteMarkers() {
 	log("TestDeleteMarkers");
 	IMarker[] markers = null;
 	try {
 		markers = createMarkers(resources, IMarker.PROBLEM);
 	} catch (CoreException e) {
 		fail("0.0", e);
 	}
 
 	// Check that a collection of markers can be removed.
 	try {
 		getWorkspace().deleteMarkers(markers);
 	} catch (CoreException e) {
 		fail("1.0", e);
 	}
 	for (int i = 0; i < markers.length; i++)
 		assertTrue("1.1", !markers[i].exists());
 
 	// Check that an empty collection of markers can be removed.
 	try {
 		getWorkspace().deleteMarkers(new IMarker[0]);
 	} catch (CoreException e) {
 		fail("1.2", e);
 	}
 }
 public void testFindMarkers() {
 	log("TestFindMarkers");
 
 	// test finding some markers which actually exist
 	IMarker[] markers = null;
 	try {
 		markers = createMarkers(resources, IMarker.PROBLEM);
 	} catch (CoreException e) {
 		fail("0.0", e);
 	}
 	try {
 		IMarker[] found = getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
 		assertEquals("1.0", markers, found);
 	} catch (CoreException e) {
 		fail("1.1", e);
 	}
 	try {
 		IMarker[] found = getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		assertEquals("1.2", markers, found);
 	} catch (CoreException e) {
 		fail("1.3", e);
 	}
 
 	// test finding some markers which don't exist
 	try {
 		IMarker[] found = getWorkspace().getRoot().findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_INFINITE);
 		assertTrue("2.0", found.length == 0);
 	} catch (CoreException e) {
 		fail("2.1", e);
 	}
 
 	// add more markers and do a search on all marker types
 	Vector allMarkers = new Vector(markers.length * 3);
 	for (int i = 0; i < markers.length; i++)
 		allMarkers.add(markers[i]);
 	try {
 		markers = createMarkers(resources, IMarker.BOOKMARK);
 	} catch (CoreException e) {
 		fail("3.0", e);
 	}
 	for (int i = 0; i < markers.length; i++)
 		allMarkers.add(markers[i]);
 	try {
 		markers = createMarkers(resources, IMarker.TASK);
 	} catch (CoreException e) {
 		fail("3.1", e);
 	}
 	for (int i = 0; i < markers.length; i++)
 		allMarkers.add(markers[i]);
 	try {
 		IMarker[] found = getWorkspace().getRoot().findMarkers(null, false, IResource.DEPTH_INFINITE);
 		assertEquals("3.2", (IMarker[]) allMarkers.toArray(new IMarker[allMarkers.size()]), found);
 	} catch (CoreException e) {
 		fail("3.3", e);
 	}
 	try {
 		IMarker[] found = getWorkspace().getRoot().findMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
 		assertEquals("3.4", (IMarker[]) allMarkers.toArray(new IMarker[allMarkers.size()]), found);
 	} catch (CoreException e) {
 		fail("3.5", e);
 	}
 }
 public void test_10989() {
 	log("test_10989");
 
 	try {
 		IProject project = getWorkspace().getRoot().getProject("MyProject");
 		project.create(null);
 		project.open(null);
 		IFile file = project.getFile("foo.txt");
 		file.create(getRandomContents(), true, null);
 		file.createMarker(IMarker.PROBLEM);
 		IMarker[] found = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
 		assertEquals("1.0", 1, found.length);
 		found = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		assertEquals("1.1", 1, found.length);
 		project.delete(true, true, null);
 	} catch (CoreException e) {
 		fail("1.99", e);
 	}
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void testMarkerChangesInDelta() {
 	log("TestMarkerChangesInDelta");
 
 	// Create and register a listener.
 	MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		IResource resource;
 		IMarker[] markers;
 		IMarker marker;
 		for (int i = 0; i < resources.length; i++) {
 			resource = resources[i];
 			markers = new IMarker[3];
 
 			// ADD a marker
 			listener.reset();
 			try {
 				markers[0] = resource.createMarker(IMarker.PROBLEM);
 				assertExists("1.0." + resource.getFullPath(), markers[0]);
 				assertEquals("1.1." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("1.2." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { markers[0] }, null, null));
 			} catch (CoreException e) {
 				fail("1.99." + resource.getFullPath(), e);
 			}
 
 			// ADD more markers to the same resource
 			listener.reset();
 			try {
 				markers[1] = resource.createMarker(IMarker.BOOKMARK);
 				markers[2] = resource.createMarker(IMarker.TASK);
 				assertExists("2.0." + resource.getFullPath(), new IMarker[] { markers[1], markers[2] });
 				assertEquals("2.1." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("2.2." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { markers[1], markers[2] }, null, null));
 			} catch (CoreException e) {
 				fail("2.99." + resource.getFullPath(), e);
 			}
 
 			// CHANGE a marker
 			listener.reset();
 			try {
 				markers[0].setAttribute(IMarker.MESSAGE, "My text.");
 				assertEquals("3.0." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("3.1." + resource.getFullPath(), listener.checkChanges(resource, null, null, new IMarker[] { markers[0] }));
 			} catch (CoreException e) {
 				fail("3.99." + resource.getFullPath(), e);
 			}
 
 			// CHANGE more markers
 			listener.reset();
 			try {
 				markers[1].setAttribute(IMarker.SEVERITY, "Low");
 				markers[2].setAttribute(IMarker.PRIORITY, "Normal");
 				assertEquals("4.0." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("4.1." + resource.getFullPath(), listener.checkChanges(resource, null, null, new IMarker[] { markers[1], markers[2] }));
 			} catch (CoreException e) {
 				fail("4.99." + resource.getFullPath(), e);
 			}
 
 			// DELETE a marker
 			listener.reset();
 			try {
 				markers[0].delete();
 				assertDoesNotExist("5.0." + resource.getFullPath(), markers[0]);
 				assertEquals("5.1." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("5.2." + resource.getFullPath(), listener.checkChanges(resource, null, new IMarker[] { markers[0] }, null));
 			} catch (CoreException e) {
 				fail("5.99." + resource.getFullPath(), e);
 			}
 
 			// DELETE more markers
 			listener.reset();
 			try {
 				resource.deleteMarkers(null, false, IResource.DEPTH_ZERO);
 				assertDoesNotExist("6.0." + resource.getFullPath(), new IMarker[] { markers[1], markers[2] });
 				assertEquals("6.1." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("6.2." + resource.getFullPath(), listener.checkChanges(resource, null, new IMarker[] { markers[1], markers[2] }, null));
 			} catch (CoreException e) {
 				fail("6.99." + resource.getFullPath(), e);
 			}
 
 			// ADD, REMOVE and CHANGE markers
 			try {
 				markers[0] = resource.createMarker(IMarker.PROBLEM);
 				markers[1] = resource.createMarker(IMarker.BOOKMARK);
 				listener.reset();
 				markers[0].delete();
 				assertDoesNotExist("7.0." + resource.getFullPath(), markers[0]);
 				markers[1].setAttribute(IMarker.MESSAGE, getRandomString());
 				markers[2] = resource.createMarker(IMarker.TASK);
 				assertEquals("7.1." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("7.2." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { markers[2] }, new IMarker[] { markers[0] }, new IMarker[] { markers[1] }));
 			} catch (CoreException e) {
 				fail("7.99." + resource.getFullPath(), e);
 			}
 		}
 
 		// DELETE the resource and see what the marker delta is
 		try {
 			resource = getWorkspace().getRoot();
 			resource.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
 			marker = resource.createMarker(IMarker.BOOKMARK);
 			listener.reset();
 			resource.delete(true, getMonitor());
 			assertDoesNotExist("8.0", marker);
 			assertTrue("8.1", listener.checkChanges(resource, null, new IMarker[] { marker }, null));
 		} catch (CoreException e) {
 			fail("8.99", e);
 		}
 
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  * Particularly, checks that the MarkerDelta attributes reflect the 
  * state of the marker before the change occurred.
  */
 public void testMarkerDeltaAttributes() {
 	log("testMarkerDeltaAttributes");
 
 	// create markers on various resources
 	final IMarker[] markers = new IMarker[3];
 	IWorkspaceRunnable body = new IWorkspaceRunnable() {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			markers[0] = resources[0].createMarker(IMarker.BOOKMARK);
 			markers[1] = resources[1].createMarker(IMarker.BOOKMARK);
 			markers[1].setAttribute(IMarker.CHAR_START, 5);
 			markers[2] = resources[2].createMarker(IMarker.PROBLEM);
 			markers[2].setAttribute(IMarker.DONE, true);
 			markers[2].setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
 			markers[2].setAttribute(IMarker.MESSAGE, "Hello");
 		}
 	};
 	try {
 		getWorkspace().run(body, getMonitor());
 	} catch (CoreException e) {
 		fail("0.99", e);
 	}
 	
 	//create the attribute change listener
 	MarkerAttributeChangeListener listener = new MarkerAttributeChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 	try {
 		//add a new attribute
 		listener.expectChanges(markers[0]);
 		markers[0].setAttribute(IMarker.MESSAGE, "Message");
 		listener.verifyChanges();
 		
 		//change an attribute
 		listener.expectChanges(markers[0]);
 		markers[0].setAttribute(IMarker.MESSAGE, "NewMessage");
 		listener.verifyChanges();
 		
 		//remove an attribute
 		listener.expectChanges(markers[0]);
 		markers[0].setAttribute(IMarker.MESSAGE, null);
 		listener.verifyChanges();
 		
 		//add attribute to marker that already has attributes
 		listener.expectChanges(markers[2]);
 		markers[2].setAttribute(IMarker.CHAR_END, 5);
 		listener.verifyChanges();
 		
 		//add+change
 		listener.expectChanges(markers[1]);
 		getWorkspace().run(new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				markers[1].setAttribute(IMarker.CHAR_START, 5);
 				markers[1].setAttribute(IMarker.CHAR_END, 10);
 			}
 		}, getMonitor());
 		listener.verifyChanges();
 
 		//change+remove same marker
 		listener.expectChanges(markers[1]);
 		getWorkspace().run(new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				markers[1].setAttribute(IMarker.CHAR_START, 5);
 				markers[1].setAttribute(IMarker.CHAR_START, null);
 			}
 		}, getMonitor());
 		listener.verifyChanges();
 		
 		//change multiple markers
 		listener.expectChanges(markers);
 		getWorkspace().run(new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				markers[0].setAttribute(IMarker.CHAR_START, 5);
 				markers[1].setAttribute(IMarker.CHAR_START, 10);
 				markers[2].setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_LOW);
 			}
 		}, getMonitor());
 		listener.verifyChanges();
 	} catch (CoreException e) {
 		fail("1.99", e);		
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void testMarkerDeltasCopyResource() {
 	log("testMarkerDeltasCopyResource");
 
 	// Create and register a listener.
 	final MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		// create markers on all the non-project resources 
 		final Hashtable table = new Hashtable(1);
 		final int[] count = new int[1];
 		count[0] = 0;
 		IWorkspaceRunnable body = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				IResourceVisitor visitor = new IResourceVisitor() {
 					public boolean visit(IResource resource) throws CoreException {
 						if (resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT)
 							return true;
 						IMarker marker = resource.createMarker(IMarker.BOOKMARK);
 						table.put(resource, marker);
 						count[0]++;
 						return true;
 					}
 				};
 				getWorkspace().getRoot().accept(visitor);
 			}
 		};
 		try {
 			getWorkspace().run(body, getMonitor());
 		} catch (CoreException e) {
 			fail("0.99", e);
 		}
 		listener.reset();
 
 		// copy all non-project resources
 		try {
 			IProject[] projects = getWorkspace().getRoot().getProjects();
 			for (int i = 0; i < projects.length; i++) {
 				IResource[] children = projects[i].members();
 				for (int j = 0; j < children.length; j++) {
 					IPath destination = new Path(children[j].getName() + "copy");
 					children[j].copy(destination, true, getMonitor());
 				}
 			}
 		} catch (CoreException e) {
 			fail("1.99", e);
 		}
 
 		// verify marker deltas
 //		IResourceVisitor visitor = new IResourceVisitor() {
 //			public boolean visit(IResource resource) throws CoreException {
 //				if (resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT)
 //					return true;
 //				if (!resource.getName().endsWith("copy"))
 //					return false;
 //				String name = resource.getFullPath().segment(0);
 //				IPath path = new Path(name.substring(0, name.length() - 4)).makeAbsolute();
 //				path = path.append(resource.getFullPath().removeFirstSegments(1));
 //				IResource oldResource = ((Workspace) getWorkspace()).newResource(path, resource.getType());
 //				IMarker marker = (IMarker) table.get(oldResource);
 //				assertNotNull("2.1." + oldResource.getFullPath(), marker);
 //				IMarker[] markers = resource.findMarkers(null, true, IResource.DEPTH_ZERO);
 //				assertEquals("2.2." + resource.getFullPath(), 1, markers.length);
 //				assertEquals("2.3." + resource.getFullPath(), marker.getId(), markers[0].getId());
 //				assertTrue("2.4." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { markers[0] }, null, null));
 //				return true;
 //			}
 //		};
 		// marker deltas should not appear after a resource copy
 		//assertEquals("2.5", count[0], listener.numAffectedResources());
 		assertEquals("2.5", 0, listener.numAffectedResources());
 		//try {
 			//getWorkspace().getRoot().accept(visitor);
 		//} catch (CoreException e) {
 			//fail("2.99", e);
 		//}
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void testMarkerDeltasMerge() {
 	log("testMarkerDeltasMerge");
 
 	// Create and register a listener.
 	final MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		for (int i = 0; i < resources.length; i++) {
 			final IResource resource = resources[i];
 
 			// ADD + REMOVE = nothing
 			try {
 				IWorkspaceRunnable body = new IWorkspaceRunnable() {
 					public void run(IProgressMonitor monitor) throws CoreException {
 						listener.reset();
 						IMarker marker = resource.createMarker(IMarker.PROBLEM);
 						assertExists("1.0." + resource.getFullPath(), marker);
 						marker.delete();
 						assertDoesNotExist("1.1." + resource.getFullPath(), marker);
 					}
 				};
 				getWorkspace().run(body, getMonitor());
 				assertEquals("1.2." + resource.getFullPath(), 0, listener.numAffectedResources());
 				assertTrue("1.3." + resource.getFullPath(), listener.checkChanges(resource, null, null, null));
 			} catch (CoreException e) {
 				fail("1.99." + resource.getFullPath(), e);
 			}
 
 			// ADD + CHANGE = ADD
 			try {
 				// cannot re-assign variable value within the code below, so must
 				// put our marker value inside an array and set the element.
 				final IMarker[] markers = new IMarker[1];
 				IWorkspaceRunnable body = new IWorkspaceRunnable() {
 					public void run(IProgressMonitor monitor) throws CoreException {
 						listener.reset();
 						markers[0] = resource.createMarker(IMarker.PROBLEM);
 						assertExists("2.0." + resource.getFullPath(), markers[0]);
 						markers[0].setAttribute(IMarker.MESSAGE, "my message text");
 						assertEquals("2.1." + resource.getFullPath(), "my message text", markers[0].getAttribute(IMarker.MESSAGE));
 					}
 				};
 				getWorkspace().run(body, getMonitor());
 				assertEquals("2.2." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("2.3." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { markers[0] }, null, null));
 			} catch (CoreException e) {
 				fail("2.99." + resource.getFullPath(), e);
 			}
 
 			// REMOVE + ADD = CHANGE
 			//try {
 				//final IMarker[] markers = new IMarker[1];
 				//markers[0] = resource.createMarker(IMarker.PROBLEM);
 				//assertExists("3.0." + resource.getFullPath(), markers[0]);
 				//IWorkspaceRunnable body = new IWorkspaceRunnable() {
 					//public void run(IProgressMonitor monitor) throws CoreException {
 						//listener.reset();
 						//markers[0].delete();
 						//assertDoesNotExist("3.1." + resource.getFullPath(), markers[0]);
 						//markers[0] = resource.createMarker(IMarker.PROBLEM);
 						//assertExists("3.2." + resource.getFullPath(), markers[0]);
 					//}
 				//};
 				//getWorkspace().run(body, getMonitor());
 				//assertEquals("3.3." + resource.getFullPath(), 1, listener.numAffectedResources());
 				//assert("3.4." + resource.getFullPath(), listener.checkChanges(resource, null, null, new IMarker[] { markers[0] }));
 			//} catch (CoreException e) {
 				//fail("3.99." + resource.getFullPath(), e);
 			//}
 
 			// CHANGE + CHANGE = CHANGE
 			try {
 				final IMarker[] markers = new IMarker[1];
 				markers[0] = resource.createMarker(IMarker.PROBLEM);
 				assertExists("4.0." + resource.getFullPath(), markers[0]);
 				IWorkspaceRunnable body = new IWorkspaceRunnable() {
 					public void run(IProgressMonitor monitor) throws CoreException {
 						listener.reset();
 						markers[0].setAttribute(IMarker.MESSAGE, "my message text");
 						assertEquals("4.1." + resource.getFullPath(), "my message text", markers[0].getAttribute(IMarker.MESSAGE));
 						markers[0].setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
 						assertEquals("4.2." + resource.getFullPath(), IMarker.PRIORITY_HIGH, ((Integer) markers[0].getAttribute(IMarker.PRIORITY)).intValue());
 					}
 				};
 				getWorkspace().run(body, getMonitor());
 				assertEquals("4.3." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("4.4." + resource.getFullPath(), listener.checkChanges(resource, null, null, new IMarker[] { markers[0] }));
 			} catch (CoreException e) {
 				fail("4.99." + resource.getFullPath(), e);
 			}
 
 			// CHANGE + REMOVE = REMOVE
 			try {
 				final IMarker[] markers = new IMarker[1];
 				markers[0] = resource.createMarker(IMarker.PROBLEM);
 				assertExists("5.0." + resource.getFullPath(), markers[0]);
 				IWorkspaceRunnable body = new IWorkspaceRunnable() {
 					public void run(IProgressMonitor monitor) throws CoreException {
 						listener.reset();
 						markers[0].setAttribute(IMarker.MESSAGE, "my message text");
 						assertEquals("5.1." + resource.getFullPath(), "my message text", markers[0].getAttribute(IMarker.MESSAGE));
 						markers[0].delete();
 						assertDoesNotExist("5.2." + resource.getFullPath(), markers[0]);
 					}
 				};
 				getWorkspace().run(body, getMonitor());
 				assertEquals("5.3." + resource.getFullPath(), 1, listener.numAffectedResources());
 				assertTrue("5.4." + resource.getFullPath(), listener.checkChanges(resource, null, new IMarker[] { markers[0] }, null));
 			} catch (CoreException e) {
 				fail("5.99." + resource.getFullPath(), e);
 			}
 
 			// cleanup after each iteration
 			try {
 				resource.deleteMarkers(null, true, IResource.DEPTH_ZERO);
 			} catch (CoreException e) {
 				fail("99.99", e);
 			}
 		}
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void testMarkerDeltasMoveFolder() {
 	log("testMarkerDeltasMoveFolder");
 
 	IWorkspaceRoot root = getWorkspace().getRoot();
 	final IProject project = root.getProject("MyProject");
 	IFolder folder = project.getFolder("folder");
 	IFile file = project.getFile("file.txt");
 	IFile subFile = folder.getFile("subFile.txt");
 	ensureExistsInWorkspace(new IResource[] {project, folder, file, subFile}, true);
 	IFolder destFolder = project.getFolder("myOtherFolder");
 	IFile destSubFile = destFolder.getFile(subFile.getName());
 	IMarker folderMarker = null;
 	IMarker subFileMarker = null;
 	IMarker[] markers = null;
 
 	// Create and register a listener.
 	final MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		// create markers on the resources
 		try {
 			folderMarker = folder.createMarker(IMarker.BOOKMARK);
 		} catch(CoreException e) {
 			fail("1.0", e);
 		}
 		try {
 			subFileMarker = subFile.createMarker(IMarker.BOOKMARK);
 		} catch(CoreException e) {
 			fail("1.1", e);
 		}
 		listener.reset();
 
 		// move the files
 		try {
 		folder.move(destFolder.getFullPath(), IResource.FORCE, getMonitor());
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 
 		// verify marker deltas
 		assertTrue("3.1", listener.checkChanges(folder, null, new IMarker[] {folderMarker}, null));
 		try {
 			markers = destFolder.findMarkers(null, true, IResource.DEPTH_ZERO);
 		} catch(CoreException e) {
 			fail("3.2", e);
 		}
 		assertEquals("3.3", 1, markers.length);
 		assertEquals("3.4", folderMarker.getId(), markers[0].getId());
 		assertTrue("3.5", listener.checkChanges(destFolder, new IMarker[] {markers[0]}, null, null));
 
 		assertTrue("3.7", listener.checkChanges(subFile, null, new IMarker[] {subFileMarker}, null));
 		try {
 			markers = destSubFile.findMarkers(null, true, IResource.DEPTH_ZERO);
 		} catch(CoreException e) {
 			fail("3.8", e);
 		}
 		assertEquals("3.9", 1, markers.length);
 		assertEquals("3.10", subFileMarker.getId(), markers[0].getId());
 		assertTrue("3.11", listener.checkChanges(destSubFile, new IMarker[] {markers[0]}, null, null));
 
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void testMarkerDeltasMoveFile() {
 	log("testMarkerDeltasMoveFile");
 	IWorkspaceRoot root = getWorkspace().getRoot();
 	final IProject project = root.getProject("MyProject");
 	IFolder folder = project.getFolder("folder");
 	IFile file = project.getFile("file.txt");
 	IFile subFile = folder.getFile("subFile.txt");
 	ensureExistsInWorkspace(new IResource[] {project, folder, file, subFile} , true);
 	IFile destFile = folder.getFile(file.getName());
 	IFile destSubFile = project.getFile(subFile.getName());
 	IMarker fileMarker = null;
 	IMarker subFileMarker = null;
 	IMarker[] markers = null;
 
 	// Create and register a listener.
 	final MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		// create markers on the resources
 		try {
 			fileMarker = file.createMarker(IMarker.BOOKMARK);
 		} catch(CoreException e) {
 			fail("1.0", e);
 		}
 		try {
 			subFileMarker = subFile.createMarker(IMarker.BOOKMARK);
 		} catch(CoreException e) {
 			fail("1.1", e);
 		}
 		listener.reset();
 
 		// move the files
 		try {
 		file.move(destFile.getFullPath(), IResource.FORCE, getMonitor());
 		subFile.move(destSubFile.getFullPath(), IResource.FORCE, getMonitor());
 		} catch (CoreException e) {
 			fail("2.0", e);
 		}
 
 		// verify marker deltas
 		assertTrue("3.1", listener.checkChanges(file, null, new IMarker[] {fileMarker}, null));
 		try {
 			markers = destFile.findMarkers(null, true, IResource.DEPTH_ZERO);
 		} catch(CoreException e) {
 			fail("3.2", e);
 		}
 		assertEquals("3.3", 1, markers.length);
 		assertEquals("3.4", fileMarker.getId(), markers[0].getId());
 		assertTrue("3.5", listener.checkChanges(destFile, new IMarker[] {markers[0]}, null, null));
 
 		assertTrue("3.7", listener.checkChanges(subFile, null, new IMarker[] {subFileMarker}, null));
 		try {
 			markers = destSubFile.findMarkers(null, true, IResource.DEPTH_ZERO);
 		} catch(CoreException e) {
 			fail("3.8", e);
 		}
 		assertEquals("3.9", 1, markers.length);
 		assertEquals("3.10", subFileMarker.getId(), markers[0].getId());
 		assertTrue("3.11", listener.checkChanges(destSubFile, new IMarker[] {markers[0]}, null, null));
 
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 
 /**
  * Tests the appearance of marker changes in the resource delta.
  */
 public void testMarkerDeltasMoveProject() {
 	log("testMarkerDeltasMoveProject");
 
 	// Create and register a listener.
 	final MarkersChangeListener listener = new MarkersChangeListener();
 	getWorkspace().addResourceChangeListener(listener);
 
 	try {
 		// create markers on all the resources
 		final Hashtable table = new Hashtable(1);
 		final int[] count = new int[1];
 		count[0] = 0;
 		IWorkspaceRunnable body = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				IResourceVisitor visitor = new IResourceVisitor() {
 					public boolean visit(IResource resource) throws CoreException {
 						if (resource.getType() == IResource.ROOT)
 							return true;
 						IMarker marker = resource.createMarker(IMarker.BOOKMARK);
 						table.put(resource, marker);
 						count[0]++;
 						return true;
 					}
 				};
 				getWorkspace().getRoot().accept(visitor);
 			}
 		};
 		try {
 			getWorkspace().run(body, getMonitor());
 		} catch (CoreException e) {
 			fail("0.99", e);
 		}
 		listener.reset();
 
 		// move all resources
 		IProject[] projects = getWorkspace().getRoot().getProjects();
 		for (int i = 0; i < projects.length; i++) {
 			IPath destination = new Path(projects[i].getName() + "move");
 			try {
 				projects[i].move(destination, true, getMonitor());
 			} catch (CoreException e) {
 				fail("1.99", e);
 			}
 		}
 
 		// verify marker deltas
 		IResourceVisitor visitor = new IResourceVisitor() {
 			public boolean visit(IResource resource) throws CoreException {
 				if (resource.getType() == IResource.ROOT)
 					return true;
 				String name = resource.getFullPath().segment(0);
 				IPath path = new Path(name.substring(0, name.length() - 4)).makeAbsolute();
 				path = path.append(resource.getFullPath().removeFirstSegments(1));
 				IResource oldResource = ((Workspace) getWorkspace()).newResource(path, resource.getType());
 				IMarker marker = (IMarker) table.get(oldResource);
 				assertNotNull("2.1." + oldResource.getFullPath(), marker);
 				assertTrue("2.2." + oldResource.getFullPath(), listener.checkChanges(oldResource, null, new IMarker[] { marker }, null));
 				IMarker[] markers = resource.findMarkers(null, true, IResource.DEPTH_ZERO);
 				assertEquals("2.3." + resource.getFullPath(), 1, markers.length);
 				assertEquals("2.4." + resource.getFullPath(), marker.getId(), markers[0].getId());
 				assertTrue("2.5." + resource.getFullPath(), listener.checkChanges(resource, new IMarker[] { markers[0] }, null, null));
 				return true;
 			}
 		};
 		assertEquals("2.6", count[0] * 2, listener.numAffectedResources());
 		try {
 			getWorkspace().getRoot().accept(visitor);
 		} catch (CoreException e) {
 			fail("2.99", e);
 		}
 	} finally {
 		getWorkspace().removeResourceChangeListener(listener);
 	}
 }
 public void testMarkerSave() {
 	log("TestMarkerSave");
 
 	IMarker[] newMarkers = null;
 	IMarker[] expected = null;
 	try {
 		newMarkers = createMarkers(resources, IMarker.PROBLEM);
 		expected = new IMarker[newMarkers.length * 3];
 		for (int i = 0; i < newMarkers.length; i++)
 			expected[i] = newMarkers[i];
 		newMarkers = createMarkers(resources, IMarker.BOOKMARK);
 		for (int i = 0; i < newMarkers.length; i++)
 			expected[i + newMarkers.length] = newMarkers[i];
 		newMarkers = createMarkers(resources, IMarker.TASK);
 		for (int i = 0; i < newMarkers.length; i++)
 			expected[i + (newMarkers.length * 2)] = newMarkers[i];
 	} catch (CoreException e) {
 		fail("1.0", e);
 	}
 
 	final MarkerManager manager = ((Workspace) getWorkspace()).getMarkerManager();
 
 	// write all the markers to the output stream
 	File file = Platform.getLocation().append(".testmarkers").toFile();
 	OutputStream fileOutput = null;
 	DataOutputStream o1 = null;
 	try {
 		fileOutput = new FileOutputStream(file);
 		o1 = new DataOutputStream(fileOutput);
 	} catch (IOException e) {
 		if (fileOutput != null)
 			try {
 				fileOutput.close();
 			} catch (IOException e2) {
 			}
 		fail("2.0", e);
 	}
 	final DataOutputStream output = o1;
 	final List list = new ArrayList(5);
 	IResourceVisitor visitor = new IResourceVisitor() {
 		public boolean visit(IResource resource) throws CoreException {
 			try {
 				manager.save(resource, output, list);
 			} catch (IOException e) {
 				fail("2.1", e);
 			}
 			return true;
 		}
 	};
 	try {
 		getWorkspace().getRoot().accept(visitor);
 	} catch (CoreException e) {
 		fail("2.2", e);
 	} finally {
 		try {
 			output.close();
 		} catch (IOException e) {
 			fail("2.3", e);
 		}
 	}
 
 	// delete all markers resources
 	try {
 		getWorkspace().getRoot().deleteMarkers(null, true, IResource.DEPTH_INFINITE);
 	} catch (CoreException e) {
 		fail("3.0", e);
 	}
 	assertDoesNotExist("3.1", expected);
 
 	// read in the markers from the file
 	try {
 		InputStream fileInput = new FileInputStream(file);
 		final DataInputStream input = new DataInputStream(fileInput);
 		IWorkspaceRunnable body = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				MarkerReader reader = new MarkerReader((Workspace) getWorkspace());
 				try {
 					reader.read(input, true);
 				} catch (IOException e) {
 					fail("4.0", e);
 				}
 			}
 		};
 		try {
 			getWorkspace().run(body, getMonitor());
 		} finally {
 			try {
 				input.close();
 			} catch (IOException e) {
 				fail("4.1", e);
 			}
 		}
 	} catch (FileNotFoundException e) {
 		fail("4.2", e);
 	} catch (CoreException e) {
 		fail("4.3", e);
 	}
 
 	// assert that the markers retrieved are the same as the ones we used
 	// to have
 	try {
 		assertExists("5.0", expected);
 		IMarker[] actual = getWorkspace().getRoot().findMarkers(null, false, IResource.DEPTH_INFINITE);
 		assertEquals("5.1", expected, actual);
 	} catch (CoreException e) {
 		fail("5.2", e);
 	}
 
 	// cleanup
 	assertTrue("6.0", file.delete());
 }
 public void testMarkerSaveTransient() {
 	log("TestMarkerSaveTransient");
 
 	// create the markers on the resources. create both transient
 	// and persistent markers.
 	final ArrayList persistentMarkers = new ArrayList();
 	IResourceVisitor visitor = new IResourceVisitor() {
 		public boolean visit(IResource resource) throws CoreException {
 			IMarker marker = resource.createMarker(IMarker.PROBLEM);
 			persistentMarkers.add(marker);
 			marker = resource.createMarker(IMarker.BOOKMARK);
 			persistentMarkers.add(marker);
 			marker = resource.createMarker(TRANSIENT_MARKER);
 			return true;
 		}
 	};
 	try {
 		getWorkspace().getRoot().accept(visitor);
 	} catch (CoreException e) {
 		fail("1.0", e);
 	}
 
 	final MarkerManager manager = ((Workspace) getWorkspace()).getMarkerManager();
 	IMarker[] expected = (IMarker[]) persistentMarkers.toArray(new IMarker[persistentMarkers.size()]);
 
 	// write all the markers to the output stream
 	File file = Platform.getLocation().append(".testmarkers").toFile();
 	OutputStream fileOutput = null;
 	DataOutputStream o1 = null;
 	try {
 		fileOutput = new FileOutputStream(file);
 		o1 = new DataOutputStream(fileOutput);
 	} catch (IOException e) {
 		if (fileOutput != null)
 			try {
 				fileOutput.close();
 			} catch (IOException e2) {
 			}
 		fail("2.0", e);
 	}
 	final DataOutputStream output = o1;
 	final List list = new ArrayList(5);
 	visitor = new IResourceVisitor() {
 		public boolean visit(IResource resource) throws CoreException {
 			try {
 				manager.save(resource, output, list);
 			} catch (IOException e) {
 				fail("2.1", e);
 			}
 			return true;
 		}
 	};
 	try {
 		getWorkspace().getRoot().accept(visitor);
 	} catch (CoreException e) {
 		fail("2.2", e);
 	} finally {
 		try {
 			output.close();
 		} catch (IOException e) {
 			fail("2.3", e);
 		}
 	}
 
 	// delete all markers resources
 	try {
 		getWorkspace().getRoot().deleteMarkers(null, true, IResource.DEPTH_INFINITE);
 	} catch (CoreException e) {
 		fail("3.0", e);
 	}
 	assertDoesNotExist("3.1", expected);
 
 	// read in the markers from the file
 	try {
 		InputStream fileInput = new FileInputStream(file);
 		final DataInputStream input = new DataInputStream(fileInput);
 		IWorkspaceRunnable body = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				MarkerReader reader = new MarkerReader((Workspace) getWorkspace());
 				try {
 					reader.read(input, true);
 				} catch (IOException e) {
 					fail("4.0", e);
 				}
 			}
 		};
 		try {
 			getWorkspace().run(body, getMonitor());
 		} finally {
 			try {
 				input.close();
 			} catch (IOException e) {
 				fail("4.1", e);
 			}
 		}
 	} catch (FileNotFoundException e) {
 		fail("4.2", e);
 	} catch (CoreException e) {
 		fail("4.3", e);
 	}
 
 	// assert that the markers retrieved are the same as the ones we used
 	// to have
 	try {
 		assertExists("5.0", expected);
 		IMarker[] actual = getWorkspace().getRoot().findMarkers(null, false, IResource.DEPTH_INFINITE);
 		assertEquals("5.1", expected, actual);
 	} catch (CoreException e) {
 		fail("5.2", e);
 	}
 
 	// cleanup
 	assertTrue("6.0", file.delete());
 }
 /**
  * Tests whether markers correctly move with resources.
  */
 public void testMoveResource() {
 	log("TestMoveResource");
 }
 /*
  * Test for PR: "1FWT3V5: ITPCORE:WINNT - Task view shows entries for closed projects" 
  */
 public void testProjectCloseOpen() {
 	log("testProjectCloseOpen");
 	IProject project = null;
 	IMarker marker = null;
 
 	// create a marker on the project
 	project = getWorkspace().getRoot().getProjects()[0];
 	try {
 		marker = project.createMarker(IMarker.BOOKMARK);
 	} catch (CoreException e) {
 		fail("1.0", e);
 	}
 
 	// ensure that the marker was created
 	assertTrue("2.0", marker.exists());
 
 	// close the project
 	try {
 		project.close(getMonitor());
 	} catch (CoreException e) {
 		fail("3.0", e);
 	}
 
 	// now the marker should be inaccessible
 	assertTrue("", !marker.exists());
 
 	// open the project
 	try {
 		project.open(getMonitor());
 	} catch (CoreException e) {
 		fail("4.0", e);
 	}
 
 	// the marker re-appears
 	assertTrue("5.0", marker.exists());
 }
 public void testSetGetAttribute() {
 	log("testSetGetAttribute");
 
 	for (int i = 0; i < resources.length; i++) {
 		IResource resource = resources[i];
 		IMarker marker = null;
 
 		// getting a non-existant attribute should return null
 		try {
 			marker = resource.createMarker(IMarker.PROBLEM);
 		} catch (CoreException e) {
 			fail("1.0", e);
 		}
 		try {
 			assertNull("1.1." + resource.getFullPath(), marker.getAttribute(IMarker.MESSAGE));
 		} catch (CoreException e) {
 			fail("1.2." + resource.getFullPath(), e);
 		}
 
 		// set an attribute, get its value, then remove it
 		String testMessage = getRandomString();
 		try {
 			marker.setAttribute(IMarker.MESSAGE, testMessage);
 			Object value = marker.getAttribute(IMarker.MESSAGE);
 			assertEquals("2.0." + resource.getFullPath(), testMessage, (String) value);
 			marker.setAttribute(IMarker.MESSAGE, null);
 			value = marker.getAttribute(IMarker.MESSAGE);
 			assertNull("2.1." + resource.getFullPath(), value);
 		} catch (CoreException e) {
 			fail("2.2." + resource.getFullPath(), e);
 		}
 
 		// set more attributes, get their values, then remove one
 		try {
 			String[] keys = new String[] { IMarker.LOCATION, IMarker.SEVERITY, IMarker.DONE };
 			Object[] values = new Object[3];
 			values[0] = getRandomString();
 			values[1] = new Integer(5);
 			values[2] = Boolean.FALSE;
 			marker.setAttributes(keys, values);
 			Object[] found = marker.getAttributes(keys);
 			assertEquals("3.0." + resource.getFullPath(), values, found);
 			marker.setAttribute(IMarker.SEVERITY, null);
 			values[1] = null;
 			found = marker.getAttributes(keys);
 			assertEquals("3.1." + resource.getFullPath(), values, found);
 			values[1] = new Integer(5);
 			marker.setAttribute(IMarker.SEVERITY, values[1]);
 			Map all = marker.getAttributes();
 			assertEquals("3.2." + resource.getFullPath(), all, keys, values);
 		} catch (CoreException e) {
 			fail("3.2." + resource.getFullPath(), e);
 		}
 
 		// try sending null as args
 		try {
 			marker.getAttribute(null);
 			fail("4.0");
 		} catch (CoreException e) {
 			fail("4.1." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.2", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			marker.getAttributes(null);
 			fail("4.3");
 		} catch (CoreException e) {
 			fail("4.4." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.5", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			marker.setAttribute(null, getRandomString());
 			fail("4.6");
 		} catch (CoreException e) {
 			fail("4.7." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.8", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			marker.setAttributes(null, new String[] { getRandomString()});
 			fail("4.9");
 		} catch (CoreException e) {
 			fail("4.10." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.11", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			marker.setAttributes(new String[] { IMarker.MESSAGE }, null);
 			fail("4.12");
 		} catch (CoreException e) {
 			fail("4.13." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.14", e);
 		} catch (RuntimeException e) {
 		}
 		//set attributes on deleted marker
 		try {
 			marker.delete();
 		} catch(CoreException e) {
 			fail("5.0", e);
 		}
 		try {
 			marker.setAttribute(IMarker.MESSAGE, "Hello");
 			fail("5.1");
 		} catch (CoreException e) {
 		}
 		try {
 			marker.setAttributes(new String[] {IMarker.LINE_NUMBER}, new Object[] {new Integer(4)});
 			fail("5.2");
 		} catch (CoreException e) {
 		}
 		try {
 			HashMap attributes = new HashMap();
 			attributes.put(IMarker.MESSAGE, "Hello");
 			marker.setAttributes(attributes);
 			fail("5.3");
 		} catch (CoreException e) {
 		}
 	}
 }
 public void testSetGetAttribute2() {
 	log("testSetGetAttribute2");
 
 	for (int i = 0; i < resources.length; i++) {
 		IResource resource = resources[i];
 		IMarker marker = null;
 
 		// getting a non-existant attribute should return null or the specified default
 		try {
 			marker = resource.createMarker(IMarker.PROBLEM);
 		} catch (CoreException e) {
 			fail("0.0" + resource.getFullPath(), e);
 		}
 		try {
 			// #getAttribute(Object)
 			assertNull("1.0." + resource.getFullPath(), marker.getAttribute(IMarker.MESSAGE));
 			// #getAttribute(String, String)
 			assertEquals("1.1." + resource.getFullPath(), "default", marker.getAttribute(IMarker.MESSAGE, "default"));
 			// #getAttribute(String, boolean)
 			assertEquals("1.2." + resource.getFullPath(), true, marker.getAttribute(IMarker.MESSAGE, true));
 			// #getAttribute(String, int)
 			assertEquals("1.3." + resource.getFullPath(), 5, marker.getAttribute(IMarker.MESSAGE, 5));
 			// #getAttributes()
 			assertNull("1.4." + resource.getFullPath(), marker.getAttributes());
 			// #getAttributes(String[])
 			assertTrue("1.5." + resource.getFullPath(), marker.getAttributes(new String[] { IMarker.MESSAGE })[0] == null);
 		} catch (CoreException e) {
 			fail("1.99." + resource.getFullPath(), e);
 		}
 
 		// set an attribute, get its value, then remove it
 		String testMessage = getRandomString();
 		try {
 			marker.setAttribute(IMarker.MESSAGE, testMessage);
 			Object value = marker.getAttribute(IMarker.MESSAGE);
 			assertEquals("2.0." + resource.getFullPath(), testMessage, (String) value);
 			marker.setAttribute(IMarker.MESSAGE, null);
 			value = marker.getAttribute(IMarker.MESSAGE);
 			assertNull("2.1." + resource.getFullPath(), value);
 		} catch (CoreException e) {
 			fail("2.2." + resource.getFullPath(), e);
 		}
 
 		// set more attributes, get their values, then remove one
 		try {
 			String[] keys = new String[] { IMarker.LOCATION, IMarker.SEVERITY, IMarker.DONE };
 			Object[] values = new Object[3];
 			values[0] = getRandomString();
 			values[1] = new Integer(5);
 			values[2] = Boolean.FALSE;
 			marker.setAttributes(keys, values);
 			Object[] found = marker.getAttributes(keys);
 			assertEquals("3.0." + resource.getFullPath(), values, found);
 			marker.setAttribute(IMarker.SEVERITY, null);
 			values[1] = null;
 			found = marker.getAttributes(keys);
 			assertEquals("3.1." + resource.getFullPath(), values, found);
 			values[1] = new Integer(5);
 			marker.setAttribute(IMarker.SEVERITY, values[1]);
 			Map all = marker.getAttributes();
 			assertEquals("3.2." + resource.getFullPath(), all, keys, values);
 		} catch (CoreException e) {
 			fail("3.2." + resource.getFullPath(), e);
 		}
 
 		// try sending null as args
 		try {
 			// #getAttribute(String)
 			marker.getAttribute(null);
 			fail("4.0");
 		} catch (CoreException e) {
 			fail("4.1." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.2", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #getAttribute(String, String)
 			marker.getAttribute(null, "default");
 			fail("4.3");
 		} catch (NullPointerException e) {
 			fail("4.5", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #getAttribute(String, boolean)
 			marker.getAttribute(null, true);
 			fail("4.6");
 		} catch (NullPointerException e) {
 			fail("4.8", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #getAttribute(String, int)
 			marker.getAttribute(null, 5);
 			fail("4.9");
 		} catch (NullPointerException e) {
 			fail("4.11", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #getAttributes(String[])
 			marker.getAttributes(null);
 			fail("4.12");
 		} catch (CoreException e) {
 			fail("4.13." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.14", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #setAttribute(String, Object)
 			marker.setAttribute(null, getRandomString());
 			fail("4.15");
 		} catch (CoreException e) {
 			fail("4.16." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.17", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #setAttributes(String[], Object[])
 			marker.setAttributes(null, new String[] { getRandomString()});
 			fail("4.18");
 		} catch (CoreException e) {
 			fail("4.19." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.20", e);
 		} catch (RuntimeException e) {
 		}
 		try {
 			// #setAttributes(String[], Object[])
 			marker.setAttributes(new String[] { IMarker.MESSAGE }, null);
 			fail("4.21");
 		} catch (CoreException e) {
 			fail("4.22." + resource.getFullPath(), e);
 		} catch (NullPointerException e) {
 			fail("4.23", e);
 		} catch (RuntimeException e) {
 		}
 	}
 }
 }
