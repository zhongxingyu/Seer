 package org.bh.gui.chart;
 
 import java.util.List;
 
 import javax.swing.UIManager;
 
 import org.bh.platform.IPlatformListener;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.XYToolTipGenerator;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.StandardXYBarPainter;
 import org.jfree.chart.renderer.xy.XYBarRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public class BHXYBarChart extends BHChart implements IBHAddValue,
 		IPlatformListener {
 	
 	private XYSeriesCollection dataset;
 	
 	public BHXYBarChart(final String key){
 		super(key);
 		final ITranslator translator = Services.getTranslator();
 		this.dataset = new XYSeriesCollection(new XYSeries(translator.translate(key)));
 		chart = ChartFactory.createXYBarChart(
 				translator.translate(key),
 				translator.translate(key.concat(BHChart.DIMX)), 
 				false, 
 				translator.translate(key.concat(BHChart.DIMY)), 
 				dataset, 
 				PlotOrientation.VERTICAL, false, true, false);
 		
 		if ("Nimbus".equals(UIManager.getLookAndFeel().getName())) {
 			chart.setBackgroundPaint(UIManager.getColor("Chart.background"));
 		}
 		
 		XYBarRenderer renderer = (XYBarRenderer) chart.getXYPlot().getRenderer();		
 		renderer.setBarPainter(new StandardXYBarPainter());
 		renderer.setShadowVisible(false);
 		renderer.setDrawBarOutline(false);
 		renderer.setToolTipGenerator(new XYToolTipGenerator() {
 			
 			@Override
 			public String generateToolTip(XYDataset dataset, int series, int item) {
 				String x = Services.numberToString(dataset.getXValue(series, item));
 				String y = Services.numberToString(dataset.getYValue(series, item));
				
 				return translator.translate(key.concat(BHChart.DIMX)) + " " +  x + "; " + 
 						translator.translate(key.concat(BHChart.DIMY)) + " " +  y;
 			}
 		});
 		
 		XYBarRenderer barRenderer = (XYBarRenderer)chart.getXYPlot().getRenderer();
 		
 		barRenderer.setDrawBarOutline(false);
 		reloadText();
 		Services.addPlatformListener(this);
 	}
 
 	@Override
 	public void addSeries(Comparable<String> key, double[] values, int bins,
 			double minimum, double maximum) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void addSeries(Comparable<String> seriesKey, double[][] data, Integer amountOfValues, Integer average) {
 		XYSeries series = new XYSeries(translator.translate(seriesKey));
 		for(int i = 0; i < data.length; i++){
 			series.add(data[i][0], data[i][1]);
 		}
 		dataset.addSeries(series);
 		dataset.setIntervalWidth(0.0);
 		 	
 			final XYPlot plot = chart.getXYPlot();
 	        final NumberAxis axis2 = new NumberAxis(translator.translate("org.bh.plugin.stochasticResultAnalysis.BHStochasticResultController$ChartKeys.DISTRIBUTION_CHART.Y2"));
	        double upper = (amountOfValues.doubleValue()/average);
 	        double lower = 0.0;
 	        axis2.setRange(lower, upper);
 	        
 	        plot.setRangeAxis(1, axis2);
 	        plot.mapDatasetToRangeAxis(1, 1);
 	        
 	        final NumberAxis axis = (NumberAxis) plot.getDomainAxis();
 	        axis.setAutoRange(true);
 	     
 		chart.fireChartChanged();
 	}
 
 	@Override
 	public void addValue(Number value, Comparable<String> columnKey) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void addValue(Number value, Comparable rowKey,
 			Comparable<String> columnKey) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void addValues(List<?> list) {
 		// TODO Auto-generated method stub
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void platformEvent(PlatformEvent e) {
 	}
 
 }
