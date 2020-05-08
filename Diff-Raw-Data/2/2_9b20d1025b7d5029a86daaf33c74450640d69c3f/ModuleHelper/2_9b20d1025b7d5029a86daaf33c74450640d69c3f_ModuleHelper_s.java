 /*******************************************************************************
  * Copyright (c) 2005, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.tomcat.core.tests.module;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.web.archive.operations.WebFacetProjectCreationDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.internal.IMemento;
 import org.eclipse.wst.server.core.internal.XMLMemento;
 import org.eclipse.wst.server.core.model.*;
 import org.eclipse.wst.server.core.util.ProjectModule;
 import org.osgi.framework.Bundle;
 
 public class ModuleHelper {
 	// size of the buffer
 	private static final int BUFFER = 10240;
 
 	// the buffer
 	private static byte[] buf = new byte[BUFFER];
 
 	public static void createModule(String name) throws Exception {
 		IDataModel dataModel = DataModelFactory.createDataModel(new WebFacetProjectCreationDataModelProvider());
       dataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, name);
       dataModel.setBooleanProperty(IJ2EEFacetProjectCreationDataModelProperties.ADD_TO_EAR, false);
       
       FacetDataModelMap map = (FacetDataModelMap) dataModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
       IDataModel webModel = map.getFacetDataModel(J2EEProjectUtilities.DYNAMIC_WEB);
      webModel.setStringProperty(IFacetDataModelProperties.FACET_VERSION_STR, "22");
       
 		dataModel.getDefaultOperation().execute(new NullProgressMonitor(), null);
 		dataModel.dispose();
 	}
 
 	public static void createWebContent(String name, int i) throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		IFile file = project.getFile(new Path("WebContent").append("test" + i + ".html"));
 		String content = "Hello!";
 		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
 		file.create(in, true, null);
 	}
 
 	public static void createXMLContent(String name, int i) throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		IFile file = project.getFile(new Path("WebContent").append("test" + i + ".xml"));
 		String content = "<book name='test'><isbn>299827698</isbn></book>";
 		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
 		file.create(in, true, null);
 	}
 
 	public static void createJavaContent(String name, int i) throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		IFile file = project.getFile(new Path("src").append("Test" + i + ".java"));
 		String content = "public class Test" + i + " { }";
 		ByteArrayInputStream in = new ByteArrayInputStream(content.getBytes());
 		file.create(in, true, null);
 	}
 
 	public static void createJarContent(String name, int num, IPath path) throws Exception {
 		if (!path.toFile().exists())
 			path.toFile().mkdirs();
 		
 		// create external jars
 		for (int i = 0; i < num; i++) {
 			Bundle bundle = Platform.getBundle("org.eclipse.core.runtime");
 			URL url = bundle.getEntry("/");
 			url = FileLocator.resolve(url);
 			String s = url.toString();
 			url = new URL(s.substring(4, s.length() - 2));
 			InputStream in = url.openStream();
 			IPath path2 = path.append("external_jar" + i + ".jar");
 			copyFile(in, path2.toOSString());
 		}
 		
 		// update component file
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		IFile file = project.getFile(new Path(".settings").append("org.eclipse.wst.common.component"));
 		
 		InputStream in = null;
 		XMLMemento memento = null;
 		try {
 			in = file.getContents();
 			memento = (XMLMemento) XMLMemento.loadMemento(in);
 			IMemento child = memento.getChild("project-modules");
 			child = memento.getChild("wb-module");
 			
 			for (int i = 0; i < num; i++) {
 				IMemento newChild = child.createChild("dependent-module");
 				newChild.putString("deploy-path", "/WEB-INF/lib");
 				IPath path2 = path.append("external_jar" + i + ".jar");
 				newChild.putString("handle", "module:/classpath/lib/" + path2.toOSString());
 				XMLMemento child2 = (XMLMemento) newChild.createChild("dependency-type");
 				child2.putTextData("uses");
 			}
 /*			<dependent-module deploy-path="/WEB-INF/lib" handle="module:/classpath/lib/C:/Documents and Settings/Administrator/Desktop/com.ibm.version.adder_1.0.0.jar">
          <dependency-type>uses</dependency-type>
      </dependent-module>*/
 		} finally {
 			try {
 				in.close();
 			} catch (Exception ex) {
 				// ignore
 			}
 		}
 		//ByteArrayInputStream in = new ByteArrayInputStream(memento.getBytes());
 		in = memento.getInputStream();
 		file.setContents(in, true, true, null);
 	}
 
 	public static void deleteModule(String name) throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		project.delete(true, null);
 	}
 
 	public static void buildIncremental() throws CoreException {
 		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
 		boolean interrupted = true;
 		while (interrupted) {
 			try {
 				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
 						new NullProgressMonitor());
 				interrupted = false;
 			} catch (InterruptedException e) {
 				// 
 			}
 		}
 	}
 
 	public static void buildFull() throws CoreException {
 		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
 		boolean interrupted = true;
 		while (interrupted) {
 			try {
 				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
 						new NullProgressMonitor());
 				interrupted = false;
 			} catch (InterruptedException e) {
 				// 
 			}
 		}
 	}
 
 	public static void buildClean() throws CoreException {
 		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
 		boolean interrupted = true;
 		while (interrupted) {
 			try {
 				Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD,
 						new NullProgressMonitor());
 				interrupted = false;
 			} catch (InterruptedException e) {
 				// 
 			}
 		}
 	}
 
 	public static IModule getModule(String name) throws Exception {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 		IModule module = ServerUtil.getModule(project);
 		if (module == null)
 			throw new Exception("No module in Web project");
 		
 		return module;
 	}
 
 	public static int countFilesInModule(IModule module) throws CoreException {
 		ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
 		IModuleResource[] mr = pm.members();
 		
 		int count = 0;
 		int size = mr.length;
 		for (int i = 0; i < size; i++) {
 			if (mr[i] instanceof IModuleFolder)
 				count += countFilesInFolder((IModuleFolder) mr[i]);
 			else
 				count++;
 		}
 		
 		return count;
 	}
 
 	protected static int countFilesInFolder(IModuleFolder mf) {
 		int count = 0;
 		IModuleResource[] mr = mf.members();
 		if (mr == null)
 			return 0;
 		
 		int size = mr.length;
 		for (int i = 0; i < size; i++) {
 			if (mr[i] instanceof IModuleFolder)
 				count += countFilesInFolder((IModuleFolder) mr[i]);
 			else
 				count++;
 		}
 		
 		return count;
 	}
 
 	public static int countFilesInDelta(IModuleResourceDelta delta) throws CoreException {
 		int count = 0;
 		if (delta.getModuleResource() instanceof IModuleFile)
 			count++;
 		
 		IModuleResourceDelta[] children = delta.getAffectedChildren();
 		int size = children.length;
 		for (int i = 0; i < size; i++) {
 			count += countFilesInDelta(children[i]);
 		}
 		
 		return count;
 	}
 
 	public static IModuleFile getModuleFile(IModule module, IPath path) throws CoreException {
 		ProjectModule pm = (ProjectModule) module.loadAdapter(ProjectModule.class, null);
 		IModuleResource[] mr = pm.members();
 		
 		int size = mr.length;
 		for (int i = 0; i < size; i++) {
 			if (mr[i].getModuleRelativePath().equals(path)) {
 				if (mr[i] instanceof IModuleFile)
 					return (IModuleFile) mr[i];
 				return null;
 			} else if (mr[i].getModuleRelativePath().isPrefixOf(path))
 				return getModuleFile((IModuleFolder) mr[i], path);
 		}
 		
 		return null;
 	}
 
 	protected static IModuleFile getModuleFile(IModuleFolder mf, IPath path) {
 		IModuleResource[] mr = mf.members();
 		if (mr == null)
 			return null;
 		
 		int size = mr.length;
 		for (int i = 0; i < size; i++) {
 			if (mr[i].getModuleRelativePath().equals(path)) {
 				if (mr[i] instanceof IModuleFile)
 					return (IModuleFile) mr[i];
 				return null;
 			} else if (mr[i].getModuleRelativePath().isPrefixOf(path))
 				return getModuleFile((IModuleFolder) mr[i], path);
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Copy a file from a to b. Closes the input stream after use.
 	 *
 	 * @param in java.io.InputStream
 	 * @param to java.lang.String
 	 */
 	private static void copyFile(InputStream in, String to) throws IOException {
 		OutputStream out = null;
 		
 		try {
 			out = new FileOutputStream(to);
 	
 			int avail = in.read(buf);
 			while (avail > 0) {
 				out.write(buf, 0, avail);
 				avail = in.read(buf);
 			}
 		} catch (Exception e) {
 			throw new IOException("Error copying file");
 		} finally {
 			try {
 				if (in != null)
 					in.close();
 			} catch (Exception ex) {
 				// ignore
 			}
 			try {
 				if (out != null)
 					out.close();
 			} catch (Exception ex) {
 				// ignore
 			}
 		}
 	}
 }
