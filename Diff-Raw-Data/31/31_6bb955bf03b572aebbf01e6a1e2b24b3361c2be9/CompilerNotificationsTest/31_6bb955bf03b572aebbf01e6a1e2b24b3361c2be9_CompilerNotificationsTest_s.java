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
 import java.io.IOException;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationMessageType;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;
 import org.eclipse.mylyn.docs.intent.client.ui.test.util.AnnotationUtils;
 import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
 import org.eclipse.mylyn.docs.intent.parser.test.utils.FileToStringConverter;
 
 /**
  * Ensures that compiler is correctly notified when updating, adding or removing modeling units and sections.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public class CompilerNotificationsTest extends AbstractIntentUITest {
 
 	private static final String COMPILER_SHOULD_NOT_DETECT_ISSUE_FAILURE_MESSAGE = "The compiler should not detect any issue";
 
 	private static final String ECLASS_NAME = "c1";
 
 	private static final String INTENT_DATA_FOLDER = "data/unit/documents/scenario/compilerNotifications/";
 
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
 		setUpIntentProject("intentProject", INTENT_DATA_FOLDER + "compilerNotifications01.intent", true);
 
 		// Step 2 : open an editor on the root document
 		editor = openIntentEditor();
 		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());
 		waitForAllOperationsInUIThread();
 	}
 
 	public void testCompilerIsNotifiedWhenModifyingMU() {
 		// Update Modeling Unit : make it pass
 		repositoryListener.clearPreviousEntries();
 		document.set(document.get().replace(ECLASS_NAME, "new EClass c1 {}"));
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertFalse(COMPILER_SHOULD_NOT_DETECT_ISSUE_FAILURE_MESSAGE, AnnotationUtils.hasIntentAnnotation(
 				editor, IntentAnnotationMessageType.COMPILER_ERROR, "", false));
 		// FIXME add this condition
 		// waitForCompiler(false);
 
 		// Update Modeling Unit : Add an error
 		repositoryListener.clearPreviousEntries();
 		document.set(document.get().replace("new EClass c1 {}", ECLASS_NAME));
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertTrue(COMPILER_SHOULD_NOT_DETECT_ISSUE_FAILURE_MESSAGE, AnnotationUtils.hasIntentAnnotation(
 				editor, IntentAnnotationMessageType.COMPILER_ERROR, "The Entity c1 cannot be resolved", true));
 		// FIXME add this condition
 		// waitForCompiler(false);
 	}
 
 	public void testCompilerIsNotifiedWhenRenamingSections() throws IOException {
 		// Renaming the section
 		repositoryListener.clearPreviousEntries();
 		document.set(document.get().replace("Section2", "RenamedSection"));
 
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertTrue("The compiler should still detect the issue", AnnotationUtils.hasIntentAnnotation(editor,
 				IntentAnnotationMessageType.COMPILER_ERROR, "The Entity c1 cannot be resolved", true));
 		// FIXME add this condition
 		// waitForCompiler(false);
 
 		// Renaming the section and change the issue
 		repositoryListener.clearPreviousEntries();
 		document.set(document.get().replace(ECLASS_NAME, "cRenamed"));
 
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertTrue("The compiler should still detect the issue", AnnotationUtils.hasIntentAnnotation(editor,
 				IntentAnnotationMessageType.COMPILER_ERROR, "The Entity cRenamed cannot be resolved", true));
 		// FIXME add this condition
 		// waitForCompiler(false);
 
 	}
 
 	public void testCompilerIsNotifiedWhenAddingNewMUInsideNewSections() throws IOException {
 		// Create a new modeling unit inside a new section fixing the compile issue
 		repositoryListener.clearPreviousEntries();
 		String newSection = FileToStringConverter.getFileAsString(new File(INTENT_DATA_FOLDER
 				+ "newSection01.intent"));
 		document.set(document.get().replace(IntentKeyWords.MODELING_UNIT_END,
 				IntentKeyWords.MODELING_UNIT_END + "\n" + newSection));
 
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertTrue("The compiler should detect a new issue", AnnotationUtils.hasIntentAnnotation(editor,
 				IntentAnnotationMessageType.COMPILER_ERROR, "The Entity EClass44 cannot be resolved", true));
 		// FIXME add this condition
 		// waitForCompiler(false);
 
 		// Create another new Modeling Unit : Add an error
 		repositoryListener.clearPreviousEntries();
		int beginIndex = document.get().lastIndexOf(IntentKeyWords.MODELING_UNIT_END);
 		int endIndex = document.get().lastIndexOf(IntentKeyWords.MODELING_UNIT_END);
 		document.set(document.get().substring(0, beginIndex) + document.get().substring(endIndex + 2));
 
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertFalse("The compiler should not detect any new issue", AnnotationUtils.hasIntentAnnotation(
 				editor, IntentAnnotationMessageType.COMPILER_ERROR, "The Entity EClass44 cannot be resolved",
 				true));
 		// FIXME add this condition
 		// waitForCompiler(false);
 	}
 
 	public void testCompilerIsNotifiedWhenRemovingMU() {
 		// Removing the modeling unit: no issue should be displayed any more
 		repositoryListener.clearPreviousEntries();
 		int beginIndex = document.get().indexOf("@M");
 		int endIndex = document.get().indexOf("M@");
 		document.set(document.get().substring(0, beginIndex) + document.get().substring(endIndex + 2));
 
 		editor.doSave(new NullProgressMonitor());
 		waitForCompiler(true);
 		assertFalse(COMPILER_SHOULD_NOT_DETECT_ISSUE_FAILURE_MESSAGE, AnnotationUtils.hasIntentAnnotation(
 				editor, IntentAnnotationMessageType.COMPILER_ERROR, "", false));
 		// FIXME add this condition
 		// waitForCompiler(false);
 
 	}
 
 }
