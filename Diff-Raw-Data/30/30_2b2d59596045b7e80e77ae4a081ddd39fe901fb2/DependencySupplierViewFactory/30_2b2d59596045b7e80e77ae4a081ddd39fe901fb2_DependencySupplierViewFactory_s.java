 package org.eclipse.uml2.diagram.clazz.view.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EcoreFactory;
 
 import org.eclipse.gmf.runtime.diagram.ui.view.factories.ConnectionViewFactory;
 
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.View;
 
 import org.eclipse.uml2.diagram.clazz.edit.parts.DependencySupplierEditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackageEditPart;
 
 import org.eclipse.uml2.diagram.clazz.part.UMLVisualIDRegistry;
 
 /**
  * @generated
  */
 public class DependencySupplierViewFactory extends ConnectionViewFactory {
 
 	/**
 	 * @generated 
 	 */
 	protected List createStyles(View view) {
 		List styles = new ArrayList();
		styles.add(NotationFactory.eINSTANCE.createConnectorStyle());
 		styles.add(NotationFactory.eINSTANCE.createFontStyle());
 		return styles;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void decorateView(View containerView, View view, IAdaptable semanticAdapter, String semanticHint, int index, boolean persisted) {
 		if (semanticHint == null) {
 			semanticHint = UMLVisualIDRegistry.getType(DependencySupplierEditPart.VISUAL_ID);
 			view.setType(semanticHint);
 		}
 		super.decorateView(containerView, view, semanticAdapter, semanticHint, index, persisted);
 	}
 }
