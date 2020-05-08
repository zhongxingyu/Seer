 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.room.model.paintengine.tools.tools.text;
 
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelKeyEvent;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelMouseEvent;
 import cz.mgn.collabdesktop.room.model.paintengine.Canvas;
 import cz.mgn.collabdesktop.room.model.paintengine.PaintEngineInterface;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.Tool;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.paintdata.SingleImagePaintData;
 import cz.mgn.collabdesktop.room.model.paintengine.tools.tools.ToolsUtils;
 import cz.mgn.collabdesktop.utils.ImageUtil;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import javax.swing.JPanel;
 
 /**
  *
  * @author indy
  */
 public class TextTool extends Tool implements TextToolPanelListener {
 
     protected Canvas canvas = null;
     protected TextToolPanel panel;
     protected BufferedImage toolIcon;
     protected BufferedImage textImage = null;
 
     public TextTool() {
         toolIcon = ImageUtil.loadImageFromResources("/resources/tools/text-icon.png");
         panel = new TextToolPanel(this);
     }
 
     @Override
     public void setColor(int color) {
         panel.setColor(new Color(color));
     }
 
     @Override
     public void setCanvas(Canvas canvas) {
         this.canvas = canvas;
         if(canvas != null && textImage != null) {
             canvas.getVisible().setToolImage(new TextToolImage(textImage));
         }
     }
 
     @Override
     public void setPaintEngineInterface(PaintEngineInterface paintEngineInterface) {
     }
 
     @Override
     public void mouseEvent(CollabPanelMouseEvent e) {
         if (e.getEventType() == CollabPanelMouseEvent.TYPE_PRESS) {
             if (canvas != null && textImage != null) {
                 boolean add = !e.isControlDown();
                 Point point = new Point(e.getEventCoordinates().x - (textImage.getWidth() / 2), e.getEventCoordinates().y - (textImage.getHeight() / 2));
                 canvas.getPaintable().paint(new SingleImagePaintData(point, textImage, add));
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
         return "Text";
     }
 
     @Override
     public String getToolDescription() {
        return "Click on canvas to draw text.";
     }
 
     @Override
     public JPanel getToolPanel() {
         return panel;
     }
 
     @Override
     public void textRendered(BufferedImage textImage) {
         this.textImage = textImage;
         if (canvas != null) {
             canvas.getVisible().setToolImage(new TextToolImage(textImage));
         }
     }
 }
