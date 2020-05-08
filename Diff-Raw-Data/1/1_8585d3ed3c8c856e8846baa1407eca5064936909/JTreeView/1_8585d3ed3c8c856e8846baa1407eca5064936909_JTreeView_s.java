 package gui.components;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.DebugGraphics;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.text.StyleContext;
 
 import util.Tree;
 
 import database.match.Match;
 import database.players.Player;
 import database.tournamentParts.KnockOut;
 
 public class JTreeView extends JPanel {
 
 	/**
 	 * Distances between the cells
 	 */
 	private static final int dx1 = 10;
 	private static int dx2 = 0;
 	private static final int dy1 = 5;
 	/**
 	 * Height and Length of a Cell
 	 */
 	private static int h = 19;
 	private static int l = 0;
 	/**
 	 * Offset for Window Decoration
 	 */
 	private static final int seedX = 0, seedY = 10 + h;
 
 	private Tree tree;
 	private Map<Tree, Player> players;
 	private Map<Tree, Match> matches;
 
 	public JTreeView(KnockOut ko) {
 		super();
 		tree = ko.getTree();
 		players = ko.getPlayersMap();
 		matches = ko.getMatchesMap();
 		
 		FontMetrics fm = StyleContext.getDefaultStyleContext().getFontMetrics(
 				UIManager.getDefaults().getFont("TextField.font"));
 		for (Entry<Tree, Player> p : players.entrySet())
 			l = Math.max(fm.stringWidth(p.getValue().toString()) + 8, l);
 		h = fm.getHeight() + 4;
 		for (Entry<Tree, Match> m : matches.entrySet())
 			dx2 = Math.max(fm.stringWidth(m.getValue().edgePrintBottom()) + 8,
 					dx2);
 		setPreferredSize(new Dimension(tree.depth() * (l + dx1 + dx2) - dx2 + l + seedX,
 				tree.countLeaves() * (h + dy1) - dy1 + seedY));
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		for (Entry<Tree, Player> p : players.entrySet())
 			l = Math.max(g.getFontMetrics()
 					.stringWidth(p.getValue().toString()) + 8, l);
 		h = g.getFontMetrics().getHeight() + 4;
 		for (Entry<Tree, Match> m : matches.entrySet())
 			dx2 = Math.max(
 					g.getFontMetrics().stringWidth(
 							m.getValue().edgePrintBottom()) + 8, dx2);
 		g.setColor(Color.BLACK);
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		Point pnt = new Point((l + dx1 + dx2) * (tree.depth() - 1) + l + seedX,
 				seedY);
 		drawTree(pnt, (Graphics2D) g, tree);
 		setPreferredSize(new Dimension(pnt.x + l + dx1, pnt.y - h));
 	}
 
 	private int drawTree(Point p, Graphics2D g, Tree t) {
 		int[] ys = new int[2];
 		p.x -= dx1 + dx2 + l;
 		int i = 0;
 		for (Tree t1 : t.getChildren()) {
 			ys[i] = drawTree(p, g, t1);
 			i++;
 		}
 		int y = p.y;
 		if (t.hasChildren()) {
 			y = (ys[0] + ys[1]) / 2;
 			g.drawLine(p.x + l, ys[0] - h / 2, p.x + l + dx1, ys[0] - h / 2);
 			g.drawLine(p.x + l, ys[1] - h / 2, p.x + l + dx1, ys[1] - h / 2);
 			g.drawLine(p.x + l + dx1, ys[0] - h / 2, p.x + l + dx1, ys[1] - h
 					/ 2);
 			g.drawLine(p.x + l + dx1, y - h / 2, p.x + l + dx1 + dx2, y - h / 2);
 			if (matches.containsKey(t)) {
 				Match m = matches.get(t);
 				g.drawString(m.edgePrintTop(), p.x + l + dx1 + 2, y - h / 2 - 3);
 				g.drawString(m.edgePrintBottom(), p.x + l + dx1 + 2, y + h / 2
 						- 5);
 			}
 		} else {
 			p.y += h + dy1;
 		}
 		p.x += dx1 + dx2 + l;
 		if (players.containsKey(t))
 			g.drawString(players.get(t).toString(), p.x + 4, y - 2
 					- g.getFontMetrics().getDescent());
 
 		// draw box
 		g.drawLine(p.x, y, p.x + l, y); // bottom line
 		g.drawLine(p.x, y, p.x, y - h); // left line
 		g.drawLine(p.x + l, y, p.x + l, y - h); // right line
 		g.drawLine(p.x, y - h, p.x + l, y - h); // top line
 
 		return y;
 	}
 
 }
