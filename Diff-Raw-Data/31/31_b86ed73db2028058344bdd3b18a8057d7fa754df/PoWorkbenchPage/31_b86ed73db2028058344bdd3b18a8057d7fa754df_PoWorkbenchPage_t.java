 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *    Bug 336488 - DiagramEditor API
 *    mwenz - Bug 374918 - Let default paste use LocalSelectionTransfer
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.bot.pageobjects;
 
 import static org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable.syncExec;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.graphiti.bot.tests.AbstractGFTests;
 import org.eclipse.graphiti.bot.tests.GFOtherTests;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
 import org.eclipse.graphiti.ui.services.GraphitiUi;
 import org.eclipse.swt.SWT;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
 import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotPerspective;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
 import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
 import org.eclipse.swtbot.swt.finder.results.IntResult;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 
 /**
  * Encapsulates SWTBot for operations on the active workbench page.
  *
  */
 public class PoWorkbenchPage extends PageObject{
 	
 	/**
 	 * @return
 	 */
 	public SWTBotGefEditor getGefEditor() {
 		SWTBotEditor activeEditor = bot.activeEditor();
 		String title = activeEditor.getTitle();
 		SWTBotGefEditor ed = bot.gefEditor(title);
 		return ed;
 	}
 	
 	public int openDiagramEditorFromFile(final IFile diagFile) {
 		return syncExec(new IntResult() {
 			public Integer run() {
 				try {
 					IEditorPart ed = IDE.openEditor(getWorkbenchPage(), diagFile, org.eclipse.graphiti.ui.editor.DiagramEditor.DIAGRAM_EDITOR_ID);
 					GFOtherTests.assertTrue("Editor must be a diagram editor: " + ed, ed instanceof DiagramEditor);
 				} catch (PartInitException e) {
 					GFOtherTests.fail(e.getMessage());
 				}
 				return getWorkbenchPage().getEditorReferences().length;
 			}
 		});
 	}
 	
 	public int openDiagramEditorFromObject(final Diagram diagram) {
 		Resource resource = diagram.eResource();
 		assertNotNull(resource);
 
 		return syncExec(new IntResult() {
 			public Integer run() {
 				IWorkbenchPage page = getWorkbenchPage();
 				try {
 					IEditorInput input = DiagramEditorInput.createEditorInput(diagram, GraphitiUi.getExtensionManager()
 							.getDiagramTypeProviderId(diagram.getDiagramTypeId()));
 					IEditorPart ed = IDE.openEditor(page, input, DiagramEditor.DIAGRAM_EDITOR_ID);
 					assertTrue("Editor must be a diagram editor: " + ed, ed instanceof DiagramEditor);
 				} catch (PartInitException e) {
 					fail(e.getMessage());
 				}
 				return page.getEditorReferences().length;
 			}
 		});
 	}
 	
 	private IWorkbenchPage getWorkbenchPage() {
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		return page;
 	}
 	
 	public void shutdownEditor(final SWTBotGefEditor diagramEditor) {
 		// Close popups.
 		syncExec(new VoidResult() {
 			public void run() {
 				// Using SWTBot yields an exception since a keyboard layout file for DE is not available.
 				//				bot.activeShell().pressShortcut(SWT.NONE, SWT.ESC);
 				try {
 					Robot r = new Robot();
 					r.keyPress(SWT.ESC);
 					r.keyRelease(SWT.ESC);
 				} catch (AWTException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		diagramEditor.saveAndClose();
 	}
 	
 	public void shutdownEditor(final DiagramEditor diagramEditor) {
 		// Close popups.
 		syncExec(new VoidResult() {
 			public void run() {
 				// Using SWTBot yields an exception since a keyboard layout file for DE is not available.
 				//				bot.activeShell().pressShortcut(SWT.NONE, SWT.ESC);
 				try {
 					Robot r = new Robot();
 					r.keyPress(SWT.ESC);
 					r.keyRelease(SWT.ESC);
 				} catch (AWTException e) {
 					e.printStackTrace();
 				}
 				diagramEditor.doSave(new NullProgressMonitor());
 				bot.editorByTitle(diagramEditor.getTitle()).close();
 			}
 		});
 	}
 	
 	public void closeAllEditors() {
 		List<? extends SWTBotEditor> editors = bot.editors();
 		for (SWTBotEditor editor : editors) {
 			editor.close();
 		}
 	}
 	
 	public void closeActiveEditor(){
 		SWTBotEditor activeEditor = bot.activeEditor();
 		activeEditor.close();
 	}
 
 	public void openGraphitiTestPerspective() {
 		syncExec(new VoidResult() {
 			public void run() {
 				SWTBotPerspective p = bot.perspectiveById("org.eclipse.graphiti.examples.common.perspective.GFPerspective");
 				p.activate();
 				bot.activeShell().widget.setMaximized(true);
 			}
 		});
 	}
 
 	public void closeWelcomeView(AbstractGFTests abstractGFTests) {
 		try {
 			bot.viewByTitle("Welcome").close();
 		} catch (WidgetNotFoundException e) {
 			// do nothing
 		}
 	}
 
 }
