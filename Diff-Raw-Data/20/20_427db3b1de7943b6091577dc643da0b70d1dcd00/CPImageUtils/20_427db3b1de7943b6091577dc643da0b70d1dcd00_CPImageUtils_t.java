 package chibipaint.util;
 
 import chibipaint.engine.CPLayer;
 import chibipaint.engine.CPSelection;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import java.awt.image.MemoryImageSource;
 
 public class CPImageUtils
 {
 public static void PasteImageToOrigin (CPLayer layer, Image img)
 {
   int[] data = layer.getData ();
   MemoryImageSource imgSource = new MemoryImageSource (layer.getWidth (), layer.getHeight (), data, 0, layer.getWidth ());
   Image layerImage = Toolkit.getDefaultToolkit ().createImage (imgSource);
   int imageType = BufferedImage.TYPE_INT_ARGB;
   BufferedImage bI = new BufferedImage (layer.getWidth (), layer.getHeight (), BufferedImage.TYPE_INT_ARGB);
   Graphics2D g = bI.createGraphics ();
   g.drawImage (layerImage, 0, 0, null);
   g.drawImage (img, 0, 0, null);
   layer.setData (((DataBufferInt) bI.getData ().getDataBuffer ()).getData ());
 }
 
 public static Image RenderLayerSelectionToImage (CPLayer layer, CPSelection curSelection)
 {
   CPRect rect = curSelection.getBoundingRect ();
   int[] dataClone = layer.getData ().clone ();
  curSelection.applySelectionToData (dataClone);
   MemoryImageSource imgSource = new MemoryImageSource (layer.getWidth (), layer.getHeight (), dataClone, 0, layer.getWidth ());
   Image layerImage = Toolkit.getDefaultToolkit ().createImage (imgSource);
   BufferedImage bI = new BufferedImage (layer.getWidth (), layer.getHeight (), BufferedImage.TYPE_INT_ARGB);
   Graphics2D g = bI.createGraphics ();
   g.drawImage (layerImage, 0, 0, null);
   return bI.getSubimage (rect.left, rect.top, rect.getWidth (), rect.getHeight ());
 }
 }
