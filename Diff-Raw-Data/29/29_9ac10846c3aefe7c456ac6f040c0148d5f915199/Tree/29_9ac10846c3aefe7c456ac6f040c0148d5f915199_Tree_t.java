 import java.util.List;
 import java.util.LinkedList;
 import java.io.InputStreamReader;
 import java.io.BufferedReader;
 
 public class Tree
 {
   private Node head;
   private LinkedList<Node> headList;
   public Tree()
   {
     head = null;
     headList = new LinkedList<Node>();
   }
   public Tree(int hd,int ch)
   {
     head = new Node(hd,new Node(ch,null,null),null);
     headList = new LinkedList<Node>();
     headList.add(head);
   }
   public LinkedList<Node> getHeadList()
   {
     return headList;
   }
   private Node findDown(Node begin,int data)
   {
     Node current = begin;
     if (current==null) 
     {
       return null;
     }
     else if (current.getData() == data)
     {
       return current;
     }
     else 
     {
       Node rightres = findDown(current.getRight(),data);
       if ( rightres != null )
       {
         return rightres;
       }
       else
       {
         Node leftres = findDown(current.getLeft(),data);
         return leftres;
       }
     }
   }
   private Node find(int data)
   {
     Node ans = null;
     for (Node hd : headList)
     {
       ans = findDown(hd,data) ;
       if (ans != null) 
       {
         return ans;
       }
     }
     return null;
   }
   public boolean insert(int father, int child)
   {
     if(head == null)
     {
      System.out.println("New head ( "+father+" , "+child+" ) ");
 
       head = new Node(father,new Node(child,null,null),null);
       headList.add(head);
       return true;
     }
     else
     {
       Node current = find(father);
       Node rig = null;
       Node lef = null;
       if( current != null )
       {
        //System.out.println("Found father in tree there he is "+father);
         rig = current.getRight();
         lef = current.getLeft();
         if ( rig == null )
         {
           rig = new Node ( child, null, null );
           rig.setFRight(current);
           current.setRight(rig);
         }
         else 
         {
           lef = new Node ( child, null, null );
           lef.setFRight(current);
           current.setLeft(lef);
         }
 
         return true;
       }
       else
       {
         current = find(child);
         if ( current != null)
         {
          //System.out.println("Found child in tree there it is "+child);
           rig = current.getFRight();
           lef = current.getFLeft();
           if ( rig == null )
           {
             rig = new Node ( father, current, null );
             current.setFRight(rig);
             headList.add(rig);
             headList.remove(current) ;
           }
           else 
           {
             lef = new Node ( father, current, null );
             current.setFLeft(lef);
             headList.add(lef);
             headList.remove(current);
           }
           return true;
         }
         else
         {
        //   System.out.println("( "+father+" , "+child+" ) failed");
           return false;
         }
 
       }
     }
   }
   public void insertNodesInTree(LinkedList<int[]> NodeList)
   {
     int[] current = null;
     int prevSize = 0;
    int MaxTimes = NodeList.size();
     int times = 0;
     while( NodeList.size() > 0)
     { 
       current = NodeList.removeFirst();
      if( insert( current[0],current[1] ) )
      {
        times=0;
         continue;
      }
       else
       {
         NodeList.addLast(current);
         //System.out.println("Stop");
         if (prevSize == NodeList.size())
         {
           times++;
          if (times == MaxTimes) break;
         }
         else
         {
           prevSize = NodeList.size();
         }
 
       }
       
     }
   }
   public void print()
   {
     for (Node hd : headList )
     {
       printTree(hd);
       System.out.println("\nNext head\n");
     }
   }
   private void printTree(Node begin)
   {
     Node current = begin;
     if(current != null)
     {
       current.printNode();
       System.out.print(" Right= ");
       printTree(current.getRight());
       System.out.print(" Left= ");
       printTree(current.getLeft());
     //  printTree(current.getFRight());
     //  printTree(current.getFLeft());
     }
   }
 }
