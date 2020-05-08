 package cytoscape.view;
 
 import cytoscape.Cytoscape;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.CyEdge;
 
 import cytoscape.view.CyMenus;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CyNodeView;
 import cytoscape.view.CyEdgeView;
 
 import cytoscape.giny.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import java.beans.*;
 
 public class NetworkViewManager 
   implements 
     PropertyChangeListener, 
     InternalFrameListener,
     WindowFocusListener,
     ChangeListener {
 
   private java.awt.Container container;
   private Map networkViewMap;
   private Map componentMap;
   private int viewCount = 0;
 
   protected CytoscapeDesktop cytoscapeDesktop;
 
   protected int VIEW_TYPE;
   
   protected SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport( this );
 
   protected static int frame_count = 0;
 
 
   /**
    * Constructor for overiding the default Desktop view type
    */
   public NetworkViewManager ( CytoscapeDesktop desktop, int view_type ) {
     this.cytoscapeDesktop = desktop;
     VIEW_TYPE = view_type;
     initialize();
   }
 
   public NetworkViewManager ( CytoscapeDesktop desktop ) {
     this.cytoscapeDesktop = desktop;
     VIEW_TYPE = cytoscapeDesktop.getViewType();
     initialize();
   }
   
   protected void initialize () {
 
     if ( VIEW_TYPE == CytoscapeDesktop.TABBED_VIEW ) {
       //create a Tabbed Style NetworkView manager
       container = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
       ( ( JTabbedPane )container).addChangeListener( this );
     } else if ( VIEW_TYPE == CytoscapeDesktop.INTERNAL_VIEW ) {
       container = new JDesktopPane();
       //container.addComponentListener( this );
     } else if ( VIEW_TYPE == CytoscapeDesktop.EXTERNAL_VIEW ) {
       container = null;
     }
 
     // add Help hooks
     cytoscapeDesktop.getHelpBroker().enableHelp(container,
 						"network-view-manager", null);
 
     networkViewMap = new HashMap();
     componentMap = new HashMap();
   }
 
   public SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
     return pcs;
   }
 
   public JTabbedPane getTabbedPane () {
     if ( VIEW_TYPE == CytoscapeDesktop.TABBED_VIEW ) {
       return ( JTabbedPane )container;
     }
     return null;
   }
 
   public JDesktopPane getDesktopPane () {
     if ( VIEW_TYPE == CytoscapeDesktop.INTERNAL_VIEW ) {
       return ( JDesktopPane )container;
     }
     return null;
   }
 
   //------------------------------//
   // Fire Events when a Managed Network View gets the Focus
 
 
   
   /**
    * For Tabbed Panes
    */
   public void stateChanged ( ChangeEvent e ) {
     String network_id = ( String )componentMap.get( ( ( JTabbedPane )container).getSelectedComponent() );
     
     if ( network_id == null ) {
       return;
     }
     
    
 
     firePropertyChange( CytoscapeDesktop.NETWORK_VIEW_FOCUSED,
                         null,
                         network_id );
   }
 
   
   /**
    * For Internal Frames
    */
   public void 	internalFrameActivated(InternalFrameEvent e) {
     String network_id = ( String )componentMap.get( e.getInternalFrame() );
     
     if ( network_id == null ) {
       return;
     }
     
    
 
     firePropertyChange( CytoscapeDesktop.NETWORK_VIEW_FOCUSED,
                         null,
                         network_id );
   }
   
   public void 	internalFrameClosed(InternalFrameEvent e) {}
   public void 	internalFrameClosing(InternalFrameEvent e) {}
   public void 	internalFrameDeactivated(InternalFrameEvent e) {}
   public void 	internalFrameDeiconified(InternalFrameEvent e) {}
   public void 	internalFrameIconified(InternalFrameEvent e) {}
   public void 	internalFrameOpened(InternalFrameEvent e) {
     internalFrameActivated( e );
   }
 
   /**
    * For Exteernal Frames
    */
   public  void 	windowGainedFocus(WindowEvent e) {
 
    
 
     String network_id = ( String )componentMap.get( e.getWindow() );
    
     // System.out.println( " Window Gained Focus: "+ network_id );
  
     if ( network_id == null ) {
       return;
     }
     
    
 
     firePropertyChange( CytoscapeDesktop.NETWORK_VIEW_FOCUSED,
                         null,
                         network_id );
     
   }
   public void 	windowLostFocus(WindowEvent e) {}
   
 
 
 
 
 
   /**
    * This handles all of the incoming PropertyChangeEvents.  If you are going to have
    * multiple NetworkViewManagers, then this method should be extended such that the 
    * desired behaviour is achieved, assuming of course that you want your 
    * NetworkViewManagers to behave differently.
    */
   public void propertyChange ( PropertyChangeEvent e ) {
     // handle events
    
     // handle focus event
     if ( e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_FOCUS ) {
       String network_id = ( String )e.getNewValue();
       e = null;
       setFocus( network_id );
     } 
 
     // handle putting a newly created CyNetworkView into a Container
     else if ( e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_CREATED ) {
       CyNetworkView new_view = ( CyNetworkView )e.getNewValue();
       createContainer( new_view );
       e = null;
     }
 
     // handle a NetworkView destroyed
     else if ( e.getPropertyName() == CytoscapeDesktop.NETWORK_VIEW_DESTROYED ) {
       CyNetworkView view = ( CyNetworkView )e.getNewValue();
       removeView( view );
       e = null;
     }
 
   }
 
   /**
    * Fires a PropertyChangeEvent 
    */
   public void firePropertyChange ( String property_type, 
                                    Object old_value, 
                                    Object new_value ) {
     
    
 
     pcs.firePropertyChange( new PropertyChangeEvent( this,
                                                      property_type,
                                                      old_value,
                                                      new_value ) );
   }
 
   /**
    * Sets the focus of the passed network, if possible
    * The Network ID corresponds to the CyNetworkView.getNetwork().getIdentifier()
    */
   protected void setFocus ( String network_id ) {
 
     if ( networkViewMap.containsKey( network_id ) ) {
       // there is a NetworkView for this network
       if ( VIEW_TYPE == CytoscapeDesktop.TABBED_VIEW ) {
         try {
           ( ( JTabbedPane )container ).
             setSelectedComponent( (Component)networkViewMap.get( network_id ) );
         } catch ( Exception e ) {
          //  e.printStackTrace();
 //           System.err.println( "Network View unable to be focused" );
         }
       } else if ( VIEW_TYPE == CytoscapeDesktop.INTERNAL_VIEW ) {
         try {
           ( ( JInternalFrame )networkViewMap.get( network_id ) ).setIcon( false );
           ( ( JInternalFrame )networkViewMap.get( network_id ) ).show();
           ( ( JInternalFrame )networkViewMap.get( network_id ) ).setSelected( true );
 
         } catch ( Exception e ) {
           System.err.println( "Network View unable to be focused" );
         }
       }  else if ( VIEW_TYPE == CytoscapeDesktop.EXTERNAL_VIEW ) {
         try {
           ( ( JFrame )networkViewMap.get( network_id ) ).requestFocus();
           //( ( JFrame )networkViewMap.get( network_id ) ).setVisible( true );
         } catch ( Exception e ) {
           System.err.println( "Network View unable to be focused" );
         }
       }
     }
   }
 
   protected void removeView ( CyNetworkView view ) {
     
     if ( VIEW_TYPE == CytoscapeDesktop.TABBED_VIEW ) {
       try {
         ( ( JTabbedPane )container ).
           remove( (Component)networkViewMap.get( view.getNetwork().getIdentifier() ) );
       } catch ( Exception e ) {
         // possible error
       }
     } 
 
     else if ( VIEW_TYPE == CytoscapeDesktop.INTERNAL_VIEW ) {
       try {
         ( ( JInternalFrame )networkViewMap.get(  view.getNetwork().getIdentifier() ) ).dispose();
       } catch ( Exception e ) {
         System.err.println( "Network View unable to be killed" );
       }
     }
     
     else if ( VIEW_TYPE == CytoscapeDesktop.EXTERNAL_VIEW ) {
       try {
         ( ( JFrame )networkViewMap.get( view.getNetwork().getIdentifier()  ) ).dispose();
       } catch ( Exception e ) {
         System.err.println( "Network View unable to be killed" );
       }
     }
    
     networkViewMap.remove( view.getNetwork().getIdentifier() );
 
   }
 
   /**
    * Contains a CyNetworkView according to the view type of this NetworkViewManager
    */
   protected void createContainer ( final CyNetworkView view ) {
 
    
     if ( networkViewMap.containsKey( view.getNetwork().getIdentifier() ) ) {
       // already contains
       return;
     }
          
     if ( VIEW_TYPE == CytoscapeDesktop.TABBED_VIEW ) {
       // put the CyNetworkViews Component into the Tabbed Pane
       ( ( JTabbedPane )container ).addTab( view.getNetwork().getTitle(), view.getComponent() );
       
       networkViewMap.put( view.getNetwork().getIdentifier(), view.getComponent() );
       componentMap.put( view.getComponent(), view.getNetwork().getIdentifier() );
     }
 
     else if ( VIEW_TYPE == CytoscapeDesktop.INTERNAL_VIEW ) {
       // create a new InternalFrame and put the CyNetworkViews Component into it
       JInternalFrame iframe = new JInternalFrame( view.getTitle(), 
                                                   true, true, true, true );
       iframe.addInternalFrameListener(new InternalFrameAdapter() {
           public void internalFrameClosing(InternalFrameEvent e) {
            Cytoscape.destroyNetwork(view.getNetwork()); } } );
       ( ( JDesktopPane )container ).add( iframe );
       iframe.getContentPane().add( view.getComponent() );
       iframe.pack();
       iframe.setSize( 400, 400 );
       iframe.setVisible( true );
       iframe.addInternalFrameListener( this );
       
       networkViewMap.put( view.getNetwork().getIdentifier(), iframe );
       componentMap.put( iframe, view.getNetwork().getIdentifier() );
     } 
 
     else if ( VIEW_TYPE == CytoscapeDesktop.EXTERNAL_VIEW ) {
       // create a new JFrame and put the CyNetworkViews Component into it
 
       JFrame frame = new JFrame( view.getNetwork().getTitle() );
       frame.getContentPane().add( view.getComponent() );
       frame.pack();
       frame.setSize( 400, 400 );
       frame.setVisible( true );
       componentMap.put( frame, view.getNetwork().getIdentifier() );
       networkViewMap.put( view.getNetwork().getIdentifier(), frame );
       frame.addWindowFocusListener( this );
       frame.setJMenuBar( cytoscapeDesktop.getCyMenus().getMenuBar());
     }
 
     firePropertyChange( CytoscapeDesktop.NETWORK_VIEW_FOCUSED,
                         null,
                         view.getNetwork().getIdentifier() );
 
   }
 
 
 }
