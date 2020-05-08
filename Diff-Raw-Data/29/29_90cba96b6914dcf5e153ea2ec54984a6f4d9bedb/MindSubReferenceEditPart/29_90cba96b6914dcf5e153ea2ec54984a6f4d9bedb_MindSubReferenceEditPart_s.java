 package org.ow2.mindEd.adl.editor.graphic.ui.custom.edit.parts.generic;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.gmf.runtime.diagram.ui.editparts.GraphicalEditPart;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.listeners.MindListLayoutListener;
 
 public class MindSubReferenceEditPart extends MindReferenceEditPart {
 
 	
 	
 	public MindSubReferenceEditPart(GraphicalEditPart editPart, int vID) {
 		super(editPart, vID, TYPE_REFERENCE);
 	}
 	
 	public MindSubReferenceEditPart(GraphicalEditPart editPart, int vID, int mindType) {
 		super(editPart, vID, mindType);
 	}
 	
 	@Override
 	public IFigure setupContentPane(IFigure nodeShape) {
 		if (nodeShape.getLayoutManager() == null) {
 			ConstrainedToolbarLayout layout = new ConstrainedToolbarLayout();
 			nodeShape.setLayoutManager(layout);
			nodeShape.addLayoutListener(new MindListLayoutListener(realEditPart));
 		}
 		return nodeShape; // use nodeShape itself as contentPane
 	}
 
 }
