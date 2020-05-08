 package org.geworkbench.util.microarrayutils;
 
 /*
  * The geworkbench project
  * 
  * Copyright (c) 2006 Columbia University
  *
  */
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
 import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
 import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
 import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
 import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
 import org.geworkbench.engine.config.VisualPlugin;
 import org.geworkbench.engine.management.Publish;
 import org.geworkbench.engine.management.Subscribe;
 import org.geworkbench.events.GeneSelectorEvent;
 import org.geworkbench.events.MicroarraySetViewEvent;
 import org.geworkbench.events.SubpanelChangedEvent;
 
 /**
  * @author unattributable
 * @version $Id: MicroarrayViewEventBase.java,v 1.11 2006-02-13 21:58:16 keshav Exp $
  */
 public abstract class MicroarrayViewEventBase implements VisualPlugin {
 
     private Log log = LogFactory.getLog( this.getClass() );
 
     /**
      * The reference microarray set.
      */
     protected DSMicroarraySet<DSMicroarray> refMASet = null;
 
     protected DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView = null;
 
     protected boolean activateMarkers = false;
 
     protected boolean activateArrays = false;
 
     protected JCheckBox chkActivateMarkers = new JCheckBox( "Activated Markers" );
 
     protected JCheckBox chkShowArrays = new JCheckBox( "Activated Arrays" );
 
     protected JButton plotButton = new JButton( "Plot" );
 
     private final String markerLabelPrefix = "  Markers: ";
 
     protected JLabel numMarkersSelectedLabel = new JLabel( markerLabelPrefix );
 
     protected JPanel mainPanel;
 
     protected JToolBar jToolBar3;
 
     protected DSPanel<? extends DSGeneMarker> activatedMarkers = null;
 
     protected DSPanel activatedArrays = null;
 
     /**
      * 
      * 
      */
     public MicroarrayViewEventBase() {
         try {
             jbInit();
         } catch ( Exception e ) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * getComponent
      * 
      * @return Component
      */
     public Component getComponent() {
         return mainPanel;
     }
 
     /**
      * @param event
      * @return SubpanelChangedEvent
      */
     @Publish
     public SubpanelChangedEvent publishSubpanelChangedEvent( org.geworkbench.events.SubpanelChangedEvent event ) {
         return event;
     }
 
     /**
      * receiveProjectSelection
      * 
      * @param e ProjectEvent
      */
     @Subscribe
     @SuppressWarnings("unchecked")
     public void receive( org.geworkbench.events.ProjectEvent e, Object source ) {
 
         log.debug( "Source object " + source );
 
         if ( e.getMessage().equals( org.geworkbench.events.ProjectEvent.CLEARED ) ) {
             refMASet = null;
             fireModelChangedEvent( null );
         } else {
             DSDataSet dataSet = e.getDataSet();
             if ( dataSet instanceof DSMicroarraySet ) {
                 if ( refMASet != dataSet ) {
                     this.refMASet = ( DSMicroarraySet ) dataSet;
                     // panels are now invalid
                     activatedArrays = null;
                     activatedMarkers = null;
                 }
             }
             refreshMaSetView();
         }
     }
 
     /**
      * geneSelectorAction
      * 
      * @param e GeneSelectorEvent
      */
     @Subscribe
     public void receive( GeneSelectorEvent e, Object source ) {
 
         log.debug( "Source object " + source );
 
         if ( e.getPanel() != null && e.getPanel().size() > 0 ) {
 
             enablePlotButton( true );
             numMarkersSelectedLabel.setText( markerLabelPrefix + e.getPanel().size() );
 
             // TODO keshav - in efforts to keep this consistent with the rest of
             // the application at this time, I have
             // not enabled this.
             // The idea, however, is here ... we do not want to have checkboxes
             // enabled if the user has not selected
             // anything from
             // the panels. On a larger scale, we may want to rethink these
             // checkboxes altogether ... too many button
             // clicks for
             // an end user!
             // if ( !chkActivateMarkers.isEnabled() )
             // chkActivateMarkers.setEnabled( true );
 
             activatedMarkers = e.getPanel().activeSubset();
         }
 
         else {
             enablePlotButton( false );
         }
 
         // Part of the TODO above. Make sure you "uncomment this if you
         // uncomment that".
         // else {
         // chkActivateMarkers.setEnabled( false );
         // }
         // refreshMaSetView();
     }
 
     /**
      * Enables or disables the plot button.
      * 
      * @param b
      */
     private void enablePlotButton( boolean b ) {
         numMarkersSelectedLabel.setEnabled( b );
         plotButton.setEnabled( b );
 
     }
 
     /**
      * phenotypeSelectorAction
      * 
      * @param e PhenotypeSelectorEvent
      */
     @Subscribe
     @SuppressWarnings("unchecked")
     public void receive( org.geworkbench.events.PhenotypeSelectorEvent e, Object source ) {
 
         log.debug( "Source object " + source );
 
         if ( e.getTaggedItemSetTree() != null )
 
         activatedArrays = e.getTaggedItemSetTree().activeSubset();
 
     }
 
     /**
      * Refreshes the chart view.
      */
     @SuppressWarnings("unchecked")
     protected void refreshMaSetView() {
         maSetView = getDataSetView();
         fireModelChangedEvent( null );
     }
 
     /**
      * @param event TODO Why is this here ... does nothing ... more importantly, why is it ever called?
      */
     protected void fireModelChangedEvent( MicroarraySetViewEvent event ) {
 
     }
 
     /**
      * @throws Exception
      */
     protected void jbInit() throws Exception {
         mainPanel = new JPanel();
 
         jToolBar3 = new JToolBar();
         chkActivateMarkers.setToolTipText( "" );
 
         BorderLayout borderLayout2 = new BorderLayout();
         mainPanel.setLayout( borderLayout2 );
 
         chkActivateMarkers.addActionListener( new MicroarrayViewPanelBase_chkActivateMarkers_actionAdapter( this ) );
         chkShowArrays.addActionListener( new MicroarrayViewPanelBase_chkShowArrays_actionAdapter( this ) );
 
         // TODO see the "to do" listed in the receive method above ... if that
         // is uncommented, make sure to uncomment
         // this
         // as well.
         // chkActivateMarkers.setEnabled( false );
         // chkShowArrays.setEnabled( false );
 
         plotButton.addActionListener( new MicroarrayViewPanelBase_plotButton_actionAdapter( this ) );
         enablePlotButton( false );
 
         jToolBar3.add( chkShowArrays, null );
         jToolBar3.add( chkActivateMarkers, null );
         jToolBar3.add( plotButton );
         jToolBar3.add( numMarkersSelectedLabel );
         mainPanel.add( jToolBar3, java.awt.BorderLayout.SOUTH );
 
         activateMarkers = chkActivateMarkers.isSelected();
         activateArrays = chkShowArrays.isSelected();
     }
 
     /**
      * @param e
      */
     void chkShowArrays_actionPerformed( ActionEvent e ) {
         activateArrays = ( ( JCheckBox ) e.getSource() ).isSelected();
     }
 
     /**
      * @param e
      */
     void chkActivateMarkers_actionPerformed( ActionEvent e ) {
         activateMarkers = ( ( JCheckBox ) e.getSource() ).isSelected();
     }
 
     /**
      * @param e
      */
     void plotButton_actionPerformed( ActionEvent e ) {
         refreshMaSetView();
     }
 
     /**
      * @return
      */
     @SuppressWarnings("unchecked")
     public DSMicroarraySetView getDataSetView() {
         DSMicroarraySetView dataSetView = new CSMicroarraySetView( this.refMASet );
         if ( activatedMarkers != null && activatedMarkers.panels().size() > 0 )
             dataSetView.setMarkerPanel( activatedMarkers );
         if ( activatedArrays != null && activatedArrays.panels().size() > 0 )
             dataSetView.setItemPanel( activatedArrays );
         dataSetView.useMarkerPanel( activateMarkers );
         dataSetView.useItemPanel( activateArrays );
         // dataSetView.setMicroarraySet(this.refMASet);
         return dataSetView;
     }
 
 }
 
 /**
  * @author unattributable
  */
 class MicroarrayViewPanelBase_chkShowArrays_actionAdapter implements java.awt.event.ActionListener {
 
     private Log log = LogFactory.getLog( this.getClass() );
 
     MicroarrayViewEventBase adaptee;
 
     MicroarrayViewPanelBase_chkShowArrays_actionAdapter( MicroarrayViewEventBase adaptee ) {
         this.adaptee = adaptee;
     }
 
     /**
      * 
      */
     public void actionPerformed( ActionEvent e ) {
         log.debug( "ActionEvent " + e );
         adaptee.chkShowArrays_actionPerformed( e );
         adaptee.getComponent().repaint();
     }
 }
 
 /**
  * @author unattributable
  */
 class MicroarrayViewPanelBase_chkActivateMarkers_actionAdapter implements java.awt.event.ActionListener {
 
     private Log log = LogFactory.getLog( this.getClass() );
 
     MicroarrayViewEventBase adaptee;
 
     MicroarrayViewPanelBase_chkActivateMarkers_actionAdapter( MicroarrayViewEventBase adaptee ) {
         this.adaptee = adaptee;
     }
 
     public void actionPerformed( ActionEvent e ) {
         log.debug( "actionPerformed " + e );
 
         adaptee.chkActivateMarkers_actionPerformed( e );
         adaptee.getComponent().repaint();
     }
 }
 
 /**
  * @author keshav
  */
 class MicroarrayViewPanelBase_plotButton_actionAdapter implements java.awt.event.ActionListener {
 
     private Log log = LogFactory.getLog( this.getClass() );
 
     MicroarrayViewEventBase adaptee;
 
     MicroarrayViewPanelBase_plotButton_actionAdapter( MicroarrayViewEventBase adaptee ) {
         this.adaptee = adaptee;
     }
 
     public void actionPerformed( ActionEvent e ) {
         log.debug( "actionPerformed " + e );
 
         adaptee.plotButton_actionPerformed( e );
         adaptee.getComponent().repaint();
     }
 }
