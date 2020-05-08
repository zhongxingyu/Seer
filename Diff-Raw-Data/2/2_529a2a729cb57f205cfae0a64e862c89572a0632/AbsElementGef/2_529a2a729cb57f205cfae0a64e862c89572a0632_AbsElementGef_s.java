 package org.eclipse.uml2.diagram.sequence.internal.layout.abstractgde.gef;
 
 import java.awt.Color;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
 import org.eclipse.gmf.runtime.notation.View;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.uml2.diagram.common.editparts.PrimaryShapeEditPart;
 import org.eclipse.uml2.diagram.sequence.internal.layout.abstractgde.AbsElement;
 import org.eclipse.uml2.diagram.sequence.internal.missed.MissedMethods;
 
 
 /**
  * 
  */
 public abstract class AbsElementGef implements AbsElement {
     protected AbsElementGef(IGraphicalEditPart modelElementEditPart, AbsDiagramGef diagram) {
         myEditPart = modelElementEditPart;
         myAbsDiagramGef = diagram;
     }
     
     public View getReference() {
         return myEditPart.getNotationView();
     }
     
     public IGraphicalEditPart getEditPart() {
         return myEditPart;
     }
     
     /**
      * Used to store properties in graphical elements.
      */
     public IFigure getFigure() {
         return myEditPart.getFigure();
     }
     
     public IFigure getFigureForColors() {
         return getPrimaryFigure();
     }
     
     public IFigure getPrimaryFigure(){
     	return myEditPart instanceof PrimaryShapeEditPart ? ((PrimaryShapeEditPart)myEditPart).getPrimaryShape() : getFigure();
     }
 
     public void setForeground(Color color) {
     	MissedMethods._iGraphicalEditPart().setForegroundColor(myEditPart, toRGB(color));
     }
 
     public Color getForeground() {
         IFigure figure = getFigureForColors();
         return toAwtColor(figure.getForegroundColor());
     }
 
     public void setBackground(Color color) {
     	MissedMethods._iGraphicalEditPart().setBackgroundColor(myEditPart, toRGB(color));
     }
 
     public Color getBackground() {
         IFigure figure = getFigureForColors();
         return toAwtColor(figure.getBackgroundColor());
     }
     
     private static Color toAwtColor(org.eclipse.swt.graphics.Color gefColor) {
         return new Color(gefColor.getRed(), gefColor.getGreen(), gefColor.getBlue());
     }
     
     private RGB toRGB(Color awtColor){
     	return new RGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());
     }
     
     protected AbsDiagramGef getAbsDiagramGef() {
         return myAbsDiagramGef; 
     }
     protected AbsDiagramGef.AbsElementFactory getAbsElementFactory() {
         return myAbsDiagramGef.getAbsElementFactory(); 
     }
     
     public String toString() {
         StringBuffer result = new StringBuffer("<AbsElementGef:"); //$NON-NLS-1$
         View reference = getReference();
         if (reference == null) {
            result.append("view = null; ").append(this); //$NON-NLS-1$
         } else {
             result.append("view = " + reference.getType()).append("; EP = ").append(getEditPart().getClass().getSimpleName()).append("; entity: ").append(getEditPart().resolveSemanticElement()); //$NON-NLS-1$
         }
         result.append('>');
         return result.toString();
     }
     
     private final IGraphicalEditPart myEditPart;
     private final AbsDiagramGef myAbsDiagramGef;
 }
