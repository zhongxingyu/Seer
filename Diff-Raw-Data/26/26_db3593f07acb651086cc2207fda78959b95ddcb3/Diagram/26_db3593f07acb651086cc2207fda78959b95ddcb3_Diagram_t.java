 // This file is part of PleoCommand:
 // Interactively control Pleo with psychobiological parameters
 //
 // Copyright (C) 2010 Oliver Hoffmann - Hoffmann_Oliver@gmx.de
 //
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Boston, USA.
 
 package pleocmd.itfc.gui.dgr;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.ToolTipManager;
 
 public final class Diagram extends JPanel {
 
 	static final double MIN_GRID_DELTA = 2.0;
 
 	private static final double SCALE_SPEED_MOUSE = 0.01;
 
 	private static final double MOVE_SPEED_MOUSE = 0.05;
 
 	private static final long serialVersionUID = -8245547025738665255L;
 
 	private static final int BORDER = 4;
 
 	private static final DefaultColor[] DEFAULT_COLORS = new DefaultColor[] {
 			new DefaultColor(0xFF, 0x45, 0x00, "orange red"),
 			new DefaultColor(0x7F, 0xFF, 0x00, "chartreuse"),
 			new DefaultColor(0x63, 0xB8, 0xFF, "steel blue"),
 			new DefaultColor(0xFF, 0xD7, 0x00, "gold"),
 			new DefaultColor(0xFF, 0x6A, 0x6A, "indian red"),
 			new DefaultColor(0x00, 0xFF, 0xFF, "cyan"),
 			new DefaultColor(0xFF, 0xF6, 0x8F, "khaki"),
 			new DefaultColor(0x7F, 0xFF, 0xD4, "aqua marine"),
 			new DefaultColor(0xFF, 0x69, 0xB4, "hot pink"),
 			new DefaultColor(0xC1, 0xFF, 0xC1, "dark sea green"),
 			new DefaultColor(0xFF, 0xFF, 0xF0, "ivory"),
 			new DefaultColor(0x83, 0x6F, 0xFF, "slate blue"),
 			new DefaultColor(0xC0, 0xFF, 0x3E, "olive drab"),
 			new DefaultColor(0xFF, 0x83, 0xFA, "orchid"),
 			new DefaultColor(0xFF, 0xE7, 0xBA, "wheat"),
 			new DefaultColor(0xFF, 0xC1, 0xC1, "rosy brown") };
 
 	private final List<DiagramDataSet> dataSets = new ArrayList<DiagramDataSet>();
 
 	private Color backgroundColor = Color.WHITE;
 
 	private Pen axisPen = new Pen(Color.BLACK);
 
 	private Pen unitPen = new Pen(Color.GRAY);
 
 	private Pen subUnitPen = new Pen(Color.LIGHT_GRAY);
 
 	private final DiagramAxis xAxis = new DiagramAxis(this, "X");
 
 	private final DiagramAxis yAxis = new DiagramAxis(this, "Y");
 
 	private double zoom = 1.0;
 
 	private AffineTransform originalTransform;
 
 	private enum AlignH {
 		Left, Center, Right;
 	}
 
 	private enum AlignV {
 		Top, Center, Bottom;
 	}
 
 	public Diagram() {
 		addMouseMotionListener(new MouseMotionListener() {
 			private int oldx;
 
 			private int oldy;
 
 			@Override
 			public void mouseMoved(final MouseEvent e) {
 				oldx = e.getX();
 				oldy = e.getY();
 			}
 
 			@Override
 			public void mouseDragged(final MouseEvent e) {
 				final int newx = e.getX();
 				final int newy = e.getY();
 				getXAxis().setOffset(
 						getXAxis().getOffset() - (newx - oldx)
 								* MOVE_SPEED_MOUSE / getZoom());
 				getYAxis().setOffset(
 						getYAxis().getOffset() + (newy - oldy)
 								* MOVE_SPEED_MOUSE / getZoom());
 				oldx = newx;
 				oldy = newy;
 			}
 		});
 		addMouseWheelListener(new MouseWheelListener() {
 			@Override
 			public void mouseWheelMoved(final MouseWheelEvent e) {
 				final int clicks = -e.getWheelRotation();
 				if (clicks > 0)
 					setZoom(getZoom() * (1 + SCALE_SPEED_MOUSE * clicks));
 				if (clicks < 0)
 					setZoom(getZoom() / (1 - SCALE_SPEED_MOUSE * clicks));
 			}
 		});
 		ToolTipManager.sharedInstance().registerComponent(this);
 	}
 
 	synchronized void addDataSet(final DiagramDataSet dataSet) {
 		dataSets.add(dataSet);
 	}
 
 	public List<DiagramDataSet> getDataSets() {
 		return Collections.unmodifiableList(dataSets);
 	}
 
 	public synchronized Color getBackgroundColor() {
 		return backgroundColor;
 	}
 
 	public synchronized void setBackgroundColor(final Color backgroundColor) {
 		this.backgroundColor = backgroundColor;
 	}
 
 	public synchronized Pen getAxisPen() {
 		return axisPen;
 	}
 
 	public synchronized void setAxisPen(final Pen axisPen) {
 		this.axisPen = axisPen;
 	}
 
 	public synchronized Pen getUnitPen() {
 		return unitPen;
 	}
 
 	public synchronized void setUnitPen(final Pen unitPen) {
 		this.unitPen = unitPen;
 	}
 
 	public synchronized Pen getSubUnitPen() {
 		return subUnitPen;
 	}
 
 	public synchronized void setSubUnitPen(final Pen subUnitPen) {
 		this.subUnitPen = subUnitPen;
 	}
 
 	public DiagramAxis getXAxis() {
 		return xAxis;
 	}
 
 	public DiagramAxis getYAxis() {
 		return yAxis;
 	}
 
 	public synchronized double getZoom() {
 		return zoom;
 	}
 
 	public synchronized void setZoom(final double zoom) {
 		this.zoom = zoom;
 		repaint();
 	}
 
 	@Override
 	protected synchronized void paintComponent(final Graphics g) {
		final int areaWidth = getWidth();
		final int areaHeight = getHeight();
		final BufferedImage img = new BufferedImage(areaWidth, areaHeight,
 				BufferedImage.TYPE_INT_RGB);
 		final Graphics2D g2 = img.createGraphics();
 		originalTransform = g2.getTransform();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setClip(0, 0, areaWidth, areaHeight);
 		g2.setColor(backgroundColor);
		g2.fillRect(0, 0, areaWidth, areaHeight);
 		g2.setFont(getFont().deriveFont(10f));
 
 		// cache y-axis dimension
 		final int unitHeight = g2.getFontMetrics().getHeight();
		final int h = areaHeight - 1 - unitHeight - BORDER;
 		yAxis.updateCache(h);
 
 		// cache x-axis dimension
 		final int unitWidth1 = g2.getFontMetrics().stringWidth(
 				String.format("%.2f %s", yAxis.getCachedMinVisUnit(), yAxis
 						.getUnitName()));
 		final int unitWidth2 = g2.getFontMetrics().stringWidth(
 				String.format("%.2f %s", yAxis.getCachedMaxVisUnit(), yAxis
 						.getUnitName()));
 		final int unitWidth = Math.max(unitWidth1, unitWidth2);
		final int w = areaWidth - 1 - unitWidth - BORDER;
 		xAxis.updateCache(w);
 
		g2.translate(unitWidth, areaHeight - unitHeight);
 		g2.scale(1, -1);
 
 		// draw x and y axis
 		drawAxis(g2, yAxis, true, w, unitWidth);
 		drawAxis(g2, xAxis, false, h, unitHeight);
 		axisPen.assignTo(g2);
 		g2.drawLine(0, 0, w, 0);
 		g2.drawLine(0, 0, 0, h);
 
 		// draw data-sets
 		g2.clipRect(0, 0, w + BORDER, h + BORDER);
 		int idx = 0;
 		for (final DiagramDataSet ds : dataSets) {
 			if (!ds.isValid()) continue;
 			ds.prepare();
 			ds.updateCache();
 			double xold = 0;
 			double yold = 0;
 			boolean first = true;
 			final Pen pen = ds.isPenAutomatic() ? detectPen(idx) : ds.getPen();
 			pen.assignTo(g2);
 			final int y0pos = (int) ds.valueToPixelY(0);
 			for (final Point2D.Double pt : ds.getPoints()) {
 				final double xpix = ds.valueToPixelX(pt.x);
 				final double ypix = ds.valueToPixelY(pt.y);
 				final double xpos = xAxis.isReversed() ? w - xpix : xpix;
 				final double ypos = yAxis.isReversed() ? h - ypix : ypix;
 				switch (ds.getType()) {
 				case LineDiagram:
 					if (!first)
 						g2.drawLine((int) xold, (int) yold, (int) xpos,
 								(int) ypos);
 					break;
 				case BarDiagram:
 					g2.fillRect((int) xpos - 1, y0pos, 3, (int) (ypos - y0pos));
 					break;
 				case ScatterPlotDiagram:
 					g2.drawOval((int) xpos - 1, (int) ypos - 1, 2, 2);
 					break;
 				case IntersectionDiagram:
 					g2.fillRect((int) xpos - 1, 0, 3, h);
 					final int xp = (int) (xpos + pen.getStroke().getLineWidth() + 3);
 					final String str = String.format("%f", pt.y);
 					final Rectangle bounds = new Rectangle(xp, 2, w - xp, h - 4);
 					drawText(g2, bounds, AlignH.Left, AlignV.Center, str);
 					if (h >= 200) {
 						drawText(g2, bounds, AlignH.Left, AlignV.Top, str);
 						drawText(g2, bounds, AlignH.Left, AlignV.Bottom, str);
 					}
 					break;
 				}
 				xold = xpos;
 				yold = ypos;
 				first = false;
 			}
 			++idx;
 		}
 
 		// draw legend
 		g2.setFont(getFont().deriveFont(18f));
 		final int fh = Math.abs(g2.getFontMetrics().getHeight());
 		final int legendHeight = fh * dataSets.size() + 2;
 		int legendWidth = 0;
 		for (final DiagramDataSet ds : dataSets)
 			legendWidth = Math.max(legendWidth, g2.getFontMetrics()
 					.stringWidth(ds.getLabel()));
 		legendWidth += 4;
 		g2.setTransform(originalTransform);
		g2.translate(areaWidth - BORDER - legendWidth + 1, BORDER + 1);
 		final Rectangle legendRect = new Rectangle(0, 0, legendWidth,
 				legendHeight);
 		g2.setPaint(new Color(backgroundColor.getRed(), backgroundColor
 				.getGreen(), backgroundColor.getBlue(), 128));
 		g2.fill(legendRect);
 		g2.setColor(Color.BLACK);
 		g2.setStroke(new BasicStroke(2.0f));
 		g2.draw(legendRect);
 		g2.clip(legendRect);
 		idx = 0;
 		legendRect.width -= 2;
 		for (final DiagramDataSet ds : dataSets) {
 			(ds.isPenAutomatic() ? detectPen(idx) : ds.getPen()).assignTo(g2);
 			legendRect.y += fh;
 			legendRect.height -= fh;
 			drawText(g2, legendRect, AlignH.Right, AlignV.Bottom, ds.getLabel());
 			++idx;
 		}
 
		g.drawImage(img, 0, 0, null);
 	}
 
 	private void drawText(final Graphics2D g2, final Rectangle bounds,
 			final AlignH alignH, final AlignV alignV, final String str) {
 		final float w;
 		final float h;
 		if (alignH == AlignH.Left && alignV == AlignV.Bottom) {
 			w = 0;
 			h = 0;
 		} else {
 			final Rectangle2D sb = g2.getFontMetrics().getStringBounds(str, g2);
 			w = (float) sb.getWidth();
 			h = Math.abs(g2.getFontMetrics().getAscent());
 			// must be absolute because of possible negative scaling
 		}
 		final float x;
 		final float y;
 		switch (alignH) {
 		case Left:
 			x = bounds.x;
 			break;
 		case Center:
 			x = bounds.x + (bounds.width - w) / 2;
 			break;
 		case Right:
 			x = bounds.x + bounds.width - w;
 			break;
 		default:
 			throw new IllegalArgumentException("AlignH constant unknown");
 		}
 		switch (alignV) {
 		case Top:
 			y = bounds.y + bounds.height - h;
 			break;
 		case Center:
 			y = bounds.y + (bounds.height - h) / 2;
 			break;
 		case Bottom:
 			y = bounds.y;
 			break;
 		default:
 			throw new IllegalArgumentException("AlignV constant unknown");
 		}
 		final AffineTransform at = g2.getTransform();
 		final Point2D.Float p = (Point2D.Float) at.transform(new Point2D.Float(
 				x, y), new Point2D.Float());
 		g2.setTransform(originalTransform);
 		g2.drawString(str, p.x, p.y);
 		g2.setTransform(at);
 	}
 
 	private void drawAxisLine(final Graphics2D g2, final boolean vertical,
 			final double pos, final int len) {
 		if (vertical)
 			g2.drawLine(1, (int) pos, len - 1, (int) pos);
 		else
 			g2.drawLine((int) pos, 1, (int) pos, len - 1);
 	}
 
 	private void drawAxisText(final Graphics2D g2, final boolean vertical,
 			final double pos, final int unitSpace, final double unitSize,
 			final double val, final String unitName) {
 		final double begin = pos - unitSize / 2;
 		drawText(g2, vertical ? new Rectangle(-unitSpace, (int) begin,
 				unitSpace, (int) unitSize) : new Rectangle((int) begin,
 				-unitSpace, (int) unitSize, unitSpace), vertical ? AlignH.Right
 				: AlignH.Center, vertical ? AlignV.Center : AlignV.Top, String
 				.format("%.2f %s", val, unitName));
 	}
 
 	private void drawAxis(final Graphics2D g2, final DiagramAxis axis,
 			final boolean vertical, final int axisThickness, final int unitSpace) {
 		final int spu = axis.getSubsPerUnit();
 		final int upg = axis.getCachedUnitsPerGrid();
 		final double ppg = axis.getCachedPixelPerGrid();
 		final double ppsg = axis.getCachedPixelPerSubGrid();
 		final boolean rev = axis.isReversed();
 		for (double u = axis.getCachedMinVisUnit(); u <= axis
 				.getCachedMaxVisUnit(); u += upg) {
 			unitPen.assignTo(g2);
 			final double pos = axis.unitToPixel(u);
 			drawAxisLine(g2, vertical, pos, axisThickness);
 			drawAxisText(g2, vertical, pos, unitSpace, ppg, u, axis
 					.getUnitName());
 			if (ppsg >= MIN_GRID_DELTA) {
 				subUnitPen.assignTo(g2);
 				for (int s = 1; s < spu; ++s)
 					drawAxisLine(g2, vertical, rev ? pos - s * ppsg : pos + s
 							* ppsg, axisThickness);
 			}
 		}
 	}
 
 	public Pen detectPen(final int idx) {
 		final Color bc = backgroundColor;
 		int v = (bc.getRed() + bc.getGreen() + bc.getBlue()) / 3;
 		if (idx >= DEFAULT_COLORS.length) {
 			v = v < 64 || v > 192 ? 255 - v : v < 128 ? 255 : 0;
 			return new Pen(new Color(v, v, v), 2.0f);
 		}
 		return new Pen(v > 128 ? DEFAULT_COLORS[idx].makeDarker()
 				: DEFAULT_COLORS[idx], 2.0f);
 	}
 
 	static int getDefaultColorCount() {
 		return DEFAULT_COLORS.length;
 	}
 
 	static DefaultColor getDefaultColor(final int index) {
 		return DEFAULT_COLORS[index];
 	}
 
 	@Override
 	public String getToolTipText(final MouseEvent event) {
 		return null;
 	}
 
 	public JPopupMenu getMenu() {
 		final JPopupMenu menu = new JPopupMenu();
 		JMenuItem item = new JMenuItem("Reset Zoom");
 		menu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				setZoom(1.0);
 			}
 		});
 		item = new JMenuItem("Reset Scrolling");
 		menu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				getXAxis().setOffset(0);
 				getYAxis().setOffset(0);
 			}
 		});
 		xAxis.createMenu(menu);
 		yAxis.createMenu(menu);
 		for (final DiagramDataSet ds : dataSets)
 			ds.createMenu(menu);
 		return menu;
 	}
 
 }
