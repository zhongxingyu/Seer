 package fi.jawsy.jawwa.zk.highcharts;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.IdentityHashMap;
 import java.util.Map;
 
 import org.zkoss.json.JSONAware;
 import org.zkoss.json.JSONObject;
 import org.zkoss.json.JavaScriptValue;
 import org.zkoss.zk.ui.sys.ContentRenderer;
 import org.zkoss.zul.Div;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import fi.jawsy.jawwa.lang.Option;
 import fi.jawsy.jawwa.zk.highcharts.impl.HighchartsImpl;
 
 public class Highcharts extends Div {
 
     private static final long serialVersionUID = -7849878366560709958L;
 
    private Options options = createOptions();
 
     private ArrayList<HighchartsSeries<?>> series = Lists.newArrayList();
 
     @lombok.experimental.Value
     static class DeltaWrapper implements Serializable, JSONAware {
         private static final long serialVersionUID = -8401732188846298079L;
 
         public final int series;
         public final SeriesDelta<?> delta;
 
         @Override
         public String toJSONString() {
             JSONObject json = new JSONObject();
             json.put("series", series);
             json.put("delta", delta);
             return json.toJSONString();
         }
     }
 
     @lombok.experimental.Value
     class Listener implements SeriesDeltaListener<Object> {
         private final int series;
 
         @Override
         public void onDelta(SeriesDelta<? extends Object> delta) {
             smartUpdate("delta", new DeltaWrapper(series, delta), true);
         }
     }
 
     private final IdentityHashMap<HighchartsSeries<?>, Listener> listeners = Maps.newIdentityHashMap();
 
     public static final class Value<T> {
         private Option<T> value;
 
         public T get() {
             return (value == null) ? null : value.getOrNull();
         }
 
         public void set(T value) {
             this.value = Option.option(value);
         }
 
         public boolean exists() {
             return this.value != null;
         }
 
         public void clear() {
             this.value = null;
         }
 
         public void write(String name, JSONObject json) {
             if (exists())
                 json.put(name, get());
         }
 
         public static <T> Value<T> create() {
             return new Value<T>();
         }
     }
 
     public static interface Options extends RawJsonSupport {
         public static interface Chart extends RawJsonSupport {
             Value<Boolean> alignTicks();
 
             Value<Animation> animation();
 
             Value<Color> backgroundColor();
 
             Value<Color> borderColor();
 
             Value<Integer> borderRadius();
 
             Value<Integer> borderWidth();
 
             Value<Boolean> ignoreHiddenSeries();
 
             Value<Boolean> inverted();
 
             Value<Color> plotBackgroundColor();
 
             Value<Color> plotBorderColor();
 
             Value<Integer> plotBorderWidth();
 
             Value<Color> selectionMarkerFill();
 
             Value<Boolean> showAxes();
 
             Value<Integer> spacingBottom();
 
             Value<Integer> spacingLeft();
 
             Value<Integer> spacingRight();
 
             Value<Integer> spacingTop();
 
             Value<ImmutableMap<String, String>> style();
         }
 
         public static interface Credits extends RawJsonSupport {
             Value<Boolean> enabled();
         }
 
         public static interface Legend extends RawJsonSupport {
             Value<Align> align();
 
             Value<Color> backgroundColor();
 
             Value<Boolean> enabled();
 
             Value<ImmutableMap<String, String>> itemStyle();
 
             Value<Boolean> floating();
 
             Value<VerticalAlign> verticalAlign();
         }
 
         public static interface PlotOptions extends RawJsonSupport {
             public static interface Series extends RawJsonSupport {
                 public static interface DataLabels extends RawJsonSupport {
                     Value<JavaScriptValue> formatter();
                 }
 
                 Value<Boolean> allowPointSelect();
 
                 Value<Animation> animation();
 
                 Value<Color> color();
 
                 Value<Boolean> connectEnds();
 
                 Value<Boolean> connectNulls();
 
                 Value<Integer> cropThreshold();
 
                 Value<String> cursor();
 
                 Value<Boolean> enableMouseTracking();
 
                 Value<Color> fillColor();
 
                 Value<Double> fillOpacity();
 
                 Value<String> id();
 
                 Value<Integer> lineWidth();
 
                 Value<Double> pointInterval();
 
                 Value<Double> pointStart();
 
                 Value<Boolean> selected();
 
                 Value<Boolean> showCheckbox();
 
                 Value<Boolean> showInLegend();
 
                 Value<Stacking> stacking();
 
                 Value<Boolean> stickyTracking();
 
                 Value<Integer> turboThreshold();
 
                 Value<Boolean> visible();
 
                 Value<Integer> zIndex();
 
                 DataLabels dataLabels();
             }
 
             Series series();
         }
 
         public static interface Series extends RawJsonSupport {
 
             public static interface Marker extends RawJsonSupport {
                 Value<Boolean> enabled();
             }
 
             Value<Color> color();
 
             Value<String> name();
 
             Marker marker();
         }
 
         public static interface Subtitle extends RawJsonSupport {
             Value<Align> align();
 
             Value<Boolean> floating();
 
             Value<ImmutableMap<String, String>> style();
 
             Value<String> text();
 
             Value<VerticalAlign> verticalAlign();
 
             Value<Integer> x();
 
             Value<Integer> y();
         }
 
         public static interface Title extends RawJsonSupport {
             Value<Align> align();
 
             Value<Boolean> floating();
 
             Value<Integer> margin();
 
             Value<ImmutableMap<String, String>> style();
 
             Value<String> text();
 
             Value<VerticalAlign> verticalAlign();
 
             Value<Integer> x();
 
             Value<Integer> y();
         }
 
         public static interface Tooltip extends RawJsonSupport {
             Value<Boolean> animation();
 
             Value<Color> backgroundColor();
 
             Value<Color> borderColor();
 
             Value<Integer> borderRadius();
 
             Value<Integer> borderWidth();
 
             Value<Boolean> enabled();
 
             Value<String> footerFormat();
 
             Value<JavaScriptValue> formatter();
 
             Value<Boolean> shadow();
 
             Value<Boolean> shared();
 
             Value<Integer> snap();
 
             Value<ImmutableMap<String, String>> style();
 
             Value<Boolean> useHtml();
 
             Value<Integer> valueDecimals();
 
             Value<String> valuePrefix();
 
             Value<String> valueSuffix();
         }
 
         public static interface Axis extends RawJsonSupport {
             public static interface Labels extends RawJsonSupport {
                 Value<Align> align();
 
                 Value<Boolean> enabled();
 
                 Value<Double> rotation();
 
                 Value<ImmutableMap<String, String>> style();
 
                 Value<Integer> x();
 
                 Value<Integer> y();
             }
 
             public static interface Title extends RawJsonSupport {
                 Value<AxisTitleAlign> align();
 
                 Value<Integer> margin();
 
                 Value<Double> rotation();
 
                 Value<ImmutableMap<String, String>> style();
 
                 Value<String> text();
             }
 
             Value<Boolean> allowDecimals();
 
             Value<Color> alternateGridColor();
 
             Value<ImmutableList<String>> categories();
 
             Value<Boolean> endOnTick();
 
             Value<Color> gridLineColor();
 
             Value<Double> max();
 
             Value<Double> maxPadding();
 
             Value<Double> min();
 
             Value<Double> minPadding();
 
             Value<Double> minRange();
 
             Value<Double> minTickInterval();
 
             Value<Color> minorGridLineColor();
 
             Value<Integer> minorGridLineWidth();
 
             Value<Color> minorTickColor();
 
             Value<Integer> minorTickLength();
 
             Value<Integer> minorTickWidth();
 
             Value<Integer> offset();
 
             Value<Boolean> opposite();
 
             Value<Boolean> reversed();
 
             Value<Boolean> showEmpty();
 
             Value<Boolean> showFirstLabel();
 
             Value<Boolean> showLastLabel();
 
             Value<Integer> startOfWeek();
 
             Value<Boolean> startOnTick();
 
             Value<Color> tickColor();
 
             Value<Integer> tickLength();
 
             Value<Integer> tickPixelInterval();
 
             Value<Integer> tickWidth();
 
             Labels labels();
 
             Title title();
         }
 
         Chart chart();
 
         Credits credits();
 
         Legend legend();
 
         PlotOptions plotOptions();
 
         Series series(int index);
 
         Subtitle subtitle();
 
         Title title();
 
         Tooltip tooltip();
 
         Axis xAxis();
 
         Axis yAxis();
 
     }
 
     public static Options createOptions() {
         return HighchartsImpl.createOptions();
     }
 
     @Override
     protected void renderProperties(ContentRenderer renderer) throws IOException {
         super.renderProperties(renderer);
 
         if (options != null)
             renderer.render("options", options);
         if (!series.isEmpty())
             renderer.render("series", series);
     }
 
     public Options getOptions() {
         return options;
     }
 
     public void setOptions(Options options) {
         if (this.options != options) {
             this.options = options;
             smartUpdate("options", options);
         }
     }
 
     public void refreshOptions() {
         smartUpdate("options", options);
     }
 
     public void addSeries(HighchartsSeries<?> series) {
         setSeries(this.series.size(), series);
     }
 
     public void setSeries(int index, HighchartsSeries<?> series) {
         if (this.series.size() > index) {
             HighchartsSeries<?> oldSeries = this.series.set(index, series);
             for (Listener listener : Option.option(listeners.remove(oldSeries))) {
                 oldSeries.removeSeriesDeltaListener(listener);
             }
         } else {
             for (int i = this.series.size(); i < index; i++) {
                 this.series.add(null);
             }
             this.series.add(series);
         }
         Listener listener = new Listener(index);
         listeners.put(series, listener);
         series.addSeriesDeltaListener(listener);
         smartUpdate("series", this.series);
     }
 
     public void clearSeries() {
         this.series.clear();
         this.series.trimToSize();
 
         for (Map.Entry<HighchartsSeries<?>, Listener> listenerEntry : listeners.entrySet()) {
             listenerEntry.getKey().removeSeriesDeltaListener(listenerEntry.getValue());
         }
         listeners.clear();
         smartUpdate("series", this.series);
     }
 
 }
