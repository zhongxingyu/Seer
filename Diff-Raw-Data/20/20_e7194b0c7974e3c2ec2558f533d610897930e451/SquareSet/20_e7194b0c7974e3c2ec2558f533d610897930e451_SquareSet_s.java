 package com.hacklechalet.gravitris;
 
 import javax.microedition.khronos.opengles.GL10;
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 /**
  * Created by marc on 25/05/13.
  */
 public class SquareSet {
     public Set<Square> set;
 
     protected int type = -1;
     public final static int SQUARE_SET_T = 0;
     public final static int SQUARE_SET_I = 1;
     public final static int SQUARE_SET_L = 2;
     public final static int SQUARE_SET_L2 = 3;
     public final static int SQUARE_SET_S = 4;
     public final static int SQUARE_SET_S2 = 5;
     public final static int SQUARE_SET_C = 6;
 
 
     public SquareSet()
     {
         this.set = new HashSet<Square>();
     }
     public SquareSet(float size)
     {
         this(size, -1);
     }
     public SquareSet(float size, int type)
     {
         this(size, type, 0);
     }
     public SquareSet(float size, int type, int orientation)
     {
         this();
         if(type < 0 || type > 6)
         {
            Random rnd = new Random();
            type = rnd.nextInt(6);
         }
         Square primitive;
         float x,y;
         if (orientation == 1)// Landscape
         {
             x = 1.5f;
             y = -1.0f;
         }
         else if (orientation == 2) // Landscape in the other way
         {
             x = -3.0f;
             y = -1.0f;
         }
         else // (orientation == 0)  Portrait
         {
             x = -0.5f;
             y = 4.4f;
         }
 
        Random rd = new Random();
         int choice;
         switch(type)
         {
             case SQUARE_SET_T:
             {
                 float[] c = {
                         1f, 0f, 0f, 1f, // vertex 0 red
                         1f, 0f, 0f, 1f,
                         1f, 0f, 0f, 1f,
                         1f, 0f, 0f, 1f,
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 // Choice between the 4 different forms
                choice = rd.nextInt(3);
                 Square bottom;
                 this.set.add(primitive);
                 switch(choice)
                 {
                     // ###
                     //  #
                     case 0:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_LEFT));
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_BOTTOM));
                         break;
                     //  #
                     // ###
                     case 1:
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_LEFT));
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_RIGHT));
                         break;
                     // #
                     // ##
                     // #
                     case 2:
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_BOTTOM));
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_RIGHT));
                         break;
                     //  #
                     // ##
                     //  #
                     case 3:
                     default:
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_BOTTOM));
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_LEFT));
                 }
 
             }break;
             case SQUARE_SET_I:
             {
                 float[] c= {
                         0f, 1f, 0f, 1f,
                         0f, 1f, 0f, 1f, // vertex 1 green
                         0f, 1f, 0f, 1f,
                         0f, 1f, 0f, 1f,
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 this.set.add(primitive);
                choice = rd.nextInt(1);
                 switch(choice)
                 {
                     // ####
                     case 0:
                         Square left = primitive.genNeighboor(Square.DIRECTION_LEFT);
                         this.set.add(left);
                         this.set.add(left.genNeighboor(Square.DIRECTION_LEFT));
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                         break;
                     // #
                     // #
                     // #
                     // #
                     case 1:
                     default:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_TOP));
                         Square bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_BOTTOM));
                 }
 
             }break;
             case SQUARE_SET_L:
             {
                 float[] c= {
                         0f, 0f, 1f, 1f,
                         0f, 0f, 1f, 1f,
                         0f, 0f, 1f, 1f, // vertex 2 blue
                         0f, 0f, 1f, 1f,
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 this.set.add(primitive);
                choice = rd.nextInt(3);
                 Square bottom;
                 Square right;
                 switch (choice)
                 {
                     // ###
                     //   #
                     case 0:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_LEFT));
                         right = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                         this.set.add(right);
                         this.set.add(right.genNeighboor(Square.DIRECTION_BOTTOM));
                         break;
                     //  #
                     //  #
                     // ##
                     case 1:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_TOP));
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_LEFT));
                         break;
                     // #
                     // ###
                     case 2:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_TOP));
                         right = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                         this.set.add(right);
                         this.set.add(right.genNeighboor(Square.DIRECTION_RIGHT));
                         break;
                     // ##
                     // #
                     // #
                     case 3:
                     default:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_BOTTOM));
                 }
 
             }break;
             case SQUARE_SET_L2:
             {
                 float[] c= {
                         1f, 1f, 0f, 1f,
                         1f, 1f, 0f, 1f,
                         1f, 1f, 0f, 1f,
                         1f, 1f, 0f, 1f,
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 Square right;
                 Square bottom;
                 this.set.add(primitive);
                choice = rd.nextInt(3);
                 switch(choice)
                 {
                     //   #
                     // ###
                     case 0:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_LEFT));
                         right = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                         this.set.add(right);
                         this.set.add(right.genNeighboor(Square.DIRECTION_TOP));
                         break;
                     // #
                     // #
                     // ##
                     case 1:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_TOP));
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_RIGHT));
                         break;
                     // ###
                     // #
                     case 2:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                         Square left = primitive.genNeighboor(Square.DIRECTION_LEFT);
                         this.set.add(left);
                         this.set.add(left.genNeighboor(Square.DIRECTION_BOTTOM));
                         break;
                     // ##
                     //  #
                     //  #
                     case 3:
                     default:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_LEFT));
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_BOTTOM));
 
                 }
             }break;
             case SQUARE_SET_S:
             {
                 float[] c= {
                         1f, 0f, 1f, 1f,
                         1f, 0f, 1f, 1f,
                         1f, 0f, 1f, 1f,
                         1f, 0f, 1f, 1f, // vertex 3 magenta
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 this.set.add(primitive);
                 Square right;
                 Square bottom;
                 choice = rd.nextInt(2);
                 switch (choice)
                 {
                     // ##
                     //  ##
                     case 0:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_LEFT));
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_RIGHT));
                         break;
                     //  #
                     // ##
                     // #
                     case 1:
                     default:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_BOTTOM));
                         right = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                         this.set.add(right);
                         this.set.add(right.genNeighboor(Square.DIRECTION_TOP));
                 }
             }   break;
             case SQUARE_SET_S2:
             {
                 float[] c= {
                         1f, 0f, 1f, 1f,
                         1f, 0f, 1f, 1f,
                         1f, 0f, 1f, 1f,
                         1f, 0f, 1f, 1f, // vertex 3 magenta
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 this.set.add(primitive);
                 Square right;
                 Square bottom;
                 choice = rd.nextInt(2);
                 switch (choice)
                 {
                     //  ##
                     // ##
                     case 0:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                         bottom = primitive.genNeighboor(Square.DIRECTION_BOTTOM);
                         this.set.add(bottom);
                         this.set.add(bottom.genNeighboor(Square.DIRECTION_LEFT));
                         break;
                     // #
                     // ##
                     //  #
                     case 1:
                     default:
                         this.set.add(primitive.genNeighboor(Square.DIRECTION_TOP));
                         right = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                         this.set.add(right);
                         this.set.add(right.genNeighboor(Square.DIRECTION_BOTTOM));
                 }
             }   break;
             case SQUARE_SET_C:
             default:
             {
                 float[] c= {
                         0f, 1f, 1f, 1f,
                         0f, 1f, 1f, 1f,
                         0f, 1f, 1f, 1f,
                         0f, 1f, 1f, 1f,
                 };
                 Square.colors = c;
                 primitive = new Square(size, x,y);
                 this.set.add(primitive);
                 Square rightC = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                 this.set.add(rightC);
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_BOTTOM));
                 this.set.add(rightC.genNeighboor(Square.DIRECTION_BOTTOM));
             }
         }
         this.type = type;
     }
 
     public void add(Square square)
     {
         this.set.add(square);
     }
 
     public void add(SquareSet squareSet)
     {
         for(Square square : squareSet.set)
         {
             this.add(square);
         }
     }
 
     public void remove(Square square)
     {
         this.set.remove(square);
     }
 
     public void remove(SquareSet squareSet)
     {
         for(Square square : squareSet.set)
         {
             this.remove(square);
         }
     }
 
 
     public void draw(GL10 gl)
     {
         for(Square square : this.set)
         {
             square.draw(gl);
         }
     }
     public int getType()
     {
         return this.type;
     }
 }
