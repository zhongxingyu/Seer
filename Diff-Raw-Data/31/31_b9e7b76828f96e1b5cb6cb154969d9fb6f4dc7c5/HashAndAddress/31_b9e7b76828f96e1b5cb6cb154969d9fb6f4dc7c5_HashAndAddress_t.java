 package org.blockout.network.reworked;
 
 import java.net.SocketAddress;
 
 import org.blockout.network.dht.IHash;
 
 import com.google.common.base.Preconditions;
 
 public class HashAndAddress implements IHash {
 
 	private final IHash			hash;
 	private final SocketAddress	address;
 
 	public HashAndAddress(final IHash hash, final SocketAddress address) {
 
 		Preconditions.checkNotNull( hash );
 		Preconditions.checkNotNull( address );
 
 		this.hash = hash;
 		this.address = address;
 	}
 
 	public IHash getHash() {
 		return hash;
 	}
 
 	public SocketAddress getAddress() {
 		return address;
 	}
 
 	@Override
 	public int compareTo( final IHash o ) {
 		return hash.compareTo( o );
 	}
 
 	@Override
 	public String getValue() {
 		return hash.getValue();
 	}
 
 	@Override
 	public IHash getNext() {
 		return hash.getNext();
 	}
 
 	@Override
 	public IHash getPrevious() {
 		return hash.getPrevious();
 	}
 
 	@Override
 	public int getM() {
 		return hash.getM();
 	}

	@Override
	public String toString() {
		return "HashAndAddress[hash=" + getHash() + ", address=" + getAddress() + "]";
	}
 }
