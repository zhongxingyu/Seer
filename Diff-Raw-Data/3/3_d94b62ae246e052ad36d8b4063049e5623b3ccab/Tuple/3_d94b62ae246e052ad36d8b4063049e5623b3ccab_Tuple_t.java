 package com.bytopia.oboobs.utils;
 
 import java.io.Serializable;
 
 public class Tuple <A extends Serializable, B extends Serializable> implements Serializable{
	private static final long serialVersionUID = -6634496888521867981L;
 	public A a;
 	public B b;
 	public Tuple(A a, B b) {
 		this.a = a;
 		this.b = b;
 	}
 }
