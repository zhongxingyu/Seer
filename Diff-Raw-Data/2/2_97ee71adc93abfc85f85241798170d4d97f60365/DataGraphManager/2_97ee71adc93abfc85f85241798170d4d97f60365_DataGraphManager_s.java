 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Created on Apr 4, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.datagraph.state;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Image;
 import java.awt.KeyEventDispatcher;
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.geom.Point2D;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.concord.data.Unit;
 import org.concord.data.state.OTDataProducer;
 import org.concord.data.ui.DataStoreLabel;
 import org.concord.data.ui.StartableToolBar;
 import org.concord.datagraph.engine.ControllableDataGraphable;
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.datagraph.ui.DataAnnotation;
 import org.concord.datagraph.ui.DataGraph;
 import org.concord.datagraph.ui.DataGraph.TickMode;
 import org.concord.datagraph.ui.DataGraphToolbar;
 import org.concord.datagraph.ui.DataPointLabel;
 import org.concord.datagraph.ui.SingleDataAxisGrid;
 import org.concord.datagraph.ui.VerticalPlaybackLine;
 import org.concord.framework.data.DataDimension;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.view.OTControllerServiceFactory;
 import org.concord.framework.otrunk.view.OTJComponentViewContext;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.startable.Startable;
 import org.concord.framework.startable.StartableEvent;
 import org.concord.framework.startable.StartableListener;
 import org.concord.framework.util.CheckedColorTreeModel;
 import org.concord.framework.util.Copyable;
 import org.concord.graph.engine.CoordinateSystem;
 import org.concord.graph.engine.Drawable;
 import org.concord.graph.engine.Graphable;
 import org.concord.graph.engine.GraphableList;
 import org.concord.graph.engine.SelectableList;
 import org.concord.graph.event.GraphableListListener;
 import org.concord.graph.examples.GraphWindowToolBar;
 import org.concord.graph.ui.GraphTreeView;
 import org.concord.graph.ui.Grid2D;
 import org.concord.graph.ui.SingleAxisGrid;
 import org.concord.otrunk.OTrunkUtil;
 import org.concord.otrunk.logging.LogHelper;
 import org.concord.otrunk.logging.OTModelEvent.EventType;
 import org.concord.view.CheckedColorTreeControler;
 
 /**
  * @author scott
  * 
  * TODO To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Style - Code Templates
  */
 public class DataGraphManager implements OTChangeListener, ChangeListener,
 		CheckedColorTreeModel {
 	private static final Logger logger = Logger
 			.getLogger(DataGraphManager.class.getCanonicalName());
 	
 	OTDataCollector otDataCollector;
 	OTDataGraph otDataGraph;
 	DataGraph dataGraph;
 
 	DataGraphable sourceGraphable;
 	DataStoreLabel valueLabel;
 
 	OTObjectList labels;
 	SelectableList notesLayer;
 
 	OTDataAxis xOTAxis;
 	OTDataAxis yOTAxis;
 	
 	OTDataAxis authoredXOTAxis;
 	OTDataAxis authoredYOTAxis;
 
 	JPanel bottomPanel;
 	StartableToolBar toolBar = null;
 
 	boolean isCausingOTChange = false;
 	boolean isCausingRealObjChange = false;
 	
 	boolean instantRestart = false;				//sets whether start button is enabled after stop
 
 	boolean showDataControls;
 
 	Color[] colors = { Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN,
 			Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW,
 			Color.BLACK };
 
 	protected OTControllerService controllerService;
 	private OTViewContext viewContext;
 	private OTJComponentViewContext jComponentViewContext;
 	private KeyEventDispatcher treeDispatcher;
 	private DataGraphManagerStartable startable;
 	private StartableListener listener;
 	private OTrunk otrunk;
 
     private JComponent treeComponent;
 
 	private JButton clearButton;
 
 	/**
 	 * @param serviceProvider
 	 * @param jComponentViewContext
 	 *            TODO
 	 * 
 	 */
 	public DataGraphManager(OTDataGraph pfObject,
 			OTViewContext serviceProvider, boolean showDataControls,
 			OTJComponentViewContext jComponentViewContext) {
 		this.otDataGraph = pfObject;
 		if (pfObject instanceof OTDataCollector) {
 			otDataCollector = (OTDataCollector) pfObject;
 		}
 		this.showDataControls = showDataControls;
 		this.viewContext = serviceProvider;
 		this.jComponentViewContext = jComponentViewContext;
 
 		this.listener = new StartableListener(){
 			public void startableEvent(StartableEvent event) {
 				if(startable != null){
 					startable.relayEvent(event);
 				}
 			}					
 		};
 		
 		initialize();
 		
 		if (otDataCollector != null && startable != null) {
 			startable.addStartableListener(new StartableListener() {
 				OTDataGraphable lastCopy = null;
 				
 				@SuppressWarnings("deprecation")
 				public void startableEvent(StartableEvent event) {
 					HashMap<String, OTObject> extraInfo = new HashMap<String, OTObject>();
 					switch(event.getType()) {
 					case RESET:
                         if (otDataCollector.getLogGraphOnClear() || otDataCollector.getLogGraphOnReset()) {
                         	lastCopy = getGraphCopy(otDataCollector.getSource(), lastCopy);
                             extraInfo.put("graph", lastCopy);
                         }
 						LogHelper.add(otDataCollector, EventType.RESET, extraInfo);
 						break;
 					case STARTED:
                         if (otDataCollector.getLogGraphOnStart()) {
                         	lastCopy = getGraphCopy(otDataCollector.getSource(), lastCopy);
                             extraInfo.put("graph", lastCopy);
                         }
 						LogHelper.add(otDataCollector, EventType.START, extraInfo);
 						break;
 					case STOPPED:
                         if (otDataCollector.getLogGraphOnStop()) {
                         	lastCopy = getGraphCopy(otDataCollector.getSource(), lastCopy);
                             extraInfo.put("graph", lastCopy);
                         }
 						LogHelper.add(otDataCollector, EventType.STOP, extraInfo);
 						break;
 					}
 				}
 			});
 		}
 	}
 
 	public <T> T getViewService(Class<T> serviceClass) {
 		return viewContext.getViewService(serviceClass);
 	}
 
 	public OTControllerService getControllerService() {
 		return controllerService;
 	}
 	
 	public OTDataGraphable getSourceOTDataGraphable() {
 	    if(sourceGraphable == null){
             return null;
         }
         
         OTDataGraphable otSourceGraphable = (OTDataGraphable) controllerService.getOTObject(sourceGraphable);
         return otSourceGraphable;
 	}
 
 	public DataProducer getSourceDataProducer() {
 		// This will return the potential dataProducer of the
 		// sourceGraphable, this might be different than the current
 		// dataProducer. This is because of how the producerDataStores
 		// interact with dataDescriptions coming from their data Producer	
 		OTDataGraphable otSourceGraphable = getSourceOTDataGraphable();
 		if (otSourceGraphable == null) {
 			return null;
 		}
 		return getDataProducer(otSourceGraphable);
 	}
 	
 	public DataGraphable getSourceDataGraphable() {
 		return sourceGraphable;
 	}
 
 	protected void setSourceDataGraphable(DataGraphable sourceGraphable) {
 		DataProducer sourceDataProducer = getSourceDataProducer();
 		if(sourceDataProducer != null){
 			sourceDataProducer.removeStartableListener(listener);
 		}
 		this.sourceGraphable = sourceGraphable;
 		sourceDataProducer = getSourceDataProducer();
 		if(sourceDataProducer != null){
 			sourceDataProducer.addStartableListener(listener);
 		}
 	}
 	
 	public float getLastValue() {
 		if (valueLabel == null) {
 			return Float.NaN;
 		}
 		return valueLabel.getValue();
 	}
 
 	/**
 	 * @return
 	 */
 	public JPanel getBottomPanel() {
 		return bottomPanel;
 	}
 
 	public DataGraph getDataGraph() {
 		return dataGraph;
 	}
 
 	protected OTDataGraph getOTDataGraph() {
 		return otDataGraph;
 	}
 
 	private void setupStartable() {
 		// only do this once
 		if(startable != null){
 			return;
 		}
 		
 		startable = new DataGraphManagerStartable(this);
 	}
 	
 	public Startable getStartable() {
 		setupStartable();
 		return startable;
 	}
 	
 	public void setToolbarVisible(boolean visible) {
 		GraphWindowToolBar gwToolbar = dataGraph.getToolBar();
 		if (gwToolbar != null) {
 			// FIXME
 			gwToolbar.setVisible(visible);
 		}
 	}
 
 	/**
 	 * This only works for graphables that came from a loaded pfgraphables. It
 	 * doesn't yet handel cases where new graphables are created by some
 	 * external thing
 	 * 
 	 */
 	public void updateState(Object obj) {
 		// If the change was due to the graph area or coordinate system
 		if (obj == dataGraph.getGraphArea()
 				|| obj == dataGraph.getGraphArea().getCoordinateSystem()) {
 
 			Grid2D grid = dataGraph.getGrid();
 			
 			isCausingOTChange = true;
 
 			xOTAxis.setMin((float) dataGraph.getMinXAxisWorld());
 			xOTAxis.setMax((float) dataGraph.getMaxXAxisWorld());
 			yOTAxis.setMin((float) dataGraph.getMinYAxisWorld());
 			yOTAxis.setMax((float) dataGraph.getMaxYAxisWorld());
 
 			SingleAxisGrid sXAxis = grid.getXGrid();
 			if (sXAxis.getAxisLabel() != null) {
 				xOTAxis.setLabel(sXAxis.getAxisLabel());
 			}
 
 			SingleAxisGrid sYAxis = grid.getYGrid();
 			if (sYAxis.getAxisLabel() != null) {
 				yOTAxis.setLabel(sYAxis.getAxisLabel());
 			}
 			
 			otDataGraph.setTickMode(dataGraph.getTickMode());
 			otDataGraph.otUnSet(otDataGraph.otClass().getProperty("autoTick"));
 			
 			otDataGraph.setXTickInterval(dataGraph.getXTickInterval());
 			otDataGraph.setYTickInterval(dataGraph.getYTickInterval());
 			
 			// This is a general notification of a change, not one specific to a
 			// property
 			xOTAxis.notifyOTChange(null, null, null, null);
 			yOTAxis.notifyOTChange(null, null, null, null);
 			isCausingOTChange = false;
 		}
 
 		/*
 		 * if(obj instanceof DataGraphable) { OTDataGraphable source =
 		 * dataCollector.getSource();
 		 * 
 		 * source.saveObject(obj); }
 		 */
 	}
 
 	/**
 	 * @see org.concord.framework.otrunk.OTChangeListener#stateChanged(org.concord.framework.otrunk.OTChangeEvent)
 	 */
 	public void stateChanged(OTChangeEvent e) {
 		if (isCausingOTChange) {
 			// we are the cause of this change
 			return;
 		}
 		
 		isCausingRealObjChange = true;
 		
 		if (e.getSource() == xOTAxis || e.getSource() == yOTAxis) {
 			dataGraph.setLimitsAxisWorld(xOTAxis.getMin(), xOTAxis.getMax(),
 					yOTAxis.getMin(), yOTAxis.getMax());
 		} else if (e.getSource() == otDataGraph) {
 			// OT Data Graph has changed. We need to update the real DataGraph
 			// Find out what kind of change it is and whether it was a data
 			// graphable or a label
 			if (e.getOperation() == OTChangeEvent.OP_ADD
 					|| e.getOperation() == OTChangeEvent.OP_REMOVE) {
 
 				// Graphable added or removed
 				OTObject otGraphable = (OTObject) e.getValue();
 				if (e.getProperty().equals("graphables")) {
 					initNewGraphable(otGraphable);
 				} else if (e.getProperty().equals("labels")) {
 					initNewLabel(otGraphable);
 				}
 			}
 		} else if (e.getSource() instanceof OTDataGraphable) {
 			// A OT data graphable changed (not implemented anymore)
 			/*
 			 * if (e.getOperation() == OTChangeEvent.OP_SET){ OTObject
 			 * otGraphable = (OTObject)e.getSource();
 			 * updateGraphable(otGraphable); }
 			 */
 		}
 		
 		isCausingRealObjChange = false;
 	}
 
 	/**
 	 * Event triggered by the graph area changing
 	 * 
 	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
 	 */
 	public void stateChanged(ChangeEvent e) {
 		// System.out.println("state changed "+e.getSource());
 		if (!isCausingRealObjChange){
 			Object source = e.getSource();
 			updateState(source);
 		}
 	}
 
 	public void setSelectedItem(Object item, boolean checked) {
 		setSelectedDataGraphable((DataGraphable) item, checked);
 	}
 
 	protected void setSelectedDataGraphable(DataGraphable dg, boolean visible) {
 		DataGraphable oldGraphable = sourceGraphable;
 		setSourceDataGraphable(dg);
 
 		updateBottomPanel(oldGraphable, sourceGraphable);
 
 		if(dg != null){
 			dg.setVisible(visible);
 		}
 		
 		if(startable != null) {
 			startable.update();
 		}
 		
 		dataGraph.setSelectedGraphable(sourceGraphable);
 	}
 
 	private void removeLabelsFrom(DataGraphable dataGraphable,
 			GraphableList graphables) {
 		for (int i = 0; i < graphables.size(); i++) {
 			Object graphable = graphables.get(i);
 			if (graphable instanceof DataAnnotation) {
 				if (((DataAnnotation) graphable).getDataGraphable() == dataGraphable) {
 					graphables.remove(i);
 					continue;
 				}
 			}
 		}
 	}
 
 	public static void setupAxisLabel(SingleDataAxisGrid sAxis, OTDataAxis axis) {
 		if (axis.getLabel() != null) {
 			sAxis.setAxisLabel(axis.getLabel());
 		}
 
 		if (axis.getUnits() != null) {
 			String unitStr = axis.getUnits();
 			Unit unit = Unit.findUnit(unitStr);
 			if (unit == null) {
 				logger.fine("Can't find unit: " + unitStr);
 				sAxis.setUnit(new UnknownUnit(unitStr));
 			} else {
 				sAxis.setUnit(unit);
 			}
 		}
 
 		if (axis.isResourceSet("intervalWorld")) {
 			sAxis.setIntervalFixedWorld(axis.getIntervalWorld());
 		}
 		
 		OTResourceMap labelMap = axis.getCustomGridLabels();
 		String[] labelMapKeys = labelMap.getKeys();
 		for (String strValue : labelMapKeys) {
 			try {
 				double value = Double.parseDouble(strValue);
 				String label = (String) labelMap.get(strValue);
 				
 				// if it looks like it's an image url, try to add an image to the axis. For now we'll
 				// always keep the numbers as well. Eventually this should be settable.
 				//
 				// Image urls can contain their size within them. For instance, "example.jpg" will
 				// always be scaled to 25x25, but "example-50px.jpg" take example.jpg and scale it to 50x50
 				//
 				// Also, you can set *both* a text override and an image by using, e.g. 'Red::red-image.jpg'. This
 				// will set the text of the label to "Red" and the image to "red-image.jpg."
 				if (label.endsWith(".jpg") || label.endsWith(".jpeg") || label.endsWith(".png") || label.endsWith(".gif")){
 					try {
 						String labelText = null;
 						String[] labelParts = label.split("::");
 						if (labelParts.length == 2){
 							labelText = labelParts[0];
 							label = labelParts[1];
 						}
 						
 						int size = 25;
 						// see if image size is contained in label
 						String strippedLabel = label.substring(0, label.lastIndexOf("."));
 						labelParts = strippedLabel.split("-");
 						if (labelParts.length > 1 && labelParts[labelParts.length - 1].endsWith("px")){
 							String sizeString = labelParts[labelParts.length - 1];
 							size = Integer.parseInt(sizeString.substring(0, sizeString.length()-2));
 							label = label.replaceAll("-"+sizeString, "");
 						}
 						
 						ImageIcon icon = new ImageIcon(new URL(label));
 						sAxis.addGridLabelOverride(value, icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH), true);
 						if (labelText != null)
 							sAxis.addGridLabelOverride(value, labelText);
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 						sAxis.addGridLabelOverride(value, label);
 					}
 				} else {
 					sAxis.addGridLabelOverride(value, label);
 				}
 			} catch (NumberFormatException e) {
 				logger.warning("Grid label key -- expected a double, got: " + strValue);
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void initialize() {
 		OTControllerServiceFactory controllerServiceFactory = 
 			getViewService(OTControllerServiceFactory.class);
 		
 		otrunk = otDataGraph.getOTObjectService().getOTrunkService(OTrunk.class);
 
 		controllerService = 
 			controllerServiceFactory.createControllerService(otDataGraph.getOTObjectService());
 		controllerService.addService(OTViewContext.class, viewContext);
 		controllerService.addService(OTJComponentViewContext.class,
 				jComponentViewContext);
 
 		dataGraph = new DataGraph();
 		
 		dataGraph.setAntialias(otDataGraph.getAntialias());
 		
 		if (otDataGraph.isResourceSet("showToolbar")
 				&& !otDataGraph.getShowToolbar()) {
 			dataGraph.setToolBar(null);
 		} else {
 			dataGraph.changeToDataGraphToolbar();
 		}
 		
 		double xTick = otDataGraph.getXTickInterval();
 		double yTick = otDataGraph.getYTickInterval();
 		TickMode mode;
 		if (otDataGraph.isResourceSet("tickMode")) {
 		    mode = otDataGraph.getTickMode();
 		} else {
 		    if (otDataGraph.getAutoTick()) {
                 mode = TickMode.AUTO;
             } else {
                 mode = TickMode.FIXED;
             }
 		}
 		otDataGraph.otUnSet(otDataGraph.otClass().getProperty("autoTick"));
 		dataGraph.setTickInfo(mode, xTick, yTick);
 		
 		initGraphables();
 		dataGraph.setAutoFitMode(otDataGraph.getAutoFitMode());
 
 		dataGraph.setFocusable(true);
 
 		notesLayer = new SelectableList();
 		dataGraph.getGraph().add(notesLayer);
 		
 		if (otDataCollector!= null) {
 			dataGraph.setShowLabelCoordinates(otDataCollector.getShowLabelCoordinates());
 			dataGraph.setLabelCoordinatesDecPlaces(otDataCollector.getLabelDecimalPlaces());
 			dataGraph.setRestoreScaleOnReset(otDataCollector.getRestoreScaleOnReset());
 			dataGraph.setFillLabelBackground(otDataCollector.getFillLabelBackground());
 			dataGraph.setShowInfoLabel(otDataCollector.getShowInfoLabel());
 		}
 
 		if (dataGraph.getToolBar() != null && dataGraph.getToolBar() instanceof DataGraphToolbar) {
 			DataGraphToolbar toolbar = (DataGraphToolbar) dataGraph.getToolBar();
 			toolbar.setDataGraph(dataGraph);
 			toolbar.setNotesLayer(notesLayer);
 			
 			toolbar.removeAll();
 			
 			if (otDataCollector == null || otDataCollector.getUseDefaultToolBar()){
 				boolean selectableIsDefault = true;
 				if (otDataCollector!= null && otDataCollector.getSource() != null){
 					DataGraphable sg = (DataGraphable) controllerService.getRealObject(otDataCollector.getSource());
 					if (sg instanceof ControllableDataGraphable){
 						toolbar.setSourceGraphable(sg);
 						toolbar.addButton(DataGraphToolbar.DRAWING_BTN, false);
 						selectableIsDefault = false;
 					}
 				}
     			toolbar.addButton(DataGraphToolbar.SELECT_BTN, selectableIsDefault);
     			toolbar.addButton(DataGraphToolbar.ZOOM_IN_BTN);
     			toolbar.addButton(DataGraphToolbar.ZOOM_OUT_BTN);
     			toolbar.addButton(DataGraphToolbar.RESTORE_SCALE_BTN);
     			// not shown by default to maintain backward compatibility
     			// toolbar.addButton(DataGraphToolbar.RESTORE_AUTHOR_SCALE_BTN);
     			toolbar.addButton(DataGraphToolbar.ADD_NOTE_BTN);
     			if (otDataCollector != null && otDataCollector.getRulerEnabled()) {
     				toolbar.addButton(DataGraphToolbar.RULER_BTN);
     			}
     			if (otDataCollector != null && otDataCollector.getAutoScaleEnabled()) {
     				toolbar.addButton(DataGraphToolbar.AUTOSCALE_GRAPH_BTN);
     				toolbar.addButton(DataGraphToolbar.AUTOSCALE_X_BTN);
     				toolbar.addButton(DataGraphToolbar.AUTOSCALE_Y_BTN);
     			}
 			} else {
 				if (otDataCollector!= null && otDataCollector.getSource() != null){
 					DataGraphable sg = (DataGraphable) controllerService.getRealObject(otDataCollector.getSource());
 					if (sg instanceof ControllableDataGraphable){
 						toolbar.setSourceGraphable(sg);
 						toolbar.addButton(DataGraphToolbar.DRAWING_BTN, true);
 					}
 				}
 				if (!otDataCollector.getDisplayButtons().equals("")){
     				String[] buttons = otDataCollector.getDisplayButtons().split(",");
     				for (int i = 0; i < buttons.length; i++) {
     					toolbar.addButton(Integer.parseInt(buttons[i]), i==0);
                     }
 				}
 			}
 		}
 
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		
 		treeDispatcher = new KeyEventDispatcher(){					
 			public boolean dispatchKeyEvent(KeyEvent e) {
 				if(dataGraph.isAncestorOf(e.getComponent()) &&
 						((e.getModifiers() | KeyEvent.KEY_RELEASED) != 0) &&
 				        ((e.getModifiersEx() & java.awt.event.InputEvent.CTRL_DOWN_MASK) != 0) &&
 				        (e.getKeyCode() == java.awt.event.KeyEvent.VK_T)) {
 					GraphTreeView gtv = new GraphTreeView();
 					gtv.setGraph(dataGraph.getGraph());
 					GraphTreeView.showAsDialog(gtv, "graph tree");
 					return true;
 				}
 				
 				return false;
 			}
 			
 		};
 
 		focusManager.addKeyEventDispatcher(treeDispatcher);
 
 		xOTAxis = otDataGraph.getXDataAxis();
 		yOTAxis = otDataGraph.getYDataAxis();
 		
 		// store the authored axis info, as well
 		try {
 		  authoredXOTAxis = otrunk.getRuntimeAuthoredObject(xOTAxis);
 		  authoredYOTAxis = otrunk.getRuntimeAuthoredObject(yOTAxis);
 		  
 		  dataGraph.setAuthoredLimitsAxisWorld(authoredXOTAxis.getMin(), authoredXOTAxis.getMax(),
 		                                       authoredYOTAxis.getMin(), authoredYOTAxis.getMax());
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Couldn't get authored versions of the axis objects!",e);
 		}
 		
 		float xMin = xOTAxis.getMin();
 		float xMax = xOTAxis.getMax();
 		float yMin = yOTAxis.getMin();
 		float yMax = yOTAxis.getMax();
 
 		// NaN and infinite aren't very useful values, so reset the axis value if we encounter one
 		if (authoredXOTAxis != null) {
     		if (Float.isNaN(xMin) || Float.isInfinite(xMin)) {
     		    xMin = authoredXOTAxis.getMin();
     		    xOTAxis.setMin(xMin);
     		}
     		if (Float.isNaN(xMax) || Float.isInfinite(xMax)) {
     		    xMax = authoredXOTAxis.getMax();
                 xOTAxis.setMax(xMax);
     		}
 		}
 		if (authoredYOTAxis != null) {
     		if (Float.isNaN(yMin) || Float.isInfinite(yMin)) {
     		    yMin = authoredYOTAxis.getMin();
                 yOTAxis.setMin(yMin);
     		}
     		if (Float.isNaN(yMax) || Float.isInfinite(yMax)) {
     		    yMax = authoredYOTAxis.getMax();
                 yOTAxis.setMax(yMax);
     		}
 		}
 
         xOTAxis.addOTChangeListener(this);
         yOTAxis.addOTChangeListener(this);
         
         dataGraph.setLimitsAxisWorld(xOTAxis.getMin(), xOTAxis.getMax(),
                 yOTAxis.getMin(), yOTAxis.getMax());
 		
 		dataGraph.setLockedX(xOTAxis.isLocked());
 		dataGraph.setLockedY(yOTAxis.isLocked());		
 		
 		boolean autoformatXAxis = xOTAxis.getLabelFormat().equalsIgnoreCase(OTDataAxis.FORMAT_ENGINEERING);
 		boolean autoformatYAxis = yOTAxis.getLabelFormat().equalsIgnoreCase(OTDataAxis.FORMAT_ENGINEERING);
 
 		dataGraph.setAutoformatAxes(autoformatXAxis, autoformatYAxis);
 
 		Grid2D grid = dataGraph.getGrid();
 
 		SingleDataAxisGrid sXAxis = (SingleDataAxisGrid) grid.getXGrid();
 
 		// DataGraphStateManager.setupAxisLabel(sXAxis, xOTAxis);
 		setupAxisLabel(sXAxis, xOTAxis);
 
 		SingleDataAxisGrid sYAxis = (SingleDataAxisGrid) grid.getYGrid();
 		// DataGraphStateManager.setupAxisLabel(sYAxis, yOTAxis);
 		setupAxisLabel(sYAxis, yOTAxis);
 		
 		// reset insets now that axes have been completely initialized
 		dataGraph.setInsets(dataGraph.calcInsets());
 
 		dataGraph.setPreferredSize(new Dimension(400, 320));
 
 		dataGraph.getGraphArea().addChangeListener(this);
 
 		otDataGraph.addOTChangeListener(this);
 
 		initLabels();
 		
 //		if (sourceGraphable != null
 //				&& sourceGraphable.findDataProducer() != null
 //				&& sourceGraphable.findDataProducer() instanceof DataGraphListener) {
 //			((DataGraphListener)sourceGraphable.findDataProducer()).graphInitialized();
 //		}
 //		for (DataProducer dp : dataGraph.getDataProducers()) {
 //			if (dp instanceof DataGraphListener)
 //				((DataGraphListener)dp).graphInitialized();
 //		}
 	}
 
 	protected void initGraphables() {
 		OTObjectList pfGraphables = otDataGraph.getGraphables();
 		Vector<DataGraphable> realGraphables = new Vector<DataGraphable>();
 
 		// for each list item get the data producer object
 		// add it to the data graph
 		for (int i = 0; i < pfGraphables.size(); i++) {
 			DataGraphable realGraphable = initNewGraphable(pfGraphables
 					.get(i));
 
 			realGraphables.add(realGraphable);
 		}
 
 		OTDataGraphable source = null;
 		if (otDataCollector != null) {
 			source = otDataCollector.getSource();
 		}
 
 		String title = otDataGraph.getTitle();		
 		
 		if (source != null) {
 			if (title == null) {
 				title = source.getName();
 			}
 
 			setSourceDataGraphable((DataGraphable) controllerService
 					.getRealObject(source));
 
 			// dProducer.getDataDescription().setDt(0.1f);
 			if (sourceGraphable instanceof ControllableDataGraphable && showDataControls) {
 				bottomPanel = new JPanel(new FlowLayout());
 				clearButton = new JButton("Clear");
 				clearButton.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 					    // only reset the currently selected graphable
 					    getSourceDataGraphable().reset();
 						// dataGraph.reset();
 					    // Log the event
 					    if (otDataCollector != null) {
 					    	LogHelper.add(otDataCollector, EventType.RESET);
 					    }
 					}
 				});
 
 				bottomPanel.add(clearButton);
 				// bottomPanel.add(about);
 
 				dataGraph.add(bottomPanel, BorderLayout.SOUTH);
 
 			} else if (showDataControls) {
 				bottomPanel = new JPanel(new FlowLayout());
 				if(toolBar != null){
 					
 				}
 				toolBar = new StartableToolBar();
 				setupStartable();
 				toolBar.setStartable(startable);
 
 				updateBottomPanel(null, sourceGraphable);
 
 				dataGraph.add(bottomPanel, BorderLayout.SOUTH);
 			}
 
 			if (sourceGraphable != null) {
 				realGraphables.insertElementAt(sourceGraphable, 0);
 				dataGraph.addDataGraphable(sourceGraphable);
 			}
 		}
 		setupStartable();
 
 		if (title != null) {
 			dataGraph.setTitle(title);
 		}
 
 		// If the enabled is not set then the multiple graphable control is
 		// shown if there is more than one graphable
 		// If the enabled is set to false then the multiple graphable control
 		// is not shown even if there is more than one.
 		// This whole logic should be re-worked
 		boolean multiAllowed = true;
 		boolean multiEnabled = false;
 		if (otDataCollector != null) {
 			boolean multiEnabledSet = otDataCollector
 					.isResourceSet("multipleGraphableEnabled");
 			multiEnabled = otDataCollector.getMultipleGraphableEnabled();
 			if (multiEnabledSet) {
 				multiAllowed = multiEnabled;
 			}
 		} else {
 			boolean multiEnabledSet = otDataGraph
 					.isResourceSet("showGraphableList");
 			if (multiEnabledSet) {
 				multiEnabled = otDataGraph.getShowGraphableList();
 				multiAllowed = multiEnabled;
 			}
 		}
 		if (multiEnabled || (multiAllowed && realGraphables.size() > 1)) {
 			CheckedColorTreeControler dataSetTree = new CheckedColorTreeControler();
 			treeComponent = dataSetTree.setup(this, true, otDataGraph.getGraphableListEditable());
 
 			// The source should be the last item because it was setup that
 			// way above. We want it selected
 			// This works but there is some strange behavior here.
 			// the dataSetTree.setup changes the sourceGraphable. because it
 			// sets the
 			// selected to be the first realGraphable.
 			if (source != null) {
 				dataSetTree.setSelectedRow(realGraphables.size() - 1);
 			}
 			dataGraph.add(treeComponent, BorderLayout.WEST);
 		}
 		
 		final OTDataProducer playbackDataProducer = otDataGraph.getPlaybackDataProducer();
         if (playbackDataProducer != null) {
 		    JPanel controlPanel = new JPanel(new BorderLayout());
 		    if (treeComponent != null) {
 		        controlPanel.add(treeComponent, BorderLayout.CENTER);
 		    }
 		    
 		    DataProducer dataProducer = (DataProducer) controllerService.getRealObject(playbackDataProducer);
 		    StartableToolBar controls = new StartableToolBar();
 		    controls.setStartable(dataProducer);
             
 		    controlPanel.add(controls, BorderLayout.SOUTH);
 		    
 		    dataGraph.add(controlPanel, BorderLayout.WEST);
 		    
 		    VerticalPlaybackLine playbackLine = new VerticalPlaybackLine(dataProducer);
 		    dataGraph.getGraph().add(playbackLine);
 		    
 		    dataProducer.addStartableListener(new StartableListener() {
 		        private OTDataProducer dataProducerCopy = null;
                 public void startableEvent(StartableEvent event) {
                     HashMap<String, OTObject> extraInfo = new HashMap<String, OTObject>();
                     switch(event.getType()) {
                     case RESET:
                         if (otDataCollector.getLogGraphOnPlaybackReset()) {
                         	dataProducerCopy = getGraphCopy(playbackDataProducer, dataProducerCopy);
                             extraInfo.put("graph", dataProducerCopy);
                         }
                         LogHelper.add(otDataCollector, EventType.PLAYBACK_RESET, extraInfo);
                         break;
                     case STARTED:
                         if (otDataCollector.getLogGraphOnPlaybackStart()) {
                         	dataProducerCopy = getGraphCopy(playbackDataProducer, dataProducerCopy);
                             extraInfo.put("graph", dataProducerCopy);
                         }
                         LogHelper.add(otDataCollector, EventType.PLAYBACK_START, extraInfo);
                         break;
                     case STOPPED:
                         if (otDataCollector.getLogGraphOnPlaybackStop()) {
                         	dataProducerCopy = getGraphCopy(playbackDataProducer, dataProducerCopy);
                             extraInfo.put("graph", dataProducerCopy);
                         }
                         LogHelper.add(otDataCollector, EventType.PLAYBACK_STOP, extraInfo);
                         break;
                     }
                 }
             });
 		}
 
 		// Listen to the graphable list
 		GraphableList graphableList = dataGraph.getObjList();
 		graphableList.addGraphableListListener(new MainLayerGraphableListener());
 		
 		//Take care of the extra graphables
 		OTObjectList otExtraGraphables = otDataGraph.getExtraGraphables();
 		for (int i = 0; i < otExtraGraphables.size(); i++) {
 			OTObject extraGraphable = otExtraGraphables.get(i);
 
 			if (extraGraphable == null) continue;
 			
 			Object realGraphable = controllerService.getRealObject(extraGraphable);
 			
 			if (realGraphable == null){
 				logger.warning("Real object for extra graphable not found: "+extraGraphable);
 				continue;
 			}
 			
 			if (realGraphable instanceof Drawable){
 				dataGraph.getGraph().add((Drawable)realGraphable);
 			}
 			else if (realGraphable instanceof GraphableList){
 				dataGraph.getGraph().add((GraphableList)realGraphable);
 			}
 			else{
 				logger.warning("Extra graphable cannot be added. Class not supported on a graph: "+realGraphable.getClass().getName());
 			}
 		}
 		//		
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	private <T extends OTObject> T getGraphCopy(T dp, T lastCopied) {
         // OTunkUtil returns true if the objects are the same
         if (lastCopied == null || ! OTrunkUtil.compareObjects(dp, lastCopied, true)) {
             try {
             	lastCopied = (T) otDataCollector.getOTObjectService().copyObject(dp, -1);
             } catch (Exception e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         return lastCopied;
     }
 
 	protected void updateBottomPanel(DataGraphable oldSourceGraphable,
 			DataGraphable newSourceGraphable) {
		if (toolBar == null) {
 			if (clearButton != null) {
 				clearButton.setEnabled(!newSourceGraphable.isLocked());
 			}
 			return;
 		}
 
 		bottomPanel.removeAll();
 		
 		if(valueLabel != null){
 			valueLabel.dispose();
 			valueLabel = null;
 		}
 		
 		bottomPanel.setLayout(new FlowLayout());
 		if(newSourceGraphable != null){
 			valueLabel = new DataStoreLabel(newSourceGraphable, 1);
 			valueLabel.setColumns(8);
 			bottomPanel.add(valueLabel);
 		}
 		
 		bottomPanel.add(toolBar);
 		bottomPanel.revalidate();
 	}
 
 	/**
 	 * Called when a new OT graphable was added and we need to create a real
 	 * graphable object for it and add it to the Data Graph
 	 * 
 	 * @param object
 	 * @return the new DataGraphable just added to the Data Graph
 	 */
 	protected DataGraphable initNewGraphable(OTObject otGraphable) {
 		isCausingRealObjChange = true;
 
 		DataGraphable realGraphable = (DataGraphable) controllerService
 				.getRealObject(otGraphable);
 
 		if (realGraphable == null) {
 			System.err
 					.println("Unable to get realGraphable from controllerService");
 			return null;
 		}
 		if (realGraphable.getDataProducer() != null) {
 			System.err
 					.println("Trying to display a background graphable with a data producer");
 		}
 
 		dataGraph.addDataGraphable(realGraphable);
 
 		// Listen to OT graphable changes (not anymore! the Graphable controller
 		// takes care of this)
 		// ((OTDataGraphable)otGraphable).addOTChangeListener(this);
 
 		isCausingRealObjChange = false;
 
 		return realGraphable;
 	}
 
 	/**
 	 * Called when an OT graphable is changed and we need to update the real
 	 * graphable object too
 	 * 
 	 * @param otGraphable
 	 */
 	protected void updateGraphable(OTObject otGraphable) {
 		isCausingRealObjChange = true;
 
 		DataGraphable realGraphable = (DataGraphable) controllerService
 				.getRealObject(otGraphable);
 
 		// Call loadRealObject on the controller
 		controllerService.loadRealObject(otGraphable, realGraphable);
 
 		isCausingRealObjChange = false;
 	}
 
 	protected void initLabels() {
 		OTObjectList pfDPLabels = otDataGraph.getLabels();
 
 		// Load the data point labels
 		for (int i = 0; i < pfDPLabels.size(); i++) {
 			OTObject obj = pfDPLabels.get(i);
 			initNewLabel(obj);
 		}
 
 		// Listen to the graphable list
 		notesLayer.addGraphableListListener(new NotesLayerGraphableListener());
 	}
 
 	/**
 	 * @param obj
 	 */
 	private Graphable initNewLabel(OTObject obj) {
 		Graphable label = (Graphable) controllerService.getRealObject(obj);
 
 		if (label instanceof DataAnnotation) {
 			((DataAnnotation) label).setGraphableList(dataGraph.getObjList());
 		}
 		
 		if (label instanceof DataPointLabel){
 		    DataPointLabel dpLabel = (DataPointLabel) label;
 		    dpLabel.setShowCoordinates(otDataCollector.getShowLabelCoordinates());
 			dpLabel.setShowInfoLabel(otDataCollector.getShowInfoLabel());
 		    dpLabel.setCoordinateDecimalPlaces(otDataCollector.getLabelDecimalPlaces());
 		    dpLabel.setFillBackground(otDataCollector.getFillLabelBackground());
 			if (otDataCollector.getReadOnlyLabels() || dpLabel.isReadOnly()) {
 			    // set the label as read-only if the label was already read-only, or if we're forcing them to be
 			    dpLabel.setReadOnly(true);
 			}
 		}
 		notesLayer.add(label);
 
 		return label;
 	}
 
 	public static class UnknownUnit implements DataDimension {
 		String unit;
 
 		public UnknownUnit(String unit) {
 			this.unit = unit;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.concord.framework.data.DataDimension#getDimension()
 		 */
 		public String getDimension() {
 			return unit;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.concord.framework.data.DataDimension#setDimension(java.lang.String)
 		 */
 		public void setDimension(String dimension) {
 			unit = dimension;
 		}
 	}
 
 	/**
 	 * This should work with Graphable protoypes not OTGraphable prototypes that
 	 * would make it more generally useful.
 	 * 
 	 * @param name
 	 * @param color
 	 * @param prototype
 	 * @return
 	 * @throws Exception
 	 */
 	protected DataGraphable addGraphable(String name, Color color,
 			OTDataGraphable prototype) throws Exception {
 		OTObjectService service = getOTDataGraph().getOTObjectService();
 
 		OTDataGraphable otGraphable = null;
 
 		if (prototype == null) {
 
 			otGraphable = service
 					.createObject(OTDataGraphable.class);
 			DataProducer sourceDataProducer = getSourceDataProducer();
 			if (sourceDataProducer != null) {
 				// copy the the producer,
 				// TODO there might be some way to use the same producer on 2
 				// datastores
 				// that would be a fall back for un copyable data producers
 				if (sourceDataProducer instanceof Copyable) {
 					DataProducer producer = (DataProducer) ((Copyable) sourceDataProducer)
 							.getCopy();
 					setDataProducer(otGraphable, producer);
 				} else {
 					System.err
 							.println("Cannot copy the source data producer:\n"
 									+ "  "
 									+ sourceDataProducer
 									+ "\n"
 									+ "  It doesn't implement the Copyable interface");
 				}
 			}
 
 			otGraphable.setDrawMarks(false);
 
 			// Might need to set default values for color
 			// and the name.
 			if (getSourceDataGraphable() instanceof ControllableDataGraphable) {
 			    otGraphable.setControllable(true);
 			}
 		} else {
 			otGraphable = (OTDataGraphable) service.copyObject(prototype, -1);
 		}
 		
 		DataGraphable graphable = (DataGraphable) controllerService
 				.getRealObject(otGraphable);
 
 		// I don't know if this should be done or not
 		if (name != null) {
 			// This doesn't fire a graphable changed event it should
 			graphable.setLabel(name);
 		}
 
 		if (color != null) {
 			graphable.setColor(color);
 		}
 
 		dataGraph.addDataGraphable(graphable);
 		// adding the graphable to the dataGraph will cause an event to
 		// be thrown so the ot graphable is added to the ot data graph
 		// otDataGraph.getGraphables().add(otGraphable);
 
 		return graphable;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.concord.framework.util.CheckedColorTreeModel#addItem(java.lang.Object,
 	 *      java.lang.String, java.awt.Color)
 	 */
 	public Object addItem(Object parent, String name, Color color) {
 		DataGraphable newGraphable = null;
 
 		OTObjectList prototypes = getOTDataGraph().getPrototypeGraphables();
 
 		try {
 			if (prototypes == null || prototypes.size() == 0) {
 				newGraphable = addGraphable(name, color, null);
 			} else {
 				for (int i = 0; i < prototypes.size(); i++) {
 					newGraphable = addGraphable(name, color,
 							(OTDataGraphable) prototypes.get(i));
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return newGraphable;
 	}
 
 	public Object removeItem(Object parent, Object item) {
 		DataGraphable dataGraphable = (DataGraphable) item;
 		dataGraph.removeDataGraphable(dataGraphable);
 
 		// remove any graphables that reference this one in
 		// either of the 2 graphable lists
 		removeLabelsFrom(dataGraphable, dataGraph.getObjList());
 		removeLabelsFrom(dataGraphable, notesLayer);
 
 		return null;
 	}
 
 	public void updateItems() {
 		getDataGraph().repaint();
 	}
 
 	public Color getItemColor(Object item) {
 		return ((DataGraphable) item).getColor();
 	}
 
 	public String getItemLabel(Object item) {
 		return ((DataGraphable) item).getLabel();
 	}
 
 	public void setItemLabel(Object item, String label) {
 		((DataGraphable) item).setLabel(label);
 
 	}
 
 	public void setItemChecked(Object item, boolean checked) {
 		((DataGraphable) item).setVisible(checked);
 		if(item == sourceGraphable && startable != null){
 			if(startable.isRunning()){
 				startable.stop();
 			}
 			startable.update();
 		}
 	}
 
 	public String getItemTypeName() {
 		return "Data Set";
 	}
 
 	public Vector<Object> getItems(Object parent) {
 		return (Vector<Object>) (dataGraph.getObjList().clone()); 
 	}
 
 	public Color getNewColor() {
 		Color color = null;
 
 		Vector<Graphable> graphables = dataGraph.getObjList();
 		for (int i = 0; i < colors.length; i++) {
 			color = colors[i];
 			boolean uniqueColor = true;
 			for (int j = 0; j < graphables.size(); j++) {
 				Color graphableColor = getItemColor(graphables.get(j));
 
 				if (graphableColor.equals(colors[i])) {
 					uniqueColor = false;
 					break;
 				}
 			}
 			if (uniqueColor) {
 				break;
 			}
 		}
 
 		if (color == null)
 			color = Color.BLACK;
 
 		return color;
 	}
 
 	public void viewClosed() {
 		// Remove all the OT listeners
 		xOTAxis.removeOTChangeListener(this);
 		yOTAxis.removeOTChangeListener(this);
 		otDataGraph.removeOTChangeListener(this);
 
 		// This should call dispose on all the controllers that are syncing
 		// the real objects with the ot objects.
 		controllerService.dispose();
 
 		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
 		focusManager.removeKeyEventDispatcher(treeDispatcher);
 		
 		// Not anymore!
 		// OTObjectList pfGraphables = otDataGraph.getGraphables();
 		// for(int i=0; i<pfGraphables.size(); i++) {
 		// OTChangeNotifying pfGraphable =
 		// (OTChangeNotifying)pfGraphables.get(i);
 		// pfGraphable.removeOTChangeListener(this);
 		// }
 	}
 
 	class NotesLayerGraphableListener implements GraphableListListener {
 		public void listGraphableAdded(EventObject e) {
 			// Verify we are not triggering this change ourselves
 			if (!isCausingRealObjChange) {
 				isCausingOTChange = true;
 				Object obj = e.getSource();
 				OTObject otObject = controllerService.getOTObject(obj);
 				otDataGraph.getLabels().add(otObject);
 				isCausingOTChange = false;
 			}
 		}
 
 		public void listGraphableChanged(EventObject e) {
 			// TODO verify this is necessary
 			// this is just copied from the old code
 			// Verify we are not triggering this change ourselves
 			if (!isCausingRealObjChange) {
 				isCausingOTChange = true;
 				updateState(e.getSource());
 				isCausingOTChange = false;
 			}
 		}
 
 		public void listGraphableRemoved(EventObject e) {
 			// Verify we are not triggering this change ourselves
 			if (!isCausingRealObjChange) {
 				isCausingOTChange = true;
 				Object obj = e.getSource();
 				OTObject otObject = controllerService.getOTObject(obj);
 				otDataGraph.getLabels().remove(otObject);
 				isCausingOTChange = false;
 			}
 		}
 	}
 
 	class MainLayerGraphableListener implements GraphableListListener {
 		public void listGraphableAdded(EventObject e) {
 			// TODO verify this is doesn't screw up things
 			// Verify we are not triggering this change ourselves
 			if (!isCausingRealObjChange) {
 				isCausingOTChange = true;
 				Object obj = e.getSource();
 				OTObject otObject = controllerService.getOTObject(obj);
 				otDataGraph.getGraphables().add(otObject);
 				isCausingOTChange = false;
 			}
 		}
 
 		public void listGraphableChanged(EventObject e) {
 			// TODO verify this is necessary
 			// this is just copied from the old code
 			// Verify we are not triggering this change ourselves
 			if (!isCausingRealObjChange) {
 				isCausingOTChange = true;
 				updateState(e.getSource());
 				isCausingOTChange = false;
 			}
 		}
 
 		public void listGraphableRemoved(EventObject e) {
 			// TODO verify this is doesn't screw up things
 			// Verify we are not triggering this change ourselves
 			if (!isCausingRealObjChange) {
 				isCausingOTChange = true;
 				Object obj = e.getSource();
 				OTObject otObject = controllerService.getOTObject(obj);
 				otDataGraph.getGraphables().remove(otObject);
 				isCausingOTChange = false;
 			}
 		}
 	}
 
 	DataProducer getDataProducer(OTDataGraphable model) {
 		OTDataProducer otDataProducer = model.getDataProducer();
 		return (DataProducer) controllerService.getRealObject(otDataProducer);
 	}
 
 	void setDataProducer(OTDataGraphable model, DataProducer dp) {
 		OTDataProducer otDataProducer = (OTDataProducer) controllerService
 				.getOTObject(dp);
 		model.setDataProducer(otDataProducer);
 	}
 
 	public void setOTJComponentViewContext(OTJComponentViewContext viewContext) {
 		jComponentViewContext = viewContext;
 	}
 	
 	public void setInstantRestart(boolean instantRestart){
 		this.instantRestart = instantRestart;
 	}
 	
 	public boolean getInstantRestart(){
 		return instantRestart;
 	}
 	
 	/*
 	 * This is a utility method for easy adding of labels. It adds a new note
 	 * with a set text to a specified data point on a dataGraphable.
 	 */
 	public void addDataPointLabel(String note, DataGraphable dataGraphable, int pointIndex){
 		DataPointLabel label = addDataPointLabel(note, false);
 		label.setDataGraphable(dataGraphable);
 		Point2D p = DataPointLabel.getPointDataGraphable(dataGraphable, pointIndex);
 		CoordinateSystem cs = dataGraph.getGraphArea().getCoordinateSystem();
 		Point2D pD = cs.transformToDisplay(p);
 		
 		// this sets all the properties on label needed for displaying in the right location
 		label.mouseReleased(new Point((int)pD.getX(), (int)pD.getY()));
 
 		label.addAtPoint(null, p);
 	}
 	
 	/*
 	 * This is a utility method for adding labels.
 	 * 
 	 * If newNote is true, this adds a new note to the graph as if the user had
 	 * just clicked the "Add Note" button. The note is then "attached" to the user's
 	 * mouse and if they click on a data line a new note will be added there.
 	 * 
 	 * If newNote is false, a new note is added to the graph as if it had already
 	 * been there. Note that it will not be displayed unless the note then has its
 	 * coordinates set.
 	 */
 	public DataPointLabel addDataPointLabel(String note, boolean newNote){
 		DataPointLabel label = new DataPointLabel(newNote);
 		label.setGraphableList(dataGraph.getObjList());
 		label.setMessage(note);
 		label.setShowCoordinates(otDataCollector.getShowLabelCoordinates());
 		label.setShowInfoLabel(otDataCollector.getShowInfoLabel());
 		label.setCoordinateDecimalPlaces(otDataCollector.getLabelDecimalPlaces());
 		label.setFillBackground(otDataCollector.getFillLabelBackground());
 		label.setReadOnly(otDataCollector.getReadOnlyLabels());
 		notesLayer.add(label);
 		return label;
 	}
 }
