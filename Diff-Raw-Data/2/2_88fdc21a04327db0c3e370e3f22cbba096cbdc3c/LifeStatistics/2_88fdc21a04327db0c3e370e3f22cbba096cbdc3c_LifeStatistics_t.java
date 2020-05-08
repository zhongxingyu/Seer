 package edu.agh.tunev.statistics;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public final class LifeStatistics implements Statistics {
 
 	private JFreeChart chart;
 	private XYSeries killed, alive, rescued;
 
 	public LifeStatistics() {
 		killed = new XYSeries("killed");
 		alive = new XYSeries("alive");
 		rescued = new XYSeries("rescued");
 
 		XYSeriesCollection dataset = new XYSeriesCollection();
 		dataset.addSeries(killed);
 		dataset.addSeries(alive);
 		dataset.addSeries(rescued);
 
 		chart = ChartFactory.createXYLineChart(getTitle(), "Time [t]",
 				"Num. people", dataset, PlotOrientation.VERTICAL, true, true,
 				false);
 	}
 
 	@Override
 	public String getTitle() {
 		return "Life statistics";
 	}
 
 	@Override
 	public JFreeChart getChart() {
 		return chart;
 	}
 
 	public void add(double time, int numAlive, int numRescued, int numDead) {
 		killed.add(time, numDead);
		alive.add(time, numAlive);
		rescued.add(time, numRescued);
 	}
 
 }
