 package com.minecarts.familyjewels;
 
 
 import net.minecraft.server.*;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 
 import java.text.MessageFormat;
 import java.util.zip.DataFormatException;
 import java.util.zip.Deflater;
 import java.util.zip.Inflater;
 
 public class NetServerHandlerHook extends net.minecraft.server.NetServerHandler {
     private EntityPlayer player;
     public final int[] hiddenBlocks = {14,15,16,21,48,52,54,56,73,74};
     private Field packetSize;
 
     public NetServerHandlerHook(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer player){
         super(minecraftserver,networkmanager,player);
         this.player = player;
 
         //Setup our reflection to access the compressed data size
         try{
             packetSize = Packet51MapChunk.class.getDeclaredField("h");
             packetSize.setAccessible(true);
         } catch (NoSuchFieldException e){
             e.printStackTrace();
             System.out.println("FamilyJewels> Unable to find compressed data size field! Did the packet structure change?");
         }
     }
 
     @Override
     public void sendPacket(Packet packet){
          if(packet instanceof Packet14BlockDig){
              Packet14BlockDig dataPacket = (Packet14BlockDig) packet;
              if(dataPacket.e == 2){ //If it's a block break
                  int x = dataPacket.a;
                  int y = dataPacket.b;
                  int z = dataPacket.c;
                  //Mark the nearby blocks as dirty so that any hidden blocks will be shown
                  player.world.notify(x + 1, y, z);
                  player.world.notify(x - 1, y, z);
                  player.world.notify(x, y + 1, z);
                  player.world.notify(x, y - 1, z);
                  player.world.notify(x, y, z - 1);
                  player.world.notify(x, y, z + 1);
              }
          } else if(packet instanceof Packet51MapChunk){
              Packet51MapChunk dataPacket = (Packet51MapChunk) packet;
 
              Inflater inflater = new Inflater();
              Deflater deflater = new Deflater(-1);
 
              int origDataSize = 0;
              int newDataSize = 0;
              int actualDataSize = 0;
 
              try{
                 origDataSize = packetSize.getInt(dataPacket);
              } catch (Exception e){
                  System.out.println("Unable to get data size");
              }
 
              //Decompres the data so we can overwrite it
              inflater.setInput(dataPacket.g);
              try { inflater.inflate(dataPacket.g); }
              catch (DataFormatException dataformatexception) { System.out.println("FamilyJewels> Bad compressed data format"); return; }
              finally { inflater.end(); }
 
             //System.out.println(MessageFormat.format("[PLUGIN] Positions: ({0},{1},{2}), Max: ({3},{4},{5}), DataSize: {6}",xPosition,yPosition,zPosition,xSize,ySize,zSize,dataPacket.g.length));
              this.breakPacketIntoChunks(dataPacket.a,dataPacket.b,dataPacket.c,dataPacket.d,dataPacket.e,dataPacket.f,dataPacket.g);
 
             //Recompress the data
              try{
                  deflater.setInput(dataPacket.g);
                  deflater.finish();
                  actualDataSize = deflater.deflate(dataPacket.g);
                  packetSize.setInt(dataPacket,actualDataSize); //Reflection to access this private value >:(
              } catch (Exception e){ System.out.println("FamilyJewels> Failed to recompress data:" + e.getMessage()); }
              finally { deflater.end(); }
 
              //if(newDataSize > origDataSize){
                 //System.out.println("Did it crash? Orig Size: " + origDataSize + " vs new size: " + newDataSize + " vs actual:" + actualDataSize);
              //}
 
              super.sendPacket(dataPacket);
              return;
         }
         super.sendPacket(packet);
     }//sendPacket()
 
     private int replaceUnlitBlocks(Chunk chunk,int xPos, int yPos, int zPos, int xSize, int ySize, int zSize,int k1, byte abyte[]){
         int tracker = 0;
         byte[] newArray; //Create a temporary array that we're going to store our modified data in
         if(ySize == 128){ newArray = new byte[(xSize-xPos) * (ySize-yPos) * (zSize-zPos)]; }
         else { newArray = new byte[(ySize-yPos)]; }
         
         //Loop over all the blocks in this chunk
         for(int x=xPos; x<xSize || x==xPos; x++){
             for(int z=zPos; z<zSize || z==zPos; z++){
                 tracker = 0;
                 for(int y=yPos; y<ySize || y==yPos; y++){
                     int index = tracker++; //For partial chunk updates, we only loop over the y values in this function
                     if(ySize == 128){ index = (x << 11 | z << 7 | y); } //Use a different index if it's a full chunk update

                    if(index >= newArray.length) continue; //WorldEditing blocks outside the map can cause out of boudns errors

                     int type = chunk.getTypeId(x,y,z);
                     if(Arrays.binarySearch(this.hiddenBlocks, type) >= 0){
                         boolean set = false;
                         CHECKLIGHT: //Check the lighting propagation around the block
                         {
                             if(this.getLightLevel(chunk, x + 1, y, z) > 0) break CHECKLIGHT;
                             if(this.getLightLevel(chunk, x - 1, y, z) > 0) break CHECKLIGHT;
                             if(this.getLightLevel(chunk, x, y + 1, z) > 0) break CHECKLIGHT;
                             if(this.getLightLevel(chunk, x, y - 1, z) > 0) break CHECKLIGHT;
                             if(this.getLightLevel(chunk, x, y, z + 1) > 0) break CHECKLIGHT;
                             if(this.getLightLevel(chunk, x, y, z - 1) > 0) break CHECKLIGHT;
                             if(this.getLightLevel(chunk, x, y, z) > 0) break CHECKLIGHT;
                             //System.out.println(MessageFormat.format("Replaced: Type: {9}, XYZ: {0},{1},{2}, XYZ Start: {3},{4},{5}, XYZ End: {6},{7},{8}", x,y,z,i,j,k,l,i1,j1,type));
                             newArray[index] = ((byte)(1 & 0xff));
                             set = true;
                         }
                         if(!set) newArray[index] = ((byte)(type & 0xff));
                     } else {
                         newArray[index] = ((byte)(type & 0xff));
                     }
                 }
             }
         }
         //Copy our temporary generated array data into the packet data field (abyte)
         System.arraycopy(newArray, 0, abyte, k1, newArray.length);
         return k1 + newArray.length;
     }
     public int getLightLevel(Chunk chunk, int x, int y, int z){
         //We have to use the world.getLight because sometimes the lighting will cross chunks / updates
         //  which leaves us with missing ores and generally cause issues.
         return player.world.getLightLevel((chunk.x << 4) | (x & 0xF), y & 0x7F, (chunk.z << 4) | (z & 0xF));
     }
 
 
     //This is done because the arrays are concatinated together inside the packet
     //  we can't directly access data for a given x,y,z because we don't know where in the packet
     //  it is without going through this, this is the getMultiChunkData() function
     private void breakPacketIntoChunks(int i, int j, int k, int l, int i1, int j1, byte abyte0[]){
         int k1 = i >> 4;
         int l1 = k >> 4;
         int i2 = i + l - 1 >> 4;
         int j2 = k + j1 - 1 >> 4;
         int k2 = 0;
         int l2 = j;
         int i3 = j + i1;
 
         if(l2 < 0) { l2 = 0; }
         if(i3 > 128) { i3 = 128; }
 
         for (int j3 = k1; j3 <= i2; ++j3) {
             int k3 = i - j3 * 16;
             int l3 = i + l - j3 * 16;
 
             if(k3 < 0) { k3 = 0; }
             if(l3 > 16) { l3 = 16; }
 
             for(int i4 = l1; i4 <= j2; ++i4) {
                 int j4 = k - i4 * 16;
                 int k4 = k + j1 - i4 * 16;
 
                 if(j4 < 0) { j4 = 0; }
                 if(k4 > 16) { k4 = 16; }
 
                 Chunk chunk = player.world.getChunkAt(j3, i4);
                 //System.out.println(MessageFormat.format("[PLUGIN] Chunk: {0},{1} - Sizes: ({2},{3},{4}) -> ({5},{6},{7}), K2: {8}",j3,i4,k3,l2,j4,l3,i3,k4,k2));
                 if(i1 == 128){ //It's a full chunk update
                     k2 = replaceUnlitBlocks(chunk,k3, l2, j4, l3, i3, k4, k2,abyte0);
                 } else { //Partial chunk update
                     for (int subchunkx = k3; subchunkx < l3; ++subchunkx) {
                         for (int subchunkz = j4; subchunkz < k4; ++subchunkz) {
                             k2 = this.replaceUnlitBlocks(chunk,subchunkx,l2,subchunkz,subchunkx,i3,subchunkz,k2,abyte0);
                         }
                     }
                 }
             }
         }
     }
 }
