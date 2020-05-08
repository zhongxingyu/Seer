 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GridShape;
 
 import gameoflife.Grid;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Christiaan
  */
 public class Sphere implements Profile {
     
     public List<Point> GetNeighbours(Grid grid, GridCell.Profile cellProfile, Point position) {
         List<Point> potentialNeighbours = cellProfile.GetNeighbours(position);
         List<Point> neighbours = new ArrayList<Point>();
         
         for(Point p : potentialNeighbours) {
             int x = position.x + p.x;
             int y = position.y + p.y;
             
             if(x < 0) {
                 x += grid.maxWidth();
                 y *= 2;
             }
             else if(x >= grid.maxWidth()) {
                 x -= grid.maxWidth();
                 y *= -2;
             }
             
             if(y < 0) {
                 x *= 2;
                 y += grid.maxHeight();
             }
             else if(y >= grid.maxHeight()) {
                 x *= -2;
                 y -= grid.maxHeight();
             }
             
            if(x < 0)
                x += grid.maxWidth();
            else if(x >= grid.maxWidth())
                x -= grid.maxWidth();
 
            if(y < 0)
                y += grid.maxHeight();
            else if(y >= grid.maxHeight())
                y -= grid.maxHeight();
             
             neighbours.add(new Point(x,y));
         }
 
         return neighbours;
     }
 }
