 /*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.compare.tests;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 
 import junit.framework.TestCase;
 
 import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.patch.PatchMessages;
 import org.eclipse.compare.internal.patch.PatchWizard;
 import org.eclipse.compare.internal.patch.PatchWizardDialog;
 import org.eclipse.core.internal.resources.WorkspaceRoot;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.TreeSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
 
 public class PatchUITest extends TestCase {
 
 	private static final String TEST_PROJECT = "ApplyPatchTest";
 
 	private IWorkspaceRoot workspaceRoot = null;
 	private IProject testProject = null;
 
 	private PatchWizardDialog wizardDialog = null;
 	private PatchWizard wizard = null;
 
 	public PatchUITest(String name) {
 		super(name);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		testProject = workspaceRoot.getProject(TEST_PROJECT);
 		testProject.create(null);
 		testProject.open(null);
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		testProject.delete(true, null);
 	}
 
 	public void testApplyClipboardPatch() throws CoreException {
 		// Clipboard support on Mac OS is not reliable when tests are run
 		// through an SSH session, see bug 272870 for details
 		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 			return;
 		}
 
 		copyIntoClipboard("patch_context0.txt");
 		copyIntoWorkspace("context.txt");
 
 		openPatchWizard();
 		assertTrue(wizard.getPageCount() == 3);
 		IWizardPage patchWizardPage = wizard.getPages()[0];
 
 		assertTrue(patchWizardPage.canFlipToNextPage());
 
 		callMethod(wizardDialog, "nextPressed", new Object[] {});
 
 		processQueuedEvents();
 		assertTrue(wizard.canFinish());
 		wizard.performFinish();
 		wizardDialog.close();
 
 		InputStream expected = PatchUtils.asInputStream("exp_context.txt");
 		InputStream actual = testProject.getFile("context.txt").getContents();
 		compareStreams(expected, actual);
 	}
 
 	public void testApplyWorkspacePatch() throws CoreException {
 		copyIntoWorkspace("patch_addition.txt");
 
 		openPatchWizard();
 		assertTrue(wizard.getPageCount() == 3);
 		IWizardPage patchWizardPage = wizard.getPages()[0];
 
 		getButton(patchWizardPage, "fUseClipboardButton").setSelection(false);
 		getButton(patchWizardPage, "fUsePatchFileButton").setSelection(false);
 		getButton(patchWizardPage, "fUseWorkspaceButton").setSelection(true);
 
 		TreeViewer tree = getTreeViewer(patchWizardPage, "fTreeViewer");
 		treeSelect(tree, TEST_PROJECT + "/patch_addition.txt");
 
 		processQueuedEvents();
 		assertTrue(patchWizardPage.canFlipToNextPage());
 		callMethod(wizardDialog, "nextPressed", new Object[] {});
 
 		assertTrue(wizard.canFinish());
 		wizard.performFinish();
 		wizardDialog.close();
 
 		InputStream expected = PatchUtils.asInputStream("exp_addition.txt");
 		InputStream actual = testProject.getFile("exp_addition.txt")
 				.getContents();
 		compareStreams(expected, actual);
 	}
 
 	private void openPatchWizard() {
		ImageDescriptor patchWizardImage = CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.png");
		String patchWizardTitle = PatchMessages.PatchWizard_title;
		
 		IStorage patch = null;
 		IResource target = null;
 		CompareConfiguration configuration = new CompareConfiguration();
 
 		wizard = new PatchWizard(patch, target, configuration);
 		if (patchWizardImage != null)
 			wizard.setDefaultPageImageDescriptor(patchWizardImage);
 		if (patchWizardTitle != null)
 			wizard.setWindowTitle(patchWizardTitle);
 		wizard.setNeedsProgressMonitor(true);
 
 		wizardDialog = new PatchWizardDialog(getShell(), wizard);
 		wizardDialog.setBlockOnOpen(false);
 		wizardDialog.open();
 	}
 
 	private void copyIntoClipboard(String name) {
 		Clipboard clipboard = new Clipboard(getShell().getDisplay());
 		InputStream patchIS = PatchUtils.asInputStream(name);
 		String patch = null;
 		try {
 			patch = PatchUtils.asString(patchIS);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		TextTransfer textTransfer = TextTransfer.getInstance();
 		Transfer[] transfers = new Transfer[] { textTransfer };
 		Object[] data = new Object[] { patch };
 		clipboard.setContents(data, transfers);
 		clipboard.dispose();
 	}
 
 	private void copyIntoWorkspace(String name) {
 		IFile file = testProject.getFile(name);
 		InputStream is = PatchUtils.asInputStream(name);
 		try {
 			file.create(is, true, null);
 		} catch (CoreException e) {
 			fail(e.getMessage());
 		}
 	}
 
 	private void compareStreams(InputStream expectedIS, InputStream actualIS) {
 		String expected = null;
 		String actual = null;
 		try {
 			expected = PatchUtils.asString(expectedIS);
 			actual = PatchUtils.asString(actualIS);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		assertEquals(expected, actual);
 	}
 
 	private void treeSelect(TreeViewer tree, String path) {
 		WorkspaceRoot root = (WorkspaceRoot) tree.getInput();
 		IFile file = root.getFile(path);
 		TreePath treePath = new TreePath(new Object[] { file });
 		TreeSelection sel = new TreeSelection(treePath);
 		tree.setSelection(sel);
 	}
 
 	private Button getButton(Object object, String name) {
 		return (Button) getField(object, name);
 	}
 
 	private TreeViewer getTreeViewer(Object object, String name) {
 		return (TreeViewer) getField(object, name);
 	}
 
 	private Object getField(Object object, String name) {
 		Object ret = null;
 		try {
 			ret = ReflectionUtils.getField(object, name);
 		} catch (IllegalArgumentException e) {
 			fail(e.getMessage());
 		} catch (SecurityException e) {
 			fail(e.getMessage());
 		} catch (IllegalAccessException e) {
 			fail(e.getMessage());
 		} catch (NoSuchFieldException e) {
 			fail(e.getMessage());
 		}
 		return ret;
 	}
 
 	private Object callMethod(Object object, String name, Object args[]) {
 		Object ret = null;
 		try {
 			ret = ReflectionUtils.callMethod(object, name, args);
 		} catch (IllegalArgumentException e) {
 			fail(e.getMessage());
 		} catch (IllegalAccessException e) {
 			fail(e.getMessage());
 		} catch (InvocationTargetException e) {
 			fail(e.getMessage());
 		} catch (NoSuchMethodException e) {
 			fail(e.getMessage());
 		}
 		return ret;
 	}
 
 	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 	}
 
 	private void processQueuedEvents() {
 		while (Display.getCurrent().readAndDispatch()) {
 			// Process all the events in the queue
 		}
 	}
 
 }
