 package com.bogdan.sokoban.ai.model;
 
 import org.junit.Test;
 
 import java.util.PriorityQueue;
 
 import static org.junit.Assert.*;
 
 /**
  * @author Bogdan
  */
 public class SquareTest {
 
     @Test
     public void createTest() {
         Square node = new Square(SquareType.EMPTY, 0, 1);
         assertNotNull(node);
         assertEquals("Square_0_1", node.getName());
         assertEquals(SquareType.EMPTY, node.getType());
     }
 
     @Test
     public void compareTest() {
         Square s1 = createSquare(1);
         Square s2 = createSquare(2);
         assertTrue("s1 must be better than s2", s1.compareTo(s2) < 0);
     }
 
     @Test
     public void priorityQueueCompareTest() {
         Square s1 = createSquare(2);
         Square s2 = createSquare(1);
         Square s3 = createSquare(3);
         PriorityQueue<Square> pq =
                 new PriorityQueue<Square>();
         pq.add(s1);
         pq.add(s2);
         pq.add(s3);
         assertEquals(s2, pq.peek());
     }
     
    @Test void deadEndSquareTest() {
         Square square = new Square(SquareType.EMPTY, 0, 0);
         square.setDeadEnd(true);
         assertTrue(square.isDeadEnd());
     }
 
     private Square createSquare(int f) {
         Square square = new Square(SquareType.EMPTY, 0, 0);
         square.setG(0);
         square.setH(f);
         return square;
     }
 
 }
