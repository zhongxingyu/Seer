 package com.github.bcap.dht.message.request;
 
 import java.io.Serializable;
 
import com.github.bcap.dht.node.Contact;

 public class FindValueRequest extends IdentifierRequest implements Serializable {
 
 	private static final long serialVersionUID = IdentifierRequest.serialVersionUID + 1L;
 
	public FindValueRequest(Contact from, Contact to) {
 		super();
 	}
 
 }
