 package com.blogspot.vikkyrk;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 public class mainTest {
 	public static void main(String[] args) {
 	    //myRun mRun = new ReverseLL();
 	    //myRun mRun = new LLPalindrome();
 		//myRun mRun = new LLSort();
 		//mRun.run();
 		//LLRun();
 		//stackRun();
 		//myPowersOfHanoiRecursion(4);
 		//nQueensImpl.printList(nQueensImpl.nQueensSolve(1));
 		//System.out.println(myPower(2,11));
 		BSTRun();
 	}
 	
 	public static void stackRun() {
 		myStack<Integer> mStack = new myArrayStack<Integer>(10);
 		mStack.push(1);
 		mStack.push(2);
 		mStack.push(3);
 		System.out.println(mStack.toString());
 		mStack.pop();
 		mStack.pop();
 		System.out.println(mStack.toString());
 		System.out.println(mStack.pop());
 		System.out.println(mStack);
 		
 		myListImpl<Integer> ll = new myListImpl<Integer>();
 		ll.add(1);ll.add(2);;ll.add(3);ll.add(4);
 		ll.printList();
 		while(ll.size() != 0) {
 			mStack.push(ll.remove(ll.size()-1));
 		}
 		System.out.println(mStack.toString());
 		while(!mStack.isEmpty()) {
 			ll.add(mStack.pop());
 		}
 		ll.printList();
 		
 	}
 	public static void LLRun() {
 		myListImpl<Integer> ml = new myListImpl<Integer>();
 		ml.printList();
 		ml.add(2);
 		ml.printList();
 		ml.add(4);
 		ml.printList();
 		ml.add(3,1);
 		ml.printList();
 		ml.add(4,0);
 		ml.printList();
 		ml.set(5,0);
 		ml.printList();
 		ml.set(3, 0);
 		ml.printList();
 		ml.set(6,3);
 		ml.printList();
 		System.out.println(ml.get(0));
 		System.out.println(ml.get(ml.size()-1));
 		
 		Iterator<Integer> iter = ml.iterator();
 		
 		while(iter.hasNext()) {
 			System.out.println(iter.next());
 		}
 		
 		ml.remove(0);
 		ml.remove(0);
 		ml.remove(0);
 		ml.remove(0);
 		ml.printList();
 	}
 	
 	public static void BSTRun() {
 		BSTree<Integer> mTree = new BSTree<Integer>();
	/*	mTree.insert(6);
 		mTree.insert(5);
 		mTree.insert(8);
 		mTree.insert(4);
 		mTree.insert(1);
 		mTree.insert(2);
 		mTree.insert(7);
 		mTree.insert(9);
 		mTree.insert(3);
	*/	
 		mTree.recursiveInsert(6);
 		mTree.recursiveInsert(5);
 		mTree.recursiveInsert(8);
 		mTree.recursiveInsert(4);
 		mTree.recursiveInsert(1);
 		mTree.recursiveInsert(2);
 		mTree.recursiveInsert(7);
 		mTree.recursiveInsert(9);
 		mTree.recursiveInsert(3);
		
 		mTree.preOrderTraversal();
 		mTree.inOrderTraversal();
 	}
 	
 	public static void myPowersOfHanoiRecursion(int i) {
 		if(i > 0) {
 			myPowersOfHanoiRecursion(i-1);
 			System.out.print(i+ " ");
 			myPowersOfHanoiRecursion(i-1);
 		}
 	}
 	
 	public static int myPower(int x, int n) {
 		if(n == 0) 
 			return 1;
 		if(n == 1)
 			return x;
 		
 		int temp = myPower(x,n/2);
 		if(n%2 == 0) 
 			return temp*temp;
 		else
 			return x*temp*temp;
 	}
 	
 	public static int myBinarySearch(int [] arr, int k) {
 		
 		int left = 0, right = arr.length-1;
 		int mid;
 		while(left <= right) {
 			mid = left + (right - left)/2;
 			if(arr[mid] == k)
 				return mid-1;
 			else if(arr[mid] > k) {
 				right = mid+1;
 			}
 			else
 				left = mid;
 		}
 		return -1;
 	}
 }
