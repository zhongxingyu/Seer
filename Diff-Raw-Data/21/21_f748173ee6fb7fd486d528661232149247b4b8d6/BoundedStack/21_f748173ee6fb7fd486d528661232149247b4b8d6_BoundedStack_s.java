 public class BoundedStack implements Stack {
     private int size = 0;
     private int capacity;
     private int [] elements;
 
     public BoundedStack(int capacity) {
         this.capacity = capacity;
         elements = new int [capacity];
     }
 
     public Boolean isEmpty() {
         return size == 0;
     }
 
     public Integer getSize() {
         return size;
     }
 
     public void push(int element) {
         if(size == capacity)
             throw new Overflow();
         this.elements[size++] = element;
     }
 
     public int pop() {
         if(size == 0)
             throw new Underflow();
         return elements[--size];
     }
 
     public Integer top() {
         return elements[size - 1];
     }
 
     public static Stack make(int capacity) {
         if(capacity < 0)
             throw new IllegalCapacity();
         if(capacity == 0)
             return new ZeroCapacityStack();
 
         return new BoundedStack(capacity);
     }
 
     public static class Overflow extends RuntimeException {
     }
 
     public static class IllegalCapacity extends RuntimeException {
     }
 
     public static class Underflow extends RuntimeException {
     }
 
     private static class ZeroCapacityStack implements Stack {
         public Boolean isEmpty() {
             return true;
         }
 
         public Integer getSize() {
             return 0;
         }
 
         public void push(int element) {
             throw new Overflow();
         }
 
         public int pop() {
             throw new Underflow();
         }
 
         public Integer top() {
             return null;
         }
     }
 }
