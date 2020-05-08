 package com.spacechase0.minecraft.usefulpets;
 
 import com.spacechase0.minecraft.usefulpets.entity.*;
 import com.spacechase0.minecraft.usefulpets.item.*;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraftforge.common.Configuration;
 
 @Mod( modid = "SC0_UsefulPets", name = "Useful Pets", version = "0.1" )
 @NetworkMod( clientSideRequired = true, serverSideRequired = false )
 public class UsefulPets
 {
 	@Instance( "SC0_UsefulPets" )
 	public static UsefulPets instance;
 	
 	@SidedProxy( serverSide = "com.spacechase0.minecraft.usefulpets.CommonProxy",
 			     clientSide = "com.spacechase0.minecraft.usefulpets.client.ClientProxy" )
 	public static CommonProxy proxy;
 	
 	@EventHandler
 	void preInit( FMLPreInitializationEvent event )
 	{
 		config = new Configuration( event.getSuggestedConfigurationFile() );
 		config.load();
 	}
 	
 	@EventHandler
 	void init( FMLInitializationEvent event )
 	{
 		registerItems();
 		registerEntities();
 		registerLanguage();
 		proxy.registerRenderers();
 	}
 	
 	@EventHandler
 	void postInit( FMLPostInitializationEvent event )
 	{
 		config.save();
 	}
 	
 	private void registerItems()
 	{
 		converter = new ConverterItem( getItemId( "converter", 0 ) );
 	}
 	
 	private void registerEntities()
 	{
 		petEntityId = EntityRegistry.findGlobalUniqueEntityId();
 		EntityRegistry.registerGlobalEntityID( PetEntity.class, "Pet", petEntityId );
 	}
 	
 	private void registerLanguage()
 	{
 		registerLanguage( "en_US" );
 	}
 	
 	private int getItemId( String name, int itemNum )
 	{
 		return config.getItem( name, ITEM_ID_BASE + itemNum ).getInt();
 	}
 	
 	private void registerLanguage( String lang )
 	{
 		LanguageRegistry.instance().loadLocalization( "/assets/usefulpets/lang/" + lang + ".lang", lang, false );
 	}
 	
 	public ConverterItem converter;
 	
 	public int petEntityId;
 	
 	private Configuration config;
	private static final int ITEM_ID_BASE = 15764;
 }
