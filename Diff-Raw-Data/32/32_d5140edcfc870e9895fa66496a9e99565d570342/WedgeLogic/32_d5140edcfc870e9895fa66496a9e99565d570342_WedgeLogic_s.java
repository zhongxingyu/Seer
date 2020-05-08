 package jo.sm.ship.logic;
 
 public class WedgeLogic
 {
     private static final short[] CLOCKWISE_X = {
         4, 10, 0, 8,        6, 13, 2, 11, 
        5, -1, 7, 1,        -1, 3, -1, -1,
     };
     private static final short[] CLOCKWISE_Y = {
         1, 2, 3, 0,         7, 4, 5, 6,
        10, -1, 11, 13,     -1, 8, -1, -1,
     };
     private static final short[] CLOCKWISE_Z = {
         10, 7, 11, 1,       8, 3, 13, 5,
        0, -1, 4, 6,        -1, 2, -1, -1,
     };
     
     public static short rotate(short ori, int rx, int ry, int rz)
     {
         rx = -rx;
         //ry = -ry;
         //rz = -rz;
         rx %= 4;
         ry %= 4;
         rz %= 4;
         if (rx < 0)
             rx = 4 + rx;
         if (ry < 0)
             ry = 4 + ry;
         if (rz < 0)
             rz = 4 + rz;
         ori = rotate(ori, CLOCKWISE_X, rx);
         ori = rotate(ori, CLOCKWISE_Y, ry);
         ori = rotate(ori, CLOCKWISE_Z, rz);
         return ori;
     }
 
     private static short rotate(short ori, short[] turns, int num)
     {
         while (num-- > 0)
         {
             if (ori < 0)
                 return ori;
             ori = turns[ori];
         }
         return ori;
     }
 }
