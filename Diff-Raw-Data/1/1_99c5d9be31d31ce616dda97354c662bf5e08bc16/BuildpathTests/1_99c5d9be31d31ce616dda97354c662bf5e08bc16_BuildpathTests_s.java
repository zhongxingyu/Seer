 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.buildpath;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Map;
 
 import junit.framework.Test;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathAttribute;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IBuiltinModuleProvider;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IModelMarker;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.tests.model.ModelTestsPlugin;
 import org.eclipse.dltk.core.tests.model.ModifyingResourceTests;
 import org.eclipse.dltk.core.tests.util.Util;
 import org.eclipse.dltk.internal.core.ArchiveProjectFragment;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.internal.core.DLTKProject;
 import org.eclipse.dltk.utils.CorePrinter;
 
 
 public class BuildpathTests extends ModifyingResourceTests {
 
 	private static final String[] TEST_NATURE = new String[] { "org.eclipse.dltk.core.tests.testnature" };
 
 	private static final String BUILDPATH_PRJ_0 = "Buildpath0";
 
 	private static final String BUILDPATH_PRJ_1 = "Buildpath1";
 	
 	private static final String BUILDPATH_PRJ_2 = "Buildpath2";
 
 	public class TestContainer implements IBuildpathContainer {
 		IPath path;
 
 		IBuildpathEntry[] entries;
 
 		TestContainer(IPath path, IBuildpathEntry[] entries) {
 			this.path = path;
 			this.entries = entries;
 		}
 
 		public IPath getPath() {
 			return this.path;
 		}
 
 		public IBuildpathEntry[] getBuildpathEntries() {
 			return this.entries;
 		}
 
 		public String getDescription() {
 			return this.path.toString();
 		}
 
 		public int getKind() {
 			return 0;
 		}
 
 		public IBuiltinModuleProvider getBuiltinProvider() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	public BuildpathTests(String name) {
 		super(ModelTestsPlugin.PLUGIN_NAME, name);
 	}
 
 	public static Test suite() {
 		return new Suite(BuildpathTests.class);
 	}
 
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
 		setUpScriptProject("ModelMembers");
 
 		setUpScriptProject(BUILDPATH_PRJ_0);
 		setUpScriptProject(BUILDPATH_PRJ_1);		
 		setUpScriptProject("p1");
 		setUpScriptProject("p2");
 	}
 
 	private void assertEncodeDecodeEntry(String projectName, String expectedEncoded,
 			IBuildpathEntry entry) {
 		IDLTKProject project = getScriptProject(projectName);
 		String encoded = project.encodeBuildpathEntry(entry);
 		assertSourceEquals("Unexpected encoded entry", expectedEncoded, encoded);
 		IBuildpathEntry decoded = project.decodeBuildpathEntry(encoded);
 		assertEquals("Unexpected decoded entry", entry, decoded);
 	}
 
 	protected void assertStatus(String expected, IStatus status) {
 		String actual = status.getMessage();
 		if (!expected.equals(actual)) {
 			System.out.print(Util.displayString(actual, 2));
 			System.out.println(",");
 		}
 		assertEquals(expected, actual);
 	}
 
 	protected void assertStatus(String message, String expected, IStatus status) {
 		String actual = status.getMessage();
 		if (!expected.equals(actual)) {
 			System.out.print(Util.displayString(actual, 2));
 			System.out.println(",");
 		}
 		assertEquals(message, expected, actual);
 	}
 
 	protected File createFile(File parent, String name, String content) throws IOException {
 		File file = new File(parent, name);
 		FileOutputStream out = new FileOutputStream(file);
 		out.write(content.getBytes());
 		out.close();
 		/*
 		 * Need to change the time stamp to realize that the file has been
 		 * modified
 		 */
 		file.setLastModified(System.currentTimeMillis() + 2000);
 		return file;
 	}
 
 	protected File createFolder(File parent, String name) {
 		File file = new File(parent, name);
 		file.mkdirs();
 		return file;
 	}
 	protected int numberOfCycleMarkers(IDLTKProject scriptProject) throws CoreException {
 		IMarker[] markers = scriptProject.getProject().findMarkers(IModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
 		int result = 0;
 		for (int i = 0, length = markers.length; i < length; i++) {
 			IMarker marker = markers[i];
 			String cycleAttr = (String)marker.getAttribute(IModelMarker.CYCLE_DETECTED);
 			if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
 				result++;
 			}
 		}
 		return result;
 	}
 
 	public void tearDownSuite() throws Exception {
 		// TODO Auto-generated method stub
 		deleteProject(BUILDPATH_PRJ_0);
 		deleteProject(BUILDPATH_PRJ_1);		
 		deleteProject("p1");
 		deleteProject("p2");
 		super.tearDownSuite();
 	}
 
 	public void test001() throws ModelException {
 		DLTKProject project = (DLTKProject) getScriptProject(BUILDPATH_PRJ_0);
 		assertNotNull(project);
 		IBuildpathEntry entrys[] = project.getRawBuildpath();
 		assertEquals(3, entrys.length);
 		assertEquals(IBuildpathEntry.BPE_SOURCE, entrys[0].getEntryKind());
 		assertEquals(IBuildpathEntry.BPE_PROJECT, entrys[1].getEntryKind());
 		assertEquals(IBuildpathEntry.BPE_SOURCE, entrys[2].getEntryKind());
 		IModelElement[] children = project.getChildren();
 		assertEquals("Sould be 2 accessible project fragments", 2, children.length);
 		assertTrue(children[0] instanceof IProjectFragment);
 		assertTrue(children[1] instanceof IProjectFragment);
 		IProjectFragment fr0 = (IProjectFragment) children[0];
 		IProjectFragment fr1 = (IProjectFragment) children[1];
 
 		children = fr0.getChildren();
 		assertEquals(1, children.length);
 		assertTrue(children[0] instanceof IScriptFolder);
 		IModelElement[] folderChildren = ((IScriptFolder) children[0]).getChildren();
 		assertEquals(2, folderChildren.length);
 		assertTrue(folderChildren[0] instanceof ISourceModule);
 		assertEquals("X.txt", ((ISourceModule) (folderChildren[0])).getElementName());
 		assertEquals("X2.txt", ((ISourceModule) (folderChildren[1])).getElementName());
 		assertTrue(folderChildren[1] instanceof ISourceModule);
 
 		children = fr1.getChildren();
 		assertEquals(1, children.length);
 		assertTrue(children[0] instanceof IScriptFolder);
 		folderChildren = ((IScriptFolder) children[0]).getChildren();
 		assertEquals(1, folderChildren.length);
 		assertTrue(folderChildren[0] instanceof ISourceModule);
 		assertEquals("X3.txt", ((ISourceModule) (folderChildren[0])).getElementName());
 	}
 
 	public void test002() throws ModelException {
 		DLTKProject project = (DLTKProject) getScriptProject(BUILDPATH_PRJ_0);
 		DLTKProject project2 = (DLTKProject) getScriptProject(BUILDPATH_PRJ_1);
 		assertNotNull(project);
 		IProjectFragment fragments[] = project.getProjectFragments();
 		IBuildpathEntry entrys[] = project.getResolvedBuildpath();
 
 //		CorePrinter cPrinter = new CorePrinter(System.out);
 //		System.out.println("Project 1 model:");
 //		project.printNode(cPrinter);
 //		cPrinter.flush();
 //		System.out.println("Project 2 model:");
 //		project2.printNode(cPrinter);
 //		cPrinter.flush();
 	}
 
 	/**
 	 * Testie container test
 	 * 
 	 * @throws ModelException
 	 */
 	public void test003() throws ModelException {
 		DLTKProject project = (DLTKProject) getScriptProject(BUILDPATH_PRJ_0);
 		DLTKProject project2 = (DLTKProject) getScriptProject(BUILDPATH_PRJ_1);
 		assertNotNull(project);
 		IProjectFragment fragments[] = project.getProjectFragments();
 		IBuildpathEntry entrys[] = project.getResolvedBuildpath();
 
 //		CorePrinter cPrinter = new CorePrinter(System.out);
 //		System.out.println("Project 1 model:");
 //		project.printNode(cPrinter);
 //		cPrinter.flush();
 //		System.out.println("Project 2 model:");
 //		project2.printNode(cPrinter);
 //		cPrinter.flush();
 	}
 	
 	/**
 	 * Library BuildpathEntry test  
 	 * @throws Exception 
 	 */
 	public void test004() throws Exception {
 		setUpScriptProject(BUILDPATH_PRJ_2);
 		IDLTKProject project = (IDLTKProject) getScriptProject(BUILDPATH_PRJ_2);
 		assertNotNull(project);
 		IBuildpathEntry entrys[] = project.getRawBuildpath();
 		assertEquals(1, entrys.length);
 		assertEquals(IBuildpathEntry.BPE_LIBRARY, entrys[0].getEntryKind());
 		IProjectFragment[] fragments = project.getProjectFragments();
 		assertEquals(1, fragments.length);
 		assertTrue(fragments[0] instanceof ArchiveProjectFragment);
 		IProjectFragment fragment = fragments[0];
 		IModelElement[] elements = fragment.getChildren();
 		
 		System.out.println("Model:");
 		CorePrinter printer = new CorePrinter(System.out);
 		((DLTKProject)project).printNode(printer);
 		printer.flush();
 		
 		deleteProject(BUILDPATH_PRJ_2);		
 	}
 	
 	/**
 	 * External folder Library BuildpathEntry test   
 	 * @throws Exception 
 	 */
 	public void test005() throws Exception {
 		try {
 			URL url = ModelTestsPlugin.getDefault().getBundle().getEntry("workspace/Buildpath3");
 			URL res = FileLocator.resolve(url);
 			String filePath = res.getFile();			
 			IDLTKProject proj = this.createScriptProject("P", TEST_NATURE, new String[] { "src" });
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length + 1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newExtLibraryEntry(new Path( filePath ));
 
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP);
 
 			assertStatus("OK", status);
 			
 			proj.setRawBuildpath(newCP, null);
 			
 			IProjectFragment[] fragments = proj.getProjectFragments();			
 			
 			System.out.println("Model:");
 			CorePrinter printer = new CorePrinter(System.out, true);
 			((DLTKProject)proj).printNode(printer);
 			printer.flush();
 		} finally {
 			this.deleteProject("P");			
 		}
 	}
 	
 	public void test006() throws Exception {
 		try {
 			URL url = ModelTestsPlugin.getDefault().getBundle().getEntry("/workspace/Buildpath3");
 			URL res = FileLocator.resolve(url);			
 			String filePath = res.getFile();			
 			IDLTKProject proj = this.createScriptProject("P", TEST_NATURE, new String[] { "src" });
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length + 1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newContainerEntry(new Path("Testie" + filePath));
 
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP);
 
 			assertStatus("OK", status);
 			
 			proj.setRawBuildpath(newCP, null);
 			
 			IProjectFragment[] fragments = proj.getProjectFragments();
 			assertEquals(1, fragments.length);
 			
 			System.out.println("Model:");
 			CorePrinter printer = new CorePrinter(System.out, true);
 			((DLTKProject)proj).printNode(printer);
 			printer.flush();
 		} finally {
 			this.deleteProject("P");			
 		}
 	}
 
 
 	/*
 	 * Ensures that a source Buildpath entry can be encoded and decoded.
 	 */
 	public void testEncodeDecodeEntry01() {
 		assertEncodeDecodeEntry("P", "<buildpathentry kind=\"src\" path=\"src\"/>\n", DLTKCore
 				.newSourceEntry(new Path("/P/src")));
 	}
 	/*
 	 * Ensures that a source Buildpath entry with all possible attributes can be encoded and decoded.
 	 */
 	public void testEncodeDecodeEntry02() {
 		assertEncodeDecodeEntry(
 			"P", 
 			"<buildpathentry excluding=\"**/X.java\" including=\"**/Y.java\" kind=\"src\" path=\"src\">\n" + 
 			"	<attributes>\n" + 
 			"		<attribute name=\"attrName\" value=\"some value\"/>\n" + 
 			"	</attributes>\n" + 
 			"</buildpathentry>\n",
 			DLTKCore.newSourceEntry(
 				new Path("/P/src"), 
 				new IPath[] {new Path("**/Y.java")},
 				new IPath[] {new Path("**/X.java")},				
 				new IBuildpathAttribute[] {DLTKCore.newBuildpathAttribute("attrName", "some value")})
 		);
 	}
 
 	/*
 	 * Ensures that a project Buildpath entry can be encoded and decoded.
 	 */
 	public void testEncodeDecodeEntry03() {
 		assertEncodeDecodeEntry("P1", "<buildpathentry kind=\"prj\" path=\"/P2\"/>\n", DLTKCore
 				.newProjectEntry(new Path("/P2")));
 	}
 	
 
 	/**
 	 * Ensures that adding an empty Buildpath container generates the correct
 	 * deltas.
 	 */
 	public void testEmptyContainer() throws CoreException {
 		try {
 			IDLTKProject proj = createScriptProject("P", TEST_NATURE, null);
 
 			startDeltas();
 
 			// create container
 			DLTKCore.setBuildpathContainer(new Path("container/default"),
 					new IDLTKProject[] { proj }, new IBuildpathContainer[] { new TestContainer(
 							new Path("container/default"), new IBuildpathEntry[] {}) }, null);
 
 			// set P's Buildpath with this container
 			IBuildpathEntry container = DLTKCore.newContainerEntry(new Path("container/default"),
 					true);
 			proj.setRawBuildpath(new IBuildpathEntry[] { container }, null);
 
 			assertDeltas("Unexpected delta", "P[*]: {CONTENT | BUILDPATH CHANGED}\n"
 					+ "	ResourceDelta(/P/.buildpath)[*]");
 		} finally {
 			stopDeltas();
 			this.deleteProject("P");
 		}
 	}
 
 	/*
 	 * Ensures that a non existing source folder cannot be put on the Buildpath.
 	 * (regression test for bug 66512 Invalid Buildpath entry not rejected)
 	 */
 	public void testInvalidSourceFolder() throws CoreException {
 		try {
 			createScriptProject("P1i", TEST_NATURE, new String[]{""});
 			IDLTKProject proj = createScriptProject("P2i", TEST_NATURE,
 					new String[]{""}, new String[] { "/P1i/src1/src2" });
 			assertMarkers("Unexpected markers",
 					"Project P2i is missing required source folder: \'/P1i/src1/src2\'", proj);
 		} finally {
 			deleteProject("P1i");
 			deleteProject("P2i");
 		}
 	}
 
 	/**
 	 * Should detect duplicate entries on the Buildpath
 	 */
 	public void testBuildpathValidation01() throws CoreException {
 		try {
 			IDLTKProject proj = this.createScriptProject("P", TEST_NATURE, new String[] { "src" });
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length + 1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = newCP[0];
 
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP);
 
 			assertStatus("should have detected duplicate entries on the buildpath",
 					"Build path contains duplicate entry: \'src\' for project P", status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}	
 	public void testBuildpathLibraryValidation01() throws CoreException {
 		try {
 			IDLTKProject proj = this.createScriptProject("Pv0", TEST_NATURE, new String[] { "src" });
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length + 1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newExtLibraryEntry(new Path("/opt2"));
 
 			IModelStatus status = BuildpathEntry.validateBuildpathEntry(proj, newCP[originalCP.length], false);
 
 			assertStatus("should detect not pressent folders",
 					"Required library cannot denote external folder or archive: \'/opt2\' for project Pv0", status);
 		} finally {
 			this.deleteProject("Pv0");
 		}
 	}
 
 	/**
 	 * Should detect nested source folders on the Buildpath
 	 */
 	public void testBuildpathValidation02() throws CoreException {
 		try {
 			IDLTKProject proj = this.createScriptProject("P", TEST_NATURE, new String[] { "src" });
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length + 1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"));
 
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP);

 			assertStatus(
 					"should have detected nested source folders on the buildpath",
 					"Cannot nest \'P/src\' inside \'P\'. To enable the nesting exclude \'src/\' from \'P\'",
 					status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Should detect library folder nested inside source folder on the Buildpath
 	 */ 
 	public void testBuildpathValidation03() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newLibraryEntry(new Path("/P/src/lib"));
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP);
 			
 			assertStatus(
 				"should have detected library folder nested inside source folder on the Buildpath", 
 				"Cannot nest \'P/src/lib\' inside \'P/src\'. To enable the nesting exclude \'lib/\' from \'P/src\'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 
 	public void testBuildpathValidation05() throws CoreException {
 
 		IDLTKProject[] p = null;
 		try {
 
 			p = new IDLTKProject[] {
 					this.createScriptProject("P0v", TEST_NATURE, new String[] { "src0", "src1" }),
 					this.createScriptProject("P1v", TEST_NATURE, new String[] { "src1" }), };
 
 			DLTKCore.setBuildpathContainer(new Path("container/default"),
 					new IDLTKProject[] { p[0] }, new IBuildpathContainer[] { new TestContainer(
 							new Path("container/default"), new IBuildpathEntry[] { DLTKCore
 									.newSourceEntry(new Path("/P0v/src0")) }) }, null);
 
 			IBuildpathEntry[] newBuildpath = new IBuildpathEntry[] {
 					DLTKCore.newSourceEntry(new Path("/P0v/src1")),
 					DLTKCore.newContainerEntry(new Path("container/default")), };
 
 			// validate Buildpath
 			IModelStatus status = BuildpathEntry.validateBuildpath(p[0], newBuildpath);
 			assertStatus(
 					"should not have detected external source folder through a container on the Buildpath",
 					"OK", status);
 
 			// validate Buildpath entry
 			status = BuildpathEntry.validateBuildpathEntry(p[0], newBuildpath[1], true);
 			assertStatus(
 					"should have detected external source folder through a container on the Buildpath",
 					"Invalid buildpath container: \'container/default\' in project P0v", status);
 
 		} finally {
 			this.deleteProject("P0v");
 			this.deleteProject("P1v");
 		}
 	}
 	public void testBuildpathValidation06() throws CoreException {
 		
 		IDLTKProject[] p = null;
 		try {
 
 			p = new IDLTKProject[]{
 				this.createScriptProject("P0", TEST_NATURE, new String[] {"src"} ),
 			};
 
 			// validate Buildpath entry
 			IBuildpathEntry[] newBuildpath = new IBuildpathEntry[]{
 				DLTKCore.newSourceEntry(new Path("/P0")),
 				DLTKCore.newSourceEntry(new Path("/P0/src")),
 			};
 					
 			IModelStatus status = BuildpathEntry.validateBuildpath(p[0], newBuildpath );
 			assertStatus(
 				"should have detected nested source folder", 
 				"Cannot nest \'P0/src\' inside \'P0\'. To enable the nesting exclude \'src/\' from \'P0\'",				
 				status);
 		} finally {
 			this.deleteProject("P0");
 		}
 	}
 	/**
 	 * Should allow nested source folders on the Buildpath as long as the outer
 	 * folder excludes the inner one.
 	 */ 
 	public void testBuildpathValidation07() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"} );
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("src/")});
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"should have allowed nested source folders with exclusion on the buildpath", 
 				"OK",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Should allow a nested binary folder in a source folder on the buildpath as
 	 * long as the outer folder excludes the inner one.
 	 */ 
 	public void testBuildpathValidation08() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {});
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("lib/")});
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"should have allowed nested lib folders with exclusion on the buildpath", 
 				"OK",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	
 	/**
 	 * Should not allow nested source folders on the Buildpath if exclusion filter has no trailing slash.
 	 */ 
 	public void testBuildpathValidation15() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"} );
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("**/src")});
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"End exclusion filter \'src\' with / to fully exclude \'P/src\'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Should not allow exclusion patterns if project preference disallow them
 	 */
 	public void testBuildpathValidation21() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {});
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P/src"), new IPath[]{new Path("**/src")});
 			
 			Map options = new Hashtable(5);
 			options.put(DLTKCore.CORE_ENABLE_BUILDPATH_EXCLUSION_PATTERNS, DLTKCore.DISABLED);
 			proj.setOptions(options);
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"Inclusion or exclusion patterns are disabled in project P, cannot selectively include or exclude from entry: \'src\'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * 33207 - Reject output folder that coincidate with distinct source folder
 	 * but 36465 - Unable to create multiple source folders when not using bin for output
 	 * default output scenarii is still tolerated
 	 */
 	public void testBuildpathValidation23() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {});
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+2];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P/"), new IPath[]{new Path("src/")}, BuildpathEntry.EXCLUDE_NONE);
 			newCP[originalCP.length+1] = DLTKCore.newSourceEntry(new Path("/P/src"), new IPath[0], BuildpathEntry.EXCLUDE_NONE);
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP);
 	
 			assertStatus(
 				"Cannot nest 'P/src' inside 'P/'. To enable the nesting exclude 'src/' from 'P/'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Should not allow nested source folders on the buildpath if the outer
 	 * folder includes the inner one.
 	 */ 
 	public void testBuildpathValidation34() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("src/")}, new IPath[0], null);
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"should not have allowed nested source folders with inclusion on the buildpath", 
 				"Cannot nest \'P/src\' inside \'P\'. To enable the nesting exclude \'src/\' from \'P\'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}	
 	/**
 	 * Should allow nested source folders on the buildpath if inclusion filter has no trailing slash.
 	 */ 
 	public void testBuildpathValidation36() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"), new IPath[] {new Path("**/src")}, new Path[0], null);
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"OK",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Should not allow inclusion patterns if project preference disallow them
 	 */
 	public void testBuildpathValidation37() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {} );
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P/src"), new IPath[]{new Path("**/src")}, new Path[0], null);
 			
 			Map options = new Hashtable(5);
 			options.put(DLTKCore.CORE_ENABLE_BUILDPATH_EXCLUSION_PATTERNS, DLTKCore.DISABLED);
 			proj.setOptions(options);
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"Inclusion or exclusion patterns are disabled in project P, cannot selectively include or exclude from entry: \'src\'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	
 	/*
 	 * Should detect nested source folders on the buildpath and indicate the preference if disabled
 	 * (regression test for bug 122615 validate buildpath propose to exlude a source folder even though exlusion patterns are disabled)
 	 */ 
 	public void testBuildpathValidation42() throws CoreException {
 		try {
 			IDLTKProject proj =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			proj.setOption(DLTKCore.CORE_ENABLE_BUILDPATH_EXCLUSION_PATTERNS, DLTKCore.DISABLED);
 			IBuildpathEntry[] originalCP = proj.getRawBuildpath();
 		
 			IBuildpathEntry[] newCP = new IBuildpathEntry[originalCP.length+1];
 			System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
 			newCP[originalCP.length] = DLTKCore.newSourceEntry(new Path("/P"));
 			
 			IModelStatus status = BuildpathEntry.validateBuildpath(proj, newCP );
 			
 			assertStatus(
 				"should have detected nested source folders on the buildpath", 
 				"Cannot nest \'P/src\' inside \'P\'. To allow the nesting enable use of exclusion patterns in the preferences of project \'P\' and exclude \'src/\' from \'P\'",
 				status);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Setting the buildpath with two entries specifying the same path
 	 * should fail.
 	 */
 	public void testBuildpathWithDuplicateEntries() throws CoreException {
 		try {
 			IDLTKProject project =  this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			IBuildpathEntry[] cp= project.getRawBuildpath();
 			IBuildpathEntry[] newCp= new IBuildpathEntry[cp.length *2];
 			System.arraycopy(cp, 0, newCp, 0, cp.length);
 			System.arraycopy(cp, 0, newCp, cp.length, cp.length);
 			try {
 				project.setRawBuildpath(newCp, null);
 			} catch (ModelException jme) {
 				return;
 			}
 			assertTrue("Setting the buildpath with two entries specifying the same path should fail", false);
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Adding an entry to the buildpath for a project that does not exist
 	 * should not break the model. The buildpath should contain the
 	 * entry, but the root should not appear in the children.
 	 */
 	public void testBuildpathWithNonExistentProjectEntry() throws CoreException {
 		try {
 			IDLTKProject project= this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			IBuildpathEntry[] originalPath= project.getRawBuildpath();
 			IProjectFragment[] originalRoots= project.getProjectFragments();
 		
 			IBuildpathEntry[] newPath= new IBuildpathEntry[originalPath.length + 1];
 			System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);
 		
 			IBuildpathEntry newEntry= DLTKCore.newProjectEntry(new Path("/NoProject"), false);
 			newPath[originalPath.length]= newEntry;
 		
 			project.setRawBuildpath(newPath, null);
 		
 			IBuildpathEntry[] getPath= project.getRawBuildpath();
 			assertTrue("should be the same length", getPath.length == newPath.length);
 			for (int i= 0; i < getPath.length; i++) {
 				assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
 			}
 		
 			IProjectFragment[] newRoots= project.getProjectFragments();
 			assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
 			for (int i= 0; i < newRoots.length; i++) {
 				assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
 			}
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Adding an entry to the buildpath for a folder that does not exist
 	 * should not break the model. The buildpath should contain the
 	 * entry, but the root should not appear in the children.
 	 */
 	public void testBuildpathWithNonExistentSourceEntry() throws CoreException {
 		try {
 			IDLTKProject project= this.createScriptProject("P", TEST_NATURE, new String[] {"src"});
 			IBuildpathEntry[] originalPath= project.getRawBuildpath();
 			IProjectFragment[] originalRoots= project.getProjectFragments();
 
 			IBuildpathEntry[] newPath= new IBuildpathEntry[originalPath.length + 1];
 			System.arraycopy(originalPath, 0, newPath, 0, originalPath.length);
 
 			IBuildpathEntry newEntry= DLTKCore.newSourceEntry(new Path("/P/moreSource"));
 			newPath[originalPath.length]= newEntry;
 
 			project.setRawBuildpath(newPath, null);
 
 			IBuildpathEntry[] getPath= project.getRawBuildpath();
 			assertTrue("should be the same length", getPath.length == newPath.length);
 			for (int i= 0; i < getPath.length; i++) {
 				assertTrue("entries should be the same", getPath[i].equals(newPath[i]));
 			}
 
 			IProjectFragment[] newRoots= project.getProjectFragments();
 			assertTrue("Should be the same number of roots", originalRoots.length == newRoots.length);
 			for (int i= 0; i < newRoots.length; i++) {
 				assertTrue("roots should be the same", originalRoots[i].equals(newRoots[i]));
 			}
 		} finally {
 			this.deleteProject("P");
 		}
 	}
 	/**
 	 * Ensure that cycle are properly reported.
 	 */
 	public void testCycleReport() throws CoreException {
 
 		try {
 			IDLTKProject p1 = this.createScriptProject("p1_", TEST_NATURE, new String[] {""} );
 			IDLTKProject p2 = this.createScriptProject("p2_", TEST_NATURE, new String[] {""} );
 			IDLTKProject p3 = this.createScriptProject("p3_", TEST_NATURE, new String[] {""},  new String[] {"/p2_"} );
 		
 			// Ensure no cycle reported
 			IDLTKProject[] projects = { p1, p2, p3 };
 			int cycleMarkerCount = 0;
 			for (int i = 0; i < projects.length; i++){
 				cycleMarkerCount += this.numberOfCycleMarkers(projects[i]);
 			}
 			assertTrue("Should have no cycle markers", cycleMarkerCount == 0);
 		
 			// Add cycle
 			IBuildpathEntry[] originalP1CP= p1.getRawBuildpath();
 			IBuildpathEntry[] originalP2CP= p2.getRawBuildpath();
 
 			// Add P1 as a prerequesite of P2
 			int length = originalP2CP.length;
 			IBuildpathEntry[] newCP= new IBuildpathEntry[length + 1];
 			System.arraycopy(originalP2CP, 0 , newCP, 0, length);
 			newCP[length]= DLTKCore.newProjectEntry(p1.getProject().getFullPath(), false);
 			p2.setRawBuildpath(newCP, null);
 
 			// Add P3 as a prerequesite of P1
 			length = originalP1CP.length;
 			newCP= new IBuildpathEntry[length + 1];
 			System.arraycopy(originalP1CP, 0 , newCP, 0, length);
 			newCP[length]= DLTKCore.newProjectEntry(p3.getProject().getFullPath(), false);
 			p1.setRawBuildpath(newCP, null);
 
 			waitForAutoBuild(); // wait for cycle markers to be created
 			cycleMarkerCount = 0;
 			for (int i = 0; i < projects.length; i++){
 				cycleMarkerCount += numberOfCycleMarkers(projects[i]);
 			}
 			assertEquals("Unexpected number of projects involved in a buildpath cycle", 3, cycleMarkerCount);
 			
 		} finally {
 			// cleanup  
 			deleteProjects(new String[] {"p1_", "p2_", "p3_"});
 		}
 	}
 	/**
 	 * Setting the buildpath to empty should result in no entries,
 	 * and a delta with removed roots.
 	 */
 	public void testEmptyBuildpath() throws CoreException {
 		IDLTKProject project = this.createScriptProject("P", TEST_NATURE, new String[] {""} );
 		try {
 			startDeltas();
 			setBuildpath(project, new IBuildpathEntry[] {});
 			IBuildpathEntry[] cp= project.getRawBuildpath();
 			assertTrue("buildpath should have no entries", cp.length == 0);
 
 			// ensure the deltas are correct
 			assertDeltas(
 				"Unexpected delta",
 				"P[*]: {CHILDREN | BUILDPATH CHANGED}\n" + 
 				"	<project root>[*]: {REMOVED FROM BUILDPATH}\n" + 
 				"	ResourceDelta(/P/.buildpath)[*]"
 			);
 		} finally {
 			stopDeltas();
 			this.deleteProject("P");
 		}
 	}
 	/*
 	 * Ensures that a source folder that contains character that must be encoded can be written.
 	 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=70193)
 	 */
 	public void testEncoding() throws CoreException {
 		try {
 			createScriptProject("P", TEST_NATURE, new String[] {"src\u3400"});
 			IFile file = getFile("/P/.buildpath");
 			String encodedContents = new String (org.eclipse.dltk.internal.core.util.Util.getResourceContentsAsCharArray(file, "UTF-8"));
 			encodedContents = Util.convertToIndependantLineDelimiter(encodedContents);
 			assertEquals(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 				"<buildpath>\n" +
 				"	<buildpathentry kind=\"src\" path=\"src\u3400\"/>\n" +
 				"</buildpath>\n",
 				encodedContents);
 		} finally {
 			deleteProject("P");
 		}
 	}
 	/**
 	 * Tests the cross project Buildpath setting
 	 */
 	public void testBuildpathCrossProject() throws CoreException {
 		IDLTKProject project = this.createScriptProject("P1c", TEST_NATURE, new String[] {""} );
 		this.createScriptProject("P2c", TEST_NATURE, new String[] {});
 		try {
 			startDeltas();
 			IProjectFragment oldRoot= getProjectFragment("P1c", "");
 	 		IBuildpathEntry projectEntry= DLTKCore.newProjectEntry(new Path("/P2c"), false);
 			IBuildpathEntry[] newBuildpath= new IBuildpathEntry[]{projectEntry};
 			project.setRawBuildpath(newBuildpath, null);
 			project.getProjectFragments();
 			IModelElementDelta removedDelta= getDeltaFor(oldRoot, true);
 			assertDeltas(
 				"Unexpected delta", 
 				"<project root>[*]: {REMOVED FROM BUILDPATH}", 
 				removedDelta);
 		} finally {
 			stopDeltas();
 			this.deleteProjects(new String[] {"P1c", "P2c"});
 		}
 	}
 }
