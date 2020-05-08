 package binevi.View;
 
 import y.base.*;
 import y.module.YModule;
 import y.module.OrganicLayoutModule;
 import y.option.OptionHandler;
 import y.util.D;
 import y.view.*;
 import y.view.hierarchy.GroupNodeRealizer;
 import y.view.hierarchy.HierarchyManager;
 import y.view.ViewMode.*;
 import y.layout.Layouter;
 import y.layout.LayoutTool;
 import y.layout.organic.SmartOrganicLayouter;
 import y.layout.hierarchic.incremental.HierarchicLayouter;
 import y.layout.hierarchic.IncrementalHierarchicLayouter;
 
 import javax.swing.*;
 
 import binevi.Resources.PathCaseResources.TableQueries;
 
 import edu.cwru.nashua.pathwaysservice.PathwaysService;
 import edu.cwru.nashua.pathwaysservice.PathwaysServiceMetabolomics;
 
 import java.awt.event.*;
 import java.awt.*;
 import java.awt.geom.Rectangle2D;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.StringTokenizer;
 
 public class DefaultYGraphViewerMetabolomics {
 
     //The view component of this viewer.
     public Graph2DView view;
 
     //Mode variables
     public boolean ISDELETIONENABLED = false;
     public boolean PATHCASEQUERYINGENABLED = true;
     
     //EDITING variables
     public Node lastSelectedNode = null;
     public static enum LINK_TYPE {
     	REACTION_TO_PRODUCT, NONE, REACTION_TO_REACTANT
     }
     public LINK_TYPE linkMode = LINK_TYPE.NONE;
 
                             
     //Graph modes
     public static enum GRAPH_MODE {
         EDIT, PAN, INTERACTIVE_ZOOM, AREA_ZOOM, MAGNIFIER
     }
 
     //Access to container GUI
     PathCaseViewerMetabolomics pathCaseGUI;
 
     //instantiate yfiles related objects
     public void initializeGraphViewerComponents() {
         view = new Graph2DView();
         view.setAntialiasedPainting(true);
         //resetGraphHider();
         enableBridgesForEdgePaths();
 
     }
 
     //uncomment this method if we want to enable bridgeing
     protected void enableBridgesForEdgePaths() {
         /*Graph2DRenderer gr = view.getGraph2DRenderer();
         if (gr instanceof DefaultGraph2DRenderer)
         {
           DefaultGraph2DRenderer dgr = (DefaultGraph2DRenderer)gr;
           // If there is no BridgeCalculator instance set, ...
           if (dgr.getBridgeCalculator() == null)
           {
             // ... then register a newly created one that uses default settings.
             BridgeCalculator calculator = new BridgeCalculator();
             dgr.setBridgeCalculator(calculator);
             calculator.setCrossingStyle(BridgeCalculator.CROSSING_STYLE_TWO_SIDES);
           }
         }*/
     }
 
 
     public DefaultYGraphViewerMetabolomics(PathCaseViewerMetabolomics pathCaseGUI) {
         view = new Graph2DView();
         view.setAntialiasedPainting(true);
 //        view.setFitContentOnResize(true);
         this.pathCaseGUI = pathCaseGUI;
     }
 
     /////////////////////////    VIEW MODE OPERATIONS  //////////////////////////////////
 
     private static NavigationMode navigationMode;
     private static AutoDragViewMode autoDragViewMode;
     private static AreaZoomMode areaZoomMode;
     private static ToolTipEditMode editMode;
     private static InteractiveZoomMode interactiveZoomMode;
     private static PopupMenuMode popupMode;
     private static MagnifierViewMode magnifyingMode;
     private static TooltipViewMode tooltipViewMode;  // added by En in Dec. 01, 2007
     private static ClickViewMode clickViewMode;
 
 
 
     public void resetView() {
         //view.removeAll();
         view.setGraph2D(new Graph2D());
         //initializeGraphViewerComponents();
 
     }
 
     //view actions
     protected void registerViewActions() {
         //register keyboard actions
         Graph2DViewActions actions = new Graph2DViewActions(view);
         ActionMap amap = actions.createActionMap();
         InputMap imap = actions.createDefaultInputMap(amap);
         if (!ISDELETIONENABLED) {
             amap.remove(Graph2DViewActions.DELETE_SELECTION);
         }
         view.getCanvasComponent().setActionMap(amap);
         view.getCanvasComponent().setInputMap(JComponent.WHEN_FOCUSED, imap);
     }
 
     protected void setViewMode(GRAPH_MODE mode) {
         removeCurrentViewModes();
         createViewModes();
         switch (mode) {
             case PAN:
                 view.addViewMode(navigationMode);
                 view.addViewMode(tooltipViewMode); // added by En
                 view.addViewMode(autoDragViewMode);
                 break;
             case EDIT:
                 view.addViewMode(editMode);
                 view.addViewMode(autoDragViewMode);
                 view.addViewMode(tooltipViewMode);
                 view.addViewMode(clickViewMode);
                 break;
             case INTERACTIVE_ZOOM:
                 view.addViewMode(interactiveZoomMode);
                 view.addViewMode(popupMode);
                 view.addViewMode(tooltipViewMode); // added by En
                 break;
             case AREA_ZOOM:
                 view.addViewMode(areaZoomMode);
                 view.addViewMode(popupMode);
                 view.addViewMode(tooltipViewMode); // added by En
                 break;
             case MAGNIFIER:
                 view.addViewMode(magnifyingMode);
                 view.addViewMode(editMode);
                 view.addViewMode(autoDragViewMode);
                 break;
         }
     }
 
     private void removeCurrentViewModes() {
         if (navigationMode != null) view.removeViewMode(navigationMode);
         if (autoDragViewMode != null) view.removeViewMode(autoDragViewMode);
         if (areaZoomMode != null) view.removeViewMode(areaZoomMode);
         if (editMode != null) view.removeViewMode(editMode);
         if (interactiveZoomMode != null) view.removeViewMode(interactiveZoomMode);
         if (popupMode != null) view.removeViewMode(popupMode);
         if (magnifyingMode != null) view.removeViewMode(magnifyingMode);
         if (tooltipViewMode != null) view.removeViewMode(tooltipViewMode); //added by En
         if (clickViewMode != null) view.removeViewMode(clickViewMode);
     }
 
     class ToolTipEditMode extends EditMode {
 
         protected String getNodeTip(Node node) {
 //            return getNodeTipText(node);
             return "not identified";
         }
     }
 
     // Author: En Cheng
     // Date: Dec. 01, 2007
     // Main function: show tooltips in all view modes
     class TooltipViewMode extends ViewMode {
 
         public TooltipViewMode() {
             super();
         }
 
         public void mouseMoved(double x, double y) {
             String tipText = null;
             HitInfo hitInfo = getHitInfo(x, y);
 
             if (hitInfo.getHitNode() != null) {
                 tipText = getNodeTip(hitInfo.getHitNode());
             } else {
                 tipText = null;
 //                System.out.println("");
             }
 
             view.setToolTipText(tipText);            
         }
 
         public String getNodeTip(Node v) {
 
             // return getGraph2D().getLabelText(v);
 
             return getNodeTipText(v);
         }
 
     }
 
     class PopupMenuMode extends PopupMode {
 
         public PopupMenuMode() {
             super();
 
         }
 
         public JPopupMenu getBendPopup(Bend b) {
             return getBendPopupMenu(b);
         }
 
         public JPopupMenu getEdgeLabelPopup(EdgeLabel label) {
             return getEdgeLabelPopupMenu(label);
         }
 
         public JPopupMenu getEdgePopup(Edge e) {
             return getEdgePopupMenu(e);
         }
 
         public JPopupMenu getNodePopup(Node v) {
             return getNodePopupMenu(v);
         }
 
         public JPopupMenu getPaperPopup(double x, double y) {
             return getPaperPopupMenu(x, y);
         }
 
         public JPopupMenu getSelectionPopup(double x, double y) {
             return getSelectionPopupMenu(x, y);
         }
     }
 
 
     class ClickViewMode extends ViewMode {
         public ClickViewMode() {
             super();
         }
         Node v;
         boolean bDragged=false;
 //        double initialWorldX=0,initialWorldY=0;
         double orgX=0,orgY=0;
         double finalX=0,finalY=0;
 
 //        public void mousePressedLeft(double x, double y) {
 //                  initialWorldX = x;
 //                  initialWorldY = y;
 //              }
 
 //         public void mouseClicked(MouseEvent e)
 //        {
 //            HierarchyManager hm;
 //            v = getHitInfo(e).getHitNode();
 //            if(v != null)
 //            {
 //                hm = HierarchyManager.getInstance(view.getGraph2D());
 //                if(hm.isGroupNode(v)){
 //                    if(hm.getParentNode(v)!=null)System.out.println(" Parent node is: "+hm.getParentNode(v).index());
 //                        NodeRealizer nr = view.getGraph2D().getRealizer(v);
 //                        if (nr instanceof GroupNodeRealizer) {
 //                          GroupNodeRealizer groupNodeRealizer = (GroupNodeRealizer)nr;
 //
 //                          Insets borderInsets = groupNodeRealizer.getBorderInsets();
 //                          groupNodeRealizer.setBorderInsets(
 //                            new Insets(borderInsets.top, borderInsets.left, borderInsets.bottom, borderInsets.right + 20));
 //
 //                          // Resetting all insets effectively shrinks the group node (if not already at its minimum).
 //                          groupNodeRealizer.setMinimalInsets(new Insets(0, 0, 0, 0));
 //                          groupNodeRealizer.setBorderInsets(new Insets(0, 0, 0, 0));
 //                        }else            {
 ////                        System.out.println("Not GroupNodeRealizer.");
 //                        if(nr instanceof ShapeNodeRealizer)System.out.println("is ShapeNodeRealizer.");
 //                        }
 
 //                       hm.closeGroup(v);
 //                }
 //            }
 //                else {
 ////                        System.out.println("not Group Node."+v.index()+"   .  Center: "+view.getGraph2D().getCenterX(v)+","+view.getGraph2D().getCenterY(v)+"  . Height:"+view.getGraph2D().getSize(v).height+" Width:"+view.getGraph2D().getSize(v).width);
 ////                        System.out.println(" Parent node is: "+hm.getParentNode(v).index());
 //
 ////                         HitInfo hitInfo2 = getHitInfo(353,13);
 ////                                            System.out.println("another node index:"+hitInfo2.getHitNode().index());
 //                    }
 //
 ////                if (hm.isFolderNode(v))
 ////                openFolder(v); // Invokes hm.openFolder(v) ultimately...
 ////                else
 ////                closeGroup(v); // Invokes hm.closeGroup(v) ultimately...
 ////                }
 ////                }
 //
 ////                System.out.println("Node boundary minX is:"+view.getGraph2D().getRealizer(v).getBoundingBox().getMinX());
 ////                System.out.println("Node boundary minY is:"+view.getGraph2D().getRealizer(v).getBoundingBox().getMinY());
 //
 //            }
 //            else
 //            {
 //    //          navigateToParentGraph();
 //                System.out.println("Node is empty");
 //
 //            }
 //        }
 
         public void mouseClicked(double x,double y){
             v=getHitInfo(x,y).getHitNode();
             if(v!=null){
                 //suppose dot lines are all mapping lines, need to be hightlighted
                 for(EdgeCursor ie = v.inEdges();ie.ok();ie.next()){
                     Edge ed=ie.edge();
                     EdgeRealizer edr=view.getGraph2D().getRealizer(ed);
                     if(edr.getLineType()==LineType.DOTTED_1){
                         if(edr.getLineColor()==Color.lightGray) {
                             edr.setLineColor(Color.orange);
                             edr.setLineType(LineType.DASHED_3);
                             edr.setTargetArrow(Arrow.STANDARD);
 //                            edr.getLabel().setModel(EdgeLabel.SIX_POS);
 //                            edr.getLabel().setPosition(EdgeLabel.HEAD);
                             edr.getLabel().setVisible(true);
                             
                         }
 //                        else if(edr.getLineColor()==Color.BLACK) edr.setLineColor(Color.lightGray);
                     }else if(edr.getLineType()==LineType.DASHED_3 && edr.getLineColor()==Color.orange){
                         edr.setLineColor(Color.lightGray);
                         edr.setLineType(LineType.DOTTED_1);
                         edr.setTargetArrow(Arrow.NONE);
                         edr.getLabel().setVisible(false);
                     }
                 }
                 for(EdgeCursor ie = v.outEdges();ie.ok();ie.next()){
                     Edge ed=ie.edge();
                     EdgeRealizer edr=view.getGraph2D().getRealizer(ed);
                     if(edr.getLineType()==LineType.DOTTED_1 && edr.getLineColor()==Color.lightGray){
                         edr.setLineColor(Color.orange);
                         edr.setLineType(LineType.DASHED_3);
                         edr.setTargetArrow(Arrow.STANDARD);
                          edr.getLabel().setVisible(true);
                     }else if(edr.getLineType()==LineType.DASHED_3 && edr.getLineColor()==Color.orange){
                         edr.setLineColor(Color.lightGray);
                         edr.setLineType(LineType.DOTTED_1);
                         edr.setTargetArrow(Arrow.NONE);
                         edr.getLabel().setVisible(false);                        
                     }
                 }
                 view.getGraph2D().updateViews();
             }else{
 
             }
             v=null;
         }
 
         public void mouseReleasedLeft(double x,double y){
             v=null;
             bDragged=false;
         }
 
 //        public void mouseReleasedRight(double x, double y){
 ////            Graph2D graph = view.getGraph2D();
 ////            HierarchyManager hierarchy;
 ////            hierarchy = new HierarchyManager(graph);
 //////
 ////            Node nodes[]=graph.getNodeArray();
 //////            hierarchy.closeGroup(nodes[8]);
 //////                            Rectangle2D.Double vR= graph.getRealizer(nodes[8])
 //            closeGroup(v);
 //        }
 
         public void mouseDraggedLeft(double x,double y){
             bDragged=true;
             HierarchyManager hm;
             Graph2D graph = view.getGraph2D();
             hm = HierarchyManager.getInstance(graph);
 //            double finalX=x,finalY=y;
             finalX=3000;finalY=3000;
 //            boolean iX=false,iY=false;
             if(v==null){
 //                System.out.println("Null:");
                 v=getHitInfo(x,y).getHitNode();
                 if(v!=null){
                     //--------------
                     //--------------
                     //--------------
                     ////get orgX/Y from click Event;   
                     //--------------
                     //--------------
                     //--------------
 
 
 //                    System.out.println("got it:"+v.index());
                     orgX=view.getGraph2D().getCenterX(v);
                     orgY=view.getGraph2D().getCenterY(v);
 
 //                    System.out.println("First  originX:"+orgX+"originY:"+orgY);
 //                                        System.out.println("originX:"+orgX+"originY:"+orgY);
 //                                                                 System.out.println("Selection:"+graph.isSelectionEmpty());
                 }
             }
         if (v!=null){
 
            if(!graph.isSelectionSingleton()) {
 //                   graph.unselectAll(); graph.setSelected(v,true);
 //               System.out.println("Grouped selection...");
               for(NodeCursor nc=graph.selectedNodes();nc.ok();nc.next())
                  boundNodes(graph,hm,nc.node());
            }
             else{
                boundNodes(graph,hm,v);
            }
         }
             
     }
 
 void boundNodes(Graph2D graph,HierarchyManager hm,Node v){
         boolean iX=false,iY=false;
         boolean iXD=false,iYD=false;
         Rectangle2D.Double vR;
         double gX=0,gY=0;
 
         if(hm.getParentNode(v)!=null){
 //q begin: make nodes inside its boundary
             vR= graph.getRealizer(hm.getParentNode(v)).getBoundingBox();
 
             if(graph.getRealizer(v).getBoundingBox().getMinX()<vR.getMinX()) {
                 iX=true;
                 finalX=vR.getMinX()+graph.getRealizer(v).getWidth()/2;
 //                finalX=vR.getMinX()+vR.getWidth()/2;
             }
             else if(graph.getRealizer(v).getBoundingBox().getMaxX()>vR.getMaxX()){
                 iX=true;
                 finalX=vR.getMaxX()-graph.getRealizer(v).getWidth()/2;
 //                finalX=vR.getMaxX()-vR.getWidth()/2;
             }
 
             if(graph.getRealizer(v).getBoundingBox().getMinY()<vR.getMinY()) {
                 iY=true;
                 finalY=vR.getMinY()+graph.getRealizer(v).getHeight()/2;
 //                finalY=vR.getMinY()+vR.getHeight()/2;
             }
             else if(graph.getRealizer(v).getBoundingBox().getMaxY()>vR.getMaxY()) {
                 iY=true;
                 finalY=vR.getMaxY()-graph.getRealizer(v).getHeight()/2;
 //                finalY=vR.getMaxY()-vR.getHeight()/2;
             }
 
             if(iX&&iY) graph.setCenter(v,finalX,finalY);
             else if(iX) graph.setCenter(v,finalX,graph.getCenterY(v));
             else if(iY) graph.setCenter(v,graph.getCenterX(v),finalY);
             iX=false;
             iY=false;
 //            if(iX||iY)System.out.println("Set from : Make nodes inside boundary");
         }
         if((hm.isGroupNode(v)&&(bHasGroupNodeChild(hm,(hm.getParentNode(v)))>1))||(!hm.isGroupNode(v)&&(bHasGroupNodeChild(hm,(hm.getParentNode(v)))>0))){
 // if has same level grouped node, limit nodes out of the bounday of neighbour grouped node
             iX=false;
             iY=false;
             if(!hm.isGroupNode(v)){
 //non grouped nodes should out of boundary of neighbour grouped nodes
                 NodeList slevelGroupedNodes=new NodeList();
                 for(NodeCursor nc=hm.getChildren(hm.getParentNode(v));nc.ok();nc.next()){
                     if(hm.isGroupNode(nc.node())) slevelGroupedNodes.add(nc.node());
                 }
                 Rectangle2D.Double vRec=graph.getRealizer(v).getBoundingBox();
                 for(NodeCursor nc=slevelGroupedNodes.nodes();nc.ok();nc.next()){
                     Rectangle2D.Double ncRec=graph.getRealizer(nc.node()).getBoundingBox();
                     String strResult = boundaryIntersection(graph.getRealizer(v).getBoundingBox(),graph.getRealizer(nc.node()).getBoundingBox());
                     if(strResult.startsWith("L")){
                         iXD=true;
                         gX=ncRec.getMinX()-vRec.getWidth()/2;
                     }
                     else if(strResult.startsWith("R")){
                        iXD=true;
                        gX=ncRec.getMaxX()+vRec.getWidth()/2;
                     }
                   if(strResult.startsWith("T")){
                         iYD=true;
                         gY=ncRec.getMinY()-vRec.getHeight()/2;
                   }
                   else if(strResult.startsWith("B")){
                        iYD=true;
                        gY=ncRec.getMaxY()+vRec.getHeight()/2;
                   }
                   if(iXD) graph.setCenter(v,gX,graph.getCenterY(v));
                   if(iYD) graph.setCenter(v,graph.getCenterX(v),gY);
                     iXD=false;
                     iYD=false;
                 }
             }else{
                 /// to be done later. to process multi grouped nodes in same level.
 
             }
         }
 //q end: make nodes inside its boundary
 //q begin: make nodes outside a sub boundary
 //q begin: make grouped nodes move together and boundary stop when meet nodes.
         if(hm.isGroupNode(v))   //is group node
             {
 //group node should stop when met neighbour nodes
                     //check same level nodes boundary
 //                    Node vLevel=v=getHitInfo(x,y).getHitNode();
 //                    if (vLevel!=null) {
 ////                        Rectangle2D.Double vLevelR= graph.getRealizer(vLevel).getBoundingBox();
 //////
 ////                   System.out.println("Group Node Hit Node."+vLevel.index());
 //                    }
 
 //                    if(hm.getParentNode(v)!=null){
 //////                        ;
 //                        System.out.println("wCurrent Node Index:"+v.index());
 //                    System.out.println("wCurrent Node Parent Index:"+hm.getParentNode(v).index());
             if(hm.getChildren(hm.getParentNode(v)).size()>1){
 //                gX=0;gY=0;
                 Rectangle2D.Double vRec= graph.getRealizer(v).getBoundingBox();
                 for(NodeCursor ncNodes=hm.getChildren(hm.getParentNode(v));ncNodes.ok();ncNodes.next()){
                     Rectangle2D.Double ncRec=graph.getRealizer(ncNodes.node()).getBoundingBox();
                     String strResult=boundaryIntersection(vRec,ncRec);
                     if(strResult.startsWith("L")){
                         iXD=true;
                         gX=ncRec.getMinX()-vRec.getWidth()/2;
                     }
                     else if(strResult.startsWith("R")){
                        iXD=true;
                        gX=ncRec.getMaxX()+vRec.getWidth()/2;
                     }
                     if(strResult.startsWith("T")){
                         iYD=true;
                         gY=ncRec.getMinY()-vRec.getHeight()/2;
                     }
                     else if(strResult.startsWith("B")){
                         iYD=true;
                         gY=ncRec.getMaxY()+vRec.getHeight()/2;
                     }
 //                            switch(boundaryIntersection(vRec,ncRec)){
 //                                case 'L':break;
 //                                case 'R':iX=true;finalX=ncRec.getMaxX()+vRec.getWidth()/2;break;
 //                                case 'T':iY=true;finalY=ncRec.getMinY()-vRec.getHeight()/2;break;
 //                                case 'B':iY=true;break;
 //                            }
 
                     if(iXD){ graph.setCenter(v,gX,graph.getCenterY(v)); iXD=false;}
                     if(iYD){ graph.setCenter(v,graph.getCenterX(v),gY);iYD=false;}
                 }
             }
 //move subnodes of grouped node
 //                System.out.println("Before use  originX:"+orgX+"originY:"+orgY);
                 double dx=graph.getCenterX(v)-orgX;
                 double dy=graph.getCenterY(v)-orgY;
                 NodeList subNodes = new NodeList(hm.getChildren(v));
                 BendList subBends=new BendList();
                 NodeRealizer vnr=graph.getRealizer(v);
                 NodeRealizer vnrp;
                 if(hm.getParentNode(v)!=null)
                     vnrp=graph.getRealizer(hm.getParentNode(v));
                 else
                     vnrp=null;
                 for(BendCursor bends=graph.bends(); bends.ok();bends.next()){
                     Bend b=bends.bend();
                     if(b.isInBox(vnr.getX(),vnr.getY(),vnr.getWidth(),vnr.getHeight()))subBends.add(b); //&&(subNodes.contains(b.getEdge().source())||subNodes.contains(b.getEdge().target()))
                     //source node or target node in box, and bends in parent box
                     else
                     {
                          if(vnrp!=null)
                          {
 //                             if((subNodes.contains(b.getEdge().source())||subNodes.contains(b.getEdge().target()))&& b.isInBox(vnrp.getX(),vnrp.getY(),vnrp.getWidth(),vnrp.getHeight()))
 //                                 subBends.add(b);
 
                          }else
                          {
                              if((subNodes.contains(b.getEdge().source())||subNodes.contains(b.getEdge().target()))&& notInOtherBox(graph,hm,b,v))
                                  subBends.add(b);
                          }
                     }
                 }
 
                 if(bHasGroupNodeChild(hm,v)>0){
                     getMovedNodes(hm,v,subNodes);
                 }
 //                        System.out.println("wCurrent Node:"+ (v.index()));
 //                        for(NodeCursor nT=subNodes.nodes();nT.ok();nT.next()){
 //                            System.out.println("    wChildren:"+ (nT.node().index()));
 //                        }
 
 //                   System.out.println("    orgx:"+ orgX +"  orgy:"+orgY);
 //                    System.out.println("    centerx:"+ graph.getCenterX(v) +"  centery:"+graph.getCenterY(v));
 //                  System.out.println("    dx:"+ dx +"  dy:"+dy);
 //                System.out.println("dx:"+dx+"   dy:"+dy);
 //                System.out.println("Centerx:"+graph.getCenterX(v)+"   Centery:"+graph.getCenterY(v));
                if((dx!=0&&dx!=graph.getCenterX(v)) || (dy!=0&&dy!=graph.getCenterY(v))){
 //                 if(dx!=0 || dy!=0){
 //                   Rectangle vBox=graph.getCenterX() .getBoundingBox(v);
 //                   if(true){
                        graph.moveNodes(subNodes.nodes(),dx,dy);
                        graph.moveBends(subBends.bends(),dx,dy);
 //                   }
 //                   System.out.println("Set from here: Moved for: Grouped nodes should move toeghter.");
                }
                 orgX=view.getGraph2D().getCenterX(v);
                 orgY=view.getGraph2D().getCenterY(v);
             }
 
         }
 
         boolean notInOtherBox(Graph2D graph,HierarchyManager hm,Bend b,Node gpn){
             for(NodeCursor nc=graph.nodes();nc.ok();nc.next()){
                 Node n=nc.node();
                 NodeRealizer nr=graph.getRealizer(n);
                 if(!hm.isGroupNode(nc.node()))continue;
                 else if(nc.node()==gpn) continue;
                 else if(b.isInBox(nr.getX(),nr.getY(),nr.getWidth(),nr.getHeight()))
                 return false;
             }
             return true;
         }
 
         String boundaryIntersection(Rectangle2D.Double dN, Rectangle2D.Double nN){ //dN:dragged Node; nN:neighbourNode
             String bResult="Z";
             if(dN.getMinX()<nN.getMaxX()&& dN.getMinX()>nN.getMinX()){//Left of dragged box between neighbour node's width:from right
                 if((dN.getMinY()<nN.getMinY())&&(dN.getMaxY()>nN.getMaxY()))bResult="R"; //neighbour node between dragged box's height
                 if((dN.getMinY()>nN.getMinY())&&(dN.getMinY()<nN.getMaxY()))bResult="R"; //dragged box's top between neighbour's height
                 if((dN.getMaxY()>nN.getMinY())&&(dN.getMaxY()<nN.getMaxY()))bResult="R";//dragged box's bottom between neighbour's height
             }else if(dN.getMaxX()>nN.getMinX()&& dN.getMaxX()<nN.getMaxX()){ //Right of dragged box between neighbour node's width:from left
                 if((dN.getMinY()<nN.getMinY())&&(dN.getMaxY()>nN.getMaxY()))bResult="L";
                 if((dN.getMinY()>nN.getMinY())&&(dN.getMinY()<nN.getMaxY()))bResult="L";
                 if((dN.getMaxY()>nN.getMinY())&&(dN.getMaxY()<nN.getMaxY()))bResult="L";
             }
 
             if(dN.getMinY()<nN.getMaxY()&&dN.getMinY()>nN.getMinY()){ //Top of dragged box between neighbour node's height:from bottom
                 if((dN.getMinX()<nN.getMinX())&&(dN.getMaxX()>nN.getMaxX()))bResult="B";//neighbour node between dragged box's width
                 if((dN.getMinX()>nN.getMinX())&&(dN.getMinX()<nN.getMaxX()))//dragged box's left between neighbour node's width
                 {
                     if((nN.getMaxX()-dN.getMinX())>(nN.getMaxY()-dN.getMinY())) //(dN.getMinX()-nN.getMinX())>(nN.getMaxY()-dN.getMinY())||
                          bResult="B";
                 }
                 if((dN.getMaxX()>nN.getMinX())&&(dN.getMaxX()<nN.getMaxX()))//dragged box's right between neighbour node's width
                 {
                     if((dN.getMaxX()-nN.getMinX())>(nN.getMaxY()-dN.getMinY()))//||(nN.getMaxX()-dN.getMaxX())>(nN.getMaxY()-dN.getMinY())
                          bResult="B";
                 }
             }else if(dN.getMaxY()>nN.getMinY()&& dN.getMaxY()<nN.getMaxY()){ //Bottom of dragged box between neighbour node's height:from top
                 if((dN.getMinX()<nN.getMinX())&&(dN.getMaxX()>nN.getMaxX()))bResult="T";
                 if((dN.getMinX()>nN.getMinX())&&(dN.getMinX()<nN.getMaxX()))
                 {
                         if((nN.getMaxX()-dN.getMinX())>(dN.getMaxY()-nN.getMinY())){ //(dN.getMinX()-nN.getMinX())>(dN.getMaxY()-nN.getMinY())||
                            bResult="T";
                         }
                 }
                 if((dN.getMaxX()>nN.getMinX())&&(dN.getMaxX()<nN.getMaxX()))
                 {
                         if((dN.getMaxX()-nN.getMinX())>(dN.getMaxY()-nN.getMinY())){//||(nN.getMaxX()-dN.getMaxX())>(dN.getMaxY()-nN.getMinY())
                               bResult="T";
                         }
                 }
             }
             return bResult;
         }
 
         void getMovedNodes(HierarchyManager hm, Node v, NodeList nl){
                NodeCursor nc = hm.getChildren(v);
                     while(nc.ok()){
                         if(hm.isGroupNode(nc.node()))
                         {
                             nl.addAll(hm.getChildren(nc.node()));
                             if(bHasGroupNodeChild(hm,nc.node())>0)getMovedNodes(hm,nc.node(),nl);
                         }
                            nc.next();
                        }
         }
 
         int bHasGroupNodeChild(HierarchyManager hm, Node v){
             int iResult=0;
             if(hm.isGroupNode(v))
             {
                 for(NodeCursor nc = hm.getChildren(v); nc.ok(); nc.next()){
                        if(hm.isGroupNode(nc.node())){
                            iResult++;
                        }
                     }
             }
             return iResult;
         }
     }
 
       private static PathCaseShapeNodeRealizer getSubstrateProductShapeNodeRealizerCompartmentH(String moleculename, boolean isCommon) {
         PathCaseShapeNodeRealizer nr = new PathCaseShapeNodeRealizer();
 
         nr.setShapeType(ShapeNodeRealizer.ELLIPSE);
         if (moleculename.length() > 15)
             moleculename = moleculename.substring(0, 13) + "..";
 
         nr.setSize(20, 20);
         NodeLabel nodelabel = nr.createNodeLabel();
 //        nodelabel.setModel(NodeLabel.EIGHT_POS);
         nodelabel.setText(moleculename);
         nodelabel.setFontName("Arial");
         nodelabel.setFontSize(10);
         nodelabel.setPosition(NodeLabel.CENTER);
 //        nodelabel.setAutoSizePolicy(NodeLabel.AUTOSIZE_NODE_WIDTH);
         //nodelabel.setAutoSizePolicy(NodeLabel.AUTOSIZE_NODE_HEIGHT);
         nr.setLabel(nodelabel);
        nr.setCenter(10,20);
         nr.setVisible(true);
 
         if (!isCommon)
             nr.setNodeRole(PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT);
         else
             nr.setNodeRole(PathCaseShapeNodeRealizer.PathCaseNodeRole.SUBSTRATEORPRODUCT_COMMON);
         //System.out.println(nodelabel.getText());
         return nr;
 
 
     }
 
     class InteractiveZoomMode extends ViewMode {
 
         double initialViewX, initialViewY;
         double initialWorldX, initialWorldY;
         double initialZoomAmount;
 
         public void mousePressedLeft(double x, double y) {
             initialViewX = toViewCoordX(x);
             initialViewY = toViewCoordY(y);
             initialWorldX = x;
             initialWorldY = y;
             initialZoomAmount = view.getZoom();
         }
 
         public void mouseDraggedLeft(double x, double y) {
             double draggedviewx = toViewCoordX(x);
             double draggedviewy = toViewCoordY(y);
 
             double mouseXMovement = (draggedviewx - initialViewX);
             double mouseYMovement = (draggedviewy - initialViewY);
             double mouseMovement = Math.sqrt(mouseXMovement * mouseXMovement + mouseYMovement * mouseYMovement);
             if (mouseYMovement < 0) mouseMovement *= -1;
 
             double nextzoom = initialZoomAmount * (1 + mouseMovement * 0.01f);
 
             if (nextzoom > 0) {
                 view.setZoom(nextzoom);
                 double neworiginX = view.getViewPoint2D().getX() - toWorldCoordX((int) initialViewX) + initialWorldX;
                 double neworiginY = view.getViewPoint2D().getY() - toWorldCoordY((int) initialViewY) + initialWorldY;
 
                 view.setViewPoint2D(neworiginX, neworiginY);
                 view.updateView();
             }
         }
 
         // From world to view coordinates...
         int toViewCoordX(double x) {
             return (int) ((x - view.getViewPoint2D().getX()) * view.getZoom());
         }
 
         int toViewCoordY(double y) {
             return (int) ((y - view.getViewPoint2D().getY()) * view.getZoom());
         }
 
         // ... and vice-versa.
         double toWorldCoordX(int x) {
             return x / view.getZoom() + view.getViewPoint2D().getX();
         }
 
         double toWorldCoordY(int y) {
             return y / view.getZoom() + view.getViewPoint2D().getY();
         }
 
 
     }
 
     private void createViewModes() {
         if (popupMode == null) {
             popupMode = new PopupMenuMode();
         }
         if (navigationMode == null) {
             navigationMode = new NavigationMode();
             navigationMode.setPopupMode(popupMode);
         }
         if (magnifyingMode == null) {
             magnifyingMode = new MagnifierViewMode();
         }
         if (autoDragViewMode == null) {
             autoDragViewMode = new AutoDragViewMode();
         }
         if (areaZoomMode == null) {
             areaZoomMode = new AreaZoomMode();
         }
         if (editMode == null) {
             editMode = new ToolTipEditMode();
             //todo deletion?
             editMode.allowResizeNodes(true);
             editMode.allowNodeEditing(false);
             editMode.allowNodeCreation(false);
             editMode.allowBendCreation(true);
             editMode.allowEdgeCreation(false);
             editMode.setMixedSelectionEnabled(true);
             //editMode.allowNodeEditing(true);
             editMode.showNodeTips(true);
             editMode.setPopupMode(popupMode);
         }
         if (interactiveZoomMode == null) {
             interactiveZoomMode = new InteractiveZoomMode();
         }
         if (tooltipViewMode == null) {
             tooltipViewMode = new TooltipViewMode();
         }
         if (clickViewMode == null) {
             clickViewMode = new ClickViewMode();
         }
     }
 
     protected void destroyViewModes() {
         navigationMode = null;
         autoDragViewMode = null;
         areaZoomMode = null;
         editMode = null;
         interactiveZoomMode = null;
         popupMode = null;
         magnifyingMode = null;
         tooltipViewMode = null;
         clickViewMode=null;
     }
 
     //Instantiates and registers the listeners for the view.
     protected void registerViewListeners() {
         //Note that mouse wheel support requires J2SE 1.4 or higher.
         view.getCanvasComponent().addMouseWheelListener(new Graph2DViewMouseWheelZoomListener());
         view.getCanvasComponent().addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
 //q begin                System.out.println(view.getCanvasComponent().getBounds().getMinX()+","+view.getCanvasComponent().getBounds().getMinY());
 //q begin                System.out.println(view.getCanvasComponent().getBounds().getMaxX()+","+view.getCanvasComponent().getBounds().getMaxY());
 
             }            
         });
 
     }
 
     //////////////////////////    TOOLTIPS   //////////////////////////
 
     protected String getNodeTipText(Node v) {
 
         return pathCaseGUI.getNodeTipText(v);
 
 //        return "";
     }
 
     /////////////////////////   RIGHT CLICK MENUS /////////////////////
 
     protected JPopupMenu getSelectionPopupMenu(final double x,final double y) {
     	JPopupMenu nodePopup = new JPopupMenu();
     	
     	JMenu insertMenu = new JMenu("Insert");
     	nodePopup.add(insertMenu);
     	
     	JMenuItem insertNodeItem = new JMenuItem("Species");
     	insertNodeItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				displayInsertSpecies(x,y);
 				
 			}
     		
     	});
     	
     	insertMenu.add(insertNodeItem);
     	
         return nodePopup;
     }
     protected JPopupMenu getNodePopupMenu(final Node v) {
 
         JPopupMenu nodePopup = new JPopupMenu();
         
         
 
         JMenu insertMenu = new JMenu("Insert");
         nodePopup.add(insertMenu);
         JMenu linkMenu = new JMenu("Link");
 
         PathCaseShapeNodeRealizer.PathCaseNodeRole role = pathCaseGUI.getNodeRole(v);
         switch(role) {
         	case GENERICPROCESS:
         	case REACTION:
         		
         		 JMenuItem addProduct = new JMenuItem("Product");
         		 addProduct.addActionListener(new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						displayInsertProduct(v);
 					}
         			 
         		 });
                  insertMenu.add(addProduct);
                  JMenuItem addReactant = new JMenuItem("Reactant");
                  addReactant.addActionListener(new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						displayInsertReactant(v);
 					}
                 	 
                  });
                  insertMenu.add(addReactant);
                  JMenuItem linkToProduct = new JMenuItem("to Product");
                  linkToProduct.addActionListener(new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						displayLinkReactionToProduct(v);
 					}
                 	 
                  });
                  linkMenu.add(linkToProduct);
                  JMenuItem linkToReactant = new JMenuItem("to Reactant");
                  linkToReactant.addActionListener(new ActionListener() {
 
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						displayLinkReactionToReactant(v);
 					}
                 	 
                  });
                  linkMenu.add(linkToReactant);
                  nodePopup.add(linkMenu);
         	     break;
         	case SUBSTRATEORPRODUCT:
         	case SPECIES:
         	case REACTIONSPECIES:
         		if (linkMode != LINK_TYPE.NONE) {
                 	JMenuItem linkHere = new JMenuItem("Link To Here");
                 	linkHere.addActionListener(new ActionListener() {
 
         				@Override
         				public void actionPerformed(ActionEvent arg0) {
         					switch(linkMode) {
         						case REACTION_TO_PRODUCT:
         							linkReactionToProduct(lastSelectedNode,v);
         					        linkMode = LINK_TYPE.NONE;
         					        lastSelectedNode = null;
         							break;
         						case REACTION_TO_REACTANT:
         							linkReactionToReactant(lastSelectedNode,v);
         					        linkMode = LINK_TYPE.NONE;
         					        lastSelectedNode = null;
         							break;
         						default:
         							break;
         					}
         					
         				}
                 		
                 	});
                 	nodePopup.add(linkHere);
                 }
         		 JMenuItem addReaction = new JMenuItem("Reaction");
         		 addReaction.addActionListener(new ActionListener() {
         			
         			 @Override
         			 public void actionPerformed(ActionEvent arg0) {
         			
         			 }
         			 
         		 });
                  insertMenu.add(addReaction);
                  break;
         	default:
             break;
         }
         
         
         
         if(linkMode != LINK_TYPE.NONE) {
         	JMenuItem cancelLink = new JMenuItem("Cancel Link");
         	cancelLink.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					linkMode = LINK_TYPE.NONE;
 					lastSelectedNode = null;
 				}
         		
         	});
         	nodePopup.add(cancelLink);
         	
         }
         
         JMenuItem delete = new JMenuItem("Delete");
         delete.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				 deleteNode(v);
 			}
         	
         });
         
         nodePopup.add(delete);
         
 
 		JMenu bugMenu = new JMenu("Report a Bug");
 		JMenuItem bugSimpleItem = new JMenuItem("Simple Report");
 		JMenuItem bugAdvancedItem = new JMenuItem("Advanced Report");
 
 		bugSimpleItem.addActionListener(new SendButtonSimpleListener(htmlToString(getNodeTipText(v))));		
 		bugAdvancedItem.addActionListener(new SendButtonAdvancedListener(htmlToString(getNodeTipText(v))));
 		bugMenu.add(bugSimpleItem);
 		bugMenu.add(bugAdvancedItem);
         
         JMenu menu = new JMenu("Layout Options");
 
         JMenuItem item = new JMenuItem("Apply Neighbor Hierarchy");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 makeNeighhborhoodHierarchical(v);
             }
         });
         menu.add(item);
 
         JMenuItem item2 = new JMenuItem("Collapse");
         item2.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
 /*By Xinjian                                       This is commented for new function,which in only used for compartment hierarchy visualization.
                 makeCollapseGroupNode(v);
 By Xinjian End*/
                 makeCollapseGroupNodeCompartmentH(v);
             }
         });
          menu.add(item2);
 
          nodePopup.add(bugMenu);
 
         if (!PATHCASEQUERYINGENABLED)
             return nodePopup;
 
         menu = pathCaseGUI.getPathCaseNodePopupQueries(v);
 
         //if (menu != null) nodePopup.add(menu);
         return nodePopup;
     }
 
     protected void linkReactionToReactant(Node reaction, Node reactant) {
     	Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
     	String reactantId = pathCaseGUI.getUUID(reactant);
 		String reactionId = pathCaseGUI.getPathCaseIdForNode(reaction);
 		TableQueries.addReactantToReaction(pathCaseGUI.repository, reactionId, graph.getRealizer(reactant).getLabelText(), reactantId);
     	Edge edge = graph.createEdge(reaction,reactant);
 		EdgeRealizer er = graph.getRealizer(edge);
         er.setSourceArrow(Arrow.STANDARD);
         graph.updateViews(); 
 	}
 
 	protected void linkReactionToProduct(Node reaction, Node product) {
 		Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		String productId = pathCaseGUI.getUUID(product);
 		String reactionId = pathCaseGUI.getPathCaseIdForNode(reaction);
 		TableQueries.addProductToReaction(pathCaseGUI.repository, reactionId, graph.getRealizer(product).getLabelText(), productId);
 		Edge edge = graph.createEdge(reaction, product);
 		EdgeRealizer er = graph.getRealizer(edge);
         er.setTargetArrow(Arrow.STANDARD);
         graph.updateViews(); 
 	}
 
 
 	protected void displayLinkReactionToReactant(Node v) {
 		linkMode = LINK_TYPE.REACTION_TO_REACTANT;
 		lastSelectedNode = v;
 	}
 
 	protected void displayLinkReactionToProduct(Node v) {
 		linkMode = LINK_TYPE.REACTION_TO_PRODUCT;
 		lastSelectedNode = v;
 	}
 
 	private String htmlToString(String toolTip) {
     	String type = "";
     	try {
     		type = toolTip.substring(toolTip.indexOf("<b>")+3, toolTip.indexOf("</b>"));
     	} catch (StringIndexOutOfBoundsException e) {
     		
     	}
 		String name = "";
 		try {
 			if(toolTip.indexOf("<i>") != -1) {
 				name = toolTip.substring(toolTip.indexOf("<i>")+3, toolTip.indexOf("</i>"));
 			} else {
 				name = toolTip.substring(toolTip.indexOf("</b>")+4, toolTip.indexOf("</body>"));
 			}
 		} catch (StringIndexOutOfBoundsException e) {
 			
 		}
 		return type+name;
     }
 	private String getMethodHierarchy() {
 		StringBuilder methodCalls = new StringBuilder();
 		StackTraceElement[] ste = PathCaseViewGenerator.getStackTrace();
 		for(StackTraceElement s : ste){
 			methodCalls.insert(0, "() ");
 			methodCalls.insert(0, s.getMethodName());
 			methodCalls.insert(0, ".");
 			methodCalls.insert(0, s.getClassName());
 			methodCalls.insert(0, "->");					
 		}
 		return methodCalls.substring(0, methodCalls.length());
 	}
 	
 	private NodeRealizer getCompartmentRealizer(final double x, final double y) {
 		LinkedList<Node> compartments = pathCaseGUI.getGroupNodes(pathCaseGUI.graphViewer.view.getGraph2D());
 		NodeRealizer lastRealizer = null;
 		Node compartment = null;
 		NodeRealizer compartmentRealizer = null;
 		for(Node c : compartments) {
 			NodeRealizer realizer = pathCaseGUI.graphViewer.view.getGraph2D().getRealizer(c);
 			if(realizer.getBoundingBox().contains(x, y)) {
 				//User clicked in a compartment!
 				if(lastRealizer == null) {
 					compartment = c;
 					compartmentRealizer = realizer;
 				}
 				if(lastRealizer != null && (realizer.getWidth()*realizer.getHeight() < lastRealizer.getWidth()*lastRealizer.getHeight())) {
 					compartment = c;
 					compartmentRealizer = realizer;
 				}
 				lastRealizer = realizer;
 			}
 		}
 		
 		return compartmentRealizer;
 	}
 	
 	private void deleteNode(final Node node) {
 		final Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		String id = pathCaseGUI.getUUID(node);
 		PathCaseShapeNodeRealizer.PathCaseNodeRole role = pathCaseGUI.getNodeRole(node);
         switch(role) {
         	case SPECIES:
         	case REACTIONSPECIES:
         	case SUBSTRATEORPRODUCT:
         		graph.removeNode(node);
         		TableQueries.deleteSpecies(pathCaseGUI.repository, id);
         		break;
         	case REACTION:
         	case GENERICPROCESS:
         		graph.removeNode(node);
         		TableQueries.deleteReaction(pathCaseGUI.repository, id);
         		break;
         	default:
         		break;	
         }
 	
 		graph.updateViews();
 	}
 	
 	private void displayInsertReactant(final Node reaction) {
 		final JFrame frame = new JFrame("Insert Reactant");
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
		frame.
 		final Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		final TextField nodeName = new TextField();
 		Label nodeNameLabel = new Label("Please enter the reactants name:");
 		
 		final double x = graph.getX(reaction);
 		final double y = graph.getY(reaction);
 		final NodeRealizer compartmentRealizer = getCompartmentRealizer(x, y);
 		final String cid = pathCaseGUI.getPathCaseIdForNode(compartmentRealizer.getNode());
 
 		
 		JButton addButton = new JButton();
 		addButton.setText("Add");
 		JButton cancelButton = new JButton();
 		cancelButton.setText("Cancel");
 		
 		addButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Node reactant = PathCaseViewGenerator.createNode(graph,compartmentRealizer.getNode(), nodeName.getText(), x+10, y+10, PathCaseShapeNodeRealizer.PathCaseNodeRole.SPECIES);
 				String reactantId = pathCaseGUI.getUUID(reactant);
 				pathCaseGUI.addNodeToDataCache(reactant, pathCaseGUI.getUUID(reactant));
 				String reactionId = pathCaseGUI.getPathCaseIdForNode(reaction);
 				TableQueries.addSpecies(pathCaseGUI.repository, reactantId, nodeName.getText(), reactantId, "", "", "","", true, true, "", true, false,cid);
 				TableQueries.addReactantToReaction(pathCaseGUI.repository, reactionId, nodeName.getText(), reactantId);
 				Edge edge = graph.createEdge(reaction,reactant);
 				EdgeRealizer er = graph.getRealizer(edge);
                 er.setSourceArrow(Arrow.STANDARD);
                 graph.updateViews(); 
 				frame.dispose();
 			}
 		
 		});
 		
 		cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 			
 		});
 		
 		//Layout
 		
 		Container pane = frame.getContentPane();
 		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
 		pane.add(nodeNameLabel);
 		pane.add(nodeName);
 		if(compartmentRealizer != null) {
 			JLabel compartmentLabel = new JLabel("Compartment: " + compartmentRealizer.getLabelText());
 			compartmentLabel.setAlignmentX(SwingConstants.LEFT);
 			 pane.add(compartmentLabel);
 		}
 		Container buttonContainer = new Container();
 		buttonContainer.setLayout(new BoxLayout(buttonContainer,BoxLayout.X_AXIS));
 		buttonContainer.add(cancelButton);
 		buttonContainer.add(addButton);
 		pane.add(buttonContainer);
 		
 		frame.pack();
 		frame.setVisible(true);
 		frame.requestFocus();
 		nodeName.requestFocus();
 	}
 	
 	private void displayInsertProduct(final Node reaction) {
 		final JFrame frame = new JFrame("Insert Product");
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		final Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		final TextField nodeName = new TextField();
 		Label nodeNameLabel = new Label("Please enter the product name:");
 		
 		final double x = graph.getX(reaction);
 		final double y = graph.getY(reaction);
 		final NodeRealizer compartmentRealizer = getCompartmentRealizer(x, y);
 		final String cid = pathCaseGUI.getPathCaseIdForNode(compartmentRealizer.getNode());
 
 		
 		JButton addButton = new JButton();
 		addButton.setText("Add");
 		JButton cancelButton = new JButton();
 		cancelButton.setText("Cancel");
 		
 		addButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Node product = PathCaseViewGenerator.createNode(graph,compartmentRealizer.getNode(), nodeName.getText(), x+10, y+10, PathCaseShapeNodeRealizer.PathCaseNodeRole.SPECIES);
 				String productId = pathCaseGUI.getUUID(product);
 				pathCaseGUI.addNodeToDataCache(product, productId);
 				String reactionId = pathCaseGUI.getPathCaseIdForNode(reaction);
 				TableQueries.addSpecies(pathCaseGUI.repository, productId, nodeName.getText(), productId, "", "", "","", true, true, "", true, false,cid);
 				TableQueries.addProductToReaction(pathCaseGUI.repository, reactionId, nodeName.getText(), productId);
 				Edge edge = graph.createEdge(reaction, product);
 				EdgeRealizer er = graph.getRealizer(edge);
                 er.setTargetArrow(Arrow.STANDARD);
       
                 graph.updateViews(); 
 				frame.dispose();
 			}
 		
 		});
 		
 		cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 			
 		});
 		
 		//Layout
 		
 		Container pane = frame.getContentPane();
 		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
 		pane.add(nodeNameLabel);
 		pane.add(nodeName);
 		if(compartmentRealizer != null) {
 			JLabel compartmentLabel = new JLabel("Compartment: " + compartmentRealizer.getLabelText());
 			compartmentLabel.setAlignmentX(SwingConstants.LEFT);
 			 pane.add(compartmentLabel);
 		}
 		Container buttonContainer = new Container();
 		buttonContainer.setLayout(new BoxLayout(buttonContainer,BoxLayout.X_AXIS));
 		buttonContainer.add(cancelButton);
 		buttonContainer.add(addButton);
 		pane.add(buttonContainer);
 		
 		frame.pack();
 		frame.setVisible(true);
 		frame.requestFocus();
 		nodeName.requestFocus();
 	}
 	
 	private void displayInsertReaction(final double x, final double y) {
 		final JFrame frame = new JFrame("Insert Reaction");
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		final Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		final TextField nodeName = new TextField();
 		Label nodeNameLabel = new Label("Please enter the reaction name:");
 		
 		final NodeRealizer compartmentRealizer = getCompartmentRealizer(x, y);
 		
 		JButton addButton = new JButton();
 		addButton.setText("Add");
 		JButton cancelButton = new JButton();
 		cancelButton.setText("Cancel");
 		
 		addButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Node n = PathCaseViewGenerator.createNode(graph,compartmentRealizer.getNode(), nodeName.getText(), x, y, PathCaseShapeNodeRealizer.PathCaseNodeRole.REACTION);
 				String reactionId = pathCaseGUI.getUUID(n);
 				pathCaseGUI.addNodeToDataCache(n, reactionId);
 				TableQueries.addReaction(pathCaseGUI.repository, reactionId, nodeName.getText(), reactionId);
 				graph.updateViews();
 				frame.dispose();
 			}
 		
 		});
 		
 		cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 			
 		});
 		
 		//Layout
 		
 		Container pane = frame.getContentPane();
 		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
 		pane.add(nodeNameLabel);
 		pane.add(nodeName);
 		if(compartmentRealizer != null) {
 			JLabel compartmentLabel = new JLabel("Compartment: " + compartmentRealizer.getLabelText());
 			compartmentLabel.setAlignmentX(SwingConstants.LEFT);
 			 pane.add(compartmentLabel);
 		}
 		Container buttonContainer = new Container();
 		buttonContainer.setLayout(new BoxLayout(buttonContainer,BoxLayout.X_AXIS));
 		buttonContainer.add(cancelButton);
 		buttonContainer.add(addButton);
 		pane.add(buttonContainer);
 		
 		frame.pack();
 		frame.setVisible(true);
 		frame.requestFocus();
 		nodeName.requestFocus();
 	}
 	
 	private void displayInsertSpecies(final double x, final double y) {
 		final JFrame frame = new JFrame("Insert Species");
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		final Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		final TextField nodeName = new TextField();
 		Label nodeNameLabel = new Label("Please enter the species name:");
 		
 		final NodeRealizer compartmentRealizer = getCompartmentRealizer(x, y);
 		final String cid = pathCaseGUI.getPathCaseIdForNode(compartmentRealizer.getNode());
 		
 		JButton addButton = new JButton();
 		addButton.setText("Add");
 		JButton cancelButton = new JButton();
 		cancelButton.setText("Cancel");
 		
 		addButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Node n = PathCaseViewGenerator.createNode(graph, compartmentRealizer.getNode(), nodeName.getText(), x, y, PathCaseShapeNodeRealizer.PathCaseNodeRole.SPECIES);
 				String tmpPathCaseID = pathCaseGUI.getUUID(n);
 				pathCaseGUI.addNodeToDataCache(n, tmpPathCaseID);
 				TableQueries.addSpecies(pathCaseGUI.repository, tmpPathCaseID, nodeName.getText(), tmpPathCaseID,
 						"", "", "","",true, true, "",true, false,cid);
 				graph.updateViews();
 				frame.dispose();
 			}
 		
 		});
 		
 		cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 			
 		});
 		
 		//Layout
 		
 		Container pane = frame.getContentPane();
 		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
 		pane.add(nodeNameLabel);
 		pane.add(nodeName);
 		if(compartmentRealizer != null) {
 			JLabel compartmentLabel = new JLabel("Compartment: " + compartmentRealizer.getLabelText());
 			compartmentLabel.setAlignmentX(SwingConstants.LEFT);
 			 pane.add(compartmentLabel);
 		}
 		Container buttonContainer = new Container();
 		buttonContainer.setLayout(new BoxLayout(buttonContainer,BoxLayout.X_AXIS));
 		buttonContainer.add(cancelButton);
 		buttonContainer.add(addButton);
 		pane.add(buttonContainer);
 		
 		frame.pack();
 		frame.setVisible(true);
 		frame.requestFocus();
 		nodeName.requestFocus();
 	}
 	
 	private void displayInsertCompartment(final double x, final double y) {
 		final JFrame frame = new JFrame("Insert Reaction");
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		final Graph2D graph = pathCaseGUI.graphViewer.view.getGraph2D();
 		final TextField nodeName = new TextField();
 		Label nodeNameLabel = new Label("Please enter the compartment name:");
 		
 		final NodeRealizer compartmentRealizer = getCompartmentRealizer(x, y);
 		
 		JButton addButton = new JButton();
 		addButton.setText("Add");
 		JButton cancelButton = new JButton();
 		cancelButton.setText("Cancel");
 		
 		addButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Node n = PathCaseViewGenerator.createNode(graph,compartmentRealizer.getNode(), nodeName.getText(), x, y, PathCaseShapeNodeRealizer.PathCaseNodeRole.COMPARTMENT);
 				pathCaseGUI.addNodeToDataCache(n, pathCaseGUI.getUUID(n));
 				graph.updateViews();
 				frame.dispose();
 			}
 		
 		});
 		
 		cancelButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}
 			
 		});
 		
 		//Layout
 		
 		Container pane = frame.getContentPane();
 		pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));
 		pane.add(nodeNameLabel);
 		pane.add(nodeName);
 		if(compartmentRealizer != null) {
 			JLabel compartmentLabel = new JLabel("Parent Compartment: " + compartmentRealizer.getLabelText());
 			compartmentLabel.setAlignmentX(SwingConstants.LEFT);
 			 pane.add(compartmentLabel);
 		}
 		Container buttonContainer = new Container();
 		buttonContainer.setLayout(new BoxLayout(buttonContainer,BoxLayout.X_AXIS));
 		buttonContainer.add(cancelButton);
 		buttonContainer.add(addButton);
 		pane.add(buttonContainer);
 		
 		frame.pack();
 		frame.setVisible(true);
 	}
 	
 
 	private void displaySimpleBugReport(String nodeDesc){
 		final JFrame frame = new JFrame("Report a Bug");
 		frame.setLocationRelativeTo(null);
 		frame.setResizable(false);
 		
 		final TextField bugDesc = new TextField();
 		Label info = new Label("Please enter a bug description:");
 
 		bugDesc.setText(nodeDesc==null?"":nodeDesc);
 		
 		JButton goToAdvancedButton = new JButton();
 		goToAdvancedButton.setBackground(Color.pink);
 		goToAdvancedButton.setText("Details >>");
 		goToAdvancedButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				displayAdvancedBugReport(bugDesc.getText());
 				frame.dispose();
 			}});
 
 		JButton sendButton = new JButton();
 		sendButton.setText("Send");
 		JButton cancelButton = new JButton();
 		cancelButton.setText("Cancel");
 		sendButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				// TODO: BugZilla integration is needed.
 
 				sendBugReport(frame, bugDesc.getText(), getMethodHierarchy(), null, null, null, null, 2, 0, "Simple Bug Report");
 			}});
 		cancelButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				frame.dispose();
 			}});
 
 		frame.getContentPane().setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		frame.getContentPane().add(info, c);
 
 		c.gridx = 0;
 		c.gridy = 1;
 		c.gridwidth = 3;
 		c.weightx = 1;
 		frame.getContentPane().add(bugDesc, c);
 
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weightx = 1;		
 		c.insets = new Insets(10, 10, 10, 90);
 		frame.getContentPane().add(goToAdvancedButton, c);
 
 		c.gridx = 1;
 		c.gridy = 2;
 		c.weightx = 1;		
 		c.insets = new Insets(10, 0, 10, 0);
 		frame.getContentPane().add(sendButton, c);
 
 		c.gridx = 2;
 		c.gridy = 2;
 		c.weightx = 1;
 		c.insets = new Insets(10, 10, 10, 10);			
 		frame.getContentPane().add(cancelButton, c);
 
 		frame.pack();
 		frame.setVisible(true);
     }
 	private void displayAdvancedBugReport(String bugDesc) {
 
 		final JFrame frame = new JFrame("Report a Bug");
 	    
         Label label1 = new java.awt.Label();
         final Checkbox incorrectCompartmentCheckBox = new java.awt.Checkbox();
         final Checkbox incorrectStructureCheckBox = new java.awt.Checkbox();
         final Checkbox incorrectLabelingCheckBox = new java.awt.Checkbox();
         final Checkbox otherCategoryCheckbox = new java.awt.Checkbox();
         Label label2 = new java.awt.Label();
         final TextArea bugDescriptionTextArea = new java.awt.TextArea();
         Label label3 = new java.awt.Label();
         Label label4 = new java.awt.Label();
         Label label5 = new java.awt.Label();
         Label label6 = new java.awt.Label();
         Label label7 = new java.awt.Label();
         final TextField firstNameTextField = new java.awt.TextField();
         final TextField lastNameTextField = new java.awt.TextField();
         final TextField emailTextField = new java.awt.TextField();
         final TextField phoneTextField = new java.awt.TextField();
         final TextField otherCategoryTextField = new java.awt.TextField();
         JSeparator jSeparator1 = new javax.swing.JSeparator();
         JSeparator jSeparator2 = new javax.swing.JSeparator();
         JButton cancelButton = new javax.swing.JButton();
         JButton sendButton = new javax.swing.JButton();
         final JComboBox priorityComboBox = new javax.swing.JComboBox();
         JSeparator jSeparator3 = new javax.swing.JSeparator();
         Label label8 = new java.awt.Label();
 
         frame.setResizable(false);
         bugDescriptionTextArea.setText(bugDesc==null?"":bugDesc);
         label1.setFont(new java.awt.Font("Dialog", 1, 12));
         label1.setText("Please indicate a category:");
         incorrectCompartmentCheckBox.setLabel("Incorrect Compartment");
         incorrectStructureCheckBox.setLabel("Incorrect Structure");
         incorrectLabelingCheckBox.setLabel("Incorrect Labeling");
         otherCategoryCheckbox.setLabel("Other:");
         label2.setFont(new java.awt.Font("Dialog", 1, 12));
         label2.setText("Please enter a detailed descripton:");
         label3.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
         label3.setText("Please enter your contact details(Optional):");
         label4.setText("First Name");
         label5.setText("Last Name");
         label6.setText("E-mail");
         label7.setText("Phone");
         cancelButton.setText("Cancel");
         sendButton.setText("Send");
 
         priorityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "High", "Medium", "Low" }));
         priorityComboBox.setSelectedIndex(1);
         priorityComboBox.setToolTipText("Please choose a severity of the bug...");
 
         label8.setText("Severity:");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(frame.getContentPane());
         frame.getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(bugDescriptionTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(104, 104, 104))
                     .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap(186, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(incorrectCompartmentCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(incorrectStructureCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(incorrectLabelingCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(otherCategoryCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(18, 18, 18)
                                 .addComponent(otherCategoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(106, 106, 106))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                     .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                     .addComponent(lastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addComponent(emailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addComponent(phoneTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                 .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                 .addComponent(firstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                         .addGap(11, 11, 11))
                                     .addComponent(label3, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                                 .addGap(74, 74, 74))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                 .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addContainerGap())
                     .addComponent(jSeparator3, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(priorityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())
                     .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(label2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(bugDescriptionTextArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(label1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(incorrectCompartmentCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(incorrectStructureCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(incorrectLabelingCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(otherCategoryCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(otherCategoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(16, 16, 16)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(label8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(priorityComboBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(label3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(label4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(firstNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(label5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(label6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(label7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(lastNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(emailTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(phoneTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
 		sendButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO: BugZilla integration is needed.
 				
 				StringWriter st = new StringWriter();
 				if(incorrectCompartmentCheckBox.getState())
 					st.append(incorrectCompartmentCheckBox.getLabel()).append(", ");
 				if(incorrectLabelingCheckBox.getState())
 					st.append(incorrectLabelingCheckBox.getLabel()).append(", ");
 				if(incorrectStructureCheckBox.getState())
 					st.append(incorrectStructureCheckBox.getLabel()).append(", ");
 				if(otherCategoryCheckbox.getState())
 					st.append(otherCategoryTextField.getText()).append(", ");
 				String bugTypes = st.toString();
 				
 				sendBugReport(frame, bugDescriptionTextArea.getText(), getMethodHierarchy(), firstNameTextField.getText(), lastNameTextField.getText(), emailTextField.getText(), phoneTextField.getText(), 
 						priorityComboBox.getSelectedIndex()+1, 0 , bugTypes.substring(0, bugTypes.length()-2));				
 			}});
 
 		cancelButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 			}});
 
 		frame.pack();
 		frame.setVisible(true);
 		frame.setLocationRelativeTo(null);
 	}    
     
 	/*
 	 * Send bug report to the configured bug reporting tool. Contact fields can be null.
 	 */
 	private void sendBugReport(final JFrame frame, final String bugDesc, final String callHierarchy, final String firstName, 
 			final String lastName, final String eMail, final String phone, final int severity, final int bugType, final String bugTypeOther)
 	{
 		// Add model name to the bug description.
 		final String bugDescWithModel = String.format("Model: %s<BR/><BR/>%s", PathCaseViewGenerator.getModelName() , bugDesc);
 		
 		PathCaseViewGenerator.getModelName();
 		new Thread() {
 			public void run() {
 				PathwaysService service = new PathwaysService();
 				int bugId = -1;
 				if(service !=null){
 					bugId = service.getPathwaysServiceSoap().insertBug(
 							bugDescWithModel, callHierarchy, 
 							firstName, lastName, eMail, phone, (byte)severity, (byte)bugType, bugTypeOther);				
 				} 
 				if(bugId != -1){
 					frame.dispose();
 					JOptionPane.showMessageDialog(null, String.format("Thank you for your invaluable bug report.\n Your bug is recorded with ID: %d.\n You can communicate with us with this bug id.", bugId), "Thank you", JOptionPane.INFORMATION_MESSAGE);
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "An error has occurred while sending bug report. Please try again.", "Sorry", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}.run();		
 	}
     
     protected JPopupMenu getPaperPopupMenu(final double x, final double y) {
 
         JPopupMenu nodePopup = new JPopupMenu();
         
 
     	JMenu insertMenu = new JMenu("Insert");
     	nodePopup.add(insertMenu);
     	
     	JMenuItem insertSpeciesItem = new JMenuItem("Species");
     	insertSpeciesItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				displayInsertSpecies(x,y);
 				
 			}
     		
     	});
     	
     	insertMenu.add(insertSpeciesItem);
     	
     	JMenuItem insertReactionItem = new JMenuItem("Reaction");
     	insertReactionItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				displayInsertReaction(x, y);
 			}
   
     	});
     	
     	insertMenu.add(insertReactionItem);
     	
     	JMenuItem insertCompartmentItem = new JMenuItem("Compartment");
     	insertCompartmentItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				displayInsertCompartment(x, y);
 			}
   
     	});
     	
     	//insertMenu.add(insertCompartmentItem);
         
         if (!PATHCASEQUERYINGENABLED)
             return nodePopup;
 
         JMenu menu = pathCaseGUI.getPathCasePaperPopupQueries();
         if (menu != null)
             nodePopup.add(menu);
         return nodePopup;
 
     }
 
     protected JPopupMenu getEdgePopupMenu(Edge e) {
         return null;
     }
 
     protected JPopupMenu getEdgeLabelPopupMenu(EdgeLabel label) {
         return null;
     }
 
     protected JPopupMenu getBendPopupMenu(final Bend b) {
         JPopupMenu nodePopup = new JPopupMenu();
         JMenuItem item = new JMenuItem("Remove Bend");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 view.getGraph2D().getRealizer(b.getEdge()).removeBend(b);//view.remove(b);
                 view.updateView();
             }
         });
         nodePopup.add(item);
 
 
         return nodePopup;
     }
 
     ////////////////////////    LAYOUT OPERATIONS    ///////////////////////////////
 
     public boolean makeNeighhborhoodHierarchical(String sourceNodeId) {
 
         Node v = pathCaseGUI.getNodeByPathCaseID(sourceNodeId);
 
         if (v != null) {
             makeNeighhborhoodHierarchical(v);
             return true;
         } else
             return false;
     }
 
     public void makeCollapseGroupNode(Node v) {
          HierarchyManager hm = HierarchyManager.getInstance(view.getGraph2D());
 
 //          HierarchyManager hierarchy;
               Graph2D graph = view.getGraph2D();
 //        hierarchy = new HierarchyManager(graph);
 //        hierarchy.addHierarchyListener(new GroupNodeRealizer.StateChangeListener());
          graph.firePreEvent();
         hm.closeGroup(v);
         graph.firePostEvent();
         view.updateView();
     }
 
     public void makeOpenGroupNodeCompartmentH(Node v) {
         HierarchyManager hm = HierarchyManager.getInstance(view.getGraph2D());
         Graph2D graph = view.getGraph2D();
         if(hm.isGroupNode(v)){
             GroupNodeRealizer nr = new GroupNodeRealizer();
             nr.setTransparent(true);
             graph.setRealizer(v,nr);
 
             hm.openFolder(v);
         }
     }
 
     public void makeCollapseGroupNodeCompartmentH(Node v) {
         HierarchyManager hm = HierarchyManager.getInstance(view.getGraph2D());
         Graph2D graph = view.getGraph2D();
         if(hm.isGroupNode(v)){            //JOptionPane.showMessageDialog(null, "I am happy.");
 //        for(NodeCursor nc= hm.getChildren(v);nc.ok();nc.next()){
 //            graph.hide(nc.node());
 ////            Rectangle2D.Double ncRec=graph.getRealizer(nc.node()).getBoundingBox();
 //        }
 //        graph.hide(v);
             NodeLabel ndl=graph.getRealizer(v).getLabel();
            GroupNodeRealizer nr = new GroupNodeRealizer();
 //        GroupNodeRealizer re=new GroupNodeRealizer();
 //                nr.setShapeType(GroupNodeRealizer.RECT);// ShapeNodeRealizer.RECT
 //                                 nr.setAutoBoundsEnabled(true);
 //                Color selectedColor;
 
                 nr.setTransparent(false);
             Rectangle2D.Double ncRec=graph.getRealizer(v).getBoundingBox();
 
 //
 //                NodeLabel nodelabel = nr.createNodeLabel();
 //                nodelabel.setModel(NodeLabel.TOP );
 //                nodelabel.setPosition(NodeLabel.TOP_RIGHT);
 
 //                nodelabel.setText(label);
 //                nodelabel.setFontName("Arial");
 //                nodelabel.setFontSize(10);
 //                nodelabel.setAutoSizePolicy(YLabel.AUTOSIZE_CONTENT);
 //                nr.setLabel(nodelabel);
             graph.setRealizer(v,nr);
 
 
           hm.closeGroup(v);
            PathCaseShapeNodeRealizer nrp = new PathCaseShapeNodeRealizer();
 
             nrp.setCenter((ncRec.getMaxX()+ncRec.getMinX())/2,(ncRec.getMaxY()+ncRec.getMinY())/2);
             nrp.setShapeType(ShapeNodeRealizer.ROUND_RECT);
             nrp.setFillColor(Color.ORANGE);
             ndl.setPosition(NodeLabel.TOP);
             nrp.setLabel(ndl);
             nrp.setTransparent(false);
             graph.setRealizer(v,nrp);
 
 //        Rectangle2D.Double vR= graph.getRealizer(hm.getParentNode(v)).getBoundingBox();
 //            Node vNew=graph.createNode();
 //            NodeRealizer vNewR=new ShapeNodeRealizer();
 //            vNewR.setCenter((vR.getMinX()+vR.getMaxX())/2,(vR.getMinY()+vR.getMaxY())/2);
 //            vNewR.setSize(35,35);
 //            graph.setRealizer(vNew,vNewR);
         }
 //        graph.hide(v);
 //        graph.updateViews();
         view.updateView();
     }
 
     public void makeNeighhborhoodHierarchical(Node v) {
 
         (new PathCaseLayouterMetabolomics("hierarchical", v)).start(view.getGraph2D());
 
         /*
         HierarchicLayouter layouter = new HierarchicLayouter();
 
         //TODO: MAKE THREADED LAYOUTING
         //createLoaderDialog("Applying layout...");
 
         //layouter.setLayerer(new NeighborLayerer(v));
         layouter.setLayeringStrategy(HierarchicLayouter.LAYERING_BFS);
 
         BooleanNodeMap nodemap = new BooleanNodeMap();
         nodemap.setBool(v, true);
         view.getGraph2D().addDataProvider(BFSLayerer.CORE_NODES, nodemap);
         layouter.doLayout(view.getGraph2D());
         //System.out.println(view.getGraph2D().getLabelText(v));
 
         (new SALabeling()).label(view.getGraph2D());
 
         view.fitContent();
         view.updateView();
         */
 
         //killLoaderDialog();
     }
 
     public void bestLayout() {
         int nodecount = view.getGraph2D().nodeCount();
         int edgecount = view.getGraph2D().edgeCount();
         float density = (float) edgecount / (float) nodecount;
 
         String layout;
 
         if (density > 3) layout = "circular";
         else if (nodecount < 10 && density < 1.5f) layout = "orthogonal";
         else if (nodecount < 40) layout = "hierarchical";
         else layout = "organic";
         //q begin
         layout = "organic";
         //q end
 
         (new PathCaseLayouterMetabolomics(layout)).start(view.getGraph2D());
 
 //        view.fitContent();
 //        view.updateView();
     }
 
     public void initialLayout(String layout, String sourceNode) {
 
         //view pre-layout
         if (view == null || view.getGraph2D() == null) return;
 
         //TODO MAKE THREADED
         // createLoaderDialog("Applying initial layout...");
 
         if (layout == null || layout.equals("")) {
             boolean largelayout = view.getGraph2D().nodeCount() > 50 || view.getGraph2D().edgeCount() > 150;
 
             if (largelayout) {
                 //OrganicLayoutModule module = new OrganicLayoutModule();
                 //module.start(view.getGraph2D());
                 (new PathCaseLayouterMetabolomics("organic")).start(view.getGraph2D());
             } else {
                 //HierarchicLayoutModule module = new HierarchicLayoutModule();
                 //module.start(view.getGraph2D());
                 (new PathCaseLayouterMetabolomics("hierarchical")).start(view.getGraph2D());
             }
         } else if (layout.equals("hierarchical")) {
             if (sourceNode == null || sourceNode.equals("") || !makeNeighhborhoodHierarchical(sourceNode)) {
                 //HierarchicLayoutModule module = new HierarchicLayoutModule();
                 //module.start(view.getGraph2D());
                 (new PathCaseLayouterMetabolomics("hierarchical")).start(view.getGraph2D());
             } else {
                 //initialLayout(null,null);
             }
         } else if (layout.equals("organic")) {
 //            OrganicLayoutModule module = new OrganicLayoutModule();
 //            module.start(view.getGraph2D());
 
 //            graph.i
 //            (new PathCaseLayouterMetabolomics("organic")).start(view.getGraph2D());
     // add new function here to solve the problem: first layout of hierarchy compartments may not be correct
 //            Graph2D graph = view.getGraph2D();
            /*Graph2D graph = view.getGraph2D();
            HierarchyManager hm = HierarchyManager.getInstance(graph);
     //            graph.getn
     //            graph.getNodeArray()
             int i=0;
             Node node1=null, node2=null;
             NodeRealizer tpenr1=null,tpenr2=null;
             for(NodeCursor nc=graph.nodes();nc.ok();nc.next()){
                 if(hm.isGroupNode(nc.node())){
     //                    if(nc.node().)
 //                    Node tnode = graph.createNode();
                     // = new ShapeNodeRealizer();
 
 //            int iResult=0;
 //            if(hm.isGroupNode(v))
 //            {
 //                for(NodeCursor nc = hm.getChildren(v); nc.ok(); nc.next()){
 //                       if(hm.isGroupNode(nc.node())){
 //                           iResult++;
 //                       }
 //                    }
 //            }
 
 //                   HashMap<String, ArrayList<String>> compartment_hierarchy  = new HashMap<String, ArrayList<String>>();
 //                   compartment_hierarchy.put("root",new ArrayList<String>());
 //
                     if(hm.getParentNode(nc.node())!=null){
 //                        compartment_hierarchy.put("root",new ArrayList<String>());
 //                    }
 //                    else{
 //
 //                    }
 //                    NodeRealizer tpenr=graph.getRealizer(hm.getParentNode(nc.node()));
 //                    tpenr.setCenter(250,180);
 //                    tpenr.setLabelText("This is moved");
 //                    tpenr.setLineColor(Color.CYAN);
 //
 //////                    ShapeNodeRealizer tpenr = new ShapeNodeRealizer();
 //////                    tpenr.setLabelText("GroupNode "+(++i));
 //////                    tpenr.setLineColor(Color.green);
 //////                    tpenr.setSize(30,60);
 //////                    tpenr.setCenter(i*15,i*15);
 //                    graph.setRealizer(hm.getParentNode(nc.node()),tpenr);
                     }else{
                         if(node1==null)  {
                             node1=nc.node();
                             tpenr1=graph.getRealizer(node1);
 //                            tpenr1.setLabelText("This is moved1");
 //                            tpenr1.setLineColor(Color.CYAN);
                         }
                         else{
                             node2=nc.node();
                             tpenr2=graph.getRealizer(node2);
 //                            tpenr2.setLabelText("This is moved2");
 //                            tpenr2.setLineColor(Color.BLUE);
 //                            tpenr2.setX(tpenr1.getBoundingBox().getMaxX()+50);
                         }
                     }
                 }
             }
             if(bHasGroupNodeChild(hm,node1)==0){
                 tpenr1.setX(tpenr2.getBoundingBox().getMaxX()+50);
                 rearrangeSubChildren(graph,hm,node1);
             }
             else{
                 tpenr2.setX(tpenr1.getBoundingBox().getMaxX()+50);
                 rearrangeSubChildren(graph,hm,node2);
             }  */
         }
         else
         (new PathCaseLayouterMetabolomics("hierarchical")).start(view.getGraph2D());
 
         //(new SALabeling()).label(view.getGraph2D());
 
 //        view.fitContent();
         view.updateView();
     }
 
       int bHasGroupNodeChild(HierarchyManager hm, Node v){
             int iResult=0;
             if(hm.isGroupNode(v))
             {
                 for(NodeCursor nc = hm.getChildren(v); nc.ok(); nc.next()){
                        if(hm.isGroupNode(nc.node())){
                            iResult++;
                        }
                     }
             }
             return iResult;
         }
     void rearrangeSubChildren(Graph2D graph, HierarchyManager hm, Node v){
         int count=0;
         int chsize=hm.getChildren(v).size();
         NodeRealizer vr=graph.getRealizer(v);
         long  innerwidth=Math.round(Math.sqrt((vr.getHeight()/vr.getWidth())*chsize)+0.5);
         long  innerheight=Math.round(chsize/innerwidth+0.5);
         double curx=vr.getBoundingBox().getMinX()+1,cury=vr.getBoundingBox().getMinY()+1;
                                 NodeRealizer vrinner=null;
         for(NodeCursor nc = hm.getChildren(v); nc.ok(); nc.next()){
             vrinner=graph.getRealizer(nc.node());
             if((((count++)%innerheight)==0)) {
                 cury=vr.getBoundingBox().getMinY()+vrinner.getHeight();
                 curx=vr.getBoundingBox().getMinX()+(count/innerheight)*(1+vr.getWidth()/(innerwidth))+1;
             }else{
                 cury=cury+((count-1)%innerheight)*(vr.getHeight()/(innerheight+2));
             }
             vrinner.setX(Math.min(curx,vr.getBoundingBox().getMaxX()-vrinner.getWidth()));
             vrinner.setY(Math.min(cury,vr.getBoundingBox().getMaxY()-vrinner.getHeight()));
         }
 
 //        for (int i=0;i<ratio;i++){
 //            if((count++)==chsize)continue;
 //            for(int j=0;j<(Math.round(chsize/ratio+0.5));j++){
 //
 //            }
 //        }
 //
 //        graph.getSize()
     }
 
     public void applyGraphLayout(YModule module) {
 
 
         OptionHandler op = module.getOptionHandler();
         if (op != null) {
             if (!op.showEditor())
                 return;
         }
 
         //TODO: make threaded
         //createLoaderDialog("Applying layout...");
 
         module.startAsThread(view.getGraph2D());
 
         //TODO: make threaded
         //killLoaderDialog();
     }
 
     public JPopupMenu getLayoutMenu() {
         JPopupMenu menu = new JPopupMenu();
 
         JMenu customlayoutsmenu = getCustomLayoutMenu();
         menu.add(customlayoutsmenu);
 
         JMenu pathCaseMenu = new JMenu("PathCase Predefined Layouts");
         menu.add(pathCaseMenu);
 
         JMenuItem item = new JMenuItem("Organic");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 (new PathCaseLayouterMetabolomics("organic")).start(view.getGraph2D());
                 view.updateView();
                 view.fitContent();
             }
         });
         pathCaseMenu.add(item);
 
         item = new JMenuItem("Hierarchical");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 (new PathCaseLayouterMetabolomics("hierarchical")).start(view.getGraph2D());
                 view.updateView();
                 view.fitContent();
             }
         });
         pathCaseMenu.add(item);
 
         item = new JMenuItem("Orthogonal");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 (new PathCaseLayouterMetabolomics("orthogonal")).start(view.getGraph2D());
                 view.updateView();
                 view.fitContent();
             }
         });
         pathCaseMenu.add(item);
 
         item = new JMenuItem("Circular");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 (new PathCaseLayouterMetabolomics("circular")).start(view.getGraph2D());
                 view.updateView();
                 view.fitContent();
             }
         });
         pathCaseMenu.add(item);
 
         item = new JMenuItem("Choose Best Layout");
         item.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 bestLayout();
             }
         });
         menu.add(item);
 
 
         return menu;
 
 
     }
 
     protected JMenu getCustomLayoutMenu() {
 
         JMenu layoutmenu = new JMenu("Custom Layout Algorithms");
 
         JMenu layouters = new JMenu("Node Layouters");
         layoutmenu.add((layouters));
         layouters.add(new LaunchModule(new y.module.CircularLayoutModule(), "Circular"));
         layouters.add(new LaunchModule(new y.module.DirectedOrthogonalLayoutModule(), "DirectedOrthogonal"));
         layouters.add(new LaunchModule(new y.module.GRIPModule(), "GRIP"));
         layouters.add(new LaunchModule(new y.module.HierarchicLayoutModule(), "Hierarchic"));
         layouters.add(new LaunchModule(new y.module.IncrementalHierarchicLayoutModule(), "Incremental Hierarchical"));
         layouters.add(new LaunchModule(new y.module.OrganicLayoutModule(), "Organic"));
         layouters.add(new LaunchModule(new y.module.OrthogonalLayoutModule(), "Orthogonal"));
         layouters.add(new LaunchModule(new y.module.CompactOrthogonalLayoutModule(), "Compact Orthogonal"));
         //layouters.add(new LaunchModule(new y.module.ParallelEdgeLayoutModule(), "Parallel Edge"));
         layouters.add(new LaunchModule(new y.module.RandomLayoutModule(), "Random"));
         layouters.add(new LaunchModule(new y.module.ShuffleLayoutModule(), "Shuffle"));
         layouters.add(new LaunchModule(new y.module.SmartOrganicLayoutModule(), "Smart Organic"));
         layouters.add(new LaunchModule(new y.module.TreeLayoutModule(), "Tree"));
         //layouters.add(new LaunchModule(new y.module.ComponentLayoutModule(), "Component Layout"));
 
         JMenu edgerouters = new JMenu("Edge Routers");
         layoutmenu.add((edgerouters));
         edgerouters.add(new LaunchModule(new y.module.OrthogonalEdgeRouterModule(), "Orthogonal"));
         edgerouters.add(new LaunchModule(new y.module.OrganicEdgeRouterModule(), "Organic"));
 
         /*JMenu constraints = new JMenu ("Constraints");
     layoutmenu.add((constraints));
     constraints.add(new LaunchModule(new y.module.EdgeGroupConstraintModule(), "Edge Grouping"));
     constraints.add(new LaunchModule(new y.module.PortConstraintModule(), "Porting")); */
 
         layoutmenu.add(new LaunchModule(new y.module.LabelingModule(), "Labeler"));
 
         layoutmenu.add(new LaunchModule(new y.module.GraphTransformerModule(), "Transformer"));
 
 
         return layoutmenu;
     }
 
     //Launches a generic YModule. If the modules provides an option handler display it before the modules gets launched.
     class LaunchModule extends AbstractAction {
         YModule module;
 
         LaunchModule(YModule module, String title) {
             super(title);
             this.module = module;
         }
 
         public void actionPerformed(ActionEvent e) {
             applyGraphLayout(module);
         }
     }
     class SendButtonAdvancedListener implements ActionListener{
     	private String label; 	
     	public String getLabel() {
 			return label;
 		}
 		public void setLabel(String label) {
 			this.label = label;
 		}
 		
 		public SendButtonAdvancedListener(String name){
 			setLabel(name);
 		}
 		@Override
     		public void actionPerformed(ActionEvent e) {
 				displayAdvancedBugReport(getLabel());
     		}
     }
 
     class SendButtonSimpleListener implements ActionListener{
     	private String label; 	
     	public String getLabel() {
 			return label;
 		}
 		public void setLabel(String label) {
 			this.label = label;
 		}
 		
 		public SendButtonSimpleListener(String name){
 			setLabel(name);
 		}
 		@Override
     		public void actionPerformed(ActionEvent e) {
 				displaySimpleBugReport(getLabel());
     		}
     }
 
 
 }
