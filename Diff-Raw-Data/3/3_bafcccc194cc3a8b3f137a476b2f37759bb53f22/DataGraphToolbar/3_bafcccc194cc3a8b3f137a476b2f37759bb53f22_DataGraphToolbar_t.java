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
  * Last modification information:
  * $Revision: 1.8 $
  * $Date: 2007-03-08 22:10:52 $
  * $Author: sfentress $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.datagraph.ui;
 
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 
 import org.concord.datagraph.engine.ControllableDataGraphable;
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.graph.engine.AxisScale;
 import org.concord.graph.engine.GraphableList;
 import org.concord.graph.engine.MouseSensitive;
 import org.concord.graph.engine.MultiRegionAxisScale;
 import org.concord.graph.engine.Selectable;
 import org.concord.graph.engine.SelectableList;
 import org.concord.graph.examples.GraphWindowToolBar;
 import org.concord.graph.ui.DefaultGraphMouseManager;
 import org.concord.graph.ui.GraphMouseManager;
 import org.concord.graph.util.control.DrawingAction;
 import org.concord.swing.SelectableToggleButton;
 
 /**
  * DataGraphToolbar
  *
  * Date created: Feb 22, 2005
  *
  * @author Scott Cytacki<p>
  * @author Ingrid Moncada<p>
  *
  */
 public class DataGraphToolbar extends GraphWindowToolBar
 {
 	private static final long serialVersionUID = 1L;
 	protected AbstractButton selButton;
 	private SelectableList notesLayer;
 	private DataGraph dataGraph;
 	private DataGraphable sourceGraphable;
 	private GraphMouseManager defaultMouseManager;
     private DataGraphable selectedGraphable;
 
 	public final static int SELECT_BTN = 0;
 	public final static int ZOOM_IN_BTN = 1;
 	public final static int ZOOM_OUT_BTN = 2;
 	public final static int RESTORE_SCALE_BTN = 3;
 	public final static int ADD_NOTE_BTN = 4;
 	public final static int RULER_BTN = 5;
 	public final static int AUTOSCALE_GRAPH_BTN = 6;
 	public final static int AUTOSCALE_X_BTN = 7;
 	public final static int AUTOSCALE_Y_BTN = 8;
 	public final static int DRAWING_BTN = 9;				// not to be added to customization views
     private DrawingAction drawingAction;
 	
     public DataGraphToolbar()
     {
         this(new int[] {SELECT_BTN, ZOOM_IN_BTN, ZOOM_OUT_BTN, RESTORE_SCALE_BTN});
     }
     
     /**
      * Creates a tool bar with the buttons specified. The buttons will be
      * added in the order specified, and the first button added will be the
      * default button, which is selected when the graph is first opened.
      * 
      * @param buttons
      */
     public DataGraphToolbar(int[] buttons){
     	super(false);
     	
     	for (int i = 0; i < buttons.length; i++) {
 	        addButton(buttons[i], i==0);
         }
     	
     }
     
     public void setNotesLayer(SelectableList notesLayer){
     	this.notesLayer = notesLayer;
     }
     
     public void setDataGraph(DataGraph dataGraph){
     	this.dataGraph = dataGraph;
     }
     
     public void setSourceGraphable(DataGraphable sg){
     	this.sourceGraphable = sg;
     }
     
     /**
      * Adds button of the specified type and returns the newly
      * created button.
      * 
      * @param buttonType
      * @return
      */
     public AbstractButton addButton(int buttonType){
     	return addButton(buttonType, false);
     }
     
     /**
      * Adds button of the specified type and returns the newly
      * created button. If setDefault, button is set as the default
      * button, and is selected when graph is first shown.
      * 
      * @param buttonType
      * @return
      */
     public AbstractButton addButton(int buttonType, boolean setDefault){
     	AbstractButton button = null;
     	switch (buttonType){
     		case SELECT_BTN:
     			button = addButton("arrow.gif", 
     			        "" + MultiRegionAxisScale.DRAGMODE_TRANSLATE_DILATE, 
     			        "Move and Scale graph");
     			button.addActionListener(new DeselectAllActionListener());
     			button.addActionListener(new SwapManagerActionListener(null));
     			selButton = button;
     			break;
     		case ZOOM_IN_BTN:
     			button = addButton("zoomin.gif", 
         		        "" + AxisScale.DRAGMODE_ZOOM_IN, 
         		        "Zoom in to a point");
     			button.addActionListener(new SwapManagerActionListener(null));
     			break;
     		case ZOOM_OUT_BTN:
     			button = addButton("zoomout.gif", 
         		        "" + AxisScale.DRAGMODE_ZOOM_OUT, 
         		        "Zoom out from a point");
     			button.addActionListener(new SwapManagerActionListener(null));
     			break;
     		case RESTORE_SCALE_BTN:
     			button = addButton("restorescale.gif", 
     					"restorescale", "Restore initial scale", false);
     			button.addActionListener(new SwapManagerActionListener(null));
     			break;
     		case ADD_NOTE_BTN:
     			if (dataGraph != null && notesLayer != null){
         			button = new SelectableToggleButton(
 				        new AddDataPointLabelAction(notesLayer, dataGraph
 				                .getObjList(), dataGraph.getToolBar(),
 				                dataGraph.isShowLabelCoordinates(), dataGraph
 				                        .getLabelCoordinatesDecPlaces()));
         			button.setActionCommand("" + MultiRegionAxisScale.DRAGMODE_TRANSLATE_DILATE);
         			button.addActionListener(new DeselectAllActionListener());
         			button.addActionListener(new SwapManagerActionListener(new NotesMouseManager()));
         			addButton(button, "Add a note to a point in the graph");
     			} else {
     				System.err.println("DataGraph and NotesLayer must be added before add notes button may be added");
     			}
     			break;
     		case RULER_BTN:
     			if (dataGraph != null && notesLayer != null){
         			button = new SelectableToggleButton(
     						new AddDataPointLabelActionExt(notesLayer, dataGraph
     								.getObjList(), dataGraph.getToolBar()));
         			button.addActionListener(new DeselectAllActionListener());
         			button.addActionListener(new SwapManagerActionListener(null));
     				addButton(button, "Add a ruler to a point in the graph");
     			} else {
     				System.err.println("DataGraph and NotesLayer must be added before ruler button may be added");
     			}
 				break;
     		case AUTOSCALE_GRAPH_BTN:
     			if (dataGraph != null){
     				button = new JButton(new AutoScaleAction(dataGraph));
         			button.addActionListener(new SwapManagerActionListener(null));
     				addButton(button, "Autoscale the graph");
     			} else {
     				System.err.println("DataGraph must be added before autoscale button may be added");
     			}
     			break;
     		case AUTOSCALE_X_BTN:
     			if (dataGraph != null){
     				button = new JButton(new AutoScaleAction(AutoScaleAction.AUTOSCALE_X, dataGraph));
         			button.addActionListener(new SwapManagerActionListener(null));
     				addButton(button, "Autoscale the graph");
     			} else {
     				System.err.println("DataGraph must be added before autoscale button may be added");
     			}
     			break;
     		case AUTOSCALE_Y_BTN:
     			if (dataGraph != null){
     				button = new JButton(new AutoScaleAction(AutoScaleAction.AUTOSCALE_Y, dataGraph));
         			button.addActionListener(new SwapManagerActionListener(null));
     				addButton(button, "Autoscale the graph");
     			} else {
     				System.err.println("DataGraph must be added before autoscale button may be added");
     			}
     			break;
     		case DRAWING_BTN:
     			if (sourceGraphable != null){
    			    if (selectedGraphable == null) {
                        selectedGraphable = sourceGraphable;
                    }
     				drawingAction = new DrawingAction();
     				drawingAction.setDrawingObject((ControllableDataGraphable) selectedGraphable);
     				button = new SelectableToggleButton(drawingAction);
     				button.setActionCommand("" + MultiRegionAxisScale.DRAGMODE_TRANSLATE_DILATE);
         			// button.addActionListener(new DeselectAllActionListener());
         			button.addActionListener(new SwapManagerActionListener(new DrawingMouseManager()));
         			addButton(button, "Draw a function", 0, false, true);
     			} else {
     				System.err.println("sourceGraphable must be added before drawing button may be added");
     			}
     			break;
     		default:
     			System.err.println("No button of that type is defined");
     	}
     	if (setDefault){
     		setDefaultButton(button);
     	}
     	return button;
     }
     
     /**
      * Swap in a new mouse manager. Saves the default manager
      * the first time this is called. If null is passed in, re-adds
      * the original default manager.
      * 
      * @param manager
      */
     private void setMouseManager(GraphMouseManager manager){
     	if (dataGraph == null || dataGraph.graph == null)
     		return;
     	
     	if (defaultMouseManager == null){
     		defaultMouseManager = dataGraph.graph.getMouseManager();
     	}
     	
     	if (manager != null){
     		dataGraph.graph.setMouseManager(manager);
     	} else {
     		dataGraph.graph.setMouseManager(defaultMouseManager);
     	}
     }
     
 	/**
 	 * This is called when graph is first viewed, and is a good time to
 	 * select the default button.
 	 * 
 	 * @see org.concord.graph.examples.GraphWindowToolBar#addAxisScale(org.concord.graph.engine.AxisScale)
 	 */
 	@Override
     public void addAxisScale(AxisScale ax)
 	{
 		super.addAxisScale(ax);
 		if (getDefaultButton() != null){
 			getDefaultButton().doClick();
 		}
 	}
 	
 	// For editing window
 	public static int[] getAllButtons(){
 		return new int[] {
 				SELECT_BTN,
 				ZOOM_IN_BTN,
 				ZOOM_OUT_BTN,
 				RESTORE_SCALE_BTN,
 				RULER_BTN,
 				AUTOSCALE_GRAPH_BTN,
 				AUTOSCALE_X_BTN,
 				AUTOSCALE_Y_BTN,
 		};
 	}
 	
 	// For editing window
 	public static String buttonName(int buttonType){
 		switch (buttonType){
 			case SELECT_BTN:
 				return "Selection";
 			case ZOOM_IN_BTN:
 				return "Zoom in";
 			case ZOOM_OUT_BTN:
 				return "Zoom out";
 			case RESTORE_SCALE_BTN:
 				return "Restore original scale";
 			case ADD_NOTE_BTN:
 				return "Add label";
 			case RULER_BTN:
 				return "Ruler";
 			case AUTOSCALE_GRAPH_BTN:
 				return "Autoscale";
 			case AUTOSCALE_X_BTN:
 				return "Autoscale X";
 			case AUTOSCALE_Y_BTN:
 				return "Autoscale Y";
 			default:
 				System.err.println("Unknown button type: "+buttonType);
 				return "";
 		}
 	}
 	
 	private class DeselectAllActionListener implements ActionListener{
 
 		public void actionPerformed(ActionEvent e) {
 			if (dataGraph != null){
 				for (int i = 0; i < dataGraph.getObjList().size(); i++) {
 					if (dataGraph.getObjList().get(i) instanceof Selectable){
 						((Selectable)dataGraph.getObjList().get(i)).deselect();
 					}
 				}
 			}
 			if (notesLayer != null){
 				for (int i = 0; i < notesLayer.size(); i++) {
 					if (notesLayer.get(i) instanceof Selectable){
 						((Selectable)notesLayer.get(i)).deselect();
 					}
 				}
 			}
 		}
 	}
 	
 	private class SwapManagerActionListener implements ActionListener{
 		
 		private GraphMouseManager manager;
 
 		public SwapManagerActionListener(GraphMouseManager manager){
 			this.manager = manager;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			setMouseManager(manager);
 		}
 	}
 	
 	private class DrawingMouseManager extends DefaultGraphMouseManager{
 		@Override
         public MouseSensitive getFirstObjectToSelect(Point location, Vector list, boolean checkSelectedFirst)
 		{
 			//In the vector aux, we will store any MultiRegionAxisScale objects
 			Vector scaleObjs = new Vector();
 				
 			// Loop trough all the objects to see which one accepts the mouse click
 			// Look for selection candidate in backwards order
 			for (int i = list.size()-1; i>=0; i--){
 				Object obj = list.elementAt(i);
 				if (obj instanceof GraphableList){
 
 					MouseSensitive ms = getFirstObjectToSelect(location, (GraphableList)obj);
 					if (ms != null){
 						return ms;
 					}
 				}
 				else{
 					if (obj instanceof Selectable){
 						Selectable s = (Selectable)obj;
 						
 						if (checkSelectedFirst){
 							// Ignore everything but ControllableDataGraphables
 							// is there is a MultiRegionAxisScale, save it for the next pass
 							if (!s.isSelected() || !(s instanceof ControllableDataGraphable)){
 								if (s instanceof MultiRegionAxisScale)
 									scaleObjs.add(0,s);
 								continue;
 							}
 						}
 					}
 		
 					if (obj instanceof MouseSensitive){
 						MouseSensitive ms = (MouseSensitive)obj;
 						if (ms.isPointInProximity(location)){
 							return ms;
 						}
 					}
 				}
 			} // for
 			
 			//Now search in the non-selected objects
 			if (scaleObjs.size() > 0){
 				return getFirstObjectToSelect(location, scaleObjs, false); 
 			}
 			
 			return null;
 		}
 
 	}
 	
 	private class NotesMouseManager extends DefaultGraphMouseManager{
 		@Override
         public MouseSensitive getFirstObjectToSelect(Point location, Vector list, boolean checkSelectedFirst)
 		{
 			//In the vector aux, we will store any MultiRegionAxisScale objects
 			Vector otherLabels = new Vector();
 			
 			//In the vector aux, we will store any MultiRegionAxisScale objects
 			Vector scaleObjs = new Vector();
 				
 			// Loop trough all the objects to see which one accepts the mouse click
 			// Look for selection candidate in backwards order
 			for (int i = list.size()-1; i>=0; i--){
 				Object obj = list.elementAt(i);
 				if (obj instanceof GraphableList){
 
 					MouseSensitive ms = getFirstObjectToSelect(location, (GraphableList)obj);
 					if (ms != null){
 						return ms;
 					}
 				}
 				else{
 					if (obj instanceof Selectable){
 						Selectable s = (Selectable)obj;
 						
 						if (checkSelectedFirst){
 							// Ignore everything but ControllableDataGraphables
 							// is there is a MultiRegionAxisScale, save it for the next pass
 							if (!s.isSelected()){
 								if (s instanceof DataPointLabel){
 									otherLabels.add(0,s);
 								} else if (s instanceof MultiRegionAxisScale){
 									scaleObjs.add(0,s);
 								}
 								continue;
 							}
 						}
 					}
 		
 					if (obj instanceof DataPointLabel || obj instanceof MultiRegionAxisScale){
 						MouseSensitive ms = (MouseSensitive)obj;
 						if (ms.isPointInProximity(location)){
 							return ms;
 						}
 					}
 				}
 			} // for
 			
 			//Now search in the non-selected objects
 			if (otherLabels.size() > 0){
 				return getFirstObjectToSelect(location, otherLabels, false); 
 			}
 			
 			//Now search in the non-selected objects
 			if (scaleObjs.size() > 0){
 				return getFirstObjectToSelect(location, scaleObjs, false); 
 			}
 			
 			return null;
 		}
 
 	}
 	
     public void setSelectedGraphable(DataGraphable selectedGraphable) {
         this.selectedGraphable = selectedGraphable;
         if (drawingAction != null && this.selectedGraphable instanceof ControllableDataGraphable) {
             // System.out.println("Setting new selected graphable: " + this.selectedGraphable.getLabel());
             drawingAction.setDrawingObject((ControllableDataGraphable) this.selectedGraphable);
         }
     }
 }
