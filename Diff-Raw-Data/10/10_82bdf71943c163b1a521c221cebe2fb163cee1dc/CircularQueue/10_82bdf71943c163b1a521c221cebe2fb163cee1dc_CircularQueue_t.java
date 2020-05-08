 package com.matthieu.epi;
 
 import java.util.Arrays;
 
 public class CircularQueue implements Solution {
 
     @SuppressWarnings("unchecked")
     public static class MyQueue<T> {
         T array[];
         int start=0, end=0;
         public MyQueue(int capacity) {
             array = (T[]) new Object[capacity];
         }
 
         private void resize() {
             T new_array[] = (T[]) new Object[array.length*2];
             for (int i=start; i!=end; i = (i+1)%array.length) {
                 new_array[(i-start)%array.length] = array[i];
             }
             int current_size = size();
             array = new_array;
             start=0;
             end=current_size;
         }
 
         public int size() {
            int res = (end-start) % array.length;
            if (res < 0) res+=array.length;
            return res;
         }
 
         public void push(T obj) {
             int new_end = (end+1) % array.length;
             if (new_end == start)
                 resize();
 
             array[end] = obj;
             end = (end+1) % array.length;
         }
 
         public T poll() {
             if (size() == 0)
                 return null;
             T res = array[start];
             array[start] = null; // not doing this may cause memory leak (keeping reference to an object that may not be used anymore)
             start = (start+1) % array.length;
             return res;
         }
 
         public String toString() {
             return Arrays.toString(array)+" with start at "+start+" and end at "+end;
         }
     }
     @Override
     public void solveProblem() {
         MyQueue<Integer> queue = new MyQueue<Integer>(3);
 
        for (int i=0; i<4; i++) {
             int new_number = (int) (Math.random()*10);
             queue.push(new_number);
             System.out.println("pushed "+new_number+", state is "+queue);
         }
        for (int i=0; i<10; i++) {
             if (Math.random() < 0.5) {
                 System.out.println("Polling "+queue.poll()+", state is "+queue);
             } else {
                 int new_number = (int) (Math.random()*10);
                 queue.push(new_number);
                 System.out.println("pushed "+new_number+", state is "+queue);
             }
         }
     }
 }
