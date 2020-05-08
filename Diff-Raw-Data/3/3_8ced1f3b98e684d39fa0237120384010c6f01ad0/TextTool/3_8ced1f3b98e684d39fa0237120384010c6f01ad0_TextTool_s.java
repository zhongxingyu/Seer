 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.gui.desk.paintengine.tool.tools.text;
 
 import cz.mgn.collabcanvas.canvas.utils.graphics.OutlineUtil;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelKeyEvent;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelMouseEvent;
 import cz.mgn.collabcanvas.interfaces.visible.ToolImage;
 import cz.mgn.collabdesktop.gui.desk.paintengine.tool.SimpleMouseCursor;
 import cz.mgn.collabdesktop.gui.desk.paintengine.tool.Tool;
 import cz.mgn.collabdesktop.gui.desk.paintengine.tool.paintdata.SingleImagePaintData;
 import cz.mgn.collabdesktop.utils.ImageUtil;
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import javax.swing.JPanel;
 
 /**
  *
  * @author indy
  */
 public class TextTool extends Tool implements TextImageListener {
 
     protected int x = -1;
     protected int y = -1;
     protected BufferedImage textImage = null;
     protected BufferedImage toolImage = null;
     protected TextPanel toolPanel;
 
     public TextTool() {
         super();
         init(new SimpleMouseCursor(ImageUtil.loadImageFromResources("/resources/tools/text-cursor.gif")),
                 ImageUtil.loadImageFromResources("/resources/tools/text-icon.png"), "Text", "Press to draw text.");
 
         toolPanel = new TextPanel(this);
     }
 
     public void mouseMoved(int x, int y) {
         this.x = x;
         this.y = y;
     }
 
     public void mousePressed(int x, int y) {
         if (textImage != null) {
             canvasInterface.getPaintable().paint(new SingleImagePaintData(new Point(x - (textImage.getWidth() / 2), y - (textImage.getHeight() / 2)), textImage, true));
         }
     }
 
     @Override
     public void setColor(int color) {
         toolPanel.setColor(new Color(color));
     }
 
     @Override
     public JPanel getToolOptionsPanel() {
         return toolPanel;
     }
 
     @Override
     public void textRendered(BufferedImage textImage) {
         this.textImage = textImage;
         toolImage = OutlineUtil.generateOutline(textImage, Color.GRAY, true);
     }
 
     @Override
     public void canvasInterfaceSeted() {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void canvasInterfaceUnset() {
        throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void mouseEvent(CollabPanelMouseEvent e) {
         switch (e.getEventType()) {
             case CollabPanelMouseEvent.TYPE_MOVE:
                 mouseMoved(e.getEventCoordinates().x, e.getEventCoordinates().y);
                 break;
             case CollabPanelMouseEvent.TYPE_PRESS:
                 mousePressed(e.getEventCoordinates().x, e.getEventCoordinates().y);
                 break;
         }
     }
 
     @Override
     public void keyEvent(CollabPanelKeyEvent e) {
     }
 
     @Override
     public ToolImage getToolImage() {
         return null;
     }
 }
