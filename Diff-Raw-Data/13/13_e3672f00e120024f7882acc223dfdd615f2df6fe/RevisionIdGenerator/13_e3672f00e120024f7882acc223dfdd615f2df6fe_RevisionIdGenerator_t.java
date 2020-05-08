 package ch.x42.terye.mk.hbase;
 
 import org.apache.jackrabbit.mk.api.MicroKernelException;
 
 public class RevisionIdGenerator {
 
     /**
      * The timestamp of the revision ids to be generated is based on the
      * following date instead of the usual unix epoch: 01.01.2013 00:00:00.
      * 
      * NEVER EVER CHANGE THIS VALUE!
      */
     public static final long EPOCH = 1356998400;
 
     // the machine id associated with this generator instance
     private long machineId;
     // the timestamp of the id last generated
     private long lastTimestamp;
     // counter to differentiate ids generated within the same millisecond
     private int count;
 
     public RevisionIdGenerator(int machineId) {
         if (machineId < 0 || machineId > 65535) {
             throw new IllegalArgumentException("Machine id is out of range");
         }
         this.machineId = machineId;
         lastTimestamp = -1;
         count = 0;
     }
 
     /**
      * This method generates a new revision id. A revision id is a signed (but
      * non-negative) long composed of the concatenation (left-to-right) of
      * following fields:
      * 
      * <pre>
      *  ------------------------------------------------------------------
      * |                              timestamp |     machine_id |  count |
      *  ------------------------------------------------------------------
      *  
      * - timestamp: 5 bytes, [0, 1099511627775]
      * - machine_id: 2 bytes, [0, 65535]
      * - count: 1 byte, [0, 255]
      * </pre>
      * 
      * The unit of the "timestamp" field is milliseconds and since we only have
      * 5 bytes available, we base it on an artificial epoch constant in order to
      * have more values available. The machine id used for "machine_id" is
      * either set explicitly or generated using the hashcode of the MAC address
      * string. If the same machine commits a revision within the same
      * millisecond, then the "count" field is used in order to create a unique
      * revision id.
      * 
      * @return a new and unique revision id
      */
     public long getNewId() {
         // timestamp
         long timestamp = System.currentTimeMillis() - EPOCH;
         timestamp <<= 24;
         // check for overflow
         if (timestamp < 0) {
             // we have used all available time values
            throw new MicroKernelException(
                    "Error generating new revision id, timestamp overflowed");
         }
 
         // machine id
        long machineId = (this.machineId << 8)
                & Long.decode("0x0000000000FFFF00");
 
         // counter
         if (timestamp == lastTimestamp) {
             count++;
         } else {
             lastTimestamp = timestamp;
             count = 0;
         }
        if (count > 255) {
            throw new MicroKernelException(
                    "Error generating new revision id, counter overflowed");
        }
         long count = this.count & Integer.decode("0x000000FF");
 
         // assemble and return revision id
         return timestamp | machineId | count;
     }
 
 }
