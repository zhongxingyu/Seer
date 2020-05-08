 package Code;
 
 import java.awt.Point;
 import javax.swing.Icon;
 
 public class BishopB extends Piece {
 
     public BishopB(Icon icon) {
         super(icon);
         maxY = -75;
         maxX = 0;
         team = 2;
         name = "Bishop";
     }
 
     @Override
     public Icon getIcon() {
         return icon;
     }
 
     @Override
     public int getTeam() {
         return team;
     }
 
     @Override
     public boolean legalMove(int y, int x, Point start, Object brikke, int team2) {
         if (team2 == team) return false;
        if ((Math.abs((int)start.getX() - x) == (Math.abs((int)start.getY()-y))) && (Math.abs((int)start.getY()-y) == 75)){
                 return true;
         }
         return false;
     }
 }
