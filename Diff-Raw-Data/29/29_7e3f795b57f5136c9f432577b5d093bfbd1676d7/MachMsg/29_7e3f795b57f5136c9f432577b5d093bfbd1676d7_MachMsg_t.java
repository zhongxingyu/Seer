 package org.gnu.mach;
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.nio.ByteOrder;
 import java.nio.ByteBuffer;
 
 /**
  * Safe Mach message buffer.
  *
  * <h3>Embedded port names and {@link MachPort} objects</h3>
  *
 * Mach messages store port names in their header and in some of the
  * subsequent data items. To avoid port rights from being accidentally
  * deallocated or leaked, we must handle them carefully.
  *
  * When a {@link MachMsg} object is initially created, or after it has been
  * cleared, the header's port names are initialized to {@code MACH_PORT_NULL}
  * and no correponding {@link MachMsg} object exists. When port names are
  * added to the message contents through the {@link #setRemotePort} and
  * {@link #setLocalPort} methods or through one of the {@code put}
  * operations, on of two things can happen depending on the associated
  * {@link MachMsgType} used.
  *
  * If the port type is {@link MachMsgType#COPY_SEND}, {@link
  * MachMsgType#MAKE_SEND} or {@link MachMsgType#MAKE_SEND_ONCE}), the port
  * name is acquired using {@link MachPort#name()} and is kept as long as the
  * buffer contains the port name in question.
  *
  * If the port type is {@link MachMsgType#MOVE_RECEIVE}, {@link
  * MachMsgType#MOVE_SEND} or {@link MachMsgType#MOVE_SEND_ONCE}, the port
  * name is acquired using {@link MachPort#clear()}.
  */
 public class MachMsg {
     /**
      * Construct a value for the msgh_bits header field.
      */
     private static int MSGH_BITS(int remote, int local) {
         return remote | (local << 8);
     }
     private static int MSGH_BITS_REMOTE(int bits) {
         return bits & 0xff;
     }
     private static int MSGH_BITS_LOCAL(int bits) {
         return (bits >> 8) & 0xff;
     }
     private static final int MSGH_BITS_COMPLEX = 0x80000000;
 
     /**
      * The (direct) ByteBuffer backing this message.
      */
     private ByteBuffer buf;
 
     /**
      * Manage port names stored in the header.
      */
     private class HeaderPort {
         /**
          * Index into {@link MachPort#buf} for the port name we manage.
          */
         private int index;
 
         /**
          * MachPort object for the port name we manage. If non-null, we hold
          * one external reference to the name. The object is either provided
          * by the user or created on their behalf, and it is always their
          * responsability to {@link MachPort#deallocate} it.
          */
         private MachPort port;
 
         /** Initialize for a given index in {@link MachMsg#buf}. */
         public HeaderPort(int index) {
             this.index = index;
             port = null;
         }
 
         /**
          * Prepare to read from the buffer.
          *
         * We assume the old port value was modified behind our back, and was
          * deallocated as required by the associated type descriptor.
          */
         public void flip() throws Unsafe { 
             if(port != null) {
                 port.releaseName();
                 port = null;
             }
         }
 
         /** Set to MACH_PORT_NULL. */
         public void clear() {
             try {
                 /* A port name which was never accessed must be deallocated. */
                 if(port == null) {
                     int name = buf.getInt(index);
                     if(name != Mach.Port.NULL)
                         new MachPort(name).deallocate();
                 }
                 buf.putInt(index, Mach.Port.NULL);
                 flip();
             } catch(Unsafe exc) {}
         }
 
        /**
         * Write the name encapsulated in {@param newPort} into the buffer. An
         * external reference to the port name will be held until
         * {@link #clear} is called.
         */
         public void set(MachPort newPort, MachMsgType type) {
             if(!type.isPort())
                 throw new IllegalArgumentException();
 
             /* Release any port name we have. */
             clear();
             if(newPort == MachPort.NULL)
                 return;
 
             try {
                 if(type.isDeallocatedPort()) {
                     /* The name will be deallocated, so clear newPort. */
                     buf.putInt(index, newPort.clear());
                     port = null;
                 } else {
                     /* Otherwise, acquire an external name reference. */
                     buf.putInt(index, newPort.name());
                     port = newPort;
                 }
             } catch(Unsafe exc) {}
         }
 
        /**
         * Read the port name from the buffer and return a corresponding
         * MachPort object. An external reference to the port name will be held
         * until {@link #clear} is called.
         */
         public MachPort get() {
             if(port == null) {
                 int name = buf.getInt(index);
                 if(name != Mach.Port.NULL) {
                     /* Create a new MachPort object and indicate that we
                      * hold an external reference to the port name. */
                     try {
                         port = new MachPort(name);
                         port.name();
                     } catch(Unsafe exc) {}
                 }
             }
             return port;
         }
     }
 
     private HeaderPort remotePort, localPort;
     private MachMsgType remoteType, localType;
     private boolean complex;
 
     /* Extra ports referenced by this message. */
     private Collection<MachPort> refPorts;
 
     /**
      * Allocate a new message buffer.
      */
     public MachMsg(int size) {
         buf = ByteBuffer.allocateDirect(size);
         buf.order(ByteOrder.nativeOrder());
         remotePort = new HeaderPort(8);
         localPort = new HeaderPort(12);
         refPorts = new ArrayList<MachPort>();
         clear();
     }
 
     @SuppressWarnings("unused")
     public ByteBuffer buf() throws Unsafe {
         return buf;
     }
 
     /**
      * Release all port name references held by this message.
      *
      * This operation correponds to a partial {@link #clear()} which does
      * not alter the actual message contents. It is used after a message has
      * been received to release overwritten port references.
      */
     private void releaseNames() throws Unsafe {
         for(MachPort port : refPorts)
             port.releaseName();
 
         refPorts.clear();
     }
 
     /**
      * Clear the message's contents.
      */
     public synchronized MachMsg clear() {
         /* Clear the header. */
         remotePort.clear();
         remoteType = null;
         localPort.clear();
         localType = null;
         complex = false;
 
         /* FIXME: if a received message was not read completely, we leak the
          * remaining port rights and out-of-line memory. */
 
         /* Release port name references. */
         try { releaseNames(); } catch(Unsafe exc) {}
 
         /* Reset the message to a blank header. */
         buf.clear();
         for(int i = 0; i < 6; i++)
             buf.putInt(0);
 
         return this;
     }
 
     /**
      * 
      */
     public synchronized void flip() throws Unsafe {
         /* Flip header ports. */
         remotePort.flip();
         localPort.flip();
 
         /* Release port name references. */
         releaseNames();
 
         /* Read the new values */
         buf.clear();
         buf.limit(buf.getInt(4));
         buf.position(24);
     }
 
     /** Rewrite the header's {@code msgh_bits} field. */
     private void putBits() {
         int remoteBits = (remoteType != null) ? remoteType.name() : 0;
         int localBits = (localType != null) ? localType.name() : 0;
         int complexBit = complex ? MSGH_BITS_COMPLEX : 0;
         buf.putInt(0, MSGH_BITS(remoteBits, localBits) | complexBit);
     }
 
     /** Set the header's {@code msgh_remote_port} field. */
     public synchronized MachMsg setRemotePort(MachPort port, MachMsgType type) {
         remotePort.set(port, type);
         remoteType = type;
         putBits();
         return this;
     }
 
     /**
      * Get the header's {@code msgh_remote_port} field.
      *
      * The remote port is type checked against the given type, which should
      * be {@link MachMsgType#PORT_RECEIVE}, {@link MachMsgType#PORT_SEND}
      * or {@link MachMsgType#PORT_SEND_ONCE}. If the types match, the port
      * name is encapsulated into a new {@link MachPort} object and returned.
      * Subsequent calls will return the same {@link MachPort} object.
      */
     public synchronized MachPort getRemotePort(MachMsgType type)
         throws TypeCheckException
     {
         int typeVal = MSGH_BITS_REMOTE(buf.getInt(0));
         if(typeVal != type.name())
             throw new TypeCheckException();
 
         return remotePort.get();
     }
 
     /** Set the header's {@code msgh_local_port} field. */
     public synchronized MachMsg setLocalPort(MachPort port, MachMsgType type) {
         localPort.set(port, type);
         localType = type;
         putBits();
         return this;
     }
 
     /**
      * Get the header's {@code msgh_local_port} field.
      *
      * The local port is type checked against the given type, which should
      * be {@link MachMsgType#PORT_RECEIVE}, {@link MachMsgType#PORT_SEND}
      * or {@link MachMsgType#PORT_SEND_ONCE}. If the types match, the port
      * name is encapsulated into a new {@link MachPort} object and returned.
      * Subsequent calls will return the same {@link MachPort} object.
      */
     public synchronized MachPort getLocalPort(MachMsgType type)
         throws TypeCheckException
     {
         int typeVal = MSGH_BITS_LOCAL(buf.getInt(0));
         if(typeVal != type.name())
             throw new TypeCheckException();
 
         return localPort.get();
     }
 
     /** Set the header's {@code msgh_id} field. */
     public synchronized MachMsg setId(int id) {
         buf.putInt(20, id);
         return this;
     }
 
     /** Get the header's {@code msgh_id} field. */
     public synchronized int getId() {
         return buf.getInt(20);
     }
 
 
     /* Writing data items */
 
     static private interface PutOperation {
         void operate();
     }
 
     private void atomicPut(PutOperation op) {
         buf.mark();
         try {
             op.operate();
         } catch(Error exc) {
             buf.reset();
             throw exc;
         } catch(RuntimeException exc) {
             buf.reset();
             throw exc;
         }
     }
 
     /**
      * Append a {@code MACH_MSG_TYPE_CHAR} data item to this message.
      */
     public synchronized MachMsg putChar(final byte ch) {
         atomicPut(new PutOperation() {
             public void operate() {
                 MachMsgType.CHAR.put(buf);
                 buf.put(ch);
             }
         });
         return this;
     }
     public synchronized MachMsg putChar(final byte[] src) {
         atomicPut(new PutOperation() {
             public void operate() {
                 MachMsgType.CHAR.put(buf, src.length);
                 buf.put(src);
             }
         });
         return this;
     }
 
     /**
      * Append a {@code MACH_MSG_TYPE_INTEGER_64} data item to this message.
      */
     public synchronized MachMsg putInteger64(final long value) {
         atomicPut(new PutOperation() {
             public void operate() {
                 MachMsgType.INTEGER_64.put(buf);
                 buf.putLong(value);
             }
         });
         return this;
     }
 
     /* Reading data items */
 
     private static interface GetOperation {
         void operate() throws TypeCheckException;
     }
 
     private void atomicGet(GetOperation op) throws TypeCheckException {
         buf.mark();
         try {
             op.operate();
         } catch(Error exc) {
             buf.reset();
             throw exc;
         } catch(RuntimeException exc) {
             buf.reset();
             throw exc;
         } catch(TypeCheckException exc) {
             buf.reset();
             throw exc;
         }
     }
 
     /**
      * Read a {@code MACH_MSG_TYPE_INTEGER_32} data item from this message.
      */
     public synchronized int getInteger32() throws TypeCheckException {
         final int[] value = new int[1];
         atomicGet(new GetOperation() {
             public void operate() throws TypeCheckException {
                 MachMsgType.INTEGER_32.check(buf, 1);
                 value[0] = buf.getInt();
             }
         });
         return value[0];
     }
 }
