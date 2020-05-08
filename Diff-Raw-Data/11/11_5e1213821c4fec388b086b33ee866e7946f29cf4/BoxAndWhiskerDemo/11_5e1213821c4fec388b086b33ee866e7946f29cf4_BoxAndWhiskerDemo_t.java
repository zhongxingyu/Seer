 package ss.apiImpl.charts;
 
 import java.awt.Font;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.CategoryAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
 import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
 import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
 import org.jfree.ui.ApplicationFrame;
 
 import ss.apiImpl.BackupItem;
 
 import com.google.common.collect.Lists;
 
 /**
  * Demonstration of a box-and-whisker chart using a {@link CategoryPlot}.
  * 
  * @author David Browning
  */
 @SuppressWarnings("serial")
 public class BoxAndWhiskerDemo extends ApplicationFrame {
 
 	/**
 	 * Creates a new demo.
 	 * 
 	 * @param title
 	 *            the frame title.
 	 */
 	public BoxAndWhiskerDemo(
 			LinkedList<Map<String, LinkedList<BackupItem>>> backups) {
 
 		super("Resultados");
 
 		final BoxAndWhiskerCategoryDataset dataset = createSampleDataset(backups);
 
 		final CategoryAxis xAxis = new CategoryAxis("Estrategia");
 		final NumberAxis yAxis = new NumberAxis(
 				"Porcentaje de proyectos terminados");
 		yAxis.setAutoRangeIncludesZero(false);
 		final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
 		renderer.setFillBox(false);
 		// renderer.setMeanVisible(false);
 		renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
 		final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis,
 				renderer);
 
		final JFreeChart chart = new JFreeChart("Proyectos terminados",
 				new Font("SansSerif", Font.BOLD, 14), plot, true);
 		final ChartPanel chartPanel = new ChartPanel(chart);
 		chartPanel.setPreferredSize(new java.awt.Dimension(450, 270));
 		setContentPane(chartPanel);
 
 	}
 
 	/**
 	 * Creates a sample dataset.
 	 * 
 	 * @return A sample dataset.
 	 */
 	private BoxAndWhiskerCategoryDataset createSampleDataset(
 			LinkedList<Map<String, LinkedList<BackupItem>>> backupsList) {
 
 		final DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
 		int k = 1;
 		for (Map<String, LinkedList<BackupItem>> backups : backupsList) {
 
 			int i = 1;
 			for (String key : backups.keySet()) {
 				List<BackupItem> runs = backups.get(key);
 				final List<Number> list = Lists.newArrayList();
 				for (int j = 0; j < runs.size(); j++) {
 					list.add(runs.get(j).getFinishedProjects()
 							/ ((double) runs.get(j).getTotalProjects()));
 				}
 				dataset.add(list, key + " " + k, "Estrategias");
 				i++;
 			}
 			k++;
 		}
 
 		return dataset;
 	}
 
 }
