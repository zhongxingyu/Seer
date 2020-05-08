 package edu.msergey.jalg.exercises.ch4.ex23;
 
 public class TwoWaysStack<E> {
    private E[] elements;
     private final int maxCount;
     private int countFromStart;
     private int countFromEnd;
 
     public TwoWaysStack(int maxCount) {
         if (maxCount <= 0) throw new IllegalArgumentException("maxCount should be positive");
 
         this.maxCount = maxCount;
         this.countFromStart = 0;
         this.countFromEnd = 0;
        elements = createArrayFromGenericType(maxCount);
     }
 
     public void pushToStart(E item) {
         if (item == null) throw new IllegalArgumentException("item can't be null");
         if (this.countFromStart + this.countFromEnd >= this.maxCount)
             throw new IllegalStateException("no more free elements");
 
         elements[countFromStart] = item;
         countFromStart++;
     }
 
     public void pushToEnd(E item) {
         if (item == null) throw new IllegalArgumentException("item can't be null");
         if (this.countFromStart + this.countFromEnd >= this.maxCount)
             throw new IllegalStateException("no more free elements");
 
         elements[maxCount - countFromEnd - 1] = item;
         countFromEnd++;
     }
 
     public E popFromStart() {
         if (this.countFromStart == 0) throw new IllegalStateException("no elements at start");
 
         countFromStart--;
        return elements[countFromStart];
     }
 
     public E popFromEnd() {
         if (this.countFromEnd == 0) throw new IllegalStateException("no elements at end");
 
         countFromEnd--;
        return elements[maxCount - countFromEnd - 1];
    }

    @SuppressWarnings({"unchecked"})
    private E[] createArrayFromGenericType(int size) {
        return (E[])new Object[size];
     }
 }
