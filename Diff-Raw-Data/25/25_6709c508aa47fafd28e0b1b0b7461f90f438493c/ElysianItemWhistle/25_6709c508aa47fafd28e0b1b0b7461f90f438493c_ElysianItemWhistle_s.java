 package mods.elysium.item;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import cpw.mods.fml.client.FMLClientHandler;
 
 import mods.elysium.Elysium;
 import mods.elysium.ShuffleBag;
 import mods.elysium.proxy.CommonProxy;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityList;
 import net.minecraft.entity.boss.EntityDragon;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.logging.ILogAgent;
 import net.minecraft.network.NetworkListenThread;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.world.EnumGameType;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldProviderEnd;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.biome.BiomeGenEnd;
 import net.minecraft.world.biome.WorldChunkManager;
 import net.minecraftforge.client.MinecraftForgeClient;
 import net.minecraftforge.common.ForgeHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.player.ArrowNockEvent;
 import net.minecraftforge.transformers.ForgeAccessTransformer;
 
 public class ElysianItemWhistle extends ElysianItem
 {
 	private static Random rand = new Random();
 	
 	//Add API support
 	public static ArrayList<String> dragonExist = new ArrayList<String>();
 	public static ArrayList<String> tips = new ArrayList<String>();
 	
 	private long lastPlay = 0;
 
 	public ElysianItemWhistle(int id)
 	{
 		super(id);
 		this.maxStackSize = 1;
         this.setMaxDamage(1);
         
         tips.add("Hmm... looks like a musical instrument");
         tips.add("Its sound could be heared from a long distance...");
         tips.add("Sorry, I can use it now!");
         
         dragonExist.add("Looks like there is a dragon somewhere...");
         dragonExist.add("Oh! There is the Ender Dragon!");
         dragonExist.add("There can't be more dragons in this dimension...");
         dragonExist.add("There should be a dragon somewhere here...");
 	}
 	
 	@Override
 	/**
      * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
      */
     public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entity)
     {
     	if(!world.isRemote)
     	{
    		if((System.currentTimeMillis() - lastPlay) / 1000 > 9D)
     		{
 	    		lastPlay = System.currentTimeMillis();
     			
 				world.playSoundAtEntity(entity, "elysium:flute", 1F, 1F);
				System.out.println("playing whistle sound");
 		    	if(world.getWorldChunkManager().getBiomeGenAt(0, 0) instanceof BiomeGenEnd )
 		    	{
 		    		if(isDragonAlive(world) >= Elysium.MaxDragon)
 		    			entity.addChatMessage(dragonExist.get(rand.nextInt(dragonExist.size())));
 		    	}
 		    	else
 		    	{
 		    		entity.addChatMessage(tips.get(rand.nextInt(tips.size())));
 		    	}
     		}
 		}
//    	else {
//			if((System.currentTimeMillis() - lastPlay) / 1000 > 9D)
//			{
//	    		lastPlay = System.currentTimeMillis();
//			}
//		}
     	entity.setItemInUse(itemStack, this.getMaxItemUseDuration(itemStack));
         return itemStack;
     }
     
     private int isDragonAlive(World world)
     {
     	List list = world.getLoadedEntityList();
     	int dragonNum = 0;
     	
     	for (int i = 0; i < list.size(); i++)
     	{
     		if(list.get(i) instanceof EntityDragon)
     			dragonNum++;
     	}
 		
     	return dragonNum;
 	}
     
     @Override
     public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer entity)
     {
 	    if(!world.isRemote)
 	    {
     		if(world.getWorldChunkManager().getBiomeGenAt(0, 0) instanceof BiomeGenEnd )
 	    	{
	    		itemStack.damageItem(2, entity);
	    		
	    		EntityDragon entitydragon = new EntityDragon(world);
				entitydragon.setLocationAndAngles(0.0D, 128.0D, 0.0D, rand.nextFloat() * 360.0F, 0.0F);
				world.spawnEntityInWorld(entitydragon);
 	    	}
 	    }
 		return itemStack;
     }
     
     @Override
     /**
      * Returns True is the item is renderer in full 3D when hold.
      */
     public boolean isFull3D()
     {
         return true;
     }
     
     @Override
     /**
      * How long it takes to use or consume an item
      */
     public int getMaxItemUseDuration(ItemStack item)
     {
         return 10*20;
     }
     
     @Override
     /**
      * returns the action that specifies what animation to play when the items is being used
      */
     public EnumAction getItemUseAction(ItemStack par1ItemStack)
     {
         return EnumAction.block;
     }
 }
