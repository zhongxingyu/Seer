 package denoflionsx.DenPipes.Proxy;
 
 import buildcraft.transport.ItemPipe;
 import buildcraft.transport.TransportProxyClient;
import net.minecraft.item.Item;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 public class ProxyClient extends ProxyCommon {
 
     @Override
     public void registerPipeRendering(ItemPipe pipe) {
        if (Item.itemsList[pipe.itemID] instanceof ItemPipe) {
            MinecraftForgeClient.registerItemRenderer(pipe.itemID, TransportProxyClient.pipeItemRenderer);
        }
     }
 }
