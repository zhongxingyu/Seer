 
 package TSP;
 
 /**
  *
  * Drew McDermott
  * adm75@pitt.edu
  */
 import java.util.ArrayList;
 import java.util.List;
 import java.awt.*;
 
 
 
 public class MinSpanTree 
 {
     private ArrayList<GraphVertex> verts;
     private double[] Queue;
     private int[] Parent;
     
     public MinSpanTree(ArrayList<GraphVertex> Vertices)
     {
         verts = Vertices;
         Queue = new double[verts.size()];
        Parent = new int[verts.size()];
     }
     
     public void generateTree()
     {
         for(int i  =0; i < verts.size(); i++)
         {
             Queue[i] = Double.MAX_VALUE;    
             Parent[i] = -1;
         }
         addNode(0);
         
     }
     
     private int addNode(int par)
     {
         GraphVertex p = verts.get(par);
         int unmarked = 0;
         
         // Update min Distance
         double t_min;
         GraphVertex gv;
         for(int i  = 0; i < verts.size(); i++)
         {
             gv = verts.get(i);
             if(!gv.Marked)
             {
                 unmarked++;
                 t_min = p.distanceTo(gv);
                 if(t_min < Queue[i])
                 {
                     //This path is shorter
                     Parent[i] = par;
                     Queue[i] = t_min; 
                 }
             }
         }
         
         //Now pick shortest path in Queue
         double minQ = Double.MAX_VALUE;
         int minI = -1;
         for(int i  = 0; i < verts.size(); i++)
         {
             if(Queue[i] < minQ)
             {
                 minI = i;
                 minQ = Queue[i];
             }
         }
         
         //Add it and mark it
         GraphVertex conn_par = verts.get(Parent[minI]);
         GraphVertex conn_child = verts.get(minI);
         conn_child.connectTo(conn_par);
         conn_child.Marked = true;
         unmarked--;
         
         if(unmarked > 0)
         {
             addNode(minI);
         }
         
         return unmarked;
         
     }
     
     
 
 }
