 package denoflionsx.DenPipes.Proxy;
 
 import buildcraft.transport.ItemPipe;
 import buildcraft.transport.TransportProxyClient;
 import net.minecraftforge.client.MinecraftForgeClient;
 
 public class ProxyClient extends ProxyCommon {
 
     @Override
     public void registerPipeRendering(ItemPipe pipe) {
        MinecraftForgeClient.registerItemRenderer(pipe.itemID, TransportProxyClient.pipeItemRenderer);
     }
 }
