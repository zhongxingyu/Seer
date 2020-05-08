 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
  Copyright (C) 2001-2007 by:
  Department of Geography, University of Bonn
  http://www.giub.uni-bonn.de/deegree/
  lat/lon GmbH
  http://www.lat-lon.de
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  Contact:
 
  Andreas Poth
  lat/lon GmbH
  Aennchenstr. 19
  53177 Bonn
  Germany
  E-Mail: poth@lat-lon.de
 
  Prof. Dr. Klaus Greve
  Department of Geography
  University of Bonn
  Meckenheimer Allee 166
  53115 Bonn
  Germany
  E-Mail: greve@giub.uni-bonn.de
  ---------------------------------------------------------------------------*/
 
 package org.deegree.igeo.modules;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.Map;
 
 import javax.swing.JPanel;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.igeo.ActiveMapModelChanged;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.config.ModuleType;
 import org.deegree.igeo.config._ComponentPositionType;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.modules.ActionDescription.ACTIONTYPE;
 import org.deegree.igeo.state.mapstate.MapStateChangedEvent;
 import org.deegree.igeo.state.mapstate.MeasureAreaState;
 import org.deegree.igeo.state.mapstate.MeasureDistanceState;
 import org.deegree.igeo.views.swing.measure.AreaMeasureResultLabel;
 import org.deegree.igeo.views.swing.measure.DistanceMeasureResultLabel;
 import org.deegree.igeo.views.swing.measure.MeasurePanel;
 import org.deegree.igeo.views.swing.measure.MeasureResultLabel;
 
 /**
  * The <code>MeasureModule</code> is an module providing the possibility to measure distances or areas in the map by
  * drawing a linestring or polygon.
  * 
  * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
  * @author last edited by: $Author$
  * 
  * @version $Revision$, $Date$
  * 
  */
 public class MeasureModule<T> extends DefaultModule<T> implements ChangeListener {
 
     private static final ILogger LOG = LoggerFactory.getLogger( MeasureModule.class );
 
     public static enum MeasureType {
         DISTANCE, AREA
     };
 
     private MeasurePanel measurePanel;
 
     private DefaultMapModule<?> mapModule;
 
     static {
         ActionDescription ad1 = new ActionDescription( "measureArea", "sets application into 'measure area' state",
                                                        null, "start measuring area", ACTIONTYPE.PushButton, null, null );
         ActionDescription ad2 = new ActionDescription( "measureDistance",
                                                        "sets application into 'measure distance' state", null,
                                                        "start measuring distance", ACTIONTYPE.PushButton, null, null );
         moduleCapabilities = new ModuleCapabilities( ad1, ad2 );
     }
 
     @Override
     public void init( ModuleType moduleType, _ComponentPositionType componentPosition, ApplicationContainer<T> appCont,
                       IModule<T> parent, Map<String, String> initParams ) {
         super.init( moduleType, componentPosition, appCont, parent, initParams );
 
        mapModule = getMapModule();
        if ( mapModule == null ) {
             LOG.logError( "no map module found " );
             return;
         }
 
        mapModule.getMapTool().addChangeListener( this );
    }

    private DefaultMapModule<?> getMapModule() {
        DefaultMapModule<T> activeMapModule = appContainer.getActiveMapModule();
        if ( activeMapModule != null ) {
            return activeMapModule;
        }
        for ( Object module : appContainer.getModules() ) {
            if ( module instanceof DefaultMapModule<?> ) {
                return (DefaultMapModule<?>) module;
            }
        }
        return null;
     }
 
     @Override
     public void actionPerformed( ActionEvent e ) {
         super.actionPerformed( e );
         if ( e instanceof ActiveMapModelChanged ) {
             removeMeasurePanel();
             this.mapModule.getMapTool().resetState();
             updateMapModuleAndChangeListener();
         }
     }
 
     private void updateMapModuleAndChangeListener() {
        DefaultMapModule<?> newMapModule = getMapModule();
         if ( this.mapModule != newMapModule ) {
             this.mapModule.getMapTool().removeChangeListener( this );
             this.mapModule = newMapModule;
             // this.mapModule.getMapTool().resetState();
             this.mapModule.getMapTool().addChangeListener( this );
         }
     }
 
     /**
      * method assigned to action
      */
     public void measureArea() {
         this.mapModule.getMapTool().setMeasureAreaState();
     }
 
     /**
      * method assigned to action
      */
     public void measureDistance() {
         this.mapModule.getMapTool().setMeasureDistanceState();
     }
 
     private void setMeasureResultsLabel( MeasureResultLabel mrl ) {
         JPanel resultContainer = (JPanel) this.getViewForm();
         resultContainer.removeAll();
         resultContainer.setLayout( new BorderLayout() );
         resultContainer.add( mrl, BorderLayout.CENTER );
         measurePanel.setMeasureResultLabel( mrl );
     }
 
     private void removeMeasureResultsLabel() {
         JPanel resultContainer = (JPanel) this.getViewForm();
         resultContainer.removeAll();
     }
 
     /**
      * appends the measure panel to the mapModule
      */
     private void addMeasurePanel() {
         if ( getViewForm() instanceof Container && measurePanel == null ) {
             createMeasurePanel();
         }
     }
 
     /**
      * removes the measure panel from the mapModule
      */
     private void removeMeasurePanel() {
         if ( measurePanel != null ) {
             MouseListener[] ml = measurePanel.getParent().getMouseListeners();
             for ( MouseListener mouseListener : ml ) {
                 measurePanel.getParent().removeMouseListener( mouseListener );
             }
             MouseMotionListener[] mml = measurePanel.getParent().getMouseMotionListeners();
             for ( MouseMotionListener mouseMotionListener : mml ) {
                 measurePanel.getParent().removeMouseMotionListener( mouseMotionListener );
             }
             mapModule.clear();
             measurePanel = null;
         }
     }
 
     /**
      * creates the measure panel, but does not add it to the mapModule
      */
     @SuppressWarnings("unchecked")
     private void createMeasurePanel() {
         this.mapModule.clear();
         MapModel mapModel = appContainer.getMapModel( null );
         Container jco = (Container) mapModule.getViewForm();
 
         this.measurePanel = new MeasurePanel( (IModule<Container>) this, this.mapModule.getMapTool(), jco );
         this.measurePanel.setBounds( 0, 0, mapModel.getTargetDevice().getPixelWidth(),
                                      mapModel.getTargetDevice().getPixelHeight() );
         this.measurePanel.setBackground( new Color( 255, 255, 255, 0 ) );
         this.measurePanel.setVisible( true );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.ChangeListener#valueChanged(org.deegree.igeo.ValueChangedEvent)
      */
     public void valueChanged( ValueChangedEvent event ) {
         removeMeasurePanel();
         if ( event instanceof MapStateChangedEvent ) {
             updateMapModuleAndChangeListener();
 
             MapModel mapModel = appContainer.getMapModel( null );
             MapStateChangedEvent mapStateEvent = (MapStateChangedEvent) event;
             if ( mapStateEvent.getValue() instanceof MeasureDistanceState ) {
                 addMeasurePanel();
                 MeasureResultLabel mrl = new DistanceMeasureResultLabel( mapModel );
                 setMeasureResultsLabel( mrl );
             } else if ( mapStateEvent.getValue() instanceof MeasureAreaState ) {
                 addMeasurePanel();
                 MeasureResultLabel mrl = new AreaMeasureResultLabel( mapModel );
                 setMeasureResultsLabel( mrl );
             } else {
                 removeMeasureResultsLabel();
             }
         }
     }
 
 }
