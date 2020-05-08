 /**
  * Copyright (c) 2010, Cloudsmith Inc and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * Contributors:
  * - Cloudsmith Inc - initial API and implementation.
  */
 package org.cloudsmith.graph.emf;
 
 import java.util.WeakHashMap;
 
 import org.cloudsmith.graph.IGraphElement;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 
 /**
  * The GraphElementAdapter associates an instance of IGraphElement with a (weak) key.
 * The intended use is to associate a Vertex or Edge with an EObject keyed by some context (like a graph, but it is
 * not limited to any graph related class).
  * The adapter allows association of one graph element per key.
  */
 public class GraphElementAdapter extends AdapterImpl {
 	WeakHashMap<Object, IGraphElement> associatedInfo = new WeakHashMap<Object, IGraphElement>();
 
 	public IGraphElement getGraphElement(Object key) {
 		return associatedInfo.get(key);
 	}
 
 	@Override
 	public boolean isAdapterForType(Object type) {
 		return type == GraphElementAdapter.class;
 	}
 
 	public void setAssociatedInfo(Object key, IGraphElement info) {
 		associatedInfo.put(key, info);
 	}
 }
