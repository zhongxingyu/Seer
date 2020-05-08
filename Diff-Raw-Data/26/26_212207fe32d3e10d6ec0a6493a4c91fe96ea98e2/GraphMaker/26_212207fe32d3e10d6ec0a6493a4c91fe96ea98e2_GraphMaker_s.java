 package graph;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.Collection;
 import java.util.List;
 
import javax.swing.JFrame;

 import org.apache.commons.io.IOUtils;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.LegendItem;
 import org.jfree.chart.LegendItemCollection;
 import org.jfree.chart.LegendItemSource;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.labels.StandardXYItemLabelGenerator;
 import org.jfree.chart.labels.XYItemLabelGenerator;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.chart.title.LegendTitle;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.RefineryUtilities;
 import org.jfree.util.ShapeUtilities;
 
 import util.ThreadUtil;
 import exec.CSMADemo;
 
/*
 * changed from ApplicationFrame as closing graphs used to terminate application. But Now have to use
 * System.exit(0); at main end to terminate application
 */
public class GraphMaker extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private String _type;
 
 	public GraphMaker(String iTitle, String iXTitle, String iYTitle, Collection<List<XYSeries>> iCollection, double iUpperXRange) {
 		super(iTitle);
 		if (iTitle.startsWith("1-P")) {
 			_type = "1p";
 		} else if (iTitle.startsWith("P-P")) {
 			_type = "pp";
 		} else {
 			_type = "np";
 		}
 		// X-axis
 		NumberAxis rangeAxis = new NumberAxis(iXTitle);
 		rangeAxis.setRange(-1, iUpperXRange);
 
 		// Y-axis
 		NumberAxis domainAxis = new NumberAxis(iYTitle);
 		// domainAxis.setRange(0, 2000);
 		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
 		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
 		NumberFormat format = NumberFormat.getNumberInstance();
 		format.setMaximumFractionDigits(2); // etc.
 		// {0} {1} {2} = Thread ID - x axis - y axis
 		XYItemLabelGenerator generator = new StandardXYItemLabelGenerator("{1}", format, format);
 		renderer.setBaseItemLabelGenerator(generator);
 		renderer.setBaseItemLabelsVisible(true);
 
 		XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
 
 		XYSeriesCollection aXYSeriesCollection = new XYSeriesCollection();
 		int i = 0;
 		for (List<XYSeries> aXYSeries : iCollection) {
 			aXYSeriesCollection.addSeries(aXYSeries.get(0));
 			renderer.setSeriesLinesVisible(i, true);
 			// shows points for every read on the line
 			renderer.setSeriesShapesVisible(i, true);
 			renderer.setSeriesShape(i, ShapeUtilities.createDiamond(4));
 			renderer.setSeriesStroke(i, new BasicStroke(2));
 			renderer.setSeriesPaint(i, Color.GREEN);
 			// if (i > 0)
 			// renderer.setSeriesVisibleInLegend(i, false);
 			i++;
 		}
 		i = 0;
 		XYSeriesCollection aXYSeriesCollection1 = new XYSeriesCollection();
 		for (List<XYSeries> aXYSeries : iCollection) {
 			aXYSeriesCollection1.addSeries(aXYSeries.get(1));
 			renderer1.setSeriesLinesVisible(i, true);
 			// shows points for every read on the line
 			renderer1.setSeriesShapesVisible(i, true);
 			// if (i > 0)
 			// renderer1.setSeriesVisibleInLegend(i, false);
 			renderer1.setSeriesStroke(i, new BasicStroke(2));
 			renderer1.setSeriesPaint(i, Color.RED);
 			renderer1.setSeriesShape(i, ShapeUtilities.createDiamond(4));
 			i++;
 		}
 		XYPlot plot = new XYPlot(null, rangeAxis, domainAxis, renderer);
 		plot.setDataset(0, aXYSeriesCollection);
 		plot.setRenderer(0, renderer);
 		plot.setDataset(1, aXYSeriesCollection1);
 		plot.setRenderer(1, renderer1);
 		plot.setOrientation(PlotOrientation.VERTICAL);
 
 		// reverse order of bottom legend to put it in ascending order
 		LegendItemCollection legendItemsOld = plot.getLegendItems();
 		final LegendItemCollection legendItemsNew = new LegendItemCollection();
 		// System.out.println(legendItemsOld.getItemCount());
 
 		for (i = 0; i < legendItemsOld.getItemCount(); i++) {
 			LegendItem aLegendItem = legendItemsOld.get(i);
 			// System.out.println(aLegendItem.getSeriesKey() + "," + aLegendItem.getSeriesIndex());
 			if (aLegendItem.getSeriesKey().equals(ThreadUtil.THREAD_WAIT_GRAPH_TITLE) || aLegendItem.getSeriesKey().equals(ThreadUtil.THREAD_RUN_GRAPH_TITLE))
 				legendItemsNew.add(aLegendItem);
 		}
 
 		plot.setFixedLegendItems(legendItemsNew);
 
 		JFreeChart chart = new JFreeChart(iTitle, plot);
 		/*
 		 * chartClone used for writeScaledChartAsPNG purpose to avoid a bug duplicating Legend info and sometimes throwing below exception
 		 * 
 		 * Exception in thread "AWT-EventQueue-0" java.lang.ArrayIndexOutOfBoundsException: 3 at org.jfree.chart.block.FlowArrangement.arrangeNN(FlowArrangement.java:365)
 		 */
 		JFreeChart chartClone = new JFreeChart(iTitle, plot);
 		LegendItemSource source = new LegendItemSource() {
 
 			public LegendItemCollection getLegendItems() {
 				LegendItemCollection lic = new LegendItemCollection();
 				lic.add(new LegendItem("Clients=" + CSMADemo.THREAD_COUNT));
 				lic.add(new LegendItem("Channel Use Time (ms)=" + CSMADemo.CHANNEL_USE_PERIOD));
 				lic.add(new LegendItem("Client Arrival Interval (ms)=" + CSMADemo.THREAD_START_INTERVAL));
 				if (_type.equals("1p")) {
 					//
 				} else if (_type.equals("pp")) {
 					lic.add(new LegendItem("Unit Wait Time (ms)=" + CSMADemo.PP_WAIT_UNIT_TIME));
 					lic.add(new LegendItem("Probability=" + CSMADemo.PP_PROB));
 				} else {
 					lic.add(new LegendItem("Max Random Wait Time (ms)=" + CSMADemo.NP_MAX_RANDOM_WAIT));
 				}
 				return lic;
 			}
 		};
 
 		chart.addLegend(new LegendTitle(source));
 		chartClone.addLegend(new LegendTitle(source));
 		ChartPanel chartPanel = new ChartPanel(chart);
 		// List<Title> subTitles = new ArrayList<Title>();
 		// subTitles.add(new TextTitle("SubTitle 1"));
 		// subTitles.add(new TextTitle("SubTitle 2"));
 		// chart.setSubtitles(subTitles);
 		chartPanel.setPreferredSize(new Dimension(1800, 900));
 		setContentPane(chartPanel);
 		pack();
 		RefineryUtilities.centerFrameOnScreen(this);
 		setVisible(true);
 		saveChartAsPNG(chartClone);
 	}
 
 	private void saveChartAsPNG(JFreeChart iJFreechart) {
 		int width = 1920, height = 1080;
 		// basically multiplies width n height by scaleFactor int value, and provides high resolution pics
 		int scaleFactor = 3;
 		String imageType = "png";
 		String name = System.getProperty("user.home") + "/" + (_type + CSMADemo.THREAD_COUNT) + "." + imageType;
 		FileOutputStream fos = null;
 		File file = null;
 		try {
 			fos = new FileOutputStream(name);
 			ChartUtilities.writeScaledChartAsPNG(fos, iJFreechart, width, height, scaleFactor, scaleFactor);
 			file = new File(name);
 			System.out.println(imageType + " image (" + (width * scaleFactor) + "x" + (height * scaleFactor) + ") exported to=" + name + "  ,,,  size(kB)=" + (float) file.length()
 					/ 1024);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			IOUtils.closeQuietly(fos);
 		}
 	}
 }
