 /*
  Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
  Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
 
  Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
  Copyright (c) 2006 - Andy Reek, D. Mitte
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */ 
 package org.lyllo.kickassplugin;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.management.monitor.Monitor;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.statushandlers.StatusManager;
 import org.lyllo.kickassplugin.prefs.ProjectPrefenceHelper;
 
 /**  
  * Builder for ASM-Files.
  * 
  * @author Andy Reek
  * @since 25.11.2005
  */
 public class KickAssemblerBuilder extends IncrementalProjectBuilder {
 
 	private IPath[] srcFolders;
 	private IProgressMonitor monitor;
 
 	/**
 	 * {@inheritDoc}
 	 */
 
 	protected IPath[] getSrcFolders(){
 		return srcFolders;
 	}
 
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
 
 		this.monitor = monitor;
 		Activator.getConsole().bringConsoleToFront();
 		Activator.getConsole().println(Messages.BUILDING_TEXT_CONSOLE);
 		Activator.getConsole().println();
 
 		createSrcIPaths();
 
 		try {
 			IResourceDelta delta = getDelta(getProject());
 			if (kind == IncrementalProjectBuilder.FULL_BUILD
 					|| kind == IncrementalProjectBuilder.CLEAN_BUILD
 					|| delta == null) {
 				fullBuild(monitor);
 			} else {
 				incrementalBuild(delta, monitor);
 			}
 		} catch (CoreException ex){
 			Status status = new Status(Status.ERROR, Constants.PLUGIN_ID, "Problems while compiling", ex);
 			StatusManager.getManager().handle(status,StatusManager.SHOW);
 		}
 
 		return null;
 	}
 
 	private void getAllIncluders(IFile file, Set<String> visited, Map<String,IFile> srcs) {
 
 		if (visited.contains(file.getRawLocation().toOSString()) ||
 				(file.getParent() != null && visited.contains(file.getParent().getRawLocation().toOSString())))
 			return;
 
 		try {
 			monitor.subTask("Searching for resources with " + file.getName() + " as dependency");
 			IContainer container = file.getParent();
 			String filename = file.getName();
 
 			while (!monitor.isCanceled() && container!= null && container.getType() != IResource.ROOT){
 				SrcCollectingVisitor srcCollectingVisitor = new SrcCollectingVisitor(filename,visited,srcs);
 				for(IResource sibling: container.members()){
 					sibling.accept(srcCollectingVisitor,IResource.DEPTH_ZERO,IResource.NONE);
 				}
 				filename = container.getName()+"/"+filename;
 				container = container.getParent();
 			}
 
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private class SrcCollectingVisitor implements IResourceVisitor {
 
 		private String filename;
 		private Set<String> visited;
 		private Map<String, IFile> srcs;
 
 		public SrcCollectingVisitor(String filename, Set<String> visited, Map<String, IFile> srcs) {
 			this.filename = filename;
 			this.visited = visited;
 			this.srcs = srcs;
 		}
 
 		public boolean visit(IResource arg0) throws CoreException {
 
 			if (arg0 == null || arg0.getRawLocation() == null){
 				return false;
 			}
 			String tmp = arg0.getRawLocation().toOSString();
 			if (visited.contains(tmp)){
 				return false;
 			}
 			visited.add(tmp);
 
 			if (arg0.getType() == IResource.FOLDER){
 				if (matchingSourceFolder(arg0.getProjectRelativePath())){
 					SrcCollectingVisitor srcCollectingVisitor = new SrcCollectingVisitor("../"+filename, visited,srcs);
 					for (IResource child: ((IFolder)arg0).members()){
 						if (!monitor.isCanceled())
 							child.accept(srcCollectingVisitor,IResource.DEPTH_ZERO,IResource.NONE);
 					}
 				}
 			} 
 
 			if (arg0.getType() == IResource.FILE){
				String ext = arg0.getFileExtension().toLowerCase();
 				if ("asm".equals(ext) || "s".equals(ext) || "inc".equals(ext) || "sym".equals(ext)){
 					if (containsFilename((IFile) arg0, filename)){
 						if ("asm".equals(ext) || "s".equals(ext)){
 							srcs.put(arg0.getRawLocation().toOSString(), (IFile)arg0);
 						} else {
 							
 							getAllIncluders((IFile) arg0, new HashSet<String>(),srcs);
 						}
 					}
 				} 
 			}
 
 			return true;
 		}
 
 		private boolean containsFilename(IFile arg0, String filename2) throws CoreException {
 
 			boolean retval = false;
 			BufferedReader br = new BufferedReader(new InputStreamReader(arg0.getContents(true)));
 			String line = null;
 			try {
 				while(!retval && (line = br.readLine()) != null){
 					if (line.trim().toLowerCase().startsWith(".import")){
 						retval = line.matches("^\\s*\\.import\\s+source\\s+\""+filename2+"\"");
 					}
 				}
 			} catch (IOException e) {
 			} finally {
 				if (br != null){
 					try {
 						br.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 
 			return retval;
 		}
 
 
 	}
 
 	private void createSrcIPaths() {
 		String[] split = ProjectPrefenceHelper.getSourceDirs(getProject());
 		if (split.length == 0){
 			split = new String[] {Constants.DEFAULT_SRC_DIRECTORY};
 		}
 		srcFolders = new IPath[split.length];
 
 		for (int i = 0; i < split.length; i++ ){
 			srcFolders[i] =getProject().getFolder(split[i]).getProjectRelativePath();
 		}
 	}
 
 	/**
 	 * Builds the whole project.
 	 * 
 	 * @param monitor A monitor.
 	 * @throws CoreException 
 	 */
 	private void fullBuild(IProgressMonitor monitor) throws CoreException {
 		IProject project = getProject();
 		monitor.beginTask(Messages.BUILDING_TITLE, 100);
 		monitor.subTask(Messages.BUILDING_TEXT_COMPILE);
 		project.accept(new MyFullBuildVisitor());
 		monitor.done();
 		project.refreshLocal(IResource.DEPTH_INFINITE, null);
 	}
 
 	/**
 	 * TODO - clean build-dir
 	 */
 	@Override
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		super.clean(monitor);
 	}
 	/**
 	 * Builds only the project delta.
 	 * 
 	 * @param delta The given delta.
 	 * @param monitor A monitor.
 	 * @throws CoreException 
 	 */
 	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
 		IProject project = getProject();
 		monitor.beginTask(Messages.BUILDING_TITLE, 100);
 		monitor.subTask(Messages.BUILDING_TEXT_COMPILE);
 		delta.accept(new MyIncrementalBuildVisitor(monitor));
 		monitor.done();
 		project.refreshLocal(IResource.DEPTH_INFINITE, null);
 	}
 
 	/**
 	 * Compiles a given file.
 	 * 
 	 * @param file The file to be compiled.
 	 * @throws CoreException 
 	 * @throws IOException 
 	 */
 	private void compileFile(IFile file) throws CoreException {
 
 		String buildDir = ProjectPrefenceHelper.getBuildDir(getProject());
 
 		String destdir = getProject().getLocationURI().getRawPath() + File.separator + buildDir;
 
 		//FIXME
 		IFolder destFolder = file.getProject().getFolder(buildDir);
 
 		if (!destFolder.exists()){
 			destFolder.create(IResource.NONE, true, null);
 			destFolder.setDerived(true,null);
 		}
 		String filedir = file.getParent().getLocation().toFile().getAbsolutePath();
 		String dest = destdir + File.separator + file.getName();
 		String destName = file.getName().substring(0,file.getName().lastIndexOf('.')+1)+"prg";
 		dest = dest.substring(0,dest.lastIndexOf(File.separatorChar)+1) + destName;
 		String cmdLine[] = createCommandLine(file,dest);
 		if (cmdLine == null){
 			throw new CoreException(new Status(Status.ERROR, Constants.PLUGIN_ID, "Could not compile. Please make sure that you have set the path to Kickass.jar in the Preferences"));
 		}
 
 		new KickAssLauncher().launch(cmdLine, filedir, file.getProject());
 		try {
 
 			destFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
 			IResource member = destFolder.findMember(destName);
 			if (member != null){
 				member.setDerived(true,null);
 			}
 		} catch (CoreException e) {
 		}
 
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 		boolean viceSymbols = store.getBoolean(Constants.PREFERENCES_COMPILER_VICESYMBOLS);
 		if (viceSymbols){
 			moveViceSymbolFile(file, destFolder);
 		}
 
 	}
 
 	private static String[] createCommandLine(IFile file, String dest) {
 
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 
 		String compiler = store.getString(Constants.PREFERENCES_COMPILER_NAME);
 		String params = store.getString(Constants.PREFERENCES_COMPILER_PARAMS);
 		boolean viceSymbols = store.getBoolean(Constants.PREFERENCES_COMPILER_VICESYMBOLS);
 		boolean symbols = store.getBoolean(Constants.PREFERENCES_COMPILER_SYMBOLS);
 		boolean afo = store.getBoolean(Constants.PREFERENCES_COMPILER_AFO);
 		List<String> libdirsArray = new ArrayList<String>();
 		libdirsArray.addAll(ProjectPrefenceHelper.getAbsoluteLibDirs(file.getProject()));
 		libdirsArray.addAll(Activator.getGlobalLibdirs());
 
 		if ((compiler == null) || (compiler.trim().length() < 1)) {
 			return null;
 		}
 		compiler = compiler.trim();
 		File f = new File(compiler);
 		if (!f.exists() || !f.canRead()){
 			return null;
 		}
 
 		if (params == null) {
 			params = "";
 		}
 
 		List<String> cmdArray = new ArrayList<String>();
 		cmdArray.add(file.getName());
 		if (symbols){
 			cmdArray.add("-symbolfile");
 		}
 		if (viceSymbols){
 			cmdArray.add("-vicesymbols");
 		}
 		if (afo){
 			cmdArray.add("-afo");
 		}
 
 
 		if (!libdirsArray.isEmpty()){
 			for (String dir : libdirsArray){
 				dir = dir.trim();
 				if (new File(dir).exists()){
 					cmdArray.add("-libdir");
 					cmdArray.add(dir);
 				} else {
 					Activator.getDefault().getLog().log(
 							new Status(Status.ERROR, Constants.PLUGIN_ID, "CommandLineArgument is invalid, libdir does not exist [" + dir +"]"));
 
 				}
 			}
 		}
 
 		cmdArray.add("-o");
 		cmdArray.add(dest);
 
 		for (String p :params.split("\n")){
 			if (p.matches("\\s*\\w+\\s*=\\s*\\w+\\s*")){
 				cmdArray.add(":" +p.trim());
 			} else if (!"".equals(p.trim())){
 				Activator.getDefault().getLog().log(
 						new Status(Status.ERROR, Constants.PLUGIN_ID, "CommandLineArgument is invalid: [" + p +"]"));
 
 			}
 		}
 
 		return cmdArray.toArray(new String[]{});
 	}
 
 
 	private void moveViceSymbolFile(IFile file, IFolder dest) {
 		try {
 			file.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
 
 			IFile src = (IFile) file.getParent().findMember(file.getName().replace(file.getFileExtension(), "vs"));
 
 			if (src != null && src.exists()){
 				src.setDerived(true,null);
 				IFile oldFile = dest.getFile(src.getName());
 				if (oldFile != null && oldFile.exists()){
 					oldFile.delete(true, null);
 				}
 				src.move(oldFile.getFullPath(), true, null);
 			}
 		} catch (CoreException e) {
 			Activator.getDefault().getLog().log(
 					new Status(Status.ERROR, Constants.PLUGIN_ID, Status.OK,
 							Messages.LAUNCH_ERROR, e));
 		}
 	}
 
 
 	/**
 	 * Class for visiting all resources when a full build is initiated.
 	 * 
 	 * @author andre
 	 * @since 25.11.2005
 	 */
 	private class MyFullBuildVisitor implements IResourceVisitor {
 		/**
 		 * {@inheritDoc}
 		 */
 		public boolean visit(IResource resource) throws CoreException {
 			int resourceType = resource.getType();
 
 			if (resourceType == IResource.PROJECT) {
 				return getProject().getName().equals(resource.getName());
 			} else if (resourceType == IResource.FOLDER) {
 				return matchingSourceFolder(resource.getProjectRelativePath());
 			} else if (resourceType == IResource.FILE) {
 				String extension = resource.getFileExtension();
 				if ("asm".equalsIgnoreCase(extension) || "s".equalsIgnoreCase(extension)){
 					compileFile((IFile) resource);
 				}
 			}
 
 			return false;
 		}
 
 
 	}
 
 	private boolean matchingSourceFolder(IPath projectRelativePath) {
 		for (IPath path: getSrcFolders()){
 			if (projectRelativePath.matchingFirstSegments(path) == path.segmentCount()){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Class for visiting all resources when a incremental build is initiated.
 	 * 
 	 * @author andre
 	 * @since 25.11.2005
 	 */
 	private class MyIncrementalBuildVisitor implements IResourceDeltaVisitor {
 
 		private IProgressMonitor monitor;
 
 		public MyIncrementalBuildVisitor(IProgressMonitor monitor){
 			this.monitor = monitor;
 		}
 
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			int deltaKind = delta.getKind();
 
 			if ((deltaKind == IResourceDelta.ADDED) || (deltaKind == IResourceDelta.CHANGED)) {
 				IResource resource = delta.getResource();
 				int resourceType = resource.getType();
 
 				if (resourceType == IResource.FILE) {
					String extension = resource.getFileExtension().toLowerCase();
					if ((extension != null) && ( "asm".equals(extension) || "s".equals(extension))) {
 						if (!monitor.isCanceled())
 							compileFile((IFile) resource);
					} else if ("inc".equalsIgnoreCase(extension) || "sym".equalsIgnoreCase(extension)){
 						Map<String,IFile>srcs = new HashMap<String, IFile>();
 						
 						getAllIncluders((IFile)resource, new HashSet<String>(),srcs);
 						
 						for (IFile file: srcs.values()){
 							if (!monitor.isCanceled())
 								compileFile(file);
 						}
 					}
 				} else if (resourceType == IResource.PROJECT) {
 					return getProject().getName().equals(resource.getName());
 				} else if (resourceType == IResource.FOLDER) {
 					return matchingSourceFolder(resource.getProjectRelativePath());
 				} 
 			}
 
 			return false;
 		}
 	}
 }
