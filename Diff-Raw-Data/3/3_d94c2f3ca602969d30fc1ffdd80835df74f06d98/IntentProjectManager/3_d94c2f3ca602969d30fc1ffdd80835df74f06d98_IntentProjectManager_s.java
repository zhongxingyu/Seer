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
 package org.eclipse.mylyn.docs.intent.client.ui.ide.launcher;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.cdo.eresource.EresourcePackage;
 import org.eclipse.mylyn.docs.intent.client.compiler.launcher.CompilerCreator;
 import org.eclipse.mylyn.docs.intent.client.compiler.repositoryconnection.CompilerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.indexer.IndexerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.indexer.launcher.IndexerCreator;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.SynchronizerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.client.synchronizer.launcher.SynchronizerCreator;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentDocumentProvider;
 import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentNature;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.generatedelementlistener.IDEGeneratedElementListener;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.navigator.ProjectExplorerRefresher;
 import org.eclipse.mylyn.docs.intent.client.ui.ide.structurer.IntentWorkspaceRepositoryStructurer;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotificationFactoryHolder;
 import org.eclipse.mylyn.docs.intent.collab.ide.notification.WorkspaceRepositoryChangeNotificationFactory;
 import org.eclipse.mylyn.docs.intent.collab.ide.repository.WorkspaceConfig;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.collab.utils.RepositoryCreatorHolder;
 import org.eclipse.mylyn.docs.intent.core.compiler.CompilerPackage;
 import org.eclipse.mylyn.docs.intent.core.descriptionunit.DescriptionUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentPackage;
 import org.eclipse.mylyn.docs.intent.core.genericunit.GenericUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndexerPackage;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitPackage;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Handles an Intent Project lifecycle :
  * <ul>
  * <li>Create/Delete the Repository</li>
  * <li>Launch/Stop Intent Clients</li>.
  * </ul>
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
  */
 public final class IntentProjectManager {
 
 	/**
 	 * The list of created {@link IntentProjectManager}s, associated to the corresponding project.
 	 */
 	private static Map<IProject, IntentProjectManager> projectManagers = new HashMap<IProject, IntentProjectManager>();
 
 	private CompilerRepositoryClient compilerClient;
 
 	private SynchronizerRepositoryClient synchronizerClient;
 
 	private IndexerRepositoryClient indexerClient;
 
 	private ProjectExplorerRefresher refresher;
 
 	/**
 	 * The project associated to this IntentProjectManager (must be associated to the Intent nature).
 	 */
 	private IProject project;
 
 	private Repository repository;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param project
 	 *            the project to associate to this IntentProjectManager (must be associated to the Intent
 	 *            nature)
 	 */
 	private IntentProjectManager(IProject project) {
 		this.project = project;
 	}
 
 	/**
 	 * Creates and Launch all the clients needed by the Intent application :
 	 * <ul>
 	 * <li>Compiler</li>
 	 * <li>Synchronizer</li>
 	 * <li>Indexer</li>
 	 * <li>Project Explorer Refresher.</li>
 	 * </ul>
 	 * 
 	 * @throws RepositoryConnectionException
 	 *             if the {@link Repository} cannot be created or accessed
 	 */
 	public synchronized void connect() throws RepositoryConnectionException {
 		try {
			System.out.println("[IntentProjectManager] Connecting to project " + project.isAccessible()
					+ "  " + project.getNature(IntentNature.NATURE_ID) != null);
 			if (project.isAccessible() && project.getNature(IntentNature.NATURE_ID) != null) {
 				getRepository().getOrCreateSession();
 
 				// Clients creation (if needed)
 
 				// Compiler
 				if (compilerClient == null) {
 					compilerClient = CompilerCreator.createCompilerClient(getRepository());
 				}
 
 				// Synchronizer
 				if (synchronizerClient == null) {
 					synchronizerClient = SynchronizerCreator.createSynchronizer(getRepository(),
 							new IDEGeneratedElementListener());
 				}
 
 				// Indexer
 				if (indexerClient == null) {
 					indexerClient = IndexerCreator.launchIndexer(getRepository());
 				}
 
 				// Project explorer refresher
 				if (refresher == null) {
 					refresher = ProjectExplorerRefresher.createProjectExplorerRefresher(project);
 				}
 
 				// notifies the clients
 
 				// launch the indexer in order to allow navigation within the document
 				indexerClient.handleChangeNotification(null);
 
 				// launch the compiler to detect eventual existing issues
 				compilerClient.handleChangeNotification(null);
 
 			} else {
 				throw new RepositoryConnectionException("Cannot create Repository on project "
 						+ project.getName());
 			}
 		} catch (CoreException e) {
 			throw new RepositoryConnectionException(e.getMessage());
 		}
 	}
 
 	/**
 	 * Disconnects the given project by closing the session and the repository.
 	 * 
 	 * @throws RepositoryConnectionException
 	 *             if the {@link Repository} cannot be deleted or accessed
 	 */
 	public synchronized void disconnect() throws RepositoryConnectionException {
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow();
 				if (activeWorkbenchWindow != null) {
 					IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
 					IEditorReference[] references = page.getEditorReferences();
 					for (IEditorReference reference : references) {
 						IEditorPart part = reference.getEditor(false);
 						if (part instanceof IntentEditor) {
 							IntentEditor editor = (IntentEditor)part;
 							IntentDocumentProvider provider = (IntentDocumentProvider)editor
 									.getDocumentProvider();
 							if (repository.equals(provider.getRepository())) {
 								editor.close(editor.isSaveOnCloseNeeded()); // this will dispose clients
 							}
 						}
 					}
 				}
 
 			}
 		});
 
 		projectManagers.remove(project);
 
 		compilerClient.dispose();
 		synchronizerClient.dispose();
 		indexerClient.dispose();
 		refresher.dispose();
 
 		repository.closeSession();
 	}
 
 	/**
 	 * Gets or creates the {@link Repository} associated to the considered project.
 	 * 
 	 * @return the {@link Repository} associated to the considered project
 	 * @throws RepositoryConnectionException
 	 *             if the {@link Repository} cannot be created or accessed
 	 */
 	private Repository getRepository() throws RepositoryConnectionException {
 		if (this.repository == null) {
 
 			// Step 1 : create the Repository configuration
 			WorkspaceConfig wpConfig = new WorkspaceConfig(project, IntentLocations.INDEXES_LIST);
 
 			// Step 2 : define a structurer used to split the repository resources
 			IntentWorkspaceRepositoryStructurer structurer = new IntentWorkspaceRepositoryStructurer();
 
 			// Step 3 : initialize the creator that will be used for creating RepositoryAdapter
 			if (RepositoryCreatorHolder.getCreator() == null) {
 				RepositoryCreatorHolder.setCreator(new IntentWorkspaceRepositoryCreator(structurer));
 			}
 			// Step 4 : initialize the Notification Factory
 			if (RepositoryChangeNotificationFactoryHolder.getChangeNotificationFactory() == null) {
 				RepositoryChangeNotificationFactoryHolder
 						.setChangeNotificationFactory(new WorkspaceRepositoryChangeNotificationFactory());
 			}
 
 			// Step 5 : initialize the Repository's Package registry
 			repository = RepositoryCreatorHolder.getCreator().createRepository(wpConfig);
 			repository.getPackageRegistry().put(IntentIndexerPackage.eNS_URI, IntentIndexerPackage.eINSTANCE);
 			repository.getPackageRegistry().put(CompilerPackage.eNS_URI, CompilerPackage.eINSTANCE);
 			repository.getPackageRegistry().put(IntentDocumentPackage.eNS_URI,
 					IntentDocumentPackage.eINSTANCE);
 			repository.getPackageRegistry().put(ModelingUnitPackage.eNS_URI, ModelingUnitPackage.eINSTANCE);
 			repository.getPackageRegistry().put(DescriptionUnitPackage.eNS_URI,
 					DescriptionUnitPackage.eINSTANCE);
 			repository.getPackageRegistry().put(GenericUnitPackage.eNS_URI, GenericUnitPackage.eINSTANCE);
 			repository.getPackageRegistry().put(EresourcePackage.eNS_URI, EresourcePackage.eINSTANCE);
 		}
 		return repository;
 	}
 
 	/**
 	 * Returns the {@link Repository} associated to the given Intent project.
 	 * 
 	 * @param project
 	 *            the Intent project to get the Repository from
 	 * @return the {@link Repository} associated to the given Intent project
 	 * @throws RepositoryConnectionException
 	 *             if the repository cannot be created
 	 */
 	public static Repository getRepository(IProject project) throws RepositoryConnectionException {
 		return getInstance(project, true).getRepository();
 	}
 
 	/**
 	 * Returns the {@link IntentProjectManager} handling the given Intent project. If no IntentProjectManager
 	 * is associated to this project, creates a new one.
 	 * 
 	 * @param project
 	 *            the Intent project to get the {@link IntentProjectManager} from
 	 * @param create
 	 *            if true, creates a new project manager instance
 	 * @return the {@link IntentProjectManager} handling the given Intent project
 	 */
 	public static IntentProjectManager getInstance(IProject project, boolean create) {
 		if (projectManagers.get(project) == null && create) {
 			projectManagers.put(project, new IntentProjectManager(project));
 		}
 		return projectManagers.get(project);
 	}
 
 	/**
 	 * Indicates if the given project is handled by a {@link IntentProjectManager}.
 	 * 
 	 * @param project
 	 *            the project to test
 	 * @return true if the given project is handled by a {@link IntentProjectManager}, false otherwise
 	 */
 	public static boolean isManagedProject(IProject project) {
 		return projectManagers.get(project) != null;
 	}
 
 }
