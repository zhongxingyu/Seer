 //Copyright (C) 2011 Tomáš Vejpustek
 //Full copyright notice found in src/LICENSE.  
 package selector;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 import ltl.ModelChange;
 import ltl.ModifyTransition;
 import ltl.Transition;
 import ui.Canvas;
 import ui.StatusBar;
 import coordinates.Transformation;
 
 /**
  * Selector of a {@link Transition}.
  * 
  * @author Tomáš Vejpustek
  *
  */
 public abstract class TransitionSelector extends AbstractSelector {
 	private Transition target;
	private Transition original = null;
 	private DerivativeSelector derivative;
 	
 	/**
 	 * 
 	 * @param coord Coordinate transformation.
 	 * @return Appropriate selector for <code>target</code>.
 	 */
 	public static TransitionSelector get(Transformation coord, Transition target) {
 		if (target.getConcentration().isSet()) {
 			return new BoxTransitionSelector(coord, target);
 		} else {
 			return new PointTransitionSelector(coord, target);
 		}
 	}
 	
 	/**
 	 * Initializes contained {@link Transition} and {@link Transformation}.
 	 */
 	protected TransitionSelector(Transformation coord, Transition target) {
 		super(coord);
		this.target = target;
 		derivative = DerivativeSelector.get(coord, target.getDerivative(), new Point2D.Double(getTransformation().getTime(getCenter().getX()), getTransformation().getConcentration(getCenter().getY())));
 	}
 	
 	/**
 	 * @return Selected {@link Transition}.
 	 */
 	protected Transition getTarget() {
 		return target;
 	}
 	
 	/**
 	 * @return Included derivative selector.
 	 */
 	protected DerivativeSelector getDerivativeSelector() {
 		return derivative;
 	}
 	
 	/**
 	 * @return Point in the middle of line specifying the transition.
 	 */
 	protected Point2D getCenter() {
 		double leftX = (getTarget().getLeft() != null) ? getTransformation().getX(getTarget().getLeft().getTime().getCenter()) : 0;
 		double rightX = (getTarget().getRight() != null) ? getTransformation().getX(getTarget().getRight().getTime().getCenter()) : getTransformation().getSize().getX();
 		double leftY = (getTarget().getLeft() != null) ? getTransformation().getY(getTarget().getLeft().getConcentration().getCenter()) : getTransformation().getSize().getY()/2;
 		double rightY = (getTarget().getRight() != null) ? getTransformation().getY(getTarget().getRight().getConcentration().getCenter()) : getTransformation().getSize().getY()/2;
 		return new Point2D.Double((leftX+rightX)/2, (leftY+rightY)/2);
 	}
 	
 	@Override
 	public ModelChange delete() {
 		if (isDragging()) {
 			throw new IllegalStateException("Cannot delete Transition that is being dragged.");
 		}
 		Transition empty = new Transition(getTarget().getLeft(), getTarget().getRight());
 		return new ModifyTransition(getTarget(), empty);
 	}
 	@Override
 	public void draw(Canvas c) {
 		c.drawTransition(getTarget());
 		if (!isDragging()){
 			double leftX = (getTarget().getLeft() != null) ? getTransformation().getX(getTarget().getLeft().getTime().getCenter()) : 0;
 			double leftY = (getTarget().getLeft() != null) ? getTransformation().getY(getTarget().getLeft().getConcentration().getCenter()) : getTransformation().getSize().getY()/2;
 			double rightX = (getTarget().getRight() != null) ? getTransformation().getX(getTarget().getRight().getTime().getCenter()) : getTransformation().getSize().getX();
 			double rightY = (getTarget().getRight() != null) ? getTransformation().getY(getTarget().getRight().getConcentration().getCenter()) : getTransformation().getSize().getY()/2;
 			c.drawSelector(new Point2D.Double(leftX, leftY), false);
 			c.drawSelector(new Point2D.Double(rightX, rightY), false);
 		}
 	}
 	@Override
 	public ModelChange endDrag(MouseEvent e) {
 		drag(e);
 		ModelChange result = new ModifyTransition(original, getTarget());
 		original = null;
 		return result;
 	}
 
 	@Override
 	public ModelChange endMove(Point2D p) {
 		throw new UnsupportedOperationException("Transition selectors do not support move operation.");
 	}
 
 	@Override
 	public boolean isDragging() {
		return (original != null);
 	}
 	
 	@Override
 	public boolean isMoving() {
 		return false;
 	}
 	
 	@Override
 	public void move(Point2D p) {
 		throw new UnsupportedOperationException("Transition selectors do not support move operation.");
 	}
 
 	@Override
 	public boolean objectContains(Point2D p) {
 		return getTarget().contains(p, getTransformation());
 	}
 
 	@Override
 	public boolean startDrag(MouseEvent e) {
 		if (isDragging()) {
 			throw new IllegalStateException("Cannot initiate dragging when already dragging.");
 		}
 		if (contains(getPoint(e))) {
			original = getTarget().clone();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean startMove(Point2D p) {
 		return false;
 	}
 
 	@Override
 	public void refreshStatusBar(StatusBar target) {
 		target.setSelectedTransition(getTarget());
 	}
 }
