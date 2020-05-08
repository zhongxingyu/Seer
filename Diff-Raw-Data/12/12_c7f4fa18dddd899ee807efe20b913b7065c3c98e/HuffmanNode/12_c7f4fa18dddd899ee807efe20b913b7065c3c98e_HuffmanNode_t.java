 package com.algorithms.huffman;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
  * Class allows modification of all information about nodes
  */
 public class HuffmanNode<T> implements Comparable {
 	
 		private T data;
 		private double frequency;
 		public boolean visited;
 		private int height;
 		private HuffmanNode<T> parent;
 		private List<HuffmanNode<T>> children;
 		public HuffmanNode<T> rightChild, leftChild;
 		
 		public void setData(T data) {
 			this.data = data;
 		}
 		
 		public void setFreq(double freq) {
 			this.frequency = freq;
 		}
 		
 		public T getData() {
 			return this.data;
 		}
 		
 		public double getFreq() {
 			return this.frequency;
 		}
 		
 		public int getHeight() {
 			return this.height;
 		}
 		
 		public void setHeight(int height) {
 			this.height = height;
 		}
 		
 		public void setParent(HuffmanNode<T> parent) {
 			this.parent = parent;
 		}
 		
 		public void setVisited(boolean visited) {
 			this.visited = visited;
 		}
 		
 		public void removeChildren() {
 			this.rightChild = null;
 			this.leftChild = null;
 		}
 		
 		public int compareTo(Object other) {
 			if (other instanceof HuffmanNode) {
				return (int) (this.frequency - ((HuffmanNode<T>) other).getFreq());
 			} else {
 				return 0;
 			}
 		}
 	
 }
