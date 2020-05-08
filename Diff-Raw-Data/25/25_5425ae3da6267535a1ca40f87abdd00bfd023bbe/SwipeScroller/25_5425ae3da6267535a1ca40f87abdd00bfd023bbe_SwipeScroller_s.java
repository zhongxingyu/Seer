 package ch.sebastienzurfluh.swissmuseum.parcours.client.view.navigation.animation;
 
 import ch.sebastienzurfluh.swissmuseum.core.client.control.eventbus.EventBus;
 import ch.sebastienzurfluh.swissmuseum.core.client.control.eventbus.events.PageChangeRequest;
 import ch.sebastienzurfluh.swissmuseum.core.client.model.structure.DataReference;
 import ch.sebastienzurfluh.swissmuseum.parcours.client.view.navigation.NavigationSlider;
 
 import com.google.gwt.animation.client.Animation;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.TouchEndEvent;
 import com.google.gwt.event.dom.client.TouchMoveEvent;
 import com.google.gwt.event.dom.client.TouchStartEvent;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Animation for the scrolling effects.
  * Create a new object for each animated panel.
  *
  * @author Sebastien Zurfluh
  *
  */
 public class SwipeScroller extends Animation  implements NavigationAnimator {
 	private static final int SLOW = 400; //ms
 	private static final int QUICK = 40; //ms
 	private AbsolutePanel animatedPanel;
 	private Widget movingWidget;
 	private NavigationSlider slider;
 	private EventBus pageRequestBus;
 	
 	/**
 	 * You need a different object per animated panel.
 	 * Only one child can be animated this way.
 	 * If the size of the panel changes you have to recreate this widget.
 	 *
 	 * @param animatedPanel parent of the moving widget
 	 * @param movingWidget itself
 	 */
 	public SwipeScroller(
 			AbsolutePanel animatedPanel, 
 			Widget movingWidget,
 			NavigationSlider slider,
 			EventBus pageRequestBus) {
 		this.animatedPanel = animatedPanel;
 		this.movingWidget = movingWidget;
 		this.slider = slider;
 		this.pageRequestBus = pageRequestBus;
     }
 	
 	/**
 	 * Move the scroller to the specified widget with an animation.
 	 * @param rank -th widget starting at 1
 	 */
 	public void setFocusWidget(int rank) {
 		setFocusWidget(rank, true);
 	}
 	
 	/**
 	 * Move the scroller to the specified
 	 * @param rank -th widget starting at 1
 	 * @param animate is true, if you want an animation or not
 	 */
 	public void setFocusWidget(int rank, boolean animate) {
 		slider.setCurrentItem(rank);
 		int itemPosition = (rank-1) * slider.getWidgetWidth();
 		System.out.println("SwipeScroller: New calculated position: " + itemPosition +
 				" with rank " + rank);
 		if(animate) {
 			scrollTo(itemPosition, SLOW);
 		} else {
 			scrollTo(itemPosition, 0);
 		}
 	}
 
 	int positionBeforeAnimation, delta;
     private void scroll(int delta, int duration) {
     	positionBeforeAnimation = movingWidget.getAbsoluteLeft() - animatedPanel.getAbsoluteLeft();
     	this.delta = delta;
     	
     	run(duration);
     }
     
     /**
      * @param newPos -ition absolute
      * @param duration in ms
      */
     private void scrollTo(int newPos, int duration) {
     	
     	positionBeforeAnimation = movingWidget.getAbsoluteLeft() - animatedPanel.getAbsoluteLeft();
     	// we scroll the opposite way of the direction we want
     	// in order to bring the widget to us.
     	this.delta = - newPos - positionBeforeAnimation;
     	
     	run(duration);
     }
 
     @Override
     protected void onUpdate(double progress) {
         int position = (int) (positionBeforeAnimation + (progress * delta));
         animatedPanel.setWidgetPosition(movingWidget, position, 0);
     }
     
     @Override
     protected void onComplete() {
     	animatedPanel.setWidgetPosition(movingWidget, positionBeforeAnimation + delta, 0);
     };
     
     
 	private HandlerRegistration mouseMoveHandler;
 	private HandlerRegistration touchMoveHandler;
 	
 	private int startPosition;
 	
 	/**
 	 * Position relative to the last update
 	 */
 	private int movingRelativePosition;
 	private void onStart(int xPosition) {
 		// adding and removing the handlers is the best solution to
 		// avoid unwanted move events.
 		mouseMoveHandler = slider.addMouseMoveHandler(this);
		touchMoveHandler = slider.addTouchMoveHandler(this);
 		
 		startPosition = xPosition;
 		movingRelativePosition = startPosition;
 	}
 	
 	/**
      * @param newXPos -ition absolute
      */
 	private void onMove(int newXPosition) {
 		scroll(- movingRelativePosition + newXPosition, QUICK);
 		movingRelativePosition = newXPosition;
 	}
 
 	/**
      * @param newXPos -ition absolute
      */
 	private void onEnd(int newXPos) {
 		// see {@code onStart}
 		mouseMoveHandler.removeHandler();
		touchMoveHandler.removeHandler();
 		
 		int previousItem = slider.getCurrentItemRank();
 		int nextItem;
     	
     	
     	// movement has to be more than a third of the widget
 		int delta = newXPos - startPosition;
     	if (delta > slider.getWidgetWidth() / 3) {
     		nextItem = slider.getCurrentItemRank()-1;
     	} else if (delta < - slider.getWidgetWidth() / 3) {
     		nextItem = slider.getCurrentItemRank()+1;
     	} else {
     		nextItem = previousItem;
     	}
     	
     	// get the widget back into boundaries.
     	boolean outOfBoundaries = false;
 		int leftDelta = animatedPanel.getAbsoluteLeft() - movingWidget.getAbsoluteLeft();
     	if (leftDelta < 0 || leftDelta > slider.getWidgetWidth()) {
     		outOfBoundaries = true;
     	}
     	
     	if (nextItem != previousItem) {
     		if(outOfBoundaries) {
 	    		pageRequestBus.fireEvent(
 	    				new PageChangeRequest(
 	    						DataReference.SUPER));
 	    	} else {
 	    		// move to the right place
 		    	setFocusWidget(nextItem);
 		
 	    		if (previousItem != nextItem)
 	    			pageRequestBus.fireEvent(
 	    					new PageChangeRequest(
 	    							slider.getItem(nextItem).getReference()));
 	    	}
     	}
 	}
 	
 
 	@Override
 	public void onTouchStart(TouchStartEvent event) {
 		onStart(event.getChangedTouches().get(0).getRelativeX(event.getRelativeElement()));
 	}
 
 	@Override
 	public void onTouchEnd(TouchEndEvent event) {
 		onEnd(event.getChangedTouches().get(0).getRelativeX(event.getRelativeElement()));
 	}
 
 	@Override
 	public void onTouchMove(TouchMoveEvent event) {
 		onMove(event.getChangedTouches().get(0).getRelativeX(event.getRelativeElement()));
 	}
 
 	@Override
 	public void onMouseMove(MouseMoveEvent event) {
 		onMove(event.getRelativeX(event.getRelativeElement()));
 		
 	}
 
 	@Override
 	public void onMouseUp(MouseUpEvent event) {
 		onEnd(event.getRelativeX(event.getRelativeElement()));
 	}
 
 	@Override
 	public void onMouseDown(MouseDownEvent event) {
 		onStart(event.getRelativeX(event.getRelativeElement()));
 	}
 }
