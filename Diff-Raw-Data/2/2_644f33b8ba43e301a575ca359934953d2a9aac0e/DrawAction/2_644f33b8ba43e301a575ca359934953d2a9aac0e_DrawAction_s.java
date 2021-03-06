 // License: GPL. Copyright 2007 by Immanuel Scholz and others
 package org.openstreetmap.josm.actions.mapmode;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.awt.Cursor;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import javax.swing.KeyStroke;
 import javax.swing.JComponent;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.actions.GroupAction;
 import org.openstreetmap.josm.actions.mapmode.SelectAction.Mode;
 import org.openstreetmap.josm.command.AddCommand;
 import org.openstreetmap.josm.command.ChangeCommand;
 import org.openstreetmap.josm.command.Command;
 import org.openstreetmap.josm.command.SequenceCommand;
 import org.openstreetmap.josm.data.coor.EastNorth;
 import org.openstreetmap.josm.data.osm.Node;
 import org.openstreetmap.josm.data.osm.NodePair;
 import org.openstreetmap.josm.data.osm.OsmPrimitive;
 import org.openstreetmap.josm.data.osm.Way;
 import org.openstreetmap.josm.data.osm.WaySegment;
 import org.openstreetmap.josm.gui.MapFrame;
 import org.openstreetmap.josm.tools.ImageProvider;
 
 /**
  *
  */
 public class DrawAction extends MapMode {
 
 	public DrawAction(MapFrame mapFrame) {
 		super(tr("Draw"), "node/autonode", tr("Draw nodes"),
 			KeyEvent.VK_A, mapFrame, getCursor());
 
 		// Add extra shortcut N
 		Main.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
 			KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), tr("Draw"));
 
 		//putValue("help", "Action/AddNode/Autnode");
 	}
 
 	private static Cursor getCursor() {
 		try {
 	        return ImageProvider.getCursor("crosshair", null);
         } catch (Exception e) {
         }
 	    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
     }
 
 	@Override public void enterMode() {
 		super.enterMode();
 		Main.map.mapView.addMouseListener(this);
 	}
 
 	@Override public void exitMode() {
 		super.exitMode();
 		Main.map.mapView.removeMouseListener(this);
 	}
 
 	/**
 	 * If user clicked with the left button, add a node at the current mouse
 	 * position.
 	 *
 	 * If in nodeway mode, insert the node into the way. 
 	 */
 	@Override public void mouseClicked(MouseEvent e) {
 		if (e.getButton() != MouseEvent.BUTTON1)
 			return;
 
 		boolean ctrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
 		boolean alt = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
 		boolean shift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
 		
 		Collection<OsmPrimitive> selection = Main.ds.getSelected();
 		Collection<Command> cmds = new LinkedList<Command>();
 
 		ArrayList<Way> reuseWays = new ArrayList<Way>(),
 			replacedWays = new ArrayList<Way>();
 		boolean newNode = false;
 		Node n = null;
 		if (!ctrl) n = Main.map.mapView.getNearestNode(e.getPoint());
 		if (n == null) {
 			n = new Node(Main.map.mapView.getLatLon(e.getX(), e.getY()));
 			if (n.coor.isOutSideWorld()) {
 				JOptionPane.showMessageDialog(Main.parent,
 					tr("Cannot add a node outside of the world."));
 				return;
 			}
 			newNode = true;
 
 			cmds.add(new AddCommand(n));
 
 			if (!ctrl) {
 				// Insert the node into all the nearby way segments
 				List<WaySegment> wss = Main.map.mapView.getNearestWaySegments(e.getPoint());
 				Map<Way, List<Integer>> insertPoints = new HashMap<Way, List<Integer>>();
 				for (WaySegment ws : wss) {
 					List<Integer> is;
 					if (insertPoints.containsKey(ws.way)) {
 						is = insertPoints.get(ws.way);
 					} else {
 						is = new ArrayList<Integer>();
 						insertPoints.put(ws.way, is);
 					}
 
 					is.add(ws.lowerIndex);
 				}
 
 				Set<NodePair> segSet = new HashSet<NodePair>();
 
 				for (Map.Entry<Way, List<Integer>> insertPoint : insertPoints.entrySet()) {
 					Way w = insertPoint.getKey();
 					List<Integer> is = insertPoint.getValue();
 
 					Way wnew = new Way(w);
 
 					pruneSuccsAndReverse(is);
 					for (int i : is) segSet.add(
 						new NodePair(w.nodes.get(i), w.nodes.get(i+1)).sort());
 					for (int i : is) wnew.nodes.add(i + 1, n);
 
 					cmds.add(new ChangeCommand(insertPoint.getKey(), wnew));
 					replacedWays.add(insertPoint.getKey());
 					reuseWays.add(wnew);
 				}
 
 				adjustNode(segSet, n);
 			}
 		}
 		boolean extendedWay = false;
 		// shift modifier never connects, just makes new node
 		if (!shift && selection.size() == 1 && selection.iterator().next() instanceof Node) {
 			Node n0 = (Node) selection.iterator().next();
 			if (n0 == n) {
 				return; // Don't create zero length way segments.
 			}
 
 			// alt modifier makes connection to selected node but not existing way
 			Way way = alt ? null : getWayForNode(n0);
 			if (way == null) {
 				way = new Way();
 				way.nodes.add(n0);
 				cmds.add(new AddCommand(way));
 			} else {
 				int i;
 				if ((i = replacedWays.indexOf(way)) != -1) {
 					way = reuseWays.get(i);
 				} else {
 					Way wnew = new Way(way);
 					cmds.add(new ChangeCommand(way, wnew));
 					way = wnew;
 				}
 			}
 
 			if (way.nodes.get(way.nodes.size() - 1) == n0) {
 				way.nodes.add(n);
 			} else {
 				way.nodes.add(0, n);
 			}
 
 			extendedWay = true;
 		}
 
 		String title;
 		if (!extendedWay && !newNode) {
 			return; // We didn't do anything.
 		} else if (!extendedWay) {
 			if (reuseWays.isEmpty()) {
 				title = tr("Add node");
 			} else {
 				title = tr("Add node into way");
 			}
 		} else if (!newNode) {
 			title = tr("Connect existing way to node");
 		} else if (reuseWays.isEmpty()) {
 			title = tr("Add a new node to an existing way");
 		} else {
 			title = tr("Add node into way and connect");
 		}
 
 		Command c = new SequenceCommand(title, cmds);
 	
 		Main.main.undoRedo.add(c);
 		Main.ds.setSelected(n);
 		Main.map.mapView.repaint();
 	}
 	
 	/**
 	 * @return If the node is the end of exactly one way, return this. 
 	 * 	<code>null</code> otherwise.
 	 */
 	public static Way getWayForNode(Node n) {
 		Way way = null;
 		for (Way w : Main.ds.ways) {
			if (w.nodes.size() < 1) continue;
 			Node firstNode = w.nodes.get(0);
 			Node lastNode = w.nodes.get(w.nodes.size() - 1);
 			if ((firstNode == n || lastNode == n) && (firstNode != lastNode)) {
 				if (way != null)
 					return null;
 				way = w;
 			}
 		}
 		return way;
 	}
 
 	private static void pruneSuccsAndReverse(List<Integer> is) {
 		//if (is.size() < 2) return;
 
 		HashSet<Integer> is2 = new HashSet<Integer>();
 		for (int i : is) {
 			if (!is2.contains(i - 1) && !is2.contains(i + 1)) {
 				is2.add(i);
 			}
 		}
 		is.clear();
 		is.addAll(is2);
 		Collections.sort(is);
 		Collections.reverse(is);
 	}
 
 	/*
 	 * Adjusts the position of a node to lie on a segment (or a segment
 	 * intersection).
 	 * 
 	 * If one or more than two segments are passed, the node is adjusted
 	 * to lie on the first segment that is passed.
 	 * 
 	 * If two segments are passed, the node is adjusted to be at their
 	 * intersection.
 	 * 
 	 * No action is taken if no segments are passed.
 	 * 
 	 * @param segs the segments to use as a reference when adjusting
 	 * @param n the node to adjust
 	 */
 	private static void adjustNode(Collection<NodePair> segs, Node n) {
 		
 		switch (segs.size()) {
 		case 0:
 			return;
 		case 2:
 			// algorithm used here is a bit clumsy, anyone's welcome to replace
 			// it by something else. All it does it compute the intersection between
 			// the two segments and adjust the node position. The code doesnt 
 			Iterator<NodePair> i = segs.iterator();
 			NodePair seg = i.next();
 			EastNorth A = seg.a.eastNorth;
 			EastNorth B = seg.b.eastNorth;
 			seg = i.next();
 			EastNorth C = seg.a.eastNorth;
 			EastNorth D = seg.b.eastNorth;
 
 			EastNorth intersection = new EastNorth(
 				det(det(A.east(), A.north(), B.east(), B.north()), A.east() - B.east(),
 					det(C.east(), C.north(), D.east(), D.north()), C.east() - D.east())/
 					det(A.east() - B.east(), A.north() - B.north(), C.east() - D.east(), C.north() - D.north()),
 				det(det(A.east(), A.north(), B.east(), B.north()), A.north() - B.north(),
 					det(C.east(), C.north(), D.east(), D.north()), C.north() - D.north())/
 					det(A.east() - B.east(), A.north() - B.north(), C.east() - D.east(), C.north() - D.north())
 			);
 			
 			// only adjust to intersection if within 10 pixel of mouse click; otherwise
 			// fall through to default action.
 			// (for semi-parallel lines, intersection might be miles away!)
 			if (Main.map.mapView.getPoint(n.eastNorth).distance(Main.map.mapView.getPoint(intersection)) < 10) {
 				n.eastNorth = intersection;
 				return;
 			}
 		
 		default:
 			EastNorth P = n.eastNorth;
 			seg = segs.iterator().next();
 			A = seg.a.eastNorth;
 			B = seg.b.eastNorth;
 			double a = P.distance(B);
 			double b = P.distance(A);
 			double c = A.distance(B);
 			double q = (a - b + c) / (2*c);
 			n.eastNorth = new EastNorth(
 				B.east() + q * (A.east() - B.east()),
 				B.north() + q * (A.north() - B.north()));
 		}
 	}
 	
 	// helper for adjustNode
 	static double det(double a, double b, double c, double d)
 	{
 		return a * d - b * c;
 	}
 
 	
 	@Override public String getModeHelpText() {
 		return "Click to add a new node. Ctrl: no node re-use/auto-insert. Shift: no auto-connect. Alt: new way";
 	}
 }
