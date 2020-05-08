 package silentAbyss.item;
 
 import java.util.List;
 
 import silentAbyss.Abyss;
 import silentAbyss.configuration.Config;
 import silentAbyss.core.handlers.ChaosHandler;
 import silentAbyss.core.util.LogHelper;
 import silentAbyss.core.util.NBTHelper;
 import silentAbyss.entity.projectile.EntityProjectileMagic;
 import silentAbyss.lib.Strings;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.EnumAction;
 import net.minecraft.item.EnumRarity;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.potion.PotionEffect;
 import net.minecraft.world.World;
 
 // TODO: AbyssSigil class needs major cleanup.
 public class AbyssSigil extends ItemSA {
 
 	public AbyssSigil(int par1) {
 		super(par1);
 		this.maxStackSize = 64;
 		this.setCreativeTab(CreativeTabs.tabTools);
 	}
 	
 	// Activates the intended effects of the sigil.
 	public boolean sigilEffects(ItemStack stack, World world, EntityPlayer player) {
 
 		NBTTagCompound tags = stack.getTagCompound();
 		
 		if (tags == null || !tags.hasKey("Effects")) {
 			LogHelper.warning("Abyss Sigil with missing effects list!");
 			return true;
 		}
 		
 		// Parse effects and apply.
 		String[] effects = tags.getString("Effects").split(" ");
 		
 		String color = "White";
 		int amplifier = 0;
 		// prevents Healing from being stacked
 		boolean healingApplied = false;
 		
 		for (int i = 0; i < effects.length; ++i) {
 			// Amplify effect
 			if (effects[i].equals("Amplify")) {
 				++amplifier;
 			}
 			// Color
 			else if (SigilStone.isColorSigil(effects[i])) {
 				color = effects[i];
 			}
 			// Fireball
 			else if (effects[i].equals("Fireball")) {
 				if (!world.isRemote) {
 					EntityProjectileMagic p = new EntityProjectileMagic(world, player).setType(1).setColor(color).setDamage(Config.SIGIL_BASE_PROJECTILE_DAMAGE + Config.SIGIL_BASE_PROJECTILE_DAMAGE * amplifier / 2);
 					world.spawnEntityInWorld(p);
 					world.playSoundAtEntity(player, "mob.ghast.fireball", 0.5f, 0.5f / (itemRand.nextFloat() * 0.4f + 0.8f));
 				}
 				
 				amplifier = 0;
 				color = "White";
 			}
 			// Icebolt
 			else if (effects[i].equals("Icebolt")) {
 				if (!world.isRemote) {
 					EntityProjectileMagic p = new EntityProjectileMagic(world, player).setType(2).setColor(color).setDamage(Config.SIGIL_BASE_PROJECTILE_DAMAGE + Config.SIGIL_BASE_PROJECTILE_DAMAGE * amplifier / 2);
 					world.spawnEntityInWorld(p);
 				}
 				
 				amplifier = 0;
 				color = "White";
 			}
 			// Lightning
 			else if (effects[i].equals("Lightning")) {
 				if (!world.isRemote) {
 					EntityProjectileMagic p = new EntityProjectileMagic(world, player).setType(3).setColor(color).setDamage(Config.SIGIL_BASE_PROJECTILE_DAMAGE + Config.SIGIL_BASE_PROJECTILE_DAMAGE * amplifier / 2);
 					world.spawnEntityInWorld(p);
 					world.playSoundAtEntity(player, "ambient.weather.thunder", 0.5f, 1.6f / (itemRand.nextFloat() * 0.4f + 0.8f));
 				}
 				
 				amplifier = 0;
 				color = "White";
 			}
 			// Earthquake
 			else if (effects[i].equals("Earthquake")) {
 				// TODO
 				if (!world.isRemote) {
 					EntityProjectileMagic p = new EntityProjectileMagic(world, player).setType(4).setColor(color).setDamage(Config.SIGIL_BASE_PROJECTILE_DAMAGE + Config.SIGIL_BASE_PROJECTILE_DAMAGE / 2);
 					world.spawnEntityInWorld(p);
 				}
 				
 				amplifier = 0;
 				color = "White";
 			}
 			// Healing
 			else if (effects[i].equals("Healing") && !healingApplied) {
 				int k = 6 + 6 * amplifier;
 				player.heal(k);
 				amplifier = 0;
 				color = "White";
 				healingApplied = true;
 			}
 			// Resistance
 			else if (effects[i].equals("Resistance")) {
 				if (!world.isRemote) {
 					player.addPotionEffect(new PotionEffect(11, Config.SIGIL_BASE_SUPPORT_DURATION * (amplifier + 1), amplifier));
 				}
 				amplifier = 0;
 				color = "White";
 			}
 			// Remedy
 			else if (effects[i].equals("Remedy")) {
 				if (!world.isRemote) {
 					player.curePotionEffects(new ItemStack(Item.bucketMilk));
 				}
 				amplifier = 0;
 				color = "White";
 			}
 			// Cloak
 			else if (effects[i].equals("Cloak")) {
 				player.addPotionEffect(new PotionEffect(14, Config.SIGIL_BASE_SUPPORT_DURATION * (amplifier + 1), amplifier));
 				amplifier = 0;
 				color = "White";
 			}
 			// Teleport
 			else if (effects[i].equals("Teleport")) {
 				if (!NBTHelper.hasValidXYZD(tags) || tags.getInteger("Y") <= 0) {
 					LogHelper.warning("Invalid location for teleport effect");
 					player.addChatMessage("Invalid location for teleport effect");
 				}
 				else {
 					int dx, dy, dz, dd;
 					dx = tags.getInteger("X");
 					dy = tags.getInteger("Y");
 					dz = tags.getInteger("Z");
 					dd = tags.getInteger("D");
 					
 					if (dd != player.dimension) {
 						player.travelToDimension(dd);
 					}
 					player.setPositionAndUpdate(dx + 0.5, dy + 1, dz + 0.5);
 				}
 				
 				amplifier = 0;
 				color = "White";
 			}
 			// Speed
 			else if (effects[i].equals("Speed")) {
 				// Do nothing. This is handled in getMaxItemUseDuration.
 				amplifier = 0;
 				color = "White";
 			}
 			// Derp catcher
 			else {
 				player.addChatMessage("Effect " + effects[i] + " is not implemented!");
 				LogHelper.warning("Effect " + effects[i] + " is not implemented! (Abyss Sigil)");
 				
 				amplifier = 0;
 				color = "White";
 			}
 		}
 		
 		// Chaos cost
 		ChaosHandler.addChaos(Config.CHAOS_COST_SIGIL);
 		
 		return true;
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
     public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
 		if (stack.stackTagCompound == null) {
 			//stack.setTagCompound(new NBTTagCompound());
 			return;
 		}
 		
 		NBTTagCompound tags = stack.getTagCompound();
 		
 		if (tags.hasKey("Effects")) {
 			list.add(tags.getString("Effects"));
 		}
 		
		list.add(LogHelper.coordFromNBT(tags));
 	}
 
 	@Override
 	public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
 		if (sigilEffects(itemStack, world, player) && !world.isRemote) {
 			// Don't consume in creative mode.
 			if (!player.capabilities.isCreativeMode) {
 				// Chance of breaking
 				if (Abyss.rng.nextInt(100) + 1 < Config.SIGIL_BASE_BREAK_CHANCE) {
 					--itemStack.stackSize;
 				}
 			}
 		}
 		return itemStack;
 	}
 	
 	@Override
 	public int getMaxItemUseDuration(ItemStack itemStack) {
 		// Determine speed level.
 		String[] effects = getListOfEffects(itemStack);
 		
 		int amplifier = 1;
 		for (int i = 0; i < effects.length; ++i) {
 			// Amplify word.
 			if (effects[i].equals("Amplify")) {
 				amplifier += 1;
 			}
 			// Speed word.
 			else if (effects[i].equals("Speed")) {
 				// We don't want amplifier to be too high.
 				if (amplifier > 3) {
 					amplifier = 3;
 				}
 				// 20% reduction in casting time per level.
 				// 3 levels is still a bit too fast, but I don't think it would be used much.
 				return (int)(Config.SIGIL_BASE_USE_DURATION - Config.SIGIL_BASE_USE_DURATION * 0.20F * amplifier);
 			}
 			// Not Amplify or Speed, so reset amplifier.
 			else {
 				++amplifier;
 			}
 		}
 		
 		// No speed effect present.
 		return Config.SIGIL_BASE_USE_DURATION;
 	}
 	
 	@Override
 	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
 		if (AbyssSigil.hasSigilEfffect(stack, "Teleport") && !NBTHelper.hasValidXYZD(stack.stackTagCompound)) {
 			if (!world.isRemote) { 
 				player.addChatMessage("No teleport destination set. Try right-clicking an Abyss Teleporter.");
 			}
 		}
 		else {
 			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
 		}
 		return stack;
 	}
 	
 	@Override
 	public EnumAction getItemUseAction(ItemStack itemStack) {
 		return EnumAction.bow;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void registerIcons(IconRegister iconRegister) {
 		itemIcon = iconRegister.registerIcon("SilentAbyss:Sigil");
 	}
 	
 	@Override
 	public boolean hasEffect(ItemStack itemStack) {
 		return true;
 	}
 	
 	@Override
 	public EnumRarity getRarity(ItemStack itemStack) {
 		return EnumRarity.rare;
 	}
 	
 	public static boolean hasSigilEfffect(ItemStack stack, String effect) {
 		if (!stack.hasTagCompound()) {
 			return false;
 		}
 		
 		// Verify sigil effects exists for bug prevention.
 		boolean exists = false;
 		for (int i = 0; i < SigilStone.names.length; ++i){
 			if (effect.equals(SigilStone.names[i])) {
 				exists = true;
 				break;
 			}
 		}
 		
 		if (!exists) {
 			LogHelper.warning("Sigil effect \"" + effect + "\" does not exist!");
 		}
 		
 		// Check for effect
 		if (stack.stackTagCompound.hasKey("Effects")) {
 			String[] effects = stack.stackTagCompound.getString("Effects").split(" ");
 			for (int i = 0; i < effects.length; ++i) {
 				if (effects[i].equals(effect)) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	public String[] getListOfEffects(ItemStack sigil) {
 		NBTTagCompound tags = sigil.getTagCompound();
 		
 		if (tags == null || !tags.hasKey("Effects")) {
 			LogHelper.warning("Abyss Sigil with missing effects list!");
 			return new String[0];
 		}
 		
 		return tags.getString("Effects").split(" ");
 	}
 	
 	@Override
     public String getUnlocalizedName(ItemStack stack) {
 
         StringBuilder s = new StringBuilder();
         s.append("item.");
         s.append(Strings.RESOURCE_PREFIX);
         s.append(Strings.ABYSS_SIGIL_NAME);
         
         return s.toString();
     }
 }
