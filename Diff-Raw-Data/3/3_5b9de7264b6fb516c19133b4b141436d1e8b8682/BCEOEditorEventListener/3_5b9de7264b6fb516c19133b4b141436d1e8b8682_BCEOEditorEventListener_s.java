 package edu.berkeley.eduride.editoroverlay;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 
 import edu.berkeley.eduride.base_plugin.util.Console;
 import edu.berkeley.eduride.base_plugin.util.IPartListenerInstaller;
 
 public class BCEOEditorEventListener implements IPartListener2 {
 
 	
 	public BCEOEditorEventListener(boolean install) {
 
 		String errStr;
 		// install listener for editor events
 		errStr = IPartListenerInstaller.installOnWorkbench(this, "BCEO");
 		if (errStr != null) {
			Console.err(errStr);
			
 		}
 
 		// install on currently open editors
 		// run this in the UI thread?
 		Display.getDefault().asyncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				ArrayList<IEditorPart> eds = IPartListenerInstaller
 						.getCurrentEditors();
 				for (IEditorPart ed : eds) {
 					installDance(ed);
 				}
 			}
 		}
 
 		);
 
 		//Console.msg("BCEOEditorEventListener - yo");
 
 	}
 	
 	
 	private static void installDance(IWorkbenchPartReference partRef) {
 		IEditorPart editor = getEditor(partRef);
 		if (editor != null) {
 			installDance(editor);
 		}
 	}
 	
 	private static void installDance(IEditorPart ed) {
 		BoxConstrainedEditorOverlay.ensureInstalled(ed);
 		// TODO install dance
 	}
 	
 	
 	
 	private static IEditorPart getEditor(IWorkbenchPartReference partRef) {
 		IWorkbenchPart part = partRef.getPart(false);
 		IEditorPart editor = null;
 		if (part != null && part instanceof IEditorPart) {
 			editor = (IEditorPart) part.getAdapter(IEditorPart.class);
 		}
 		return editor;
 	}
 	
 	
 	private static void drawBoxesOnEditor(IEditorPart editor) {
 		BoxConstrainedEditorOverlay bceo = BoxConstrainedEditorOverlay.getBCEO(editor);
 		if (bceo != null) {
 			bceo.drawBoxes();
 		}
 	}
 	
 	////////////////////////
 	
 	@Override
 	public void partActivated(IWorkbenchPartReference partRef) {
 		//drawBoxesOnEditor(getEditor(partRef));
 	}
 
 	@Override
 	public void partBroughtToTop(IWorkbenchPartReference partRef) {
 
 
 	}
 
 	@Override
 	public void partClosed(IWorkbenchPartReference partRef) {
 
 
 	}
 
 	@Override
 	public void partDeactivated(IWorkbenchPartReference partRef) {
 
 
 	}
 
 	@Override
 	public void partOpened(IWorkbenchPartReference partRef) {
 		installDance(partRef);
 		
 	}
 
 	@Override
 	public void partHidden(IWorkbenchPartReference partRef) {
 
 
 	}
 
 	@Override
 	public void partVisible(IWorkbenchPartReference partRef) {
 
 
 	}
 
 	@Override
 	public void partInputChanged(IWorkbenchPartReference partRef) {
 		// TODO we probably need to worry about this, yo
 
 	}
 
 }
