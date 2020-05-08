 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.part;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.ui.URIEditorInput;
 import org.eclipse.emf.common.ui.action.WorkbenchWindowActionDelegate;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.gef.Request;
 import org.eclipse.gmf.runtime.common.core.util.Log;
 import org.eclipse.gmf.runtime.common.core.util.Trace;
 import org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.ShapeEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.internal.editparts.ISurfaceEditPart;
 import org.eclipse.gmf.runtime.diagram.ui.render.internal.DiagramUIRenderDebugOptions;
 import org.eclipse.gmf.runtime.diagram.ui.render.internal.DiagramUIRenderPlugin;
 import org.eclipse.gmf.runtime.diagram.ui.render.internal.DiagramUIRenderStatusCodes;
 import org.eclipse.gmf.runtime.diagram.ui.render.internal.dialogs.CopyToImageDialog;
 import org.eclipse.gmf.runtime.diagram.ui.render.internal.l10n.DiagramUIRenderMessages;
 import org.eclipse.gmf.runtime.diagram.ui.render.util.CopyToImageUtil;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWTError;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.fritzing.fritzing.diagram.edit.parts.SketchEditPart;
 
 /**
  * Taken from the ImageExportWizard from STP project,
  * modified to run as an action instead of an export wizard.
  * 
  * @generated NOT
  */
 public class FritzingImageExportAction extends WorkbenchWindowActionDelegate {
 
 	public void run(IAction action) {
 		FritzingImageExportActionImpl a = new FritzingImageExportActionImpl(
 				getWindow().getActivePage().getActiveEditor());
 		a.run();
 	}
 
 	public class FritzingImageExportActionImpl extends DiagramAction {
 
 		public FritzingImageExportActionImpl(IWorkbenchPage workbenchPage) {
 			super(workbenchPage);
 		}
 		
 		public FritzingImageExportActionImpl(IWorkbenchPart workbenchpart) {
 			super(workbenchpart);
 		}
 
 		/**
 		 * the copy diagram to image file dialog used by the action.
 		 */
 		private CopyToImageDialog dialog = null;
 
 		/**
 		 * @generated NOT
 		 */
 		public void run() {
 			IPath path = null;
 			String fileName = null;
 
 			if (getWorkbenchPart() instanceof IEditorPart) {
 				IEditorPart editor = (IEditorPart) getWorkbenchPart();
 
 				// The editor's input may provide us with an IContainer where
 				// we should store items related to it.
 				IContainer container = (IContainer) editor.getEditorInput()
 						.getAdapter(IContainer.class);
 
 				// If there is a container in the workspace and it exists then
 				// we will use its path to store the image.
 				if (container != null && container.exists()) {
 					// The path has to be an absolute filesystem path for this
 					// use case rather than just the path relative to the
 					// workspace
 					// root.
 					path = container.getLocation();
 				}
 
 				// Otherwise, we will try to adapt the input to the IFile that
 				// represents the place where the editor's input file resides.
 				// We can extrapolate a destination path from this file.
 				if (path == null) {
 					URIEditorInput input = (URIEditorInput) editor.getEditorInput();
 					URI uri = input.getURI();
 
 					// We can't necessarily assume that the editor input is a
 					// file.
 					if (uri != null) {
						path = new Path(uri.trimSegments(1).toFileString());
 						fileName = uri.trimFileExtension().lastSegment();
 					}
 				}
 			}
 
 			dialog = new CopyToImageDialog(Display.getCurrent()
 					.getActiveShell(), path, fileName);
 			if (dialog.open() == CopyToImageDialog.CANCEL) {
 				return;
 			}
 
 			if (!overwriteExisting()) {
 				return;
 			}
 
 			Trace
 					.trace(
 							DiagramUIRenderPlugin.getInstance(),
 							"Copy Diagram to " + dialog.getDestination().toOSString() + " as " + dialog.getImageFormat().toString()); //$NON-NLS-1$ //$NON-NLS-2$
 
 			final MultiStatus status = new MultiStatus(DiagramUIRenderPlugin
 					.getPluginId(), DiagramUIRenderStatusCodes.OK,
 					DiagramUIRenderMessages.CopyToImageAction_Label, null);
 
 			IRunnableWithProgress runnable = createRunnable(status);
 
 			ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
 					Display.getCurrent().getActiveShell());
 			try {
 				progressMonitorDialog.run(false, true, runnable);
 			} catch (InvocationTargetException e) {
 				Log.warning(DiagramUIRenderPlugin.getInstance(),
 						DiagramUIRenderStatusCodes.IGNORED_EXCEPTION_WARNING, e
 								.getTargetException().getMessage(), e
 								.getTargetException());
 
 				if (e.getTargetException() instanceof OutOfMemoryError) {
 					openErrorDialog(DiagramUIRenderMessages.CopyToImageAction_outOfMemoryMessage);
 				} else if (e.getTargetException() instanceof SWTError) {
 					/**
 					 * SWT returns an out of handles error when processing large
 					 * diagrams
 					 */
 					openErrorDialog(DiagramUIRenderMessages.CopyToImageAction_outOfMemoryMessage);
 				} else {
 					openErrorDialog(e.getTargetException().getMessage());
 				}
 				return;
 			} catch (InterruptedException e) {
 				/* the user pressed cancel */
 				Log.warning(DiagramUIRenderPlugin.getInstance(),
 						DiagramUIRenderStatusCodes.IGNORED_EXCEPTION_WARNING, e
 								.getMessage(), e);
 			}
 
 			if (!status.isOK()) {
 				openErrorDialog(status.getChildren()[0].getMessage());
 			}
 		}
 
 		/**
 		 * copy the selected shapes in the diagram to an image file.
 		 * 
 		 * @param diagramEditPart
 		 *            the diagram editor
 		 * @param list
 		 *            list of selected shapes in the diagram
 		 * @param destination
 		 *            path to the new image file
 		 * @param imageFormat
 		 *            image format to create
 		 * @return the runnable with a progress monitor
 		 */
 		private IRunnableWithProgress createRunnable(final MultiStatus status) {
 			return new IRunnableWithProgress() {
 
 				public void run(IProgressMonitor monitor) {
 					try {
 						List editparts = createOperationSet(); 
 //							getOperationSet(); // This is buggy, submitted to Bugzilla
 
 						if (editparts.size() == 1
 								&& editparts.get(0) instanceof DiagramEditPart) {
 							monitor.beginTask("", 6); //$NON-NLS-1$
 							monitor.worked(1);
 							monitor
 									.setTaskName(NLS
 											.bind(
 													DiagramUIRenderMessages.CopyToImageAction_copyingDiagramToImageFileMessage,
 													dialog.getDestination()
 															.toOSString()));
 							new CopyToImageUtil().copyToImage(
 									(DiagramEditPart) editparts.get(0), dialog
 											.getDestination(), dialog
 											.getImageFormat(), monitor);
 						} else {
 							monitor.beginTask("", 6); //$NON-NLS-1$
 							monitor.worked(1);
 							monitor
 									.setTaskName(NLS
 											.bind(
 													DiagramUIRenderMessages.CopyToImageAction_copyingSelectedElementsToImageFileMessage,
 													dialog.getDestination()
 															.toOSString()));
 							new CopyToImageUtil().copyToImage(
 									getDiagramEditPart(), editparts, dialog
 											.getDestination(), dialog
 											.getImageFormat(), monitor);
 						}
 					} catch (CoreException e) {
 						Log
 								.warning(
 										DiagramUIRenderPlugin.getInstance(),
 										DiagramUIRenderStatusCodes.IGNORED_EXCEPTION_WARNING,
 										e.getMessage(), e);
 						status.add(e.getStatus());
 					} finally {
 						monitor.done();
 					}
 				}
 			};
 		}
 
 		/**
 		 * display an error dialog
 		 * 
 		 * @param message
 		 *            cause of the error
 		 */
 		private void openErrorDialog(String message) {
 			MessageDialog
 					.openError(
 							Display.getCurrent().getActiveShell(),
 							DiagramUIRenderMessages.CopyToImageAction_copyToImageErrorDialogTitle,
 							NLS
 									.bind(
 											DiagramUIRenderMessages.CopyToImageAction_copyToImageErrorDialogMessage,
 											message));
 		}
 
 		/**
 		 * Warn the user with a question dialog if an existing file is going to
 		 * be overwritten and the user has not selected overwrite existing.
 		 * 
 		 * @return true of it is ok to continue with the copy to image.
 		 */
 		private boolean overwriteExisting() {
 			if (dialog.overwriteExisting()) {
 				/**
 				 * the user has selected to overwrite existing
 				 */
 				return true;
 			}
 
 			if (!dialog.getDestination().toFile().exists()) {
 				/**
 				 * the file does not already exist
 				 */
 				return true;
 			}
 
 			/**
 			 * ask the user to confirm to overwrite existing file.
 			 */
 			return MessageDialog
 					.openQuestion(
 							Display.getCurrent().getActiveShell(),
 							DiagramUIRenderMessages.CopyToImageAction_overwriteExistingConfirmDialogTitle,
 							NLS
 									.bind(
 											DiagramUIRenderMessages.CopyToImageAction_overwriteExistingConfirmDialogMessage,
 											dialog.getDestination()
 													.toOSString()));
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.gmf.runtime.diagram.ui.actions.DiagramAction#createOperationSet()
 		 */
 		protected List createOperationSet() {
 			List selection = getSelectedObjects();
 
 			if (selection.size() == 1) {
 				Object editpart = selection.get(0);
 				if (editpart instanceof DiagramEditPart) {
 					return selection;
 				}
 				if (editpart instanceof ISurfaceEditPart) {
 					selection = ((ISurfaceEditPart) editpart)
 							.getPrimaryEditParts();
 				}
 			}
 
 			// must contain at least one shape
 			for (Iterator iter = selection.iterator(); iter.hasNext();) {
 				Object editpart = iter.next();
 				if (editpart instanceof ShapeEditPart) {
 					return selection;
 				}
 			}
 			
 			// handles the case when another view is selected, (e.g. Properties)
 			if (selection.size() == 0) {
 				FritzingDiagramEditor diagram = FritzingDiagramEditorUtil.getActiveDiagramPart();
 				if (diagram != null) {
 					DiagramEditPart part = diagram.getDiagramEditPart();
 					List autoSelection = new ArrayList();
 					autoSelection.add((SketchEditPart) part);
 					return autoSelection;
 				}
 			}
 			
 			return Collections.EMPTY_LIST;
 		}
 
 
 		/**
 		 * This action is not really a <code>DiagramAction</code> as it
 		 * doesn't have a request. The doRun() and calculatedEnabled() have been
 		 * overwritten appropriately.
 		 */
 		protected Request createTargetRequest() {
 			return null;
 		}
 
 		protected void doRun(IProgressMonitor progressMonitor) {
 			try {
 				// whatever we are copying belongs to the same editing domain as
 				// the Diagram
 				getDiagramEditPart().getEditingDomain().runExclusive(
 						new Runnable() {
 
 							public void run() {
 								FritzingImageExportActionImpl.this.run();
 							}
 						});
 			} catch (Exception e) {
 				Trace.catching(DiagramUIRenderPlugin.getInstance(),
 						DiagramUIRenderDebugOptions.EXCEPTIONS_CATCHING,
 						getClass(), "doRun()", //$NON-NLS-1$
 						e);
 			}
 		}
 
 		/**
 		 * Subclasses may override to specialize the rendering to an image file.
 		 * 
 		 * @return the <code>CopyToImageUtil</code> class to be used.
 		 */
 		protected CopyToImageUtil getCopyToImageUtil() {
 			return new CopyToImageUtil();
 		}
 		
 		protected boolean isSelectionListener() {
 			return true;
 		}
 
 
 		protected boolean calculateEnabled() {
 //			return !getOperationSet().isEmpty();
 			return true;
 		}
 	}
 }
