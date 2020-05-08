 /** Copyright 2004, 2005 The Apache Software Foundation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * Addendum:
  * The original version of this file belongs to the retired Apache HiveMind 
  * project: http://hivemind.apache.org/hivemind1/index.html
  * The original files can be found in hivemind-1.1.jar in package org.apache.hivemind.order
  * 
  * The original version has been modified entirely by Riena committers such:
  * - removed dependencies from HiveMind
  * - added generics, uses extended for loops, ..
  * - adapted to Riena/Eclipse coding conventions
  * - moved internal classes (separate files) into one file 
  */
 package org.eclipse.riena.core.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 
 /**
  * Used to order objects into an "execution" order. Each object must have a
  * name. It may specify a list of pre-requisites and a list of post-requisites.
  * 
  * @author Howard Lewis Ship
  */
 public class Orderer<T> {
 
 	private final List<Ordering<T>> orderingsList = new ArrayList<Ordering<T>>();
 	private final Map<String, Ordering<T>> orderingsMap = new HashMap<String, Ordering<T>>();
 
 	private Node<T> leader;
 	private Node<T> trailer;
 
 	private static final String HIGHEST_DEPENDENCY = "*"; //$NON-NLS-1$
 
 	private final static Logger LOGGER = Log4r.getLogger(Orderer.class);
 
 	/**
 	 * Adds a new object. All invocations of
 	 * {@link #add(Object, String, String, String)} should occur before invoking
 	 * {@link #getOrderedObjects()}.
 	 * 
 	 * @param object
 	 *            an object to be sorted into order based on preReqs and
 	 *            postReqs
 	 * @param name
 	 *            a unique name for the
 	 * @param preReqs
 	 *            a comma-separated list of the names of objects that should
 	 *            precede this object in the list (or null)
 	 * @param postReqs
 	 *            a comma-separated list of the names of objects that should
 	 *            follow this object in the list (or null)
 	 */
 	public void add(final T object, final String name, final String preReqs, final String postReqs) {
 		Ordering<T> ordering = getOrderable(name);
 
 		if (ordering != null) {
 			throw new OrdererFailure("More then one object with the name '" + name + "'."); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		ordering = new Ordering<T>(object, name, preReqs, postReqs);
 
 		orderingsMap.put(name, ordering);
 		orderingsList.add(ordering);
 	}
 
 	private Ordering<T> getOrderable(final String name) {
 		return orderingsMap.get(name);
 	}
 
 	/**
 	 * Uses the information provided by
 	 * {@link #add(Object, String, String, String)} to order the objects into an
 	 * appropriate order based on the pre- and post-reqts provided. Errors such
 	 * as cyclic dependencies or unrecognized names are logged and ignored.
 	 */
 	public List<T> getOrderedObjects() {
 		if (orderingsMap.isEmpty()) {
 			return Collections.emptyList();
 		}
 		final Map<Ordering<T>, Node<T>> nodeMap = new HashMap<Ordering<T>, Node<T>>();
 		try {
 			initializeGraph(nodeMap);
 			return trailer.getOrder();
 		} catch (final OrdererFailure e) {
 			throw new OrdererFailure("Found a conflict within Orderings [" + StringUtils.join(orderingsList, ",") //$NON-NLS-1$ //$NON-NLS-2$
 					+ "]. " + e.getMessage()); //$NON-NLS-1$
 		}
 	}
 
 	private void initializeGraph(final Map<Ordering<T>, Node<T>> nodeMap) {
 		addNodes(nodeMap);
 
 		if (leader == null) {
 			leader = new Node<T>(null, "*-leader-*"); //$NON-NLS-1$
 		}
 		if (trailer == null) {
 			trailer = new Node<T>(null, "*-trailer-*"); //$NON-NLS-1$
 		}
 		addDependencies(nodeMap);
 	}
 
 	private Node<T> getNode(final Map<Ordering<T>, Node<T>> nodeMap, final String name) {
 		return nodeMap.get(getOrderable(name));
 	}
 
 	private void addNodes(final Map<Ordering<T>, Node<T>> nodeMap) {
 		for (final Ordering<T> ordering : orderingsList) {
 			final Node<T> node = new Node<T>(ordering.getObject(), ordering.getName());
 			nodeMap.put(ordering, node);
 
 			if (HIGHEST_DEPENDENCY.equals(ordering.getPostReqs())) {
 				if (leader != null) {
 					throw new OrdererFailure(
 							"More than one leader. Conflicting '" + leader.getName() + "' (ordered unknown) and '" + node.getName() + "' (first)."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				}
 				leader = node;
 			}
 
 			if (HIGHEST_DEPENDENCY.equals(ordering.getPreReqs())) {
 				if (trailer != null) {
 					throw new OrdererFailure(
 							"More than one trailer. Conflicting '" + trailer.getName() + "' (ordered unknown) and '" + node.getName() + "' (last)."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				}
 				trailer = node;
 			}
 		}
 	}
 
 	private void addDependencies(final Map<Ordering<T>, Node<T>> nodeMap) {
 		for (final Ordering<T> ordering : orderingsList) {
 			addDependencies(nodeMap, ordering, getNode(nodeMap, ordering.getName()));
 		}
 	}
 
 	private void addDependencies(final Map<Ordering<T>, Node<T>> nodeMap, final Ordering<T> orderable,
 			final Node<T> node) {
 		addPreReqs(nodeMap, orderable, node);
 		addPostReqs(nodeMap, orderable, node);
 
 		if (node != leader) {
 			node.addDependency(leader);
 		}
 		if (node != trailer) {
 			trailer.addDependency(node);
 		}
 	}
 
 	private void addPreReqs(final Map<Ordering<T>, Node<T>> nodeMap, final Ordering<T> ordering, final Node<T> node) {
 		final String preReqs = ordering.getPreReqs();
 
 		if (HIGHEST_DEPENDENCY.equals(preReqs)) {
 			return;
 		}
 
 		final List<String> names = new RichString(preReqs).toList();
 
 		for (final String beforeName : names) {
 			final Node<T> beforeNode = getNode(nodeMap, beforeName);
 			if (beforeNode == null) {
 				if (beforeName.length() > 0) {
 					LOGGER.log(LogService.LOG_WARNING, "bad dependency: " + beforeName + ", " + ordering); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			} else {
 				node.addDependency(beforeNode);
 			}
 		}
 	}
 
 	private void addPostReqs(final Map<Ordering<T>, Node<T>> nodeMap, final Ordering<T> ordering, final Node<T> node) {
 		final String postReqs = ordering.getPostReqs();
 
 		if (HIGHEST_DEPENDENCY.equals(postReqs)) {
 			return;
 		}
 
 		final List<String> names = new RichString(postReqs).toList();
 
 		for (final String afterName : names) {
 			final Node<T> afterNode = getNode(nodeMap, afterName);
 			if (afterNode == null) {
 				if (afterName.length() > 0) {
 					LOGGER.log(LogService.LOG_WARNING, "bad dependency: " + afterName + ", " + ordering); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 			} else {
 				afterNode.addDependency(node);
 			}
 		}
 	}
 
 	private static class Node<T> {
 
 		private final T object;
 		private final String name;
 		private final List<Node<T>> dependencies = new ArrayList<Node<T>>();
 
 		public Node(final T object, final String name) {
 			this.object = object;
 			this.name = name;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public void addDependency(final Node<T> node) {
 			if (node.isReachable(this)) {
 				throw new OrdererFailure("A cycle has been detected between '" + name + "' and '" //$NON-NLS-1$ //$NON-NLS-2$
 						+ node.getName() + "'."); //$NON-NLS-1$
 			}
 			dependencies.add(node);
 		}
 
 		private boolean isReachable(final Node<T> node) {
 			boolean reachable = (node == this);
 			for (final Node<T> dependency : dependencies) {
 				if (reachable) {
 					return true;
 				}
 				reachable = (dependency == node) ? true : dependency.isReachable(node);
 			}
 			return reachable;
 		}
 
 		public List<T> getOrder() {
 			final List<T> result = new ArrayList<T>();
 			fillOrder(result);
 
 			return result;
 		}
 
 		private void fillOrder(final List<T> result) {
			if (contains(result, object)) {
 				return;
 			}
 			for (final Node<T> dependency : dependencies) {
 				dependency.fillOrder(result);
 			}
 			if (object != null) {
 				result.add(object);
 			}
 		}

		/**
		 * Avoid {@code List.contains()} because that calls {@code equals} which
		 * in case that the object is a dynamic proxy implementing a lazy
		 * creation pattern this might cause the object to be created which
		 * might be too early.
		 * 
		 * @param result
		 * @param object
		 * @return
		 */
		private boolean contains(final List<T> result, final T object) {
			for (final T o : result) {
				if (o == object) {
					return true;
				}
			}
			return false;
		}
 	}
 
 	private static class Ordering<T> {
 		private final T object;
 		private final String name;
 		private final String preReqs;
 		private final String postReqs;
 
 		Ordering(final T object, final String name, final String preReqs, final String postReqs) {
 			this.object = object;
 			this.name = name;
 			this.preReqs = preReqs;
 			this.postReqs = postReqs;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public T getObject() {
 			return object;
 		}
 
 		public String getPostReqs() {
 			return postReqs;
 		}
 
 		public String getPreReqs() {
 			return preReqs;
 		}
 
 		@Override
 		public String toString() {
 			return "Ordering [name=" + name + ", preReqs=" + preReqs + ", postReqs=" + postReqs + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 		}
 
 	}
 
 	public static class OrdererFailure extends UtilFailure {
 
 		private static final long serialVersionUID = 5018828794366775202L;
 
 		public OrdererFailure(final String msg) {
 			super(msg);
 		}
 
 	}
 }
