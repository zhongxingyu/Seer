 package com.burningman.tictactoe;
 
 /**
  * Created by Anton on 24.05.13.
  */
 public class Core {
     private Field field;
 
     public Core() {
         field = new Field();
     }
 
     public Player get_Field(int n, int m) {
         return field.get_field(n, m);
     }
 
     public void set_Field(int n, int m, Player player) {
         field.set_field(n, m, player);
     }
 
     public class Field {
         private Player[][] field;
 
         public Field() {
             field = new Player[3][3];
             for (int i = 0; i < 3; i++)
                 for (int j = 0; j < 3; j++)
                     field[i][j] = Player.none;
         }
 
         public void set_field(int n, int m, Player player) {
             field[n][m] = player;
         }
 
         public Player get_field(int n, int m) {
             return field[n][m];
         }
 
     }
 
     public Player search_won() {
         for (int i = 0; i < 3; i++)
            if (field.get_field(0, i) == field.get_field(1, i) && field.get_field(0, i) == field.get_field(2, i)&&field.get_field(1,i)!=Player.none)
                 return field.get_field(0, i);
         for (int i = 0; i < 3; i++)
            if (field.get_field(i, 0) == field.get_field(i, 1) && field.get_field(i, 0) == field.get_field(i, 2)&field.get_field(i,1)!=Player.none)
                 return field.get_field(i, 0);
        if (field.get_field(0, 0) == field.get_field(1, 1) && field.get_field(0, 0) == field.get_field(2, 2)&field.get_field(1,1)!=Player.none)
             return field.get_field(0, 0);
        if (field.get_field(2, 0) == field.get_field(1, 1) && field.get_field(2, 0) == field.get_field(0, 2)&field.get_field(1,1)!=Player.none)
             return field.get_field(2, 0);
         return Player.none;
     }
 
     public boolean check_draw() {
         for (int i = 0; i < 3; i++)
             for (int j = 0; j < 3; j++)
                 if (get_Field(i, j) == Player.none)
                     return false;
         return true;
     }
 
 
 }
