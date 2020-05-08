 package ClassAdminFrontEnd;
 
 
 
 /*import java.awt.RenderingHints;
 
  import org.jfree.chart.ChartPanel;
  import org.jfree.chart.JFreeChart;
  import org.jfree.chart.axis.NumberAxis;
  import org.jfree.chart.plot.FastScatterPlot;
  import org.jfree.ui.ApplicationFrame;
  import org.jfree.ui.RefineryUtilities;
 
  *//**
  * A demo of the fast scatter plot.
  *
  */
 /*
  public class ScatterChart extends ApplicationFrame {
 
  *//** A constant for the number of items in the sample dataset. */
 /*
  private static final int COUNT = 200;
 
  *//** The data. */
 /*
  private float[][] data = new float[2][200];
 
  *//**
  * Creates a new fast scatter plot demo.
  *
  * @param title  the frame title.
  */
 /*
  public ScatterChart(final String title) {
 
  super(title);
  populateData();
  final NumberAxis domainAxis = new NumberAxis("Studente nr");
  domainAxis.setAutoRangeIncludesZero(false);
  final NumberAxis rangeAxis = new NumberAxis("Final punt");
  rangeAxis.setAutoRangeIncludesZero(false);
  final FastScatterPlot plot = new FastScatterPlot(this.data, domainAxis, rangeAxis);
  final JFreeChart chart = new JFreeChart("Fast Scatter Plot", plot);
 
  JFreeChart chart = ChartFactory.createScatterPlot(
  "Scatter Plot Demo",
  "X", "Y", 
  data, 
  PlotOrientation.VERTICAL,
  true, 
  true, 
  false
  );
  //        chart.setLegend(null);
 
  // force aliasing of the rendered content..
  chart.getRenderingHints().put
  (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
  final ChartPanel panel = new ChartPanel(chart, true);
  panel.setPreferredSize(new java.awt.Dimension(500, 270));
  //      panel.setHorizontalZoom(true);
  //    panel.setVerticalZoom(true);
  panel.setMinimumDrawHeight(1);
  panel.setMaximumDrawHeight(400);
  panel.setMinimumDrawWidth(1);
  panel.setMaximumDrawWidth(400);
 
  setContentPane(panel);
 
  }
  private void populateData() {
 
  for (int i = 0; i < this.data[0].length; i++) {
  final float x = (float) i ;
  this.data[0][i] = x;
  this.data[1][i] = (float) Math.random() * 100;
  }
 
  }
  }*/
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
//import oefen.ScatterChart;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartMouseEvent;
 import org.jfree.chart.ChartMouseListener;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.annotations.XYAnnotation;
 import org.jfree.chart.annotations.XYDrawableAnnotation;
 
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.entity.ChartEntity;
 import org.jfree.chart.entity.XYItemEntity;
 import org.jfree.chart.plot.PlotOrientation;
 
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RefineryUtilities;
 
 public class ScatterChart extends ApplicationFrame implements MouseListener,
 		MouseMotionListener {
 
 	public static final long serialVersionUID = 1L;
 	public final JLabel mousePosition;
 	public int x1, x2, y1, y2;
 	public int x, y, w, h;
 
 	public boolean isNewRect = true;
 
 	public ScatterChart(String title,XYDataset chartdata) {
 
 		super(title);
 		final XYDataset data = createDataset();
 		final JFreeChart chart = ChartFactory.createScatterPlot(
 				"Scatter Plot Demo", "Studente nr", "Finale punt", chartdata,
 				PlotOrientation.VERTICAL, false, false, false);
 
 		NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
 		domainAxis.setAutoRangeIncludesZero(false);
 		
 		final ChartPanel chartPanel = new ChartPanel(chart);
 
 		this.mousePosition = new JLabel();
 		this.mousePosition.setHorizontalAlignment(SwingConstants.CENTER);
 		getContentPane().add(this.mousePosition, BorderLayout.CENTER);
 		addMouseListener(this); // listens for own mouse and
 		addMouseMotionListener(this); // mouse-motion events
 		
 		chartPanel.addChartMouseListener(new ChartMouseListener() {
 
 			public void chartMouseClicked(ChartMouseEvent e) {
 				/*
 				 * XYPlot xyPlot2 = chartPanel.getChart().getXYPlot();
 				 * 
 				 * System.out.println(xyPlot2.getDomainCrosshairValue() + " " +
 				 * xyPlot2.getRangeCrosshairValue());
 				 */
 				ChartEntity entity = e.getEntity();
 
 				if (entity instanceof XYItemEntity && entity != null) {
 
 					XYItemEntity ent = (XYItemEntity) entity;
 					/*
 					 * XYLineAnnotation line = new XYLineAnnotation( 3, 0, 3,
 					 * 17, new BasicStroke(2f), Color.blue);
 					 */
 
 					int sindex = ent.getSeriesIndex();
 					int iindex = ent.getItem();
 
 					final CircleDrawer cd = new CircleDrawer(Color.red,
 							new BasicStroke(1.0f), null);
 					final XYAnnotation bestBid = new XYDrawableAnnotation(data
 							.getXValue(sindex, iindex), data.getYValue(sindex,
 							iindex), 11, 11, cd);
 
 					chart.getXYPlot().addAnnotation(bestBid);
 
 					System.out.println("x = " + data.getXValue(sindex, iindex));
 					System.out.println("y = " + data.getYValue(sindex, iindex));
 				}
 
 			}
 
 			@Override
 			public void chartMouseMoved(ChartMouseEvent arg0) {
 				// TODO Auto-generated method stub
 
 			}
 			
 		});
 
 		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 		// chartPanel.setVerticalAxisTrace(true);
 		// chartPanel.setHorizontalAxisTrace(true);
 		// chartPanel.setVerticalZoom(true);
 		// chartPanel.setHorizontalZoom(true);
 		setContentPane(chartPanel);
 
 	}
 
 	@Override
 	public void paint(final Graphics g) {
 		super.paint(g); // clear the frame surface
 
 		int width = this.x1 - this.x2;
 		int height = this.y1 - this.y2;
 
 		this.w = Math.abs(width);
 		this.h = Math.abs(height);
 		this.x = width < 0 ? this.x1 : this.x2;
 		this.y = height < 0 ? this.y1 : this.y2;
 
 		if (!this.isNewRect) {
 			g.drawRect(this.x, this.y, this.w, this.h);
 		}
 
 	}
 
 
 	private static XYDataset createDataset() {
 		XYSeriesCollection dataset = new XYSeriesCollection();
 
 		XYSeries series = new XYSeries("Scatter");
 		for (int i = 0; i < 100; i++) {
 			final float x = i;
 			series.add(x, Math.random() * 100);
 
 		}
 
 		dataset.addSeries(series);
 		return dataset;
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		this.mousePosition.setVisible(false);
 		this.mousePosition.setText((this.x2 = arg0.getX()) + ", "
 				+ (this.y2 = arg0.getY())); // call repaint which calls paint
 												// repaint();
 
 		this.isNewRect = false;
 
 		repaint();
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {
 		repaint();
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		this.isNewRect = true;
 		repaint();
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		repaint();
 System.out.println("Hello");
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	
 		this.mousePosition.setText("Pressed at [" + (this.x1 = e.getX())
 				+ ", " + (this.y1 = e.getY()) + "]");
 
 		this.isNewRect = true;
 
 		repaint();
 
 	}
 	public static void main(String[] args) {
 
	/*	Window demo = new ScatterChart("Scatter Plot Demo");
 		demo.pack();
 		RefineryUtilities.centerFrameOnScreen(demo);
		demo.setVisible(true);*/
 
 	}
 
 }
