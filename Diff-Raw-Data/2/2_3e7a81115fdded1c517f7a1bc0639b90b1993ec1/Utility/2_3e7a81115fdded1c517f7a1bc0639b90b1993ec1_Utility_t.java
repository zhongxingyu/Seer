 package naybur;
 
 import java.util.ArrayList;
 
 public class Utility 
 {
     /**
       * Calculates the squared distance from point a to point b
       *
       * @param
       */
     public static double sqDist(ArrayList<Double> a, ArrayList<Double> b)
     {
         double dist = 0;
 
         for(int i = 0; i < a.size(); i++)
         {
             dist += Math.pow(a.get(i) - b.get(i), 2);
         }
 
         return dist;
     }
 
     /**
       * Calculates the squared distance from point a to point b
       *
       * @param
       */
     public static double sqDistArray(double[] a, double[] b)
     {
         double dist = 0;
 
         for(int i = 0; i < a.length; i++)
         {
             dist += Math.pow(a[i] - b[i], 2);
         }
 
         return dist;
     }
 
 
     /**
       * Maps a list of points from one range to another. For example, given a list of points: { {5, 5} {4, 2} {9, 6} } a start_range {0, 10} and end_range {0,1} 
       * the output would be { {.5,.5} {.4,.2} {.9,.6} }
       *
       * @param point_list The list of points to be mapped
       * @param start_range The range that the points lie within. All coordinates of the point must lie within this range. 
      * @param end_range The range that the points should be mapped to
       * @param dims The dimensionality of the points
       */
     public static double[][] map(double[][] point_list, double[] start_range, double[] end_range, int dims)
     {
         double start_diff = Math.abs(start_range[0] - start_range[1]);
         double end_diff = Math.abs(end_range[0] - end_range[1]);
 
         for(int i = 0; i < point_list.length; i++)
         {
             for(int j = 0; j < dims; j++)
             {
                 point_list[i][j] = ((point_list[i][j] - start_range[0])/start_diff + end_range[0]) * end_diff; 
             }
         }
 
         return point_list;
     }
     
 
     /**
       * Maps a single point from one range to the specified end range. 
       *
       * @param point The point to be mapped
       * @param start_range The initial range that the point lies on. 
       * @param end_range The final range that the point should be mapped to
       * @param dims The dimensionality of the point.
       */
     public static double[] map(double[] point, double[] start_range, double[] end_range, int dims)
     {
         double start_diff = Math.abs(start_range[0] - start_range[1]);
         double end_diff = Math.abs(end_range[0] - end_range[1]);
 
         for(int j = 0; j < dims; j++)
         {
             point[j] = ((point[j] - start_range[0])/start_diff + end_range[0]) * end_diff; 
         }
 
         return point;
     }
 
 
     /**
       * Maps a set of 2d points inside a rectangle of any size to the unit square.
       *
       * @param point_list A set of points to be mapped
       * @param range The x and y bounds of the rectangle that contains the points in point_list. For a rectangle with its lower left hand corner at (1,2) and width 7
       * and height 10 the range would be { {1,8} {2,12} }
       */
     public static double[][] map(double[][] point_list, double range[][])
     {
         double largest_diff = 0;
         int largest_range = 0;
 
         for(int i = 0; i < start_range.length; i++)
         {
             double diff = Math.abs(start_range[i][0] - start_range[i][1]);
 
             if(diff > largest_diff) 
                 largest_diff = diff;
         }
 
         for(int i = 0; i < point_list.length; i++)
         {
             for(int j = 0; j < 2; j++)
             {
                 point_list[i][j] = (point_list[i][j] - start_range[j][0])/start_diff;
             }
         }
 
     }
 }
