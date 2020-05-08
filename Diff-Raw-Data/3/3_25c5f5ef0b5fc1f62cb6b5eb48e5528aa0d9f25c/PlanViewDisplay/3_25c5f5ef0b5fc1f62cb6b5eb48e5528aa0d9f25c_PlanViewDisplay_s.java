 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on May 22, 2007
  */
 package com.soartech.simjr.ui.pvd;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import org.apache.log4j.Logger;
 
 import com.soartech.math.Vector3;
 import com.soartech.math.geotrans.Geodetic;
 import com.soartech.shapesystem.CoordinateTransformer;
 import com.soartech.shapesystem.ShapeSystem;
 import com.soartech.shapesystem.SimplePosition;
 import com.soartech.shapesystem.swing.SwingCoordinateTransformer;
 import com.soartech.shapesystem.swing.SwingPrimitiveRendererFactory;
 import com.soartech.simjr.adaptables.Adaptables;
 import com.soartech.simjr.app.ApplicationState;
 import com.soartech.simjr.app.ApplicationStateService;
 import com.soartech.simjr.radios.RadioHistory;
 import com.soartech.simjr.services.ServiceManager;
 import com.soartech.simjr.sim.Entity;
 import com.soartech.simjr.sim.EntityConstants;
 import com.soartech.simjr.sim.Simulation;
 import com.soartech.simjr.sim.Terrain;
 import com.soartech.simjr.sim.entities.AbstractPolygon;
 import com.soartech.simjr.ui.ObjectContextMenu;
 import com.soartech.simjr.ui.SelectionManager;
 import com.soartech.simjr.ui.SelectionManagerListener;
 import com.soartech.simjr.ui.SimulationMainFrame;
 import com.soartech.simjr.ui.actions.ActionManager;
 import com.soartech.simjr.ui.shapes.DetonationShapeManager;
 import com.soartech.simjr.ui.shapes.EntityShape;
 import com.soartech.simjr.ui.shapes.EntityShapeManager;
 import com.soartech.simjr.ui.shapes.SpeechBubbleManager;
 import com.soartech.simjr.ui.shapes.TimedShapeManager;
 import com.soartech.simjr.util.StringTools;
 import com.soartech.simjr.util.SwingTools;
 
 /**
  * @author ray
  */
 public class PlanViewDisplay extends JPanel
 {
     private static final Logger logger = Logger.getLogger(PlanViewDisplay.class);
     
     private static final long serialVersionUID = 6151999888052532421L;
     
 
     private ServiceManager app;
     private Simulation sim;
     private Point contextMenuPoint;
     private ObjectContextMenu contextMenu;
     
     private SelectionManagerListener selectionListener = new SelectionManagerListener(){
 
         public void selectionChanged(Object source)
         {
             appSelectionChanged(source);
         }};
         
     private Timer repaintTimer;
     
     private final SwingCoordinateTransformer transformer = new SwingCoordinateTransformer(this);
     private final SwingPrimitiveRendererFactory factory = new SwingPrimitiveRendererFactory(transformer);
     private final ShapeSystem shapeSystem = new ShapeSystem();
     private final TimedShapeManager timedShapes = new TimedShapeManager(shapeSystem);
     
     private EntityShapeManager shapeAdapter;
     private DistanceToolManager distanceTools;
     private DetonationShapeManager detonationShapes;
     private SpeechBubbleManager speechBubbles;
     private final GridManager grid = new GridManager(transformer);
     
     private final PanAnimator panAnimator = new PanAnimator(transformer);
     
     private Point panOrigin = new Point();
     
     private Point lastDragPoint = new Point(0, 0);
     private boolean draggingEntity = false;
     
     private MapImage map;
     
     private Entity lockEntity;
 
     private CoordinatesPanel coordinates;
     
     private final AppStateIndicator appStateIndicator;
     
     public PlanViewDisplay(ServiceManager app, PlanViewDisplay toCopy)
     {
         setLayout(null);
         
         this.app = app;
         
         this.appStateIndicator = new AppStateIndicator(this.app.findService(ApplicationStateService.class), this);
         
         this.sim = this.app.findService(Simulation.class);
         this.contextMenu = new ObjectContextMenu(this.app);
         this.shapeAdapter = new EntityShapeManager(sim, shapeSystem, factory);
         this.distanceTools = new DistanceToolManager(app, shapeSystem);
         this.detonationShapes = new DetonationShapeManager(sim, timedShapes);
         this.speechBubbles = new SpeechBubbleManager(sim, this.app.findService(RadioHistory.class), shapeAdapter);
         
         setToolTipText(""); // Enable tooltips
         setFocusable(true);
         setBackground(Color.WHITE);
         setBorder(BorderFactory.createLoweredBevelBorder());
         addMouseListener(new MouseHandler());
         addMouseMotionListener(new MouseMotionHandler());
         addMouseWheelListener(new MouseWheelHandler());
         
         final SelectionManager selectionService = SelectionManager.findService(this.app);
         if(selectionService != null)
         {
             selectionService.addListener(selectionListener);
         }
         
         if(toCopy != null)
         {
             setMapImage(toCopy.getMapImage());
         }
         // Periodically redraw the screen rather than trying to only redraw
         // when something changes in the simulation
         repaintTimer = new Timer(200, new ActionListener() {
 
             public void actionPerformed(ActionEvent e)
             {
                 if(!isAnimating())
                 {
                     repaint();
                 }
             }});
         
         repaintTimer.start();
     }
 
     public void dispose()
     {
         logger.info("Disposing PVD " + this);
         SelectionManager.findService(this.app).removeListener(selectionListener);
         repaintTimer.stop();
         appStateIndicator.dispose();
         shapeAdapter.dispose();
         speechBubbles.dispose();
         detonationShapes.dispose();
     }
     
     public EntityShapeManager getShapeAdapter()
     {
         return shapeAdapter;
     }
     
     public ShapeSystem getShapeSystem()
     {
         return shapeSystem;
     }
     
     /**
      * @return the currently installed context menu
      */
     public ObjectContextMenu getContextMenu()
     {
         return contextMenu;
     }
     
     public Point getContextMenuPoint()
     {
         return contextMenuPoint;
     }
 
     /**
      * @param contextMenu the new context menu
      */
     public void setContextMenu(ObjectContextMenu contextMenu)
     {
         if(contextMenu == null)
         {
             throw new NullPointerException("contextMeny cannot be null");
         }
         this.contextMenu = contextMenu;
     }
 
     public Terrain getTerrain()
     {
         return sim.getTerrain();
     }
     
     public CoordinateTransformer getTransformer()
     {
         return transformer;
     }
     
     public void setMapImage(MapImage map)
     {
         this.map = map;
     }
     
     public MapImage getMapImage()
     {
         return map;
     }
     
     public GridManager getGrid()
     {
         return grid;
     }
     
     public DistanceToolManager getDistanceTools()
     {
         return distanceTools;
     }
 
     /**
      * @return the lockEntity
      */
     public Entity getLockEntity()
     {
         return lockEntity;
     }
 
     /**
      * @param lockEntity the lockEntity to set
      */
     public void setLockEntity(Entity lockEntity)
     {
         this.lockEntity = lockEntity;
         ActionManager.update(app);
     }
 
     /**
      * @return The current extents of the view in meters. Origin is at the
      *  <b>bottom</b> left. 
      */
     public Rectangle2D getViewExtentsInMeters()
     {
         Vector3 bottomLeft = transformer.screenToMeters(0, getHeight());
         Vector3 topRight = transformer.screenToMeters(getWidth(), 0);
         
         return new Rectangle2D.Double(bottomLeft.x, bottomLeft.y,
                                       topRight.x - bottomLeft.x,
                                       topRight.y - bottomLeft.y);
     }
     
     /**
      * @return The center of the display in meters. Z is "fixed" to ground 
      *      level.
      */
     public Vector3 getCenterInMeters()
     {
         Vector3 pos = transformer.screenToMeters(getWidth() / 2, getHeight() / 2);
         Geodetic.Point lla = sim.getTerrain().toGeodetic(pos);
         lla.altitude = 0.0;
         return sim.getTerrain().fromGeodetic(lla);
     }
     
     private boolean isAnimating()
     {
         return panAnimator.isAnimating();
     }
     
     public void showPosition(Vector3 p)
     {
         panAnimator.panToPosition(p);
     }
     
     /**
      * Force the given position to be shown in the center of the display
      * 
      * @param p The position to show in meters
      * @param repaint If true, a repaint is forced.
      */
     public void showPosition(Vector3 p, boolean repaint)
     {
         // Note: This function transfers all coordinates to pixels, computes the
         // difference between the current and desired location, and increases
         // the pan-offset accordingly.
 
         // find the current location of (x,y) in meters
         SimplePosition currentPosition = transformer.metersToScreen(p.x, p.y);
 
         // find the position of the center of the screen
         double desiredX = getWidth() / 2;
         double desiredY = getHeight() / 2;
 
         // add the difference to the current offset to create the new offset
         double offsetX = transformer.getPanOffsetX() + desiredX - currentPosition.x;
         double offsetY = transformer.getPanOffsetY() + desiredY - currentPosition.y;
         transformer.setPanOffset(offsetX, offsetY);
 
         if(repaint)
         {
             repaint();
         }
     }
     
     /**
      * Force the display to zoom out to show all of the entities in the 
      * simulation. Does nothing if there are no entities
      */
     public void showAll()
     {
         double minX = Double.MAX_VALUE;
         double minY = Double.MAX_VALUE;
         double maxX = Double.MIN_VALUE;
         double maxY = Double.MIN_VALUE;
         
         boolean visibleEntities = false;
         synchronized(sim.getLock())
         {
             List<Entity> entities = sim.getEntitiesFast();
             if(entities.isEmpty())
             {
                 return;
             }
             
             for(Entity e : entities)
             {
                 if(e.hasPosition())
                 {
                     Vector3 p = e.getPosition();
                     minX = Math.min(minX, p.x);
                     minY = Math.min(minY, p.y);
                     maxX = Math.max(maxX, p.x);
                     maxY = Math.max(maxY, p.y);
                     
                     visibleEntities = true;
                 }
             }
         }
         
         if(!visibleEntities)
         {
             return;
         }
         
         if(maxX - minX < 10.0)
         {
             maxX += 10.0;
             minX -= 10.0;
         }
         if(maxY - minY < 10.0)
         {
             maxY += 10.0;
             minY -= 10.0;
         }
         
         double centerX = (maxX + minX) / 2.0;
         double centerY = (maxY + minY) / 2.0;
         
         showPosition(new Vector3(centerX, centerY, 0.0), false);
         
         double desiredWidth = maxX - minX;
         double desiredHeight = maxY - minY;
         
         Point center = new Point(getWidth() / 2, getHeight() / 2);
         Rectangle2D extents = getViewExtentsInMeters();
         
         if(extents.isEmpty())
         {
             return;
         }
         
         // First zoom in
         while(desiredWidth < extents.getWidth() || 
                 desiredHeight < extents.getHeight())
         {
             controlMouseWheel(center, -1);
             extents = getViewExtentsInMeters();
         }
         
         // Now zoom back out
         while(desiredWidth >= extents.getWidth() || 
               desiredHeight >= extents.getHeight())
         {
             controlMouseWheel(center, 1);
             extents = getViewExtentsInMeters();
         }
         
         // one more for good measure. Otherwise we may end up with entities right
         // on the edge of the screen.
         controlMouseWheel(center, 1);
     }
     
     public void zoom(int amount)
     {
         controlMouseWheel(new Point(getWidth() / 2, getHeight() / 2), amount);
     }
 
     public void zoom(int amount, Point pointToZoomOn)
     {
         controlMouseWheel(pointToZoomOn, amount);
     }
     
     private void showCoordinates()
     {
         if(this.coordinates != null)
         {
             return;
         }
         
         this.coordinates = new CoordinatesPanel();
         this.coordinates.setActivePvd(this);
         add(coordinates);
         coordinates.setBounds(10, 10, 200, 20);
     }
     
 
     /* (non-Javadoc)
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     @Override
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);
         
         if(appStateIndicator.getState() != ApplicationState.RUNNING)
         {
             return;
         }
         
         showCoordinates();
         
         // Briefly lock the sim to update entity shapes and stuff.
         double time = 0.0;
         synchronized (sim.getLock())
         {
             if(lockEntity != null)
             {
                 // TODO: JCC - Remove before finalizing.
                 //lockEntity.setHeading(lockEntity.getHeading() + Math.PI/6400);
                 
                 // TODO: JCC - Clean up. Now centers on the actual entity rather than the shadow.
                 Double agl = (Double) lockEntity.getProperty(EntityConstants.PROPERTY_AGL);
                showPosition(EntityShape.adjustPositionForShadow(lockEntity.getPosition(), agl), false);
                 transformer.setRotation(-lockEntity.getHeading() + Math.PI/2);
             }
             time = sim.getTime();
             shapeAdapter.update();
             detonationShapes.update();
             speechBubbles.update();
             shapeSystem.update(transformer);
         }
         
         // Set up the graphics contexts...
         Graphics2D g2d = (Graphics2D) g;
         SwingTools.enableAntiAliasing(g2d);
         
         Graphics2D g2dCopy = (Graphics2D) g2d.create();
         SwingTools.enableAntiAliasing(g2dCopy);
         
         // Now draw everything again. None of the following code should be
         // dependent on a sim lock.
         if(map != null)
         {
             map.draw(g2d, transformer);
         }
         
         grid.draw(g2d);
         factory.setGraphics2D(g2dCopy, getWidth(), getHeight());
         
         timedShapes.update(time);
         
         shapeSystem.draw(factory);
         shapeSystem.displayErrors(factory);
         //shapeSystem.displayDebugging(factory, transformer);
         
         g2dCopy.dispose();
     }
     
     private Entity getSelectedEntity()
     {
         return Adaptables.adapt(SelectionManager.findService(this.app).getSelectedObject(), Entity.class);
     }
     
     private List<Entity> getSelectedEntities()
     {
         return Adaptables.adaptCollection(SelectionManager.findService(app).getSelection(), Entity.class);
     }
     
     private void appSelectionChanged(Object source)
     {
         shapeAdapter.updateSelection(getSelectedEntities());
         repaint();
     }
     
     private Entity getEntityAtScreenPoint(Point point)
     {
         final List<Entity> entities = shapeAdapter.getEntitiesAtScreenPoint(point.getX(), point.getY(), 15.0);
         return !entities.isEmpty() ? entities.get(0) : null;
     }
     
     private void mouseMoved(MouseEvent e)
     {
         shapeAdapter.highlightEntity(getEntityAtScreenPoint(e.getPoint()));
         repaint();
     }
     
     private void mousePressed(MouseEvent e)
     {
         if(e.isControlDown())
         {
             return;
         }
         
         final Entity entityUnderCursor = getEntityAtScreenPoint(e.getPoint());
         final SelectionManager sm = SelectionManager.findService(this.app);
         final List<Entity> selectedEntities = getSelectedEntities();
         
         if(SwingUtilities.isRightMouseButton(e) || !selectedEntities.contains(entityUnderCursor))
         {
             sm.setSelection(this, entityUnderCursor);
         }
         
         if(SwingUtilities.isRightMouseButton(e))
         {
             return;
         }
         
         draggingEntity = entityUnderCursor != null;
         lastDragPoint.setLocation(e.getPoint());
         
         if(!draggingEntity)
         {
             // change mouse icon to grab icon
             setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
             
             panOrigin.setLocation(e.getPoint());
         }
         
         repaint();
     }
     
     /**
      * Restores the cursor following a drag/pan operation.
      */
     private void mouseReleased(MouseEvent e)
     {
         requestFocus();
         
         // restore the cursor to standard pointer
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         
         final SelectionManager sm = SelectionManager.findService(this.app);
         final List<Entity> selectedEntities = getSelectedEntities();
         final Entity entityUnderCursor = getEntityAtScreenPoint(e.getPoint());
         
         if(SwingUtilities.isRightMouseButton(e))
         {
             contextMenuPoint = e.getPoint();
             contextMenu.show(this, e.getX(), e.getY());
         }
         else if(!e.isControlDown() && selectedEntities.size() > 1)
         {
             // Multi-selection management is done on mouse release.
             sm.setSelection(this, entityUnderCursor);
         }
         else if(e.isControlDown())
         {
             // Multi-selection management is done on mouse release.
             // Ctrl-click adds/removes an entity from the selection
             final List<Object> newSel = new ArrayList<Object>(sm.getSelection());
             if(!newSel.remove(entityUnderCursor))
             {
                 newSel.add(0, entityUnderCursor);
             }
             sm.setSelection(this, newSel);
         }
         
         draggingEntity = false;
         
         repaint();
         
         dragFinished();
     }
     
     private void dragEntity(MouseEvent e)
     {
         assert draggingEntity;
         
         final Entity entity = getSelectedEntity();
         if(entity == null)
         {
             return;
         }
         
         final Boolean locked = (Boolean) entity.getProperty(EntityConstants.PROPERTY_LOCKED);
         if(locked != null && locked.booleanValue())
         {
             return;
         }
         
         final Point screenDelta = new Point(e.getX() - lastDragPoint.x, e.getY() - lastDragPoint.y);
         lastDragPoint.setLocation(e.getPoint());
         final Vector3 delta = new Vector3(transformer.screenToMeters(screenDelta.getX()),
                                          -transformer.screenToMeters(screenDelta.getY()), // Y down
                                           0.0); // Preserve altitude
         synchronized(sim.getLock())
         {
             // If it's a polygon (route, area, etc) move all the points together
             final List<Entity> points;
             final AbstractPolygon polygon = Adaptables.adapt(entity, AbstractPolygon.class);
             if(polygon != null)
             {
                 points = polygon.getPoints();
             }
             else
             {
                 points = Arrays.asList(entity);
             }
             
             for(Entity p : points)
             {
                 moveEntityPreservingAltitude(p, delta);
             }
             
             // Update the properties display while we're dragging
             // TODO: This is a hack.
             final SimulationMainFrame mainFrame = SimulationMainFrame.findService(app);
             if(mainFrame != null)
             {
                 mainFrame.getPropertiesView().refreshModel();
             }
         }
         
         // Don't wait for the timer. This makes the UI a little snappier
         repaint();
     }
 
     private void moveEntityPreservingAltitude(Entity p, final Vector3 delta)
     {
         // Preserve altitude. z is *not* altitude
         final Geodetic.Point oldLla = sim.getTerrain().toGeodetic(p.getPosition());
         final Vector3 newPosition = p.getPosition().add(delta);
         final Geodetic.Point newLla = sim.getTerrain().toGeodetic(newPosition);
         newLla.altitude = oldLla.altitude;
         p.setPosition(sim.getTerrain().fromGeodetic(newLla));
         
         // Update calculated properties if the sim isn't running
         if(sim.isPaused())
         {
             p.updateProperties();
         }
     }
     
     /**
      * 
      */
     private void dragPan(MouseEvent e)
     {
         // modify the pan to account for changes in position of the mouse while dragging
         double offsetX = transformer.getPanOffsetX() + e.getPoint().x - panOrigin.getX();
         double offsetY = transformer.getPanOffsetY() + e.getPoint().y - panOrigin.getY();
         transformer.setPanOffset(offsetX, offsetY);
 
         // reset the pan origin
         panOrigin.setLocation(e.getPoint());
         
         repaint();
     }
     
     public boolean isDraggingEntity()
     {
         return draggingEntity;
     }
     protected void dragFinished()
     {
     }
 
     /**
      * Zooms in or out based on mouse wheel rotation, but retains the mouse
      * point under the cursor.
      */
     private void controlMouseWheel(Point point, int rotation) 
     {
         // capture fixedPoint which is under mouse cursor
         final Vector3 fixedPoint = transformer.screenToMeters(point.getX(), point.getY());
 
         // set the scale
         final double factor = Math.pow(.9, rotation);
         transformer.setScale(transformer.getScale() * factor);
 
         // change offset so that the fixedPoint continues to be under the mouse
         // Note: treat the new screen position as the pan origin
         final SimplePosition newScreenPosition = transformer.metersToScreen(fixedPoint.x, fixedPoint.y);
         final double newX = transformer.getPanOffsetX() + point.getX() - newScreenPosition.x;
         final double newY = transformer.getPanOffsetY() + point.getY() - newScreenPosition.y;
         transformer.setPanOffset(newX, newY);
         
         repaint();
     }
 
     /* (non-Javadoc)
      * @see javax.swing.JComponent#getToolTipText(java.awt.event.MouseEvent)
      */
     @SuppressWarnings("unchecked")
     @Override
     public String getToolTipText(MouseEvent ev)
     {
         // Build the tooltip. By using html, we can make the tooltip 
         // multiline and generally nicer looking.
         final Entity e = getEntityAtScreenPoint(ev.getPoint());
         if(e == null)
         {
             return null;
         }
         
         final Map<String, Object> props = e.getProperties();
         String s = "<html>";
         s += "<b>" + e.getName() + " - " + e.getPrototype() + "</b><br>";
         final Object mgrs = props.get(EntityConstants.PROPERTY_MGRS);
         if(mgrs != null)
         {
             s += "<b>Location:</b> " + mgrs + "<br>";
         }
         final Object freq = props.get(EntityConstants.PROPERTY_FREQUENCY);
         if(freq != null)
         {
             s += "<b>Frequency:</b> " + freq + "<br>";
         }
         final Object voice = props.get(EntityConstants.PROPERTY_VOICE);
         if(voice != null)
         {
             s += "<b>Voice:</b> " + voice + "<br>";
         }
 
         final List<Entity> contents = (List<Entity>) props.get(EntityConstants.PROPERTY_CONTAINS);
         if(contents != null && !contents.isEmpty())
         {
             s += "<b>Contains:</b> " + StringTools.join(contents, ", ");
         }
         s += "</html>";
         
         return s;
     }
 
     private class MouseHandler extends MouseAdapter
     {
         /* (non-Javadoc)
          * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
          */
         @Override
         public void mousePressed(MouseEvent e)
         {
             PlanViewDisplay.this.mousePressed(e);
         }
         
         public void mouseReleased(MouseEvent e)
         {
             PlanViewDisplay.this.mouseReleased(e);
         }
     }
     
     private class MouseMotionHandler extends MouseMotionAdapter
     {
 
         /* (non-Javadoc)
          * @see java.awt.event.MouseMotionAdapter#mouseDragged(java.awt.event.MouseEvent)
          */
         @Override
         public void mouseDragged(MouseEvent e)
         {
             if(SwingUtilities.isLeftMouseButton(e))
             {
                 if (draggingEntity)
                 {
                     PlanViewDisplay.this.dragEntity(e);
                 }
                 else
                 {
                     PlanViewDisplay.this.dragPan(e);
                 }
             }
         }
 
         /* (non-Javadoc)
          * @see java.awt.event.MouseMotionAdapter#mouseMoved(java.awt.event.MouseEvent)
          */
         @Override
         public void mouseMoved(MouseEvent e)
         {
             PlanViewDisplay.this.mouseMoved(e);
         }
     }
     
     private class MouseWheelHandler implements MouseWheelListener
     {
         public void mouseWheelMoved(MouseWheelEvent e) 
         {
             PlanViewDisplay.this.controlMouseWheel(e.getPoint(), e.getWheelRotation());
         }
     }
 }
