 package team094.modules;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 
 public class Communicator {
     private final BroadcastController comm;
     private final RobotController myRC;
     private final int DM;
 
     public static final int NONE = 0;
     public static final int ATTACK = 1;
     public static final int ATTACK_BUILDING = 2;
     public static final int DEFEND = 4;
     public static final int HEAL = 8;
     public static final int SCATTER = 16;
 
     // Cache
     private int mask;
     private MapLocation destination;
     private Message [] messages;
     private int length;
 
 
     /**
      * Facilitates communication to and from this robot.
      *
      * Message format:
      *   ints: (round-id) [(checksum) (mask) (rank) (distance from source to last ^2) (rebroadcast-rounds)] [repeat...]
      *   strings: null
      *   locations: [(source) (dest)] [repeat...]
      *
      * Checksum:
      *     (rebroadcast-rounds)
      *     ^(distance from source to last ^2 &lt;&lt; 1)
      *     ^(rank &lt;&lt; 2)
      *     ^(mask &lt;&lt; 3)
      *     ^(destination_location &lt;&lt; 4)
      *     ^(source_location &lt;&lt; 5)
      *
      *
      * @param rp The robots RobotProperties.
      */
     public Communicator(RobotProperties rp) {
         comm = rp.comm;
         myRC = rp.myRC;
         DM = rp.DIST_MULTIPLIER;
         messages = new Message [] {};
     }
 
     /**
      * Clears all messages from the message queue.
      *
      * @return true if messages were cleared.
      */
     public boolean clear() {
         messages = null;
         length = 0;
         mask = 0;
         destination = null;
         return myRC.getAllMessages().length > 0;
     }
 
     public boolean turnOn(int [] ids) throws GameActionException {
         if (comm == null || comm.isActive()) return false;
         comm.broadcastTurnOn(ids);
         return true;
     }
 
     /**
      * Relays the cached message if there is one and we should relay it.
      *
      * @return true if we relayed the message
      */
     public boolean send() throws GameActionException {
         return send(0,0,0,null);
 
     }
 
     /**
      * Sends out a command to robots within range.
      *
      * @param bitmask The bitmask that describes the robots that should receive this message.
      * @param rank The rank of the situation
      * @param destination The destination.
      * @return true if the message was sent.
      */
     public boolean send(int bitmask, int rank, int rebroadcast, MapLocation destination) throws GameActionException {
         if (comm == null || comm.isActive() || (length == 0 && destination == null)) return false;
 
         Message message = new Message();
         int locs_length, ints_length;
 
         if (destination != null) {
             message.ints = new int [ints_length = (length*5)+6];
             message.ints[2] = bitmask;
             message.ints[3] = rank;
             message.ints[4] = 0; //distance from source
             message.ints[5] = rebroadcast;
 
             message.locations = new MapLocation [locs_length = length*2 + 2];
             message.locations[0] = myRC.getLocation();
             message.locations[1] = destination;
 
             message.ints[1] = (message.ints[5] ^ (message.ints[4]<<1) ^ (message.ints[3]<<2) ^ (message.ints[2]<<3) ^ (message.locations[1].hashCode()<<4) ^ (message.locations[0].hashCode()<<5));
         } else {
             message.ints = new int [ints_length = length*5+1];
             message.locations = new MapLocation [locs_length = length*2];
         }
         if (messages != null) {
             for (int i = messages.length; i-- > 0; ) {
                 if (messages[i] == null) continue;
                 int ml_length = messages[i].locations.length - 1;
                 int mi_length = messages[i].ints.length - 1;
                 // Can be a do while because all messages that are too short = null
                System.out.println(ints_length+":"+locs_length+":"+mi_length+":"+ml_length);
                 do {
                     if (messages[i].ints[mi_length] < 0) {
                         ml_length -= 2;
                         mi_length -= 5;
                         continue;
                     }
                    System.out.println("RESEND");
 
                     message.ints[--ints_length] = messages[i].ints[mi_length--];
                     message.ints[--ints_length] = messages[i].ints[mi_length--];
                     message.ints[--ints_length] = messages[i].ints[mi_length--];
                     message.ints[--ints_length] = messages[i].ints[mi_length--];
                     message.ints[--ints_length] = messages[i].ints[mi_length--];
 
                     message.locations[--locs_length] = messages[i].locations[ml_length--];
                     message.locations[--locs_length] = messages[i].locations[ml_length--];
                 } while (ml_length > 0);
             }
         }
         messages = null;
         length = 0;
 
         // Put this at the end to guarentee that the round number is correct.
         message.ints[0] = MessageID.get(Clock.getRoundNum());
         comm.broadcast(message);
         return true;
     }
 
     /**
      * Parses incomming messages but does not pick one out for use.
      * This method also parses all other messages and prepares them for the send method it MUST be called before send.
      *
      * @return false
      */
     public boolean receive() {
         return receive(0);
     }
 
     /**
      * Gets the highest ranked command matching bitmask.
      * This method also parses all other messages and prepares them for the send method it MUST be called before send.
      *
      * Bitmask: 1 - Attack
      *          2 - Attack Building
      *          4 - Defend
      *          8 - Heal
      *         16 - Base
      *
      * @param bitmask The bitmask that must match the command.
      * @return true if a message was chosen.
      */
     public boolean receive(int bitmask) {
         // Do this so that both id_now and id_prev are stored in constants. (saves a bytecode per lookup and one here)
         int id_now = Clock.getRoundNum();
         int id_prev = MessageID.get(id_now - 1);
         id_now = MessageID.get(id_now);
 
         int [] ints;
         MapLocation [] locations;
         MapLocation myLoc = myRC.getLocation();
 
         int locs_length;
         int ints_length;
         //saves bytecode if we don't care about parsing a message
         int high_rank;
         if (bitmask == 0)
            high_rank = -1000;
        else
             high_rank = 10000;
         int distance;
         messages = myRC.getAllMessages();
         int message_count = messages.length;
         length = 0;
         int csCache; // Checksum cache;
 
     read:
        while (--message_count > 0) {
             // 1. Check if ints is null.
             // 2. Check if locations is null.
             // 3. ints needs 6 items.
             // 4. The first int must be either the current or the previous id.
             // 5. The the bitmask must be doubled.
             // 6. The mask must match the passed bitmask.
             // 7. The rebroadcast must be doubled and greater than or equal to 0.
             // 8. locations needs at least 2 items (source/dest).
             if (  (ints = messages[message_count].ints) == null
                || (locations = messages[message_count].locations) == null
                || (ints_length = ints.length) < 4
                ||!(  ints[0] == id_now
                   || ints[0] == id_prev
                   )
                || (locs_length = locations.length) < 2
                || (locs_length & 1) != 0
                || (ints_length - 1) % 5 != 0
                 )
             {
                 messages[message_count] = null;
                System.out.println("FAIL ROUND");
                 continue read;
             }
 
 
             // There is a reason for every decriment etc. in here.
             // You may even be able to decypher my reasons if you
             // stare at it long enough... (think: "the index must always be valid")
             // ...but I recommend that you don't do that...the horror...the horror...
 
             do {
                 if (locations[locs_length-1] == null || locations[locs_length-2] == null)
                 {
                     messages[message_count].ints[ints_length-1] = -1;
                     ints_length -= 5;
                     continue;
                 } else if ((ints[--ints_length]
                        ^(ints[--ints_length]<<1)
                        ^(csCache = ((ints[--ints_length]<<2)
                        ^(ints[--ints_length]<<3)
                        ^(locations[--locs_length].hashCode()<<4)
                        ^(locations[--locs_length].hashCode()<<5))))
                       !=ints[--ints_length]
                       )
                 {
                    System.out.println("FAIL");
                     messages[message_count].ints[ints_length+4] = -1;
                     continue;
                 }
 
 
                 if (--ints[ints_length+4] >= 0 && ints[ints_length+3] < (ints[ints_length+3] = myLoc.distanceSquaredTo(locations[locs_length]))) {
                     length++;
                     ints[ints_length] = ints[ints_length+4]^(ints[ints_length+3]<<1)^csCache;
                 } else {
                     ints[ints_length+4] = -1;
                    System.out.println("DROP");
                 }
 
                 if ((ints[ints_length+2] - ints[ints_length+3]*DM) > high_rank && (bitmask & ints[ints_length+1]) != 0) {
                    System.out.println("CHOOSE");
                     high_rank = ints[ints_length+2] - ints[ints_length+3]*DM;
                     destination = locations[locs_length+1];
                     mask = ints[ints_length+1];
                 }
                System.out.println("RECIEVE");
             } while (locs_length > 0);
         }
         if (destination != null) {
             return true;
         } else {
            destination = null;
             mask = 0;
             return false;
         }
     }
 
     /**
      * Gets the command bitmask for the last message.
      * @return The last message's bitmask.
      */
     public int getCommand() {
         return mask;
     }
 
     /**
      * Gets the destination for the last message.
      * @return The last message's destination.
      */
     public MapLocation getDestination() {
         return destination;
     }
 }
