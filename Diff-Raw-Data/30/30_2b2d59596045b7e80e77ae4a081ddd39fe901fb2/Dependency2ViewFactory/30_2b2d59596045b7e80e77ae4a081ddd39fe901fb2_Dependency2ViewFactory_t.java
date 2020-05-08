 package org.eclipse.uml2.diagram.clazz.view.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcoreFactory;
 
 import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
 
 import org.eclipse.gmf.runtime.diagram.ui.view.factories.ConnectionViewFactory;
 
 import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.View;
 
 import org.eclipse.uml2.diagram.clazz.edit.parts.Dependency2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.DependencyName2EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.DependencyName3EditPart;
 import org.eclipse.uml2.diagram.clazz.edit.parts.PackageEditPart;
 
 import org.eclipse.uml2.diagram.clazz.part.UMLVisualIDRegistry;
 
 /**
  * @generated
  */
 public class Dependency2ViewFactory extends ConnectionViewFactory {
 
 	/**
 	 * @generated 
 	 */
 	protected List createStyles(View view) {
 		List styles = new ArrayList();
		styles.add(NotationFactory.eINSTANCE.createRoutingStyle());
 		styles.add(NotationFactory.eINSTANCE.createFontStyle());
 		return styles;
 	}
 
 	/**
 	 * @generated
 	 */
 	protected void decorateView(View containerView, View view, IAdaptable semanticAdapter, String semanticHint, int index, boolean persisted) {
 		if (semanticHint == null) {
 			semanticHint = UMLVisualIDRegistry.getType(Dependency2EditPart.VISUAL_ID);
 			view.setType(semanticHint);
 		}
 		super.decorateView(containerView, view, semanticAdapter, semanticHint, index, persisted);
 		IAdaptable eObjectAdapter = null;
 		EObject eObject = (EObject) semanticAdapter.getAdapter(EObject.class);
 		if (eObject != null) {
 			eObjectAdapter = new EObjectAdapter(eObject);
 		}
 		getViewService().createNode(eObjectAdapter, view, UMLVisualIDRegistry.getType(DependencyName2EditPart.VISUAL_ID), ViewUtil.APPEND, true, getPreferencesHint());
 		getViewService().createNode(eObjectAdapter, view, UMLVisualIDRegistry.getType(DependencyName3EditPart.VISUAL_ID), ViewUtil.APPEND, true, getPreferencesHint());
 	}
 }
