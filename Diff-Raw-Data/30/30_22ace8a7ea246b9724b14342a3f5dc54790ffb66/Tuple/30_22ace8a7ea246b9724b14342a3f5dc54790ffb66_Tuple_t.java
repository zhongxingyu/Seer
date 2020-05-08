 package com.szas.server.gwt.client;
 
 import java.util.Random;
 
 public abstract class Tuple {
 	private static final Random random = new Random();
	protected int id;
 	
 	public Tuple() {
 		id = random.nextInt();
 	}
 
 	public int getId() {
 		return id;
 	}
 }
