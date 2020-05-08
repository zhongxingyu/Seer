 package com.bitspatter;
 
 import java.util.Random;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.bitspatter.renderers.BlockRenderer;
 
 public class Piece implements Cloneable {
     public static Piece[] pieces;
 
     public BlockRenderer renderer;
     public Color color;
     public boolean[][] blocks;
     public int topLeftX, topLeftY;
 
     // True if this piece has been mutated so far.
     public boolean warped = false;
 
     final Color outlineColor = Color.white;
 
     public Piece(BlockRenderer renderer, Color color, boolean[][] blocks) throws SlickException {
         this.renderer = renderer;
         this.color = color;
         this.blocks = blocks;
         this.topLeftX = this.topLeftY = 0;
     }
 
     boolean shouldRender(int x, int y) {
         if (dragging && (x == draggingX && y == draggingY)) {
             return false;
         }
 
        return blocks[y][x];
     }
 
     public void render(Graphics g, boolean renderOutlines) {
        for (int y = 0; y < blocks.length; ++y) {
            for (int x = 0; x < blocks[y].length; ++x) {
                 if (shouldRender(x, y)) {
                     int blockX = topLeftX + x;
                     int blockY = topLeftY + y;
                     renderer.render(g, blockX, blockY, color);
                     if (renderOutlines) {
                         renderer.renderOutline(g, blockX, blockY, outlineColor);
                     }
                 }
             }
         }
     }
 
     public void renderDraggable(Graphics g, int x, int y) {
         if (dragging) {
             renderer.renderAtPixel(g, x, y, color);
             renderer.renderOutlineAtPixel(g, x, y, outlineColor);
         }
     }
 
     public static void createPieces(BlockRenderer renderer) throws SlickException {
         pieces = new Piece[] {
                         // I piece
                         new Piece(renderer, Color.cyan, new boolean[][] { { true, true, true, true } }),
                         // O piece
                         new Piece(renderer, Color.yellow, new boolean[][] { { true, true }, { true, true } }),
                         // T piece
                         new Piece(renderer, Color.decode("#9900CC"), new boolean[][] { { false, true, false },
                                         { true, true, true } }),
                         // S piece
                         new Piece(renderer, Color.green,
                                         new boolean[][] { { false, true, true }, { true, true, false } }),
                         // Z piece
                         new Piece(renderer, Color.red, new boolean[][] { { true, true, false }, { false, true, true } }),
                         // L piece
                         new Piece(renderer, Color.orange, new boolean[][] { { true, false }, { true, false },
                                         { true, true } }),
                         // J piece
                         new Piece(renderer, Color.blue,
                                         new boolean[][] { { true, false, false }, { true, true, true } }) };
     }
 
     static Random random = new Random();
 
     public static Piece getRandomPiece() {
         if (pieces == null) {
             return null;
         }
 
         Piece piece = pieces[random.nextInt(pieces.length)];
         try {
             return (Piece) piece.clone();
         } catch (CloneNotSupportedException cnse) {
             return null;
         }
     }
 
     public Piece rotated(boolean clockwise) throws SlickException {
         boolean[][] newBlocks = new boolean[blocks[0].length][blocks.length];
         for (int y = 0; y < blocks.length; ++y) {
             for (int x = 0; x < blocks[0].length; ++x) {
                 if (clockwise) {
                     newBlocks[x][y] = blocks[blocks.length - 1 - y][x];
                 } else {
                     newBlocks[x][y] = blocks[y][blocks[0].length - 1 - x];
                 }
             }
         }
 
         Piece newPiece = new Piece(renderer, color, newBlocks);
         newPiece.topLeftX = topLeftX;
         newPiece.topLeftY = topLeftY;
         return newPiece;
     }
 
     public int getMaxX() {
         return getWidth() + topLeftX;
     }
 
     public int getWidth() {
         return blocks[0].length;
     }
 
     public int getMaxY() {
         return getHeight() + topLeftY;
     }
 
     public int getHeight() {
         return blocks.length;
     }
 
     int draggingX, draggingY;
     boolean dragging = false;
 
     public void startDrag(int blockX, int blockY) {
         if (!contains(blockX, blockY)) {
             return;
         }
 
         int localX = blockX - topLeftX;
         int localY = blockY - topLeftY;
         if (!blocks[localY][localX]) {
             return;
         }
 
         draggingX = localX;
         draggingY = localY;
         dragging = true;
     }
 
     public void stopDrag() {
         dragging = false;
     }
 
     boolean[][] growBlocks(int offsetX, int offsetY, int growX, int growY) {
         if (offsetX > growX || offsetY > growY) {
             return null;
         }
 
         boolean[][] newBlocks = new boolean[getHeight() + growY][getWidth() + growX];
         for (int y = 0; y < getHeight(); ++y) {
             System.arraycopy(blocks[y], 0, newBlocks[y + offsetY], offsetX, getWidth());
         }
 
         return newBlocks;
     }
 
     public boolean stopDrag(int blockX, int blockY) {
         if (!dragging) {
             return false;
         }
 
         stopDrag();
 
         int localX = blockX - topLeftX;
         int localY = blockY - topLeftY;
 
         // If it's inside the current piece, just check if it's occupied.
         if (contains(blockX, blockY)) {
             // Was it dropped on top of an occupied space.
             if (blocks[localY][localX]) {
                 return false;
             }
 
             blocks = growBlocks(0, 0, 0, 0);
             blocks[localY][localX] = true;
             blocks[draggingY][draggingX] = false;
             return true;
         }
 
         // It has to be within one block of a current block.
         if (!hasAnyNonDraggedNeighbor(localX, localY)) {
             return false;
         }
 
         int growX = 0, offsetX = 0;
         int growY = 0, offsetY = 0;
 
         if (localX < 0) {
             growX = 1;
             offsetX = 1;
         } else if (localX >= getWidth()) {
             growX = 1;
         }
 
         if (localY < 0) {
             growY = 1;
             offsetY = 1;
         } else if (localY >= getHeight()) {
             growY = 1;
         }
 
         blocks = growBlocks(offsetX, offsetY, growX, growY);
         blocks[draggingY + offsetY][draggingX + offsetX] = false;
         blocks[localY + offsetY][localX + offsetX] = true;
         topLeftX -= offsetX;
         topLeftY -= offsetY;
 
         return true;
     }
 
     public boolean hasAnyNonDraggedNeighbor(int localX, int localY) {
         for (int xDelta = -1; xDelta <= 1; ++xDelta) {
             for (int yDelta = -1; yDelta <= 1; ++yDelta) {
                 int neighborX = xDelta + localX;
                 int neighborY = yDelta + localY;
                 if (neighborY == draggingY && neighborX == draggingX)
                     continue;
 
                 if (containsLocal(neighborX, neighborY) && blocks[neighborY][neighborX]) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public boolean containsLocal(int localX, int localY) {
         if (localX < 0 || localY < 0) {
             return false;
         }
 
         if (localX >= getWidth() || localY >= getHeight()) {
             return false;
         }
 
         return true;
     }
 
     public boolean contains(int blockX, int blockY) {
         if (blockX < topLeftX || blockY < topLeftY) {
             return false;
         }
 
         if (blockX >= getMaxX() || blockY >= getMaxY()) {
             return false;
         }
 
         return true;
     }
 }
