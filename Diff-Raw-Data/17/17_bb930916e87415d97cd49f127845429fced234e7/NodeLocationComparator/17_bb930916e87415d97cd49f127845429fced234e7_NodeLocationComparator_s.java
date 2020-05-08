 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.utils;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
import org.eclipse.jface.text.Assert;
 import org.w3c.dom.Node;
 
 /**
  * To sort the location of tags, this comparator is used to compare tags' order.
  * 
  * @author mengbo
  */
 public class NodeLocationComparator implements Comparator {
 	private final static Map orders = new HashMap();
 
 	private final static Integer DEFAULT_ORDER = new Integer(Integer.MAX_VALUE);
 
 	private static NodeLocationComparator _instance = new NodeLocationComparator();
 	static {
 		orders.put("taglib", new Integer(0));
 		orders.put("directive.taglib", new Integer(0));
 		orders.put("head", new Integer(1));
 	}
 
 	private NodeLocationComparator() {
         // no external instantiation
 	}
 
 	public static NodeLocationComparator getInstance() {
 		return _instance;
 	}
 
 	/**
 	 * The object to be compared could be Node or tag name.
 	 * 
 	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 	 */
 	public int compare(Object o1, Object o2) {
 		Assert.isTrue((o1 instanceof Node || o1 instanceof String)
 				&& (o2 instanceof Node || o2 instanceof String));
 		Integer i1 = getOrder(o1);
 		Integer i2 = getOrder(o2);
 		return i1.compareTo(i2);
 	}
 
 	private Integer getOrder(Object n) {
 		String name = null;
 		if (n instanceof Node) {
 			name = ((Node) n).getLocalName();
 		} else {
 			name = (String) n;
 		}
 		if (name != null) {
 			Object order = orders.get(name);
 			if (order != null) {
 				return (Integer) order;
 			}
 		}
 		return DEFAULT_ORDER;
 	}
 }
