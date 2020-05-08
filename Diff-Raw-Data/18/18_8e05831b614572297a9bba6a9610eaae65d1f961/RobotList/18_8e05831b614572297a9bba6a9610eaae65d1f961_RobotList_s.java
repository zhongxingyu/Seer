 package arena;
 
 /* Default libraries */
 import java.util.Random;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Comparator;
 
 /* Libraries */
 import parameters.*;
 
 public class RobotList implements Game, Iterable<Robot>
 {
     private Robot[] armies;
     private HashMap <Integer,Double> speedy 
         = new HashMap <Integer,Double>();
     final private Random rand = new Random();
     
     int nextRobot  = -1;
     int emptySpace = 0;
     
     RobotList(int nPlayers) 
     {
         Debugger.say("[RobotList] Builded");
         armies = new Robot[nPlayers * ROBOTS_NUM_MAX];
     }
     
     void add(Robot r)
     {
         /* TODO: When the three initial are created,
          *       we will not need this test any more */
         if(r == null) return;
         armies[emptySpace++] = r;
         speedy.put(r.ID, 1.0 * r.speed);
     }
     
     public void remove(Robot robot)
     {
         for(int i = 0; i < emptySpace; i++) 
         {
             if(armies[i] == robot) 
             {
                 Robot r = armies[i];
                 armies[i] = null;
                 speedy.remove(r.ID);
                 break;
             }
         }
     }
     
     void sort()
     {
         for(Robot r: armies)
         {
             if(r == null) continue;
             speedy.put(r.ID, rand.nextDouble() + r.speed);
            Debugger.say("[SPEED]", speedy.get(r.ID));
         }
         
         quickSort(0, emptySpace-1);
         //Arrays.<Robot>sort(armies, 0, emptySpace,
         //    new Comparator<Robot>() {
         //        public int compare(Robot robotA, Robot robotB)
         //        {
         //            /* Comparison function */
         //            double costA = speedy.get(robotA.ID);
         //            double costB = speedy.get(robotB.ID);
         //            
         //            Debugger.say("[CMP] costA: ", costA);
         //            Debugger.say("[CMP] costB: ", costB);
         //             
         //            if(speedy.get(robotA) == null) 
         //                costA = Double.POSITIVE_INFINITY;
         //            if(speedy.get(robotB) == null) 
         //                costB = Double.POSITIVE_INFINITY;
         //            
         //            /* Compare */
         //            if(costA < costB) return -1;
         //            if(costA > costB) return 1;
         //            return 0;
         //        }
         //        
         //        public boolean equals(Object r)
         //        {
         //            return this.equals(r);
         //        }
         //    }
         //);
         
        Debugger.say("[SORT]    ");
         for(Robot r: armies) 
             if(r != null) Debugger.print(r, ", ");
         Debugger.say();
         
         int emptySpace = 1;
         for(Robot r: armies) 
             if(r == null) break; 
             else emptySpace++;
     }
     
     //private Robot next()
     //{
     //    nextRobot++;
     //    if(nextRobot == emptySpace) 
     //    { 
     //        nextRobot = -1; 
     //        return null;
     //    }
     //    return armies[nextRobot];
     //}
 
     public Iterator<Robot> iterator()
     {
         return new RobotListIterator(emptySpace);
     }
     
     private void mergeSort(int begin, int end)
     {
         Debugger.say("[MERGESORT][begin: ", begin, "]");
         Debugger.say("[MERGESORT][end:   ", end  , "]");
         if(begin < end)
         {
             Debugger.say("[MERGESORT][IN]");
             int middle = (begin + end)/2;
             mergeSort(begin, middle);
             mergeSort(middle+1, end);
             merge(begin, middle+1, end);
         }
     }
     
     private void merge(int begin, int middle, int end)
     {
         int i, j, k;
         Robot[] aux = new Robot[end-begin+1];
         
         // Copy first part untill the middle in the right order
         for(i = 0, k = begin; k < middle; i++, k++)
             aux[i] = armies[k];
         
         // copy the second part in the recerse order
         for(j = end-middle+1; k < end; j--, k++)
             aux[j] = armies[k];
         i = 0; j = end-begin-1;
         
         // Compares the elements and exchange them
         for(k = begin; k < end; k++)
             if(cmpLessRobot(aux[i], aux[j]))
                 armies[k] = aux[i++];
             else armies[k] = aux[j--];
         
         Debugger.print("        ");
         for(Robot r: armies)
             Debugger.print(r, ", ");
         Debugger.say();
     }
     
     private void quickSort(int begin, int end)
     {
         if(begin < end)
         {
             int middle = divide(begin, end);
             quickSort(begin, middle-1);
             quickSort(middle +1, end);
         }
     }
     
     private int divide(int begin,int end)
     {
         int     i = begin -1;
         Robot   x = armies[end];
         
         for(int j = begin; j <= end; j++)
             if(cmpLessRobot(armies[j], x))
             {
                 Robot t = armies[++i];
                 armies[i] = armies[j];
                 armies[j] = t;
             }
         
         return i;
     }
     
     private boolean cmpLessRobot(Robot robotA, Robot robotB)
     {
         /* Comparison function */
         double costA = speedy.get(robotA.ID);
         double costB = speedy.get(robotB.ID);
         
        Debugger.say("[CMP] costA: ", costA);
        Debugger.say("[CMP] costB: ", costB);
          
//        if(speedy.get(robotA) == null) 
//            costA = Double.POSITIVE_INFINITY;
//        if(speedy.get(robotB) == null) 
//            costB = Double.POSITIVE_INFINITY;
         return (costA <= costB);
     }
     
     private class RobotListIterator implements Iterator<Robot>
     {
         private int nextRobot = -1;
         final private int emptySpace;
 
         public RobotListIterator(int emptySpace)
         {
             this.emptySpace = emptySpace;
         }
         
         public boolean hasNext()
         {
             String pre = "[Iterator] ";
             if(nextRobot + 1 != emptySpace)
             {
                 Debugger.say("\n", pre, "Has next!");
                 Debugger.say(pre, "[nextRobot:", 
                              armies[nextRobot+1], "]");
                 Debugger.say(pre, "[emptySpace:", 
                              emptySpace, "]");
             }
             else Debugger.say("\n", pre, "No next...");
             return nextRobot + 1 != emptySpace;
         }
         
         public Robot next()
         {
             Debugger.say("[Iterator] Getting next!");
             nextRobot++;
             return armies[nextRobot];
         }
         
         public void remove()
         {
             Robot r = armies[nextRobot];
             armies[nextRobot] = null;
             speedy.remove(r.ID);
         }
     }
 }
