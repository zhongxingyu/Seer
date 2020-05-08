 package io.seq;
 
 import java.io.Serializable;
 
public class Sequence implements Serializable {
 
	public String header;
 	
	public String value;
 
 	@Override
 	public String toString() {
 		return String.format(">%s|%s", header, value);
 	}
 	 
 	
 }
