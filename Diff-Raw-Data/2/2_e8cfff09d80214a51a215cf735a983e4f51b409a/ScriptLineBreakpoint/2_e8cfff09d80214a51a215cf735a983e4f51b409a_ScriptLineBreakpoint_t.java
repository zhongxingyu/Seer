 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.core.model;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 
 public class ScriptLineBreakpoint extends AbstractScriptBreakpoint implements
 		IScriptLineBreakpoint {
 
 	protected String getMarkerId() {
 		return ScriptMarkerFactory.LINE_BREAKPOINT_MARKER_ID;
 	}
 
 	protected void addLineBreakpointAttributes(Map attributes, IPath path,
 			int lineNumber, int charStart, int charEnd) {
 		if (path != null) {
 			attributes.put(IMarker.LOCATION, path.toPortableString());
 		}
 		attributes.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
 		attributes.put(IMarker.CHAR_START, new Integer(charStart));
 		attributes.put(IMarker.CHAR_END, new Integer(charEnd));
 	}
 
 	public ScriptLineBreakpoint() {
 
 	}
 
 	public ScriptLineBreakpoint(final String debugModelId,
 			final IResource resource, final IPath path, final int lineNumber,
 			final int charStart, final int charEnd, final boolean add)
 			throws DebugException {
 
 		IWorkspaceRunnable wr = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 				// create the marker
 				setMarker(resource.createMarker(getMarkerId()));
 
 				// add attributes
 				final Map attributes = new HashMap();
 				addScriptBreakpointAttributes(attributes, debugModelId, true);
 				addLineBreakpointAttributes(attributes, path, lineNumber,
 						charStart, charEnd);
 
 				// set attributes
 				ensureMarker().setAttributes(attributes);
 
 				// add to breakpoint manager if requested
 				register(add);
 			}
 		};
 		run(getMarkerRule(resource), wr);
 	}
 
 	// ILineBreakpoint
 	public int getLineNumber() throws CoreException {
 		return ensureMarker().getAttribute(IMarker.LINE_NUMBER, -1);
 	}
 
 	public int getCharStart() throws CoreException {
 		return ensureMarker().getAttribute(IMarker.CHAR_START, -1);
 	}
 
 	public int getCharEnd() throws CoreException {
 		return ensureMarker().getAttribute(IMarker.CHAR_END, -1);
 
 	}
 
 	public String getResourceName() throws CoreException {
 		IResource resource = ensureMarker().getResource();
 		if (!resource.equals(getWorkspaceRoot()))
 			return resource.getName();
 
 		// else
 		String portablePath = (String) ensureMarker().getAttribute(
 				IMarker.LOCATION);
 		if (portablePath != null) {
 			IPath path = Path.fromPortableString(portablePath);
 			return path.lastSegment();
 		} else {
 			return null;
 		}
 	}
 
 	// IScriptLineBreakpoint
 	public IResource getResource() {
 		try {
 			final IResource resource = ensureMarker().getResource();
 			if (!resource.equals(getWorkspaceRoot())) {
 				return resource;
 			}
 		} catch (CoreException e) {
 			DLTKDebugPlugin.log(e);
 		}
 		return null;
 	}
 
 	private static IWorkspaceRoot getWorkspaceRoot() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 
 	public IPath getResourcePath() {
 		try {
 			final IResource resource = ensureMarker().getResource();
 			if (!resource.equals(getWorkspaceRoot()))
 				return ensureMarker().getResource().getFullPath();
 			final String path = (String) ensureMarker().getAttribute(
 					IMarker.LOCATION);
 			if (path != null) {
 				return Path.fromPortableString(path);
 			}
 		} catch (CoreException e) {
 			DLTKDebugPlugin.log(e);
 		}
 		return null;
 	}
 
 	public URI getResourceURI() {
 		try {
 			IResource resource = ensureMarker().getResource();
 			if (!resource.equals(getWorkspaceRoot())) {
 				final IEnvironment environment = EnvironmentManager
 						.getEnvironment(resource.getProject());
 				if (environment != null) {
 					final IFileHandle handle = environment.getFile(resource
 							.getLocationURI());
 					if (handle != null) {
 						try {
 							String path = handle.getPath().toString();
 							if (!path.startsWith("/")) { //$NON-NLS-1$
 								path = "/" + path; //$NON-NLS-1$
 							}
							return new URI("file", "", path, null); //$NON-NLS-1$ //$NON-NLS-2$
 						} catch (URISyntaxException e) {
 							if (DLTKCore.DEBUG) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 				// FIXME review later...
 				return makeUri(new Path(resource.getLocationURI().getPath()));
 			}
 
 			// else
 			String portablePath = (String) ensureMarker().getAttribute(
 					IMarker.LOCATION);
 			if (portablePath != null) {
 				IPath path = Path.fromPortableString(portablePath);
 				return makeUri(path);
 			}
 		} catch (CoreException e) {
 			DLTKDebugPlugin.log(e);
 		}
 		return null;
 	}
 
 	private static final String[] UPDATABLE_ATTRS = new String[] {
 			IMarker.LINE_NUMBER, IBreakpoint.ENABLED,
 			AbstractScriptBreakpoint.HIT_CONDITION,
 			AbstractScriptBreakpoint.HIT_VALUE,
 			AbstractScriptBreakpoint.EXPRESSION,
 			AbstractScriptBreakpoint.EXPRESSION_STATE };
 
 	public String[] getUpdatableAttributes() {
 		return UPDATABLE_ATTRS;
 	}
 }
