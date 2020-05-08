 package org.bodytrack.client;
 
 /**
  * Publishes information to the page outside of the Grapher
  * widget.  This information is available outside of GWT and
  * offers a consistent interface to the rest of the page,
  * regardless of how GWT compiles this widget.
  *
  * <p>This class contains several methods for publishing
  * information to the rest of the page.  They would be
  * static, except that some initialization must be done
  * for the first call into this interface.  As such, this
  * class does not rely on static methods, but is
  * instance-controlled.  There is never more than one instance
  * of this class available at any time.</p>
  *
  * <p>Note that race conditions are possible, since this modifies the
  * global state, but race conditions are not a problem with a single
  * thread.  Since JavaScript is single-threaded (notwithstanding the
  * very limited threading model provided by Web Workers), there is
  * no harm in guarantees that only hold for a single-threaded
  * program.</p>
  *
  * <p>This class deals with the public API between the GWT application
  * and the rest of the webpage.  This is through the
  * window.grapherState dictionary, which contains several predefined
  * keys for the interface.  See the {@link #initialize()} method to
  * see the dictionary keys that are available for the rest of the
  * page.</p>
  *
  * <p>There are several informative parts of the API, and one part by
  * which the rest of the page can request to be notified on changes.
  * The window.grapherState[&quot;change_listeners&quot;] part of the
  * API is meant to be an array of no-argument functions.  Outside
  * JavScript may modify this array at will.  Whenever a change is made
  * to the informative part of the API, and actually when any non-static
  * method of this class is called from GWT, all these functions are
  * called in JavaScript.  Thus, these functions may be called when the
  * actual information does not change, so they are responsible for
  * checking the pertinent information in the window.grapherState
  * variable.</p>
  */
 public final class InfoPublisher {
 	private static final InfoPublisher INSTANCE = new InfoPublisher();
 
 	// Don't make constructors for this class available to the
 	// rest of the widget
 	private InfoPublisher() {
 		initialize();
 	}
 
 	/**
 	 * Used by the constructor to initialize the
 	 * window.grapherState global variable.
 	 */
 	private native void initialize() /*-{
 		$wnd.grapherState = {};
 		$wnd.grapherState['change_listeners'] = [];
 		$wnd.grapherState['x_axis'] = {};
 		$wnd.grapherState['y_axis'] = {};
 		$wnd.grapherState['channel_colors'] = {};
 	}-*/;
 
 	/**
 	 * Returns an InfoPublisher instance.
 	 *
 	 * @return
 	 * 		an InfoPublisher to be used by this widget
 	 */
 	public static InfoPublisher getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Publishes the min/max values for the X-axis.
 	 *
 	 * @param min
 	 * 		the current min value for the X-axis
 	 * @param max
 	 * 		the current max value for the X-axis
 	 */
 	public native void publishXAxisBounds(double min, double max) /*-{
 		$wnd.grapherState['x_axis']['min'] = min;
 		$wnd.grapherState['x_axis']['max'] = max;
 
 		var len = $wnd.grapherState['change_listeners'].length;
 		for (var i = 0; i < len; i++) {
 			$wnd.grapherState['change_listeners'][i]();
 		}
 	}-*/;
 
 	/**
 	 * Publishes the min/max values for the Y-axis.
 	 *
 	 * @param channelName
 	 * 		the name of the channel with which this Y-axis is paired
 	 * @param min
 	 * 		the current min value for the Y-axis
 	 * @param max
 	 * 		the current max value for the Y-axis
 	 */
 	public native void publishYAxisBounds(String channelName, double min,
 			double max) /*-{
		if (! channelName in $wnd.grapherState['y_axis'])
 			$wnd.grapherState['y_axis'][channelName] = {};
 
 		$wnd.grapherState['y_axis'][channelName]['min'] = min;
 		$wnd.grapherState['y_axis'][channelName]['max'] = max;
 
 		var len = $wnd.grapherState['change_listeners'].length;
 		for (var i = 0; i < len; i++) {
 			$wnd.grapherState['change_listeners'][i]();
 		}
 	}-*/;
 
 	/*
 	 * TODO: Add this, or something similar
 	 *
 	 * Manipulate a $wnd.grapherState['plot_type'] dictionary, perhaps
 	 *
 	 * Could even use an InfoPublisher.PlotType enum, giving nice
 	 *   properties in Java, with only a little glue code needed to
 	 *   convert to JavaScript values and publish to the page
 	 *
 	 * Perhaps expect that Zeo plots are published with the special
 	 * color &quot;ZEO&quot;, which will alert any outside scripts to
 	 * the type of channel.
 	 *
 	 * Now, though, Zeo plots are published with the color
 	 * Grapher2.ZEO_COLOR_STRING, which is the empty string
 
 	public native void publishPlotType(String channelName, int plotType);
 	*/
 
 	/**
 	 * Publishes the color for a channel.
 	 *
 	 * @param channelName
 	 * 		the name of the channel
 	 * @param color
 	 * 		the color of the data plot with the specified channel name
 	 */
 	public native void publishChannelColor(String channelName,
 			String color) /*-{
 		$wnd.grapherState['channel_colors'][channelName] = color;
 
 		var len = $wnd.grapherState['change_listeners'].length;
 		for (var i = 0; i < len; i++) {
 			$wnd.grapherState['change_listeners'][i]();
 		}
 	}-*/;
 }
