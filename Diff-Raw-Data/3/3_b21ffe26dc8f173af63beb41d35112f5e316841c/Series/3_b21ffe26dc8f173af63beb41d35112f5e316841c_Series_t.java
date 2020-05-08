 /*
  * Copyright (c) 2013 Philippe VIENNE
  *
  * This file is a part of SpeleoGraph
  *
  * SpeleoGraph is free software: you can redistribute
  * it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * SpeleoGraph is distributed in the hope that it will
  * be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE.  See the GNU General Public License
  * for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with SpeleoGraph.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.cds06.speleograph.data;
 
 import org.apache.commons.lang3.Validate;
 import org.cds06.speleograph.GraphPanel;
 import org.cds06.speleograph.graph.DrawStyle;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.renderer.xy.HighLowRenderer;
 import org.jfree.chart.renderer.xy.XYAreaRenderer;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.DomainOrder;
 import org.jfree.data.general.DatasetChangeEvent;
 import org.jfree.data.general.DatasetChangeListener;
 import org.jfree.data.general.DatasetGroup;
 import org.jfree.data.time.DateRange;
 import org.jfree.data.xy.OHLCDataset;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.awt.*;
 import java.io.File;
 import java.util.*;
 import java.util.List;
 
 /**
  * Represent a Series of Data.
  * A series is coherent set of Data.
  */
 public class Series implements Comparable, OHLCDataset, Cloneable {
 
     /**
      * Logger for debug and errors in Series instances.
      */
     @SuppressWarnings("UnusedDeclaration")
     @NonNls
     private static final Logger log = LoggerFactory.getLogger(Series.class);
     private static GraphPanel graphPanel;
     private boolean stepped = false;
     private boolean minMax = false;
 
     /**
      * Create a new Series opened from a file with a default Type.
      *
      * @param origin The file where this series has been read.
      * @param type   The type for this series
      */
     public Series(@NotNull File origin, @NotNull Type type) {
         Validate.notNull(type, "Type can not be null");// NON-NLS
         Validate.notNull(origin);
         this.origin = origin;
         this.type = type;
         instances.add(this);
        setStyle(DrawStyle.AUTO);
         notifyListeners();
     }
 
     /**
      * Flag to define if we must show this series on chart.
      */
     private boolean show = false;
     /**
      * The file where this series has been read.
      */
     private File origin = null;
     /**
      * Series items, children of series.
      */
     private ArrayList<Item> items = new ArrayList<>();
     /**
      * The name of the series.
      */
     private String name;
     /**
      * Axis linked to this series.
      * This axis replaces the Type's Axis only if it's not null.
      */
     private NumberAxis axis = null;
 
     private static final ArrayList<Series> instances = new ArrayList<>(20);
 
     /**
      * Get all series currently in the SpeleoGraph Instance
      *
      * @return Unmodifiable list of instances.
      */
     public static List<Series> getInstances() {
         return Collections.unmodifiableList(instances);
     }
 
     /**
      * Detect if series is the first element of instances list.
      *
      * @return true if it's the first element.
      */
     public boolean isFirst() {
         return instances.indexOf(this) == 0;
     }
 
     /**
      * Detect if series is the last element of instances list.
      *
      * @return true if it's the last element.
      */
     public boolean isLast() {
         return instances.indexOf(this) == instances.size() - 1;
     }
 
     /**
      * Move the current series to n-1 position.
      */
     public void upSeriesInList() {
         int index = instances.indexOf(this), newIndex = index - 1;
         if (newIndex < 0) return; // We are already on top.
         Series buffer = instances.get(newIndex);
         instances.set(newIndex, instances.get(index));
         instances.set(index, buffer);
         notifyListeners();
     }
 
     /**
      * Move the current series to n+1 position.
      */
     public void downSeriesInList() {
         int index = instances.indexOf(this), newIndex = index + 1;
         if (newIndex >= instances.size()) return; // We are already on top.
         Series buffer = instances.get(newIndex);
         instances.set(newIndex, instances.get(index));
         instances.set(index, buffer);
         notifyListeners();
     }
 
     public static void setGraphPanel(GraphPanel graphPanel) {
         Series.graphPanel = graphPanel;
     }
 
     /**
      * Count the number of items into this Series.
      *
      * @return The number of items (assuming is 0 or more)
      */
     public int getItemCount() {
         if (items == null) return 0;
         return items.size();
     }
 
     /**
      * Get the file used to read the data.
      *
      * @return The data origin's file.
      */
     public File getOrigin() {
         return origin;
     }
 
     /**
      * Compute the date range of the items in this set.
      *
      * @return A date range which contains the lower and upper bounds of data.
      */
     public DateRange getRange() {
         int max = getItemCount();
         DateRange range;
         if (max == 0) {
             Date now = Calendar.getInstance().getTime();
             return new DateRange(now, now);
         }
         Date minDate = new Date(Long.MAX_VALUE), maxDate = new Date(Long.MIN_VALUE);
         for (int i = 0; i < max; i++) {
             Item item = items.get(i);
             if (item.getDate().before(minDate)) minDate = item.getDate();
             if (item.getDate().after(maxDate)) maxDate = item.getDate();
         }
         range = new DateRange(minDate, maxDate);
         return range;
     }
 
     /**
      * Getter for the series Type.
      * If this series is not attached to a DataSet, then we suppose that Type is {@link Type#UNKNOWN}
      *
      * @return The type for this series
      */
     @NotNull
     public Type getType() {
         return type;
     }
 
     /**
      * Say if we should show this series on a graph.
      *
      * @return true if we should show this series.
      */
     public boolean isShow() {
         return show;
     }
 
     /**
      * Set if we should show this series on a graph.
      *
      * @param v true if we should show
      */
     public void setShow(boolean v) {
         show = v;
         notifyListeners();
     }
 
     /**
      * Getter for the axis to display for this series.
      * If the series does not define his own axis, this function will search the Type's Axis.
      *
      * @return A NumberAxis to display it in a chart (never null)
      * @throws IllegalStateException if we can not find an axis for this series.
      */
     public NumberAxis getAxis() {
         if (axis != null) return axis;
         else if (type != null) return getType().getAxis();
         else throw new IllegalStateException("Can not find an axis for series !"); //NON-NLS
     }
 
     /**
      * Setter for the axis.
      * If an axis is set to the series, then the Type axis would not be shown and the Chart will display this axis for
      * the series even if other shown series are using the Type's axis.
      *
      * @param axis The axis to set for this series.
      */
     public void setAxis(NumberAxis axis) {
         if (axis == null) {
             log.info("Setting a null axis to series " + getName());
         }
         this.axis = axis;
         notifyInstanceListeners();
     }
 
     /**
      * Listeners for this series.
      */
     private ArrayList<DatasetChangeListener> listeners = new ArrayList<>();
 
     /**
      * Notify listeners about something changed into the series.
      */
     protected void notifyListeners() {
         final DatasetChangeEvent event = new DatasetChangeEvent(this, this);
         if (graphPanel != null)
             graphPanel.datasetChanged(event);
         for (DatasetChangeListener listener : staticListeners) {
             listener.datasetChanged(event);
         }
         for (DatasetChangeListener listener : listeners) {
             listener.datasetChanged(event);
         }
     }
 
     /**
      * Add a listener on series' properties.
      *
      * @param listener The listener will be called on events
      */
     public void addChangeListener(DatasetChangeListener listener) {
         if (!listeners.contains(listener)) listeners.add(listener);
     }
 
     /**
      * Remove a listener on series' properties.
      *
      * @param listener The listener which will be removed.
      */
     public void removeChangeListener(DatasetChangeListener listener) {
         if (listeners.contains(listener)) listeners.remove(listener);
     }
 
     /**
      * Getter for the human name of this series.
      * If the name is not set, it computes a name as "[Origin File Name] - [Name of the Type]"
      *
      * @return The display name for this Series.
      */
     public String getName() {
         return name == null ? getOrigin().getName() + " - " + getType().getName() : name;
     }
 
     /**
      * Say if the Series name has been chosen by human or generated.
      *
      * @return true if name is set by human
      */
     public boolean isNameHumanSet() {
         return name != null;
     }
 
     /**
      * Set an human name for this Series.
      *
      * @param name The name to set (should not be null)
      */
     public void setName(String name) {
         this.name = name;
         notifyListeners();
     }
 
     @Override
     public int compareTo(Object o) {
         return this.equals(o) ? 0 : -1;
     }
 
     /**
      * Add an item to this series.
      *
      * @param item The item to add.
      */
     public void add(Item item) {
         Validate.notNull(item);
         items.add(item);
     }
 
     /**
      * Returns the high-value for the specified series and item.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The value.
      */
     @Override
     public Number getHigh(int series, int item) {
         return getHighValue(series, item);
     }
 
     /**
      * Returns the high-value (as a double primitive) for an item within a
      * series.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The high-value.
      */
     @Override
     public double getHighValue(int series, int item) {
         if (isMinMax())
             if (isShow() && (item > -1 && item < items.size()))
                 return items.get(item).getHigh();
             else
                 return Double.NaN;
         else
             return Double.NaN;
     }
 
     /**
      * Returns the low-value for the specified series and item.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The value.
      */
     @Override
     public Number getLow(int series, int item) {
         return getLowValue(series, item);
     }
 
     /**
      * Returns the low-value (as a double primitive) for an item within a
      * series.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The low-value.
      */
     @Override
     public double getLowValue(int series, int item) {
         if (isMinMax())
             if (isShow() && (item > -1 && item < items.size()))
                 return items.get(item).getLow();
             else
                 return Double.NaN;
         else
             return Double.NaN;
     }
 
     /**
      * Returns the open-value for the specified series and item.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The value.
      */
     @Override
     public Number getOpen(int series, int item) {
         return getOpenValue(series, item);
     }
 
     /**
      * Returns the open-value (as a double primitive) for an item within a
      * series.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The open-value.
      */
     @Override
     public double getOpenValue(int series, int item) {
         return Double.NaN;
     }
 
     /**
      * Returns the y-value for the specified series and item.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The value.
      */
     @Override
     public Number getClose(int series, int item) {
         return getCloseValue(series, item);
     }
 
     /**
      * Returns the close-value (as a double primitive) for an item within a
      * series.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The close-value.
      */
     @Override
     public double getCloseValue(int series, int item) {
         return Double.NaN;
     }
 
     /**
      * Returns the volume for the specified series and item.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The value.
      */
     @Override
     public Number getVolume(int series, int item) {
         return getVolumeValue(series, item);
     }
 
     /**
      * Returns the volume-value (as a double primitive) for an item within a
      * series.
      *
      * @param series the series (zero-based index).
      * @param item   the item (zero-based index).
      * @return The volume-value.
      */
     @Override
     public double getVolumeValue(int series, int item) {
         return Double.NaN;
     }
 
     /**
      * Returns the order of the domain (or X) values returned by the dataset.
      *
      * @return The order (never <code>null</code>).
      */
     @Override
     public DomainOrder getDomainOrder() {
         return DomainOrder.ASCENDING;
     }
 
     /**
      * Returns the number of items in a series.
      * <br><br>
      * It is recommended that classes that implement this method should throw
      * an <code>IllegalArgumentException</code> if the <code>series</code>
      * argument is outside the specified range.
      *
      * @param series the series index (in the range <code>0</code> to
      *               <code>getSeriesCount() - 1</code>).
      * @return The item count.
      */
     @Override
     public int getItemCount(int series) {
         return isShow() ? items.size() : 0;
     }
 
     /**
      * Returns the x-value for an item within a series.  The x-values may or
      * may not be returned in ascending order, that is up to the class
      * implementing the interface.
      *
      * @param series the series index (in the range <code>0</code> to
      *               <code>getSeriesCount() - 1</code>).
      * @param item   the item index (in the range <code>0</code> to
      *               <code>getItemCount(series)</code>).
      * @return The x-value (never <code>null</code>).
      */
     @Override
     public Number getX(int series, int item) {
         return getXValue(series, item);
     }
 
     /**
      * Returns the x-value for an item within a series.
      *
      * @param series the series index (in the range <code>0</code> to
      *               <code>getSeriesCount() - 1</code>).
      * @param item   the item index (in the range <code>0</code> to
      *               <code>getItemCount(series)</code>).
      * @return The x-value.
      */
     @Override
     public double getXValue(int series, int item) {
         if (isShow() && (item > -1 && item < items.size()))
             return items.get(item).getDate().getTime();
         else
             return Double.NaN;
     }
 
     /**
      * Returns the y-value for an item within a series.
      *
      * @param series the series index (in the range <code>0</code> to
      *               <code>getSeriesCount() - 1</code>).
      * @param item   the item index (in the range <code>0</code> to
      *               <code>getItemCount(series)</code>).
      * @return The y-value (possibly <code>null</code>).
      */
     @Override
     public Number getY(int series, int item) {
         return getYValue(series, item);
     }
 
     /**
      * Returns the y-value (as a double primitive) for an item within a series.
      *
      * @param series the series index (in the range <code>0</code> to
      *               <code>getSeriesCount() - 1</code>).
      * @param item   the item index (in the range <code>0</code> to
      *               <code>getItemCount(series)</code>).
      * @return The y-value.
      */
     @Override
     public double getYValue(int series, int item) {
         if (isShow() && (item > -1 && item < items.size()))
             return items.get(item).getValue();
         else
             return Double.NaN;
     }
 
     /**
      * Returns the number of series in the dataset.
      *
      * @return The series count.
      */
     @Override
     public int getSeriesCount() {
         return isShow() ? 1 : 0;
     }
 
     /**
      * Returns the key for a series.
      *
      * @param series the series index (in the range <code>0</code> to
      *               <code>getSeriesCount() - 1</code>).
      * @return The key for the series.
      */
     @Override
     public Comparable getSeriesKey(int series) {
         return getName();
     }
 
     /**
      * Returns the index of the series with the specified key, or -1 if there
      * is no such series in the dataset.
      *
      * @param seriesKey the series key (<code>null</code> permitted).
      * @return The index, or -1.
      */
     @Override
     public int indexOf(Comparable seriesKey) {
         return getName().compareTo(String.valueOf(seriesKey)) == 0 ? 0 : -1;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
 
     private Type type = Type.UNKNOWN;
 
     /**
      * Returns the dataset group.
      *
      * @return The dataset group.
      */
     @Override
     public Type getGroup() {
         return getType();
     }
 
     /**
      * Sets the dataset group.
      *
      * @param group the dataset group.
      */
     @Override
     public void setGroup(DatasetGroup group) {
         if (group instanceof Type) {
             setType((Type) group);
         } else {
             throw new IllegalArgumentException("Group must by a SpeleoGraph Type");
         }
     }
 
     /**
      * Define the {@link Object#toString()} to be alias of {@link #getName()}.
      *
      * @return The name of this series.
      * @see #getName()
      */
     @Override
     public String toString() {
         return getName();
     }
 
     public void delete() {
         instances.remove(this);
         items.clear();
         notifyListeners();
     }
 
     private XYItemRenderer renderer;
 
     public XYItemRenderer getRenderer() {
        if (renderer == null) setupRendererAuto();
         if (color != null) {
             renderer.setSeriesPaint(0, color);
         }
         return renderer;
     }
 
     public Series generateSampledSeries(long length) {
         final Series newSeries;
         newSeries = new Series(origin, type);
         newSeries.setStepped(true);
         final int itemsCount = getItemCount();
         final ArrayList<Item> newItems = newSeries.items;
         double bufferValue = 0D;
         DateRange range = getRange();
         long lastStartBuffer = range.getLowerMillis();
         newItems.add(new Item(newSeries, new Date(lastStartBuffer), 0.0));
         for (int i = 1; i < itemsCount; i++) {
             final Item originalItem = items.get(i), previousOriginalItem = items.get(i - 1);
             if (lastStartBuffer + length <= originalItem.getDate().getTime()) {
                 newItems.add(new Item(newSeries, new Date(lastStartBuffer), bufferValue));
                 newItems.add(new Item(newSeries, new Date(lastStartBuffer + length), bufferValue));
                 final long time = originalItem.getDate().getTime();
                 lastStartBuffer = lastStartBuffer + length;
                 if (lastStartBuffer + 2 * length < time) {
                     newItems.add(new Item(newSeries, new Date(lastStartBuffer), 0));
                     lastStartBuffer = time -
                             ((originalItem.getDate().getTime() - lastStartBuffer) % length);
                     newItems.add(new Item(newSeries, new Date(lastStartBuffer), 0));
                 }
                 bufferValue = 0D;
             }
             bufferValue = bufferValue + (originalItem.getValue() - previousOriginalItem.getValue());
         }
         newItems.add(new Item(newSeries, new Date(lastStartBuffer), bufferValue));
         newItems.add(new Item(newSeries, new Date(range.getUpperMillis()), bufferValue));
         return newSeries;
     }
 
     private static final HashSet<DatasetChangeListener> staticListeners = new HashSet<>(2);
 
     public static void addListener(DatasetChangeListener listener) {
         staticListeners.add(listener);
     }
 
     public List<Item> getItems() {
         return Collections.unmodifiableList(items);
     }
 
     /**
      *
      */
     private DrawStyle style = DrawStyle.AUTO;
 
     public DrawStyle getStyle() {
         return style;
     }
 
     public void setStyle(DrawStyle style) {
         Validate.notNull(style);
         if (isMinMax() && !(style == DrawStyle.AUTO || style == DrawStyle.HIGH_LOW)) return;
         if (style.equals(this.style)) return;
         this.style = style;
         switch (style) {
             case AUTO:
                 setupRendererAuto();
                 break;
             case AREA:
                 renderer = new XYAreaRenderer(XYAreaRenderer.AREA);
                 break;
             case HIGH_LOW:
                 renderer = new HighLowRenderer();
                 break;
             default:
             case LINE:
                 renderer = new XYLineAndShapeRenderer(true, false);
 
         }
         notifyListeners();
     }
 
     private void setupRendererAuto() {
         if (isMinMax()) {
             renderer = new HighLowRenderer();
         } else if (isStepped()) {
             renderer = new XYAreaRenderer(XYAreaRenderer.AREA);
         } else {
             renderer = new XYLineAndShapeRenderer(true, false);
         }
     }
 
     /**
      * Color of the series on screen.
      */
     private Color color;
 
     public Color getColor() {
         if (color == null && renderer != null) {
             return (Color) renderer.getSeriesPaint(0);
         }
         return color;
     }
 
     public void setColor(Color color) {
         if (renderer == null) setupRendererAuto();
         this.color = color;
         notifyListeners();
     }
 
     /**
      * Notify all static listeners that an edit occurs.
      * <p>Note: This function will refresh graphics, so it could occur thread blocking</p>
      */
     public static void notifyInstanceListeners() {
         final DatasetChangeEvent event = new DatasetChangeEvent(Series.class, null);
         if (graphPanel != null)
             graphPanel.datasetChanged(event);
         for (DatasetChangeListener listener : staticListeners) {
             listener.datasetChanged(event);
         }
     }
 
     public boolean hasOwnAxis() {
         return axis != null;
     }
 
     public void setStepped(boolean stepped) {
         this.stepped = stepped;
     }
 
     public boolean isStepped() {
         return stepped;
     }
 
     public void setMinMax(boolean minMax) {
         this.minMax = minMax;
     }
 
     public boolean isMinMax() {
         return minMax;
     }
 }
