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
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
 
 /**
  * Tests the correct behavior of Intent editor changes. Ensures that EMF Compare is able to maintain order and
  * structure of the Intent document.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public class ChangeEditorUpdateTest extends AbstractIntentUITest {
 
 	private static final int FOCUS_ON_DOCUMENT = 3;
 
 	private static final String SAMPLE_MODELING_CONTENT = "\n\t\n\t@M\n\t\tSide {\n\t\t\tserializable = \"true\";\n\t\t}\n\tM@\n\t\n\n\t";
 
 	private static final String MIDDLE_OF_DESCRIPTION_UNIT = "@see \"TheUnmatchElementConcept\" ). T";
 
	private static final String A_NEW_DESCRIPTION_UNIT = "\n\n\tA new description Unit";
 
 	private static final String LINEBREAK_AND_INDENT = "\n\t";
 
 	private static final String FAILURE_MESSAGE = "Editor update dit not occur as";
 
 	private static final String PREFIX_SECTION_1_1 = "* an element defined in the right model is not matching any element in the left model (added element)\n\t\n\n\t";
 
 	private static final String PREFIX_SECTION_1_2 = "Otherwise, it is a deleted element.";
 
	private static final String NEW_SECTION_1 = "\n\tSection {\n\t\tSubSection1\n\n\t\tMySubSection\n\t}";
 
	private static final String NEW_SECTION_2 = "\n\tSection {\n\t\tSubSection2\n\n\t\tMySubSection\n\t}";
 
 	private static final String PREFIX_CHAPTER_1 = "Document {\n\tChapter {";
 
 	private static final String INTENT_DOCUMENT_EXAMPLE_PATH = "data/unit/documents/editorupdates/changeEditorUpdateTest.intent";
 
 	private static final String PREFIX_CHAPTER_2_FOCUS_DOCUMENT = "\t}\n\tChapter {";
 
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
 	 * Ensures that, when adding a new paragraph to a chapter containing no description unit, when emf compare
 	 * is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddParagraphToMiddleOfChapterWithoutDescriptionUnitsWithFocusOnChapter() {
 		// opening an editor on the second chapter
 		editor = openIntentEditor(getIntentChapter(2));
	
		genericUpdateTest("The 2.1 Section.\n\t}", A_NEW_DESCRIPTION_UNIT);
 	}
 
 	/**
 	 * Ensures that, when adding a new paragraph to a chapter containing no description unit, when emf compare
 	 * is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddParagraphToBeginningOfChapterWithoutDescriptionUnitsWithFocusOnChapter() {
 		// opening an editor on the second chapter
 		editor = openIntentEditor(getIntentChapter(2));
 
		genericUpdateTest("Chapter {", A_NEW_DESCRIPTION_UNIT);
 	}
 
 	/**
 	 * Ensures that, when adding a new paragraph to a chapter containing no description unit, when emf compare
 	 * is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddParagraphToBottomOfChapterWithoutDescriptionUnitsWithFocusOnChapter() {
 		// opening an editor on the second chapter
 		editor = openIntentEditor(getIntentChapter(2));
 
		genericUpdateTest("The 2.2 Section.\n\t}", A_NEW_DESCRIPTION_UNIT);
 	}
 
 	/**
 	 * Ensures that, when adding a new title to a chapter, when emf compare is used to update the repository
 	 * the chapter keeps its expected location.
 	 */
 	public void testAddTitleToExistingChapterWithFocusOnDocument() {
 		// Step 1 : opening an editor on the document
 		editor = openIntentEditor();
 
 		// getting the document associated to the opened editor
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 
 		// Step 2 : update section by adding 2 subsections textually
 		String documentContent = document.get();
 		String expectedDocumentContent = documentContent.replace(PREFIX_CHAPTER_1, PREFIX_CHAPTER_1
 				+ "\n\t\tMy Chapter Title");
 
 		// Step 3 : check merge
 		checkMerging(expectedDocumentContent);
 	}
 
 	/**
 	 * Ensures that, when adding a new paragraph to a chapter containing no description unit, when emf compare
 	 * is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddParagraphToChapterWithoutDescriptionUnitsWithFocusOnDocument() {
 		// opening an editor on the document
 		editor = openIntentEditor();
 
		genericUpdateTest(PREFIX_CHAPTER_2_FOCUS_DOCUMENT, "\n\n\t\tA new description Unit");
 	}
 
 	/**
 	 * Ensures that, when adding a new modeling unit in the middle of an existing description unit, when emf
 	 * compare is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddModelingUnitBySplittingADescriptionUnitWithFocusOnDocument() {
 		// opening an editor on the document
 		editor = openIntentEditor();
 
 		genericUpdateTest(MIDDLE_OF_DESCRIPTION_UNIT, SAMPLE_MODELING_CONTENT, FOCUS_ON_DOCUMENT);
 	}
 
 	/**
 	 * Ensures that, when adding a new modeling unit in the middle of an existing description unit, when emf
 	 * compare is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddModelingUnitBySplittingADescriptionUnitWithFocusOnChapter() {
 		// opening an editor on the chapter
 		editor = openIntentEditor(getIntentChapter(1));
 
 		genericUpdateTest(MIDDLE_OF_DESCRIPTION_UNIT, SAMPLE_MODELING_CONTENT, 2);
 	}
 
 	/**
 	 * Ensures that, when adding a new modeling unit in the middle of an existing description unit, when emf
 	 * compare is used to update the repository the chapter keeps its expected location and structure.
 	 */
 	public void testAddModelingUnitBySplittingADescriptionUnitWithFocusOnSection() {
 		// opening an editor on the section
 		editor = openIntentEditor(getIntentSection(1, 1));
 
 		genericUpdateTest(MIDDLE_OF_DESCRIPTION_UNIT, SAMPLE_MODELING_CONTENT, 1);
 	}
 
 	/**
 	 * Ensures that when typing new sections inside the Intent Document, when emf compare is used to update
 	 * the repository the structure is respected.
 	 */
 	public void testSubSectionOrderWithFocusOnDocument() {
 		// opening an editor on a section
 		editor = openIntentEditor();
 
 		genericTestSubSectionOrderWithFocusOnDocuments(3);
 	}
 
 	/**
 	 * Ensures that when typing new sections inside the Intent Document, when emf compare is used to update
 	 * the repository the structure is respected.
 	 */
 	public void testSubSectionOrderWithFocusOnSection() {
 		// opening an editor on a section
 		editor = openIntentEditor(section);
 
 		genericTestSubSectionOrderWithFocusOnDocuments(1);
 	}
 
 	/**
 	 * <ul>
 	 * <li>Replaces the given prefix by prefix + contentToAdd in the current Document</li>
 	 * <li>Saves the editor</li>
 	 * <li>after merging with the repository content and serializing, check that the editor now displays the
 	 * expected document content.
 	 * <li>
 	 * </ul>
 	 * 
 	 * @param contentToAdd
 	 *            the string corresponding to the new content to add
 	 * @param prefix
 	 *            the prefix to use for identifying the chapter to modify
 	 */
 	private void genericUpdateTest(String prefix, String contentToAdd) {
 		genericUpdateTest(prefix, contentToAdd, -1);
 	}
 
 	/**
 	 * <ul>
 	 * <li>Replaces the given prefix by prefix + contentToAdd in the current Document</li>
 	 * <li>Saves the editor</li>
 	 * <li>after merging with the repository content and serializing, check that the editor now displays the
 	 * expected document content.
 	 * <li>
 	 * </ul>
 	 * 
 	 * @param contentToAdd
 	 *            the string corresponding to the new content to add
 	 * @param prefix
 	 *            the prefix to use for identifying the chapter to modify
 	 * @param focusLevel
 	 *            the focus level
 	 */
 	private void genericUpdateTest(String prefix, String contentToAdd, int focusLevel) {
 		// Step 1 : getting the document associated to the opened editor
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 
 		// Step 2 : update the document
 		// Step 2.1 : including focus level
 		String initialContent = document.get();
 		String indent = LINEBREAK_AND_INDENT;
 		for (int i = 1; i < focusLevel; i++) {
 			indent += "\t";
 		}
 		String updatedPrefix = prefix.replace(LINEBREAK_AND_INDENT, indent);
 		String updatedContentToAdd = contentToAdd.replace(LINEBREAK_AND_INDENT, indent);
 
 		// Step 2.2 : change the document
 		String expectedDocumentContent = initialContent.replace(updatedPrefix, updatedPrefix
 				+ updatedContentToAdd);
 
 		// Step 3 : check merge
 		checkMerging(expectedDocumentContent);
 
 	}
 
 	/**
 	 * Ensures that when typing new sections inside the Intent Document, when emf compare is used to update
 	 * the repository the structure is respected.
 	 */
 	private void genericTestSubSectionOrderWithFocusOnDocuments(int focusLevel) {
 		// Step 1 : get the content of the editor
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 
 		// Step 2 : update section by adding 2 subsections textually
 		String initialContent = document.get();
 
 		String indent = LINEBREAK_AND_INDENT;
 		for (int i = 1; i < focusLevel; i++) {
 			indent += "\t";
 		}
 		String prefixSection1WithFocusLevel = PREFIX_SECTION_1_1.replace(LINEBREAK_AND_INDENT, indent);
 		String prefixSection2WithFocusLevel = PREFIX_SECTION_1_2.replace(LINEBREAK_AND_INDENT, indent);
 		String replacementWithFocusLevel1 = NEW_SECTION_1.replace(LINEBREAK_AND_INDENT, indent);
 		String replacementWithFocusLevel2 = NEW_SECTION_2.replace(LINEBREAK_AND_INDENT, indent);
 		String expectedDocumentContent = initialContent.replace(prefixSection1WithFocusLevel,
 				prefixSection1WithFocusLevel + replacementWithFocusLevel1);
 		assertFalse(
 				"No change made on the document. You should check that the test has been correctly written",
 				initialContent.equals(expectedDocumentContent));
 		expectedDocumentContent = expectedDocumentContent.replace(prefixSection2WithFocusLevel,
 				prefixSection2WithFocusLevel + replacementWithFocusLevel2);
 
 		// Step 3 : check merge
 		checkMerging(expectedDocumentContent);
 	}
 
 	/**
 	 * Ensures that when setting the given document content and then save the editor, after merging with the
 	 * repository content and serializing, the editor now displays the expected document content.
 	 * 
 	 * @param expectedDocumentContent
 	 *            the excepted document content
 	 */
 	private void checkMerging(String expectedDocumentContent) {
 		// Step 1 : we ensure that the initial content is different from expected content
 		String initialContent = document.get();
 		assertFalse(
 				"No change made on the document. You should check that the test has been correctly written",
 				initialContent.equals(expectedDocumentContent));
 
 		// Step 2 : setting the new content
 		document.set(expectedDocumentContent);
 
 		// Step 3 : save editor
 		editor.doSave(new NullProgressMonitor());
 		waitForAllOperationsInUIThread();
 
 		// Step 4 : checking that when reserializing the parsed document we obtain the expected text
 		String newDocumentContent = document.get();
 		assertEquals(FAILURE_MESSAGE, expectedDocumentContent, newDocumentContent);
 	}
 
 }
