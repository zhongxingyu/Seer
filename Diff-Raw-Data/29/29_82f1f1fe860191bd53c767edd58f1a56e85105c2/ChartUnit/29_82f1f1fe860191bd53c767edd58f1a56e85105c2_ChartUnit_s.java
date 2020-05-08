 package nl.ctmm.trait.proteomics.qcviewer.gui;
 
 import java.awt.Color;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Logger;
 
 import nl.ctmm.trait.proteomics.qcviewer.utils.Constants;
 
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYBarPainter;
 import org.jfree.chart.renderer.xy.XYBarRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 /**
  * The object of this class represents a tic chart (including maximum intensity) of a single msrun.
  *
  * @author <a href="mailto:pravin.pawar@nbic.nl">Pravin Pawar</a>
  * @author <a href="mailto:freek.de.bruijn@nbic.nl">Freek de Bruijn</a>
  */
 public class ChartUnit {
     /**
      * The logger for this class.
      */
     private static final Logger logger = Logger.getLogger(ChartUnit.class.getName());
 
     /**
      * The list of alternating colors that is used to draw the tic charts.
      */
     private static final List<Color> GRAPH_COLORS = Arrays.asList(
             Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE,
             Color.PINK, Color.LIGHT_GRAY, Color.RED, Color.GREEN);
 
     /**
      * The percentage amount by which the width of the bars will be trimmed.
      */
     private static final double TRIM_PERCENTAGE_BARS = 0.98;
 
     /**
      * Message written to the logger while creating chart unit. 
      */
    private static final String CHART_UNIT_MESSAGE = "In ChartUnit: reportIndex = %s " +
    		"msrunName %s maxIntensity %s";
 
     /**
      * Title of the TIC chart. 
      */
    private static final String CHART_UNIT_TITLE = "Index = %s   msrun = %s     " +
    		"MaxIntensity = %s";
 
     /**
     * Decimal format for displaying max intensity string
      */
    private static final String MAX_INTENSITY_STRING_FORMAT = "0.0000E0";
 
     /**
     * Decimal format for displaying number on the range axis (y axis) of TIC chart
      */
    private static final String RANGE_AXIS_NUMBER_FORMAT = "0E00";
 
     /**
      * The JFreeChart object used to draw this tic chart.
      */
     private JFreeChart ticChart;
 
     /**
      * The maximum intensity, which is equal to the maximum y value in the data series.
      * 
      */
     private double maxIntensity;
 
     /**
      * Create a chart unit with the specified msrun name, report number and data series.
      *
      * @param msrunName the name of the msrun.
      * @param reportIndex the index of msrun report.
      * @param series the data series.
      */
     public ChartUnit(final String msrunName, final int reportIndex, final XYSeries series) {

         String maxIntensityString = Constants.NOT_AVAILABLE_STRING;
         if (series != null) {
             maxIntensity = series.getMaxY();
            maxIntensityString = new DecimalFormat(MAX_INTENSITY_STRING_FORMAT).format(maxIntensity);
         }
         logger.fine(String.format(CHART_UNIT_MESSAGE, reportIndex, msrunName, maxIntensity));
         final XYBarRenderer renderer = createBarRenderer(reportIndex);
         final XYSeriesCollection xyDataset = new XYSeriesCollection(series);
         //Prepare chart using plot - this is the best option to control domain and range axes
         final NumberAxis domainAxis = new NumberAxis(null);
         final NumberAxis rangeAxis = new NumberAxis(null);
         final XYPlot plot = new XYPlot(xyDataset, domainAxis, rangeAxis, renderer);
        rangeAxis.setNumberFormatOverride(new DecimalFormat(RANGE_AXIS_NUMBER_FORMAT));
         final String title = String.format(CHART_UNIT_TITLE, reportIndex, 
                 msrunName, maxIntensityString);
         ticChart = new JFreeChart(title, Constants.CHART_TITLE_FONT, plot, false);
         // performance
         ticChart.setAntiAlias(false);
     }
 
     /**
      * Create a <code>XYBarRenderer</code>.
      *
      * @param reportIndex the report index, which is used for picking a color.
      * @return the <code>XYBarRenderer</code>.
      */
     private XYBarRenderer createBarRenderer(final int reportIndex) {
         final XYBarRenderer renderer = new XYBarRenderer();
         renderer.setMargin(TRIM_PERCENTAGE_BARS);
         renderer.setDrawBarOutline(false);
         renderer.setShadowVisible(false);
         final Color currentColor = GRAPH_COLORS.get(reportIndex % GRAPH_COLORS.size());
         renderer.setSeriesPaint(0, currentColor);
         renderer.setGradientPaintTransformer(null);
         renderer.setSeriesOutlinePaint(0, currentColor);
         renderer.setBaseFillPaint(currentColor);
         renderer.setBasePaint(currentColor);
         renderer.setBarPainter(new StandardXYBarPainter());
         renderer.setSeriesStroke(0, null);
         renderer.setBasePaint(Color.WHITE);
         XYBarRenderer.setDefaultShadowsVisible(false);
         //Add support for toolTip
         final StandardXYToolTipGenerator toolTipGenerator = new StandardXYToolTipGenerator();
         renderer.setBaseToolTipGenerator(toolTipGenerator);
         renderer.setDrawBarOutline(false);
         return renderer;
     }
 
     /**
      * Get the corresponding tic chart.
      *
      * @return the tic chart.
      */
     public JFreeChart getTicChart() {
         return ticChart;
     }
 
     /**
      * Get the maximum intensity of the tic graph.
      *
      * @return the maximum intensity of the tic graph.
      */
     public double getMaxTicIntensity() {
         return maxIntensity;
     }
 }
