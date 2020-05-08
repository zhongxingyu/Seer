 /*
  * Copyright (c) 2011-2012 Vikram Sundar.
  * All Rights Reserved.
  */
 package org.vikramdock;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import java.lang.*;
 import java.lang.reflect.*;
 
 class ImmutableArrayList {
 	ArrayList al;
 	public ImmutableArrayList(ArrayList al) {
 		this.al = al;
 	}
 	public ImmutableArrayList(ImmutableArrayList clone) {
		al = new ArrayList();
		for (int i = 0; i < clone.size(); i++) {
			Atom next = (Atom)clone.get(i);
			Atom clonea = new Atom(next);
			al.add(clonea);
 		}
 	}
 	public Object get(int index) {
 		return al.get(index);
 	}
 	public int size() {
 		return al.size();
 	}
 	public ArrayList getAL() {
 		return al;
 	}
 }
