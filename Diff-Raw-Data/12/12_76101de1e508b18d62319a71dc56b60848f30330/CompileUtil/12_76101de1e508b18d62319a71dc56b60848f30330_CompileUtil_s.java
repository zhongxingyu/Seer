 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaModelMarker;
 
 /**
  * @author artem
  *
  */
 public class CompileUtil {
 
 	public IStatus build(IProject project) {
 		try {
 			System.err.println("Compiling project " + project.getName());
			System.err.println("\t" + "Natures:");
			for (String s : project.getDescription().getNatureIds()) {
				System.err.println("\t" + s);
 			}
 		} catch (Exception ex) {
 			System.err.println("!!! JavaProject games are over with " + ex);
 		}
 		try {
 			project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 			IMarker[] compileErrors = getJavaErrors(project);
 			if (compileErrors.length > 0) {
 				StringBuffer sb = new StringBuffer();
 				sb.append(project.getName()).append(" has compilation problems:\n");
 				String errorsMsg = formatErrors(sb, compileErrors);
 				return new Status(Status.ERROR, Plugin.getPluginID(), 0, errorsMsg, null);
 			}
 			return Status.OK_STATUS;
 		} catch (CoreException ex) {
 			System.err.println("============> CompileUtil.build:");
 			ex.printStackTrace(System.err);
 			return ex.getStatus();
 		} catch (Exception ex) {
 			return Status.CANCEL_STATUS;
 		}
 	}
 
 	private IMarker[] collectJavaMarkers(IProject p) throws CoreException {
 		return p.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
 	}
 
 	private IMarker[] getJavaErrors(IProject p) throws CoreException {
 		return filterSevereMarkers(collectJavaMarkers(p));
 	}
 
 	private IMarker[] filterSevereMarkers(IMarker[] problems) throws CoreException {
 		ArrayList<IMarker> rv = new ArrayList<IMarker>(problems.length);
 		for (int i = 0; i < problems.length; i++) {
 			if (IMarker.SEVERITY_ERROR == ((Integer) problems[i].getAttribute(IMarker.SEVERITY)).intValue()) {
 				rv.add(problems[i]);
 			}
 		}
 		return rv.toArray(new IMarker[rv.size()]);
 	}
 
 	private String formatErrors(StringBuffer sb, IMarker[] compileErrors) {
 		for (int i = 0; i < compileErrors.length; i++) {
 			try {
 				sb.append(compileErrors[i].getResource().getName());
 				sb.append(':');
 				sb.append(compileErrors[i].getAttribute(IMarker.MESSAGE));
 			} catch (CoreException ex) {
 				sb.append("--ex:");
 				sb.append(ex.getMessage());
 			}
 			sb.append(",\n");
 		}
 		return sb.toString();
 	}
 }
