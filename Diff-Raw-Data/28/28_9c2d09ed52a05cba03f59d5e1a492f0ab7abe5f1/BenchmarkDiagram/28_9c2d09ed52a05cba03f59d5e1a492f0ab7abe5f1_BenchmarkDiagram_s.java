 package diagram;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
 
 import benchmark.BenchmarkResult;
 
 public class BenchmarkDiagram {
 
 	private final static int chartWidth = 650;
 
 	private final static int chartHeight = 450;
 
 	private final static int xAxisMaxValue = 10;
 
 	private final static int xAxisStep = 1;
 
 	private final static int yAxisMaxValue = 5000;
 
 	private final static int yAxisStep = 1000;
 
 	private final static Map<String, String> colorMapping = new HashMap<String, String>();
 
 	static {
 		colorMapping.put("Q1", "FF0000");
 		colorMapping.put("Q1.WITH", "FF0000");
 		colorMapping.put("Q2", "FFFF00");
 		colorMapping.put("Q2.WITH", "FFFF00");
 		colorMapping.put("Q3", "00FF00");
 		colorMapping.put("Q3.WITH", "00FF00");
 		colorMapping.put("Q4", "0000FF");
 		colorMapping.put("Q4.WITH", "0000FF");
 		colorMapping.put("Q5", "00FFFF");
 		colorMapping.put("Q5.WITH", "00FFFF");
 		colorMapping.put("Q6", "FF00FF");
 		colorMapping.put("Q6.WITH", "FF00FF");
 		colorMapping.put("Q7", "000000");
 		colorMapping.put("Q7.WITH", "CCCCCC");
 	}
 
 	private Map<Integer, BenchmarkResult> results;
 
 	public BenchmarkDiagram() {
 		results = new HashMap<Integer, BenchmarkResult>();
 	}
 
 	public String generateGoogleCharApiUrl() {
 
 		if (results.keySet().isEmpty() == false) {
 
 			String data = "";
 
 			List<String> usedQueries = results.get(1).getQueries();
 
 			String scales = "";
 			String labels = "";
 			String colors = "";
 			
			SortedSet<Integer> sortedSet = new TreeSet<Integer>(results.keySet());
 			
 			for (String query : usedQueries) {
 				labels += query + "|";
 				colors += colorMapping.get(query) + ",";
 
 				String xValues = "";
				for (Integer x : results.keySet()) {
 					xValues += x + ",";
 				}
 				xValues = xValues.substring(0, xValues.length() - 1);
 
 				String yValues = "";
				for (Integer x : (Integer[]) sortedSet.toArray()) {
 					BenchmarkResult result = results.get(x);
 					yValues += result.getAverageResponseTimeForQuery(query) + ",";
 				}
 				yValues = yValues.substring(0, yValues.length() - 1);
 				data += xValues + "|" + yValues + "|";
 				scales += "1," + xAxisMaxValue + ",0," + yAxisMaxValue + ",";
 			}
 			labels = labels.substring(0, labels.length() - 1);
 			colors = colors.substring(0, colors.length() - 1);
 			scales = scales.substring(0, scales.length() - 1);
 			data = data.substring(0, data.length() - 1);
 
 			String chartUrl = "http://chart.apis.google.com/chart";
 			chartUrl += "?cht=lxy"; // chart type
 			chartUrl += "&chs=" + chartWidth + "x" + chartHeight;
 			chartUrl += "&chxt=x,y"; // display axis values
 			chartUrl += "&chxr=0,1," + xAxisMaxValue + "," + xAxisStep + "|1,0," + yAxisMaxValue + "," + yAxisStep; // ranges
 																																										// steps
 			chartUrl += "&chdl=" + labels;
 			chartUrl += "&chco=" + colors;
 			chartUrl += "&chds=" + scales;
 			chartUrl += "&chd=t:" + data;
 
 			return chartUrl;
 		}
 
 		return null;
 	}
 
 	public void addBenchmark(int clients, BenchmarkResult benchmarkResult) {
 		results.put(clients, benchmarkResult);
 	}
 
 }
