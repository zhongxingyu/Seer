 package org.eclipse.iee.editor.jdt.editors;
 
 import org.eclipse.iee.editor.IeeEditorPlugin;
 import org.eclipse.iee.editor.core.container.ContainerManager;
 import org.eclipse.iee.editor.core.pad.Pad;
 import org.eclipse.iee.editor.core.pad.PadManager;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.custom.StyledText;
 
 @SuppressWarnings("restriction")
 public class ExtendedJavaEditor extends CompilationUnitEditor {
 	
 	private ContainerManager fContainerManager;
 	private PadManager fPadManager;
 	
 	public void createPartControl(org.eclipse.swt.widgets.Composite parent) {
 		super.createPartControl(parent);
 		
 		initIeeEditorCore();
 	};
 	
 	protected void initIeeEditorCore() {
 		StyledText styledText = getSourceViewer().getTextWidget();
 		IDocument document = getSourceViewer().getDocument();
 
 		fContainerManager = new ContainerManager(document, styledText);
 		
 		fPadManager = IeeEditorPlugin.getDefault().getPadManager();
 		fPadManager.registerContainerManager(fContainerManager);
 	}
 	
 	public void dispose() {
 		fPadManager.removeContainerManager(fContainerManager);
 		fContainerManager = null;
 				
 		super.dispose();
 	}
 	
 	public void createPad(Pad pad, int location) {
		fPadManager.insertPad(pad, location, fContainerManager);	
 	}
 	
 	public int getCaretOffset() {
 		return getSourceViewer().getTextWidget().getCaretOffset();
 	}
 }
