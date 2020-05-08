 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.gui.desk.paintengine.tool.tools.brushable.brush;
 
 import java.awt.*;
import java.awt.image.*;
 import java.util.ArrayList;
 import java.util.Random;
 
 /**
  *
  * @author indy
  */
 public class Brush {
 
     protected BrushListener brushListener = null;
     //
     protected String name = "";
     protected BufferedImage brushImage = null;
     protected BufferedImage paintImage = null;
     protected float step = 1f;
     protected Color color = Color.BLACK;
     protected float scale = 1f;
     protected float jitter = 0f;
     protected float opacity = 1f;
 
     public Brush(BufferedImage brushImage, String name) {
         this.name = name;
         if (brushImage.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
             this.brushImage = new BufferedImage(brushImage.getWidth(), brushImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
             Graphics2D g = (Graphics2D) this.brushImage.getGraphics();
             g.drawImage(brushImage, 0, 0, null);
             g.dispose();
         } else {
             this.brushImage = brushImage;
         }
         step = Math.max(1f, (float) Math.max(brushImage.getWidth(), brushImage.getHeight()) / 4f);
         init();
     }
 
     public void setBrushListener(BrushListener brushListener) {
         this.brushListener = brushListener;
     }
 
     public Brush cloneBrush() {
         BufferedImage cloneBrushImage = new BufferedImage(brushImage.getWidth(), brushImage.getHeight(), brushImage.getType());
         brushImage.copyData(cloneBrushImage.getRaster());
         return new Brush(cloneBrushImage, name);
     }
 
     public String getName() {
         return name;
     }
 
     protected void init() {
         generate();
     }
 
     protected void generate() {
         generatePaintImage();
     }
 
     protected void generatePaintImage() {
         int w = (int) (brushImage.getWidth() * scale);
         w = Math.max(1, w);
         int h = (int) (brushImage.getHeight() * scale);
         h = Math.max(1, h);
        
         paintImage = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g = (Graphics2D) paintImage.getGraphics();
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
         g.drawImage(brushImage, 0, 0, w, h, null);
         g.dispose();
        
        for(int x = 0; x < paintImage.getWidth(); x++) {
            for(int y = 0; y < paintImage.getHeight(); y++) {
                Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), paintImage.getAlphaRaster().getPixel(x, y, new int[1])[0]);
                paintImage.setRGB(x, y, c.getRGB());
            }
        }
     }
 
     public float getScale() {
         return scale;
     }
 
     public float getJitter() {
         return jitter;
     }
 
     public float getStep() {
         return step;
     }
 
     public float getOpacity() {
         return opacity;
     }
 
     public void setColor(int color) {
         this.color = new Color(color);
         generate();
     }
 
     public void setScale(float scale) {
         this.scale = scale;
         generate();
         if (brushListener != null) {
             brushListener.brushScaled(scale);
         }
     }
 
     public void setStep(float step) {
         this.step = step;
         if (brushListener != null) {
             brushListener.brushStep(step);
         }
     }
 
     public void setJitter(float jitter) {
         this.jitter = jitter;
         if (brushListener != null) {
             brushListener.brusheJitter(jitter);
         }
     }
 
     public void setOpacity(float opacity) {
         this.opacity = opacity;
         generate();
     }
 
     public BufferedImage getBrushImage() {
         return brushImage;
     }
 
     public PaintBrush paintLine(int x1, int y1, int x2, int y2) {
         x1 -= paintImage.getWidth() / 2;
         x2 -= paintImage.getWidth() / 2;
         y1 -= paintImage.getHeight() / 2;
         y2 -= paintImage.getHeight() / 2;
 
 
         PaintBrush paintBrush = new PaintBrush(paintImage);
         int size = Math.max(paintImage.getWidth(), paintImage.getHeight());
 
         float dx = x2 - x1;
         float dy = y2 - y1;
         float steps = (float) Math.sqrt(dx * dx + dy * dy) / (step * scale);
         dx /= steps;
         dy /= steps;
 
         float x = x1;
         float y = y1;
         Random random = null;
         if (jitter != 0) {
             random = new Random();
         }
 
         for (int i = 0; i <= steps; i++) {
             float xr = x;
             float yr = y;
             if (random != null) {
                 float localJitter = jitter * (float) size * random.nextFloat();
                 float adx = ((random.nextFloat() * 2f) - 1f) * localJitter;
                 float ady = (float) Math.sqrt(localJitter * localJitter - adx * adx);
                 if (random.nextBoolean()) {
                     ady *= -1;
                 }
                 xr += adx;
                 yr += ady;
             }
             paintBrush.addPoint((int) xr, (int) yr);
             x += dx;
             y += dy;
         }
 
         return paintBrush;
     }

     public class PaintBrush {
 
         protected ArrayList<Point> points = new ArrayList<Point>();
         public BufferedImage paint = null;
 
         public PaintBrush(BufferedImage paint) {
             this.paint = paint;
         }
 
         public void addPoint(int x, int y) {
             points.add(new Point(x, y));
         }
 
         public ArrayList<Point> getPoints() {
             return points;
         }
     }
 }
