 // File LinkedDequeue.java 
 /** 
  *  This class represents a Queue datatype implemented using a singly-linked
  *  list with appropriate operations.
  *
  * @author: Henry Leitner
  * @version: Last modified on April 11, 2013
  * Implements a Queue as a linked-list
  */
 
 public class LinkedDequeue
 {
     private QueueNode tail;
     private QueueNode head;
     private int count;
     
     /**
      *  The QueueNode class is an inner class implemented to model a queue node;
      *  it can contain an Object type of data, and also holds the link to the
      *  next node in the queue.  If there are no other nodes, the link will be null.
      */
      class QueueNode        // an inner class
      {
 
         private Object item;
         private QueueNode link;
    }
 
     /**
      *  This constructor for the class will set up the needed instance variables
      *  which begin with no nodes present and thus are set to null.
      */
     public LinkedDequeue ()
     {
         tail = head = null;
         count = 0;
     }
 
     /**
      *  This method will construct a new QueueNode and add it onto the tail
      *  of the queue (standard FIFO behavior). If it is the first node added into
      *  the queue, both head and tail will reference it, otherwise it is added
      *  using the tail variable.  The node counter is also updated.
      *
      *  @param   x     The Object to be added as part of a new QueueNode
      */
     public void tailAdd (Object x)
     {
         QueueNode temp = new QueueNode();
         temp.item = x;
         temp.link = null;
 
         if (tail == null) head = tail = temp;
         else
         {
             tail.link = temp;
             tail = temp;
         }
         count++ ;
     }
 
     public Object tailPeek()
     {
         return tail.item;
     }
 
     public Object tailRemove()
     {
         if ( isEmpty() ) return null;
         else
         {
             Object tempItem = tail.item;
 
             QueueNode newTail = null;
             QueueNode next = head;
 
             while (next.link != null)
             {
                 newTail = next;
                 next = next.link;
             }
 
             tail = newTail;
             tail.link = null;
             if (tail == null) head = null;
             count --;
 
             return tempItem;
         }
     }
 
     public void headAdd (Object o)
     {
         QueueNode temp = new QueueNode();
         temp.item = o;
         temp.link = head;
 
         if (head == null) head = tail = temp;
         else
         {
             head = temp;
         }
         count++;
     }
 
     public Object headPeek()
     {
         return head.item;
     }
 
     /**
      *  This method will remove an item from the head of the queue.  
      *  In doing so, the queue variables are reset to detach the node,
      *  and the Object which it contains is then returned.  The queue
      *  counter is also updated to reflect the removal.
      *
      *  @return     The Object which was just removed from the queue.
      */
     public Object headRemove ()
     {
        if ( isEmpty() ) return null;
        else
        {
             Object tempItem = head.item;
             head = head.link;
             if (head == null)   tail = null;
             count -- ;
             return tempItem;
         }
     }
 
     /**
      *  This method will test for an empty queue and return a boolean result.
      *
      *  @return     true for an empty list; false if the queue contains QueueNodes.
      */
     public boolean isEmpty()
     {
         return ( count == 0 );
     }
 
     /**
      *  This method will evaluate and return the current size of the queue.
      *
      *  @return     An int describing the current number of nodes in the queue
      */
     public int size()
     {
         return count;
     }
 
     public String toString()
     {
         StringBuilder dequeueOut = new StringBuilder();
         String newline = System.getProperty("line.separator");
         QueueNode currentNode = head;
         do
         {
            dequeueOut.add(currentNode.item + newline);
             currentNode = currentNode.link;
         } while (currentNode.link != null);
         return dequeueOut;
     }
 
     public static void main(String[] args)
     {
 
     }
 }
