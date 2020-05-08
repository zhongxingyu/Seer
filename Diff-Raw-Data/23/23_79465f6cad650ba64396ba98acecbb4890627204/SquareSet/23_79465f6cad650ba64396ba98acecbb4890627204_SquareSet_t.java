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
 
     public final static int SQUARE_SET_T = 0;
     public final static int SQUARE_SET_I = 1;
     public final static int SQUARE_SET_L = 2;
     public final static int SQUARE_SET_S = 3;
     public final static int SQUARE_SET_C = 4;
     public final static int SQUARE_SET_L2 = 5;
 
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
         if(type < 0 || type > 5)
         {
             Random rnd = new Random();
             type = rnd.nextInt(5);
         }
         Square primitive;
         if(orientation == 0) // Portrait
         {
             primitive = new Square(size, -1, 5);
         }
        else if (orientation == 1)// Landscape
         {
            primitive = new Square(size, 1.5f, -1);
        }
        else
        {
            primitive = new Square(size, -3.5f, -1);
         }
         this.set.add(primitive);
         switch(type)
         {
             case SQUARE_SET_T:
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_LEFT));
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_BOTTOM));
                 break;
             case SQUARE_SET_I:
                 Square left = primitive.genNeighboor(Square.DIRECTION_LEFT);
                 this.set.add(left);
                 this.set.add(left.genNeighboor(Square.DIRECTION_LEFT));
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_RIGHT));
                 break;
             case SQUARE_SET_L:
                 Square right = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                 this.set.add(right);
                 this.set.add(right.genNeighboor(Square.DIRECTION_RIGHT));
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_BOTTOM));
                 break;
             case SQUARE_SET_L2:
                 Square right2 = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                 this.set.add(right2);
                 this.set.add(right2.genNeighboor(Square.DIRECTION_RIGHT));
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_TOP));
                 break;
             case SQUARE_SET_S:
                 Square rightS = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                 this.set.add(rightS);
                 Square bottomS = rightS.genNeighboor(Square.DIRECTION_BOTTOM);
                 this.set.add(bottomS);
                 this.set.add(bottomS.genNeighboor(Square.DIRECTION_RIGHT));
                 break;
             case SQUARE_SET_C:
             default:
                 Square rightC = primitive.genNeighboor(Square.DIRECTION_RIGHT);
                 this.set.add(rightC);
                 this.set.add(primitive.genNeighboor(Square.DIRECTION_BOTTOM));
                 this.set.add(rightC.genNeighboor(Square.DIRECTION_BOTTOM));
         }
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
 }
