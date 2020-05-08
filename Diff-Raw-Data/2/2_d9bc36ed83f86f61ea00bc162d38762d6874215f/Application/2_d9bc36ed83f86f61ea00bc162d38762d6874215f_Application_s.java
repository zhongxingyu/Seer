 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.sample.app.client.mail;
 
import org.osgi.framework.Bundle;

 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ApplicationNode;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.ui.swt.application.SwtApplication;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  * This class controls all aspects of the application's execution
  */
 public class Application extends SwtApplication {
 
 	public static final String ID_GROUP_MBOXES = "rcp.mail.groupMailboxes"; //$NON-NLS-1$
 
 	@Override
 	protected IApplicationNode createModel() {
 
 		ApplicationNode app = new ApplicationNode("Riena Mail"); //$NON-NLS-1$
 
 		ISubApplicationNode subApp = new SubApplicationNode("Your Mail"); //$NON-NLS-1$
 		app.addChild(subApp);
 		WorkareaManager.getInstance().registerDefinition(subApp, "rcp.mail.perspective"); //$NON-NLS-1$
 
 		IModuleGroupNode groupMailboxes = new ModuleGroupNode(new NavigationNodeId(Application.ID_GROUP_MBOXES));
 		subApp.addChild(groupMailboxes);
 
 		IModuleNode moduleAccount1 = NodeFactory.createModule(
 				new NavigationNodeId("account1"), "me@this.com", groupMailboxes); //$NON-NLS-1$ //$NON-NLS-2$
 		moduleAccount1.setClosable(false);
 		NodeFactory.createSubMobule(new NavigationNodeId("inbox"), "Inbox", moduleAccount1, View.ID); //$NON-NLS-1$ //$NON-NLS-2$
 		NodeFactory.createSubMobule(new NavigationNodeId("drafts"), "Drafts", moduleAccount1, View.ID); //$NON-NLS-1$ //$NON-NLS-2$
 		NodeFactory.createSubMobule(new NavigationNodeId("sent"), "Sent", moduleAccount1, View.ID); //$NON-NLS-1$ //$NON-NLS-2$
 
 		IModuleNode moduleAccount2 = NodeFactory.createModule(
 				new NavigationNodeId("account2"), "other@aol.com", groupMailboxes); //$NON-NLS-1$ //$NON-NLS-2$
 		NodeFactory.createSubMobule(new NavigationNodeId("inbox2"), "Inbox", moduleAccount2, View.ID); //$NON-NLS-1$ //$NON-NLS-2$
 
 		return app;
 	}
 
 }
