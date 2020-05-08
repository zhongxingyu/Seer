 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package graphview;
 
 import graphview.shapes.NodeAspect;
 import graphview.shapes.EdgeAspect;
 import graphview.shapes.EllipseShape;
 import graphview.shapes.BaseShape;
 import graphview.shapes.BoxShape;
 import graphview.shapes.LineShape;
 import graphview.shapes.ImageShape;
 import graphview.shapes.RootShape;
 import graphview.shapes.TextShape;
 import geometry.Rect;
 import geometry.Vec2;
 import graphevents.ShapeMouseEvent;
 import graphview.layouts.HierarchicalLayout;
 import graphview.shapes.*;
 import graphview.shapes.EdgeAspect.eEdgeAspectType;
 import graphview.shapes.NodeAspect.eNodeAspectType;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.RectangularShape;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import parser.DotParser;
 import property.PropertyPanel;
 
 /**
  *
  * @author Kirill
  */
 
 class MouseState{
     public Vec2 screenPos=new Vec2();
     public Vec2 screenDelta=new Vec2();
     public Vec2 scenePos=new Vec2();
     public Vec2 sceneDelta=new Vec2();
     public int MouseBtnL=0;
     public int MouseBtnR=0;
     public int MouseBtnW=0;
     public int LastBtn=0;
     
     public int getBtn(int nButton)
     {
         switch(nButton)
         {
             case 1: return MouseBtnL;
             case 2: return MouseBtnW;
             case 3: return MouseBtnR;
         };
         return 0;
     };
     
     public void setBtn(int nButton, int val)
     {
         switch(nButton)
         {
             case 1: MouseBtnL=val; break;
             case 2: MouseBtnW=val; break;
             case 3: MouseBtnR=val; break;
         };
         LastBtn=nButton;
     };
 }
 public class GraphScene extends javax.swing.JPanel{
     Vec2 frameSize=new Vec2(1,1);
     Vec2 offset=new Vec2();
     Vec2 scale=new Vec2(1,1);
     BaseShape root=new RootShape();
     MouseState mouseState=new MouseState();
     Rect selectionRect=new Rect();
     Font sceneFont= new Font("Arial",Font.PLAIN,13);
    int drawCount=0;
    int manualUpdateCount=0;
    public boolean bEnableSceneRedraw = true;
     
     int sceneMode=0;
     public static final int SCENE_MODE_NONE = 0;
     public static final int SCENE_MODE_RECTANGLE_SELECT = 1;
     public static final int SCENE_MODE_DRAG_SELECTED = 2;
     public static final int SCENE_MODE_OFFSET = 3;
     public static final int SCENE_MODE_DRAG_GRIP=4;
     
     public PropertyPanel objectProperties=new PropertyPanel();
     
     BaseShape dragTarget=null;
     int selectedGrip=0;
     Rect gridFrameSizeHack=null;
     
     ArrayList<GraphSceneListener> listeners=new ArrayList<GraphSceneListener>();
     
     public GraphScene() {
         initComponents();
     }
 
     private void initComponents() {
 
         addMouseWheelListener(new java.awt.event.MouseWheelListener() {
             public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                 formMouseWheelMoved(evt);
             }
         });
         addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseReleased(java.awt.event.MouseEvent evt) {
                 formMouseReleased(evt);
             }
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 formMouseClicked(evt);
             }
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 formMousePressed(evt);
             }
         });
 
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 formComponentResized(evt);
             }
         });
         addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
             public void mouseDragged(java.awt.event.MouseEvent evt) {
                 formMouseDragged(evt);
             }
             public void mouseMoved(java.awt.event.MouseEvent evt) {
                 formMouseMoved(evt);
             }
         });
         
         objectProperties.addPropertySheetChangeListener(listener);
 
 //        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
 //        this.setLayout(layout);
 //        layout.setHorizontalGroup(
 //            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 //            .addGap(0, 0, Short.MAX_VALUE)
 //        );
 //        layout.setVerticalGroup(
 //            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 //            .addGap(0, 0, Short.MAX_VALUE)
 //        );
     }
     
     ////////////////////LISTENERS/////////////////////////////////////////////
     
     public void addListener(GraphSceneListener list)
     {
         listeners.add(list);
     };
     
     public void notifySceneRedraw()
     {
         for(int i=0;i<listeners.size();i++)
         {
             listeners.get(i).onSceneRedraw();
         };
     };
     
     /////////////////END LISTENERS////////////////////////////////////////////
 
 
     ////////////////////MESSAGES//////////////////////////////////////////////
     void updateMousePos(Vec2 pos)
     {
         pos.x-=this.getLocationOnScreen().x;
         pos.y-=this.getLocationOnScreen().y;
 
         mouseState.screenDelta=pos.minus(mouseState.screenPos);
         mouseState.screenPos=pos;
 
         mouseState.scenePos=fromScreen(mouseState.screenPos);
         mouseState.sceneDelta=mouseState.screenDelta.divide(scale);
     };
 
     boolean bIgnorePropListener=false;
     PropertyChangeListener listener = new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent pce) {
             if(bIgnorePropListener) return;
             bIgnorePropListener=true;
             if(objectProperties.getPropObject()!=null)
             {
                 objectProperties.getPropObject().updateProperties(false);
                 updateScene();
             };
             bIgnorePropListener=false;
         }
     };
     
     
     
     private void formMouseDragged(java.awt.event.MouseEvent evt) {                                  
         updateMousePos(Vec2.fromPoint(evt.getLocationOnScreen()));
         root.processEvent(ShapeMouseEvent.createMouseDrag(mouseState.LastBtn, mouseState.scenePos, mouseState.sceneDelta));
         processSceneMode();
         updateScene();
     }                                 
 
     private void formMouseMoved(java.awt.event.MouseEvent evt) {                                
         updateMousePos(Vec2.fromPoint(evt.getLocationOnScreen()));
         root.processEvent(ShapeMouseEvent.createMouseMove(mouseState.scenePos, mouseState.sceneDelta));
         processSceneMode();
         updateScene();
     }                                                                   
 
     private void formMousePressed(java.awt.event.MouseEvent evt) {                                  
         mouseState.setBtn(evt.getButton(), 1);
         
         BaseShape shape=root.getIntersectedChild(mouseState.scenePos);
         dragTarget=null;
         
         if(shape!=null)
         {
             if(shape.isSelected() || (shape.getParent()!=null && shape.getParent().isSelected()))
             {
                 dragTarget=shape;
                 selectedGrip=dragTarget.isGripIntersect(mouseState.scenePos);
                 if(selectedGrip==0) setSceneMode(SCENE_MODE_DRAG_SELECTED);
                 else setSceneMode(SCENE_MODE_DRAG_GRIP);
             }
             root.processEvent(ShapeMouseEvent.createMousePress(mouseState.LastBtn, mouseState.scenePos));
         }
         else
         {
             if(mouseState.MouseBtnL==1)
                 setSceneMode(SCENE_MODE_RECTANGLE_SELECT);
             else if(mouseState.MouseBtnR==1)
                 setSceneMode(SCENE_MODE_OFFSET);
         };
         
         
         processSceneMode();
         updateScene();
     }                                 
 
     private void formMouseReleased(java.awt.event.MouseEvent evt) {                                   
         mouseState.setBtn(evt.getButton(), 0);
         dragTarget=null;
         
         if(sceneMode==SCENE_MODE_DRAG_GRIP) endSceneMode();
         if(sceneMode==SCENE_MODE_DRAG_SELECTED) endSceneMode();
         if(sceneMode==SCENE_MODE_OFFSET) endSceneMode();
         if(sceneMode==SCENE_MODE_RECTANGLE_SELECT) endSceneMode();
         
         root.processEvent(ShapeMouseEvent.createMouseRelease(mouseState.LastBtn, mouseState.scenePos));
         processSceneMode();
         updateScene();
     }                                  
 
     private void formMouseClicked(java.awt.event.MouseEvent evt) {                                  
         updateMousePos(Vec2.fromPoint(evt.getLocationOnScreen()));
         
         BaseShape shape=root.getIntersectedChild(mouseState.scenePos);
         if(shape!=null)
             objectProperties.fromPropObject(shape);
         else 
             objectProperties.clearShape();
         
         root.clearAllSelection();
         root.processEvent(ShapeMouseEvent.createMouseClick(mouseState.LastBtn, mouseState.scenePos));
         updateScene();
     }
     
     private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {                                     
         if(evt.getWheelRotation()>0) {
             zoom(0.8f);
         }
         else if(evt.getWheelRotation()<0) {
             zoom(1.2f);
         }
     }
     
     private void formComponentResized(java.awt.event.ComponentEvent evt) {              
         frameSize=new Vec2(this.getSize().width,this.getSize().height);
         updateScene();
     }
     
     public void zoom(float mult)
     {
         mouseState.scenePos=fromScreen(mouseState.screenPos);
         Rect screen = getScreenRect();
         Vec2 prevPos=mouseState.scenePos;
         
         setScale(scale.x*mult);
                 
         mouseState.scenePos=fromScreen(mouseState.screenPos);
         Vec2 delta=mouseState.scenePos.minus(prevPos);
         
         setOffset(offset.plus(delta.multiply(scale)));
         
         mouseState.scenePos=fromScreen(mouseState.screenPos);
         updateScene();
     };
     
     void setSceneRect(Rect r)
     {
         setSceneRect(r,getScreenRect());
     };
     
     void setSceneRect(Rect r, Rect screen)
     {
         Rect prev=getSceneRect();
         
         Vec2 newScale=new Vec2(screen.getSize().x/r.getSize().x,screen.getSize().y/r.getSize().y);
         if(newScale.x<newScale.y) newScale.y=newScale.x;
         else if(newScale.x>newScale.y) newScale.x=newScale.y;
         
         Vec2 newOffset=r.getTopLeft().multiply(-1);
         newOffset=newOffset.multiply(newScale);
         
         newOffset.x+=screen.left;
         newOffset.y+=screen.top;
         
         setOffset(newOffset);
         setScale(newScale.x);
         mouseState.scenePos=fromScreen(mouseState.screenPos);
         updateScene();
     };
     
     public void setScale(float sca)
     {
         if(sca>20) return;
         if(sca<0.000000001) return;
         
         this.scale.set(sca,sca);
     };
     
     public void setOffset(Vec2 off)
     {
         if(Math.abs(off.x)<500000 && Math.abs(off.y)<500000)
         {
             offset.set(off);
         };
     };
     
     public void fitScene()
     {
         setSceneRect(root.getChildsRect().getIncreased(10));
     };
 
     Rect getScreenRect()
     {
         Rect r=new Rect();
         r=Rect.fromRectangle2D(getBounds());
         return r;
     };
     
     Rect getSceneRect()
     {
         return fromScreen(getScreenRect());
     };
 
     public void setSceneSelected(BaseShape shape,boolean bSel, boolean bDeselectOther)
     {
         if(shape==null) return;
         
         shape.setSelected(bSel);
         if(bSel)
             objectProperties.fromPropObject(shape);
         else 
             objectProperties.clearShape();
         
         if(bDeselectOther) root.clearAllSelection();
     };
     /////////////////END MESSAGES//////////////////////////////////////////////
     
     ////////////////////SCENE MODE/////////////////////////////////////////////
     public void setSceneMode(int nMode)
     {
         endSceneMode();
         sceneMode=nMode;
         
         if(sceneMode==SCENE_MODE_RECTANGLE_SELECT)
         {
             root.clearAllSelection();
             objectProperties.clearShape();
             selectionRect.set(mouseState.scenePos.x,mouseState.scenePos.y,mouseState.scenePos.x,mouseState.scenePos.y);
             updateScene();
         };
     };
     
     public void endSceneMode()
     {
         sceneMode=0;
         updateScene();
     };
     
     public void processSceneMode()
     {
         if(sceneMode==SCENE_MODE_RECTANGLE_SELECT)
         {
             selectionRect.right=mouseState.scenePos.x;
             selectionRect.bottom=mouseState.scenePos.y;
             
             root.clearSelection();
             for(int i=0;i<root.getNumChilds();i++)
             {
                 if(root.getChild(i)==null) continue;
 
                 if(root.getChild(i).isIntersects(selectionRect.getConvertedToStd()))
                 {
                     setSceneSelected(root.getChild(i),true,false);
                 };
             }
         }
         else if(sceneMode==SCENE_MODE_OFFSET)
         {
             setOffset(offset.plus(mouseState.screenDelta));
         }
         else if(sceneMode==SCENE_MODE_DRAG_SELECTED)
         {
             if(dragTarget==null) 
             {
                 setSceneMode(SCENE_MODE_NONE);
                 return;
             }
             
             if(dragTarget.getParent()==null)
                 dragTarget.move(mouseState.sceneDelta);
             else
             {
                 if(dragTarget.getParent().getShapeAspect()==BaseShape.eShapeAspect.NODE &&
                         dragTarget.getParent()!=root)
                 {
                     setSceneSelected(dragTarget,false,false);
                     dragTarget=dragTarget.getParent();
                     setSceneSelected(dragTarget,true,false);
                 }
 
                 for(int i=0;i<dragTarget.getParent().getNumChilds();i++)
                 {
                     if(dragTarget.getParent().getChild(i)==null) continue;
                     if(dragTarget.getParent().getChild(i).isSelected())
                     {
                         dragTarget.getParent().getChild(i).move(mouseState.sceneDelta);
                     };
                 };
 
             };
         }
         else if(sceneMode==SCENE_MODE_DRAG_GRIP)
         {
             if(dragTarget==null || selectedGrip==0) 
             {
                 setSceneMode(SCENE_MODE_NONE);
                 return;
             }
             
             if(dragTarget.getParent()==null)
                 dragTarget.move(mouseState.sceneDelta);
             else
             {
                 for(int i=0;i<dragTarget.getParent().getNumChilds();i++)
                 {
                     if(dragTarget.getParent().getChild(i)==null) continue;
                     if(dragTarget.getParent().getChild(i).isSelected())
                     {
                         dragTarget.getParent().getChild(i).onGripDragged(selectedGrip,mouseState.sceneDelta);
                     };
                 };
             };
         }
     };
     ///////////////////END SCENE MODE//////////////////////////////////////////
     
     /////////////////////////DRAW//////////////////////////////////////////////
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         //g=getGraphics();
 
         //Graphics2D g2d = (Graphics2D) g;
         Graphics2D g2d = (Graphics2D)g.create();
         
         Shape clip=g2d.getClip();
         
         g2d.setColor(Color.white);
         g2d.fillRect(0, 0, getWidth(), getHeight());
         
         g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
 
         Shape clip3=g2d.getClip();
         draw(g2d);
         drawInfo(g2d);
         Shape clip2=g2d.getClip();
         g2d.dispose();
     }
     
     public void draw(Graphics2D g){
        drawCount++;
         setSceneDrawMode(g);
         //Rectangle rz=this.getBounds();
         //g.setClip(0,0,(int)frameSize.x,(int)frameSize.y);
         g.setFont(sceneFont);
 
         drawGrid(g,new Vec2(100,100));
         root.draw(g);
         
         //Rect chRect=root.getChildsRect().getIncreased(10);
         //g.setColor(Color.orange);
         //g.drawRect((int)chRect.left, (int)chRect.top, (int)chRect.getSize().x, (int)chRect.getSize().y);
         
         if(sceneMode==SCENE_MODE_RECTANGLE_SELECT)
         {
             g.setColor(Color.orange);
             Rect r=selectionRect.getConvertedToStd();
             g.drawRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
         }
         //Rect r=new Rect(mouseState.scenePos.x-5,mouseState.scenePos.y-5,mouseState.scenePos.x+5,mouseState.scenePos.y+5);
         //g.drawRect((int)r.left, (int)r.top, (int)r.getSize().x, (int)r.getSize().y);
     }
     
     public void drawInfo(Graphics2D g)
     {
         setScreenDrawMode(g);
         g.setFont(sceneFont);
         g.setColor(Color.black);
         Rect screen=getSceneRect();
         g.drawString(String.format("scale: %.2f, %.2f", scale.x,scale.y), 0, 20);
         g.drawString(String.format("offset: %.2f, %.2f", offset.x,offset.y), 0, 40);
         g.drawString(String.format("mouseScreen: %.2f, %.2f", mouseState.screenPos.x,mouseState.screenPos.y), 0, 60);
         g.drawString(String.format("mouseScene: %.2f, %.2f", mouseState.scenePos.x,mouseState.scenePos.y), 0, 80);
        g.drawString(String.format("drawCount: %d, mup: %d", drawCount,manualUpdateCount), 0, 100);
     };
     
     public void setScreenDrawMode(Graphics2D g)
     {
         AffineTransform tr=new AffineTransform();
         tr.translate(0,0);
         tr.scale(1,1);
         g.setTransform(tr);
     };
     
     public void setSceneDrawMode(Graphics2D g)
     {
         AffineTransform tr=new AffineTransform();
         tr.translate(offset.x, offset.y);
         tr.scale(scale.x, scale.y);
         g.setTransform(tr);
     };
     
     public void drawGrid(Graphics2D g, Vec2 gridSize)
     {
         Rect frameRect=null;
         
         if(gridFrameSizeHack==null) frameRect=fromScreen(new Rect(0,0,frameSize.x,frameSize.y));
         else frameRect=fromScreen(gridFrameSizeHack);
         
         if((frameRect.getSize().x/gridSize.x)>50) return;
         if((frameRect.getSize().y/gridSize.y)>50) return;
         
         float startX=frameRect.left-frameRect.left%gridSize.x;
         float startY=frameRect.top-frameRect.top%gridSize.y;
         
         
         g.setColor(Color.lightGray);
         
         Stroke oldStroke=g.getStroke();
         BasicStroke stroke=new BasicStroke(1,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0,new float[]{9}, 0);
         g.setStroke(stroke);
         
         for(float i=startX;i<=frameRect.right;i+=gridSize.x)
         {
             if(((int)i)==0) g.setColor(Color.red);
             g.drawLine((int)i, (int)frameRect.top, (int)i, (int)frameRect.bottom);
             if(((int)i)==0) g.setColor(Color.lightGray);
         };
         
         for(float i=startY;i<=frameRect.bottom;i+=gridSize.y)
         {
             if(((int)i)==0) g.setColor(Color.red);
             g.drawLine((int)frameRect.left, (int)i, (int)frameRect.right, (int)i);
             if(((int)i)==0) g.setColor(Color.lightGray);
         };
         g.setStroke(oldStroke);
     };
     
     void updateScene()
     {
        if(!bEnableSceneRedraw) return;
        manualUpdateCount++;
         updateUI();
         notifySceneRedraw();
     };
     
     public void drawOverview(Graphics g, Rect r){
         Graphics2D g2d = (Graphics2D)g.create();
         g2d.setColor(Color.white);
         g2d.fillRect(0, 0, (int)r.getSize().x, (int)r.getSize().y);
         
         Rect oldSceneRect=getSceneRect();
         
         Rect chRect=root.getChildsRect().getIncreased(10);
         Rect scrChRect=new Rect();
         
         scrChRect.bottom=r.right*(chRect.getSize().y/chRect.getSize().x);
         scrChRect.right=r.bottom*(chRect.getSize().x/chRect.getSize().y);
         
         if(scrChRect.getSize().x<r.getSize().x)
             scrChRect.move(new Vec2((r.getSize().x-scrChRect.getSize().x)/2,0));
         
         if(scrChRect.getSize().y<r.getSize().y)
             scrChRect.move(new Vec2(0,(r.getSize().y-scrChRect.getSize().y)/2));
         
         setSceneRect(chRect,scrChRect);
         Rect newChRect=this.toScreen(chRect);
         Rect newSceneRect=this.toScreen(oldSceneRect);
         
         g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
 
         gridFrameSizeHack=newChRect;
         draw(g2d);
         gridFrameSizeHack=null;
         
         setScreenDrawMode(g2d);
         Area sceneRect=new Area(new Rectangle2D.Float(0, 0, (int)r.getSize().x, (int)r.getSize().y));
         sceneRect.exclusiveOr(new Area(new Rectangle2D.Float((int)newSceneRect.left, (int)newSceneRect.top, (int)newSceneRect.getSize().x, (int)newSceneRect.getSize().y)));
         
         Color col=new Color(0,0,0,100);
         Color col1=new Color(255,0,0,150);
         g2d.setColor(col);
         g2d.fill(sceneRect);
         g2d.setColor(Color.black);
         g2d.drawRect((int)newSceneRect.left, (int)newSceneRect.top, (int)newSceneRect.getSize().x, (int)newSceneRect.getSize().y);
         
         if(newSceneRect.left>r.right)
         {
             g2d.setColor(col1);
             g2d.fillPolygon(new int[]{(int)r.right-10,(int)r.right,(int)r.right,(int)r.right-10},
                     new int[]{(int)r.top+10,(int)r.top,(int)r.bottom,(int)r.bottom-10},4);
         };
         
         if(newSceneRect.top>r.bottom)
         {
             g2d.setColor(col1);
             g2d.fillPolygon(new int[]{(int)r.left,(int)r.left+10,(int)r.right-10,(int)r.right},
                     new int[]{(int)r.bottom,(int)r.bottom-10,(int)r.bottom-10,(int)r.bottom},4);
         };
         
         if(newSceneRect.right<r.left)
         {
             g2d.setColor(col1);
             g2d.fillPolygon(new int[]{(int)r.left+10,(int)r.left,(int)r.left,(int)r.left+10},
                     new int[]{(int)r.top+10,(int)r.top,(int)r.bottom,(int)r.bottom-10},4);
         };
         
         if(newSceneRect.bottom<r.top)
         {
             g2d.setColor(col1);
             g2d.fillPolygon(new int[]{(int)r.left,(int)r.left+10,(int)r.right-10,(int)r.right},
                     new int[]{(int)r.top,(int)r.top+10,(int)r.top+10,(int)r.top},4);
         };
         
         g2d.dispose();
         
         setSceneRect(oldSceneRect);
     }
     
     public Image drawToImage(int sizeX, int sizeY, NodeAspect shape)
     {
         BufferedImage img = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g2d = img.createGraphics();
         
         Rect shapeRect=shape.getGlobalRectangle();
         Vec2 newSize=shapeRect.getSize();
         Vec2 center=shapeRect.getCenter();
         if(newSize.x<newSize.y) newSize.x=newSize.y;
         else if(newSize.x>newSize.y) newSize.y=newSize.x;
         
         shapeRect.set(center.x-newSize.x/2,center.y-newSize.y/2,center.x+newSize.x/2,center.y+newSize.y/2);
         
         
         sizeX-=2;
         sizeY-=2;
         Vec2 newScale=new Vec2((float)sizeX/newSize.x,(float)sizeY/newSize.y);
         Vec2 newOffset=shapeRect.getTopLeft();
         
         //shape.setGlobalRectangle(new Rect(0,0,sizeX-1,sizeY-1));
         
         
         try {
             g2d.translate(newOffset.x, newOffset.y);
             g2d.scale(newScale.x, newScale.y);
 
             g2d.setColor(new Color(255,255,255,0));
             g2d.fillRect(0, 0, sizeX, sizeY);
 
             g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
             g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
 
             shape.draw(g2d);
         } finally {
             g2d.dispose();
         }
         
         //shape.setGlobalRectangle(oldRect);
         return img;
     };
     //////////////////////END DRAW/////////////////////////////////////////////
     
     /////////////////UNIT CONVERSION///////////////////////////////////////////
     public Vec2 fromScreen(Vec2 pt)
     {
         AffineTransform tr=new AffineTransform();
         tr.translate(offset.x, offset.y);
         tr.scale(scale.x, scale.y);
         
         Point.Float pt1=new Point.Float();
         try {
             tr.inverseTransform(pt.toPoint(), pt1);
         } catch (NoninvertibleTransformException ex) {
             Logger.getLogger(GraphScene.class.getName()).log(Level.SEVERE, null, ex);
         }
         return new Vec2(pt1.x,pt1.y);
     };
     
     public Rect fromScreen(Rect r)
     {
         Vec2 topLeft=fromScreen(r.getTopLeft());
         Vec2 bottomRight=fromScreen(r.getBottomRight());
         return new Rect(topLeft.x,topLeft.y,bottomRight.x,bottomRight.y);
     }
     
     public Vec2 toScreen(Vec2 pt)
     {
         AffineTransform tr=new AffineTransform();
         tr.translate(offset.x, offset.y);
         tr.scale(scale.x, scale.y);
         
         Point.Float pt1=new Point.Float();
         tr.transform(pt.toPoint(), pt1);
         return new Vec2(pt1.x,pt1.y);
     };
     
     public Rect toScreen(Rect r)
     {
         Vec2 topLeft=toScreen(r.getTopLeft());
         Vec2 bottomRight=toScreen(r.getBottomRight());
         return new Rect(topLeft.x,topLeft.y,bottomRight.x,bottomRight.y);
     }
     /////////////END UNIT CONVERSION///////////////////////////////////////////
     
     /////////////////////SHAPES////////////////////////////////////////////////
     public void addShape(BaseShape shape){
         root.addChild(shape);
     }
     
     public void removeAllShapes()
     {
         root.removeAllChilds();
     };
     
     public void removeShape(BaseShape shape)
     {
         if(shape==null) return;
         if(shape==root) return;
         if(shape.getParent()==null) return;
         
         shape.getParent().removeChild(shape.getIndex());
     };
     //////////////////END SHAPES///////////////////////////////////////////////
     
     
     /////////////////SHAPE CREATION////////////////////////////////////////////
     
     public static NodeAspect createNodeShape(eNodeAspectType shapeType, eNodeAspectType containmentType)
     {
         NodeAspect shape=createNodeShape(shapeType);
         shape.setContainerMode(NodeAspect.eContainerType.RESIZE_CHILDS_TO_PARENT);
         if(containmentType==eNodeAspectType.TEXT)
         {
             shape.createLabel("Text");
         }
         else shape.addChild(createNodeShape(containmentType));
         
         return shape;
     };
     
     public static NodeAspect createNodeShape(eNodeAspectType shapeType)
     {
         switch(shapeType)
         {
             case BOX: return new BoxShape(new Rect(0,0,80,80));
             case TRINGLE: return new TringleShape(new Vec2(0, 0), 3, 0);
             case TEXT: return new TextShape("12345678");
             case ELLIPSE: return new EllipseShape(new Rect(0,0,80,80));
             case IMAGE: return new ImageShape(new Rect(0,0,80,80),"res/images/default.png");
         }
         return null;
     };
     
     public static EdgeAspect createEdgeShape(eEdgeAspectType shapeType)
     {
         switch(shapeType)
         {
             case SIMPLE_LINE: return new LineShape(null,null);
         }
         return null;
     };
     
     public GraphNode createTextCircleNode(String txt, Color col)
     {
         NodeAspect node=createNodeShape(eNodeAspectType.ELLIPSE);
         node.createLabel(txt);
         node.setColor(col);
         node.setContainerMode(NodeAspect.eContainerType.RESIZE_PARENT_TO_CHILDS_EQUI);
         node.fitToChilds(true);
         return createNode(node);
     };
     ////////////////END SHAPE CREATION/////////////////////////////////////////
     
     /////////////////EDGES/NODES CREATION//////////////////////////////////////
     private int nodeCount=0;
     private int edgeCount=0;
     private ArrayList<GraphNode> nodesArray = new ArrayList<GraphNode>();
     private ArrayList<GraphEdge> edgesArray = new ArrayList<GraphEdge>();
     
     public GraphNode createNode(eNodeAspectType shapeType, eNodeAspectType containmentType)
     {
         GraphNode node=createNode(shapeType);
         node.getAspect().setContainmentObject(containmentType);
         return node;
     };
     
     public GraphNode createNode(eNodeAspectType shapeType)
     {
         GraphNode node=new GraphNode(nodesArray.size(),GraphScene.createNodeShape(shapeType));
         nodesArray.add(node);
         root.addChild(node.getAspect());
         nodeCount++;
         return node;
     };
     
     public GraphNode createNode(NodeAspect shape)
     {
         GraphNode node=new GraphNode(nodesArray.size(),shape);
         nodesArray.add(node);
         root.addChild(node.getAspect());
         nodeCount++;
         return node;
     };
     
     public GraphEdge createEdge(int from, int to, eEdgeAspectType shapeType)
     {
         if(!testNodeID(from)) {System.err.println("Cant create edge!"); return null;};
         if(!testNodeID(to)) {System.err.println("Cant create edge!"); return null;};
         
         GraphEdge edge=new GraphEdge(edgesArray.size(),from,to,shapeType);
         edgesArray.add(edge);
         nodesArray.get(from).addEdge(edge.getID());
         nodesArray.get(to).addEdge(edge.getID());
         root.addChild(edge.getAspect());
         edgeCount++;
         edge.syncronize(this);
         return edge;
     };
     
     public GraphEdge createEdge(int from, int to, EdgeAspect shape)
     {
         if(!testNodeID(from)) {System.err.println("Cant create edge!"); return null;};
         if(!testNodeID(to)) {System.err.println("Cant create edge!"); return null;};
         
         GraphEdge edge=new GraphEdge(edgesArray.size(),from,to,shape);
         edgesArray.add(edge);
         nodesArray.get(from).addEdge(edge.getID());
         nodesArray.get(to).addEdge(edge.getID());
         root.addChild(edge.getAspect());
         edgeCount++;
         edge.syncronize(this);
         return edge;
     };
     
     public void removeNode(int id)
     {
         if(!testNodeID(id)) return;
         
         GraphNode node=nodesArray.get(id);
         
         while(node.getSizeOfNodeEdgesIDArray()!=0)
         {
             int edgeID=node.getElementOfNodeEdgesIDArray(0);
             removeEdge(edgeID);
         }
         
         removeShape(node.getAspect());
         nodesArray.set(id,null);
         nodeCount--;
     };
     
     public void removeEdge(int id)
     {
         if(!testEdgeID(id)) return;
         
         GraphEdge edge=edgesArray.get(id);
         
         if(edge.getFromID()!=-1)
         {
             nodesArray.get(edge.getFromID()).deleteEdgeFromArray(id);
         };
         
         if(edgesArray.get(id).getToID()!=-1)
         {
             nodesArray.get(edge.getToID()).deleteEdgeFromArray(id);
         };
         
         removeShape(edge.getAspect());
         edgesArray.set(id,null);
         edgeCount--;
     };
     
     boolean testNodeID(int id)
     {
         if((id<0) || (id>=nodesArray.size())) return false;
         if(nodesArray.get(id)==null) return false;
         return true;
     };
     
     boolean testEdgeID(int id)
     {
         if((id<0) || (id>=edgesArray.size())) return false;
         if(edgesArray.get(id)==null) return false;
         return true;
     };
     
     public GraphNode getNode(int id)
     {
         if(!testNodeID(id)) return null;
         return nodesArray.get(id);
     };
     
     public GraphEdge getEdge(int id)
     {
         if(!testEdgeID(id)) return null;
         return edgesArray.get(id);
     };
     
     /**
      * Функция для получения размера массива вершин
      * @return Возвращает размер массива вершин
      */
     public int getSizeNodeArray()
     {
         return nodesArray.size();
     }
     
     /**
      * Функция для получения размера массива ребер
      * @return Возвращает размер массива ребер
      */
     public int getSizeEdgeArray()
     {
         return edgesArray.size();
     }
    
     /**
      * Функция для получения количества вершин графа
      * @return Возвращает количество вершин графа
      */
     public int getCountNodes()
     {
         return this.nodeCount;
     }
     
     /**
      * Функция для получения количества ребер графа
      * @return Возвращает количество ребер графа
      */
     public int getCountEdges()
     {
         return this.edgeCount;
     }
 
     /**
      * Функция для удаления всех ребер и вершин графа
      */
     public void removeAllItems()
     {
         nodeCount=0;
         edgeCount=0;
         this.edgesArray.clear();
         this.nodesArray.clear();
         removeAllShapes();
     };
     
     public boolean loadDot(String filename){
         removeAllItems();
         Reader stream=null;
         File f = new File(filename);
         try {
             stream = new InputStreamReader(new FileInputStream(f), "cp1251");
         } catch (Exception ex) {
             Logger.getLogger(GraphScene.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         DotParser parser=new DotParser(stream,this);
         boolean b=parser.parse();
         updateScene();
         applySimpleLayout();
         fitScene();
         //applyTestLayout();
         //applyRadialLayout();
         return b;
     }
     
     public void applySimpleLayout()
     {
         //removeAllItems();
         GraphNode Node = null;
         int NodesPerLineCounter = 1;
         int NodesPerColumnCounter = 1;
         Vec2 DemencionsOfNode = new Vec2();
         Vec2 MaxDemencions = new Vec2();
         Vec2 tempPlacement = new Vec2();
         BaseShape NodeShape = null;
         int NodeCount = getCountNodes();
         double tempNodesPerLine = Math.sqrt(NodeCount);
         int NodesPerLine = (int) Math.round(tempNodesPerLine);
         for (int i = 0; i<NodeCount; i++)
         {
             Node = getNode(i);
             DemencionsOfNode = Node.getAspect().getGlobalRectangle().getSize();
             MaxDemencions.x = Math.max(MaxDemencions.x, DemencionsOfNode.x);
             MaxDemencions.y = Math.max(MaxDemencions.y, DemencionsOfNode.y);
         }
         for (int i = 0; i<NodeCount; i++)
         {
             Node = getNode(i);
             tempPlacement.x = MaxDemencions.x*2*NodesPerLineCounter;
             tempPlacement.y = MaxDemencions.y*2*NodesPerColumnCounter;
             Node.getAspect().setPosition(tempPlacement);
             if (NodesPerLineCounter == NodesPerLine)
             {
                 NodesPerLineCounter = 1;
                 NodesPerColumnCounter++;
             }
             else
             {
                 NodesPerLineCounter++;
             }
             
         }
         updateScene();
         
         ArrayList<ArrayList<GraphNode>> clusters=GraphUtils.findClusters(this);
         
         Color c=null;
         for(int i=0;i<clusters.size();i++)
         {
             c=GraphUtils.nextColor(c);
             for(int j=0;j<clusters.get(i).size();j++)
             {
                 clusters.get(i).get(j).getAspect().highlight(c);
             };
         };
     };
     
     public void applyRadialLayout()
     {
         //removeAllItems();
         GraphNode Node = null;
         Vec2 DemencionsOfNode = new Vec2();
         Vec2 MaxDemencions = new Vec2();
         Vec2 tempPlacement = new Vec2();
         Vec2 center = new Vec2();
         center.x = 0;
         center.y = 0;
         int NodeCount = getCountNodes();
         double SectorSize;
         double tempNodesPerLine = Math.sqrt(NodeCount);
         int NodesPerLine = (int) Math.round(tempNodesPerLine);
         double Radius;
         for (int i = 0; i<NodeCount; i++)
         {
             Node = getNode(i);
             DemencionsOfNode = Node.getAspect().getGlobalRectangle().getSize();
             MaxDemencions.x = Math.max(MaxDemencions.x, DemencionsOfNode.x);
             MaxDemencions.y = Math.max(MaxDemencions.y, DemencionsOfNode.y);
         }
         //Вычисляем радиус круга
         Radius = (double)Math.max(MaxDemencions.x, MaxDemencions.y)*NodesPerLine*3;
         //Вычисляем размер сектора в углах
         SectorSize = (double)358/NodeCount;
         //РАсстановка вершин по кругу с радиусом Radius и Центром в 0.0
         for(int i = 0; i<NodeCount;i++)
         {
             Node = getNode(i);
             float tempX = (float)(Radius*Math.cos(SectorSize*i));
             tempPlacement.x = (center.x + tempX);
             float tempY = (float)(Radius*Math.sin(SectorSize*i));
             tempPlacement.y = (center.y + tempY);
             Node.getAspect().setPosition(tempPlacement);
         }
         updateScene();
     };
     
     public void applyTestLayout()
     {
         //removeAllItems();
         HierarchicalLayout layout=new HierarchicalLayout();
         layout.applyLayout(this);
         updateScene();
     };
     
     public boolean setFromEdge(int edgeID, int fromID)
     {
         if(edgesArray.get(edgeID).setFrom(fromID, this))
         {          
             return true;                     
         }
         else
         {
             return false;
         }
         
     }
     
     public boolean setToEdge(int edgeID, int toID)
     {
         if(edgesArray.get(edgeID).setTo(toID, this))
         {          
             return true;                     
         }
         else
         {
             return false;
         }       
                 
     }
         
     public void clearAllSelection()
     {
         root.clearAllSelection();
     }
     /////////////////END EDGES/NODES CREATION//////////////////////////////////
 }
