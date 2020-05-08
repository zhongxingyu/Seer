 package com.psddev.dari.util;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
import java.util.NoSuchElementException;
 
 public class ThreadLocalStack<T> {
 
     private final ThreadLocal<Deque<T>> stackLocal = new ThreadLocal<Deque<T>>();
 
     public T push(T object) {
         Deque<T> stack = stackLocal.get();
         T current;
 
         if (stack == null) {
             stack = new ArrayDeque<T>();
             stackLocal.set(stack);
             current = null;
 
         } else {
             current = stack.peekFirst();
         }
 
         stack.addFirst(object);
 
         return current;
     }
 
     public T get() {
         Deque<T> stack = stackLocal.get();
 
         return stack != null ? stack.peekFirst() : null;
     }
 
    private T popReally(Deque<T> stack) {
        T popped = stack.pollFirst();

        if (stack.isEmpty()) {
            stackLocal.remove();
        }

        return popped;
    }

     public T pop() {
         Deque<T> stack = stackLocal.get();
 
         if (stack == null || stack.isEmpty()) {
             return null;
 
         } else {
            return popReally(stack);
        }
    }

    public T popOrError() {
        Deque<T> stack = stackLocal.get();

        if (stack == null || stack.isEmpty()) {
            throw new NoSuchElementException();

        } else {
            return popReally(stack);
         }
     }
 
     public void set(T value) {
         Deque<T> stack = new ArrayDeque<T>();
 
         stack.addFirst(value);
         stackLocal.set(stack);
     }
 
     public void remove() {
         stackLocal.remove();
     }
 }
