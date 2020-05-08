 
 package inc;
 
 public class Cell
 {
         public int data;
         public int id;
         public static int sdata;
      // public SubCell subcell = new SubCell();
      // public SubCell subcell2;
         public Object W = new Object();
         public Object E = new Object();
 
         public void go()
         {
      //         synchronized(subcell)
      //         {
      //                 int x = 5;
      //         }
 
      //         synchronized(subcell2)
      //         {
      //                 int y = 5;
      //         }
 
                 synchronized(W)
                 {
                        int z = 6;
                }
                synchronized(E)
                {
                        int t = 6;
                 }
         }
 }
 
 
       
 
