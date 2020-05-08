 package org.eclipse.emf.refactor.smells.xtext.managers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.jface.text.source.ISourceViewer;
 import org.eclipse.xtext.nodemodel.ICompositeNode;
 import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
 import org.eclipse.xtext.ui.editor.XtextEditor;
 import org.eclipse.xtext.ui.editor.utils.EditorUtils;
 
 import com.google.inject.Inject;
 
 public class XtextHighlightManager {
 	
 	private static XtextHighlightManager instance = null;	
 	private List<EObject> selected; 
 	
 	private XtextHighlightManager() {
 		selected = new ArrayList<EObject>();
 	}
 	
 	public static XtextHighlightManager getInstance() {
 		if(instance == null) {
 			instance = new XtextHighlightManager();
 		}
 		return instance;
 	}
 	
 	@Inject 
 	public void highlight(Object selection) {
 		// clear former selected eObjects
 		selected.clear();
 		// set selected eObjects from selection
 		if (selection instanceof EObjectGroup) {			
 			selected.addAll(((EObjectGroup) selection).getEObjects());
 		}
 		if (selection instanceof EObject) {
 			selected.add((EObject) selection);
 		}
 		// highlight each eObject in editor
 		for (EObject eObject : selected) {
 			XtextEditor xEditor = EditorUtils.getActiveXtextEditor();
 			ICompositeNode node = NodeModelUtils.findActualNodeFor(eObject);
			if (node != null && xEditor != null) {
	    		int offset = node.getOffset();
				int length = node.getLength();
	    		ISourceViewer textViewer = xEditor.getInternalSourceViewer();
				textViewer.setRangeIndication(offset, length, true);
				textViewer.revealRange(offset, length);
				textViewer.setSelectedRange(offset, length);
			}
 		}
 	}
 
 	public List<EObject> getSelected() {
 		return selected;
 	}		
 }
