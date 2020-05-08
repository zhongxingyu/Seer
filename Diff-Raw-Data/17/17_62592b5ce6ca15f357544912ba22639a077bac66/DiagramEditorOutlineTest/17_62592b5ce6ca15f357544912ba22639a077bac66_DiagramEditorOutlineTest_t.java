 /*
  * Copyright (c) 2007, 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    bblajer - initial API and implementation
  */
 package org.eclipse.gmf.tests.lite.gef;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.NodeEditPart;
 import org.eclipse.gef.RequestConstants;
 import org.eclipse.gef.requests.GroupRequest;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.runtime.notation.Node;
 import org.eclipse.gmf.tests.gef.AbstractDiagramEditorTest;
 import org.eclipse.gmf.tests.lite.setup.LibraryConstrainedSetup;
 import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 
 public class DiagramEditorOutlineTest extends AbstractDiagramEditorTest {

 	public DiagramEditorOutlineTest(String name) {
 		super(name);
 		myDefaultSetup = LibraryConstrainedSetup.getInstance();
 	}
 
 	@Override
 	protected IEditorPart openEditor(IFile diagramFile) {
 		IEditorPart result = super.openEditor(diagramFile);
 		try {
			assertNotNull(result.getAdapter(IContentOutlinePage.class));
			final IWorkbenchPage wp = result.getSite().getPage();
			IViewPart introPart = wp.findView("org.eclipse.ui.internal.introview");
			if (introPart != null) {
				// with the fix of [168524] PartService no longer sends partActivated
				// for invisible editors (or, rather delays event till the control becomes visible)
				// so we need to make sure editor area is visible by hiding intro view
				wp.hideView(introPart);
			}
			wp.showView("org.eclipse.ui.views.ContentOutline");
 		} catch (PartInitException e) {
 			fail("Exception occurred while opening outline view: " + e.getMessage());
 		}
 		return result;
 	}
 
 	/**
 	 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=176362
 	 */
 	public void testDeleteElementViaOutline() throws Exception {
 		GenNode metaBook = getSetup().getGenModel().getNodeB();
 		Node book1Node = createNode(metaBook, getDiagram());
 		EObject book1 = book1Node.getElement();
 		assertNotNull("book is not contained within a resource", book1.eResource());
 		NodeEditPart book1EP = (NodeEditPart) findEditPart(book1Node);
 		EditPart treeEditPart = (EditPart) book1EP.getAdapter(org.eclipse.gef.TreeEditPart.class);
 		GroupRequest deleteRequest = new GroupRequest(RequestConstants.REQ_DELETE);
 		deleteRequest.setEditParts(treeEditPart);
 		org.eclipse.gef.commands.Command command = treeEditPart.getCommand(deleteRequest);
 		assertNotNull(command);
 		assertTrue(command.canExecute());
 		execute(command);
 		assertNull("Command obtained via outline has not executed correctly", book1.eResource());
 	}
 }
