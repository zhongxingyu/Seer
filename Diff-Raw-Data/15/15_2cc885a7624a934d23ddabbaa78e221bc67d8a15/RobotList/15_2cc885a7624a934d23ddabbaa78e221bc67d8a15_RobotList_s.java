 package arena;
 
 /* Default libraries */
 import java.util.Random;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /* Libraries */
 import parameters.*;
 
 public class RobotList implements Game
 {
     private Robot[] armies;
     private HashMap <Integer,Double> speedy 
         = new HashMap <Integer,Double>();
     final private Random rand = new Random();
     
     int nextRobot  = -1;
     int emptySpace = 0;
     
     RobotList(int nPlayers) 
     {
         armies = new Robot[nPlayers * ROBOTS_NUM_MAX];
     }
     
     void add(Robot r)
     {
         armies[emptySpace++] = r;
         speedy.put(r.ID, 1.0 * r.speed);
     }
     
     void remove(Robot r)
     {
         for(int i = 0; i < emptySpace; i++) armies[i] = null;
         speedy.remove(r.ID);
     }
     
     void sort()
     {
         for(Robot r: armies)
         {
             if(r == null) continue;
             speedy.put(r.ID, rand.nextDouble() + r.speed);
         }
         mergeSort(0, emptySpace-1);
         
         int emptySpace = 1;
         for(Robot r: armies) 
             if(r == null) break; 
             else emptySpace++;
     }
     
     private void mergeSort(int begin, int end)
     {
         if(begin < end-1)
         {
             int middle = (begin + end)/2;
             mergeSort(begin, middle);
             mergeSort(middle, end);
             merge(begin, middle, end);
         }
     }
     
     private void merge(int begin, int middle, int end)
     {
         int i, j, k;
         Robot[] aux = new Robot[end-begin];
         
         // Copy first part untill the middle in the right order
         for(i = 0, k = begin; k < middle; i++, k++)
             aux[i] = armies[k];
         
         // copy the second part in the recerse order
         for(j = end-middle-1; k < end; j--, k--)
             aux[j] = armies[k];
         i = 0; j = end-begin-1;
         
         // Compares the elements and exchange them
         for(k = begin; k < end; k++)
             if(cmpLessRobot(aux[i], aux[j]))
                 armies[k] = aux[i++];
             else armies[k] = aux[j--];
     }
     
     private boolean cmpLessRobot(Robot robotA, Robot robotB)
     {
         /* Comparison function */
         double costA = speedy.get(robotA.ID);
         double costB = speedy.get(robotA.ID);
         if(speedy.get(robotA) == null) 
             costA = Double.POSITIVE_INFINITY;
         if(speedy.get(robotB) == null) 
             costB = Double.POSITIVE_INFINITY;
         return costA <= costB;
     }
     
     Robot nextRobot()
     {
         return null;
     }
 }
