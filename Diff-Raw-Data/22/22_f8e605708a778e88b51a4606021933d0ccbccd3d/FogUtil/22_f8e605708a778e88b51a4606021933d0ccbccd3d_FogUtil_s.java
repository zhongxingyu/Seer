 package net.rptools.maptool.client.ui.zone;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import net.rptools.lib.GeometryUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.Path;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.util.GraphicsUtil;
 
 public class FogUtil {
 
 	public static Area calculateVisibility(int x, int y, Area vision, AreaData topology) {
 		
 		vision = new Area(vision);
 		vision.transform(AffineTransform.getTranslateInstance(x, y));
 		
 		// sanity check
 		if (topology.contains(x, y)) {
 			return null;
 		}
 		
 		Point origin = new Point(x, y);
 		
 		Area clearedArea = new Area();
 		
 		int blockCount = 0;
 		int skippedAreas = 0;
 		for (AreaMeta areaMeta : topology.getAreaList(origin)) {
 //			int pointCount = 0;
 //			int origSize = 0;
 //			int afterSize = 0;
 
 			if (clearedArea.contains(areaMeta.area.getBounds())) {
 				skippedAreas++;
 				continue;
 			}
 			
 			LinkedList<Line2D> lineList = new LinkedList<Line2D>(areaMeta.getAllFaces());
 //			LinkedList<Line2D> lineList = new LinkedList<Line2D>(areaMeta.getFrontFaces(origin));
 			
 			List<RelativeLine> relativeLineList = new LinkedList<RelativeLine>();
 			for (Iterator<Line2D> lineIter = lineList.iterator(); lineIter.hasNext();) {
 				Line2D line = lineIter.next();
 				relativeLineList.add(new RelativeLine(line, GeometryUtil.getDistance(origin, GeometryUtil.getCloserPoint(origin, line))));
 			}
 			
 			Collections.sort(relativeLineList, new Comparator<RelativeLine>() {
 				public int compare(RelativeLine o1, RelativeLine o2) {
 					
 					return o1.distance < o2.distance ? -1 : o2.distance < o1.distance ? 1 : 0;
 				}
 			});
 			
 			List<Area> blockList = new LinkedList<Area>();
 			for (RelativeLine rline : relativeLineList) {
 				Line2D line = rline.line;
 				
 				double rx = Math.min(line.getP1().getX(), line.getP2().getX())-1;
 				double ry = Math.min(line.getP1().getY(), line.getP2().getY())-1;
 				double width = Math.max(line.getP1().getX(), line.getP2().getX()) - rx+2;
 				double height = Math.max(line.getP1().getY(), line.getP2().getY()) - ry+2;
 	
 				if (clearedArea.contains(new Rectangle2D.Double(rx, ry, width, height))) {
 					continue;
 				}
 
 				blockList.add(createBlockArea(origin, line));
 				
 				blockCount++;
 			}
 			
 			while (blockList.size() > 1) {
 				
 				Area a1 = blockList.remove(0);
 				Area a2 = blockList.remove(0);
 				
 				a1.add(a2);
 				blockList.add(a1);
 			}
 			
 			if (blockList.size() > 0) {
 				clearedArea.add(blockList.remove(0));
 			}
 			
 		}
 //		System.out.println("Blocks: " + blockCount + " : Skipped: " + skippedAreas);
 
 		// For simplicity, this catches some of the edge cases
 		vision.subtract(clearedArea);
 
 		
 		return vision;
 	}	
 	
 	private static class RelativeLine {
 		private Line2D line;
 		private double distance;
 		public RelativeLine(Line2D line, double distance) {
 			this.line = line;
 			this.distance = distance;
 		}
 	}
 	
 	private static Area createBlockArea(Point2D origin, Line2D line) {
 
 		Point2D p1 = line.getP1();
 		Point2D p2 = line.getP2();
 		
 		Point2D p1out = GraphicsUtil.getProjectedPoint(origin, p1, Integer.MAX_VALUE/2);
 		Point2D p2out = GraphicsUtil.getProjectedPoint(origin, p2, Integer.MAX_VALUE/2);
 		
 		// TODO: Remove the (float) when we move to jdk6
 		GeneralPath path = new GeneralPath();
 		path.moveTo((float)p1.getX(), (float)p1.getY());
 		path.lineTo((float)p2.getX(), (float)p2.getY());
 		path.lineTo((float)p2out.getX(), (float)p2out.getY());
 		path.lineTo((float)p1out.getX(), (float)p1out.getY());
 		path.closePath();
 		
 		return new Area(path);
 	}
 	
 	public static void exposeVisibleArea(ZoneRenderer renderer, Set<GUID> tokenSet) {
 
 		Zone zone = renderer.getZone();
 		for (GUID tokenGUID : tokenSet) {
 			Token token = zone.getToken(tokenGUID);
 			if (token == null) {
 				continue;
 			}
 			
 			if (!token.hasSight()) {
 				continue;
 			}
 
 			Area tokenVision = renderer.getTokenVision(token);
 
 			if (tokenVision != null) {
 				zone.exposeArea(tokenVision);
 				MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision);
 			}
 		}
 	}
 	
 	public static void exposePCArea(ZoneRenderer renderer) {
 
 		Set<GUID> tokenSet = new HashSet<GUID>();
 		for (Token token : renderer.getZone().getPlayerTokens()) {
 			
 			if (!token.hasSight()) {
 				continue;
 			}
 			
 			tokenSet.add(token.getId());
 		}
 		exposeVisibleArea(renderer, tokenSet);
 	}
 	
 	public static void exposeLastPath(ZoneRenderer renderer, Set<GUID> tokenSet) {
 
 		Zone zone = renderer.getZone();
 		for (GUID tokenGUID : tokenSet) {
 			Token token = zone.getToken(tokenGUID);
 			if (token == null) {
 				continue;
 			}
 			
 			if (!token.hasSight()) {
 				continue;
 			}
 			
 			Path<CellPoint> lastPath = (Path<CellPoint>) token.getLastPath();
 			if (lastPath == null) {
 				continue;
 			}
 			
 			Grid grid = zone.getGrid();
 			Area visionArea = new Area();
 
 //			for (CellPoint cell : lastPath.getCellPath()) {
 //				
 //				ZonePoint zp = grid.convert(cell); 
 //				int x = zp.x;
 //				int y = zp.y;
 //				
 //				Point p = calculateVisionCenter(token, zone);
 //    			
 //				Area currVisionArea = FogUtil.calculateVisibility(p.x, p.y, vision.getArea(zone, token), renderer.getTopologyAreaData());
 //				if (currVisionArea != null) {
 //					visionArea.add(currVisionArea);
 //				}
 //			}
 //
 //			zone.exposeArea(visionArea);
 //			MapTool.serverCommand().exposeFoW(zone.getId(), visionArea);
 		}
 		
 	}
 	
 	/**
 	 * Find the center point of a vision
 	 * TODO: This is a horrible horrible method.  the API is just plain disgusting.  But it'll work to consolidate
 	 * all the places this has to be done until we can encapsulate it into the vision itself
 	 */
 	public static Point calculateVisionCenter(Token token, Zone zone) {
 		
 		Grid grid = zone.getGrid();
 		int x=0, y=0;
 		
		if (token.isSnapToScale()) {

			Rectangle bounds = null;
			if (token.isSnapToGrid()) {
				bounds = token.getFootprint(grid).getBounds(grid, grid.convert(new ZonePoint(token.getX(), token.getY())));
			} else {
				bounds = token.getBounds(zone);
			}

			x = bounds.x + bounds.width/2;
			y = bounds.y + bounds.height/2;
 		} else {
			x = token.getX();
			y = token.getY();
 		}
 		
 		return new Point(x, y);
 	}
 
 
 
 
 
 	public static void main(String[] args) {
 		
 		System.out.println("Creating topology");
 		final int topSize = 10000;
 		final Area topology = new Area();
 		Random r = new Random(12345);
 		for (int i = 0; i < 100; i++) {
 			int x = r.nextInt(topSize);
 			int y = r.nextInt(topSize);
 			int w = r.nextInt(500) + 50;
 			int h = r.nextInt(500) + 50;
 			
 			topology.add(new Area(new Rectangle(x, y, w, h)));
 		}
 		
 		// Make sure the the center point is not contained inside the blocked area
 		topology.subtract(new Area(new Rectangle(topSize/2-200, topSize/2-200, 400, 400)));
 		
 		final Area vision = new Area(new Rectangle(-Integer.MAX_VALUE/2, -Integer.MAX_VALUE/2, Integer.MAX_VALUE, Integer.MAX_VALUE));
 		
 		int pointCount = 0;
 		for (PathIterator iter = topology.getPathIterator(null); !iter.isDone(); iter.next()) {
 			pointCount++;
 		}
 		
 		System.out.println("Starting test " + pointCount + " points");
 		final AreaData data = new AreaData(topology);
 		data.digest();
 		
 		// Make sure all classes are loaded
 		calculateVisibility(topSize/2, topSize/2, vision, data);
 
 		
 		Area area1 = new Area();
 //		JOptionPane.showMessageDialog(new JFrame(), "Hello");
 		long start = System.currentTimeMillis();
 		for (int i = 0; i < 1; i++) {
 			area1 = calculateVisibility(topSize/2, topSize/2, vision, data);
 		}
 
 		final Area a1 = area1;
 		JFrame f = new JFrame();
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setBounds(0, 0, 400, 200);
 		f.setLayout(new GridLayout());
 		f.add(new JPanel() {
 			BufferedImage topImage = null;
 			Area theArea = null;
 			{
 				addMouseMotionListener(new MouseMotionAdapter() {
 					@Override
 					public void mouseDragged(MouseEvent e) {
 						
 						Dimension size = getSize();
 						int x = (int)((e.getX() - (size.width/2)) / (size.width/2.0/topSize));
 						int y = (int)(e.getY() / (size.height/2.0/topSize)/2);
 						
 						long start = System.currentTimeMillis();
 						theArea = calculateVisibility(x, y, vision, data);
 						System.out.println("Calc: " + (System.currentTimeMillis() - start));
 						repaint();
 					}
 				});
 				addMouseListener(new MouseAdapter() {
 					@Override
 					public void mousePressed(MouseEvent e) {
 						
 						Dimension size = getSize();
 						int x = (int)((e.getX() - (size.width/2)) / (size.width/2.0/topSize));
 						int y = (int)(e.getY() / (size.height/2.0/topSize)/2);
 						
 						long start = System.currentTimeMillis();
 						theArea = calculateVisibility(x, y, vision, data);
 						System.out.println("Calc: " + (System.currentTimeMillis() - start));
 						repaint();
 					}
 				});
 			}
 			@Override
 			protected void paintComponent(Graphics g) {
 
 				Dimension size = getSize();
 				g.setColor(Color.white);
 				g.fillRect(0, 0, size.width, size.height);
 				
 				Graphics2D g2d = (Graphics2D)g;
 				
 				AffineTransform at = AffineTransform.getScaleInstance((size.width/2)/(double)topSize, (size.height)/(double)topSize);
 				if (topImage == null) {
 					Area top = topology.createTransformedArea(at);
 					topImage = new BufferedImage(size.width/2, size.height, BufferedImage.OPAQUE);
 					
 					Graphics2D g2 = topImage.createGraphics();
 					g2.setColor(Color.white);
 					g2.fillRect(0, 0, size.width/2, size.height);
 					
 					g2.setColor(Color.green);
 					g2.fill(top);
 					g2.dispose();
 				}
 				
 				g.setColor (Color.black);
 				g.drawLine(size.width/2, 0, size.width/2, size.height);
 
 //				g.setClip(new Rectangle(0, 0, size.width/2, size.height));
 //				g.setColor(Color.green);
 //				g2d.fill(top);
 //				
 //				g.setColor(Color.lightGray);
 //				g2d.fill(a1.createTransformedArea(at));
 				
 				g.setClip(new Rectangle(size.width/2, 0, size.width/2, size.height));
 				g2d.translate(200, 0);
 				g.setColor(Color.green);
 				g2d.drawImage(topImage, 0, 0, this);
 				g.setColor(Color.gray);
 				if (theArea != null) {
 					g2d.fill(theArea.createTransformedArea(at));
 				}
 				
 				for (AreaMeta areaMeta : data.getAreaList(new Point(0, 0))) {
 					g.setColor(Color.red);
 					g2d.draw(areaMeta.area.createTransformedArea(at));
 				}
 //				g.setColor(Color.red);
 //				System.out.println("Size: " + data.metaList.size() + " - " + skippedAreaList.size());
 //				for (Area area : skippedAreaList) {
 //					g2d.fill(area.createTransformedArea(at));
 //				}
 				g2d.translate(-200, 0);
 			}
 		});
 		f.setVisible(true);
 		
 	}
 
 	public static void main2(String[] args) {
 		
 		final Area topology = new Area();
 //		topology.add(new Area(new Rectangle(100, 100, 200, 200)));
 //		topology.add(new Area(new Rectangle(350, 350, 50, 50)));
 //		topology.subtract(new Area(new Rectangle(110, 110, 50, 50)));
 //		topology.subtract(new Area(new Rectangle(200, 200, 50, 50)));
 //		topology.add(new Area(new Rectangle(115, 115, 10, 10)));
 		
 		final Area vision = new Area(new Rectangle(-Integer.MAX_VALUE/2, -Integer.MAX_VALUE/2, Integer.MAX_VALUE, Integer.MAX_VALUE));
 		
 		int pointCount = 0;
 		for (PathIterator iter = topology.getPathIterator(null); !iter.isDone(); iter.next()) {
 			pointCount++;
 		}
 		
 		final AreaData data = new AreaData(topology);
 		data.digest();
 		
 		JFrame f = new JFrame();
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setBounds(100, 100, 500, 500);
 		f.setLayout(new GridLayout());
 		f.add(new JPanel() {
 			Area theArea = null;
 			int x = 200;
 			int y = 200;
 			{
 				addMouseMotionListener(new MouseMotionAdapter() {
 					@Override
 					public void mouseDragged(MouseEvent e) {
 						
 						Dimension size = getSize();
 						x = e.getX();
 						y = e.getY();
 						
 						long start = System.currentTimeMillis();
 						theArea = calculateVisibility(x, y, vision, data);
 //						System.out.println("Calc: " + (System.currentTimeMillis() - start));
 						repaint();
 					}
 				});
 				addMouseListener(new MouseAdapter() {
 					@Override
 					public void mousePressed(MouseEvent e) {
 						
 						Dimension size = getSize();
 						x = e.getX();
 						y = e.getY();
 						
 						long start = System.currentTimeMillis();
 						theArea = calculateVisibility(x, y, vision, data);
 //						System.out.println("Calc: " + (System.currentTimeMillis() - start));
 						repaint();
 					}
 				});
 			}
 			@Override
 			protected void paintComponent(Graphics g) {
 
 				Dimension size = getSize();
 				g.setColor(Color.white);
 				g.fillRect(0, 0, size.width, size.height);
 				
 				Graphics2D g2d = (Graphics2D)g;
 
 				g.setColor(Color.gray);
 				g2d.fill(topology);
 				
 				g.setColor(Color.lightGray);
 				if (theArea != null) {
 					g2d.fill(theArea);
 				}
 				
 				g.setColor(Color.black);
 				g.drawLine(x, y-4, x, y+4);
 				g.drawLine(x-4, y, x+4, y);
 
 				g.setColor(Color.red);
 //				System.out.println("Size: " + data.metaList.size() + " - " + skippedAreaList.size());
 //				for (Area area : skippedAreaList) {
 //					g2d.fill(area);
 //				}
 				
 				for (AreaMeta areaMeta : data.getAreaList(new Point(0, 0))) {
 					g.setColor(Color.green);
 					g2d.fill(areaMeta.area);
 					g.setColor(Color.red);
 					g2d.draw(areaMeta.area);
 				}
 				
 			}
 		});
 		f.setVisible(true);
 		
 	}
 }
