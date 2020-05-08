 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.b2eclipse.git.checkout;
 
 import java.io.File;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.egit.ui.internal.repository.tree.FolderNode;
 import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNode;
 import org.eclipse.egit.ui.internal.repository.tree.RepositoryTreeNodeType;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.sourcepit.b2eclipse.git.handler.B2GitCheckoutHandler;
 import org.sourcepit.b2eclipse.ui.B2Wizard;
 
 /**
  * @author Marco Grupe <marco.grupe@googlemail.com>
  */
 
 public class B2GitCheckout extends B2GitCheckoutHandler
 {
 
 
    public Object execute(ExecutionEvent arg0) throws ExecutionException
    {
       final String path;
       RepositoryTreeNode<?> node = (RepositoryTreeNode<?>) getSelectedNode(arg0);
 
       if (node.getType() == RepositoryTreeNodeType.WORKINGDIR)
          path = node.getRepository().getWorkTree().toString();
       else if (node.getType() == RepositoryTreeNodeType.FOLDER)
          path = ((File) ((FolderNode) node).getObject()).getPath().toString();
       else
          return null;
 
       B2Wizard wizard = new B2Wizard();
       WizardDialog dialog = new WizardDialog(getShell(arg0), wizard);
      wizard.getB2WizardPage().setPath(new Path(path));
       dialog.open();
       return null;
    }
 
 }
