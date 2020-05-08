 import java.util.*;
 import java.math.*;
 import java.io.*;
 
 /// A square on the board.
 class ESquare {
     
     // owner and color
     EOwner  owner;
     int     color;
     
     // state flags
     Boolean visited;
     Boolean border;
     
     // coordinates: may be deprecated if irregular boards are supported
     int     x_coord;
     int     y_coord;
     
     /// Creates a new unowned square with COLOR_0.
     public ESquare(int x_coord, int y_coord) {
         this.owner   = null;
         this.color   = 0;
         this.visited = false;
         this.border  = false;
         this.x_coord = x_coord;
         this.y_coord = y_coord;
     }
     
     /// Switches the square's owner to the given owner.
     public void set_owner(EOwner new_owner) {
         this.owner = new_owner;
         return;
     }
     
     /// Returns the square's owner.
     public EOwner get_owner() {
         return this.owner;
     }
     
     /// Switches the square's color to the given color.
     public void set_color(int new_color) {
         this.color = new_color;
         return;
     }
     
     /// Returns the square's color.
     public int get_color() {
         return this.color;
     }
     
     /// Randomizes the square's color.
     public void randomize_color(int num_colors) {
         this.color = (int) Math.floor(Math.random() * num_colors);
         return;
     }
     
     /// Returns false if this square is owned by the opponent.
     public Boolean conquerable_by_player(EPlayer player) {
         if(this.get_owner() == player.get_opponent()) {
             return false;
         }
         return true;
     }
     
     /// Returns true if this square will be conquered by this move.
    public Boolean conquered_by_move(EColor next_color, EPlayer conqueror) {
        if(this.color.equals(next_color) || this.owner == conqueror) {
             return true;
         }
         return false;
     }
 }
