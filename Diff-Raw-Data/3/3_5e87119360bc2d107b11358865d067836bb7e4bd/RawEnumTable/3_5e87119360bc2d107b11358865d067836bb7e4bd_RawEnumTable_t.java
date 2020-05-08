 package kniemkiewicz.jqblocks.ingame.block;
 
 import kniemkiewicz.jqblocks.ingame.PointOfView;
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import kniemkiewicz.jqblocks.ingame.object.ObjectRenderer;
 import kniemkiewicz.jqblocks.ingame.object.RenderableObject;
 import kniemkiewicz.jqblocks.util.Assert;
 import kniemkiewicz.jqblocks.util.BeanName;
 import kniemkiewicz.jqblocks.util.GeometryUtils;
 import kniemkiewicz.jqblocks.util.SpringBeanProvider;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Shape;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.List;
 
 /**
  * User: krzysiek
  * Date: 06.08.12
  */
 public class RawEnumTable<T extends Enum<T> & RenderableBlockType> implements Serializable, RenderableObject<RawEnumTable<T>> {
 
   final Object[][] data;
   final T emptyType;
   final T outsideTable;
   transient EnumMap<T, RenderableBlockType.Renderer> rendererCache;
 
   public RawEnumTable(T emptyType, T outsideTable) {
     int width = Sizes.LEVEL_SIZE_X / Sizes.BLOCK;
     int height = Sizes.LEVEL_SIZE_Y / Sizes.BLOCK;
     data = new Object[width][height];
     for (int i = 0; i < width; i++) {
       for (int j = 0; j < height; j++) {
         data[i][j] = emptyType;
       }
     }
     this.emptyType = emptyType;
     this.outsideTable = outsideTable;
   }
 
   final public T get(int x, int y) {
     if (x < 0) return outsideTable;
     if (y < 0) return outsideTable;
     if (x >= data.length) return outsideTable;
     if (y >= data[0].length) return outsideTable;
     return (T) data[x][y];
   }
 
   final public void set(int x, int y, T type) {
     data[x][y] = type;
   }
 
   final public boolean safeSet(int x, int y, T type) {
     if (x < 0) return false;
     if (y < 0) return false;
     if (x >= data.length) return false;
     if (y >= data[0].length) return false;
     data[x][y] = type;
     return true;
   }
 
   @Override
   public BeanName<? extends ObjectRenderer> getRenderer() {
     return null;
   }
 
   public void fillRendererCache(SpringBeanProvider springBeanProvider) {
     // emptyType.getClass() is NOT enum type.
     rendererCache = new EnumMap<T, RenderableBlockType.Renderer>((Class<T>) emptyType.getDeclaringClass());
     for (T key : ((Class<T>)emptyType.getDeclaringClass()).getEnumConstants()) {
       if (key.getRenderer() != null) {
         rendererCache.put(key, springBeanProvider.getBean(key.getRenderer(), true));
       } else {
         rendererCache.put(key, null);
       }
     }
   }
 
   @Override
   public void renderObject(Graphics g, PointOfView pov) {
     // Someone forgot to call fillRendererCache?
     assert rendererCache != null;
     int x0 = (pov.getShiftX() - Sizes.MIN_X) / Sizes.BLOCK;
     int y0 = (pov.getShiftY() - Sizes.MIN_Y) / Sizes.BLOCK;
     int width = pov.getWindowWidth() / Sizes.BLOCK + 2;
     int height = pov.getWindowHeight() / Sizes.BLOCK + 3;
     if (x0 < 0) {
       x0 = 0;
     }
     if (y0 < 0) {
       y0 = 0;
     }
     if (x0 + width > data.length) {
       width = data.length - x0;
     }
     if (y0 + height > data[0].length) {
       height = data[0].length - y0;
     }
     renderBlocks(g, x0, y0, width, height);
     renderBorders(g, x0, y0, width, height);
   }
 
   private void renderBlocks(Graphics g, int x0, int y0, int width, int height) {
     for (int x = x0; x < x0 + width; x++) {
       int currentFirstY = y0;
       T currentType = emptyType;
       int xx = x * Sizes.BLOCK + Sizes.MIN_X;
       for (int y = y0; y < y0 + height; y++) {
         if (currentType != data[x][y]) {
           int yy1 = currentFirstY * Sizes.BLOCK + Sizes.MIN_Y;
           int height2 = (y - currentFirstY) * Sizes.BLOCK;
           RenderableBlockType.Renderer r1 = rendererCache.get(currentType);
           if (r1 != null) {
             r1.renderBlock(xx, yy1, Sizes.BLOCK, height2, g);
           }
           currentType = (T)data[x][y];
           currentFirstY = y;
         }
       }
       int y = y0 + height;
       int yy1 = currentFirstY * Sizes.BLOCK + Sizes.MIN_Y;
       int height2 = (y - currentFirstY - 1) * Sizes.BLOCK;
       RenderableBlockType.Renderer r = rendererCache.get(currentType);
       if (r != null) {
         r.renderBlock(xx, yy1, Sizes.BLOCK, height2, g);
       }
     }
   }
 
   private void renderBorders(Graphics g, int x0, int y0, int width, int height) {
     for (int x = x0; x < x0 + width; x++) {
       int currentFirstY = y0;
       T currentType = emptyType;
       int xx = x * Sizes.BLOCK + Sizes.MIN_X;
       for (int y = y0; y < y0 + height; y++) {
         if (currentType != data[x][y]) {
           int yy1 = currentFirstY * Sizes.BLOCK + Sizes.MIN_Y;
           int height2 = (y - currentFirstY) * Sizes.BLOCK;
           RenderableBlockType.Renderer r1 = rendererCache.get(currentType);
           if (r1 != null) {
             r1.renderBorder(xx, yy1 + height2, Sizes.BLOCK, RenderableBlockType.Border.BOTTOM, (T)data[x][y], g);
           }
           RenderableBlockType.Renderer r2 = rendererCache.get((T)data[x][y]);
           if (r2 != null) {
             r2.renderBorder(xx, yy1 + height2, Sizes.BLOCK, RenderableBlockType.Border.TOP, currentType, g);
           }
           currentType = (T)data[x][y];
           currentFirstY = y;
         }
       }
     }
     for (int x = x0 + 1; x < x0 + width; x++) {
       int currentFirstY = y0;
       T currentType = emptyType;
       T currentTypeLeft = emptyType;
       int xx = x * Sizes.BLOCK + Sizes.MIN_X;
       for (int y = y0; y < y0 + height; y++) {
         if ((currentType != data[x][y])||(currentTypeLeft != data[x - 1][y])) {
           int yy1 = currentFirstY * Sizes.BLOCK + Sizes.MIN_Y;
           int height2 = (y - currentFirstY) * Sizes.BLOCK;
           RenderableBlockType.Renderer r1 = rendererCache.get(currentType);
           if (r1 != null) {
             r1.renderBorder(xx, yy1, height2, RenderableBlockType.Border.LEFT, currentTypeLeft, g);
           }
           RenderableBlockType.Renderer r2 = rendererCache.get(currentTypeLeft);
           if (r2 != null) {
             r2.renderBorder(xx, yy1, height2, RenderableBlockType.Border.RIGHT, currentType, g);
           }
           currentType = (T)data[x][y];
           currentFirstY = y;
           currentTypeLeft = (T)data[x - 1][y];
         }
       }
       int y = y0 + height;
       int yy1 = currentFirstY * Sizes.BLOCK + Sizes.MIN_Y;
       int height2 = (y - currentFirstY - 1) * Sizes.BLOCK;
       RenderableBlockType.Renderer r1 = rendererCache.get(currentType);
       if (r1 != null) {
         r1.renderBorder(xx, yy1, height2, RenderableBlockType.Border.LEFT, currentTypeLeft, g);
       }
       RenderableBlockType.Renderer r2 = rendererCache.get(currentTypeLeft);
       if (r2 != null) {
         r2.renderBorder(xx, yy1, height2, RenderableBlockType.Border.RIGHT, currentType, g);
       }
     }
   }
 
   @Override
   public Layer getLayer() {
     return emptyType.getLayer();
   }
 
   @Override
   public Shape getShape() {
     return new Rectangle(Sizes.MIN_X, Sizes.MIN_Y, Sizes.LEVEL_SIZE_X, Sizes.LEVEL_SIZE_Y);
   }
 
   public void setRectUnscaled(Rectangle shape, T type) {
     final int x0 = Math.round((shape.getX() - Sizes.MIN_X) / Sizes.BLOCK);
     final int y0 = Math.round((shape.getY() - Sizes.MIN_Y) / Sizes.BLOCK);
     final int width = Math.round(shape.getWidth() / Sizes.BLOCK);
     final int height = Math.round(shape.getHeight() / Sizes.BLOCK);
     for (int x = Math.max(x0, 0); x < Math.min(x0 + width, this.data.length); x++) {
       for (int y = Math.max(y0,0); y < Math.min(y0 + height, this.data[0].length); y++) {
         data[x][y] = type;
       }
     }
   }
 
   final public int toXIndex(int unscaledX) {
     return (unscaledX - Sizes.MIN_X) / Sizes.BLOCK;
   }
 
   final public int toYIndex(int unscaledY) {
     return (unscaledY - Sizes.MIN_Y) / Sizes.BLOCK;
   }
 
   public T getValueForUnscaledPoint(int x, int y) {
     return (T) data[toXIndex(x)][toYIndex(y)];
   }
 
   // For given shape which does not collide with any blocks, find height at which it would stop by free falling down.
   // Gives bottom border. Takes into account only blocks as obstacles.
   public int getUnscaledDropHeight(Shape unscaledShape) {
     Rectangle rectangle = GeometryUtils.getBoundingRectangle(unscaledShape);
     int x1 = toXIndex((int)rectangle.getX() + 1);
     int x2 = toXIndex((int)GeometryUtils.getMaxX(rectangle)) + 1;
     int y1 = toYIndex((int)GeometryUtils.getMaxY(rectangle) - 1);
     boolean emptyRow = true;
     int y;
     for (y = y1; y < data[0].length;y++) {
       for (int i = x1; i < x2; i++) {
         if (data[i][y] != emptyType) {
           emptyRow = false;
           break;
         }
       }
       if (!emptyRow) break;
     }
     return Sizes.MIN_Y + y * Sizes.BLOCK - 1;
   }
 
   public boolean collidesWithNonEmpty(Shape shape) {
     Rectangle rect = GeometryUtils.getBoundingRectangle(shape);
     return collidesWithNonEmpty(rect);
   }
 
   public boolean collidesWithNonEmpty(Rectangle unscaledRect) {
     int x1 = toXIndex((int)Math.ceil(unscaledRect.getX()));
     int x2 = toXIndex((int)Math.floor(GeometryUtils.getMaxX(unscaledRect)));
     int y1 = toYIndex((int)Math.ceil(unscaledRect.getY()));
     int y2 = toYIndex((int)Math.floor(GeometryUtils.getMaxY(unscaledRect)));
     if ((x1 < 0) || (x2 >= data.length) || (y1 < 0) || (y2 >= data[0].length)) {
       return true;
     }
     for (int i = x1; i <= x2; i++) {
       for (int j = y1; j <= y2; j++) {
         if (data[i][j] != emptyType) {
           return true;
         }
       }
     }
     return false;
   }
 
   // Rectangles returned by this method are meant to be used with HitResolver, they are not smallest possible ones,
   // some points may be in more than one and so on.
   public List<Rectangle> getIntersectingRectangles(Rectangle unscaledRect) {
     int x1 = toXIndex((int)Math.ceil(unscaledRect.getMinX()));
     int x2 = toXIndex((int)Math.floor(GeometryUtils.getMaxX(unscaledRect)));
     int y1 = toYIndex((int)Math.ceil(unscaledRect.getMinY()));
     int y2 = toYIndex((int)Math.floor(GeometryUtils.getMaxY(unscaledRect)));
     List<Rectangle> rectangles = new ArrayList<Rectangle>();
     if (x1 < 0) {
       rectangles.add(new Rectangle(Sizes.MIN_X - 1000, Sizes.MIN_Y - 1000, 1000, Sizes.LEVEL_SIZE_Y + 2000));
       x1 = 0;
     }
     if (x2 >= data.length) {
       rectangles.add(new Rectangle(Sizes.MAX_X, Sizes.MIN_Y - 1000, 1000, Sizes.LEVEL_SIZE_Y + 2000));
       x2 = data.length - 1;
     }
     if (y1 < 0) {
       rectangles.add(new Rectangle(Sizes.MIN_X - 1000, Sizes.MIN_Y - 1000, Sizes.LEVEL_SIZE_X + 2000, 1000));
       y1 = 0;
     }
     if (y2 >= data[0].length) {
       rectangles.add(new Rectangle(Sizes.MIN_X - 1000, Sizes.MAX_Y, Sizes.LEVEL_SIZE_X + 2000, 1000));
       y2 = data[0].length - 1;
     }
    // java.lang.NegativeArraySizeException?
     boolean[][] nonEmpty = new boolean[x2 - x1 + 1][y2 - y1 + 1];
     boolean[][] used = new boolean[x2 - x1 + 1][y2 - y1 + 1];
     for (int x = x1; x <= x2; x++) {
       for (int y = y1; y <= y2; y++) {
         nonEmpty[x - x1][y - y1] = (data[x][y] != emptyType);
       }
     }

     for (int i = 0; i < nonEmpty.length; i++) {
       int firstNonEmpty = -1;
       int lastNonEmpty = -1;
       for (int j = 0; j < nonEmpty[i].length; j++) {
         if (nonEmpty[i][j]) {
           if (firstNonEmpty < 0) {
             firstNonEmpty = j;
           }
           lastNonEmpty = j;
         }
       }
       if (firstNonEmpty != lastNonEmpty) {
         rectangles.add(new Rectangle(Sizes.MIN_X + (x1 + i) * Sizes.BLOCK, Sizes.MIN_Y + (y1 + firstNonEmpty) * Sizes.BLOCK,
             Sizes.BLOCK, (lastNonEmpty - firstNonEmpty) * Sizes.BLOCK));
         for (int j = firstNonEmpty; j <= lastNonEmpty; j++) {
           used[i][j] = true;
         }
       }
     }
     for (int i = 0; i < nonEmpty[0].length; i++) {
       int firstNonEmpty = -1;
       int lastNonEmpty = -1;
       for (int j = 0; j < nonEmpty.length; j++) {
         if (nonEmpty[j][i]) {
           if (firstNonEmpty < 0) {
             firstNonEmpty = j;
           }
           lastNonEmpty = j;
         }
       }
       if (firstNonEmpty != lastNonEmpty) {
         rectangles.add(new Rectangle(Sizes.MIN_X + (x1 + firstNonEmpty) * Sizes.BLOCK, Sizes.MIN_Y + (y1 + i) * Sizes.BLOCK,
             (lastNonEmpty - firstNonEmpty) * Sizes.BLOCK, Sizes.BLOCK));
         for (int j = firstNonEmpty; j <= lastNonEmpty; j++) {
           used[j][i] = true;
         }
       }
     }
     // Now we add remaining small ones.
     for (int x = x1; x <= x2; x++) {
       for (int y = y1; y <= y2; y++) {
         if (nonEmpty[x - x1][y - y1] && ! used[x - x1][y - y1]) {
           rectangles.add(new Rectangle(Sizes.MIN_X + x * Sizes.BLOCK, Sizes.MIN_Y + y * Sizes.BLOCK, Sizes.BLOCK, Sizes.BLOCK));
         }
       }
     }
     if (Assert.ASSERT_ENABLED) {
       for (Rectangle r : rectangles) {
         assert GeometryUtils.intersects(r, unscaledRect);
       }
     }
     return rectangles;
   }
 
   public int getHeight() {
     return data[0].length;
   }
 
   public int getWidth() {
     return data.length;
   }
 }
