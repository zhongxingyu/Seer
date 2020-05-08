 package br.ufrj.dcc.compgraf.im.crop;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 
 import br.ufrj.dcc.compgraf.im.ui.UIContext;
 import br.ufrj.dcc.compgraf.im.ui.swing.ext.ScrollablePicture;
 
 public class CropMouseAdapter extends MouseAdapter
 {
   
   private static CropMouseAdapter instance;
   
   private boolean nextClickIsCrop = false;
   
   private CropMouseAdapter() {}
   
   public static CropMouseAdapter instance()
   {
     if (instance == null)
       instance = new CropMouseAdapter();
     
     return instance;
   }
 
   @Override
   public void mouseClicked(MouseEvent e)
   {
     if (!nextClickIsCrop)
     {
       Point clickedPixel = new Point(e.getX(), e.getY());
       CropContext.instance().setClickedPixel(clickedPixel);
     
       ((Component) e.getSource()).repaint();
       
       nextClickIsCrop = true;
     }
     else
     {
       CropContext cc = CropContext.instance();
       
       int startx = (int) cc.getClickedPixel().getX();
       int starty = (int) cc.getClickedPixel().getY();
       int endx = (int) cc.getCurrentPixel().getX();
       int endy = (int) cc.getCurrentPixel().getY();
       
       BufferedImage cropped = new Cropper().crop(UIContext.instance().getCurrentImage(), startx, starty, endx, endy);
       UIContext.instance().changeCurrentImage(cropped);
       
       cc.setCropRunning(false);
       
       ScrollablePicture pic = (ScrollablePicture) e.getSource();
       pic.repaint();
     }
   }
   
   @Override
   public void mouseMoved(MouseEvent e)
   {
     Point currentPixel = new Point(e.getX(), e.getY());
     CropContext.instance().setCurrentPixel(currentPixel);
     
     ((Component) e.getSource()).repaint();
   }
   
 }
