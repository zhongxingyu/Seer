 package net.minecraft.network.packet;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 public class Packet8UpdateHealth extends Packet
 {
     /** Variable used for incoming health packets */
     public int healthMP;
     public int food;
     public int thirstlevel;
     /**
      * Players logging on get a saturation of 5.0. Eating food increases the saturation as well as the food bar.
      */
     public float foodSaturation;
 
     public Packet8UpdateHealth() {}
 
     public Packet8UpdateHealth(int par1, int par2, float par3, int thirstlevel)
     {
         this.healthMP = par1;
         this.food = par2;
         this.foodSaturation = par3;
         this.thirstlevel = thirstlevel;
     }
 
     /**
      * Abstract. Reads the raw packet data from the data stream.
      */
     public void readPacketData(DataInputStream par1DataInputStream) throws IOException
     {
         this.healthMP = par1DataInputStream.readShort();
         this.food = par1DataInputStream.readShort();
         this.foodSaturation = par1DataInputStream.readFloat();
        this.thirstlevel = par1DataInputStream.readInt();
     }
 
     /**
      * Abstract. Writes the raw packet data to the data stream.
      */
     public void writePacketData(DataOutputStream par1DataOutputStream) throws IOException
     {
         par1DataOutputStream.writeShort(this.healthMP);
         par1DataOutputStream.writeShort(this.food);
         par1DataOutputStream.writeFloat(this.foodSaturation);
        par1DataOutputStream.writeInt(this.thirstlevel);
     }
 
     /**
      * Passes this Packet on to the NetHandler for processing.
      */
     public void processPacket(NetHandler par1NetHandler)
     {
         par1NetHandler.handleUpdateHealth(this);
     }
 
     /**
      * Abstract. Return the size of the packet (not counting the header).
      */
     public int getPacketSize()
     {
         return 8;
     }
 
     /**
      * only false for the abstract Packet class, all real packets return true
      */
     public boolean isRealPacket()
     {
         return true;
     }
 
     /**
      * eg return packet30entity.entityId == entityId; WARNING : will throw if you compare a packet to a different packet
      * class
      */
     public boolean containsSameEntityIDAs(Packet par1Packet)
     {
         return true;
     }
 }
