 package objective;
 
 import objective.primitivehighlight.PrimitiveAnnotationModel;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class ObjectiveUtilsPlugin extends AbstractUIPlugin {
 	PrimitiveWatcher partListener = new PrimitiveWatcher();
 
 	public static final String PLUGIN_ID = "objective.eclipse.utils"; //$NON-NLS-1$
 
 	private static ObjectiveUtilsPlugin plugin;
 
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		
 		addPrimitiveAnnotationListeners();
 	}
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	public static ObjectiveUtilsPlugin getDefault() {
 		return plugin;
 	}
 
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
 	private void addPrimitiveAnnotationListeners() {
 		IWorkbench workbench = getWorkbench();
 		IPartListener2 partListener = new PrimitiveWatcher();
 		for (final IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
 			w.getPartService().addPartListener(partListener);
 		}
 		workbench.addWindowListener(windowListener);
 		annotateAllEditors(workbench);
 	}
 
 	private void annotateAllEditors(IWorkbench workbench) {
 		for (final IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
 			for (final IWorkbenchPage p : w.getPages()) {
 				for (final IEditorReference e : p.getEditorReferences()) {
 					annotateEditor(e);
 				}
 			}
 		}
 	}
 
 	private void annotateEditor(IWorkbenchPartReference partref) {
 		IWorkbenchPart part = partref.getPart(false);
 		if (part instanceof ITextEditor) {
 			PrimitiveAnnotationModel.attach((ITextEditor) part);
 		}
 	}
 	
 	private IWindowListener windowListener = new IWindowListener() {
 		public void windowOpened(IWorkbenchWindow window) {
 			window.getPartService().addPartListener(partListener);
 		}
 
 		public void windowClosed(IWorkbenchWindow window) {
 			window.getPartService().removePartListener(partListener);
 		}
 
 		public void windowActivated(IWorkbenchWindow window) { }
 
 		public void windowDeactivated(IWorkbenchWindow window) { }
 	};
 
 	private final class PrimitiveWatcher implements IPartListener2 {
 
 		@Override
 		public void partOpened(IWorkbenchPartReference partRef) {
 			annotateEditor(partRef);
 		}
 
 		@Override public void partActivated(IWorkbenchPartReference partRef) { }
 
 		@Override public void partBroughtToTop(IWorkbenchPartReference partRef) { }
 
 		@Override public void partClosed(IWorkbenchPartReference partRef) { }
 
 		@Override public void partDeactivated(IWorkbenchPartReference partRef) { }
 
 		@Override public void partHidden(IWorkbenchPartReference partRef) { }
 
 		@Override public void partVisible(IWorkbenchPartReference partRef) { }
 
 		@Override public void partInputChanged(IWorkbenchPartReference partRef) { }
 	}
 	
 	public static void log(Exception e) {
 		plugin.getLog().log(errorStatus(e.getMessage(), e));
 	}
 
 	public static void log(String message, Exception e) {
 		plugin.getLog().log(errorStatus(message, e));
	}	
 	
	public static IStatus errorStatus(String message, Throwable t) {
 		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, t);
 	}
 
 }
