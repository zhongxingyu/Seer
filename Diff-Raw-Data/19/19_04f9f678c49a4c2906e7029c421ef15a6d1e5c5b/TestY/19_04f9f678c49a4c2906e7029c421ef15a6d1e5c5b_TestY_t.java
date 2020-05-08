 import static org.junit.Assert.*;
 import java.util.Vector;
 import org.junit.*;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.game.YGame;
 import edu.berkeley.gamesman.game.YGame.Node;
 
/**
 * @author Daniel and hEADcRASH
 *
 */
 public class TestY {
     
 /*
     @Test
     public void TestConstructor() {
         YGame yGame = new YGame(3, 6);
         assertTrue(yGame.getBoardSize() == 54); // not counting inner most
                                                 // triangle
     }
 
 
     // General test case to print all index numbers, etc...
     @Test
     public void Test_Print_All() {
 
         int innerTriangleSegments = 3;
         int outerRingSegments = 6;
 
         int index = 0;
         int currentTriangleSegments = 3 * innerTriangleSegments;
         int triangle = 1;
         int triangleRings = (outerRingSegments - innerTriangleSegments) + 1;
 
         YGame yGame = new YGame(innerTriangleSegments, outerRingSegments);
         yGame.fillBoardWithPlayer('X');
 
         for (int j = 0; j < triangleRings; j++, currentTriangleSegments += 3, triangle++) {
             for (int k = 0; k < currentTriangleSegments; k++, index++) {
 
                 Vector<YGame.Node> neighbors = yGame.getNeighbors(triangle,
                         index, 'X');
                 System.out.print("\nTriangle " + triangle + " at index "
                         + index + ": ");
                 int saw[] = new int[neighbors.size()];
                 for (int i = 0; i < neighbors.size(); i++)
                     saw[i] = neighbors.get(i).getTriangle();
             
                 Arrays.sort(saw);
                 for (int i = 0; i < neighbors.size(); i++)
                     System.out.print(saw[i] + " ");
                 
             }
             System.out.println();
             index = 0;
         }
         assertEquals(0,0);
     }
     */
     
     /*
      * Array exp[][] is of the form { {TRIANGLE#, INDEX#}, {TRIANGLE#, INDEX#} ...}
      */
 
     private Configuration conf24, conf36;
     private YGame ygame24, ygame36;
 
 
     @Before
     public void setUp() throws ClassNotFoundException{
        conf24 = new Configuration("jobs/YGame24.job");
         ygame24 = (YGame)conf24.getGame();     
         ygame24.fillBoardWithPlayer('X');
         
        conf36 = new Configuration("jobs/YGame36.job");
         ygame36 = (YGame)conf36.getGame();     
         ygame36.fillBoardWithPlayer('X');
     }
 
     @Test
     public void Test_2by4_Tri0_Ind0()  {
         Vector<Node> neighbors = ygame24.getNeighbors(0, 0, 'X');
         int exp[][] = { {0,1}, {0,5} , {1,8}, {1,0}, {1,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri0_Ind1()  {
         Vector<Node> neighbors = ygame24.getNeighbors(0, 1, 'X');
         int exp[][] = { {0,2}, {0,3} , {0,5}, {0,0}, {1,1}, {1,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_2by4_Tri0_Ind2()  {
         Vector<Node> neighbors = ygame24.getNeighbors(0, 2, 'X');
         int exp[][] = { {0,3}, {0,1} , {1,2}, {1,3}, {1,4}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri0_Ind3()  {
         Vector<Node> neighbors = ygame24.getNeighbors(0, 3, 'X');
         int exp[][] = { {0,4}, {0,5} , {0,1}, {0,2}, {1,4}, {1,5} };
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri0_Ind4()  {
         Vector<Node> neighbors = ygame24.getNeighbors(0, 4, 'X');
         int exp[][] = { {0,5}, {0,3} , {1,5}, {1,6}, {1,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri0_Ind5()  {
         Vector<Node> neighbors = ygame24.getNeighbors(0, 5, 'X');
         int exp[][] = { {0,0}, {0,1} , {0,3}, {0,4}, {1,7}, {1,8}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_2by4_Tri1_Ind0()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 0, 'X');
         int exp[][] = { {1,1}, {0,1} , {1,8}, {2,11}, {2,0}, {2,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_2by4_Tri1_Ind1()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 1, 'X');
         int exp[][] = { {1,2}, {0,1} , {0,0}, {1,0}, {2,1}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind2()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 2, 'X');
         int exp[][] = { {1,3}, {0,2} , {0,1}, {1,1}, {2,2}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind3()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 3, 'X');
         int exp[][] = { {1,4}, {0,2} , {1,2}, {2,3}, {2,4}, {2,5}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind4()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 4, 'X');
         int exp[][] = { {1,5}, {0,3} , {0,2}, {1,3}, {2,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind5()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 5, 'X');
         int exp[][] = { {1,6}, {0,4} , {0,3}, {1,4}, {2,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind6()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 6, 'X');
         int exp[][] = { {1,7}, {0,4} , {1,5}, {2,7}, {2,8}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind7()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 7, 'X');
         int exp[][] = { {1,8}, {0,5} , {0,4}, {1,6}, {2,9}, {2,10}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri1_Ind8()  {
         Vector<Node> neighbors = ygame24.getNeighbors(1, 8, 'X');
         int exp[][] = { {1,0}, {0,0} , {0,5}, {1,7}, {2,10}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind0()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 0,'X');
         int exp[][] = { {2,1}, {1,0}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_2by4_Tri2_Ind1()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 1,'X');
         int exp[][] = { {2,2}, {1,1}, {1,0}, {2,0}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind2()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 2,'X');
         int exp[][] = { {2,3}, {1,2}, {1,1}, {2,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind3()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 3,'X');
         int exp[][] = { {2,4}, {1,3}, {1,2}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind4()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2,4 ,'X');
         int exp[][] = { {2,5}, {1,3}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind5()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 5,'X');
         int exp[][] = { {2,6}, {1,4}, {1,3}, {2,4}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind6()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2,  6,'X');
         int exp[][] = { {2,7}, {1,5}, {1,4}, {2,5}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind7()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 7 ,'X');
         int exp[][] = { {2,8}, {1,6}, {1,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind8()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 8,'X');
         int exp[][] = { {2,9}, {1,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind9()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 9,'X');
         int exp[][] = { {2,10}, {1,7}, {1,6}, {2,8}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind10()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 10,'X');
         int exp[][] = { {2,11}, {1,8}, {1,7}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_2by4_Tri2_Ind11()  {
         Vector<Node> neighbors = ygame24.getNeighbors(2, 11,'X');
         int exp[][] = { {2,0}, {1,0}, {1,8}, {2,10}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     /*
      * 
      * 
      * 
      * Next test case: 3by6 YGame
      * 
      * 
      * 
      */
     
     @Test
     public void Test_3by6_Tri0_Ind0()  {
         Vector<Node> neighbors = ygame36.getNeighbors(0, 0,'X');
         int exp[][] = { {1,1}, {1,2}, {1,4}, {1,5}, {1,7}, {1,8}}; 
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind0()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 0,'X');
         int exp[][] = { {1,1}, {1,8}, {2,11}, {2,0}, {2,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind1()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 1,'X');
         int exp[][] = { {1,2}, {0,0}, {1,8}, {1,0}, {2,1}, {2,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind2()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 2,'X');
         int exp[][] = { {1,3}, {1,4}, {0,0}, {1,1}, {2,2}, {2,3}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind3()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 3,'X');
         int exp[][] = { {1,4}, {1,2}, {2,3}, {2,4}, {2,5}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind4()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 4,'X');
         int exp[][] = { {1,5}, {0,0}, {1,2}, {1,3}, {2,5}, {2,6}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind5()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 5,'X');
         int exp[][] = { {1,6}, {1,7}, {0,0}, {1,4}, {2,6}, {2,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind6()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 6,'X');
         int exp[][] = { {1,7}, {1,5}, {2,7}, {2,8}, {2,9}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind7()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 7,'X');
         int exp[][] = { {1,8}, {0,0}, {1,5}, {1,6}, {2,9}, {2,10}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri1_Ind8()  {
         Vector<Node> neighbors = ygame36.getNeighbors(1, 8,'X');
         int exp[][] = { {1,0}, {1,1}, {0,0}, {1,7}, {2,10}, {2,11}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri2_Ind0()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 0,'X');
         int exp[][] = { {2,1}, {1,0}, {2,11}, {3,14}, {3,0}, {3,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri2_Ind1()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 1,'X');
         int exp[][] = { {2,2}, {1,1}, {1,0}, {2,0}, {3,1}, {3,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind2()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 2,'X');
         int exp[][] = { {2,3}, {1,2}, {1,1}, {2,1}, {3,2}, {3,3}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind3()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 3,'X');
         int exp[][] = { {2,4}, {1,3}, {1,2}, {2,2}, {3,3}, {3,4}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind4()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 4,'X');
         int exp[][] = { {2,5}, {1,3}, {2,3}, {3,4}, {3,5}, {3,6}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind5()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 5,'X');
         int exp[][] = { {2,6}, {1,4}, {1,3}, {2,4}, {3,6}, {3,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind6()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 6,'X');
         int exp[][] = { {2,7}, {1,5}, {1,4}, {2,5}, {3,7}, {3,8}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind7()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 7,'X');
         int exp[][] = { {2,8}, {1,6}, {1,5}, {2,6}, {3,8}, {3,9}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind8()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 8,'X');
         int exp[][] = { {2,9}, {1,6}, {2,7}, {3,9}, {3,10}, {3,11}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind9()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 9,'X');
         int exp[][] = { {2,10}, {1,7}, {1,6}, {2,8}, {3,11}, {3,12}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind10()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 10,'X');
         int exp[][] = { {2,11}, {1,8}, {1,7}, {2,9}, {3,12}, {3,13}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri2_Ind11()  {
         Vector<Node> neighbors = ygame36.getNeighbors(2, 11,'X');
         int exp[][] = { {2,0}, {1,0}, {1,8}, {2,10}, {3,13}, {3,14}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri3_Ind0()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 0,'X');
         int exp[][] = { {3,1}, {2,0}, {3,14}, {4,17}, {4,0}, {4,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri3_Ind1()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 1,'X');
         int exp[][] = { {3,2}, {2,1}, {2,0}, {3,0}, {4,1}, {4,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind2()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 2,'X');
         int exp[][] = { {3,3}, {2,2}, {2,1}, {3,1}, {4,2}, {4,3}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind3()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 3,'X');
         int exp[][] = { {3,4}, {2,3}, {2,2}, {3,2}, {4,3}, {4,4}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind4()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 4,'X');
         int exp[][] = { {3,5}, {2,4}, {2,3}, {3,3}, {4,4}, {4,5}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind5()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 5,'X');
         int exp[][] = { {3,6}, {2,4}, {3,4}, {4,5}, {4,6}, {4,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind6()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 6,'X');
         int exp[][] = { {3,7}, {2,5}, {2,4}, {3,5}, {4,7}, {4,8}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind7()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 7,'X');
         int exp[][] = { {3,8}, {2,6}, {2,5}, {3,6}, {4,8}, {4,9}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind8()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 8,'X');
         int exp[][] = { {3,9}, {2,7}, {2,6}, {3,7}, {4,9}, {4,10}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind9()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 9,'X');
         int exp[][] = { {3,10}, {2,8}, {2,7}, {3,8}, {4,10}, {4,11}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind10()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 10,'X');
         int exp[][] = { {3,11}, {2,8}, {3,9}, {4,11}, {4,12}, {4,13}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind11()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 11,'X');
         int exp[][] = { {3,12}, {2,9}, {2,8}, {3,10}, {4,13}, {4,14}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind12()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 12,'X');
         int exp[][] = { {3,13}, {2,10}, {2,9}, {3,11}, {4,14}, {4,15}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind13()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 13,'X');
         int exp[][] = { {3,14}, {2,11}, {2,10}, {3,12}, {4,15}, {4,16}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri3_Ind14()  {
         Vector<Node> neighbors = ygame36.getNeighbors(3, 14,'X');
         int exp[][] = { {3,0}, {2,0}, {2,11}, {3,13}, {4,16}, {4,17}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri4_Ind0()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 0,'X');
         int exp[][] = { {4,1}, {3,0}, {4,17}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_3by6_Tri4_Ind1()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 1,'X');
         int exp[][] = { {4,2}, {3,1}, {3,0}, {4,0}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind2()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 2,'X');
         int exp[][] = { {4,3}, {3,2}, {3,1}, {4,1}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind3()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 3,'X');
         int exp[][] = { {4,4}, {3,3}, {3,2}, {4,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind4()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 4,'X');
         int exp[][] = { {4,5}, {3,4}, {3,3}, {4,3}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind5()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 5,'X');
         int exp[][] = { {4,6}, {3,5}, {3,4}, {4,4}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind6()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 6,'X');
         int exp[][] = { {4,7}, {3,5}, {4,5}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind7()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 7,'X');
         int exp[][] = { {4,8}, {3,6}, {3,5}, {4,6}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind8()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 8,'X');
         int exp[][] = { {4,9}, {3,7}, {3,6}, {4,7}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind9()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 9,'X');
         int exp[][] = { {4,10}, {3,8}, {3,7}, {4,8}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind10()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 10,'X');
         int exp[][] = { {4,11}, {3,9}, {3,8}, {4,9}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind11()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 11,'X');
         int exp[][] = { {4,12}, {3,10}, {3,9}, {4,10}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind12()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 12,'X');
         int exp[][] = { {4,13}, {3,10}, {4,11}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind13()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 13,'X');
         int exp[][] = { {4,14}, {3,11}, {3,10}, {4,12}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind14()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 14,'X');
         int exp[][] = { {4,15}, {3,12}, {3,11}, {4,13}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind15()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 15,'X');
         int exp[][] = { {4,16}, {3,13}, {3,12}, {4,14}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind16()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 16,'X');
         int exp[][] = { {4,17}, {3,14}, {3,13}, {4,15}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_3by6_Tri4_Ind17()  {
         Vector<Node> neighbors = ygame36.getNeighbors(4, 17,'X');
         int exp[][] = { {4,0}, {3,0}, {3,14}, {4,16}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     /*
     @Test
     public void Test_4by8_Tri0_Ind0()  {
         Vector<Node> neighbors = ygame48.getNeighbors(0, 0,'X');
         int exp[][] = { {0,1}, {0,2}, {1,10}, {1,11}, {1,1}, {1,2}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     
     @Test
     public void Test_4by8_Tri0_Ind1()  {
         Vector<Node> neighbors = ygame48.getNeighbors(0, 1,'X');
         int exp[][] = { {0,2}, {0,0}, {1,2}, {1,3}, {1,5}, {1,6}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     
     @Test
     public void Test_4by8_Tri0_Ind2()  {
         Vector<Node> neighbors = ygame48.getNeighbors(0, 2,'X');
         int exp[][] = { {0,0}, {0,1}, {1,6}, {1}, {}, {}};
 
         for (int i = 0; i < exp.length; i++){
             assertEquals(exp[i][0], neighbors.get(i).getTriangle());
             assertEquals(exp[i][1], neighbors.get(i).getIndex());
         }
     }
     */
     @AfterClass
     public static void oneTimeTearDown() {}
     
 }
