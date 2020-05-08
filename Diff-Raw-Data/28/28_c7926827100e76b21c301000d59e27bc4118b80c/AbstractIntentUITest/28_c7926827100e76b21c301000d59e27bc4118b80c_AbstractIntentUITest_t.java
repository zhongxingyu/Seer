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
 package org.eclipse.mylyn.docs.intent.client.ui.test.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotation;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationMessageType;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentNature;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.ToggleNatureAction;
 import org.eclipse.mylyn.docs.intent.client.ui.preferences.IntentPreferenceConstants;
 import org.eclipse.mylyn.docs.intent.client.ui.utils.IntentEditorOpener;
 import org.eclipse.mylyn.docs.intent.collab.common.query.IntentDocumentQuery;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryInitializer;
 import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.core.document.IntentChapter;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.parser.IntentParser;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.ParseException;
 import org.eclipse.mylyn.docs.intent.parser.test.utils.FileToStringConverter;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * An abstract test class providing API for managing an Intent IDE projects and editors.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public abstract class AbstractIntentUITest extends TestCase implements ILogListener {
 	public static final String INTENT_NEW_PROJECT_WIZARD_ID = "org.eclipse.mylyn.docs.intent.client.ui.ide.wizards.NewIntentProjectWizard";
 
 	protected static final String INTENT_EMPTY_DOC_PATH = "data/unit/documents/empty.intent";
 
 	private static final int WAITING_DELAY_MILLIS = 500;
 
 	protected IProject intentProject;
 
 	protected Repository repository;
 
 	protected RepositoryAdapter repositoryAdapter;
 
 	protected RepositoryListenerForTests repositoryListener;
 
 	private IntentDocument intentDocument;
 
 	private List<IntentEditor> openedEditors;
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		initClassLoader();
 
 		// Step 1 : printing testclass name (make hudson debugs easier)
 		for (int i = 0; i < getClass().getName().length() - 1; i++) {
 			System.out.print("=");
 		}
 		System.out.println("=");
 		System.out.println(getClass().getName());
 		super.setUp();
 
 		// Step 2 : deactivating the automatic build of the workspace (if needed)
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IWorkspaceDescription description = workspace.getDescription();
 		if (description.isAutoBuilding()) {
 			description.setAutoBuilding(false);
 			workspace.setDescription(description);
 		}
 
 		// Step 3 : clean workspace, close welcome page
 		WorkspaceUtils.cleanWorkspace();
 		WorkspaceUtils.closeWelcomePage();
 		waitForAllOperationsInUIThread();
 		IntentEditorActivator.getDefault().getLog().addLogListener(this);
 
 		openedEditors = new ArrayList<IntentEditor>();
 	}
 
 	/**
 	 * Forces OSGI to load ui.ide plugin first. Otherwise, IntentRepositoryManager.INSTANCE.getRepository is
 	 * called twice at the same time (?). Happens only in the test suite.
 	 */
 	private void initClassLoader() {
 		assertNotNull(IntentNature.class);
 	}
 
 	private void traceHeapSize() {
 		long maxHeapSize = Runtime.getRuntime().maxMemory();
 		long allocatedHeapSize = Runtime.getRuntime().totalMemory();
 		long usedHeap = allocatedHeapSize - Runtime.getRuntime().freeMemory();
 
 		System.out.println(" Heap size : " + usedHeap + "/" + allocatedHeapSize + "("
 				+ Math.ceil(usedHeap * 100 / allocatedHeapSize) + "% - "
 				+ Math.ceil(usedHeap * 100 / maxHeapSize) + "% of max heap size)");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		waitForAllOperationsInUIThread();
 		// Step 1: close editors
 		for (IntentEditor editor : openedEditors) {
 			editor.close(false);
 		}
 
 		// Step 2: clean workspace
 		if (intentProject != null) {
 			IntentRepositoryManager.INSTANCE.deleteRepository(intentProject.getName());
 			waitForAllOperationsInUIThread();
 
 			intentProject.delete(true, true, new NullProgressMonitor());
 		}
 		IntentEditorActivator.getDefault().getLog().removeLogListener(this);
 		WorkspaceUtils.cleanWorkspace();
 
 		// Step 3: unregister repository listener and deactivate advance loggin
 		if (repositoryListener != null) {
 			Platform.removeLogListener(repositoryListener);
 		}
 		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(IntentEditorActivator.getDefault()
 				.getBundle().getSymbolicName());
 		node.putBoolean(IntentPreferenceConstants.ACTIVATE_ADVANCE_LOGGING, false);
 
 		// Step 4: setting all fields to null (to avoid memory leaks)
 		setAllFieldsToNull();
 
 		super.tearDown();
 	}
 
 	/**
 	 * Creates and register an new {@link org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient } in
 	 * charge of detecting that events happened on the repository.
 	 */
 	protected final void registerRepositoryListener() {
 		this.repositoryListener = new RepositoryListenerForTests();
 		Platform.addLogListener(repositoryListener);
 	}
 
 	/**
 	 * Creates a new empty Intent project.
 	 * 
 	 * @param projectName
 	 *            the intent project name
 	 */
 	protected final void setUpIntentProject(final String projectName) {
 		setUpIntentProject(projectName, INTENT_EMPTY_DOC_PATH, false);
 	}
 
 	/**
 	 * Creates a new Intent project using the intent document located at the given path.
 	 * 
 	 * @param projectName
 	 *            the intent project name
 	 * @param intentDocumentPath
 	 *            the path of the intent document to use (relative to the
 	 *            org.eclipse.mylyn.docs.intent.client.ui.test project).
 	 */
 	protected final void setUpIntentProject(final String projectName, String intentDocumentPath) {
 		setUpIntentProject(projectName, intentDocumentPath, false);
 	}
 
 	/**
 	 * Creates a new Intent project using the intent document located at the given path.
 	 * 
 	 * @param projectName
 	 *            the intent project name
 	 * @param intentDocumentPath
 	 *            the path of the intent document to use (relative to the
 	 *            org.eclipse.mylyn.docs.intent.client.ui.test project)
 	 * @param listenForRepository
 	 *            indicates whether a repository listener should be registered. If you want to determine if a
 	 *            client has done a job (by calling {@link AbstractIntentUITest#waitForIndexer() for example}
 	 *            ), this must be true.
 	 */
 	protected void setUpIntentProject(final String projectName, String intentDocumentPath,
 			boolean listenForRepository) {
 		try {
 			// Step 1: getting the content of the intent document located at the given path.
 			File file = new File(intentDocumentPath);
 			final String intentDocumentContent = FileToStringConverter.getFileAsString(file);
 
 			// Step 2: creating the intent project
 			doCreateIntentProject(projectName, intentDocumentContent);
 
 			while (repositoryAdapter == null) {
 				Thread.sleep(10);
 			}
 
 			// Step 3: registering the repository listener
 			registerRepositoryListener();
 			repositoryListener.clearPreviousEntries();
 
 			// Step 4: additional setup operations
 			additionalSetUpOperations();
 
 		} catch (CoreException e) {
 			AssertionFailedError error = new AssertionFailedError("Failed to create Intent project");
 			error.setStackTrace(e.getStackTrace());
 			throw error;
 		} catch (IOException e) {
 			AssertionFailedError error = new AssertionFailedError(
 					"Failed to get content of intent document '" + intentDocumentPath + "'");
 			error.setStackTrace(e.getStackTrace());
 			throw error;
 		} catch (InterruptedException e) {
 			AssertionFailedError error = new AssertionFailedError("Failed to create Intent project");
 			error.setStackTrace(e.getStackTrace());
 			throw error;
 		}
 	}
 
 	/**
 	 * Creates the intent project with the given name, with the given content.
 	 * 
 	 * @param projectName
 	 *            the name of the project to create
 	 * @param intentDocumentContent
 	 *            the content to associate to the created intent document
 	 * @throws CoreException
 	 *             if the project cannot be created properly
 	 */
 	protected void doCreateIntentProject(final String projectName, final String intentDocumentContent)
 			throws CoreException {
 		IWorkspaceRunnable create = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				IProject project = WorkspaceUtils.createProject(projectName, monitor);
 				ToggleNatureAction.toggleNature(project);
 
 				IntentRepositoryInitializer.initializeContent(projectName, intentDocumentContent);
 
 				// Step 3 : initializing all useful informations
 				intentProject = project;
 				setUpRepository(project);
 			}
 		};
 		ResourcesPlugin.getWorkspace().run(create, null);
 	}
 
 	/**
 	 * Set up the repository for the given project.
 	 * 
 	 * @param project
 	 *            the project
 	 */
 	protected final void setUpRepository(IProject project) {
 		assertNotNull(project);
 		try {
 			repository = IntentRepositoryManager.INSTANCE.getRepository(project.getName());
 			assertNotNull(repository);
 			repositoryAdapter = repository.createRepositoryAdapter();
 		} catch (RepositoryConnectionException e) {
 			AssertionFailedError error = new AssertionFailedError(
 					"Cannot connect to the created IntentRepository");
 			error.setStackTrace(e.getStackTrace());
 			throw error;
 		} catch (CoreException e) {
 			AssertionFailedError error = new AssertionFailedError(
 					"Cannot retrieve the correct IntentRepository type");
 			error.setStackTrace(e.getStackTrace());
 			throw error;
 		}
 		// wait for initialization completed
 		waitForAllOperationsInUIThread();
 	}
 
 	// /**
 	// * Loads the {@link IntentStructuredElement} located at the given path. If it contains an
 	// IntentDocument,
 	// * also updates the intentDocument field.
 	// *
 	// * @param intentDocumentModelPath
 	// * the path of the Intent document model (from
 	// * org.eclipse.mylyn.docs.intent.client.ui.test/data)
 	// * @return the loaded {@link IntentStructuredElement}
 	// */
 	// protected IntentStructuredElement loadIntentDocumentFromTests(String intentDocumentModelPath) {
 	// ResourceSet rs = new ResourceSetImpl();
 	// URI documentURI = URI.createURI("platform:/plugin/org.eclipse.mylyn.docs.intent.client.ui.test/data/"
 	// + intentDocumentModelPath);
 	// Resource documentResource = rs.getResource(documentURI, true);
 	// if (documentResource != null && documentResource.getContents().iterator().hasNext()
 	// && documentResource.getContents().iterator().next() instanceof IntentStructuredElement) {
 	// if (documentResource.getContents().iterator().next() instanceof IntentDocument) {
 	// intentDocument = (IntentDocument)documentResource.getContents().iterator().next();
 	// }
 	// return (IntentStructuredElement)documentResource.getContents().iterator().next();
 	// }
 	// throw new AssertionFailedError("Could not load Intent model at " + intentDocumentModelPath);
 	// }
 
 	/**
 	 * Parses the {@link IntentStructuredElement} located at the given path. If it contains an IntentDocument,
 	 * also updates the intentDocument field.
 	 * 
 	 * @param intentDocumentModelPath
 	 *            the path of the Intent document model (from
 	 *            org.eclipse.mylyn.docs.intent.client.ui.test/data)
 	 * @return the loaded {@link IntentStructuredElement}
 	 */
 	protected IntentDocument parseIntentDocumentFromTests(String intentDocumentModelPath) {
 		EObject res = null;
 		try {
 			res = new IntentParser().parse(FileToStringConverter.getFileAsString(new File(
 					intentDocumentModelPath)));
 		} catch (ParseException e) {
 			fail(e.getMessage());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		if (res instanceof IntentDocument) {
 			intentDocument = (IntentDocument)res;
 		}
 		return intentDocument;
 	}
 
 	/**
 	 * Returns the intentDocument associated to the current Intent project.
 	 * 
 	 * @return the intentDocument associated to the current Intent project
 	 */
 	protected final IntentDocument getIntentDocument() {
 		if (intentDocument == null) {
 			intentDocument = new IntentDocumentQuery(repositoryAdapter).getOrCreateIntentDocument();
 		}
 		return intentDocument;
 	}
 
 	/**
 	 * Return the chapter at the given number.
 	 * 
 	 * @param number
 	 *            the number of the chapter
 	 * @return the chapter
 	 */
 	protected final IntentChapter getIntentChapter(int number) {
 		return getIntentDocument().getChapters().get(number - 1);
 	}
 
 	/**
 	 * Return the section at the given number.
 	 * 
 	 * @param number
 	 *            the number of the section
 	 * @return the section
 	 */
 	protected final IntentSection getIntentSection(int... number) {
 		IntentSection section = getIntentChapter(number[0]).getSubSections().get(number[1] - 1);
 		if (number.length > 2) {
 			for (int i = 2; i < number.length; i++) {
 				section = section.getSubSections().get(number[i] - 1);
 			}
 		}
 		return section;
 	}
 
 	/**
 	 * Opens an editor on the Document contained in the intent project.
 	 * 
 	 * @return the opened editor
 	 */
 	protected final IntentEditor openIntentEditor() {
 		return openIntentEditor(getIntentDocument());
 	}
 
 	/**
 	 * Opens an editor on the given {@link IntentStructuredElement}.
 	 * 
 	 * @param element
 	 *            the {@link IntentStructuredElement} to open an editor on
 	 * @return the opened editor
 	 */
 	protected final IntentEditor openIntentEditor(IntentStructuredElement element) {
 		IntentEditorOpener.openIntentEditor(repository, element, true, null, true);
 		waitForAllOperationsInUIThread();
 		IntentEditor editor = IntentEditorOpener.getAlreadyOpenedEditor(element);
 		assertNotNull(editor);
 		openedEditors.add(editor);
 		return editor;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.core.runtime.ILogListener#logging(org.eclipse.core.runtime.IStatus, java.lang.String)
 	 */
 	public void logging(IStatus status, String plugin) {
 		if (status.getSeverity() == IStatus.ERROR) {
 			fail(status.getMessage());
 		}
 	}
 
 	/**
 	 * Wait until the end of all asynchronous operations launched in the UI Thread.
 	 */
 	protected static void waitForAllOperationsInUIThread() {
 		while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				// Nothing to do
 			}
 		}
 	}
 
 	/**
 	 * Wait for synchronizer to complete work.
 	 */
 	protected final void waitForSynchronizer() {
 		waitForSynchronizer(true);
 	}
 
 	/**
 	 * Wait for repository to complete work.
 	 */
 	protected final void waitForIndexer() {
 		waitForIndexer(true);
 	}
 
 	/**
 	 * Wait for compiler to complete work.
 	 */
 	protected final void waitForCompiler() {
 		waitForCompiler(true);
 	}
 
 	/**
 	 * Wait for the project explorer refresher to complete work.
 	 */
 	protected final void waitForProjectExplorerRefresher() {
 		waitForProjectExplorerRefresher(true);
 	}
 
 	/**
 	 * Ensures that the synchronizer has been launched or not, according to the given boolean.
 	 * 
 	 * @param synchronizerShouldBeNotified
 	 *            indicates whether the synchronizer should be notified or not
 	 */
 	protected final void waitForSynchronizer(boolean synchronizerShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for synchronizer : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (synchronizerShouldBeNotified) {
 			assertTrue("Time out : synchronizer should have handle changes but did not",
					repositoryListener.waitForModificationOn(synchronizerShouldBeNotified, "Synchronizer"));
 		} else {
 			assertFalse("Synchonizer should not have been notifed",
					repositoryListener.waitForModificationOn(synchronizerShouldBeNotified, "Synchronizer"));
 		}
 		try {
 			Thread.sleep(WAITING_DELAY_MILLIS);
 		} catch (InterruptedException e) {
 			// fail silently
 		}
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * Ensures that the indexer has been launched or not, according to the given boolean.
 	 * 
 	 * @param indexerShouldBeNotified
 	 *            indicates whether the indexer should be notified or not
 	 */
 	protected final void waitForIndexer(boolean indexerShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for Indexer : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (indexerShouldBeNotified) {
 			assertTrue("Time out : indexer should have handle changes but did not",
					repositoryListener.waitForModificationOn(indexerShouldBeNotified, "Indexer"));
 		} else {
 			assertFalse("Indexer should not have been notifed",
					repositoryListener.waitForModificationOn(indexerShouldBeNotified, "Indexer"));
 		}
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * Ensures that the project explorer refreshed has been launched or not, according to the given boolean.
 	 * 
 	 * @param refresherShouldBeNotified
 	 *            indicates whether the indexer should be notified or not
 	 */
 	protected final void waitForProjectExplorerRefresher(boolean refresherShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for Project Explorer Refresher : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (refresherShouldBeNotified) {
 			assertTrue("Time out : Project Explorer Refresher should have handle changes but did not",
					repositoryListener.waitForModificationOn(refresherShouldBeNotified,
							"Project Explorer Refresher"));
 		} else {
 			assertFalse("Project Explorer Refresher should not have been notifed",
					repositoryListener.waitForModificationOn(refresherShouldBeNotified,
							"Project Explorer Refresher"));
 		}
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * Ensures that the compiler has been launched or not, according to the given boolean.
 	 * 
 	 * @param compilerShouldBeNotified
 	 *            indicates whether the compiler should be notified or not
 	 */
 	protected final void waitForCompiler(boolean compilerShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for compiler : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (compilerShouldBeNotified) {
 			assertTrue("Time out : compiler should have handle changes but did not",
					repositoryListener.waitForModificationOn(compilerShouldBeNotified, "Compiler"));
 		} else {
 			assertFalse("Compiler should not have been notifed",
					repositoryListener.waitForModificationOn(compilerShouldBeNotified, "Compiler"));
 		}
 		try {
 			Thread.sleep(WAITING_DELAY_MILLIS);
 		} catch (InterruptedException e) {
 			// fail silently
 		}
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * Fixes the issue associated to the Intent annotation with the given message using the quickfix.
 	 * 
 	 * @param message
 	 *            the annotation message
 	 * @param editor
 	 *            the editor containing the annotation to search
 	 * @param document
 	 *            the editor's {@link IntentEditorDocument}
 	 */
 	protected void fixIssueUsingQuickFix(IntentEditor editor, IntentEditorDocument document, String message) {
 		for (IntentAnnotation annotation : AnnotationUtils.getIntentAnnotations(editor,
 				IntentAnnotationMessageType.SYNC_WARNING)) {
 			if (annotation.getText().equals(message)) {
 				AnnotationUtils.applyAnnotationFix(document, repositoryAdapter, annotation, 1);
 				editor.doSave(new NullProgressMonitor());
 				waitForCompiler();
 				waitForSynchronizer();
 				return;
 			}
 		}
 		AnnotationUtils.displayAnnotations(editor);
 		fail("Annotation not found: " + message);
 	}
 
 	/**
 	 * Additional operations performed at the end of set-up.
 	 */
 	protected void additionalSetUpOperations() {
 		// waiting for synchronizer to pass
 		waitForSynchronizer();
 	}
 
 	/**
 	 * Sets all fields of the current test case to null (to avoid memory leaks).
 	 */
 	private void setAllFieldsToNull() {
 		// For all fields defined in the current test and all its superclasses
 		for (Class<?> clazz = this.getClass(); clazz != TestCase.class; clazz = clazz.getSuperclass()) {
 			for (Field field : clazz.getDeclaredFields()) {
 				boolean isReference = !field.getType().isPrimitive();
 				try {
 					field.setAccessible(true);
 					boolean isSet = field.get(this) != null;
 					// We do not clean non set references
 					if (isReference && isSet) {
 						boolean isFinal = Modifier.isFinal(field.getModifiers());
 						// We do not clean final fields
 						if (!isFinal) {
 							// Setting the field to null
 							field.set(this, null);
 						}
 					}
 				} catch (IllegalArgumentException e) {
 					// Do nothing
 				} catch (IllegalAccessException e) {
 					// Do nothing
 				}
 			}
 		}
 	}

 }
