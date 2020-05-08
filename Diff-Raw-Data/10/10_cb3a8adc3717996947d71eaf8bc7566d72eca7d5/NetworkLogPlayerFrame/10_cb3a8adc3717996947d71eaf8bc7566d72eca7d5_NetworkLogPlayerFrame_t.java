 package org.blockout.utils;
 
 import java.awt.BasicStroke;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Paint;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JSlider;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import org.apache.commons.collections15.Predicate;
 import org.apache.commons.collections15.Transformer;
 import org.blockout.common.NetworkLogMessage;
 import org.blockout.utils.NetworkLogPlayer.IMessageProcessor;
 
 import com.google.common.base.Preconditions;
 
 import edu.uci.ics.jung.algorithms.layout.CircleLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SortedSparseMultigraph;
 import edu.uci.ics.jung.graph.util.Context;
 import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 
 /**
  * The NetworkLogPlayerFrame renders a network graph loaded from multiple log
  * files. While the graph is rendered you can see how is is build up step by
  * step.
  * 
  * @author Florian Müller
  * 
  */
 public class NetworkLogPlayerFrame extends JFrame implements IMessageProcessor {
     private static final long serialVersionUID = 6182193527471497535L;
 
     public static void main( final String[] args ) {
         NetworkLogPlayerFrame player = new NetworkLogPlayerFrame( args );
         player.setVisible( true );
     }
 
     public static final int MAX_DELAY_MS = 5000;
 
     private enum PlayerState {
         PLAYING, PAUSED, STOPPED, NOT_LOADED, FINISHED;
     }
 
     private enum EdgesToDisplayOptions {
         ALL, CHORD, NETWORK
     }
 
     private Graph<NetworkVertex, NetworkEdge>               graph;
     private VisualizationViewer<NetworkVertex, NetworkEdge> visViewer;
     private Layout<NetworkVertex, NetworkEdge>              layout;
 
     private JSlider                                         delaySlider;
     private JLabel                                          currentDelayLabel;
     private JButton                                         playButton;
     private JButton                                         pauseButton;
     private JButton                                         stopButton;
     private JProgressBar                                    progressBar;
 
     private PlayerState                                     state;
     private NetworkLogPlayer                                player;
     private EdgesToDisplayOptions                           edgesToDisplay;
     private final List<NetworkLogMessage>                   pendingConnect;
 
     /**
      * Constructor to create a new NetworkLogPlayerFrame
      * 
      * @param args
      *            Optional commandline args
      */
     public NetworkLogPlayerFrame( final String[] args ) {
         edgesToDisplay = EdgesToDisplayOptions.ALL;
         graph = new SortedSparseMultigraph<NetworkVertex, NetworkEdge>();
         pendingConnect = new LinkedList<NetworkLogMessage>();
 
         initLayout();
         changePlayerState( PlayerState.NOT_LOADED );
     }
 
     /**
      * Initialize the SWING layout
      */
     private void initLayout() {
         Preconditions.checkNotNull( graph, "No graph to display" );
 
         setSize( 400, 400 );
         setLocation( 100, 100 );
         setTitle( "NetworkLogPlayer" );
         setDefaultCloseOperation( EXIT_ON_CLOSE );
 
         initMenuBar();
         initToolBar();
 
         layout = new CircleLayout<NetworkVertex, NetworkEdge>( graph );
         layout.setSize( new Dimension( 800, 800 ) );
 
         visViewer = new VisualizationViewer<NetworkVertex, NetworkEdge>( layout );
         visViewer.setPreferredSize( new Dimension( 400, 400 ) );
         visViewer.getRenderContext().setVertexLabelTransformer(
                 new ToStringLabeller<NetworkVertex>() );
 
         Transformer<NetworkEdge, Paint> edgeDrawPaintTransformer = new Transformer<NetworkEdge, Paint>() {
             @Override
             public Paint transform( final NetworkEdge edge ) {
                 if( "net".equals( edge.getType() ) ) {
                     return Color.GRAY;
                 } else if( "chord".equals( edge.getType() ) ) {
                     if( "successor".equals( edge.getLabel() ) ) {
                         return Color.GREEN;
                     } else {
                         return Color.RED;
                     }
                 }
 
                 return Color.BLACK;
             }
         };
 
         visViewer
                 .getRenderContext()
                 .setEdgeIncludePredicate(
                         new Predicate<Context<Graph<NetworkVertex, NetworkEdge>, NetworkEdge>>() {
 
                             @Override
                             public boolean evaluate(
                                     final Context<Graph<NetworkVertex, NetworkEdge>, NetworkEdge> arg ) {
 
                                 NetworkEdge edge = arg.element;
                                 switch ( edgesToDisplay ) {
                                 case NETWORK:
                                     return "net".equals( edge.getType() );
                                 case CHORD:
                                     return "chord".equals( edge.getType() );
                                 default:
                                     return true;
                                 }
                             }
                         } );
 
         visViewer.getRenderContext().setEdgeDrawPaintTransformer(
                 edgeDrawPaintTransformer );
         visViewer.getRenderContext().setArrowDrawPaintTransformer(
                 edgeDrawPaintTransformer );
         visViewer.getRenderContext().setArrowFillPaintTransformer(
                 edgeDrawPaintTransformer );
 
         float dash[] = { 10.0f };
         final Stroke edgeStroke = new BasicStroke( 1.0f, BasicStroke.CAP_BUTT,
                 BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f );
         Transformer<NetworkEdge, Stroke> edgeStrokeTransformer = new Transformer<NetworkEdge, Stroke>() {
             @Override
             public Stroke transform( final NetworkEdge edge ) {
 
                 if( "net".equals( edge.getType() ) ) {
                     return edgeStroke;
                 }
 
                 return new BasicStroke( 2.0f );
             }
         };
         visViewer.getRenderContext().setEdgeStrokeTransformer(
                 edgeStrokeTransformer );
 
         visViewer.getRenderContext().setEdgeLabelTransformer(
                 new ToStringLabeller<NetworkEdge>() );
 
         // Add mouse controls for zooming and panning
         DefaultModalGraphMouse<NetworkVertex, NetworkEdge> gm = new DefaultModalGraphMouse<NetworkVertex, NetworkEdge>();
         gm.setMode( ModalGraphMouse.Mode.TRANSFORMING );
         visViewer.setGraphMouse( gm );
 
        GraphZoomScrollPane gzsp = new GraphZoomScrollPane( visViewer );
        gzsp.add( visViewer );

        getContentPane().add( gzsp, BorderLayout.CENTER );
 
         JPanel progressPanel = new JPanel();
         BoxLayout box = new BoxLayout( progressPanel, BoxLayout.LINE_AXIS );
         progressPanel.setLayout( box );
 
         JLabel progressLabel = new JLabel( "Progress:" );
         progressPanel.add( progressLabel );
 
         progressBar = new JProgressBar( 0, 100 );
         progressPanel.add( progressBar );
         getContentPane().add( progressPanel, BorderLayout.SOUTH );
 
         pack();
     }
 
     /**
      * Initialize the menu bar
      */
     private void initMenuBar() {
         JMenuBar menuBar = new JMenuBar();
 
         JMenu fileMenu = new JMenu( "File" );
         fileMenu.setMnemonic( KeyEvent.VK_F );
         menuBar.add( fileMenu );
 
         JMenuItem open = new JMenuItem( new AbstractAction( "Open" ) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 onMenuOpen();
             }
         } );
         fileMenu.add( open );
 
         JMenuItem close = new JMenuItem( new AbstractAction( "Close" ) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 System.exit( 0 );
             }
         } );
         fileMenu.add( close );
 
         JMenu viewMenu = new JMenu( "View" );
         viewMenu.setMnemonic( KeyEvent.VK_V );
         menuBar.add( viewMenu );
 
         JMenu edgesMenu = new JMenu( "Edges" );
         edgesMenu.setMnemonic( KeyEvent.VK_E );
         viewMenu.add( edgesMenu );
 
         JMenuItem edgesAll = new JMenuItem( new AbstractAction(
                 "Draw all edges" ) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 edgesToDisplay = EdgesToDisplayOptions.ALL;
                 updateGraph();
             }
         } );
         edgesMenu.add( edgesAll );
 
         JMenuItem edgesChord = new JMenuItem( new AbstractAction(
                 "Draw chord edges" ) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 edgesToDisplay = EdgesToDisplayOptions.CHORD;
                 updateGraph();
             }
         } );
         edgesMenu.add( edgesChord );
 
         JMenuItem edgesNet = new JMenuItem( new AbstractAction(
                 "Draw network edges" ) {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 edgesToDisplay = EdgesToDisplayOptions.NETWORK;
                 updateGraph();
             }
         } );
         edgesMenu.add( edgesNet );
 
         setJMenuBar( menuBar );
     }
 
     /**
      * Initialize the toolbar to control the player
      */
     private void initToolBar() {
         JToolBar toolbar = new JToolBar();
         toolbar.setRollover( true );
         toolbar.setFloatable( false );
 
         toolbar.add( new JLabel( "Delay:" ) );
 
         delaySlider = new JSlider( 0, MAX_DELAY_MS, 1000 );
         delaySlider.addChangeListener( new ChangeListener() {
             @Override
             public void stateChanged( final ChangeEvent e ) {
                 JSlider slider = (JSlider) e.getSource();
                 currentDelayLabel.setText( String.format( "%04d",
                         slider.getValue() )
                         + " ms" );
 
                 if( null != player ) {
                     player.setDelay( slider.getValue() );
                 }
             }
         } );
 
         toolbar.add( delaySlider );
 
         currentDelayLabel = new JLabel( String.format( "%04d",
                 delaySlider.getValue() )
                 + " ms" );
         toolbar.add( currentDelayLabel );
         toolbar.addSeparator();
 
         playButton = new JButton( new ImageIcon( "icons/play_18x24.png" ) );
         playButton.addActionListener( new ActionListener() {
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 onPlay();
             }
         } );
 
         toolbar.add( playButton );
         toolbar.addSeparator();
 
         pauseButton = new JButton( new ImageIcon( "icons/pause_18x24.png" ) );
         pauseButton.setEnabled( false );
         pauseButton.addActionListener( new ActionListener() {
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 onPause();
             }
         } );
         toolbar.add( pauseButton );
         toolbar.addSeparator();
 
         stopButton = new JButton( new ImageIcon( "icons/stop_24x24.png" ) );
         stopButton.setEnabled( false );
         stopButton.addActionListener( new ActionListener() {
 
             @Override
             public void actionPerformed( final ActionEvent e ) {
                 onStop();
             }
         } );
 
         toolbar.add( stopButton );
 
         getContentPane().add( toolbar, BorderLayout.NORTH );
     }
 
     /**
      * Change the internal state of the player and change the UI according to
      * this state
      * 
      * @param newState
      *            The new state of the player
      */
     private void changePlayerState( final PlayerState newState ) {
         if( state == newState ) {
             return;
         }
 
         state = newState;
 
         switch ( state ) {
         case PLAYING:
             playButton.setEnabled( false );
             pauseButton.setEnabled( true );
             stopButton.setEnabled( true );
             break;
         case PAUSED:
             playButton.setEnabled( true );
             pauseButton.setEnabled( false );
             stopButton.setEnabled( true );
             break;
         case STOPPED:
             playButton.setEnabled( true );
             pauseButton.setEnabled( false );
             stopButton.setEnabled( false );
             break;
         case FINISHED:
             playButton.setEnabled( false );
             pauseButton.setEnabled( false );
             stopButton.setEnabled( true );
             break;
         default:
             playButton.setEnabled( false );
             pauseButton.setEnabled( false );
             stopButton.setEnabled( false );
             break;
         }
     }
 
     /**
      * Callback if the user wants to open some logs
      */
     protected void onMenuOpen() {
         JFileChooser fileChooser = new JFileChooser( new File( "." ) );
         fileChooser.addChoosableFileFilter( new FileFilter() {
 
             @Override
             public String getDescription() {
                 return "Logfiles (*.log)";
             }
 
             @Override
             public boolean accept( final File f ) {
                 return f.getName().endsWith( ".log" ) || f.isDirectory();
             }
         } );
         fileChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
         fileChooser.setMultiSelectionEnabled( true );
         int status = fileChooser.showOpenDialog( this );
 
         if( JFileChooser.APPROVE_OPTION == status ) {
             File[] files = fileChooser.getSelectedFiles();
             List<NetworkLogMessage> messages = new LinkedList<NetworkLogMessage>();
 
             NetworkLogReader reader;
             for ( File file : files ) {
                 try {
                     reader = new NetworkLogReader( new FileReader( file ) );
                 } catch ( FileNotFoundException e1 ) {
                     e1.printStackTrace();
                     continue;
                 }
 
                 NetworkLogMessage msg;
 
                 try {
                     while ( null != ( msg = reader.readMessage() ) ) {
                         messages.add( msg );
                     }
                 } catch ( IOException e1 ) {
                     e1.printStackTrace();
                     continue;
                 }
             }
 
             Collections.sort( messages, new Comparator<NetworkLogMessage>() {
                 @Override
                 public int compare( final NetworkLogMessage o1,
                         final NetworkLogMessage o2 ) {
                     return new Long( o1.getTimestamp() ).compareTo( o2
                             .getTimestamp() );
                 };
             } );
 
             player = new NetworkLogPlayer( messages, NetworkLogPlayerFrame.this );
             player.setDelay( delaySlider.getValue() );
             changePlayerState( PlayerState.STOPPED );
         }
     }
 
     /**
      * Callback if the user press play
      */
     private void onPlay() {
         changePlayerState( PlayerState.PLAYING );
         if( null != player ) {
             player.play();
         }
     }
 
     /**
      * Callback if the user press pause
      */
     private void onPause() {
         changePlayerState( PlayerState.PAUSED );
         player.stop();
     }
 
     /**
      * Callback if the user press stop
      */
     private void onStop() {
         player.stop();
         reset();
         updateGraph();
 
         changePlayerState( PlayerState.STOPPED );
     }
 
     /**
      * Reset the player to its initial state
      */
     public void reset() {
         graph = new SortedSparseMultigraph<NetworkVertex, NetworkEdge>();
         pendingConnect.clear();
         player.reset();
         progressBar.setValue( 0 );
     }
 
     public NetworkVertex findVertexById( final String id ) {
         for ( NetworkVertex vertex : graph.getVertices() ) {
             if( vertex.getId().equals( id ) ) {
                 return vertex;
             }
         }
 
         return null;
     }
 
     @Override
     public void process( final NetworkLogMessage message ) {
         System.out.println( message );
 
         int progress = (int) ( ( player.getCurrentLogMessageIndex() / (float) player
                 .getNumLogMessages() ) * 100 );
         progressBar.setValue( progress );
 
         if( message.hasExtra( "chord" ) ) {
             String type = (String) message.getExtra( "chord" );
             if( "predecessor".equals( type ) ) {
                 updatePredecessor( message );
             } else if( "successor".equals( type ) ) {
                 updateSuccessor( message );
             }
         }
 
         if( message.hasExtra( "net" ) ) {
             String type = (String) message.getExtra( "net" );
 
             if( "connect".equals( type ) ) {
                 updateConnected( message );
             } else if( "disconnect".equals( type ) ) {
                 updateDisconnected( message );
             }
         }
 
         updateGraph();
     }
 
     @Override
     public void finished() {
         changePlayerState( PlayerState.FINISHED );
         JOptionPane.showMessageDialog( this, "Log replay finished", "Done",
                 JOptionPane.INFORMATION_MESSAGE );
 
     }
 
     /**
      * Find a pending connect with this local/remote address
      * 
      * A Pending connect is a connection we have seen in the log but can't
      * confirm the other side.
      * 
      * @param localAddr
      * @param remoteAddr
      * @return
      */
     private NetworkLogMessage findPendingConnect( final String localAddr,
             final String remoteAddr ) {
         NetworkLogMessage result = null;
         for ( NetworkLogMessage msg : pendingConnect ) {
             String localAddrOther = (String) msg.getExtra( "localaddr" );
             String remoteAddrOther = (String) msg.getExtra( "remoteaddr" );
 
             if( localAddr.equals( remoteAddrOther )
                     && remoteAddr.equals( localAddrOther ) ) {
                 result = msg;
                 break;
             }
         }
 
         if( null != result ) {
             pendingConnect.remove( result );
         }
 
         return result;
     }
 
     /**
      * New connection arrived -> update graph
      * 
      * @param message
      *            Connect message
      */
     private void updateConnected( final NetworkLogMessage message ) {
         String id1 = (String) message.getExtra( "id" );
         String localaddr1 = (String) message.getExtra( "localaddr" );
         String remoteaddr1 = (String) message.getExtra( "remoteaddr" );
 
         // Do we have a pending connect that correlates with this connection?
         NetworkLogMessage pending = findPendingConnect( localaddr1, remoteaddr1 );
 
         if( null == pending ) {
             // Found nothing, add it to pending connects
             pendingConnect.add( message );
             return;
         }
 
         String id2 = (String) pending.getExtra( "id" );
         String localaddr2 = (String) pending.getExtra( "localaddr" );
 
         // find a vertex with this id or create it
         NetworkVertex a = findVertexById( id1 );
         if( null == a ) {
             a = new NetworkVertex( id1 );
             graph.addVertex( a );
         }
 
         // find a vertex with this id or create it
         NetworkVertex b = findVertexById( id2 );
         if( null == b ) {
             b = new NetworkVertex( id2 );
             graph.addVertex( b );
         }
 
         // Add the edges to the graph
         graph.addEdge( new NetworkEdge( "net", localaddr1 ), a, b,
                 EdgeType.DIRECTED );
         graph.addEdge( new NetworkEdge( "net", localaddr2 ), b, a,
                 EdgeType.DIRECTED );
     }
 
     /**
      * A disconnect appears -> delete the connection edge
      * 
      * @param message
      *            Disconnect message
      */
     private void updateDisconnected( final NetworkLogMessage message ) {
         String id = (String) message.getExtra( "id" );
         String localaddr = (String) message.getExtra( "localaddr" );
         String remoteaddr = (String) message.getExtra( "remoteaddr" );
 
         NetworkVertex local = findVertexById( id );
         if( null == local ) {
             return;
         }
 
         Collection<NetworkEdge> edges = graph.getIncidentEdges( local );
         for ( NetworkEdge edge : edges ) {
             if( edge.getLabel().equals( localaddr )
                     || edge.getLabel().equals( remoteaddr ) ) {
                 graph.removeEdge( edge );
             }
         }
     }
 
     /**
      * The successor of a chord not has changed
      * 
      * @param message
      *            Successor message
      */
     private void updateSuccessor( final NetworkLogMessage message ) {
         String id = (String) message.getExtra( "id" );
         String succ = (String) message.getExtra( "succid" );
 
         NetworkVertex me = findVertexById( id );
         NetworkVertex other = findVertexById( succ );
 
         if( null == other ) {
             other = new NetworkVertex( succ );
             graph.addVertex( other );
         }
 
         if( null == me ) {
             me = new NetworkVertex( id );
             graph.addVertex( me );
         }
 
         Collection<NetworkEdge> edges = new LinkedList<NetworkEdge>(
                 graph.getOutEdges( me ) );
         for ( NetworkEdge edge : edges ) {
             if( "successor".equals( edge.getLabel() ) ) {
                 graph.removeEdge( edge );
             }
         }
 
         graph.addEdge( new NetworkEdge( "chord", "successor" ), me, other,
                 EdgeType.DIRECTED );
     }
 
     /**
      * The predecessor of a chord not has changed
      * 
      * @param message
      *            predecessor message
      */
     private void updatePredecessor( final NetworkLogMessage message ) {
         String id = (String) message.getExtra( "id" );
         String pred = (String) message.getExtra( "predid" );
 
         NetworkVertex me = findVertexById( id );
         NetworkVertex other = findVertexById( pred );
 
         if( null == other ) {
             other = new NetworkVertex( pred );
             graph.addVertex( other );
         }
 
         if( null == me ) {
             me = new NetworkVertex( id );
             graph.addVertex( me );
         }
 
         Collection<NetworkEdge> edges = new LinkedList<NetworkEdge>(
                 graph.getOutEdges( me ) );
         for ( NetworkEdge edge : edges ) {
             if( "predecessor".equals( edge.getLabel() ) ) {
                 graph.removeEdge( edge );
             }
         }
 
         graph.addEdge( new NetworkEdge( "chord", "predecessor" ), me, other,
                 EdgeType.DIRECTED );
     }
 
     /**
      * Relayout and redraw the graph
      */
     private void updateGraph() {
         SwingUtilities.invokeLater( new Runnable() {
             @Override
             public void run() {
                 // need to create new layout to update node positions
                 visViewer
                         .setGraphLayout( new CircleLayout<NetworkVertex, NetworkEdge>(
                                 graph ) );
                 visViewer.repaint();
                 NetworkLogPlayerFrame.this.repaint();
             }
         } );
     }
 }
