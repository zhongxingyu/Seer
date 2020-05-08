 /*******************************************************************************
  * Copyright (c) 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.test.util.project;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.text.MessageFormat;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.swordfish.tooling.target.platform.test.Activator;
 import org.eclipse.swordfish.tooling.test.util.Util;
 import org.osgi.framework.Bundle;
 
 /**
  * A compare helper that allows to compare a workspace project with a reference project that is stored in a bundle.
  */
 public class ProjectCompare {
	public static final String OK_RESULT = "OK";
	private static String FILESETS_SIZES_DIFFER = "Expected {0} files but found {1} files in workspace.";
	private static String FILE_MISMATCH = "Files '{0}' and '{1}' don't match!";
	private static String COMPARE_FAILED = "Content of '{0}' is different from '{1}'";
	
 	private Bundle referenceBundle;
 	private String bundleResDir;
 	private IWorkspace workspace;
 	private ResourceFilter filter;
 	private WorkspaceResourceFinder wsResFind;
 	private BundleResourceFinder bndlResFind;
 	private String compareResult;
 
 	/**
 	 * Create a default instance using its containing bundle as location for reference projects
 	 * and the current workspace as location for generated projects.
 	 */
 	public ProjectCompare() {
 		this(Activator.getDefault().getBundle(), "resources/reference/", ResourcesPlugin.getWorkspace());
 	}
 
 	/**
 	 * Construct an instance of project compare with custom parameters
 	 * @param referenceBundle - the bundle that contains the reference project(s)
 	 * @param bundleResDir - the folder in which the reference projects are stored 
 	 * @param workspace - the workspace that contains the generated bundles.
 	 */
 	public ProjectCompare(Bundle referenceBundle, String bundleResDir, IWorkspace workspace) {
 		this.referenceBundle = referenceBundle;
 		this.bundleResDir = bundleResDir;
 		filter = new ResourceFilter();
 		filter.addExclusion(".*/bin/.*");
 		filter.addExclusion(".*/\\.svn/.*");
 		filter.addExclusion(".*/.*prefs/.*");
 		this.workspace = workspace;
 		wsResFind = new WorkspaceResourceFinder(workspace, filter);
 		bndlResFind = new BundleResourceFinder(this.referenceBundle, filter);
 	}
 
 	
 	/**
 	 * Compare contents of reference project with project from current workspace
 	 * @param projectName - the name of the project to be compared with the reference
 	 * @return ProjectCompare.OK_RESULT if contents are equal, otherwise an error string indicating which problem occurred.
 	 * @throws IOException if reading of files fails.
 	 */
 	public String compare(String projectName) throws IOException {
 		compareResult = OK_RESULT;
 		List<String> wsResources = wsResFind.findResources(projectName);
 		List<String> refResources = bndlResFind.findResources(bundleResDir + projectName);
 
 		if (wsResources.size() != refResources.size()) {
			compareResult = MessageFormat.format(FILESETS_SIZES_DIFFER, refResources.size(), wsResources.size());
 		}
 
 		Iterator<String> wsIt = wsResources.iterator();
 		Iterator<String> refIt = refResources.iterator();
 
 		while (OK_RESULT.equals(compareResult) && wsIt.hasNext() && refIt.hasNext()) {
 			String wsRes = wsIt.next();
 			String refRes = refIt.next();
 			
 			if (!refRes.endsWith(wsRes)) {
				compareResult = MessageFormat.format(FILE_MISMATCH, refRes, wsRes);
 
 			} else  {
 				InputStream refStream = openStream(referenceBundle, refRes);
 				InputStream genStream = openStream(workspace, wsRes);
 	
 				boolean isEqual = (wsRes.endsWith(".java")) ? 
 						ClassCompare.compareClasses(false, refStream, genStream) 
 							: Util.asString(genStream).equals(Util.asString(refStream));
 	
 				if (!isEqual) {
					compareResult = MessageFormat.format(COMPARE_FAILED, refRes, wsRes);
 				}
 			}
 		}
 		
 		return compareResult;
 	}
 
 	/**
 	 * Constructs an input stream to read a resource from a bundle
 	 * @param bundle - the bundle reference
 	 * @param path - the path to the resource to be accessed
 	 * @return an input stream to read the resource
 	 * @throws IOException - in case of errors
 	 */
 	private InputStream openStream(Bundle bundle, String path) throws IOException {
 		return new BufferedInputStream(bundle.getResource(path).openStream());
 	}
 	
 	/**
 	 * Constructs an input stream to read a resource from the workspace
 	 * @param ws - the workspace to use
 	 * @param path - the path to the resource (project/...)
 	 * @return an input stream to read the resource
 	 * @throws IOException in case getting the contents of the workspace file causes an exception.
 	 */
 	private InputStream openStream(IWorkspace ws, String path) throws IOException {
 		IResource res = (IFile) ws.getRoot().findMember(path);
 		try {
 			return new BufferedInputStream(((IFile) res).getContents());			
 		} catch (CoreException cx) {
 			IOException iox = new IOException();
 			iox.initCause(cx);
 			throw iox;
 		}
 	}
 }
