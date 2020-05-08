 import java.util.Calendar;
 import java.util.Random;
 import java.util.TimeZone;
 
 import flotjf.Chart;
 import flotjf.data.PlotData;
 import flotjf.options.Axis;
 
 
 public class ExampleGraph4 {
 
 	public static String getGraphOptions() {
 		Chart chart = new Chart();
 	
 		Axis yAxis = new Axis();
 		yAxis.setMax(550L);
 		chart.addYAxis(yAxis);
 
 		Axis xAxis = new Axis();
 		xAxis.setMode("time");
 		chart.addXAxis(xAxis);
 	
 		return chart.printChartOptions();
 	}
 	
 	public static String getGraph() {
 		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+0000"));
 		Calendar localCal = Calendar.getInstance();
		// Timespan in seconds
 		Integer timeSpan = 300;
 
 		Long endTime = cal.getTimeInMillis();
 		// Adjust endTime
 		endTime += localCal.getTimeZone().getOffset(cal.getTimeInMillis());
 		Long startTime = endTime-(timeSpan*1000);
 
 		PlotData sinPlot = new PlotData("rand(x)", null);
 
 		// Create new random generator using seed 1
 		Random rand = new Random(1);
 		// Create sequence based on time to be used to retrieve numbers from random seed
 		int startIntSeed = (int)Math.floor((startTime % 10000000) / 1000);
 		// Forward to the current next random number
 		for (int seq = 0; seq < startIntSeed; seq++) {
 			rand.nextInt(500);
 		}
 
 		// Add marker every 2 sec between startTime and endTime
 		for (int loop = 0; loop < timeSpan; loop+=2) {
 			sinPlot.addPoint(startTime+(loop*1000), rand.nextInt(500));
 		}
 		System.out.println();
 		Chart chart = new Chart();
 		chart.addElements(sinPlot);
 
 		return chart.printChart();
 	}
 
 }
