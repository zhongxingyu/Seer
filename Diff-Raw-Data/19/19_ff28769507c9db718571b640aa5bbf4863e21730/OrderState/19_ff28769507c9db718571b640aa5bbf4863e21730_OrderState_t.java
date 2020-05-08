 package de.tud.in.middleware.order;
 
 import java.io.Serializable;
 
 public enum OrderState implements Serializable {
	INIT,
 	OPEN,
 	DISPATCHED,
 	DELIVERED;
 }
