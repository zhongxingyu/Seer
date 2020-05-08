 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.model;
 
 import junit.framework.Test;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.team.core.RepositoryProvider;
 
 
 public class WorkingCopyTests extends ModifyingResourceTests {
 	private static final String[] TEST_NATURE = new String[] { "org.eclipse.dltk.core.tests.testnature" };
 	ISourceModule cu = null;
 	ISourceModule copy = null;
 	public class TestWorkingCopyOwner extends WorkingCopyOwner {
 		public IBuffer createBuffer(ISourceModule workingCopy) {
 			return new TestBuffer(workingCopy);
 		}
 	}
 
 	public WorkingCopyTests(String name) {
 		super(ModelTestsPlugin.PLUGIN_NAME, name);
 	}
 
 	public static Test suite() {
 		return new Suite(WorkingCopyTests.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		try {
 			this.createScriptProject("P", TEST_NATURE,new String[] {
 				"src"
 			} );
 			this.createFolder("P/src/x/y");
 			this.createFile("P/src/x/y/A.txt", "package x.y;\n" + "import java.io.File;\n" + "public class A {\n"
 					+ "  public class Inner {\n" + "    public class InnerInner {\n" + "    }\n" + "    int innerField;\n"
 					+ "    void innerMethod() {\n" + "    }\n" + "  }\n" + "  static String FIELD;\n" + "  {\n"
 					+ "    FIELD = File.pathSeparator;\n" + "  }\n" + "  int field1;\n" + "  boolean field2;\n" + "  public void foo() {\n"
 					+ "  }\n" + "}");
 			this.cu = this.getSourceModule("P/src/x/y/A.txt");
 			this.copy = cu.getWorkingCopy(null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e.getMessage());
 		}
 	}
 
 	protected void tearDown() throws Exception {
 		if (this.copy != null)
 			this.copy.discardWorkingCopy();
 		this.deleteProject("P");
 		super.tearDown();
 	}
 
 	/*
 	 * Ensures that cancelling a make consistent operation doesn't leave the
 	 * working copy closed (regression test for bug 61719 Incorrect fine grain
 	 * delta after method copy-rename)
 	 */
 	public void testCancelMakeConsistent() throws ModelException {
 		String newContents = "package x.y;\n" + "public class A {\n" + "  public void bar() {\n" + "  }\n" + "}";
 		this.copy.getBuffer().setContents(newContents);
 		NullProgressMonitor monitor = new NullProgressMonitor();
 		monitor.setCanceled(true);
 		try {
 			this.copy.makeConsistent(monitor);
 		} catch (OperationCanceledException e) {
 			// got exception
 		}
 		assertTrue("Working copy should be opened", this.copy.isOpen());
 	}
 
 	/**
 	 */
 	public void testChangeContent() throws CoreException {
 		String newContents = "package x.y;\n" + "public class A {\n" + "  public void bar() {\n" + "  }\n" + "}";
 		this.copy.getBuffer().setContents(newContents);
 		this.copy.reconcile(false, null, null);
 		assertSourceEquals("Unexpected working copy contents", newContents, this.copy.getBuffer().getContents());
 		this.copy.commitWorkingCopy(true, null);
 		assertSourceEquals("Unexpected original cu contents", newContents, this.cu.getBuffer().getContents());
 	}
 
 	/*
 	 * Ensures that one cannot commit the contents of a working copy on a read
 	 * only cu.
 	 */
 	public void testChangeContentOfReadOnlyCU1() throws CoreException {
 		IResource resource = this.cu.getUnderlyingResource();
 		boolean readOnlyFlag = isReadOnly(resource);
 		boolean didComplain = false;
 		try {
 			setReadOnly(resource, true);
 			this.copy.getBuffer().setContents("invalid");
 			this.copy.commitWorkingCopy(true, null);
 		} catch (ModelException e) {
 			didComplain = true;
 		} finally {
 			setReadOnly(resource, readOnlyFlag);
 		}
 		assertTrue("Should have complained about modifying a read-only unit:", didComplain);
 		assertTrue("ReadOnly buffer got modified:", !this.cu.getBuffer().getContents().equals("invalid"));
 	}
 
 	/*
 	 * Ensures that one can commit the contents of a working copy on a read only
 	 * cu if a pessimistic repository provider allows it.
 	 */
 	public void testChangeContentOfReadOnlyCU2() throws CoreException {
 		String newContents = "package x.y;\n" + "public class A {\n" + "  public void bar() {\n" + "  }\n" + "}";
 		IResource resource = this.cu.getUnderlyingResource();
 		IProject project = resource.getProject();
 		boolean readOnlyFlag = isReadOnly(resource);
 		try {
 			RepositoryProvider.map(project, TestPessimisticProvider.NATURE_ID);
 			TestPessimisticProvider.markWritableOnSave = true;
 			setReadOnly(resource, true);
 			this.copy.getBuffer().setContents(newContents);
 			this.copy.commitWorkingCopy(true, null);
 			assertSourceEquals("Unexpected original cu contents", newContents, this.cu.getBuffer().getContents());
 		} finally {
 			TestPessimisticProvider.markWritableOnSave = false;
 			RepositoryProvider.unmap(project);
 			setReadOnly(resource, readOnlyFlag);
 		}
 	}
 	/**
 	 * Ensures that the primary cu can be retrieved.
 	 */
 	public void testGetPrimaryCU() {
 		IModelElement primary= this.copy.getPrimaryElement();
 		assertTrue("Element is not a cu", primary instanceof ISourceModule && !((ISourceModule)primary).isWorkingCopy());
 		assertTrue("Element should exist", primary.exists());
 	}	
 }
