import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * @author Ian Hunt
  * @author Christoffer Rosen
  * @date 1/9/12
  *
  * High performance, thread safe, singly linked list of Strings.
  * The list comprises Nodes, and as few nodes as possible are locked by any thread so as to permit
  * the greatest possible concurrency.
  */
 
 //@ThreadSafe
 public class HPList {
     
     public static final String DUMMY_NODE_VALUE = "";
     
 
     class Node {
         final String value ;          // String in this node; a null string ("") is a dummy node/
         Node next ;                   // The node following this one.
         final Lock lock ;             // Guards this node ;
         // CONDITION PREDICATE: The next linked node is not current being accessed.
         final Condition nextChanged ; // Signaled when the next link for this node changes.
 
         Node(String value, Node next) {
             this.value = value ;
             this.next = next ;
             this.lock = new ReentrantLock() ;
             this.nextChanged = lock.newCondition() ;
         }
     }
 
     private Node head ;    // Head of list  - always points to something
 
     /*
     * An HP list always contains at least two nodes - a dummy head
     * and a dummy tail.
     */
     public HPList() {
         head = new Node(DUMMY_NODE_VALUE, new Node(DUMMY_NODE_VALUE, null)) ;
     }
 
     /*
     * Insert string s into the HPList. Simply returns if s is already
     * in the list, otherwise inserts it so as to keep the list in
     * ascending order of strings.
     */
     public void insert(String s) {
         Node current = head;
         
         // While we are not at the end of the linked list, which is shown by arriving at a 
         // dummy node value.
         while(current.next.value != DUMMY_NODE_VALUE) {
             boolean dupe = false;
             try {
                 current.lock.lock();
                 dupe = current.next.value.equals(s); 
                 if(current.next.value.compareTo(s) < 0 && !dupe) {
                     current.next = new Node(s, current.next);
                     current.nextChanged.signal();
                 } 
             } catch (Exception ex) {
                 ex.printStackTrace();
             } finally {             
                 current.lock.unlock();    
             }
             if(dupe) break;
             current = current.next;
         }
         
         //insert at end
         //todo: fix this copypaste code
         try {
             current.lock.lock();
             current.next = new Node(s, current.next);
             current.nextChanged.signalAll();
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             current.lock.unlock();
         }
     }
 
     /*
     * Returns true if String s is in the HPList, otherwise returns
     * false. If block is true, will wait until s is inserted and
     * unconditionally return true.
     */
     public boolean find(String s, boolean block) {
         
         Node current = head.next;
         boolean found = false;
         while(current.value != DUMMY_NODE_VALUE && !found) {
 
             try {
                 current.lock.lock();
                 
                while((s.compareTo(current.next.value) < 0 ||
                		current.next.value == DUMMY_NODE_VALUE) && block == true)
                 	current.nextChanged.wait();
                 
                 if(current.value.equals(s)) {
                     found = true;
                 }
                else if(s.compareTo(current.next.value) >0){
                	found = false;
                	break;
                }
                 
                 
             } catch (Exception ex) {
                 ex.printStackTrace();
             } finally {
                 current.lock.unlock();
             }
            current = current.next;
         }
 
         return found;
     }
     
     
 }
 
 


