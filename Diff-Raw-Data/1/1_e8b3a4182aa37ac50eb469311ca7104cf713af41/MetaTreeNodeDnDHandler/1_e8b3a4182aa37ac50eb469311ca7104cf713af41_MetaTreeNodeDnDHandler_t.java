 /*
  * MetaTreeNodeDnDHandler.java
  *
  * Created on 16. September 2004, 09:47
  */
 
 package Sirius.navigator.ui.dnd;
 
 import java.awt.datatransfer.*;
 import java.awt.dnd.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.image.*;
 import javax.swing.tree.*;
 import javax.swing.*;
 import java.awt.geom.*;
 
 import org.apache.log4j.Logger;
 
 import Sirius.navigator.ui.tree.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.connection.*;
 import Sirius.navigator.ui.*;
 import Sirius.navigator.method.*;
 import Sirius.server.newuser.permission.*;
 import java.awt.event.MouseAdapter;
 import java.util.Vector;
 
 /**
  *
  * @author  pascal
  */
 public class MetaTreeNodeDnDHandler implements DragGestureListener, DropTargetListener, DragSourceListener{
     private final Logger logger;
     private final ResourceManager resources;
     
     private final MetaCatalogueTree metaTree;
     private final DragSource dragSource;
     
     private MetaTransferable metaTransferable;
     
     private Point dragPoint = new Point();
     private BufferedImage dragImage;
     
     // Fields...
     private TreePath _pathLast		= null;
     private Rectangle2D 	_raCueLine		= new Rectangle2D.Float();
     private Rectangle2D 	_raGhost		= new Rectangle2D.Float();
     private Color			_colorCueLine;
     private Point			_ptLast			= new Point();
     private TreePath[] dragPaths=new TreePath[0];
     private Vector<MutableTreeNode> draggedNodes =new Vector<MutableTreeNode>();
 //    private TreePath[] cachedTreePaths; //DND Fehlverhalten Workaround
     
      private TreePath[] cachedTreePaths; //DND Fehlverhalten Workaround
     
     
     public MetaTreeNodeDnDHandler(final MetaCatalogueTree metaTree) {
         this.logger = Logger.getLogger(this.getClass());
         this.resources = ResourceManager.getManager();
        
         logger.info("MetaTreeNodeDnDHandler() creating new instance. Drag Image Support: " + DragSource.isDragImageSupported());
         
         this.metaTree = metaTree;
         this.dragSource = DragSource.getDefaultDragSource();
         metaTree.addMouseListener(new MouseAdapter() {                                       //DND Fehlverhalten Workaround
                 public void mouseReleased(MouseEvent e) {                           //DND Fehlverhalten Workaround
                     cachedTreePaths= metaTree.getSelectionModel().getSelectionPaths();       //DND Fehlverhalten Workaround
                 }
             });
 //        metaTree.addMouseListener(new MouseAdapter() {                                       //DND Fehlverhalten Workaround
 //                public void mouseReleased(MouseEvent e) {                           //DND Fehlverhalten Workaround
 //                    cachedTreePaths= metaTree.getSelectionPaths();       //DND Fehlverhalten Workaround
 //                }
 //            });
         int sourceActions = DnDConstants.ACTION_COPY_OR_MOVE;
 //        if(this.metaTree instanceof SearchResultsTree) {
 //            sourceActions = DnDConstants.ACTION_COPY;// & InputEvent.BUTTON3_MASK;
 //        } else {
 //            sourceActions = DnDConstants.ACTION_COPY_OR_MOVE + DnDConstants.ACTION_LINK;// & InputEvent.BUTTON3_MASK;
 //        }
         
         _colorCueLine = new Color(SystemColor.controlShadow.getRed(), SystemColor.controlShadow.getGreen(),SystemColor.controlShadow.getBlue(), 64);
         
         DragGestureRecognizer dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer(this.metaTree, sourceActions , this);
 //        if(!(this.metaTree instanceof SearchResultsTree)) {
 //            DropTarget dropTarget = new DropTarget(this.metaTree, this);
 //        }
     }
     
     public void dragGestureRecognized(DragGestureEvent dge) {
 //        metaTree.setSelectionPaths(cachedTreePaths); //DND Fehlverhalten Workaround
 //        TreePath selPath = metaTree.getPathForLocation((int)dge.getDragOrigin().getX(),(int)dge.getDragOrigin().getY());//DND Fehlverhalten Workaround
 //
 //        if ((dge.getTriggerEvent().getModifiers()& (dge.getTriggerEvent().CTRL_MASK | dge.getTriggerEvent().SHIFT_MASK) ) !=0) {//DND Fehlverhalten Workaround
 //            metaTree.setSelectionPaths(cachedTreePaths); //DND Fehlverhalten Workaround /
 //            metaTree.addSelectionPath(selPath);          //DND Fehlverhalten Workaround
 //            cachedTreePaths= metaTree.getSelectionPaths();//DND Fehlverhalten Workaround
 //        } else {
 //            metaTree.setSelectionPath(selPath);//DND Fehlverhalten Workaround
 //        }
         
         if(logger.isDebugEnabled())logger.debug("dragGestureRecognized()");
         
         metaTree.getSelectionModel().setSelectionPaths(cachedTreePaths); //DND Fehlverhalten Workaround
             TreePath selPath = metaTree.getPathForLocation((int)dge.getDragOrigin().getX(),(int)dge.getDragOrigin().getY());//DND Fehlverhalten Workaround
             
             if ((dge.getTriggerEvent().getModifiers()& (dge.getTriggerEvent().CTRL_MASK | dge.getTriggerEvent().SHIFT_MASK) ) !=0) {//DND Fehlverhalten Workaround
                 metaTree.getSelectionModel().setSelectionPaths(cachedTreePaths); //DND Fehlverhalten Workaround /
                 metaTree.getSelectionModel().addSelectionPath(selPath);          //DND Fehlverhalten Workaround
                 cachedTreePaths=metaTree.getSelectionModel().getSelectionPaths();//DND Fehlverhalten Workaround
             } else {
                 metaTree.getSelectionModel().setSelectionPath(selPath);//DND Fehlverhalten Workaround
             }
         
         
         if(this.metaTree.getSelectedNode() !=  null) {
             
             //draggedNode=metaTree.getSelectedNode();
             Point dragOrigin = dge.getDragOrigin();
             TreePath treePath = this.metaTree.getPathForLocation(dragOrigin.x, dragOrigin.y);
             //
             dragPaths=metaTree.getSelectionPaths();
             Rectangle pathBounds = this.metaTree.getPathBounds(treePath);
             this.dragPoint.setLocation(dragOrigin.x - pathBounds.x, dragOrigin.y - pathBounds.y);
             
           metaTree.setDragImage(this.getDragImage(metaTree));
             
             this.metaTransferable = new MetaTreeNodeTransferable(this.metaTree);
             this.metaTransferable.setTransferAction(dge.getDragAction());
             this.dragSource.startDrag(dge, this.getCursor(dge.getDragAction()), this.getDragImage(), this.dragPoint, this.metaTransferable, this);
         } else if(logger.isDebugEnabled()) {
             logger.warn("dragGestureRecognized() no valid selection for DnD operation");
         }
     }
     
     public void drop(DropTargetDropEvent dtde) {
         if(logger.isDebugEnabled())logger.debug("drop()");
         metaTree.repaint();
         
         
         boolean nodeEditable = false;
         
         String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
         
         //Permission p = SessionManager.getSession().getWritePermission();
         
         try {
             nodeEditable = this.metaTree.getSelectedNode().isEditable(key, PermissionHolder.WRITEPERMISSION);
         }catch(Exception e){logger.debug(" Knoten nicht editierbar");}
         
         if(this.metaTree.isEditable()&& nodeEditable) {
             Transferable transferable = dtde.getTransferable();
             
             try {
                 if(transferable.isDataFlavorSupported(MetaTreeNodeTransferable.dataFlavors[0])) {
                     Point location = dtde.getLocation();
                     TreePath destinationPath = this.metaTree.getClosestPathForLocation(location.x, location.y);
                     DefaultMetaTreeNode sourceNode = (DefaultMetaTreeNode)transferable.getTransferData(MetaTreeNodeTransferable.dataFlavors[0]);
                     TreePath sourcePath = new TreePath(sourceNode.getPath());
                     
                     logger.debug("drop(): performing dnd operation: " + sourceNode + " -> " + destinationPath);
                     if(this.checkDestination(destinationPath, sourcePath, dtde.getDropAction())) {
                         DefaultMetaTreeNode destinationNode = (DefaultMetaTreeNode)destinationPath.getLastPathComponent();
                         
                         // los geht's ...
                         switch(dtde.getDropAction()) {
                             case DnDConstants.ACTION_COPY:
                                 if(MethodManager.getManager().checkPermission(destinationNode.getNode(), PermissionHolder.WRITEPERMISSION)) {
                                     dtde.dropComplete(MethodManager.getManager().copyNode(this.metaTree, destinationNode, sourceNode));
                                 }
                                 break;
                                 
                             case DnDConstants.ACTION_MOVE:
                                 if(MethodManager.getManager().checkPermission(destinationNode.getNode(),  PermissionHolder.WRITEPERMISSION)) {
                                     if(MethodManager.getManager().checkPermission(sourceNode.getNode(),  PermissionHolder.WRITEPERMISSION)) {
                                         dtde.dropComplete(MethodManager.getManager().moveNode(this.metaTree, destinationNode, sourceNode));
                                     }
                                 }
                                 break;
                                 
                             case DnDConstants.ACTION_LINK:
                                 dtde.dropComplete(true);
                                 if(MethodManager.getManager().checkPermission(destinationNode.getNode(),  PermissionHolder.WRITEPERMISSION)) {
                                     if(MethodManager.getManager().checkPermission(sourceNode.getNode(),  PermissionHolder.WRITEPERMISSION)) {
                                         dtde.dropComplete(MethodManager.getManager().linkNode(this.metaTree, destinationNode, sourceNode));
                                     }
                                 }
                                 break;
                                 
                             default:     logger.error("unsupported dnd operation: " + dtde.getDropAction());
                             JOptionPane.showMessageDialog(this.metaTree, resources.getString("tree.dnd.error.unsupported.operation"), resources.getString("tree.dnd.error.unsupported.operation.title"), JOptionPane.ERROR_MESSAGE);
                             dtde.rejectDrop();
                         }
                     } else {
                         dtde.rejectDrop();
                     }
                 } else {
                     throw new UnsupportedFlavorException(MetaTreeNodeTransferable.dataFlavors[0]);
                 }
             } catch(Throwable t) {
                 logger.warn("data flavour '" + MetaTreeNodeTransferable.dataFlavors[0].getHumanPresentableName() + "' is not supported, rejecting dnd", t);
                 
                 JOptionPane.showMessageDialog(this.metaTree, resources.getString("tree.dnd.error.unsupported.object"), resources.getString("tree.dnd.error.unsupported.object.title"), JOptionPane.ERROR_MESSAGE);
                 dtde.rejectDrop();
             }
         } else {
             logger.error("catalog is not editable");
             JOptionPane.showMessageDialog(this.metaTree, resources.getString("tree.dnd.error.readonly"), resources.getString("tree.dnd.error.readonly.title"), JOptionPane.ERROR_MESSAGE);
             dtde.rejectDrop();
         }
     }
     
     public void dragDropEnd(DragSourceDropEvent dsde) {
         if(logger.isDebugEnabled())logger.debug("dragDropEnd()");
         
         // ignorieren, passiert schon in drop() ...
             /*if(dsde.getDropAction() == DnDConstants.ACTION_MOVE)
             {
                 logger.warn("dragDropEnd() moving nodes");
             }*/
     }
     private int insertAreaHeight = 8;
     private Rectangle lastRowBounds;
     private Object lastNode=null;
     private long timestamp=0;
     private void markNode(JTree tree, Point location) {
         TreePath path = tree.getClosestPathForLocation(location.x, location.y);
         if(path != null) {
             if(lastRowBounds != null) {
                 Graphics g = tree.getGraphics();
                 g.setColor(Color.white);
                 g.drawLine(lastRowBounds.x, lastRowBounds.y,
                         lastRowBounds.x + lastRowBounds.width, lastRowBounds.y);
             }
             tree.setSelectionPath(path);
             
             //logger.fatal("path.getLastPathComponent():"+path.getLastPathComponent());
             //logger.fatal("lastNode:"+lastNode+" ? = "+lastNode==path.getLastPathComponent());
             if (!draggedNodes.contains(path.getLastPathComponent())) {
                 if (lastNode!=path.getLastPathComponent()) {
                     lastNode=path.getLastPathComponent();
                     //logger.fatal("ERSTES MAL");
                     timestamp=System.currentTimeMillis();
                 } else {
                     long value=System.currentTimeMillis()-timestamp;
                     //logger.fatal("ERNEUT:"+value);
                     if (value>1000) {
                         //     logger.fatal("EXPAND");
                         tree.expandPath(path);
                     } else {
                         //       logger.fatal("Abwarten");
                     }
                 }
             }
         }
     }
     private void paintInsertMarker(JTree tree, Point location) {
         Graphics g = tree.getGraphics();
         tree.clearSelection();
         int row = tree.getRowForPath(tree.getClosestPathForLocation(location.x, location.y));
         TreePath path = tree.getPathForRow(row);
         if(path != null) {
             Rectangle rowBounds = tree.getPathBounds(path);
             if(lastRowBounds != null) {
                 g.setColor(Color.white);
                 g.drawLine(lastRowBounds.x, lastRowBounds.y,
                         lastRowBounds.x + lastRowBounds.width, lastRowBounds.y);
             }
             if(rowBounds != null) {
                 g.setColor(Color.black);
                 g.drawLine(rowBounds.x, rowBounds.y, rowBounds.x + rowBounds.width, rowBounds.y);
             }
             lastRowBounds = rowBounds;
         }
     }
     
     
     public void updateDragMark(JTree tree, Point location) {
         int row = tree.getRowForPath(
                 tree.getClosestPathForLocation(location.x, location.y));
         TreePath path = tree.getPathForRow(row);
         if(path != null) {
             Rectangle rowBounds = tree.getPathBounds(path);
             int rby = rowBounds.y;
             int topBottomDist = insertAreaHeight / 2;
             Point topBottom = new Point(
                     rby - topBottomDist, rby + topBottomDist);
             if(topBottom.x <= location.y && topBottom.y >= location.y) {
                 paintInsertMarker(tree, location);
             } else {
                 markNode(tree, location);
             }
         }
     }
      public BufferedImage getDragImage(JTree tree) {
         try {
             TreeCellRenderer r = tree.getCellRenderer();
             DefaultTreeModel m = (DefaultTreeModel)tree.getModel();
             Vector<BufferedImage> labels=new Vector<BufferedImage>();
             int height=0;
             int maxWidth=0;
             for (int i=0;i<dragPaths.length;++i) {
                 BufferedImage image = null;
                 Rectangle pathBounds = tree.getPathBounds(dragPaths[i]);
                 boolean nIsLeaf = m.isLeaf(dragPaths[i].getLastPathComponent());
                 JComponent lbl = (JComponent)r.getTreeCellRendererComponent(
                         tree,dragPaths[i].getLastPathComponent(), false ,
                         tree.isExpanded(dragPaths[i]), nIsLeaf, 0,false);
                 lbl.setBounds(pathBounds);
                 lbl.setOpaque(false);
                 height+=lbl.getHeight();
                 if (lbl.getWidth()>maxWidth){
                     maxWidth=lbl.getWidth();
                 }
                 image = new BufferedImage(lbl.getWidth(), lbl.getHeight(),
                         java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
                 Graphics2D graphics = image.createGraphics();
                 graphics.setComposite(
                         AlphaComposite.getInstance(
                         AlphaComposite.SRC_OVER, 0.5f));
                 lbl.paint(graphics);
                 graphics.dispose();
                 labels.add(image);
             }
             BufferedImage master=new BufferedImage(maxWidth, height,java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
             Graphics2D graphics = master.createGraphics();
             int h=0;
             for (BufferedImage ii:labels) {
                 graphics.drawImage(ii,0,h,null);
                 h+=ii.getHeight();
             }
             graphics.dispose();
             return master;
         } catch (Exception e) {
             logger.error("Fehler beim Erstellen des DragImages",e);
             return null;
         }
 //        try {
 //            if (dragPath != null) {
 //                Rectangle pathBounds = tree.getPathBounds(dragPath);
 //                TreeCellRenderer r = tree.getCellRenderer();
 //                DefaultTreeModel m = (DefaultTreeModel)tree.getModel();
 //                boolean nIsLeaf = m.isLeaf(dragPath.getLastPathComponent());
 //                JComponent lbl = (JComponent)r.getTreeCellRendererComponent(
 //                        tree,draggedNode, false ,
 //                        tree.isExpanded(dragPath), nIsLeaf, 0,false);
 //                lbl.setBounds(pathBounds);
 //                image = new BufferedImage(lbl.getWidth(), lbl.getHeight(),
 //                        java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
 //                Graphics2D graphics = image.createGraphics();
 //                graphics.setComposite(
 //                        AlphaComposite.getInstance(
 //                        AlphaComposite.SRC_OVER, 0.5f));
 //                lbl.setOpaque(false);
 //                lbl.paint(graphics);
 //                graphics.dispose();
 //            }
 //        } catch (RuntimeException re) {}
 //        return image;
     }
     private Rectangle rect2D = new Rectangle();
     private final void paintImage(JTree tree, Point pt) {
         BufferedImage image = getDragImage(tree);
         if(image != null) {
             tree.paintImmediately(rect2D.getBounds());
             rect2D.setRect((int) pt.getX()-15,
                     (int) pt.getY()-15,image.getWidth(),image.getHeight());
             tree.getGraphics().drawImage(image,
                     (int) pt.getX()-15,(int) pt.getY()-15,tree);
         }
     }
     public void dragOver(DropTargetDragEvent e) {
         
         
         JTree tree = (JTree) e.getDropTargetContext().getComponent();
         Point loc = e.getLocation();
         updateDragMark(tree, loc);
         paintImage(tree,loc);
 //        // Even if the mouse is not moving, this method is still invoked 10 times per second
 //        Point pt = e.getLocation();
 //        if (pt.equals(_ptLast))
 //            return;
 //
 //        _ptLast = pt;
 //
 //
 //        Graphics2D g2 = (Graphics2D) this.metaTree.getGraphics();
 //
 //        // If a drag image is not supported by the platform, then draw my own drag image
 //        if (!DragSource.isDragImageSupported()) {
 //            this.metaTree.paintImmediately(_raGhost.getBounds());	// Rub out the last ghost image and cue line
 //            // And remember where we are about to draw the new ghost image
 //            _raGhost.setRect(pt.x - this.dragPoint.x, pt.y - this.dragPoint.y, this.dragImage.getWidth(), this.dragImage.getHeight());
 //            g2.drawImage(this.dragImage, AffineTransform.getTranslateInstance(_raGhost.getX(), _raGhost.getY()), null);
 //        } else	// Just rub out the last cue line
 //            this.metaTree.paintImmediately(_raCueLine.getBounds());
 //
 //
 //
 //        TreePath path = this.metaTree.getClosestPathForLocation(pt.x, pt.y);
 //        if (!(path == _pathLast)) {
 //            _pathLast = path;
 //            //this.metaTree.setSelectionPath(path);
 //        }
 //
 //        // In any case draw (over the ghost image if necessary) a cue line indicating where a drop will occur
 //        Rectangle raPath = this.metaTree.getPathBounds(path);
 //        _raCueLine.setRect(0,  raPath.y+(int)raPath.getHeight(), this.metaTree.getWidth(), 2);
 //
 //        g2.setColor(_colorCueLine);
 //        g2.fill(_raCueLine);
 //
 //
 //        // And include the cue line in the area to be rubbed out next time
 //        _raGhost = _raGhost.createUnion(_raCueLine);
     }
     
     public void dragOver(DragSourceDragEvent dsde) {
         //if(logger.isDebugEnabled())logger.debug("dragOver(DragSourceDragEvent)");
     }
     
     public void dragEnter(DropTargetDragEvent dtde) {
         if(logger.isDebugEnabled())logger.debug("dragEnter(DropTargetDragEvent)");
         logger.info(dtde.getSource());
         //this.thisTarget = true;
     }
     
     public void dragEnter(DragSourceDragEvent dsde) {
         if(logger.isDebugEnabled())logger.debug("dragEnter(DragSourceDragEvent)");
         
         DragSourceContext dragSourceContext = dsde.getDragSourceContext();
         dragSourceContext.setCursor(this.getCursor(dsde.getDropAction()));
     }
     
     public void dragExit(DragSourceEvent dse) {
         //if(logger.isDebugEnabled())logger.debug("dragExit(DragSourceEvent)");
         dse.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
         metaTree.setSelectionPaths(dragPaths);
         
     }
     
     public void dragExit(DropTargetEvent dte) {
         //if(logger.isDebugEnabled())logger.debug("dragExit(DropTargetEvent)");
         //this.dragSourceContext.setCursor(DragSource.DefaultCopyNoDrop);
         //this.thisTarget = false;
         if (!DragSource.isDragImageSupported()) {
             this.metaTree.repaint(); //this.dragImage.);
         }
         
     }
     
     public void dropActionChanged(DropTargetDragEvent dtde) {
         if(logger.isDebugEnabled())logger.debug("dropActionChanged(DropTargetDragEvent)");
     }
     
     public void dropActionChanged(DragSourceDragEvent dsde) {
         if(logger.isDebugEnabled())logger.debug("dropActionChanged(DragSourceDragEvent)");
         
         DragSourceContext dragSourceContext = dsde.getDragSourceContext();
         dragSourceContext.setCursor(this.getCursor(dsde.getUserAction()));
         this.metaTransferable.setTransferAction(dsde.getUserAction());
     }
     
     private Cursor getCursor(int dragAction) {
         Cursor cursor = DragSource.DefaultCopyNoDrop;
         if((dragAction & DnDConstants.ACTION_MOVE) != 0) {
             if(logger.isDebugEnabled())logger.debug("getCursor(): ACTION_MOVE");
             cursor = DragSource.DefaultMoveDrop;
         } else if((dragAction & DnDConstants.ACTION_COPY) != 0) {
             if(logger.isDebugEnabled())logger.debug("getCursor(): ACTION_COPY");
             cursor = DragSource.DefaultCopyDrop;
         } else if((dragAction & DnDConstants.ACTION_LINK) != 0) {
             if(logger.isDebugEnabled())logger.debug("getCursor(): ACTION_LINK");
             cursor = DragSource.DefaultLinkDrop;
         }
         
         return cursor;
     }
     
     private boolean checkDestination(TreePath destinationPath, TreePath sourcePath, int dropAction) {
         if(destinationPath != null) {
             if(destinationPath.getLastPathComponent() instanceof PureTreeNode || destinationPath.getLastPathComponent() instanceof ObjectTreeNode) {
                 if(destinationPath.equals(sourcePath)) {
                     logger.warn("destination path equals source path");
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), resources.getString("tree.dnd.error.path.equal.1") + destinationPath.getLastPathComponent() + resources.getString("tree.dnd.error.path.equal.2"), resources.getString("tree.dnd.error.path.equal.title"), JOptionPane.WARNING_MESSAGE);
                 } else if(sourcePath.isDescendant(destinationPath)) {
                     logger.warn("destination path can not be a descendant of the source path");
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), resources.getString("tree.dnd.error.path.descendant.1") + destinationPath.getLastPathComponent() + resources.getString("tree.dnd.error.path.descendant.2"), resources.getString("tree.dnd.error.path.descendant.title"), JOptionPane.WARNING_MESSAGE);
                 } else if(dropAction != DnDConstants.ACTION_COPY && sourcePath.getParentPath().equals(destinationPath)) {
                     logger.warn("destination node is the parent of the source node");
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), resources.getString("tree.dnd.error.path.parent.1") + destinationPath.getLastPathComponent() + resources.getString("tree.dnd.error.path.parent.2"), resources.getString("tree.dnd.error.path.parent.title"), JOptionPane.WARNING_MESSAGE);
                 } else {
                     if(logger.isDebugEnabled())logger.debug("checkDestination() dnd destination ok: " + sourcePath.getLastPathComponent() + " -> " + destinationPath.getLastPathComponent());
                     return true;
                 }
             } else {
                 logger.warn("destination node '" + destinationPath.getLastPathComponent() + "' is no pure or object node");
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(), resources.getString("tree.dnd.error.node.1") + destinationPath.getLastPathComponent() + resources.getString("tree.dnd.error.node.2"), resources.getString("tree.dnd.error.node.title"), JOptionPane.WARNING_MESSAGE);
             }
         } else {
             logger.warn("no node found at this location");
         }
         
         return false;
     }
     
 //    private BufferedImage getDragImage(TreePath treePath, TreeNode treeNode, Rectangle pathBounds) {
 //        // Get the cell renderer (which is a JLabel) for the path being dragged
 //        JLabel cellRenderer = (JLabel) this.metaTree.getCellRenderer().getTreeCellRendererComponent(this.metaTree, treeNode, false, this.metaTree.isExpanded(treePath), treeNode.isLeaf(), 0, false);
 //        cellRenderer.setSize((int)pathBounds.getWidth(), (int)pathBounds.getHeight()); // <-- The layout manager would normally do this
 //        
 //        // Get a buffered image of the selection for dragging a ghost image
 //        BufferedImage dragImage = new BufferedImage((int)pathBounds.getWidth(), (int)pathBounds.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
 //        Graphics2D g2 = dragImage.createGraphics();
 //        
 //        // Ask the cell renderer to paint itself into the BufferedImage
 //        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));		// Make the image ghostlike
 //        cellRenderer.paint(g2);
 //        
 //        // Now paint a gradient UNDER the ghosted JLabel text (but not under the icon if any)
 //        // Note: this will need tweaking if your icon is not positioned to the left of the text
 //        Icon icon = cellRenderer.getIcon();
 //        int nStartOfText = (icon == null) ? 0 : icon.getIconWidth() + cellRenderer.getIconTextGap();
 //        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f));	// Make the gradient ghostlike
 //        g2.setPaint(new GradientPaint(nStartOfText, 0, SystemColor.controlShadow, this.metaTree.getWidth(), 0, new Color(255,255,255,0)));
 //        g2.fillRect(nStartOfText, 0, this.metaTree.getWidth(), dragImage.getHeight());
 //        g2.dispose();
 //        
 //        return dragImage;
 //    }
 
     public BufferedImage getDragImage() {
         return metaTree.getDragImage();
     }
 
     public void setDragImage(BufferedImage dragImage) {
         this.dragImage = dragImage;
         metaTree.setDragImage(dragImage);
     }
 
     
 }
