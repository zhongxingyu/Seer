 /*******************************************************************************
 * Copyright (c) 2008, 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.impl.filters.business;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 /**
  * Filter for business rules
  * 
  * @author <a href="mailto:nathalie.lepine@obeo.fr">Nathalie Lepine</a>
  */
 public abstract class BusinessViewerFilter extends ViewerFilter {
 
 	/**
 	 * the name
 	 */
 	protected String name;
 
 	/**
 	 * the description
 	 */
 	protected String description;
 
 	/**
 	 * current object on which the filter is applicated
 	 */
 	protected EObject current;
 
 	/**
 	 * empty line or not
 	 */
 	protected boolean nullable;
 
 	/**
 	 * @param eObject
 	 * @param nullable
 	 */
 	public BusinessViewerFilter(EObject eObject, boolean nullable) {
 		super();
 		this.current = eObject;
 		this.nullable = nullable;
 	}
 
 
 	/**
 	 * @param eObject
 	 * @param name
 	 */
 	public BusinessViewerFilter(EObject eObject, String name, boolean nullable) {
 		super();
 		this.name = name;
 		this.current = eObject;
 		this.nullable = nullable;
 	}
 
 	/**
 	 * @param eObject
 	 * @param name
 	 * @param description
 	 */
 	public BusinessViewerFilter(EObject eObject, String name, String description, boolean nullable) {
 		super();
 		this.name = name;
 		this.description = description;
 		this.current = eObject;
 		this.nullable = nullable;
 	}
 
 	/**
 	 * @return description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @return current object
 	 */
 	public EObject getCurrent() {
 		return current;
 	}
 
 	/**
 	 * @return if it is nullable
 	 */
 	public boolean isNullable() {
 		return nullable;
 	}
 
 }
