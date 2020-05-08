 package bump.org.comp;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.awt.geom.Path2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Path2D.Double;
 
 import bump.org.comp.Animation.IAnimatedChart;
 import bump.org.util.ChartingUtil;
 
 public class SmoothAnimatedSingleLineChart extends SingleLineChart implements
 		IAnimatedChart {
	private float updaterate = 0.5F;
	private int fpp = 10;
 	private int frame = 1;
 
 	public int getFramesperPoint() {
 		return fpp;
 	}
 
 	public void setFramesperPoint(int g) {
 		fpp = g;
 	}
 
 	public void update(Graphics g) {
 		this.setBackground(getBackgroundcolor());
 		super.update(g);
 	}
 
 	public void paint(Graphics g) {
 
 		if (data.size() >= 1) {
 
 			Graphics2D put = (Graphics2D) g;
 			double hstep = getWidth() / data.size();
 			double vstep = getHeight() / ChartingUtil.Max(data);
 			if (vstep < .2) {
 				vstep = .2;
 			}
 
 			// Add rendering hints to the graphics objects according to the
 			// members of the class.
 			RenderingHints h;
 
 			if (antialias) {
 				h = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
 						RenderingHints.VALUE_ANTIALIAS_ON);
 
 			} else {
 				h = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
 						RenderingHints.VALUE_ANTIALIAS_OFF);
 			}
 			if (bicubic)
 				h.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
 						RenderingHints.VALUE_INTERPOLATION_BICUBIC));
 			else
 				h.add(new RenderingHints(RenderingHints.KEY_INTERPOLATION,
 						RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR));
 			put.addRenderingHints(h);
 			put.setPaintMode();
 			// set the color to the background color member
 			put.setColor(getBackgroundcolor());
 			// Fill the background of the graph
			put.fill(new Rectangle2D.Float(0, 0, getWidth(), getHeight()));
 			drawTicks(put, hstep, vstep);
 			put.setColor(linecolor);
 			Path2D.Double a = new Path2D.Double();
 			a.moveTo(0, data.get(0));
 			int i;
 			for (i = 1; i < data.size() && i < (float) frame / (float) fpp; i++) {
 				a.lineTo(hstep * i, vstep * data.get(i));
 			}
 			a.lineTo(
 					((float) frame / (float) fpp) * hstep,
 					vstep
 							* ChartingUtil.pointslope(
 									(float) frame / (float) fpp,
 									ChartingUtil.slope(data.get(i - 1),
 											data.get(i), 1), data.get(i - 1),
 									i - 1));
 			put.draw(a);
			put.draw(new Ellipse2D.Double((frame / fpp) * hstep,
					ChartingUtil
 							.pointslope(
 									(float) frame / (float) fpp,
 									ChartingUtil.slope(data.get(i - 1),
 											data.get(i), 1), data.get(i - 1),
 									i - 1)
 							* vstep, 2, 2));
 			frame += 1;
 			if (frame >= data.size() * fpp)
 				frame = 1;
 		}
 	}
 
 	private void drawTicks(Graphics2D put, double hstep, double vstep) {
 		put.setColor(ChartingUtil.createInverse(getBackgroundcolor()));
 		// Draw the horizontal Axis.
 		put.draw(new Line2D.Double(0, getHeight() / 2, getWidth(),
 				getHeight() / 2));
 		for (int i = 0; i * vstep < getWidth(); i++)
 			if (i % xtick == 0)
 				put.draw(new Line2D.Double(i * hstep, 0 + getHeight() / 2, i
 						* hstep, 5 + getHeight() / 2));
 		// draw the vertical ticks.
 		put.draw(new Line2D.Double(getWidth() / 2, -getHeight(),
 				getWidth() / 2, getHeight()));
 		for (int i = 0; (i * vstep) < (getHeight()); i++) {
 			// if the integer is a multiple of i, draw the tick.
 			if (i % ytick == 0) {
 				put.draw(new Line2D.Double(getWidth() / 2, i * vstep,
 						getWidth() / 2 + 5, i * vstep));
 			}
 		}
 	}
 
 	private Thread ad;
 
 	public SmoothAnimatedSingleLineChart() {
 		ad = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (true) {
 					if (SmoothAnimatedSingleLineChart.this.isVisible())
 						SmoothAnimatedSingleLineChart.this.repaint();
 					try {
 						Thread.sleep(Math
 								.round(SmoothAnimatedSingleLineChart.this
 										.getUpdateRate() * 1000));
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 						return;
 					}
 				}
 			}
 
 		});
 		ad.start();
 	}
 
 	@Override
 	public void setUpdateRate(int updaterate) {
 		this.updaterate = 1000F / (float) updaterate;
 	}
 
 	@Override
 	public float getUpdateRate() {
 		return this.updaterate;
 	}
 
 	@Override
 	public void resetFrame() {
 		frame = 1;
 	}
 
 }
