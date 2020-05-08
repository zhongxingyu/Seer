 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
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
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Line2D;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import net.rptools.lib.CodeTimer;
 import net.rptools.maptool.client.AppUtil;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ui.zone.vbl.AreaOcean;
 import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
 import net.rptools.maptool.client.ui.zone.vbl.VisibleAreaSegment;
 import net.rptools.maptool.model.CellPoint;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Grid;
 import net.rptools.maptool.model.Path;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.util.GraphicsUtil;
 
 public class FogUtil {
 
 
 	public static Area calculateVisibility(int x, int y, Area vision, AreaTree topology) {
 		CodeTimer timer = new CodeTimer("5");
 
 		vision = new Area(vision);
 		vision.transform(AffineTransform.getTranslateInstance(x, y));
 
 		// sanity check
 //		if (topology.contains(x, y)) {
 //			return null;
 //		}
 
 		Point origin = new Point(x, y);
 
 		AreaOcean ocean = topology.getOceanAt(origin);
 		if (ocean == null) {
 			return null;
 		}
 
 		int blockCount = 0;
 		int skippedAreas = 0;
 
 		List<VisibleAreaSegment> segmentList = new ArrayList<VisibleAreaSegment>(ocean.getVisibleAreaSegments(origin));
 		Collections.sort(segmentList);
 
 		List<Area> clearedAreaList = new LinkedList<Area>();
 		for (VisibleAreaSegment segment : segmentList) {
 
 			boolean found = false;
 			for (Area clearedArea : clearedAreaList) {
 				if (clearedArea.contains(segment.getPath().getBounds())) {
 					skippedAreas ++;
 					found = true;
 					break;
 				}
 			}
 			if (found) {
 				continue;
 			}
 
 			Area area = segment.getArea();
 
 			timer.start("combine");
 			Area intersectedArea = null;
 			for (ListIterator<Area> iter = clearedAreaList.listIterator(); iter.hasNext();) {
 				Area clearedArea = iter.next();
 
 				if (clearedArea.intersects(segment.getPath().getBounds())) {
 					clearedArea.add(area);
 					iter.remove(); // we'll put it on the back of the list to prevent crazy growth at the front
 					intersectedArea = clearedArea;
 					break;
 				}
 			}
 			timer.stop("combine");
 
 			clearedAreaList.add(intersectedArea != null ? intersectedArea : area);
 		}
 
 		blockCount = segmentList.size();
 		int metaBlockCount = clearedAreaList.size();
 
 		while (clearedAreaList.size() > 1) {
 
 			Area a1 = clearedAreaList.remove(0);
 			Area a2 = clearedAreaList.remove(0);
 
 			a1.add(a2);
 			clearedAreaList.add(a1);
 		}
 
 		if (clearedAreaList.size() > 0) {
 			vision.subtract(clearedAreaList.get(0));
 		}
 
 //		System.out.println("Blocks: " + blockCount + " Skipped: " + skippedAreas + " metaBlocks: " + metaBlockCount );
 //		System.out.println(timer);
 
 		// For simplicity, this catches some of the edge cases
 
 
 		return vision;
 	}
 
 	private static class RelativeLine {
 		private final Line2D line;
 		private final double distance;
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
 
 			if (!token.getHasSight()) {
 				continue;
 			}
 			if(!AppUtil.playerOwns(token)){
 				continue;
 			}
 
 			Area tokenVision = renderer.getVisibleArea(token);
 
 			if (tokenVision != null) {
 				zone.exposeArea(tokenVision);
 				MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision);
 			}
 		}
 	}
 
 	public static void exposePCArea(ZoneRenderer renderer) {
 
 		Set<GUID> tokenSet = new HashSet<GUID>();
 		for (Token token : renderer.getZone().getPlayerTokens()) {
 
 			if (!token.getHasSight()) {
 				continue;
 			}
 
 			tokenSet.add(token.getId());
 		}
 		exposeVisibleArea(renderer, tokenSet);
 	}
 
 	public static void exposeLastPath(ZoneRenderer renderer, Set<GUID> tokenSet) {
 
 		if (!renderer.getZone().getGrid().getCapabilities().isPathingSupported() || !renderer.getZone().getGrid().getCapabilities().isSnapToGridSupported()) {
 			return;
 		}
 
 		Zone zone = renderer.getZone();
 		for (GUID tokenGUID : tokenSet) {
 			Token token = zone.getToken(tokenGUID);
 			if (token == null) {
 				continue;
 			}
 
 			if (!token.getHasSight()) {
 				continue;
 			}
 
 			if (!token.isSnapToGrid()) {
 				// We don't support this currently
 				continue;
 			}
 
 			Path<CellPoint> lastPath = (Path<CellPoint>) token.getLastPath();
 			if (lastPath == null) {
 				continue;
 			}
 
 			Grid grid = zone.getGrid();
 			Area visionArea = new Area();
 
 			Token tokenClone = new Token(token);
 			for (CellPoint cell : lastPath.getCellPath()) {
 
 				ZonePoint zp = grid.convert(cell);
 
 				tokenClone.setX(zp.x);
 				tokenClone.setY(zp.y);
 
 				Area currVisionArea = renderer.getZoneView().getVisibleArea(tokenClone);
 				if (currVisionArea != null) {
 					visionArea.add(currVisionArea);
 				}
 				renderer.getZoneView().flush(tokenClone);
 			}
 
 			zone.exposeArea(visionArea);
 			MapTool.serverCommand().exposeFoW(zone.getId(), visionArea);
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
 
 		Rectangle bounds = null;
 		if (token.isSnapToGrid()) {
 			bounds = token.getFootprint(grid).getBounds(grid, grid.convert(new ZonePoint(token.getX(), token.getY())));
 		} else {
 			bounds = token.getBounds(zone);
 		}
 
 		x = bounds.x + bounds.width/2;
 		y = bounds.y + bounds.height/2;
 
 		return new Point(x, y);
 	}
 
 
 	public static void main(String[] args) {
 
 		System.out.println("Creating topology");
 		final int topSize = 10000;
 		final Area topology = new Area();
 		Random r = new Random(12345);
 		for (int i = 0; i < 500; i++) {
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
 		final AreaTree tree = new AreaTree(topology);
 
 		// Make sure all classes are loaded
 		calculateVisibility(topSize/2, topSize/2, vision, tree);
 
 
 		Area area1 = new Area();
 //		JOptionPane.showMessageDialog(new JFrame(), "Hello");
 		long start = System.currentTimeMillis();
 		for (int i = 0; i < 1; i++) {
 			area1 = calculateVisibility(topSize/2, topSize/2, vision, tree);
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
 //						theArea = calculateVisibility5(x, y, vision, data);
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
 //						theArea = calculateVisibility5(x, y, vision, data);
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
 
 }
