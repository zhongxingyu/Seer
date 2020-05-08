 package com.mikea.concrete;
 
 import java.util.NoSuchElementException;
 
 import static com.mikea.concrete.Stack.newStack;
 import static com.mikea.concrete.Stacks.reverse;
 
 /**
  * Invariants:
  *   |head| <= C * |tail| + 1
  *   |tail| <= C * |head| + 1
  */
 public class AmortizedDeque<T> implements PDeque<T> {
   private static final int C = 2;
   final PStack<T> head;
   final PStack<T> tail;
 
   private AmortizedDeque(PStack<T> head, PStack<T> tail) {
     if (head.size() >= tail.size()) {
       this.head = head;
       this.tail = tail;
     } else {
       this.head = Stacks.pushAllToBack(head, reverse(tail));
       this.tail = newStack();
     }
   }
 
   private AmortizedDeque() {
     this(Stack.<T>newStack(), Stack.<T>newStack());
   }
 
   @Override
   public AmortizedDeque<T> pushFront(T value) {
     return new AmortizedDeque<T>(head.pushFront(value), tail);
   }
 
   @Override
   public AmortizedDeque<T> popBack() {
     if (!tail.isEmpty()) {
       return new AmortizedDeque<T>(head, tail.popFront());
     } else if (head.isEmpty()) {
       throw new NoSuchElementException();
     } else {
       return new AmortizedDeque<T>(Stacks.popBack(head), tail);
     }
   }
 
   @Override
   public T peekBack() {
     if (tail.isEmpty()) {
       return Stacks.peekBack(head);
     } else {
       return tail.peekFront();
     }
   }
 
   public static <T> AmortizedDeque<T> newAmortizedDeque() {
     return new AmortizedDeque<T>();
   }
 
   @Override
   public boolean isEmpty() {
     return head.isEmpty();
   }
 
   @Override
   public int size() {
     return head.size() + tail.size();
   }
 
   @Override
  public PCollection<T> clear() {
     return newAmortizedDeque();
   }
 
   /**
    * Push element to the back of the queue. Worst-case O(N), amortized O(1).
    */
   @Override
   public AmortizedDeque<T> pushBack(T t) {
     return new AmortizedDeque<T>(head, tail.pushFront(t));
   }
 
   /**
    * Pop element from the head of the queue. O(1).
    */
   @Override
   public AmortizedDeque<T> popFront() throws NoSuchElementException {
     if (isEmpty()) {
       throw new NoSuchElementException();
     }
     return new AmortizedDeque<T>(head.popFront(), tail);
   }
 
   /**
    * Peek at the head element. Null if empty. O(1).
    */
   @Override
   public T peekFront() {
     if (isEmpty()) {
       return null;
     }
     return head.peekFront();
   }
 }
