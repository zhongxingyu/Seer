 package edu.msoe.se2800.h4.jplot.grid;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.JPanel;
 
 import edu.msoe.se2800.h4.Path;
 import edu.msoe.se2800.h4.jplot.AxisPanel;
 import edu.msoe.se2800.h4.jplot.Constants;
 import edu.msoe.se2800.h4.jplot.JPoint;
 import edu.msoe.se2800.h4.jplot.Constants.GridMode;
 import edu.msoe.se2800.h4.jplot.plotpanel.PlotPanel;
 import edu.msoe.se2800.h4.jplot.plotpanel.PlotPanelAdminDecorator;
 import edu.msoe.se2800.h4.jplot.plotpanel.PlotPanelImmediateDecorator;
 import edu.msoe.se2800.h4.jplot.plotpanel.PlotPanelInterface;
 
 public class Grid extends JPanel implements GridInterface {
 	
 	private static Grid instance = null;
 	
 	public static Grid getInstance() {
 		if (instance == null) {
 			synchronized(Grid.class) {
 				if (instance == null) {
 					instance = new Grid();
 				}
 			}
 		}
 		return instance;
 	}
 	
 	/**
 	 * Generated serialVersionUID
 	 */
 	private static final long serialVersionUID = 1623331249534914071L;
 	
 	private PlotPanelInterface plotPanel;
 	private AxisPanel xAxisPanel, yAxisPanel;
 	private Path path;
 	private JPoint highlightedPoint;
 	private File loadedFile;
 	
 	private int gridDensity = Constants.DEFAULT_GRID_DENSITY;
 
 	private Grid() {
		path = new Path();
 		
 		setLayout(new BorderLayout());
 		setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT));
 		setVisible(true);
 		
 		redraw();
 	}
 	
 	@Override
 	public void initSubviews() {
 		plotPanel = new PlotPanel();
 		if (Constants.CURRENT_MODE == GridMode.ADMINISTRATOR_MODE) {
 			plotPanel = new PlotPanelAdminDecorator(plotPanel);
 		} else if (Constants.CURRENT_MODE == GridMode.IMMEDIATE_MODE) {
 			plotPanel = new PlotPanelImmediateDecorator(plotPanel);
 		}
 		xAxisPanel = new AxisPanel(Constants.HORIZONTAL);
 		yAxisPanel = new AxisPanel(Constants.VERTICAL);
 		
 		add(xAxisPanel, BorderLayout.SOUTH);
 		add(yAxisPanel, BorderLayout.WEST);
 		add(plotPanel.getComponent(), BorderLayout.CENTER);
 	}
 	
 	@Override
 	public void addSubview(Component c, Object constraints) {
 		add(c, constraints);
 	}
 	
 	@Override
 	public void addPoint(JPoint p) {
 		path.add(p);
 		redraw();
 	}
 	
 	public void removePoint(int indexOfPoint) {
 		path.getPoints().remove(indexOfPoint);
 		redraw();
 	}
 	
 	public List<JPoint> getPathPoints() {
 		return this.path.getPoints();
 	}
 	
 	public void zoomIn() {
 		setGridDensity(getGridDensity()-1);
 		redraw();
 	}
 	
 	public void zoomOut() {
 		setGridDensity(getGridDensity()+1);
 		redraw();
 	}
 	
 	public void redraw() {
 		repaint();
 	}
 	
 	public int getGridDensity() {
 		return gridDensity;
 	}
 	
 	public void setGridDensity(int density) {
 		if (density > 1) {
 			gridDensity = Math.min(density, 100);
 			redraw();
 		} else {
 			gridDensity = 1;
 			redraw();
 		}
 	}
 	
 	public JPoint getHighlightedPoint() {
 		return this.highlightedPoint;
 	}
 	
 	public void setHighlightedPoint(int indexInPointsArray) {
 		if (indexInPointsArray == -5) {
 			this.highlightedPoint = null;
 		} else {
 			this.highlightedPoint = this.path.get(indexInPointsArray);
 		}
 	}
 	
 	public Path getPath() {
 		return path;
 	}
 	
 	public File getLoadedPathFile() {
 		return loadedFile;
 	}
 	
 	public void setLoadedPathFile(File file) {
 		this.loadedFile = file;
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		setBackground(Color.BLACK);
 		g.clearRect(0, 0, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
 	}
 
 	@Override
 	public Component getComponent() {
 		return this;
 	}
 
 }
