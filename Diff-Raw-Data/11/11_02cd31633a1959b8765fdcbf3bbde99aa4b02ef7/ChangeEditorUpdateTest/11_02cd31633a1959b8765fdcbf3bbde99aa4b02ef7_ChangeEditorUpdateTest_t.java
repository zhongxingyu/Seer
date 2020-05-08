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
 package org.eclipse.mylyn.docs.intent.client.ui.test.unit.compare;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractUITest;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
 
 /**
  * Tests the correct behavior of Intent editor changes. Ensures that EMF Compare is able to maintain order and
  * structure of the Intent document.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class ChangeEditorUpdateTest extends AbstractUITest {
 
 	private static final String FAILURE_MESSAGE = "Editor update dit not occur has expected";
 
 	private static final String PREFIX_SECTION_1_1 = "* an element defined in the right model is not matching any element in the left model (added element)";
 
 	private static final String PREFIX_SECTION_1_2 = "Otherwise, it is a deleted element.";
 
 	private static final String NEW_SECTION_1 = "\n\tSection {\n\t\tSubSection1\n\n\t\tMySubSection\n\t}";
 
 	private static final String NEW_SECTION_2 = "\n\tSection {\n\t\tSubSection2\n\n\t\tMySubSection\n\t}";
 
 	private static final String PREFIX_CHAPTER_1 = "Document {\n\tChapter {";
 
 	private static final String PREFIX_CHAPTER_2 = "\t}\n\tChapter {";
 
 	private static String INTENT_DOCUMENT_EXAMPLE_PATH = "data/unit/documents/editorupdates/changeEditorUpdateTest.intent";
 
 	private IntentSection section;
 
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
 		setUpIntentProject("intentProject", INTENT_DOCUMENT_EXAMPLE_PATH);
 		section = getIntentDocument().getChapters().iterator().next().getSubSections().iterator().next();
 
 	}
 
 	/**
 	 * Ensures that, when adding a new title to a chapter, when emf compare is used to update the repository
 	 * the chapter keeps its expected location.
 	 */
 	public void testAddTitleToExistingChapter() {
 		// Step 1 : opening an editor on the document
 		editor = openIntentEditor();
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 
 		// Step 2 : update section by adding 2 subsections textually
 		String documentContent = document.get();
 		String expectedDocumentContent = documentContent.replace(PREFIX_CHAPTER_1, PREFIX_CHAPTER_1
 				+ "\n\t\tMy Chapter Title");
 		document.set(expectedDocumentContent);
 
 		// Step 3 : save editor
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
		// Step 4 : checking that when reserializing the parsed document we obtain the expected text
 		String newDocumentContent = document.get();
 		assertEquals(FAILURE_MESSAGE, expectedDocumentContent, newDocumentContent);
 	}
 
 	/**
	 * Ensures that, when adding a new paragraph to a chapter containing no description unit, when emf compare
 	 * is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddParagraphToChapterWithoutDescriptionUnits() {
 		// Step 1 : opening an editor on the document
 		editor = openIntentEditor();
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 
 		// Step 2 : update section by adding 2 subsections textually
 		String documentContent = document.get();
 		String expectedDocumentContent = documentContent.replace(PREFIX_CHAPTER_2, PREFIX_CHAPTER_2
 				+ "\n\n\t\tA new description Unit");
 		document.set(expectedDocumentContent);
 
 		// Step 3 : save editor
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
		// Step 4 : checking that when reserializing the parsed document we obtain the expected text
 		String newDocumentContent = document.get();
 		assertEquals(FAILURE_MESSAGE, expectedDocumentContent, newDocumentContent);
 	}
 
 	/**
 	 * Ensures that when typing new sections inside the Intent Document, when emf compare is used to update
 	 * the repository the structure is respected.
 	 */
 	public void testSubSectionOrder() {
 		// Step 1 : opening an editor on a section
 		editor = openIntentEditor(section);
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 
 		// Step 2 : update section by adding 2 subsections textually
 		String documentContent = document.get();
 		String expectedDocumentContent = documentContent.replace(PREFIX_SECTION_1_1, PREFIX_SECTION_1_1
 				+ NEW_SECTION_1);
 		expectedDocumentContent = expectedDocumentContent.replace(PREFIX_SECTION_1_2, PREFIX_SECTION_1_2
 				+ NEW_SECTION_2);
 		document.set(expectedDocumentContent);
 
 		// Step 3 : save editor
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
		// Step 4 : checking that when reserializing the parsed section we obtain the expected text
 		String newDocumentContent = document.get();
 		assertEquals(FAILURE_MESSAGE, expectedDocumentContent, newDocumentContent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		if (editor != null) {
 			editor.close(false);
 		}
 		super.tearDown();
 	}
 }
