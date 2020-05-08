 package pickitup;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 
 public class ClientProxy extends CommonProxy {
     // Called from the coremod's Hook.FORCE_SNEAK.
     public static boolean amIHoldingABlock() {
         return PickItUp.amIHoldingABlock();
     }
 
     // Called from the coremod's Hook.RENDER_HELD_BLOCK.
     public static void renderHeldBlock(int render_pass, double partialTick) {
         if (render_pass == 1) {
             FakeWorld.renderHeldBlock(partialTick);
         }
     }
 
     public double getReach(EntityPlayer player) {
         Minecraft mc = Minecraft.getMinecraft();
         return (double)mc.playerController.getBlockReachDistance();
     }
 }
