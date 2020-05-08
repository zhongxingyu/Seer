 package hunternif.mc.dota2items.config;
 
 import hunternif.mc.dota2items.block.BlockCycloneContainer;
 import hunternif.mc.dota2items.core.buff.Buff;
 import hunternif.mc.dota2items.core.buff.BuffPoorMansShield;
 import hunternif.mc.dota2items.inventory.Column;
 import hunternif.mc.dota2items.item.ArcaneBoots;
 import hunternif.mc.dota2items.item.BlinkDagger;
 import hunternif.mc.dota2items.item.Clarity;
 import hunternif.mc.dota2items.item.Daedalus;
 import hunternif.mc.dota2items.item.Dagon;
 import hunternif.mc.dota2items.item.Dota2Item;
 import hunternif.mc.dota2items.item.Dota2Logo;
 import hunternif.mc.dota2items.item.EulsScepter;
 import hunternif.mc.dota2items.item.ForceStaff;
 import hunternif.mc.dota2items.item.GoldCoin;
 import hunternif.mc.dota2items.item.HandOfMidas;
 import hunternif.mc.dota2items.item.ItemRecipe;
 import hunternif.mc.dota2items.item.MaskOfMadness;
 import hunternif.mc.dota2items.item.PhaseBoots;
 import hunternif.mc.dota2items.item.QuellingBlade;
 import hunternif.mc.dota2items.item.Salve;
 import hunternif.mc.dota2items.item.Tango;
 
 public class Config {
 	// Special items
 		public static CfgInfo<Dota2Logo> dota2Logo = new CfgInfo<Dota2Logo>(27000, "Dota 2 Logo");
 		public static CfgInfo<GoldCoin> goldCoin = new CfgInfo<GoldCoin>(27007, "Gold Coin");
 		public static CfgInfo<ItemRecipe> recipe = new CfgInfo<ItemRecipe>(27011, "Recipe");
 		
 		// Items column: Secret Shop ==================================================================
 //		orbOfVenom
 		public static CfgInfo<Dota2Item> ringOfHealth = new CfgInfo<Dota2Item>(27039, "Ring of Health")
 				.setPrice(875).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Ring of Health").setHealthRegen(5));
 		public static CfgInfo<Dota2Item> voidStone = new CfgInfo<Dota2Item>(27008, "Void Stone")
 				.setPrice(875).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Void Stone").setManaRegenPercent(100));
 		public static CfgInfo<Dota2Item> hyperstone = new CfgInfo<Dota2Item>(27040, "Hyperstone")
 				.setPrice(2100).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Hyperstone").setAttackSpeed(55));
 		public static CfgInfo<Dota2Item> demonEdge = new CfgInfo<Dota2Item>(27041, "Demon Edge")
 				.setPrice(2400).setColumn(Column.SECRET_SHOP)
 				.setWeaponDamage(7).setPassiveBuff(new Buff("Demon Edge").setDamage(46));
 		public static CfgInfo<Dota2Item> sacredRelic = new CfgInfo<Dota2Item>(27042, "Sacred Relic")
 				.setPrice(3800).setColumn(Column.SECRET_SHOP)
 				.setWeaponDamage(7).setPassiveBuff(new Buff("Sacred Relic").setDamage(60));
 		public static CfgInfo<Dota2Item> reaver = new CfgInfo<Dota2Item>(27043, "Reaver")
 				.setPrice(3200).setColumn(Column.SECRET_SHOP)
 				.setWeaponDamage(7).setPassiveBuff(new Buff("Reaver").setStrength(25));
 		public static CfgInfo<Dota2Item> eaglesong = new CfgInfo<Dota2Item>(27044, "Eaglesong")
 				.setPrice(3300).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Eaglesong").setAgility(25));
 		public static CfgInfo<Dota2Item> mysticStaff = new CfgInfo<Dota2Item>(27045, "Mystic Staff")
 				.setPrice(2700).setColumn(Column.SECRET_SHOP)
 				.setWeaponDamage(2).setPassiveBuff(new Buff("Mystic Staff").setIntelligence(25));
 		public static CfgInfo<Dota2Item> vitalityBooster = new CfgInfo<Dota2Item>(27046, "Vitality Booster")
 				.setPrice(1100).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Vitality Booster").setHealth(250));
 		public static CfgInfo<Dota2Item> energyBooster = new CfgInfo<Dota2Item>(27047, "Energy Booster")
 				.setPrice(1000).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Energy Booster").setMana(250));
 		public static CfgInfo<Dota2Item> pointBooster = new CfgInfo<Dota2Item>(27048, "Point Booster")
 				.setPrice(1200).setColumn(Column.SECRET_SHOP)
 				.setPassiveBuff(new Buff("Point Booster").setHealth(200).setMana(150));
 		
 		// Items column: Consumables ==================================================================
 		public static CfgInfo<Clarity> clarity = new CfgInfo<Tango>(27059, "Clarity")
 				.setPrice(50).setColumn(Column.CONSUMABLES)
 				.setDescription("Use: Restores mana over time. If the user is attacked, "
 						+ "the effect is lost.\n[Duration:] {30}\n[Mana restored:] {100}");
 		public static CfgInfo<Tango> tango = new CfgInfo<Tango>(27002, "Tango")
 				.setPrice(30).setColumn(Column.CONSUMABLES)
 				.setDescription("Use: Eat Tree - Consume a tree to restore HP over time. "
 						+ "Comes with 3 charges.\n[Duration:] {16}\n[Health restored:] {115}");
 		public static CfgInfo<Salve> healingSalve = new CfgInfo<Salve>(27063, "Healing Salve")
 			.setPrice(100).setColumn(Column.CONSUMABLES)
 			.setDescription("Use: Regenerate - Restores HP over time. If the user is attacked, "
 					+ "the effect is lost.\n[Duration:] {10}\n[Health restored:] {400}");
 //		smokeOfDeceit,
 //		townPortal,
 //		dustOfAppearance,
 //		animalCourier,
 //		flyingCourier,
 //		observerWard,
 //		sentryWard,
 //		bottle
 		
 		// Item column: Attributes ====================================================================
 		public static CfgInfo<Dota2Item> ironBranch = new CfgInfo<Dota2Item>(27012, "Iron Branch")
 				.setPrice(53).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Iron Branch").setStrength(1).setAgility(1).setIntelligence(1));
 		public static CfgInfo<Dota2Item> gauntletsOfStrength = new CfgInfo<Dota2Item>(27013, "Gauntlets of Strength")
 				.setPrice(150).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Gauntlets of Strength").setStrength(3));
 		public static CfgInfo<Dota2Item> slippersOfAgility = new CfgInfo<Dota2Item>(27014, "Slippers of Agility")
 				.setPrice(150).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Slippers of Agility").setAgility(3));
 		public static CfgInfo<Dota2Item> mantleOfIntelligence =	new CfgInfo<Dota2Item>(27015, "Mantle of Intelligence")
 				.setPrice(150).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Mantle of Intelligence").setIntelligence(3));
 		public static CfgInfo<Dota2Item> circlet = new CfgInfo<Dota2Item>(27016, "Circlet")
 				.setPrice(185).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Circlet").setStrength(2).setAgility(2).setIntelligence(2));
 		public static CfgInfo<Dota2Item> beltOfStrength = new CfgInfo<Dota2Item>(27017, "Belt of Strength")
 				.setPrice(450).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Belt of Strength").setStrength(6));
 		public static CfgInfo<Dota2Item> bandOfElvenskin = new CfgInfo<Dota2Item>(27018, "Band of Elvenskin")
 				.setPrice(450).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Band of Elvenskin").setAgility(6));
 		public static CfgInfo<Dota2Item> robeOfTheMagi = new CfgInfo<Dota2Item>(27019, "Robe of the Magi")
 				.setPrice(450).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Robe of the Magi").setIntelligence(6));
 		public static CfgInfo<Dota2Item> ogreClub = new CfgInfo<Dota2Item>(27020, "Ogre Club")
 				.setPrice(1000).setColumn(Column.ATTRIBUTES)
 				.setWeaponDamage(7).setPassiveBuff(new Buff("Ogre Club").setStrength(10));
 		public static CfgInfo<Dota2Item> bladeOfAlacrity = new CfgInfo<Dota2Item>(27021, "Blade of Alacrity")
 				.setPrice(1000).setColumn(Column.ATTRIBUTES)
 				.setWeaponDamage(7).setPassiveBuff(new Buff("Blade of Alacrity").setAgility(10));
 		public static CfgInfo<Dota2Item> staffOfWizardry = new CfgInfo<Dota2Item>(27010, "Staff of Wizardry")
 				.setPrice(1000).setWeaponDamage(2).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Staff of Wizardry").setIntelligence(10));
 		public static CfgInfo<Dota2Item> ultimateOrb = new CfgInfo<Dota2Item>(27022, "Ultimate Orb")
 				.setPrice(2100).setColumn(Column.ATTRIBUTES)
 				.setPassiveBuff(new Buff("Ultimate Orb").setStrength(10).setAgility(10).setIntelligence(10));
 		
 		// Item column: Armaments =====================================================================
 		public static CfgInfo<Dota2Item> ringOfProtection = new CfgInfo<Dota2Item>(27005, "Ring of Protection")
 				.setPrice(175).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Ring of Protection").setArmor(2));
 		public static CfgInfo<QuellingBlade> quellingBlade = new CfgInfo<QuellingBlade>(27003, "Quelling Blade")
 				.setPrice(225).setWeaponDamage(6).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Quelling Blade").setDamagePercent(32, 16).setDoesNotStack())
 				.setDescription("Active: Destroy Tree/Ward - Destroy a target tree, or deals 100 damage to a ward.\n" +
 				"Passive: Quell - Gives bonus attack damage against non-player units, depending on the type of your " +
 				"weapon (melee or ranged).\n[Melee bonus:] {32%}\n[Ranged bonus:] {12%}");
 		public static CfgInfo<Dota2Item> stoutShield = new CfgInfo<Dota2Item>(27031, "Stout Shield")
 				.setPrice(250).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Stout Shield").setDamageBlock(20, 10, 60))
 				.setDescription("Passive: Damage Block - Gives a chance to block damage, depending on the type of your weapon (melee or ranged).");
 		public static CfgInfo<Dota2Item> bladesOfAttack = new CfgInfo<Dota2Item>(27023, "Blades of Attack")
 				.setPrice(450).setWeaponDamage(5).setIsFull3D(false).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Blades of Attack").setDamage(9));
 		public static CfgInfo<Dota2Item> chainmail = new CfgInfo<Dota2Item>(27024, "Chainmail")
 				.setPrice(550).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Chainmail").setArmor(5));
 		public static CfgInfo<Dota2Item> helmOfIronWill = new CfgInfo<Dota2Item>(27025, "Helm of Iron Will")
 				.setPrice(950).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Helm of Iron Will").setArmor(5).setHealthRegen(3));
 		public static CfgInfo<Dota2Item> broadsword = new CfgInfo<Dota2Item>(27026, "Broadsword")
 				.setPrice(1200).setWeaponDamage(7).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Broadsword").setDamage(18));
 		public static CfgInfo<Dota2Item> quarterstaff = new CfgInfo<Dota2Item>(27027, "Quarterstaff")
 				.setPrice(900).setWeaponDamage(4).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Quarterstaff").setDamage(10).setAttackSpeed(10));
 		public static CfgInfo<Dota2Item> claymore = new CfgInfo<Dota2Item>(27028, "Claymore")
 				.setPrice(1400).setWeaponDamage(7).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Claymore").setDamage(21));
 		public static CfgInfo<Dota2Item> javelin = new CfgInfo<Dota2Item>(27064, "Javelin")
 				.setPrice(1500).setWeaponDamage(6).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Javelin").setDamage(21).setBonusDamage(40, 20))
 				.setDescription("Passive: Pierce - Grants a chance to deal bonus damage.\n"
 				+ "[Chance to Pierce:] {20%}\n[Pierce Damage:] {40}");
 		public static CfgInfo<Dota2Item> platemail = new CfgInfo<Dota2Item>(27029, "Platemail")
 				.setPrice(1400).setColumn(Column.ARMAMENTS)
 				.setPassiveBuff(new Buff("Platemail").setArmor(10));
 		public static CfgInfo<Dota2Item> mithrilHammer = new CfgInfo<Dota2Item>(27030, "Mithril Hammer")
 				.setPrice(1600).setColumn(Column.ARMAMENTS)
 				.setWeaponDamage(7).setPassiveBuff(new Buff("Mithril Hammer").setDamage(24));
 		
 		// Item column: Arcane ========================================================================
 //		magicStick
 		public static CfgInfo<Dota2Item> sagesMask = new CfgInfo<Dota2Item>(27009, "Sage's Mask")
 				.setPrice(325).setColumn(Column.ARCANE)
 				.setPassiveBuff(new Buff("Sage's Mask").setManaRegenPercent(50));
 		public static CfgInfo<Dota2Item> ringOfRegen = new CfgInfo<Dota2Item>(27032, "Ring of Regen")
 				.setPrice(350).setColumn(Column.ARCANE)
 				.setPassiveBuff(new Buff("Ring of Regen").setHealthRegen(2));
 		public static CfgInfo<Dota2Item> bootsOfSpeed = new CfgInfo<Dota2Item>(27006, "Boots of Speed")
 				.setPrice(450).setColumn(Column.ARCANE)
 				.setPassiveBuff(new Buff("Boots of Speed").setMovementSpeed(50, true));
 		public static CfgInfo<Dota2Item> glovesOfHaste = new CfgInfo<Dota2Item>(27033, "Gloves of Haste")
 				.setPrice(500).setColumn(Column.ARCANE)
 				.setPassiveBuff(new Buff("Gloves of Haste").setAttackSpeed(15));
 		public static CfgInfo<Dota2Item> cloak = new CfgInfo<Dota2Item>(27034, "Cloak")
 			.setPrice(550).setColumn(Column.ARCANE)
 			.setPassiveBuff(new Buff("Cloak").setSpellResistance(15))
 			.setDescription("Multiple instances of spell resistance from items do not stack.");
 //		gemOfTrueSight
 		public static CfgInfo<Dota2Item> morbidMask = new CfgInfo<Dota2Item>(27062, "Morbid Mask")
 				.setPrice(900).setColumn(Column.ARCANE)
 				.setPassiveBuff(new Buff("Morbid Mask").setLifesteal(15));
 //		ghostScepter
 		public static CfgInfo<Dota2Item> talismanOfEvasion = new CfgInfo<Dota2Item>(27035, "Talisman of Evasion")
 				.setPrice(1800).setColumn(Column.ARCANE)
 				.setPassiveBuff(new Buff("Talisman of Evasion").setEvasionPercent(25));
 		public static CfgInfo<BlinkDagger> blinkDagger = new CfgInfo<BlinkDagger>(27001, "Blink Dagger")
 				.setPrice(2150).setWeaponDamage(4).setColumn(Column.ARCANE)
 				.setDescription("Active: Blink - Teleport to a target point up to 50 blocks away. " +
 				"If damage is taken from an enemy player, Blink Dagger cannot be used for 3 seconds.");
 //		shadowAmulet
 		
 		// Item column: Common ========================================================================
 		public static CfgInfo<Dota2Item> wraithBand = new CfgInfo<Dota2Item>(27036, "Wraith Band")
 				.setPrice(150).setRecipe(slippersOfAgility, circlet).setColumn(Column.COMMON)
 				.setPassiveBuff(new Buff("Wraith Band").setStrength(3).setAgility(6).setIntelligence(3).setDamage(3));
 		public static CfgInfo<Dota2Item> nullTalisman = new CfgInfo<Dota2Item>(27037, "Null Talisman")
 				.setPrice(135).setRecipe(mantleOfIntelligence, circlet).setColumn(Column.COMMON)
 				.setPassiveBuff(new Buff("Null Talisman").setStrength(3).setAgility(3).setIntelligence(6).setDamage(3));
 //		magicWand
 		public static CfgInfo<Dota2Item> bracer = new CfgInfo<Dota2Item>(27038, "Bracer")
 				.setPrice(190).setRecipe(gauntletsOfStrength, circlet).setColumn(Column.COMMON)
 				.setPassiveBuff(new Buff("Bracer").setStrength(6).setAgility(3).setIntelligence(3).setDamage(3));
 		public static CfgInfo<Dota2Item> poorMansShield = new CfgInfo<Dota2Item>(27068, "Poor Man's Shield")
 				.setRecipe(stoutShield, slippersOfAgility, slippersOfAgility).setColumn(Column.COMMON)
 				.setPassiveBuff(new BuffPoorMansShield("Poor Man's Shield"))
 				.setDescription("Passive: Damage Block - Blocks physical attack damage, " +
 				"depending on the type of your weapon (melee or ranged). Poor Man's Shield will always block " +
 				"attacks from enemy Heroes, but has a chance to block damage from creeps.");
 //		soulRing
 		public static CfgInfo<PhaseBoots> phaseBoots = new CfgInfo<PhaseBoots>(27065, "Phase Boots")
 			.setRecipe(bootsOfSpeed, bladesOfAttack, bladesOfAttack).setColumn(Column.COMMON)
 			.setPassiveBuff(new Buff("Phase Boots").setDamage(24).setMovementSpeed(55, true))
 			.setDescription("Active: Phase - Gives increased movement speed and makes you immune " +
 					"to knockback. Phase is cancelled upon using another item or ability.\n" +
 					"[Phase duration:] {4}\n[Phase move boost:] {16%}");
 //		powerTreads
 		public static CfgInfo<Dota2Item> oblivionStaff = new CfgInfo<Dota2Item>(27051, "Oblivion Staff")
 				.setWeaponDamage(2).setColumn(Column.COMMON)
 				.setRecipe(quarterstaff, sagesMask, robeOfTheMagi)
 				.setPassiveBuff(new Buff("Oblivion Staff").setIntelligence(6).setAttackSpeed(10).setDamage(15).setManaRegenPercent(75));
 		public static CfgInfo<Dota2Item> perseverance = new CfgInfo<Dota2Item>(27050, "Perseverance")
 				.setRecipe(ringOfHealth, voidStone).setColumn(Column.COMMON)
 				.setPassiveBuff(new Buff("Perseverance").setHealthRegen(5).setManaRegenPercent(125).setDamage(10));
 		public static CfgInfo<HandOfMidas> handOfMidas = new CfgInfo<HandOfMidas>(27060, "Hand of Midas")
 			.setPrice(1400).setRecipe(glovesOfHaste).setColumn(Column.COMMON)
 			.setPassiveBuff(new Buff("Hand of Midas").setAttackSpeed(30))
 			.setDescription("Active: Transmute - Kills a non-player target for 190 gold and 2.5x experience.");
 //		bootsOfTravel
 		
 		// Item column: Support =======================================================================
 //		ringOfBasilius
 //		headdress
 //		buckler
 //		urnOfShadows
 //		ringOfAquila
 //		tranquilBoots
 //		medallionOfCourage
 		public static CfgInfo<ArcaneBoots> arcaneBoots = new CfgInfo<ArcaneBoots>(27066, "Arcane Boots")
 				.setRecipe(bootsOfSpeed, energyBooster).setColumn(Column.SUPPORT)
 				.setPassiveBuff(new Buff("Arcane Boots").setMana(250).setMovementSpeed(60, true))
 				.setDescription("Active: Replenish Mana - Restores mana in an area around the player.\n" +
 						"[Mana restored:] {135}\n[Radius:] {6} [blocks]");
 //		drumOfEndurance
 //		vladmirsOffering
 //		mekansm
 //		pipeOfInsight
 		
 		// Item column: Caster ========================================================================
 		public static CfgInfo<ForceStaff> forceStaff = new CfgInfo<ForceStaff>(27069, "Force Staff")
 				.setPrice(900).setWeaponDamage(2).setColumn(Column.CASTER)
 				.setPassiveBuff( new Buff("Force Staff").setIntelligence(10).setHealthRegen(3) )
 				.setRecipe(staffOfWizardry, ringOfRegen)
 				.setDescription("Active (left click): Force - Pushes any target unit 15 " +
 						"blocks in the direction it is facing. Right-click to self-cast.");
 //		necronomicon
 		public static CfgInfo<EulsScepter> eulsScepter = new CfgInfo<EulsScepter>(27004, "Eul's Scepter of Divinity")
 				.setPrice(600).setWeaponDamage(2).setColumn(Column.CASTER)
 				.setPassiveBuff( new Buff("Eul's Scepter of Divinity").setMovementSpeed(30, false).setIntelligence(10).setManaRegenPercent(150) )
 				.setRecipe(staffOfWizardry, sagesMask, voidStone)
 				.setDescription("Active (left click on unit or right click on block below): Cyclone - "
 						+ "Target unit is swept up in a cyclone for 2.5 seconds, and is invulnerable.");
 		public static CfgInfo<Dagon> dagon1 = new CfgInfo<Dagon>(27070, "Dagon level 1")
				.setPrice(1250).setRecipe(staffOfWizardry, nullTalisman).setColumn(Column.CASTER)
 				.setWeaponDamage(2).setUpgradeLevel(1).setDescription("Active (left click): "
 						+ "Energy Burst - Burst of damage to target enemy unit. Upgradable.");
 		public static CfgInfo<Dagon> dagon2 = new CfgInfo<Dagon>(27071, "Dagon level 2")
 				.setPrice(1250).setRecipe(dagon1).setWeaponDamage(2).setColumn(Column.CASTER)
 				.setDescription(dagon1.description).setUpgradeLevel(2).setBaseShopItem(dagon1);
 		public static CfgInfo<Dagon> dagon3 = new CfgInfo<Dagon>(27072, "Dagon level 3")
 				.setPrice(1250).setRecipe(dagon2).setWeaponDamage(2).setColumn(Column.CASTER)
 				.setDescription(dagon1.description).setUpgradeLevel(3).setBaseShopItem(dagon1);
 		public static CfgInfo<Dagon> dagon4 = new CfgInfo<Dagon>(27073, "Dagon level 4")
 				.setPrice(1250).setRecipe(dagon3).setWeaponDamage(2).setColumn(Column.CASTER)
 				.setDescription(dagon1.description).setUpgradeLevel(4).setBaseShopItem(dagon1);
 		public static CfgInfo<Dagon> dagon5 = new CfgInfo<Dagon>(27074, "Dagon level 5")
 				.setPrice(1250).setRecipe(dagon4).setWeaponDamage(2).setColumn(Column.CASTER)
 				.setDescription(dagon1.description).setUpgradeLevel(5).setBaseShopItem(dagon1);
 //		veilOfDiscord
 //		rodOfAtos
 		public static CfgInfo<Dota2Item> aghanimsScepter = new CfgInfo<Dota2Item>(27056, "Aghanim's Scepter")
 				.setWeaponDamage(2).setColumn(Column.CASTER)
 				.setRecipe(pointBooster, ogreClub, bladeOfAlacrity, staffOfWizardry)
 				.setPassiveBuff(new Buff("Aghanim's Scepter").setHealth(200).setMana(150).setStrength(10).setAgility(10).setIntelligence(10));
 //		orchidMalevolence
 //		refresherOrb
 //		scytheOfVyse
 		
 		// Item column: Weapons
 		public static CfgInfo<Dota2Item> crystalys = new CfgInfo<Dota2Item>(27057, "Crystalys")
 				.setWeaponDamage(7).setColumn(Column.WEAPONS)
 				.setPrice(500).setRecipe(broadsword, bladesOfAttack)
 				.setPassiveBuff(new Buff("Crystalys").setDamage(30).setCrit(20, 175))
 				.setDescription("Passive: Critical Strike - Grants a chance to deal critical damage on an attack.");
 //		armletOfMordiggian
 //		skullBasher
 //		shadowBlade
 //		battleFury
 //		etherealBlade
 //		radiance
 //		monkeyKingBar
 		public static CfgInfo<Daedalus> daedalus = new CfgInfo<Dota2Item>(27058, "Daedalus")
 			.setIsFull3D(true).setColumn(Column.WEAPONS)
 			.setPrice(1000).setRecipe(crystalys, demonEdge)
 			.setPassiveBuff(new Buff("Daedalus").setDamage(81).setCrit(25, 240))
 			.setDescription("Passive: Critical Strike - Grants a chance to deal critical damage on an attack.");
 		public static CfgInfo<Dota2Item> butterfly = new CfgInfo<Dota2Item>(27053, "Butterfly")
 				.setWeaponDamage(7).setColumn(Column.WEAPONS)
 				.setRecipe(talismanOfEvasion, quarterstaff, eaglesong)
 				.setPassiveBuff(new Buff("Butterfly").setAgility(30).setDamage(30).setEvasionPercent(35).setAttackSpeed(30));
 		public static CfgInfo<Dota2Item> divineRapier = new CfgInfo<Dota2Item>(27054, "Divine Rapier")
 				.setDropsOnDeath(true).setWeaponDamage(7).setColumn(Column.WEAPONS)
 				.setRecipe(sacredRelic, demonEdge)
 				.setPassiveBuff(new Buff("Divine Rapier").setDamage(300));
 //		abyssalBlade
 		
 		// Item column: Armor
 		public static CfgInfo<Dota2Item> hoodOfDefiance = new CfgInfo<Dota2Item>(27055, "Hood of Defiance")
 				.setRecipe(ringOfHealth, cloak, ringOfRegen, ringOfRegen).setColumn(Column.ARMOR)
 				.setPassiveBuff(new Buff("Hood of Defiance").setHealthRegen(8).setSpellResistance(30))
 				.setDescription("Multiple instances of spell resistance from items do not stack.");
 //		bladeMail
 		public static CfgInfo<Dota2Item> vanguard = new CfgInfo<Dota2Item>(27052, "Vanguard")
 				.setRecipe(ringOfHealth, vitalityBooster, stoutShield).setColumn(Column.ARMOR)
 				.setPassiveBuff(new Buff("Vanguard").setHealth(250).setHealthRegen(6).setDamageBlock(40, 20, 70))
 				.setDescription("Passive: Damage Block - Gives a chance to block damage, depending on the type of your weapon (melee or ranged).");
 		public static CfgInfo<Dota2Item> soulBooster = new CfgInfo<Dota2Item>(27049, "Soul Booster")
 				.setRecipe(vitalityBooster, energyBooster, pointBooster).setColumn(Column.ARMOR)
 				.setPassiveBuff(new Buff("Soul Booster").setHealth(450).setMana(400).setHealthRegen(4).setManaRegenPercent(100));
 //		blackKingBar
 //		shivasGuard
 //		mantaStyle
 //		bloodstone
 //		linkensSphere
 //		assaultCuirass
 //		heartOfTarrasque
 		
 		// Item column: Artifacts
 //		helmOfTheDominator
 		public static CfgInfo<MaskOfMadness> maskOfMadness = new CfgInfo<MaskOfMadness>(27067, "Mask of Madness")
 				.setPrice(1000).setRecipe(morbidMask).setColumn(Column.ARTIFACTS)
 				.setPassiveBuff(new Buff("Mask of Madness").setLifesteal(17))
 				.setDescription("Active: Berserk - Gives 100 attack speed and 30% movement speed " +
 						"but causes you to take extra 30% damage. Lasts 12 seconds.\nPassive: " +
 						"Lifesteal - Grants lifesteal on attacks.");
 //		sange
 		public static CfgInfo<Dota2Item> yasha = new CfgInfo<Dota2Item>(27061, "Yasha")
 				.setWeaponDamage(7).setColumn(Column.ARTIFACTS)
 				.setPrice(600).setRecipe(bladeOfAlacrity, bandOfElvenskin)
 				.setPassiveBuff(new Buff("Yasha").setAgility(16).setAttackSpeed(15).setMovementSpeedPercent(10))
 				.setDescription("Percentage based movement speed bonuses from multiple items do not stack.");
 //		maelstrom
 //		diffusalBlade
 //		desolator
 //		heavensHalberd
 //		sangeAndYasha
 //		mjollnir
 //		eyeOfSkadi
 //		satanic
 		
 		// Last item ID: 27074
 		
 		// Blocks
 		public static CfgInfo<BlockCycloneContainer> cycloneContainer = new CfgInfo<BlockCycloneContainer>(2700, "Cyclone Container");
 }
