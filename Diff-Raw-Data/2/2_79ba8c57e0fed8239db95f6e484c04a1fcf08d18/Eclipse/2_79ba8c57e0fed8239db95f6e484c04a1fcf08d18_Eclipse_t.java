 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.console;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.search.IJavaSearchConstants;
 import org.eclipse.jdt.core.search.IJavaSearchScope;
 import org.eclipse.jdt.core.search.SearchEngine;
 import org.eclipse.jdt.core.search.SearchMatch;
 import org.eclipse.jdt.core.search.SearchParticipant;
 import org.eclipse.jdt.core.search.SearchPattern;
 import org.eclipse.jdt.core.search.SearchRequestor;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.utils.FileUtils;
 
 public class Eclipse {
 
 	private static final Logger logger = LogProvider.getLogger(BuilderConsoleActivator.class);
 	
 	private Eclipse() {
 		// static methods only
 	}
 	
 	public static void open(final File file) {
 		if(file.isFile()) {
 			Display.getDefault().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					Program.launch(file.getAbsolutePath());
 				}
 			});
 		} else if(file.isDirectory()) {
 			Display.getDefault().syncExec(new Runnable() {
 				@Override
 				public void run() {
 					try {
 						Desktop.getDesktop().open(file);
 					} catch(IOException e) {
 						e.printStackTrace();
 						// oh well...
 					}
 				}
 			});
 		}
 	}
 
 	public static void removeProject(String name) {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject project = root.getProject(name);
 		try {
 			IProgressMonitor monitor = new NullProgressMonitor();
 			project.delete(true, monitor);
 		} catch(CoreException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void importProject(String name, File location) {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject project = root.getProject(name);
 		IProgressMonitor monitor = new NullProgressMonitor();
 		try {
 			IProjectDescription description = root.getWorkspace().newProjectDescription(name);
 			description.setLocationURI(location.toURI());
 			project.create(description, monitor);
 			project.open(monitor);
 		} catch(CoreException e1) {
 			try {
 				project.create(monitor);
 				project.open(monitor);
 			} catch(CoreException e2) {
 				logger.warn(e2);
 			}
 		}
 	}
 
 	public static void openFile(File project, File file) {
 		String projectName = project.getName();
 		String fileName = file.getAbsolutePath().substring(project.getAbsolutePath().length());
 		openFile(projectName, fileName);
 	}
 	
 	public static void openFile(String projectName, String fileName) {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 			if(!project.isOpen()) {
 				project.open(new NullProgressMonitor());
 			}
 			final IFile file = project.getFile(fileName);
 			if(file.exists()) {
 				file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
 				Display.getDefault().syncExec(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
 						    IEditorPart editor = IDE.openEditor(window.getActivePage(), file, true);
 						    if(editor != null) {
 								editor.setFocus();
 								// TODO Shell#forceActive() does not currently work... check SWT bugs
 								window.getShell().forceActive();
 						    }
 						} catch(PartInitException e) {
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		} catch(CoreException e) {
 			logger.warn(e.getMessage());
 		}
 	}
 
 	public static void openResource(String name, final int line) {
 		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
 			File folder = project.getLocation().toFile();
 			File file = FileUtils.find(folder, name);
 			if(file != null) {
 				String relativeName = file.getAbsolutePath().substring(folder.getAbsolutePath().length());
 				IFile ifile = project.getFile(relativeName);
 				open(ifile, line);
 				return;
 			}
 		}
 	}
 
 	public static void openType(String type, final int line) {
 		SearchPattern pattern = SearchPattern.createPattern(type, IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
 		IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
 		
 		SearchRequestor sr = new SearchRequestor() {
 			@Override
 			public void acceptSearchMatch(SearchMatch match) throws CoreException {
 				IResource resource = match.getResource();
 				if(resource instanceof IFile) {
 					final IFile file = (IFile) match.getResource();
 					logger.debug(String.valueOf(match));
 					Display.getDefault().syncExec(new Runnable() {
 						@Override
 						public void run() {
 							open(file, line);
 						}
 					});
 				}
 			}
 		};
 			
 		SearchEngine engine = new SearchEngine();
 		try {
 			engine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, sr, null);
 		} catch(CoreException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void open(IFile file, int line) {
 		try {
 			IWorkbenchWindow window = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
 		    IEditorPart editor = IDE.openEditor(window.getActivePage(), file, true);
 		    if(editor instanceof AbstractDecoratedTextEditor) {
 			    AbstractDecoratedTextEditor ed = (AbstractDecoratedTextEditor) editor;
 			    IDocument doc = ed.getDocumentProvider().getDocument(ed.getEditorInput());
 			    try {
 					int offset = doc.getLineOffset(line - 1);
 					ed.selectAndReveal(offset, 0);
 				} catch(BadLocationException e) {
 					e.printStackTrace();
 				}
 		    }
 			editor.setFocus();
 			// TODO Shell#forceActive() does not currently work... check SWT bugs
 			window.getShell().forceActive();
 		} catch(PartInitException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void refreshProject(String name) {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
 			if(!project.isOpen()) {
 				project.open(new NullProgressMonitor());
 			}
 			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 		} catch(CoreException e) {
 			logger.warn(e.getLocalizedMessage());
 		}
 	}
 
 	public static void refresh(File project, File file) {
 		String projectName = project.getName();
 		String fileName = file.getAbsolutePath().substring(project.getAbsolutePath().length());
 		refresh(projectName, fileName);
 	}
 	
 	/**
 	 * Refresh the file or directory specified by fileName in the project
 	 * specified by projectName
 	 */
 	public static void refresh(String projectName, String fileName) {
 		try {
 			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 			if(!project.isOpen()) {
 				project.open(new NullProgressMonitor());
 			}
 
 			IFile file = project.getFile(fileName);
 			if(file.exists()) {
 				file.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
 			} else {
 				IFolder folder = project.getFolder(fileName);
				if(folder.exists()) {
 					folder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 				} else {
 					logger.warn("the file \"" + fileName + "\" does not exist in project \"" + projectName);
 				}
 			}
 		} catch(CoreException e) {
 			logger.warn(e.getLocalizedMessage());
 		}
 	}
 
 }
