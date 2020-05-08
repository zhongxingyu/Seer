 package pleocmd.itfc.gui;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Line2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.AbstractButton;
 import javax.swing.Icon;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.MenuElement;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 
 import pleocmd.Log;
 import pleocmd.cfg.ConfigValue;
 import pleocmd.exc.InternalException;
 import pleocmd.exc.PipeException;
 import pleocmd.exc.StateException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.itfc.gui.icons.IconLoader;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.PipePart;
 import pleocmd.pipe.PipePartDetection;
 import pleocmd.pipe.PipePart.HelpKind;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 // TODO auto ordering of PipeParts via A* algorithm
 // TODO raster
 // TODO width of parts according to label
 public final class PipeConfigBoard extends JPanel {
 
 	public static final int DEF_RECT_WIDTH = 150;
 
 	public static final int DEF_RECT_HEIGHT = 20;
 
 	private static final boolean PAINT_DEBUG = false;
 
 	private static final long serialVersionUID = -4525676341777864359L;
 
 	private static final Color BACKGROUND = Color.LIGHT_GRAY;
 
 	private static final Color MOVEMENT_HINT = new Color(192, 208, 192);
 
 	private static final Color ORDER_HINT_BACK = new Color(255, 255, 128);
 
 	private static final Color SECT_BORDER = Color.BLACK;
 
 	private static final Color RECT_BACKGROUND = new Color(255, 255, 255);
 
 	private static final Color INNER_MODIFYABLE = new Color(200, 200, 255);
 
 	private static final Color INNER_READONLY = Color.LIGHT_GRAY;
 
 	private static final Color OUTER_OK = Color.BLACK;
 
 	private static final Color OUTER_BAD = Color.RED;
 
 	private static final Color OUTER_SEL_OK = Color.BLUE;
 
 	private static final Color OUTER_SEL_BAD = Color.MAGENTA;
 
 	private static final Color CONNECTION_OK = Color.BLACK;
 
 	private static final Color CONNECTION_BAD = Color.RED;
 
 	private static final Color CONNECTION_SEL_OK = Color.BLUE;
 
 	private static final Color CONNECTION_SEL_BAD = Color.MAGENTA;
 
 	private static final Color SHADOW_COLOR = Color.GRAY;
 
 	private static final int SHADOW_DEPTH = 4;
 
 	private static final boolean SHADOW_ORDERHINT = true;
 
 	private static final boolean SHADOW_RECTS = true;
 
 	private static final boolean SHADOW_CONNECTIONS = false;
 
 	private static final int ARROW_TIP = 14;
 
 	private static final int ARROW_WING = 8;
 
 	private static final int ICON_WIDTH = 18;
 
 	private static final Color ICON_OUTLINE = Color.GRAY;
 
 	private static final Color ICON_HOVER = Color.BLUE;
 
 	private static final Color ICON_SELECTED = new Color(128, 128, 0);
 
 	private static final Icon ICON_CONF = IconLoader.getIcon("configure");
 
 	private static final int ICON_CONF_POS = 1;
 
 	private static final Icon ICON_DGR = IconLoader.getIcon("games-difficult");
 
 	private static final int ICON_DGR_POS = 0;
 
 	private static final int INNER_WIDTH = ICON_WIDTH + 2;
 
 	private static final int INNER_HEIGHT = 6;
 
 	private static final double LINE_CLICK_DIST = 10;
 
 	private static final int SECTION_FRAC = 5;
 
 	private static final int SECTION_SPACE = 20;
 
 	private static final double ORDER_HINT_TRUNK_WIDTH = 0.3;
 
 	private static final double ORDER_HINT_TRUNK_HEIGHT = 0.65;
 
 	private static final double ORDER_HINT_ARROW_WIDTH = 0.3;
 
 	private static final double ORDER_HINT_ARROW_HEIGHT = 0.3;
 
 	private static final int GROW_LABEL_REDRAW = 14;
 
 	private final JPopupMenu menuInput;
 
 	private final JPopupMenu menuConverter;
 
 	private final JPopupMenu menuOutput;
 
 	private int idxMenuAdd;
 
 	private final Set<PipePart> set;
 
 	private final Dimension bounds = new Dimension();
 
 	private int border1;
 
 	private int border2;
 
 	private Point handlePoint;
 
 	private PipePart currentPart;
 
 	private Icon currentIcon;
 
 	private Rectangle currentConnection;
 
 	private PipePart currentConnectionsTarget;
 
 	private boolean currentConnectionValid;
 
 	private PipePart underCursor;
 
 	private int grayVal = 128;
 
 	private int idxMenuConfPart;
 
 	private int idxMenuDelPart;
 
 	private int idxMenuDelPartConn;
 
 	private int idxMenuDelConn;
 
 	private int idxMenuToggleDgr;
 
 	private final Set<PipePart> saneConfigCache;
 
 	private boolean modifyable;
 
 	private double scale = 1.0;
 
 	private boolean delayedReordering;
 
 	private Point lastMenuLocation;
 
 	public PipeConfigBoard() {
 		setPreferredSize(new Dimension(400, 300));
 
 		menuInput = createMenu("Input", Input.class);
 		menuConverter = createMenu("Converter", Converter.class);
 		menuOutput = createMenu("Output", Output.class);
 
 		set = new HashSet<PipePart>();
 		for (final PipePart pp : Pipe.the().getInputList())
 			set.add(pp);
 		for (final PipePart pp : Pipe.the().getConverterList())
 			set.add(pp);
 		for (final PipePart pp : Pipe.the().getOutputList())
 			set.add(pp);
 
 		saneConfigCache = new HashSet<PipePart>();
 		updateSaneConfigCache();
 
 		updateState();
 
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(final MouseEvent e) {
 				if (e.isPopupTrigger())
 					showPopup(e);
 				else
 					updateCurrent(e.getPoint());
 			}
 
 			@Override
 			public void mouseClicked(final MouseEvent e) {
 				updateCurrent(e.getPoint());
 				if (e.getModifiers() == InputEvent.BUTTON1_MASK)
 					switch (e.getClickCount()) {
 					case 1:
 						checkIconClicked();
 						break;
 					case 2:
 						configureCurrentPart(true);
 						break;
 					}
 			}
 
 			@Override
 			public void mouseReleased(final MouseEvent e) {
 				// if (e.isPopupTrigger()) showPopup(e);
 				releaseCurrent();
 			}
 
 			private void showPopup(final MouseEvent e) {
 				updateCurrent(e.getPoint());
 				showMenu(PipeConfigBoard.this, e.getX(), e.getY());
 			}
 		});
 
 		addMouseMotionListener(new MouseMotionListener() {
 			@Override
 			public void mouseDragged(final MouseEvent e) {
 				if (e.getModifiers() != InputEvent.BUTTON1_MASK) return;
 				PipeConfigBoard.this.mouseDragged(e.getPoint());
 			}
 
 			@Override
 			public void mouseMoved(final MouseEvent e) {
 				PipeConfigBoard.this.mouseMoved(e.getPoint());
 			}
 		});
 
 		addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentResized(final ComponentEvent e) {
 				updateBounds(getWidth(), getHeight());
 			}
 		});
 		ToolTipManager.sharedInstance().registerComponent(this);
 	}
 
 	private JPopupMenu createMenu(final String name,
 			final Class<? extends PipePart> clazz) {
 		final JPopupMenu menu = new JPopupMenu();
 
 		idxMenuAdd = menu.getSubElements().length;
 		final JMenu menuAdd = new JMenu("Add " + name);
 		menu.add(menuAdd);
 		for (final Class<? extends PipePart> pp : PipePartDetection.ALL_PIPEPART)
 			if (clazz.isAssignableFrom(pp)) {
 				final JMenuItem item = new JMenuItem(PipePartDetection
 						.callHelp(pp, HelpKind.Name));
 				menuAdd.add(item);
 				item.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(final ActionEvent e) {
 						addPipePart(pp, getLastMenuLocation());
 					}
 				});
 			}
 		menu.addSeparator();
 
 		idxMenuConfPart = menu.getSubElements().length;
 		final JMenuItem itemConfPart = new JMenuItem("Configure This PipePart",
 				ICON_CONF);
 		itemConfPart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				configureCurrentPart(false);
 			}
 		});
 		menu.add(itemConfPart);
 
 		idxMenuDelPart = menu.getSubElements().length;
 		final JMenuItem itemDelPart = new JMenuItem("Delete This PipePart");
 		itemDelPart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentPart();
 			}
 		});
 		menu.add(itemDelPart);
 
 		idxMenuDelPartConn = menu.getSubElements().length;
 		final JMenuItem itemDelPartConn = new JMenuItem(
 				"Delete Connections Of This PipePart");
 		itemDelPartConn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentPartsConnections();
 			}
 		});
 		menu.add(itemDelPartConn);
 
 		idxMenuDelConn = menu.getSubElements().length;
 		final JMenuItem itemDelConn = new JMenuItem("Delete This Connection");
 		itemDelConn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				removeCurrentConnection();
 			}
 		});
 		menu.add(itemDelConn);
 
 		idxMenuToggleDgr = menu.getSubElements().length;
 		final JCheckBoxMenuItem itemToggleDgr = new JCheckBoxMenuItem(
 				"Visualize PipePart's Output", ICON_DGR);
 		itemToggleDgr.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				toggleDiagram();
 			}
 		});
 		menu.add(itemToggleDgr);
 
 		return menu;
 	}
 
 	protected Point getLastMenuLocation() {
 		return lastMenuLocation;
 	}
 
 	protected void removeCurrentConnection() {
 		if (currentPart != null && currentConnectionsTarget != null
 				&& ensureModifyable()) {
 			try {
 				currentPart.disconnectFromPipePart(currentConnectionsTarget);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot delete connection");
 			}
 			currentConnection = null;
 			currentConnectionsTarget = null;
 			currentConnectionValid = false;
 			updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void removeCurrentPartsConnections() {
 		if (currentPart != null && ensureModifyable()) {
 			final Set<PipePart> copy = new HashSet<PipePart>(currentPart
 					.getConnectedPipeParts());
 			try {
 				for (final PipePart pp : copy)
 					currentPart.disconnectFromPipePart(pp);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot delete connections");
 			}
 			currentConnection = null;
 			currentConnectionsTarget = null;
 			currentConnectionValid = false;
 			updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void removeCurrentPart() {
 		if (currentPart != null && ensureModifyable()) {
 			try {
 				for (final PipePart srcPP : set)
 					if (srcPP.getConnectedPipeParts().contains(currentPart))
 						srcPP.disconnectFromPipePart(currentPart);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot delete connections");
 			}
 			set.remove(currentPart);
 			try {
 				if (currentPart instanceof Input)
 					Pipe.the().removeInput((Input) currentPart);
 				else if (currentPart instanceof Converter)
 					Pipe.the().removeConverter((Converter) currentPart);
 				else if (currentPart instanceof Output)
 					Pipe.the().removeOutput((Output) currentPart);
 				else
 					throw new InternalException(
 							"Invalid sub-class of PipePart '%s'", currentPart);
 			} catch (final StateException e) {
 				Log.error(e, "Cannot remove PipePart '%s'", currentPart);
 			}
 			currentPart = null;
 			currentConnection = null;
 			currentConnectionsTarget = null;
 			currentConnectionValid = false;
 			handlePoint = null;
 			updateSaneConfigCache();
 			repaint();
 		}
 	}
 
 	protected void configureCurrentPart(final boolean onlyIfNoIcon) {
 		if (currentPart != null && !currentPart.getGuiConfigs().isEmpty()
 				&& (!onlyIfNoIcon || currentIcon == null) && ensureModifyable()) {
 			createConfigureDialog("Configure", currentPart, null);
 			repaint();
 		}
 	}
 
 	protected void checkIconClicked() {
 		if (currentIcon == ICON_CONF) configureCurrentPart(false);
 		if (currentIcon == ICON_DGR) toggleDiagram();
 	}
 
 	protected void toggleDiagram() {
 		if (currentPart != null)
 			currentPart.setVisualize(!currentPart.isVisualize());
 	}
 
 	protected Set<PipePart> getSet() {
 		return set;
 	}
 
 	protected void updateSaneConfigCache() {
 		final Set<PipePart> sane = Pipe.the().getSanePipeParts();
 		if (!saneConfigCache.equals(sane)) {
 			saneConfigCache.clear();
 			saneConfigCache.addAll(sane);
 			repaint();
 		}
 	}
 
 	@Override
 	public void paintComponent(final Graphics g) {
 		final Rectangle clip = g.getClipBounds();
 		final BufferedImage img = new BufferedImage(clip.width, clip.height,
 				BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g2 = img.createGraphics();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g2.setClip(0, 0, clip.width, clip.height);
 
 		final Rectangle clipOrg = new Rectangle((int) (clip.x / scale),
 				(int) (clip.y / scale), (int) (clip.width / scale),
 				(int) (clip.height / scale));
 
 		final long start = System.currentTimeMillis();
 		if (PAINT_DEBUG) {
 			grayVal = (grayVal - 118) % 64 + 128;
 			g2.setColor(new Color(grayVal, grayVal, grayVal));
 		} else
 			g2.setColor(BACKGROUND);
 		g2.fillRect(0, 0, clip.width, clip.height);
 		g2.translate(-clip.x, -clip.y);
 		g2.scale(scale, scale);
 		drawMovementHint(g2);
 		final long time1 = System.currentTimeMillis();
 
 		if (clip.x < border1) drawOrderingHint(g2);
 		final long time2 = System.currentTimeMillis();
 		drawSectionBorders(g2);
 		final long time3 = System.currentTimeMillis();
 		final int cnt4 = drawPipeParts(g2, clipOrg);
 		final long time4 = System.currentTimeMillis();
 		final int cnt5 = drawConnections(g2, clipOrg);
 		final long time5 = System.currentTimeMillis();
 
 		if (PAINT_DEBUG) {
 			g2.translate(clip.x / scale, clip.y / scale);
 			drawDebugTime(g2, time1 - start, -1, "Background", 1);
 			drawDebugTime(g2, time2 - time1, -1, "Hint", 2);
 			drawDebugTime(g2, time3 - time2, -1, "Border", 3);
 			drawDebugTime(g2, time4 - time3, cnt4, "Parts", 4);
 			drawDebugTime(g2, time5 - time4, cnt5, "Conn's", 5);
 		}
 
 		g.drawImage(img, clip.x, clip.y, null);
 	}
 
 	private void drawDebugTime(final Graphics2D g2, final long elapsed,
 			final int count, final String name, final int pos) {
 		final Font f = g2.getFont();
 		g2.setFont(f.deriveFont(10f));
 		g2.setColor(Color.GREEN);
 		final String str = count == -1 ? name : String.format("%d %s", count,
 				name);
 		g2.drawString(String.format("%d ms for %s", elapsed, str), 0, 10 * pos);
 		g2.setFont(f);
 	}
 
 	private void drawMovementHint(final Graphics2D g2) {
 		final List<? extends PipePart> list;
 		final int x0;
 		final int x1;
 		if (currentPart instanceof Input) {
 			list = Pipe.the().getInputList();
 			x0 = 0;
 			x1 = border1;
 		} else if (currentPart instanceof Converter) {
 			list = Pipe.the().getConverterList();
 			x0 = border1;
 			x1 = border2;
 		} else if (currentPart instanceof Output) {
 			list = Pipe.the().getOutputList();
 			x0 = border2;
 			x1 = bounds.width;
 		} else
 			return;
 		final int idx = list.indexOf(currentPart);
 		final PipePart before = idx > 0 ? list.get(idx - 1) : null;
 		final PipePart after = idx < list.size() - 1 ? list.get(idx + 1) : null;
 
 		final int y0 = before == null ? 0 : before.getGuiPosition().y
 				+ before.getGuiPosition().height + 1;
 		final int y1 = after == null ? bounds.height
 				: after.getGuiPosition().y - 1;
 		final Rectangle r = new Rectangle(x0, y0, x1 - x0, y1 - y0);
 		if (PAINT_DEBUG)
 			g2.setColor(new Color(grayVal, grayVal + 16, grayVal));
 		else
 			g2.setColor(MOVEMENT_HINT);
 		g2.fill(r);
 	}
 
 	private void drawOrderingHint(final Graphics2D g2) {
 		final int tw = (int) (border1 * ORDER_HINT_TRUNK_WIDTH);
 		final int th = (int) (bounds.height * ORDER_HINT_TRUNK_HEIGHT);
 		final int aw = (int) (border1 * ORDER_HINT_ARROW_WIDTH);
 		final int ah = (int) (bounds.height * ORDER_HINT_ARROW_HEIGHT);
 		final int cw = tw + 2 * aw;
 		final int ch = th + ah;
 		final int ow = (border1 - cw) / 2;
 		final int oh = (bounds.height - ch) / 2;
 		final Polygon p = new Polygon();
 		p.addPoint(ow + aw, oh);
 		p.addPoint(ow + aw + tw, oh);
 		p.addPoint(ow + aw + tw, oh + th);
 		p.addPoint(ow + aw + tw + aw, oh + th);
 		p.addPoint(ow + aw + tw / 2, oh + th + ah);
 		p.addPoint(ow, oh + th);
 		p.addPoint(ow + aw, oh + th);
 		p.addPoint(ow + aw, oh);
 
 		if (SHADOW_ORDERHINT && SHADOW_DEPTH > 0) {
 			final AffineTransform at = g2.getTransform();
 			g2.translate(SHADOW_DEPTH, SHADOW_DEPTH);
 			g2.setColor(SHADOW_COLOR);
 			g2.fillPolygon(p);
 			g2.setTransform(at);
 		}
 
 		g2.setColor(ORDER_HINT_BACK);
 		g2.fillPolygon(p);
 	}
 
 	private void drawSectionBorders(final Graphics2D g2) {
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_BEVEL, 0, new float[] { 2, 2 }, 0));
 		g2.setColor(SECT_BORDER);
 		g2.drawLine(border1, 0, border1, bounds.height);
 		g2.drawLine(border2, 0, border2, bounds.height);
 	}
 
 	private int drawPipeParts(final Graphics2D g2, final Rectangle clip) {
 		int cnt = 0;
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_BEVEL, 0, null, 0));
 		for (final PipePart pp : set)
 			cnt += drawPipePart(g2, pp, pp == underCursor, clip);
 		return cnt;
 	}
 
 	private int drawConnections(final Graphics2D g2, final Rectangle clip) {
 		int cnt = 0;
 		for (final PipePart src : set)
 			for (final PipePart trg : src.getConnectedPipeParts()) {
 				final boolean sel = currentPart == src
 						&& currentConnectionsTarget == trg;
 				if (saneConfigCache.contains(src))
 					g2.setColor(sel ? CONNECTION_SEL_OK : CONNECTION_OK);
 				else
 					g2.setColor(sel ? CONNECTION_SEL_BAD : CONNECTION_BAD);
 				cnt += drawConnection(g2, src.getGuiPosition(), trg
 						.getGuiPosition(), src, trg, clip);
 			}
 		if (currentConnection != null && currentConnectionsTarget == null) {
 			g2.setColor(currentConnectionValid ? CONNECTION_SEL_OK
 					: CONNECTION_SEL_BAD);
 			cnt += drawConnection(g2, currentPart.getGuiPosition(),
 					currentConnection, currentPart, underCursor, clip);
 		}
 		return cnt;
 	}
 
 	private int drawPipePart(final Graphics2D g2, final PipePart part,
 			final boolean hover, final Rectangle clip) {
 		final Rectangle rect = part.getGuiPosition();
 		if (!rect.intersects(clip)) return 0;
 
 		final Color outerClr;
 		if (saneConfigCache.contains(part))
 			outerClr = part == currentPart ? OUTER_SEL_OK : OUTER_OK;
 		else
 			outerClr = part == currentPart ? OUTER_SEL_BAD : OUTER_BAD;
 
 		if (SHADOW_RECTS && SHADOW_DEPTH > 0) {
 			final AffineTransform at = g2.getTransform();
 			g2.translate(SHADOW_DEPTH, SHADOW_DEPTH);
 			g2.setColor(SHADOW_COLOR);
 			g2.fill(rect);
 			g2.setTransform(at);
 		}
 
 		g2.setColor(RECT_BACKGROUND);
 		g2.fill(rect);
 
 		g2.setColor(outerClr);
 		g2.draw(rect);
 
 		final String s = part.getName();
 		final Rectangle2D sb = g2.getFontMetrics().getStringBounds(s, g2);
 
 		if (hover) {
 			final Rectangle inner = new Rectangle(rect);
 			inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
 			g2.setColor(modifyable ? INNER_MODIFYABLE : INNER_READONLY);
 			g2.fill(inner);
 		}
 
 		final Shape shape = g2.getClip();
 		g2.clipRect(rect.x, rect.y, rect.width, rect.height);
 		g2.setColor(outerClr);
 		g2.drawString(s, (float) (rect.x + (rect.width - sb.getWidth()) / 2),
 				(float) (rect.y + sb.getHeight() + (rect.height - sb
 						.getHeight()) / 2));
 		drawIcon(g2, hover, rect, ICON_CONF, ICON_CONF_POS, !modifyable
 				|| part.getGuiConfigs().isEmpty(), false);
 		drawIcon(g2, hover, rect, ICON_DGR, ICON_DGR_POS, false, part
 				.isVisualize());
 		g2.setClip(shape);
 		return 1;
 	}
 
 	/**
 	 * Draws an icon to the image
 	 * 
 	 * @param g2
 	 *            {@link Graphics2D} of the image
 	 * @param hover
 	 *            if true, an outline will be drawn
 	 * @param rect
 	 *            position of the image in which to align the icon
 	 * @param icon
 	 *            {@link Icon} to draw
 	 * @param pos
 	 *            the position inside the rectangle as follow<br>
 	 *            [ 1 2 3 ..... -2 -1 0 ]
 	 * @param disabled
 	 *            if true, icon is drawn in disabled state
 	 * @param selected
 	 *            if true, icon is drawn in selected state
 	 */
 	private void drawIcon(final Graphics2D g2, final boolean hover,
 			final Rectangle rect, final Icon icon, final int pos,
 			final boolean disabled, final boolean selected) {
 		final Rectangle b = getIconBounds(rect, icon, pos);
 		if (hover) {
 			g2.setColor(modifyable ? INNER_MODIFYABLE : INNER_READONLY);
 			g2.fill(b);
 		}
 		Icon ico = disabled ? (selected ? UIManager.getLookAndFeel()
 				.getDisabledSelectedIcon(null, icon) : UIManager
 				.getLookAndFeel().getDisabledIcon(null, icon)) : icon;
 		if (ico == null) ico = icon;
 		ico.paintIcon(null, g2, b.x, b.y);
 		g2.setColor(selected ? ICON_SELECTED : hover ? ICON_HOVER
 				: ICON_OUTLINE);
 		g2.draw3DRect(b.x, b.y, b.width, b.height, !selected);
 	}
 
 	/**
 	 * Gets the bounding rectangle around an icon.
 	 * 
 	 * @param rect
 	 *            position of the image in which to align the icon
 	 * @param icon
 	 *            {@link Icon} which would be drawn
 	 * @param pos
 	 *            the position inside the rectangle as follow<br>
 	 *            [ 1 2 3 ..... -2 -1 0 ]
 	 * @return the bounding rectangle
 	 */
 	private Rectangle getIconBounds(final Rectangle rect, final Icon icon,
 			final int pos) {
 		final boolean alignRight = pos <= 0;
 		final Rectangle b = new Rectangle();
 		b.width = icon.getIconWidth();
 		b.height = icon.getIconHeight();
 		if (alignRight)
 			b.x = rect.x + rect.width - (1 - pos) * ICON_WIDTH;
 		else
 			b.x = rect.x + pos * ICON_WIDTH - b.width;
 		b.y = rect.y + (rect.height - b.width) / 2;
 		return b;
 	}
 
 	private int drawConnection(final Graphics2D g2, final Rectangle srcRect,
 			final Rectangle trgRect, final PipePart src, final PipePart trg,
 			final Rectangle clip) {
 		if (!srcRect.union(trgRect).intersects(clip)) return 0;
 
 		final Point srcPoint = new Point();
 		final Point trgPoint = new Point();
 		calcConnectorPositions(srcRect, trgRect, srcPoint, trgPoint);
 		final Polygon arrow = getArrowPolygon(srcPoint.x, srcPoint.y,
 				trgPoint.x, trgPoint.y);
 
 		if (SHADOW_CONNECTIONS && SHADOW_DEPTH > 0) {
 			final Color clr = g2.getColor();
 			final AffineTransform at = g2.getTransform();
 			g2.translate(SHADOW_DEPTH / 2, SHADOW_DEPTH / 2);
 			g2.setColor(SHADOW_COLOR);
 			g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
 			g2.fillPolygon(arrow);
 			drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x,
 					trgPoint.y, src.getOutputDescription());
 			drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x,
 					srcPoint.y, trg == null ? "" : trg.getInputDescription());
 			g2.setTransform(at);
 			g2.setColor(clr);
 		}
 
 		g2.drawLine(srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y);
 		g2.fillPolygon(arrow);
 
 		drawConnectorLabel(g2, srcPoint.x, srcPoint.y, trgPoint.x, trgPoint.y,
 				src.getOutputDescription());
 		drawConnectorLabel(g2, trgPoint.x, trgPoint.y, srcPoint.x, srcPoint.y,
 				trg == null ? "" : trg.getInputDescription());
 		return 1;
 	}
 
 	private void drawConnectorLabel(final Graphics2D g2, final int sx,
 			final int sy, final int tx, final int ty, final String str) {
 		if (str.isEmpty()) return;
 
 		// draw in image
 		final Rectangle sb = g2.getFontMetrics().getStringBounds(str, g2)
 				.getBounds();
 		final BufferedImage img = new BufferedImage((int) (sb.width * scale),
 				(int) (sb.height * scale), BufferedImage.TYPE_INT_ARGB);
 		final Graphics2D imgG2D = img.createGraphics();
 		imgG2D.scale(scale, scale);
 		imgG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		if (PAINT_DEBUG) {
 			imgG2D.setColor(Color.YELLOW);
 			imgG2D.fillRect(0, 0, sb.width, sb.height);
 		}
 		imgG2D.setColor(g2.getColor());
 		imgG2D.setFont(g2.getFont());
 		imgG2D.drawString(str, 0, sb.height);
 
 		// make sure text is never bottom-up and correctly positioned
 		double d = Math.atan2(sy - ty, sx - tx);
 		int xoffs = 16;
 		if (d > Math.PI / 2)
 			d -= Math.PI;
 		else if (d < -Math.PI / 2.0)
 			d += Math.PI;
 		else
 			xoffs = (int) (-sb.width * scale - xoffs);
 
 		// draw rotated image
 		final Shape shape = g2.getClip();
 		final AffineTransform at = g2.getTransform();
 
 		// only use translation from original transformation
 		// scaling is already done in imgG2D
 		g2.setTransform(new AffineTransform());
 		g2.translate(at.getTranslateX() + sx * scale, at.getTranslateY() + sy
 				* scale);
 		g2.rotate(d);
 		final int len = (int) (Math.sqrt((sx - tx) * (sx - tx) + (sy - ty)
 				* (sy - ty)) * scale);
 		if (xoffs < 0)
 			g2.clipRect(-len / 2, 0, len / 2, img.getHeight());
 		else
 			g2.clipRect(0, 0, len / 2, img.getHeight());
 		g2.drawImage(img, new AffineTransformOp(new AffineTransform(),
 				AffineTransformOp.TYPE_BILINEAR), xoffs, 0);
 		g2.setTransform(at);
 		g2.setClip(shape);
 	}
 
 	private static void calcConnectorPositions(final Rectangle srcRect,
 			final Rectangle trgRect, final Point srcPos, final Point trgPos) {
 		// calculate center of source and target
 		final int csx = srcRect.x + srcRect.width / 2;
 		final int csy = srcRect.y + srcRect.height / 2;
 		final int ctx = trgRect.x + trgRect.width / 2;
 		final int cty = trgRect.y + trgRect.height / 2;
 
 		// determine side of rectangle for the connection
 		final int dcx = ctx - csx;
 		final int dcy = cty - csy;
 		if (dcx > Math.abs(dcy)) {
 			// target is right of source
 			if (srcPos != null)
 				srcPos.setLocation(srcRect.x + srcRect.width, csy);
 			if (trgPos != null) trgPos.setLocation(trgRect.x, cty);
 		} else if (dcy >= Math.abs(dcx)) {
 			// target is below source
 			if (srcPos != null)
 				srcPos.setLocation(csx, srcRect.y + srcRect.height);
 			if (trgPos != null) trgPos.setLocation(ctx, trgRect.y);
 		} else if (dcx < 0 && Math.abs(dcx) > Math.abs(dcy)) {
 			// target is left of source
 			if (srcPos != null) srcPos.setLocation(srcRect.x, csy);
 			if (trgPos != null)
 				trgPos.setLocation(trgRect.x + trgRect.width, cty);
 		} else {
 			// target is above source
 			if (srcPos != null) srcPos.setLocation(csx, srcRect.y);
 			if (trgPos != null)
 				trgPos.setLocation(ctx, trgRect.y + trgRect.height);
 		}
 	}
 
 	// from http://www.java-forums.org/awt-swing/
 	// 5842-how-draw-arrow-mark-using-java-swing.html
 	private static Polygon getArrowPolygon(final int sx, final int sy,
 			final int tx, final int ty) {
 		final Polygon p = new Polygon();
 		final double d = Math.atan2(sx - tx, sy - ty);
 		// tip
 		p.addPoint(tx, ty);
 		// wing 1
 		p.addPoint(tx + xCor(ARROW_TIP, d + .5), ty + yCor(ARROW_TIP, d + .5));
 		// on line
 		p.addPoint(tx + xCor(ARROW_WING, d), ty + yCor(ARROW_WING, d));
 		// wing 2
 		p.addPoint(tx + xCor(ARROW_TIP, d - .5), ty + yCor(ARROW_TIP, d - .5));
 		// back to tip, close polygon
 		p.addPoint(tx, ty);
 		return p;
 	}
 
 	private static int xCor(final int len, final double dir) {
 		return (int) (len * Math.sin(dir));
 	}
 
 	private static int yCor(final int len, final double dir) {
 		return (int) (len * Math.cos(dir));
 	}
 
 	protected void addPipePart(final Class<? extends PipePart> part,
 			final Point location) {
 		if (!ensureModifyable()) return;
 		try {
 			final PipePart pp = part.newInstance();
 			createConfigureDialog("Add", pp, new Runnable() {
 				@Override
 				public void run() {
 					if (location != null)
 						pp.getGuiPosition().setLocation(location);
 					check(pp.getGuiPosition(), pp);
 					try {
 						if (pp instanceof Input)
 							Pipe.the().addInput((Input) pp);
 						else if (pp instanceof Converter)
 							Pipe.the().addConverter((Converter) pp);
 						else if (pp instanceof Output)
 							Pipe.the().addOutput((Output) pp);
 						else
 							throw new InternalException(
 									"Invalid sub-class of PipePart '%s'", pp);
 						getSet().add(pp);
 						checkPipeOrdering(null);
 					} catch (final StateException e) {
 						Log.error(e, "Cannot add new PipePart");
 					}
 					updateSaneConfigCache();
 					repaint();
 				}
 			});
 		} catch (final InstantiationException e) {
 			Log.error(e);
 		} catch (final IllegalAccessException e) {
 			Log.error(e);
 		}
 	}
 
 	/**
 	 * Should be invoked during a Drag&Drop operation if this section is the
 	 * source of the operation. Updates the position of the current connection,
 	 * if any, or otherwise tries to move the current remembered
 	 * {@link PipePart} to the given position.
 	 * 
 	 * @param ps
 	 *            current cursor position (scaled to screen)
 	 */
 	protected void mouseDragged(final Point ps) {
 		if (currentPart == null || handlePoint == null) return;
 		final Point p = getOriginal(ps);
 		if (currentConnection != null) {
 			// move connector instead of pipe-part
 			if (!ensureModifyable()) return;
 			if (currentConnectionsTarget != null) {
 				try {
 					currentPart
 							.disconnectFromPipePart(currentConnectionsTarget);
 				} catch (final StateException e) {
 					Log.error(e, "Cannot delete connection");
 				}
 				updateSaneConfigCache();
 				currentConnectionsTarget = null;
 			}
 
 			Rectangle r = new Rectangle(currentPart.getGuiPosition());
 			r = r.union(currentConnection);
 
 			currentConnection.setLocation(p.x - handlePoint.x, p.y
 					- handlePoint.y);
 			currentConnection.setSize(0, 0);
 			check(currentConnection, null);
 			currentConnectionValid = false;
 			for (final PipePart pp : set)
 				if (pp.getGuiPosition().contains(
 						currentConnection.getLocation())) {
 					currentConnectionValid = currentPart
 							.isConnectionAllowed(pp);
 					break;
 				}
 			mouseMoved(ps);
 
 			Rectangle2D.union(r, currentConnection, r);
 			// need to take care of labels
 			r.grow(GROW_LABEL_REDRAW, GROW_LABEL_REDRAW);
 			scaleRect(r);
 			repaint(r);
 		} else {
 			// move pipe-part
 			final Rectangle orgPos = new Rectangle(currentPart.getGuiPosition());
 			Rectangle r = new Rectangle(orgPos);
 			unionConnectionTargets(r);
 			unionConnectionSources(r);
 			currentPart.getGuiPosition().setLocation(p.x - handlePoint.x,
 					p.y - handlePoint.y);
 			check(currentPart.getGuiPosition(), currentPart);
 			if (!checkPipeOrdering(null))
 				currentPart.getGuiPosition().setLocation(orgPos.getLocation());
 			r = r.union(currentPart.getGuiPosition());
 			unionConnectionTargets(r);
 			unionConnectionSources(r);
 			// need to take care of labels
 			r.grow(GROW_LABEL_REDRAW, GROW_LABEL_REDRAW);
 			scaleRect(r);
 			repaint(r);
 		}
 	}
 
 	private void unionConnectionSources(final Rectangle r) {
 		for (final PipePart srcPP : set)
 			if (srcPP.getConnectedPipeParts().contains(currentPart))
 				unionConnection(r, srcPP);
 	}
 
 	private void unionConnectionTargets(final Rectangle r) {
 		for (final PipePart trgPP : currentPart.getConnectedPipeParts())
 			unionConnection(r, trgPP);
 	}
 
 	private void unionConnection(final Rectangle r, final PipePart pp) {
 		final Point pt = new Point();
 		calcConnectorPositions(currentPart.getGuiPosition(), pp
 				.getGuiPosition(), null, pt);
 		Rectangle2D.union(r, new Rectangle(pt.x, pt.y, 0, 0), r);
 	}
 
 	protected void check(final Rectangle r, final PipePart pp) {
 		final int xMin;
 		final int xMax;
 		final int yMin = 1;
 		final int yMax = bounds.height - 1;
 		if (Input.class.isInstance(pp)) {
 			xMin = 1;
 			xMax = border1 - 1;
 		} else if (Converter.class.isInstance(pp)) {
 			xMin = border1 + 1;
 			xMax = border2 - 1;
 		} else if (Output.class.isInstance(pp)) {
 			xMin = border2 + 1;
 			xMax = bounds.width - 1;
 		} else {
 			xMin = 1;
 			xMax = bounds.width - 1;
 		}
 		if (r.x < xMin) r.x = xMin;
 		if (r.y < yMin) r.y = yMin;
 		if (r.x + r.width > xMax) r.x = xMax - r.width;
 		if (r.y + r.height > yMax) r.y = yMax - r.height;
 		for (final PipePart other : set)
 			if (other != pp && other.getGuiPosition().intersects(r)) {
 				// move r, so it doesn't intersect anymore
 				final Rectangle rO = other.getGuiPosition();
 				final Rectangle i = r.intersection(rO);
 				final int x0 = r.x + r.width / 2;
 				final int y0 = r.y + r.height / 2;
 				final int x1 = rO.x + rO.width / 2;
 				final int y1 = rO.y + rO.height / 2;
 				if (i.width < i.height && r.x - i.width >= xMin
 						&& r.x + r.width + i.width <= xMax) {
 					if (x0 > x1) // move right
 						r.translate(i.width, 0);
 					else
 						// move left
 						r.translate(-i.width, 0);
 				} else if (y0 > y1) {
 					if (r.y + r.height + i.height > yMax)
 						// move up instead of down
 						r.translate(0, i.height - r.height - rO.height);
 					else
 						r.translate(0, i.height); // move down
 				} else if (r.y - i.height < yMin)
 					// move down instead of up
 					r.translate(0, r.height + rO.height - i.height);
 				else
 					r.translate(0, -i.height); // move up
 				// check bounds again
 				// (overlapping is better than being out of bounds)
 				if (r.x < xMin) r.x = xMin;
 				if (r.y < yMin) r.y = yMin;
 				if (r.x + r.width > xMax) r.x = xMax - r.width;
 				if (r.y + r.height > yMax) r.y = yMax - r.height;
 			}
 	}
 
 	/**
 	 * Should be called whenever the mouse is moved over this section.
 	 * 
 	 * @param ps
 	 *            current cursor position (scaled to screen)
 	 */
 	protected void mouseMoved(final Point ps) {
 		final Point p = getOriginal(ps);
 		PipePart found = null;
 		for (final PipePart pp : set)
 			if (pp.getGuiPosition().contains(p)) {
 				found = pp;
 				break;
 			}
 		if (underCursor != found) {
 			if (underCursor != null)
 				repaint(scaleRect(new Rectangle(underCursor.getGuiPosition())));
 			underCursor = found;
 			if (underCursor != null)
 				repaint(scaleRect(new Rectangle(underCursor.getGuiPosition())));
 		}
 	}
 
 	/**
 	 * Remembers the {@link PipePart} and the connector which is under the given
 	 * cursor position for later use in {@link #mouseDragged(Point)} and during
 	 * {@link #paintComponent(Graphics)}.
 	 * 
 	 * @param ps
 	 *            current cursor position (scaled to screen)
 	 */
 	protected void updateCurrent(final Point ps) {
 		// invoked on click or when a drag&drop operation starts
 		updateCurrent0(ps);
 		repaint();
 	}
 
 	private void updateCurrent0(final Point pscr) {
 		currentPart = null;
 		currentIcon = null;
 		currentConnection = null;
 		currentConnectionsTarget = null;
 		currentConnectionValid = false;
 		handlePoint = null;
 
 		// check all pipe-parts
 		final Point p = getOriginal(pscr);
 		for (final PipePart pp : set)
 			if (pp.getGuiPosition().contains(p)) {
 				currentPart = pp;
 
 				final Rectangle inner = new Rectangle(pp.getGuiPosition());
 				inner.grow(-INNER_WIDTH, -INNER_HEIGHT);
 				if (inner.contains(p))
 					handlePoint = new Point(p.x - pp.getGuiPosition().x, p.y
 							- pp.getGuiPosition().y);
 				else {
 					final Rectangle ibC = getIconBounds(pp.getGuiPosition(),
 							ICON_CONF, ICON_CONF_POS);
 					final Rectangle ibD = getIconBounds(pp.getGuiPosition(),
 							ICON_DGR, ICON_DGR_POS);
 					if (ibC.contains(p))
 						currentIcon = ICON_CONF;
 					else if (ibD.contains(p))
 						currentIcon = ICON_DGR;
 					else {
 						currentConnection = new Rectangle(p.x, p.y, 0, 0);
 						handlePoint = new Point(0, 0);
 					}
 				}
 
 				return;
 			}
 
 		// check all connections
 		for (final PipePart srcPP : set)
 			for (final PipePart trgPP : srcPP.getConnectedPipeParts()) {
 				final Point ps = new Point();
 				final Point pt = new Point();
 				calcConnectorPositions(srcPP.getGuiPosition(), trgPP
 						.getGuiPosition(), ps, pt);
 				if (Line2D.ptSegDistSq(ps.x, ps.y, pt.x, pt.y, p.x, p.y) < LINE_CLICK_DIST
 						|| getArrowPolygon(ps.x, ps.y, pt.x, pt.y).contains(p)) {
 					currentPart = srcPP;
 					currentConnection = new Rectangle(p.x, p.y, 0, 0);
 					currentConnectionsTarget = trgPP;
 					handlePoint = new Point(p.x - pt.x, p.y - pt.y);
 					return;
 				}
 			}
 	}
 
 	/**
 	 * Forgets about the currently remembered {@link PipePart} and connection,
 	 * if any. If a connection has been remembered and is currently pointing to
 	 * a valid {@link PipePart}, the remembered {@link PipePart} is connected to
 	 * the one the connection is pointing to, even if it's outside of this
 	 * section.
 	 */
 	protected void releaseCurrent() {
 		// invoked on click or when a drag&drop operation is finished
 		if (currentConnection != null && currentConnectionsTarget == null
 				&& ensureModifyable()) {
 			final Point p = new Point(currentConnection.getLocation());
 			for (final PipePart pp : set)
 				if (pp.getGuiPosition().contains(p)
 						&& currentPart.isConnectionAllowed(pp)) {
 					try {
 						currentPart.connectToPipePart(pp);
 					} catch (final StateException e) {
 						Log.error(e, "Cannot create connection");
 					}
 					updateSaneConfigCache();
 					break;
 				}
 		}
 		// currentPart = null;
 		currentIcon = null;
 		currentConnection = null;
 		// currentConnectionsTarget = null;
 		currentConnectionValid = false;
 		handlePoint = null;
 		repaint();
 	}
 
 	/**
 	 * Sorts all {@link PipePart}s according to their location on the board,
 	 * from top to down, from left to right.
 	 * 
 	 * @param <T>
 	 *            subclass of {@link PipePart} (compile-time)
 	 * @param clazz
 	 *            subclass of {@link PipePart} (run-time)
 	 * @return sorted list of {@link PipePart}s
 	 */
 	@SuppressWarnings("unchecked")
 	private <T extends PipePart> List<T> getSortedParts(final Class<T> clazz) {
 		final List<T> res = new ArrayList<T>();
 		for (final PipePart pp : set)
 			if (clazz.isInstance(pp)) res.add((T) pp);
 		Collections.sort(res, new Comparator<T>() {
 			@Override
 			public int compare(final T pp1, final T pp2) {
 				final Rectangle r1 = pp1.getGuiPosition();
 				final Rectangle r2 = pp2.getGuiPosition();
 				int cmp = r1.y - r2.y;
 				if (cmp == 0) cmp = r1.x - r2.x;
 				return cmp;
 			}
 		});
 		return res;
 	}
 
 	/**
 	 * Checks whether {@link PipePart}s in the {@link Pipe} need to be reordered
 	 * to reflect their GUI positions. If reordering is needed but the board is
 	 * in read-only state (as the Pipe is running), this method will return
 	 * false and delay the reordering until the Pipe has finished running.
 	 * 
 	 * @param warnIfNeeded
 	 *            if not empty, a message will be printed if reordering is
 	 *            needed
 	 * @return true if reordering was not needed or succeeded, false if
 	 *         reordering was needed and failed.
 	 * @see #updateState()
 	 */
 	protected boolean checkPipeOrdering(final String warnIfNeeded) {
 		// is reordering needed?
 		final List<Input> orderInput = getSortedParts(Input.class);
 		final List<Converter> orderConverter = getSortedParts(Converter.class);
 		final List<Output> orderOutput = getSortedParts(Output.class);
 		if (Pipe.the().getInputList().equals(orderInput)
 				&& Pipe.the().getConverterList().equals(orderConverter)
 				&& Pipe.the().getOutputList().equals(orderOutput)) return true;
 
 		if (warnIfNeeded != null && !warnIfNeeded.isEmpty())
 			Log.warn(warnIfNeeded);
 
 		if (!ensureModifyable()) {
 			// delay until pipe has finished ...
 			delayedReordering = true;
 			// ... and stop drag&drop operation (if any)
 			return false;
 		}
 
 		// reorder
 		try {
 			Pipe.the().reorderInputs(orderInput);
 			Pipe.the().reorderConverter(orderConverter);
 			Pipe.the().reorderOutputs(orderOutput);
 		} catch (final StateException e) {
 			Log.error(e, "Cannot reorder PipeParts");
 		} catch (final IllegalArgumentException e) {
 			Log.error(e, "Cannot reorder PipeParts");
 		}
 		delayedReordering = false;
 		repaint();
 		return true;
 	}
 
 	protected void updateBounds(final int width, final int height) {
 		bounds.setSize(width, height);
 		final int maxWidth = DEF_RECT_WIDTH + SECTION_SPACE;
 		border1 = Math.min(width / SECTION_FRAC, maxWidth);
 		border2 = width - Math.min(width / SECTION_FRAC, maxWidth);
 		for (final PipePart pp : set)
 			check(pp.getGuiPosition(), pp);
 		checkPipeOrdering("Resizing the board changed the ordering of the Pipe !!! "
 				+ "Please check ordering of all PipeParts.");
 		repaint();
 	}
 
 	protected void showMenu(final Component invoker, final int x, final int y) {
 		lastMenuLocation = new Point(x, y);
 		if (x <= border1)
 			showMenu(menuInput, invoker, x, y);
 		else if (x >= border2)
 			showMenu(menuOutput, invoker, x, y);
 		else
 			showMenu(menuConverter, invoker, x, y);
 	}
 
 	protected void showMenu(final JPopupMenu menu, final Component invoker,
 			final int x, final int y) {
 		final MenuElement[] items = menu.getSubElements();
 		((AbstractButton) items[idxMenuAdd]).setEnabled(modifyable
 				&& currentPart == null);
 		((AbstractButton) items[idxMenuConfPart]).setEnabled(modifyable
 				&& currentPart != null && currentConnection == null
 				&& !currentPart.getGuiConfigs().isEmpty());
 		((AbstractButton) items[idxMenuDelPart]).setEnabled(modifyable
 				&& currentPart != null && currentConnection == null);
 		((AbstractButton) items[idxMenuDelPartConn]).setEnabled(modifyable
 				&& currentPart != null && currentConnection == null
 				&& !currentPart.getConnectedPipeParts().isEmpty());
 		((AbstractButton) items[idxMenuDelConn]).setEnabled(modifyable
 				&& currentConnection != null);
 		((AbstractButton) items[idxMenuToggleDgr])
 				.setEnabled(currentPart != null);
 		((AbstractButton) items[idxMenuToggleDgr])
 				.setSelected(currentPart != null && currentPart.isVisualize());
 		menu.show(invoker, x, y);
 	}
 
 	protected void createConfigureDialog(final String prefix,
 			final PipePart pp, final Runnable runIfOK) {
 		// no need to configure if no values assigned
 		if (pp.getGroup().isEmpty()) {
 			if (runIfOK != null) runIfOK.run();
 			return;
 		}
 
 		final JDialog dlg = new JDialog();
		dlg.setTitle(String.format("%s %s", prefix, pp));
 		final Layouter lay = new Layouter(dlg);
 		for (final ConfigValue v : pp.getGuiConfigs()) {
 			// each config-value gets its own JPanel so they don't
 			// interfere with each other.
 			// LBL1 SUB1
 			// LBL2 SUB2
 			// LBL3 SUB3
 			// BUTTONS
 			final JPanel sub = new JPanel();
 			final Layouter laySub = new Layouter(sub);
 			final String compLabel = v.getLabel() + ":";
 			final JLabel lbl = new JLabel(compLabel, SwingConstants.RIGHT);
 			lbl.setVerticalAlignment(SwingConstants.TOP);
 			lay.add(lbl, false);
 			lay.addWholeLine(sub, false);
 			v.insertGUIComponents(laySub);
 		}
 
 		final JPanel buttons = new JPanel();
 		final Layouter lb = new Layouter(buttons);
 
 		lay.addVerticalSpacer();
 		lay.addWholeLine(buttons, false);
 
 		lb.addButton(Button.Help, Layouter.help(dlg, "PartConfigureDialog"));
 		lb.addSpacer();
 		dlg.getRootPane().setDefaultButton(
 				lb.addButton(Button.Ok, new Runnable() {
 					@Override
 					public void run() {
 						if (saveConfigChanges(pp)) {
 							dlg.dispose();
 							if (runIfOK != null) runIfOK.run();
 						}
 					}
 				}));
 		lb.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				saveConfigChanges(pp);
 			}
 		});
 		lb.addButton(Button.Cancel, new Runnable() {
 			@Override
 			public void run() {
 				dlg.dispose();
 			}
 		});
 
 		dlg.pack();
 		dlg.setLocationRelativeTo(null);
 		// dlg.setModal(true);
 		HelpDialog.closeHelpIfOpen();
 		dlg.setVisible(true);
 		HelpDialog.closeHelpIfOpen();
 	}
 
 	protected boolean saveConfigChanges(final PipePart pp) {
 		if (!ensureModifyable()) return false;
 		for (final ConfigValue v : pp.getGuiConfigs())
 			v.setFromGUIComponents();
 		updateSaneConfigCache();
 		final String cfgRes = pp.isConfigurationSane();
 		if (cfgRes != null) {
 			Log.error("Configuration is invalid: " + cfgRes);
 			return false;
 		}
 		try {
 			pp.configure();
 		} catch (final PipeException e) {
 			Log.error(e);
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public String getToolTipText(final MouseEvent event) {
 		if (underCursor == null) return null;
 		final StringBuilder sb = new StringBuilder("<html><b>");
 		sb.append(underCursor.getName());
 		sb.append("</b><table>");
 		for (final ConfigValue v : underCursor.getGuiConfigs()) {
 			sb.append("<tr><td align=right>");
 			sb.append(v.getLabel().replace("<", "&lt;"));
 			sb.append("</td><td align=left>");
 			sb.append(v.asString().replace("<", "&lt;"));
 			sb.append("</td></tr>");
 		}
 		sb.append("</table></html>");
 		return sb.toString();
 	}
 
 	public void updateState() {
 		final boolean changed = modifyable ^ !MainFrame.the().isPipeRunning();
 		modifyable = !MainFrame.the().isPipeRunning();
 		if (changed) {
 			if (delayedReordering) checkPipeOrdering(null);
 			repaint();
 		}
 	}
 
 	private boolean ensureModifyable() {
 		if (!modifyable)
 			Log.error("Configuration board is read-only as "
 					+ "the Pipe is currently running.");
 		return modifyable;
 	}
 
 	public void setZoom(final double zoom) {
 		if (zoom > 0)
 			scale = 1 + zoom * 9;
 		else
 			scale = zoom < 0 ? 1 / (1 + -zoom * 9) : 1;
 		repaint();
 	}
 
 	private Point getOriginal(final Point scaled) {
 		return new Point((int) (scaled.x / scale), (int) (scaled.y / scale));
 	}
 
 	private Rectangle scaleRect(final Rectangle r) {
 		r.x *= scale;
 		r.y *= scale;
 		r.width *= scale;
 		r.height *= scale;
 		return r;
 	}
 
 }
