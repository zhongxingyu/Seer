 package gov.nih.nci.caintegrator.plots.kaplanmeier;
 
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.chart.*;
 import org.jfree.chart.title.LegendTitle;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.StandardXYToolTipGenerator;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.apache.log4j.Logger;
 
 import java.io.OutputStream;
 import java.io.IOException;
 import java.awt.image.BufferedImage;
 import java.awt.*;
 import java.awt.geom.Rectangle2D;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import gov.nih.nci.caintegrator.plots.kaplanmeier.model.*;
 import gov.nih.nci.caintegrator.plots.services.KMPlotServiceImpl;
 
 /**
 * @author caIntegrator Team
 */
 @SuppressWarnings("deprecation")
 public class JFreeChartIKMPlottermpl implements KMPlotter {
     private static Logger logger = Logger.getLogger(JFreeChartIKMPlottermpl.class);
 
     public void writeBufferedImage(OutputStream out, BufferedImage image, ImageTypes imgType)
     throws KMException {
         if ( imgType == null) imgType = ImageTypes.PNG;  // default
 
         try {
             if (imgType == ImageTypes.PNG) ChartUtilities.writeBufferedImageAsPNG(out, image);
             else if (imgType == ImageTypes.JPEG) ChartUtilities.writeBufferedImageAsJPEG(out, image);
             else {
                logger.debug("UnSupported File Format: " + imgType.getValue());
                 throw new KMException( "UnSupported File Format: " + imgType.getValue());
             }
         } catch (IOException ioe) {
             logger.debug(ioe);
             throw new KMException(ioe);
         }
 
     }
 
     public void writeBufferedImageAsPNG(OutputStream out, BufferedImage image) throws IOException {
         ChartUtilities.writeBufferedImageAsPNG(out, image);
     }
 
     public void writeBufferedImageAsJPEG(OutputStream out, BufferedImage image) throws IOException {
         ChartUtilities.writeBufferedImageAsJPEG(out, image);
     }
 
      public JFreeChart createKMPlot(Collection<GroupCoordinates> groupsToBePlotted, String title, String xAxisLabel,
                                     String yAxisLabel) {
          Collection<KMPlotPointSeriesSet> kmPlotSets =
                                      convertToKaplanMeierPlotPointSeriesSet(groupsToBePlotted);
 
          XYSeriesCollection finalDataCollection =  new XYSeriesCollection();
          /*  Repackage all the datasets to go into the XYSeriesCollection */
          for(KMPlotPointSeriesSet dataSet: kmPlotSets) {
              finalDataCollection.addSeries(dataSet.getCensorPlotPoints());
              finalDataCollection.addSeries(dataSet.getProbabilityPlotPoints());
 
          }
 
          JFreeChart chart = ChartFactory.createXYLineChart(  "", xAxisLabel, yAxisLabel,
                                                              finalDataCollection, PlotOrientation.VERTICAL,
                                                              true,//legend
                                                              true,//tooltips
                                                              false//urls
                                                            );
          XYPlot plot = (XYPlot) chart.getPlot();
          /*
           * Ideally the actual Renderer settings should have been created
           * at the survivalLength of iterating KaplanMeierPlotPointSeriesSets, adding them to the actual
           * Data Set that is going to be going into the Chart plotter.  But you have no idea how
           * they are going to be sitting in the Plot dataset so there is no guarantee that setting the
           * renderer based on a supposed index will actually work. In fact
           */
 
          XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
          for(int i = 0; i < finalDataCollection.getSeriesCount();i++) {
              KMPlotPointSeries kmSeries = (KMPlotPointSeries)finalDataCollection.getSeries(i);
 
              if(kmSeries.getType() == KMPlotPointSeries.SeriesType.CENSOR) {
                  renderer.setSeriesLinesVisible(i, false);
                  renderer.setSeriesShapesVisible(i, true);
              }else if(kmSeries.getType()==KMPlotPointSeries.SeriesType.PROBABILITY){
                  renderer.setSeriesLinesVisible(i, true);
                  renderer.setSeriesShapesVisible(i, false);
 
              }else {
                  //don't show this set as it is not a known type
                  renderer.setSeriesLinesVisible(i, false);
                  renderer.setSeriesShapesVisible(i, false);
              }
              renderer.setSeriesPaint(i, getKMSetColor(kmPlotSets,kmSeries.getKey(), kmSeries.getType()),true);
          }
 
          renderer.setToolTipGenerator(new StandardXYToolTipGenerator());
          renderer.setDefaultEntityRadius(6);
          plot.setRenderer(renderer);
 
          /* change the auto tick unit selection to integer units only... */
          NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
          rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
 
          /* OPTIONAL CUSTOMISATION COMPLETED. */
          rangeAxis.setAutoRange(true);
          rangeAxis.setRange(0.0,1.0);
 
          /* set Title and Legend */
          chart.setTitle(title);
          createLegend(chart, kmPlotSets);
 
          return chart;
      }
      public BufferedImage createImage(Collection<GroupCoordinates> groupsToBePlotted, String title, String xAxisLabel,
                                       String yAxisLabel) {
 
         return createKMPlot(groupsToBePlotted, title, xAxisLabel, yAxisLabel).createBufferedImage(800,500);
     }
      
      public Object createImageOfKnownType(Collection<GroupCoordinates> groupsToBePlotted, String title, String xAxisLabel,
              String yAxisLabel) {
 
          return createKMPlot(groupsToBePlotted, title, xAxisLabel, yAxisLabel);
 }
      
 
     private Collection<KMPlotPointSeriesSet> convertToKaplanMeierPlotPointSeriesSet(Collection<GroupCoordinates> groupsToBePlotted) {
         Collection<KMPlotPointSeriesSet>
                 plotPointSeriesSetCollection = new ArrayList<KMPlotPointSeriesSet>();
 
         for (Iterator<GroupCoordinates> iterator = groupsToBePlotted.iterator(); iterator.hasNext();) {
             GroupCoordinates groupToPlot = iterator.next();
             Collection<XYCoordinate> points = groupToPlot.getDataPoints();
             KMPlotPoint[] dataPoints = new KMPlotPoint[points.size()];
             int i = 0;
             for (Iterator<XYCoordinate> iterator1 = points.iterator(); iterator1.hasNext();) {
                 XYCoordinate xyPoint = iterator1.next();
                 KMPlotPoint kmPoint = new KMPlotPoint(xyPoint.getX(), xyPoint.getY(), xyPoint.isChecked());
                 dataPoints[i++] = kmPoint;
             }
             KMPlotPointSeriesSet groupPointSeriesSet = prepareKaplanMeierPlotPointSeriesSet
                                                      (dataPoints,groupToPlot.getGroupName(), groupToPlot.getColor());
             groupPointSeriesSet.setGroupSize(groupToPlot.getGroupSize());
             plotPointSeriesSetCollection.add(groupPointSeriesSet);
         }
         return   plotPointSeriesSetCollection;
     }
 
     public static void createLegend(JFreeChart kmPlot, Collection<KMPlotPointSeriesSet> plotPointSeriesSetCollection) {
        LegendTitle legend = kmPlot.getLegend();
        LegendItemSource[] sources = new LegendItemSource[1];
        KMLegendItemSource legendSrc = new KMLegendItemSource();
        LegendItem item ;
        for (Iterator<KMPlotPointSeriesSet> iterator = plotPointSeriesSetCollection.iterator();
             iterator.hasNext();) {
            KMPlotPointSeriesSet KMPlotPointSeriesSet = iterator.next();
            Color color = KMPlotPointSeriesSet.getColor();
            String title = KMPlotPointSeriesSet.getLegendTitle() + " (" + KMPlotPointSeriesSet.getGroupSize() + ")";
            item = new LegendItem(title, null, null, null, new Rectangle2D.Double(2,2,10,10), color);
            legendSrc.addLegendItem(item);
        }
        sources[0] = legendSrc;
        legend.setSources(sources);
    }
 
    @SuppressWarnings("unchecked")
 private static Color getKMSetColor(Collection<KMPlotPointSeriesSet> kmPlotSets, Comparable setKey, KMPlotPointSeries.SeriesType type) {
             for(KMPlotPointSeriesSet seriesSet: kmPlotSets)
                 if(seriesSet.getHashKey() == setKey ) return seriesSet.getColor();
             return KMPlotServiceImpl.DEFAULT_COLOR;
    }
 
    public static class KMLegendItemSource implements LegendItemSource {
         private LegendItemCollection items = new LegendItemCollection();
         public void addLegendItem(LegendItem item) {
           items.add(item);
         }
         public LegendItemCollection getLegendItems() {
             return items;
         }
    }
     /***************************************************************************
      * Creates two data series. One of all data points used to createPlot the step
      * graph, groupName. The second contains the census data that will be
      * overlaid onto the previous step graph to complete the KM Graph,
      * censusSeries. It takes the KaplanMeierPlotPoints and breaks them into two
      * sets of XY points based on whether the data point is checked or not. A
      * checked datapoint shows that it is actually a censor point and should be
      * placed in the scatter plot.
      *
      * @param dataPoints
      * @param groupName
      * @return populated KMPlotPointSeriesSet
      */
     KMPlotPointSeriesSet prepareKaplanMeierPlotPointSeriesSet
                                                (KMPlotPoint[] dataPoints, String groupName, Color color) {
 
         // Create the DataPoint Series
         KMPlotPointSeries dataSeries = new KMPlotPointSeries(groupName, true);
         KMPlotPointSeries censusSeries = new KMPlotPointSeries(groupName + "Censor Points ", true);
         logger.debug(groupName);
 
         for (int i = 0; i < dataPoints.length; i++) {
             logger.debug(dataPoints[i]);
             dataSeries.add(dataPoints[i], i);
             if (dataPoints[i].isChecked()) {
                 censusSeries.add(dataPoints[i]);
             }
         }
         dataSeries.setDescription(groupName);
         censusSeries.setDescription(groupName);
 
         KMPlotPointSeriesSet kmPointSet = new KMPlotPointSeriesSet();
         kmPointSet.setColor(color);
         kmPointSet.setName(groupName);
         kmPointSet.setCensorPlotPoints(censusSeries);
         kmPointSet.setProbabilityPlotPoints(dataSeries);
         kmPointSet.setLegendTitle(groupName);
         return kmPointSet;
     }
 
     @SuppressWarnings("unused")
     private static class LegendCreator {
         /**
         * converts an awt.Color to a hex color string
         * in the format: #zzzzzz for use in HTML
         * @param c
         * @return String
         */
         public static String c2hex(Color c) {
             //http://rsb.info.nih.gov/ij/developer/source/index.html
             final char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
             int i = c.getRGB();
             char[] buf7 = new char[7];
             buf7[0] = '#';
             for (int pos=6; pos>=1; pos--) {
                 buf7[pos] = hexDigits[i&0xf];
                 i >>>= 4;
             }
             return new String(buf7);
         }
     }
 }
