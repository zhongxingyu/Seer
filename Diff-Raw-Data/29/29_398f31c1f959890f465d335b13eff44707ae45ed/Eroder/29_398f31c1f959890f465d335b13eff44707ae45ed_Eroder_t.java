 package com.purplefrog.minecraftExplorer.tree;
 
 import com.purplefrog.minecraftExplorer.*;
 import com.purplefrog.minecraftExplorer.landscape.*;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.image.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: thoth
  * Date: 10/17/13
  * Time: 11:02 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Eroder
 {
     public static void main(String[] argv)
     {
         int sz = 60;
        for (int i=0; i<10; i++) {
            int seed = 4262 + i;
            Random rand = new Random(seed);
            popUpFrame(sz, rand, "eroder "+seed);
        }
    }

    private static void popUpFrame(int sz, Random rand, String title)
    {
        JPanel p = new JPanel(new BorderLayout());

         Matrix m = generateSample1(sz, rand);
 
         if (true) {
             while (true) {
                 boolean [] dirty = new boolean[] { false};
                 p.add(new ImageWidget(GaussLumps.toIcon(toImageSource(m))));
                 Matrix m1 = erode2(m, dirty, rand);
                 if (!dirty[0]) {
                     break;
                 }
                 m = m1;
             }
             GridLayout layout = new GridLayout(6, 0);
             layout.setHgap(4);
             layout.setVgap(4);
             p.setLayout(layout);
         } else {
             m = erode(m, rand);
 
 
             ImageProducer ip = toImageSource(m);
             ImageWidget imageWidget = new ImageWidget(GaussLumps.toIcon(ip));
 
             p.add(imageWidget);
         }
 
        JFrame fr = new JFrame(title);
 
         fr.getContentPane().add(p);
 
         fr.pack();
         fr.setSize(1000,900);
         fr.setVisible(true);
     }
 
     private static MemoryImageSource toImageSource(Matrix m)
     {
         return new MemoryImageSource(m.w, m.h, colorize(m), 0, m.w);
     }
 
     public static Matrix generateSample1(int sz, Random rand)
     {
         int w = sz;
         int h = sz;
         Matrix m = new Matrix(w, h);
 
         for (int i = 0; i < m.raster.length; i++) {
             byte b = m.raster[i];
             m.raster[i] = 1;
         }
 
         if (false) {
         for (int x=0; x<w; x++) {
             m.set(x,h-1, 2);
         }
         } else {
             for (int y=0; y<h; y++) {
                 m.set(w/2, y, 2);
             }
         }
 
         for (int i=0; i<20;i++) {
             m.set(rand.nextInt(w), rand.nextInt(h), 2);
         }
         return m;
     }
 
     public static int[] colorize(Matrix m)
     {
         int[] rval = new int[m.raster.length];
         for (int i = 0; i < m.raster.length; i++) {
             byte b = m.raster[i];
             int c;
             if (b==0)
                 c = 0xffffffff;
             else if (b==1)
                 c = 0xff000000;
             else
                 c = 0xffff0000;
             rval[i] = c;
         }
         return rval;
     }
 
 
     public static Matrix erode(Matrix m, Random rand)
     {
         while (true) {
             boolean[] dirty = new boolean[] { false};
             Matrix rval = erode2(m, dirty, rand);
             if (!dirty[0])
                 break;
             m = rval;
             System.out.println("tick");
         }
         return m;
     }
 
     public static Matrix erode2(Matrix m, boolean[] dirty, Random rand)
     {
         m = erode1(m,dirty, 2, rand);
         m = erode1(m,dirty, 8, rand);
         m = erode1(m,dirty, 0x20, rand);
         m = erode1(m,dirty, 0x80, rand);
         return m;
     }
 
     private static Matrix erode1(Matrix m, boolean[] dirty, int polarity, Random rand)
     {
         Matrix rval = new Matrix(m);
 
         for (int x=0; x<m.w; x++) {
             for (int y=0; y<m.h; y++) {
                 int v0 = m.get(x,y);
                 if (v0!=1) {
                     // anchor
                     continue;
                 }
 
                 int code= erosionCode(m.get(x - 1, y - 1, 0) > 0,
                     m.get(x, y - 1, 0) > 0,
                     m.get(x + 1, y - 1, 0) > 0,
                     m.get(x - 1, y, 0) > 0,
                     v0 > 0,
                     m.get(x + 1, y, 0) > 0,
                     m.get(x - 1, y + 1, 0) > 0,
                     m.get(x, y + 1, 0) > 0,
                     m.get(x + 1, y + 1, 0) > 0)
                     ;
 
                if (shouldErode(code) && 0 != (code & polarity)) {
                    if (rand.nextInt(5) > 0) {
                        rval.set(x,y,0);
                    }
                     dirty[0] = true;
                 }
 
             }
         }
         return rval;
     }
 
     public static int erosionCode(boolean xm1ym1_, boolean xym1, boolean xp1ym1, boolean xm1y, boolean xy, boolean xp1y, boolean xm1yp1, boolean xyp1, boolean xp1yp1)
     {
         return (xm1ym1_ ? 1:0)
             |(xym1 ? 2:0)
             |(xp1ym1 ? 4:0)
             |(xm1y ? 8:0)
             |(xy ? 0x10:0)
             |(xp1y ? 0x20:0)
             |(xm1yp1 ? 0x40:0)
             |(xyp1 ? 0x80:0)
             |(xp1yp1 ? 0x100:0);
     }
 
     public static boolean shouldErode(int code)
     {
         boolean[] b = new boolean[] {
             (code & 1) != 0,
             (code & 2) != 0,
             (code & 4) != 0,
             (code & 0x20) != 0,
             (code & 0x100) != 0,
             (code & 0x80) != 0,
             (code & 0x40) != 0,
             (code & 8) != 0,
 
         };
 
        if ((code&0x2f) == 0x2f)
            return false; // special case?

         int changes=0;
         int count=0;
 
         for (int i=0; i<b.length; i++) {
             if (i+1<b.length && b[i] != b[i+1])
                 changes++;
 
             if (b[i])
                 count++;
         }
 
         return changes<3 && count<6;
     }
 
     public static class Matrix
     {
         public final int w , h;
         public final byte[] raster;
 
         public Matrix(int w, int h)
         {
             this.w = w;
             this.h = h;
             raster = new byte[w*h];
         }
 
         public Matrix(Matrix m)
         {
             this.w = m.w;
             this.h =m.h;
             raster = new byte[m.raster.length];
             System.arraycopy(m.raster, 0, raster, 0, raster.length);
         }
 
         public byte get(int x, int y)
         {
             return raster[x + w*y];
         }
 
         public byte get(int x, int y, int defaultValue)
         {
             if (x<0 || x>=w
                 || y<0 || y>=h)
                 return (byte) defaultValue;
             return get(x,y);
         }
 
         public void set(int x, int y, int val)
         {
             raster[x+w*y] = (byte) val;
         }
     }
 
     public static class VisualizeErosionRules
     {
         public static void main(String[] argv)
         {
             JFrame fr = new JFrame("erosion rules");
 
 
             int cellSize = 5;
             int w=16* cellSize, h=16* cellSize;
 
             int[] raster = visualizationRaster(cellSize, w, h);
             ImageProducer ip = new MemoryImageSource(w,h, raster, 0, w);
 
             fr.getContentPane().add(new ImageWidget(GaussLumps.toIcon(ip)));
 
             fr.pack();
             fr.setSize(new Dimension(800, 800));
             fr.setVisible(true);
         }
 
         public static int[] visualizationRaster(int cellSize, int w, int h)
         {
             int[] raster = new int[w*h];
 
             int BLACK = 0xff000000;
             int RED = 0xffff0000;
 
             for (int u=0; u<16; u++) {
                 for (int v=0; v<16; v++) {
                     boolean b = shouldErode(u | (v<< 5));
 
                     int x0 = u* cellSize;
                     int y0 = v*cellSize;
 
 
                     for (int x1=0; x1<cellSize; x1++) {
                         for (int y1=0; y1<cellSize; y1++) {
                             int x = x0+x1;
                             int y = y0+y1;
 
                             raster[x+w*y] = 0xffffffff;
                         }
                     }
 
                     int p = x0+w*y0;
                     if ((u&1)!=0) {
                         raster[p+1+w] = BLACK;
                     }
                     if ((u&2)!=0) {
                         raster[p+2+w] = BLACK;
                     }
                     if ((u&4)!=0) {
                         raster[p+3+w] = BLACK;
                     }
                     if ((u&8)!=0) {
                         raster[p+1+2*w] = BLACK;
                     }
 
                     raster[p+2+2*w] = b ? RED:BLACK;
 
                     if ((v&1)!=0) {
                         raster[p+3+2*w] = BLACK;
                     }
                     if ((v&2)!=0) {
                         raster[p+1+3*w] = BLACK;
                     }
                     if ((v&4)!=0) {
                         raster[p+2+3*w] = BLACK;
                     }
                     if ((v&8)!=0) {
                         raster[p+3+3*w] = BLACK;
                     }
                 }
             }
             return raster;
         }
     }
 
 }
