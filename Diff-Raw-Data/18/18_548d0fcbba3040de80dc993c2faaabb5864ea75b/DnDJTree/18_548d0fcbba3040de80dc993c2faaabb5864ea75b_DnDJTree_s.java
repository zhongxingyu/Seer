 //$HeadURL$
 /*----------------    FILE HEADER  ------------------------------------------
  This file is part of deegree.
 Copyright (C) 2001-2008 by:
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
 package org.deegree.igeo.views.swing.layerlist;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.io.IOException;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.UIManager;
 import javax.swing.plaf.ColorUIResource;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.igeo.ApplicationContainer;
 import org.deegree.igeo.ChangeListener;
 import org.deegree.igeo.ValueChangedEvent;
 import org.deegree.igeo.commands.model.MoveLayerCommand;
 import org.deegree.igeo.dataadapter.AdapterEvent;
 import org.deegree.igeo.dataadapter.AdapterEvent.ADAPTER_EVENT_TYPE;
 import org.deegree.igeo.mapmodel.Layer;
 import org.deegree.igeo.mapmodel.LayerChangedEvent;
 import org.deegree.igeo.mapmodel.LayerGroup;
 import org.deegree.igeo.mapmodel.MapModel;
 import org.deegree.igeo.mapmodel.MapModelChangedEvent;
 import org.deegree.igeo.mapmodel.MapModelEntry;
 import org.deegree.igeo.views.swing.util.IconRegistry;
 import org.deegree.kernel.Command;
 
 /**
 * 
 * 
  * 
  * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
  * @author last edited by: $Author$
  * 
  * @version. $Revision$, $Date$
  */
 public class DnDJTree extends JTree implements DragSourceListener, DropTargetListener, DragGestureListener,
                                    ChangeListener {
 
     private static final long serialVersionUID = 867560018228692980L;
 
     private static final ILogger LOG = LoggerFactory.getLogger( DnDJTree.class );
 
     private static final Color borderColor = Color.decode( "0x65a360" );
 
     private static DataFlavor localObjectFlavor;
     static {
         try {
             localObjectFlavor = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType );
         } catch ( ClassNotFoundException ex ) {
             ex.printStackTrace( System.out );
         }
     }
 
     private static DataFlavor[] supportedFlavors = { localObjectFlavor };
 
     private DragSource dragSource;
 
     private TreeNode dropTargetNode = null;
 
     private TreeNode draggedNode = null;
 
     private MapModel mapModel;
 
     private Point clickPoint;
 
     private ApplicationContainer<Container> appContainer;
 
     private boolean top;
 
     /**
      * 
      * @param root
      * @param appContainer
      * @param mapModel
      */
     DnDJTree( TreeNode root, ApplicationContainer<Container> appContainer, MapModel mapModel ) {
         super( root );
         this.appContainer = appContainer;
         this.mapModel = mapModel;
         this.mapModel.addChangeListener( this );
         _init();
 
     }
 
     private void _init() {
         DnDJTreeCellRenderer tcr = new DnDJTreeCellRenderer();
         setCellRenderer( tcr );
         dragSource = new DragSource();
         dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_MOVE, this );
         new DropTarget( this, this );
     }
 
     /**
      * @param dge
      */
     public void dragGestureRecognized( DragGestureEvent dge ) {
         clickPoint = dge.getDragOrigin();
         TreePath path = getPathForLocation( clickPoint.x, clickPoint.y );
         if ( path == null ) {
             LOG.logWarning( "NOT A NODE" );
             return;
         }
         draggedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
         Transferable trans = new RJLTransferable( draggedNode );
         dragSource.startDrag( dge, Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ), trans, this );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
      */
     public void dragEnter( DragSourceDragEvent event ) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
      */
     public void dragOver( DragSourceDragEvent event ) {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
      */
     public void dropActionChanged( DragSourceDragEvent event ) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
      */
     public void dragDropEnd( DragSourceDropEvent event ) {
         dropTargetNode = null;
         draggedNode = null;
         repaint();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
      */
     public void dragExit( DragSourceEvent event ) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragEnter( DropTargetDragEvent dtde ) {
         dtde.acceptDrag( DnDConstants.ACTION_COPY_OR_MOVE );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
      */
     public void dragOver( DropTargetDragEvent dtde ) {
 
         Point dragPoint = dtde.getLocation();
         TreePath path = getPathForLocation( dragPoint.x, dragPoint.y );
         if ( path == null ) {
             dropTargetNode = null;
         } else {
             dropTargetNode = (TreeNode) path.getLastPathComponent();
         }
         Rectangle pathBounds = getPathBounds( path );
         top = dragPoint.y < ( pathBounds.y + ( pathBounds.height / 2 ) );
 
         repaint();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
      */
     public void dropActionChanged( DropTargetDragEvent event ) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
      */
     public void dragExit( DropTargetEvent event ) {
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
      */
     public void drop( DropTargetDropEvent dtde ) {
         Point dropPoint = dtde.getLocation();
         TreePath path = getPathForLocation( dropPoint.x, dropPoint.y );
         Rectangle pathBounds = getPathBounds( path );
         boolean before = dropPoint.y < ( pathBounds.y + ( pathBounds.height / 2 ) );
         DefaultMutableTreeNode droppedNode = null;
         try {
             if ( path != null ) {
                 dtde.acceptDrop( DnDConstants.ACTION_MOVE );
                 Object droppedObject = dtde.getTransferable().getTransferData( localObjectFlavor );
 
                 if ( droppedObject instanceof MutableTreeNode ) {
                     droppedNode = (DefaultMutableTreeNode) droppedObject;
                 } else {
                     droppedNode = new DefaultMutableTreeNode( droppedObject );
                 }
 
                 // node where the mouse button has been released
                 DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                 if ( droppedNode.getUserObject().equals( targetNode.getUserObject() ) ) {
                     // not a useful operation
                     return;
                 }
 
                 // check if target node is a direct or indirect child of dropped node
                 if ( !isChild( droppedNode, targetNode ) ) {
 
                     // actualize JTree model
                     if ( targetNode.getUserObject() instanceof Layer ) {
                         // if the target node is a leaf (it is a layer that contains data) the
                         // dropped node will be inserted after it
                         DefaultMutableTreeNode parent = (DefaultMutableTreeNode) targetNode.getParent();
                         if ( parent == null ) {
                             parent = (DefaultMutableTreeNode) path.getParentPath().getLastPathComponent();
                         }
                         if ( droppedNode.getParent() != null ) {
                             ( (DefaultTreeModel) getModel() ).removeNodeFromParent( droppedNode );
                             int index = parent.getIndex( targetNode ) + 1;
 
                             if ( before ) {
                                 index--;
                             }
                             if ( index >= 0 ) {
                                 ( (DefaultTreeModel) getModel() ).insertNodeInto( droppedNode, parent, index );
                             }
                         }
                     } else {
                         // if the target node is not a leaf (it is a layergroup) the
                         // dropped node will be inserted within it
                         if ( !droppedNode.equals( targetNode ) ) {
                             ( (DefaultTreeModel) getModel() ).removeNodeFromParent( droppedNode );
                             ( (DefaultTreeModel) getModel() ).insertNodeInto( droppedNode, targetNode,
                                                                               targetNode.getChildCount() );
                         }
                     }
 
                     // actualize mapmodel
                     if ( droppedNode.getUserObject() instanceof MapModelEntry ) {
                         LayerGroup parent = null;
                         MapModelEntry antecessor = (MapModelEntry) targetNode.getUserObject();
                         if ( antecessor instanceof LayerGroup ) {
                             // if target is a LayerGroup insert dropped node into it
                             parent = (LayerGroup) antecessor;
                             antecessor = null;
                         } else {
                             if ( antecessor != null && before ) {
                                 antecessor = ( (Layer) antecessor ).getAntecessor();
                             }
                             if ( targetNode.getParent() != null ) {
                                 parent = (LayerGroup) ( (CheckNode) targetNode.getParent() ).getUserObject();
                             }
                         }
                         MapModelEntry la = (MapModelEntry) ( (CheckNode) droppedNode ).getUserObject();
                         if ( droppedNode.getUserObject() instanceof LayerGroup ) {
                             before = false;
                         }
                         Command command = new MoveLayerCommand( la, parent, antecessor, mapModel, before );
                         appContainer.getCommandProcessor().executeSychronously( command, true );
                     }
                 }
             }
 
         } catch ( Exception ex ) {
             ex.printStackTrace();
             // this probably is not a reason to forward an exception or to create a new one.
             // In case of an exception it just will be logged
             LOG.logWarning( "none critical exception during droping: " + ex.getMessage() );
         }
         dtde.dropComplete( true );
     }
 
     private boolean isChild( DefaultMutableTreeNode droppedNode, DefaultMutableTreeNode targetNode ) {
         DefaultMutableTreeNode node = targetNode;
         while ( !node.equals( getModel().getRoot() ) ) {
             if ( node.getParent().equals( droppedNode ) ) {
                 return true;
             }
             node = (DefaultMutableTreeNode) node.getParent();
         }
         return false;
     }
 
     /**
      * updates the statuts (visible - not visible) of a node assigned to the passed layer adapter
      * 
      * @param layer
      */
     void updateNodeStatus( MapModelEntry entry ) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeModel.getRoot();
         int cnt = node.getChildCount();
         for ( int i = 0; i < cnt; i++ ) {
             traverseTree( (DefaultMutableTreeNode) node.getChildAt( i ), entry );
         }
     }
 
     private void traverseTree( DefaultMutableTreeNode node, MapModelEntry thatEntry ) {
         CheckNode cn = (CheckNode) node;
         MapModelEntry entry = (MapModelEntry) cn.getUserObject();
         if ( entry.getIdentifier().equals( thatEntry.getIdentifier() ) ) {
             cn.setSelected( entry.isVisible() );
             invalidate();
             getParent().repaint();
         }
         int cnt = node.getChildCount();
         for ( int i = 0; i < cnt; i++ ) {
             traverseTree( (DefaultMutableTreeNode) node.getChildAt( i ), thatEntry );
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.ChangeListener#valueChanged(org.deegree.igeo.ValueChangedEvent)
      */
     public void valueChanged( ValueChangedEvent event ) {
         if ( ( (MapModelChangedEvent) event ).getChangeType() == MapModelChangedEvent.CHANGE_TYPE.extentChanged ) {
             // repaint is required because tree node label color depends on map scale
             invalidate();
             getParent().repaint();
         }
     }
 
     // ////////////////////////////////////////////////////////////////////////////
     // inner classes //
     // ////////////////////////////////////////////////////////////////////////////
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     class RJLTransferable implements Transferable {
         private Object object;
 
         /**
          * 
          * @param o
          */
         RJLTransferable( Object o ) {
             this.object = o;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
          */
         public DataFlavor[] getTransferDataFlavors() {
             return supportedFlavors;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
          */
         public boolean isDataFlavorSupported( DataFlavor flavor ) {
             return flavor.equals( localObjectFlavor );
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
          */
         public Object getTransferData( DataFlavor flavor )
                                 throws UnsupportedFlavorException, IOException {
             if ( isDataFlavorSupported( flavor ) ) {
                 return object;
             } else {
                 throw new UnsupportedFlavorException( flavor );
             }
         }
 
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     class DnDJTreeCellRenderer extends DefaultTreeCellRenderer {
 
         private static final long serialVersionUID = -6416294779686003390L;
 
         private boolean isTargetNode;
 
         private boolean isTargetNodeLeaf;
 
         private JCheckBox check;
 
         private JLabel label;
 
         private JToolBar toolbar;
 
         /**
          * 
          * 
          */
         DnDJTreeCellRenderer() {
             setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
 
             label = new JLabel();
 
             check = new JCheckBox();
             check.setToolTipText( "visibility" );
             check.setOpaque( false );
 
             toolbar = new JToolBar();
             toolbar.setMinimumSize( new Dimension( 400, 20 ) );
             toolbar.setPreferredSize( new Dimension( 400, 20 ) );
             toolbar.setBorder( BorderFactory.createEmptyBorder() );
             toolbar.setBackground( Color.WHITE );
 
         }
 
         @Override
         public Component getTreeCellRendererComponent( JTree tree, Object value, boolean isSelected, boolean expanded,
                                                        boolean leaf, int row, boolean hasFocus ) {
             isTargetNode = ( value == dropTargetNode );
             isTargetNodeLeaf = ( isTargetNode && ( (TreeNode) value ).isLeaf() );
 
             Component result = this;
             if ( value instanceof LayerNode || value instanceof LayerGroupNode ) {
                 setEnabled( tree.isEnabled() );
 
                 TreeLabel label_ = null;
                 if ( value instanceof LayerNode ) {
                     LayerNode ln = ( (LayerNode) value );
                     label_ = ( (LayerNode) value ).getTreeLabel();
                     // select tree node if layer is selected for action
                    label_.setSelected( ( (Layer) ln.getUserObject() ).getSelectedFor().contains(
                                                                                                  MapModel.SELECTION_ACTION ) );
                 } else if ( value instanceof LayerGroupNode ) {
                     label_ = ( (LayerGroupNode) value ).getTreeLabel();
                     label_.setSelected( false );
                 }
                 label_.setFocus( hasFocus );
                 label_.setTree( tree );
                 if ( isTargetNodeLeaf ) {
                     toolbar.setBackground( Color.LIGHT_GRAY );
                     if ( top ) {
                         toolbar.setBorder( BorderFactory.createMatteBorder( 2, 0, 0, 0, borderColor ) );
                     } else {
                         toolbar.setBorder( BorderFactory.createMatteBorder( 0, 0, 2, 0, borderColor ) );
                     }
                 } else {
                     toolbar.setBackground( Color.WHITE );
                     toolbar.setBorder( BorderFactory.createEmptyBorder() );
                 }
 
                 toolbar.removeAll();
                 Object o = ( (CheckNode) value ).getUserObject();
                 check.setSelected( ( (MapModelEntry) o ).isVisible() );
                 toolbar.add( check );
                 if ( ( (MapModelEntry) o ).getSelectedFor().contains( MapModel.SELECTION_EDITING ) ) {
                     toolbar.add( new JLabel( IconRegistry.getIcon( "pen.gif" ) ) );
                 }
                 toolbar.add( label_ );
                 result = toolbar;
             } else if ( ( (DefaultMutableTreeNode) value ).isRoot() ) {
                 String stringValue = tree.convertValueToText( value, isSelected, expanded, leaf, row, hasFocus );
                 label.setIcon( IconRegistry.getIcon( "layers.png" ) );
                 label.setText( stringValue );
                 result = label;
             } else {
                 // default
                 String stringValue = tree.convertValueToText( value, isSelected, expanded, leaf, row, hasFocus );
                 label.setText( stringValue );
                 result = label;
             }
 
             return result;
         }
 
         @Override
         public Dimension getPreferredSize() {
             Dimension d_check = check.getPreferredSize();
             return new Dimension( 400, d_check.height );
 
         }
 
         @Override
         public Dimension getMinimumSize() {
             return getPreferredSize();
         }
 
         @Override
         public void doLayout() {
             Dimension d_check = check.getPreferredSize();
             check.setBounds( 0, 0, d_check.width, d_check.height );
         }
 
     }
 
     /**
      * 
      * 
      * 
      * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
      * @author last edited by: $Author$
      * 
      * @version. $Revision$, $Date$
      */
     public static class TreeLabel extends JLabel implements ChangeListener {
 
         private static final long serialVersionUID = -3798337514877725403L;
 
         private boolean isSelected;
 
         private boolean hasFocus;
 
         private MapModelEntry mme;
 
         private JTree tree;
 
         private static Font EDITFONT = new Font( "ARIAL", Font.BOLD, 12 );
 
         /**
          * 
          * @param mme
          */
         public TreeLabel( MapModelEntry mme ) {
             this.mme = mme;
             if ( mme instanceof Layer ) {
                 setIcon( IconRegistry.getIcon( "layer.png" ) );
             } else {
                 setIcon( IconRegistry.getIcon( "layers.png" ) );
             }
             mme.addChangeListener( this );
             setText( mme.getTitle() );
         }
 
         /**
          * @param
          */
         public void setBackground( Color color ) {
             if ( color instanceof ColorUIResource )
                 color = null;
             super.setBackground( color );
         }
 
         public void setTree( JTree tree ) {
             this.tree = tree;
         }
 
         @Override
         public void paint( Graphics g ) {
             if ( mme != null ) {
                 setText( mme.getTitle() );
             }
             setForeground( Color.BLACK );
             if ( mme instanceof Layer ) {
                 Layer layer = ( (Layer) mme );
                 if ( layer.getSelectedFor().contains( MapModel.SELECTION_EDITING ) ) {
                     setForeground( Color.RED );
                     setFont( EDITFONT );
                 } else {
                     setFont( tree.getFont() );
                 }
                 double min = layer.getMinScaleDenominator();
                 double max = layer.getMaxScaleDenominator();
                 double sc = layer.getOwner().getScaleDenominator();
                 if ( min > sc || max < sc ) {
                     setForeground( Color.LIGHT_GRAY );
                 }
             }
             String str;
             if ( ( str = getText() ) != null && 0 < str.length() ) {
                 if ( isSelected ) {
                     g.setColor( Color.ORANGE );
                 } else {
                     g.setColor( new Color( 0, 0, 0, 0 ) );
                 }
                 Dimension d = getPreferredSize();
                 int imageOffset = 0;
                 Icon currentI = getIcon();
                 if ( currentI != null ) {
                     imageOffset = currentI.getIconWidth() + Math.max( 0, getIconTextGap() - 1 );
                 }
                 g.fillRect( imageOffset, 0, d.width - 1 - imageOffset, d.height + 3 );
                 if ( hasFocus ) {
                     g.setColor( UIManager.getColor( "Tree.selectionBorderColor" ) );
                     g.drawRect( imageOffset, 0, d.width - 1 - imageOffset, d.height - 1 + 3 );
                 }
             }
             super.paint( g );
         }
 
         /**
          * 
          * @param isSelected
          */
         public void setSelected( boolean isSelected ) {
             this.isSelected = isSelected;
         }
 
         /**
          * 
          * @param hasFocus
          */
         public void setFocus( boolean hasFocus ) {
             this.hasFocus = hasFocus;
         }
 
         /*
          * (non-Javadoc)
          * 
          * @see org.deegree.igeo.ChangeListener#valueChanged(org.deegree.igeo.ValueChangedEvent)
          */
         public void valueChanged( ValueChangedEvent event ) {
             if ( event instanceof LayerChangedEvent ) {
 
                 LayerChangedEvent e = (LayerChangedEvent) event;
                 if ( e.getSource().equals( mme ) && e.getEmbeddedEvent() instanceof AdapterEvent ) {
                     AdapterEvent ae = (AdapterEvent) e.getEmbeddedEvent();
                     if ( ae.getType() == ADAPTER_EVENT_TYPE.startedLoading ) {
                         setIcon( IconRegistry.getIcon( "clock.gif" ) );
                     } else if ( ae.getType() == ADAPTER_EVENT_TYPE.finishedLoading ) {
                         setIcon( IconRegistry.getIcon( "layer.png" ) );
                     } else {
                         setIcon( IconRegistry.getIcon( "cancel.png" ) );
                     }
                     if ( tree != null ) {
                         tree.repaint();
                     }
                 }
             }
         }
 
     }
 
 }
