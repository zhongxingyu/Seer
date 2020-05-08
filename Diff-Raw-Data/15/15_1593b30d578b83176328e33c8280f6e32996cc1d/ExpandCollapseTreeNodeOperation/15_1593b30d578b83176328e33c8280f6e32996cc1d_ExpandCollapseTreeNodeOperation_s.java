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
 package org.eclipse.jubula.rc.common.implclasses.tree;
 
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 
 /**
  * This operation expands or collapses a tree node.
  * 
  * @author BREDEX GmbH
  * @created 22.03.2005
  */
 public class ExpandCollapseTreeNodeOperation 
     extends AbstractExpandCollapseTreeNodeOperation {
 
     /**
      * Creates a new instance. It collapses the nodes if the passed
      * <code>collapse</code> parameter is <code>true</code>, otherwise it
      * expands the nodes.
      * @param collapse <code>true</code> or <code>false</code>
      */
     public ExpandCollapseTreeNodeOperation(boolean collapse) {
         super(collapse);
     }
 
     /**
      * Expands or collapses the passed tree node. If the node is already 
      * expanded or collapsed, respectively, nothing happens.
      * 
      * {@inheritDoc}
      */
     public boolean operate(final Object treeNode)
         throws StepExecutionException {

        if (isCollapse()) {
            getContext().collapseNode(treeNode);
        } else {
            getContext().expandNode(treeNode);
         }
         
         return true;
     }
 }
