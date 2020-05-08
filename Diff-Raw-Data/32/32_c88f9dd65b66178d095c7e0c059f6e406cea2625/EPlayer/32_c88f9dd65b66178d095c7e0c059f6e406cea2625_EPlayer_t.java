 import java.util.*;
 import java.math.*;
 import java.io.*;
 
 /// An entity that can own a square. Stores its name and the number of squares it owns.
 abstract class EOwner {
     
     String  name;
     int     default_score;
     int     score;
     
     /// Increments the number of squares this owner owns.
     public void increment_score() {
         this.score += 1;
         return;
     }
     
     /// Decrements the number of squares this owner owns.
     public void decrement_score() {
         this.score -= 1;
         return;
     }
     
     /// Resets the number of owned squares to the default.
     public void reset_score() {
         this.score = default_score;
     }
     
     /// Returns the number of squares this owner owns.
     public int get_score() {
         return this.score;
     }
     
     /// Sets the name of this owner.
     public void set_name(String new_name) {
         this.name = new_name;
         return;
     }
     
     /// Returns the name of this owner.
     public String get_name() {
         return this.name;
     }
 }
 
 /// A set of functions that a playable entity must implement.
 interface EPlayable {
     
     // attribute manipulation functions
     public void     increment_score();
     public void     decrement_score();
     public void     reset_score();
     public int      get_score();
     public void     set_name(String new_name);
     public String   get_name();
     
     // color manipulation functions
     public void     set_color(int new_color);
     public int      get_color();
     
     // opponent manipulation functions
     public void     set_opponent(EPlayer new_opponent);
     public EPlayer  get_opponent();
     
     // AI functions
     public int      ai_next_color_choice(EBoard board);
 }
 
 /// A player.
 class EPlayer extends EOwner implements EPlayable {
 
     EPlayer     opponent;
     ESquare     starting_square;
     int         color;
     int         ai_difficulty;
     
     public EPlayer(String name, int ai_difficulty) {
         this.name               = name;
         this.default_score      = 0;
         this.score              = this.default_score;
         this.color              = 0;
         this.opponent           = null;
         this.starting_square    = null;
         this.ai_difficulty      = ai_difficulty;
     }
     
     /// Switches the player's color to the given color.
     public void set_color(int new_color) {
         this.color = new_color;
         return;
     }
     
     /// Returns the player's color.
     public int get_color() {
         return this.color;
     }
     
     /// Switches the player's opponent to the given opponent.
     public void set_opponent(EPlayer new_opponent) {
         this.opponent = new_opponent;
         return;
     }
     
     /// Returns the player's opponent.
     public EPlayer get_opponent() {
         return this.opponent;
     }
     
     /// Returns the AI's next choice of color.
     public int ai_next_color_choice(EBoard board) {
        int color_choice = 0;
         if(this.ai_difficulty == 0) {
            color_choice = (int) Math.floor(Math.random() * board.num_colors);
             while(color_choice == this.color) {
                 color_choice = (int) Math.floor(Math.random() * board.num_colors);
             }
         }
         return color_choice;
     }
 }
 
 /// A neutral owner.
 class ENeutral extends EOwner {
     
     public ENeutral(String name, int squares_on_board) {
         this.name              = name;
         this.default_score     = squares_on_board;
         this.score             = this.default_score;
     }
 }
