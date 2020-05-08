 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.scenario;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.ToggleNatureAction;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.parser.utils.FileToStringConverter;
 import org.eclipse.mylyn.docs.intent.serializer.IntentSerializer;
 
 /**
  * Ensures that when reopening Intent projects after modifications, there are no lost of contents.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class IntentProjectReopeningTest extends AbstractIntentUITest {
 
 	private static final String DOCUMENTS_FOLDER_PATH = "data/unit/documents/scenario/projectReopening/";
 
 	private IntentEditor editor;
 
 	private IntentEditorDocument document;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// Step 1 : Generic set up
 		setUpIntentProject("intentProject", INTENT_EMPTY_DOC_PATH, true);
 
 		// Step 2 : open an editor on the root document
 		editor = openIntentEditor();
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 	}
 
 	public void testProjectReopeningWithSection() throws Exception {
 		String initalContent = FileToStringConverter.getFileAsString(new File(DOCUMENTS_FOLDER_PATH
 				+ "projectReopening01.intent"));
 		String newContent = initalContent.replace("Title", "A");
 		doTestProjectReopening(initalContent, newContent);
 	}
 
 	public void testProjectReopeningWithModelingUnitCreation() throws Exception {
 		String initalContent = FileToStringConverter.getFileAsString(new File(DOCUMENTS_FOLDER_PATH
 				+ "projectReopening02.intent"));
 		String newContent = initalContent.replace("Title", "A");
 		doTestProjectReopening(initalContent, newContent);
 	}
 
 	protected void doTestProjectReopening(String initalContent, String newContent) throws CoreException {
 		document.set(initalContent);
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
 		document.set(newContent);
 		repositoryListener.clearPreviousEntries();
 		editor.doSave(new NullProgressMonitor());
 		waitForIndexer();
 		waitForAllOperationsInUIThread();
 
 		IntentDocument newDocument = reopenProjectAndGetDocument();
 		assertEquals("When reopening Intent Project, some content was lost ", newContent,
 				new IntentSerializer().serialize(newDocument));
 	}
 
 	private IntentDocument reopenProjectAndGetDocument() throws CoreException {
		editor.doSave(new NullProgressMonitor());
 		ToggleNatureAction.toggleNature(intentProject);
 		waitForAllOperationsInUIThread();
 		intentProject.close(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 		intentProject.open(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 		ResourceSet rs = new ResourceSetImpl();
 		IntentDocument newDocument = null;
 		URI documentURI = URI.createURI("platform:/resource/" + intentProject.getName() + "/.repository/"
 				+ IntentLocations.INTENT_INDEX + ".xmi");
 		Resource documentResource = rs.getResource(documentURI, true);
 		if (documentResource != null && documentResource.getContents().iterator().hasNext()
 				&& documentResource.getContents().iterator().next() instanceof IntentStructuredElement) {
 			if (documentResource.getContents().iterator().next() instanceof IntentDocument) {
 				EcoreUtil.resolveAll(documentResource.getResourceSet());
 				newDocument = (IntentDocument)documentResource.getContents().iterator().next();
 			}
 		}
 		return newDocument;
 	}
 }
