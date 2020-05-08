 package flatten;
 
 
 import static org.junit.Assert.assertEquals;
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import java.util.*;
 
 /**
  * Tests for {@link Foo}.
  *
  * @author user@example.com (Jung Gyu Park)
  */
 public class FooTest {
 
     @Test
     public void test1() {
 	int[] a = {1, 5, 4, 9, 6};
         Tree<Integer> n = Tree.Node.tree(a[1], a[2], a[3]);
         Tree<Integer> l1 = Tree.Leaf.leaf(a[0]);
         Tree<Integer> l2 = Tree.Leaf.leaf(a[4]);
         Tree<Integer> t = new Tree.Node(l1, n, l2);
         MyFlattenTree<Integer> mt = new MyFlattenTree<Integer>();
         List<Integer> l = mt.flattenInOrder(t);
         Iterator<Integer> it = l.iterator();
 	int i = 0;
         while(it.hasNext()) {
             int p = it.next();
 	    org.junit.Assert.assertEquals("failure - not equal", a[i++], p);
         }
    }
    
     @Test
     public void test2() {
 	int[] a = {1, 5, 4, 9, 6, 3};
         Tree<Integer> n = Tree.Node.tree(a[1], a[2], a[3]);
         Tree<Integer> l1 = Tree.Leaf.leaf(a[0]);
         Tree<Integer> l2 = Tree.Leaf.leaf(a[4]);
         Tree<Integer> t = new Tree.Node(l1, n, l2);
        t = new Tree.Node(t, Tree.Leaf.leaf(a[5]), null);
         MyFlattenTree<Integer> mt = new MyFlattenTree<Integer>();
         List<Integer> l = mt.flattenInOrder(t);
         Iterator<Integer> it = l.iterator();
 	int i = 0;
         while(it.hasNext()) {
             int p = it.next();
 	    org.junit.Assert.assertEquals("failure - not equal", a[i++], p);
         }
         
     }
 }
