 /**
  * Copyright (c) 2011 Cloudsmith Inc. and other contributors, as listed below.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Cloudsmith
  * 
  */
 package org.cloudsmith.graph.emf;
 
 import org.cloudsmith.graph.elements.Vertex;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * A Vertex that is configured from an EObject, and that remembers the EObject.
  * 
  */
 public class EVertex extends Vertex {
 
 	public static final String DATA_KEY_INDEX = "index";
 
 	public static String idFor(EObject o) {
 		String result = null;
 		EAttribute a = o.eClass().getEIDAttribute();
 		if(a != null)
 			result = o.eGet(a).toString();
		if(result == null) {
			Resource r = o.eResource();

			if(r != null)
				result = EcoreUtil.getURI(o).toString();
		}
 		return result;
 	}
 
 	private static String labelFor(EObject o) {
 		return o.eClass().getName();
 	}
 
 	private static String styleClassFor(EObject o) {
 		return o.eClass().getName();
 	}
 
 	private final EObject vertexObj;
 
 	/**
 	 * Vertex configured from the given EObject. Label, and style class is the classname, id is
	 * the eId if one exists, the URI if EObject is contained in a resource, or null (default id in parent).
 	 * 
 	 * @param o
 	 */
 	public EVertex(EObject o) {
 		super(labelFor(o), styleClassFor(o), idFor(o));
 		vertexObj = o;
 	}
 
 	/**
 	 * Vertex configured from the given EObject and index position in a list. Label, and style class is the classname, id is
 	 * the eId if one exists, the URI if EObject is contained in a resource, or null (default id in parent).
 	 * 
 	 * @param o
 	 * @param index
 	 *            0-n if the Vertex represents an entry in a list
 	 */
 	public EVertex(EObject o, int index) {
 		super(labelFor(o), styleClassFor(o), idFor(o));
 		vertexObj = o;
 		this.getData().put("index", Integer.toString(index));
 	}
 
 	public EVertex(EObject o, int index, String label) {
 		super(label, styleClassFor(o), idFor(o));
 		vertexObj = o;
 		this.getData().put("index", Integer.toString(index));
 	}
 
 	public EVertex(EObject o, int index, String label, String styleClass) {
 		super(label, styleClass, idFor(o));
 		vertexObj = o;
 		this.getData().put("index", Integer.toString(index));
 	}
 
 	public EVertex(EObject o, int index, String label, String styleClass, String id) {
 		super(label, styleClass, id);
 		vertexObj = o;
 		this.getData().put("index", Integer.toString(index));
 
 	}
 
 	public EVertex(EObject o, String label) {
 		super(label, styleClassFor(o), idFor(o));
 		vertexObj = o;
 	}
 
 	public EVertex(EObject o, String label, String styleClass) {
 		super(label, styleClass, idFor(o));
 		vertexObj = o;
 	}
 
 	public EVertex(EObject o, String label, String styleClass, String id) {
 		super(label, styleClass, id);
 		vertexObj = o;
 	}
 
 	//
 	public EObject getEObject() {
 		return vertexObj;
 	}
 }
