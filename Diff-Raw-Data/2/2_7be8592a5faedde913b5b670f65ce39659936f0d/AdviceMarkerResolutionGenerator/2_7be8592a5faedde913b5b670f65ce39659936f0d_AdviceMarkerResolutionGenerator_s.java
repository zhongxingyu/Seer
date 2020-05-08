 package org.caesarj.ui.marker;
 
 import org.apache.log4j.Logger;
 import org.aspectj.asm.LinkNode;
 import org.caesarj.ui.editor.CaesarEditor;
 import org.caesarj.ui.util.ProjectProperties;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IMarkerResolution;
 import org.eclipse.ui.IMarkerResolutionGenerator;
 import org.eclipse.ui.IMarkerResolutionGenerator2;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 
 /**
  * @author Shadow
  * 
  * Folgendes auswhlen, um die Schablone fr den erstellten Typenkommentar zu
  * ndern: Fenster&gt;Benutzervorgaben&gt;Java&gt;Codegenerierung&gt;Code und
  * Kommentare
  */
 public class AdviceMarkerResolutionGenerator implements
 		IMarkerResolutionGenerator, IMarkerResolutionGenerator2 {
 
 	static Logger logger = Logger
 			.getLogger(AdviceMarkerResolutionGenerator.class);
 
 	public IMarkerResolution[] getResolutions(IMarker marker) {
 		try {
 			LinkNode advices[] = (LinkNode[]) marker
 					.getAttribute(AdviceMarker.LINKS);
 			IMarkerResolution res[] = new AdviceMarkerResolution[advices.length];
 			IMarkerResolution temp = null;
 			for (int i = 0; i < advices.length; i++) {
 				res[i] = new AdviceMarkerResolution(advices[i], marker);
 			}
 			return res;
 		} catch (CoreException e) {
 			logger.error("Fehler beim auslesen der LINKS aus AdviceMarker", e); //$NON-NLS-1$
 		}
 		return null;
 	}
 
 	public boolean hasResolutions(IMarker marker) {
 		return true;
 	}
 
 	public class AdviceMarkerResolution implements IMarkerResolution {
 		private LinkNode link;
 
 		boolean toAdvice = false;
 
 		public AdviceMarkerResolution(LinkNode linkArg, IMarker marker) {
 			super();
 			this.link = linkArg;
 			try {
 				this.toAdvice = marker.getAttribute(AdviceMarker.ID).equals(
 						"AdviceLink"); //$NON-NLS-1$
 			} catch (CoreException e) {
 			}
 		}
 
 		public String getLabel() {
			return this.toAdvice ? "Open Advice: " + this.link.getName() : "Open Methode: " + this.link.getName(); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		public void run(IMarker marker) {
 			IWorkbenchWindow w = PlatformUI.getWorkbench()
 					.getActiveWorkbenchWindow();
 			if (w == null)
 				return;
 			IWorkbenchPage page = w.getActivePage();
 			if (page == null)
 				return;
 			IProject activeProject = ((CaesarEditor) page.getActiveEditor())
 					.getInputJavaElement().getJavaProject().getProject();
 			try {
 				IDE.openEditor(page, this.getLinkLocation(activeProject), true);
 			} catch (PartInitException e) {
 				MessageDialog.openError(w.getShell(),
 						"ERROR", "Unable to open Editor!"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 
 		private IFile getLinkLocation(IProject activeProject) {
 			String fullPath = this.link.getProgramElementNode()
 					.getSourceLocation().getSourceFile().getAbsolutePath();
 			return (IFile) ProjectProperties.findResource(fullPath,
 					activeProject);
 		}
 
 	}
 }
