 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.internal;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.osgi.util.NLS;
 import org.jboss.tools.common.el.core.resolver.TypeInfoCollector;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.kb.KbProjectFactory;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 import org.jboss.tools.jst.web.kb.internal.scanner.IFileScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.LibraryScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.UsedJavaProjectCheck;
 import org.jboss.tools.jst.web.kb.internal.scanner.XMLScanner;
 import org.osgi.framework.Bundle;
 
 /**
  * 
  * @author V.Kabanovich
  *
  */
 public class KbBuilder extends IncrementalProjectBuilder {
 	public static String BUILDER_ID = WebKbPlugin.PLUGIN_ID + ".kbbuilder"; //$NON-NLS-1$
 
 	KbResourceVisitor resourceVisitor = null;
 
 	protected KbProject getKbProject() {
 		IProject p = getProject();
 		if(p == null) return null;
 		return (KbProject)KbProjectFactory.getKbProject(p, false);
 	}
 
 	KbResourceVisitor getResourceVisitor() {
 		if(resourceVisitor == null) {
 			KbProject p = getKbProject();
 			resourceVisitor = new KbResourceVisitor(p);
 		}
 		return resourceVisitor;
 	}
 
 	class SampleDeltaVisitor implements IResourceDeltaVisitor {
 		/*
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				return getResourceVisitor().getVisitor().visit(resource);
 			case IResourceDelta.REMOVED:
 				KbProject p = getKbProject();
 				if(p != null) p.pathRemoved(resource.getFullPath());
 				break;
 			case IResourceDelta.CHANGED:
 				return getResourceVisitor().getVisitor().visit(resource);
 			}
 			//return true to continue visiting children.
 			return true;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.core.resource.InternalProjectBuilder#build(int,
 	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected IProject[] build(int kind, Map<String,String> args, IProgressMonitor monitor)
 			throws CoreException {
 		KbProject sp = getKbProject();
 		if(sp == null) {
 			return null; 
 		}
 		
 		long begin = System.currentTimeMillis();
 		
 		sp.postponeFiring();
 		
 		try {
 		
 			sp.resolveStorage(kind != FULL_BUILD);
 			
 			if(sp.getClassPath().update()) {
 				sp.getClassPath().process();
 			} else if(sp.getClassPath().hasToUpdateProjectDependencies()) {
 				sp.getClassPath().validateProjectDependencies();
 			}
 			new UsedJavaProjectCheck().check(sp);
 
 			TypeInfoCollector.cleanCache();
 
 			if (kind == FULL_BUILD) {
 				fullBuild(monitor);
 			} else {
 				IResourceDelta delta = getDelta(getProject());
 				if (delta == null) {
 					fullBuild(monitor);
 				} else {
 					incrementalBuild(delta, monitor);
 				}
 			}
 			long end = System.currentTimeMillis();
 			sp.fullBuildTime += end - begin;
 //			try {
 //				sp.store();
 //			} catch (IOException e) {
 //				WebModelPlugin.getPluginLog().logError(e);
 //			}
 			
 //			sp.postBuild();
 		
 		} finally {
 			sp.fireChanges();
 		}
 		resourceVisitor = null;
 	
 		buildExtensionModels(kind, args, monitor);
 
 		return null;
 	}
 
 	protected void fullBuild(final IProgressMonitor monitor)
 			throws CoreException {
 		try {
 			PageContextFactory.getInstance().cleanUp(getProject());
 			getProject().accept(getResourceVisitor().getVisitor());
 		} catch (CoreException e) {
 			WebModelPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		PageContextFactory.getInstance().cleanUp(delta);
 		// the visitor does the work.
 		delta.accept(new SampleDeltaVisitor());
 	}
 	
 	/**
 	 * Access to xml scanner for test.
 	 * @return
 	 */
 	public static IFileScanner getXMLScanner() {
 		return new XMLScanner();
 	}
 
 	/**
 	 * Access to library scanner for test.
 	 * @return
 	 */
 	public static IFileScanner getLibraryScanner() {
 		return new LibraryScanner();
 	}
 
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		KbProject sp = getKbProject();
 		if(sp != null) sp.clean();
 		PageContextFactory.getInstance().cleanUp(getProject());
 	}
 
 	static String[][] builders = {
 		{"org.jboss.tools.jsf", "org.jboss.tools.jsf.jsf2.bean.build.JSF2ProjectBuilder"}
 	};
 
 	void buildExtensionModels(int kind, Map args, IProgressMonitor monitor) throws CoreException {
 		for (int i = 0; i < builders.length; i++) {
 			Bundle b = Platform.getBundle(builders[i][0]);
 			if(b != null) {
 				try {
 					IncrementalProjectBuilder builder = (IncrementalProjectBuilder)b.loadClass(builders[i][1]).newInstance();
 					KbProjectFactory.setProjectToBuilder(builder, getProject());
 					((IIncrementalProjectBuilderExtension)builder).build(kind, args, monitor);
 				} catch (InstantiationException e) {
 					WebKbPlugin.getDefault().logError(e);
 				} catch (IllegalAccessException e) {
 					WebKbPlugin.getDefault().logError(e);
 				} catch (ClassNotFoundException e) {
 					WebKbPlugin.getDefault().logError(e);
 				}
 			}
 		}
 	}
 }
