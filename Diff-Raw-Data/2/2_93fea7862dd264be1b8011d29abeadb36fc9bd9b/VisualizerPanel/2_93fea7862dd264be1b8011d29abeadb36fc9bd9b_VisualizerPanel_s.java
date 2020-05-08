 package raisa.vis;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.HierarchyBoundsListener;
 import java.awt.event.HierarchyEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D.Float;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 public class VisualizerPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private Color measurementColor = new Color(0.4f, 0.4f, 0.4f);
 	private Float robot = new Float();
 	private Float camera = new Float();
 	private Float mouse = new Float();
 	private Float mouseDragStart = new Float();
 	private float scale = 1.0f;
 	private List<Sample> samples = new ArrayList<Sample>();
 	private List<Sample> latestIR = new ArrayList<Sample>();
 	private List<Sample> latestSR = new ArrayList<Sample>();
 	private Grid grid = new Grid();
 
 	public VisualizerPanel() {
 		setBackground(Color.gray);
 		setFocusable(true);
 		addHierarchyBoundsListener(new PanelSizeHandler());
 		addMouseMotionListener(new MouseMotionHandler());
 		addMouseListener(new MouseHandler());
 		addKeyListener(new KeyboardHandler());
 	}
 
 	public void update(List<Sample> samples) {
 		for (Sample sample : samples) {
 			if (sample.data.containsKey("ir") && sample.isSpot()) {
 				grid.addSpot(sample.getSpot());
 				latestIR.add(sample);
 				latestIR = takeLast(latestIR, 10);
 			}
 			if (sample.data.containsKey("sr") && sample.data.containsKey("sd")) {
 				latestSR.add(sample);
 				latestSR = takeLast(latestSR, 10);
 			}
 		}
 		synchronized (this.samples) {
 			this.samples.addAll(samples);
 		}
 		repaint();
 	}
 
 	public void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		int screenWidth = getBounds().width;
 		int screenHeight = getBounds().height;
 		g.clearRect(0, 0, screenWidth, screenHeight);
 
 		Float tl = new Float(camera.x - screenWidth * 0.5f / scale, camera.y
 				- screenHeight * 0.5f / scale);
 		grid.draw(g2, new Rectangle2D.Float(tl.x * scale, tl.y * scale,
 				screenWidth * scale, screenHeight * scale), this);
 
 		g.setColor(Color.black);
 		/*
 		 * synchronized (samples) { for (Sample sample : samples) { if
 		 * (sample.isSpot()) { drawPoint(g, sample.getSpot()); } } }
 		 */
 
 		g.setColor(measurementColor);
 		drawMeasurementLine(g, robot, toWorld(mouse));
 
 		if (!latestSR.isEmpty()) {
 			float sonarWidth = 25.0f;
 			List<Sample> srs = new ArrayList<Sample>(latestSR);
 			Collections.reverse(srs);
 			float sr = 1.0f;
 			for (Sample sample : srs) {
 				Spot spot = sample.getSrSpot();
 				g.setColor(new Color(0.0f, 0.6f, 0.6f, sr));
 				if (sr >= 1.0f) {
					drawMeasurementLine(g, sample.getRobot(), sample.getSpot());						
 					g.setColor(new Color(0.0f, 0.6f, 0.6f, 0.05f));
 					drawSector(g, robot, spot, sonarWidth);
 				} else {
 					g.setColor(new Color(0.0f, 0.6f, 0.6f, 0.05f));
 					drawSector(g, robot, spot, sonarWidth);
 				}
 				g.setColor(new Color(0.0f, 0.6f, 0.6f, sr));
 				drawPoint(g, sample.getSpot());
 				sr *= 0.8f;
 			}
 		}
 
 		if (!latestIR.isEmpty()) {
 			List<Sample> irs = new ArrayList<Sample>(latestIR);
 			Collections.reverse(irs);
 			float ir = 1.0f;
 			for (Sample sample : irs) {
 				if (sample.isSpot()) {
 					g.setColor(new Color(1.0f, 0.0f, 0.0f, ir));
 					if (ir >= 1.0f) {
 						drawMeasurementLine(g, sample.getRobot(), sample.getSpot());						
 					} else {
 						drawMeasurementLine(g, sample.getRobot(), sample.getSpot(), false);
 					}
 					drawPoint(g, sample.getSpot());
 					ir *= 0.8f;
 				}
 			}
 		}
 	}
 
 	private void drawSector(Graphics g, Float from, Float to, float sector) {
 		Float p1 = toScreen(from);
 		Float p2 = toScreen(to);
 		g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
 		float dx = (p2.x - p1.x);
 		float dy = (p2.y - p1.y);
 		float l = (float) Math.sqrt(dx * dx + dy * dy);
 		float a = (float) (Math.atan2(-dy, dx) / Math.PI * 180.0) - sector
 				* 0.5f;
 		g.fillArc((int) (p1.x - l), (int) (p1.y - l), (int) (2.0f * l),
 				(int) (2.0f * l), (int) a, (int) sector);
 	}
 
 	private void drawPoint(Graphics g, Float point) {
 		float w = 3.0f;
 		float h = 3.0f;
 		Float p = toScreen(point);
 		g.fillRect((int) (p.x - 0.5f * w), (int) (p.y - 0.5f * h), (int) w,
 				(int) h);
 	}
 
 	private void drawMeasurementLine(Graphics g, Float from, Float to) {
 		drawMeasurementLine(g, from, to, true);
 	}
 	
 	private void drawMeasurementLine(Graphics g, Float from, Float to, boolean drawDistanceString) {
 		Float p1 = toScreen(from);
 		Float p2 = toScreen(to);
 		g.drawLine((int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y);
 		if (drawDistanceString) {
 			float dx = (from.x - to.x);
 			float dy = (from.y - to.y);
 			float l = (float) Math.sqrt(dx * dx + dy * dy);
 			String distanceString = String.format("%3.1f cm", l);
 			g.drawString(distanceString, (int) (p2.x + 10), (int) p2.y);
 		}
 	}
 
 	public Float toWorld(Float screen) {
 		int screenWidth = getBounds().width;
 		int screenHeight = getBounds().height;
 		return new Float(camera.x + (screen.x - screenWidth * 0.5f) / scale,
 				camera.y + (screen.y - screenHeight * 0.5f) / scale);
 	}
 
 	public float toScreen(float size) {
 		return size * scale;
 	}
 
 	public Float toScreen(Float from) {
 		int screenWidth = getBounds().width;
 		int screenHeight = getBounds().height;
 		float x1 = (from.x - camera.x) * scale + 0.5f * screenWidth;
 		float y1 = (from.y - camera.y) * scale + 0.5f * screenHeight;
 		Float f = new Float(x1, y1);
 		return f;
 	}
 
 	private final class PanelSizeHandler implements HierarchyBoundsListener {
 		@Override
 		public void ancestorResized(HierarchyEvent arg0) {
 			resetMouseLocationLine();
 			repaint();
 		}
 
 		private void resetMouseLocationLine() {
 			mouse.x = camera.x;
 			mouse.y = camera.y;
 		}
 
 		@Override
 		public void ancestorMoved(HierarchyEvent arg0) {
 		}
 	}
 
 	private final class MouseMotionHandler extends MouseAdapter {
 		@Override
 		public void mouseMoved(MouseEvent mouseEvent) {
 			mouse.x = mouseEvent.getX();
 			mouse.y = mouseEvent.getY();
 			repaint();
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent mouseEvent) {
 			mouse.x = mouseEvent.getX();
 			mouse.y = mouseEvent.getY();
 			camera.x += (mouseDragStart.x - mouse.x) / scale;
 			camera.y += (mouseDragStart.y - mouse.y) / scale;
 			mouseDragStart.x = mouse.x;
 			mouseDragStart.y = mouse.y;
 			repaint();
 		}
 	}
 
 	private final class MouseHandler extends MouseAdapter {
 		@Override
 		public void mousePressed(MouseEvent mouseEvent) {
 			mouseDragStart.x = mouseEvent.getX();
 			mouseDragStart.y = mouseEvent.getY();
 		}
 	}
 
 	private final class KeyboardHandler extends KeyAdapter {
 		@Override
 		public void keyReleased(KeyEvent arg0) {
 			int keyCode = arg0.getKeyCode();
 			if (keyCode == KeyEvent.VK_C) {
 				System.out.println("Cleared samples");
 				samples.clear();
 				repaint();
 			} else if (keyCode == KeyEvent.VK_H) {
 				System.out.println("Removed old samples");
 				samples = takeLast(samples, 1000);
 				repaint();
 			} else if (keyCode == KeyEvent.VK_PLUS) {
 				scale *= 1.25f;
 				repaint();
 			} else if (keyCode == KeyEvent.VK_MINUS) {
 				scale *= 0.8f;
 				repaint();
 			}
 		}
 	}
 
 	private List<Sample> takeLast(List<Sample> samples, int length) {
 		if (samples.size() > length) {
 			int fromIndex = Math.max(0, samples.size() - length);
 			int toIndex = samples.size();
 			List<Sample> newSamples = new ArrayList<Sample>();
 			newSamples.addAll(samples.subList(fromIndex, toIndex));
 			return newSamples;
 		}
 		return samples;
 	}
 }
