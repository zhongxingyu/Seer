 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.handlers;
 
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.handlers.AbstractSelectionBasedHandler;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.UINodeBP;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.rcp.views.AbstractJBTreeView;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PlatformUI;
 
 
 /**
  * @author BREDEX GmbH
  * @created Aug 11, 2010
  */
 public abstract class AbstractShowSpecificationHandler 
     extends AbstractSelectionBasedHandler {
     /**
      * Shows the SpecTS in the Test Suite Browser
      * 
      * @param node
      *            the node to show
      * @param viewPart
      *            the view part to show the specification in
      */
     protected void showSpecUINode(INodePO node, IViewPart viewPart) {
         if (!Utils.openPerspective(Constants.SPEC_PERSPECTIVE)) {
             return;
         }
         if (!PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                 .getActivePage().getPerspective().getId()
                 .equals(Constants.SPEC_PERSPECTIVE)) {
             // show error must be in SpecPers
             ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.I_NO_PERSPECTIVE_CHANGE);
             return;
         }
         if (viewPart instanceof AbstractJBTreeView) {
             AbstractJBTreeView jbtv = (AbstractJBTreeView)viewPart;
             UINodeBP.selectNodeInTree(node.getId(), jbtv.getTreeViewer(),
                     jbtv.getEntityManager());
         }
     }
 }
