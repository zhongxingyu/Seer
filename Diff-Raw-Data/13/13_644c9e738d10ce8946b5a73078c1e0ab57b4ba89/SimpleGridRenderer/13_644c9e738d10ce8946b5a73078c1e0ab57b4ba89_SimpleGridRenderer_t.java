 package tetris;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
import java.net.URL;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Jesse-SouljaBoy-FanB
  */
 public class SimpleGridRenderer implements GridRenderer {
 
     private int blockWidth = 10;
     private int blockHeight = 10;
 
     /* Store BufferedImage objects for different coloured blocks */
     private static BufferedImage[] blocks;
     /* Special BufferedImage for highlighted block. A highlighted block is
      * one which is flashing before being deleted as part of a filled row */
     private static BufferedImage highlight;
 
     public SimpleGridRenderer() {
         loadImages();
     }
 
     public BufferedImage renderGrid(Game game) {
         byte[][] grid = game.getCurrentGrid();
         int gridWidth = grid.length;
         int gridHeight = grid[0].length;
 
         int gridImgWidth = blockWidth * gridWidth;
         int gridImgHeight = blockHeight * gridHeight;
 
         BufferedImage gridImage = new BufferedImage(gridImgWidth, gridImgHeight, BufferedImage.TYPE_INT_RGB);
         Graphics2D g = gridImage.createGraphics();
 
         g.setColor(new Color(0xaaaaaa));
         g.setComposite(AlphaComposite.Src);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
         g.fillRect(0, 0, gridImgWidth, gridImgHeight);
         for (int i = 0; i <
                 gridWidth; i++) {
             for (int j = 0; j <
                     gridHeight; j++) {
                 if (grid[i][j] == 0) {
                     g.setColor(new Color(0xf3f3f3));
                     g.fillRect(i * blockWidth + 2, j * blockHeight + 2, blockWidth - 2, blockHeight - 2);
                 } else if (grid[i][j] < 0) {
                     g.drawImage(highlight, i * blockWidth, j * blockHeight, blockWidth, blockHeight, null);
                 } else {
                     g.drawImage(blocks[grid[i][j] - 1], i * blockWidth, j * blockHeight, blockWidth, blockHeight, null);
                 }
 
             }
         }
         return gridImage;
     }
 
     public void setBlockWidth(int width) {
         this.blockWidth = width;
     }
 
     public void setBlockHeight(int height) {
         this.blockHeight = height;
     }
 
     private void loadImages() {
         blocks = new BufferedImage[7];
         try {
            for (int i = 0; i < 7; i++) {
                URL imageURL = this.getClass().getClassLoader().getResource("img/block-" + i + ".gif");
                blocks[i] = ImageIO.read(imageURL);
             }
            URL highlightURL = this.getClass().getClassLoader().getResource("img/block-highlight.gif");
            highlight = ImageIO.read(highlightURL);
         } catch (Exception e) {
             //...
         }
     }
 }
