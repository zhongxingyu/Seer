 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.core.util.StringMatcher;
 
 /**
  * Utility class for navigation nodes.
  */
 public final class NavigationNodeUtility {
 
 	private NavigationNodeUtility() {
 		// utility class
 	}
 
 	/**
 	 * Returns the <i>long</i> ID of the given node.<br>
 	 * The <i>long</i> ID is a combination of the ID of the given node and all
 	 * its parent nodes. The IDs are separated with slashes ("/").<br>
 	 * e.g.: /app1/subApp2/modGroup3/mod4/subMod4711
 	 * 
 	 * @param node
 	 *            navigation node
 	 * @return long ID
 	 */
 	public static String getNodeLongId(final INavigationNode<?> node) {
 
 		final StringBuilder builder = new StringBuilder();
 		addToNodeLongId(builder, node);
 		return builder.toString();
 
 	}
 
 	private static void addToNodeLongId(final StringBuilder builder, final INavigationNode<?> node) {
 
 		if (node != null) {
 			if (node.getNodeId() != null) {
 				final String id = node.getNodeId().getTypeId();
 				if (id != null) {
 					builder.insert(0, id);
 				}
 			}
 			builder.insert(0, "/"); //$NON-NLS-1$
 			addToNodeLongId(builder, node.getParent());
 		}
 
 	}
 
 	/**
 	 * Searches for the node with the given ID starting at the given node.<br>
 	 * The given ID will be compared with the typeID of the node.<br>
 	 * This method supports the wild cards: * and ?. The <b>first</b> matching
 	 * node is returned.
 	 * 
 	 * @param id
 	 *            ID
 	 * @param node
 	 *            start mode
 	 * @return list of found nodes or {@code null} if none was found
 	 * @since 3.0
 	 */
 	public static List<INavigationNode<?>> findNode(final String id, final INavigationNode<?> node) {
 
 		return findNode(id, node, new IIdClosure() {
 			public String getId(final INavigationNode<?> node) {
 				if (node.getNodeId() == null) {
 					return null;
 				} else {
 					return node.getNodeId().getTypeId();
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * Searches for the node with the given ID starting at the given node.<br>
 	 * The given ID will be compared with the <i>long ID</i> of the node.<br>
 	 * This method supports the wild cards: * and ?. The <b>first</b> matching
 	 * node is returned.
 	 * 
 	 * @param id
 	 *            ID
 	 * @param node
 	 *            start mode
 	 * @return list of found nodes or {@code null} if none was found
 	 * 
 	 * @see #getNodeLongId(INavigationNode)
 	 * @since 3.0
 	 */
 	public static List<INavigationNode<?>> findNodesByLongId(final String id, final INavigationNode<?> node) {
 		final ArrayList<INavigationNode<?>> result = new ArrayList<INavigationNode<?>>();
 		findNodesByLongId(id, node, result);
 		return result;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public static void findNodesByLongId(final String id, final INavigationNode<?> node,
 			final List<INavigationNode<?>> result) {
 		findNode(id, node, new IIdClosure() {
 			public String getId(final INavigationNode<?> node) {
 				final String nodeLongId = getNodeLongId(node);
 				return nodeLongId;
 			}
 		}, result);
 	}
 
 	private static void findNode(final String id, final INavigationNode<?> node, final IIdClosure closure,
 			final List<INavigationNode<?>> collector) {
 
 		Assert.isNotNull(id);
 		Assert.isNotNull(node);
 
 		final StringMatcher matcher = new StringMatcher(id);
 		final String nodeId = closure.getId(node);
 		if (matcher.match(nodeId)) {
 			collector.add(node);
 		}
 		final List<?> children = node.getChildren();
 		for (final Object child : children) {
 			if (child instanceof INavigationNode<?>) {
 				findNode(id, (INavigationNode<?>) child, closure, collector);
 
 			}
 		}
 
 	}
 
 	/**
 	 * Searches for the node with the given ID starting at the given node.<br>
 	 * The given closure returns the ID of the node.<br>
 	 * This method supports the wild cards: * and ?. The <b>first</b> matching
 	 * node is returned.
 	 * 
 	 * @param id
 	 *            ID
 	 * @param node
 	 *            start node
 	 * @param closure
 	 *            returns the ID of a node
 	 * @return list of found nodes or {@code null} if none was found
 	 */
 	private static List<INavigationNode<?>> findNode(final String id, final INavigationNode<?> node,
 			final IIdClosure closure) {
 		final List<INavigationNode<?>> result = new ArrayList<INavigationNode<?>>();
 		findNode(id, node, closure, result);
 		return result;
 
 	}
 
	private interface IIdClosure {
 		String getId(INavigationNode<?> node);
 	}
 
 }
