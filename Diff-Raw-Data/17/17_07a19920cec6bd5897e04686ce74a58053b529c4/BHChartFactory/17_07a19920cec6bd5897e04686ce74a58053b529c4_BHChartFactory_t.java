 package org.bh.gui.chart;
 
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.data.category.DefaultCategoryDataset;
 import org.jfree.data.general.Dataset;
 import org.jfree.data.general.DefaultPieDataset;
 import org.jfree.data.statistics.HistogramDataset;
 import org.jfree.data.xy.DefaultXYDataset;
 
 /**
  * 
  * BHChartFactory class to create the charts
  * 
  * 
  * @author Lars
  * @version 0.1, 16.12.2009
  *
  * @author Marco Hammel
  * @version 0.2 11.01.2010
  * 
  */
 public class BHChartFactory {
 
     static ITranslator translator = Services.getTranslator();
 
     /**
      * Method to create a LineChart
      *
      * @param key
      * @return created LineChart
      */
     public static BHChartPanel getLineChart(final Object key) {
 
         BHLineChart chart = new BHLineChart(dimDataset(getAxes(key)[0], getAxes(key)[1]), key.toString());
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
 
     //TODO Lars.Zuckschwerdt JavaDoc
     /**
      *
      * @param key
      * @return
      */
     public static BHChartPanel getWaterfallChart( final Object key, boolean legend, boolean tooltips) {
 
         BHwaterfallChart chart = new BHwaterfallChart(dimDataset(getAxes(key)[0], getAxes(key)[1]), key.toString(), legend, tooltips);
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
     //TODO Lars.Zuckschwerdt JavaDoc
 
     /**
      *
      * @param key
      * @return
      */
     public static BHChartPanel getBarChart( final Object key, boolean legend, boolean tooltips) {
 
         BHBarChart chart = new BHBarChart(key.toString(), dimDataset(getAxes(key)[0], getAxes(key)[1]), legend, tooltips);
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
 
     //TODO Lars.Zuckschwerdt JavaDoc
     /**
      *
      * @param key
      * @return
      */
     public static BHChartPanel getStackedBarChart( final Object key, boolean legend, boolean tooltips) {
 
         BHstackedBarChart chart = new BHstackedBarChart(dimDataset(getAxes(key)[0], getAxes(key)[1]), key.toString(), legend, tooltips);
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
 
     /**
      * method to create the <code>BHpieChart</code>
      *
      * @param key
      * 		<code>String</code> key
      * @return
      */
     public static BHChartPanel getPieChart( final Object key) {
 
         BHPieChart chart = new BHPieChart(dimDataset(), key.toString());
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
 
     /**
      * method to create the <code>BHxyAreaChart</code>
      *
      * @param seriesKey
      * @param data
      * @param key
      *            <code>String</code> key
      * @return
      *
      */
     public static BHChartPanel getXYAreaChart( final String seriesKey, final double[][] data, final Object key) {
 
         BHxyAreaChart chart = new BHxyAreaChart(
                 dimDataset(seriesKey, data), key.toString());
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
     
     public static BHChartPanel getXYBarChart(Object key) {
 
        BHXYBarChart chart = new BHXYBarChart(key.toString());   	
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
 
     /**
      * method to create the <code>BHHistogramChart</code>
      *
      * @param datasetKey
      * @param values
      * @param bins
      * @param minimum
      * @param maximum
      * @param key
      *            <code>String</code> key
      * @return
      *
      */
     public static BHChartPanel getHistogramChart(final Object key) {
 
 //        BHHistogramChart chart = new BHHistogramChart(
 //                dimDataset(datasetKey, values, bins, minimum, maximum), key.toString());
         
         BHHistogramChart chart = new BHHistogramChart(histogrammDataset(), key.toString());
         
         return new BHChartPanel(key, chart.getChart(), chart.getClass(), chart);
     }
 
 //    public static BHChartPanel getStochasticChart(final String datasetKey, final double[] values, final int bins,
 //            final double minimum, final double maximum, final Object key){
 //        //TODO Marco define Stochastic Chart
 //
 //    } 
 
     /**
      * method to create a empty DefaultCategoryDataset
      *
      * @param column
      *            String column
      * @param row
      *            String row
      * @param type
      *            Class type
      * @return DefaultCategoryDataset dataset
      */
     private static Dataset dimDataset(final Comparable<String> column,
             final Comparable<String> row) {
 
         DefaultCategoryDataset dataset = new DefaultCategoryDataset();
         //dataset.addValue(null, row, column);
         return dataset;
     }
 
     /**
      * method to create a empty DefaultXYDataset
      *
      * @param seriesKey
      *            String seriesKey
      * @param data
      *            double[][] data
      * @return DefaultXYDataset dataset
      */
     private static Dataset dimDataset(final java.lang.Comparable<String> seriesKey,
             final double[][] data) {
 
         DefaultXYDataset dataset = new DefaultXYDataset();
         //dataset.addSeries(seriesKey, data);
         return dataset;
     }
 
     /**
      * method to create a empty HistogramDataset
      *
      * @param key
      *            String key
      * @param values
      *            double[] values
      * @param bins
      *            int bins
      * @param minimum
      *            double minimum
      * @param maximum
      *            double maximum
      * @return HistogramDataset dataset
      */
     private static Dataset dimDataset(final java.lang.Comparable<String> key,
             final double[] values, final int bins, final double minimum, final double maximum) {
 
         HistogramDataset dataset = new HistogramDataset();
         //dataset.addSeries(key, values, bins, minimum, maximum);
         return dataset;
     }
     
     private static Dataset histogrammDataset(){
     	return new HistogramDataset();
     }
 
     private static Dataset dimDataset() {
         DefaultPieDataset dataset = new DefaultPieDataset();
 
         return dataset;
     }
 
     private static String[] getAxes(Object key) {
         String[] axes = new String[2];
         axes[0] = translator.translate(key.toString().concat(BHChart.DIMX));
         axes[1] = translator.translate(key.toString().concat(BHChart.DIMY));
         return axes;
     }
 }
