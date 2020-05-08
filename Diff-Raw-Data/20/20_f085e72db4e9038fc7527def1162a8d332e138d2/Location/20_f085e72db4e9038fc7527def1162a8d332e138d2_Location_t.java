 package maze;
 
 public class Location {
     int column;
     int row;
 
     public Location(int column, int row) {
         this.column = column;
         this.row = row;
     }
 
     @Override
     public boolean equals(Object obj) {
        if (obj instanceof Location) {
             Location other = (Location) obj;
             return column == other.column && row == other.row;
         }
         return false;
     }
 
     public Location getLocationAtDirection(Direction direction) {
        switch (direction) {
             case UP:
                return new Location(column, row - 1);
             case DOWN:
                return new Location(column, row + 1);
             case LEFT:
                return new Location(column - 1, row);
             case RIGHT:
                return new Location(column + 1, row);
             default:
                return new Location(column, row - 1);
         }
     }
 
     public int getX() {
         return column * 2 + 1;
     }
 
     public int getY() {
         return row * 2 + 1;
     }
 }
