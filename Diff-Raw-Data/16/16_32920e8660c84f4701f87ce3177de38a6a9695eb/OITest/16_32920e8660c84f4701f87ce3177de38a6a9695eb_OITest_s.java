 package naybur.grid;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import java.util.*;
 import static naybur.Utility.*;
 import naybur.Point;
  
 public class OITest{
  
     private ArrayList<Point> point_list, test_point_list; 
     private OIGrid grid;
     private int scale_x = 10, scale_y = 10; 
     private int num_points = 10;
     private double[][] pos_range = { {0, scale_x}, {0, scale_y} };
     private double[][] neg_range = { {-scale_x, scale_x}, {-scale_y, scale_y} };
     
     @Before
     public void setUp()
     {
     }
 
     public ArrayList<Point> createTestList(int num_point_list, boolean integers, boolean negative)
     {
         ArrayList<Point> list = new ArrayList<Point>();
 
         for(int i = 0; i < num_point_list; i++)
         {
             double x = Math.random() * scale_x;
             double y = Math.random() * scale_y;
             
             if(integers)
             {
                 x = Math.floor(x);
                 y = Math.floor(y);
             }
 
             if(negative)
             {
                 x = x * 2 - scale_x; 
                 y = y * 2 - scale_y; 
             }
 
             Point p = new Point(x, y);
             list.add(p);
         }
 
         return list;
     } 
 
     public void compareResults(ArrayList<Point> point_list, ArrayList<Point> test_point_list)
     {
         System.out.println("compareResults");
         
         for(Point search_point: test_point_list)
         {
             int num_results = 1;
             
             int grid_result = grid.findNearest(search_point, num_results).get(0);
             int linear_result = linearSearch(point_list, search_point);
             
             assertEquals(grid_result, linear_result);
         }
 
     }
 
 
     public static void printPoints(ArrayList<Point> list)
     {
         System.out.println("printPoints");
         
         for(Point point: list)
         {
             System.out.println(point);
         }
     }
 
     @Test
     public void testBuildGrid() 
     {
         System.out.println("\ntestBuildGrid\n");
         ArrayList<Point> point_list = createTestList(num_points, false, false);
 
         printPoints(point_list);
 
         grid = new OIGrid(point_list, pos_range);
     }
 
     @Test
     public void testSearchCustom()
     {
         double[][] point_array = { {-6.0, -4.0}, {-8.0, -10.0}, {2.0, -2.0}, {-2.0, -10.0}, {-10.0, 8.0}, {-10.0, 6.0}, {-2.0, 0.0}, {0.0, 2.0}, {8.0, -10.0}, {-8.0, -10.0} };
 
        for(int i = 0; i < point_list.size(); i++)
         {
             double x = point_array[i][0];
             double y = point_array[i][1];
 
            Point p = new Point(x,y);

            point_list.add(p);     
         }
 
         OIGrid grid = new OIGrid(point_list, neg_range);
 
         Point search_point = new Point(-4.0, 6.0);
 
         printPoints(point_list);
 
         int num_results = 1;
             
         int grid_result = grid.findNearest(search_point, num_results).get(0);
         int linear_result = linearSearch(point_list, search_point);
         
         assertEquals(grid_result, linear_result);
     } 
 
     @Test
     public void testSearchRandomIntegerPos() 
     {
         System.out.println("\ntestSearchRandomIntegerPos\n");
         point_list = createTestList(num_points, true, false);
         test_point_list = createTestList(num_points, true, false);
 
         grid = new OIGrid(point_list, pos_range);
 
 //        printPoints(point_list);
 //        printPoints(test_point_list);
 
         compareResults(point_list, test_point_list);
     }
 
     @Test 
     public void testSearchRandomDoublePos()
     {
         System.out.println("\ntestSearchRandomDoublePos\n");
         point_list = createTestList(num_points, false, false); 
         test_point_list = createTestList(num_points, false, false);
 
         grid = new OIGrid(point_list, pos_range);
 
 //        printPoints(point_list);
 //        printPoints(test_point_list);
 
         compareResults(point_list, test_point_list);
     }
 
     @Test
     public void testSearchRandomIntegerNeg() 
     {
         System.out.println("\ntestSearchRandomIntegerNeg\n");
         point_list = createTestList(num_points, true, true);
         test_point_list = createTestList(num_points, true, true);
 
         grid = new OIGrid(point_list, neg_range);
 
 //        printPoints(point_list);
 //        printPoints(test_point_list);
 
         compareResults(point_list, test_point_list);
     }
 
     @Test
     public void testSearchRandomDoubleNeg()
     {
         System.out.println("\ntestSearchRandomDoubleNeg\n");
         point_list = createTestList(num_points, false, true); 
         test_point_list = createTestList(num_points, false, true);
 
         grid = new OIGrid(point_list, neg_range);
 
 //        printPoints(point_list);
 //        printPoints(test_point_list);
 
         compareResults(point_list, test_point_list);
     }
    
 }
