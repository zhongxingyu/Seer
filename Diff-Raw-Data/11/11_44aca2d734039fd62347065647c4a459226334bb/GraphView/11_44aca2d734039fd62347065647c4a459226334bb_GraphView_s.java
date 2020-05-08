 package edu.tufts.cs.gv.view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JSlider;
 import javax.swing.ToolTipManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import edu.tufts.cs.gv.controller.VizEventType;
 import edu.tufts.cs.gv.controller.VizState;
 import edu.tufts.cs.gv.model.Dataset;
 import edu.tufts.cs.gv.model.graph.Edge;
 import edu.tufts.cs.gv.model.graph.Graph;
 import edu.tufts.cs.gv.model.graph.Vertex;
 import edu.tufts.cs.gv.util.Vector;
 
 public class GraphView extends VizView implements MouseListener, MouseMotionListener, MouseWheelListener {
 	private static final long serialVersionUID = 1L;
 
 	private static double Kc = 200;
 	private static double Ks = .05;
 	private static double Kg = .1;
 	private static double ENERGY_LIMIT = .5;
 	private static double SPRING_LENGTH = 3;
 	private static final double radius = 10;
 	private static final double diameter = radius * 2;
 	
 	private static final double zoom_rate = .9;
 	
 	private Dataset dataset;
 	private Graph graph;
 	
 	private boolean simulating;
 	private Vertex moving;
 	private Point lastPoint;
 	
 	private Vector offset;
 	private double scale;
 	
 	public GraphView() {
 		VizState.getState().addVizUpdateListener(this);
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		this.addMouseWheelListener(this);
 		dataset = null;
 		graph = null;
 		simulating = false;
 		lastPoint = null;
 		offset = new Vector();
 		this.setToolTipText("");
 		ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
 		ToolTipManager.sharedInstance().setInitialDelay(100);
 	}
 	
 	@Override
 	public void vizUpdated(VizEventType eventType) {
 		VizState state = VizState.getState();
 		if (state.getDataset() != dataset) {
 			dataset = state.getDataset();
 			graph = new Graph(dataset);
 			for (Vertex v : graph.getVertices()) {
 				if (v.isRoot()) {
 					v.setX(getWidth() / 2);
 					v.setY(diameter);
 				} else {
 					v.setX(Math.random() * getWidth() * .5 + .25 * getWidth());
 					v.setY(Math.random() * getWidth() * .5 + .25 * getHeight());
 				}
 			}
 			simulating = true;
 			moving = null;
 			offset = new Vector(0, 0);
 			scale = 1;
 		}
 		if (eventType == VizEventType.HOVERING_TESTS) {
 			//System.out.println(VizState.getState().getMousedOverTests().toString());
 		}
 	}
 
 	@Override
 	public void paint(Graphics g1) {
 		Graphics2D g = (Graphics2D) g1;
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 			RenderingHints.VALUE_ANTIALIAS_ON);
 		
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		if (graph != null) {
 			g.setColor(Color.BLACK);
 			for (Edge e : graph.getEdges()) {
 				Vector a = worldToScreen(e.getA().getX(), e.getA().getY());
 				Vector b = worldToScreen(e.getB().getX(), e.getB().getY());
 				g.drawLine((int)a.x, (int)a.y, (int)b.x, (int)b.y);
 			}
 			for (Vertex v : graph.getVertices()) {
 				if (v.isSelected()) {
 					g.setColor(Color.CYAN);
 				} else {
 					g.setColor(Color.RED);
 				}
 				Vector center = worldToScreen(v.getX(), v.getY());
 				double radius = calcRadius(v.getTestNames().size());
 				double diameter = 2 * radius;
 				g.fillOval((int)(center.x - radius * scale), (int)(center.y - radius * scale), (int)(diameter * scale), (int)(diameter * scale));
 				g.setColor(Color.BLACK);
 				g.drawOval((int)(center.x - radius * scale), (int)(center.y - radius * scale), (int)(diameter * scale), (int)(diameter * scale));
 			}
 		}
 	}
 	
 	public int calcRadius(int numTests) {
 		return Math.max((int)(Math.sqrt(numTests) * radius), 5);
 	}
 	
 	public Vector worldToScreen(Point world) {
 		return new Vector(world.x * scale + offset.x, world.y * scale + offset.y);
 	}
 	
 	public Vector worldToScreen(double x, double y) {
 		return new Vector(x * scale + offset.x, y * scale + offset.y);
 	}
 	
 	public Vector screenToWorld(Point screen) {
 		return new Vector((screen.x - offset.x) / scale, (screen.y - offset.y) / scale); 
 	}
 	
 	public Vector screenToWorld(double x, double y) {
 		return new Vector((x - offset.x) / scale, (y - offset.y) / scale); 
 	}
 
 	@Override
 	public void update() {
 		if (graph != null && simulating) {
 			for (Vertex a : graph.getVertices()) {
 				for (Vertex b : graph.getVertices()) {
 					if (a == b) { continue; }
 					Vector repel = new Vector(b.getX() - a.getX(), b.getY() - a.getY());
 					repel.normalize();
 					double mag = - (Kc * a.getTestNames().size() * b.getTestNames().size()) / (a.getDistance2(b));
 					if (a.getDistance2(b) < .0001) {
 						mag = 0;
 					}
 					repel.scale(mag);
 					a.applyForce(repel.getX(), repel.getY());
 				}
 				a.applyForce(0, Kg);
 			}
 			for (Edge e : graph.getEdges()) {
 				Vertex a = e.getA();
 				Vertex b = e.getB();
 				Vector attract = new Vector(a.getX() - b.getX(), a.getY() - b.getY());
 				attract.normalize();
 				double mag = - Ks * (a.getDistance(b) - (e.getStudentDiff() / (float)dataset.getAllStudents().size())
 						* getHeight() * SPRING_LENGTH);
 				attract.scale(mag);
 				a.applyForce(attract.getX(), attract.getY());
 				b.applyForce(-attract.getX(), -attract.getY());
 			}
 			double totalE = 0;
 			for (Vertex v : graph.getVertices()) {
 				totalE += v.getVelocity2();
 				v.move();
 			}
 			if (totalE < ENERGY_LIMIT * graph.getVertices().size()) {
 				simulating = false;
 			}
 		}
 	}
 	
 	public String getToolTipText(MouseEvent e) {
 		if (graph != null) {
 			Vector point = screenToWorld(e.getPoint());
 			for (Vertex v : graph.getVertices()) {
 				if (v.getDistance(point.x, point.y) <= calcRadius(v.getTestNames().size())) {
 					String tooltip = "<html>";
 					if (v.getTestNames().size() == 0) {
 						tooltip += "No Tests";
 					} else {
 						for (String test : v.getTestNames()) {
 							tooltip += test + "<br>";
 						}
 					}
 					tooltip += "</html>";
 					return tooltip;
 				}
 			}
 			return null;
 		}
 		return null;
 	}
 	
 	public ChangeListener getSpringListener() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				Ks = ((JSlider)e.getSource()).getValue() / 10000.0;
 				simulating = true;
 			}
 		};
 	}
 	
 	public ChangeListener getSpringLengthListener() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				SPRING_LENGTH = ((JSlider)e.getSource()).getValue() / 100.0;
 				simulating = true;
 			}
 		};
 	}
 	
 	public ChangeListener getRepulstionListener() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				Kc = ((JSlider)e.getSource()).getValue();
 				simulating = true;
 			}
 		};
 	}
 	
 	public ChangeListener getGravityListener() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				Kg = ((JSlider)e.getSource()).getValue() / 1000.0;
 				simulating = true;
 			}
 		};
 	}
 	
 	public ChangeListener getEnergyListener() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				ENERGY_LIMIT = ((JSlider)e.getSource()).getValue() / 100.0;
 				simulating = true;
 			}
 		};
 	}
 	
 	public void mouseDragged(MouseEvent e) {
 		if (moving != null) {
 			double dx = e.getX() - lastPoint.x;
 			double dy = e.getY() - lastPoint.y;
 			moving.moveDelta(dx / scale, dy / scale);
 			simulating = true;
 			lastPoint = e.getPoint();
 		} else {
 			double dx = e.getX() - lastPoint.x;
 			double dy = e.getY() - lastPoint.y;
 			offset.x += dx;
 			offset.y += dy;
 			lastPoint = e.getPoint();
 		}
 	}
 
 	public void mousePressed(MouseEvent e) {
 		if (graph == null) { return; }
 		Vector p = screenToWorld(e.getPoint());
 		for (Vertex v : graph.getVertices()) {
 			if (v.getDistance(p.x, p.y) <= radius) {
 				moving = v;
 			}
 		}
 		if (moving != null) {
 			lastPoint = e.getPoint();
 			simulating = true;
 			moving.setOverriding(true);
 		} else {
 			lastPoint = e.getPoint();
 		}
 	}
 
 	public void mouseReleased(MouseEvent e) {
 		if (moving != null) {
 			moving.setOverriding(false);
 			moving = null;
 			lastPoint = null;
 		} else {
 			lastPoint = null;
 		}
 	}
 
 	public void mouseMoved(MouseEvent e) {
 		if (graph == null) { return; }
		Point p = e.getPoint();
 		Set<String> selectedTests = new HashSet<>();
 		boolean none = true;
 		for (Vertex v : graph.getVertices()) {
			if (v.getDistance(p.x, p.y) <= radius) {
 				selectedTests.addAll(v.getTestNames());
 				none = false;
 			}
 		}
 		if (none) {
 			VizState.getState().setMousedOverTests(null);
 		} else {
 			VizState.getState().setMousedOverTests(selectedTests);
 		}
 	}
 	
 	public void mouseWheelMoved(MouseWheelEvent e) {
 		Vector centerWorld = screenToWorld(e.getPoint());
 		if (e.getWheelRotation() < 0) {
			scale *= Math.abs(e.getWheelRotation()) * zoom_rate;
		} else {
 			scale /= Math.abs(e.getWheelRotation()) * zoom_rate;
 		}
 		Vector newCenterWorld = screenToWorld(e.getPoint());
 		offset.x += (newCenterWorld.x - centerWorld.x) * scale;
 		offset.y += (newCenterWorld.y - centerWorld.y) * scale;
 	}
 	
 	public void mouseClicked(MouseEvent e) {}
 	public void mouseEntered(MouseEvent e) {}
 	public void mouseExited(MouseEvent e) {}
 }
