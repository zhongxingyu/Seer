 package fi.haju.haju3d.client.chunk.light;
 
 import fi.haju.haju3d.protocol.coordinate.LocalTilePosition;
 import fi.haju.haju3d.protocol.world.ByteArray3d;
 
 public final class ChunkLighting {
 
   private final static byte LIGHT_MASK = (byte)0x7F;
  private final static byte SOURCE_MASK = (byte)0x80;
   
   private final ByteArray3d light;
   
   public ChunkLighting(int chunkSize) {
     light = new ByteArray3d(chunkSize, chunkSize, chunkSize);
   }
 
   public int getLight(LocalTilePosition pos) {
     if (!light.isInside(pos)) return 0;
     int val = light.get(pos) & LIGHT_MASK;
     return val < ChunkLightManager.AMBIENT ? ChunkLightManager.AMBIENT : val;
   }
 
   public void setLight(LocalTilePosition pos, int lightValue) {
    light.set(pos, (byte)((byte)lightValue & (byte)LIGHT_MASK));
   }
   
   public boolean isLightSource(LocalTilePosition pos) {
     if (!light.isInside(pos)) return false;
     return (light.get(pos) & SOURCE_MASK) != 0;
   }
   
   public void setLightSource(LocalTilePosition pos, boolean isSource) {
    byte l = light.get(pos);
     if(isSource) {
       l = (byte)(l | SOURCE_MASK);
     } else {
       l = (byte)(l & LIGHT_MASK);
     }
   }
 
 }
