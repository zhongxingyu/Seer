 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.columns;
 
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 
 /**
  * The comparator for the tree column "name".
  */
 public class FSTreeElementComparator extends FSTreeNodeComparator {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.filesystem.internal.columns.FSTreeNodeComparator#doCompare(org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode, org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode)
 	 */
 	@Override
 	public int doCompare(FSTreeNode node1, FSTreeNode node2) {
 		String name1 = node1.name;
 		String name2 = node2.name;
 		if (name1 != null && name2 != null) {
 			if (name1.startsWith(".") && !name2.startsWith(".")) return -1; //$NON-NLS-1$ //$NON-NLS-2$
 			if (!name1.startsWith(".") && name2.startsWith(".")) return 1; //$NON-NLS-1$ //$NON-NLS-2$
			return name1.compareToIgnoreCase(name2);
 		}
 		return 0;
 	}
 }
