 package com.dns.core.handlers;
 
 import java.util.EnumSet;
 import java.util.List;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.AbstractClientPlayer;
 import net.minecraft.client.renderer.ThreadDownloadImageData;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.StringUtils;
 import net.minecraft.world.World;
 
 import com.dns.configuration.DataProxy;
 import com.dns.lib.Reference;
 
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.common.ITickHandler;
 import cpw.mods.fml.common.TickType;
 
 public class TickHandler implements ITickHandler {
 
     private int tickCount = DataProxy.delay;
     private String label = Reference.updaterName.toUpperCase().replace(" ", "_") + "_TICKHANDLER";
     private Minecraft mc;
 
     @Override
     public void tickStart(EnumSet<TickType> type, Object... tickData) {
 
     }
 
     @Override
     public void tickEnd(EnumSet<TickType> type, Object... tickData) {
 
         mc = Minecraft.getMinecraft();
 
         updater();
         
         if(allowCape()) {
         
         	cape();
         }
     }
 
     @SuppressWarnings("rawtypes")
     private void cape() {
     	
     	// Needs Re-writing
     	
     	World world = mc.theWorld;
     	AbstractClientPlayer player = null;
     	
     	if(world != null && world.playerEntities.size() > 0) {
     		
     		List<AbstractClientPlayer> players = world.playerEntities;
     		
     		for(int i = 0; i < world.playerEntities.size(); i++) {
     			
     			if(players.get(i) != null) {
     				
     				player = players.get(i);
     				
    				if(Reference.staffList.contains(player.username.toLowerCase())) {
     					
     					// Give Admin Cape
 //    					ResourceLocation capeRL = new ResourceLocation("dnscapes/" + StringUtils.stripControlCodes(player.username));
 //    					ThreadDownloadImageData capeThread = CapeHandler.getDownloadThread(capeRL, CapeHandler.getAdminCape());
     					
 //    					player.field_110315_c = capeThread;
     				} else {
     					
     					// Give User cape
 //    					ResourceLocation capeRL = new ResourceLocation("dnscapes/" + StringUtils.stripControlCodes(player.username));
 //    					ThreadDownloadImageData capeThread = CapeHandler.getDownloadThread(capeRL, CapeHandler.getUserCape());
     					
 //    					player.field_110315_c = capeThread;
     				}
     			}
     		}
     	}
     	
 //        if(mc.theWorld != null && mc.theWorld.playerEntities.size() > 0) {
 //
 //            List players = mc.theWorld.playerEntities;
 //
 //            for(int counter = 0; counter < players.size(); counter++) {
 //
 //                if(players.get(counter) != null) {
 //
 //                    EntityPlayer thePlayer = (EntityPlayer) players.get(counter);
 //                    String oldCloak = thePlayer.cloakUrl;
 //                    String newCloak;
 //
 //                    if(Reference.staffList.contains(thePlayer.username.toLowerCase())) {
 //                    	
 //                    	newCloak = CapeHandler.getAdminCape();
 //                    } else {
 //                    	
 //                    	newCloak = CapeHandler.getUserCape();
 //                    }
 //                    
 //                    thePlayer.cloakUrl = newCloak;
 //                    /*for(String staff : Reference.staff) {
 //
 //                        if(thePlayer.username.equalsIgnoreCase(staff)) {
 //
 //                            String newCloakUrl;
 //
 //                            newCloakUrl = CapeHandler.getAdminCape();
 //
 //                            thePlayer.cloakUrl = newCloakUrl;
 //                            break;
 //                        } else {
 //
 //                            String newCloakUrl = CapeHandler.getUserCape();
 //                            thePlayer.cloakUrl = newCloakUrl;
 //                        }
 //                    }*/
 //
 //                    if(thePlayer.cloakUrl != oldCloak && !Reference.isOffline) {
 //
 //                        mc.renderEngine.obtainImageData(thePlayer.cloakUrl, new CapeDownloadHandler());
 //                    }
 //                }
 //            }
 //        }
     }
 
     private void updater() {
 
         if(!VersionHandler.isUpdated()) {
 
             if(FMLClientHandler.instance().getClient().thePlayer != null) {
 
                 if(tickCount == 0) {
 
                     System.out.println("[" + Reference.updaterName + "] There is a new update out: " + VersionHandler.getRemoteVersion() + " (Current Version: " + VersionHandler.getLocalVersion()
                             + ")");
                     String url = VersionHandler.packURL;
                     ChatHandler.sendChat(Reference.colour + "[" + Reference.updaterName + Reference.colour + "] Version " + VersionHandler.getRemoteVersion() + " is available now. You have " + url);
                     ChatHandler.sendChat(VersionHandler.getInfo());
                     tickCount = -1;
                 } else {
 
                     --tickCount;
                 }
             }
         }
     }
 
     @Override
     public EnumSet<TickType> ticks() {
 
         return EnumSet.of(TickType.CLIENT);
     }
 
     @Override
     public String getLabel() {
 
         return label;
     }
     
     private boolean allowCape() {
         
         return Reference.allowCape;
     }
 }
