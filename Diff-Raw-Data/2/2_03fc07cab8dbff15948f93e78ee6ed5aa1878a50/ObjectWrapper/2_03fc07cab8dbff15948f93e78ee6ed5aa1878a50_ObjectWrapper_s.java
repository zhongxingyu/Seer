 package no.runsafe.framework.wrapper;
 
 import no.runsafe.framework.server.*;
 import no.runsafe.framework.server.block.*;
 import no.runsafe.framework.server.chunk.RunsafeChunk;
 import no.runsafe.framework.server.enchantment.RunsafeEnchantment;
 import no.runsafe.framework.server.entity.*;
 import no.runsafe.framework.server.inventory.*;
 import no.runsafe.framework.server.item.RunsafeItemStack;
 import no.runsafe.framework.server.item.meta.*;
 import no.runsafe.framework.server.material.RunsafeMaterial;
 import no.runsafe.framework.server.material.RunsafeMaterialData;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.server.potion.RunsafePotionEffect;
 import no.runsafe.framework.wrapper.item.BukkitItemStack;
 import no.runsafe.framework.wrapper.metadata.RunsafeMetadata;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.*;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.*;
 import org.bukkit.inventory.meta.*;
 import org.bukkit.material.MaterialData;
 import org.bukkit.metadata.Metadatable;
 import org.bukkit.potion.PotionEffect;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 @SuppressWarnings("deprecation")
 public class ObjectWrapper
 {
 	@SuppressWarnings("unchecked")
 	public static <Wrapper> List<Wrapper> convert(List<?> toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		ArrayList<Wrapper> results = new ArrayList<Wrapper>();
 		for (Object item : toWrap)
 		{
 			if (item instanceof Metadatable)
 				results.add((Wrapper) convert((Metadatable) item));
 			else if (item instanceof ItemStack)
 				results.add((Wrapper) convert((ItemStack) item));
 			else if (item instanceof PotionEffect)
 				results.add((Wrapper) convert((PotionEffect) item));
 		}
 		return results;
 	}
 
 	@Deprecated
 	private static RunsafePotionEffect convert(PotionEffect item)
 	{
 		if (item == null)
 			return null;
 		return new RunsafePotionEffect(item);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <Wrapper extends RunsafeMetadata, Raw extends Metadatable> List<Wrapper> convert(Raw[] toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		ArrayList<Wrapper> results = new ArrayList<Wrapper>();
 		for (Raw item : toWrap)
 			results.add((Wrapper) convert(item));
 		return results;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <Wrapper extends BukkitItemStack, Raw extends ItemStack> List<Wrapper> convert(Raw[] toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		ArrayList<Wrapper> results = new ArrayList<Wrapper>();
 		for (Raw item : toWrap)
 			results.add((Wrapper) convert(item));
 		return results;
 	}
 
 	public static RunsafeInventory convert(Inventory toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		if (toWrap instanceof AnvilInventory)
 			return new RunsafeAnvilInventory((AnvilInventory) toWrap);
 
 		return new RunsafeInventory(toWrap);
 	}
 
 	public static RunsafeEntityEquipment convert(EntityEquipment toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		return new RunsafeEntityEquipment(toWrap);
 	}
 
 	@Deprecated
 	public static RunsafeMaterial convert(Material toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeMaterial(toWrap);
 	}
 
 	public static RunsafeChunk convert(Chunk toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeChunk(toWrap);
 	}
 
 	public static RunsafeMaterialData convert(MaterialData toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeMaterialData(toWrap);
 	}
 
 	public static List<RunsafePlayer> convert(OfflinePlayer[] toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		ArrayList<RunsafePlayer> results = new ArrayList<RunsafePlayer>();
 		for (OfflinePlayer player : toWrap)
 			results.add(new RunsafePlayer(player));
 		return results;
 	}
 
 	public static List<RunsafePlayer> convert(Set<? extends OfflinePlayer> toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		ArrayList<RunsafePlayer> results = new ArrayList<RunsafePlayer>();
 		for (OfflinePlayer player : toWrap)
 			results.add(new RunsafePlayer(player));
 		return results;
 	}
 
 	public static RunsafeMetadata convert(Metadatable toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		if (toWrap instanceof Block)
 			return convert((Block) toWrap);
 
 		if (toWrap instanceof BlockState)
 			return convert((BlockState) toWrap);
 
 		if (toWrap instanceof Entity)
 			return convert((Entity) toWrap);
 
 		if (toWrap instanceof World)
 			return convert((World) toWrap);
 
 		return new RunsafeMetadata(toWrap);
 	}
 
 	public static RunsafeBlock convert(Block toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		if (toWrap.getState() != null && toWrap.getState() instanceof CreatureSpawner)
 			return new RunsafeSpawner(toWrap);
 		return new RunsafeBlock(toWrap);
 	}
 
 	public static RunsafeBlockState convert(BlockState toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		if (toWrap instanceof BrewingStand)
 			return convert((BrewingStand) toWrap);
 
 		if (toWrap instanceof Chest)
 			return convert((Chest) toWrap);
 
 		if (toWrap instanceof CreatureSpawner)
 			return convert((CreatureSpawner) toWrap);
 
 		if (toWrap instanceof Dispenser)
 			return convert((Dispenser) toWrap);
 
 		if (toWrap instanceof Furnace)
 			return convert((Furnace) toWrap);
 
 		if (toWrap instanceof Jukebox)
 			return convert((Jukebox) toWrap);
 
 		if (toWrap instanceof NoteBlock)
 			return convert((NoteBlock) toWrap);
 
 		if (toWrap instanceof Sign)
 			return convert((Sign) toWrap);
 
 		return new RunsafeBlockState(toWrap);
 	}
 
 	public static RunsafeBrewingStand convert(BrewingStand toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeBrewingStand(toWrap);
 	}
 
 	public static RunsafeChest convert(Chest toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeChest(toWrap);
 	}
 
 	public static RunsafeDoubleChest convert(DoubleChest toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeDoubleChest(toWrap);
 	}
 
 	public static RunsafeCreatureSpawner convert(CreatureSpawner toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeCreatureSpawner(toWrap);
 	}
 
 	public static RunsafeDispenser convert(Dispenser toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeDispenser(toWrap);
 	}
 
 	public static RunsafePlayerInventory convert(PlayerInventory toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafePlayerInventory(toWrap);
 	}
 
 	public static RunsafeFurnace convert(Furnace toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeFurnace(toWrap);
 	}
 
 	public static RunsafeJukebox convert(Jukebox toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeJukebox(toWrap);
 	}
 
 	public static RunsafeFallingBlock convert(FallingBlock toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		return new RunsafeFallingBlock(toWrap);
 	}
 
 	public static RunsafeNoteBlock convert(NoteBlock toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeNoteBlock(toWrap);
 	}
 
 	public static RunsafeSign convert(Sign toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeSign(toWrap);
 	}
 
 	public static RunsafeEntity convert(Entity toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		if (toWrap instanceof Player)
 			return convert((Player) toWrap);
 
 		if (toWrap instanceof Painting)
 			return convert((Painting) toWrap);
 
 		if (toWrap instanceof ItemFrame)
 			return convert((ItemFrame) toWrap);
 
 		if (toWrap instanceof LivingEntity)
 			return convert((LivingEntity) toWrap);
 
 		if (toWrap instanceof Projectile)
 			return convert((Projectile) toWrap);
 
 		if (toWrap instanceof Item)
 			return convert((Item) toWrap);
 
 		return new RunsafeEntity(toWrap);
 	}
 
 	public static RunsafePlayer convert(Player toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafePlayer(toWrap);
 	}
 
 	public static RunsafePlayer convert(HumanEntity toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafePlayer((Player) toWrap);
 	}
 
 	public static RunsafePainting convert(Painting toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafePainting(toWrap);
 	}
 
 	public static RunsafeItemFrame convert(ItemFrame toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeItemFrame(toWrap);
 	}
 
 	public static RunsafeLivingEntity convert(LivingEntity toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeLivingEntity(toWrap);
 	}
 
 	public static RunsafeProjectile convert(Projectile toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeProjectile(toWrap);
 	}
 
 	public static RunsafeWorld convert(World toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeWorld(toWrap);
 	}
 
 	public static RunsafeLocation convert(Location toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeLocation(toWrap);
 	}
 
 	public static RunsafeItem convert(Item toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeItem(toWrap);
 	}
 
 	public static RunsafeItemStack convert(ItemStack toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeItemStack(toWrap);
 	}
 
 	public static RunsafeEnchantment convert(Enchantment toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeEnchantment(toWrap);
 	}
 
 	@Deprecated
 	public static RunsafeFireworkEffect convert(FireworkEffect toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeFireworkEffect(toWrap);
 	}
 
 	public static RunsafeInventoryType convert(InventoryType toWrap)
 	{
 		return RunsafeInventoryType.valueOf(toWrap.name());
 	}
 
 	public static RunsafeItemMeta convert(ItemMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		if (toWrap instanceof BookMeta)
 			return convert((BookMeta) toWrap);
 
 		if (toWrap instanceof EnchantmentStorageMeta)
 			return convert(((EnchantmentStorageMeta) toWrap));
 
 		if (toWrap instanceof FireworkEffectMeta)
 			return convert((FireworkEffectMeta) toWrap);
 
 		if (toWrap instanceof FireworkMeta)
 			return convert((FireworkMeta) toWrap);
 
 		if (toWrap instanceof LeatherArmorMeta)
 			return convert((LeatherArmorMeta) toWrap);
 
 		if (toWrap instanceof MapMeta)
 			return convert((MapMeta) toWrap);
 
 		if (toWrap instanceof PotionMeta)
 			return convert((PotionMeta) toWrap);
 
 		if (toWrap instanceof SkullMeta)
 			return convert((SkullMeta) toWrap);
 
 		return new RunsafeItemMeta(toWrap);
 	}
 
 	public static RunsafeBookMeta convert(BookMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeBookMeta(toWrap);
 	}
 
 	public static RunsafeEnchantmentStorageMeta convert(EnchantmentStorageMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeEnchantmentStorageMeta(toWrap);
 	}
 
 	public static RunsafeFireworkEffectMeta convert(FireworkEffectMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeFireworkEffectMeta(toWrap);
 	}
 
 	public static RunsafeInventoryView convert(InventoryView toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeInventoryView(toWrap);
 	}
 
 	public static RunsafeFireworkMeta convert(FireworkMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeFireworkMeta(toWrap);
 	}
 
 	public static RunsafeTravelAgent convert(TravelAgent toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeTravelAgent(toWrap);
 	}
 
 	public static RunsafeLeatherArmorMeta convert(LeatherArmorMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeLeatherArmorMeta(toWrap);
 	}
 
 	public static RunsafeMapMeta convert(MapMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeMapMeta(toWrap);
 	}
 
 	public static RunsafePotionMeta convert(PotionMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafePotionMeta(toWrap);
 	}
 
 	public static RunsafeSkullMeta convert(SkullMeta toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		return new RunsafeSkullMeta(toWrap);
 	}
 
 	public static RunsafeHopper convert(Hopper toWrap)
 	{
 		if (toWrap == null)
 			return null;
 
 		return new RunsafeHopper(toWrap);
 	}
 
 	public static IInventoryHolder convert(InventoryHolder toWrap)
 	{
 		if (toWrap == null)
 			return null;
 		if (toWrap instanceof Hopper)
 			return convert((Hopper) toWrap);
 		if (toWrap instanceof BrewingStand)
 			return convert((BrewingStand) toWrap);
 		if (toWrap instanceof Chest)
 			return convert((Chest) toWrap);
 		if (toWrap instanceof Dispenser)
 			return convert((Dispenser) toWrap);
 		if (toWrap instanceof Furnace)
 			return convert((Furnace) toWrap);
 		if (toWrap instanceof DoubleChest)
 			return convert((DoubleChest) toWrap);
 		if (toWrap instanceof Player)
 			return convert((Player) toWrap);
 		return null;
 	}
 
 	public static RunsafeEntityType convert(EntityType type)
 	{
 		if (type == null)
 			return null;
 		return no.runsafe.framework.server.entity.EntityType.convert(type);
 	}
 }
