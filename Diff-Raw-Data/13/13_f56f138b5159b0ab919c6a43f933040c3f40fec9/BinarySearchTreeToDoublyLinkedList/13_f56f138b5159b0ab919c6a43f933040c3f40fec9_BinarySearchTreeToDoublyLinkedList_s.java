 package guibin.zhang.datastructure.tree;
 
 /**
  * http://cslibrary.stanford.edu/109/TreeListRecursion.html
  * Here's the formal problem statement: 
  * Write a recursive function treeToList(Node root) that takes an ordered binary tree 
  * and rearranges the internal pointers to make a **circular doubly linked list** out of the tree nodes. 
  * The "previous" pointers should be stored in the "small" field 
  * and the "next" pointers should be stored in the "large" field. 
  * The list should be arranged so that the nodes are in increasing order. 
  * Return the head pointer to the new list. 
  * The operation can be done in O(n) time -- essentially operating on each node once. 
  * 
  * Hints:
  * Hint #1
  * The recursion is key. 
  * Trust that the recursive call on each sub-tree works 
  * and concentrate on assembling the outputs of the recursive calls to build the result. 
  * 
  * It's too complex to delve into how each recursive call is going to work -- trust that it did work and assemble the answer from there.
  * 
  * 
  * Hint #2
  * The recursion will go down the tree, recursively changing the small and large sub-trees into lists, 
  * and then append those lists together with the parent node to make larger lists. 
  * Separate out a utility function append(Node a, Node b) that takes two circular doubly linked lists 
  * and appends them together to make one list which is returned. 
  * Writing a separate utility function helps move some of the complexity out of the recursive function.
  * 
  * Hint #3
  * Trust that the recursive calls return correct output when fed correct input -- make the leap of faith. 
  * Look at the partial results that the recursive calls give you, and construct the full result from them. 
  * If you try to step into the recursive calls to think how they are working, you'll go crazy.
  * 
  * Hint #4
  * Decomposing out well defined helper functions is a good idea. 
  * Writing the list-append code separately helps you concentrate on the recursion which is complex enough on its own.
  * 
  * @author guibin
  */
 public class BinarySearchTreeToDoublyLinkedList {
     
     /*
       helper function -- given two list nodes, join them
       together so the second immediately follow the first.
       Sets the .next of the first and the .previous of the second.
     */
     public static void join(BinaryNodeJ<Integer> a, BinaryNodeJ<Integer> b) {
         a.right = b;//right means learge
         b.left = a;//left means small
     }
     
     /*
       helper function -- given two circular doubly linked
       lists, append them and return the new list.
     */
     public static BinaryNodeJ<Integer> append(BinaryNodeJ<Integer> a, BinaryNodeJ<Integer> b) {
         // if either is null, return the other
         if (a==null) return b;
         if (b==null) return a;
         
         // find the last node in each using the .previous pointer
         BinaryNodeJ<Integer> aLast = a.left;
        BinaryNodeJ<Integer> bLast = b.right;
         
         // join the two together to make it connected and circular
         join(aLast, b);
         join(bLast, a);
         
         return(a);
     }
     
     /*
       --Recursion--
       Given an ordered binary tree, recursively change it into
       a circular doubly linked list which is returned.
     */
     public static BinaryNodeJ<Integer> treeToList(BinaryNodeJ<Integer> root) {
         // base case: empty tree -> empty list
         if (root==null) return null;
         
         // Recursively do the subtrees (leap of faith!)
         BinaryNodeJ<Integer> aList = treeToList(root.left);
         BinaryNodeJ<Integer> bList = treeToList(root.right);
         
         // Make the single root node into a list length-1
         // in preparation for the appending
         root.left = root;
         root.right = root;
         
         // At this point we have three lists, and it's
         // just a matter of appending them together
         // in the right order (aList, root, bList)
         aList = append(aList, root);
         aList = append(aList, bList);
         
         return aList;
     }
     
     /*
       Given a non-empty tree, insert a new node in the proper
       place. The tree must be non-empty because Java's lack
       of reference variables makes that case and this
       method messier than they should be.
     */
     public static void treeInsert(BinaryNodeJ<Integer> root, int newData) {
        if (newData<=root.data) {
            if (root.left!=null) treeInsert(root.left, newData);
             else root.left = new BinaryNodeJ<Integer>(newData);
         }
         else {
            if (root.right!=null) treeInsert(root.right, newData);
             else root.right = new BinaryNodeJ<Integer>(newData);
         }
     }
     
     // Do an inorder traversal to print a tree
     // Does not print the ending "\n"
     public static void printTree(BinaryNodeJ<Integer> root) {
         if (root==null) return;
         printTree(root.left);
         System.out.print(Integer.toString(root.data) + " ");
         printTree(root.right);
     }
     
     // Do a traversal of the list and print it out
     public static void printList(BinaryNodeJ<Integer> head) {
         BinaryNodeJ<Integer> current = head;
         
         while (current != null) {
             System.out.print(Integer.toString(current.data) + " ");
             current = current.right;
             if (current == head) break;
         }
         
         System.out.println();
     }
     
     // Demonstrate tree->list with the list 1..5
     public static void main(String[] args) {
     
         // first build the tree shown in the problem document
         // http://cslibrary.stanford.edu/109/
         BinaryNodeJ<Integer> root = new BinaryNodeJ<Integer>(4);
         treeInsert(root, 2);
         treeInsert(root, 1);
         treeInsert(root, 3);
         treeInsert(root, 5);
         
         System.out.println("tree:");
         printTree(root);   // 1 2 3 4 5
         System.out.println();
         
         System.out.println("list:");
         BinaryNodeJ<Integer> head = treeToList(root);
         printList(head);   // 1 2 3 4 5   yay!
     }
 }
 
