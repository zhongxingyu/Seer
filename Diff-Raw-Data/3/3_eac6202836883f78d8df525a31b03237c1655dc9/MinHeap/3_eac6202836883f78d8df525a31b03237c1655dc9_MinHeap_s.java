 package com.hceris.datastructures;
 import java.util.NoSuchElementException;
 
 public class MinHeap<T extends Comparable<? super T>> {
     private T[] a;
     private int size;
 
     @SuppressWarnings("unchecked")
 	public MinHeap(int maxSize) {
         a = (T[]) new Comparable[maxSize];
     }
 
     public int size() { return size; }
 
     public T peek() {
         checkEmpty();
         return a[0];
     }
 
     public T poll() {
         checkEmpty();
         T value = a[0];
         swap(a, 0, size - 1);
        heapify(0);
         size--;
         return value;            
     }
 
     public void offer(T e) {
         checkSize();
         a[size++] = e;
         int i = size - 1;
 
         while(parent(i) >= 0 && a[parent(i)].compareTo(a[i]) > 0) {
             swap(a, i, parent(i));
             i = parent(i);
         }
     }
 
     private void checkSize() {
 		if(size == a.length) {
 			throw new IllegalStateException("heap is full");
 		}		
 	}
 
 	private void checkEmpty() {
         if(size == 0) {
             throw new NoSuchElementException();
         }
     }
 
     private void swap(T[] a, int i, int j) {
         T tmp = a[i];
         a[i] = a[j];
         a[j] = tmp;
     }
 
     private int parent(int i) { return (i - 1) / 2; }
 
     private int left(int i) { return (i *  2) + 1; }
 
     private int right(int i) { return (i *  2) + 2; }
 
     private void heapify(int i) {
         if(left(i) >= size) { return; }
 
         int min = i;
         if(a[i].compareTo(a[left(i)]) > 0) {
             min = left(i);
         }
 
         if(right(i) < size && a[min].compareTo(a[right(i)]) > 0) {
             min = right(i);
         }
 
         if(i != min) {
             swap(a, i, min);
             heapify(min);
         }
     }    
 }
