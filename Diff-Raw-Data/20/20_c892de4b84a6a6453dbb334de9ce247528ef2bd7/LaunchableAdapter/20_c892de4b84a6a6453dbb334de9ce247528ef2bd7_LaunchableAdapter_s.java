 /**********************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.model.*;
 /**
  * 
  */
 public class LaunchableAdapter implements ILaunchableAdapter {
 	private IConfigurationElement element;
 	private LaunchableAdapterDelegate delegate;
 
 	/**
 	 * LaunchableAdapter constructor comment.
 	 * 
 	 * @param element a configuration element
 	 */
 	public LaunchableAdapter(IConfigurationElement element) {
 		super();
 		this.element = element;
 	}
 
 	/**
 	 * Returns the id of this LaunchableAdapter.
 	 *
 	 * @return java.lang.String
 	 */
 	public String getId() {
 		return element.getAttribute("id");
 	}
 
 	public LaunchableAdapterDelegate getDelegate() {
 		if (delegate == null) {
 			try {
 				delegate = (LaunchableAdapterDelegate) element.createExecutableExtension("class");
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "Could not create delegate" + toString() + ": " + t.getMessage());
 			}
 		}
 		return delegate;
 	}
 
 	/**
 	 * @see ILaunchableAdapter#getLaunchable(IServer, IModuleArtifact)
 	 */
	public Object getLaunchable(IServer server, IModuleArtifact object) {
 		try {
 			return getDelegate().getLaunchable(server, object);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate " + toString() + ": " + e.getMessage());
 			return null;
 		}
 	}
 
 	/**
 	 * Return a string representation of this object.
 	 * 
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return "LaunchableAdapter[" + getId() + "]";
 	}
 }
