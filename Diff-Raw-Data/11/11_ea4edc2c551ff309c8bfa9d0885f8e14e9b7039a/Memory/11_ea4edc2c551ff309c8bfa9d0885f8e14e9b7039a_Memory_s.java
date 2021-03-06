 package com.bridj;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 
 /**
  *
  * @author Olivier
  */
 class Memory<T> extends DefaultPointer<T> {
     protected final long validStart, validSize;
 
     Memory(PointerIO<T> io, long peer, long validStart, long validSize, Object memoryOwner) {
         super(io, peer);
         this.validStart = validStart;
         this.validSize = validSize;
         this.memoryOwner = memoryOwner;
         //assert (getPeer() % 8) == 0; 
     }
     Memory(PointerIO<T> io, long peer, long validSize, Object memoryOwner) {
 		this(io, peer, peer, validSize, memoryOwner);
 	}
 	Memory(long size) {
         this(null, size);
     }
 	/*public Memory(PointerIO<T> io, Buffer directBuffer, long byteOffset) {
 		this(io, JNI.getDirectBufferAddress(directBuffer) + byteOffset, JNI.getDirectBufferCapacity(directBuffer), directBuffer);
 		assert directBuffer != null && directBuffer.isDirect();
 	}*/
     Memory(PointerIO<T> io, Memory<?> memoryOwner) {
         this(io, memoryOwner.getPeer(), memoryOwner.getValidStart(), memoryOwner.getValidStart(), memoryOwner);
     }
     Memory(PointerIO<T> io, long peer, long validSize) {
         this(io, peer, peer, validSize, null);
     }
     Memory(PointerIO<T> io, long size) {
         this(io, JNI.malloc(size), size);
         JNI.memset(getPeer(), (byte)0, size);
         
         //Exception ex = new RuntimeException();
         //ex.getStackTrace();
         //mallocTraces.put(getPeer(), ex);
     }
     //static Map<Long, Exception> mallocTraces = new HashMap<Long, Exception>();
     
     
     public long getValidSize() {
         return validSize;
     }
     public long getValidStart() {
         return validStart;
     }
     
 
     @Override
     protected long getCheckedPeer(long byteOffset, long validityCheckLength) {
 		long peer = super.getCheckedPeer(byteOffset, validityCheckLength);
         if (validSize < 0)
             return peer;
         if (peer < validStart || peer + validityCheckLength > validStart + validSize)
             throw new IndexOutOfBoundsException("Cannot access to memory data of length " + validityCheckLength + " at offset " + byteOffset + " : valid memory start is " + validStart + ", valid memory size is " + validSize);
         return peer;
     }
     
     //Set<Long> freed = new HashSet<Long>();
 
 	@Override
     protected void free(long peer) {
 		assert peer != 0;
         if (peer == 0)
 			return;
 		
 		/*
         if (!freed.add(peer)) {
         	Exception ex = mallocTraces.get(peer);
         	if (ex != null)
         		BridJ.log(Level.SEVERE, "Re-freeing memory !", ex);
         	else
         		BridJ.log(Level.SEVERE, "Freeing memory that wasn't malloced here !");
         }*/
         
        BridJ.log(Level.SEVERE, "Leaking memory at address " + peer + " to avoid the free() crash.");
        //JNI.free(peer);
     }
     
     @Override
     protected void finalize() throws Throwable {
         deallocate();
     }
 
     @Override
     public synchronized void release() {
         if (memoryOwner != null) {
             if (memoryOwner instanceof Pointer<?>)
                 ((Pointer<?>)memoryOwner).release();
             memoryOwner = null;
         } else
             deallocate();
     }
 
     /// TODO merge with DefaultPointer.share
 	@Override
     public Pointer<T> offset(long byteOffset) {
         PointerIO<T> io = getIO();
         int size = io != null ? io.getTargetSize() : 1;
         Memory<T> p = new Memory<T>(io, getCheckedPeer(byteOffset, size), validStart + byteOffset, validSize, memoryOwner == null ? this : memoryOwner);
 		p.memoryOwner = memoryOwner;
 		p.peerOrOffsetInOwner += byteOffset;
         return p;
     }
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public Pointer<Pointer<T>> getReference() {
 		if (memoryOwner != null)
 			return ((Pointer<Pointer<T>>)memoryOwner).offset(peerOrOffsetInOwner);
 		else
 			return super.getReference();
 	}
 
 }
