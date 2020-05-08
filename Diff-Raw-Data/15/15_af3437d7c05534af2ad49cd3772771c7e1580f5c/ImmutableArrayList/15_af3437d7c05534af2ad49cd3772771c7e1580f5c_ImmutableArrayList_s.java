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
		ArrayList cloneal = clone.getAL();
		for (int i = 0; i < cloneal.size(); i++) {
			al.add(new Atom((Atom)cloneal.get(i)));
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
