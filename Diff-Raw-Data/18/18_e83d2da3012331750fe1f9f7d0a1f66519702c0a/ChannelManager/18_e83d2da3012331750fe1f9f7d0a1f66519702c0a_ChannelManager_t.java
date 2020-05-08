 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 /**
  * A global channel management class, which keeps track of which
  * channels are showing and which are not.
  *
  * <p>Objects of this class are mutable, and no internal synchronization
  * is implemented, so external synchronization is required to make
  * objects of this class thread-safe.</p>
  */
 public class ChannelManager {
 	// Need to go through wrapper every time we draw, in case the
 	// user switches a plot style.  That's why we can't use the Java object
 	// every time
 	private final List<JavaScriptObject> dataPlots;
 	private List<JavaScriptObject> unmodDataPlots;
 
 	/*
 	 * xAxisMap and yAxisMap provide the reverse mapping from
 	 * dataPlots.get(i).getXAxis() and dataPlots.get(i).getYAxis().
 	 * They map from axes to sets of data plots associated with
 	 * those axes.
 	 *
 	 * An invariant is that each element of xAxisMap and yAxisMap
 	 * has at least one plot in dataPlots referencing it.
 	 */
 	private final Map<GraphAxis, List<JavaScriptObject>> xAxisMap;
 	private Map<GraphAxis, List<JavaScriptObject>> unmodXAxisMap;
 	private Set<GraphAxis> unmodXAxes;
 
 	private final Map<GraphAxis, List<JavaScriptObject>> yAxisMap;
 	private Map<GraphAxis, List<JavaScriptObject>> unmodYAxisMap;
 	private Set<GraphAxis> unmodYAxes;
 
 	// There is some redundancy provided by this variable, but it
 	// does increase efficiency when dealing with channel names
 	private final Map<StringPair, JavaScriptObject> channelMap;
 	private Map<StringPair, JavaScriptObject> unmodChannelMap;
 	private Set<StringPair> unmodChannels;
 
 	private final List<ChannelChangedListener> listeners;
 
 	/**
 	 * Creates a new <tt>ChannelManager</tt> that does not include
 	 * any channels.
 	 */
 	public ChannelManager() {
 		dataPlots = new ArrayList<JavaScriptObject>();
 		xAxisMap = new HashMap<GraphAxis, List<JavaScriptObject>>();
 		yAxisMap = new HashMap<GraphAxis, List<JavaScriptObject>>();
 		channelMap = new HashMap<StringPair, JavaScriptObject>();
 		listeners = new ArrayList<ChannelChangedListener>();
 
 		refreshUnmodifiableCaches();
 	}
 
 	/**
 	 * Drops and then recalculates the caches of unmodifiable copies
 	 * of the private variables.
 	 */
 	private void refreshUnmodifiableCaches() {
 		unmodDataPlots = Collections.unmodifiableList(dataPlots);
 
 		unmodXAxisMap = Collections.unmodifiableMap(xAxisMap);
 		unmodXAxes = Collections.unmodifiableSet(xAxisMap.keySet());
 
 		unmodYAxisMap = Collections.unmodifiableMap(yAxisMap);
 		unmodYAxes = Collections.unmodifiableSet(yAxisMap.keySet());
 
 		unmodChannelMap = Collections.unmodifiableMap(channelMap);
 		unmodChannels = Collections.unmodifiableSet(channelMap.keySet());
 	}
 
 	/**
 	 * Returns an unmodifiable view of the list of
 	 * the data plots held by this <tt>ChannelManager</tt>.
 	 *
 	 * @return
 	 * 		an unmodifiable view of the list of data plots this holds
 	 */
 	public List<JavaScriptObject> getDataPlots() {
 		return unmodDataPlots;
 	}
 
 	/**
 	 * Returns an unmodifiable view of the map from channel names
 	 * back to the plots that actually make those channels
 	 * available to the user.
 	 *
 	 * @return
 	 * 		a map from channel names to plots that graph those
 	 * 		channels
 	 */
 	public Map<StringPair, JavaScriptObject> getChannelMap() {
 		return unmodChannelMap;
 	}
 
 	/**
 	 * Returns an unmodifiable view of the set of currently available
 	 * channel names.
 	 *
 	 * @return
 	 * 		an unmodifiable view of the set of names for all channels
 	 * 		currently held by this <tt>ChannelManager</tt>
 	 */
 	public Set<StringPair> getChannelNames() {
 		return unmodChannels;
 	}
 
 	/**
 	 * Returns an unmodifiable view of a map from X-axes back to their
 	 * associated {@link org.bodytrack.client.DataPlot DataPlot} objects.
 	 *
 	 * @return
 	 * 		an unmodifiable view of a map from X-axes back to their
 	 * 		associated {@link org.bodytrack.client.DataPlot DataPlot}
 	 * 		objects
 	 */
 	public Map<GraphAxis, List<JavaScriptObject>> getXAxisMap() {
 		return unmodXAxisMap;
 	}
 
 	/**
 	 * Returns an unmodifiable view of a map from Y-axes back to their
 	 * associated data plots.
 	 *
 	 * @return
 	 * 		an unmodifiable view of a map from Y-axes back to their
 	 * 		associated plots
 	 */
 	public Map<GraphAxis, List<JavaScriptObject>> getYAxisMap() {
 		return unmodYAxisMap;
 	}
 
 	/**
 	 * Returns an unmodifiable view of the set of X-axes held by
 	 * any data plot held by this <tt>ChannelManager</tt>.
 	 *
 	 * @return
 	 * 		an unmodifiable view of the set of X-axes on
 	 * 		data plots this holds
 	 */
 	public Set<GraphAxis> getXAxes() {
 		return unmodXAxes;
 	}
 
 	/**
 	 * Returns an unmodifiable view of the set of Y-axes held by
 	 * any data plot held by this <tt>ChannelManager</tt>.
 	 *
 	 * @return
 	 * 		an unmodifiable view of the set of Y-axes on data plots this holds
 	 */
 	public Set<GraphAxis> getYAxes() {
 		return unmodYAxes;
 	}
 
 	/**
 	 * Checks for inclusion of plot in the set of data plots this
 	 * <tt>ChannelManager</tt> maintains.
 	 *
 	 * @param nativePlot
 	 * 		the plot to check for
 	 * @return
 	 * 		<tt>false</tt> if nativePlotlot is <tt>null</tt>, and the
 	 * 		equivalent of {@code getDataPlots().contains(nativePlot)}
 	 * 		otherwise, comparing by IDs
 	 */
 	public boolean hasChannel(JavaScriptObject nativePlot) {
 		if (nativePlot == null)
 			return false;
 
 		Dynamic plot = nativePlot.cast();
		Integer id = plot.get("id");

      if (id != null) {
         for (JavaScriptObject test: dataPlots) {
            Dynamic testPlot = test.cast();
            Integer testID = testPlot.get("id");
            if (id.equals(testID))
               return true;
         }
      }
 
 		return false;
 	}
 
 	/**
 	 * Checks for whether the specified channel is contained in this
 	 * <tt>ChannelManager</tt>.
 	 *
 	 * @param deviceName
 	 * 		the name of the device for the channel to check
 	 * @param channelName
 	 * 		the name of the channel on the device
 	 * @return
 	 * 		<tt>true</tt> if and only if there is some plot with
 	 * 		the specified device and channel name in this
 	 * 		<tt>ChannelManager</tt>.  If deviceName or channelName
 	 * 		is <tt>null</tt>, always returns <tt>false</tt>
 	 */
 	public boolean hasChannel(String deviceName, String channelName) {
 		return deviceName != null && channelName != null
 			&& channelMap.containsKey(new StringPair(deviceName, channelName));
 	}
 
 	/**
 	 * Returns <tt>true</tt> if and only if axis is an X-axis or
 	 * Y-axis for some <tt>DataPlot</tt> object this holds.
 	 *
 	 * @param axis
 	 * 		the axis to check for membership
 	 * @return
 	 * 		<tt>true</tt> if and only if axis is not <tt>null</tt>,
 	 * 		and there is some <tt>DataPlot</tt> {@code d} in the
 	 * 		return value of {@code getDataPlots()} such that
 	 * 		{@code d.getXAxis().equals(axis)} or such that
 	 * 		{@code d.getYAxis().equals(axis)}
 	 */
 	public boolean hasAxis(GraphAxis axis) {
 		return axis != null
 			&& (xAxisMap.containsKey(axis) || yAxisMap.containsKey(axis));
 	}
 
 	/**
 	 * Returns the plot associated with the specified
 	 * (deviceName, channelName) pair, if such a channel is available.
 	 *
 	 * @param name
 	 * 		the name of the channel
 	 * @return
 	 * 		the plot associated with name, if possible, or
 	 * 		<tt>null</tt> if name is <tt>null</tt> or has not
 	 * 		been added through any channel
 	 */
 	public JavaScriptObject getChannel(StringPair name) {
 		if (name == null)
 			return null;
 
 		return channelMap.get(name);
 	}
 
 	/**
 	 * Adds the specified channel to the list of data plot objects
 	 * held by this <tt>ChannelManager</tt>, and updates axis references
 	 * as well.
 	 *
 	 * @param nativePlot
 	 * 		the data plot to add to the set of plots this hold
 	 * @throws NullPointerException
 	 * 		if plot is <tt>null</tt>
 	 */
 	public void addChannel(JavaScriptObject nativePlot) {
 		if (nativePlot == null)
 			throw new NullPointerException("Cannot add a null plot");
 
 		DataPlot plot = DataPlot.getDataPlot(nativePlot);
 		GraphAxis xAxis = plot.getXAxis();
 		GraphAxis yAxis = plot.getYAxis();
 
 		StringPair name =
 			new StringPair(plot.getDeviceName(), plot.getChannelName());
 		if (dataPlots.contains(nativePlot) || channelMap.containsKey(name))
 			return;
 
 		dataPlots.add(nativePlot);
 
 		// TODO: Check for bug if the same axis is both an X-axis
 		// and a Y-axis, which should never happen in reality
 
 		if (! xAxisMap.containsKey(xAxis)) {
 			List<JavaScriptObject> axisList =
 				new ArrayList<JavaScriptObject>();
 			axisList.add(nativePlot);
 			xAxisMap.put(xAxis, axisList);
 		} else
 			xAxisMap.get(xAxis).add(nativePlot);
 
 		if (! yAxisMap.containsKey(yAxis)) {
 			List<JavaScriptObject> axisList =
 				new ArrayList<JavaScriptObject>();
 			axisList.add(nativePlot);
 			yAxisMap.put(yAxis, axisList);
 		} else
 			yAxisMap.get(yAxis).add(nativePlot);
 
 		channelMap.put(name, nativePlot);
 
 		// Notify our event listeners to the occurrence of an event
 		for (ChannelChangedListener l: listeners)
 			l.channelAdded(plot.getDeviceName(), plot.getChannelName());
 
 		// Very important to refresh the cache after any mutation
 		refreshUnmodifiableCaches();
 	}
 
 	/**
 	 * Removes the specified channel from the list of plots
 	 * held by this <tt>ChannelManager</tt>, if it is part of that list,
 	 * and updates axis references as well.
 	 *
 	 * <p>If plot is <tt>null</tt> or has not yet been added using a call
 	 * to {@link #addChannel(JavaScriptObject)}, does absolutely nothing.</p>
 	 *
 	 * @param nativePlot
 	 * 		the plot to remove from the set of plots this holds
 	 */
 	public void removeChannel(JavaScriptObject nativePlot) {
 		if (nativePlot == null)
 			return;
 
 		DataPlot plot = DataPlot.getDataPlot(nativePlot);
 		GraphAxis xAxis = plot.getXAxis();
 		GraphAxis yAxis = plot.getYAxis();
 
 		if (! dataPlots.contains(nativePlot))
 			return;
 
 		dataPlots.remove(nativePlot);
 
 		if (xAxisMap.get(xAxis).size() > 1)
 			xAxisMap.get(xAxis).remove(nativePlot);
 		else
 			xAxisMap.remove(xAxis);
 
 		if (yAxisMap.get(yAxis).size() > 1)
 			yAxisMap.get(yAxis).remove(nativePlot);
 		else
 			yAxisMap.remove(yAxis);
 
 		channelMap.remove(new StringPair(
 				plot.getDeviceName(), plot.getChannelName()));
 
 		// Notify our event listeners to the occurrence of an event
 		for (ChannelChangedListener l: listeners)
 			l.channelRemoved(plot.getDeviceName(), plot.getChannelName());
 
 		// Very important to refresh the cache after any mutation
 		refreshUnmodifiableCaches();
 	}
 
 	/**
 	 * Removes the channel with the specified device and channel names,
 	 * if such a channel is part of this <tt>ChannelManager</tt>.
 	 *
 	 * @param deviceName
 	 * 		the device name for the channel to remove
 	 * @param channelName
 	 * 		the name for the channel to remove on the device
 	 */
 	public void removeChannel(String deviceName, String channelName) {
 		StringPair name = new StringPair(deviceName, channelName);
 
 		if (channelMap.containsKey(name))
 			removeChannel(channelMap.get(name));
 	}
 
 	/**
 	 * Adds a listener to the list of listeners to receive event
 	 * notifications whenever a channel is added, removed, etc.
 	 *
 	 * @param l
 	 * 		some {@link org.bodytrack.client.ChannelChangedListener
 	 * 		ChannelChangedListener} that should receive notifications
 	 * 		whenever an event occurs
 	 * @throws NullPointerException
 	 * 		if l is <tt>null</tt>
 	 */
 	public void addChannelListener(ChannelChangedListener l) {
 		if (l == null)
 			throw new NullPointerException(
 				"Can't fire events to null listener");
 
 		listeners.add(l);
 	}
 
 	/**
 	 * Clears the contents of this <tt>ChannelManager</tt>, except
 	 * for the listeners.
 	 *
 	 * <p>In other words, removes all channels from this
 	 * <tt>ChannelManager</tt>.  Additionally, the listeners are
 	 * notified about each change to the set of channels.</p>
 	 */
 	public void clear() {
 		while (dataPlots.size() > 0)
 			removeChannel(dataPlots.get(0));
 	}
 
 	/**
 	 * Replaces all the channels of this with the channels of other.
 	 *
 	 * <p>This is a thread-unsafe method - any call in multithreaded
 	 * code must be externally synchronized to prevent any concurrent
 	 * changes to this or to other.  Then again, JavaScript is
 	 * single-threaded, which stops this problem.</p>
 	 *
 	 * @param other
 	 * 		the <tt>ChannelManager</tt> whose channels to use
 	 * 		for this
 	 * @throws NullPointerException
 	 * 		if other is <tt>null</tt>
 	 */
 	public void replaceChannels(ChannelManager other) {
 		if (other == null)
 			throw new NullPointerException(
 				"Can't replace contents of this with a null set of channels");
 
 		clear();
 
 		for (JavaScriptObject nativePlot: other.getDataPlots())
 			addChannel(nativePlot);
 	}
 
 	/**
 	 * A simple class to hold an immutable pair of non-<tt>null</tt> strings.
 	 */
 	public static final class StringPair {
 		private String s1;
 		private String s2;
 
 		/**
 		 * Initializes a new <tt>StringPair</tt>.
 		 *
 		 * @param s1
 		 * 		the first string to hold
 		 * @param s2
 		 * 		the second string to hold
 		 */
 		public StringPair(String s1, String s2) {
 			if (s1 == null || s2 == null)
 				throw new NullPointerException(
 					"Cannot use null string with the StringPair class");
 
 			this.s1 = s1;
 			this.s2 = s2;
 		}
 
 		/**
 		 * Returns the first string this holds.
 		 *
 		 * @return
 		 * 		the value of s1 passed to the constructor when this
 		 * 		<tt>StringPair</tt> was created
 		 */
 		public String getFirst() {
 			return s1;
 		}
 
 		/**
 		 * Returns the second string this holds.
 		 *
 		 * @return
 		 * 		the value of s2 passed to the constructor when this
 		 * 		<tt>StringPair</tt> was created
 		 */
 		public String getSecond() {
 			return s2;
 		}
 
 		/**
 		 * Builds and returns a string representation of this object,
 		 * suitable for display to a user.
 		 *
 		 * <p>Never throws an exception or returns <tt>null</tt>.</p>
 		 */
 		public String toDisplayString() {
 			return s1 + "." + s2;
 		}
 
 		/**
 		 * Builds and returns a string representation of this object.
 		 * Never throws an exception or returns <tt>null</tt>.
 		 */
 		@Override
 		public String toString() {
 			return toDisplayString();
 		}
 
 		/**
 		 * @see java.lang.Object#hashCode()
 		 */
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + s1.hashCode();
 			result = prime * result + s2.hashCode();
 			return result;
 		}
 
 		/**
 		 * @see java.lang.Object#equals(java.lang.Object)
 		 */
 		@Override
 		public boolean equals(Object obj) {
 			if (this == obj)
 				return true;
 			if (obj == null)
 				return false;
 			if (! (obj instanceof StringPair))
 				return false;
 			StringPair other = (StringPair) obj;
 			// We know that s1 and s2 are never null
 			return s1.equals(other.s1) && s2.equals(other.s2);
 		}
 	}
 }
