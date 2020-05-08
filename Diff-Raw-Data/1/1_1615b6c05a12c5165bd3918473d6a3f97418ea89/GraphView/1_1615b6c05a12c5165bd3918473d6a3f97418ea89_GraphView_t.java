 package vitro.view;
 
 import vitro.util.*;
 import vitro.model.*;
 import vitro.model.graph.*;
 import vitro.controller.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.awt.geom.*;
 
 
 public class GraphView implements View {
 
 	public final Graph model;
 	private final Controller controller;
 
 	private final int width;
 	private final int height;
 	private final BufferedImage buffer;
 	private final BufferedImage target;
 	private final Graphics bg;
 	private final Graphics tg;
 
 	private final ReversibleMap<Node, NodeView> nodeToView = new ReversibleMap<Node, NodeView>();
 	private final ReversibleMap<NodeView, Node> viewToNode = nodeToView.reverse();
 
 	private final Set<EdgeView>  edgeViews  = new HashSet<EdgeView>();
 	private final Set<ActorView> actorViews = new HashSet<ActorView>();
 
 	public GraphView(Graph model, Controller controller, int width, int height) {
 		this.model = model;
 		this.controller = controller;
 		this.width = width;
 		this.height = height;
 		buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		target = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		bg = buffer.getGraphics();
 		tg = target.getGraphics();
 	}
 
 	public Controller controller() {
 		return controller;
 	}
 
 	public Node createNode(double x, double y) {
 		return createNode(x, y, "");
 	}
 
 	public Node createNode(double x, double y, String label) {
 		Node ret = model.createNode();
 		nodeToView.put(ret, new NodeView(ret, (int)(x*width), (int)(y*height), label));
 		return ret;
 	}
 
 	public Edge createEdge(Node start, Node end) {
 		Edge ret = model.createEdge(start, end);
 		edgeViews.add(new EdgeView(nodeToView.get(start), nodeToView.get(end)));
 		return ret;
 	}
 
 	private static int stringWidth(String s, Graphics g) {
 		Font font = g.getFont();
 		return (int)font.getStringBounds(s, g.getFontMetrics().getFontRenderContext()).getWidth();
 	}
 
 	public static void drawStringCentered(String s, int x, int y, Graphics g) {
 		Font font = g.getFont();
 		Rectangle2D bounds = font.getStringBounds(s, g.getFontMetrics().getFontRenderContext());
 		g.drawString(
 			s,
 			x-((int)bounds.getWidth()/2),
 			y+((int)bounds.getHeight()/2)
 		);
 	}
 	
 	private double sofar = 0;
 	public void tick(double time) {
 		sofar += time;
 		if (sofar >= 1) {
 			if (controller.hasNext()) { controller.next(); }
 			sofar = 0;
 		}
 	}
 
 	public boolean done() {
 		return (!controller.hasNext());
 	}
 
 	public void draw() {
 		actorViews.clear();
 		for(Actor a : model.actors) {
 			actorViews.add(new ActorView(a));
 		}
 		synchronized(target) {
 			tg.setColor(Color.WHITE);
 			tg.fillRect(0, 0, width, height);
 
 			if (tg instanceof Graphics2D) {
 				Graphics2D g2 = (Graphics2D)tg;
 				// moar pixels:
 				//g2.setRenderingHint( RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_SPEED);
 				//g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
 
 				// moar smoothnesses:
 				g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
 			}
 
 			for(EdgeView edge : edgeViews) {
 				edge.draw(tg);
 			}
 			for(NodeView node : viewToNode.keySet()) {
 				node.draw(tg);
 			}
 			for(ActorView actor : actorViews) {
 				actor.draw(tg);
 			}
 		}
 
 		int x = 10;
 		int y = 18;
 
 		Map<Color, String> key = new HashMap<Color, String>();
 		int maxWidth = 0;
 		for(ActorView a : actorViews) {
 			String name = a.actor.getClass().toString().split(" ")[1];
 			key.put(a.fill, name);
 			maxWidth = Math.max(maxWidth, stringWidth(name, tg));
 		}
 		tg.setColor(Color.WHITE);
 		tg.fillRoundRect(x, y+7, maxWidth + 60, 24 * key.size() + 1, 15, 15);
 		tg.setColor(Color.BLACK);
 		tg.drawRoundRect(x, y+7, maxWidth + 60, 24 * key.size() + 1, 15, 15);
 		tg.drawString("Key:", x+3, 18);
 		y += 8;
 		for(Map.Entry<Color, String> pair : key.entrySet()) {
 			tg.setColor(pair.getKey());
 			tg.fillRoundRect(x+5, y+4, 40, 16, 8, 8);
 			tg.setColor(Color.BLACK);
 			tg.drawRoundRect(x+5, y+4, 40, 16, 8, 8);
 			tg.drawString(pair.getValue(), x+50, y+16);
 			y += 24;
 		}
 	}
 
 	public Image getBuffer() {
 		synchronized(target) {
 			bg.drawImage(target, 0, 0, null);
 		}
 		return buffer;
 	}
 
 	private static class NodeView {
 		public final Node node;
 		public int x;
 		public int y;
 		public int radius = 40;
 		public String label;
 		
 		private NodeView(Node node, int x, int y, String label) {
 			this.node = node;
 			this.x = x;
 			this.y = y;
 			this.label = label;
 		}
 
 		public void draw(Graphics g) {
 			g.setColor(Color.WHITE);
 			g.fillOval(x-radius, y-radius, 2*radius, 2*radius);
 			g.setColor(Color.BLACK);
 			g.drawOval(x-radius, y-radius, 2*radius, 2*radius);
 			drawStringCentered(label, x, y + radius + 5, g);
 		}
 	}
 
 	private static class EdgeView {
 		private final NodeView start;
 		private final NodeView end;
 
 		private EdgeView(NodeView start, NodeView end) {
 			this.start = start;
 			this.end = end;
 		}
 
 		public void draw(Graphics g) {
 			// get desired parametric displacement
 			int[] s = { end.x - start.x, end.y - start.y };
 			double t = 1 - (end.radius / Math.sqrt(s[0] * s[0] + s[1] * s[1]));
 			
 			s[0] = start.x + (int)Math.round(s[0] * t);
 			s[1] = start.y + (int)Math.round(s[1] * t);
 
 			g.setColor(new Color(150, 150, 150));
 			g.drawLine(start.x, start.y, end.x, end.y);
 			g.setColor(Color.BLACK);
 			g.fillOval(s[0] - 4, s[1] - 4, 8, 8);
 		}
 	}
 
 	private class ActorView {
 		public final Actor actor;
 		public final Color fill;
 		public int radius = 10;
 
 		private ActorView(Actor actor) {
 			if (actor instanceof GraphActor) {
 				radius = 14;
 			}
 
 			this.actor = actor;
 			int x = actor.getClass().hashCode();
 			/*
 			fill = new Color(
 				(x >> 16) & 0xFF,
 				(x >>  8) & 0xFF,
 				(x >>  0) & 0xFF,
 				128
 			);
 			*/
 			fill = new Color(
 				((x >> 24) & 0xF0) | ((x >>  0) & 0x0F),
 				((x >> 16) & 0xF0) | ((x >>  8) & 0x0F),
 				((x >>  8) & 0xF0) | ((x >> 16) & 0x0F),
 				128
 			);
 		}
 
 		public int x() {
 			return nodeToView.get(model.getLocation(actor)).x;
 		}
 
 		public int y() {
 			return nodeToView.get(model.getLocation(actor)).y;
 		}
 
 		public void draw(Graphics g) {
			if (!nodeToView.containsKey(model.getLocation(actor))) { return; }
 			g.setColor(fill);
 			g.fillOval(x()-radius, y()-radius, 2*radius, 2*radius);
 			g.setColor(Color.BLACK);
 			g.drawOval(x()-radius, y()-radius, 2*radius, 2*radius);
 		}
 	}
 
 	private static class Arrow {
 		public double headWidth;
 		public double headLength;
 		public double width;
 		public Color fill;
 		public Color outline;
 		public int startx;
 		public int starty;
 		public int endx;
 		public int endy;
 
 		public void draw(Graphics g) {
 			
 		}
 	}
 	
 	public Node[][] layoutGrid(int xSize, int ySize) {
 		Node[][] array = new Node[xSize][ySize];
 		
 		for(int y = 0; y < ySize; y++) {
 			for(int x = 0; x < xSize; x++) {
 				array[x][y] = createNode((double)(x + 1) / (xSize + 1), (double)(y + 1) / (ySize + 1), "");
 			}
 		}
 		
 		return array;
 	}
 }
