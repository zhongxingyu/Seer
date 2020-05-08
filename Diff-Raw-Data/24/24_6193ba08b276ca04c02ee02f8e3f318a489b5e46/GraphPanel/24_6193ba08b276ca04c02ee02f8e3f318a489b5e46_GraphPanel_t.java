 package gui;
 
 import java.awt.BasicStroke;
 import java.awt.Paint;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.StringTokenizer;
 
 import main.ResultSaver;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.data.xy.DefaultXYDataset;
 
 import core.Constants;
 import core.Mutation;
 
 public class GraphPanel {
 	private static ChartPanel createGraph(double[][] abscissas,
 			double[][] ordinates, String title, String[] graphNames,
 			String xLabel, String yLabel, Paint[] colors, double[] width) {
 		int graphs = abscissas.length;
 		DefaultXYDataset data = new DefaultXYDataset();
 		for (int i = 0; i < graphs; ++i) {
 			data.addSeries(graphNames[i], new double[][] { abscissas[i],
 					ordinates[i] });
 		}
 		JFreeChart chart = ChartFactory.createXYLineChart(title, xLabel,
 				yLabel, data, PlotOrientation.VERTICAL, true, false, false);
 		XYItemRenderer ren = chart.getXYPlot().getRenderer();
 		for (int i = 0; i < graphs; ++i) {
 			ren.setSeriesStroke(i, new BasicStroke((float) width[i],
 					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 			ren.setSeriesPaint(i, colors[i]);
 		}
 		return new ChartPanel(chart);
 	}
 
 	private static void readMax(BufferedReader buff, double[] data)
 			throws IOException {
 		for (int i = 0; i < data.length; ++i) {
 			buff.readLine();
 			StringTokenizer st = new StringTokenizer(buff.readLine());
 			data[i] += Double.parseDouble(st.nextToken());
 			buff.readLine();
 			buff.readLine();
 		}
 	}
 
 	private static void readMean(BufferedReader buff, double[] data)
 			throws IOException {
 		for (int i = 0; i < data.length; ++i) {
 			buff.readLine();
 			buff.readLine();
 			StringTokenizer st = new StringTokenizer(buff.readLine());
 			data[i] += Double.parseDouble(st.nextToken());
 			buff.readLine();
 		}
 	}
 
 	public static ChartPanel getGraph(Mutation m, boolean maximal) {
 		final int mut = Mutation.values().length;
 		double[] prob = new double[mut];
 		final int iter = Constants.ITERATIONS;
 		final double[] probs = Constants.MUTATION_PROBABILITIES;
 		final int graphs = probs.length;
 		double[][] abscissas = new double[graphs][iter];
 		double[][] ordinates = new double[graphs][iter];
 		for (int pr = 0; pr < graphs; ++pr) {
 			prob[m.ordinal()] = probs[pr];
 			String dirName = ResultSaver.RESULTS_DIR + "/"
 					+ ResultSaver.getDirectoryName(prob) + "/";
 			File[] list = new File(dirName).listFiles();
 			for (File f : list) {
 				File full = new File(dirName + f.getName() + "/"
 						+ ResultSaver.GENERATIONS_FILENAME);
 				try {
 					BufferedReader buff = new BufferedReader(new FileReader(
 							full));
 					if (maximal) {
 						readMax(buff, ordinates[pr]);
 					} else {
 						readMean(buff, ordinates[pr]);
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			for (int i = 0; i < iter; ++i) {
 				abscissas[pr][i] = i + 1;
 				ordinates[pr][i] /= list.length;
 			}
 		}
 		String title = "Зависимость "
 				+ (maximal ? "максимального" : "среднего")
 				+ " значения fitness-функции от вероятности мутации "
 				+ m.getRuType();
 		String[] graphNames = new String[probs.length];
 		for (int i = 0; i < graphs; ++i) {
 			graphNames[i] = "вероятность мутации " + probs[i];
 		}
 		String xLabel = "Номер поколения";
 		String yLabel = "Fitness-функция - доля собранной еды";
 		Paint[] colors = Arrays.copyOf(Constants.DEFAULT_COLORS, graphs);
 		double[] width = new double[graphs];
 		Arrays.fill(width, 2.);
 		return createGraph(abscissas, ordinates, title, graphNames, xLabel,
 				yLabel, colors, width);
 	}
 }
