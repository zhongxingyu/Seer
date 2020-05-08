 package assets.fyresmodjam;
 
 import java.util.List;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.projectile.EntityPotion;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.util.Icon;
 import net.minecraft.util.StatCollector;
 import net.minecraft.world.World;
 
 public class ItemMysteryPotion extends Item {
 
 	public static Icon[] icons = null;
 	
 	public ItemMysteryPotion(int par1) {
 		super(par1);
 	}
 	
 	@SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister) {
 		if(icons == null) {
 			icons = new Icon[13];
 			for(int i = 0; i < 13; i++) {icons[i] = par1IconRegister.registerIcon("fyresmodjam:mysteryPotion_" + (i + 1));}
 		}
 		
         this.itemIcon = icons[0];
     }
 	
 	@SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int par1)  {
         return par1 < 13 ? icons[par1] : icons[0];
     }
 	
 	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
         for(int i = 0; i < 13; i++) {par3List.add(new ItemStack(par1, 1, i));}
     }
 	
 	@SideOnly(Side.CLIENT)
 	public String getItemDisplayName(ItemStack par1ItemStack) {
 		String name = "Mystery Potion #" + (par1ItemStack.getItemDamage() + 1);
 		
 		String blessing = null;
 		
 		if(FMLCommonHandler.instance().getSide() == Side.CLIENT) {blessing = getBlessing();}
 		
 		if(par1ItemStack.getItemDamage() < 12) {
 			if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.getEntityData().hasKey("PotionKnowledge")) {
 				if(Minecraft.getMinecraft().thePlayer.getEntityData().getIntArray("PotionKnowledge")[par1ItemStack.getItemDamage()] != -1) {
 					Potion potion = Potion.potionTypes[FyresWorldData.potionValues[par1ItemStack.getItemDamage()]];
 					name = StatCollector.translateToLocal(potion.getName()) + " Potion";
 					
 					if(!potion.isInstant()) {
 						int time = FyresWorldData.potionDurations[par1ItemStack.getItemDamage()];
 						name += " (" + time + " seconds)";
 					}
 				}
 			}
 		} else if(par1ItemStack.getItemDamage() >= 12) {
 			name = "Wildcard Potion";
 		}
 		
 		if((blessing != null && blessing.equals("Alchemist")) /*|| FyresWorldData.currentDisadvantage.equals("Illiterate")*/) {name = "\u00A7k" + name;}
 		
         return name;
     }
 	
 	public EnumAction getItemUseAction(ItemStack par1ItemStack) {
         return EnumAction.drink;
     }
 	
 	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
         par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
         return par1ItemStack;
     }
 	
 	public int getMaxItemUseDuration(ItemStack par1ItemStack) {
         return 32;
     }
 	
 	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
         if(!par3EntityPlayer.capabilities.isCreativeMode) {--par1ItemStack.stackSize;}
         
         String blessing = (par3EntityPlayer != null && par3EntityPlayer.getEntityData().hasKey("Blessing")) ? par3EntityPlayer.getEntityData().getString("Blessing") : null;
         
         if((blessing == null || !blessing.equals("Alchemist")) && par1ItemStack.getItemDamage() < 12) {
 	        if(!par2World.isRemote) {
 	        	
 	        	int value = FyresWorldData.potionValues[par1ItemStack.getItemDamage()];
 	        	
 	        	if(!Potion.potionTypes[value].isInstant()) {
 	        		par3EntityPlayer.addPotionEffect(new PotionEffect(value, FyresWorldData.potionDurations[par1ItemStack.getItemDamage()] * 20, 1, false));
 	        	} else {
 	        		Potion.potionTypes[value].affectEntity(par3EntityPlayer, par3EntityPlayer, 1, 1);
 	        	}
 	        	
 	        	if(!par3EntityPlayer.getEntityData().hasKey("PotionKnowledge")) {par3EntityPlayer.getEntityData().setIntArray("PotionKnowledge", new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});}
 		        
 	        	if(par3EntityPlayer.getEntityData().getIntArray("PotionKnowledge")[par1ItemStack.getItemDamage()] == -1) {
 	        		par3EntityPlayer.getEntityData().getIntArray("PotionKnowledge")[par1ItemStack.getItemDamage()] = 1;
 		        	
 		        	PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_POTION_KNOWLEDGE, new Object[] {par3EntityPlayer.getEntityData().getIntArray("PotionKnowledge")}), (Player) par3EntityPlayer);
 		        
 		        	Potion potion = Potion.potionTypes[FyresWorldData.potionValues[par1ItemStack.getItemDamage()]];
 					String name = StatCollector.translateToLocal(potion.getName()) + " Potion";
 					
 					if(!potion.isInstant()) {
 						int time = FyresWorldData.potionDurations[par1ItemStack.getItemDamage()];
 						name += " (" + time + " seconds)";
 					}
 					
 					PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7oYou learnt Mystery Potion #" + (par1ItemStack.getItemDamage() + 1) + " was a " + name + "!"}), (Player) par3EntityPlayer);
 	        	}
 	        }
 	        
         } else if(!par2World.isRemote) {
         	int value = ModjamMod.r.nextInt(Potion.potionTypes.length);
         	while(Potion.potionTypes[value] == null) {value = ModjamMod.r.nextInt(Potion.potionTypes.length);}
         	int time = 5 + ModjamMod.r.nextInt(26);
         	
         	if(!Potion.potionTypes[value].isInstant()) {
         		par3EntityPlayer.addPotionEffect(new PotionEffect(value, time * 20, 1, false));
         	} else {
         		Potion.potionTypes[value].affectEntity(par3EntityPlayer, par3EntityPlayer, 1, 1);
         	}
         	
         	Potion potion = Potion.potionTypes[value];
 			String name = StatCollector.translateToLocal(potion.getName()) + " Potion";
 			
 			if(!potion.isInstant()) {name += " (" + time + " seconds)";}
         	
         	PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7oYou drank a " + name + "."}), (Player) par3EntityPlayer);
         }
         
         return par1ItemStack;
     }
 	
 	@SideOnly(Side.CLIENT)
 	public String getBlessing() {
 		return (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.getEntityData().hasKey("Blessing")) ? Minecraft.getMinecraft().thePlayer.getEntityData().getString("Blessing") : null;
 	}
 	
 	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
         return false;
     }
 
 }
