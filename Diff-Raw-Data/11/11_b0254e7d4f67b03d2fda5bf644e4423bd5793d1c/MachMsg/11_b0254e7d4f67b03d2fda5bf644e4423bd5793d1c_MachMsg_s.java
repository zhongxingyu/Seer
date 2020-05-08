 package org.gnu.mach;
 
 import java.util.Collection;
 import java.util.ArrayList;
 import java.nio.ByteOrder;
 import java.nio.ByteBuffer;
 
 /**
  * Safe Mach message buffer.
  */
 public class MachMsg {
     /**
      * Type descriptor for data items.
      *
      * This enumeration is used to write and check the type descriptors in a
      * Mach message. Each member represents a possible data type. The put() and
      * get() methods read and write mach_msg_type_t (or mach_msg_long_type_t)
      * structures to/from a given ByteBuffer object.
      *
      * All the methods in this class must offer the guarantee that the buffer's
      * position stays in a consistent state no matter what happens. In other
      * words, put() and get() should behave in an atomic manner with respect to
      * the buffer's position. FIXME: or maybe we could manage this at the
      * MachMsg level?
      */
     public static enum Type {
         /* List of types currently in use. Extend as needed. */
         CHAR(8, 8, true),
         INTEGER_32(2, 32, false),
         INTEGER_64(11, 64, false),
         COPY_SEND(19, 32, false),
         MAKE_SEND_ONCE(21, 32, false);
 
         /* Constants for mach_msg_type_t */
         private static final int BIT_INLINE     = 0x10000000;
         private static final int BIT_LONGFORM   = 0x20000000;
         private static final int BIT_DEALLOCATE = 0x40000000;
         private static final int CHECKED_BITS   = 0xf000ffff;
 
         /* Characteristics of this type */
         private int name;
         private int size;
         private boolean longform;
 
         /* Pre-constructed proto-header */
         private int header;
 
         /* Fill-in the data and build the proto-header */
         private Type(int name, int size, boolean longform) {
             this.name = name;
             this.size = size;
             this.longform = longform;
 
             header = longform ? BIT_LONGFORM : name | (size << 8);
 
             /* FIXME: for now we support only inline data. */
             header |= BIT_INLINE;
         }
 
         /** Get this type's name value. */
         public int value() { return name; }
 
         /* Check a value against the proto-header. */
         /* TODO: custom exception */
         private void checkHeader(int header) throws Exception {
             if((header & CHECKED_BITS) != this.header)
                 throw new Exception(String.format(
                             "Type check error (0x%x instead of 0x%x)",
                             header, this.header));
         }
 
         /* Check the remainder of a long form header. */
         /* TODO: custom exception */
         private void checkLongHeader(int name, int size) throws Exception {
             if(name != this.name)
                 throw new Exception(String.format(
                             "Type check error (name is 0x%x instead of 0x%x)",
                             name, this.name));
             if(size != this.size)
                 throw new Exception(String.format(
                             "Type check error (size is 0x%x instead of 0x%x)",
                             size, this.size));
         }
 
         /**
          * Write a type descriptor into the given ByteBuffer.
          *
          * The buffer's mark is overwritten in all cases.
          */
         public void put(ByteBuffer buf, int num, boolean inl, boolean dealloc) {
             buf.mark();
             try {
                 int header = this.header;
 
                 if(inl)
                     header |= BIT_INLINE;
                 if(dealloc)
                     header |= BIT_DEALLOCATE;
                 if(!longform)
                     header |= (num & 0x0fff) << 16;
 
                 /* Align to the next word boundary. FIXME: hardcoded. */
                 while(buf.position() % 4 != 0)
                     buf.put((byte) 0);
 
                 buf.putInt(header);
                 if(longform) {
                     buf.putShort((short) name);
                     buf.putShort((short) size);
                     buf.putInt(num);
                 }
             }
             /* Ensure the buffer stays in a sane state no matter what. */
             catch(Error exc) {
                 buf.reset();
                 throw exc;
             } catch(RuntimeException exc) {
                 buf.reset();
                 throw exc;
             }
         }
 
         /* Convenience versions. */
         public void put(ByteBuffer buf, int num) {
             put(buf, num, true, false);
         }
         public void put(ByteBuffer buf) {
             put(buf, 1);
         }
 
         /**
          * Check the type descriptor read from the given ByteBuffer.
          *
          * @param buf The buffer to read the type descriptor from.
          * @return The number of elements.
          *
          * If the type checking fails, an exception is thrown and the buffer is
          * restored to its original position.
          *
          * The buffer's mark is overwritten in all cases.
          *
          * TODO: recognize out-of-line data.
          */
         public int get(ByteBuffer buf) throws Exception {
             buf.mark();
             try {
                 int header = buf.getInt();
                 int number;
 
                 /* Align to the next word boundary. FIXME: hardcoded. */
                 while(buf.position() % 4 != 0)
                     buf.get();
 
                 checkHeader(header);
                 if(longform) {
                     int name = buf.getShort();
                     int size = buf.getShort();
                     checkLongHeader(name, size);
                     number = buf.getInt();
                 } else {
                     number = (header >> 16) & 0x0fff;
                 }
                 return number;
             }
             /* Ensure the buffer stays in a sane state no matter what. */
             catch(Error exc) {
                 buf.reset();
                 throw exc;
             } catch(RuntimeException exc) {
                 buf.reset();
                 throw exc;
             } catch(Exception exc) {
                 buf.reset();
                 throw exc;
             }
         }
     }
 
     /**
      * Construct a value for the msgh_bits header field.
      */
     private static final int MSGH_BITS(int remote, int local) {
         return remote | (local << 8);
     }
     private static final int MSGH_BITS_COMPLEX = 0x80000000;
 
     /**
      * The (direct) ByteBuffer backing this message.
      */
     public ByteBuffer buf;
 
     /* Header data */
     private MachPort remotePort, localPort;
     private Type remoteType, localType;
     private boolean complex;
 
     /* Extra ports referenced by this message. */
     private Collection<MachPort> refPorts;
 
     /**
      * Allocate a new message buffer.
      */
     public MachMsg(int size) {
         buf = ByteBuffer.allocateDirect(size);
         buf.order(ByteOrder.nativeOrder());
         refPorts = new ArrayList<MachPort>();
         clear();
     }
 
     public ByteBuffer buf() {
         return buf;
     }
 
     /**
      * Release all port name references held by this message.
      *
      * This operation correponds to a partial {@link #clear()} which does
      * not alter the actual message contents. It is used after a message has
      * been received to release overwritten port references.
      */
     private void clearNames() {
         /* Release references. */
         if(remotePort != null)
             remotePort.releaseName();
         if(localPort != null)
             localPort.releaseName();
         for(MachPort port : refPorts)
             port.releaseName();
 
         /* Forget them. */
         remotePort = null;
         remoteType = null;
         localPort = null;
         localType = null;
         complex = false;
         refPorts.clear();
     }
 
     /**
      * Clear the message's contents.
      */
     public synchronized MachMsg clear() {
         /* Release port name references. */
         clearNames();
 
         /* Reset the message to a blank header. */
         buf.clear();
         for(int i = 0; i < 6; i++)
             buf.putInt(0);
 
         return this;
     }
 
    /** Rewrite the header's @code{msgh_bits} field. */
     private void putBits() {
         int remoteBits = (remoteType != null) ? remoteType.value() : 0;
         int localBits = (localType != null) ? localType.value() : 0;
         int complexBit = complex ? MSGH_BITS_COMPLEX : 0;
         buf.putInt(0, MSGH_BITS(remoteBits, localBits) | complexBit);
     }
 
    /** Set the header's @code{msgh_remote_port} field. */
     public synchronized MachMsg setRemotePort(MachPort port, Type type) {
         if(remotePort != null)
             remotePort.releaseName();
 
         remotePort = port;
         buf.putInt(8, remotePort.name());
 
         remoteType = type;
         putBits();
 
         return this;
     }
 
    /** Set the header's @code{msgh_local_port} field. */
     public synchronized MachMsg setLocalPort(MachPort port, Type type) {
         if(localPort != null)
             localPort.releaseName();
 
         localPort = port;
         buf.putInt(12, localPort.name());
 
         localType = type;
         putBits();
 
         return this;
     }
 
    /** Set the header's @code{msgh_id} field. */
     public synchronized MachMsg setId(int id) {
         buf.putInt(20, id);
         return this;
     }
 }
