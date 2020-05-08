 public class SMap extends SinglyLinkedList implements SimpleMap {
 
     public void put(int key, String name) {
        if(retrieve(key) == null) {
            super.add(new Node(new KeyValuePair(key,name)));
        }
     }
 
     public String get(int key) {
         Node node = retrieve(key);
         if(node == null) {
            return null;
         }
         KeyValuePair kvp = (KeyValuePair) node.getValue();
         return kvp.getName();
     }
 
     public void remove(int key) {
         Node node = retrieve(key);
        if(node != null) {
            super.delete(node);
        }
     }
 
     public boolean isEmpty() {
         return super.size() == 0;
     }
 
     private Node retrieve(int key) {
         Node head = getHead();
	KeyValuePair kvp = null;
        if(head != null) {
            kvp = (KeyValuePair) head.getValue();
        }
         while(head != null && kvp.getKey() != key) {
             head = head.getNext();
             if(head != null) {
                 kvp = (KeyValuePair) head.getValue();
             }
         }
         return head;
     }
 }
