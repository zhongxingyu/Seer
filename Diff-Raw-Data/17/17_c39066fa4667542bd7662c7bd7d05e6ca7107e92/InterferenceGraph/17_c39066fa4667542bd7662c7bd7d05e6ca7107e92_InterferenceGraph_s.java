 import java.util.*;
 
 public class InterferenceGraph
 {
   public ArrayList<Node> nodes;
   
   public InterferenceGraph()
   {
     nodes = new ArrayList<Node>();
   }
   
   public void addNode(Node n)
   {
     nodes.add(n);
   }
   
   public Node nodeForRegister(Register r)
   {
     for(Node n : nodes)
     {
       if(n.reg.sparcEquals(r))
       {
         return n;
       }
     }
     System.err.println("couldn't find " + r + " yo");
     //return null;
     //the following is really hacky/f'ed up solution...don't trust this!!!!
     Node n = new Node(r);
     nodes.add(n);
     return n;
   }
   
   public void addAndColorNode(Node n, ArrayList<String> colors)
   {
     // color node based on its neighbors
     ArrayList<String> neighborColors = new ArrayList<String>();
     for(Node other : n.edges)
     {
       neighborColors.add(other.reg.sparcName);
     }
     ArrayList<String> availableColors = new ArrayList<String>(colors);
     availableColors.removeAll(neighborColors);
     String color = availableColors.get(0); // will have to change for spill later (ie if array is empty)
    n.reg.sparcName = color;
     
     // add colored node to graph
     nodes.add(n);
   }
 }
