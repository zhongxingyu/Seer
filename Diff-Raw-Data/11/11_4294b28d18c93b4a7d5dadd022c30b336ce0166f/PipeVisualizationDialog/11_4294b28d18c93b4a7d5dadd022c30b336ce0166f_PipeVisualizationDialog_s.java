 package pleocmd.itfc.gui.dgr;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JDialog;
 
 import pleocmd.Log;
 import pleocmd.itfc.gui.AutoDisposableWindow;
 import pleocmd.itfc.gui.Layouter;
 import pleocmd.itfc.gui.MainFrame;
 import pleocmd.pipe.PipePart;
 
 public final class PipeVisualizationDialog extends JDialog implements
 		AutoDisposableWindow {
 
 	private static final long serialVersionUID = 2818789810493796194L;
 
 	private static final long TIME_SPAN = 3 * 1000;
 
 	private final PipePart part;
 
 	private final Diagram diagram;
 
 	private final List<DiagramDataSet> dataSets;
 
 	public PipeVisualizationDialog(final PipePart part, final int dataSetCount) {
 		this.part = part;
 
 		Log.detail("Creating Pipe-Visualization-Dialog");
 		setTitle("Visualization for " + part.getName());
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				close();
 			}
 		});
 
 		// Add components
 		final Layouter lay = new Layouter(this);
 		diagram = new Diagram();
 		lay.addWholeLine(diagram, true);
 		diagram.getXAxis().setMin(0);
 		diagram.getXAxis().setMax(TIME_SPAN);
 		diagram.getXAxis().setUnitName("s");
 		diagram.getXAxis().setSubsPerUnit(10);
 
 		// Add DataSets
 		dataSets = new ArrayList<DiagramDataSet>(dataSetCount);
 		for (int i = 0; i < dataSetCount; ++i) {
 			final DiagramDataSet ds = new DiagramDataSet(diagram, String
 					.format("#%d", i + 1));
 			ds.setValuePerUnitX(1000);
 			dataSets.add(ds);
 		}
 
 		setSize(400, 300);
 		setLocationRelativeTo(null);
 		setAlwaysOnTop(true);
 		Log.detail("Pipe-Visualization-Dialog created");
 		MainFrame.the().addKnownWindow(this);
 
 		diagram.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(final MouseEvent e) {
				if (e.isPopupTrigger())
 					getDiagram().getMenu().show(PipeVisualizationDialog.this,
 							e.getX(), e.getY());
 			}
 		});
 
 	}
 
 	protected void close() {
 		MainFrame.the().removeKnownWindow(this);
 		dispose();
 		part.setVisualize(false);
 	}
 
 	@Override
 	public void autoDispose() {
 		MainFrame.the().removeKnownWindow(this);
 		dispose();
 	}
 
 	public Diagram getDiagram() {
 		return diagram;
 	}
 
 	public DiagramDataSet getDataSet(final int index) {
 		return index < 0 || index >= dataSets.size() ? null : dataSets
 				.get(index);
 	}
 
 	public void plot(final int index, final double x, final double y) {
 		if (index < 0 || index >= dataSets.size()) {
 			Log.detail("Ignoring plotting of invalid DataSet %d", index);
 			return;
 		}
 		dataSets.get(index).addPoint(new Point2D.Double(x, y));
 		final long elapsed = part.getPipe().getFeedback().getElapsed();
 		diagram.getXAxis().setMin(Math.max(0, elapsed - TIME_SPAN) / 1000.0);
 		diagram.getXAxis().setMax(Math.max(TIME_SPAN, elapsed) / 1000.0);
 	}
 
 	public void reset() {
 		for (final DiagramDataSet ds : dataSets)
 			ds.setPoints(new ArrayList<Point2D.Double>());
 		repaint();
 	}
 
 }
