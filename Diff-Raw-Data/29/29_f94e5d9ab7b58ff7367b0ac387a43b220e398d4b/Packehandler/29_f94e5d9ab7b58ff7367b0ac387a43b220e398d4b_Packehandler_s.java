 package Enderware.network;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.logging.Level;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.server.MinecraftServer;
 import Enderware.client.config.SettingsAdabterClient;
 import Enderware.client.gui.GuiRestart;
 import Enderware.config.ModSetting;
 import Enderware.config.SettingsAdapterServer;
 import Enderware.permissions.PermissionManager;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.IPacketHandler;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 
 
 public class Packehandler implements IPacketHandler{
 private static boolean toshoutdown = false;
 	@Override
 	public void onPacketData(INetworkManager manager,
 			Packet250CustomPayload packet, Player player) {
 	    if(packet.channel.equalsIgnoreCase("shoutdown")){
 	        toshoutdown = true;
 	    }
 		if(packet.channel.equalsIgnoreCase("ModSettings")){
		    if(FMLCommonHandler.instance().getEffectiveSide().isServer()&&!Minecraft.getMinecraft().isSingleplayer()&& PermissionManager.instance.hasPlayerPermission(((EntityPlayer)player).username, "ChangeConfigs")){
 		        MinecraftServer.logger.log(Level.WARNING, "The Player "+((EntityPlayer)player).username+"tried to change a config without acess");
 		        return;
 		    }
 		    
 			 DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
 			 try {
 			   
 			    
 			     
 			     inputStream.reset(); 
 			    String filetemp = inputStream.readUTF();
 			    String categorytemp =inputStream.readUTF();
 			     if(!ModSetting.AllSettings.containsKey(filetemp) || !ModSetting.AllSettings.get(filetemp).categorys.containsKey(categorytemp)||!ModSetting.AllSettings.get(filetemp).categorys.get(categorytemp).containsKey(inputStream.readUTF())){
 			         MinecraftServer.logger.log(Level.WARNING, "The Player "+((EntityPlayer)player).username+"tried to change a not existing config!");
 			         return;
 			     }
 			     inputStream.reset(); 
 			         if(FMLCommonHandler.instance().getEffectiveSide().isClient()&&!Minecraft.getMinecraft().isSingleplayer()){
                
                 if(ModSetting.AllSettings.get(inputStream.readUTF()).categorys.get(inputStream.readUTF()).get(inputStream.readUTF()).equals(inputStream.readUTF())){
                  
                  return;
                  }
 			             if(toshoutdown){
 			             new Thread(new endthread()).start();
 			             toshoutdown = false;
 			             }
                  }else{
                      
                  }
                
              
              
              
           
 			         inputStream.reset(); 
 				ModSetting.AllSettings.get(inputStream.readUTF()).save(inputStream.readUTF(), inputStream.readUTF(),inputStream.readUTF());
 				
 				
 			
 			
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				
 			}
 			 return;
 		}
 		
 		   Side side = FMLCommonHandler.instance().getEffectiveSide();
 		    if (side.isServer()) 
 		if(((SettingsAdapterServer) SettingsAdapterServer.instance()).handlePacket(packet)){return;}
 		    if(side == Side.CLIENT)
 		if(((SettingsAdabterClient)SettingsAdabterClient.instance()).handlePacket(packet)){return;}
 		
 	}
 	protected boolean isAdmin(String username){
 	     @SuppressWarnings("unchecked")
         Iterator<String> i = MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer()).getOps().iterator();
 	     while (i.hasNext()) {
            if(i.next().equals(username))return true;
             
         }
 	    return false;
 	
 	
 	}
 	class endthread implements Runnable{
 	    @Override
 	    public void run() {
 	        GuiRestart.displayGuiRestart("Config Changed");
             try {
                 Thread.sleep(5000);
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
             Minecraft.getMinecraft().shutdown();
 
 	    }
 	}
 
 }
