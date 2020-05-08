 package daisy;
 
 import java.awt.Color;
 import java.util.List;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 
 public class DaisyLineFrame extends ApplicationFrame {
 	private static final long serialVersionUID = -2796492945005823682L;
 	
 	public DaisyLineFrame(String title) {
 		super(title);
 
         final XYDataset dataset = createDataset();
         final JFreeChart chart = createChart(dataset);
         final ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new java.awt.Dimension(1200, 750));
         setContentPane(chartPanel);
 	}
 
 	
 	public DaisyLineFrame(String title, List<Integer> blackFlowerList, List<Integer> whiteFlowerList) {
 		super(title);
 
         final XYDataset dataset = setDataset(blackFlowerList, whiteFlowerList);
         final JFreeChart chart = createChart(dataset);
         final ChartPanel chartPanel = new ChartPanel(chart);
         chartPanel.setPreferredSize(new java.awt.Dimension(750, 500));
         setContentPane(chartPanel);
 	}
 	
 	public XYDataset setDataset(List<Integer> blackFlowerList, List<Integer> whiteFlowerList) {
 
         final XYSeries blackSerie = new XYSeries("Black");
         int counter = 1;
         for(Integer i : blackFlowerList) {
         	blackSerie.add(counter++, i);
         }
         
         counter = 1;
         final XYSeries whiteSerie = new XYSeries("White");
         for(Integer i : whiteFlowerList) {
        	blackSerie.add(counter++, i);
         }
 
         final XYSeriesCollection dataset = new XYSeriesCollection();
         dataset.addSeries(blackSerie);
         dataset.addSeries(whiteSerie);
                 
         return dataset;
 	}
 	
     public XYDataset createDataset() {
         
         final XYSeries series1 = new XYSeries("First");
         series1.add(1.0, 1.0);
         series1.add(2.0, 4.0);
         series1.add(3.0, 3.0);
         series1.add(4.0, 5.0);
         series1.add(5.0, 5.0);
         series1.add(6.0, 7.0);
         series1.add(7.0, 7.0);
         series1.add(8.0, 8.0);
 
         final XYSeries series2 = new XYSeries("Second");
         series2.add(1.0, 5.0);
         series2.add(2.0, 7.0);
         series2.add(3.0, 6.0);
         series2.add(4.0, 8.0);
         series2.add(5.0, 4.0);
         series2.add(6.0, 4.0);
         series2.add(7.0, 2.0);
         series2.add(8.0, 1.0);
 
         final XYSeries series3 = new XYSeries("Third");
         series3.add(3.0, 4.0);
         series3.add(4.0, 3.0);
         series3.add(5.0, 2.0);
         series3.add(6.0, 3.0);
         series3.add(7.0, 6.0);
         series3.add(8.0, 3.0);
         series3.add(9.0, 4.0);
         series3.add(10.0, 3.0);
 
         final XYSeriesCollection dataset = new XYSeriesCollection();
         dataset.addSeries(series1);
         dataset.addSeries(series2);
         dataset.addSeries(series3);
                 
         return dataset;
         
     }
     
 
     /**
      * Creates a chart.
      * 
      * @param dataset  the data for the chart.
      * 
      * @return a chart.
      */
     private JFreeChart createChart(final XYDataset dataset) {
         
         // create the chart...
         final JFreeChart chart = ChartFactory.createXYLineChart(
             "Daisy Black/White simulation",      // chart title
             "Generation",                      // x axis label
             "Population size",                      // y axis label
             dataset,                  // data
             PlotOrientation.VERTICAL,
             true,                     // include legend
             true,                     // tooltips
             false                     // urls
         );
 
         // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
         chart.setBackgroundPaint(Color.white);
 
 //        final StandardLegend legend = (StandardLegend) chart.getLegend();
   //      legend.setDisplaySeriesShapes(true);
         
         // get a reference to the plot for further customisation...
         final XYPlot plot = chart.getXYPlot();
         plot.setBackgroundPaint(Color.lightGray);
     //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
         plot.setDomainGridlinePaint(Color.white);
         plot.setRangeGridlinePaint(Color.white);
         
         final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
         renderer.setSeriesLinesVisible(0, false);
         renderer.setSeriesShapesVisible(1, false);
         plot.setRenderer(renderer);
 
         // change the auto tick unit selection to integer units only...
         final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
         rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
         // OPTIONAL CUSTOMISATION COMPLETED.
                 
         return chart;
         
     }
 
     // ****************************************************************************
     // * JFREECHART DEVELOPER GUIDE                                               *
     // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
     // * to purchase from Object Refinery Limited:                                *
     // *                                                                          *
     // * http://www.object-refinery.com/jfreechart/guide.html                     *
     // *                                                                          *
     // * Sales are used to provide funding for the JFreeChart project - please    * 
     // * support us so that we can continue developing free software.             *
     // ****************************************************************************
    
 }
