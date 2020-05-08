 package org.gnu.mach;
 import java.nio.ByteBuffer;
 
 /**
  * Type descriptor for data items.
  *
  * This class is used to read and write the type descriptors in message
  * buffers, and corresponds to the C structures {@code mach_msg_type_t} and
  * {@code mach_msg_type_long_t}. Additionally, templates are defined for the
  * most commonly used types, which correspond to the {@code MACH_MSG_TYPE_*}
  * constants in {@code <mach/message.h>}.
  *
  * Reading and writing type descriptors is not atomic: in case an exception
  * occurs, the buffer position can be anywhere between its original value and
  * the end of the type descriptor being read or written. Users are advised to
  * set the buffer's mark before using these methods and restore it if an
  * exception occurs.
  */
 public class MachMsgType {
     /* Constants for mach_msg_type_t */
     private static final int BIT_INLINE     = 0x10000000;
     private static final int BIT_LONGFORM   = 0x20000000;
     private static final int BIT_DEALLOCATE = 0x40000000;
     private static final int CHECKED_BITS   = 0xf000ffff;
 
     /* Characteristics of this type */
     private final int name;
     private final int size;
     private final int number;
     private final boolean inl;
     private final boolean longform;
     private final boolean deallocate;
 
     /* Fill-in the data and build the proto-header */
     private MachMsgType(int name, int size, int number, boolean inl,
                         boolean longform, boolean deallocate) {
         assert !(inl && deallocate);
 
         this.name = name;
         this.size = size;
         this.number = number;
         this.inl = inl;
         this.longform = longform;
         this.deallocate = deallocate;
     }
     private MachMsgType(int name, int size, boolean longform) {
         this(name, size, 1, true, longform, false);
     }
 
     /** Get this type descriptor's name value. */
     public final int name() { return name; }
 
     /** Get this type descriptor's size field. */
     public final int size() { return size; }
 
     /** Get this type descriptor's number field. */
     public final int number() { return number; }
 
     /** Get this type descriptor's inline bit. */
     public final boolean inl() { return longform; }
 
     /** Get this type descriptor's longform bit. */
     public final boolean longform() { return longform; }
 
     /** Get this type descriptor's deallocate bit. */
     public final boolean deallocate() { return longform; }
 
     /**
      * Whether this is a port type.
      *
      * This corresponds to the {@code MACH_MSG_TYPE_PORT_ANY} C preprocessor
      * macro from {@code <mach/message.h>}.
      */
     public final boolean isPort() {
         return (name >= MOVE_RECEIVE.name()) && (name <= MAKE_SEND_ONCE.name());
     }
 
     /**
      * Whether this is a deallocating port type.
      *
      * This corresponds to the {@code MACH_MSG_TYPE_PORT_ANY_RIGHT} C
      * preprocessor macro from {@code <mach/message.h>}.
      */
     public final boolean isDeallocatedPort() {
         return (name >= MOVE_RECEIVE.name()) && (name <= MOVE_SEND_ONCE.name());
     }
 
     /**
      * Length in bytes of the data associated with this type descriptor.
      */
     public final int bytes() {
         return size() * number() / 8;
     }
 
     /* Reading and writing */
 
     /**
      * Align the position of buf to the next word boundary.
      */
     private static void align(ByteBuffer buf) {
         /* FIXME: hardcoded for 32 bits architectures. */
         while(buf.position() % 4 != 0)
             buf.put((byte) 0);
     }
 
     /**
      * Put together a header value.
      */
     private static int makeHeader(int name, int size, int number, boolean inl,
                                   boolean longform, boolean deallocate) {
         int header = 0;
 
         assert (name & ~0xff) == 0;
         header |= name;
 
         assert (size & ~0xff) == 0;
         header |= size << 8;
        
         assert (number & ~0x0fff) == 0;
         header |= number << 16;
 
         if(inl)
             header |= BIT_INLINE;
         if(longform)
             header |= BIT_LONGFORM;
         if(deallocate)
             header |= BIT_DEALLOCATE;
 
         return header;
     }
 
     /**
      * Write a type descriptor into the given ByteBuffer.
      */
     public final void put(ByteBuffer buf, int number, boolean inl, boolean deallocate) {
         align(buf);
 
         if(longform) {
             buf.putInt(makeHeader(0, 0, 0, inl, longform, deallocate));
             buf.putShort((short) name);
             buf.putShort((short) size);
             buf.putInt(number);
         } else {
             buf.putInt(makeHeader(name, size, number, inl, longform, deallocate));
         }
     }
 
     /* Convenience versions. */
     public final void put(ByteBuffer buf, int number) {
         put(buf, number, inl, deallocate);
     }
     public final void put(ByteBuffer buf) {
         put(buf, number);
     }
 
     /**
      * Read a type descriptor from the given ByteBuffer.
      */
     public static MachMsgType get(ByteBuffer buf) {
         align(buf);
 
         int header = buf.getInt();
         boolean inl = (header & BIT_INLINE) != 0;
         boolean longform = (header & BIT_LONGFORM) != 0;
         boolean deallocate = (header & BIT_DEALLOCATE) != 0;
 
         int name, size, number;
         if(longform) {
             name = buf.getShort();
             size = buf.getShort();
             number = buf.getInt();
         } else {
             name = header & 0xff;
             size = (header >> 8) & 0xff;
             number = (header >> 16) & 0x0fff;
         }
 
         return new MachMsgType(name, size, number, inl, longform, deallocate);
     }
 
     /**
      * Type descriptor template.
      *
      * Whereas {@link MachMsgType} itself handles reading and writing arbitrary
      * type descriptors from {@link java.nio.ByteBuffer} objects, this class
      * provides the additionnal functionnality required for the predefined type
      * descriptor templates, namely the ability to write variations on a given
      * descriptor and to check arbitrary descriptors against this one.
      */
     public static class Template extends MachMsgType {
         /* Pre-constructed proto-header */
         private final int header;
 
         /* Construct a new type descriptor template */
         private Template(int name, int size, boolean longform) {
             super(name, size, longform);
             header = longform ? makeHeader(0, 0, 0, true, longform, false)
                               : makeHeader(name, size, 0, true, longform, false);
         }
 
         /* Check a value against the proto-header. */
         private void checkHeader(int header) throws TypeCheckException {
             if((header & CHECKED_BITS) != this.header)
                 throw new TypeCheckException(String.format(
                             "Type check error (0x%x instead of 0x%x)",
                             header, this.header));
         }
 
         /* Check the remainder of a long form header. */
         private void checkLongHeader(int name, int size)
             throws TypeCheckException
         {
             if(name != this.name())
                 throw new TypeCheckException(String.format(
                             "Type check error (name is 0x%x instead of 0x%x)",
                             name, this.name()));
             if(size != this.size())
                 throw new TypeCheckException(String.format(
                             "Type check error (size is 0x%x instead of 0x%x)",
                             size, this.size()));
         }
 
         /**
          * Check the type descriptor read from the given ByteBuffer.
          *
          * @param buf The buffer to read the type descriptor from.
          * @return The number of elements.
          *
          * If the type checking fails, an exception is thrown. As for any
          * other exception, when that occurs the buffer position is
          * unspecified.
          *
          * TODO: recognize out-of-line data.
          */
         public final int check(ByteBuffer buf) throws TypeCheckException {
             align(buf);
 
             int header = buf.getInt();
             int number;
 
             checkHeader(header);
             if(longform()) {
                 int name = buf.getShort();
                 int size = buf.getShort();
                 checkLongHeader(name, size);
                 number = buf.getInt();
             } else {
                 number = (header >> 16) & 0x0fff;
             }
             return number;
         }
 
         /**
          * Check the type descriptor read from the given ByteBuffer.
          *
          * This version behaves the same was as {@link #get(ByteBuffer)}
          * does, but additionally checks the type descriptor's
          * {@code msgt_number} field instead of returning it.
          */
         public final void check(ByteBuffer buf, int expectedNumber)
             throws TypeCheckException
         {
             int actualNumber = check(buf);
 
             if(actualNumber != expectedNumber)
                 throw new TypeCheckException(String.format(
                     "Type check error: msgt_number was %d (expected %d)",
                     actualNumber, expectedNumber));
         }
     }
 
     public static final Template
 
         /* List of types currently in use. Extend as needed. */
         CHAR =              new Template(8, 8, true),
         INTEGER_32 =        new Template(2, 32, false),
         INTEGER_64 =        new Template(11, 64, false),
         MOVE_RECEIVE =      new Template(16, 32, false),
         MOVE_SEND =         new Template(17, 32, false),
         MOVE_SEND_ONCE =    new Template(18, 32, false),
         COPY_SEND =         new Template(19, 32, false),
         MAKE_SEND =         new Template(20, 32, false),
         MAKE_SEND_ONCE =    new Template(21, 32, false),
 
         /* Aliases used for received ports. */
         PORT_RECEIVE =      MOVE_RECEIVE,
         PORT_SEND =         MOVE_SEND,
         PORT_SEND_ONCE =    MOVE_SEND_ONCE;
 }
