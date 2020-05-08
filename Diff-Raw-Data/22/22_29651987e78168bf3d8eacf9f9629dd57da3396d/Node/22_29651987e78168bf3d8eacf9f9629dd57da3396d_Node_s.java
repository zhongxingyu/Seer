 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package da.alg1000.oblig3;
 

 /**
  *
  * @author Martin
  */
 class Node<T> {
 
     private T element;
     private Node next;
 
     public Node(T e, Node n) {
         element = e;
         next = n;
     }
 
     public T getElement() {
         return element;
     }
 
     public Node getNext() {
         return next;
     }
     public void setElement(T element) {
        this.element=element;
     }
 
     public void setNext(Node next) {
        this.next=next;
     }
    public boolean hasNext(){
        return next!=null;
     }
     @Override
    public String toString(){
        return getElement()+"";
     }
 } //class Node
 
