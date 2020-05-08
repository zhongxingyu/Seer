 public class NodeArrayParser
 {
   private int nodes;
   private int start;
   private int stop;
   private int[][] NodeArrPrv = null;
   public NodeArrayParser(int[][] NDArr)
   {
     NodeArrPrv = NDArr;
     nodes = NDArr.length;
   }
   public void parseArray()
   {
     for (int i=0;i<nodes;i++)
     {
       if(NodeArrPrv[i][2]==2)
       {
         start = i;
         break;
       }
     }
     int current = start;
    while(current!=0)
     {
       if (NodeArrPrv[current][1]==2)
       {
         stop=current;
         break;
       }
       else if (NodeArrPrv[current][2]==2)
       {
         start=current;
       }
       current=next(current);
 
     }
   }
   public void printSolution()
   {
     int current = start;
     while(current!=stop)
     {
       System.out.print(current+1+" ");
       current=next(current);
     }
     System.out.println(stop+1);
   }
   private int next(int i)
   {
     return NodeArrPrv[i][0]-1;
   }
 
 }
