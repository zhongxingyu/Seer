 package stars;
 
 public class UnionFind {
 
   public static Node find(Node n){
     if(n.parent == null){
       return n;
     }
     else{
      Node parent = find(n.parent);
       n.parent = parent;
       return parent;
     }
   }
   public static boolean union(Node a, Node b){
     Node aRoot = find(a);
     Node bRoot = find(b);
 
     if(aRoot == bRoot){
       return false;
     }
     if(a.rank < b.rank){
       b.parent = a;
     }
     else if(a.rank > b.rank){
       a.parent = b;
     }
     else{
       b.parent = a;
       a.rank = a.rank +1;
     }
     return true;
 
   }
 
 
 
 }
