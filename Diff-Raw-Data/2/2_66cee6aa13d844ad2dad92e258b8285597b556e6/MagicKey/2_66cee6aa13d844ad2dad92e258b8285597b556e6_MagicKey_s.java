 package com.yahoo.ycsb.db;
 
 import java.io.Serializable;
 
 import org.infinispan.remoting.transport.Address;
 
 import com.yahoo.ycsb.Client;
 
 
 public class MagicKey implements Serializable {
 
     private static final long serialVersionUID = -1072474466685642719L;
     public static int CLIENTS;
     public static int NUMBER;
     public static Address ADDRESS;
     public static CustomHashing HASH;
     public static int OWNERS;
     
     public final String key;
     public final int num;
     public final int node;
     
     public static int local = 0;
     public static int remote = 0;
     
     public MagicKey(String key, int num) {
 	this.key = key;
 	this.num = num;
 	this.node = (int) Math.floor((double)((num * CLIENTS) / NUMBER));
 	
 	if (Client.NODE_INDEX == this.node) {
 	    local++;
 	} else {
 	    remote++;
 	}
     }
     
     public void locationCheck() {
         if (!HASH.isKeyLocalToAddress(ADDRESS, this, OWNERS)) {
             System.out.println("here!");
         }
         boolean found = false;
         for (Address a : HASH.locate(this, OWNERS)) {
             if (a.equals(ADDRESS)) {
                 found = true;
             }
         }
         if (!found) {
             System.out.println("omg!");
         }
         if (!ADDRESS.equals(HASH.primaryLocation(this))) {
             System.out.println("HERE!");
         }
     }
     
     @Override
     public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || !getClass().equals(o.getClass())) return false;
 
        MagicKey other = (MagicKey) o;
 
        if (this.hashCode() != other.hashCode()) return false;
       return this.key == other.key && this.node == other.node && this.num == other.num;
     }
     
     public int hashCode() {
 	return num;
     }
     
     @Override
     public String toString() {
         return this.node + " owns " + this.key; 
     }
 }
