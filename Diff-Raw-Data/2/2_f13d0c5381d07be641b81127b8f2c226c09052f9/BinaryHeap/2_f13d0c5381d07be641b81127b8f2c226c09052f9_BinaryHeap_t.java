 package datastructures;
 
 public class BinaryHeap {
    private DynamicArray array;
 
     public BinaryHeap() {
         array = new DynamicArray(8);
     }
 
     public void enqueue(int x) {
         enqueueToNode(1, x);
     }
 
     private void enqueueToNode(int indexInArray, int value) {
         Integer nodeValue = array.getObject(indexInArray);
         if(nodeValue == null){
             array.put(indexInArray, value);
         } else if(value < nodeValue) {
             enqueueToLeftNodeOf(indexInArray, value);
         } else {
             enqueueToRightNodeOf(indexInArray, value);
         }
     }
 
     private void enqueueToLeftNodeOf(int indexOfHead, int value) {
         enqueueToNode(getIndexOfLeft(indexOfHead), value);
     }
 
     private void enqueueToRightNodeOf(int indexOfHead, int value) {
         enqueueToNode(getIndexOfRight(indexOfHead), value);
     }
 
     public int dequeue() {
         return dequeueMinOfNode(1);
     }
 
     private int dequeueMinOfNode(int indexInArray) {
         int indexOfLeftNode = getIndexOfLeft(indexInArray);
         if(array.getObject(indexOfLeftNode) == null) {
             return removeNodeAt(indexInArray);
         } else {
             return dequeueMinOfNode(indexOfLeftNode);
         }
     }
 
     private Integer removeNodeAt(int indexInArray) {
         Integer nodeValue = array.getObject(indexInArray);
         Integer indexOfRightNode = getIndexOfRight(indexInArray);
         Integer valueOnRight = array.getObject(indexOfRightNode);
         if(valueOnRight == null) {
             array.put(indexInArray, null);
         } else {
             array.put(indexInArray, dequeueMinOfNode(indexOfRightNode));
         }
         return nodeValue;
     }
 
     private int getIndexOfLeft(int head) {
         return 2*head;
     }
 
     private int getIndexOfRight(int head) {
         return 2*head + 1;
     }
 
     public boolean isEmpty() {
         return array.getObject(1) == null;
     }
 
 }
