 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.core.classpath;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.ClasspathContainerInitializer;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.jboss.tools.ws.core.messages.JBossWSCoreMessages;
 
 /**
  * @author Grid Qian
  */
 public class JBossWSRuntimeClassPathInitializer extends
 		ClasspathContainerInitializer {
 
 	public JBossWSRuntimeClassPathInitializer() {
 	}
 
 	private String segment;
 
 	@Override
 	public void initialize(IPath containerPath, IJavaProject project)
 			throws CoreException {
 
 		if (containerPath.segment(0).equals(
 				JBossWSCoreMessages.JBossWS_Runtime_Lib)) {
 			JBossWSRuntimeClasspathContainer container = new JBossWSRuntimeClasspathContainer(
 					containerPath);
 			segment = containerPath.segment(1);
 			JavaCore.setClasspathContainer(containerPath,
 					new IJavaProject[] { project },
 					new IClasspathContainer[] { container }, null);
 		}
 	}
 
 	public IClasspathEntry[] getEntries(IPath path) {
 		return new JBossWSRuntimeClasspathContainer(path).getClasspathEntries();
 	}
 
 	public class JBossWSRuntimeClasspathContainer implements
 			IClasspathContainer {
 		private IPath path;
 		private IClasspathEntry[] entries = null;
 
 		public JBossWSRuntimeClasspathContainer(IPath path) {
 			this.path = path;
 		}
 
 		public String getDescription() {
 			return JBossWSCoreMessages.JBossWS_Runtime;
 		}
 
 		public int getKind() {
 			return IClasspathContainer.K_APPLICATION;
 		}
 
 		public IPath getPath() {
 			return path;
 		}
 
 		public IClasspathEntry[] getClasspathEntries() {
 			if (entries == null) {
 				ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();
 				JBossWSRuntime jbws = JBossWSRuntimeManager.getInstance()
 						.findRuntimeByName(segment);
 				if (jbws != null) {

					List<String> jars = JBossWSRuntimeManager.getInstance()
							.getAllRuntimeJars(jbws);
 					for (String jar : jars) {
 						entryList.add(getEntry(new Path(jar)));
 					}
					entries = entryList.toArray(new IClasspathEntry[entryList
							.size()]);
 					if (entries == null)
 						return new IClasspathEntry[0];
 				}
 			}
 			return entries;
 		}
 
 		protected IClasspathEntry getEntry(IPath path) {
 			return JavaRuntime.newArchiveRuntimeClasspathEntry(path)
 					.getClasspathEntry();
 		}
 
 		public void removeEntry(String jarName) {
 			if (entries == null) {
 				return;
 			}

			List<IClasspathEntry> entriesList = new ArrayList<IClasspathEntry>();
 			for (IClasspathEntry entry : entries) {
				if (entry != null) {
					IPath path = entry.getPath();
					if (path != null) {
						if (path != null && path.lastSegment() != null
								&& path.lastSegment().equals(jarName)) {
							continue;
						}
					}
					entriesList.add(entry);
 				}
 			}
			entries = entriesList.toArray(new IClasspathEntry[0]);
 		}
 
 	}
 
 	public boolean filterJars(String jarName, ArrayList<IClasspathEntry> list) {
 		for (IClasspathEntry entry : list) {
 			if (entry.getPath().lastSegment().equals(jarName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
