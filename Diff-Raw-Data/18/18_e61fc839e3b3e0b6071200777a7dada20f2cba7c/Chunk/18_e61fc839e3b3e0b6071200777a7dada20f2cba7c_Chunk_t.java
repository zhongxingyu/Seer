 package platformer;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 public class Chunk {
     public int x, y;
     public World world;
     public Tile[][] tiles;
     public byte[][] tileData;
     public int textureID;
     public boolean needsupdate;
     
     public Chunk (int x, int y, World world)
     {
         needsupdate = false;
         textureID = -1;
         tileData = new byte[world.chunkSize][world.chunkSize];
         this.x = x;
         this.y = y;
         this.world = world;
     }
     public void generate ()
     {
         tiles = world.worldgenerator.generateTiles(x, y, world.chunkSize);
         chunkUpdate();
     }
     public boolean isVisible ()
     {
         return
               (((x + 1) * world.chunkSize * 16) > (world.thePlayer.x - Utilities.getWidth()  / 2))
            && ((x       * world.chunkSize * 16) < (world.thePlayer.x + Utilities.getWidth()  / 2))
            && (((y + 1) * world.chunkSize * 16) > (world.thePlayer.y - Utilities.getHeight() / 2))
            && ((y       * world.chunkSize * 16) < (world.thePlayer.y + Utilities.getHeight() / 2));
     }
     public void chunkUpdate ()
     {
         BufferedImage chunk = new BufferedImage(16 * world.chunkSize, 16 * world.chunkSize, BufferedImage.TYPE_INT_ARGB);
         Graphics2D graphics = chunk.createGraphics();
         for (int x = 0; x < tiles.length; x++) {
             for (int y = 0; y < tiles.length; y++) {
                int txtr = tiles[x][y].getTileTexture(world,
                                                     (this.x * world.chunkSize) + x, 
                                                     (this.y * world.chunkSize) + y);
                 if (txtr < 0) continue;
                 
                 int x0, x1, y0, y1;
                 x0 = y0 = 0;
                 x1 = y1 = 16;
                 if (txtr != 0) { //Prevent devision by zero
                     x0 = txtr % (Sheetholder.tiles.getWidth() / 16) * 16;
                     y0 = (int) Math.floor(txtr / (Sheetholder.tiles.getHeight() / 16));
                     x1 = x0 + 16;
                     y1 = y0 + 16;
                 }
                 graphics.drawImage(Sheetholder.tiles,
                                     (x + 1) * 16, (y + 1) * 16,
                                     x * 16, y * 16, 
                                     x1, y0,
                                     x0, y1, 
                                     null);
             }
         }
         graphics.dispose();
         graphics = null;
         if (textureID != -1) glDeleteTextures(textureID);
         textureID = Utilities.loadTexture(chunk);
     }
     public void render ()
     {
         if (textureID == -1 || !isVisible()) return;
 
         glEnable(GL_TEXTURE_2D);
         glEnable(GL_BLEND);
         glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
             
         glPushMatrix();
         glBindTexture(GL_TEXTURE_2D, textureID);
         glColor3d(1.0, 1.0, 1.0);
         glBegin(GL_QUADS);
             glTexCoord2f(0, 0);
             glVertex2f(x * world.chunkSize * 16, y * world.chunkSize * 16);
                
             glTexCoord2f(1, 0);
             glVertex2f((x + 1) * world.chunkSize * 16, y * world.chunkSize * 16);
                
             glTexCoord2f(1, 1);
             glVertex2f((x + 1) * world.chunkSize * 16, (y + 1) * world.chunkSize * 16);
                
             glTexCoord2f(0, 1);
             glVertex2f(x * world.chunkSize * 16, (y + 1) * world.chunkSize * 16);
         glEnd();
         
         glPopMatrix();
         glDisable(GL_BLEND);
         glDisable(GL_TEXTURE_2D);
     }
     public Tile getTileAbsolute(int x, int y)
     {
         int ax = (x - 1) % tiles.length;
         int ay = (y - 1) % tiles.length;
         ax = x - this.x * tiles.length - 1;
         ay = y - this.y * tiles.length - 1;
         return tiles[ax][ay];
     }
     public Tile setTileAbsolute(int x, int y, Tile tile)
     {
         int ax = (x - 1) % tiles.length;
         int ay = (y - 1) % tiles.length;
         ax = x - this.x * tiles.length - 1;
         ay = y - this.y * tiles.length - 1;
         Tile wasTile = tiles[ax][ay];
         tiles[ax][ay] = tile;
         if (wasTile.id != tile.id) needsupdate = true;
         return wasTile;
     }
     public byte getTileDataAbsolute (int x, int y)
     {
         int ax = (x - 1) % tiles.length;
         int ay = (y - 1) % tiles.length;
         ax = x - this.x * tiles.length - 1;
         ay = y - this.y * tiles.length - 1;
         return tileData[ax][ay];
     }
     public void setTileDataAbsolute (int x, int y, byte data)
     {
         int ax = (x - 1) % tiles.length;
         int ay = (y - 1) % tiles.length;
         ax = x - this.x * tiles.length - 1;
         ay = y - this.y * tiles.length - 1;
         tileData[ax][ay] = data;
     }
     public Tile getTileRelative(int x, int y)
     {
         return tiles[x][y];
     }
 }
