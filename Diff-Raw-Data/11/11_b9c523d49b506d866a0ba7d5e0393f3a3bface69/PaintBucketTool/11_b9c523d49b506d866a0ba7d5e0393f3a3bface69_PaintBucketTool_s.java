 /*
  * Collab desktop - Software for shared drawing via internet in real-time
  * Copyright (C) 2012 Martin Indra <aktive@seznam.cz>
  *
  * This file is part of Collab desktop.
  *
  * Collab desktop is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Collab desktop is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Collab desktop.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.mgn.collabdesktop.room.model.paintengine.tools.tools.paintbucket;
 
 import cz.mgn.collabdesktop.room.model.paintengine.tools.tools.paintbucket.floodfill.FloodFill;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelKeyEvent;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelMouseEvent;
 import cz.mgn.collabdesktop.room.model.paintengine.Canvas;
 import cz.mgn.collabdesktop.room.model.paintengine.PaintEngineInterface;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.Tool;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.paintdata.SimplePaintData;
import cz.mgn.collabdesktop.room.model.paintengine.tools.paintdata.SingleImagePaintData;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.tools.ToolsUtils;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.tools.paintbucket.floodfill.FloodFillResult;
 import cz.mgn.collabdesktop.utils.ImageUtil;
import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 
 /**
  *
  *  @author Martin Indra <aktive@seznam.cz>
  */
 public class PaintBucketTool extends Tool {
 
     protected PaintBucketPanel toolPanel;
     protected float tolerance = 0f;
     protected int color = 0xff000000;
     protected Canvas canvas = null;
     protected BufferedImage toolIcon;
 
     public PaintBucketTool() {
         toolIcon = ImageUtil.loadImageFromResources("/resources/tools/paintbucket-icon.png");
         toolPanel = new PaintBucketPanel();
     }
 
     @Override
     public void setColor(int color) {
         this.color = color;
     }
 
     @Override
     public void setCanvas(Canvas canvas) {
         this.canvas = canvas;
     }
 
     @Override
     public void setPaintEngineInterface(PaintEngineInterface paintEngineInterface) {
     }
 
     @Override
     public void mouseEvent(CollabPanelMouseEvent e) {
         if (e.getEventType() == CollabPanelMouseEvent.TYPE_PRESS) {
             BufferedImage over = canvas.getPaintable().getSelectedLayerImage(null);
             if (e.getEventCoordinates().x >= 0
                     && e.getEventCoordinates().y >= 0
                     && e.getEventCoordinates().x < over.getWidth()
                     && e.getEventCoordinates().y < over.getHeight()) {
                 int localColor = color;
                 FloodFill fill = new FloodFill(over, tolerance);
                 FloodFillResult gen = fill.fill(e.getEventCoordinates().x, e.getEventCoordinates().y, localColor);
                 ArrayList<Point> points = new ArrayList<Point>();
                 points.add(gen.getPosition());
                 canvas.getPaintable().paint(new SimplePaintData(points, gen.getImage(), !e.isControlDown()));
             }
         }
     }
 
     @Override
     public void keyEvent(CollabPanelKeyEvent e) {
     }
 
     @Override
     public BufferedImage getToolIcon() {
         return toolIcon;
     }
 
     @Override
     public BufferedImage getToolIcon(int width, int height) {
         return ToolsUtils.transformToolIcon(toolIcon, width, height);
     }
 
     @Override
     public String getToolName() {
         return "Paint bucket";
     }
 
     @Override
     public String getToolDescription() {
         return "Click to fill area, press CTRL for earsing.";
     }
 
     @Override
     public JPanel getToolPanel() {
         return toolPanel;
     }
 }
