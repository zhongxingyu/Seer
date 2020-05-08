 import java.io.Serializable;
 import java.net.InetSocketAddress;
 import java.util.HashMap;
 
 /**
  * User: Ivan
  * Date: 24/02/13
  */
 public class MazewarPacket extends MazewarPacketIdentifier implements Serializable, Comparable<MazewarPacket> {
     public static final int NULL = 0;
     public static final int ERROR_DUPLICATED_CLIENT = 1;
 
     public static final int REGISTER = 100;
     public static final int REGISTER_SUCCESS = 101;
 
     public static final int ADD_NOTICE = 200;
     public static final int REPORT_LOCATION = 201;
     public static final int ADD = 202;
 
     public static final int MOVE_FORWARD = 300;
     public static final int MOVE_BACKWARD = 301;
 
     public static final int TURN_LEFT = 302;
     public static final int TURN_RIGHT = 303;
 
     public static final int FIRE = 400;
     public static final int INSTANT_KILL = 401;
     public static final int KILL = 402;
 
     public static final int QUIT = 500;
 
     //packet definitions
     public String newClient;
     public String ACKer;
     public String victim;
     public int seqNum;
 
     public InetSocketAddress address;
     public HashMap<String, InetSocketAddress> clientAddresses;
 
     public int type = MazewarPacket.NULL;
     public DirectedPoint directedPoint;
     public Integer score;
 
     @Override
     public int compareTo(MazewarPacket o) {
         if (this.seqNum == o.seqNum)
             return this.owner.compareTo(o.owner);
         else
             return this.seqNum > o.seqNum ? 1 : -1;
     }
 }
