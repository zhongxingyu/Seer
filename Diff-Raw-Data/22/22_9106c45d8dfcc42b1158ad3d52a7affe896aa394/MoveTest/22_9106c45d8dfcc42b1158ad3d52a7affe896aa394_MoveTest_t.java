 package lv.k2611a.domain.goals;
 
 import org.junit.Test;
 
 import lv.k2611a.domain.Map;
 import lv.k2611a.util.MapGenerator;
 
 import static junit.framework.Assert.assertEquals;
 
 public class MoveTest {
     @Test
     public void testAStar() {
         Map map = new Map(50,50);
        assertEquals(49, getPathLengthTo(map, 0,0));
        assertEquals(49, getPathLengthTo(map, 0, 49));
        assertEquals(49, getPathLengthTo(map, 49, 0));
 
        assertEquals(1, getPathLengthTo(map, 48, 49));
        assertEquals(1, getPathLengthTo(map, 49, 48));
        assertEquals(1, getPathLengthTo(map, 48, 48));
 
        assertEquals(0, getPathLengthTo(map, 49, 49));
    }

    private int getPathLengthTo(Map map, int x, int y) {
        map.clearUsageFlag();
        return new Move(x,y ).calcShortestPath(49,49,map,-1).size();
     }
 
     @Test
     public void testAStarPerformance() {
         Map map = new Map(256,256);
         long startTime = System.currentTimeMillis();
         int iterationCount = 100;
         for (int i = 0; i < iterationCount; i++) {
             Move move = new Move(255,255);
             move.calcShortestPath(0,0,map,-1);
         }
         long endTime = System.currentTimeMillis();
         long delta = endTime - startTime;
         double timeForIteration = (double)delta / iterationCount;
         System.out.println("A star time for iteration " + timeForIteration + " ms");
     }
 }
