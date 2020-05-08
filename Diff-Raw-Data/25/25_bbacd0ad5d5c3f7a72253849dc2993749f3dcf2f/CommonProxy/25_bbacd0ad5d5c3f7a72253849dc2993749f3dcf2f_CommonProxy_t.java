 /**
  * This Code is Open Source and distributed under a
  * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
  * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
  */
 // Created @ 23 Dec 2012
 package vazkii.tinkerer.core;
 
 import java.awt.Color;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import vazkii.tinkerer.handler.TickHandler;
 import vazkii.tinkerer.helper.MiscHelper;
 import vazkii.tinkerer.helper.PacketHelper;
 import vazkii.tinkerer.network.packet.PacketClientSpells;
 import vazkii.tinkerer.network.packet.PacketCompleteResearch;
 import vazkii.tinkerer.network.packet.PacketElementalDeskSync;
 import vazkii.tinkerer.network.packet.PacketElementalistTinkeringAltarStartRecipe;
 import vazkii.tinkerer.network.packet.PacketElementalistTinkeringAltarSync;
 import vazkii.tinkerer.network.packet.PacketKeybind;
 import vazkii.tinkerer.reference.AnnotationConstants;
 import vazkii.tinkerer.reference.BlockNames;
 import vazkii.tinkerer.tile.TileEntityAttuner;
 import vazkii.tinkerer.tile.TileEntityCatalystCapsule;
 import vazkii.tinkerer.tile.TileEntityElementalDesk;
 import vazkii.tinkerer.tile.TileEntityElementalTinkeringAltar;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 /**
  * CommonProxy
  *
  * Common proxy, used between the client and the server.
  * The client and server proxies extend this class to
  * add functionality only available on the specific side.
  *
  * @see ClientProxy
  * @author Vazkii
  */
 public class CommonProxy {
 
 	/** Registers the tile entities, used by the client to
 	 * also register the renderers **/
 	public void registerTileEntities() {
 		GameRegistry.registerTileEntity(TileEntityElementalDesk.class, AnnotationConstants.MOD_NAME + "_" + BlockNames.ELEMENTAL_DESK_NAME);
 		GameRegistry.registerTileEntity(TileEntityCatalystCapsule.class, AnnotationConstants.MOD_NAME + "_" + BlockNames.CATALYST_CAPSULE_NAME);
 		GameRegistry.registerTileEntity(TileEntityElementalTinkeringAltar.class, AnnotationConstants.MOD_NAME + "_" + BlockNames.ELEMENTALIST_TINKERING_ALTAR_NAME);
 		GameRegistry.registerTileEntity(TileEntityAttuner.class, AnnotationConstants.MOD_NAME + "_" + BlockNames.ATTUNER_NAME);
 	}
 
 	/** Registers the mod's packets. Some packets use different
 	 * instances per side. **/
 	public void registerPackets() {
 		PacketHelper.packetHandlers.add(PacketElementalDeskSync.RECIEVER_INSTANCE);
 		PacketHelper.packetHandlers.add(PacketElementalistTinkeringAltarSync.RECIEVER_INSTANCE);
 		PacketHelper.packetHandlers.add(PacketElementalistTinkeringAltarStartRecipe.RECIEVER_INSTANCE);
 		PacketHelper.packetHandlers.add(PacketCompleteResearch.RECIEVER_INSTANCE);
 		PacketHelper.packetHandlers.add(PacketClientSpells.RECIEVER_INSTANCE);
 		PacketHelper.packetHandlers.add(PacketKeybind.INSTANCE);
 	}
 
 	/** Registers the tick handler **/
 	public void registerTickHandler() {
 		TickRegistry.registerTickHandler(TickHandler.INSTANCE, Side.SERVER);
 	}
 
 	/** Registers any handlers exclusive to the client side,
 	 * obviously NO-OP in server side. **/
 	public void registerClientHandlers() {
 		// NO-OP
 	}
 
 	/** Used by the client to map entity classes to their adequate
 	 *  render instances. NO-OP in server side. **/
 	public void mapEntityRenderers() {
 		// NO-OP
 	}
 
 	/** Registers various block Renders. NO-OP in server side **/
 	public void registerBlockRenders() {
 		//NO-OP
 	}
 
 	/** Preloads the textures, to avoid render glitches, NO-OP
 	 * in server side **/
 	public void preloadTextures() {
 		// NO-OP
 	}
 
 	/** Reads the research descriptions, used to show the text
 	 * for the researches in the Elementalist's Lexicon GUI,
 	 * NO-OP in server side. **/
 	public void readResearchDescriptions() {
 		// NO-OP
 	}
 
 	/** Initializes the client spells, this is called when
 	 * a spell packet is recieved, NO-OP in server side,
 	 * because similar packets are sent for both sides **/
 	public void initClientSpells() {
 		// NO-OP
 	}
 
 	/** Sets the on-screen tooltip (simlar to the one that shows up
 	 * when you change items, but higher up and with a different
 	 * render method), NO-OP in server side. **/
 	public void setItemOnScreenTooltip(String tooltip) {
 		// NO-OP
 	}
 
 	/** Gets how many game ticks have elapsed. This calls different fields per tick handler **/
 	public long getGameTicksElapsed() {
 		return TickHandler.elapsedTicks;
 	}
 
 	/** Gets a player entity from the name passed in, this has different implementations for both
 	 * client and server side of course. **/
 	public EntityPlayer getPlayerAsEntity(String player) {
 		return MiscHelper.getServer().getConfigurationManager().getPlayerForUsername(player);
 	}
	
	/** Checks if the current server is PVP, always false on client, as
	 * damage calculations happen on the server side. **/
	public boolean isServerPVP() {
		return MiscHelper.getServer().isPVPEnabled();
	}
 
 	/** Used by the client to spawn a colored portal particle at
 	 * the location passed in, with the color and motion passed
 	 * in. NO-OP in server side. **/
 	public void spawnColoredPortalParticle(Color color, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
 		// NO-OP
 	}
 
 	/** Used by the client to spawn a steam particle at
 	 * the location passed in, with the motion passed
 	 * in. NO-OP in server side. **/
 	public void spawnSteamParticle(World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
 		// NO-OP
 	}
 }
