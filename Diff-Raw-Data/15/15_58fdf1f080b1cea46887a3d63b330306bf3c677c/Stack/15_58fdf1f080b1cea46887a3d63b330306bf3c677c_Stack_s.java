 package com.mikea.concrete;
 
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * Stack storage. Consumes O(N) space.
  */
 public class Stack<T> implements CCollection<T, Stack<T>>, Iterable<T> {
   // Null object to be used as terminator.
   private static Stack NULL_STACK = new Stack<Object>(null, null);
 
   private final T value;
   private final Stack<T> next;
 
   private Stack(T value, Stack<T> next) {
     this.value = value;
     this.next = next;
   }
 
   /**
    * Creates an empty stack.
    */
   @SuppressWarnings("unchecked")
   public Stack() {
     this(null, NULL_STACK);
   }
 
   /**
    * Checks if the stack is empty. O(1).
    */
   public boolean isEmpty() { return next == NULL_STACK; }
 
 
   /**
    * Pushes new value on the top of the stack. O(1).
    */
   public Stack<T> push(T value) {
     return new Stack<T>(value, this);
   }
 
   /**
    * Gets value from the top of the stack. O(1).
    */
   public T peek() {
     if (isEmpty()) {
       throw new NoSuchElementException("Empty stack");
     }
     return value;
   }
 
   /**
    * Pops(removes) value from the top of the stack. O(1).
    */
   public Stack<T> pop() {
     if (isEmpty()) {
       throw new NoSuchElementException("Empty stack");
     }
     return next;
   }
 
   public Iterator<T> iterator() {
     return new Iterator<T>() {
       Stack<T> pointer = Stack.this;
 
       public boolean hasNext() {
         return pointer.next != NULL_STACK;
       }
 
       public T next() {
         if (!hasNext()) {
           throw new NoSuchElementException();
         }
         T value = pointer.value;
         pointer = pointer.next;
         return value;
       }
 
       public void remove() {
         throw new UnsupportedOperationException("remove is not implemented in ");
       }
     };
   }
 
   /**
    * Reverses the stack. O(N).
    */
   public Stack<T> reverse() {
     Stack<T> result = new Stack<T>();
     for (T t : this) {
       result = result.push(t);
     }
     return result;
   }
 }
