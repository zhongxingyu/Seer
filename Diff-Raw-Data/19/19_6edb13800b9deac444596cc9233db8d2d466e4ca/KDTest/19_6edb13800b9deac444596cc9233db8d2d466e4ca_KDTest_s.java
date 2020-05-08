 package naybur.kdtree;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import java.util.*;
  
 public class KDTest{
  
     private ArrayList<ArrayList<Double>> point_list, test_points; 
     private KDTree kd;
     private double scale_factor_x = 10;
     private double scale_factor_y = 10;
 
     @Before
     public void setUp()
     {
     }
 
     public ArrayList<ArrayList<Double>> createTestList(int num_points, boolean integers)
     {
         ArrayList<ArrayList<Double>> test_list = new ArrayList();
 
         for(int i = 0; i < num_points; i++)
         {
             ArrayList<Double> temp_arraylist = new ArrayList();
             
             double temp_val1 = Math.random() * scale_factor_x;
             double temp_val2 = Math.random() * scale_factor_x;
             
             if(integers)
             {
                 temp_val1 = Math.floor(temp_val1);
                 temp_val2 = Math.floor(temp_val2);
             }
 
             temp_arraylist.add(temp_val1);
             temp_arraylist.add(temp_val2);
 
             test_list.add(temp_arraylist);
         }
 
         return test_list;
     } 
 
     public void compareResults(ArrayList<ArrayList<Double>> point_list, ArrayList<ArrayList<Double>> test_points)
     {
         System.out.println("compareResults");
         
         for(int i = 0; i < test_points.size(); i++)
         {
             ArrayList<Double> linear_result = linearSearch(point_list, test_points.get(i));
 
             ArrayList<Double> kd_result = kd.findNearest(test_points.get(i)).getPoint();
  
             System.out.println(i + ": " + test_points.get(i));
             assertEquals(linear_result.get(0), kd_result.get(0));
             assertEquals(linear_result.get(1), kd_result.get(1));
         }
 
     }
 
 
     public static void printPoints(ArrayList<ArrayList<Double>> list)
     {
        System.out.println("{ ");
 
         int i, j = 0;
 
         for(i = 0; i < list.size()-1; i++)
         {
            System.out.println("{");
 
             for(j = 0; j < list.get(i).size()-1; j++)
             {
                 System.out.print(list.get(i).get(j) + ", " );
             }
 
            System.out.println(list.get(i).get(j+1) + "}, ");
 
         } 
 
         for(j = 0; j < list.get(i).size()-1; j++)
         {
            System.out.print(list.get(i+1).get(j) + ", " );
         }
     }
     
     @After
     public void tearDown() {
     }
  
 
     public static ArrayList<Double> linearSearch(ArrayList<ArrayList<Double>> list, ArrayList<Double> point)
     {
         double best_dist;
         double dist;
 
         ArrayList<Double> best_point = null;
 
         dist = KDTree.sqDist(list.get(0), point);
         best_dist = dist;
 
         for(int i = 0; i < list.size(); i++)
         {
             dist = KDTree.sqDist(list.get(i), point);
 
             if(dist < best_dist)
             {
                 best_dist = dist;
                 best_point = list.get(i);
             }
         }
 
         return best_point;
     }
 
     @Test
     public void testBuildTree() 
     {
         kd = new KDTree(2);
 
         ArrayList<ArrayList<Double>> point_list = createTestList(100, false);
 
         kd.build(point_list, 0);
 
         printPoints(point_list);
 //        KDTree.printTree(kd.getRoot());        
     }
 
     @Test
     public void testSearchCustom()
     {
         int dims = 2;
 
         kd = new KDTree(dims);
 
         double[][] points = {   {5.0, 0.0}, {1.0, 6.0}, {6.0, 9.0}, {4.0, 5.0}, {3.0, 9.0}, {7.0, 6.0}, {9.0, 9.0}, {3.0, 0.0}, {2.0, 8.0}, {6.0, 7.0} }; 
 
         ArrayList<ArrayList<Double>> custom_point_list = new ArrayList(), test_points;
         ArrayList<Double> ta[] = new ArrayList[points.length];
 
         test_points = createTestList(10, true);
 
         for(int i = 0; i < points.length; i++)
         {
             ta[i] = new ArrayList();
 
             for(int j = 0; j < points[i].length; j++)
             {
                 ta[i].add(points[i][j]);
             }
 
             custom_point_list.add(ta[i]);
         } 
 
    
         KDNode root = kd.build(custom_point_list, 0);
 
         ArrayList<Double> sp = new ArrayList<Double>();
 
         sp.add(7.0);
         sp.add(0.0);
         
         ArrayList<Double> kd_result = kd.findNearest(sp).getPoint();
         ArrayList<Double> linear_result = linearSearch(custom_point_list, sp);
 
         assertEquals(linear_result.get(0), kd_result.get(0));
         assertEquals(linear_result.get(1), kd_result.get(1));
     } 
 
     @Test
     public void testSearchRandomInteger() 
     {
         System.out.println("\ntestSearchRandomInteger\n");
         point_list = createTestList(10, true);
         test_points = createTestList(10, true);
 
         kd = new KDTree(2);
         kd.build(point_list, 0);
 
         KDTree.printTree(kd.getRoot());
 
         printPoints(point_list);
         printPoints(test_points);
 
         compareResults(point_list, test_points);
     }
 
     @Test 
     public void testSearchRandomDouble()
     {
         System.out.println("\ntestSearchRandomDouble\n");
         point_list = createTestList(10, false);
         test_points = createTestList(10, false);
 
         kd = new KDTree(2);
         kd.build(point_list, 0);
 
         printPoints(point_list);
         printPoints(test_points);
 
         compareResults(point_list, test_points);
     }
 }
