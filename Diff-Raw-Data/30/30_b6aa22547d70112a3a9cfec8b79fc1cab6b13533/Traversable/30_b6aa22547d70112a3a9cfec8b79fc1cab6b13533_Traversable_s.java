 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: Traversable.java
  * Author: YoungHoon Jung <yj2244@columbia.edu>
  * Reviewer: Younghoon Jeon <yj2231@columbia.edu>
  * Description: Traversable
  */
 package edu.columbia.mipl.runtime.traverse;
 
 import java.io.*;
 import java.util.*;
 
 public class Traversable extends ArrayList<Traversable> implements Serializable {
 	public enum Method {
 		PRE,
 		IN,
 		POST,
 	};
 
 	boolean immediate;
 
 	public void traverse(Traverser traverser) {
 		traverse(traverser, false);
 	}
 
 	public void traverse(Traverser traverser, boolean immediate) {
 		traverse(traverser, Method.POST, immediate);
 	}
 
 	public void traverse(Traverser traverser, Method method, boolean immediate) {
 		this.immediate = immediate;
 
 		switch (method) {
 			case PRE:
 				preTraverse(traverser);
 				return;
 			case IN:
 				inTraverse(traverser);
 				return;
 			case POST:
 				postTraverse(traverser);
 				return;
 		}
 	}
 
 	public void preTraverse(Traverser traverser) {
 		preTraverseInternal(traverser);
 		if (!immediate)
 			traverser.finish();
 	}
 
 	void preTraverseInternal(Traverser traverser) {
 		int i;
 		traverser.reach(this);
 		for (i = size() - 1; i >= 0; i--) {
 			get(i).preTraverseInternal(traverser);
 		}
 	}
 
 
 	public void inTraverse(Traverser traverser) {
 		inTraverseInternal(traverser);
 		if (!immediate)
 			traverser.finish();
 	}
 
 	void inTraverseInternal(Traverser traverser) {
 		int i;
 		for (i = size() - 1; i >= 0; i--) {
 			get(i).inTraverseInternal(traverser);
 			if (i > 0)
 				traverser.reach(this);
 		}
 	}
 
 	public void postTraverse(Traverser traverser) {
 		postTraverseInternal(traverser);
 		if (!immediate) {
 			traverser.finish();
			new Exception().printStackTrace();
 		}
 	}
 
 	void postTraverseInternal(Traverser traverser) {
 		int i;
 		for (i = size() - 1; i >= 0; i--) {
 			get(i).postTraverseInternal(traverser);
 		}
 		traverser.reach(this);
 	}
 
 }
