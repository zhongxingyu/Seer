 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 
 import component.LikelihoodComponent;
 import gui.figure.FigureElement;
 import gui.figure.series.HistogramSeries;
 import gui.figure.series.XYSeries;
 import gui.figure.series.XYSeriesElement;
 import gui.figure.series.XYSeriesFigure;
 import parameter.AbstractParameter;
 import parameter.DoubleParameter;
 
 public class StatusFigure extends JPanel {
 
 	enum Mode {TRACE, HISTOGRAM};
 	
 	XYSeriesFigure traceFigure;
 	XYSeries[] series;
 	HistogramSeries[] histoSeries;
 	String[] titles;
 	String logKey = null;
 	Mode mode = Mode.TRACE;
 	
 	public StatusFigure(AbstractParameter<?> param, String logKey) {
 		this.param = param;
 		this.logKey = logKey;
 		initializeFigure();
 		//This ALL NEEDS TO BE REFACTORED BADLY! BUT how do we know if things should be grouped together, or not? 
 		String[] logKeyItems = logKey.split("\\t");
 		
 		
 		String logStr = (param.getLogItem(logKey)).toString();
 		String[] logToks = logStr.split("\\t");
 		series = new XYSeries[logToks.length];
 		titles = new String[logToks.length];
 		for(int i=0; i<Math.min(logToks.length, logKeyItems.length); i++) {
 			String displayName = logKeyItems[i];
 			if (i>0)
 				displayName = logKeyItems[i] + "(" + i + ")";
 			titles[i] = displayName;
 			series[i] = new XYSeries(displayName);
 			XYSeriesElement serEl = traceFigure.addDataSeries(series[i]);
 			serEl.setLineWidth(defaultLineWidth);	
 		}
 	}
 	
 	public StatusFigure(AbstractParameter<?> param) {
 		this.param = param;
 		Object t = param.getValue();
 		
 		initializeFigure();
 		if (t instanceof Double) {
 			series = new XYSeries[1];
 			series[0] = new XYSeries(param.getName());
 			XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
 			serEl.setLineWidth(defaultLineWidth);	
 		}
 		else {
 			if (t instanceof double[]) {
 				//also fine, but we'll draw multiple lines
 				double[] vals = (double[])t;
 				series = new XYSeries[ vals.length ];
 				for(int i=0; i<vals.length; i++) {
 					series[i] = new XYSeries(param.getName() + "(" + i + ")");
 					XYSeriesElement serEl = traceFigure.addDataSeries(series[i]);
 					serEl.setLineWidth(defaultLineWidth);	
 				}
 			}
 			else {
 				if (t instanceof Integer) {
 					//OK
 					//We could theoretically do an array of integers?
 					series = new XYSeries[1];
 				}
 				else {
 					throw new IllegalArgumentException("Can't create a Figure for parameter with type " + t.getClass());
 				}
 			}
 		}
 	}
 	
 	public StatusFigure(LikelihoodComponent comp) {
 		this.comp = comp;
 		initializeFigure();
 		series = new XYSeries[1];
 		series[0] = new XYSeries(comp.getLogHeader());
 		titles = new String[1];
 		titles[0] = "Log likelihood";
 		XYSeriesElement serEl = traceFigure.addDataSeries(series[0]);
 		serEl.setLineWidth(defaultLineWidth);
 	}
 	
 	/**
 	 * Creates the figure and sets a few defaults for it
 	 */
 	private void initializeFigure() {
 		this.setLayout(new BorderLayout());
 		traceFigure = new XYSeriesFigure();
 		this.add(traceFigure, BorderLayout.CENTER);
 		traceFigure.setAllowMouseDragSelection(false);
 		traceFigure.setXLabel("MCMC state");
 		traceFigure.getAxes().setNumXTicks(4);
 		traceFigure.getAxes().setNumYTicks(4);
 		initializePopup();
 		traceFigure.setYLabel(null);
 	}
 	
 	/**
 	 * Creates the popup 
 	 */
 	private void initializePopup() {
 		 popup = new JPopupMenu();
 		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
 		 switchItem = new JMenuItem("Switch to histogram");
 		 switchItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				switchHistoTrace();
 			}
 		 });
 		 popup.add(switchItem);
 		 
 		 
 		 histoOptions = new JMenuItem("Configure histogram");
 		 histoOptions.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				popupHistoOptions();
 			} 
 		 });
 		 popup.add(histoOptions);
 		 
 		 JMenuItem saveImage = new JMenuItem("Save image");
 		 saveImage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				saveImage();
 			}
 		 });
 		 
 		 popup.add(saveImage);
 		 PopupListener popupListener = new PopupListener();
 		 this.addMouseListener(popupListener);
 		 traceFigure.addMouseListener(popupListener);
 	}
 	
 	
 
 	protected void popupHistoOptions() {
 		HistoOptionsFrame histoFrame = new HistoOptionsFrame(this, histoSeries[0]);
 		histoFrame.setVisible(true);
 	}
 
 	protected void saveImage() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * Create the histogram series that are used to store data for drawing
 	 * in XYSeriesElements
 	 */
 	private void createHistograms() {
 		histoSeries = new HistogramSeries[series.length];
 		for(int i=0; i<series.length; i++) {
 			HistogramSeries hSeries = new HistogramSeries(titles[i] , series[i].getPointList(), 100, series[i].getMinY(), series[i].getMaxY());
 			histoSeries[i] = hSeries;
			System.out.println("Creating histogram with min X: " + hSeries.getMinX() + " maxX: " + hSeries.getMaxX() + " minY: "+ hSeries.getMinY() + " maxY: "+ hSeries.getMaxY());
 		}		
 	}
 	
 	
 	/**
 	 * Called to switch between the Histogram and Trace modes
 	 */
 	protected void switchHistoTrace() {
 		if (mode == Mode.TRACE) {
 			mode = Mode.HISTOGRAM; //We're now in histogram mode
 			traceFigure.removeAllSeries();
 			if (histoSeries == null) {
 				createHistograms();
 			}
 			
 			for(int i=0; i<series.length; i++) {
 				XYSeriesElement el = traceFigure.addDataSeries(histoSeries[i]);
 				el.setMode(XYSeriesElement.BOXES);				
 			}
 
			traceFigure.inferBoundsFromCurrentSeries();
 			traceFigure.repaint();
 			switchItem.setText("Switch to trace");
 			traceFigure.setXLabel(titles[0]);
 		}
 		else {
 			mode = Mode.TRACE;
 			traceFigure.removeAllSeries();
 			for(int i=0; i<series.length; i++) {
 				traceFigure.addDataSeries(series[i]);
 			}
 			switchItem.setText("Switch to histogram");
 			traceFigure.setXLabel("MCMC State");
 		}
 		
		traceFigure.inferBoundsFromCurrentSeries();
 		traceFigure.repaint();
 	}
 
 	public void updateHistogram(HistogramSeries hSeries, Integer bins) {
 		//Find the series we're replacing
 		int i = 0;
 		for(i=0; i<histoSeries.length; i++) {
 			if (histoSeries[i] == hSeries) {
 				break;
 			}
 		}
 		
 		if (i==histoSeries.length) {
 			//Couldn't find series we're replacing...abort
 			System.out.println("Aggh! Couldn't find index for series!");
 			return;
 		}
 		
 		//Find element to remove from figure
 		XYSeriesElement el = null;
 		for(Object obj : traceFigure.getElementList()) {
 			if (obj instanceof XYSeriesElement) {
 				XYSeriesElement testEl = (XYSeriesElement)obj;
 				if (testEl.getSeries() == hSeries) {
 					el = testEl;
 					break;
 				}
 			}
 		}
 		
 		if (el == null) {
 			return;
 		}
 		
		HistogramSeries newSeries = new HistogramSeries(titles[i] , series[i].getPointList(), bins, series[i].getMinY()*0.8, series[i].getMaxY()*1.1);
 		el.setSeries(newSeries);				
 		histoSeries[i] = newSeries;
 		repaint();
 	}
 	
 	/**
 	 * Called when the mcmc chain fires a new state ot the MainOutputWindow,
 	 * this is where we add new data to the chart 
 	 * @param state
 	 */
 	public void update(int state) {
 		if (param != null) {
 			String logStr = (param.getLogItem(logKey)).toString();
 			String[] logToks = logStr.split("\\t");
 			for(int i=0; i<logToks.length; i++) {
 				try {
 					Double val = Double.parseDouble(logToks[i]);
 					Point2D.Double point = new Point2D.Double(state, val);
 					series[i].addPointInOrder(point);
 					if (histoSeries != null) {
 						histoSeries[i].addValue(val);
 					}
 				}
 				catch (NumberFormatException nex) {
 					//don't worry about it
 				}
 			}
 		}
 		else {
 			Double val = comp.getCurrentLogLikelihood();
 			Point2D.Double point = new Point2D.Double(state, val);
 			series[0].addPointInOrder(point);
 			
 			if (histoSeries != null) {
 				if (histoSeries[0] == null)
 					createHistograms();
 				histoSeries[0].addValue(val);
 			}
 		}
 		traceFigure.inferBoundsPolitely();
 		repaint();
 	}
 	
 	
 	class PopupListener extends MouseAdapter {
 	    public void mousePressed(MouseEvent e) {
 	        maybeShowPopup(e);
 	    }
 
 	    public void mouseReleased(MouseEvent e) {
 	        maybeShowPopup(e);
 	    }
 
 	    private void maybeShowPopup(MouseEvent e) {
 	        if (e.isPopupTrigger()) {
 	            popup.show(e.getComponent(),
 	                       e.getX(), e.getY());
 	        }
 	    }
 	}
 	
 	private JMenuItem histoOptions;
 	private JMenuItem switchItem;
 	private JPopupMenu popup;
 	private AbstractParameter<?> param = null;
 	private LikelihoodComponent comp = null;
 	
 	private float defaultLineWidth = 1.1f;
 
 	
 }
