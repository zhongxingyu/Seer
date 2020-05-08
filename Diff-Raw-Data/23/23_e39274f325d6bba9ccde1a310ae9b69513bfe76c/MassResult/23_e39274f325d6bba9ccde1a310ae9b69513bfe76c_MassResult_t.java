 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.jdesktop.swingx.graphics.GraphicsUtilities;
 
 import edu.cmu.cs.diamond.opendiamond.Result;
 import edu.cmu.cs.diamond.opendiamond.Util;
 
 public class MassResult extends Result {
     final private Result theResult;
 
     final private BufferedImage thumbnail;

     final private BufferedImage image;
 
     final private boolean malignant;
 
     public MassResult(Result r, int size, int border, int pad) {
         theResult = r;
 
         // is malignant?
         malignant = new String(theResult.getValue("name")).startsWith("TM");
 
         // read image
         BufferedImage b = null;
         try {
             b = ImageIO.read(new ByteArrayInputStream(r.getData()));
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         // draw thumbnail
         BufferedImage t;
         if (b == null) {
             thumbnail = null;
             image = null;
             return;
         }
 
        double scale = Util.getScaleForResize(b.getWidth(), b.getHeight(),
                size, size);
        t = Util.scaleImageFast(b, scale);

         image = GraphicsUtilities.toCompatibleImage(b);
 
         // draw border
         int w = t.getWidth() + (border + pad) * 2;
         int h = t.getHeight() + (border + pad) * 2;
         BufferedImage newThumb;
 
         newThumb = GraphicsUtilities.createCompatibleTranslucentImage(w, h);
         Graphics2D g = newThumb.createGraphics();
         if (malignant) {
             g.setStroke(new BasicStroke(border));
             g.setColor(Color.WHITE);
             g.drawRect(border / 2, border / 2, w - border, h - border);
         }
         g.drawImage(t, border + pad, border + pad, null);
         g.dispose();
 
         thumbnail = newThumb;
     }
 
     public BufferedImage getThumbnail() {
         return thumbnail;
     }
 
     public boolean isMalignant() {
         return malignant;
     }
 
     @Override
     public byte[] getData() {
         return theResult.getData();
     }
 
     @Override
     public List<String> getKeys() {
         return theResult.getKeys();
     }
 
     @Override
     public String getObjectName() {
         return theResult.getObjectName();
     }
 
     @Override
     public String getServerName() {
         return theResult.getServerName();
     }
 
     @Override
     public byte[] getValue(String key) {
         return theResult.getValue(key);
     }
 
     public BufferedImage getImage() {
         return image;
     }
 }
