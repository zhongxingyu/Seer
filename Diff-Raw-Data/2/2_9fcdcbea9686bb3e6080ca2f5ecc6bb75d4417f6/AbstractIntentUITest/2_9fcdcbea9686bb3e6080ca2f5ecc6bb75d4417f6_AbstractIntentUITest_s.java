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
 
 import com.google.common.collect.Sets;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
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
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentNature;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.ToggleNatureAction;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.launcher.IDEApplicationManager;
 import org.eclipse.mylyn.docs.intent.client.ui.utils.IntentEditorOpener;
 import org.eclipse.mylyn.docs.intent.collab.common.IntentRepositoryManager;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.ReadOnlyException;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.ReadWriteRepositoryObjectHandlerImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList.ElementListNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.typeListener.TypeNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerPackage;
 import org.eclipse.mylyn.docs.intent.core.document.IntentChapter;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
 import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
 import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
 import org.eclipse.mylyn.docs.intent.parser.modelingunit.test.utils.FileToStringConverter;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * An abstract test class providing API for manage an Intent IDE projects and editors.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public abstract class AbstractIntentUITest extends TestCase implements ILogListener {
 
 	public static final String INTENT_NEW_PROJECT_WIZARD_ID = "org.eclipse.mylyn.docs.intent.client.ui.ide.wizards.NewIntentProjectWizard";
 
	private static final String INTENT_EMPTY_DOC_PATH = "data/unit/documents/scenario/empty.intent";
 
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
 
 		System.out.println("-- SETTED UP.;");
 		traceHeapSize();
 
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
 		// Step 1 : close editors
 		for (IntentEditor editor : openedEditors) {
 			editor.close(false);
 		}
 
 		// Step 2 : clean workspace
 		if (intentProject != null) {
 			IntentRepositoryManager.INSTANCE.deleteRepository(intentProject.getName());
 			waitForAllOperationsInUIThread();
 
 			intentProject.delete(true, true, new NullProgressMonitor());
 		}
 		IntentEditorActivator.getDefault().getLog().removeLogListener(this);
 		WorkspaceUtils.cleanWorkspace();
 
 		// Step 3 : setting all fields to null (to avoid memory leaks)
 		setAllFieldsToNull();
 
 		super.tearDown();
 		long totalHeapSize = Runtime.getRuntime().totalMemory();
 		long usedHeap = totalHeapSize - Runtime.getRuntime().freeMemory();
 		System.out.println("-- TEARED DOWN.");
 		traceHeapSize();
 	}
 
 	/**
 	 * Creates and register an new {@link org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient } in
 	 * charge of detecting that events happened on the repository.
 	 */
 	protected void registerRepositoryListener() {
 		// Step 1 : creating the handler
 		// we listen for all modifications made on the traceability index
 		RepositoryObjectHandler handler = new ReadWriteRepositoryObjectHandlerImpl(repositoryAdapter);
 
 		if (getIntentDocument() == null) {
 			fail("Cannot register a repository listener without having setted up an Intent Document");
 		}
 		Set<EObject> listenedElements = Sets.newLinkedHashSet();
 		listenedElements.add(getIntentDocument());
 		listenedElements.add(handler.getRepositoryAdapter()
 				.getResource(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH).getContents().iterator().next());
 		Notificator elementNotificator = new ElementListNotificator(listenedElements,
 				new ElementListAdapter(), repositoryAdapter);
 		Notificator compilationStatusNotificator = new TypeNotificator(
 				Sets.newLinkedHashSet(CompilerPackage.eINSTANCE.getCompilationStatusManager()
 						.getEAllStructuralFeatures()));
 		handler.addNotificator(elementNotificator);
 		handler.addNotificator(compilationStatusNotificator);
 		// Step 2 : creating the client
 		this.repositoryListener = new RepositoryListenerForTests();
 		repositoryListener.addRepositoryObjectHandler(handler);
 	}
 
 	/**
 	 * Creates a new empty Intent project.
 	 * 
 	 * @param projectName
 	 *            the intent project name
 	 */
 	protected void setUpIntentProject(final String projectName) {
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
 	protected void setUpIntentProject(final String projectName, String intentDocumentPath) {
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
 			// Step 1 : getting the content of the intent document located at the given path.
 			File file = new File(intentDocumentPath);
 			final String intentDocumentContent = FileToStringConverter.getFileAsString(file);
 
 			// Step 2 : creating the intent project
 			IWorkspaceRunnable create = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor) throws CoreException {
 					IProject project = WorkspaceUtils.createProject(projectName, monitor);
 					ToggleNatureAction.toggleNature(project);
 
 					IDEApplicationManager.initializeContent(project, intentDocumentContent);
 
 					// Step 3 : initializing all useful informations
 					intentProject = project;
 					setUpRepository(project);
 				}
 			};
 			ResourcesPlugin.getWorkspace().run(create, null);
 
 			while (repositoryAdapter == null
 			// && (repository == null || ((WorkspaceRepository)repository).getEditingDomain()
 			// .getCommandStack() == null)
 			) {
 				Thread.sleep(10);
 			}
 
 			// Step 3 : registering the repository listener
 			registerRepositoryListener();
 
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
 	 * Set up the repository for the given project.
 	 * 
 	 * @param project
 	 *            the project
 	 */
 	protected void setUpRepository(IProject project) {
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
 
 	/**
 	 * Loads the {@link IntentStructuredElement} located at the given path. If it contains an IntentDocument,
 	 * also updates the intentDocument field.
 	 * 
 	 * @param intentDocumentModelPath
 	 *            the path of the Intent document model (from
 	 *            org.eclipse.mylyn.docs.intent.client.ui.test/data)
 	 * @return the loaded {@link IntentStructuredElement}
 	 */
 	protected IntentStructuredElement loadIntentDocumentFromTests(String intentDocumentModelPath) {
 		ResourceSet rs = new ResourceSetImpl();
 		URI documentURI = URI.createURI("platform:/plugin/org.eclipse.mylyn.docs.intent.client.ui.test/data/"
 				+ intentDocumentModelPath);
 		Resource documentResource = rs.getResource(documentURI, true);
 		if (documentResource != null && documentResource.getContents().iterator().hasNext()
 				&& documentResource.getContents().iterator().next() instanceof IntentStructuredElement) {
 			if (documentResource.getContents().iterator().next() instanceof IntentDocument) {
 				intentDocument = (IntentDocument)documentResource.getContents().iterator().next();
 			}
 			return (IntentStructuredElement)documentResource.getContents().iterator().next();
 		}
 		throw new AssertionFailedError("Could not load Intent model at " + intentDocumentModelPath);
 	}
 
 	/**
 	 * Returns the intentDocument associated to the current Intent project.
 	 * 
 	 * @return the intentDocument associated to the current Intent project
 	 */
 	protected IntentDocument getIntentDocument() {
 		if (intentDocument == null) {
 			try {
 				Resource documentResource = repositoryAdapter
 						.getOrCreateResource(IntentLocations.INTENT_INDEX);
 				assertTrue("Invalid content of resource '" + IntentLocations.INTENT_INDEX + "'",
 						documentResource.getContents().iterator().next() instanceof IntentDocument);
 				intentDocument = (IntentDocument)documentResource.getContents().iterator().next();
 			} catch (ReadOnlyException e) {
 				// Cannot happen in the test context : no readonly access
 			}
 
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
 	protected IntentChapter getIntentChapter(int number) {
 		return getIntentDocument().getChapters().get(number - 1);
 	}
 
 	/**
 	 * Return the section at the given number.
 	 * 
 	 * @param number
 	 *            the number of the section
 	 * @return the section
 	 */
 	protected IntentSection getIntentSection(int... number) {
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
 	protected IntentEditor openIntentEditor() {
 		return openIntentEditor(getIntentDocument());
 	}
 
 	/**
 	 * Opens an editor on the given {@link IntentStructuredElement}.
 	 * 
 	 * @param element
 	 *            the {@link IntentStructuredElement} to open an editor on
 	 * @return the opened editor
 	 */
 	protected IntentEditor openIntentEditor(IntentStructuredElement element) {
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
 	protected void waitForSynchronizer() {
 		waitForSynchronizer(true);
 	}
 
 	/**
 	 * Wait for repository to complete work.
 	 */
 	protected void waitForIndexer() {
 		waitForIndexer(true);
 	}
 
 	/**
 	 * Wait for compiler to complete work.
 	 */
 	protected void waitForCompiler() {
 		waitForCompiler(true);
 	}
 
 	/**
 	 * Ensures that the synchronizer has been launched or not, according to the given boolean.
 	 * 
 	 * @param compilerShouldBeNotified
 	 *            indicates whether the synchronizer should be notified or not
 	 */
 	protected void waitForSynchronizer(boolean synchronizerShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for synchronizer : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (synchronizerShouldBeNotified) {
 			assertTrue("Time out : synchronizer should have handle changes but did not",
 					repositoryListener.waitForModificationOn(IntentLocations.COMPILATION_STATUS_INDEX_PATH));
 		} else {
 			assertFalse("Synchonizer should not have been notifed",
 					repositoryListener.waitForModificationOn(IntentLocations.COMPILATION_STATUS_INDEX_PATH));
 		}
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * Ensures that the indexer has been launched or not, according to the given boolean.
 	 * 
 	 * @param compilerShouldBeNotified
 	 *            indicates whether the indexer should be notified or not
 	 */
 	protected void waitForIndexer(boolean indexerShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for Indexer : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (indexerShouldBeNotified) {
 			assertTrue("Time out : indexer should have handle changes but did not",
 					repositoryListener.waitForModificationOn(IntentLocations.GENERAL_INDEX_PATH));
 		} else {
 			assertFalse("Indexer should not have been notifed",
 					repositoryListener.waitForModificationOn(IntentLocations.GENERAL_INDEX_PATH));
 		}
 		waitForAllOperationsInUIThread();
 	}
 
 	/**
 	 * Ensures that the compiler has been launched or not, according to the given boolean.
 	 * 
 	 * @param compilerShouldBeNotified
 	 *            indicates whether the compiler should be notified or not
 	 */
 	protected void waitForCompiler(boolean compilerShouldBeNotified) {
 		waitForAllOperationsInUIThread();
 		assertNotNull(
 				"Cannot wait for compiler : you need to initialize a repository listener by calling the registerRepositoryListener() method",
 				repositoryListener);
 		if (compilerShouldBeNotified) {
 			assertTrue("Time out : compiler should have handle changes but did not",
 					repositoryListener.waitForModificationOn(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH));
 		} else {
 			assertFalse("Compiler should not have been notifed",
 					repositoryListener.waitForModificationOn(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH));
 		}
 		waitForAllOperationsInUIThread();
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
