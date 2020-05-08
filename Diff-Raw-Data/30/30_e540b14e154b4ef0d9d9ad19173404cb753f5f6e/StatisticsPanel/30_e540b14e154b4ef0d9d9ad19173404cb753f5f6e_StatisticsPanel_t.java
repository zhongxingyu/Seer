 package net.sprauer.sitzplaner.view.panels;
 
import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import net.sprauer.sitzplaner.EA.Configuration;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public class StatisticsPanel extends JPanel {
 	private static final long serialVersionUID = -5215412009575339154L;
 
 	private class Series {
 		public Series(int index) {
 			best = new XYSeries("best Fitness " + index);
 			worst = new XYSeries("worst Fitness " + index);
 		}
 
 		XYSeries best;
 		XYSeries worst;
 	}
 
 	private static StatisticsPanel _instance;
 	private final JFreeChart chart;
 	private final ChartPanel chartPanel;
 	private final Map<Configuration, Series> seriesMap = new HashMap<Configuration, Series>();
 	private final JTextField txtBestFitness;
 	private final XYSeriesCollection seriesCollection;
 	private final JLabel lblWorstFitness;
 	private final JTextField txtWorstFitness;
 	private final JLabel lblDelta;
 	private final JTextField txtDelta;
 	private final JLabel lblWorstFitness_1;
 	private final JTextField txtCurrentFitness;
 
 	public StatisticsPanel() {
 		_instance = this;
 		seriesCollection = new XYSeriesCollection();
 		chart = createChart(seriesCollection);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 78, 80, 100, 80, 60, 80, 85, 80, 0 };
 		gridBagLayout.rowHeights = new int[] { 0, 218, 0 };
 		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
 		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		lblWorstFitness = new JLabel("Current Fitness:");
 		GridBagConstraints gbc_lblWorstFitness = new GridBagConstraints();
 		gbc_lblWorstFitness.anchor = GridBagConstraints.EAST;
 		gbc_lblWorstFitness.insets = new Insets(0, 0, 5, 5);
 		gbc_lblWorstFitness.gridx = 0;
 		gbc_lblWorstFitness.gridy = 0;
 		add(lblWorstFitness, gbc_lblWorstFitness);
 
 		txtCurrentFitness = new JTextField();
 		txtCurrentFitness.setFont(new Font("Tahoma", Font.BOLD, 11));
 		txtCurrentFitness.setEditable(false);
 		txtCurrentFitness.setText("0.0");
 		GridBagConstraints gbc_txtCurrentFitness = new GridBagConstraints();
 		gbc_txtCurrentFitness.insets = new Insets(0, 0, 5, 5);
 		gbc_txtCurrentFitness.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtCurrentFitness.gridx = 1;
 		gbc_txtCurrentFitness.gridy = 0;
 		add(txtCurrentFitness, gbc_txtCurrentFitness);
 		txtCurrentFitness.setColumns(10);
 
 		lblWorstFitness_1 = new JLabel("Worst Fitness:");
 		GridBagConstraints gbc_lblWorstFitness_1 = new GridBagConstraints();
 		gbc_lblWorstFitness_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblWorstFitness_1.anchor = GridBagConstraints.EAST;
 		gbc_lblWorstFitness_1.gridx = 2;
 		gbc_lblWorstFitness_1.gridy = 0;
 		add(lblWorstFitness_1, gbc_lblWorstFitness_1);
 
 		txtWorstFitness = new JTextField();
 		txtWorstFitness.setText("0.0");
 		txtWorstFitness.setEditable(false);
 		GridBagConstraints gbc_txtWorstFitness = new GridBagConstraints();
 		gbc_txtWorstFitness.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtWorstFitness.insets = new Insets(0, 0, 5, 5);
 		gbc_txtWorstFitness.gridx = 3;
 		gbc_txtWorstFitness.gridy = 0;
 		add(txtWorstFitness, gbc_txtWorstFitness);
 		txtWorstFitness.setColumns(10);
 
 		lblDelta = new JLabel("Delta:");
 		GridBagConstraints gbc_lblDelta = new GridBagConstraints();
 		gbc_lblDelta.anchor = GridBagConstraints.EAST;
 		gbc_lblDelta.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDelta.gridx = 4;
 		gbc_lblDelta.gridy = 0;
 		add(lblDelta, gbc_lblDelta);
 
 		txtDelta = new JTextField();
 		txtDelta.setEditable(false);
 		txtDelta.setText("0.0");
 		GridBagConstraints gbc_txtDelta = new GridBagConstraints();
 		gbc_txtDelta.insets = new Insets(0, 0, 5, 5);
 		gbc_txtDelta.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtDelta.gridx = 5;
 		gbc_txtDelta.gridy = 0;
 		add(txtDelta, gbc_txtDelta);
 		txtDelta.setColumns(10);
 
 		JLabel lblCurrentFitness = new JLabel("Best Fitness:");
 		GridBagConstraints gbc_lblCurrentFitness = new GridBagConstraints();
 		gbc_lblCurrentFitness.anchor = GridBagConstraints.EAST;
 		gbc_lblCurrentFitness.ipady = 5;
 		gbc_lblCurrentFitness.ipadx = 5;
 		gbc_lblCurrentFitness.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCurrentFitness.gridx = 6;
 		gbc_lblCurrentFitness.gridy = 0;
 		add(lblCurrentFitness, gbc_lblCurrentFitness);
 
 		txtBestFitness = new JTextField();
 		txtBestFitness.setFont(new Font("Tahoma", Font.BOLD, 11));
 		txtBestFitness.setEditable(false);
 		txtBestFitness.setText("0.0");
 		GridBagConstraints gbc_fitnessValue = new GridBagConstraints();
 		gbc_fitnessValue.ipady = 5;
 		gbc_fitnessValue.ipadx = 5;
 		gbc_fitnessValue.fill = GridBagConstraints.HORIZONTAL;
 		gbc_fitnessValue.insets = new Insets(0, 0, 5, 0);
 		gbc_fitnessValue.gridx = 7;
 		gbc_fitnessValue.gridy = 0;
 		add(txtBestFitness, gbc_fitnessValue);
 		txtBestFitness.setColumns(10);
 		chartPanel = new ChartPanel(chart);
 		chartPanel.setPreferredSize(new java.awt.Dimension(800, 200));
 		GridBagConstraints gbc_chartPanel = new GridBagConstraints();
 		gbc_chartPanel.weighty = 1.0;
 		gbc_chartPanel.weightx = 1.0;
 		gbc_chartPanel.fill = GridBagConstraints.BOTH;
 		gbc_chartPanel.gridwidth = 8;
 		gbc_chartPanel.gridx = 0;
 		gbc_chartPanel.gridy = 1;
 		add(chartPanel, gbc_chartPanel);
 	}
 
 	public static StatisticsPanel instance() {
 		return _instance;
 	}
 
 	private JFreeChart createChart(XYSeriesCollection dataset) {
 		JFreeChart chart = ChartFactory.createXYLineChart("", // title
 				"Generation", // x axis label
 				"Fitness", // y axis label
 				dataset, // data
 				PlotOrientation.VERTICAL, false, // include legend
 				true, // tooltips
 				false // urls
 				);
 		chart.setBackgroundPaint(Color.white);
 
 		XYPlot plot = chart.getXYPlot();
 		plot.setBackgroundPaint(Color.lightGray);
 		plot.setDomainGridlinePaint(Color.white);
 		plot.setRangeGridlinePaint(Color.white);
 
 		plot.setDomainCrosshairVisible(true);
 		plot.setRangeCrosshairVisible(true);
 
 		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
 		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
 		return chart;
 
 	}
 
 	public void addFitness(Configuration conf, double best, double worst) {
 		txtBestFitness.setText("" + best);
 		txtWorstFitness.setText("" + worst);
 		txtDelta.setText("" + (best - worst));
 		Series series = seriesMap.get(conf);
 		if (series == null) {
			series = new Series(seriesCollection.getSeriesCount());
 			seriesCollection.addSeries(series.best);
 			seriesCollection.addSeries(series.worst);
			XYItemRenderer renderer = chart.getXYPlot().getRenderer();
			float[] dash = {10.0f};
			int worstIndex = seriesCollection.getSeriesIndex(series.worst.getKey());
			renderer.setSeriesPaint(seriesCollection.getSeriesIndex(series.best.getKey()), conf.getColor());
			renderer.setSeriesPaint(worstIndex, conf.getColor());
			renderer.setSeriesStroke(worstIndex, new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, dash, 3.0f) );
 			seriesMap.put(conf, series);
 		}
 		if (series.best.getItemCount() == 0) {
 			series.best.add(1, 0);
 			series.worst.add(1, 0);
 		} else {
 			series.best.add(series.best.getMaxX() + 1, best);
 			series.worst.add(series.worst.getMaxX() + 1, worst);
 		}
 	}
 
 	public void clear() {
 		seriesMap.clear();
 		seriesCollection.removeAllSeries();
 		txtBestFitness.setText("0.0");
		txtWorstFitness.setText("0.0");
		txtDelta.setText("0.0");
		txtCurrentFitness.setText("0.0");
 		validate();
 	}
 
 	public void setCurrentFitness(double fitness) {
 		txtCurrentFitness.setText("" + fitness);
 	}
 }
