 package org.eclipse.emf.refactor.refactoring.papyrus.managers;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.facet.infra.browser.uicore.internal.model.ModelElementItem;
 import org.eclipse.emf.refactor.refactoring.managers.SelectionManager;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.jface.viewers.ISelection;
 
 @SuppressWarnings("restriction")
 public class PapyrusSelectionManager extends SelectionManager {
 
 	public static List<EObject> getESelection(ISelection selection) {
 		if (selection == null)
 			return null;
 		List<EObject> r = SelectionManager.getESelection(selection);
 		for (Object o : getSelection(selection)) {
 			if (o instanceof ModelElementItem) {
 				System.out.println("instanceof ModelElementItem");
 				ModelElementItem mei = (ModelElementItem) o;
 	    		System.out.println("element: " + mei.getEObject());
 	    		r.add(mei.getEObject());
 			} else {
 				if (o instanceof IGraphicalEditPart) {
 		    		System.out.println("instanceof IGraphicalEditPart");
 		    		IGraphicalEditPart gep = (IGraphicalEditPart) o;
 		    		System.out.println("element: " + gep.resolveSemanticElement());
 		    		r.add(gep.resolveSemanticElement());
 		    	} else {
 		    		return null;
 		    	}
 			}
 		}
 		return r;
 	}

 }
