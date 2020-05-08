 package com.bytopia.oboobs.utils;
 
 import java.io.Serializable;
 
 public class Tuple <A extends Serializable, B extends Serializable> implements Serializable{
 	public A a;
 	public B b;
 	public Tuple(A a, B b) {
		super();
 		this.a = a;
 		this.b = b;
 	}
 }
