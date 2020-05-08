 // $Id: TreePanel.java,v 1.61 2009/06/30 01:37:27 cmzmasek Exp $
 // FORESTER -- software libraries and applications
 // for evolutionary biology research and applications.
 //
 // Copyright (C) 2008-2009 Christian M. Zmasek
 // Copyright (C) 2008-2009 Burnham Institute for Medical Research
 // All rights reserved
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 //
 // Contact: cmzmasek@yahoo.com
 // WWW: www.phylosoft.org/forester
 
 package org.forester.archaeopteryx;
 /**
  * NOTE - The original file was obtained from SourceForge.net (ATV Version 4.1.04) on 2009.07.02
  *  and was modified by the LANL Influenza Sequence Database IT team (flu@lanl.gov)
  */
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Arc2D;
 import java.awt.geom.CubicCurve2D;
 import java.awt.geom.QuadCurve2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JApplet;
 import javax.swing.JColorChooser;
 import javax.swing.JDialog;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 
 import org.forester.archaeopteryx.ControlPanel.NodeClickAction;
 import org.forester.archaeopteryx.Options.NODE_LABEL_DIRECTION;
 import org.forester.archaeopteryx.Options.PHYLOGENY_GRAPHICS_TYPE;
 import org.forester.archaeopteryx.phylogeny.data.RenderableDomainArchitecture;
 import org.forester.phylogeny.Phylogeny;
 import org.forester.phylogeny.PhylogenyMethods;
 import org.forester.phylogeny.PhylogenyNode;
 import org.forester.phylogeny.data.Annotation;
 import org.forester.phylogeny.data.BranchColor;
 import org.forester.phylogeny.data.Confidence;
 import org.forester.phylogeny.data.Event;
 import org.forester.phylogeny.data.PhylogenyData;
 import org.forester.phylogeny.data.Sequence;
 import org.forester.phylogeny.data.Taxonomy;
 import org.forester.phylogeny.iterators.PhylogenyNodeIterator;
 import org.forester.phylogeny.iterators.PreorderTreeIterator;
 import org.forester.util.ForesterConstants;
 import org.forester.util.ForesterUtil;
 
 //******************************************START**********************************************************//
 
 import com.lanl.application.TPTD.applet.AppletParams;
 import com.lanl.application.TPTD.applet.NewWindowSubtree;
 import com.lanl.application.TPTD.applet.SubTreePanel;
 import com.lanl.application.TPTD.custom.data.Accession;
 import com.lanl.application.treeDecorator.applet.TreeDecoratorPaint;
 import com.lanl.application.treeDecorator.applet.ui.drawDecoration.DecoratorColorSet;
 import com.lanl.application.treePruner.custom.data.WorkingSet;
 import com.lanl.application.treePruner.applet.TreePrunerColorSet;
 import com.lanl.application.treePruner.applet.TreePrunerPaint;
 //********************************************END**********************************************************//
 
 public class TreePanel extends JPanel implements ActionListener, MouseWheelListener, Printable {
 
     private static final float             PI                                = ( float ) ( Math.PI );
     private static final float             TWO_PI                            = ( float ) ( 2 * Math.PI );
     private static final float             ONEHALF_PI                        = ( float ) ( 1.5 * Math.PI );
     private static final float             HALF_PI                           = ( float ) ( Math.PI / 2.0 );
     private static final float             ANGLE_ROTATION_UNIT               = ( float ) ( Math.PI / 32 );
     private static final short             OV_BORDER                         = 10;
     final static Cursor                    MOVE_CURSOR                       = Cursor
                                                                                      .getPredefinedCursor( Cursor.MOVE_CURSOR );
     final static Cursor                    ARROW_CURSOR                      = Cursor
                                                                                      .getPredefinedCursor( Cursor.DEFAULT_CURSOR );
     final static Cursor                    HAND_CURSOR                       = Cursor
                                                                                      .getPredefinedCursor( Cursor.HAND_CURSOR );
     final static Cursor                    WAIT_CURSOR                       = Cursor
                                                                                      .getPredefinedCursor( Cursor.WAIT_CURSOR );
     private final static long              serialVersionUID                  = -978349745916505029L;
     private final static int               EURO_D                            = 10;
     private final static String            NODE_POPMENU_NODE_CLIENT_PROPERTY = "node";
     private final static int               MIN_ROOT_LENGTH                   = 3;
     private final static int               BOX_SIZE                          = 4;
     private final static int               HALF_BOX_SIZE                     = TreePanel.BOX_SIZE / 2;
     private final static int               MAX_SUBTREES                      = 100;
     private final static int               MAX_NODE_FRAMES                   = 10;
     private final static int               MOVE                              = 20;
     private final static NumberFormat      FORMATTER_1                       = new DecimalFormat( "#.##" );
     private final static NumberFormat      FORMATTER_3                       = new DecimalFormat( "#.###" );
     private final static boolean           DRAW_MEAN_COUNTS                  = false;
     private final static int               WIGGLE                            = 2;
     private final static int               HALF_BOX_SIZE_PLUS_WIGGLE         = HALF_BOX_SIZE + WIGGLE;
     private final static int               LIMIT_FOR_HQ_RENDERING            = 1000;
     // TODO "rendering_hints" was static before. Need to make sure everything is OK with it not
     // being static anymore (02/20/2009).
     private final RenderingHints           _rendering_hints                  = new RenderingHints( RenderingHints.KEY_RENDERING,
                                                                                                    RenderingHints.VALUE_RENDER_DEFAULT );
     private File                           _treefile                         = null;
     private Configuration                  _configuration                    = null;
     private final NodeFrame[]              _node_frames                      = new NodeFrame[ TreePanel.MAX_NODE_FRAMES ];
     private int                            _node_frame_index                 = 0;
     private Phylogeny                      _phylogeny                        = null;
     private final Phylogeny[]              _phylogenies                      = new Phylogeny[ TreePanel.MAX_SUBTREES ];
   //******************************************START CHANGED**********************************************************//
     private static int                            _subtree_index                    = 0;
     //changed from instance vatiable to static variable
   //********************************************END**********************************************************//
     
     private MainPanel                      _main_panel                       = null;
     private Set<PhylogenyNode>             _found_nodes                      = null;
     private PhylogenyNode                  _highlight_node                   = null;
     private JPopupMenu                     _node_popup_menu                  = null;
     private JMenuItem                      _node_popup_menu_items[]          = null;
     private int                            _longest_ext_node_info            = 0;
     private float                          _x_correction_factor              = 0.0f;
     private float                          _ov_x_correction_factor           = 0.0f;
     private float                          _x_distance                       = 0.0f;
     private float                          _y_distance                       = 0.0f;
     private PHYLOGENY_GRAPHICS_TYPE        _graphics_type                    = PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR;                      //TODO FIXME use enum!
     private double                         _domain_structure_width           = Constants.DOMAIN_STRUCTURE_DEFAULT_WIDTH;
     private int                            _domain_structure_e_value_thr_exp = Constants.DOMAIN_STRUCTURE_E_VALUE_THR_DEFAULT_EXP;
     private float                          _last_drag_point_x                = 0;
     private float                          _last_drag_point_y                = 0;
     private ControlPanel                   _control_panel                    = null;
     private int                            _external_node_index              = 0;
     private final Polygon                  _polygon                          = new Polygon();
     private final StringBuilder            _sb                               = new StringBuilder();
     private JColorChooser                  _color_chooser                    = null;
     private double                         _scale_distance                   = 0.0;
     private String                         _scale_label                      = null;
     private final CubicCurve2D             _cubic_curve                      = new CubicCurve2D.Float();
     private final QuadCurve2D              _quad_curve                       = new QuadCurve2D.Float();
     private Options                        _options                          = null;
     private float                          _ov_max_width                     = 0;
     private float                          _ov_max_height                    = 0;
     private int                            _ov_x_position                    = 0;
     private int                            _ov_y_position                    = 0;
     private int                            _ov_y_start                       = 0;
     private float                          _ov_y_distance                    = 0;
     private float                          _ov_x_distance                    = 0;
     private boolean                        _ov_on                            = false;
     private float                          _urt_starting_angle               = ( float ) ( Math.PI / 2 );
     private float                          _urt_factor                       = 1;
     private final boolean                  _phy_has_branch_lengths;
     private final Rectangle2D              _ov_rectangle                     = new Rectangle2D.Float();
     private boolean                        _in_ov_rect                       = false;
     private boolean                        _in_ov                            = false;
     private final Rectangle                _ov_virtual_rectangle             = new Rectangle();
     final private static double            _180_OVER_PI                      = 180.0 / Math.PI;
     private int                            _circ_max_depth;
     private int                            _circ_num_ext_nodes;
     private PhylogenyNode                  _root;
     final private Arc2D                    _arc                              = new Arc2D.Float();
     final private HashMap<Integer, Double> _urt_nodeid_angle_map             = new HashMap<Integer, Double>();
     HashMap<Float, Integer>                _angles                           = new HashMap<Float, Integer>();
     Graphics2D                             _g2d                              = null;
     private AffineTransform                _at;
   //******************************************START**********************************************************//
     public SubTreePanel subTreePanel;
     public TreePrunerPaint treePrunerPaint;
     public TreeDecoratorPaint treeDecoratorPaint;
     public WorkingSet workingSet;
     public NewWindowSubtree newWindowSubtree = new NewWindowSubtree(this);
     //********************************************END**********************************************************//
     
     TreePanel( final Phylogeny t, final Configuration configuration, final MainPanel tjp ) {
         requestFocusInWindow();
         addKeyListener( new KeyAdapter() {
 
             @Override
             public void keyPressed( final KeyEvent key_event ) {
                 keyPressedCalls( key_event );
                 requestFocusInWindow();
             }
         } );
         addFocusListener( new FocusAdapter() {
 
             @Override
             public void focusGained( final FocusEvent e ) {
                 requestFocusInWindow();
             }
         } );
         if ( ( t == null ) || t.isEmpty() ) {
             throw new IllegalArgumentException( "ill advised attempt to draw phylogeny which is null or empty" );
         }
         _graphics_type = tjp.getOptions().getPhylogenyGraphicsType();
         _main_panel = tjp;
         _configuration = configuration;
         _phylogeny = t;
         _phy_has_branch_lengths = ForesterUtil.isHasAtLeastOneBranchLengthLargerThanZero( _phylogeny );
         init();
         if ( !_phylogeny.isEmpty() ) {
             _phylogeny.recalculateNumberOfExternalDescendants( true );
         }
         //******************************************START**********************************************************//
         if(AppletParams.isEitherTPorTDForAll()){
         	subTreePanel = new SubTreePanel(this);
             subTreePanel.setPhylogeny(t);
             subTreePanel.setSubTreeNodeInfo();
             subTreePanel.setFullTreeNodeInfo();
             workingSet = new WorkingSet();
             treePrunerPaint = new TreePrunerPaint(this,workingSet);
             treeDecoratorPaint = new TreeDecoratorPaint(this);
         }
         //********************************************END**********************************************************//
         setBackground( getTreeColorSet().getBackgroundColor() );
         final MouseListener mouse_listener = new MouseListener( this );
         addMouseListener( mouse_listener );
         addMouseMotionListener( mouse_listener );
         addMouseWheelListener( this );
         calculateScaleDistance();
     }
 
     public void actionPerformed( final ActionEvent e ) {
         int index;
         boolean done = false;
         final JMenuItem node_popup_menu_item = ( JMenuItem ) e.getSource();
         for( index = 0; ( index < _node_popup_menu_items.length ) && !done; index++ ) {
             // NOTE: index corresponds to the indices of click-to options
             // in the control panel.
             if ( node_popup_menu_item == _node_popup_menu_items[ index ] ) {
                 // Set this as the new default click-to action
                 _main_panel.getControlPanel().setClickToAction( index );
                 final PhylogenyNode node = ( PhylogenyNode ) _node_popup_menu
                         .getClientProperty( NODE_POPMENU_NODE_CLIENT_PROPERTY );
                 handleClickToAction( _control_panel.getActionWhenNodeClicked(), node );
                 done = true;
             }
         }
         //requestFocus(); //TODO needed?
         requestFocusInWindow();
         // requestFocus();//TODO needed?
     }
 
     public MainPanel getMainPanel() {
         return _main_panel;
     }
 
     public void mouseWheelMoved( final MouseWheelEvent e ) {
         final int notches = e.getWheelRotation();
         if ( inOvVirtualRectangle( e ) ) {
             if ( !isInOvRect() ) {
                 setInOvRect( true );
                 repaint();
             }
         }
         else {
             if ( isInOvRect() ) {
                 setInOvRect( false );
                 repaint();
             }
         }
         if ( e.isControlDown() ) {
             if ( notches < 0 ) {
                 getTreeFontSet().increaseFontSize();
                 getControlPanel().displayedPhylogenyMightHaveChanged( true );
             }
             else {
                 getTreeFontSet().decreaseFontSize();
                 getControlPanel().displayedPhylogenyMightHaveChanged( true );
             }
         }
         else if ( e.isShiftDown() ) {
             if ( ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.UNROOTED )
                     || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CIRCULAR ) ) {
                 if ( notches < 0 ) {
                     for( int i = 0; i < ( -notches ); ++i ) {
                         setStartingAngle( ( getStartingAngle() % TWO_PI ) + ANGLE_ROTATION_UNIT );
                         getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                 }
                 else {
                     for( int i = 0; i < notches; ++i ) {
                         setStartingAngle( ( getStartingAngle() % TWO_PI ) - ANGLE_ROTATION_UNIT );
                         if ( getStartingAngle() < 0 ) {
                             setStartingAngle( TWO_PI + getStartingAngle() );
                         }
                         getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                 }
             }
             else {
                 if ( notches < 0 ) {
                     for( int i = 0; i < ( -notches ); ++i ) {
                         getControlPanel().zoomInY( Constants.WHEEL_ZOOM_IN_FACTOR );
                         getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                 }
                 else {
                     for( int i = 0; i < notches; ++i ) {
                         getControlPanel().zoomOutY( Constants.WHEEL_ZOOM_OUT_FACTOR );
                         getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                 }
             }
         }
         else {
             if ( notches < 0 ) {
                 for( int i = 0; i < ( -notches ); ++i ) {
                     getControlPanel().zoomInX( Constants.WHEEL_ZOOM_IN_FACTOR,
                                                Constants.WHEEL_ZOOM_IN_X_CORRECTION_FACTOR );
                     getControlPanel().zoomInY( Constants.WHEEL_ZOOM_IN_FACTOR );
                     getControlPanel().displayedPhylogenyMightHaveChanged( false );
                 }
             }
             else {
                 for( int i = 0; i < notches; ++i ) {
                     getControlPanel().zoomOutY( Constants.WHEEL_ZOOM_OUT_FACTOR );
                     getControlPanel().zoomOutX( Constants.WHEEL_ZOOM_OUT_FACTOR,
                                                 Constants.WHEEL_ZOOM_OUT_X_CORRECTION_FACTOR );
                     getControlPanel().displayedPhylogenyMightHaveChanged( false );
                 }
             }
         }
         requestFocus();
         requestFocusInWindow();
         requestFocus();
     }
 
     @Override
     public void paintComponent( final Graphics g ) {
         super.paintComponent( g );
         ( ( Graphics2D ) g ).setRenderingHints( _rendering_hints ); //TODO may this does not need to be here
         paintPhylogeny( g, false, false, 0, 0, 0, 0 );
     }
 
     public int print( final Graphics g, final PageFormat page_format, final int page_index ) throws PrinterException {
         if ( page_index > 0 ) {
             return ( NO_SUCH_PAGE );
         }
         else {
             final Graphics2D g2d = ( Graphics2D ) g;
             g2d.translate( page_format.getImageableX(), page_format.getImageableY() );
             // Turn off double buffering !?
             paintPhylogeny( g2d, true, false, 0, 0, 0, 0 );
             // Turn double buffering back on !?
             return ( PAGE_EXISTS );
         }
     }
 
     // void addToUrtFactor( final float f ) {
     //     _urt_factor += f;
     // }
     void assignGraphicsForNodeBoxWithColorForParentBranch( final PhylogenyNode node, final Graphics g ) {
         if ( getControlPanel().isColorBranches() && ( PhylogenyMethods.getBranchColorValue( node ) != null ) ) {
             g.setColor( PhylogenyMethods.getBranchColorValue( node ) );
         }
         else {
         	//******************************************START CHANGED**********************************************************//
         	if(AppletParams.isTreePrunerForAll() ){
         		treePrunerPaint.initArrayLists();
                 treePrunerPaint.paintKeepRemove(g,node);
                 _control_panel.controlPanelAdditions.callTreePrunerAutoSaveToRefresh();
             }
         	else{
         		g.setColor( getTreeColorSet().getBranchColor() );
         	}
         	//changed
             //********************************************END**********************************************************//
             
         }
     }
 
     void calcMaxDepth() {
         if ( _phylogeny != null ) {
             _circ_max_depth = PhylogenyMethods.calculateMaxDepth( _phylogeny );
         }
     }
 
     void calculateLongestExtNodeInfo() {
         if ( ( _phylogeny == null ) || _phylogeny.isEmpty() ) {
             return;
         }
         int longest = 20;
         for( final PhylogenyNode node : _phylogeny.getExternalNodes() ) {
             int sum = 0;
             if ( node.isCollapse() ) {
                 continue;
             }
             if ( getControlPanel().isShowNodeNames() ) {
                 sum += getTreeFontSet()._fm_large.stringWidth( node.getNodeName() + " " );
             }
             if ( node.getNodeData().isHasSequence() ) {
                 if ( getControlPanel().isShowSequenceAcc()
                         && ( node.getNodeData().getSequence().getAccession() != null ) ) {
                     sum += getTreeFontSet()._fm_large.stringWidth( node.getNodeData().getSequence().getAccession()
                             .getValue()
                             + " " );
                 }
                 if ( getControlPanel().isShowGeneNames() && ( node.getNodeData().getSequence().getName().length() > 0 ) ) {
                     sum += getTreeFontSet()._fm_large.stringWidth( node.getNodeData().getSequence().getName() + " " );
                 }
                 if ( getControlPanel().isShowAnnotation()
                         && ( node.getNodeData().getSequence().getAnnotations() != null )
                         && !node.getNodeData().getSequence().getAnnotations().isEmpty() ) {
                     sum += getTreeFontSet()._fm_large.stringWidth( node.getNodeData().getSequence().getAnnotations()
                             .get( 0 ).asSimpleText()
                             + " " );
                 }
             }
             if ( node.getNodeData().isHasTaxonomy() ) {
                 final Taxonomy tax = node.getNodeData().getTaxonomy();
                 if ( getControlPanel().isShowTaxonomyCode() && !ForesterUtil.isEmpty( tax.getTaxonomyCode() ) ) {
                     sum += getTreeFontSet()._fm_large_italic.stringWidth( tax.getTaxonomyCode() + " " );
                 }
                 if ( getControlPanel().isShowTaxonomyNames() && !ForesterUtil.isEmpty( tax.getScientificName() ) ) {
                     sum += getTreeFontSet()._fm_large_italic.stringWidth( tax.getScientificName() + " " );
                 }
                 if ( getControlPanel().isShowTaxonomyNames() && !ForesterUtil.isEmpty( tax.getCommonName() ) ) {
                     sum += getTreeFontSet()._fm_large_italic.stringWidth( tax.getCommonName() + " " );
                 }
             }
             if ( getControlPanel().isShowBinaryCharacters() && node.getNodeData().isHasBinaryCharacters() ) {
                 sum += getTreeFontSet()._fm_large.stringWidth( node.getNodeData().getBinaryCharacters()
                         .getGainedCharactersAsStringBuffer().toString() );
             }
             if ( getControlPanel().isShowDomainArchitectures() && node.getNodeData().isHasSequence()
                     && ( node.getNodeData().getSequence().getDomainArchitecture() != null ) ) {
                 sum += ( ( RenderableDomainArchitecture ) node.getNodeData().getSequence().getDomainArchitecture() )
                         .getRenderingSize().getWidth();
             }
             if ( sum > longest ) {
                 longest = sum;
             }
         }
         setLongestExtNodeInfo( longest );
     }
 
     void calculateScaleDistance() {
         if ( ( _phylogeny == null ) || _phylogeny.isEmpty() ) {
             return;
         }
         final double height = PhylogenyMethods.calculateMaxDistanceToRoot( _phylogeny );
         if ( height > 0 ) {
             if ( ( height <= 0.5 ) ) {
                 setScaleDistance( 0.01 );
             }
             else if ( height <= 5.0 ) {
                 setScaleDistance( 0.1 );
             }
             else if ( height <= 50.0 ) {
                 setScaleDistance( 1 );
             }
             else if ( height <= 500.0 ) {
                 setScaleDistance( 10 );
             }
             else {
                 setScaleDistance( 100 );
             }
         }
         else {
             setScaleDistance( 0.0 );
         }
         String scale_label = String.valueOf( getScaleDistance() );
         if ( !ForesterUtil.isEmpty( _phylogeny.getDistanceUnit() ) ) {
             scale_label += " [" + _phylogeny.getDistanceUnit() + "]";
         }
         setScaleLabel( scale_label );
     }
 
     /**
      * Collapse the tree from the given node
      * 
      * @param node
      *            a PhylogenyNode
      */
     void collapse( final PhylogenyNode node ) {
         if ( !node.isExternal() && !node.isRoot() ) {
             final boolean collapse = !node.isCollapse();
             Util.collapseSubtree( node, collapse );
             _phylogeny.recalculateNumberOfExternalDescendants( true );
             calculateLongestExtNodeInfo();
             resetPreferredSize();
             updateOvSizes();
             _main_panel.adjustJScrollPane();
             repaint();
         }
     }
 
     void collapseSpeciesSpecificSubtrees() {
         if ( ( _phylogeny == null ) || ( _phylogeny.getNumberOfExternalNodes() < 2 ) ) {
             return;
         }
         setWaitCursor();
         Util.collapseSpeciesSpecificSubtrees( _phylogeny );
         _phylogeny.recalculateNumberOfExternalDescendants( true );
         calculateLongestExtNodeInfo();
         resetPreferredSize();
         _main_panel.adjustJScrollPane();
         setArrowCursor();
         repaint();
     }
 
     void confColor() {
         if ( ( _phylogeny == null ) || ( _phylogeny.getNumberOfExternalNodes() < 2 ) ) {
             return;
         }
         setWaitCursor();
         Util.colorPhylogenyAccordingToConfidenceValues( _phylogeny, this );
         _control_panel.setColorBranches( true );
         if ( _control_panel.getColorBranchesCb() != null ) {
             _control_panel.getColorBranchesCb().setSelected( true );
         }
         setArrowCursor();
         repaint();
     }
 
     void decreaseDomainStructureEvalueThreshold() {
         if ( _domain_structure_e_value_thr_exp > -20 ) {
             _domain_structure_e_value_thr_exp -= 1;
         }
     }
 
     /**
      * Find the node, if any, at the given location
      * 
      * @param x
      * @param y
      * @return pointer to the node at x,y, null if not found
      */
     PhylogenyNode findNode( final int x, final int y ) {
         if ( ( _phylogeny == null ) || _phylogeny.isEmpty() ) {
             return null;
         }
         for( final PhylogenyNodeIterator iter = _phylogeny.iteratorPostorder(); iter.hasNext(); ) {
             final PhylogenyNode node = iter.next();
             if ( ( _phylogeny.isRooted() || !node.isRoot() || ( node.getNumberOfDescendants() > 2 ) )
                     && ( ( node.getXcoord() - HALF_BOX_SIZE_PLUS_WIGGLE ) <= x )
                     && ( ( node.getXcoord() + HALF_BOX_SIZE_PLUS_WIGGLE ) >= x )
                     && ( ( node.getYcoord() - HALF_BOX_SIZE_PLUS_WIGGLE ) <= y )
                     && ( ( node.getYcoord() + HALF_BOX_SIZE_PLUS_WIGGLE ) >= y ) ) {
                 return node;
             }
         }
         return null;
     }
 
     Configuration getConfiguration() {
         return _configuration;
     }
 
     ControlPanel getControlPanel() {
         return _control_panel;
     }
 
     int getDomainStructureEvalueThreshold() {
         return _domain_structure_e_value_thr_exp;
     }
 
     Set<PhylogenyNode> getFoundNodes() {
         return _found_nodes;
     }
 
     int getLongestExtNodeInfo() {
         return _longest_ext_node_info;
     }
 
     Options getOptions() {
         if ( _options == null ) {
             _options = getControlPanel().getOptions();
         }
         return _options;
     }
 
     Rectangle2D getOvRectangle() {
         return _ov_rectangle;
     }
 
     Rectangle getOvVirtualRectangle() {
         return _ov_virtual_rectangle;
     }
 
     /**
      * Get a pointer to the phylogeny 
      * 
      * @return a pointer to the phylogeny
      */
     Phylogeny getPhylogeny() {
         return _phylogeny;
     }
 
     PHYLOGENY_GRAPHICS_TYPE getPhylogenyGraphicsType() {
         return _graphics_type;
     }
 
     float getStartingAngle() {
         return _urt_starting_angle;
     }
 
     /**
      * @return pointer to colorset for tree drawing
      */
     TreeColorSet getTreeColorSet() {
         return getMainPanel().getTreeColorSet();
     }
 
     File getTreeFile() {
         return _treefile;
     }
 
     float getXcorrectionFactor() {
         return _x_correction_factor;
     }
 
     float getXdistance() {
         return _x_distance;
     }
 
     float getYdistance() {
         return _y_distance;
     }
 
     void increaseDomainStructureEvalueThreshold() {
         if ( _domain_structure_e_value_thr_exp < 3 ) {
             _domain_structure_e_value_thr_exp += 1;
         }
     }
 
     void inferCommonPartOfScientificNames() {
         if ( ( _phylogeny == null ) || ( _phylogeny.getNumberOfExternalNodes() < 2 ) ) {
             return;
         }
         setWaitCursor();
         Util.inferCommonPartOfScientificNames( _phylogeny );
         setArrowCursor();
         repaint();
     }
 
     void initNodeData() {
         if ( ( _phylogeny == null ) || _phylogeny.isEmpty() ) {
             return;
         }
         double max_original_domain_structure_width = 0.0;
         for( final PhylogenyNode node : _phylogeny.getExternalNodes() ) {
             if ( node.getNodeData().isHasSequence()
                     && ( node.getNodeData().getSequence().getDomainArchitecture() != null ) ) {
                 RenderableDomainArchitecture rds = null;
                 if ( !( node.getNodeData().getSequence().getDomainArchitecture() instanceof RenderableDomainArchitecture ) ) {
                     rds = new RenderableDomainArchitecture( node.getNodeData().getSequence().getDomainArchitecture() );
                     node.getNodeData().getSequence().setDomainArchitecture( rds );
                 }
                 else {
                     rds = ( RenderableDomainArchitecture ) node.getNodeData().getSequence().getDomainArchitecture();
                 }
                 if ( getControlPanel().isShowDomainArchitectures() ) {
                     final double dsw = rds.getOriginalSize().getWidth();
                     if ( dsw > max_original_domain_structure_width ) {
                         max_original_domain_structure_width = dsw;
                     }
                 }
             }
         }
         if ( getControlPanel().isShowDomainArchitectures() ) {
             final double ds_factor_width = _domain_structure_width / max_original_domain_structure_width;
             for( final PhylogenyNode node : _phylogeny.getExternalNodes() ) {
                 if ( node.getNodeData().isHasSequence()
                         && ( node.getNodeData().getSequence().getDomainArchitecture() != null ) ) {
                     final RenderableDomainArchitecture rds = ( RenderableDomainArchitecture ) node.getNodeData()
                             .getSequence().getDomainArchitecture();
                     rds.setRenderingFactorWidth( ds_factor_width );
                     rds.setParameter( _domain_structure_e_value_thr_exp );
                 }
             }
         }
     }
 
     boolean inOv( final MouseEvent e ) {
         return ( ( e.getX() > getVisibleRect().x + getOvXPosition() + 1 )
                 && ( e.getX() < getVisibleRect().x + getOvXPosition() + getOvMaxWidth() - 1 )
                 && ( e.getY() > getVisibleRect().y + getOvYPosition() + 1 ) && ( e.getY() < getVisibleRect().y
                 + getOvYPosition() + getOvMaxHeight() - 1 ) );
     }
 
     boolean inOvRectangle( final MouseEvent e ) {
         return ( ( e.getX() >= getOvRectangle().getX() - 1 )
                 && ( e.getX() <= getOvRectangle().getX() + getOvRectangle().getWidth() + 1 )
                 && ( e.getY() >= getOvRectangle().getY() - 1 ) && ( e.getY() <= getOvRectangle().getY()
                 + getOvRectangle().getHeight() + 1 ) );
     }
 
     boolean isInOvRect() {
         return _in_ov_rect;
     }
 
     boolean isOvOn() {
         return _ov_on;
     }
 
     boolean isPhyHasBranchLengths() {
         return _phy_has_branch_lengths;
     }
 
     void midpointRoot() {
         if ( ( _phylogeny == null ) || ( _phylogeny.getNumberOfExternalNodes() < 2 ) ) {
             return;
         }
         if ( !_phylogeny.isRerootable() ) {
             JOptionPane.showMessageDialog( this,
                                            "This is not rerootable",
                                            "Not rerootable",
                                            JOptionPane.WARNING_MESSAGE );
             return;
         }
         setWaitCursor();
         PhylogenyMethods.midpointRoot( _phylogeny );
         setArrowCursor();
         repaint();
     }
 
     void mouseClicked( final MouseEvent e ) {
         if ( getOptions().isShowOverview() && isOvOn() && isInOv() ) {
             final double w_ratio = getVisibleRect().width / getOvRectangle().getWidth();
             final double h_ratio = getVisibleRect().height / getOvRectangle().getHeight();
             double x = ( e.getX() - getVisibleRect().x - getOvXPosition() - getOvRectangle().getWidth() / 2.0 )
                     * w_ratio;
             double y = ( e.getY() - getVisibleRect().y - getOvYPosition() - getOvRectangle().getHeight() / 2.0 )
                     * h_ratio;
             if ( x < 0 ) {
                 x = 0;
             }
             if ( y < 0 ) {
                 y = 0;
             }
             final double max_x = getWidth() - getVisibleRect().width;
             final double max_y = getHeight() - getVisibleRect().height;
             if ( x > max_x ) {
                 x = max_x;
             }
             if ( y > max_y ) {
                 y = max_y;
             }
             getMainPanel().getCurrentScrollPane().getViewport()
                     .setViewPosition( new Point( ForesterUtil.roundToInt( x ), ForesterUtil.roundToInt( y ) ) );
             setInOvRect( true );
             repaint();
         }
         else {
             final PhylogenyNode node = findNode( e.getX(), e.getY() );
             if ( node != null ) {
                 if ( !node.isRoot() && node.getParent().isCollapse() ) {
                     return;
                 }
                 _highlight_node = node;
                 // Check if shift key is down
                 if ( ( e.getModifiers() & InputEvent.SHIFT_MASK ) != 0 ) {
                     // Yes, so add to _found_nodes
                     if ( getFoundNodes() == null ) {
                         setFoundNodes( new HashSet<PhylogenyNode>() );
                     }
                     getFoundNodes().add( node );
                     // Check if control key is down
                 }
                 else if ( ( e.getModifiers() & InputEvent.CTRL_MASK ) != 0 ) {
                     // Yes, so pop-up menu
                     displayNodePopupMenu( node, e.getX(), e.getY() );
                     // Handle unadorned click
                 }
                 else {
                     // Check for right mouse button
                     if ( e.getModifiers() == 4 ) {
                         displayNodePopupMenu( node, e.getX(), e.getY() );
                     }
                     else {
                         // if not in _found_nodes, clear _found_nodes
                         handleClickToAction( _control_panel.getActionWhenNodeClicked(), node );
                     }
                 }
             }
             else {
                 // no node was clicked
                 _highlight_node = null;
             }
         }
         repaint();
     }
 
     void mouseDragInBrowserPanel( final MouseEvent e ) {
         setCursor( MOVE_CURSOR );
         final Point scroll_position = getMainPanel().getCurrentScrollPane().getViewport().getViewPosition();
         scroll_position.x -= ( e.getX() - getLastDragPointX() );
         scroll_position.y -= ( e.getY() - getLastDragPointY() );
         if ( scroll_position.x < 0 ) {
             scroll_position.x = 0;
         }
         else {
             final int max_x = getMainPanel().getCurrentScrollPane().getHorizontalScrollBar().getMaximum()
                     - getMainPanel().getCurrentScrollPane().getHorizontalScrollBar().getVisibleAmount();
             if ( scroll_position.x > max_x ) {
                 scroll_position.x = max_x;
             }
         }
         if ( scroll_position.y < 0 ) {
             scroll_position.y = 0;
         }
         else {
             final int max_y = getMainPanel().getCurrentScrollPane().getVerticalScrollBar().getMaximum()
                     - getMainPanel().getCurrentScrollPane().getVerticalScrollBar().getVisibleAmount();
             if ( scroll_position.y > max_y ) {
                 scroll_position.y = max_y;
             }
         }
         if ( isOvOn() || getOptions().isShowScale() ) {
             //~~~~~~~~~~~~~~<<<<<<<<<<<<<<
             repaint();//~~~~~~~~~~~~~~<<<<<<<<<<<<<<
             //~~~~~~~~~~~~~~<<<<<<<<<<<<<<
         }
         getMainPanel().getCurrentScrollPane().getViewport().setViewPosition( scroll_position );
     }
 
     void mouseDragInOvRectangle( final MouseEvent e ) {
         setCursor( HAND_CURSOR );
         //angela aki
         //yuki soari
         final double w_ratio = getVisibleRect().width / getOvRectangle().getWidth();
         final double h_ratio = getVisibleRect().height / getOvRectangle().getHeight();
         final Point scroll_position = getMainPanel().getCurrentScrollPane().getViewport().getViewPosition();
         double dx = ( w_ratio * e.getX() - w_ratio * getLastDragPointX() );
         double dy = ( h_ratio * e.getY() - h_ratio * getLastDragPointY() );
         scroll_position.x = ForesterUtil.roundToInt( scroll_position.x + dx );
         scroll_position.y = ForesterUtil.roundToInt( scroll_position.y + dy );
         if ( scroll_position.x <= 0 ) {
             scroll_position.x = 0;
             dx = 0;
         }
         else {
             final int max_x = getMainPanel().getCurrentScrollPane().getHorizontalScrollBar().getMaximum()
                     - getMainPanel().getCurrentScrollPane().getHorizontalScrollBar().getVisibleAmount();
             if ( scroll_position.x >= max_x ) {
                 dx = 0;
                 scroll_position.x = max_x;
             }
         }
         if ( scroll_position.y <= 0 ) {
             dy = 0;
             scroll_position.y = 0;
         }
         else {
             final int max_y = getMainPanel().getCurrentScrollPane().getVerticalScrollBar().getMaximum()
                     - getMainPanel().getCurrentScrollPane().getVerticalScrollBar().getVisibleAmount();
             if ( scroll_position.y >= max_y ) {
                 dy = 0;
                 scroll_position.y = max_y;
             }
         }
         repaint();
         getMainPanel().getCurrentScrollPane().getViewport().setViewPosition( scroll_position );
         setLastMouseDragPointX( ( float ) ( e.getX() + dx ) );
         setLastMouseDragPointY( ( float ) ( e.getY() + dy ) );
     }
 
     void mouseMoved( final MouseEvent e ) {
         requestFocusInWindow();
         if ( getOptions().isShowOverview() && isOvOn() ) {
             if ( inOvVirtualRectangle( e ) ) {
                 if ( !isInOvRect() ) {
                     setInOvRect( true );
                     repaint();
                 }
             }
             else {
                 if ( isInOvRect() ) {
                     setInOvRect( false );
                     repaint();
                 }
             }
         }
         if ( inOv( e ) && getOptions().isShowOverview() && isOvOn() ) {
             if ( !isInOv() ) {
                 setInOv( true );
             }
         }
         else {
             if ( isInOv() ) {
                 setInOv( false );
             }
             final PhylogenyNode node = findNode( e.getX(), e.getY() );
             if ( ( node != null ) && ( node.isRoot() || !node.getParent().isCollapse() ) ) {
                 // cursor is over a tree node
                 setCursor( HAND_CURSOR );
             }
             else {
                 setCursor( ARROW_CURSOR );
             }
         }
     }
 
     void mouseReleasedInBrowserPanel( final MouseEvent e ) {
         setCursor( ARROW_CURSOR );
     }
 
     void multiplyUrtFactor( final float f ) {
         _urt_factor *= f;
     }
 
     void paintBranchCircular( final PhylogenyNode p,
                               final PhylogenyNode c,
                               final Graphics2D g,
                               final boolean radial_labels,
                               final boolean to_pdf,
                               final boolean to_graphics_file ) {
         final double angle = _urt_nodeid_angle_map.get( c.getNodeId() );
         final double root_x = _root.getXcoord();
         final double root_y = _root.getYcoord();
         final double dx = root_x - p.getXcoord();
         final double dy = root_y - p.getYcoord();
         final double parent_radius = Math.sqrt( dx * dx + dy * dy );
         final double arc = ( _urt_nodeid_angle_map.get( p.getNodeId() ) ) - angle;
         final double start = -angle - arc;
         assignGraphicsForBranchWithColorForParentBranch( c, false, g, to_pdf, to_graphics_file );
         if ( c.isFirstChildNode() || c.isLastChildNode() ) {
             final double r2 = 2.0 * parent_radius;
             drawArc( root_x - parent_radius, root_y - parent_radius, r2, r2, start, arc, g );
         }
         drawLine( c.getXcoord(), c.getYcoord(), root_x + ( Math.cos( angle ) * parent_radius ), root_y
                 + ( Math.sin( angle ) * parent_radius ), g );
         if ( c.isExternal() ) {
             paintNodeDataUnrootedCirc( g, c, to_pdf, to_graphics_file, radial_labels, 0 );
         }
     }
 
     void paintCircular( final Phylogeny phy,
                         final float starting_angle,
                         final int center_x,
                         final int center_y,
                         final int radius,
                         final Graphics g,
                         final boolean to_pdf,
                         final boolean to_graphics_file ) {
         _circ_num_ext_nodes = phy.getNumberOfExternalNodes();
         _root = phy.getRoot();
         _root.setXcoord( center_x );
         _root.setYcoord( center_y );
         final boolean radial_labels = _options.getNodeLabelDirection() == NODE_LABEL_DIRECTION.RADIAL;
         double current_angle = starting_angle;
         for( final PhylogenyNodeIterator it = phy.iteratorExternalForward(); it.hasNext(); ) {
             final PhylogenyNode n = it.next();
             n.setXcoord( center_x + ( float ) ( radius * Math.cos( current_angle ) ) );
             n.setYcoord( center_y + ( float ) ( radius * Math.sin( current_angle ) ) );
             _urt_nodeid_angle_map.put( n.getNodeId(), current_angle );
             current_angle += ( TWO_PI / _circ_num_ext_nodes );
         }
         paintCirculars( phy.getRoot(),
                         phy,
                         center_x,
                         center_y,
                         radius,
                         radial_labels,
                         ( Graphics2D ) g,
                         to_pdf,
                         to_graphics_file );
     } //shimazu aiya enka
 
     //TODO FIXME once pdf issue is settled, used Graphics2D where ever possible!!!!!!!!!!!!!!!!
     //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
   //******************************************START CHANGED**********************************************************//
     public void paintPhylogeny( final Graphics g,
     // void paintPhylogeny( final Graphics g, // default->public - changed
     //********************************************END**********************************************************//
                          final boolean to_pdf,
                          final boolean to_graphics_file,
                          final int graphics_file_width,
                          final int graphics_file_height,
                          final int graphics_file_x,
                          final int graphics_file_y ) {
         if ( ( getPhylogenyGraphicsType() != PHYLOGENY_GRAPHICS_TYPE.UNROOTED )
                 && ( getPhylogenyGraphicsType() != PHYLOGENY_GRAPHICS_TYPE.CIRCULAR ) ) {
             _external_node_index = 0;
             // Position starting X of tree
             if ( !_phylogeny.isRooted() ) {
                 _phylogeny.getRoot().setXcoord( TreePanel.MOVE );
             }
             else if ( ( _phylogeny.getRoot().getDistanceToParent() > 0.0 ) && getControlPanel().isDrawPhylogram() ) {
                 _phylogeny.getRoot().setXcoord( ( float ) ( TreePanel.MOVE + ( _phylogeny.getRoot()
                         .getDistanceToParent() * getXcorrectionFactor() ) ) );
             }
             else {
                 _phylogeny.getRoot().setXcoord( TreePanel.MOVE + getXdistance() );
             }
             // Position starting Y of tree
             _phylogeny.getRoot().setYcoord( ( getYdistance() * _phylogeny.getRoot().getNumberOfExternalNodes() )
                     + ( TreePanel.MOVE / 2.0f ) );
             // Color the background
             if ( !to_pdf ) {
             	//******************************************START CHANGED**********************************************************//
             	if(AppletParams.isTreePrunerForAll() ){
             		g.setColor(TreePrunerColorSet.getBackgroundColor());
                 }
             	else if(AppletParams.isTreeDecoratorForAll() ){
             		g.setColor(DecoratorColorSet.getBackgroundColor());
                 }
             	else{
             		g.setColor( getTreeColorSet().getBackgroundColor() );
             	}
             	
             	//	  //Changed the background color from Back to white - changed
             															 
                 //********************************************END**********************************************************//
                 if ( to_graphics_file ) {
                     if ( getOptions().isPrintBlackAndWhite() ) {
                         g.setColor( Color.WHITE );
                     }
                     g.fillRect( graphics_file_x, graphics_file_y, graphics_file_width, graphics_file_height );
                 }
                 else {
                     g.fillRect( getX(), getY(), getWidth(), getHeight() );
                 }
             }
             int dynamic_hiding_factor = 0;
             if ( getControlPanel().isDynamicallyHideData() ) {
                 dynamic_hiding_factor = ( int ) ( getTreeFontSet()._fm_large.getHeight() / ( 1.5 * getYdistance() ) );
             }
             if ( getControlPanel().getDynamicallyHideData() != null ) {
                 if ( dynamic_hiding_factor > 1 ) {
                     getControlPanel().setDynamicHidingIsOn( true );
                 }
                 else {
                     getControlPanel().setDynamicHidingIsOn( false );
                 }
             }
             final PhylogenyNodeIterator it;
             for( it = _phylogeny.iteratorPreorder(); it.hasNext(); ) {
                 paintNode( g, it.next(), to_pdf, dynamic_hiding_factor > 1, dynamic_hiding_factor, to_graphics_file );
             }
             if ( getOptions().isShowScale() ) {
                 if ( !( to_graphics_file || to_pdf ) ) {
                     paintScale( g,
                                 getVisibleRect().x,
                                 getVisibleRect().y + getVisibleRect().height,
                                 to_pdf,
                                 to_graphics_file );
                 }
                 else {
                     paintScale( g, graphics_file_x, graphics_file_y + graphics_file_height, to_pdf, to_graphics_file );
                 }
             }
             if ( getOptions().isShowOverview() && isOvOn() && !to_graphics_file && !to_pdf ) {
                 paintPhylogenyLite( g );
             }
         }
         else if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.UNROOTED ) {
             final float angle = getStartingAngle();
             final boolean radial_labels = _options.getNodeLabelDirection() == NODE_LABEL_DIRECTION.RADIAL;
             paintUnrooted( _phylogeny.getRoot(), angle, angle + 2 * Math.PI, radial_labels, g, to_pdf, to_graphics_file );
         }
         else if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CIRCULAR ) {
             final int radius = ( int ) ( ( Math.min( getPreferredSize().getWidth(), getPreferredSize().getHeight() ) / 2 ) - ( MOVE + getLongestExtNodeInfo() ) );
             final int d = radius + MOVE + getLongestExtNodeInfo();
             paintCircular( _phylogeny, getStartingAngle(), d, d, /**radius > 1 ?**/
             radius /**: 1**/
             , g, to_pdf, to_graphics_file );
             if ( getOptions().isShowOverview() && isOvOn() && !to_graphics_file && !to_pdf ) {
                 paintPhylogenyLite( g );
                 paintCircular( _phylogeny, getStartingAngle(), 40, 40, /**radius > 1 ?**/
                 20 /**: 1**/
                 , g, to_pdf, to_graphics_file );
                 paintOvRectangle( g );
             }
         }
     }
 
     /**
      * Remove all edit-node frames
      */
     void removeAllEditNodeJFrames() {
         for( int i = 0; i <= ( TreePanel.MAX_NODE_FRAMES - 1 ); i++ ) {
             if ( _node_frames[ i ] != null ) {
                 _node_frames[ i ].dispose();
                 _node_frames[ i ] = null;
             }
         }
         _node_frame_index = 0;
     }
 
     /**
      * Remove a node-edit frame.
      */
     void removeEditNodeFrame( final int i ) {
         _node_frame_index--;
         _node_frames[ i ] = null;
         if ( i < _node_frame_index ) {
             for( int j = 0; j < _node_frame_index - 1; j++ ) {
                 _node_frames[ j ] = _node_frames[ j + 1 ];
             }
             _node_frames[ _node_frame_index ] = null;
         }
     }
 
     void reRoot( final PhylogenyNode node ) {
         if ( !getPhylogeny().isRerootable() ) {
             JOptionPane.showMessageDialog( this,
                                            "This is not rerootable",
                                            "Not rerootable",
                                            JOptionPane.WARNING_MESSAGE );
             return;
         }
         if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.UNROOTED ) {
             JOptionPane.showMessageDialog( this,
                                            "Cannot reroot in unrooted display type",
                                            "Not rerootable",
                                            JOptionPane.WARNING_MESSAGE );
             return;
         }
         getPhylogeny().reRoot( node );
         getPhylogeny().recalculateNumberOfExternalDescendants( true );
         resetPreferredSize();
         getMainPanel().adjustJScrollPane();
         repaint();
     }
 
   //******************************************START CHANGED**********************************************************//
     public void resetPreferredSize() {
     //void resetPreferredSize() { //changed default -> public	
     //********************************************END**********************************************************//
         if ( ( getPhylogeny() == null ) || getPhylogeny().isEmpty() ) {
             return;
         }
         int x = 0;
         int y = 0;
         final int ext_nodes = getPhylogeny().getRoot().getNumberOfExternalNodes();
         y = TreePanel.MOVE + ForesterUtil.roundToInt( getYdistance() * ext_nodes * 2 );
         if ( getControlPanel().isDrawPhylogram() ) {
             x = TreePanel.MOVE
                     + getLongestExtNodeInfo()
                     + ForesterUtil
                             .roundToInt( ( getXcorrectionFactor() * getPhylogeny().getHeight() ) + getXdistance() );
         }
         else {
             if ( !isNonLinedUpCladogram() ) {
                 x = TreePanel.MOVE + getLongestExtNodeInfo()
                         + ForesterUtil.roundToInt( getXdistance() * ( ext_nodes + 2 ) );
             }
             else {
                 x = TreePanel.MOVE
                         + getLongestExtNodeInfo()
                         + ForesterUtil.roundToInt( getXdistance()
                                 * ( PhylogenyMethods.calculateMaxDepth( getPhylogeny() ) + 1 ) );
             }
         }
         setPreferredSize( new Dimension( x, y ) );
     }
 
     void setArrowCursor() {
         setCursor( ARROW_CURSOR );
         repaint();
     }
 
     void setControlPanel( final ControlPanel atv_control ) {
         _control_panel = atv_control;
     }
 
     void setFoundNodes( final Set<PhylogenyNode> found_nodes ) {
         _found_nodes = found_nodes;
     }
 
     void setInOvRect( final boolean in_ov_rect ) {
         _in_ov_rect = in_ov_rect;
     }
 
     void setLargeFonts() {
         getTreeFontSet().largeFonts();
     }
 
     void setLastMouseDragPointX( final float x ) {
         _last_drag_point_x = x;
     }
 
     void setLastMouseDragPointY( final float y ) {
         _last_drag_point_y = y;
     }
 
     void setLongestExtNodeInfo( final int i ) {
         _longest_ext_node_info = i;
     }
 
     void setMediumFonts() {
         getTreeFontSet().mediumFonts();
     }
 
     void setOvOn( final boolean ov_on ) {
         _ov_on = ov_on;
     }
 
     /**
      * Set parameters for printing the displayed tree
      * 
      * @param x
      * @param y
      */
   //******************************************START CHANGED**********************************************************//
     public void setParametersForPainting( final int x, final int y, final boolean recalc_longest_ext_node_info ) {
   // void setParametersForPainting( final int x, final int y, final boolean recalc_longest_ext_node_info ) { default -> public - changed
   //********************************************END**********************************************************//
         // updateStyle(); not needed?
         if ( ( _phylogeny != null ) && !_phylogeny.isEmpty() ) {
             initNodeData();
             if ( recalc_longest_ext_node_info ) {
                 calculateLongestExtNodeInfo();
             }
             int ext_nodes = _phylogeny.getRoot().getNumberOfExternalNodes();
             if ( ext_nodes == 1 ) {
                 ext_nodes = PhylogenyMethods.calculateMaxDepth( _phylogeny );
                 if ( ext_nodes < 1 ) {
                     ext_nodes = 1;
                 }
             }
             updateOvSizes();
             float xdist = 0;
             float ov_xdist = 0;
             if ( !isNonLinedUpCladogram() ) {
                 xdist = ( float ) ( ( x - getLongestExtNodeInfo() - TreePanel.MOVE ) / ( ext_nodes + 3.0 ) );
                 ov_xdist = ( float ) ( getOvMaxWidth() / ( ext_nodes + 3.0 ) );
             }
             else {
                 xdist = ( ( x - getLongestExtNodeInfo() - TreePanel.MOVE ) / ( PhylogenyMethods
                         .calculateMaxDepth( _phylogeny ) + 1 ) );
                 ov_xdist = ( getOvMaxWidth() / ( PhylogenyMethods.calculateMaxDepth( _phylogeny ) + 1 ) );
             }
             float ydist = ( float ) ( ( y - TreePanel.MOVE ) / ( ext_nodes * 2.0 ) );
             if ( xdist < 0.0 ) {
                 xdist = 0.0f;
             }
             if ( ov_xdist < 0.0 ) {
                 ov_xdist = 0.0f;
             }
             if ( ydist < 0.0 ) {
                 ydist = 0.0f;
             }
             setXdistance( xdist );
             setYdistance( ydist );
             setOvXDistance( ov_xdist );
             final double height = _phylogeny.getHeight();
             if ( height > 0 ) {
                 final float corr = ( float ) ( ( x - TreePanel.MOVE - getLongestExtNodeInfo() - getXdistance() ) / height );
                 setXcorrectionFactor( corr > 0 ? corr : 0 );
                 final float ov_corr = ( float ) ( ( getOvMaxWidth() - getOvXDistance() ) / height );
                 setOvXcorrectionFactor( ov_corr > 0 ? ov_corr : 0 );
             }
             else {
                 setXcorrectionFactor( 0 );
                 setOvXcorrectionFactor( 0 );
             }
             if ( isPhyHasBranchLengths() ) {
                 setUrtFactor( ( float ) ( getVisibleRect().width / ( 2 * PhylogenyMethods
                         .calculateMaxDistanceToRoot( _phylogeny ) ) ) );
             }
             else {
                 final int max_depth = PhylogenyMethods.calculateMaxDepth( _phylogeny );
                 if ( max_depth > 0 ) {
                     setUrtFactor( ( getVisibleRect().width / ( 2 * max_depth ) ) );
                 }
                 else {
                     setUrtFactor( ( getVisibleRect().width / 2 ) );
                 }
             }
             calcMaxDepth();
         }
     }
 
     void setPhylogenyGraphicsType( final PHYLOGENY_GRAPHICS_TYPE graphics_type ) {
         _graphics_type = graphics_type;
         setTextAntialias();
     }
 
     void setSmallFonts() {
         getTreeFontSet().smallFonts();
     }
 
     void setStartingAngle( final float starting_angle ) {
         _urt_starting_angle = starting_angle;
     }
 
     void setSuperTinyFonts() {
         getTreeFontSet().superTinyFonts();
     }
 
     void setTextAntialias() {
         if ( ( _phylogeny != null ) && !_phylogeny.isEmpty() ) {
             if ( _phylogeny.getNumberOfExternalNodes() <= LIMIT_FOR_HQ_RENDERING ) {
                 _rendering_hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
             }
             else {
                 _rendering_hints.put( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
             }
         }
         if ( getMainPanel().getOptions().isAntialiasScreen() ) {
             if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR ) {
                 _rendering_hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
             }
             else {
                 _rendering_hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
             }
             try {
                 _rendering_hints.put( RenderingHints.KEY_TEXT_ANTIALIASING,
                                       RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT );
             }
             catch ( final Throwable e ) {
                 _rendering_hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
             }
         }
         else {
             _rendering_hints.put( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
             _rendering_hints.put( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
         }
     }
 
     void setTinyFonts() {
         getTreeFontSet().tinyFonts();
     }
 
     /**
      * Set a phylogeny tree.
      * 
      * @param t
      *            an instance of a Phylogeny
      */
     void setTree( final Phylogeny t ) {
         _phylogeny = t;
     }
 
     void setTreeFile( final File treefile ) {
         _treefile = treefile;
     }
 
     void setWaitCursor() {
         setCursor( WAIT_CURSOR );
         repaint();
     }
 
     void setXcorrectionFactor( final float f ) {
         _x_correction_factor = f;
     }
 
     void setXdistance( final float x ) {
         _x_distance = x;
     }
 
     void setYdistance( final float y ) {
         _y_distance = y;
     }
 
     /**
      * Find a color for this species name.
      * 
      * @param species
      * @return the species color
      */
     Color speciesStringToColor( final PhylogenyNode node ) {
         if ( ( node == null ) || ( !node.getNodeData().isHasTaxonomy() ) ) {
             // return non-colorized color
             return getTreeColorSet().getTaxonomyColor();
         }
         return speciesStringToColor( node.getNodeData().getTaxonomy() );
     }
 
     Color speciesStringToColor( final Taxonomy tax ) {
         String species = tax.getTaxonomyCode();
         if ( ForesterUtil.isEmpty( species ) ) {
             species = tax.getScientificName();
             if ( ForesterUtil.isEmpty( species ) ) {
                 species = tax.getCommonName();
             }
         }
         if ( ForesterUtil.isEmpty( species ) ) {
             return getTreeColorSet().getTaxonomyColor();
         }
         // Look in species hash
         Color c = getControlPanel().getSpeciesColors().get( species );
         if ( c == null ) {
             c = Util.calculateColorFromString( species );
             getControlPanel().getSpeciesColors().put( species, c );
         }
         return c;
     }
     
     void subTree( final PhylogenyNode node ) {
     	//******************************************START CHANGED**********************************************************//
     	if(AppletParams.isEitherTPorTDForAll()){
     		newWindowSubtree.subTree(node, _phylogeny, subTreePanel);
         }
     	else{
     		if ( !node.isExternal() && !node.isRoot() && ( _subtree_index <= ( TreePanel.MAX_SUBTREES - 1 ) ) ) {
                 _phylogenies[ _subtree_index++ ] = _phylogeny;
                 _phylogeny = _phylogeny.subTree( node.getNodeId() );
                 updateSubSuperTreeButton();
             }
             else if ( node.isRoot() && ( _subtree_index >= 1 ) ) {
                 superTree();
             }
             _main_panel.getControlPanel().showWhole();
             repaint();
     	}
    // 	 ENTIRE METHOD of subTree(node) has been overwritten - changed
     	//********************************************END**********************************************************//
     }
     
     
     void superTree() {
     	//******************************************START CHANGED**********************************************************//
     	if(AppletParams.isEitherTPorTDForAll()){
     		newWindowSubtree.superTree();
     	}
     	//  ENTIRE METHOD of superTree() has been overwritten - changed
     	else{
 	    	_phylogenies[ _subtree_index ] = null;
 	        _phylogeny = _phylogenies[ --_subtree_index ];
 	        updateSubSuperTreeButton();
     	}
     
     	//********************************************END**********************************************************//
     }
     
     
   
     void swap( final PhylogenyNode node ) {
         if ( !node.isExternal() ) {
             _phylogeny.swapChildren( node );
         }
         repaint();
     }
 
     void taxColor() {
         if ( ( _phylogeny == null ) || ( _phylogeny.getNumberOfExternalNodes() < 2 ) ) {
             return;
         }
         setWaitCursor();
         Util.colorPhylogenyAccordingToExternalTaxonomy( _phylogeny, this );
         _control_panel.setColorBranches( true );
         if ( _control_panel.getColorBranchesCb() != null ) {
             _control_panel.getColorBranchesCb().setSelected( true );
         }
         setArrowCursor();
         repaint();
     }
 
     void updateOvSettings() {
         switch ( getOptions().getOvPlacement() ) {
             case LOWER_LEFT:
                 setOvXPosition( OV_BORDER );
                 setOvYPosition( ForesterUtil.roundToInt( getVisibleRect().height - OV_BORDER - getOvMaxHeight() ) );
                 setOvYStart( ForesterUtil.roundToInt( getOvYPosition() + ( getOvMaxHeight() / 2 ) ) );
                 break;
             case LOWER_RIGHT:
                 setOvXPosition( ForesterUtil.roundToInt( getVisibleRect().width - OV_BORDER - getOvMaxWidth() ) );
                 setOvYPosition( ForesterUtil.roundToInt( getVisibleRect().height - OV_BORDER - getOvMaxHeight() ) );
                 setOvYStart( ForesterUtil.roundToInt( getOvYPosition() + ( getOvMaxHeight() / 2 ) ) );
                 break;
             case UPPER_RIGHT:
                 setOvXPosition( ForesterUtil.roundToInt( getVisibleRect().width - OV_BORDER - getOvMaxWidth() ) );
                 setOvYPosition( OV_BORDER );
                 setOvYStart( ForesterUtil.roundToInt( OV_BORDER + ( getOvMaxHeight() / 2 ) ) );
                 break;
             default:
                 setOvXPosition( OV_BORDER );
                 setOvYPosition( OV_BORDER );
                 setOvYStart( ForesterUtil.roundToInt( OV_BORDER + ( getOvMaxHeight() / 2 ) ) );
                 break;
         }
     }
 
     void updateOvSizes() {
         if ( ( getWidth() > 1.05 * getVisibleRect().width ) || ( getHeight() > 1.05 * getVisibleRect().height ) ) {
             setOvOn( true );
             //            final float w_to_h = ( float ) getWidth() / getHeight();
             //            System.out.println( " w_to_h=" + w_to_h );
             //            float ov_h = getOvMaxWidth() / w_to_h;
             //            float ov_w = getOvMaxWidth();
             //            if ( ov_h > getOvMaxHeight() ) {
             //                ov_h = getOvMaxHeight();
             //                ov_w = getOvMaxHeight() * w_to_h;
             //            }
             //            setOvHeight( ( short ) ov_h );
             //            setOvWidth( ( short ) ov_w );
             //            System.out.println( "h=" + getOvHeight() );
             //            System.out.println( "w=" + getOvWidth() );
             float l = getLongestExtNodeInfo();
             final float w_ratio = getOvMaxWidth() / getWidth();
             l *= w_ratio;
             // final int ext_nodes = _phylogeny.getNumberOfExternalNodes();
             final int ext_nodes = _phylogeny.getRoot().getNumberOfExternalNodes();
             setOvYDistance( getOvMaxHeight() / ( 2 * ext_nodes ) );
             float ov_xdist = 0;
             if ( !isNonLinedUpCladogram() ) {
                 ov_xdist = ( ( getOvMaxWidth() - l ) / ( ext_nodes ) );
             }
             else {
                 ov_xdist = ( ( getOvMaxWidth() - l ) / ( PhylogenyMethods.calculateMaxDepth( _phylogeny ) ) );
             }
             float ydist = ( float ) ( ( getOvMaxWidth() / ( ext_nodes * 2.0 ) ) );
             if ( ov_xdist < 0.0 ) {
                 ov_xdist = 0.0f;
             }
             if ( ydist < 0.0 ) {
                 ydist = 0.0f;
             }
             setOvXDistance( ov_xdist );
             final double height = _phylogeny.getHeight();
             if ( height > 0 ) {
                 final float ov_corr = ( float ) ( ( ( getOvMaxWidth() - l ) - getOvXDistance() ) / height );
                 setOvXcorrectionFactor( ov_corr > 0 ? ov_corr : 0 );
             }
             else {
                 setOvXcorrectionFactor( 0 );
             }
         }
         else {
             setOvOn( false );
         }
     }
 
     //void updateStyle() {
     //  setPhylogenyGraphicsType( getOptions().getPhylogenyGraphicsType() );
     // }
     void updateSubSuperTreeButton() {
         if ( _subtree_index < 1 ) {
             getControlPanel().deactivateButtonToReturnToSuperTree();
         }
         else {
             getControlPanel().activateButtonToReturnToSuperTree( _subtree_index );
         }
     }
 
     void zoomInDomainStructure() {
         if ( _domain_structure_width < 2000 ) {
             _domain_structure_width *= 1.2;
         }
     }
 
     void zoomOutDomainStructure() {
         if ( _domain_structure_width > 20 ) {
             _domain_structure_width *= 0.8;
         }
     }
 
     private void assignGraphicsForBranchWithColorForParentBranch( final PhylogenyNode node,
                                                                   final boolean is_vertical,
                                                                   final Graphics g,
                                                                   final boolean to_pdf,
                                                                   final boolean to_graphics_file ) {
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else if ( getControlPanel().isColorBranches() && ( PhylogenyMethods.getBranchColorValue( node ) != null ) ) {
             g.setColor( PhylogenyMethods.getBranchColorValue( node ) );
         }
         else if ( to_pdf ) {
         	//******************************************START CHANGED**********************************************************//
         	if(AppletParams.isTreePrunerForAll() ){
         		treePrunerPaint.initArrayLists();
                 treePrunerPaint.paintKeepRemove(g,node);
             }
         	else if(AppletParams.isTreeDecoratorForAll() ){
         		treeDecoratorPaint.decorateBranch(g, node);      
         	}
         	else{
         		g.setColor( getTreeColorSet().getBranchColorForPdf() ); 
         	}
             //commented -  changed
            //********************************************END**********************************************************//
             
         }
         else {
         	//******************************************START CHANGED**********************************************************//
         	if(AppletParams.isTreePrunerForAll() ){
         		treePrunerPaint.initArrayLists();
                 treePrunerPaint.paintKeepRemove(g,node);
             }
         	else if(AppletParams.isTreeDecoratorForAll() ){
         		treeDecoratorPaint.decorateBranch(g, node);      
         	}
         	else{
         		g.setColor( getTreeColorSet().getBranchColor() ); 
         	}
             //commented -  changed
            //********************************************END**********************************************************//
         }
     }
 
     /**
      * Calculate the length of the distance between the given node and its
      * parent.
      * 
      * @param node
      * @param ext_node_x
      * @factor
      * @return the distance value
      */
     private float calculateBranchLengthToParent( final PhylogenyNode node, final int factor ) {
         if ( getControlPanel().isDrawPhylogram() ) {
             if ( node.getDistanceToParent() < 0.0 ) {
                 return 0.0f;
             }
             return ( float ) ( getXcorrectionFactor() * node.getDistanceToParent() );
         }
         else {
             if ( ( factor == 0 ) || isNonLinedUpCladogram() ) {
                 return getXdistance();
             }
             return getXdistance() * factor;
         }
     }
 
     private Color calculateColorForAnnotation( final PhylogenyData ann ) {
         Color c = getTreeColorSet().getAnnotationColor();
         if ( getControlPanel().isColorAccordingToAnnotation() && ( getControlPanel().getAnnotationColors() != null ) ) {
             c = getControlPanel().getAnnotationColors().get( ann.asSimpleText().toString() );
             if ( c == null ) {
                 c = getTreeColorSet().getAnnotationColor();
             }
         }
         return c;
     }
 
     private float calculateOvBranchLengthToParent( final PhylogenyNode node, final int factor ) {
         if ( getControlPanel().isDrawPhylogram() ) {
             if ( node.getDistanceToParent() < 0.0 ) {
                 return 0.0f;
             }
             return ( float ) ( getOvXcorrectionFactor() * node.getDistanceToParent() );
         }
         else {
             if ( ( factor == 0 ) || isNonLinedUpCladogram() ) {
                 return getOvXDistance();
             }
             return getOvXDistance() * factor;
         }
     }
 
     private void cannotOpenBrowserWarningMessage( final String type_type ) {
         JOptionPane.showMessageDialog( this,
                                        "Cannot launch web browser for " + type_type + " data of this node",
                                        "Cannot launch web browser",
                                        JOptionPane.WARNING_MESSAGE );
     }
 
     private void colorizeSubtree( final Color c, final PhylogenyNode node ) {
         _control_panel.setColorBranches( true );
         if ( _control_panel.getColorBranchesCb() != null ) {
             _control_panel.getColorBranchesCb().setSelected( true );
         }
         for( final PreorderTreeIterator it = new PreorderTreeIterator( node ); it.hasNext(); ) {
             it.next().getBranchData().setBranchColor( new BranchColor( c ) );
         }
         repaint();
     }
 
     private void colorSubtree( final PhylogenyNode node ) {
         Color intitial_color = null;
         if ( getControlPanel().isColorBranches() && ( PhylogenyMethods.getBranchColorValue( node ) != null )
                 && ( ( ( !node.isRoot() && ( node.getParent().getNumberOfDescendants() < 3 ) ) ) || ( node.isRoot() ) ) ) {
             intitial_color = PhylogenyMethods.getBranchColorValue( node );
         }
         else {
             intitial_color = getTreeColorSet().getBranchColor();
         }
         _color_chooser.setColor( intitial_color );
         _color_chooser.setPreviewPanel( new JPanel() );
         final JDialog dialog = JColorChooser
                 .createDialog( this,
                                "Subtree colorization",
                                true,
                                _color_chooser,
                                new SubtreeColorizationActionListener( _color_chooser, node ),
                                null );
         dialog.setVisible( true );
     }
 
     //TODO FIXME use me!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     private void createNodeDataString( final PhylogenyNode node ) {
         _sb.setLength( 0 );
         _sb.append( " " );
         if ( node.isCollapse() && ( ( !node.isRoot() && !node.getParent().isCollapse() ) || node.isRoot() ) ) {
             _sb.append( " [" );
             _sb.append( node.getAllExternalDescendants().size() );
             _sb.append( "] " );
         }
         if ( node.getNodeData().isHasTaxonomy()
                 && ( getControlPanel().isShowTaxonomyCode() || getControlPanel().isShowTaxonomyNames() ) ) {
             final Taxonomy taxonomy = node.getNodeData().getTaxonomy();
             if ( _control_panel.isShowTaxonomyCode() && !ForesterUtil.isEmpty( taxonomy.getTaxonomyCode() ) ) {
                 _sb.append( taxonomy.getTaxonomyCode() );
                 _sb.append( " " );
             }
             if ( _control_panel.isShowTaxonomyNames() ) {
                 if ( !ForesterUtil.isEmpty( taxonomy.getScientificName() )
                         && !ForesterUtil.isEmpty( taxonomy.getCommonName() ) ) {
                     _sb.append( taxonomy.getScientificName() );
                     _sb.append( " (" );
                     _sb.append( taxonomy.getCommonName() );
                     _sb.append( ") " );
                 }
                 else if ( !ForesterUtil.isEmpty( taxonomy.getScientificName() ) ) {
                     _sb.append( taxonomy.getScientificName() );
                     _sb.append( " " );
                 }
                 else if ( !ForesterUtil.isEmpty( taxonomy.getCommonName() ) ) {
                     _sb.append( taxonomy.getCommonName() );
                     _sb.append( " " );
                 }
             }
         }
         if ( getControlPanel().isShowNodeNames() && ( node.getNodeName().length() > 0 ) ) {
             _sb.append( node.getNodeName() );
             _sb.append( " " );
         }
         if ( node.getNodeData().isHasSequence() ) {
             if ( getControlPanel().isShowSequenceAcc() && ( node.getNodeData().getSequence().getAccession() != null ) ) {
                 if ( !ForesterUtil.isEmpty( node.getNodeData().getSequence().getAccession().getSource() ) ) {
                     _sb.append( node.getNodeData().getSequence().getAccession().getSource() );
                     _sb.append( ":" );
                 }
                 _sb.append( node.getNodeData().getSequence().getAccession().getValue() );
                 _sb.append( " " );
             }
             if ( getControlPanel().isShowGeneNames() && ( node.getNodeData().getSequence().getName().length() > 0 ) ) {
                 _sb.append( node.getNodeData().getSequence().getName() );
                 _sb.append( " " );
             }
         }
         if ( getControlPanel().isShowDistribution() && node.getNodeData().isHasDistribution() ) {
             _sb.append( node.getNodeData().getDistribution().asText() );
             _sb.append( " " );
         }
         if ( getControlPanel().isShowDate() && node.getNodeData().isHasDate() ) {
             _sb.append( node.getNodeData().getDate().asText() );
             _sb.append( " " );
         }
         if ( getControlPanel().isShowDate() && node.getNodeData().isHasProperties() ) {
             _sb.append( node.getNodeData().getProperties().asSimpleText() );
             _sb.append( " " );
         }
     }
 
     private void decreaseOvSize() {
         if ( ( getOvMaxWidth() > 20 ) && ( getOvMaxHeight() > 20 ) ) {
             setOvMaxWidth( getOvMaxWidth() - 5 );
             setOvMaxHeight( getOvMaxHeight() - 5 );
             updateOvSettings();
             getControlPanel().displayedPhylogenyMightHaveChanged( false );
         }
     }
 
     private void displayNodePopupMenu( final PhylogenyNode node, final int x, final int y ) {
         // If menu doesn't exist, create it
         if ( _node_popup_menu == null ) {
             makePopupMenus();
         }
         _node_popup_menu.putClientProperty( NODE_POPMENU_NODE_CLIENT_PROPERTY, node );
         try {
             if ( isCanOpenSeqWeb( node ) ) {
                 ( _node_popup_menu_items[ Configuration.open_seq_web ] ).setEnabled( true );
             }
             else {
                 ( _node_popup_menu_items[ Configuration.open_seq_web ] ).setEnabled( false );
             }
             if ( isCanOpenTaxWeb( node ) ) {
                 ( _node_popup_menu_items[ Configuration.open_tax_web ] ).setEnabled( true );
             }
             else {
                 ( _node_popup_menu_items[ Configuration.open_tax_web ] ).setEnabled( false );
             }
         }
         catch ( final ArrayIndexOutOfBoundsException e ) {
             // Do nothing.
         }
         _node_popup_menu.show( this, x, y );
     }
 
     private void drawArc( final double x,
                           final double y,
                           final double width,
                           final double heigth,
                           final double start_angle,
                           final double arc_angle,
                           final Graphics2D g ) {
         //TODO FIXME:
         // if ( Math.abs( arc_angle ) < 0.0005 ) {
         //     return;
         // }
         _arc.setArc( x, y, width, heigth, _180_OVER_PI * start_angle, _180_OVER_PI * arc_angle, Arc2D.OPEN );
         g.draw( _arc );
     }
 
     private float getLastDragPointX() {
         return _last_drag_point_x;
     }
 
     private float getLastDragPointY() {
         return _last_drag_point_y;
     }
 
     private float getOvMaxHeight() {
         return _ov_max_height;
     }
 
     // private float getOvWidth() {
     //     return _ov_width;
     // }
     // private void setOvWidth( float ov_width ) {
     //     _ov_width = ov_width;
     // }
     // private float getOvHeight() {
     //     return _ov_height;
     // }
     //  private void setOvHeight( float ov_height ) {
     //     _ov_height = ov_height;
     //  }
     private float getOvMaxWidth() {
         return _ov_max_width;
     }
 
     private float getOvXcorrectionFactor() {
         return _ov_x_correction_factor;
     }
 
     private float getOvXDistance() {
         return _ov_x_distance;
     }
 
     private int getOvXPosition() {
         return _ov_x_position;
     }
 
     private float getOvYDistance() {
         return _ov_y_distance;
     }
 
     private int getOvYPosition() {
         return _ov_y_position;
     }
 
     private int getOvYStart() {
         return _ov_y_start;
     }
 
     private double getScaleDistance() {
         return _scale_distance;
     }
 
     private String getScaleLabel() {
         return _scale_label;
     }
 
     private TreeFontSet getTreeFontSet() {
         return getMainPanel().getTreeFontSet();
     }
 
     private float getUrtFactor() {
         return _urt_factor;
     }
 
     private void handleClickToAction( final NodeClickAction action, final PhylogenyNode node ) {
         switch ( action ) {
             case SHOW_DATA:
                 showNodeFrame( node );
                 break;
             case COLLAPSE:
                 collapse( node );
                 break;
             case REROOT:
                 reRoot( node );
                 break;
             case SUBTREE:
                 subTree( node );
                 break;
             case SWAP:
                 swap( node );
                 break;
             case COLOR_SUBTREE:
                 colorSubtree( node );
                 break;
             case OPEN_SEQ_WEB:
                 openSeqWeb( node );
                 break;
             case OPEN_TAX_WEB:
                 openTaxWeb( node );
                 break;
           //******************************************START**********************************************************//
             case KEEP_SEQUENCES:
             	workingSet.memorizeKeepNodes(node, this);
             	break;
             case REMOVE_SEQUENCES:	
             	workingSet.memorizeRemoveNodes(node);
             	break;
            //********************************************END**********************************************************//
             default:
                 throw new IllegalArgumentException( "unknown action: " + action );
         }
     }
 
     private void increaseOvSize() {
         if ( ( getOvMaxWidth() < getMainPanel().getCurrentScrollPane().getViewport().getVisibleRect().getWidth() / 2 )
                 && ( getOvMaxHeight() < getMainPanel().getCurrentScrollPane().getViewport().getVisibleRect()
                         .getHeight() / 2 ) ) {
             setOvMaxWidth( getOvMaxWidth() + 5 );
             setOvMaxHeight( getOvMaxHeight() + 5 );
             updateOvSettings();
             getControlPanel().displayedPhylogenyMightHaveChanged( false );
         }
     }
 
     private void init() {
         _color_chooser = new JColorChooser();
         setTextAntialias();
         setTreeFile( null );
         initializeOvSettings();
         setStartingAngle( TWO_PI * 3 / 4 );
     }
 
     private void initializeOvSettings() {
         setOvMaxHeight( getConfiguration().getOvMaxHeight() );
         setOvMaxWidth( getConfiguration().getOvMaxWidth() );
     }
 
     private boolean inOvVirtualRectangle( final int x, final int y ) {
         return ( ( x >= getOvVirtualRectangle().x - 1 )
                 && ( x <= getOvVirtualRectangle().x + getOvVirtualRectangle().width + 1 )
                 && ( y >= getOvVirtualRectangle().y - 1 ) && ( y <= getOvVirtualRectangle().y
                 + getOvVirtualRectangle().height + 1 ) );
     }
 
     private boolean inOvVirtualRectangle( final MouseEvent e ) {
         return ( inOvVirtualRectangle( e.getX(), e.getY() ) );
     }
 
     private boolean isApplet() {
         return getMainPanel() instanceof MainPanelApplets;
     }
 
     private boolean isCanOpenSeqWeb( final PhylogenyNode node ) {
         if ( node.getNodeData().isHasSequence()
                 && ( node.getNodeData().getSequence().getAccession() != null )
                 && !ForesterUtil.isEmpty( node.getNodeData().getSequence().getAccession().getSource() )
                 && !ForesterUtil.isEmpty( node.getNodeData().getSequence().getAccession().getValue() )
                 && getConfiguration().isHasWebLink( node.getNodeData().getSequence().getAccession().getSource()
                         .toLowerCase() ) ) {
             return true;
         }
         return false;
     }
 
     private boolean isCanOpenTaxWeb( final PhylogenyNode node ) {
         if ( node.getNodeData().isHasTaxonomy()
                 && ( ( ( node.getNodeData().getTaxonomy().getIdentifier() != null )
                         && !ForesterUtil.isEmpty( node.getNodeData().getTaxonomy().getIdentifier().getType() )
                         && !ForesterUtil.isEmpty( node.getNodeData().getTaxonomy().getIdentifier().getValue() ) && getConfiguration()
                         .isHasWebLink( node.getNodeData().getTaxonomy().getIdentifier().getType().toLowerCase() ) )
                         || ( !ForesterUtil.isEmpty( node.getNodeData().getTaxonomy().getScientificName() ) )
                         || ( !ForesterUtil.isEmpty( node.getNodeData().getTaxonomy().getTaxonomyCode() ) ) || ( !ForesterUtil
                         .isEmpty( node.getNodeData().getTaxonomy().getCommonName() ) ) ) ) {
             return true;
         }
         else {
             return false;
         }
     }
 
     private boolean isInFoundNodes( final PhylogenyNode node ) {
         return ( ( getFoundNodes() != null ) && getFoundNodes().contains( node ) );
     }
 
     private boolean isInOv() {
         return _in_ov;
     }
 
     private boolean isNodeDataInvisible( final PhylogenyNode node ) {
         return ( ( node.getYcoord() < getVisibleRect().getMinY() - 40 )
                 || ( node.getYcoord() > getVisibleRect().getMaxY() + 40 ) || ( ( node.getParent() != null ) && ( node
                 .getParent().getXcoord() > getVisibleRect().getMaxX() ) ) );
     }
 
     private boolean isNonLinedUpCladogram() {
         return getOptions().isNonLinedUpCladogram();
     }
 
     private void keyPressedCalls( final KeyEvent e ) {
         if ( isOvOn() && ( getMousePosition() != null ) && ( getMousePosition().getLocation() != null ) ) {
             if ( inOvVirtualRectangle( getMousePosition().x, getMousePosition().y ) ) {
                 if ( !isInOvRect() ) {
                     setInOvRect( true );
                 }
             }
             else if ( isInOvRect() ) {
                 setInOvRect( false );
             }
         }
         if ( e.getModifiersEx() == InputEvent.CTRL_DOWN_MASK ) {
             if ( ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) || ( e.getKeyCode() == KeyEvent.VK_HOME ) ) {
                 getMainPanel().getTreeFontSet().mediumFonts();
                 getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( true );
             }
             else if ( ( e.getKeyCode() == KeyEvent.VK_SUBTRACT ) || ( e.getKeyCode() == KeyEvent.VK_MINUS ) ) {
                 getMainPanel().getTreeFontSet().decreaseFontSize();
                 getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( true );
             }
             else if ( plusPressed( e.getKeyCode() ) ) {
                 getMainPanel().getTreeFontSet().increaseFontSize();
                 getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( true );
             }
         }
         else {
             if ( ( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) || ( e.getKeyCode() == KeyEvent.VK_HOME ) ) {
                 getControlPanel().showWhole();
             }
             else if ( ( e.getKeyCode() == KeyEvent.VK_UP ) || ( e.getKeyCode() == KeyEvent.VK_DOWN )
                     || ( e.getKeyCode() == KeyEvent.VK_LEFT ) || ( e.getKeyCode() == KeyEvent.VK_RIGHT ) ) {
                 if ( e.getModifiersEx() == InputEvent.SHIFT_DOWN_MASK ) {
                     if ( e.getKeyCode() == KeyEvent.VK_UP ) {
                         getMainPanel().getControlPanel().zoomInY( Constants.WHEEL_ZOOM_IN_FACTOR );
                         getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                     else if ( e.getKeyCode() == KeyEvent.VK_DOWN ) {
                         getMainPanel().getControlPanel().zoomOutY( Constants.WHEEL_ZOOM_OUT_FACTOR );
                         getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                     else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) {
                         getMainPanel().getControlPanel().zoomOutX( Constants.WHEEL_ZOOM_OUT_FACTOR,
                                                                    Constants.WHEEL_ZOOM_OUT_X_CORRECTION_FACTOR );
                         getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                     else if ( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
                         getMainPanel().getControlPanel().zoomInX( Constants.WHEEL_ZOOM_IN_FACTOR,
                                                                   Constants.WHEEL_ZOOM_IN_FACTOR );
                         getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( false );
                     }
                 }
                 else {
                     final int d = 80;
                     int dx = 0;
                     int dy = -d;
                     if ( e.getKeyCode() == KeyEvent.VK_DOWN ) {
                         dy = d;
                     }
                     else if ( e.getKeyCode() == KeyEvent.VK_LEFT ) {
                         dx = -d;
                         dy = 0;
                     }
                     else if ( e.getKeyCode() == KeyEvent.VK_RIGHT ) {
                         dx = d;
                         dy = 0;
                     }
                     final Point scroll_position = getMainPanel().getCurrentScrollPane().getViewport().getViewPosition();
                     scroll_position.x = scroll_position.x + dx;
                     scroll_position.y = scroll_position.y + dy;
                     if ( scroll_position.x <= 0 ) {
                         scroll_position.x = 0;
                     }
                     else {
                         final int max_x = getMainPanel().getCurrentScrollPane().getHorizontalScrollBar().getMaximum()
                                 - getMainPanel().getCurrentScrollPane().getHorizontalScrollBar().getVisibleAmount();
                         if ( scroll_position.x >= max_x ) {
                             scroll_position.x = max_x;
                         }
                     }
                     if ( scroll_position.y <= 0 ) {
                         scroll_position.y = 0;
                     }
                     else {
                         final int max_y = getMainPanel().getCurrentScrollPane().getVerticalScrollBar().getMaximum()
                                 - getMainPanel().getCurrentScrollPane().getVerticalScrollBar().getVisibleAmount();
                         if ( scroll_position.y >= max_y ) {
                             scroll_position.y = max_y;
                         }
                     }
                     repaint();
                     getMainPanel().getCurrentScrollPane().getViewport().setViewPosition( scroll_position );
                 }
             }
             else if ( ( e.getKeyCode() == KeyEvent.VK_SUBTRACT ) || ( e.getKeyCode() == KeyEvent.VK_MINUS ) ) {
                 getMainPanel().getControlPanel().zoomOutY( Constants.WHEEL_ZOOM_OUT_FACTOR );
                 getMainPanel().getControlPanel().zoomOutX( Constants.WHEEL_ZOOM_OUT_FACTOR,
                                                            Constants.WHEEL_ZOOM_OUT_X_CORRECTION_FACTOR );
                 getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( false );
             }
             else if ( plusPressed( e.getKeyCode() ) ) {
                 getMainPanel().getControlPanel().zoomInX( Constants.WHEEL_ZOOM_IN_FACTOR,
                                                           Constants.WHEEL_ZOOM_IN_FACTOR );
                 getMainPanel().getControlPanel().zoomInY( Constants.WHEEL_ZOOM_IN_FACTOR );
                 getMainPanel().getControlPanel().displayedPhylogenyMightHaveChanged( false );
             }
             else if ( e.getKeyCode() == KeyEvent.VK_S ) {
                 if ( ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.UNROOTED )
                         || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CIRCULAR ) ) {
                     setStartingAngle( ( getStartingAngle() % TWO_PI ) + ANGLE_ROTATION_UNIT );
                     getControlPanel().displayedPhylogenyMightHaveChanged( false );
                 }
             }
             else if ( e.getKeyCode() == KeyEvent.VK_A ) {
                 if ( ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.UNROOTED )
                         || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CIRCULAR ) ) {
                     setStartingAngle( ( getStartingAngle() % TWO_PI ) - ANGLE_ROTATION_UNIT );
                     if ( getStartingAngle() < 0 ) {
                         setStartingAngle( TWO_PI + getStartingAngle() );
                     }
                     getControlPanel().displayedPhylogenyMightHaveChanged( false );
                 }
             }
             else if ( e.getKeyCode() == KeyEvent.VK_D ) {
                 if ( getOptions().getNodeLabelDirection() == NODE_LABEL_DIRECTION.HORIZONTAL ) {
                     getOptions().setNodeLabelDirection( NODE_LABEL_DIRECTION.RADIAL );
                     getMainPanel().getMainFrame()._label_direction_cbmi.setSelected( true );
                 }
                 else {
                     getOptions().setNodeLabelDirection( NODE_LABEL_DIRECTION.HORIZONTAL );
                     getMainPanel().getMainFrame()._label_direction_cbmi.setSelected( false );
                 }
                 repaint();
             }
             else if ( getOptions().isShowOverview() && isOvOn() && ( e.getKeyCode() == KeyEvent.VK_O ) ) {
                 MainFrame.cycleOverview( getOptions(), this );
                 repaint();
             }
             else if ( getOptions().isShowOverview() && isOvOn() && ( e.getKeyCode() == KeyEvent.VK_I ) ) {
                 increaseOvSize();
             }
             else if ( getOptions().isShowOverview() && isOvOn() && ( e.getKeyCode() == KeyEvent.VK_U ) ) {
                 decreaseOvSize();
             }
             e.consume();
         }
     }
 
     private void makePopupMenus() {
         _node_popup_menu = new JPopupMenu();
         final List<String> clickto_names = _main_panel.getControlPanel().getSingleClickToNames();
         _node_popup_menu_items = new JMenuItem[ clickto_names.size() ];
         for( int i = 0; i < clickto_names.size(); i++ ) {
             final String title = clickto_names.get( i );
             _node_popup_menu_items[ i ] = new JMenuItem( title );
             _node_popup_menu_items[ i ].addActionListener( this );
             _node_popup_menu.add( _node_popup_menu_items[ i ] );
         }
     }
 
     private JApplet obtainApplet() {
         return ( ( MainPanelApplets ) getMainPanel() ).getApplet();
     }
 
     private void openSeqWeb( final PhylogenyNode node ) {
         if ( !isCanOpenSeqWeb( node ) ) {
             cannotOpenBrowserWarningMessage( "sequence" );
             return;
         }
         String uri_str = null;
         final Sequence seq = node.getNodeData().getSequence();
         final String source = seq.getAccession().getSource().toLowerCase();
         final WebLink weblink = getConfiguration().getWebLink( source );
         try {
             uri_str = weblink.getUrl() + URLEncoder.encode( seq.getAccession().getValue(), ForesterConstants.UTF8 );
         }
         catch ( final UnsupportedEncodingException e ) {
             Util.showErrorMessage( this, e.toString() );
             e.printStackTrace();
         }
         if ( !ForesterUtil.isEmpty( uri_str ) ) {
             try {
                 JApplet applet = null;
                 if ( isApplet() ) {
                     applet = obtainApplet();
                 }
                 Util.launchWebBrowser( new URI( uri_str ), isApplet(), applet, "_atv_seq" );
             }
             catch ( final IOException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
             catch ( final URISyntaxException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
         }
         else {
             cannotOpenBrowserWarningMessage( "sequence" );
         }
     }
 
     private void openTaxWeb( final PhylogenyNode node ) {
         if ( !isCanOpenTaxWeb( node ) ) {
             cannotOpenBrowserWarningMessage( "taxonomic" );
             return;
         }
         String uri_str = null;
         final Taxonomy tax = node.getNodeData().getTaxonomy();
         if ( ( tax.getIdentifier() != null ) && !ForesterUtil.isEmpty( tax.getIdentifier().getType() ) ) {
             final String type = tax.getIdentifier().getType().toLowerCase();
             if ( getConfiguration().isHasWebLink( type ) ) {
                 final WebLink weblink = getConfiguration().getWebLink( type );
                 try {
                     uri_str = weblink.getUrl()
                             + URLEncoder.encode( tax.getIdentifier().getValue(), ForesterConstants.UTF8 );
                 }
                 catch ( final UnsupportedEncodingException e ) {
                     Util.showErrorMessage( this, e.toString() );
                     e.printStackTrace();
                 }
             }
         }
         else if ( !ForesterUtil.isEmpty( tax.getScientificName() ) ) {
             try {
                 uri_str = "http://www.eol.org/search?q="
                         + URLEncoder.encode( tax.getScientificName(), ForesterConstants.UTF8 );
             }
             catch ( final UnsupportedEncodingException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
         }
         else if ( !ForesterUtil.isEmpty( tax.getTaxonomyCode() ) ) {
             try {
                 uri_str = "http://www.uniprot.org/taxonomy/?query="
                         + URLEncoder.encode( tax.getTaxonomyCode(), ForesterConstants.UTF8 );
             }
             catch ( final UnsupportedEncodingException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
         }
         else if ( !ForesterUtil.isEmpty( tax.getCommonName() ) ) {
             try {
                 uri_str = "http://www.eol.org/search?q="
                         + URLEncoder.encode( tax.getCommonName(), ForesterConstants.UTF8 );
             }
             catch ( final UnsupportedEncodingException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
         }
         if ( !ForesterUtil.isEmpty( uri_str ) ) {
             try {
                 JApplet applet = null;
                 if ( isApplet() ) {
                     applet = obtainApplet();
                 }
                 Util.launchWebBrowser( new URI( uri_str ), isApplet(), applet, "_atv_tax" );
             }
             catch ( final IOException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
             catch ( final URISyntaxException e ) {
                 Util.showErrorMessage( this, e.toString() );
                 e.printStackTrace();
             }
         }
         else {
             cannotOpenBrowserWarningMessage( "taxonomic" );
         }
     }
 
     /**
      * Paint a branch which consists of a vertical and a horizontal bar
      * @param is_ind_found_nodes 
      */
     private void paintBranch( final Graphics g,
                               final float x1,
                               final float x2,
                               float y1,
                               final float y2,
                               final PhylogenyNode node,
                               final boolean to_pdf,
                               final boolean to_graphics_file ) {
         assignGraphicsForBranchWithColorForParentBranch( node, false, g, to_pdf, to_graphics_file );
         if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.TRIANGULAR ) {
             TreePanel.drawLine( x1, y1, x2, y2, g );
         }
         else if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CONVEX ) {
             _quad_curve.setCurve( x1, y1, x1, y2, x2, y2 );
             ( ( Graphics2D ) g ).draw( _quad_curve );
         }
         else if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CURVED ) {
             final float dx = x2 - x1;
             final float dy = y2 - y1;
             _cubic_curve.setCurve( x1, y1, x1 + ( dx * 0.4f ), y1 + ( dy * 0.2f ), x1 + ( dx * 0.6f ), y1
                     + ( dy * 0.8f ), x2, y2 );
             ( ( Graphics2D ) g ).draw( _cubic_curve );
         }
         else {
             float x2a = x2;
             float x1a = x1;
             // draw the vertical line
             boolean draw_horizontal = true;
             if ( node.isFirstChildNode() || node.isLastChildNode()
                     || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) ) {
                 boolean draw_vertical = true;
                 final PhylogenyNode parent = node.getParent();
                 if ( ( ( getOptions().isShowNodeBoxes() && !to_pdf && !to_graphics_file ) || ( ( getControlPanel()
                         .isEvents() )
                         && ( parent != null ) && parent.isHasAssignedEvent() ) )
                         && ( _phylogeny.isRooted() || !( ( parent != null ) && parent.isRoot() ) )
                         && !( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() && !parent
                                 .isDuplication() ) ) {
                     if ( getPhylogenyGraphicsType() != PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) {
                         if ( Math.abs( y2 - y1 ) <= TreePanel.HALF_BOX_SIZE ) {
                             draw_vertical = false;
                         }
                         else {
                             if ( y1 < y2 ) {
                                 y1 += TreePanel.HALF_BOX_SIZE;
                             }
                             else {
                                 if ( !to_pdf ) {
                                     y1 -= TreePanel.HALF_BOX_SIZE + 1;
                                 }
                                 else {
                                     y1 -= TreePanel.HALF_BOX_SIZE;
                                 }
                             }
                         }
                     }
                     if ( ( x2 - x1 ) <= TreePanel.HALF_BOX_SIZE ) {
                         draw_horizontal = false;
                     }
                     else if ( !draw_vertical ) {
                         x1a += TreePanel.HALF_BOX_SIZE;
                     }
                     if ( ( ( x2 - x1a ) > TreePanel.HALF_BOX_SIZE )
                             && !( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() && !node
                                     .isDuplication() ) ) {
                         x2a -= TreePanel.HALF_BOX_SIZE;
                     }
                 }
                 if ( draw_vertical ) {
                     if ( !to_graphics_file
                             && !to_pdf
                             && ( ( ( y2 < getVisibleRect().getMinY() - 20 ) && ( y1 < getVisibleRect().getMinY() - 20 ) ) || ( ( y2 > getVisibleRect()
                                     .getMaxY() + 20 ) && ( y1 > getVisibleRect().getMaxY() + 20 ) ) ) ) {
                         // Do nothing.
                     }
                     else {
                         if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) {
                             float x2c = x1 + EURO_D;
                             if ( x2c > x2a ) {
                                 x2c = x2a;
                             }
                             TreePanel.drawLine( x1, y1, x2c, y2, g );
                         }
                         else {
                             TreePanel.drawLine( x1, y1, x1, y2, g );
                         }
                     }
                 }
             }
             // draw the horizontal line
             if ( !to_graphics_file && !to_pdf
                     && ( ( y2 < getVisibleRect().getMinY() - 20 ) || ( y2 > getVisibleRect().getMaxY() + 20 ) ) ) {
                 return;
             }
             if ( draw_horizontal ) {
                 if ( !getControlPanel().isWidthBranches() || ( PhylogenyMethods.getBranchWidthValue( node ) == 1 ) ) {
                     if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) {
                         final float x1c = x1a + EURO_D;
                         if ( x1c < x2a ) {
                             TreePanel.drawLine( x1c, y2, x2a, y2, g );
                         }
                     }
                     else {
                     	//******************************************START CHANGED**********************************************************//
                     	if(AppletParams.isTreePrunerForAll() ){
                     		treePrunerPaint.drawThickLine(g,node, x1a, y2, x2a, y2);
                     	}
                     	else{
                     		TreePanel.drawLine( x1a, y2, x2a, y2, g );
                     	}
                          //Commented changed
                         //********************************************END**********************************************************//
                         
                     }
                 }
                 else {
                     final double w = PhylogenyMethods.getBranchWidthValue( node );
                     if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) {
                         final float x1c = x1a + EURO_D;
                         if ( x1c < x2a ) {
                             TreePanel.fillRect( x1c, y2 - ( w / 2 ), x2a - x1c, w, g );
                         }
                     }
                     else {
                         TreePanel.fillRect( x1a, y2 - ( w / 2 ), x2a - x1a, w, g );
                     }
                 }
             }
         }
         paintNodeBox( x2, y2, node, g, to_pdf, to_graphics_file, isInFoundNodes( node ) );
     }
 
     private void paintBranchLength( final Graphics g,
                                     final PhylogenyNode node,
                                     final boolean to_pdf,
                                     final boolean to_graphics_file ) {
         g.setFont( getTreeFontSet().getSmallFont() );
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else {
             g.setColor( getTreeColorSet().getBranchLengthColor() );
         }
         if ( !node.isRoot() ) {
             if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) {
                 TreePanel.drawString( FORMATTER_3.format( ForesterUtil.round( node.getDistanceToParent(), 3 ) ), node
                         .getParent().getXcoord()
                         + EURO_D, node.getYcoord() - getTreeFontSet()._small_max_descent, g );
             }
             else {
                 TreePanel.drawString( FORMATTER_3.format( ForesterUtil.round( node.getDistanceToParent(), 3 ) ), node
                         .getParent().getXcoord() + 3, node.getYcoord() - getTreeFontSet()._small_max_descent, g );
             }
         }
         else {
             TreePanel.drawString( FORMATTER_3.format( ForesterUtil.round( node.getDistanceToParent(), 3 ) ), 3, node
                     .getYcoord()
                     - getTreeFontSet()._small_max_descent, g );
         }
     }
 
     private void paintBranchLite( final Graphics g,
                                   final float x1,
                                   final float x2,
                                   final float y1,
                                   final float y2,
                                   final PhylogenyNode node ) {
         g.setColor( getTreeColorSet().getOvColor() );
         if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.TRIANGULAR ) {
             TreePanel.drawLine( x1, y1, x2, y2, g );
         }
         else if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CONVEX ) {
             _quad_curve.setCurve( x1, y1, x1, y2, x2, y2 );
             ( ( Graphics2D ) g ).draw( _quad_curve );
         }
         else if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.CURVED ) {
             final float dx = x2 - x1;
             final float dy = y2 - y1;
             _cubic_curve.setCurve( x1, y1, x1 + ( dx * 0.4f ), y1 + ( dy * 0.2f ), x1 + ( dx * 0.6f ), y1
                     + ( dy * 0.8f ), x2, y2 );
             ( ( Graphics2D ) g ).draw( _cubic_curve );
         }
         else {
             final float x2a = x2;
             final float x1a = x1;
             // draw the vertical line
             if ( node.isFirstChildNode() || node.isLastChildNode() ) {
                 TreePanel.drawLine( x1, y1, x1, y2, g );
             }
             // draw the horizontal line
             TreePanel.drawLine( x1a, y2, x2a, y2, g );
         }
     }
 
     private double paintCirculars( final PhylogenyNode n,
                                    final Phylogeny phy,
                                    final float center_x,
                                    final float center_y,
                                    final double radius,
                                    final boolean radial_labels,
                                    final Graphics2D g,
                                    final boolean to_pdf,
                                    final boolean to_graphics_file ) {
         if ( n.isExternal() ) {
             return _urt_nodeid_angle_map.get( n.getNodeId() );
         }
         else {
             final List<PhylogenyNode> descs = n.getDescendants();
             double sum = 0;
             for( final PhylogenyNode desc : descs ) {
                 sum += paintCirculars( desc,
                                        phy,
                                        center_x,
                                        center_y,
                                        radius,
                                        radial_labels,
                                        g,
                                        to_pdf,
                                        to_graphics_file );
             }
             float r = 0;
             if ( !n.isRoot() ) {
                 r = 1 - ( ( ( float ) _circ_max_depth - PhylogenyMethods.calculateDepth( n ) ) / _circ_max_depth );
             }
             final double theta = sum / descs.size();
             n.setXcoord( center_x + ( float ) ( radius * Math.cos( theta ) * r ) );
             n.setYcoord( center_y + ( float ) ( radius * Math.sin( theta ) * r ) );
             _urt_nodeid_angle_map.put( n.getNodeId(), theta );
             for( final PhylogenyNode desc : descs ) {
                 paintBranchCircular( n, desc, g, radial_labels, to_pdf, to_graphics_file );
             }
             return theta;
         }
     }
 
     private void paintCollapsedNode( final Graphics g,
                                      final PhylogenyNode node,
                                      final boolean to_graphics_file,
                                      final boolean to_pdf,
                                      final boolean is_in_found_nodes ) {
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else if ( is_in_found_nodes ) {
             g.setColor( getTreeColorSet().getFoundColor() );
         }
         else if ( getControlPanel().isColorAccordingToTaxonomy() ) {
             g.setColor( speciesStringToColor( node ) );
         }
         else {
             g.setColor( getTreeColorSet().getCollapseFillColor() );
         }
         double d = node.getAllExternalDescendants().size();
         if ( d > 1000 ) {
             d = ( 3 * _y_distance ) / 3;
         }
         else {
             d = ( Math.log10( d ) * _y_distance ) / 2.5;
         }
         if ( d < BOX_SIZE ) {
             d = BOX_SIZE;
         }
         _polygon.reset();
         _polygon.addPoint( ForesterUtil.roundToInt( node.getXcoord() - TreePanel.BOX_SIZE ), ForesterUtil
                 .roundToInt( node.getYcoord() ) );
         _polygon.addPoint( ForesterUtil.roundToInt( node.getXcoord() + TreePanel.BOX_SIZE ), ForesterUtil
                 .roundToInt( node.getYcoord() - d ) );
         _polygon.addPoint( ForesterUtil.roundToInt( node.getXcoord() + TreePanel.BOX_SIZE ), ForesterUtil
                 .roundToInt( node.getYcoord() + d ) );
         g.fillPolygon( _polygon );
         paintNodeData( g, node, to_graphics_file, to_pdf, is_in_found_nodes );
     }
 
     private void paintFoundNode( final int x, final int y, final Graphics g ) {
         g.setColor( getTreeColorSet().getFoundColor() );
         g.fillRect( x - TreePanel.HALF_BOX_SIZE, y - TreePanel.HALF_BOX_SIZE, TreePanel.BOX_SIZE, TreePanel.BOX_SIZE );
     }
 
     private void paintGainedAndLostCharacters( final Graphics g,
                                                final PhylogenyNode node,
                                                final String gained,
                                                final String lost ) {
         if ( node.getParent() != null ) {
             final double parent_x = node.getParent().getXcoord();
             final double x = node.getXcoord();
             g.setFont( getTreeFontSet().getLargeFont() );
             g.setColor( getTreeColorSet().getGainedCharactersColor() );
             if ( Constants.SPECIAL_CUSTOM ) {
                 g.setColor( Color.BLUE );
             }
             TreePanel
                     .drawString( gained, parent_x
                             + ( ( x - parent_x - getTreeFontSet()._fm_large.stringWidth( gained ) ) / 2 ), ( node
                             .getYcoord() - getTreeFontSet()._fm_large.getMaxDescent() ) - 1, g );
             g.setColor( getTreeColorSet().getLostCharactersColor() );
             TreePanel.drawString( lost,
                                   parent_x + ( ( x - parent_x - getTreeFontSet()._fm_large.stringWidth( lost ) ) / 2 ),
                                   ( node.getYcoord() + getTreeFontSet()._fm_large.getMaxAscent() ) + 1,
                                   g );
         }
     }
 
     private void paintNode( final Graphics g,			
                             final PhylogenyNode node,
                             final boolean to_pdf,
                             final boolean dynamically_hide,
                             final int dynamic_hiding_factor,
                             final boolean to_graphics_file ) {
         final boolean is_in_found_nodes = isInFoundNodes( node );
         if ( node.isCollapse() ) {
             if ( ( !node.isRoot() && !node.getParent().isCollapse() ) || node.isRoot() ) {
                 paintCollapsedNode( g, node, to_graphics_file, to_pdf, is_in_found_nodes );
             }
             return;
         }
         if ( node.isExternal() ) {
             ++_external_node_index;
         }
         // Confidence values
         if ( getControlPanel().isShowBootstrapValues()
                 && !node.isExternal()
                 && !node.isRoot()
                 && ( PhylogenyMethods.getConfidenceValue( node ) != Confidence.CONFIDENCE_DEFAULT_VALUE )
                 && ( PhylogenyMethods.getConfidenceValue( node ) >= getOptions().getMinConfidenceValue() )
                 && ( ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR ) || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) ) ) {
             paintSupportValue( g, node, to_pdf, to_graphics_file );
         }
         // Draw a line to root:
         if ( node.isRoot() && _phylogeny.isRooted() ) {
             paintRootBranch( g, node.getXcoord(), node.getYcoord(), node, to_pdf, to_graphics_file );
         }
         else if ( node.getNumberOfDescendants() > 2 ) {
             paintNodeBox( node.getXcoord(), node.getYcoord(), node, g, to_pdf, to_graphics_file, isInFoundNodes( node ) );
         }
         float new_x = 0;
         float new_x_min = Float.MAX_VALUE;
         if ( !node.isExternal() && !node.isCollapse() ) {
             boolean first_child = true;
             float y2 = 0.0f;
             for( int i = 0; i < node.getNumberOfDescendants(); ++i ) {
                 final PhylogenyNode child_node = node.getChildNode( i );
                 final int factor = node.getNumberOfExternalNodes() - child_node.getNumberOfExternalNodes();
                 if ( first_child ) {
                     first_child = false;
                     y2 = node.getYcoord() - ( _y_distance * factor );
                 }
                 else {
                     y2 += _y_distance * child_node.getNumberOfExternalNodes();
                 }
                 final float x2 = calculateBranchLengthToParent( child_node, factor );
                 new_x = x2 + node.getXcoord();
                 if ( dynamically_hide && ( x2 < new_x_min ) ) {
                     new_x_min = x2;
                 }
                 paintBranch( g, node.getXcoord(), new_x, node.getYcoord(), y2, child_node, to_pdf, to_graphics_file );
                 child_node.setXcoord( new_x );
                 child_node.setYcoord( y2 );
                 y2 += _y_distance * child_node.getNumberOfExternalNodes();
             }
         }
         if ( dynamically_hide
                 && !is_in_found_nodes
                 && ( ( node.isExternal() && ( _external_node_index % dynamic_hiding_factor != 1 ) ) || ( !node
                         .isExternal() && ( ( new_x_min < 20 ) || ( _y_distance * node.getNumberOfExternalNodes() < getTreeFontSet()._fm_large
                         .getHeight() ) ) ) ) ) {
             return;
         }
         paintNodeData( g, node, to_graphics_file, to_pdf, is_in_found_nodes );
         paintNodeWithRenderableData( g, node, to_graphics_file );
     }
 
     /**
      * Draw a box at the indicated node.
      * 
      * @param x
      * @param y
      * @param node
      * @param g
      */
     private void paintNodeBox( final double x,
                                final double y,
                                final PhylogenyNode node,
                                final Graphics g,
                                final boolean to_pdf,
                                final boolean to_graphics_file,
                                final boolean is_in_found_nodes ) {
         if ( node.isCollapse() ) {
             return;
         }
         // if this node should be highlighted, do so
         if ( ( _highlight_node == node ) && !to_pdf && !to_graphics_file ) {
             g.setColor( getTreeColorSet().getFoundColor() );
             TreePanel.drawOval( x - 8, y - 8, 16, 16, g );
             TreePanel.drawOval( x - 9, y - 8, 17, 17, g );
             TreePanel.drawOval( x - 9, y - 9, 18, 18, g );
         }
       //******************************************START**********************************************************//
         if(AppletParams.isEitherTPorTDForAll()){
         	newWindowSubtree.paintNodeTracker(g, x, y, node, to_pdf, to_graphics_file);
         }
         
         if(AppletParams.isTreeDecoratorForAll()){
         	_control_panel.controlPanelAdditions.callTreeDecoratorAutoSaveToRefresh();
         	if ( is_in_found_nodes ) {
 	            paintFoundNode( ForesterUtil.roundToInt( x ), ForesterUtil.roundToInt( y ), g );
 	        }
         	else if  ( getOptions().isShowNodeBoxes()){
         		treeDecoratorPaint.decorateNodeBox(g, ForesterUtil.roundToInt(x), ForesterUtil.roundToInt(y), node);
         	}
         }
         else{
       //********************************************END**********************************************************//  
 	        if ( is_in_found_nodes ) {
 	            paintFoundNode( ForesterUtil.roundToInt( x ), ForesterUtil.roundToInt( y ), g );
 	        }
 	        else {
 	            if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
 	                g.setColor( Color.BLACK );
 	            }
 	            else if ( getControlPanel().isEvents() && Util.isHasAssignedEvent( node ) ) {
 	                final Event event = node.getNodeData().getEvent();
 	                if ( event.isDuplication() ) {
 	                    g.setColor( getTreeColorSet().getDuplicationBoxColor() );
 	                }
 	                else if ( event.isSpeciation() ) {
 	                    g.setColor( getTreeColorSet().getSpecBoxColor() );
 	                }
 	                else if ( event.isSpeciationOrDuplication() ) {
 	                    g.setColor( getTreeColorSet().getDuplicationOrSpeciationColor() );
 	                }
 	            }
 	            else {
 	                assignGraphicsForNodeBoxWithColorForParentBranch( node, g );
 	            }
 	            if ( ( getOptions().isShowNodeBoxes() && !to_pdf && !to_graphics_file )
 	                    || ( getControlPanel().isEvents() && node.isHasAssignedEvent() ) ) {
 	                if ( to_pdf || to_graphics_file ) {
 	                    if ( node.isDuplication() || !getOptions().isPrintBlackAndWhite() ) {
 	                        g.fillOval( ForesterUtil.roundToInt( x - HALF_BOX_SIZE ), ForesterUtil.roundToInt( y
 	                                - HALF_BOX_SIZE ), BOX_SIZE, BOX_SIZE );
 	                    }
 	                }
 	                else {
 	                    TreePanel.fillRect( x - HALF_BOX_SIZE, y - HALF_BOX_SIZE, BOX_SIZE, BOX_SIZE, g );
 	                }
 	            }
 	      //******************************************START**********************************************************//
 	        }
 	      //********************************************END**********************************************************//
         }
     }
 
     private void paintNodeData( final Graphics g,
                                 final PhylogenyNode node,
                                 final boolean to_graphics_file,
                                 final boolean to_pdf,
                                 final boolean is_in_found_nodes ) {
     	if ( isNodeDataInvisible( node ) && !to_graphics_file && !to_pdf ) {
             return;
         }
         if ( getOptions().isShowBranchLengthValues()
                 && ( ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR ) || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) )
                 && ( !node.isRoot() ) && ( node.getDistanceToParent() >= 0.0 ) ) {
             paintBranchLength( g, node, to_pdf, to_graphics_file );
         }
         if ( !getControlPanel().isShowInternalData() && !node.isExternal() && !node.isCollapse() ) {
             return;
         }
         int x = 0;
         if ( node.getNodeData().isHasTaxonomy()
                 && ( getControlPanel().isShowTaxonomyCode() || getControlPanel().isShowTaxonomyNames() ) ) {
             x = paintTaxonomy( g, node, is_in_found_nodes, to_pdf, to_graphics_file );
         }
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else if ( is_in_found_nodes ) {
             g.setColor( getTreeColorSet().getFoundColor() );
         }
         else if ( getControlPanel().isColorAccordingToTaxonomy() ) {
             g.setColor( speciesStringToColor( node ) );
         }
         else {
             g.setColor( getTreeColorSet().getSequenceColor() );
         }
         _sb.setLength( 0 );
         if ( node.isCollapse() && ( ( !node.isRoot() && !node.getParent().isCollapse() ) || node.isRoot() ) ) {
             _sb.append( " [" );
             _sb.append( node.getAllExternalDescendants().size() );
             _sb.append( "]" );
         }
         if ( getControlPanel().isShowNodeNames() && ( node.getNodeName().length() > 0 ) ) {
             if ( _sb.length() > 0 ) {
                 _sb.append( " " );
             }
             //******************************************START CHANGED**********************************************************//
            if(AppletParams.isEitherTPorTDForAll()){
             	_sb.append(Accession.removeAccessionFromStrain(node));
             }
             else{
             	_sb.append( node.getNodeName() );
             }
                 //commented changed
             //********************************************END**********************************************************//
             
         }
         if ( node.getNodeData().isHasSequence() ) {
             if ( getControlPanel().isShowSequenceAcc() && ( node.getNodeData().getSequence().getAccession() != null ) ) {
                 if ( _sb.length() > 0 ) {
                     _sb.append( " " );
                 }
                 if ( !ForesterUtil.isEmpty( node.getNodeData().getSequence().getAccession().getSource() ) ) {
                     _sb.append( node.getNodeData().getSequence().getAccession().getSource() );
                     _sb.append( ":" );
                 }
                 _sb.append( node.getNodeData().getSequence().getAccession().getValue() );
             }
             if ( getControlPanel().isShowGeneNames() && ( node.getNodeData().getSequence().getName().length() > 0 ) ) {
                 if ( _sb.length() > 0 ) {
                     _sb.append( " " );
                 }
                 _sb.append( node.getNodeData().getSequence().getName() );
             }
         }
         
       //******************************************START**********************************************************//
         if(AppletParams.isTreePrunerForAll() ){
         	treePrunerPaint.initArrayLists();
             treePrunerPaint.paintKeepRemove(g,node);
         }
        //********************************************END**********************************************************//
     	
         g.setFont( getTreeFontSet().getLargeFont() );
         if ( is_in_found_nodes ) {
             g.setFont( getTreeFontSet().getLargeFont().deriveFont( Font.BOLD ) );
         }
         double down_shift_factor = 3.0;
         if ( !node.isExternal() && ( node.getNumberOfDescendants() == 1 ) ) {
             down_shift_factor = 1;
         }
         if ( _sb.length() > 0 ) {
         	//******************************************START**********************************************************//
         	if(AppletParams.isTreeDecoratorForAll()){
         		if ( is_in_found_nodes ) {
                     g.setColor( getTreeColorSet().getFoundColor() );
                     g.setFont( getTreeFontSet().getLargeFont().deriveFont( Font.BOLD ) );
                     TreePanel.drawString( _sb.toString(), node.getXcoord() + x + 2 + TreePanel.HALF_BOX_SIZE, node.getYcoord()
                             + ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor ), g );
         		}
         		else{
         		treeDecoratorPaint.decorateStrain(g, ForesterUtil.roundToInt
         				(node.getXcoord() + x + 2 + TreePanel.HALF_BOX_SIZE),
         				ForesterUtil.roundToInt(node.getYcoord()+ ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor))
         						, _sb.toString(), node,to_pdf);
         		}
         	}
         	else
         	//********************************************END**********************************************************//
             TreePanel.drawString( _sb.toString(), node.getXcoord() + x + 2 + TreePanel.HALF_BOX_SIZE, node.getYcoord()
                     + ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor ), g );
             
         }
         if ( getControlPanel().isShowAnnotation() && node.getNodeData().isHasSequence()
                 && ( node.getNodeData().getSequence().getAnnotations() != null )
                 && ( !node.getNodeData().getSequence().getAnnotations().isEmpty() ) ) {
             if ( _sb.length() > 0 ) {
                 x += getTreeFontSet()._fm_large.stringWidth( _sb.toString() ) + 5; // TODO
                 // keep
                 // an
                 // eye
                 // on
                 // this
             }
             final Annotation ann = ( Annotation ) node.getNodeData().getSequence().getAnnotations().get( 0 );
             if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
                 g.setColor( Color.BLACK );
             }
             else {
                 g.setColor( calculateColorForAnnotation( ann ) );
             }
             final String ann_str = ann.asSimpleText().toString();
             TreePanel.drawString( ann_str, node.getXcoord() + x + 3 + TreePanel.HALF_BOX_SIZE, node.getYcoord()
                     + ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor ), g );
             _sb.setLength( 0 );
             _sb.append( ann_str );
         }
         if ( ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR )
                 || ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) ) {
             if ( ( getControlPanel().isShowBinaryCharacters() || getControlPanel().isShowBinaryCharacterCounts() )
                     && node.getNodeData().isHasBinaryCharacters() ) {
                 if ( _sb.length() > 0 ) {
                     x += getTreeFontSet()._fm_large.stringWidth( _sb.toString() ) + 5; // TODO
                     // keep
                     // an
                     // eye
                     // on
                     // this
                 }
                 if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
                     g.setColor( Color.BLACK );
                 }
                 else {
                     g.setColor( getTreeColorSet().getBinaryDomainCombinationsColor() );
                 }
                 if ( getControlPanel().isShowBinaryCharacters() ) {
                     TreePanel.drawString( node.getNodeData().getBinaryCharacters().getPresentCharactersAsStringBuffer()
                             .toString(), node.getXcoord() + x + 1 + TreePanel.HALF_BOX_SIZE, node.getYcoord()
                             + ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor ), g );
                     paintGainedAndLostCharacters( g, node, node.getNodeData().getBinaryCharacters()
                             .getGainedCharactersAsStringBuffer().toString(), node.getNodeData().getBinaryCharacters()
                             .getLostCharactersAsStringBuffer().toString() );
                 }
                 else {
                     if ( DRAW_MEAN_COUNTS && node.isInternal() ) {
                         final List<PhylogenyNode> ec = node.getAllExternalDescendants();
                         double sum = 0;
                         int count = 0;
                         for( final PhylogenyNode phylogenyNode : ec ) {
                             count++;
                             if ( phylogenyNode.getNodeData().getBinaryCharacters() != null ) {
                                 sum += phylogenyNode.getNodeData().getBinaryCharacters().getPresentCount();
                             }
                         }
                         final double mean = ForesterUtil.round( sum / count, 1 );
                         TreePanel.drawString( node.getNodeData().getBinaryCharacters().getPresentCount() + " [" + mean
                                 + "]", node.getXcoord() + x + 2 + TreePanel.HALF_BOX_SIZE, node.getYcoord()
                                 + ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor ), g );
                     }
                     else {
                         TreePanel.drawString( node.getNodeData().getBinaryCharacters().getPresentCount(), node
                                 .getXcoord()
                                 + x + 2 + TreePanel.HALF_BOX_SIZE, node.getYcoord()
                                 + ( getTreeFontSet()._fm_large.getAscent() / down_shift_factor ), g );
                     }
                     paintGainedAndLostCharacters( g, node, "+"
                             + node.getNodeData().getBinaryCharacters().getGainedCount(), "-"
                             + node.getNodeData().getBinaryCharacters().getLostCount() );
                 }
             }
         }
     }
 
     private void paintNodeDataUnrootedCirc( final Graphics g,
                                             final PhylogenyNode node,
                                             final boolean to_pdf,
                                             final boolean to_graphics_file,
                                             final boolean radial_labels,
                                             final double ur_angle ) {
         if ( isNodeDataInvisible( node ) && !to_graphics_file && !to_pdf ) {
             return;
         }
         final boolean is_in_found_nodes = isInFoundNodes( node );
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else if ( is_in_found_nodes ) {
             g.setColor( getTreeColorSet().getFoundColor() );
         }
         else if ( getControlPanel().isColorAccordingToTaxonomy() ) {
             g.setColor( speciesStringToColor( node ) );
         }
         else {
             g.setColor( getTreeColorSet().getSequenceColor() );
         }
         _sb.setLength( 0 );
         _sb.append( " " );
         if ( node.getNodeData().isHasTaxonomy()
                 && ( getControlPanel().isShowTaxonomyCode() || getControlPanel().isShowTaxonomyNames() ) ) {
             final Taxonomy taxonomy = node.getNodeData().getTaxonomy();
             if ( _control_panel.isShowTaxonomyCode() && !ForesterUtil.isEmpty( taxonomy.getTaxonomyCode() ) ) {
                 _sb.append( taxonomy.getTaxonomyCode() );
                 _sb.append( " " );
             }
             if ( _control_panel.isShowTaxonomyNames() ) {
                 if ( !ForesterUtil.isEmpty( taxonomy.getScientificName() )
                         && !ForesterUtil.isEmpty( taxonomy.getCommonName() ) ) {
                     _sb.append( taxonomy.getScientificName() );
                     _sb.append( " (" );
                     _sb.append( taxonomy.getCommonName() );
                     _sb.append( ") " );
                 }
                 else if ( !ForesterUtil.isEmpty( taxonomy.getScientificName() ) ) {
                     _sb.append( taxonomy.getScientificName() );
                     _sb.append( " " );
                 }
                 else if ( !ForesterUtil.isEmpty( taxonomy.getCommonName() ) ) {
                     _sb.append( taxonomy.getCommonName() );
                     _sb.append( " " );
                 }
             }
         }
         if ( node.isCollapse() && ( ( !node.isRoot() && !node.getParent().isCollapse() ) || node.isRoot() ) ) {
             _sb.append( " [" );
             _sb.append( node.getAllExternalDescendants().size() );
             _sb.append( "]" );
         }
         if ( getControlPanel().isShowNodeNames() && ( node.getNodeName().length() > 0 ) ) {
             if ( _sb.length() > 0 ) {
                 _sb.append( " " );
             }
             _sb.append( node.getNodeName() );
         }
         if ( node.getNodeData().isHasSequence() ) {
             if ( getControlPanel().isShowSequenceAcc() && ( node.getNodeData().getSequence().getAccession() != null ) ) {
                 if ( _sb.length() > 0 ) {
                     _sb.append( " " );
                 }
                 if ( !ForesterUtil.isEmpty( node.getNodeData().getSequence().getAccession().getSource() ) ) {
                     _sb.append( node.getNodeData().getSequence().getAccession().getSource() );
                     _sb.append( ":" );
                 }
                 _sb.append( node.getNodeData().getSequence().getAccession().getValue() );
             }
             if ( getControlPanel().isShowGeneNames() && ( node.getNodeData().getSequence().getName().length() > 0 ) ) {
                 if ( _sb.length() > 0 ) {
                     _sb.append( " " );
                 }
                 _sb.append( node.getNodeData().getSequence().getName() );
             }
         }
         g.setFont( getTreeFontSet().getLargeFont() );
         if ( is_in_found_nodes ) {
             g.setFont( getTreeFontSet().getLargeFont().deriveFont( Font.BOLD ) );
         }
         if ( _sb.length() > 1 ) {
             final String sb_str = _sb.toString();
             double m = 0;
             if ( _graphics_type == PHYLOGENY_GRAPHICS_TYPE.CIRCULAR ) {
                 m = _urt_nodeid_angle_map.get( node.getNodeId() ) % TWO_PI;
             }
             else {
                 m = ( float ) ( ur_angle % TWO_PI );
             }
             _g2d = ( Graphics2D ) g;
             _at = _g2d.getTransform();
             boolean need_to_reset = false;
             final float x_coord = node.getXcoord();
             final float y_coord = node.getYcoord() + ( getTreeFontSet()._fm_large.getAscent() / 3.0f );
             if ( radial_labels ) {
                 need_to_reset = true;
                 boolean left = false;
                 if ( ( m > HALF_PI ) && ( m < ONEHALF_PI ) ) {
                     m -= PI;
                     left = true;
                 }
                 _g2d.rotate( m, x_coord, node.getYcoord() );
                 if ( left ) {
                     _g2d.translate( -( getTreeFontSet()._fm_large.getStringBounds( sb_str, _g2d ).getWidth() ), 0 );
                 }
             }
             else {
                 if ( ( m > HALF_PI ) && ( m < ONEHALF_PI ) ) {
                     need_to_reset = true;
                     _g2d.translate( -getTreeFontSet()._fm_large.getStringBounds( sb_str, _g2d ).getWidth(), 0 );
                 }
             }
             TreePanel.drawString( sb_str, x_coord, y_coord, g );
             if ( need_to_reset ) {
                 _g2d.setTransform( _at );
             }
         }
     }
 
     private void paintNodeLite( final Graphics g, final PhylogenyNode node ) {
         if ( node.isCollapse() ) {
             if ( ( !node.isRoot() && !node.getParent().isCollapse() ) || node.isRoot() ) {
                 paintCollapsedNode( g, node, false, false, false );
             }
             return;
         }
         if ( isInFoundNodes( node ) ) {
             g.setColor( getTreeColorSet().getFoundColor() );
             fillRect( node.getXSecondary() - 1, node.getYSecondary() - 1, 3, 3, g );
         }
         float new_x = 0;
         if ( !node.isExternal() && !node.isCollapse() ) {
             boolean first_child = true;
             float y2 = 0.0f;
             for( int i = 0; i < node.getNumberOfDescendants(); ++i ) {
                 final PhylogenyNode child_node = node.getChildNode( i );
                 final int factor = node.getNumberOfExternalNodes() - child_node.getNumberOfExternalNodes();
                 if ( first_child ) {
                     first_child = false;
                     y2 = node.getYSecondary() - ( getOvYDistance() * factor );
                 }
                 else {
                     y2 += getOvYDistance() * child_node.getNumberOfExternalNodes();
                 }
                 final float x2 = calculateOvBranchLengthToParent( child_node, factor );
                 new_x = x2 + node.getXSecondary();
                 paintBranchLite( g, node.getXSecondary(), new_x, node.getYSecondary(), y2, child_node );
                 child_node.setXSecondary( new_x );
                 child_node.setYSecondary( y2 );
                 y2 += getOvYDistance() * child_node.getNumberOfExternalNodes();
             }
         }
     }
 
     private void paintNodeWithRenderableData( final Graphics g, final PhylogenyNode node, final boolean to_graphics_file ) {
         if ( isNodeDataInvisible( node ) && !to_graphics_file ) {
             return;
         }
         if ( ( !getControlPanel().isShowInternalData() && !node.isExternal() ) ) {
             return;
         }
         if ( getControlPanel().isShowDomainArchitectures() && node.getNodeData().isHasSequence()
                 && ( node.getNodeData().getSequence().getDomainArchitecture() != null ) ) {
             RenderableDomainArchitecture rds = null;
             try {
                 rds = ( RenderableDomainArchitecture ) node.getNodeData().getSequence().getDomainArchitecture();
             }
             catch ( final ClassCastException cce ) {
                 return;
             }
             rds.setRenderingHeight( 6 );
             int x = 0;
             if ( getControlPanel().isShowTaxonomyCode() && ( PhylogenyMethods.getSpecies( node ).length() > 0 ) ) {
                 x += getTreeFontSet()._fm_large_italic.stringWidth( PhylogenyMethods.getSpecies( node ) + " " );
             }
             if ( getControlPanel().isShowNodeNames() && ( node.getNodeName().length() > 0 ) ) {
                 x += getTreeFontSet()._fm_large.stringWidth( node.getNodeName() + " " );
             }
             rds.render( node.getXcoord() + x, node.getYcoord() - 3, g, this );
         }
     }
 
     private void paintOvRectangle( final Graphics g ) {
         final float w_ratio = ( float ) getWidth() / getVisibleRect().width;
         final float h_ratio = ( float ) getHeight() / getVisibleRect().height;
         final float x_ratio = ( float ) getWidth() / getVisibleRect().x;
         final float y_ratio = ( float ) getHeight() / getVisibleRect().y;
         final float width = getOvMaxWidth() / w_ratio;
         final float height = getOvMaxHeight() / h_ratio;
         final float x = getVisibleRect().x + getOvXPosition() + getOvMaxWidth() / x_ratio;
         final float y = getVisibleRect().y + getOvYPosition() + getOvMaxHeight() / y_ratio;
         g.setColor( getTreeColorSet().getFoundColor() );
         getOvRectangle().setRect( x, y, width, height );
         if ( ( width < 6 ) && ( height < 6 ) ) {
             fillRect( x, y, 6, 6, g );
             getOvVirtualRectangle().setRect( x, y, 6, 6 );
         }
         else if ( width < 6 ) {
             fillRect( x, y, 6, height, g );
             getOvVirtualRectangle().setRect( x, y, 6, height );
         }
         else if ( height < 6 ) {
             fillRect( x, y, width, 6, g );
             getOvVirtualRectangle().setRect( x, y, width, 6 );
         }
         else {
             drawRect( x, y, width, height, g );
             if ( isInOvRect() ) {
                 drawRect( x + 1, y + 1, width - 2, height - 2, g );
             }
             getOvVirtualRectangle().setRect( x, y, width, height );
         }
     }
 
     private void paintPhylogenyLite( final Graphics g ) {
         _phylogeny
                 .getRoot()
                 .setXSecondary( ( float ) ( getVisibleRect().x + getOvXPosition() + ( MOVE / ( getVisibleRect().width / getOvRectangle()
                         .getWidth() ) ) ) );
         _phylogeny.getRoot().setYSecondary( ( getVisibleRect().y + getOvYStart() ) );
         final PhylogenyNodeIterator it;
         for( it = _phylogeny.iteratorPreorder(); it.hasNext(); ) {
             paintNodeLite( g, it.next() );
         }
         paintOvRectangle( g );
     }
 
     /**
      * Paint the root branch. (Differs from others because it will always be a
      * single horizontal line).
      * @param to_graphics_file 
      * 
      * @return new x1 value
      */
     private void paintRootBranch( final Graphics g,
                                   final float x1,
                                   final float y1,
                                   final PhylogenyNode root,
                                   final boolean to_pdf,
                                   final boolean to_graphics_file ) {
         assignGraphicsForBranchWithColorForParentBranch( root, false, g, to_pdf, to_graphics_file );
         float d = getXdistance();
         if ( getControlPanel().isDrawPhylogram() && ( root.getDistanceToParent() > 0.0 ) ) {
             d = ( float ) ( getXcorrectionFactor() * root.getDistanceToParent() );
         }
         if ( d < MIN_ROOT_LENGTH ) {
             d = MIN_ROOT_LENGTH;
         }
         //paintLine( x1 - ( d < 3 ? 3 : d ), root.getYcoord(), x1, root.getYcoord(), root, false, g );
         if ( !getControlPanel().isWidthBranches() || ( PhylogenyMethods.getBranchWidthValue( root ) == 1 ) ) {
             TreePanel.drawLine( x1 - d, root.getYcoord(), x1, root.getYcoord(), g );
         }
         else {
             final double w = PhylogenyMethods.getBranchWidthValue( root );
             TreePanel.fillRect( x1 - d, root.getYcoord() - ( w / 2 ), d, w, g );
         }
         paintNodeBox( x1, root.getYcoord(), root, g, to_pdf, to_graphics_file, isInFoundNodes( root ) );
     }
 
     private void paintScale( final Graphics g, int x1, int y1, final boolean to_pdf, final boolean to_graphics_file ) {
         if ( !getControlPanel().isDrawPhylogram() || ( getScaleDistance() <= 0.0 ) ) {
             return;
         }
         x1 += MOVE;
         final double x2 = x1 + ( getScaleDistance() * getXcorrectionFactor() );
         y1 -= 12;
         final int y2 = y1 - 8;
         final int y3 = y1 - 4;
         g.setFont( getTreeFontSet().getSmallFont() );
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else {
             g.setColor( getTreeColorSet().getBranchLengthColor() );
         }
         drawLine( x1, y1, x1, y2, g );
         drawLine( x2, y1, x2, y2, g );
         drawLine( x1, y3, x2, y3, g );
         if ( getScaleLabel() != null ) {
             g.drawString( getScaleLabel(), ( x1 + 2 ), y3 - 2 );
         }
     }
 
     private void paintSupportValue( final Graphics g,
                                     final PhylogenyNode node,
                                     final boolean to_pdf,
                                     final boolean to_graphics_file ) {
         final String bs = ""
                 + FORMATTER_1.format( ForesterUtil.round( PhylogenyMethods.getConfidenceValue( node ), 1 ) );
         final double parent_x = node.getParent().getXcoord();
         double x = node.getXcoord();
         g.setFont( getTreeFontSet().getSmallFont() );
         if ( getPhylogenyGraphicsType() == PHYLOGENY_GRAPHICS_TYPE.EURO_STYLE ) {
             x += EURO_D;
         }
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else {
             g.setColor( getTreeColorSet().getConfidenceColor() );
         }
         TreePanel.drawString( bs,
                               parent_x + ( ( x - parent_x - getTreeFontSet()._fm_small.stringWidth( bs ) ) / 3.0f ),
                               ( node.getYcoord() + getTreeFontSet()._small_max_ascent ) - 1,
                               g );
     }
 
     private int paintTaxonomy( final Graphics g,
                                final PhylogenyNode node,
                                final boolean is_in_found_nodes,
                                final boolean to_pdf,
                                final boolean to_graphics_file ) {
         final Taxonomy taxonomy = node.getNodeData().getTaxonomy();
         g.setFont( getTreeFontSet().getLargeItalicFont() );
         if ( ( to_pdf || to_graphics_file ) && getOptions().isPrintBlackAndWhite() ) {
             g.setColor( Color.BLACK );
         }
         else if ( is_in_found_nodes ) {
             g.setFont( getTreeFontSet().getLargeItalicFont().deriveFont( TreeFontSet.BOLD_AND_ITALIC ) );
             g.setColor( getTreeColorSet().getFoundColor() );
         }
         else if ( getControlPanel().isColorAccordingToTaxonomy() ) {
             g.setColor( speciesStringToColor( node ) );
         }
         else {
             g.setColor( getTreeColorSet().getTaxonomyColor() );
         }
         final double start_x = node.getXcoord() + 3 + TreePanel.HALF_BOX_SIZE;
         final double start_y = node.getYcoord()
                 + ( getTreeFontSet()._fm_large.getAscent() / ( node.getNumberOfDescendants() == 1 ? 1 : 3.0 ) );
         _sb.setLength( 0 );
         if ( _control_panel.isShowTaxonomyCode() && !ForesterUtil.isEmpty( taxonomy.getTaxonomyCode() ) ) {
             _sb.append( taxonomy.getTaxonomyCode() );
             _sb.append( " " );
         }
         if ( _control_panel.isShowTaxonomyNames() ) {
             if ( !ForesterUtil.isEmpty( taxonomy.getScientificName() )
                     && !ForesterUtil.isEmpty( taxonomy.getCommonName() ) ) {
                 _sb.append( taxonomy.getScientificName() );
                 _sb.append( " (" );
                 _sb.append( taxonomy.getCommonName() );
                 _sb.append( ") " );
             }
             else if ( !ForesterUtil.isEmpty( taxonomy.getScientificName() ) ) {
                 _sb.append( taxonomy.getScientificName() );
                 _sb.append( " " );
             }
             else if ( !ForesterUtil.isEmpty( taxonomy.getCommonName() ) ) {
                 _sb.append( taxonomy.getCommonName() );
                 _sb.append( " " );
             }
         }
         final String label = _sb.toString();
         TreePanel.drawString( label, start_x, start_y, g );
         if ( is_in_found_nodes ) {
             return getTreeFontSet()._fm_large_italic_bold.stringWidth( label );
         }
         else {
             return getTreeFontSet()._fm_large_italic.stringWidth( label );
         }
     }
 
     private void paintUnrooted( final PhylogenyNode n,
                                 final double low_angle,
                                 final double high_angle,
                                 final boolean radial_labels,
                                 final Graphics g,
                                 final boolean to_pdf,
                                 final boolean to_graphics_file ) {
         if ( n.isRoot() ) {
             n.setXcoord( ( float ) ( getPreferredSize().getWidth() / 2 ) );
             n.setYcoord( ( float ) ( getPreferredSize().getHeight() / 2 ) );
         }
         if ( n.isExternal() ) {
             paintNodeDataUnrootedCirc( g, n, to_pdf, to_graphics_file, radial_labels, ( high_angle + low_angle ) / 2 );
             return;
         }
         final float num_enclosed = n.getNumberOfExternalNodes();
         final double x = n.getXcoord();
         final double y = n.getYcoord();
         double current_angle = low_angle;
         for( int i = 0; i < n.getNumberOfDescendants(); ++i ) {
             final PhylogenyNode desc = n.getChildNode( i );
             final float desc_num_enclosed = desc.getNumberOfExternalNodes();
             final double child_ratio = desc_num_enclosed / num_enclosed;
             final double arc_size = child_ratio * ( high_angle - low_angle );
             double length;
             if ( isPhyHasBranchLengths() ) {
                 length = desc.getDistanceToParent() * getUrtFactor();
             }
             else {
                 length = getUrtFactor();
             }
             final double mid_angle = current_angle + arc_size / 2;
             final double new_x = x + Math.cos( mid_angle ) * length;
             final double new_y = y + Math.sin( mid_angle ) * length;
             desc.setXcoord( ( float ) new_x );
             desc.setYcoord( ( float ) new_y );
             paintUnrooted( desc, current_angle, current_angle + arc_size, radial_labels, g, to_pdf, to_graphics_file );
             current_angle += arc_size;
             assignGraphicsForBranchWithColorForParentBranch( desc, false, g, false, false );
             drawLine( x, y, new_x, new_y, g );
         }
     }
 
     private void setInOv( final boolean in_ov ) {
         _in_ov = in_ov;
     }
 
     private void setOvMaxHeight( final float ov_max_height ) {
         _ov_max_height = ov_max_height;
     }
 
     private void setOvMaxWidth( final float ov_max_width ) {
         _ov_max_width = ov_max_width;
     }
 
     private void setOvXcorrectionFactor( final float f ) {
         _ov_x_correction_factor = f;
     }
 
     private void setOvXDistance( final float ov_x_distance ) {
         _ov_x_distance = ov_x_distance;
     }
 
     private void setOvXPosition( final int ov_x_position ) {
         _ov_x_position = ov_x_position;
     }
 
     private void setOvYDistance( final float ov_y_distance ) {
         _ov_y_distance = ov_y_distance;
     }
 
     private void setOvYPosition( final int ov_y_position ) {
         _ov_y_position = ov_y_position;
     }
 
     private void setOvYStart( final int ov_y_start ) {
         _ov_y_start = ov_y_start;
     }
 
     private void setScaleDistance( final double scale_distance ) {
         _scale_distance = scale_distance;
     }
 
     private void setScaleLabel( final String scale_label ) {
         _scale_label = scale_label;
     }
 
     private void setUrtFactor( final float urt_factor ) {
         _urt_factor = urt_factor;
     }
 
     private void showNodeFrame( final PhylogenyNode n ) {
         if ( _node_frame_index < TreePanel.MAX_NODE_FRAMES ) {
             // pop up edit box for single node
             _node_frames[ _node_frame_index ] = new NodeFrame( n, _phylogeny, this, _node_frame_index );
             _node_frame_index++;
         }
         else {
             JOptionPane.showMessageDialog( this, "too many node windows are open" );
         }
     }
 
     private static void drawLine( final double x1, final double y1, final double x2, final double y2, final Graphics g ) {
         final int x1_ = ForesterUtil.roundToInt( x1 );
         final int x2_ = ForesterUtil.roundToInt( x2 );
         final int y1_ = ForesterUtil.roundToInt( y1 );
         final int y2_ = ForesterUtil.roundToInt( y2 );
         if ( ( x1_ == x2_ ) && ( y1_ == y2_ ) ) {
             return;
         }
         g.drawLine( x1_, y1_, x2_, y2_ );
     }
 
     private static void drawOval( final double x,
                                   final double y,
                                   final double width,
                                   final double heigth,
                                   final Graphics g ) {
         g.drawOval( ForesterUtil.roundToInt( x ),
                     ForesterUtil.roundToInt( y ),
                     ForesterUtil.roundToInt( width ),
                     ForesterUtil.roundToInt( heigth ) );
     }
 
     private static void drawRect( final float x, final float y, final float width, final float heigth, final Graphics g ) {
         g.drawRect( ForesterUtil.roundToInt( x ),
                     ForesterUtil.roundToInt( y ),
                     ForesterUtil.roundToInt( width ),
                     ForesterUtil.roundToInt( heigth ) );
     }
 
     private static void drawString( final int i, final double x, final double y, final Graphics g ) {
         g.drawString( String.valueOf( i ), ForesterUtil.roundToInt( x ), ForesterUtil.roundToInt( y ) );
     }
 
     private static void drawString( final String str, final double x, final double y, final Graphics g ) {
         g.drawString( str, ForesterUtil.roundToInt( x ), ForesterUtil.roundToInt( y ) );
     }
 
     private static void fillRect( final double x,
                                   final double y,
                                   final double width,
                                   final double heigth,
                                   final Graphics g ) {
         g.fillRect( ForesterUtil.roundToInt( x ),
                     ForesterUtil.roundToInt( y ),
                     ForesterUtil.roundToInt( width ),
                     ForesterUtil.roundToInt( heigth ) );
     }
 
     private static void fillRect( final float x, final float y, final float width, final float heigth, final Graphics g ) {
         g.fillRect( ForesterUtil.roundToInt( x ),
                     ForesterUtil.roundToInt( y ),
                     ForesterUtil.roundToInt( width ),
                     ForesterUtil.roundToInt( heigth ) );
     }
 
     private static boolean plusPressed( final int key_code ) {
         return ( ( key_code == KeyEvent.VK_ADD ) || ( key_code == KeyEvent.VK_PLUS )
                 || ( key_code == KeyEvent.VK_EQUALS ) || ( key_code == KeyEvent.VK_SEMICOLON ) || ( key_code == KeyEvent.VK_1 ) );
     }
 
     private class SubtreeColorizationActionListener implements ActionListener {
 
         JColorChooser _chooser;
         PhylogenyNode _node;
 
         SubtreeColorizationActionListener( final JColorChooser chooser, final PhylogenyNode node ) {
             _chooser = chooser;
             _node = node;
         }
 
         public void actionPerformed( final ActionEvent e ) {
             final Color c = _chooser.getColor();
             if ( c != null ) {
                 colorizeSubtree( c, _node );
             }
         }
     }
     
     //******************************************START**********************************************************//
     
     public void setPhylogeny(Phylogeny phylogeny){
     	_phylogeny = phylogeny;
     }
     
     public Phylogeny getCurrentPhylogeny(){
     	return _phylogeny;
     }
     
     public static void reset_subtree_index(){
     	_subtree_index = 0; 
     }
     
     public static void set_subtree_index(int n){
     	_subtree_index = n; 
     }
     public static int get_subtree_index(){
     	return _subtree_index; 
     }
     
     public void updateSubSuperTreeButton(boolean deactivate) {
         if ( deactivate ) {
             getControlPanel().deactivateButtonToReturnToSuperTree();
         }
         else {
             getControlPanel().activateButtonToReturnToSuperTree( 0 );
         }
     }
     
 	public File get_tree_file() {
 		return _treefile;
 	}
 
 	public void set_tree_file(final File treefile) {
 		_treefile = treefile;
 	}
 
 	public void set_arrow_cursor() {
 		setCursor(ARROW_CURSOR);
 		repaint();
 	}
 
 	public PHYLOGENY_GRAPHICS_TYPE get_phylogeny_graphicsType() {
 		return _graphics_type;
 	}
     //********************************************END**********************************************************//
 }
