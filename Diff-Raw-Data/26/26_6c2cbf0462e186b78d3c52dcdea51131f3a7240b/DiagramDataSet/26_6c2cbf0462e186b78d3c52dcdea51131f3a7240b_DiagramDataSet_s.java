 package pleocmd.itfc.gui.dgr;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D.Double;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 public final class DiagramDataSet {
 
 	public enum DiagramType {
 		LineDiagram, BarDiagram, ScatterPlotDiagram, IntersectionDiagram
 	}
 
 	private static final double MIN_VAL_PER_UNIT = 0.0000001;
 
 	private final Diagram diagram;
 
 	private final List<Double> points = new ArrayList<Double>();
 
 	private boolean prepared;
 
 	private String label;
 
 	private Pen pen;
 
 	private DiagramType type = DiagramType.LineDiagram;
 
 	private double valuePerUnitX = 1.0;
 
 	private double valuePerUnitY = 1.0;
 
 	private double cachedValueToPixXIncr;
 
 	private double cachedValueToPixYIncr;
 
 	private double cachedValueToPixXFactor;
 
 	private double cachedValueToPixYFactor;
 
 	public DiagramDataSet(final Diagram diagram, final String label) {
 		this.diagram = diagram;
 		this.label = label;
 		diagram.addDataSet(this);
 	}
 
 	public Diagram getDiagram() {
 		return diagram;
 	}
 
 	public List<Double> getPoints() {
 		return Collections.unmodifiableList(points);
 	}
 
 	public void setPoints(final List<Double> points) {
 		synchronized (diagram) {
 			this.points.clear();
 			this.points.addAll(points);
 			prepared = false;
 			diagram.repaint();
 		}
 	}
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(final String label) {
 		synchronized (diagram) {
 			this.label = label;
 			diagram.repaint();
 		}
 	}
 
 	public Pen getPen() {
 		return pen;
 	}
 
 	public void setPen(final Pen pen) {
 		synchronized (diagram) {
 			this.pen = pen;
 			diagram.repaint();
 		}
 	}
 
 	public boolean isPenAutomatic() {
 		return pen == null;
 	}
 
 	public DiagramType getType() {
 		return type;
 	}
 
 	public void setType(final DiagramType type) {
 		synchronized (diagram) {
 			this.type = type;
 			diagram.repaint();
 		}
 	}
 
 	public double getValuePerUnitX() {
 		return valuePerUnitX;
 	}
 
 	public void setValuePerUnitX(final double valuePerUnitX) {
 		synchronized (diagram) {
 			this.valuePerUnitX = valuePerUnitX;
 			diagram.repaint();
 		}
 	}
 
 	public double getValuePerUnitY() {
 		return valuePerUnitY;
 	}
 
 	public void setValuePerUnitY(final double valuePerUnitY) {
 		synchronized (diagram) {
 			this.valuePerUnitY = valuePerUnitY;
 			diagram.repaint();
 		}
 	}
 
 	public void addPoint(final Double point) {
 		synchronized (diagram) {
 			points.add(point);
 			prepared = false;
 			diagram.repaint();
 		}
 	}
 
 	public void addSequence(final int[] values) {
 		addSequence(values, 0);
 	}
 
 	public void addSequence(final int[] values, final int xStart) {
 		addSequence(values, xStart, 1);
 	}
 
 	public void addSequence(final int[] values, final int xStart,
 			final int xIncr) {
 		synchronized (diagram) {
 			int x = xStart;
 			for (final int value : values) {
 				points.add(new Double(x, value));
 				x += xIncr;
 			}
 			prepared = false;
 			diagram.repaint();
 		}
 	}
 
 	public void addSequence(final double[] values) {
 		addSequence(values, 0);
 	}
 
 	public void addSequence(final double[] values, final double xStart) {
 		addSequence(values, xStart, 1);
 	}
 
 	public void addSequence(final double[] values, final double xStart,
 			final int xIncr) {
 		double x = xStart;
 		for (final double value : values) {
 			points.add(new Double(x, value));
 			x += xIncr;
 		}
 		prepared = false;
 		diagram.repaint();
 	}
 
 	void prepare() {
 		if (prepared) return;
 		prepared = true;
 		if (type == DiagramType.LineDiagram)
 			Collections.sort(points, new Comparator<Double>() {
 
 				@Override
 				public int compare(final Double p1, final Double p2) {
 					return p1.x < p2.x ? -1 : p1.x > p2.x ? 1 : 0;
 				}
 
 			});
 	}
 
 	public boolean isValid() {
 		return !points.isEmpty() && valuePerUnitX >= MIN_VAL_PER_UNIT
 				&& valuePerUnitY >= MIN_VAL_PER_UNIT;
 	}
 
 	void updateCache() {
 		cachedValueToPixXIncr = -diagram.getXAxis().getCachedMinVisUnit();
 		cachedValueToPixXFactor = diagram.getXAxis().getCachedPixelPerUnit();
 		cachedValueToPixYIncr = -diagram.getYAxis().getCachedMinVisUnit();
 		cachedValueToPixYFactor = diagram.getYAxis().getCachedPixelPerUnit();
 	}
 
 	double valueToPixelX(final double value) {
 		return (value / valuePerUnitX + cachedValueToPixXIncr)
 				* cachedValueToPixXFactor;
 	}
 
 	double valueToPixelY(final double value) {
 		return (value / valuePerUnitY + cachedValueToPixYIncr)
 				* cachedValueToPixYFactor;
 	}
 
 	double getCachedValueToPixXIncr() {
 		return cachedValueToPixXIncr;
 	}
 
 	double getCachedValueToPixYIncr() {
 		return cachedValueToPixYIncr;
 	}
 
 	double getCachedValueToPixXFactor() {
 		return cachedValueToPixXFactor;
 	}
 
 	double getCachedValueToPixYFactor() {
 		return cachedValueToPixYFactor;
 	}
 
 	public void createMenu(final JPopupMenu parent) {
 		final JMenu menu = new JMenu("DataSet " + getLabel());
 		parent.add(menu);
 		JMenuItem item = new JMenuItem("Set Default Pen");
 		menu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				setPen(null);
 			}
 		});
 		final JMenu colorMenu = new JMenu("Set Color");
 		menu.add(colorMenu);
 		for (int i = 0; i < Diagram.getDefaultColorCount(); ++i) {
 			final int index = i;
 			item = new JMenuItem(Diagram.getDefaultColor(i).getName());
 			item.setForeground(Diagram.getDefaultColor(i));
 			colorMenu.add(item);
 			item.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(final ActionEvent e) {
 					changePen(Diagram.getDefaultColor(index));
 				}
 			});
 			item = new JMenuItem("dark " + Diagram.getDefaultColor(i).getName());
 			item.setForeground(Diagram.getDefaultColor(i).makeDarker());
 			colorMenu.add(item);
 			item.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(final ActionEvent e) {
 					changePen(Diagram.getDefaultColor(index).makeDarker());
 				}
 			});
 		}
 
 		final JMenu swMenu = new JMenu("Set Stroke Width");
 		menu.add(swMenu);
 		for (float i = 0.25f; i <= 4.0f; i += 0.25f) {
 			final float index = i;
 			item = new JMenuItem(String.format("%.2f", i));
 			swMenu.add(item);
 			item.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(final ActionEvent e) {
 					changePen(index);
 				}
 			});
 		}
 
 		final JMenu stMenu = new JMenu("Set Stroke Type");
 		menu.add(stMenu);
 		item = new JMenuItem("solid");
 		stMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				changePen(new float[] { 1 });
 			}
 		});
 		item = new JMenuItem("dotted");
 		stMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				changePen(new float[] { 2, 5 });
 			}
 		});
 		item = new JMenuItem("dashed");
 		stMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				changePen(new float[] { 10, 10 });
 			}
 		});
 		item = new JMenuItem("dash-dotted");
 		stMenu.add(item);
 		item.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				changePen(new float[] { 10, 6, 2, 6 });
 			}
 		});
 	}
 
 	public void changePen(final Color color) {
 		final BasicStroke stroke = pen == null ? new BasicStroke(2.0f) : pen
 				.getStroke();
 		setPen(new Pen(color, stroke));
 	}
 
 	public void changePen(final float strokeWidth) {
 		final Color color = pen == null ? diagram.detectPen(
 				diagram.getDataSets().indexOf(this)).getColor() : pen
 				.getColor();
 		final BasicStroke stroke = new BasicStroke(strokeWidth,
 				BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
 				pen == null ? null : pen.getStroke().getDashArray(), 0.0f);
 		setPen(new Pen(color, stroke));
 	}
 
	public void changePen(final float dash[]) {
 		final Color color = pen == null ? diagram.detectPen(
 				diagram.getDataSets().indexOf(this)).getColor() : pen
 				.getColor();
 		final BasicStroke stroke = new BasicStroke(pen == null ? 2.0f : pen
 				.getStroke().getLineWidth(), BasicStroke.CAP_SQUARE,
 				BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
 		setPen(new Pen(color, stroke));
 	}
 }
