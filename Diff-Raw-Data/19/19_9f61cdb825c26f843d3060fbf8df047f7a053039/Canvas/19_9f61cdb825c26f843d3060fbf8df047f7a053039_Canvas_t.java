 package com.joshondesign.treegui;
 
 import com.joshondesign.treegui.docmodel.Layer;
 import com.joshondesign.treegui.docmodel.ResizableRectNode;
 import com.joshondesign.treegui.docmodel.SketchDocument;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import com.joshondesign.treegui.model.TreeNode;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.Font;
 import org.joshy.gfx.draw.GFX;
 import org.joshy.gfx.event.Callback;
 import org.joshy.gfx.event.EventBus;
 import org.joshy.gfx.event.MouseEvent;
 import org.joshy.gfx.node.Bounds;
 import org.joshy.gfx.node.control.Control;
 import org.joshy.gfx.node.control.Focusable;
 import org.joshy.gfx.node.control.ScrollPane;
 
 public class Canvas extends Control implements Focusable, ScrollPane.ScrollingAware {
     BindingBox popup;
     BindingBox popup2;
     Point2D startDragPoint;
     boolean dragging;
     Point2D currentDragPoint;
     Binding currentBinding;
     private TreeNode<SketchNode> masterRoot;
     private TreeNode<SketchNode> editRoot;
     private ScrollPane scrollPane;
     private SketchDocument document;
     private double scrollX;
     private double scrollY;
     private CanvasTool currentTool;
     private Bounds maxBounds;
    private Bounds docBounds = new Bounds(0,0,600,400);
     private Bounds baseBounds = new Bounds(-100,-100,600+200,400+200);
     private Bounds totalBounds = new Bounds(-100,-100,600+200,400+200);
     private boolean boundsRecalcEnabled = false;
 
 
     public void setMasterRoot(final TreeNode<SketchNode> masterRoot) {
         this.masterRoot = masterRoot;
         masterRoot.addListener(new TreeNode.TreeListener() {
             public void added(TreeNode node) {
                 setDrawingDirty();
             }
 
             public void removed(TreeNode node) {
                 setDrawingDirty();
             }
 
             public void modified(TreeNode node) {
                 setDrawingDirty();
                 if(!boundsRecalcEnabled) return;
                 recalcBounds();
             }
         });
         setDrawingDirty();
     }
 
     public void recalcBounds() {
         Bounds tmp = MathUtils.unionBounds(masterRoot);
         maxBounds = new Bounds(tmp.getX()-100,tmp.getY()-100,tmp.getWidth()+200,tmp.getHeight()+200);
         //maxBounds = tmp;
         if (maxBounds.getX2() > baseBounds.getX2()
                 || maxBounds.getY2() > baseBounds.getY2()
                 || maxBounds.getX() < baseBounds.getX()
                 || maxBounds.getY() < baseBounds.getY()
                 ) {
             totalBounds = maxBounds.union(baseBounds);
             setLayoutDirty();
             setDrawingDirty();
         } else {
             totalBounds = baseBounds;
         }
     }
 
     public void setEditRoot(TreeNode<SketchNode> editRoot) {
         this.editRoot = editRoot;
         setDrawingDirty();
     }
 
     public TreeNode<SketchNode> getEditRoot() {
         return editRoot;
     }
 
     public Canvas() {
 
         EventBus.getSystem().addListener(this, MouseEvent.MouseAll, new Callback<MouseEvent>(){
             public void call(MouseEvent mouseEvent) throws Exception {
                 if(currentTool == null) return;
                 Point2D pt = toEditRootCoords(mouseEvent.getPointInNodeCoords(Canvas.this));
                 if (mouseEvent.getType() == MouseEvent.OpenContextMenu && document.getSelection() != null) {
                     showBindingMenu(mouseEvent.getPointInNodeCoords(Canvas.this));
                 }
 
                 if (mouseEvent.getType() == MouseEvent.MousePressed) {
                     currentTool.pressed(new CanvasMouseEvent(Canvas.this, pt, mouseEvent));
                 }
                 if(mouseEvent.getType() == MouseEvent.MouseDragged) {
                     currentTool.dragged(new CanvasMouseEvent(Canvas.this, pt, mouseEvent));
                 }
                 if(mouseEvent.getType() == MouseEvent.MouseReleased) {
                     currentTool.released(new CanvasMouseEvent(Canvas.this, pt, mouseEvent));
                 }
             }
         });
 
     }
 
     public void setBoundsRecalcEnabled(boolean boundsRecalcEnabled) {
         this.boundsRecalcEnabled = boundsRecalcEnabled;
     }
 
     public boolean isBoundsRecalcEnabled() {
         return boundsRecalcEnabled;
     }
 
     public TreeNode<SketchNode> getMasterRoot() {
         return masterRoot;
     }
 
 
     public static class CanvasMouseEvent {
 
         final Point2D pt;
         final MouseEvent mouseEvent;
         public Canvas canvas;
 
         public CanvasMouseEvent(Canvas canvas, Point2D pt, MouseEvent mouseEvent) {
             this.canvas = canvas;
             this.pt = pt;
             this.mouseEvent = mouseEvent;
         }
     }
 
 
 
     void showBindingMenu(Point2D pt) {
 
         currentBinding = new Binding();
         currentBinding.setSource(document.getSelection().get(0));
 
         if(popup == null) {
             popup = new BindingBox();
             this.getParent().getStage().getPopupLayer().add(popup);
         }
         if(popup != null) {
             popup.reset();
             popup.setVisible(true);
         }
         SketchNode node = document.getSelection().get(0);
         if(node instanceof DynamicNode) {
             BindingUtils.populateWithBindablePropertiesDynamic(popup, (DynamicNode) node, this, document);
         } else {
             BindingUtils.populateWithBindablePropertiesRegular(popup, node, this, document);
         }
         popup.setTranslateX(pt.getX() + this.getTranslateX());
         popup.setTranslateY(pt.getY() + this.getTranslateY());
     }
 
 
     void showTargetPopup(final SketchNode node, MouseEvent mouseEvent) {
         if(popup2 == null) {
             popup2 = new BindingBox();
             this.getParent().getStage().getPopupLayer().add(popup2);
         }
         if(popup2 != null) {
             popup2.reset();
             popup2.setVisible(true);
         }
         if(node instanceof DynamicNode) {
             BindingUtils.populateWithTargetPropertiesDynamic(popup2, (DynamicNode)node, currentBinding, this, document);
         } else {
             BindingUtils.populateWithTargetPropertiesRegular(popup2, node, currentBinding, this, document);
 
         }
         Point2D pt = mouseEvent.getPointInNodeCoords(Canvas.this);
         popup2.setTranslateX(pt.getX() + this.getTranslateX());
         popup2.setTranslateY(pt.getY() + this.getTranslateY());
     }
 
     @Override
     public void doLayout() {
     }
 
     @Override
     public void doPrefLayout() {
         this.setWidth(getPrefWidth());
         this.setHeight(getPrefHeight());
     }
 
     @Override
     public void doSkins() {
     }
 
     @Override
     public void setLayoutDirty() {
         super.setLayoutDirty();
     }
 
     @Override
     public void draw(GFX gfx) {
         gfx.setPaint(FlatColor.fromRGBInts(230,230,230));
         gfx.fillRect(0,0,getWidth(),getHeight());
 
 
         double dx = scrollX-totalBounds.getX();
         double dy = scrollY-totalBounds.getY();
         gfx.translate(dx,dy);
 
         drawDocumentBounds(gfx, masterRoot);
         drawMasterRoot(gfx, masterRoot);
         drawBindings(gfx);
         drawSelectionOverlay(gfx);
         drawActiveBindingLineOverlay(gfx);
         drawHandles(gfx);
         drawGroupEditOverlay(gfx);
         //drawDebug(gfx);
 
         drawToolOverlay(gfx);
 
         gfx.translate(-dx,-dy);
 
         //drawMaxBounds(gfx,maxBounds);
     }
 
     private void drawMaxBounds(GFX gfx, Bounds maxBounds) {
         if(maxBounds == null) return;
         gfx.setPaint(FlatColor.PURPLE);
         if(!masterRoot.children().iterator().hasNext()) return;
         gfx.drawRect(maxBounds.getX(),maxBounds.getY(),maxBounds.getWidth(),maxBounds.getHeight());
     }
 
     private void drawToolOverlay(GFX gfx) {
         if(currentTool != null) {
             currentTool.drawOverlay(gfx);
         }
     }
 
     private void drawDocumentBounds(GFX gfx, TreeNode<SketchNode> masterRoot) {
         gfx.setPaint(FlatColor.WHITE);
        gfx.fillRect(0,0,docBounds.getWidth(), docBounds.getHeight());
         if(masterRoot instanceof Layer) {
             gfx.setPaint(FlatColor.GRAY);
             gfx.drawRect(0,0,600,400);
         }
     }
 
 
     private void drawMasterRoot(GFX gfx, TreeNode<SketchNode> root) {
         if(root == null) return;
         for(SketchNode n : root.children()) {
             drawNode(gfx, n);
         }
     }
 
     private void drawNode(GFX gfx, SketchNode n) {
         gfx.translate(n.getTranslateX(), n.getTranslateY());
         n.draw(gfx);
         for(SketchNode c : n.children()) {
             drawNode(gfx,c);
         }
         gfx.translate(-n.getTranslateX(), -n.getTranslateY());
     }
 
     private void drawBindings(GFX gfx) {
         for(Binding binding : document.getBindings()) {
             gfx.setPaint(FlatColor.BLACK);
             Bounds sb = binding.getSource().getInputBounds();
             Bounds tb = binding.getTarget().getInputBounds();
             Point2D start = toRootCoords(
                     new Point2D.Double(sb.getCenterX(), sb.getCenterY()),
                     binding.getSource());
             Point2D end = toRootCoords(
                     new Point2D.Double(tb.getCenterX(), tb.getCenterY()),
                     binding.getTarget());
             gfx.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
         }
     }
 
     private Point2D toRootCoords(Point2D start, SketchNode source) {
         start = new Point2D.Double(
                 start.getX()+source.getTranslateX(),
                 start.getY()+source.getTranslateY());
         if(source == getEditRoot()) {
             return start;
         }
 
         if(!(source.getParent() instanceof SketchNode)) {
             return start;
         }
         SketchNode parent = (SketchNode) source.getParent();
         return toRootCoords(start, parent);
     }
 
     private void drawSelectionOverlay(GFX gfx) {
         if(document.getSelection().getSize() < 1) return;
         Point2D pt = new Point2D.Double(0,0);
        pt = editRootToMasterRootCoords(pt, editRoot);
         Bounds b = MathUtils.unionBounds(document.getSelection());
         b = MathUtils.transform(b,pt);
         gfx.setPaint(FlatColor.fromRGBInts(100,100,100));
         gfx.drawRect(b.getX(),b.getY(),b.getWidth(),b.getHeight());
         gfx.setPaint(FlatColor.fromRGBInts(200,200,200));
         gfx.drawRect(b.getX() - 1, b.getY() - 1, b.getWidth() + 2, b.getHeight() + 2);
     }
 
 
     private void drawHandles(GFX gfx) {
         Point2D pt = new Point2D.Double(0,0);
         if(getEditRoot() instanceof SketchNode) {
             SketchNode sn = (SketchNode) getEditRoot();
             pt = MathUtils.transform(pt,sn.getTranslateX(),sn.getTranslateY());
         }
         gfx.translate(pt.getX(),pt.getY());
         for(Handle handle : this.handles) {
             handle.draw(gfx);
         }
         gfx.translate(-pt.getX(),-pt.getY());
     }
 
 
     private void drawActiveBindingLineOverlay(GFX gfx) {
         if(startDragPoint != null && currentDragPoint != null && dragging) {
             gfx.setPaint(FlatColor.RED);
             gfx.drawLine(startDragPoint.getX(),startDragPoint.getY(),
                     currentDragPoint.getX(),currentDragPoint.getY());
         }
     }
 
 
     private void drawGroupEditOverlay(GFX gfx) {
         if(editRoot != masterRoot && editRoot instanceof SketchNode) {
             SketchNode root = (SketchNode) editRoot;
             Bounds bounds = root.getInputBounds();
             bounds = MathUtils.transform(bounds,root.getTranslateX(),root.getTranslateY());
             gfx.setPaint(FlatColor.hsb(0,1,1,0.4));
             gfx.fillRect(0,0,bounds.getX(),getHeight());
             gfx.fillRect(bounds.getX2(),0,getWidth()-bounds.getX2(),getHeight());
             gfx.fillRect(bounds.getX(),0,bounds.getWidth(),bounds.getY());
             gfx.fillRect(bounds.getX(),bounds.getY2(),bounds.getWidth(),getHeight()-bounds.getY2());
         }
     }
 
     private void drawDebug(GFX gfx) {
         gfx.setPaint(FlatColor.hsb(0, 0, 0.7));
         gfx.fillRect(0, 0, 400, 20);
         gfx.setPaint(FlatColor.BLACK);
         gfx.drawText("edit root: " + getEditRoot().getClass().getName(), Font.DEFAULT, 5, 15);
     }
 
 
     SketchNode findNode(Point2D pt) {
         for(SketchNode n : this.editRoot.reverseChildren()) {
             if(n.contains(pt)) return n;
         }
         return null;
     }
 
     public SketchNode findNodeSkipping(Point2D pt, SketchNode node) {
         for(SketchNode n : this.editRoot.reverseChildren()) {
             if(n.contains(pt) && n != node) return n;
         }
         return null;
     }
 
 
 
     /*
     public void addToSelection(SketchNode node) {
         this.selection.add(node);
         this.getPropsView().setSelection(node);
         setDrawingDirty();
     }
     public void clearSelection() {
         if(popup != null && popup.isVisible()) {
             popup.setVisible(false);
         }
         if(popup2 != null && popup2.isVisible()) {
             popup2.setVisible(false);
         }
         this.selection.clear();
         this.handles.clear();
         setDrawingDirty();
     }
     */
 
 
     public void redraw() {
         setDrawingDirty();
     }
 
 
     public boolean isFocused() {
         return true;
     }
 
     List<Handle> handles = new ArrayList<Handle>();
     public void rebuildHandles() {
         this.handles.clear();
         for(SketchNode node : document.getSelection().children()) {
             if(node instanceof ResizableRectNode) {
                 handles.add(new ResizeHandle((ResizableRectNode) node));
             }
             if(node instanceof DynamicNode) {
                 DynamicNode dnode = (DynamicNode) node;
                 if(dnode.isResizable()) {
                     handles.add(new DynamicResizeHandle((DynamicNode) node));
                 }
             }
         }
     }
 
     public Handle findHandle(Point2D pointInNodeCoords) {
         for(Handle handle : this.handles) {
             if(handle.contains(pointInNodeCoords)) return handle;
         }
         return null;
     }
 
     private Stack<TreeNode<SketchNode>> editStack = new Stack<TreeNode<SketchNode>>();
 
     public void navigateInto(SketchNode node) {
         editStack.push(getEditRoot());
         document.getSelection().clear();
         setEditRoot(node);
         redraw();
     }
 
     public void navigateOutof() {
         setEditRoot(editStack.pop());
         document.getSelection().clear();
         redraw();
     }
 
     public Point2D toEditRootCoords(Point2D pt) {
         pt = toEditRootCoords(pt, this.editRoot);
         return MathUtils.transform(pt, -scrollX+totalBounds.getX(), -scrollY+totalBounds.getY());
     }
 
     private Point2D toEditRootCoords(Point2D point, TreeNode<SketchNode> node) {
         if(node == masterRoot) return point;
         SketchNode sn = (SketchNode) node;
         point = MathUtils.transform(point, -sn.getTranslateX(), -sn.getTranslateY());
         return toEditRootCoords(point, sn.getParent());
     }
 
    private Point2D editRootToMasterRootCoords(Point2D point, TreeNode root) {
        if(root == masterRoot) return point;
        SketchNode sn = (SketchNode) root;
        point = MathUtils.transform(point, sn.getTranslateX(), sn.getTranslateY());
        return editRootToMasterRootCoords(point, sn.getParent());
    }


 
 
     /* ======== scrolling aware implementation ========== */
 
     public double getFullWidth(double w, double h) {
         return totalBounds.getWidth();
     }
 
     public double getFullHeight(double v, double v2) {
         return totalBounds.getHeight();
     }
 
     public void setScrollX(double v) {
         this.scrollX = v;
     }
 
     public void setScrollY(double v) {
         this.scrollY = v;
     }
 
     public void setScrollParent(ScrollPane scrollPane) {
         this.scrollPane = scrollPane;
     }
 
 
 
 
 
     /* ============ more stuff ================== */
     public void setDocument(SketchDocument document) {
         this.document = document;
         document.getSelection().addListener(new TreeNode.TreeListener() {
             public void added(TreeNode node) {
                 redraw();
             }
 
             public void removed(TreeNode node) {
                 redraw();
             }
 
             public void modified(TreeNode node) {
                 redraw();
             }
         });
     }
 
     public void setTool(SelectionTool selectionTool) {
         this.currentTool = selectionTool;
     }
 }
