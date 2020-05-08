 package net.theunnameddude.mcclient.protocol;
 
 import io.netty.buffer.ByteBuf;
 import net.theunnameddude.mcclient.protocol.packets.*;
 
 import java.util.HashMap;
 
 public class PacketUtils {
 
     public static HashMap<Byte, PacketInstruction[]> packetInstructions = new HashMap<Byte, PacketInstruction[]>();
     static {
         // Time update
         packetInstructions.put( (byte)0x04, new PacketInstruction[] {
             PacketInstruction.ReadLong, PacketInstruction.ReadLong
         } );
 
         // Entity equipment
         packetInstructions.put( (byte)0x05, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadShort, PacketInstruction.ReadItem
         } );
 
         // Update health
         packetInstructions.put( (byte)0x08, new PacketInstruction[] {
             PacketInstruction.ReadFloat, PacketInstruction.ReadShort, PacketInstruction.ReadFloat
         } );
 
         // Player
         packetInstructions.put( (byte)0x0A, new PacketInstruction[] {
                 PacketInstruction.ReadBoolean
         } );
 
         // Player digging
         packetInstructions.put( (byte)0x0E, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadInt, PacketInstruction.ReadByte,
                 PacketInstruction.ReadInt, PacketInstruction.ReadByte
         } );
 
         // Item held change
         packetInstructions.put( (byte)0x10, new PacketInstruction[] {
             PacketInstruction.ReadShort
         } );
 
         // Use bed
         packetInstructions.put( (byte)0x11, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadInt,
                 PacketInstruction.ReadByte, PacketInstruction.ReadInt
         } );
 
         // Animation
         packetInstructions.put( (byte)0x12, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte
         } );
 
         // Spawn named entity
         packetInstructions.put( (byte)0x14, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadString, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadByte,
                 PacketInstruction.ReadShort, PacketInstruction.ReadEntityMeta
         } );
 
         // Collect item
         packetInstructions.put( (byte)0x16, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt
         } );
 
         // Spawn object/vehicle
         packetInstructions.put( (byte)0x17, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadByte,
                 PacketInstruction.ReadByte, PacketInstruction.ReadMotion
         } );
 
         // Spawn mob
         packetInstructions.put( (byte)0x18, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadByte,
                 PacketInstruction.ReadByte, PacketInstruction.ReadByte, PacketInstruction.ReadShort,
                 PacketInstruction.ReadShort, PacketInstruction.ReadShort, PacketInstruction.ReadEntityMeta
         } );
 
         // Spawn painting
         packetInstructions.put( (byte)0x19, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadString, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadInt
         } );
 
         // Spawn exp orb
         packetInstructions.put( (byte)0x1A, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadShort
         } );
 
         // Entity velocity
         packetInstructions.put( (byte)0x1C, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadShort, PacketInstruction.ReadShort,
                 PacketInstruction.ReadShort
         } );
 
         // Destroy entities
         packetInstructions.put( (byte)0x1D, new PacketInstruction[] {
                 PacketInstruction.ReadIntArray
         } );
 
         // Entity relative move
         packetInstructions.put( (byte) 0x1F, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadByte,
                 PacketInstruction.ReadByte
         } );
 
         // Entity look
         packetInstructions.put( (byte) 0x20, new PacketInstruction[] {
                 PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadByte
         } );
 
         // Entity look and relative move
         packetInstructions.put( (byte)0x21, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadByte,
                 PacketInstruction.ReadByte, PacketInstruction.ReadByte, PacketInstruction.ReadByte
         } );
 
         // Entity teleport
         packetInstructions.put( (byte)0x22, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadByte
         } );
 
         // Entity look
         packetInstructions.put( (byte)0x23, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte
         } );
 
         // Entity status
         packetInstructions.put( (byte)0x26, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte
         } );
 
         // Attach entity
         packetInstructions.put( (byte)0x27, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadBoolean
         } );
 
         // Entity metadata
         packetInstructions.put( (byte)0x28, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadEntityMeta
         } );
 
         // Entity effect
         packetInstructions.put( (byte)0x29, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadByte,
                 PacketInstruction.ReadShort
         } );
 
         // Entity properties
         packetInstructions.put( (byte)0x2C, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadEntityProperties
         } );
 
         // Remove entity effect
         packetInstructions.put( (byte)0x2A, new PacketInstruction[] {
                 PacketInstruction.ReadInt, PacketInstruction.ReadByte
         } );
 
         // Set Experience
         packetInstructions.put( (byte)0x2B, new PacketInstruction[] {
                 PacketInstruction.ReadFloat, PacketInstruction.ReadShort, PacketInstruction.ReadShort
         } );
 
         // Chunk data
         packetInstructions.put( (byte)0x33, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadBoolean,
                PacketInstruction.ReadUnsignedShort, PacketInstruction.ReadUnsignedShort,
                PacketInstruction.ReadIntUnsignedByteArray
         } );
 
         // Multi block change
         packetInstructions.put( (byte)0x34, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadShort,
                 PacketInstruction.ReadIntByteArray
         } );
 
         // Block change
         packetInstructions.put( (byte)0x35, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadByte, PacketInstruction.ReadInt,
                 PacketInstruction.ReadShort, PacketInstruction.ReadByte
         } );
 
         // Block action
         packetInstructions.put( (byte)0x36, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadShort, PacketInstruction.ReadInt,
                 PacketInstruction.ReadShort, PacketInstruction.ReadByte
         } );
 
         // Block break animation
         packetInstructions.put( (byte)0x37, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadByte
         } );
 
         // Read bulk chunk
         packetInstructions.put( (byte)0x38, new PacketInstruction[] {
             PacketInstruction.ReadBulkChunk
         } );
 
         // Explosions
         packetInstructions.put( (byte)0x3C, new PacketInstruction[] {
             PacketInstruction.ReadDouble, PacketInstruction.ReadDouble, PacketInstruction.ReadDouble,
                 PacketInstruction.ReadFloat, PacketInstruction.ReadInt3, PacketInstruction.ReadFloat,
                 PacketInstruction.ReadFloat, PacketInstruction.ReadFloat
         } );
 
         // Sound or particle effect
         packetInstructions.put( (byte) 0x3D, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadByte,
                 PacketInstruction.ReadInt, PacketInstruction.ReadInt, PacketInstruction.ReadBoolean
         } );
 
         // Named sound effect
         packetInstructions.put( (byte)0x3E, new PacketInstruction[] {
             PacketInstruction.ReadString, PacketInstruction.ReadInt, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt, PacketInstruction.ReadFloat, PacketInstruction.ReadByte
         } );
 
         // Particle
         packetInstructions.put( (byte)0x3F, new PacketInstruction[] {
             PacketInstruction.ReadString, PacketInstruction.ReadFloat, PacketInstruction.ReadFloat,
                 PacketInstruction.ReadFloat, PacketInstruction.ReadFloat, PacketInstruction.ReadFloat,
                 PacketInstruction.ReadFloat, PacketInstruction.ReadFloat, PacketInstruction.ReadInt
         } );
 
         // Game state change
         packetInstructions.put( (byte)0x46, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadByte
         } );
 
         // Open window
         packetInstructions.put( (byte)0x64, new PacketInstruction[] {
                 PacketInstruction.ReadOptionalWindow
         } );
 
         // Close window
         packetInstructions.put( (byte)0x65, new PacketInstruction[] {
                 PacketInstruction.ReadByte
         } );
 
         // Click window
         packetInstructions.put( (byte)0x66, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadShort, PacketInstruction.ReadByte,
                 PacketInstruction.ReadShort, PacketInstruction.ReadBoolean, PacketInstruction.ReadItem
         } );
 
         // Set item
         packetInstructions.put( (byte)0x67, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadShort, PacketInstruction.ReadItem
         } );
 
         // Window shit
         packetInstructions.put( (byte)0x68, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadItemArray
         } );
 
         // Update window property
         packetInstructions.put( (byte)0x69, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadShort, PacketInstruction.ReadShort
         } );
 
         // Confirm transaction
         packetInstructions.put( (byte)0x6A, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadShort, PacketInstruction.ReadBoolean
         } );
 
         // Creative inventory action
         packetInstructions.put( (byte)0x6B, new PacketInstruction[] {
             PacketInstruction.ReadShort, PacketInstruction.ReadItem
         } );
 
         // Update sign
         packetInstructions.put( (byte)0x82, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadShort, PacketInstruction.ReadInt,
                 PacketInstruction.ReadString, PacketInstruction.ReadString, PacketInstruction.ReadString,
                 PacketInstruction.ReadString
         } );
 
         // Item data
         packetInstructions.put( (byte)0x83, new PacketInstruction[] {
             PacketInstruction.ReadShort, PacketInstruction.ReadShort, PacketInstruction.ReadUShortByteArray
         } );
 
         // Update tileentity
         packetInstructions.put( (byte)0x84, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadShort, PacketInstruction.ReadInt,
                 PacketInstruction.ReadByte, PacketInstruction.ReadShortByteArray
         } );
         // Tile editor open
         packetInstructions.put( (byte)0x85, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadInt, PacketInstruction.ReadInt,
                 PacketInstruction.ReadInt
         } );
 
         // Wut, not documented? D:
         packetInstructions.put( (byte)0xC3, new PacketInstruction[] {
             PacketInstruction.ReadShort, PacketInstruction.ReadShort, PacketInstruction.ReadIntByteArray
         } );
 
         // Increment statistic
         packetInstructions.put( (byte)0xC8, new PacketInstruction[] {
             PacketInstruction.ReadInt, PacketInstruction.ReadInt
         } );
 
         // Player abilitys
         packetInstructions.put( (byte)0xCA, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadFloat, PacketInstruction.ReadFloat
         } );
 
         // TODO Add CB/Tab complete
 
         // Scoreboard objective
         packetInstructions.put( (byte)0xCE, new PacketInstruction[] {
                 PacketInstruction.ReadString, PacketInstruction.ReadString, PacketInstruction.ReadByte
         } );
 
         // Update score
         packetInstructions.put( (byte)0xCF, new PacketInstruction[] {
             PacketInstruction.ReadString, PacketInstruction.ReadByte, PacketInstruction.ReadString,
                 PacketInstruction.ReadInt
         } );
 
         // Display scoreboard
         packetInstructions.put( (byte)0xD0, new PacketInstruction[] {
             PacketInstruction.ReadByte, PacketInstruction.ReadString
         } );
 
         // Team
         packetInstructions.put( (byte)0xD1, new PacketInstruction[] {
             PacketInstruction.ReadString, PacketInstruction.ReadByte, PacketInstruction.ReadString,
                 PacketInstruction.ReadString, PacketInstruction.ReadString, PacketInstruction.ReadByte,
                 PacketInstruction.ReadStringArray
         } );
 
         //0x14 next, stop here for now, this is boring... :/
     }
 
     public static BasePacket readPacket( short packetId, ByteBuf buf ) {
         BasePacket packet;
         int id = (int)packetId;
         if ( packetId == 0x00 ) {
             packet = new Packet00KeepAlive();
         } else if ( packetId == 0x01 ) {
             packet = new Packet01LoginRequest();
         } else if ( packetId == 0x02 ) {
             packet = new Packet02Handshake();
         } else if ( packetId == 0x03 ) {
             packet = new Packet03Chat();
         } else if ( packetId == 0x06 ) {
             packet = new Packet06SpawnPosition();
         } else if ( packetId == 0x09 ) {
             packet = new Packet09Respawn();
         } else if ( packetId == 0x0B ) {
             packet = new Packet0BPlayerPosition();
         } else if ( packetId == 0x0D ) {
             packet = new Packet0DPositionAndLook();
         } else if ( packetId == 0xC9 ) {
             packet = new PacketC9PlayerListItem();
         } else if ( packetId == 0xCD ) {
             packet = new PacketCDClientStatus();
         } else if ( packetId == 0xD1 ) {
             packet = new PacketD1Team();
         } else if ( packetId == 0xFA ) {
             packet = new PacketFAPluginMessage();
         } else if ( packetId == 0xFC ) {
             packet = new PacketFCEncryptionResponse();
         } else if ( packetId == 0xFD ) {
             packet = new PacketFDEncryptionRequest();
         } else if ( packetId == 0xFF ) {
             packet = new PacketFFKick();
         } else {
             // Hmm, lets not care about this packet and just read it :P
 
             PacketInstruction[] instructions = packetInstructions.get( (byte)packetId );
             if ( instructions == null ) {
                 System.out.println( "Unknown packet: 0x" + Integer.toHexString( ((byte)packetId) & 0xFF ).toUpperCase() );
             } else {
                 for ( PacketInstruction instruction : instructions ) {
                     instruction.read( buf );
                 }
             }
 
             return null;
         }
         packet.onPacket( buf );
         return packet;
     }
 }
