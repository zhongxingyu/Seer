 package no.runsafe.framework.api.item;
 
 import no.runsafe.framework.exceptions.InvalidDurabilityException;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.SandstoneType;
 import org.bukkit.TreeSpecies;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.*;
 
 @SuppressWarnings("NullableProblems")
 public enum BuildingBlock implements IMaterial
 {
 	Stone(Material.STONE),
 	Grass(Material.GRASS),
 	Dirt(Material.DIRT),
 	Cobblestone(Material.COBBLESTONE),
 	Bedrock(Material.BEDROCK),
 	Sand(Material.SAND),
 	Gravel(Material.GRAVEL),
 	Sponge(Material.SPONGE),
 	Glass(Material.GLASS),
 	LapisLazuli(Material.LAPIS_BLOCK),
 	GoldBlock(Material.GOLD_BLOCK),
 	IronBlock(Material.IRON_BLOCK),
 	Brick(Material.BRICK),
 	Bookshelf(Material.BOOKSHELF),
 	MossyCobblestone(Material.MOSSY_COBBLESTONE),
 	Obsidian(Material.OBSIDIAN),
 	Diamond(Material.DIAMOND_BLOCK),
 	Ice(Material.ICE),
 	Snow(Material.SNOW_BLOCK),
 	Clay(Material.CLAY),
 	Pumpkin(Material.PUMPKIN),
 	Netherrack(Material.NETHERRACK),
 	Soulsand(Material.SOUL_SAND),
 	Glowstone(Material.GLOWSTONE),
 	JackOLantern(Material.JACK_O_LANTERN),
 	Melon(Material.MELON_BLOCK),
 	Mycelium(Material.MYCEL),
 	NetherBrick(Material.NETHER_BRICK),
 	EndStone(Material.ENDER_STONE),
 	Emerald(Material.EMERALD_BLOCK),
 	CoalBlock(Material.COAL_BLOCK),
 	OakStair(Material.WOOD_STAIRS),
 	SpruceStair(Material.SPRUCE_WOOD_STAIRS),
 	BirchStair(Material.BIRCH_WOOD_STAIRS),
 	JungleStair(Material.JUNGLE_WOOD_STAIRS),
 	CobblestoneStair(Material.COBBLESTONE_STAIRS),
 	BrickStair(Material.BRICK_STAIRS),
 	StoneBrickStair(Material.SMOOTH_STAIRS),
 	NetherBrickStair(Material.NETHER_BRICK_STAIRS),
 	QuartzStair(Material.QUARTZ_STAIRS),
 	SandstoneStair(Material.SANDSTONE_STAIRS);
 
 	public enum Wood implements IMaterial
 	{
 		Any(null),
 		Oak(TreeSpecies.GENERIC),
 		Spruce(TreeSpecies.REDWOOD),
 		Birch(TreeSpecies.BIRCH),
 		Jungle(TreeSpecies.JUNGLE),
 		DarkOak(TreeSpecies.DARK_OAK),
 		Acacia(TreeSpecies.ACACIA);
 
 		Wood(TreeSpecies species)
 		{
 			variant = species;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.LOG;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.LOG || !(data instanceof Tree))
 				return false;
 
 			return variant == null || ((Tree) data).getSpecies() == variant;
 		}
 
 		private final TreeSpecies variant;
 	}
 
 	public enum Plank implements IMaterial
 	{
 		Any(null),
 		Oak(TreeSpecies.GENERIC),
 		Spruce(TreeSpecies.REDWOOD),
 		Birch(TreeSpecies.BIRCH),
 		Jungle(TreeSpecies.JUNGLE),
 		DarkOak(TreeSpecies.DARK_OAK),
 		Acacia(TreeSpecies.ACACIA);
 
 		Plank(TreeSpecies species)
 		{
 			variant = species;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.WOOD;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.WOOD || !(data instanceof Tree))
 				return false;
 
 			return variant == null || ((Tree) data).getSpecies() == variant;
 		}
 
 		private final TreeSpecies variant;
 	}
 
 	public enum Sandstone implements IMaterial
 	{
 		Any(null),
 		Normal(SandstoneType.CRACKED),
 		Chiseled(SandstoneType.GLYPHED),
 		Smooth(SandstoneType.SMOOTH);
 
 		Sandstone(SandstoneType type)
 		{
 			variant = type;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.SANDSTONE;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.SANDSTONE || !(data instanceof org.bukkit.material.Sandstone))
 				return false;
 
 			return variant == null || ((org.bukkit.material.Sandstone) data).getType() == variant;
 		}
 
 		private final SandstoneType variant;
 	}
 
 	public enum Wool implements IMaterial
 	{
 		Any(null),
 		White(DyeColor.WHITE),
 		Orange(DyeColor.ORANGE),
 		Magenta(DyeColor.MAGENTA),
 		LightBlue(DyeColor.LIGHT_BLUE),
 		Yellow(DyeColor.YELLOW),
 		Lime(DyeColor.LIME),
 		Pink(DyeColor.PINK),
 		Gray(DyeColor.GRAY),
 		LightGray(DyeColor.GRAY),
 		Cyan(DyeColor.CYAN),
 		Purple(DyeColor.PURPLE),
 		Blue(DyeColor.BLUE),
 		Brown(DyeColor.BROWN),
 		Green(DyeColor.GREEN),
 		Red(DyeColor.RED),
 		Black(DyeColor.BLACK);
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.WOOL;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.WOOL || !(data instanceof org.bukkit.material.Wool))
 				return false;
 
 			return variant == null || ((Colorable) data).getColor() == variant;
 		}
 
 		Wool(DyeColor colour)
 		{
 			variant = colour;
 		}
 
 		private final DyeColor variant;
 	}
 
 	public enum WoodSlab implements IMaterial
 	{
 		Any(null),
 		Oak(TreeSpecies.GENERIC),
 		Spruce(TreeSpecies.REDWOOD),
 		Birch(TreeSpecies.BIRCH),
 		Jungle(TreeSpecies.JUNGLE),
 		DarkOak(TreeSpecies.DARK_OAK),
 		Acacia(TreeSpecies.ACACIA);
 
 		WoodSlab(TreeSpecies species)
 		{
 			variant = species;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.WOOD_STEP;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.WOOD_STEP || !(data instanceof WoodenStep))
 				return false;
 
 			return variant == null || ((WoodenStep) data).getSpecies() == variant;
 		}
 
 		private final TreeSpecies variant;
 	}
 
 	public enum StoneSlab implements IMaterial
 	{
 		Any(null),
 		Smoothstone(Material.STONE),
 		Sandstone(Material.SANDSTONE),
 		Cobblestone(Material.COBBLESTONE),
 		Brick(Material.BRICK),
 		StoneBrick(Material.SMOOTH_BRICK),
 		NetherRack(Material.NETHER_BRICK),
 		Quartz(Material.QUARTZ_BLOCK);
 
 		StoneSlab(Material type)
 		{
 			variant = type;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.STEP;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.STEP || !(data instanceof TexturedMaterial))
 				return false;
 
 			return variant == null || ((TexturedMaterial) data).getMaterial() == variant;
 		}
 
 		private final Material variant;
 	}
 
 	public enum StoneBrick implements IMaterial
 	{
 		Any(null);
 // No idea what to do here..
 //		Normal(),
 //		Mossy(),
 //		Cracked(),
 //		Chiseled();
 
 		StoneBrick(Material type)
 		{
 			variant = type;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.SMOOTH_BRICK;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.SMOOTH_BRICK || !(data instanceof SmoothBrick))
 				return false;
 
 			return variant == null || ((TexturedMaterial) data).getMaterial() != variant;
 		}
 
 		private final Material variant;
 	}
 
 	public enum CobbleWall implements IMaterial
 	{
 		Any(null),
 		Normal(Material.COBBLESTONE),
 		Mossy(Material.MOSSY_COBBLESTONE);
 
 		CobbleWall(Material type)
 		{
 			variant = type;
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.COBBLE_WALL;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
 			if (material != Material.COBBLE_WALL || !(data instanceof TexturedMaterial))
 				return false;
 
 			return variant == null || ((TexturedMaterial) data).getMaterial() == variant;
 		}
 
 		private final Material variant;
 	}
 
 	public enum Quartz implements IMaterial, IMaterialData
 	{
		Any(0),
		Normal(1),
		Chiseled(2),
		Pillar(3);
 
 		@SuppressWarnings("NumericCastThatLosesPrecision")
 		Quartz(int durability)
 		{
 			MaterialData temp = new MaterialData(Material.QUARTZ_BLOCK);
 			ItemStack stack = temp.toItemStack();
 			if (Material.QUARTZ_BLOCK.getMaxDurability() > durability)
 				throw new InvalidDurabilityException(temp.getItemType(), (short) durability);
 			stack.setDurability((short) durability);
 			data = stack.getData();
 		}
 
 		@Override
 		public Material getMaterial()
 		{
 			return Material.QUARTZ_BLOCK;
 		}
 
 		@Override
 		public MaterialData getData()
 		{
 			return data;
 		}
 
 		@Override
 		public boolean isSame(Material material, MaterialData data)
 		{
			return material == Material.QUARTZ_BLOCK;
 		}
 
 		private final MaterialData data;
 	}
 
 	BuildingBlock(Material material)
 	{
 		kind = material;
 	}
 
 	@Override
 	public Material getMaterial()
 	{
 		return kind;
 	}
 
 	@Override
 	public boolean isSame(Material material, MaterialData data)
 	{
 		return material != kind;
 	}
 
 	private final Material kind;
 }
