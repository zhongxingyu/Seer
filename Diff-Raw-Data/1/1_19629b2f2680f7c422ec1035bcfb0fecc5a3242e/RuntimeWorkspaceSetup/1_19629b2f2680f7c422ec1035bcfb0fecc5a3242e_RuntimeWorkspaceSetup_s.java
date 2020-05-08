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
 package org.eclipse.gmf.tests.setup;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.codegen.ecore.Generator;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.osgi.framework.Bundle;
 
 /**
  * ALMOST TRUE: With PDE, we need source code in the running workspace to allow compilation of our code 
  * (because PDE doesn't reexport set of plugins from it's running configuration, and it's no longer possible 
  * to set Target Platform to "same as running" as it was back in Eclipse 2.x).
  * 
  * !!! NEW !!!
  * 
  * Now, we managed to compile against linked binary folders, although using linked content instead of plugins 
  * requires us to explicitly add some plugins earlier available through plugin re-export (namely, oe.jface.text) 
  *  
  * 
  * Classloading works because there's -dev argument in the command line. With PDE launch, it's done by PDE.
  * Without PDE, running tests as part of the build relies on Eclipse Testing Framework's org.eclipse.test_3.1.0/library.xml
  * which specifies "-dev bin". Once it's not specified, or new format (properties file with plugin-id=binfolder) 
  * is in use, classloading of the generated code will fail and another mechanism should be invented then.
  * 
  * If you get ClassNotFoundException while running tests in PDE environment, try to set read-only attribute for the next file:
  * 'development-workspace'\.metadata\.plugins\org.eclipse.pde.core\'JUnitLaunchConfigName'\dev.properties
  * @author artem
  */
 public class RuntimeWorkspaceSetup {
 
 	/**
 	 * Copy of <code>PDECore.CLASSPATH_CONTAINER_ID</code>
 	 */
 	private static final String PLUGIN_CONTAINER_ID = "org.eclipse.pde.core.requiredPlugins"; //$NON-NLS-1$
 
 	private boolean isDevLaunchMode;
 
 	public RuntimeWorkspaceSetup() {
 		isDevLaunchMode = isDevLaunchMode();
 	}
 
 	/**
 	 * Copy (almost, except for strange unused assignment) of <code>PDECore.isDevLaunchMode()</code>
 	 */
 	private static boolean isDevLaunchMode() {
 		String[] args = Platform.getApplicationArgs();
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].equals("-pdelaunch")) { //$NON-NLS-1$
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @return <code>this</code> for convenience
 	 */
 	// TODO Refactor to clear away similar code (CodeCompilationTest, RuntimeWorkspaceSetup, GenProjectSetup)
 	public RuntimeWorkspaceSetup init() throws Exception {
 		ensureJava14();
 		if (isDevLaunchMode) {
 			// Need to get some gmf source code into target workspace 
 			importDevPluginsIntoRunTimeWorkspace(new String[] {
 //					"org.apache.batik",
 					"org.eclipse.gmf.runtime.notation", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.notation.edit", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.core", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.ui", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.draw2d.ui", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.draw2d.ui.render", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.gef.ui", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.ui.services", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.emf.type.core", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.emf.clipboard.core", //$NON-NLS-1$
 					"org.eclipse.emf.validation", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.emf.core", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.ui.services.action", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.ui.action", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.ui.action.ide", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.emf.ui", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.emf.commands.core", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.core", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.common.ui.services.properties", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.emf.ui.properties", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.actions", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.properties", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.providers", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.providers.ide", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.render", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.resources.editor", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide", //$NON-NLS-1$
 					"org.eclipse.gmf.runtime.notation.providers", //$NON-NLS-1$
 					"antlr", //$NON-NLS-1$					
 					"org.eclipse.emf.ocl", //$NON-NLS-1$
 					"org.eclipse.emf.query", //$NON-NLS-1$	
 					"org.eclipse.emf.query.ocl", //$NON-NLS-1$
					"org.eclipse.gmf.tests", //$NON-NLS-1$
 					//
 					"org.eclipse.emf.edit", //$NON-NLS-1$
 					"org.eclipse.emf.transaction", //$NON-NLS-1$
 					"org.eclipse.emf.workspace", //$NON-NLS-1$					
 			});
 		}
 		return this;
 	}
 
 	public static IProject getSOSProject() {
 		return ResourcesPlugin.getWorkspace().getRoot().getProject(".SOSProject"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Another approach - output binary folders of required plugins are linked as subfolders 
 	 * of our own sosProject (created in the target workspace). Then, we could use library classpathEntries
 	 * (details why we should use _workspace_ paths for libraries could be found at 
 	 * <code>org.eclipse.jdt.internal.core.builder.NameEnvironment#computeClasspathLocations</code>)
 	 *  
 	 * TODO don't assume workspace is clear, check sosProject existence first
 	 * TODO utilize GenDiagram.requiredPluginIDs once it's a field (i.e. add oe.jface.text and don't create plugin project then, just plain project with links
 	 */
 	private void importDevPluginsIntoRunTimeWorkspace(String[] pluginIDs) throws CoreException {
 		IProject p = getSOSProject();
 		final Path srcPath = new Path('/' + p.getName() + "/src"); //$NON-NLS-1$
 		Generator.createEMFProject(srcPath, null, Collections.EMPTY_LIST, new NullProgressMonitor(), Generator.EMF_PLUGIN_PROJECT_STYLE, null);
 		
 		StringBuffer pluginXmlContent = new StringBuffer();
 		pluginXmlContent.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<?eclipse version=\"3.0\"?>\n<plugin "); //$NON-NLS-1$
 		pluginXmlContent.append(" version=\"1.0.0\" name='%providerName' id='"); //$NON-NLS-1$
 		pluginXmlContent.append(p.getName());
 		pluginXmlContent.append("'>\n<requires>\n"); //$NON-NLS-1$
 		pluginXmlContent.append("<import plugin='org.eclipse.jface.text' export='true'/>\n"); //$NON-NLS-1$
 		pluginXmlContent.append("<import plugin='org.eclipse.ui.views.properties.tabbed' export='true'/>\n"); //$NON-NLS-1$
 
 		ClasspathEntry[] classpathEntries = getClasspathEntries(pluginIDs);
 		for (int i = 0; i < classpathEntries.length; i++) {
 			classpathEntries[i].importTo(p, pluginXmlContent);
 		}
 
 		pluginXmlContent.append("</requires>\n</plugin>"); //$NON-NLS-1$
 		p.getFile("plugin.xml").create(new ByteArrayInputStream(pluginXmlContent.toString().getBytes()), true, new NullProgressMonitor()); //$NON-NLS-1$
 	}
 
 	private ClasspathEntry[] getClasspathEntries(String[] pluginIDs) {
 		ArrayList/*<ClasspathEntry>*/ entries = new ArrayList/*<ClasspathEntry>*/(pluginIDs.length); 
 		for (int i = 0; i < pluginIDs.length; i++) {
 			ClasspathEntry nextEntry = new ClasspathEntry(pluginIDs[i]);
 			if (nextEntry.isValid()) {
 				entries.add(nextEntry);				
 			} else {
 				System.out.println("Bundle " + pluginIDs[i] + " is missing, skipped."); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		return (ClasspathEntry[]) entries.toArray(new ClasspathEntry[entries.size()]);
 	}
 
 	private IJavaProject asJavaProject(IProject p) {
 		return JavaCore.create(p);
 	}
 
 	/**
 	 * TODO uniqueClassPathEntries is not needed if diagramProj gets here only once. It's not the case
 	 * now - refactor LinkCreationConstraintsTest to utilize genProject created in AuditRulesTest (?)
 	 * TODO refactor with ClasspathContainerInitializer - just for the sake of fixing the knowledge 
 	 */
 	public void updateClassPath(IProject diagramProj) throws CoreException {
 		if (!isDevLaunchMode) {
 			return;
 		}
 		IResource[] members;
 		try {
 			members = getSOSProject().members();
 		} catch (CoreException ex) {
 			ex.printStackTrace();
 			members = new IResource[0];
 		}
 		final IJavaProject sosJavaPrj = asJavaProject(getSOSProject());
 		IClasspathEntry[] cpOrig = asJavaProject(diagramProj).getRawClasspath();
 		ArrayList rv = new ArrayList(10 + cpOrig.length + members.length);
 		IClasspathContainer c = JavaCore.getClasspathContainer(new Path(PLUGIN_CONTAINER_ID), sosJavaPrj);
 		if (c != null) {
 			IClasspathEntry[] cpAdd = c.getClasspathEntries();
 			rv.addAll(Arrays.asList(cpAdd));
 		}
 		for (int i = 0; i < members.length; i++) {
 			if (!members[i].isLinked()) {
 				continue;
 			}
 			rv.add(JavaCore.newLibraryEntry(members[i].getFullPath(), null, null));
 		}
 
 		final Set uniqueClassPathEntries = new HashSet();
 		IClasspathEntry[] cpOrigResolved = asJavaProject(diagramProj).getResolvedClasspath(true);
 		for (int i = 0; i < cpOrigResolved.length; i++) {
 			uniqueClassPathEntries.add(cpOrigResolved[i].getPath());
 		}
 		for (Iterator it = rv.iterator(); it.hasNext();) {
 			IClasspathEntry next = (IClasspathEntry) it.next();
 			if (uniqueClassPathEntries.contains(next.getPath())) {
 				it.remove();
 			} else {
 				uniqueClassPathEntries.add(next.getPath());
 			}
 		}
 		rv.addAll(Arrays.asList(cpOrig));
 		
 		IClasspathEntry[] cpNew = (IClasspathEntry[]) rv.toArray(new IClasspathEntry[rv.size()]);
 		asJavaProject(diagramProj).setRawClasspath(cpNew, new NullProgressMonitor());
 	}
 
 	/**
 	 * at least
 	 */
 	public void ensureJava14() {
 		if (!JavaCore.VERSION_1_4.equals(JavaCore.getOption(JavaCore.COMPILER_SOURCE))) {
 			Hashtable options = JavaCore.getOptions();
 			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
 			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
 			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);
 			JavaCore.setOptions(options);
 		}
 	}
 	
 	private class ClasspathEntry {
 		
 		private String myPluginID;
 		private URL myBundleURL;
 		private String myRelativePath;
 		private File myBundleFile;
 		private File myClassesContainerFile;
 
 		private ClasspathEntry(String pluginID) {
 			myPluginID = pluginID;
 		}
 
 		public void importTo(IProject p, StringBuffer pluginXmlContent) {
 			if (!getClassesContainerFile().exists()) {
 				pluginXmlContent.append("<import plugin='"); //$NON-NLS-1$
 				pluginXmlContent.append(myPluginID);
 				pluginXmlContent.append("' export='true'/>\n"); //$NON-NLS-1$
 			} else {
 				if (getClassesContainerFile().isDirectory()) {
 					String entryName = getBundleFile().getName().replace('.', '_');
 					IFolder folder = p.getFolder(entryName);
 					try {
 						folder.createLink(new Path(getClassesContainerFile().getAbsolutePath()), IResource.REPLACE, new NullProgressMonitor());
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 				} else if (getClassesContainerFile().isFile()) {
 					String entryName = getClassesContainerFile().getName();
 					IFile file = p.getFile(entryName);
 					try {
 						file.createLink(new Path(getClassesContainerFile().getAbsolutePath()), IResource.REPLACE, new NullProgressMonitor());
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		
 		private File getClassesContainerFile() {
 			if (myClassesContainerFile == null) {
 				myClassesContainerFile = new File(getBundleFile(), getRelativePath());
 			}
 			return myClassesContainerFile;
 		}
 		
 		private File getBundleFile() {
 			if (myBundleFile == null) {
 				myBundleFile = new File(getBundleURL().getFile());
 			}
 			return myBundleFile;
 		}
 		
 		private String getRelativePath() {
 			if (myRelativePath == null) {
 				if ("antlr".equals(myPluginID)) { //$NON-NLS-1$
 					myRelativePath = "/lib/antlr.jar"; //$NON-NLS-1$
 				}
 				else {
 					myRelativePath = "/bin/"; //$NON-NLS-1$
 				}
 			}
 			return myRelativePath;
 		}
 		
 		private URL getBundleURL() {
 			if (myBundleURL == null) {
 				Bundle bundle = Platform.getBundle(myPluginID);
 				if (bundle == null) {
 					throw new NullPointerException("No plugin '" + myPluginID + "' found in the platform");
 				}
 				try {
 					myBundleURL = Platform.resolve(bundle.getEntry("/")); //$NON-NLS-1$
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			return myBundleURL;
 		}
 		
 		public boolean isValid() {
 			return getBundleURL() != null;
 		}
 		
 	}
 }
