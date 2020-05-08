 package org.icefaces.ace.component.chart;
 
 import org.icefaces.ace.meta.annotation.*;
 import org.icefaces.ace.meta.baseMeta.UIComponentBaseMeta;
 import org.icefaces.ace.model.chart.ChartSeries;
 
 import javax.el.MethodExpression;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 
 @Component(
         tagName = "chart",
         componentClass  = "org.icefaces.ace.component.chart.Chart",
         generatedClass  = "org.icefaces.ace.component.chart.ChartBase",
         extendsClass    = "javax.faces.component.UIComponentBase",
         componentType   = "org.icefaces.ace.component.Chart",
         rendererType    = "org.icefaces.ace.component.ChartRenderer",
         rendererClass   = "org.icefaces.ace.component.chart.ChartRenderer",
         componentFamily = "org.icefaces.ace.Chart",
        tlddoc = "Render a HTML &lt;canvas&rt; data visualization using a Java interface to the jqPlot charting API. " +
                 "A List of ChartSeries subclasses define the data and its representation in the visualization." +
                 "<p>For more information, see the <a href=\"http://wiki.icefaces.org/display/ICE/Chart\">Chart Wiki Documentation</a>.</p>"
 )
 @ResourceDependencies({
         @ResourceDependency(library = "icefaces.ace", name = "util/ace-jquery.js"),
         @ResourceDependency(library = "icefaces.ace", name = "chart/ace-chart.js"),
         @ResourceDependency(library = "icefaces.ace", name = "util/combined.css")
 })
 @ClientBehaviorHolder(events = {
     @ClientEvent(name="click", defaultRender = "@this", defaultExecute = "@this",
             tlddoc = "Fired when data point is clicked plot."),
     @ClientEvent(name="dragStart",
             tlddoc = "Fired when a drag has begun on a particular drag-enabled plot data point."),
     @ClientEvent(name="dragStop",
             tlddoc = "Fired when a drag has ceased on a particular drag-enabled plot data point."),
     @ClientEvent(name="mouseInData",
             tlddoc = "Fired when the mouse is inside a filled plot region."),
     @ClientEvent(name="mouseOutData",
             tlddoc = "Fired when the mouse is outside a filled plot region."),
     @ClientEvent(name="showHighlighter",
             tlddoc = "Fired when the highlighter is being displayed by the mouse being near an enabled data point."),
     @ClientEvent(name="hideHighlighter",
             tlddoc = "Fired when the highlighter is being hidden by the mouse moving away from an enabled data point.")
 })
 public class ChartMeta extends UIComponentBaseMeta {
     @Property(tlddoc =
                 "Define a title of the entire chart.")
     private String title;
 
     @Property(tlddoc = "The JavaScript global component instance name. " +
             "Must be unique among components on a page. ")
     private String widgetVar;
 
     @Property(tlddoc =
                 "Define an individual instance or List of ChartSeries objects to draw on this plot.")
     private Object value;
 
     @Property(tlddoc = 
                 "Define a ChartSeries whose configuration is used where other ChartSeries " +
                 "have not made an explicit configuration. The data of this ChartSeries is " +
                 "irrelevant. If defined, this ChartSeries should explicitly have the type" +
                 "field set, this will be used as the default type for all series which have " +
                 "not explicitly defined a type. If undefined the default type is determined by the" +
                 "ChartSeries.getDefaultType() of the first attached series.")
     private ChartSeries defaultSeriesConfig;
 
     @Property(tlddoc = 
                 "Enabling displays the legend.")
     private Boolean legend;
     @Property(tlddoc = 
                 "Defines the location of legend relative to the bounds of the chart. " +
                 "All of the cardinal directions are available in the following format: " +
                 "N, NE, E, SE, S, etc.")
     private Location legendLocation;
     @Property(tlddoc = 
                 "Defines the placement of the legend relative to the content of the " +
                 "chart. The available configurations are: INSIDE_GRID, OUTSIDE_GRID " +
                 "and OUTSIDE")
     private LegendPlacement legendPlacement;
 
     @Property(tlddoc =
                 "Enables a stack or \"mountain\" plot.  Not all types of series " +
                 "may support this mode.",
         defaultValue = "false", defaultValueType = DefaultValueType.EXPRESSION)
     private Boolean stackSeries;
 
     @Property (tlddoc =
                 "Enables the draw animation behaviour of the chart. By default " +
                 "is enabled for all browsers but IE8 and lower for performance" +
                 "concerns.")
     private Boolean animated;
 
     @Property(tlddoc =
                 "Define a Axis whose configuration is used where other axes have " +
                 "not made an explicit configuration.")
     private Axis defaultAxesConfig;
 
     @Property(tlddoc =
                 "Defines the configuration of the x axis. Attempts are made to " +
                 "interpret a configuration if undefined.")
     private Axis xAxis;
 
     @Property(tlddoc =
                 "Defines the configuration of the x1 axis. Attempts are made to " +
                 "interpret a configuration if undefined.")
     private Axis x2Axis;
 
     @Property(tlddoc =
                 "Defines the configuration of the y axes (up to 9). Attempts are made to " +
                 "interpret a configuration if undefined.")
     private Axis[] yAxes;
 
     @Property(tlddoc =
                 "Enables drawing an info string at the cursor position when hovering " +
                 "at data points. Default configuration displays x, y values." )
     private Boolean highlighter;
 
     @Property(tlddoc =
                 "Enables drawing an 'active' styled point marker while the highlighter " +
                 "tooltip is shown. Is true by default.")
     private Boolean highlighterShowMarker;
 
     @Property(tlddoc =
                 "Defines the direction the highlighter tooltip is located relative to " +
                 "the cursor. Is 'NW' by default.")
     private Location highlighterLocation;
 
     @Property(tlddoc =
                 "Defines the point values passed to the highlighter tooltip, either " +
                 "X, Y, XY or YX. Default is XY.")
     private HighlighterTooltipAxes highlighterAxes;
 
     @Property(tlddoc =
                 "Defines a format string populated with the x, y values indicated by the " +
                 "highlighterAxes. Takes C-style string format options ex. 'Date: %s, number of cats: %d' ")
     private String highlighterFormatString;
 
     @Property(tlddoc =
                 "Defines how many Y values a highlighter should expect in the data points array. " +
                 "Typically this is 1.  Certain plots, like OHLC, will have more y values in each " +
                 "data point array and will need this property adjusted to parse correctly.")
     private Integer highlighterYValueCount;
 
     @Property(tlddoc =
                 "Enables the highlighted series being temporarily reordered to be entirely visible.")
     private Boolean highlighterBringSeriesToFront;
 
     @Property(tlddoc =
                 "Enable the default handling of the chart when rendered into a hidden page region. The" +
                 "chart polls its hidden status, looking for when it is shown, and then completes its initialization." +
                 "This can be expensive in environments of reduced JavaScript performance with many charts and a complex DOM." +
                 "When this is disabled, upon revealing a chart, to ensure it is correctly displayed, 'chartWidgetvar.replot()' " +
                 "must be called.",
             defaultValue = "true", defaultValueType = DefaultValueType.EXPRESSION)
     private Boolean hiddenInitPolling;
 
     @Property(tlddoc = "Enable a crosshair cursor on the chart.")
     private Boolean cursor;
 
     @Property(tlddoc = "Enable click+drag selection of a range on the chart using the cursor. The 'cursor' property must be enabled to use this property.")
     private Boolean zoom;
 
     @Property(tlddoc = "Enable display of a legend regarding the coordinates of the cursor. The 'cursor' property must be enabled to use this property.")
     private Boolean showTooltip;
 
     @Property(tlddoc = "Define the width of the entire chart region in pixels.")
     private Integer width;
 
     @Property(tlddoc = "Define the height of the entire chart region in pixels.")
     private Integer height;
 
     @Property(tlddoc = "Define CSS inline style for the chart container.")
     private String style;
 
     @Property(expression = Expression.METHOD_EXPRESSION,
             methodExpressionArgument = "org.icefaces.ace.event.SeriesSelectionEvent",
             tlddoc = "MethodExpression reference called whenever a series " +
                     "element is selected. The method receives a single " +
                     "argument, SeriesSelectionEvent.")
     private MethodExpression selectListener;
 
     @Property(expression = Expression.METHOD_EXPRESSION,
             methodExpressionArgument = "org.icefaces.ace.event.PointValueChangeEvent",
             tlddoc = "MethodExpression reference called whenever a series " +
                     "element is dragged to a new x or y value. The method receives a single " +
                     "argument, PointValueChangeEvent.")
     private MethodExpression pointChangeListener;
 }
