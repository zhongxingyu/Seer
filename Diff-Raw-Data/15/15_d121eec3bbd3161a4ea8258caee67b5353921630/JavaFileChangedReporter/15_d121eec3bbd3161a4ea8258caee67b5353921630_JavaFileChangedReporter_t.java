 package edu.uci.lighthouse.core.listeners;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.ElementChangedEvent;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IElementChangedListener;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaElementDelta;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.osgi.framework.BundleContext;
 
 /**
  * This class listen for changes in java files and notifies listeners whose
  * implement <code>IJavaFileStatusListener</code> interface.
  * 
  * @author tproenca
  *@see IJavaFileStatusListener
  */
 public class JavaFileChangedReporter implements IResourceChangeListener,
 		IElementChangedListener, IPluginListener {
 
 	/** List of listeners. */
 	Collection<IJavaFileStatusListener> listeners = new LinkedList<IJavaFileStatusListener>();
 
 	/** Logger instance. */
 	private static Logger logger = Logger
 			.getLogger(JavaFileChangedReporter.class);
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		logger.info("Starting JavaFileChangedReporter");
 		logger.debug("addElementChangedListener()");
 		JavaCore.addElementChangedListener(this,
 				ElementChangedEvent.POST_CHANGE);
 		logger.debug("addPreProcessingResourceChangedListener()");
 		JavaCore.addPreProcessingResourceChangedListener(this,
 				IResourceChangeEvent.POST_BUILD);
 		// FIXME: find a way to load eclipse workbench first to guarantee the
 		// file will be found
 		findActiveOpenFileInWorkspace();
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		logger.info("Stopping JavaFileChangedReporter");
 		JavaCore.removePreProcessingResourceChangedListener(this);
 		JavaCore.removeElementChangedListener(this);
 	}
 
 	@Override
 	public void elementChanged(ElementChangedEvent event) {
 		IJavaElementDelta delta = event.getDelta();
 		logger.debug(delta);
 		traverseDeltaTree(delta);
 	}
 
 	@Override
 	public void resourceChanged(IResourceChangeEvent event) {
 		try {
 			IResourceDelta delta = event.getDelta();
 			delta.accept(new JavaFileResourceDeltaVisitor());
 		} catch (CoreException e) {
 			logger.error(e,e);
 		}
 	}
 
 	/** Finds the active open file in the workspace (The one with focus). */
 	private void findActiveOpenFileInWorkspace() {
 		logger.info("findActiveOpenFileInWorkspace");
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				IWorkbenchWindow window = PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow();
 				IWorkbenchPage[] pages = window.getPages();
 				logger.debug("pages length: " + pages.length);
 				for (IWorkbenchPage page : pages) {
 					IEditorPart editor = page.getActiveEditor();
 					logger.debug("editor: " + editor);
 					if (editor != null) {
 						IFile file = (IFile) editor.getEditorInput()
 								.getAdapter(IFile.class);
 						if (isJavaFile(file)) {
 							try {
 								boolean hasErrors = IMarker.SEVERITY_ERROR == file
 										.findMaxProblemSeverity(
 												IMarker.PROBLEM, true,
 												IResource.DEPTH_INFINITE);
 								fireOpen(file, hasErrors);
 							} catch (CoreException e) {
 								logger.error(e,e);
 							}
 						}
 					}
 				}
 			}
 		});
 	}
 
 	/** Verifies if the resource is a java file. */
 	private boolean isJavaFile(IResource resource) {
 		return resource.getType() == IResource.FILE
 				&& resource.getFileExtension().equalsIgnoreCase("java");
 	}
 
 	/**
 	 * Visits all nodes of the java element delta. It fires the events open and
 	 * close.
 	 */
 	private void traverseDeltaTree(IJavaElementDelta delta) {
 		if (delta.getElement().getElementType() == IJavaElement.COMPILATION_UNIT) {
 			ICompilationUnit icu = (ICompilationUnit) delta.getElement();
 			IFile iFile = (IFile) icu.getResource();
 			if (iFile.exists()) {
 				try {
 					boolean hasErrors = IMarker.SEVERITY_ERROR == icu
 							.getResource().findMaxProblemSeverity(
 									IMarker.PROBLEM, true,
 									IResource.DEPTH_INFINITE);
 
 					if ((delta.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0) {
 						if (icu.isWorkingCopy()) {
 							fireOpen(iFile, hasErrors);
 						} else {
 							fireClose(iFile, hasErrors);
 						}
 					}
 				} catch (Exception e) {
 					logger.error(e,e);
 				}
 			}
 		} else {
 			for (IJavaElementDelta child : delta.getAffectedChildren()) {
 				traverseDeltaTree(child);
 			}
 		}
 	}
 
 	/**
 	 * Adds the listener to the list of listeners that will be notified when
 	 * changes happens.
 	 */
 	public void addJavaFileStatusListener(IJavaFileStatusListener listener) {
 		listeners.add(listener);
 	}
 
 	/** Remove the listener from the list of listeners. */
 	public void removeJavaFileStatusListener(IJavaFileStatusListener listener) {
 		listeners.remove(listener);
 	}
 
 	/** Fires the open event. */
 	protected void fireOpen(IFile iFile, boolean hasErrors) {
 		logger.info("Opening " + iFile.getName() + " (errors:" + hasErrors
 				+ ")");
 		for (IJavaFileStatusListener listener : listeners) {
 			listener.open(iFile, hasErrors);
 		}
 	}
 
 	/** Fire the close event. */
 	protected void fireClose(IFile iFile, boolean hasErrors) {
 		logger.info("Closing " + iFile.getName() + " (errors:" + hasErrors
 				+ ")");
 		for (IJavaFileStatusListener listener : listeners) {
 			listener.close(iFile, hasErrors);
 		}
 	}
 
 	/** Fire the add event. */
 	protected void fireAdd(IFile iFile, boolean hasErrors) {
 		logger.info("Add " + iFile.getName() + " (errors:" + hasErrors + ")");
 		for (IJavaFileStatusListener listener : listeners) {
 			listener.add(iFile, hasErrors);
 		}
 	}
 
 	/** Fire the remove event. */
 	protected void fireRemove(IFile iFile, boolean hasErrors) {
 		logger.info("Removed " + iFile.getName() + " (errors:" + hasErrors
 				+ ")");
 		for (IJavaFileStatusListener listener : listeners) {
 			listener.remove(iFile, hasErrors);
 		}
 	}
 
 	/** Fire the change event. */
 	protected void fireChange(IFile iFile, boolean hasErrors) {
 		logger.info("Changing " + iFile.getName() + " (errors:" + hasErrors
 				+ ")");
 		for (IJavaFileStatusListener listener : listeners) {
 			listener.change(iFile, hasErrors);
 		}
 	}
 
 	/**
 	 * This class uses the Visitor pattern to visit all nodes of the resource
 	 * delta. It fires the events add, remove, and change.
 	 * 
 	 */
 	class JavaFileResourceDeltaVisitor implements IResourceDeltaVisitor {
 		@Override
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 			if (isJavaFile(delta.getResource())) {
 				IFile iFile = (IFile) resource;
 				boolean hasErrors = false;
 				if (resource.exists()) {
 					hasErrors = IMarker.SEVERITY_ERROR == resource
 							.findMaxProblemSeverity(IMarker.PROBLEM, true,
 									IResource.DEPTH_INFINITE);
 				}
				if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
					if (delta.getKind() == IResourceDelta.ADDED) {
						fireAdd(iFile, hasErrors);
					} else if (delta.getKind() == IResourceDelta.CHANGED) {
						fireChange(iFile, hasErrors);
					} else if (delta.getKind() == IResourceDelta.REMOVED) {
						fireRemove(iFile, false);
					}
 				}
 			}
 			return true;
 		}
 	}
 }
