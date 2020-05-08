 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.internal;
 
 import org.eclipse.core.resources.IProject;
 
 /**
  * Meta data model key descriptor used to lazily load a model when it is required
  *
  */
 public class ModelKeyDescriptor {
 	private final IProject project;
 	private final String domain;
 	private final String uri;
 	private final String key;
 	
 	/**
 	 * Constructor
 	 * @param project
 	 * @param domain
 	 * @param uri
 	 */
 	public ModelKeyDescriptor(final IProject project, final String domain, final String uri){
 		this.project = project;
 		this.domain = domain;
 		this.uri = fixJSPURIIfNecessary(uri);
 		final String proj = project != null ? project.getName() : "Null"; //$NON-NLS-1$
 		final StringBuffer buf = new StringBuffer(proj);
 		buf.append(":"); //$NON-NLS-1$
 		buf.append(this.domain);
 		buf.append(":"); //$NON-NLS-1$
 		buf.append(this.uri);
 		key = buf.toString();
 	}
 	
 	//this is a workaround for issue where jsp "uri" may upper or lower cased
 	private String fixJSPURIIfNecessary(final String tempuri) {
		if (tempuri.equals("jsp11")) //$NON-NLS-1$
 			return tempuri.toUpperCase();
 		return tempuri;
 	}
 
 	/**
 	 * @return domain id
 	 */
 	public final String getDomain(){
 		return domain;
 	}
 	
 	/**
 	 * @return model uri
 	 */
 	public final String getUri(){
 		return uri;
 	}
 
 	/**
 	 * @return project
 	 */
 	public final IProject getProject(){
 		return project;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public final String toString(){		
 		return key;
 	}
 	
 }
