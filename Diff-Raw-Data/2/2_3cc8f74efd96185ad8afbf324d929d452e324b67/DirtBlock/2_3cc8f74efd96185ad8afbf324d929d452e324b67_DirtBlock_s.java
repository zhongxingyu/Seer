 package kniemkiewicz.jqblocks.ingame.object;
 
 import com.sun.xml.internal.bind.v2.TODO;
 import kniemkiewicz.jqblocks.ingame.Backgrounds;
 import kniemkiewicz.jqblocks.ingame.PointOfView;
 import kniemkiewicz.jqblocks.ingame.Sizes;
 import kniemkiewicz.jqblocks.ingame.SolidBlocks;
 import kniemkiewicz.jqblocks.ingame.object.background.NaturalDirtBackground;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  * User: krzysiek
  * Date: 08.07.12
  */
 public class DirtBlock extends AbstractBlock {
 
   public static Color BROWN = new Color(150.0f/255, 75.0f/255, 0);
   public static Color DARK_GREEN = new Color(0, 0.75f, 0);
 
   public static Log logger = LogFactory.getLog(DirtBlock.class);
 
   public DirtBlock(float x, float y, float width, float height) {
     super(x, y, width, height);
   }
 
   public DirtBlock(int x, int y, int width, int height) {
     super(x, y, width, height);
   }
 
   @Override
   protected AbstractBlock getSubBlock(AbstractBlock parent, int x, int y, int width, int height) {
     return new DirtBlock(x, y, width, height);
   }
 
   @Override
   public void renderObject(Graphics g, PointOfView pov) {
     renderDirt(g, x, y, height, width);
     renderGrass(g, pov, this);
   }
 
   @Override
   public Layer getLayer() {
     return Layer.WALL;
   }
 
   public static void renderDirt(Graphics g, int x, int y, int height, int width) {
     g.setColor(BROWN);
     g.fillRect(x, y, width, height);
     //logger.debug("block fillRect [x="+x+", y="+y+", width="+width+", height="+height+"]");
   }
 
   public static void renderGrass(Graphics g, PointOfView pov, DirtBlock block) {
    Rectangle window = new Rectangle(pov.getShiftX(), pov.getShiftY(), Sizes.WINDOW_WIDTH, Sizes.WINDOW_HEIGHT);
 
     g.setColor(DARK_GREEN);
     // TODO czy potrzebny Sizes.round?
     
     int fromX = Sizes.roundToBlockSizeX(block.getShape().getMinX());
     if (fromX < window.getMinX()) {
       fromX = Sizes.roundToBlockSizeX(window.getMinX());
     }
 
     int toX = Sizes.roundToBlockSizeX(block.getShape().getMaxX());
     if (toX > window.getMaxX()) {
       toX = Sizes.roundToBlockSizeX(window.getMaxX());
     }
     
     int fromY = Sizes.roundToBlockSizeX(block.getShape().getMinY());
     if (fromY < window.getMinY()) {
       fromY = Sizes.roundToBlockSizeX(window.getMinY());
     }
 
     int toY = Sizes.roundToBlockSizeX(block.getShape().getMaxY());
     if (toY > window.getMaxY()) {
       toY = Sizes.roundToBlockSizeX(window.getMaxY());
     }
 
     renderLeftGrass(g, block, fromY, toY);
     renderTopGrass(g, block, fromX, toX);
     renderRightGrass(g, block, fromY, toY);
     renderBottomGrass(g, block, fromX, toX);
   }
 
   private static void renderLeftGrass(Graphics g, DirtBlock block, int fromY, int toY) {
     for (int y = fromY; y < toY; y += Sizes.BLOCK) {
       boolean drawGrass = true;
       for (AbstractBlock neighbor : block.getLeftNeighbors()) {
         int neighborMinY = Sizes.roundToBlockSizeY(neighbor.getShape().getMinY());
         int neighborMaxY = Sizes.roundToBlockSizeY(neighbor.getShape().getMaxY());
         if (neighborMinY <= y && neighborMaxY > y) {
           drawGrass = false;
         }
       }
       if (drawGrass) {
         g.fillRect(block.x, y, 2, Sizes.BLOCK);
         //logger.debug("grass left fillRect [x="+x+", y="+block.y+", width="+(x+Sizes.BLOCK)+", height="+2+"]");
       }
     }
   }
 
   private static void renderTopGrass(Graphics g, DirtBlock block, int fromX, int toX) {
     for (int x = fromX; x < toX; x += Sizes.BLOCK) {
       boolean drawGrass = true;
       for (AbstractBlock neighbor : block.getTopNeighbors()) {
         int neighborMinX = Sizes.roundToBlockSizeX(neighbor.getShape().getMinX());
         int neighborMaxX = Sizes.roundToBlockSizeX(neighbor.getShape().getMaxX());
         if (neighborMinX <= x && neighborMaxX > x) {
           drawGrass = false;
         }
       }
       if (drawGrass) {
         g.fillRect(x, block.y, Sizes.BLOCK, 2);
         //logger.debug("grass top fillRect [x="+x+", y="+block.y+", width="+(x+Sizes.BLOCK)+", height="+2+"]");
       }
     }
   }
   
   private static void renderRightGrass(Graphics g, DirtBlock block, int fromY, int toY) {
     for (int y = fromY; y < toY; y += Sizes.BLOCK) {
       boolean drawGrass = true;
       for (AbstractBlock neighbor : block.getRightNeighbors()) {
         int neighborMinY = Sizes.roundToBlockSizeY(neighbor.getShape().getMinY());
         int neighborMaxY = Sizes.roundToBlockSizeY(neighbor.getShape().getMaxY());
         if (neighborMinY <= y && neighborMaxY > y) {
           drawGrass = false;
         }
       }
       if (drawGrass) {
         g.fillRect(block.x + block.width, y, 2, Sizes.BLOCK);
         //logger.debug("grass right fillRect [x="+x+", y="+block.y+", width="+(x+Sizes.BLOCK)+", height="+2+"]");
       }
     }
   }   
   
   private static void renderBottomGrass(Graphics g, DirtBlock block, int fromX, int toX) {
     for (int x = fromX; x < toX; x += Sizes.BLOCK) {
       boolean drawGrass = true;
       for (AbstractBlock neighbor : block.getBottomNeighbors()) {
         int neighborMinX = Sizes.roundToBlockSizeX(neighbor.getShape().getMinX());
         int neighborMaxX = Sizes.roundToBlockSizeX(neighbor.getShape().getMaxX());
         if (neighborMinX <= x && neighborMaxX > x) {
           drawGrass = false;
         }
       }
       if (drawGrass) {
         g.fillRect(x, block.y + block.height, Sizes.BLOCK, 2);
         //logger.debug("grass bottom fillRect [x="+x+", y="+block.y+", width="+(x+Sizes.BLOCK)+", height="+2+"]");
       }
     }
   }
 
   public void removeRect(Rectangle rect, SolidBlocks blocks, Backgrounds backgrounds) {
     super.removeRect(rect, blocks, backgrounds);
     backgrounds.add(new NaturalDirtBackground(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));
   }
 }
