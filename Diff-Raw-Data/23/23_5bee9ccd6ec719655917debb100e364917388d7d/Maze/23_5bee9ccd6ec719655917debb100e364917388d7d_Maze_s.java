 /*
  * Lostmazed
  *
  * Copyright 2011 Matúš Sulír.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package lostmazed.game;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import javax.imageio.ImageIO;
 import soga2d.GraphicBoard;
 import soga2d.GraphicObject;
 import soga2d.ProximityDetector;
 import soga2d.events.ProximityListener;
 import soga2d.objects.Picture;
 import soga2d.objects.Rectangle;
 import soga2d.objects.Texture;
 
 /**
  * The maze background and "walls".
  * @author Matúš Sulír
  */
 public class Maze {
     /**
      * The size of the square representing the ending point.
      */
     public static final int END_SIZE = 20;
     
     /**
      * When the distance from the center of the maze-end square to the center
      * of the player becomes lower than this, the game is considered finished.
      */
     public static final int END_DISTANCE = 15;
     
    private static final int BLACK_TILE_SIZE = 40;
     private static final int UNCOVER_DISTANCE = 50;
     private static final int FORMAT_TAG = 0x70573A73;
     private static final int VERSION = 1;
     
     private GraphicObject background;
     private GraphicObject maze;
     private Point start;
     private Point end;
     
     /**
      * Constructs the maze.
      * @param background the background image
      * @param maze the maze "walls"
      * @param start the starting point
      * @param end the ending point
      */
     public Maze(GraphicObject background, GraphicObject maze, Point start, Point end) {
         this.background = background;
         this.maze = maze;
         this.start = start;
         this.end = end;
     }
 
     /**
      * Adds this maze to the specified board without starting the game.
      * @param board the board to use
      */
     public void addToBoard(GraphicBoard board) {
         board.addObjects(background, maze);
     }
     
     /**
      * Sets the maze background texture.
      * @param background the background object
      */
     public void setBackground(GraphicObject background) {
         this.background = background;
     }
     
     /**
      * Returns the maze image to allow collision detection.
      * @return the maze image
      */
     public GraphicObject getMaze() {
         return maze;
     }
     
     /**
      * Sets the maze image.
      * @param maze the maze image
      */
     public void setMaze(GraphicObject maze) {
         this.maze = maze;
     }
     
     /**
      * Returns the starting point.
      * @return the starting point
      */
     public Point getStart() {
         return start;
     }
     
     /**
      * Returns the ending point.
      * @return the ending point
      */
     public Point getEnd() {
         return end;
     }
     
     /**
      * Sets the starting and ending point.
      * @param start the starting point
      * @param end the ending point
      */
     public void setEndpoints(Point start, Point end) {
         this.start = start;
         this.end = end;
     }
     
     /**
      * Registers the player who will use this maze, places him to the start
      * and starts the game itself.
      * @param board the board to use
      * @param player the player
      * @param endAction the action to perform after successful finish (can be null)
      */
     public void play(GraphicBoard board, Player player, Runnable endAction) {
         board.lock();
         board.clear();
         
         addToBoard(board);
         player.getGraphics().bringToForeground();
         showBlackTiles(board, player);
         
         player.placeTo(start);
         registerEndAction(board, player, endAction);
         
         board.unlock();
     }
 
     /**
      * Saves this maze to a file.
      * @param file the output file
      * @throws IOException when the file could not be saved
      */
     public void save(File file) throws IOException {
         DataOutputStream output = null;
         
         try {
             output = new DataOutputStream(new FileOutputStream(file));
 
             output.writeInt(FORMAT_TAG);
             output.writeInt(VERSION);
 
             output.writeInt((int) start.getX());
             output.writeInt((int) start.getY());
             output.writeInt((int) end.getX());
             output.writeInt((int) end.getY());
 
             ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
             ImageIO.write(background.getImage(), "png", imageStream);
             byte[] backgroundBytes = imageStream.toByteArray();
 
             imageStream.reset();
             ImageIO.write(maze.getImage(), "png", imageStream);
             byte[] mazeBytes = imageStream.toByteArray();
 
             output.writeInt(backgroundBytes.length);
             output.writeInt(mazeBytes.length);
 
             output.write(backgroundBytes);
             output.write(mazeBytes);
         } catch (IOException ex) {
             throw new IOException("The maze could not be saved", ex);
         } finally {
             if (output != null)
                 output.close();
         }
     }
     
     /**
      * Loads a maze from a file.
      * @param file the input file
      * @return the loaded maze
      * @throws IOException when the maze could not be loaded
      */
     public static Maze load(File file) throws IOException {
         try {
             return load(new FileInputStream(file));
         } catch (IOException ex) {
             throw new IOException("The maze file could not be loaded", ex);
         }
     }
     
     /**
      * Loads a maze from an input stream.
      * @param file the input stream
      * @return the loaded maze
      * @throws IOException when the maze could not be loaded
      */
     public static Maze load(InputStream stream) throws IOException {
         DataInputStream input = null;
         
         try {
             input = new DataInputStream(stream);
 
             int formatTag = input.readInt();
             int version = input.readInt();
             
             if (formatTag != FORMAT_TAG || version != VERSION)
                throw new InvalidObjectException("The input file has an invalid format");
 
             Point start = new Point(input.readInt(), input.readInt());
             Point end = new Point(input.readInt(), input.readInt());

             byte[] backgroundBytes = new byte[input.readInt()];
             byte[] mazeBytes = new byte[input.readInt()];

            input.read(backgroundBytes, 0, backgroundBytes.length);
            input.read(mazeBytes, 0, mazeBytes.length);

             BufferedImage backgroundImage = ImageIO.read(new ByteArrayInputStream(backgroundBytes));
             BufferedImage mazeImage = ImageIO.read(new ByteArrayInputStream(mazeBytes));

             GraphicObject background = new Picture(backgroundImage);
             GraphicObject maze = new Texture(mazeImage, Game.WIDTH, Game.HEIGHT);
 
            input.close();

             return new Maze(background, maze, start, end);
         } catch (InvalidObjectException ex) {
             throw ex;
         } catch (IOException ex) {
             throw new IOException("The maze could not be loaded", ex);
         } finally {
             if (input != null)
                 input.close();
         }
     }
     
     /**
      * Displays black tiles (somtimes called "fog of war") to make the maze
      * harder to finish.
      */
     private void showBlackTiles(final GraphicBoard board, Player player) {
         for (int y = 0; y < Game.HEIGHT / BLACK_TILE_SIZE; y++) {
             for (int x = 0; x < Game.WIDTH / BLACK_TILE_SIZE; x++) {
                 final Rectangle tile = new Rectangle(BLACK_TILE_SIZE * x, BLACK_TILE_SIZE * y,
                         BLACK_TILE_SIZE, BLACK_TILE_SIZE, Color.BLACK, Color.BLACK);
                 
                 new ProximityDetector(player.getGraphics(), tile, UNCOVER_DISTANCE,
                         ProximityDetector.DistanceType.CENTER_TO_CENTER).setListener(new ProximityListener() {
                     @Override
                     public void onProximity() {
                         board.removeObject(tile);
                     }
                 });
                 
                 board.addObject(tile);
             }
         }
     }
     
     /**
      * Registers the action to perform when the maze is finisehd successfully.
      * @param board the board to use
      * @param player the player
      * @param endAction the action to perform
      */
     private void registerEndAction(GraphicBoard board, Player player, final Runnable endAction) {
         Color transparent = new Color(0, 0, 0, 0);
         Rectangle endRectangle = new Rectangle((int) end.getX(), (int) end.getY(), END_SIZE, END_SIZE,
                 transparent, transparent);
         
         new ProximityDetector(player.getGraphics(), endRectangle, END_DISTANCE,
                 ProximityDetector.DistanceType.CENTER_TO_CENTER).setListener(new ProximityListener() {
             @Override
             public void onProximity() {
                 if (endAction != null)
                     endAction.run();
             }
         });
     }
 }
