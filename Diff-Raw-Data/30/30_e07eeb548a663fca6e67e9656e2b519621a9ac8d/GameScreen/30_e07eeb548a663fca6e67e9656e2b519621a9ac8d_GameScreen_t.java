 package roguelike.rpg.sisyphean;
 
 import android.widget.Toast;
 import sofia.graphics.ImageShape;
 import sofia.graphics.Color;
 import sofia.graphics.RectangleShape;
 import sofia.app.ShapeScreen;
 
 // -------------------------------------------------------------------------
 /**
  *  This is the screen/activity for the game.
  *  It will create a maze at the beginning of the activity. The player must
  *  choose a hero and enemies will spawn. There are Health and Mana points
  *  which will be represented by rectangles at the bottom of the screen.
  *
  *  @author Tk
  *  @version Nov 9, 2012
  */
 public class GameScreen extends ShapeScreen
 {
     private Maze maze;
     private RectangleShape[][] visualMaze;
 
     // ----------------------------------------------------------
     /**
      * Place a description of your method here.
      */
     public void initialize()
     {
         // Stuff here.
         maze = new Maze(1);
         visualMaze = new RectangleShape[maze.size()][maze.size()];
         float y = getHeight();
         float x = getWidth();
         float size = Math.min(y, x);
         float cellSize = size / maze.size();
         float bottom = cellSize;
         float right = cellSize;
         float top = 0;
         float left = 0;
         for ( int row = 0; row < maze.size(); row++)
         {
             left = 0;
             right = cellSize;
             top = cellSize * row;
             bottom = cellSize * (row + 1);
             for ( int col = 0; col < maze.size(); col++)
             {
                 left = cellSize * col;
                 right = cellSize * (col + 1);
                /*visualMaze[col][row] =
                     new RectangleShape(left, top, right, bottom);
                 visualMaze[col][row].setColor(Color.black);
                 visualMaze[col][row].setFilled(true);
                 visualMaze[col][row].setFillColor(Color.white);
                add(visualMaze[col][row]);*/
                 add(new ImageShape(
                         "ground", left, top, right, bottom));
 
                 if (maze.getCell(col, row).getWalls()[0]) //top wall
                 {
                     add(new ImageShape(
                         "back", left, top, right, bottom));
                 }
                 if (maze.getCell(col, row).getWalls()[1]) //right wall
                 {
                     add(new ImageShape(
                         "right", left, top, right, bottom));
                 }
                else if (row > 0 && maze.getCell(col, row - 1).getWalls()[1])
                {
                    add(new ImageShape(
                        "right_front", left, top, right, bottom));
                }
                 if (maze.getCell(col, row).getWalls()[3]) //left wall
                 {
                     add(new ImageShape(
                         "left", left, top, right, bottom));
                 }
                else if (row > 0 && maze.getCell(col, row - 1).getWalls()[3])
                {
                    add(new ImageShape(
                        "left_front", left, top, right, bottom));
                }
             }
 
         }
 
         //check();
     }
 
     public void check()
     {
         for (int col = 0; col < maze.size(); col++)
         {
             for (int row = 0; row < maze.size(); row++)
             {
                 String s = "(" + maze.getCell(col, row).x() + ", " + maze.getCell(col, row).y() + ") " + maze.getCell(col, row).wallString();
                 Toast.makeText(this, s, Toast.LENGTH_LONG).show();
             }
         }
     }
 }
