 package org.gnu.mach;
 import java.nio.ByteBuffer;
 
 /**
  * Type descriptor for data items.
  *
  * This enumeration is used to write and check the type descriptors in a
  * Mach message. Each member represents a possible data type.  The
  * {@link #put} and {@link #get} methods read and write
  * {@code mach_msg_type_t} (or {@code mach_msg_long_type_t}) structures
  * from/to a given ByteBuffer object.
  *
  * In case an exception occurs, the buffer position can be anywhere
  * between its original value and the end of the type descriptor being
  * read or written. Users are advised to set the buffer's mark before
  * using these methods and restore it if an exception occurs.
  */
 public class MachMsgType {
     public static final MachMsgType
 
         /* List of types currently in use. Extend as needed. */
         CHAR =              new MachMsgType(8, 8, true),
         INTEGER_32 =        new MachMsgType(2, 32, false),
         INTEGER_64 =        new MachMsgType(11, 64, false),
         MOVE_RECEIVE =      new MachMsgType(16, 32, false),
         MOVE_SEND =         new MachMsgType(17, 32, false),
         MOVE_SEND_ONCE =    new MachMsgType(18, 32, false),
         COPY_SEND =         new MachMsgType(19, 32, false),
         MAKE_SEND =         new MachMsgType(20, 32, false),
         MAKE_SEND_ONCE =    new MachMsgType(21, 32, false),
 
         /* Aliases used for received ports. */
         PORT_RECEIVE =      MOVE_RECEIVE,
         PORT_SEND =         MOVE_SEND,
         PORT_SEND_ONCE =    MOVE_SEND_ONCE;
 
     /* Constants for mach_msg_type_t */
     private static final int BIT_INLINE     = 0x10000000;
     private static final int BIT_LONGFORM   = 0x20000000;
     private static final int BIT_DEALLOCATE = 0x40000000;
     private static final int CHECKED_BITS   = 0xf000ffff;
 
     /* Characteristics of this type */
     private final int name;
     private final int size;
     private final boolean longform;
     private final int number;
 
     /* Pre-constructed proto-header */
     private final int header;
 
     /* Fill-in the data and build the proto-header */
     private MachMsgType(int name, int size, boolean longform, int number) {
         this.name = name;
         this.size = size;
         this.longform = longform;
         this.number = number;
 
         /* FIXME: for now we support only inline data. */
         header = (longform ? BIT_LONGFORM : name | (size << 8)) | BIT_INLINE;
     }
     private MachMsgType(int name, int size, boolean longform) {
         this(name, size, longform, 1);
     }
 
     /** Get this type's name value. */
     public final int value() { return name; }
 
     /** Get this type descriptor's number field. */
     public final int number() { return number; }
 
     /**
      * Whether this is a port type.
      *
      * This corresponds to the {@code MACH_MSG_TYPE_PORT_ANY} C preprocessor
      * macro from {@code <mach/message.h>}.
      */
     public final boolean isPort() {
         return (name >= MOVE_RECEIVE.name) && (name <= MAKE_SEND_ONCE.name);
     }
 
     /**
      * Whether this is a deallocating port type.
      *
      * This corresponds to the {@code MACH_MSG_TYPE_PORT_ANY_RIGHT} C
      * preprocessor macro from {@code <mach/message.h>}.
      */
     public final boolean isDeallocatedPort() {
         return (name >= MOVE_RECEIVE.name) && (name <= MOVE_SEND_ONCE.name);
     }
 
    /* Align the position of buf to the next word boundary. */
    private static void align(ByteBuffer buf) {
        /* FIXME: hardcoded for 32 bits architectures. */
        while(buf.position() % 4 != 0)
            buf.put((byte) 0);
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
         if(name != this.name)
             throw new TypeCheckException(String.format(
                         "Type check error (name is 0x%x instead of 0x%x)",
                         name, this.name));
         if(size != this.size)
             throw new TypeCheckException(String.format(
                         "Type check error (size is 0x%x instead of 0x%x)",
                         size, this.size));
     }
 
     /**
      * Write a type descriptor into the given ByteBuffer.
      */
     public final void put(ByteBuffer buf, int num, boolean inl, boolean dealloc) {
         int header = this.header;
 
         if(inl)
             header |= BIT_INLINE;
         if(dealloc)
             header |= BIT_DEALLOCATE;
         if(!longform)
             header |= (num & 0x0fff) << 16;
 
        align(buf);
 
         buf.putInt(header);
         if(longform) {
             buf.putShort((short) name);
             buf.putShort((short) size);
             buf.putInt(num);
         }
     }
 
     /* Convenience versions. */
     public final void put(ByteBuffer buf, int num) {
         put(buf, num, true, false);
     }
     public final void put(ByteBuffer buf) {
         put(buf, number);
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
