 import java.util.Stack;
 import java.util.Queue;
 import java.util.LinkedList;
 
 public class Salesman {
 
      private static int[] xPoints;
      private static int[] yPoints;
      private static int minPath;
      private static LinkedList<Integer> availablePoints;
      private static Stack<Integer> bestPath;
      private static int count;
 
      private static int ptDistance(int pt1, int pt2)
      {
           count++;
           int x1 = xPoints[pt1];
           int y1 = yPoints[pt1]; 
           int x2 = xPoints[pt2];
           int y2 = yPoints[pt2]; 
           int dist = (int)(Math.pow(x2-x1,2)+Math.pow(y2-y1,2)); // note: no sqrt!!
           return dist;
      }
 
      private static void find_path(int pointsLeft, int prevPoint, int dist, Stack< Integer > currPath)
      {
           if (pointsLeft==0)
           {
                if (dist<minPath)
                {
                     minPath=dist;
                     bestPath = currPath;
                }
                return;
           }
           for (int i=0; i<pointsLeft; i++)
           {
                int ptOut= availablePoints.peek();
                availablePoints.remove();
                int toNextPoint = ptDistance(ptOut, prevPoint);
                currPath.push(ptOut);
                find_path(pointsLeft-1, ptOut, dist+toNextPoint, currPath);
                availablePoints.add(ptOut);
                currPath.pop();
           }
      }
 
      public static void main(String[] args) {
           {
                availablePoints = new LinkedList<Integer>();
                bestPath = new Stack<Integer>();
                int numPoints = Integer.parseInt(args[0]);
                count=0;
                xPoints = new int[numPoints];
                yPoints = new int[numPoints];
                int start,end;
                //          time(&start);
                for (int i=1; i<numPoints+1; i++)
                {
                     xPoints[i-1] = Integer.parseInt(args[2*i-1]); 
                     yPoints[i-1] = Integer.parseInt(args[2*i]); 
                     availablePoints.add(i-1);
                }
                System.out.println(availablePoints);
                minPath=0x0FFFFFFF;
               int firstPoint = availablePoints.peek();
               availablePoints.remove();
                Stack< Integer > currPath = new Stack< Integer >();
               currPath.push(firstPoint);
               find_path(numPoints-1, firstPoint, 0, currPath);
                System.out.println("Distance: " + minPath);
                Stack< Integer > printStack = new Stack<Integer>();
                while (!bestPath.empty())
                {
                     printStack.push(bestPath.peek());
                     bestPath.pop();
                }
                while (!printStack.empty())
                {
                     System.out.print((int) printStack.peek() + " " );
                     printStack.pop();
                }
                //          time(&end);
                System.out.println("\nComparisons: " + count);
                //          double dif = difftime(end,start);
 //               System.out.printf("Program runtime: %f \n", dif);
           }
 
      }
 }
