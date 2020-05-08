 package ch.zhaw.dynsys.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.util.Calendar;
 import java.util.Collection;
 
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.SoftBevelBorder;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.DateAxis;
 import org.jfree.chart.plot.DefaultDrawingSupplier;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 
 import ch.zhaw.dynsys.simulation.Culture;
 
 public class GraphPanel extends ChartPanel implements
 		ch.zhaw.dynsys.simulation.Simulation.Listener {
 
 	public static Color[] COLOR_SEQUENCE = new Color[] { Color.RED, Color.BLUE,
 			Color.ORANGE, Color.PINK, Color.GREEN, Color.CYAN, Color.DARK_GRAY,
 			Color.BLACK, Color.YELLOW };
 
 	private static final int LASTEST_TIME_FRAME = 60000;
 	private static final long serialVersionUID = 1L;
 
 	private TimeSeriesCollection datasets = new TimeSeriesCollection();
 	private JFreeChart chart;
 	private XYPlot plot;
 	private DateAxis domainAxis;
 
 	private GraphValuesPanel graphValuesPanel;
 
 	public GraphPanel(JPanel glassPane) {
 		super(null);
 
 		setPreferredSize(new Dimension(800, 600));
 
 		// create values panel and add to frames glasspane
 		graphValuesPanel = new GraphValuesPanel();
 		SpringLayout springLayout = new SpringLayout();
 		glassPane.setLayout(springLayout);
 		glassPane.add(graphValuesPanel);
 		springLayout.putConstraint(SpringLayout.NORTH, graphValuesPanel, 400,
 				SpringLayout.NORTH, this);
 		springLayout.putConstraint(SpringLayout.WEST, graphValuesPanel, 20,
 				SpringLayout.WEST, this);
 
 		// chart
 		chart = ChartFactory.createTimeSeriesChart(null, null, null, datasets,
 				false, false, false);
 
 		// plot
 		plot = (XYPlot) chart.getPlot();
 
 		// colors
 		plot.setDrawingSupplier(new DefaultDrawingSupplier(COLOR_SEQUENCE,
 				DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
 				DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
 				DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
 				DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
 
 		plot.setBackgroundPaint(Color.white);
 		plot.setRangeGridlinePaint(Color.gray);
 		plot.setDomainGridlinePaint(Color.gray);
 
 		// range axis
 		plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_RIGHT);
 
 		// domain axis
 		domainAxis = (DateAxis) plot.getDomainAxis();
 		domainAxis.setLabel("Time");
 		// domainAxis.setVerticalTickLabels(true);
 		domainAxis.setAutoTickUnitSelection(true);
 		domainAxis.setFixedAutoRange(LASTEST_TIME_FRAME);
 		plot.setDomainAxis(domainAxis);
 
 		// legend
 		// LegendTitle legend = new LegendTitle(plot);
 		// legend.setItemFont(new Font("Dialog", Font.PLAIN, 9));
 		// legend.setItemPaint(Color.gray);
 		// legend.setBackgroundPaint(Color.white);
 		// legend.setFrame(new BlockBorder(Color.gray));
 		// legend.setPosition(RectangleEdge.LEFT);
 		// XYTitleAnnotation ta = new XYTitleAnnotation(0.01, 0.02, legend,
 		// RectangleAnchor.BOTTOM_LEFT);
 		// plot.addAnnotation(ta);
 
 		// usability
 		setMouseWheelEnabled(true);
 
 		setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
 		setChart(chart);
 	}
 
 	public void setViewAll(boolean flag) {
 		if (flag) {
 			domainAxis.setFixedAutoRange(0);
 		} else {
 			domainAxis.setFixedAutoRange(LASTEST_TIME_FRAME);
 		}
 	}
 
 	public void clear() {
 		datasets.removeAllSeries();
 	}
 
 	public GraphValuesPanel getGraphValuesPanel() {
 		return graphValuesPanel;
 	}
 
 	@Override
 	public void evolved(final Collection<Culture> cultures, final long time) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				int i = 0;
 				for (Culture culture : cultures) {
 					if (i >= datasets.getSeriesCount()) {
 						datasets.addSeries(new TimeSeries(culture.getName()));
 					}
 					TimeSeries dataset = datasets.getSeries(i);
 					dataset.setKey(culture.getName());
 					Calendar calendar = Calendar.getInstance();
 					calendar.add(Calendar.MILLISECOND, (int) time);
 					dataset.add(new Millisecond(calendar.getTime()),
 							culture.getValue());
 					graphValuesPanel.setValue(culture);
 
 					i++;
 				}
 
 				// update chart
 				revalidate();
 				repaint();
 			}
 		});
 	}
 }
