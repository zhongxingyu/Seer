 /*
  * (c) Fachhochschule Potsdam
  */
 package org.fritzing.fritzing.diagram.view.factories;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.gmf.runtime.diagram.ui.view.factories.ConnectionViewFactory;
 import org.eclipse.gmf.runtime.notation.LineStyle;
 import org.eclipse.gmf.runtime.notation.NotationFactory;
 import org.eclipse.gmf.runtime.notation.NotationPackage;
 import org.eclipse.gmf.runtime.notation.RoutingStyle;
 import org.eclipse.gmf.runtime.notation.Smoothness;
 import org.eclipse.gmf.runtime.notation.Style;
 import org.eclipse.gmf.runtime.notation.View;
 import org.fritzing.fritzing.diagram.edit.parts.LegEditPart;
 import org.fritzing.fritzing.diagram.part.FritzingVisualIDRegistry;
 
 /**
  * @generated
  */
 public class LegViewFactory extends ConnectionViewFactory {
 
 	/**
 	 * @generated NOT
 	 */
 	protected List createStyles(View view) {
 		List styles = new ArrayList();
 		styles.add(NotationFactory.eINSTANCE.createConnectorStyle());
 //		styles.add(NotationFactory.eINSTANCE.createFontStyle());
 		return styles;
 	}
 
 	/**
 	 * @generated NOT
 	 */
 	protected void decorateView(View containerView, View view,
 			IAdaptable semanticAdapter, String semanticHint, int index,
 			boolean persisted) {
 		if (semanticHint == null) {
 			semanticHint = FritzingVisualIDRegistry
 					.getType(LegEditPart.VISUAL_ID);
 			view.setType(semanticHint);
 		}
 		super.decorateView(containerView, view, semanticAdapter, semanticHint,
 				index, persisted);
 
 		// change default style
 		RoutingStyle rstyle = (RoutingStyle) view
 				.getStyle(NotationPackage.eINSTANCE.getRoutingStyle());
 		rstyle.setSmoothness(Smoothness.NORMAL_LITERAL);
         LineStyle lineStyle = (LineStyle) view.getStyle(NotationPackage.eINSTANCE.getLineStyle());
        lineStyle.setLineColor(LegEditPart.LEG_FIGURE_COLOR.hashCode());
 	}
 }
