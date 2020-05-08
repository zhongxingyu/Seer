 package jk_5.nailed.network.handlers;
 
 import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
 import cpw.mods.fml.common.network.NetworkHandshakeEstablished;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.relauncher.Side;
 import io.netty.channel.ChannelHandlerContext;
 import io.netty.channel.ChannelInboundHandlerAdapter;
 import jk_5.nailed.NailedServer;
 import jk_5.nailed.map.Map;
 import jk_5.nailed.map.MapLoader;
 import jk_5.nailed.network.MinecraftPacketAdapter;
 import net.minecraft.network.NetHandlerPlayServer;
 import net.minecraftforge.common.network.ForgeMessage;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class FMLHandshakeHandler extends ChannelInboundHandlerAdapter {
 
     @Override
     public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception{
         if(evt instanceof NetworkHandshakeEstablished){
            NetworkHandshakeEstablished event = (NetworkHandshakeEstablished) evt;
            NetHandlerPlayServer handler = (NetHandlerPlayServer) event.netHandler;
             FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
            channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(event.dispatcher);
             for(Map map : MapLoader.instance().getMaps()){
                 if(map.getID() >= -1 && map.getID() <= 1){
                     continue;
                 }
                channel.writeOutbound(new ForgeMessage.DimensionRegisterMessage(map.getID(), NailedServer.getProviderID()));
             }
 
             //Try to hack a handler into Minecraft's main pipeline so we can change some packets before they are sent
             handler.field_147371_a.channel().pipeline().addAfter("encoder", "NailedPacketAdapter", new MinecraftPacketAdapter());
         }
         ctx.fireUserEventTriggered(evt);
     }
 }
