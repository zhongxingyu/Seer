 /*
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 package net.FriendsUnited.NodeLevel.Packet;
 
 import java.util.UUID;
 
 import net.FriendsUnited.Util.ByteConverter;
 
 /**
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public final class HelloPacket
 {
     public static final byte TYPE          = (byte)1;
 
     private final boolean valid;
     private final UUID NodeId;
 
     /**
      *
      */
     public HelloPacket(UUID NodeId)
     {
         if(null == NodeId)
         {
             this.NodeId = null;
             valid = false;
         }
         else
         {
             this.NodeId = NodeId;
             valid = true;
         }
     }
 
     public HelloPacket(byte[] bytes)
     {
         ByteConverter bc = new ByteConverter(bytes);
         byte type = bc.getByte();
        NodeId = new UUID(bc.getLong(), bc.getLong());
         if(TYPE == type)
         {
             valid = true;
         }
         else
         {
             valid = false;
         }
     }
 
     public byte[] toByteArray()
     {
         if(true == valid)
         {
             ByteConverter bc = new ByteConverter();
             bc.add(TYPE);
            bc.add(NodeId.getMostSignificantBits());
            bc.add(NodeId.getMostSignificantBits());
             return bc.toByteArray();
         }
         else
         {
             return new byte[] {};
         }
     }
 
     public boolean isValid()
     {
         return valid;
     }
 
     public UUID getPacketsNodeId()
     {
         return NodeId;
     }
 
 }
