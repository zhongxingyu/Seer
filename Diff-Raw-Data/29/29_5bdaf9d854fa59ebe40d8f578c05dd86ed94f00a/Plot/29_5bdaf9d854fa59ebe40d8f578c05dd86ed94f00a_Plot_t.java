 package util;
 
 import java.awt.geom.Ellipse2D;
 
 import javax.swing.JFrame;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.xy.XYSplineRenderer;
 import org.jfree.data.xy.DefaultXYDataset;
 
 public class Plot {
 
 	public static final double PLOT_MARGIN = 0.1;				// percentage of absolute distance between min and max 
 																// values (MAE) that will be left blank between min and x-axis,
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public static void drawFunctions(double[][] ys)
 	{
 		String[] names = new String[ys.length];
 		for (int yIndex = 0; yIndex < ys.length; yIndex++)
 		{
 			names[yIndex] = "function" + yIndex;
 		}
 		drawFunction(ys, "window", "line chart", names, "x", "y");
 	}
 	
 	public static void drawFunction(double[][] ys, String windowName, String chartName
 			, String[] functionNames, String xAxisTitle, String yAxisTitle) {
 		DefaultXYDataset ds = new DefaultXYDataset();
 		double[][][] data = new double[ys.length][][];
 		for (int yIndex = 0; yIndex < ys.length; yIndex++)
 		{
 			data[yIndex] = new double[][]
 				{
 					buildNrOfQueriesArray(ys[yIndex].length),
 					ys[yIndex]
 				};
 			ds.addSeries(functionNames[yIndex], data[yIndex]);
 		}
 
 		JFreeChart chart = 
 				ChartFactory.createXYLineChart(chartName, xAxisTitle, yAxisTitle, ds, PlotOrientation.VERTICAL, true, true, false);
 		ChartPanel cp = new ChartPanel(chart);
 		
 //		XYSplineRenderer rend = new XYSplineRenderer();
 //		rend.setBaseShape(new Ellipse2D.Double());
 //		chart.getXYPlot().setRenderer(rend);
 		
 		double y_min = min(ys);
 		double y_max = max(ys);
 		chart.getXYPlot().getRangeAxis().setRange(
 				calcAxisMin(y_min, y_max)
 				,calcAxisMax( y_min, y_max)
 				);
 		JFrame frame = new JFrame(windowName);
 		frame.setSize(600, 400);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setVisible(true);
 		
 		frame.getContentPane().add(cp);
 	}
 	
 	private static double[] buildNrOfQueriesArray(int num)
 	{
 		double[] res = new double[num];
 		for (int i = 0; i < num; ++i)
 		{
			res[i] = i;
 		}
 		return res;
 	}
 	
 	private static double calcAxisMin(double actMin, double actMax)
 	{
 		return actMin - Math.abs(actMax - actMin) * PLOT_MARGIN;
 	}
 	
 	private static double calcAxisMax(double actMin, double actMax)
 	{
 		return actMax + Math.abs(actMax - actMin) * PLOT_MARGIN;
 	}
 	
 	private static double min(double[][] values)
 	{
 		double min = Double.MAX_VALUE;
 		double v;
 		for (double[] vs : values) 
 		{
 			v = min(vs);
 			if (v < min)
 				min = v;
 		}
 		return min;
 	}
 	
 	private static double max(double[][] values)
 	{
 		double max = Double.MIN_VALUE;
 		double v;
 		for (double[] vs : values)
 		{
 			v = max(vs);
 			if (v > max)
 				max = v;
 		}
 		return max;
 	}
 	
 	private static double min(double[] values)
 	{
 		double min = Double.MAX_VALUE;
 		for (double v : values)
 		{
 			if (min > v)
 				min = v;
 		}
 		return min;
 	}
 	
 	private static double max(double[] values)
 	{
 		double max = Double.MIN_VALUE;
 		for (double v : values)
 		{
 			if (max < v)
 				max = v;
 		}
 		return max;
 	}
 }
