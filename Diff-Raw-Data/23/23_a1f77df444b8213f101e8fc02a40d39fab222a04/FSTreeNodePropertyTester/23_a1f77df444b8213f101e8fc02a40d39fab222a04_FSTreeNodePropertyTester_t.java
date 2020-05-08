 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  * William Chen (Wind River) - [345387]Open the remote files with a proper editor
  * William Chen (Wind River) - [352302]Opening a file in an editor depending on
  *                             the client's permissions.
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.testers;
 
 import java.io.File;
 
 import org.eclipse.core.expressions.PropertyTester;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.te.tcf.filesystem.internal.utils.CacheManager;
 import org.eclipse.tcf.te.tcf.filesystem.internal.utils.ContentTypeHelper;
 import org.eclipse.tcf.te.tcf.filesystem.internal.utils.StateManager;
 import org.eclipse.tcf.te.tcf.filesystem.model.CacheState;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 
 /**
  * The property tester of an FSTreeNode. The properties include "isFile"
  * if it is a file node, "isDirectory" if it is a directory, "isBinaryFile"
  * if it is a binary file, "isReadable" if it is readable, "isWritable" if
  * it is writable, "isExecutable" if it is executable, "isRoot" if it is a
  * root directory, "isWindows" if it is a windows file node, "isReadOnly"
  * if it is read only, "isHidden" if it is hidden, "getCacheState" to
  * get a node's state.
  * <p>
  * "testParent" is a property by which the parent or even the grand parent
  * of a node can be tested. The arguments is a recursive list of the above
  * test property including "testParent". 
  * <p>
  * The following is an example of how it is used.
  * <pre>
  *     &lt;test
  *         args="isWritable"
  *         property="org.eclipse.tcf.te.tcf.filesystem.propertytester.treenode.testParent"&gt;
  *     &lt;/test&gt;
  * </pre>
  * <p>
  * The above example tests if the parent node is writable.
  * <pre>
  *     &lt;test
  *         args="testParent,isWritable"
  *         property="org.eclipse.tcf.te.tcf.filesystem.propertytester.treenode.testParent"&gt;
  *     &lt;/test&gt;
  * </pre>
  * <p>
  * The above example tests if the grand parent node is writable.
  * <p>
  * And so on, you can test its ancestor recursively:
  * <pre>
  *     &lt;test
  *         args="testParent,testParent,testParent,...,isWritable"
  *         property="org.eclipse.tcf.te.tcf.filesystem.propertytester.treenode.testParent"&gt;
  *     &lt;/test&gt;
  * </pre> 
  */
 public class FSTreeNodePropertyTester extends PropertyTester {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
 	 */
 	@Override
 	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
 		if(receiver == null)
 			return false;
 		Assert.isTrue(receiver instanceof FSTreeNode);
 		FSTreeNode node = (FSTreeNode) receiver;
 		if (property.equals("isFile")) { //$NON-NLS-1$
 			return node.isFile();
 		} else if (property.equals("isDirectory")) { //$NON-NLS-1$
 			return node.isDirectory();
 		} else if (property.equals("isBinaryFile")) { //$NON-NLS-1$
 			return ContentTypeHelper.getInstance().isBinaryFile(node);
 		} else if (property.equals("isReadable")){ //$NON-NLS-1$
 			return node.isReadable();
 		} else if (property.equals("isWritable")){ //$NON-NLS-1$
 			return node.isWritable();
 		} else if (property.equals("isExecutable")){ //$NON-NLS-1$
 			return node.isExecutable();
 		} else if (property.equals("isRoot")) { //$NON-NLS-1$
 			return node.isRoot();
		} else if (property.equals("isSystemRoot")) { //$NON-NLS-1$
			return node.isSystemRoot();
 		} else if (property.equals("isWindows")) { //$NON-NLS-1$
 			return node.isWindowsNode();
 		} else if (property.equals("isReadOnly")) { //$NON-NLS-1$
 			return node.isReadOnly();
 		} else if (property.equals("isHidden")) { //$NON-NLS-1$
 			return node.isHidden();
 		} else if (property.equals("testParent")) { //$NON-NLS-1$
 			return testParent(node, args, expectedValue);
 		} else if (property.equals("getCacheState")){ //$NON-NLS-1$
 			File file = CacheManager.getInstance().getCacheFile(node);
 			if(!file.exists())
 				return false;
 			CacheState state = StateManager.getInstance().getCacheState(node);
 			return state.name().equals(expectedValue);
 		}
 		return false;
 	}
 
 	private boolean testParent(FSTreeNode node, Object[] args, Object expectedValue) {
 		if(args == null || args.length == 0)
 			return false;
 		String arg = (String) args[0];
 		Object[] newArgs = new Object[args.length -1];
 		System.arraycopy(args, 1, newArgs, 0, args.length - 1);
 	    return test(node.parent, arg, newArgs, expectedValue);
     }
 }
