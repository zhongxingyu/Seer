 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * MetaTreeNodeDnDHandler.java
  *
  * Created on 16. September 2004, 09:47
  */
 package Sirius.navigator.ui.dnd;
 
 import Sirius.navigator.connection.*;
 import Sirius.navigator.method.*;
 import Sirius.navigator.resource.*;
 import Sirius.navigator.types.treenode.*;
 import Sirius.navigator.ui.*;
 import Sirius.navigator.ui.tree.*;
 
 import Sirius.server.newuser.permission.*;
 
 import org.apache.log4j.Logger;
 
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.awt.dnd.*;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.*;
 
 /**
  * DOCUMENT ME!
  *
  * @author   pascal
  * @version  $Revision$, $Date$
  */
 public class MetaTreeNodeDnDHandler implements DragGestureListener, DropTargetListener, DragSourceListener {
 
     //~ Instance fields --------------------------------------------------------
 
     private final Logger logger;
 
     private final MetaCatalogueTree metaTree;
     private final DragSource dragSource;
 
     private MetaTransferable metaTransferable;
 
     private Point dragPoint = new Point();
     private BufferedImage dragImage;
 
     // Fields...
     private TreePath _pathLast = null;
     private Rectangle2D _raCueLine = new Rectangle2D.Float();
     private Rectangle2D _raGhost = new Rectangle2D.Float();
     private Color _colorCueLine;
     private Point _ptLast = new Point();
     private TreePath[] dragPaths = new TreePath[0];
     private Vector<MutableTreeNode> draggedNodes = new Vector<MutableTreeNode>();
 //    private TreePath[] cachedTreePaths; //DND Fehlverhalten Workaround
 
     private TreePath[] cachedTreePaths; // DND Fehlverhalten Workaround
     private TreePath[] lastCachedTreePaths; // DND Fehlverhalten Workaround
     private boolean autoSelection = false;
     private boolean valueChanged = false;
     private int insertAreaHeight = 8;
     private Rectangle lastRowBounds;
     private Object lastNode = null;
     private long timestamp = 0;
     private Rectangle rect2D = new Rectangle();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new MetaTreeNodeDnDHandler object.
      *
      * @param  metaTree  DOCUMENT ME!
      */
     public MetaTreeNodeDnDHandler(final MetaCatalogueTree metaTree) {
         this.logger = Logger.getLogger(this.getClass());
 
         if (logger.isInfoEnabled()) {
             logger.info("MetaTreeNodeDnDHandler() creating new instance. Drag Image Support: "
                         + DragSource.isDragImageSupported()); // NOI18N
         }
 
         this.metaTree = metaTree;
         this.dragSource = DragSource.getDefaultDragSource();
         metaTree.addMouseListener(new MouseAdapter() {
 
             @Override
             public void mousePressed(MouseEvent e) {
                 if (!valueChanged) {
                     lastCachedTreePaths = cachedTreePaths;
                 }
                 valueChanged = false;
             }
             
         });
         metaTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
             //DND Fehlverhalten Workaround
             @Override
             public void valueChanged(TreeSelectionEvent e) {
                 if (autoSelection) {
                     return;
                 }
                 java.util.List<TreePath> path = new ArrayList<TreePath>();;
                 valueChanged = true;
                 
                 if (cachedTreePaths != null) {
                     path.addAll( Arrays.asList(cachedTreePaths) );
                 }
                 
                 for (TreePath tmpPath : e.getPaths()) {
                     if (e.isAddedPath(tmpPath) ) {
                         path.add(tmpPath);
                     } else {
                         path.remove(tmpPath);
                     }
                 }
                 lastCachedTreePaths = cachedTreePaths;
                 cachedTreePaths = path.toArray(new TreePath[path.size()]);
             }
         });
 
         final int sourceActions = DnDConstants.ACTION_COPY_OR_MOVE;
 
         _colorCueLine = new Color(SystemColor.controlShadow.getRed(),
                 SystemColor.controlShadow.getGreen(),
                 SystemColor.controlShadow.getBlue(),
                 64);
 
         final DragGestureRecognizer dragGestureRecognizer = dragSource.createDefaultDragGestureRecognizer(
                 this.metaTree,
                 sourceActions,
                 this);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void dragGestureRecognized(final DragGestureEvent dge) {
         if (logger.isDebugEnabled()) {
             logger.debug("dragGestureRecognized()"); // NOI18N
         }
 
         metaTree.getSelectionModel().setSelectionPaths(cachedTreePaths); // DND Fehlverhalten Workaround
         final TreePath selPath = metaTree.getPathForLocation((int)dge.getDragOrigin().getX(),
                 (int)dge.getDragOrigin().getY());                        // DND Fehlverhalten Workaround
 
         autoSelection = true;
         if ((dge.getTriggerEvent().getModifiers()
                         & (dge.getTriggerEvent().CTRL_MASK)) != 0) {                                    // DND Fehlverhalten Workaround
             metaTree.getSelectionModel().setSelectionPaths(cachedTreePaths);                            // DND Fehlverhalten Workaround /
             metaTree.getSelectionModel().addSelectionPath(selPath);                                     // DND Fehlverhalten Workaround
             cachedTreePaths = metaTree.getSelectionModel().getSelectionPaths();                         // DND Fehlverhalten Workaround
         } else if ((dge.getTriggerEvent().getModifiers() & dge.getTriggerEvent().SHIFT_MASK) != 0) {
             metaTree.getSelectionModel().addSelectionPaths(cachedTreePaths);                            // DND Fehlverhalten Workaround
             cachedTreePaths = metaTree.getSelectionModel().getSelectionPaths();                         // DND Fehlverhalten Workaround
         } else {
             if (contains(lastCachedTreePaths, selPath)) {
                 metaTree.getSelectionModel().setSelectionPaths(lastCachedTreePaths);                    // DND Fehlverhalten Workaround
                 cachedTreePaths = lastCachedTreePaths;
             }
         }
         autoSelection = false;
 
         if (this.metaTree.getSelectedNode() != null) {
             // draggedNode=metaTree.getSelectedNode();
             final Point dragOrigin = dge.getDragOrigin();
             final TreePath treePath = this.metaTree.getPathForLocation(dragOrigin.x, dragOrigin.y);
             //
             dragPaths = metaTree.getSelectionPaths();
             final Rectangle pathBounds = this.metaTree.getPathBounds(treePath);
             this.dragPoint.setLocation(dragOrigin.x - pathBounds.x, dragOrigin.y - pathBounds.y);
 
             metaTree.setDragImage(this.getDragImage(metaTree));
 
             this.metaTransferable = new MetaTreeNodeTransferable(this.metaTree);
             this.metaTransferable.setTransferAction(dge.getDragAction());
             this.dragSource.startDrag(
                 dge,
                 this.getCursor(dge.getDragAction()),
                 this.getDragImage(),
                 new Point(10, 0),
                 this.metaTransferable,
                 this);
         } else if (logger.isDebugEnabled()) {
             logger.warn("dragGestureRecognized() no valid selection for DnD operation"); // NOI18N
         }
     }
 
     private boolean contains(TreePath[] list, TreePath path) {
         for (TreePath tmpPath : list) {
             if (tmpPath.equals(path)) {
                 return true;
             }
         }
         
         return false;
     }
     
     @Override
     public void drop(final DropTargetDropEvent dtde) {
         if (logger.isDebugEnabled()) {
             logger.debug("drop()"); // NOI18N
         }
         metaTree.repaint();
 
         boolean nodeEditable = false;
 
         final String key = SessionManager.getSession().getUser().getUserGroup().getKey().toString();
 
         // Permission p = SessionManager.getSession().getWritePermission();
 
         try {
             nodeEditable = this.metaTree.getSelectedNode().isEditable(key, PermissionHolder.WRITEPERMISSION);
         } catch (Exception e) {
             if (logger.isDebugEnabled()) {
                 logger.debug(" Node not editable");
             }
         } // NOI18N
 
         if (this.metaTree.isEditable() && nodeEditable) {
             final Transferable transferable = dtde.getTransferable();
 
             try {
                 if (transferable.isDataFlavorSupported(MetaTreeNodeTransferable.dataFlavors[0])) {
                     final Point location = dtde.getLocation();
                     final TreePath destinationPath = this.metaTree.getClosestPathForLocation(location.x, location.y);
                     final DefaultMetaTreeNode sourceNode = (DefaultMetaTreeNode)transferable.getTransferData(
                             MetaTreeNodeTransferable.dataFlavors[0]);
                     final TreePath sourcePath = new TreePath(sourceNode.getPath());
                     if (logger.isDebugEnabled()) {
                         logger.debug("drop(): performing dnd operation: " + sourceNode + " -> " + destinationPath); // NOI18N
                     }
                     if (this.checkDestination(destinationPath, sourcePath, dtde.getDropAction())) {
                         final DefaultMetaTreeNode destinationNode = (DefaultMetaTreeNode)
                             destinationPath.getLastPathComponent();
 
                         // los geht's ...
                         switch (dtde.getDropAction()) {
                             case DnDConstants.ACTION_COPY: {
                                 if (MethodManager.getManager().checkPermission(
                                                 destinationNode.getNode(),
                                                 PermissionHolder.WRITEPERMISSION)) {
                                     dtde.dropComplete(MethodManager.getManager().copyNode(
                                             this.metaTree,
                                             destinationNode,
                                             sourceNode));
                                 }
                                 break;
                             }
 
                             case DnDConstants.ACTION_MOVE: {
                                 if (MethodManager.getManager().checkPermission(
                                                 destinationNode.getNode(),
                                                 PermissionHolder.WRITEPERMISSION)) {
                                     if (MethodManager.getManager().checkPermission(
                                                     sourceNode.getNode(),
                                                     PermissionHolder.WRITEPERMISSION)) {
                                         dtde.dropComplete(MethodManager.getManager().moveNode(
                                                 this.metaTree,
                                                 destinationNode,
                                                 sourceNode));
                                     }
                                 }
                                 break;
                             }
 
                             case DnDConstants.ACTION_LINK: {
                                 dtde.dropComplete(true);
                                 if (MethodManager.getManager().checkPermission(
                                                 destinationNode.getNode(),
                                                 PermissionHolder.WRITEPERMISSION)) {
                                     if (MethodManager.getManager().checkPermission(
                                                     sourceNode.getNode(),
                                                     PermissionHolder.WRITEPERMISSION)) {
                                         dtde.dropComplete(MethodManager.getManager().linkNode(
                                                 this.metaTree,
                                                 destinationNode,
                                                 sourceNode));
                                     }
                                 }
                                 break;
                             }
 
                             default: {
                                 logger.error("unsupported dnd operation: " + dtde.getDropAction());         // NOI18N
                                 JOptionPane.showMessageDialog(
                                     this.metaTree,
                                     org.openide.util.NbBundle.getMessage(
                                         MetaTreeNodeDnDHandler.class,
                                         "MetaTreeNodeDnDHandler.drop().unsupportedOperationError.message"), // NOI18N
                                     org.openide.util.NbBundle.getMessage(
                                         MetaTreeNodeDnDHandler.class,
                                         "MetaTreeNodeDnDHandler.drop().unsupportedOperationError.title"),   // NOI18N
                                     JOptionPane.ERROR_MESSAGE);
                                 dtde.rejectDrop();
                             }
                         }
                     } else {
                         dtde.rejectDrop();
                     }
                 } else {
                     throw new UnsupportedFlavorException(MetaTreeNodeTransferable.dataFlavors[0]);
                 }
             } catch (Throwable t) {
                 logger.warn("data flavour '" + MetaTreeNodeTransferable.dataFlavors[0].getHumanPresentableName()
                             + "' is not supported, rejecting dnd",
                     t);                                                                                     // NOI18N
 
                 JOptionPane.showMessageDialog(
                     this.metaTree,
                     org.openide.util.NbBundle.getMessage(
                         MetaTreeNodeDnDHandler.class,
                         "MetaTreeNodeDnDHandler.drop().unsupportedObjectError.message"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MetaTreeNodeDnDHandler.class,
                         "MetaTreeNodeDnDHandler.drop().unsupportedObjectError.title"), // NOI18N
                     JOptionPane.ERROR_MESSAGE);
                 dtde.rejectDrop();
             }
         } else {
             logger.error("catalog is not editable");                                   // NOI18N
             JOptionPane.showMessageDialog(
                 this.metaTree,
                 org.openide.util.NbBundle.getMessage(
                     MetaTreeNodeDnDHandler.class,
                     "MetaTreeNodeDnDHandler.drop().notEditableError.message"),         // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     MetaTreeNodeDnDHandler.class,
                     "MetaTreeNodeDnDHandler.drop().notEditableError.title"),           // NOI18N
                 JOptionPane.ERROR_MESSAGE);
             dtde.rejectDrop();
         }
     }
 
     @Override
     public void dragDropEnd(final DragSourceDropEvent dsde) {
         if (logger.isDebugEnabled()) {
             logger.debug("dragDropEnd()"); // NOI18N
         }
 
         // ignorieren, passiert schon in drop() ...
         /*if(dsde.getDropAction() == DnDConstants.ACTION_MOVE)
          * { logger.warn("dragDropEnd() moving nodes");}*/
     }
     /**
      * DOCUMENT ME!
      *
      * @param  tree      DOCUMENT ME!
      * @param  location  DOCUMENT ME!
      */
     private void markNode(final JTree tree, final Point location) {
         final TreePath path = tree.getClosestPathForLocation(location.x, location.y);
         if (path != null) {
             if (lastRowBounds != null) {
                 final Graphics g = tree.getGraphics();
                 g.setColor(Color.white);
                 g.drawLine(lastRowBounds.x, lastRowBounds.y,
                     lastRowBounds.x
                             + lastRowBounds.width, lastRowBounds.y);
             }
             tree.setSelectionPath(path);
 
             // logger.fatal("path.getLastPathComponent():"+path.getLastPathComponent());
             // logger.fatal("lastNode:"+lastNode+" ? = "+lastNode==path.getLastPathComponent());
             if (!draggedNodes.contains(path.getLastPathComponent())) {
                 if (lastNode != path.getLastPathComponent()) {
                     lastNode = path.getLastPathComponent();
                     // logger.fatal("ERSTES MAL");
                     timestamp = System.currentTimeMillis();
                 } else {
                     final long value = System.currentTimeMillis() - timestamp;
                     // logger.fatal("ERNEUT:"+value);
                     if (value > 1000) {
                         // logger.fatal("EXPAND");
                         tree.expandPath(path);
                     } else {
                         // logger.fatal("Abwarten");
                     }
                 }
             }
         }
     }
     /**
      * DOCUMENT ME!
      *
      * @param  tree      DOCUMENT ME!
      * @param  location  DOCUMENT ME!
      */
     private void paintInsertMarker(final JTree tree, final Point location) {
         final Graphics g = tree.getGraphics();
         tree.clearSelection();
         final int row = tree.getRowForPath(tree.getClosestPathForLocation(location.x, location.y));
         final TreePath path = tree.getPathForRow(row);
         if (path != null) {
             final Rectangle rowBounds = tree.getPathBounds(path);
             if (lastRowBounds != null) {
                 g.setColor(Color.white);
                 g.drawLine(lastRowBounds.x, lastRowBounds.y,
                     lastRowBounds.x
                             + lastRowBounds.width, lastRowBounds.y);
             }
             if (rowBounds != null) {
                 g.setColor(Color.black);
                 g.drawLine(rowBounds.x, rowBounds.y, rowBounds.x + rowBounds.width, rowBounds.y);
             }
             lastRowBounds = rowBounds;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  tree      DOCUMENT ME!
      * @param  location  DOCUMENT ME!
      */
     public void updateDragMark(final JTree tree, final Point location) {
         final int row = tree.getRowForPath(
                 tree.getClosestPathForLocation(location.x, location.y));
         final TreePath path = tree.getPathForRow(row);
         if (path != null) {
             final Rectangle rowBounds = tree.getPathBounds(path);
             final int rby = rowBounds.y;
             final int topBottomDist = insertAreaHeight / 2;
             final Point topBottom = new Point(
                     rby
                             - topBottomDist,
                     rby
                             + topBottomDist);
             if ((topBottom.x <= location.y) && (topBottom.y >= location.y)) {
                 paintInsertMarker(tree, location);
             } else {
                 markNode(tree, location);
             }
         }
     }
     /**
      * DOCUMENT ME!
      *
      * @param   tree  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BufferedImage getDragImage(final JTree tree) {
         try {
             final TreeCellRenderer r = tree.getCellRenderer();
             final DefaultTreeModel m = (DefaultTreeModel)tree.getModel();
             final Vector<BufferedImage> labels = new Vector<BufferedImage>();
             int height = 0;
             int maxWidth = 0;
             for (int i = 0; i < dragPaths.length; ++i) {
                 BufferedImage image = null;
                 final Rectangle pathBounds = tree.getPathBounds(dragPaths[i]);
                 final boolean nIsLeaf = m.isLeaf(dragPaths[i].getLastPathComponent());
                 final JComponent lbl = (JComponent)r.getTreeCellRendererComponent(
                         tree,
                         dragPaths[i].getLastPathComponent(),
                         false,
                         tree.isExpanded(dragPaths[i]),
                         nIsLeaf,
                         0,
                         false);
                 lbl.setBounds(pathBounds);
                 lbl.setOpaque(false);
                 height += lbl.getHeight();
                 if (lbl.getWidth() > maxWidth) {
                     maxWidth = lbl.getWidth();
                 }
                 image = new BufferedImage(lbl.getWidth(),
                         lbl.getHeight(),
                         java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
                 final Graphics2D graphics = image.createGraphics();
                 graphics.setComposite(
                     AlphaComposite.getInstance(
                         AlphaComposite.SRC_OVER,
                         0.5f));
                 lbl.paint(graphics);
                 graphics.dispose();
                 labels.add(image);
             }
             final BufferedImage master = new BufferedImage(
                     maxWidth,
                     height,
                     java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE);
             final Graphics2D graphics = master.createGraphics();
             int h = 0;
             for (final BufferedImage ii : labels) {
                 graphics.drawImage(ii, 0, h, null);
                 h += ii.getHeight();
             }
             graphics.dispose();
             return master;
         } catch (Exception e) {
             logger.error("Error while creating DragImages", e); // NOI18N
             return null;
         }
     }
     /**
      * DOCUMENT ME!
      *
      * @param  tree  DOCUMENT ME!
      * @param  pt    DOCUMENT ME!
      */
     private void paintImage(final JTree tree, final Point pt) {
         final BufferedImage image = getDragImage(tree);
         if (image != null) {
             tree.paintImmediately(rect2D.getBounds());
             rect2D.setRect((int)pt.getX() - 15, (int)pt.getY() - 15, image.getWidth(), image.getHeight());
             tree.getGraphics().drawImage(image, (int)pt.getX() - 15, (int)pt.getY() - 15, tree);
         }
     }
     @Override
     public void dragOver(final DropTargetDragEvent e) {
         final JTree tree = (JTree)e.getDropTargetContext().getComponent();
         final Point loc = e.getLocation();
         updateDragMark(tree, loc);
         paintImage(tree, loc);
     }
 
     @Override
     public void dragOver(final DragSourceDragEvent dsde) {
     }
 
     @Override
     public void dragEnter(final DropTargetDragEvent dtde) {
         if (logger.isDebugEnabled()) {
             logger.debug("dragEnter(DropTargetDragEvent)"); // NOI18N
         }
         logger.info(dtde.getSource());
     }
 
     @Override
     public void dragEnter(final DragSourceDragEvent dsde) {
         if (logger.isDebugEnabled()) {
             logger.debug("dragEnter(DragSourceDragEvent)"); // NOI18N
         }
 
         final DragSourceContext dragSourceContext = dsde.getDragSourceContext();
         dragSourceContext.setCursor(this.getCursor(dsde.getDropAction()));
     }
 
     @Override
     public void dragExit(final DragSourceEvent dse) {
         dse.getDragSourceContext().setCursor(DragSource.DefaultCopyNoDrop);
         metaTree.setSelectionPaths(dragPaths);
     }
 
     @Override
     public void dragExit(final DropTargetEvent dte) {
         if (!DragSource.isDragImageSupported()) {
             this.metaTree.repaint(); // this.dragImage.);
         }
     }
 
     @Override
     public void dropActionChanged(final DropTargetDragEvent dtde) {
         if (logger.isDebugEnabled()) {
             logger.debug("dropActionChanged(DropTargetDragEvent)"); // NOI18N
         }
     }
 
     @Override
     public void dropActionChanged(final DragSourceDragEvent dsde) {
         if (logger.isDebugEnabled()) {
             logger.debug("dropActionChanged(DragSourceDragEvent)"); // NOI18N
         }
 
         final DragSourceContext dragSourceContext = dsde.getDragSourceContext();
         dragSourceContext.setCursor(this.getCursor(dsde.getUserAction()));
         this.metaTransferable.setTransferAction(dsde.getUserAction());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   dragAction  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private Cursor getCursor(final int dragAction) {
         Cursor cursor = DragSource.DefaultCopyNoDrop;
         if ((dragAction & DnDConstants.ACTION_MOVE) != 0) {
             if (logger.isDebugEnabled()) {
                 logger.debug("getCursor(): ACTION_MOVE"); // NOI18N
             }
             cursor = DragSource.DefaultMoveDrop;
         } else if ((dragAction & DnDConstants.ACTION_COPY) != 0) {
             if (logger.isDebugEnabled()) {
                 logger.debug("getCursor(): ACTION_COPY"); // NOI18N
             }
             cursor = DragSource.DefaultCopyDrop;
         } else if ((dragAction & DnDConstants.ACTION_LINK) != 0) {
             if (logger.isDebugEnabled()) {
                 logger.debug("getCursor(): ACTION_LINK"); // NOI18N
             }
             cursor = DragSource.DefaultLinkDrop;
         }
 
         return cursor;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   destinationPath  DOCUMENT ME!
      * @param   sourcePath       DOCUMENT ME!
      * @param   dropAction       DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private boolean checkDestination(final TreePath destinationPath, final TreePath sourcePath, final int dropAction) {
         if (destinationPath != null) {
             if ((destinationPath.getLastPathComponent() instanceof PureTreeNode)
                         || (destinationPath.getLastPathComponent() instanceof ObjectTreeNode)) {
                 if (destinationPath.equals(sourcePath)) {
                     logger.warn("destination path equals source path");                                          // NOI18N
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MetaTreeNodeDnDHandler.class,
                             "MetaTreeNodeDnDHandler.checkDestination().pathsEqualWarning.message",
                             new Object[] { destinationPath.getLastPathComponent(), }),                           // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MetaTreeNodeDnDHandler.class,
                             "MetaTreeNodeDnDHandler.checkDestination().pathsEqualWarning.title"),                // NOI18N
                         JOptionPane.WARNING_MESSAGE);
                 } else if (sourcePath.isDescendant(destinationPath)) {
                     logger.warn("destination path can not be a descendant of the source path");                  // NOI18N
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MetaTreeNodeDnDHandler.class,
                             "MetaTreeNodeDnDHandler.checkDestination().pathIsDecendantWarning.message",
                             new Object[] { destinationPath.getLastPathComponent() }),                            // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             MetaTreeNodeDnDHandler.class,
                             "MetaTreeNodeDnDHandler.checkDestination().pathIsDecendantWarning.title"),           // NOI18N
                         JOptionPane.WARNING_MESSAGE);
                 } else if ((dropAction != DnDConstants.ACTION_COPY)
                             && sourcePath.getParentPath().equals(destinationPath)) {
                     logger.warn("destination node is the parent of the source node");                            // NOI18N
                     JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                         org.openide.util.NbBundle.getMessage(
                             MetaTreeNodeDnDHandler.class,
                             "MetaTreeNodeDnDHandler.checkDestination().destinationParentOfSourceWarning.message",
                             new Object[] { destinationPath.getLastPathComponent() }),                            // NOI18N,
                         org.openide.util.NbBundle.getMessage(
                             MetaTreeNodeDnDHandler.class,
                             "MetaTreeNodeDnDHandler.checkDestination().destinationParentOfSourceWarning.title"), // NOI18N
                         JOptionPane.WARNING_MESSAGE);
                 } else {
                     if (logger.isDebugEnabled()) {
                         logger.debug("checkDestination() dnd destination ok: " + sourcePath.getLastPathComponent()
                                     + " -> " + destinationPath.getLastPathComponent());                          // NOI18N
                     }
                     return true;
                 }
             } else {
                 logger.warn("destination node '" + destinationPath.getLastPathComponent()
                             + "' is no pure or object node");                                                    // NOI18N
                 JOptionPane.showMessageDialog(ComponentRegistry.getRegistry().getMainWindow(),
                     org.openide.util.NbBundle.getMessage(
                         MetaTreeNodeDnDHandler.class,
                         "MetaTreeNodeDnDHandler.checkDestination().noPureNodeWarning.message",
                         new Object[] { destinationPath.getLastPathComponent() }),                                // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         MetaTreeNodeDnDHandler.class,
                         "MetaTreeNodeDnDHandler.checkDestination().noPureNodeWarning.title"),                    // NOI18N
                     JOptionPane.WARNING_MESSAGE);
             }
         } else {
             logger.warn("no node found at this location");                                                       // NOI18N
         }
 
         return false;
     }
 
     /**
      * private BufferedImage getDragImage(TreePath treePath, TreeNode treeNode, Rectangle pathBounds) { // Get the cell
      * renderer (which is a JLabel) for the path being dragged JLabel cellRenderer = (JLabel)
      * this.metaTree.getCellRenderer().getTreeCellRendererComponent(this.metaTree, treeNode, false,
      * this.metaTree.isExpanded(treePath), treeNode.isLeaf(), 0, false);
      * cellRenderer.setSize((int)pathBounds.getWidth(), (int)pathBounds.getHeight()); // <-- The layout manager would
      * normally do this // Get a buffered image of the selection for dragging a ghost image BufferedImage dragImage =
      * new BufferedImage((int)pathBounds.getWidth(), (int)pathBounds.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
      * Graphics2D g2 = dragImage.createGraphics(); // Ask the cell renderer to paint itself into the BufferedImage
      * g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f)); // Make the image ghostlike
      * cellRenderer.paint(g2); // Now paint a gradient UNDER the ghosted JLabel text (but not under the icon if any) //
      * Note: this will need tweaking if your icon is not positioned to the left of the text Icon icon =
      * cellRenderer.getIcon(); int nStartOfText = (icon == null) ? 0 : icon.getIconWidth() +
      * cellRenderer.getIconTextGap(); g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 0.5f)); //
      * Make the gradient ghostlike g2.setPaint(new GradientPaint(nStartOfText, 0, SystemColor.controlShadow,
      * this.metaTree.getWidth(), 0, new Color(255,255,255,0))); g2.fillRect(nStartOfText, 0, this.metaTree.getWidth(),
      * dragImage.getHeight()); g2.dispose(); return dragImage; }
      *
      * @return  DOCUMENT ME!
      */
     public BufferedImage getDragImage() {
         return metaTree.getDragImage();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  dragImage  DOCUMENT ME!
      */
     public void setDragImage(final BufferedImage dragImage) {
         this.dragImage = dragImage;
         metaTree.setDragImage(dragImage);
     }
 }
