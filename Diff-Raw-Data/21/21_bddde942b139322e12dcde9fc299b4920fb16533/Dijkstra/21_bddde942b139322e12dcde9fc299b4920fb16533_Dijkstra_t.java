 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dijkstra;
 import model.Arc;
 import model.Node;
 import model.ZoneGeo;
 import model.Chemin;
 
 import java.util.*;
 
 /**
  *
  * @author anisbenyoub
  */
 
 
 public class Dijkstra {
 
     protected
             static Integer Infinity =1000;
     
     protected  static Integer getMinVertex(HashMap<Integer,Integer> dist)
     {
         Set cles = dist.keySet();
         Iterator it = cles.iterator();
         Integer min=Infinity;
         Integer node=0;
         while (it.hasNext())
         {
             Object cle = it.next();
             Object valeur = dist.get(cle); 
             if(dist.get(cle)<min);
             {
                 min=dist.get(cle);
                 node=cle.hashCode();
             }
                 
         }
         return node;
     }
     public  static ArrayList<Chemin> solve(ZoneGeo theGraph, Node source, ArrayList<Node> toEnglobe)
 
     {
     	ArrayList<Node> toCompute= (ArrayList<Node>) toEnglobe.clone();
         HashMap<Integer,Integer> dist = new HashMap<Integer, Integer>(); 
         HashMap<Integer,Integer> previous = new HashMap<Integer, Integer>(); 
         
        for(Node v : theGraph.getNodes().values())
         {
             dist.put(v.getID(),Infinity);
             previous.put(v.getID(),-1);
         }
         dist.put(source.getID(),0);
          Comparator<PriorityNode> comparator = new NodeComparator();
         PriorityQueue<PriorityNode> Q = new PriorityQueue<PriorityNode>(Infinity, comparator);
        for(Node v : theGraph.getNodes().values())
         {
             PriorityNode newNode = new PriorityNode(Infinity,v.getID());
             Q.add(newNode);
         }
         PriorityNode aNode =  new PriorityNode(0,source.getID());
         Q.add(aNode);
         while((Q.size()!=0)&& (toEnglobe.size()!=0))
         {
 
             Node u = theGraph.getNode(getMinVertex(dist));
 			System.out.println("Nouveau point"+u.getX()+" "+u.getY());
 
             Q.remove(u.getID());
             if(dist.get(u.getID())==Infinity)
             {
             }
             else
             {
                 for(Node v : u.getOutNodes())
                 {
                     Integer alt=dist.get(u.getID())+u.getDistance(v.getID() );
                     if(alt<dist.get(v.getID()))
                     {
                         dist.put(v.getID(),alt);
                         previous.put(v.getID(), u.getID());
                         PriorityNode newNode=  new PriorityNode(alt,v.getID());
                         Q.add(newNode) ;
                     }
                 }
             }
             for(Node v : toEnglobe)
             {
             	if(v.getID()==u.getID())
             	{
             		toEnglobe.remove(v);
             	}
             }
         }
         
         ArrayList<Chemin> solution= new ArrayList<Chemin>();
         for(Node n : toCompute)
         {
        	Integer distance=0;
         	ArrayList<Node> contenu = new ArrayList<Node>();
         	Node temp=theGraph.getNode(n.getID());
         	while(n.getID()!=source.getID())
         	{
         		Integer theID =previous.get(temp.getID());
         		Node toADD=theGraph.getNode(theID);
         		contenu.add(toADD);
             	temp=theGraph.getNode(theID);
         	}
        	Chemin newChemin = new Chemin(contenu,source,n,distance);        
        	solution.add(newChemin);
         }
         return solution;
     }
 }
