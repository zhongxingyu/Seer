 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.trees;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Tree;
 
 /**
  * CommonViewerListener listens to the property change event from Target Explorer's
  *  tree and update the viewer accordingly.
  */
 public abstract class CommonViewerListener extends TimerTask implements IPropertyChangeListener {
 	private static final long INTERVAL = 500;
 	private static final long MAX_IMMEDIATE_INTERVAL = 1000;
 	private static final Object NULL = new Object();
 	// The common viewer of Target Explorer view.
 	private TreeViewer viewer;
 	// Last time that the property event was processed.
 	private long lastTime = 0;
 	// The timer that process the property events periodically.
 	private Timer timer;
 	// The current queued property event sources.	
 	private Queue<Object> queue;
 
 	/***
 	 * Create an instance for the specified common viewer.
 	 *
 	 * @param viewer The common viewer from Target Explorer view.
 	 */
 	public CommonViewerListener(TreeViewer viewer) {
 		this.viewer = viewer;
 		this.timer = new Timer();
 		this.timer.schedule(this, INTERVAL, INTERVAL);
 		this.queue = new ConcurrentLinkedQueue<Object>();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 	 */
 	@Override
     public void propertyChange(final PropertyChangeEvent event) {
 		long now = System.currentTimeMillis();
 		Object object = event.getSource();
 		if(object == null)
 			object = NULL;
 		queue.offer(object);
 		if(now - lastTime > MAX_IMMEDIATE_INTERVAL) {
 			run();
 		}
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see java.util.TimerTask#run()
 	 */
 	@Override
 	public void run() {
 		if (!queue.isEmpty()) {
 			Object[] objects = queue.toArray();
 			Object object = mergeObjects(objects);
 			if (object instanceof List<?>) {
 				List<?> list = (List<?>) object;
 				if (list.isEmpty()) {
 					object = NULL;
 				}
 				else if (list.size() == 1) {
 					object = list.get(0);
 					if(isRootObject(object)) {
 						object = NULL;
 					}
 				}
 				else {
 					// If there are multiple root nodes, then select NULL as the final root.
 					object = getCommonAncestor((List<?>)object);
					System.out.println();
 					if(object == null) object = NULL;
 				}
 			}
 			processObject(object);
 			queue.clear();
 			lastTime = System.currentTimeMillis();
 		}
 	}
 	
 	/**
 	 * Get a object which is the common ancestor of the specified objects.
 	 * 
 	 * @param objects The object list.
 	 * @return The common ancestor.
 	 */
 	private Object getCommonAncestor(List<?> objects) {
 		Assert.isTrue(objects.size() > 1);
 		Object object1 = objects.get(0);
 		for (int i = 1; i < objects.size(); i++) {
 			Object object2 = objects.get(i);
 			object1 = getCommonAncestor(object1, object2);
 			if (object1 == null) return null;
 		}
 		return object1;
 	}
 
 	/**
 	 * Get the common ancestor of the specified two objects.
 	 *  
 	 * @param object1 The first object.
 	 * @param object2 The second object.
 	 * @return The common ancestor.
 	 */
 	private Object getCommonAncestor(Object object1, Object object2) {
 		Assert.isNotNull(object1);
 		Assert.isNotNull(object2);
 		if (isAncestorOf(object1, object2)) {
 			return object1;
 		}
 		if (isAncestorOf(object2, object1)) {
 			return object2;
 		}
 		Object ancestor = null;
 		Object parent1 = getParent(object1);
 		if(parent1 != null) {
 			ancestor = getCommonAncestor(parent1, object2);
 		}
 		if(ancestor != null) return ancestor;
 		Object parent2 = getParent(object2);
 		if(parent2 != null) {
 			ancestor = getCommonAncestor(object1, parent2);
 		}
 		return ancestor;
 	}
 	
 	/**
 	 * If the specified object is a root object;
 	 * 
 	 * @param object The object to be tested.
 	 * @return true if it is root object.
 	 */
 	protected abstract boolean isRootObject(Object object);
 
 	/**
 	 * Merge the current objects into an ancestor object.
 	 * 
 	 * @param objects The objects to be merged.
 	 * @return NULL or a list presenting the top objects.
 	 */
 	private Object mergeObjects(Object[] objects) {
 		// If one object is NULL, then return NULL
 		for (Object object : objects) {
 			if (object == NULL) return NULL;
 		}
 		// Remove duplicates.
 		List<Object> list = Arrays.asList(objects);
 		Set<Object> set = new HashSet<Object>(list);
 		objects = set.toArray();
 		
		list = Arrays.asList(objects);
 		List<Object> result = new ArrayList<Object>();
 		for (Object object : list) {
 			if (!hasAncestor(object, list)) {
 				result.add(object);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * If the target node has ancestor in the specified node list.
 	 *
 	 * @param target The node to be tested.
 	 * @param nodes The node list to search in.
 	 * @return true if the target node has an ancestor in the node list.
 	 */
 	private boolean hasAncestor(Object target, List<Object> nodes) {
 		for (Object node : nodes) {
 			if (isAncestorOf(node, target)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Judges if the object1 is an ancestor of the object2.
 	 * 
 	 * @param object1 The first object to be tested.
 	 * @param object2 The second object to be tested.
 	 * @return true if the first object is the ancestor of the second object2.
 	 */
 	private boolean isAncestorOf(Object object1, Object object2) {
 		if (object2 == null) return false;
 		Object parent = getParent(object2);
 		if (parent == object1) return true;
 		return isAncestorOf(object1, parent);
    }
 	
 	/**
 	 * Get the element's parent object.
 	 * 
 	 * @param element The element
 	 * @return The parent of the element.
 	 */
 	protected abstract Object getParent(Object element);
 
 	/**
 	 * Process the object node.
 	 * 
 	 * @param object The object to be processed.
 	 */
 	void processObject(final Object object) {
 		Assert.isNotNull(object);
 	    Tree tree = viewer.getTree();
 	    if (!tree.isDisposed()) {
 	    	Display display = tree.getDisplay();
 	    	if (display.getThread() == Thread.currentThread()) {
 	    		if (object != NULL) {
 	    			viewer.refresh(object);
 	    		}
 	    		else {
 	    			viewer.refresh();
 	    		}
 	    	}
 	    	else {
 	    		display.asyncExec(new Runnable() {
 	    			@Override
 	    			public void run() {
 	    				processObject(object);
 	    			}
 	    		});
 	    	}
 	    }
     }
 }
