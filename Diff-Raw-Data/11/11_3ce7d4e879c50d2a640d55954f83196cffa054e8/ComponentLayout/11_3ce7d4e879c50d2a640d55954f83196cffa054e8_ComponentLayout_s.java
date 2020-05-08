 package org.ow2.mindEd.adl.editor.graphic.ui.custom.layouts;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.ConstrainedToolbarLayout;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.figures.AbstractComponentNameWrappingLabel;
 import org.ow2.mindEd.adl.editor.graphic.ui.custom.layouts.IFractalSize;
 
 /**
  * Custom layout for our diagram editor.<p>
  * - understands constraints from edit parts of type "List"<p>
  * - name label is added at the top of the component, 
  * lists are added to the bottom then body fills the space left.<p>
  * Every component's main edit part should use this layout
  * @author maroto
  *
  */
 public class ComponentLayout extends ConstrainedToolbarLayout implements IFractalSize {
 	
 	/**
 	 * The constraints map
 	 */
 	private Map<IFigure,Rectangle> constraints;
 	
 	
 	@SuppressWarnings("rawtypes")
 	public void layout(IFigure figure) {
 		Rectangle clientArea = figure.getClientArea();
 		List children = figure.getChildren();
 		IFigure child;
 		IFigure body = null;
 		int totalTitlesHeight = 0;
 		int totalListsHeight = 0;
 		
 		for (int i = 0; i < children.size(); i++) {
 			child = (IFigure)children.get(i);
 			
 			// Label is added on top
 			if (child instanceof AbstractComponentNameWrappingLabel) { 
 				child.setBounds(
 						new Rectangle(
 							clientArea.x,
 							clientArea.y,
 							clientArea.width,
 							TITLE_HEIGHT
 							)
 						);
 				// Remember the total height of all elements
 				totalTitlesHeight += TITLE_HEIGHT;
 			}
 			
 			else if (child instanceof BorderedNodeFigure) {
 				// Body is the last to be computed, to let it fill the space left
 				body = child;
 			}
 			
 			else {
 				// Child is a list, get its constraint
 				Object constraint = this.getConstraint(child);
 				Rectangle rect;
 				if (constraint != null) {
 					rect = ((Rectangle)constraint).getCopy();
 				}
 				else {
 					rect = new Rectangle(-1,-1,-1,-1);
 				}
 				
 				if (rect.height < 0 || rect.height > MAX_LIST_HEIGHT) {
					rect.height = MAX_LIST_HEIGHT;
 				}
 				if (rect.width < 0) {
 					rect.width = clientArea.width;
 				}
				if (rect.x <= 0) {
 					rect.x = clientArea.x;
				}
				if (rect.y <= 0) {
 					rect.y = clientArea.y + clientArea.height - rect.height - totalListsHeight;
				}
 				
 				child.setBounds(rect);
 				// Remember the total height of all elements
 				totalListsHeight += rect.height;
 			}			
 		}
 		
 		if (body != null) {
 			// The height of the body is the height of the area
 			// minus the height already used
 			body.setBounds(new Rectangle(
 					clientArea.x,
 					clientArea.y + totalTitlesHeight,
 					clientArea.width,
 					clientArea.height - totalTitlesHeight - totalListsHeight
 					));
 			
 			// TODO resize the figure to allow the body to have enough space to wrap the children 
 //			Object constraint = this.getConstraint(body);
 //			Rectangle rect;
 //			if (constraint != null) {
 //				rect = ((Rectangle)constraint).getCopy();
 //				rect.height += totalTitlesHeight + totalListsHeight + 5;
 //				Rectangle bounds = figure.getBounds();
 //				bounds.width = Math.max(rect.width, bounds.width);
 //				bounds.height = Math.max(rect.height, bounds.height);
 //				figure.setBounds(bounds);
 //				figure.getParent().setSize(bounds.width, bounds.height);
 //				figure.getParent().setBounds(bounds);
 //			}
 		}
 		
 		
 	}
 	
 	
 //	public void getListHeightFromChildren(IFigure listFigure) {
 //		
 //	}
 	
 	
 	
 	/**
 	 * Sets the constraint for the given figure. Constraint must be a Rectangle,
 	 * an attribute (x,y,width,height) set to -1 means it has no constraint.
 	 */
 	public void setConstraint(IFigure child, Object constraint) {
 		if (constraint instanceof Rectangle) {
 			Rectangle r = (Rectangle) constraint;
 			super.setConstraint(child, constraint);
 			if (constraints == null)
 				constraints = new HashMap<IFigure,Rectangle>();
 			if (constraint == null || r.equals(new Rectangle(-1,-1,-1,-1))) {
 				if (constraints.containsKey(child))
 					constraints.remove(child);
 			} else
 				constraints.put(child, r);
 		}
 		// Not implemented yet
 		// Having multiple kind of constraints is not really necessary but could clarify the code
 //		else if (constraint instanceof ListConstraint) {
 //			return;
 //		}
 //		else if (constraint instanceof TitleConstraint) {
 //			return;
 //		}
 	}
 	
 	/**
 	 * Returns the constraint for the given figure.
 	 * @param child The figure
 	 * @return The constraint
 	 */
 	public Object getConstraint(IFigure child) {
 		if (constraints != null)
 			return constraints.get(child);
 		
 		return null;
 	}
 
 	@Override
 	protected Dimension calculatePreferredSize(IFigure container, int wHint,
 			int hHint) {
 		Rectangle clientArea = container.getClientArea();
 		int preferredWidth = clientArea.width;
 		int preferredHeight;
 		
 		if (hHint >= (clientArea.height)/4) {
 			preferredHeight = (clientArea.height)/4;
 		}
 		else {
 			preferredHeight = hHint;
 		}
 		return new Dimension(preferredWidth,preferredHeight);
 	}
 
 
 	
 }
 	
 
