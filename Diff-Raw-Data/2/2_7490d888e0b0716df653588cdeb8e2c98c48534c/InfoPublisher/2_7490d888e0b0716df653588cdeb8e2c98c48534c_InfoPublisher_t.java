 package org.bodytrack.client;
 
 /**
  * Publishes information to the page outside of the Grapher
  * widget.  This information is available outside of GWT and
  * offers a consistent interface to the rest of the page,
  * regardless of how GWT compiles this widget.
  *
  * <p>Note that race conditions are possible, since this modifies the
  * global state, but race conditions are not a problem with a single
  * thread.  Since JavaScript is single-threaded (notwithstanding the
  * very limited threading model provided by Web Workers), there is
  * no harm in guarantees that only hold for a single-threaded
  * program.</p>
  *
  * <p>This class deals with the public API between the GWT application
  * and the rest of the webpage.  This is through the window.grapherState
  * function, which returns the current view.</p>
  */
 public final class InfoPublisher {
 	private static InfoPublisher instance = null;
 
 	// Don't make constructors for this class available to the
 	// rest of the widget
 	private InfoPublisher(ViewSwitchWidget widget) {
 		initialize(widget);
 	}
 
 	/**
 	 * Used by the constructor to initialize the
 	 * window.grapherState global variable.
 	 */
 	private native void initialize(ViewSwitchWidget widget) /*-{
 		$wnd.grapherState = function() {
 			// In Java-like syntax:
 			// return widget.getCurrentSavableView();
			return widget.@org.bodytrack.client.ViewSwitchWidget::getCurrentSavableView()();
 		};
 	}-*/;
 
 	/**
 	 * Sets the widget that keeps track of the current view name.
 	 *
 	 * @param widget
 	 * 		the {@link ViewSwitchWidget} that keeps track of the current
 	 * 		view name
 	 */
 	public static void setWidget(ViewSwitchWidget widget) {
 		instance = new InfoPublisher(widget);
 	}
 
 	/**
 	 * Returns an InfoPublisher instance.
 	 *
 	 * <p>If the {@link #setWidget(ViewSwitchWidget)} method has not yet
 	 * been called, returns <tt>null</tt>.</p>
 	 *
 	 * @param widget
 	 * 		the widget to use to generate the current savable view
 	 * @return
 	 * 		an InfoPublisher to be used by this widget
 	 */
 	public static InfoPublisher getInstance() {
 		return instance;
 	}
 }
