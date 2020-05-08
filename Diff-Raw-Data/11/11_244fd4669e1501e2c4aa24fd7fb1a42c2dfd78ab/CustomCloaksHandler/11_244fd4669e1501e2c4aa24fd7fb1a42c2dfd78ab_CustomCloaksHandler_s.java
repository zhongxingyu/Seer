 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.client;
 
 import com.google.common.base.Joiner;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.ObfuscationReflectionHelper;
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import monnef.core.Config;
 import monnef.core.Reference;
 import monnef.core.utils.WebHelper;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.AbstractClientPlayer;
 import net.minecraft.client.renderer.IImageBuffer;
 import net.minecraft.client.renderer.ThreadDownloadImageData;
 import net.minecraft.client.renderer.texture.ITextureObject;
 import net.minecraft.client.renderer.texture.TextureManager;
 import net.minecraft.util.ResourceLocation;
 import net.minecraft.util.StringUtils;
 import net.minecraftforge.client.event.RenderPlayerEvent;
 import net.minecraftforge.event.entity.EntityJoinWorldEvent;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import static monnef.core.MonnefCorePlugin.Log;
 
 // partially based on mDiyo's EventCloakRender
 public class CustomCloaksHandler {
     private static final String JAFFA_CLOAK_URL_BASE = Reference.URL_JAFFAS + "/skin/cloak/_get.php";
     private static final String JAFFA_CLOAK_URL = JAFFA_CLOAK_URL_BASE + "?name=%s";
     private static final boolean SHOW_CLOAK_DEBUG_MESSAGES = false; // WARNING: it's really spammy!
 
     private static Set<String> specialNames = new HashSet<String>();
     private Set<AbstractClientPlayer> processedPlayers = new HashSet<AbstractClientPlayer>();
     private Map<String, ResourceLocation> nameToResLoc = new HashMap<String, ResourceLocation>();
 
     public CustomCloaksHandler() {
         ArrayList<String> tmp = new ArrayList<String>();
         WebHelper.getLinesTillFooter(JAFFA_CLOAK_URL_BASE, tmp);
         specialNames.clear();
         specialNames.addAll(tmp);
         Log.printFine("special names: " + Joiner.on(", ").join(specialNames));
     }
 
     public String getCloakUrl(UUID uuid) {
         return String.format(JAFFA_CLOAK_URL, uuid.toString());
     }
 
     @SubscribeEvent
     public void onPreRenderSpecials(RenderPlayerEvent.Specials.Pre event) {
         if (Config.areDisabledCloaksWithShaders() && Loader.isModLoaded("shadersmod")) {
             return;
         }
         if (event.entityPlayer instanceof AbstractClientPlayer) {
             AbstractClientPlayer player = (AbstractClientPlayer) event.entityPlayer;
 
             if (!processedPlayers.contains(player)) {
                 String playerName = player.getDisplayName();
                 Log.printFine("Got question on cloak for [" + playerName + "].");
                 processedPlayers.add(player);
 
                 if (!specialNames.contains(playerName)) {
                     return;
                 }
 
                 String cloakURL = getCloakUrl(player.getUniqueID());
                 if (SHOW_CLOAK_DEBUG_MESSAGES)
                     Log.printDebug("Setting cloak for [" + playerName + "]");
 
                 setCloak(player, playerName, cloakURL);
 
                 event.renderCape = true;
             }
         }
     }
 
     private void setCloak(AbstractClientPlayer player, String playerName, String cloakURL) {
         ResourceLocation resLoc = nameToResLoc.get(playerName);
         if (resLoc == null) {
             resLoc = new ResourceLocation("jaffa_cloaks/" + StringUtils.stripControlCodes(playerName));
             nameToResLoc.put(playerName, resLoc);
         }
 
         //TODO: remove reflection?
         Boolean capeAlreadyLoaded = ObfuscationReflectionHelper.getPrivateValue(ThreadDownloadImageData.class, player.getTextureCape(), "textureUploaded", "field_110559_g");
         if (capeAlreadyLoaded) {
             if (SHOW_CLOAK_DEBUG_MESSAGES)
                 Log.printDebug("Skipping cape set for [" + playerName + "]");
             return;
         }
 
         ThreadDownloadImageData obj = getDownloadImage(resLoc, cloakURL, null, new CapeImageBuffer());
 
         ObfuscationReflectionHelper.setPrivateValue(AbstractClientPlayer.class, player, obj, "downloadImageCape", "field_110315_c");
         ObfuscationReflectionHelper.setPrivateValue(AbstractClientPlayer.class, player, resLoc, "locationCape", "field_110313_e");
     }
 
     // from AbstractClientPlayer
     private static ThreadDownloadImageData getDownloadImage(ResourceLocation par0ResourceLocation, String par1Str, ResourceLocation par2ResourceLocation, IImageBuffer par3IImageBuffer) {
         TextureManager texturemanager = Minecraft.getMinecraft().getTextureManager();
         ITextureObject object = new ThreadDownloadImageData(par1Str, par2ResourceLocation, par3IImageBuffer);
         texturemanager.loadTexture(par0ResourceLocation, (ITextureObject) object);
 
         return (ThreadDownloadImageData) object;
     }
 
     private void purgeCloakCache() {
         if (SHOW_CLOAK_DEBUG_MESSAGES)
             Log.printDebug("Purging cloaks cache.");
         processedPlayers.clear();
     }
 
     @SubscribeEvent
     public void onEntityJoinWorld(EntityJoinWorldEvent event) {
         purgeCloakCache();
     }
 }
