 package decentchat.internal;
 
 import java.util.Arrays;
 
 import decentchat.internal.nodes.Node;
 
 public class NodeKey implements Comparable<NodeKey> {
 
	public static final NodeKey MIN_KEY = new NodeKey(Hasher.generateHash(Byte.MIN_VALUE));
	public static final NodeKey MAX_KEY = new NodeKey(Hasher.generateHash(Byte.MAX_VALUE));
 	
 	private byte[] hash;
 	
 	public NodeKey(byte[] hash) {
 		this.hash = hash;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(hash);
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		NodeKey other = (NodeKey) obj;
 		if (!Arrays.equals(hash, other.hash))
 			return false;
 		return true;
 	}
 
 	public NodeKey inc(long d) {
 		byte[] next = Arrays.copyOf(hash, hash.length);
 	    byte carry = 0;
 	    byte ptr = 0;
 	    while (d > 0 || carry > 0) {
 	        if (ptr == Hasher.HASH_LENGTH_IN_BYTES)
 	            break;
 	        short val = (short) (next[ptr] + (d & 0xFF) + carry);
 	        if (val > 255) {
 	            carry = 1;
 	            next[ptr] = (byte) (val & 0xFF);
 	        } else {
 	            carry = 0;
 	            next[ptr] = (byte) val;
 	        }
 	        d = d >> 8;
 	        ++ptr;
 	    }
 		return new NodeKey(next);
 	}
 
 	public boolean isWithin(NodeKey first, NodeKey last) {
 		if (first.compareTo(last) > 0) {
 			NodeKey zero = MIN_KEY;
 			NodeKey max = MAX_KEY;
 			return (first.compareTo(this) <= 0 && this.compareTo(max) <= 0)
 					|| (zero.compareTo(this) <= 0 && this.compareTo(last) <= 0);
 		} else {
 			return first.compareTo(this) <= 0 && this.compareTo(last) <= 0;
 		}
 	}
 
 	@Override
 	public int compareTo(NodeKey o) {
 	    for (int i = hash.length-1; i >= 0; --i) {
 	        if (this.hash[i] > o.hash[i]) return 1;
 	        else if (this.hash[i] < o.hash[i]) return -1;
 	    }
 	    return 0;
 	}
 
 }
