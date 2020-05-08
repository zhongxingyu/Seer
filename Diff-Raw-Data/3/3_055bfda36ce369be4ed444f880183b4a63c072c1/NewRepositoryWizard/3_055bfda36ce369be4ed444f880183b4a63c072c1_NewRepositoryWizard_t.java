 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Aleksandar Shterev - initial API and implementation
  * Edgar Mueller - Refactorings due to new API, invalidation of existing sessions
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.views.emfstorebrowser.views;
 
 import java.text.MessageFormat;
 import java.util.concurrent.Callable;
 
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.KeyStoreManager;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESServerImpl;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESWorkspaceImpl;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Wizard for adding or creating a new repository.
  * 
  * 
  * 
  * @author shterev
  * @author emueller
  */
 public class NewRepositoryWizard extends Wizard implements INewWizard {
 
 	private ESServer server;
 	private NewRepositoryWizardPageOne mainPage;
 
 	/**
 	 * Whether we use this page to edit an existing server
 	 * or to create a new one.
 	 */
 	private boolean isEdit;
 
 	/**
 	 * Default constructor.
 	 */
 	public NewRepositoryWizard() {
 		super();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param server
 	 *            the server that will be edited in case the wizard is called in edit mode
 	 */
 	public NewRepositoryWizard(ESServer server) {
 		this.server = server;
 		isEdit = true;
 	}
 
 	/**
 	 * Adds all pages in the wizard.
 	 */
 	@Override
 	public void addPages() {
 		mainPage = new NewRepositoryWizardPageOne();
 		setWindowTitle(Messages.NewRepositoryWizard_Server_Details);
 		addPage(mainPage);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean canFinish() {
 		return true;
 	}
 
 	private void invalidateSessions(final ESWorkspaceImpl workspace) throws ESException {
 		for (final Usersession session : workspace.toInternalAPI().getUsersessions()) {
 			if (session.getServerInfo() == ESServerImpl.class.cast(server).toInternalAPI()) {
 				session.logout();
				session.setSavePassword(false);
				session.setPassword(null);
				session.setPersistentPassword(null);
 			}
 		}
 
 		workspace.toInternalAPI().save();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean performFinish() {
 		if (mainPage.canFlipToNextPage()) {
 			final ESWorkspaceImpl workspace = (ESWorkspaceImpl) ESWorkspaceProvider.INSTANCE.getWorkspace();
 			final ESServer editedServer = mainPage.getServer();
 
 			if (isEdit) {
 				RunESCommand.run(new Callable<Void>() {
 					public Void call() throws Exception {
 						server.setCertificateAlias(editedServer.getCertificateAlias());
 						server.setName(editedServer.getName());
 						server.setPort(editedServer.getPort());
 						server.setURL(editedServer.getURL());
 
 						invalidateSessions(workspace);
 
 						// set default certificate
 						KeyStoreManager.getInstance().setDefaultCertificate(
 							editedServer.getCertificateAlias());
 
 						return null;
 					}
 
 				});
 
 			} else {
 				if (workspace.serverExists(editedServer)) {
 					MessageDialog.openInformation(getShell(), Messages.NewRepositoryWizard_Server_Already_Exists_Title,
 						MessageFormat.format(Messages.NewRepositoryWizard_Server_Already_Exists_Message,
 							server.getName() + ":" + server.getPort())); //$NON-NLS-1$
 				} else {
 					workspace.addServer(editedServer);
 				}
 			}
 			dispose();
 		} else {
 			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 				Messages.NewRepositoryWizard_Blank_Fields_Title,
 				Messages.NewRepositoryWizard_Blank_Fields_Message);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Getter for the server.
 	 * 
 	 * @return the server
 	 */
 	public ESServer getServer() {
 		if (server == null) {
 			// TODO: review or reuse client util
 			server = ESServer.FACTORY.createServer("localhost", 8080, //$NON-NLS-1$
 				KeyStoreManager.DEFAULT_CERTIFICATE);
 		}
 		return server;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
 	 *      org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		// nothing to do
 	}
 
 }
