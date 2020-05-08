 package org.computer.knauss.reqtDiscussion.ui.metrics;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.event.ListDataListener;
 
 import org.computer.knauss.reqtDiscussion.model.metric.AbstractDiscussionMetric;
 import org.computer.knauss.reqtDiscussion.model.metric.PatternMetric;
 import org.computer.knauss.reqtDiscussion.ui.ctrl.ExportTableCommand;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 public class MetricFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private MetricTable metricTable;
 	private JComboBox metric2Box;
 	private JComboBox metric1Box;
 	private JTabbedPane tabbedPane;
 
 	public MetricFrame() {
 		super("Metrics");
 		setLayout(new BorderLayout());
 
 		tabbedPane = new JTabbedPane();
 		add(tabbedPane, BorderLayout.CENTER);
 
 		this.metricTable = new MetricTable();
 		tabbedPane.addTab("Table",
 				new JScrollPane(new JTable(this.metricTable)));
 
 		JPanel configPanel = new JPanel(new GridLayout(5, 1));
 		JPanel tmp = new JPanel();
 		tmp.add(configPanel);
 		add(tmp, BorderLayout.EAST);
 
 		metric1Box = new JComboBox(new MetricComboboxModel(
 				this.metricTable.getMetrics()));
 		metric2Box = new JComboBox(new MetricComboboxModel(
 				this.metricTable.getMetrics()));
 		JButton createPlotBtn = new JButton("Create plot");
 		createPlotBtn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				AbstractDiscussionMetric metric1 = (AbstractDiscussionMetric) metricTable
 						.getMetrics()[metric1Box.getSelectedIndex()];
 				AbstractDiscussionMetric metric2 = (AbstractDiscussionMetric) metricTable
 						.getMetrics()[metric2Box.getSelectedIndex()];
 
 				JFreeChart chart = null;
 				if (metric1.getName().equals("Pattern"))
 					chart = createBoxPlot(metric1, metric2);
 				else
					chart = createScatterPlot(metric1, metric2);
 				tabbedPane.addTab(metric1.getName() + "/" + metric2.getName(),
 						new ChartPanel(chart));
 
 			}
 		});
 
 		configPanel.add(metric1Box);
 		configPanel.add(metric2Box);
 		configPanel.add(createPlotBtn);
 
 		configPanel.add(new JPanel());
 		ExportTableCommand etc = new ExportTableCommand();
 		etc.setTableModel(this.metricTable);
 		JButton exportButton = new JButton(etc);
 		configPanel.add(exportButton);
 		pack();
 	}
 
 	public void update() {
 		this.metricTable.fireTableChanged();
 	}
 
 	private JFreeChart createScatterPlot(AbstractDiscussionMetric metric1,
 			AbstractDiscussionMetric metric2) {
 		XYSeriesCollection result = new XYSeriesCollection();
 
 		XYSeries series = new XYSeries(metric1.getName() + "/"
 				+ metric2.getName());
 		for (int i = 0; i < metricTable.getRowCount(); i++) {
 			double x = metric1.getResults().get(metricTable.getValueAt(i, 0));
 			double y = metric2.getResults().get(metricTable.getValueAt(i, 0));
 			series.add(x, y);
 		}
 		result.addSeries(series);
 
 		return ChartFactory.createScatterPlot("Scatter Plot", // chart
 				// title
 				metric1.getName(), // x axis label
 				metric2.getName(), // y axis label
 				result, // data ***-----PROBLEM------***
 				PlotOrientation.VERTICAL, true, // include legend
 				true, // tooltips
 				false // urls
 				);
 	}
 
 	private JFreeChart createBoxPlot(AbstractDiscussionMetric metric1,
 			AbstractDiscussionMetric metric2) {
 		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
 
 		// Assume metric 1 are the patterns
 		for (int i = -1; i < 6; i++) {
 			String patternName = "unknown";
 			if (i >= 0)
 				patternName = PatternMetric.PATTERNS[i].getName();
 			List<Double> values = new LinkedList<Double>();
 			for (int row = 0; row < metricTable.getRowCount(); row++) {
 				double x = metric1.getResults().get(
 						metricTable.getValueAt(row, 0));
 				double y = metric2.getResults().get(
 						metricTable.getValueAt(row, 0));
 				if (x == i)
 					values.add(y);
 			}
 			dataset.add(values, metric2.getName(), patternName);
 
 		}
 
 		// Create the chart
 		final CategoryAxis xAxis = new CategoryAxis("Pattern");
 		final NumberAxis yAxis = new NumberAxis(metric2.getName());
 		yAxis.setAutoRangeIncludesZero(false);
 		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
 		// renderer.setFillBox(false);
 		renderer.setBaseToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
 		renderer.setSeriesToolTipGenerator(0,
 				new BoxAndWhiskerToolTipGenerator());
 		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis,
 				renderer);
 
 		return new JFreeChart("Box-and-Whisker Demo", new Font("SansSerif",
 				Font.BOLD, 14), plot, true);
 	}
 
 	public static void main(String[] args) {
 		MetricFrame mf = new MetricFrame();
 		mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mf.setVisible(true);
 	}
 
 	private class MetricComboboxModel implements ComboBoxModel {
 
 		private AbstractDiscussionMetric[] metrics;
 		private List<ListDataListener> listeners = new LinkedList<ListDataListener>();
 		private Object selectedItem;
 
 		MetricComboboxModel(AbstractDiscussionMetric[] metrics) {
 			this.metrics = metrics;
 		}
 
 		@Override
 		public void addListDataListener(ListDataListener l) {
 			this.listeners.add(l);
 		}
 
 		@Override
 		public Object getElementAt(int index) {
 			return metrics[index].getName();
 		}
 
 		@Override
 		public int getSize() {
 			return metrics.length;
 		}
 
 		@Override
 		public void removeListDataListener(ListDataListener l) {
 			listeners.remove(l);
 		}
 
 		@Override
 		public Object getSelectedItem() {
 			return this.selectedItem;
 		}
 
 		@Override
 		public void setSelectedItem(Object anItem) {
 			this.selectedItem = anItem;
 		}
 
 	}
 }
