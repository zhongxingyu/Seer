 package dk.frv.eavdam.layers;
 
 import com.bbn.openmap.InformationDelegator;
 import com.bbn.openmap.MapBean;
 import com.bbn.openmap.event.InfoDisplayEvent;
 import com.bbn.openmap.event.MapMouseEvent;
 import com.bbn.openmap.event.MapMouseListener;
 import com.bbn.openmap.event.NavMouseMode;
 import com.bbn.openmap.event.SelectMouseMode;
 import com.bbn.openmap.gui.OpenMapFrame;
 import com.bbn.openmap.layer.OMGraphicHandlerLayer;
 import com.bbn.openmap.omGraphics.OMAction;
 import com.bbn.openmap.omGraphics.OMCircle;
 import com.bbn.openmap.omGraphics.OMDistance;
 import com.bbn.openmap.omGraphics.OMGraphic;
 import com.bbn.openmap.omGraphics.OMGraphicList;
 import com.bbn.openmap.omGraphics.OMList;
 import com.bbn.openmap.omGraphics.OMPoly;
 import com.bbn.openmap.omGraphics.OMRect;
 import com.bbn.openmap.proj.Length;
 import com.bbn.openmap.tools.drawing.DrawingTool;
 import com.bbn.openmap.tools.drawing.DrawingToolRequestor;
 import com.bbn.openmap.tools.drawing.OMDrawingTool;
 import dk.frv.eavdam.app.SidePanel;
 import dk.frv.eavdam.data.ActiveStation;
 import dk.frv.eavdam.data.AISFixedStationCoverage;
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.Antenna;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.data.EAVDAMUser;
 import dk.frv.eavdam.data.Options;
 import dk.frv.eavdam.data.OtherUserStations;
 import dk.frv.eavdam.data.Simulation;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.io.XMLImporter;
 import dk.frv.eavdam.menus.EavdamMenu;
 import dk.frv.eavdam.menus.OptionsMenuItem;
 import dk.frv.eavdam.menus.StationInformationMenuItem;
 import dk.frv.eavdam.utils.DBHandler;
 import dk.frv.eavdam.utils.RoundCoverage;
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Point2D;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.bind.JAXBException;
 import javax.swing.ButtonGroup;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.SwingUtilities;
 
 public class StationLayer extends OMGraphicHandlerLayer implements MapMouseListener, ActionListener, DrawingToolRequestor {
 
 	private static final long serialVersionUID = 1L;
 
     private MapBean mapBean;
 	private OpenMapFrame openMapFrame;
 	private OMGraphicList graphics = new OMGraphicList();
 	private InformationDelegator infoDelegator;
     private DrawingTool drawingTool;
     private final com.bbn.openmap.tools.drawing.DrawingToolRequestor layer = this;
 	private SidePanel sidePanel;
 	private OMAISBaseStationTransmitCoverageLayer transmitCoverageLayer;
 	private OMAISBaseStationReceiveCoverageLayer receiveCoverageLayer;
 	private OMAISBaseStationInterferenceCoverageLayer interferenceCoverageLayer;	
 	private EavdamMenu eavdamMenu;	
 	private JMenuItem editStationMenuItem;
 	private JMenuItem editTransmitCoverageMenuItem;
 	private JMenuItem resetTransmitCoverageToCircleMenuItem;
 	private JMenuItem resetTransmitCoverageToPolygonMenuItem;
 	private JMenuItem editReceiveCoverageMenuItem;
 	private JMenuItem resetReceiveCoverageToCircleMenuItem;
 	private JMenuItem resetReceiveCoverageToPolygonMenuItem;
 	private JMenuItem editInterferenceCoverageMenuItem;	
 	private JMenuItem resetInterferenceCoverageToCircleMenuItem;
 	private JMenuItem resetInterferenceCoverageToPolygonMenuItem;	
 	private OMBaseStation currentlySelectedOMBaseStation;
 	
 	private EAVDAMData data;
 	//private int currentIcons = -1;
 	
 	private boolean stationsInitiallyUpdated = false;
 	
 	private OMGraphicHandlerLayer currentlyEditingLayer;
 	
 	private Map<OMBaseStation, OMGraphic> transmitCoverageAreas = new HashMap<OMBaseStation, OMGraphic>();
 	private Map<OMBaseStation, OMGraphic> receiveCoverageAreas = new HashMap<OMBaseStation, OMGraphic>();
 	private Map<OMBaseStation, OMGraphic> interferenceCoverageAreas = new HashMap<OMBaseStation, OMGraphic>();
 		
     public StationLayer() {}
 
     public OMAISBaseStationTransmitCoverageLayer getTransmitCoverageLayer() {
         return transmitCoverageLayer;
     }
 
     public OMAISBaseStationReceiveCoverageLayer getReceiveCoverageLayer() {
         return receiveCoverageLayer;
     }
 
     public OMAISBaseStationInterferenceCoverageLayer getInterferenceCoverageLayer() {
         return interferenceCoverageLayer;
     }
 	
     public void addBaseStation(Object datasetSource, AISFixedStationData stationData) {
 
 	    byte[] bytearr = null;		
 		
 		if (datasetSource == null ||  // own stations
 				datasetSource instanceof String) {  // simulations
 			
 			if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_own_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_own_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_own_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_own_operative.png");				
 				}
 			
 			} else if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED ||
 					stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_SIMULATED) {
 					
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_own_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_own_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_own_planned.png");
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_own_planned.png");				
 				}					
 					
 			}
 							
 		} else if (datasetSource instanceof EAVDAMUser) {  // Other user's dataset		
 		
 			if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_other_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_other_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_other_operative.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_other_operative.png");
 				}
 			
 			} else if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED ||
 					stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_SIMULATED) {
 					
 				if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 					bytearr = getImage("share/data/images/ais_base_station_other_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 					bytearr = getImage("share/data/images/ais_repeater_other_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 					bytearr = getImage("share/data/images/ais_receiver_other_planned.png");				
 				} else if (stationData.getStationType() == AISFixedStationType.ATON) {
 					bytearr = getImage("share/data/images/ais_aton_station_other_planned.png");				
 				}					
 					
 			}
 
 		}				
 		
 	    /*
 		if (stationData.getStationType() == AISFixedStationType.BASESTATION) {
 	        if (currentIcons == Options.LARGE_ICONS) {
                 bytearr = getImage("share/data/images/ais_base_station.png");
             } else if (currentIcons == Options.SMALL_ICONS) {
                 bytearr = getImage("share/data/images/ais_base_station_small.png");
             } else {
                 currentIcons = Options.LARGE_ICONS;
                 bytearr = getImage("share/data/images/ais_base_station.png");
             }                
 	    } else if (stationData.getStationType() == AISFixedStationType.REPEATER) {
 	        if (currentIcons == Options.LARGE_ICONS) {
                 bytearr = getImage("share/data/images/ais_repeater_station.png");
             } else if (currentIcons == Options.SMALL_ICONS) {
                 bytearr = getImage("share/data/images/ais_repeater_station_small.png");
             } else {
                 currentIcons = Options.LARGE_ICONS;
                 bytearr = getImage("share/data/images/ais_repeater_station.png");
             }          
 	    } else if (stationData.getStationType() == AISFixedStationType.RECEIVER) {
 	        if (currentIcons == Options.LARGE_ICONS) {
                 bytearr = getImage("share/data/images/ais_receiver_station.png");
             } else if (currentIcons == Options.SMALL_ICONS) {
                 bytearr = getImage("share/data/images/ais_receiver_station_small.png");
             } else {
                 currentIcons = Options.LARGE_ICONS;
                 bytearr = getImage("share/data/images/ais_receiver_station.png");
             }  	        
 	    } else if (stationData.getStationType() == AISFixedStationType.ATON) {
 	        if (currentIcons == Options.LARGE_ICONS) {
                 bytearr = getImage("share/data/images/ais_aton_station.png");
             } else if (currentIcons == Options.SMALL_ICONS) {
                 bytearr = getImage("share/data/images/ais_aton_station_small.png");
             } else {
                 currentIcons = Options.LARGE_ICONS;
                 bytearr = getImage("share/data/images/ais_aton_station.png");
             }
         }
 		*/
         
         OMBaseStation base = new OMBaseStation(datasetSource, stationData, bytearr);		
 		Antenna antenna = stationData.getAntenna();
 		if (antenna != null) {			
 			if (stationData.getStationType() != AISFixedStationType.RECEIVER) {		
 				if (stationData.getTransmissionCoverage() != null && stationData.getTransmissionCoverage().getCoveragePoints() != null) {
 					base.setTransmitCoverageArea(stationData.getTransmissionCoverage().getCoveragePoints());
 				} else {
 					ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(), 25);
 					base.setTransmitCoverageArea(points);
 				}
 				if (stationData.getInterferenceCoverage() != null && stationData.getInterferenceCoverage().getCoveragePoints() != null) {
 					base.setInterferenceCoverageArea(stationData.getInterferenceCoverage().getCoveragePoints());
 				} else {
 					ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(stationData.getLat(), stationData.getLon(), 25);
 					base.setInterferenceCoverageArea(points);
 				}
 			}
 			if (stationData.getReceiveCoverage() != null && stationData.getReceiveCoverage().getCoveragePoints() != null) {
 				base.setReceiveCoverageArea(stationData.getReceiveCoverage().getCoveragePoints());
 			} else {
 				ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(), 25);
 				base.setReceiveCoverageArea(points);
 			}
 						
 		}
 
 		graphics.add(base);
 		graphics.project(getProjection(), true);
 		this.repaint();
 		this.validate();
 
 		if (stationData.getStationType() != AISFixedStationType.RECEIVER) {	
 			Object transmitCoverageAreaGraphics = transmitCoverageLayer.addTransmitCoverageArea(base);
 			if (transmitCoverageAreaGraphics != null) {
 				if (transmitCoverageAreaGraphics instanceof OMCircle) {
 					transmitCoverageAreas.put(base, (OMCircle) transmitCoverageAreaGraphics);
 				} else if (transmitCoverageAreaGraphics instanceof OMPoly) {
 					transmitCoverageAreas.put(base, (OMPoly) transmitCoverageAreaGraphics);
 				}		
 			}
 			Object interferenceCoverageAreaGraphics = interferenceCoverageLayer.addInterferenceCoverageArea(base);
 			if (interferenceCoverageAreaGraphics != null) {
 				if (interferenceCoverageAreaGraphics instanceof OMCircle) {
 					interferenceCoverageAreas.put(base, (OMCircle) interferenceCoverageAreaGraphics);
 				} else if (interferenceCoverageAreaGraphics instanceof OMPoly) {
 					interferenceCoverageAreas.put(base, (OMPoly) interferenceCoverageAreaGraphics);
 				}		
 			}			
 		}
 		Object receiveCoverageAreaGraphics = receiveCoverageLayer.addReceiveCoverageArea(base);
 		if (receiveCoverageAreaGraphics != null) {
 			if (receiveCoverageAreaGraphics instanceof OMCircle) {
 				receiveCoverageAreas.put(base, (OMCircle) receiveCoverageAreaGraphics);
 			} else if (receiveCoverageAreaGraphics instanceof OMPoly) {
 				receiveCoverageAreas.put(base, (OMPoly) receiveCoverageAreaGraphics);
 			}		
 		}
 
 	}
 
 	@Override
 	public synchronized OMGraphicList prepare() {
 		graphics.project(getProjection(), true);
 		return graphics;
 	}
 
 	public MapMouseListener getMapMouseListener() {
 		return this;
 	}
 		
 	@Override
 	public String[] getMouseModeServiceList() {
 		String[] ret = new String[2];
 		ret[0] = NavMouseMode.modeID;
 		ret[1] = SelectMouseMode.modeID;
 		return ret;
 	}
 	
 	 public DrawingTool getDrawingTool() {
         // Usually set in the findAndInit() method.
         return drawingTool;
     }
 
     public void setDrawingTool(DrawingTool dt) {
         // Called by the findAndInit method.
         drawingTool = dt;
     }
 	
     /**
      * Called when the DrawingTool is complete, providing the layer with the
      * modified OMGraphic.
      */
 	@Override	 
     public void drawingComplete(OMGraphic omg, OMAction action) {
 	
 		if (currentlyEditingLayer != null) {
 			ArrayList<double[]> points = new ArrayList<double[]>();
 		
 			if (omg instanceof OMCircle) {
 				double[] radiuses = new double[2];
 				double degrees = ((OMCircle) omg).getRadius();
 				double radians = RoundCoverage.degrees2radians(degrees);
 				double kilometers = radians * RoundCoverage.EARTH_RADIUS;
 				radiuses[0] = kilometers;
 				radiuses[1] = kilometers;
 				points.add(radiuses);
 				
 			} else if (omg instanceof OMPoly) {					
 				double[] latlonArray = ((OMPoly) omg).getLatLonArray();
 				int index=0;
 				while (index+1<latlonArray.length) {
 					double lat = latlonArray[index];
 					double lon = latlonArray[index+1];
 					double[] latlon = new double[2];
 					latlon[0] = RoundCoverage.radians2degrees(lat);
 					latlon[1] = RoundCoverage.radians2degrees(lon);
 					points.add(latlon);
 					index = index+2;
 				}
 			}
 
 			if (data == null) {
 				data = DBHandler.getData();                        
 			}
 			data = saveCoverage(data, points, currentlyEditingLayer);
 			currentlyEditingLayer = null;
 			DBHandler.saveData(data);    					
 			
 		}
 	
 /*
         Object obj = omg.getAppObject();
 
         if (obj != null && (obj == internalKey || obj == externalKey)
                 && !action.isMask(OMGraphicConstants.DELETE_GRAPHIC_MASK)) {
 
             java.awt.Shape filterShape = omg.getShape();
             OMGraphicList filteredList = filter(filterShape,
                     (omg.getAppObject() == internalKey));
             if (Debug.debugging("demo")) {
                 Debug.output("DemoLayer filter: "
                         + filteredList.getDescription());
             }
         } else {
             if (!doAction(omg, action)) {
                 // null OMGraphicList on failure, should only occur if
                 // OMGraphic is added to layer before it's ever been
                 // on the map.
                 setList(new OMGraphicList());
                 doAction(omg, action);
             }
         }
 */
         repaint();
     }		
 	
 	@Override
 	public boolean mouseClicked(MouseEvent e) {			
 		if (SwingUtilities.isLeftMouseButton(e)) {
     		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
     		for (OMGraphic omGraphic : allClosest) {
     			if (omGraphic instanceof OMBaseStation) {
     				System.out.println("Mouse clicked on omGraphic: " + omGraphic);
     				OMBaseStation omBaseStation = (OMBaseStation) omGraphic;
     				sidePanel.showInfo(omBaseStation);
     				return true;
     			} else {
 					System.out.println("Mouse clicked on omGraphic: " + omGraphic);
 				}
     		}
     	} else if (SwingUtilities.isRightMouseButton(e)) {
             OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
             if (allClosest == null || allClosest.isEmpty()) {			  
 	            JPopupMenu popup = new JPopupMenu();			
                 popup.add(eavdamMenu.getShowOnMapMenu());
                 popup.show(mapBean, e.getX(), e.getY());
                 return true;            
             } else {
         		for (OMGraphic omGraphic : allClosest) {
         			if (omGraphic instanceof OMBaseStation) {
         			    currentlySelectedOMBaseStation = (OMBaseStation) omGraphic;
         	            JPopupMenu popup = new JPopupMenu();					
                         editStationMenuItem = new JMenuItem("Edit station information");
                         editStationMenuItem.addActionListener(this);
                         popup.add(editStationMenuItem);
 						JSeparator last = new JSeparator();
 						popup.add(last);
 						if (currentlySelectedOMBaseStation.getDatasetSource() == null ||  // own stations
 								currentlySelectedOMBaseStation.getDatasetSource() instanceof String) {  // simulation	
 							AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 							Antenna antenna = stationData.getAntenna();
 							if (currentlySelectedOMBaseStation.getStationData().getStationType() != AISFixedStationType.RECEIVER) {	
 								if (transmitCoverageLayer.isVisible() && antenna != null) {
 									editTransmitCoverageMenuItem = new JMenuItem("Edit transmit coverage");
 									editTransmitCoverageMenuItem.addActionListener(this);								
 									popup.add(editTransmitCoverageMenuItem);							
 									resetTransmitCoverageToCircleMenuItem = new JMenuItem("Reset transmit coverage to circle");
 									resetTransmitCoverageToCircleMenuItem.addActionListener(this);
 									popup.add(resetTransmitCoverageToCircleMenuItem);
 									resetTransmitCoverageToPolygonMenuItem = new JMenuItem("Reset transmit coverage to polygon");
 									resetTransmitCoverageToPolygonMenuItem.addActionListener(this);
 									popup.add(resetTransmitCoverageToPolygonMenuItem);
 									last = new JSeparator();
 									popup.add(last);
 								}
 								if (interferenceCoverageLayer.isVisible()) {
 									editInterferenceCoverageMenuItem = new JMenuItem("Edit interference coverage");
 									editInterferenceCoverageMenuItem.addActionListener(this);
 									popup.add(editInterferenceCoverageMenuItem);							
 									resetInterferenceCoverageToCircleMenuItem = new JMenuItem("Reset interference coverage to circle");
 									resetInterferenceCoverageToCircleMenuItem.addActionListener(this);
 									popup.add(resetInterferenceCoverageToCircleMenuItem);
 									resetInterferenceCoverageToPolygonMenuItem = new JMenuItem("Reset interference coverage to polygon");
 									resetInterferenceCoverageToPolygonMenuItem.addActionListener(this);
 									popup.add(resetInterferenceCoverageToPolygonMenuItem);
 									last = new JSeparator();
 									popup.add(last);
 								}						
 							}								
 							if (receiveCoverageLayer.isVisible() && antenna != null) {
 								editReceiveCoverageMenuItem = new JMenuItem("Edit receice coverage");
 								editReceiveCoverageMenuItem.addActionListener(this);
 								popup.add(editReceiveCoverageMenuItem);							
 								resetReceiveCoverageToCircleMenuItem = new JMenuItem("Reset receive coverage to circle");
 								resetReceiveCoverageToCircleMenuItem.addActionListener(this);
 								popup.add(resetReceiveCoverageToCircleMenuItem);
 								resetReceiveCoverageToPolygonMenuItem = new JMenuItem("Reset receive coverage to polygon");
 								resetReceiveCoverageToPolygonMenuItem.addActionListener(this);
 								popup.add(resetReceiveCoverageToPolygonMenuItem);								
 								last = new JSeparator();
 								popup.add(last);
 							}						
 						}
 						popup.remove(last);
                         popup.show(mapBean, e.getX(), e.getY());
                         return true;
                     }
                 }
             }
     	}
 		return false;
 	}
 
 	@Override
 	public boolean mouseDragged(MouseEvent e) {
 	    /*
 		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
 		for (OMGraphic omGraphic : allClosest) {
 			if (omGraphic instanceof OMBaseStation) {
 				System.out.println("Mouse dragged on omGraphic: " + omGraphic);
 				OMBaseStation r = (OMBaseStation) omGraphic;
 				Point2D p = ((MapMouseEvent) e).getLatLon();
 				r.setLatLon(p.getY(), p.getX());
 				r.generate(getProjection());
 				this.repaint();
 
 				// if (this.infoDelegator != null) {
 				// this.infoDelegator.setLabel("FOO");
 				// }
 				
 				// Consumed by this
 				return true;
 			}
 		}
 		*/
 		return false;		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {}
 
 	@Override
 	public void mouseExited(MouseEvent e) {}
 
 	@Override
 	public void mouseMoved() {}
 
 	@Override
 	public boolean mouseMoved(MouseEvent e) {
 		OMList<OMGraphic> allClosest = graphics.findAll(e.getX(), e.getY(), 5.0f);
 		for (OMGraphic omGraphic : allClosest) {
 			if (omGraphic instanceof OMBaseStation) {	    
                 OMBaseStation r = (OMBaseStation) omGraphic;
                 AISFixedStationData stationData = r.getStationData();
 				String text = 
 					"Base station '" + stationData.getStationName() + "' at "
 				    + stationData.getLat()
 					+ (stationData.getLon() > 0 ? "N" : "S")
 					+ ", " + stationData.getLat()
 					+ (stationData.getLon() > 0 ? "E" : "W");
 				this.infoDelegator.requestShowToolTip(new InfoDisplayEvent(this, text));					
 				return true;
 		    }
 		}
 	    this.infoDelegator.requestHideToolTip();		
 		return false;
 	}
 
 	@Override
 	public boolean mousePressed(MouseEvent e) {
 		return false;
 	}
 
 	@Override
 	public boolean mouseReleased(MouseEvent e) {
 		return false;
 	}
 	
 	@Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == editStationMenuItem) {
 		
 		    if (currentlySelectedOMBaseStation.getDatasetSource() == null) {
 				new StationInformationMenuItem(eavdamMenu, StationInformationMenuItem.OWN_ACTIVE_STATIONS_LABEL + "/" +
 					StationInformationMenuItem.OPERATIVE_LABEL, currentlySelectedOMBaseStation.getName()).doClick();        
 			} else if (currentlySelectedOMBaseStation.getDatasetSource() instanceof String) {  // simulation
 				String selectedSimulation = (String) currentlySelectedOMBaseStation.getDatasetSource();
 				new StationInformationMenuItem(eavdamMenu, StationInformationMenuItem.SIMULATION_LABEL + ": " +
 					selectedSimulation, currentlySelectedOMBaseStation.getName()).doClick(); 
 			} else if (currentlySelectedOMBaseStation.getDatasetSource() instanceof EAVDAMUser) {  // Other user's dataset
 				String selectedOrganization = ((EAVDAMUser) currentlySelectedOMBaseStation.getDatasetSource()).getOrganizationName();
 				new StationInformationMenuItem(eavdamMenu, StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL + " " +
 					selectedOrganization, currentlySelectedOMBaseStation.getName()).doClick(); 							
             }
 
 		} else if (e.getSource() == editTransmitCoverageMenuItem) {
 
 			DrawingTool dt = getDrawingTool();
 			if (dt != null) {
 				dt.edit(transmitCoverageAreas.get(currentlySelectedOMBaseStation), this);
 				currentlyEditingLayer = transmitCoverageLayer;
 			}
 
 		} else if (e.getSource() == editReceiveCoverageMenuItem) {
 
 			DrawingTool dt = getDrawingTool();
 			if (dt != null) {
 				dt.edit(receiveCoverageAreas.get(currentlySelectedOMBaseStation), this);
 				currentlyEditingLayer = receiveCoverageLayer;
 			}
 
 		} else if (e.getSource() == editInterferenceCoverageMenuItem) {
 
 			DrawingTool dt = getDrawingTool();
 			if (dt != null) {
 				dt.edit(interferenceCoverageAreas.get(currentlySelectedOMBaseStation), this);
 				currentlyEditingLayer = interferenceCoverageLayer;
 			}
 			
 		} else if (e.getSource() == resetTransmitCoverageToCircleMenuItem) {
 		
 			int response = JOptionPane.showConfirmDialog(openMapFrame, "This will reset the transmit coverage to a circle calculated\n" +
 				"based on the station's antenna information?\nAre you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
 					
 				AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 				Antenna antenna = stationData.getAntenna();
 				if (antenna != null) {							
 					ArrayList<double[]> points = new ArrayList<double[]>();
 					double radius = RoundCoverage.getRoundCoverageRadius(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4);
 					double[] radiuses = new double[2];
 					radiuses[0] = radius;
 					radiuses[1] = radius;
 					points.add(radiuses);
 
 					if (data == null) {
 						data = DBHandler.getData();                        
 					}					
 					data = saveCoverage(data, points, transmitCoverageLayer);
                 }
                       
 				DBHandler.saveData(data);    					
 				updateStations();
                 
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing		
 			}
 		
 		} else if (e.getSource() == resetTransmitCoverageToPolygonMenuItem) {
 
 			String input = JOptionPane.showInputDialog(openMapFrame, "This will reset the transmit coverage to a polygon calculated\n" +
 				"based on the station's antenna information.\nPlease, define how many points do you want the polygon to have?\n" +
 				"The value must be between 3 and 1000. Default value is 25.", "25"); 	
 				
 			if (input != null) {
 				int numberOfPoints = -1;
 				try {
 					Integer temp = Integer.valueOf(input);
 					if (temp.intValue() < 3 || temp.intValue() > 1000) {
 						JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 					} else {
 						numberOfPoints = temp.intValue();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 				}
 				if (numberOfPoints != -1) {
 					AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 					Antenna antenna = stationData.getAntenna();
 					if (antenna != null) {
 						ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(), numberOfPoints);
 						if (data == null) {
 							data = DBHandler.getData();                        
 						}					
 						data = saveCoverage(data, points, transmitCoverageLayer);
 					}
 						  
 					DBHandler.saveData(data);    					
 					updateStations();			
 				}
 			
 			} else if (input == null) {
 				// user canceled, do nothing
 			}		
 		
 		} else if (e.getSource() == resetReceiveCoverageToCircleMenuItem) {
 
 			int response = JOptionPane.showConfirmDialog(openMapFrame, "This will reset the receive coverage to a circle calculated\n" +
 				"based on the station's antenna information?\nAre you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
 					
 				Antenna antenna = currentlySelectedOMBaseStation.getStationData().getAntenna();
 				if (antenna != null) {							
 					ArrayList<double[]> points = new ArrayList<double[]>();
 					double radius = RoundCoverage.getRoundCoverageRadius(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4);
 					double[] radiuses = new double[2];
 					radiuses[0] = radius;
 					radiuses[1] = radius;
 					points.add(radiuses);
 
 					if (data == null) {
 						data = DBHandler.getData();                        
 					}					
 					data = saveCoverage(data, points, receiveCoverageLayer);
                 }
                       
 				DBHandler.saveData(data);    					
 				updateStations();
 			
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing		
 			}
 		
 		
 		} else if (e.getSource() == resetReceiveCoverageToPolygonMenuItem) {
 
 			String input = JOptionPane.showInputDialog(openMapFrame, "This will reset the receive coverage to a polygon calculated\n" +
 				"based on the station's antenna information.\nPlease, define how many points do you want the polygon to have?\n" +
 				"The value must be between 3 and 1000. Default value is 25.", "25"); 	
 				
 			if (input != null) {
 				int numberOfPoints = -1;
 				try {
 					Integer temp = Integer.valueOf(input);
 					if (temp.intValue() < 3 || temp.intValue() > 1000) {
 						JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 					} else {
 						numberOfPoints = temp.intValue();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 				}
 				if (numberOfPoints != -1) {
 					AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 					Antenna antenna = stationData.getAntenna();
 					if (antenna != null) {
 						ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundCoverage(antenna.getAntennaHeight()+antenna.getTerrainHeight(), 4, stationData.getLat(), stationData.getLon(), numberOfPoints);
 						if (data == null) {
 							data = DBHandler.getData();                        
 						}					
 						data = saveCoverage(data, points, receiveCoverageLayer);
 					}
 						  
 					DBHandler.saveData(data);    					
 					updateStations();			
 				}
 			
 			} else if (input == null) {
 				// user canceled, do nothing
 			}				
 		
 		} else if (e.getSource() == resetInterferenceCoverageToCircleMenuItem) {
 		
 			int response = JOptionPane.showConfirmDialog(openMapFrame, "This will reset the interference coverage to a circle calculated\n" +
 				"based on the station's information?\nAre you sure you want to do this?", "Confirm action", JOptionPane.YES_NO_OPTION);
             if (response == JOptionPane.YES_OPTION) {
 
 			ArrayList<double[]> points = new ArrayList<double[]>();
 				double radius = 120*1.852;
 				double[] radiuses = new double[2];
 				radiuses[0] = radius;
 				radiuses[1] = radius;
 				points.add(radiuses);
 
 				if (data == null) {
 					data = DBHandler.getData();                        
 				}					
 				data = saveCoverage(data, points, interferenceCoverageLayer);                
                       
 				DBHandler.saveData(data);    					
 				updateStations();                
 				
             } else if (response == JOptionPane.NO_OPTION) {                        
                 // do nothing		
 			}
 		
 		
 		} else if (e.getSource() == resetInterferenceCoverageToPolygonMenuItem) {				
 
 			String input = JOptionPane.showInputDialog(openMapFrame, "This will reset the interference coverage to a polygon calculated\n" +
 				"based on the station's information.\nPlease, define how many points do you want the polygon to have?\n" +
 				"The value must be between 3 and 1000. Default value is 25.", "25"); 	
 				
 			if (input != null) {
 				int numberOfPoints = -1;
 				try {
 					Integer temp = Integer.valueOf(input);
 					if (temp.intValue() < 3 || temp.intValue() > 1000) {
 						JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 					} else {
 						numberOfPoints = temp.intValue();
 					}
 				} catch (NumberFormatException ex) {
 					JOptionPane.showMessageDialog(openMapFrame, "Number of points must be an integer between 3 and 1000!", "Error!", JOptionPane.ERROR_MESSAGE); 
 				}
 				if (numberOfPoints != -1) {
 					AISFixedStationData stationData = currentlySelectedOMBaseStation.getStationData();
 					ArrayList<double[]> points = (ArrayList<double[]>) RoundCoverage.getRoundInterferenceCoverage(stationData.getLat(), stationData.getLon(), numberOfPoints);
 					if (data == null) {
 						data = DBHandler.getData();                        
 					}					
 					data = saveCoverage(data, points, interferenceCoverageLayer);					
 						  
 					DBHandler.saveData(data);    					
 					updateStations();			
 				}
 			
 			} else if (input == null) {
 				// user canceled, do nothing
 			}				
 		
         } else {
             updateStations();
         }
     }
 	
 	private EAVDAMData saveCoverage(EAVDAMData data, ArrayList<double[]> points, OMGraphicHandlerLayer activeLayer) {
 		if (currentlySelectedOMBaseStation.getDatasetSource() == null) {
 			List<ActiveStation> activeStations = data.getActiveStations();
 			if (activeStations != null) {
 				for (int i=0; i< activeStations.size(); i++) {
 					ActiveStation as = activeStations.get(i);
 					if (as.getStations() != null) {
 						for (int j=0; j<as.getStations().size(); j++) {
 							AISFixedStationData stationData = as.getStations().get(j);
 							if (stationData.getStationDBID() == currentlySelectedOMBaseStation.getStationData().getStationDBID()) {
 								if (activeLayer == transmitCoverageLayer) {
 									AISFixedStationCoverage coverage = stationData.getTransmissionCoverage();
 									if (coverage == null) {
 										coverage = new AISFixedStationCoverage();
 									}
 									coverage.setCoveragePoints(points);
 									stationData.setTransmissionCoverage(coverage);
 								} else if (activeLayer == receiveCoverageLayer) {
 									AISFixedStationCoverage coverage = stationData.getReceiveCoverage();
 									if (coverage == null) {
 										coverage = new AISFixedStationCoverage();
 									}
 									coverage.setCoveragePoints(points);
 									stationData.setReceiveCoverage(coverage);
 								} else if (activeLayer == interferenceCoverageLayer) {
 									AISFixedStationCoverage coverage = stationData.getInterferenceCoverage();
 									if (coverage == null) {
 										coverage = new AISFixedStationCoverage();
 									}
 									coverage.setCoveragePoints(points);
 									stationData.setInterferenceCoverage(coverage);										
 								}								
 								as.getStations().set(j, stationData);
 								data.getActiveStations().set(i, as);		
 								break;									
 							}
 						}
 					}
 				}
 			}
 		} else if (currentlySelectedOMBaseStation.getDatasetSource() instanceof String) {  // simulation
 			String selectedSimulation = (String) currentlySelectedOMBaseStation.getDatasetSource();				
 			List<Simulation> simulatedStations = data.getSimulatedStations();
 			for (Simulation s : data.getSimulatedStations()) {
 				if (selectedSimulation.equals(s.getName())) {
 					List<AISFixedStationData> stations = s.getStations();
 					for (int i=0; i<stations.size(); i++) {
 						AISFixedStationData stationData = stations.get(i);
 						if (stationData.getStationDBID() == currentlySelectedOMBaseStation.getStationData().getStationDBID()) {								
 							if (activeLayer == transmitCoverageLayer) {
 								AISFixedStationCoverage coverage = stationData.getTransmissionCoverage();
 								if (coverage == null) {
 									coverage = new AISFixedStationCoverage();
 								}
 								coverage.setCoveragePoints(points);
 								stationData.setTransmissionCoverage(coverage);
 							} else if (activeLayer == receiveCoverageLayer) {
 								AISFixedStationCoverage coverage = stationData.getReceiveCoverage();
 								if (coverage == null) {
 									coverage = new AISFixedStationCoverage();
 								}
 								coverage.setCoveragePoints(points);
 								stationData.setReceiveCoverage(coverage);
 							} else if (activeLayer == interferenceCoverageLayer) {
 								AISFixedStationCoverage coverage = stationData.getInterferenceCoverage();
 								if (coverage == null) {
 									coverage = new AISFixedStationCoverage();
 								}
 								coverage.setCoveragePoints(points);
 								stationData.setInterferenceCoverage(coverage);										
 							}							
 							stations.set(i, stationData);
 							s.setStations(stations);
 							data.setSimulatedStations(simulatedStations);
 							break;																	
 						}
 					}
 				}	
 			}
 		}	
 		
 		return data;	
 	}
 
 	@Override
 	public void findAndInit(Object obj) {
 	    if (obj instanceof MapBean) {
 			this.mapBean = (MapBean) obj;
 		} else if (obj instanceof InformationDelegator) {
 			this.infoDelegator = (InformationDelegator) obj;
 		} else if (obj instanceof OpenMapFrame) {
 			this.openMapFrame = (OpenMapFrame) obj;
 		} else if (obj instanceof OMAISBaseStationTransmitCoverageLayer) {
 			this.transmitCoverageLayer = (OMAISBaseStationTransmitCoverageLayer) obj;
 		} else if (obj instanceof OMAISBaseStationReceiveCoverageLayer) {
 			this.receiveCoverageLayer = (OMAISBaseStationReceiveCoverageLayer) obj;
 		} else if (obj instanceof OMAISBaseStationInterferenceCoverageLayer) {
 			this.interferenceCoverageLayer = (OMAISBaseStationInterferenceCoverageLayer) obj;			
 		} else if (obj instanceof EavdamMenu) {
 		    this.eavdamMenu = (EavdamMenu) obj;
 		} else if (obj instanceof SidePanel) {
 		    this.sidePanel = (SidePanel) obj;
 		} else if (obj instanceof DrawingTool) {
             setDrawingTool((DrawingTool) obj);
         }
 		if (eavdamMenu != null && transmitCoverageLayer != null && receiveCoverageLayer != null && interferenceCoverageLayer != null && sidePanel != null && !stationsInitiallyUpdated) {
 			updateStations();
 			stationsInitiallyUpdated = true;
 		}
 	}
 
 	private byte[] getImage(String filename) {    
         try {
             File file = new File(filename); 
             int size = (int) file.length(); 
             byte[] bytes = new byte[size]; 
             DataInputStream dis = new DataInputStream(new FileInputStream(file)); 
             int read = 0;
             int numRead = 0;
             while (read < bytes.length && (numRead=dis.read(bytes, read, bytes.length-read)) >= 0) {
                 read = read + numRead;
             }
             return bytes;
         } catch (Exception e) {
             System.out.println(e.getMessage());
         }
         return null;
     }
 
     public void updateStations() {
 	
 		if (eavdamMenu == null || eavdamMenu.getShowOnMapMenu() == null) {
 			return;
 		}
 
         data = DBHandler.getData();                        
         if (data != null) {
              Options options = OptionsMenuItem.loadOptions();
              //currentIcons = options.getIconsSize();
              graphics.clear();
 			 transmitCoverageLayer.getGraphicsList().clear();
 			 receiveCoverageLayer.getGraphicsList().clear();
 			 interferenceCoverageLayer.getGraphicsList().clear();			 
 			 if (eavdamMenu.getShowOnMapMenu().getOwnOperativeStationsMenuItem().isSelected() || eavdamMenu.getShowOnMapMenu().getOwnPlannedStationsMenuItem().isSelected()) {
                 List<ActiveStation> activeStations = data.getActiveStations();
                 if (activeStations != null) {
                     for (ActiveStation as : activeStations) {
                         if (as.getStations() != null) {
                             for (AISFixedStationData stationData : as.getStations()) {
                                 if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE &&
                                         eavdamMenu.getShowOnMapMenu().getOwnOperativeStationsMenuItem().isSelected()) {
                                     this.addBaseStation(null, stationData);
 								}
                                 if (stationData.getStatus().getStatusID() == DerbyDBInterface.STATUS_PLANNED &&
                                         eavdamMenu.getShowOnMapMenu().getOwnPlannedStationsMenuItem().isSelected()) {
                                      this.addBaseStation(null, stationData);
                                 }
                             }
                         }
                     }
                 }
 			}
             if (eavdamMenu.getShowOnMapMenu().getSimulationMenuItems() != null) {
 				for (JCheckBoxMenuItem simulationMenuItem : eavdamMenu.getShowOnMapMenu().getSimulationMenuItems()) {
 					if (simulationMenuItem.isSelected()) {
 						String temp = StationInformationMenuItem.SIMULATION_LABEL + ": ";
 						String selectedSimulation = simulationMenuItem.getText().substring(temp.length());
 						if (data.getSimulatedStations() != null) {
 							for (Simulation s : data.getSimulatedStations()) {
 								if (s.getName().equals(selectedSimulation)) {
 									List<AISFixedStationData> stations = s.getStations();
 									for (AISFixedStationData stationData : stations) {
 										this.addBaseStation(s.getName(), stationData);
 									}
 									break;
 								}
 							}
 						}
 					}   
 				}
 			}			
             if (eavdamMenu.getShowOnMapMenu().getOtherUsersStationsMenuItems() != null) {
 				for (JCheckBoxMenuItem otherUsersStationsMenuItem : eavdamMenu.getShowOnMapMenu().getOtherUsersStationsMenuItems()) {
 					if (otherUsersStationsMenuItem.isSelected()) {
 						String temp = StationInformationMenuItem.STATIONS_OF_ORGANIZATION_LABEL + " ";
 						String selectedOtherUser = otherUsersStationsMenuItem.getText().substring(temp.length());
 						if (data.getOtherUsersStations() != null) {
 							for (OtherUserStations ous : data.getOtherUsersStations()) {
 								EAVDAMUser user = ous.getUser();
 								if (user.getOrganizationName().equals(selectedOtherUser)) {
 									List<ActiveStation> activeStations = ous.getStations();
 									for (ActiveStation as : activeStations) {
 										List<AISFixedStationData> stations = as.getStations();
 										for (AISFixedStationData station : stations) {
 											if (station.getStatus().getStatusID() == DerbyDBInterface.STATUS_ACTIVE) {
 												this.addBaseStation(user, station);
 											}
 										}
 									}
 								}
								break;
 							}
 						}
 					}
 				}               
             }                           
             this.repaint();
 		    this.validate();
         }
     }
 
 }
