 //Copyright (C) 2011 Tomáš Vejpustek
 //Full copyright notice found in src/LICENSE.  
 package selector;
 
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 import ltl.DeleteEvent;
 import ltl.Event;
 import ltl.ModelChange;
 import ltl.ModifyEvent;
 import ltl.Property;
 import ltl.Property.Bound;
 import ui.Canvas;
 import ui.StatusBar;
 import coordinates.Transformation;
 
 /**
  * Selector of an {@link Event}. Implements its moving. Has natural movement
  * boundaries from left and right.
  * 
  * @author Tomáš Vejpustek
  */
 public abstract class EventSelector extends AbstractSelector {
 	private double leftBound, rightBound;
 	private Event target;
	private Event original = null;
 	private Point2D origin = null;
 	private DerivativeSelector derivative;
 	private boolean dragging = false;
 
 	/**
 	 * @param coord
 	 *            Coordinate transformation.
 	 * @param left
 	 *            Next {@link Event} to the left.
 	 * @param right
 	 *            Next {@link Event} to the right.
 	 * @return Appropriate selector for <code>target</code>.
 	 */
 	public static EventSelector get(Transformation coord, Event target,
 			Event left, Event right) {
 		if (target.getTime().isPoint() || !target.getTime().isSet()) {
 			if (target.getConcentration().isPoint()
 					|| !target.getConcentration().isSet()) {
 				return new PointEventSelector(coord, target, left, right);
 			} else {
 				return new LineEventSelector(coord, target, left, right, false);
 			}
 		} else {
 			if (target.getConcentration().isPoint()
 					|| !target.getConcentration().isSet()) {
 				return new LineEventSelector(coord, target, left, right, true);
 			} else {
 				return new BoxEventSelector(coord, target, left, right);
 			}
 		}
 	}
 
 	/**
 	 * Initializes contained {@link Event} and {@link Transformation}; sets
 	 * movement boundaries.
 	 * @param left left movement boundary
 	 * @param right right movement boundary
 	 */
 	protected EventSelector(Transformation coord, Event target, Event left,
 			Event right) {
 		super(coord);
 		if (left != null) {
 			leftBound = left.getTime().getCenter();
 		} else {
 			leftBound = 0;
 		}
 		if (right != null) {
 			rightBound = right.getTime().getCenter();
 		} else {
 			rightBound = getTransformation().getTimeBound();
 		}
 		this.target = target.clone();
 		original = target;
 		derivative = DerivativeSelector.get(coord, target.getDerivative(), new Point2D.Double(target.getTime().getCenter(), target.getConcentration().getCenter()));
 	}
 
 	/**
 	 * @return Selected {@link Event}.
 	 */
 	protected Event getTarget() {
 		return target;
 	}
 
 	/**
 	 * Continues dragging when the origin is a point.
 	 */
 	protected void dragPoint(MouseEvent e) {
 		if (selectorBounds(getTarget().getTime().getCenter(),
 				getTarget().getConcentration().getCenter()).contains(e.getX(),
 				e.getY())) {
 			if (original != null) {
 				if (original.getTime().isSet()) {
 					getTarget().getTime().makePoint();
 				} else {
 					getTarget().getTime().unset();
 				}
 				if (original.getConcentration().isSet()) {
 					getTarget().getConcentration().makePoint();
 				} else {
 					getTarget().getConcentration().unset();
 				}
 			} else {
 				getTarget().getTime().makePoint();
 				getTarget().getConcentration().makePoint();
 			}
 		} else {
 			Point2D p = getModelCoordinates(e);
 			double dTime = p.getX() - getTarget().getTime().getCenter();
 			double dConc = p.getY()
 					- getTarget().getConcentration().getCenter();
 			Bound bTime = ((dTime > 0) ? Bound.UPPER : Bound.LOWER);
 			Bound bConc = ((dConc > 0) ? Bound.UPPER : Bound.LOWER);
 
 			if (e.isControlDown()) { // vertical or horizontal type
 				if (Math.abs(dTime) > Math.abs(dConc)) {
 					getTarget().getConcentration().makePoint();
 					bConc = null;
 				} else {
 					getTarget().getTime().makePoint();
 					bTime = null;
 				}
 			}
 
 			if (e.isShiftDown()) {
 				if (bTime != null) {
 					double val = getTarget().getTime().getCenter() - dTime;
 					if ((val <= 0)
 							|| (val > getTransformation().getSize().getX())) {
 						getTarget().getTime().unbind(bTime.other());
 					} else {
 						getTarget().getTime().stretchBound(bTime.other(), val);
 					}
 				}
 				if (bConc != null) {
 					double val = getTarget().getConcentration().getCenter()
 							- dConc;
 					if ((val <= 0)
 							|| (val > getTransformation().getSize().getY())) {
 						getTarget().getConcentration().unbind(bConc.other());
 					} else {
 						getTarget().getConcentration().stretchBound(
 								bConc.other(), val);
 					}
 				}
 			} else {
 				if (bTime != null) {
 					getTarget().getTime().stretchBound(bTime.other(),
 							getTarget().getTime().getCenter());
 				}
 				if (bConc != null) {
 					getTarget().getConcentration().stretchBound(bConc.other(),
 							getTarget().getConcentration().getCenter());
 				}
 			}
 
 			if (bTime != null) {
 				if ((p.getX() <= 0)
 						|| (p.getX() > getTransformation().getSize().getX())) {
 					getTarget().getTime().unbind(bTime);
 				} else {
 					getTarget().getTime().stretchBound(bTime, p.getX());
 				}
 			}
 			if (bConc != null) {
 				if ((p.getY() <= 0)
 						|| (p.getY() > getTransformation().getSize().getY())) {
 					getTarget().getConcentration().unbind(bConc);
 				} else {
 					getTarget().getConcentration()
 							.stretchBound(bConc, p.getY());
 				}
 			}
 		}
 	}
 
 	/**
 	 * Drags an edge of Event -- i.e. changes only one property.
 	 * 
 	 * @param prop
 	 *            Property to be dragged (<code>getEvent.getTime()</code> or
 	 *            <code>getEvent().getConcentration()</code>).
 	 * @param bound
 	 *            Bound (one of two edges of the property) to be dragged.
 	 * @param val
 	 *            Value to which should the edge be dragged (in model
 	 *            coordinates; <code>getTransformation.getTime(e.getX())</code>
 	 *            or <code>getTransformation().getConcentration(e.getY())</code>
 	 *            where <code>e</code> is a {@link MouseEvent}).
 	 * @param maxVal
 	 *            The maximum of value to which can be the edge dragged (in
 	 *            model coordinates, minimum is 0;
 	 *            <code>getTransformation().getTime(getTransfromation().getSize().getX())</code>
 	 *            or <code>getTransformation().getConcentration(0)</code>).
 	 * @param isShiftDown
 	 *            <code>true</code> when shift is pressed, <code>false</code>
 	 *            otherwise (<code>e.isShiftDown()</code> where <code>e</code>
 	 *            is a {@link MouseEvent}).
 	 */
 	protected void dragEdge(Property prop, Bound bound, double val,
 			double maxVal, boolean isShiftDown) {
 		if (isShiftDown) {
 			double ref = prop.getCenter();
 			double d = val - ref;
 			if ((d < 0 && bound.equals(Bound.UPPER))
 					|| (d > 0 && bound.equals(Bound.LOWER))) {
 				prop.makePoint();
 			} else {
 				if (((val > maxVal) && bound.equals(Bound.UPPER))
 						|| ((val <= 0) && bound.equals(Bound.LOWER))) {
 					prop.unbind(bound);
 				} else {
 					prop.stretchBound(bound, val);
 				}
 				if ((((ref - d) > maxVal) && bound.other().equals(Bound.UPPER))
 						|| (((ref - d) <= 0) && bound.other().equals(
 								Bound.LOWER))) {
 					prop.unbind(bound.other());
 				} else {
 					prop.stretchBound(bound.other(), ref - d);
 				}
 			}
 		} else {
 			if (!(bound.equals(Bound.UPPER) && val < prop.getCenter())
 					&& !(bound.equals(Bound.LOWER) && val > prop.getCenter())) {
 				if ((bound.equals(Bound.UPPER) && val > maxVal)
 						|| (bound.equals(Bound.LOWER) && val < 0)) {
 					prop.unbind(bound);
 				} else {
 					prop.stretchBound(bound, val);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return Included derivative selector.
 	 */
 	protected DerivativeSelector getDerivativeSelector() {
 		return derivative;
 	}
 	
 	@Override
 	public void draw(Canvas c) {
 		c.drawAnchors(getTarget());
 		c.drawEvent(getTarget());
 	}
 
 	@Override
 	public boolean objectContains(Point2D p) {
 		return getTarget().contains(p, getTransformation());
 	}
 
 	@Override
 	public ModelChange endMove(Point2D p) {
 		move(p);
 		ModelChange result = new ModifyEvent(original, getTarget());
 		origin = null;
 		original = null;
 		return result;
 	}
 
 	@Override
 	public boolean isMoving() {
 		return (origin != null);
 	}
 
 	@Override
 	public void move(Point2D p) {
 		if (!isMoving()) {
 			throw new IllegalStateException("Cannot continue unstarted moving.");
 		}
 		double time = getModelCoordinates(p).getX() - origin.getX()
 				+ original.getTime().getCenter();
 		double conc = getModelCoordinates(p).getY() - origin.getY()
 				+ original.getConcentration().getCenter();
 		if (time > leftBound && time < rightBound) {
 			getTarget().getTime().move(time);
 		}
 		if (conc > 0 && conc < getTransformation().getConcentrationBound()) {
 			getTarget().getConcentration().move(conc);
 		}
 	}
 
 	@Override
 	public boolean startMove(Point2D p) {
 		if (isMoving() || isDragging()) {
 			throw new IllegalStateException(
 					"Cannot start moving when already moving or dragging.");
 		}
 		if (objectContains(p)) {
 			origin = getModelCoordinates(p);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public ModelChange endDrag(MouseEvent e) {
 		drag(e);
 		getTarget().getConcentration().refreshReference();
 		ModelChange result = new ModifyEvent(original, getTarget());
 		original = null;
 		dragging = false;
 		return result;
 	}
 
 	@Override
 	public boolean isDragging() {
 		return dragging;
 	}
 
 	@Override
 	public boolean startDrag(MouseEvent e) {
 		if (isMoving() || isDragging()) {
 			throw new IllegalStateException(
 					"Cannot start dragging when already moving or dragging.");
 		}
 		if (contains(getPoint(e))) {
 			dragging = true;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public ModelChange delete() {
 		if (isMoving() || isDragging()) {
 			throw new IllegalStateException(
 					"Cannot delete Event that is being moved or dragged.");
 		}
 		return new DeleteEvent(getTarget());
 	}
 	
 	@Override
 	public void refreshStatusBar(StatusBar target) {
 		target.setSelectedEvent(getTarget());
 	}
 	
 }
