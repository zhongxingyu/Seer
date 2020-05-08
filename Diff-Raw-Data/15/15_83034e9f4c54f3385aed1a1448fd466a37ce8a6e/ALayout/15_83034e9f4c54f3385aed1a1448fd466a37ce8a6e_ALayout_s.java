 package com.eagerlogic.cubee.client.components;
 
 import com.google.gwt.dom.client.Element;
 
 /**
  *
  * @author dipacs
  */
 public abstract class ALayout extends AComponent {
 
 	private final LayoutChildren children = new LayoutChildren(this);
 
 	public ALayout(Element element) {
 		super(element);
 	}
 
 	protected LayoutChildren getChildren() {
 		return children;
 	}
 
 	protected abstract void onChildAdded(AComponent child);
 
 	protected abstract void onChildRemoved(AComponent child, int index);
 
 	protected abstract void onChildrenCleared();
 
 	@Override
 	public final void layout() {
 		this.needsLayout = false;
 		for (AComponent child : getChildren()) {
 			if (child != null) {
 				if (child.isNeedsLayout()) {
 					child.layout();
 				}
 			}
 		}
 		this.onLayout();
 		this.measure();
 	}
 
 	@Override
 	final boolean doPointerEventClimbingUp(int screenX, int screenY, int x, int y, int wheelVelocity,
 			boolean altPressed, boolean ctrlPressed, boolean shiftPressed, boolean metaPressed, int type) {
 		if (!handlePointerProperty().get()) {
 			return false;
 		}
 		if (!enabledProperty().get()) {
 			return true;
 		}
 		if (!visibleProperty().get()) {
 			return false;
 		}
 		if (onPointerEventClimbingUp(screenX, screenY, x, y, wheelVelocity, altPressed,
 				ctrlPressed, shiftPressed, metaPressed, type)) {
 			for (int i = getChildren().size() - 1; i >= 0; i--) {
 				AComponent child = getChildren().get(i);
 				if (child != null) {
					if (child.isIntersectsPoint(x, y)) {
						int childX = x - child.getLeft() - child.translateXProperty().get();
						int childY = y - child.getTop() - child.translateYProperty().get();
 						// TODO rotate and scale child point
 						if (child.doPointerEventClimbingUp(screenX, screenY, childX, childY, wheelVelocity,
 								altPressed, ctrlPressed, shiftPressed, metaPressed, type)) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		if (pointerTransparentProperty().get()) {
 			return false;
 		} else {
 			return onPointerEventFallingDown(screenX, screenY, x, y, wheelVelocity, altPressed,
 					ctrlPressed, shiftPressed, metaPressed, type);
 		}
 		
 	}
 
 	/**
 	 * Called by the layout after the children of this layout are measured but before this layout measured. You need to
 	 * set the size of this layout if needed and set the positions of the children if needed.
 	 */
 	protected abstract void onLayout();
 }
