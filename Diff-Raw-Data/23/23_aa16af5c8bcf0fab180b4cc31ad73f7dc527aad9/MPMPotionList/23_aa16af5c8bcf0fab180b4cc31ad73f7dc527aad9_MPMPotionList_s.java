 package clashsoft.mods.morepotions.brewing;
 
 import static clashsoft.brewingapi.brewing.PotionList.*;
 import clashsoft.brewingapi.BrewingAPI;
 import clashsoft.brewingapi.brewing.PotionBase;
 import clashsoft.brewingapi.brewing.PotionList;
 import clashsoft.brewingapi.brewing.PotionType;
 import clashsoft.mods.morepotions.MorePotionsMod;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.potion.Potion;
 import net.minecraft.potion.PotionEffect;
 
 public class MPMPotionList
 {
 	public static PotionType	thorns;
 	public static PotionType	greenThumb;
 	public static PotionType	projectile;
 	
 	public static void initializePotionBases()
 	{
 		awkward = new PotionBase("awkward", new ItemStack(Item.netherStalkSeeds));
 		mundane = new PotionBase("mundane", new ItemStack(Block.mushroomBrown));
 		uninteresting = new PotionBase("uninteresting", new ItemStack(Item.paper));
 		bland = new PotionBase("bland", new ItemStack(Item.melon));
 		clear = new PotionBase("clear", MorePotionsMod.dustClay);
 		milky = new PotionBase("milky", new ItemStack(Block.sapling));
 		diffuse = new PotionBase("diffuse", new ItemStack(Item.wheat));
 		artless = new PotionBase("artless", new ItemStack(Item.reed));
 		thin = new PotionBase("thin", new ItemStack(Item.redstone));
 		flat = new PotionBase("flat", new ItemStack(Block.waterlily));
 		bulky = new PotionBase("bulky", new ItemStack(Block.sand));
 		bungling = new PotionBase("bungling", new ItemStack(Block.mushroomRed));
 		buttered = new PotionBase("buttered", new ItemStack(Item.dyePowder, 1, 11));
 		smooth = new PotionBase("smooth", new ItemStack(Item.seeds));
 		suave = new PotionBase("suave", new ItemStack(Item.dyePowder, 1, 3));
 		debonair = new PotionBase("debonair", new ItemStack(Item.dyePowder, 1, 2));
 		thick = new PotionBase("thick", new ItemStack(Item.glowstone));
 		elegant = new PotionBase("elegant", new ItemStack(Item.enderPearl));
 		fancy = new PotionBase("fancy", new ItemStack(Item.flint));
 		charming = new PotionBase("charming", new ItemStack(Block.plantRed));
 		dashing = new PotionBase("dashing", new ItemStack(Item.silk));
 		refined = new PotionBase("refined", new ItemStack(Item.slimeBall));
 		cordial = new PotionBase("cordial", new ItemStack(Item.dyePowder, 1, 15));
 		sparkling = new PotionBase("sparkling", new ItemStack(Item.goldNugget));
 		potent = new PotionBase("potent", new ItemStack(Item.blazePowder));
 		foul = new PotionBase("foul", new ItemStack(Item.rottenFlesh));
 		odorless = new PotionBase("odorless", new ItemStack(Item.bread));
 		rank = new PotionBase("rank", new ItemStack(Item.egg));
 		harsh = new PotionBase("harsh", new ItemStack(Block.slowSand));
 		acrid = new PotionBase("acrid", new ItemStack(Item.fermentedSpiderEye));
 		gross = new PotionBase("gross", new ItemStack(Item.pumpkinSeeds));
 		stinky = new PotionBase("stinky", new ItemStack(Item.fishRaw));
 	}
 	
 	public static void initializePotionTypes()
 	{
		moveSlowdown = new PotionType(new PotionEffect(Potion.moveSlowdown.id, 20 * 90, 0), 4, 20 * 240, PotionType.getBaseBrewing(PotionList.dashing));
 		moveSpeed = new PotionType(new PotionEffect(Potion.moveSpeed.id, 20 * 180, 0), 7, 20 * 360, PotionList.moveSlowdown, new ItemStack(Item.sugar), PotionType.getBaseBrewing(PotionList.dashing));
		digSlowdown = new PotionType(new PotionEffect(Potion.digSlowdown.id, 20 * 90, 0), 4, 20 * 240, PotionType.getBaseBrewing(PotionList.dashing));
 		digSpeed = new PotionType(new PotionEffect(Potion.digSpeed.id, 20 * 180, 0), 7, 20 * 360, PotionList.digSlowdown, MorePotionsMod.dustGold, PotionType.getBaseBrewing(PotionList.dashing));
 		weakness = new PotionType(new PotionEffect(Potion.weakness.id, 20 * 90, 0), 2, 20 * 240, new ItemStack(Item.fermentedSpiderEye), PotionList.awkward);
 		damageBoost = new PotionType(new PotionEffect(Potion.damageBoost.id, 20 * 180, 0), 4, 20 * 300, PotionList.weakness, new ItemStack(Item.blazePowder), PotionList.awkward);
		harm = new PotionType(new PotionEffect(Potion.harm.id, 1, 0), 1, 0, PotionType.getBaseBrewing(PotionList.thick));
 		heal = new PotionType(new PotionEffect(Potion.heal.id, 1, 0), 1, 0, PotionList.harm, new ItemStack(Item.speckledMelon), PotionType.getBaseBrewing(PotionList.thick));
 		doubleLife = new PotionType(new PotionEffect(MorePotionsMod.doubleLife.id, 1625000, 0), 0, 0, PotionList.harm, MorePotionsMod.dustNetherstar, PotionType.getBaseBrewing(PotionList.thick));
		healthBoost = new PotionType(new PotionEffect(Potion.field_76434_w.id, 45 * 20, 0), 4, 120 * 20, PotionType.getBaseBrewing(PotionList.thick));
 		absorption = new PotionType(new PotionEffect(Potion.field_76444_x.id, 45 * 20, 0), 4, 120 * 20, PotionList.healthBoost, new ItemStack(Item.appleGold), PotionType.getBaseBrewing(PotionList.thick));
		jump = new PotionType(new PotionEffect(Potion.jump.id, 20 * 180, 0), 4, 20 * 300, PotionType.getBaseBrewing(PotionList.dashing));
 		confusion = new PotionType(new PotionEffect(Potion.confusion.id, 20 * 90, 0), 2, 20 * 180, new ItemStack(Item.poisonousPotato), PotionList.awkward);
 		regeneration = new PotionType(new PotionEffect(Potion.regeneration.id, 20 * 45, 0), 2, 20 * 180, PotionList.moveSlowdown, new ItemStack(Item.ghastTear), PotionList.awkward);
 		resistance = new PotionType(new PotionEffect(Potion.resistance.id, 20 * 180, 0), 3, 20 * 240, MorePotionsMod.dustDiamond, PotionType.getBaseBrewing(PotionList.thick));
 		ironSkin = new PotionType(new PotionEffect(MorePotionsMod.ironSkin.id, 20 * 120, 0), 1, 20 * 240, MorePotionsMod.dustIron, PotionType.getBaseBrewing(PotionList.thick));
 		obsidianSkin = new PotionType(new PotionEffect(MorePotionsMod.obsidianSkin.id, 20 * 120, 0), 1, 20 * 240, MorePotionsMod.dustObsidian, PotionType.getBaseBrewing(PotionList.thick));
 		fireResistance = new PotionType(new PotionEffect(Potion.fireResistance.id, 20 * 180, 0), 0, 20 * 360, PotionList.moveSlowdown, new ItemStack(Item.magmaCream), PotionList.awkward);
		waterWalking = new PotionType(new PotionEffect(MorePotionsMod.waterWalking.id, 20 * 120, 0), 0, 240 * 20, PotionList.awkward);
 		waterBreathing = new PotionType(new PotionEffect(Potion.waterBreathing.id, 20 * 180, 0), 2, 20 * 360, PotionList.waterWalking, MorePotionsMod.dustClay, PotionList.awkward);
 		coldness = new PotionType(new PotionEffect(MorePotionsMod.coldness.id, 20 * 180, 0), 1, 20 * 360, new ItemStack(Item.snowball), PotionList.awkward);
 		invisibility = new PotionType(new PotionEffect(Potion.invisibility.id, 20 * 180, 0), 0, 720 * 20);
 		blindness = new PotionType(new PotionEffect(Potion.blindness.id, 20 * 90, 0), 0, 20 * 240, new ItemStack(Item.dyePowder, 1, 0), PotionType.getBaseBrewing(PotionList.thin));
 		nightVision = new PotionType(new PotionEffect(Potion.nightVision.id, 20 * 180, 0), 0, 20 * 300, PotionList.invisibility, new ItemStack(Item.goldenCarrot), PotionType.getBaseBrewing(PotionList.thin));
 		poison = new PotionType(new PotionEffect(Potion.poison.id, 20 * 45, 0), 2, 20 * 60, new ItemStack(Item.spiderEye), PotionType.getBaseBrewing(PotionList.acrid));
		hunger = new PotionType(new PotionEffect(Potion.hunger.id, 20 * 45, 0), 3, 20 * 60, PotionType.getBaseBrewing(PotionList.acrid));
 		saturation = new PotionType(new PotionEffect(Potion.field_76443_y.id, 20 * 45, 0), 3, 20 * 60, PotionList.hunger, new ItemStack(Item.bread), PotionType.getBaseBrewing(PotionList.awkward));
 		wither = new PotionType(new PotionEffect(Potion.wither.id, 450, 0), 1, 20 * 60, MorePotionsMod.dustWither, PotionType.getBaseBrewing(PotionList.acrid));
		explosiveness = new PotionType(new PotionEffect(MorePotionsMod.explosiveness.id, 20 * 10, 0), 4, 20 * 20, PotionList.awkward);
 		fire = new PotionType(new PotionEffect(MorePotionsMod.fire.id, 20 * 10, 0), 0, 20 * 20, PotionList.explosiveness, new ItemStack(Item.fireballCharge), PotionList.awkward);
 		random = new PotionType(new PotionEffect(MorePotionsMod.random.id, MorePotionsMod.randomMode == 0 ? 1 : 20 * 45, 0), 0, MorePotionsMod.randomMode == 0 ? 1 : 20 * 90, new ItemStack(BrewingAPI.potion2), PotionList.awkward);
 		effectRemove = new PotionType(new PotionEffect(MorePotionsMod.effectRemove.id, 20 * 45, 0), 0, 20 * 90, PotionList.random, new ItemStack(Item.bucketMilk), PotionList.awkward);
 		
 		thorns = new PotionType(new PotionEffect(MorePotionsMod.thorns.id, 20 * 60, 0), 3, 20 * 120, null, new ItemStack(Block.cactus), PotionList.awkward);
 		greenThumb = new PotionType(new PotionEffect(MorePotionsMod.greenThumb.id, 20 * 60, 0), 2, 20 * 120, null, new ItemStack(Block.leaves), PotionList.awkward);
 		projectile = new PotionType(new PotionEffect(MorePotionsMod.projectile.id, 20 * 60, 0), 2, 20 * 120, null, new ItemStack(Item.arrow), PotionList.awkward);	
 	}
 	
 	public static void registerPotionBases()
 	{
 		awkward.register();
 		thick.register();
 		thin.register();
 		dashing.register();
 		acrid.register();
 		
 		if (SHOW_ALL_BASEBREWINGS)
 		{
 			mundane.register();
 			elegant.register();
 			uninteresting.register();
 			bland.register();
 			clear.register();
 			milky.register();
 			diffuse.register();
 			artless.register();
 			flat.register();
 			bulky.register();
 			bungling.register();
 			buttered.register();
 			smooth.register();
 			suave.register();
 			debonair.register();
 			fancy.register();
 			charming.register();
 			dashing.register();
 			refined.register();
 			cordial.register();
 			sparkling.register();
 			potent.register();
 			foul.register();
 			odorless.register();
 			rank.register();
 			harsh.register();
 			gross.register();
 			stinky.register();
 		}
 	}
 	
 	public static void registerPotionTypes()
 	{
 		regeneration.register();
 		moveSpeed.register();
 		moveSlowdown.register();
 		digSpeed.register();
 		digSlowdown.register();
 		fireResistance.register();
 		waterBreathing.register();
 		waterWalking.register(); //
 		coldness.register(); //
 		doubleLife.register(); //
 		heal.register();
 		harm.register();
 		healthBoost.register();
 		absorption.register();
 		poison.register();
 		fire.register();
 		explosiveness.register();
 		wither.register();
 		saturation.register();
 		hunger.register();
 		confusion.register();
 		nightVision.register();
 		invisibility.register();
 		blindness.register();
 		damageBoost.register();
 		weakness.register();
 		jump.register();
 		resistance.register();
 		thorns.register(); //
 		projectile.register();
 		greenThumb.register(); //
 		ironSkin.register(); //
 		obsidianSkin.register(); //
 		effectRemove.register(); //
 		random.register();
 	}
 }
