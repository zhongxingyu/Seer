 package org.eclipse.bpmn2.modeler.core;
 
import org.eclipse.gef.editparts.AbstractEditPart;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 public class Bpmn2TabbedPropertySheetPage extends TabbedPropertySheetPage {
 
 	DiagramEditor diagramEditor;
 	
 	public Bpmn2TabbedPropertySheetPage(
 			ITabbedPropertySheetPageContributor tabbedPropertySheetPageContributor) {
 		super(tabbedPropertySheetPageContributor);
 		diagramEditor = (DiagramEditor)tabbedPropertySheetPageContributor;
 	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// Ignore selections from Source Viewer for now.
		// When there is better synchronization between Source Viewer and Design Editor
		// we can navigate from the selected IDOMNode to the BPMN2 model element and
		// modify the selection here...
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			Object elem = ss.getFirstElement();
			if (!(elem instanceof AbstractEditPart))
				return;
		}
		super.selectionChanged(part, selection);
	}
 
 	public DiagramEditor getDiagramEditor() {
 		return diagramEditor;
 	}
 }
