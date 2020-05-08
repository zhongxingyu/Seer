 import java.util.LinkedList;
 import java.util.HashSet;
 
 
 
 
 
 public class TreeParser
 {
   private LinkedList<Node> HeadList = null;
   public TreeParser(LinkedList<Node> hds)
   {
     HeadList = hds;
   }
   public void parseTree()
   {
     Node current = HeadList.getFirst();
     Node start = null;
     LinkedList<Node> bridges = null;
     while (current.getRight()!=null)
     {
       if (current.getFLeft()!=null)
       {
         start = current;
         bridges = new LinkedList<Node>();
       }
       if ( start != null )
       {
         if ( current.getLeft() == null )
         {
           bridges.add(current);
         }
         else
         {
           bridges.add(current);
           break;
         }
       }
       current=current.getRight();
     }
     for ( Node pr : bridges )
     {
      //System.out.println(pr);
       System.out.print(pr.getData()+" ");
     }
   }
   private LinkedList<Node> nextLevel(LinkedList<Node> begin)
   {
     HashSet<Node> st = new HashSet<Node>();
     Node rig = null;
     Node lef = null;
     for (Node current : begin)
     {
       rig = current.getRight();
       if(rig != null)
       {
         st.add(rig);
         lef = current.getLeft();
         if ( lef != null )
         {
           st.add(lef);
         }
       }
       
     }
     return new LinkedList<Node>(st);
   }
 
 
 
 }
