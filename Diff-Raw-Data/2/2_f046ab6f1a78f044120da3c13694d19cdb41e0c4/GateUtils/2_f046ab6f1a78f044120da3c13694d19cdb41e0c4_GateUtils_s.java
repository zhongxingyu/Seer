 package denoflionsx.GateCopy.Utils;
 
 import buildcraft.api.gates.ActionManager;
 import buildcraft.transport.TileGenericPipe;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.TileEntity;
 
 public class GateUtils {
 
     public static boolean isGateAutarchic(TileEntity pipe) {
         NBTTagCompound p = new NBTTagCompound();
         pipe.writeToNBT(p);
        return p.hasKey("Pulser");
     }
     
     public static void attachPulser(NBTTagCompound save, NBTTagCompound pulser){
         save.getCompoundTag("Gate").setCompoundTag("Pulser", pulser);
     }
     
     public static NBTTagCompound extractPulser(TileGenericPipe pipe){
         NBTTagCompound p = new NBTTagCompound();
         pipe.writeToNBT(p);
         NBTTagCompound Pulser = p.getCompoundTag("Gate").getCompoundTag("Pulser");
         return Pulser;
     }
 
     public static void cleanNBTData(NBTTagCompound tag) {
         for (int i = 0; i < 4; i++) {
             tag.setBoolean("wireSet[" + i + "]", false);
         }
         for (int i = 0; i < 5; i++) {
             tag.setInteger("facadeBlocks[" + i + "]", 0);
             tag.setInteger("facadeMeta[" + i + "]", 0);
         }
         if (tag.getCompoundTag("Gate").hasKey("Pulser")) {
             tag.getCompoundTag("Gate").removeTag("Pulser");
         }
     }
 
     public static void mergeNBT(NBTTagCompound pipe, NBTTagCompound paste) {
         int x = pipe.getInteger("x");
         int y = pipe.getInteger("y");
         int z = pipe.getInteger("z");
         paste.setInteger("x", x);
         paste.setInteger("y", y);
         paste.setInteger("z", z);
     }
 
     public static boolean isMatchingGateForPaste(TileGenericPipe pipe, NBTTagCompound paste) {
         NBTTagCompound p = new NBTTagCompound();
         pipe.writeToNBT(p);
         int sourceID = p.getCompoundTag("Gate").getInteger("Kind");
         int compare = paste.getCompoundTag("GateType").getInteger("ID");
         if (compare == sourceID) {
             return true;
         } else {
             return false;
         }
     }
 
     public static String getActionName(int id) {
         return ActionManager.actions[id].getDescription();
     }
 
     public static String getTriggerName(int id) {
         return ActionManager.triggers[id].getDescription();
     }
 }
