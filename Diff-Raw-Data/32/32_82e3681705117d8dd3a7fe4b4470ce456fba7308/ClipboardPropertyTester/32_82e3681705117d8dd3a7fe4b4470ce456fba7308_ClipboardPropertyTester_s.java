 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.testers;
 
 import java.util.List;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 /**
  * Provide a tester to test if the paste operation is enabled.
  */
 public class ClipboardPropertyTester extends PropertyTester {
 
 	/**
 	 * Create an instance.
 	 */
 	public ClipboardPropertyTester() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
 	 */
 	@Override
 	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 		Assert.isTrue(receiver instanceof IStructuredSelection);
 		if (property.equals("canPaste")) { //$NON-NLS-1$
 			FSClipboard cb = UIPlugin.getDefault().getClipboard();
 			if (!cb.isEmpty()) {
 				List<FSTreeNode> nodes = cb.getTreeNodes();
 				int operation = cb.getOperation();
 				boolean moving = operation == FSClipboard.CUT;
 				boolean copying = operation == FSClipboard.COPY;
 				List<FSTreeNode> selection = ((IStructuredSelection) receiver).toList();
 				FSTreeNode hovered = null;
				for (FSTreeNode node : selection) {
					if (hovered == null) hovered = node.parent;
					else if (hovered != node.parent) return false;
 				}
 				if (hovered.isDirectory() && hovered.isWritable() && (moving || copying)) {
 					FSTreeNode head = nodes.get(0);
 					String hid = head.peerNode.getPeer().getID();
 					String tid = hovered.peerNode.getPeer().getID();
 					if (hid.equals(tid)) {
 						for (FSTreeNode node : nodes) {
 							if (moving && node.parent == hovered || node.isAncestorOf(hovered)) {
 								return false;
 							}
 						}
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
