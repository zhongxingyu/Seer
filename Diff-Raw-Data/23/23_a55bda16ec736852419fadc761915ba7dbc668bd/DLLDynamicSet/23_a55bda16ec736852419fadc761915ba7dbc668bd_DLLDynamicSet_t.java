 public class DLLDynamicSet implements DynamicSet, Const {
 
     private Node head;
     private int size;
 
     // Creates an instance of ADT DynamicSet and initializes it to the empty set.   
     public DLLDynamicSet() {
         this.head = new DLLNode(MIN_VALUE);
        this.head.setRight(this.head);
        this.head.setLeft(this.head);
         this.size = 0;
     }
 
     // Returns the number of elements currently in the set.
     public int size() {
         return this.size;
     }
 
     // Inserts element e in the set under key k.
     public void insert(KeyType k, Object o) {
         // Start at the head, moving right until we find our insert point.
         Node currentNode = this.head;
         // While the next node's key is less than k, move right.
         while (!currentNode.getRight().getKey().getValue().equals(MIN_VALUE) 
             && currentNode.getRight().getKey().compareTo(k) < 0) {
             currentNode = currentNode.getRight();
         }
         // Create a new node and set pointers.
        Node n = new DLLNode(k);
        n.setLeft(currentNode);
        n.setRight(currentNode.getRight());
        currentNode.getRight().setLeft(n);
        currentNode.setRight(n);
         this.size++;
     }
                                         
     // Given a key k, removes elements indexed by k from the set.
     public void delete(KeyType k) {
         // Start at the head, moving right until we find k.
         Node currentNode = this.head;
         // While the next node's key is not k, move right.
         while (currentNode.getRight().getKey().compareTo(k) != 0) {
             // If we make it back to the head, return.
             if (currentNode.getRight().getKey().getValue().equals(MIN_VALUE)) {
                 return;
             }
             currentNode = currentNode.getRight();
         }
         currentNode.getRight().setLeft(currentNode.getLeft());
         currentNode.getLeft().setRight(currentNode.getRight());
         currentNode.setLeft(null);
         currentNode.setRight(null);
         currentNode = null;
     }
                                                    
     // Finds an Object with key k and returns a pointer to it,
     // or null if not found. 
     public Object search(KeyType k) {
         // Start at the head, moving right until we find k.
         Node currentNode = this.head;
         // While the next node's key is not k, move right.
         while (currentNode.getRight().getKey().compareTo(k) != 0) {
             // If we make it back to the head, return.
             if (currentNode.getRight().getKey().getValue().equals(MIN_VALUE)) {
                 return null;
             }
             currentNode = currentNode.getRight();
         }
         return currentNode.getKey();
     }
                                                                    
     // The following operations apply when there is a total ordering on KeyType   
                                                                          
     // Finds an Object that has the smallest key, and returns a pointer to it,
     // or null if the set is empty. 
     public Object minimum() {
         return null;
     }
                                                                                          
     // Finds an Object that has the largest key, and returns a pointer to it,
     // or null if the set is empty.
     public Object maximum() {
         return null;
     }
                                                                                                          
     // Finds an Object that has the next larger key in the set above k, 
     // and returns a pointer to it, or null if k is the maximum element.
     public Object successor(KeyType k) {
         return null;
     }
 
     // Finds an Object that has the next smaller key in the set below k,
     // and returns a pointer to it, or null if k is the minimum element.
     public Object predecessor(KeyType k) {
         return null;
     }
 
     public String toString() {
         String s = "";
         Node currentNode = this.head;
         while (currentNode.getRight() != null && !currentNode.getRight().getKey().getValue().equals(MIN_VALUE)) {
             s += currentNode.getKey().getValue() + " ";
             currentNode = currentNode.getRight();
         }
         return s;
     }
 }
