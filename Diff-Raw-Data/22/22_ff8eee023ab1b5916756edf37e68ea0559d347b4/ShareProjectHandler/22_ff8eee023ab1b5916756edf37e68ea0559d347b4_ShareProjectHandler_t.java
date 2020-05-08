 package org.eclipse.emf.emfstore.client.ui.handlers;
 
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.ui.controller.UIShareProjectController;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 
 public class ShareProjectHandler extends AbstractEMFStoreHandler implements IHandler {
 
 	@Override
	public void handle() throws EmfStoreException {
		new UIShareProjectController(getShell()).share(requireSelection(ProjectSpace.class));
 	}
 
 }
