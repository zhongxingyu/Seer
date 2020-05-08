 package com.crypticbit.javelin.js;
 
 import java.io.Serializable;
 
 import com.crypticbit.javelin.store.Digest;
 import com.crypticbit.javelin.store.Identity;
 import com.crypticbit.javelin.store.StoreException;
 import com.crypticbit.javelin.store.kas.KeyAddressableStorage;
 
 /**
  * The "anchor" represents any branch, including head. After every write the anchor is updated with the reference to the
  * head of the write. It's expected that one node will be hard-coded (set manually) which will represent the head or
  * root node, and all others will be referenced from within that data structure.
  */
 
 public class Anchor implements Serializable {
 
     private Identity address;
     private transient Identity destination;
 
     public Anchor(Identity address) {
 	this.address = address;
     }
 
     Anchor() {
 	address = new Digest();
     }
 
     Anchor(KeyAddressableStorage kas, Anchor clone) throws StoreException {
 	this();
 	write(kas, clone.read(kas));
     }
 
     /** Same as read() but returns cached/last value */
     public Identity getDestination() {
 	return destination;
     }
 
     public Identity getAddress() {
 	return address;
     }
 
     public Identity read(KeyAddressableStorage kas) throws StoreException {
 	if (kas.check(address)) {
 	    destination = new Digest(kas.get(address).getBytes());
 	    return destination;
 	}
 	else {
	    // FIXME
	    throw new Error("Address " + address + " is null.");
 	}
     }
 
     public void write(KeyAddressableStorage kas, Identity newReference) throws StoreException {
 	kas.store(address, destination, newReference);
 	destination = newReference;
     }
 
 }
