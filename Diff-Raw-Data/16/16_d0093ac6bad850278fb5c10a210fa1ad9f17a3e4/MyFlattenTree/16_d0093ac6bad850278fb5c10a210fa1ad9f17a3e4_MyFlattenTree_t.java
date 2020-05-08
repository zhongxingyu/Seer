 package flatten;
 
 import java.util.List;
 import java.util.ArrayList;
 
 public class MyFlattenTree<T> implements FlattenTree<T>
 {
     class myfunc implements Function<T, List<T>>
     {
         public myfunc(MyFlattenTree t)
         {
             mft = t;
         }
         
         public List<T> apply(T p) 
         {
	    List<T> r = new ArrayList();
             r.add(p);
             return r;
         }
 
         private MyFlattenTree mft;
     }
 
     class myfunc2 implements Function<Triple<Tree<T>>, List<T>>
     {
         public myfunc2(MyFlattenTree t) 
         {
             mft = t;
         }
         
         public List<T> apply(Triple<Tree<T>> p) 
         {
             List<T> r = new ArrayList();
 
             r.addAll(mft.flattenInOrder(p.left()));
             r.addAll(mft.flattenInOrder(p.middle()));
             r.addAll(mft.flattenInOrder(p.right()));
             return r;
         }
         private MyFlattenTree mft;
     }
     
     public List<T> flattenInOrder(Tree<T> tree) 
     {
         if (tree == null)
	    return new ArrayList<T>();
         
         Either<T, Triple<Tree<T>>> n =  tree.get();
 
         if (n.isLeft()) {
             List<T> l = n.ifLeft(new myfunc(this)); // I have to use singletone
             return l;
         } else {
 
             List<T> l = n.ifRight(new myfunc2(this)); // I have to use singletone
             return l;
         }
 
     }
 
 }
