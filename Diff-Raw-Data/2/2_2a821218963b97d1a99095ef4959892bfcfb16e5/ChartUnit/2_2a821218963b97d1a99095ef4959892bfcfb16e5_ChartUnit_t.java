 package nl.ctmm.trait.proteomics.qcviewer.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.List;
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
      * The list of alternating colors that is used to draw the tic charts.
      */
     private static final List<Color> GRAPH_COLORS = Arrays.asList(
             Color.BLUE, Color.DARK_GRAY, Color.GRAY, Color.MAGENTA, Color.ORANGE,
             Color.PINK, Color.LIGHT_GRAY, Color.RED, Color.GREEN);
 
     /**
      * The JFreeChart object used to draw this tic chart.
      */
     private JFreeChart ticChart;
 
     /**
      * The maximum intensity, which is equal to the maximum y value in the data series.
      */
     private double maxIntensity;
 
     /**
      * Create a chart unit with the specified msrun name, report number and data series.
      *
      * @param msrunName the name of the msrun.
      * @param reportNumber the report number of the msrun.
      * @param series the data series.
      */
     public ChartUnit(final String msrunName, final int reportNumber, final XYSeries series) {
         String maxIntensityString = "N/A";
         if (series != null) {
             maxIntensity = series.getMaxY();
             maxIntensityString = new DecimalFormat("0.0000E0").format(maxIntensity);
         }
         final XYBarRenderer renderer = createBarRenderer(reportNumber);
         final XYSeriesCollection xyDataset = new XYSeriesCollection(series);
         //Prepare chart using plot - this is the best option to control domain and range axes
         final NumberAxis domainAxis = new NumberAxis(null);
         final NumberAxis rangeAxis = new NumberAxis(null);
         final XYPlot plot = new XYPlot(xyDataset, domainAxis, rangeAxis, renderer);
         rangeAxis.setNumberFormatOverride(new DecimalFormat("0E00"));
         final Font font = new Font("Garamond", Font.BOLD, 13);
         ticChart = new JFreeChart(msrunName + "     MaxIntensity = " + maxIntensityString, font, plot, false);
         // performance
         ticChart.setAntiAlias(false);
     }
 
     /**
      * Create a <code>XYBarRenderer</code>.
      *
      * @param reportNumber the report number, which is used for picking a color.
      * @return the <code>XYBarRenderer</code>.
      */
     private XYBarRenderer createBarRenderer(final int reportNumber) {
         final XYBarRenderer renderer = new XYBarRenderer();
         //Sets the percentage amount by which the bars are trimmed
         renderer.setMargin(0.98); //Default renderer margin is 0.0
         renderer.setDrawBarOutline(false);
         renderer.setShadowVisible(false);
         final Color currentColor = GRAPH_COLORS.get(reportNumber % GRAPH_COLORS.size());
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
