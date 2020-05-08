 package ch.sebastienzurfluh.client.view.navigation.animation;
 
 import ch.sebastienzurfluh.client.view.navigation.NavigationSlider;
 
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
 	
 	/**
 	 * You need a different object per animated panel.
 	 * Only one child can be animated this way.
 	 * If the size of the panel changes you have to recreate this widget.
 	 *
 	 * @param animatedPanel parent of the moving widget
 	 * @param movingWidget itself
 	 */
 	public SwipeScroller(AbsolutePanel animatedPanel, Widget movingWidget, NavigationSlider slider) {
 		this.animatedPanel = animatedPanel;
 		this.movingWidget = movingWidget;
 		this.slider = slider;
     }
 	
 	/**
 	 * Updates settings that do not change often.
 	 * 
 	 * Use update in the following cases:
 	 * - screen width changed
 	 * - widget count changed
 	 * - widget size changed
 	 */
 	public void update() {
 		itemCount = slider.getItemCount();
 		itemWidth = slider.getItem(0).getOffsetWidth();
 		widgetWidth = slider.getOffsetWidth();
 	}
 	/**
 	 * The count of menu items.
 	 */
 	private int itemCount = 0;
 	/**
 	 * Pixel size of a benu item.
 	 */
 	private int itemWidth = 0;
 	private int widgetWidth = 0;
 	
 	/**
 	 * Move the scroller to the specified widget
 	 * @param number -th widget
 	 */
 	public void setFocusWidget(int number) {
 		slider.getItem(slider.getCurrentItemNumber()).setFocus(false);
 
 		slider.setCurrentItem(number);
 		int itemPosition = number * itemWidth;
 		scrollTo(itemPosition, SLOW);
 		
 		slider.getItem(number).setFocus(true);
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
 		
 		int previousItem = slider.getCurrentItemNumber();
 
 		// get the widget back into boundaries.
 		int leftDelta = animatedPanel.getAbsoluteLeft() - movingWidget.getAbsoluteLeft();
     	if (leftDelta < 0) {
     		scrollTo(0, SLOW);
     		slider.setCurrentItem(0);
    	} else if (leftDelta > widgetWidth) {
    		slider.setCurrentItem(itemCount-1);
     	} else {
     		int delta = newXPos - startPosition;
     		// movement has to be more than a third of the screen
     		if (delta > widgetWidth * 0.3) {
     			slider.setCurrentItem(slider.getCurrentItemNumber()-1);
     		} else if (delta < - widgetWidth * 0.3) {
     			slider.setCurrentItem(slider.getCurrentItemNumber()+1);
     		} else {
     			// re-focus the same widget
     		}
     	}
     	
     	
 		// move to the right place
 		setFocusWidget(slider.getCurrentItemNumber());
 		
 		if (previousItem != slider.getCurrentItemNumber())
     		slider.getItem(slider.getCurrentItemNumber()).ignite();
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
