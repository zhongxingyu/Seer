 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.common.ui.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.ide.ResourceUtil;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.wizards.IWizardDescriptor;
 
 /**
  * @author fcp94556
  *
  */
 public class EclipseUtils {
 
 	/**
 	 * Get the file path from a FileStoreEditorInput. Removes any "file:"
 	 * from the URI to the file path if it exists.
 	 * 
 	 * @param fileInput
 	 * @return String
 	 */
 	public static URI getFileURI(IEditorInput fileInput) {
 		if (fileInput instanceof IURIEditorInput) {
 			URI uri = ((IURIEditorInput)fileInput).getURI();
 			return uri;
 		} 
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param fileInput
 	 * @return File
 	 */
 	public static File getFile(IEditorInput fileInput) {
		return new File(EclipseUtils.getFileURI(fileInput));
 	}
 	
 	public static String getFilePath(IEditorInput fileInput) {
 		final File file = getFile(fileInput);
 		if (file==null) return null;
 		return file.getAbsolutePath();
 	}
	
	static private boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}
 	
 	/**
 	 * Try to determine the IFile from the edit input
 	 * @param input
 	 * @return file
 	 */
 	public static IFile getIFile(IEditorInput input) {
 		if (input instanceof FileEditorInput) {
 			return ((FileEditorInput)input).getFile();
 		}
 		return (IFile)input.getAdapter(IFile.class);
 	}
 	
 	public static String getFileName(IEditorInput input) {
 		return getFile(input).getName();
 		
 	}
 
 	/**
 	 * Try to determine the IFile from the edit input
 	 * @param input
 	 * @return file
 	 * @throws CoreException 
 	 */
 	public static InputStream getInputStream(IEditorInput input) throws Exception {
 		if (input instanceof FileEditorInput) {
 			return ((FileEditorInput)input).getFile().getContents();
 		}
 		return new FileInputStream(getFile(input));
 	}
 	public static OutputStream getOutputStream(IEditorInput input)  throws Exception {
 		if (input instanceof FileEditorInput) {
 			return new FileOutputStream(((FileEditorInput)input).getFile().getLocation().toOSString());
 		}
 		return new FileOutputStream(getFile(input));
 	}
 
 	
 	/**
 	 * @param bundleUrl 
 	 * @return bundleUrl
 	 */
 	public static URL getAbsoluteUrl(final URL bundleUrl) {
 		if (bundleUrl==null) return null;
 		if (bundleUrl.toString().startsWith("bundle"))
 			try {
 				return FileLocator.resolve(bundleUrl);
 			} catch (IOException e) {
 				return bundleUrl;
 	        } 
 		return bundleUrl;
 	}
 	
 	/**
 	 * Gets the page, even during startup.
 	 * @return the page
 	 */
 	public static IWorkbenchPage getPage() {
 		IWorkbenchPage activePage = EclipseUtils.getActivePage();
 		if (activePage!=null) return activePage;
 		return EclipseUtils.getDefaultPage();
 	}
 	
 	/**
 	 * @return IWorkbenchPage
 	 */
 	public static IWorkbenchPage getActivePage() {
 		final IWorkbench bench = PlatformUI.getWorkbench();
 		if (bench==null) return null;
 		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
 		if (window==null) return null;
 		return window.getActivePage();
 	}
 	
 	/**
 	 * @return IWorkbenchPage
 	 */
 	public static IEditorPart getActiveEditor() {
 		final IWorkbenchPage page = EclipseUtils.getPage();
 		return page.getActiveEditor();
 	}
 
 	
 	/**
 	 * @return IWorkbenchPage
 	 */
 	public static IWorkbenchPage getDefaultPage() {
 		final IWorkbench bench = PlatformUI.getWorkbench();
 		if (bench==null) return null;
 		final IWorkbenchWindow[] windows = bench.getWorkbenchWindows();
 		if (windows==null) return null;
 		
 		return windows[0].getActivePage();
 	}
 
 	/**
 	 * Delcare a builder id in a project, this is then called to build it.
 	 * @param project
 	 * @param id
 	 * @throws CoreException 
 	 */
 	public static void addBuilderToProject(IProject project, String id) throws CoreException {
 		
         if (!project.isOpen()) return;
         
         IProjectDescription des = project.getDescription();
         
         ICommand[] cmds = des.getBuildSpec();
         for (int i = 0; i < cmds.length; i++) {
 			if (cmds[i].getBuilderName().equals(id)) return;
 		}
 		
 	    ICommand com = des.newCommand();
 	    com.setBuilderName(id);
 	    List<ICommand> coms = new ArrayList<ICommand>(cmds.length+1);
 	    coms.addAll(Arrays.asList(cmds));
 	    coms.add(com);
 	    
 	    des.setBuildSpec(coms.toArray(new ICommand[coms.size()]));
 	    
 	    project.setDescription(des, null);
 	}
 
 	/**
 	 * Checks of the id passed in == the current perspectives.
 	 * @param id
 	 * @return true if is
 	 */
 	public static boolean isActivePerspective(final String id) {
 		
 		final IWorkbenchPage page = getActivePage();
 		if (page==null) return false;
 		
 		try {
 			return id.equals(page.getPerspective().getId());
 		} catch (Exception ignored) {
 			return false;
 		}
 	}
 	
 	/**
 	 * Process UI input but do not return for the specified time interval.
 	 * 
 	 * @param waitTimeMillis
 	 *            the number of milliseconds
 	 */
 	public static void delay(long waitTimeMillis) {
 		
 		if (PlatformUI.isWorkbenchRunning()) {
 
 			Display display = PlatformUI.getWorkbench().getDisplay();
 
 			// If this is the UI thread,
 			// then process input.
 			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
 			while (System.currentTimeMillis() < endTimeMillis) {
 				try {
 					if (!display.readAndDispatch()) display.sleep();
 				} catch (Exception ne) {
 					try {
 						Thread.sleep(waitTimeMillis);
 					} catch (InterruptedException e) {
 						// Ignored
 					}
 					break;
 				}
 			}
 			display.update();
 			
 		} else {
 			try {
 				Thread.sleep(waitTimeMillis);
 			} catch (InterruptedException e) {
 				// Ignored.
 			}
 		}
 	}
 	
 	/**
 	 * Gets a unique file. The file must have a parent of IFolder.
 	 * @param file
 	 * @return new file, not created.
 	 */
 	public static IFile getUniqueFile(IFile file, final String extension) {
 
 		return getUniqueFile(file, null, extension);
 	}
 	
 	/**
 	 * Gets a unique file. The file must have a parent of IFolder.
 	 * @param file
 	 * @return new file, not created.
 	 */
 	public static IFile getUniqueFile(IFile file, final String conjunctive, final String extension) {
 		
 		final String name = file.getName();
 		final Matcher matcher = Pattern.compile("(.+)(\\d+)\\."+extension, Pattern.CASE_INSENSITIVE).matcher(name);
 		int start   = 0;
 		String frag = name.substring(0,name.lastIndexOf("."));
 		if (matcher.matches()) {
 			frag  = matcher.group(1);
 			start = Integer.parseInt(matcher.group(2));
 		}
 		
 		if (conjunctive!=null) {
 			frag = frag+conjunctive;
 		}
 		
 		// First try without a start position
 		final IContainer parent = file.getParent();
 		final IFile newFile;
 		if (parent instanceof IFolder) {
 			newFile = ((IFolder)parent).getFile(frag+"."+extension);
 		} else if (parent instanceof IProject) {
 			newFile = ((IProject)parent).getFile(frag+"."+extension);
 		} else {
 			newFile = null;
 		}
 		if (newFile!=null&&!newFile.exists()) return newFile;
 		
 		return getUniqueFile(parent, frag, ++start, extension);
 	}
 
 	private static IFile getUniqueFile(IContainer parent, String frag, int start, final String extension) {
 		final IFile file;
 		if (parent instanceof IFolder) {
 			file = ((IFolder)parent).getFile(frag+start+"."+extension);
 		} else if (parent instanceof IProject) {
 			file = ((IProject)parent).getFile(frag+start+"."+extension);
 		} else {
 			throw new RuntimeException("The parent is neither a project nor a folder.");
 		}
 		if (!file.exists()) return file;
 		return getUniqueFile(parent, frag, ++start, extension);
 	}
 
 	private static final Pattern UNIQUE_PATTERN = Pattern.compile("(.+)(\\d+)", Pattern.CASE_INSENSITIVE);
 
 	public static String getUnique(IResource res) {
 		final String name = res.getName();
 		final Matcher matcher = UNIQUE_PATTERN.matcher(name);
 		int start   = 0;
 		String frag = name.indexOf(".")>-1
 		            ? name.substring(0,name.lastIndexOf("."))
 		            : name;
 		if (matcher.matches()) {
 			frag  = matcher.group(1);
 			start = Integer.parseInt(matcher.group(2));
 		}
 		
 		return getUnique(res.getParent(), frag, ++start);
 	}
 	
 	private static String getUnique(IContainer parent, String frag, int start) {
 		final IFile file;
 		final IFolder folder;
 		if (parent instanceof IFolder) {
 			file = ((IFolder)parent).getFile(frag+start);
 			folder = ((IFolder)parent).getFolder(frag+start);
 		} else if (parent instanceof IProject) {
 			file = ((IProject)parent).getFile(frag+start);
 			folder = ((IProject)parent).getFolder(frag+start);
 		} else {
 			throw new RuntimeException("The parent is niether a project nor a folder.");
 		}
 		if (!file.exists()&&!folder.exists()) return file.getName();
 		return getUnique(parent, frag, ++start);
 	}
 
 	
 	// Source code and JavaDoc adapted from org.eclipse.ui.internal.util.Util.getAdapter
 	/**
 	 * If it is possible to adapt the given object to the given type, this
 	 * returns the adapter. Performs the following checks:
 	 * 
 	 * <ol>
 	 * <li>Returns <code>sourceObject</code> if it is an instance of the
 	 * adapter type.</li>
 	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
 	 * <li>If sourceObject is not an instance of PlatformObject (which would have
 	 * already done so), the adapter manager is queried for adapters</li>
 	 * </ol>
 	 * 
 	 * Otherwise returns null.
 	 * 
 	 * @param sourceObject
 	 *            object to adapt, or null
 	 * @param adapterType
 	 *            type to adapt to
 	 * @return a representation of sourceObject that is assignable to the
 	 *         adapter type, or null if no such representation exists
 	 */
 	public static Object getAdapter(Object sourceObject, Class<?> adapterType) {
 		Assert.isNotNull(adapterType);
 	    if (sourceObject == null) {
 	        return null;
 	    }
 	    if (adapterType.isInstance(sourceObject)) {
 	        return sourceObject;
 	    }
 	
 	    return ResourceUtil.getAdapter(sourceObject, adapterType, true);
 	}
 
 	/**
 	 * Opens an external editor on a file path
 	 * @param file
 	 * @throws PartInitException
 	 */
 	public static IEditorPart openEditor(IFile file) throws PartInitException {
 		
 		final IWorkbenchPage page = EclipseUtils.getPage();
 		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
         if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName()+".txt");
 		return page.openEditor(new FileEditorInput(file), desc.getId());
 	}
 
 	/**
 	 * Opens an external editor on a file path
 	 * @param filename
 	 * @throws PartInitException
 	 */
 	public static IEditorPart openExternalEditor(String filename) throws PartInitException {
 		
 		final IWorkbenchPage page = EclipseUtils.getPage();
 		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename);
         if (desc == null) desc =  PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(filename+".txt");
 		final IFileStore externalFile = EFS.getLocalFileSystem().fromLocalFile(new File(filename));
 		final IEditorInput store      = new FileStoreEditorInput(externalFile);
 		final String       id         = desc.getId();
         return page.openEditor(store, id);
 	}
 
 	/**
 	 * Returns the active project based on active selection
 	 */
 	public static IProject getActiveProject() {
 		
 		final IWorkbenchPage page = EclipseUtils.getActivePage();
 		if (page==null) return null;
 		
 		final IEditorPart activeEditor = page.getActiveEditor();
 		if (activeEditor!=null) {
 			final IEditorInput input = activeEditor.getEditorInput();
 			if (input instanceof FileEditorInput) {
 				return ((FileEditorInput)input).getFile().getProject();
 			}
 		}
 		
 		final ISelectionService service = page.getWorkbenchWindow().getSelectionService();
 		final ISelection        sel     = service.getSelection();
 		if (!(sel instanceof IStructuredSelection)) return null;
 		
 		final IStructuredSelection ss = (IStructuredSelection) sel;
 		final Object          element = ss.getFirstElement();
 		if (element instanceof IResource) return ((IResource)element).getProject();
 		
 		if (!(element instanceof IAdaptable)) return null;
 		IAdaptable adaptable = (IAdaptable)element;
 		Object adapter = adaptable.getAdapter(IResource.class);
 		return  ((IResource)adapter).getProject();
 	}
 	
 	public static void setBold(Control point) {
 		
 		final FontData[] fData = point.getFont().getFontData();
 		for (int i = 0; i < fData.length; i++) fData[i].setStyle(SWT.BOLD);
 		point.setFont(new Font(point.getDisplay(),fData));
 		
 	}
 
 	/**
 	 * Thread Safe. This is a hard busy setter - it overrides everything and sets everything busy.
 	 * 
 	 * Always use with try, finally. Only use when you are sure that there is no other alternative.
 	 * For instance @see Job class.
 	 * 
 	 * @param isBusy
 	 */
 	public static void setBusy(final boolean isBusy) {
 		
 		final Display display = PlatformUI.getWorkbench().getDisplay();
 		if (display==null || display.isDisposed()) return;
 
 		display.syncExec(new Runnable() {
 			public void run() {
 				Cursor cursor  = display.getSystemCursor(SWT.CURSOR_WAIT);
 				Shell[] shells = display.getShells();
 
 				for (int i = 0; i < shells.length; i++) {
 					if (isBusy) {
 						shells[i].setCursor(cursor);
 					} else {
 						shells[i].setCursor(null);
 					}
 				}
 			}
 		});
 	}
 
 	public static boolean isProjectExisting(final String natureId) {
 	
 		return getExistingProject(natureId)!=null;
 	}
 	
 	public static IProject getExistingProject(final String natureId) {
 		
 		// Show the create a workflows project creator is there are no projects already
 		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		final IProject[] projects = root.getProjects();	
 		for (IProject iProject : projects) {
 			try {
 				if (iProject.getNature(natureId)!=null) {
 					return iProject;
 				}
 			} catch (CoreException e) {
 				continue;
 			}
 		}
 
 		return null;
 	}
 
 
 	public static IWizard openWizard(String id, boolean requireOpen) throws Exception {
 		
 		// First see if this is a "new wizard".
 		IWizardDescriptor descriptor = PlatformUI.getWorkbench()
 				.getNewWizardRegistry().findWizard(id);
 		// If not check if it is an "import wizard".
 		if  (descriptor == null) {
 			descriptor = PlatformUI.getWorkbench().getImportWizardRegistry()
 					.findWizard(id);
 		}
 		// Or maybe an export wizard
 		if  (descriptor == null) {
 			descriptor = PlatformUI.getWorkbench().getExportWizardRegistry()
 					.findWizard(id);
 		}
 		// Then if we have a wizard, open it.
 		if  (descriptor != null) {
 			IWizard wizard = descriptor.createWizard();
 
 			if (requireOpen) {
 				WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wizard);
 				wd.setTitle(wizard.getWindowTitle());
 				wd.open();
 			}
 
 			return wizard;
 		}
 		return null;
 	}
 
 }
 
 	
