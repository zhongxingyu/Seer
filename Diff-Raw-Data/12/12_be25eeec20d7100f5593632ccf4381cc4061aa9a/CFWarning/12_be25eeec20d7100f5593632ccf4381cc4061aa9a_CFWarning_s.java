 package org.bicknese.cfwarning;
 
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.*;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.model.*;
 import org.eclipse.ui.texteditor.ITextEditor;
 
 public class CFWarning extends ViewPart implements IPartListener2, IPropertyListener {
 	
 	ListViewer viewer;
 	String fileName;
   
 	public CFWarning() {;}
   
 	public void createPartControl(Composite parent) {
 		
 		viewer = new ListViewer(parent);
 		
 		// if the objects in the viewer implement the IDesktopElement adapter,
 		// these generic content and label providers can be used.
 		viewer.setContentProvider(new WorkbenchContentProvider());
 		viewer.setLabelProvider(new WorkbenchLabelProvider());
 		
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				
 				ISelection selection = event.getSelection();
 				if(selection != null && (selection instanceof IStructuredSelection)) {
 					IStructuredSelection ss = (IStructuredSelection)selection;
 					
 					if(ss.getFirstElement() instanceof IClick) {
 						IClick c = (IClick)ss.getFirstElement();
 						IEditorPart ip = getSite().getPage().getActiveEditor();
						ip.setFocus();
						if(ip instanceof ITextEditor) {
							ITextEditor ie = (ITextEditor)ip;
							ie.setHighlightRange(c.getOffset(), c.getRange(), true);
 						}
 					}
 					
 				}
 			}
 		});
 		
 		//add myself as a global listener
 		getSite().getPage().addPartListener(this);
 		
 	}
   
 	@Override
 	public void partActivated(IWorkbenchPartReference partRef) {
 
 		partRef.addPropertyListener(this);
 		
 		try {
 			String activatedPart = partRef.getPart(false).getClass().toString();
 			String editorPart = getSite().getPage().getActiveEditor().getClass().toString();
 			if(activatedPart.compareTo(editorPart) == 0) {
 				AdaptableList input = CFWarningModelFactory.getInstance().getWarnings(partRef.getTitleToolTip());
 				viewer.setInput(input);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void propertyChanged(Object source, int propId) {
 
 		try {
 			IEditorPart editor = (IEditorPart)source;
 			AdaptableList input = CFWarningModelFactory.getInstance().getWarnings(editor.getTitleToolTip());
 			viewer.setInput(input);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void partDeactivated(IWorkbenchPartReference partRef) {
 		partRef.removePropertyListener(this);
 	}
 
 	@Override
 	public void setFocus() {
 		
 	}
 
 	@Override
 	public void partBroughtToTop(IWorkbenchPartReference partRef) {
 
 	}
 
 	@Override
 	public void partClosed(IWorkbenchPartReference partRef) {
 		partRef.removePropertyListener(this);
 	}
 
 	@Override
 	public void partHidden(IWorkbenchPartReference partRef) {
 
 	}
 
 	@Override
 	public void partInputChanged(IWorkbenchPartReference partRef) {
 
 	}
 
 	@Override
 	public void partOpened(IWorkbenchPartReference partRef) {
 
 	}
 
 	@Override
 	public void partVisible(IWorkbenchPartReference partRef) {
 
 	}
 
 }
