 package de.bdh.ks;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 
 
 public class KrimBlockName 
 {
 	public static HashMap<String,String> names = new HashMap<String,String>();
 	
 	public KrimBlockName()
 	{
 		//INIT VALUES
 		KrimBlockName.put("air", "0");
 		KrimBlockName.put("stone","1");
 		KrimBlockName.put("grassblock","2");
 		KrimBlockName.put("dirt","3");
 		KrimBlockName.put("cobblestone","4");
 		KrimBlockName.put("woodenplanks","5");
 		KrimBlockName.put("pinewoodenplanks","5:1");
 		KrimBlockName.put("birchwoodenplanks","5:2");
 		KrimBlockName.put("djunglewoodenplanks","5:3");
 		KrimBlockName.put("sapling","6");
		KrimBlockName.put("birchsapling","6:1");
		KrimBlockName.put("pinesapling","6:2");
 		KrimBlockName.put("djunglesapling","6:3");
 		KrimBlockName.put("bedrock","7");
 		KrimBlockName.put("watersource","8");
 		KrimBlockName.put("waterstill","9");
 		KrimBlockName.put("lavasource","10");
 		KrimBlockName.put("lavastill","11");
 		KrimBlockName.put("sand","12");
 		KrimBlockName.put("gravel","13");
 		KrimBlockName.put("goldore","14");
 		KrimBlockName.put("ironore","15");
 		KrimBlockName.put("coalore","16");
 		KrimBlockName.put("wood","17");
		KrimBlockName.put("birchwood","17:1");
		KrimBlockName.put("pinewood","17:2");
 		KrimBlockName.put("djunglewood","17:3");
 		KrimBlockName.put("leaves","18");
		KrimBlockName.put("birchleaves","18:6");
 		KrimBlockName.put("oakleaves","18:4");
		KrimBlockName.put("spruceleaves","18:5");
 		KrimBlockName.put("djungleleaves","18:7");
 		KrimBlockName.put("sponge","19");
 		KrimBlockName.put("glass","20");
 		KrimBlockName.put("lapislazuliore","21");
 		KrimBlockName.put("lapislazuliblock","22");
 		KrimBlockName.put("dispenser","23");
 		KrimBlockName.put("sandstone","24");
 		KrimBlockName.put("chiseledsandstone","24:1");
 		KrimBlockName.put("smoothsandstone","24:2");
 		KrimBlockName.put("noteblock","25");
 		KrimBlockName.put("poweredtrack","27");
 		KrimBlockName.put("detectortrack","28");
 		KrimBlockName.put("stickypiston","29");
 		KrimBlockName.put("cobweb","30");
 		KrimBlockName.put("grass","31:1");
 		KrimBlockName.put("fern","31:2");
 		KrimBlockName.put("deadbush","32");
 		KrimBlockName.put("piston","33");
 		KrimBlockName.put("wool","35");
 		KrimBlockName.put("orangewool","35:1");
 		KrimBlockName.put("magentawool","35:2");
 		KrimBlockName.put("lightbluewool","35:3");
 		KrimBlockName.put("yellowwool","35:4");
 		KrimBlockName.put("limewool","35:5");
 		KrimBlockName.put("pinkwool","35:6");
 		KrimBlockName.put("graywool","35:7");
 		KrimBlockName.put("lightgraywool","35:8");
 		KrimBlockName.put("cyanwool","35:9");
 		KrimBlockName.put("purplewool","35:10");
 		KrimBlockName.put("bluewool","35:11");
 		KrimBlockName.put("brownwool","35:12");
 		KrimBlockName.put("greenwool","35:13");
 		KrimBlockName.put("redwool","35:14");
 		KrimBlockName.put("blackwool","35:15");
 		KrimBlockName.put("flower","37");
 		KrimBlockName.put("rose","38");
 		KrimBlockName.put("brownmushroom","39");
 		KrimBlockName.put("redmushroom","40");
 		KrimBlockName.put("blockofgold","41");
 		KrimBlockName.put("goldblock","41");
 		KrimBlockName.put("blockofiron","42");
 		KrimBlockName.put("ironblock","42");
 		KrimBlockName.put("doubleslab","43");
 		KrimBlockName.put("doublesandstoneslab","43:1");
 		KrimBlockName.put("doublewoodenslab","43:2");
 		KrimBlockName.put("doublecobbelstoneslab","43:3");
 		KrimBlockName.put("doublebricksslab","43:4");
 		KrimBlockName.put("doublestonebricksslab","43:5");
 		KrimBlockName.put("stoneslab","44");
 		KrimBlockName.put("sandstoneslab","44:1");
 		KrimBlockName.put("woodenslab","44:2");
 		KrimBlockName.put("coobelstoneslab","44:3");
 		KrimBlockName.put("bricksslab","44:4");
 		KrimBlockName.put("stonebricksslab","44:5");
 		KrimBlockName.put("bricks","45");
 		KrimBlockName.put("tnt","46");
 		KrimBlockName.put("bookshelf","47");
 		KrimBlockName.put("mossstone","48");
 		KrimBlockName.put("obsidian","49");
 		KrimBlockName.put("torch","50");
 		KrimBlockName.put("fire","51");
 		KrimBlockName.put("monsterspawner(creeper)","52:50");
 		KrimBlockName.put("monsterspawner(skeleton)","52:51");
 		KrimBlockName.put("monsterspawner(spider)","52:52");
 		KrimBlockName.put("monsterspawner(giant)","52:53");
 		KrimBlockName.put("monsterspawner(zombie)","52:54");
 		KrimBlockName.put("monsterspawner(slime)","52:55");
 		KrimBlockName.put("monsterspawner(ghast)","52:56");
 		KrimBlockName.put("monsterspawner(pigzombie)","52:57");
 		KrimBlockName.put("monsterspawner(enderman)","52:58");
 		KrimBlockName.put("monsterspawner(cavespider)","52:59");
 		KrimBlockName.put("monsterspawner(silverfish)","52:60");
 		KrimBlockName.put("monsterspawner(blaze)","52:61");
 		KrimBlockName.put("monsterspawner(lavaslime)","52:62");
 		KrimBlockName.put("monsterspawner(pig)","52:90");
 		KrimBlockName.put("monsterspawner(sheep)","52:91");
 		KrimBlockName.put("monsterspawner(cow)","52:92");
 		KrimBlockName.put("monsterspawner(chicken)","52:93");
 		KrimBlockName.put("monsterspawner(squid)","52:94");
 		KrimBlockName.put("monsterspawner(wolf)","52:95");
 		KrimBlockName.put("monsterspawner(mushroomcow)","52:96");
 		KrimBlockName.put("monsterspawner(snowman)","52:97");
 		KrimBlockName.put("monsterspawner(ozelot)","52:98");
 		KrimBlockName.put("monsterspawner(irongolem)","52:99");
 		KrimBlockName.put("monsterspawner(villager)","52:120");
 		KrimBlockName.put("monsterspawner(horse)","52:220");
 		KrimBlockName.put("monsterspawner(ogre)","52:221");
 		KrimBlockName.put("monsterspawner(fireogre)","52:222");
 		KrimBlockName.put("monsterspawner(caveogre)","52:223");
 		KrimBlockName.put("monsterspawner(boar)","52:224");
 		KrimBlockName.put("monsterspawner(bear)","52:225");
 		KrimBlockName.put("monsterspawner(duck)","52:226");
 		KrimBlockName.put("monsterspawner(bigcat)","52:227");
 		KrimBlockName.put("monsterspawner(deer)","52:228");
 		KrimBlockName.put("monsterspawner(wildwolf)","52:229");
 		KrimBlockName.put("monsterspawner(polarbear)","52:230");
 		KrimBlockName.put("monsterspawner(wraith)","52:231");
 		KrimBlockName.put("monsterspawner(flamewraith)","52:232");
 		KrimBlockName.put("monsterspawner(bunny)","52:233");
 		KrimBlockName.put("monsterspawner(bird)","52:234");
 		KrimBlockName.put("monsterspawner(fox)","52:235");
 		KrimBlockName.put("monsterspawner(werewolf)","52:236");
 		KrimBlockName.put("monsterspawner(shark)","52:237");
 		KrimBlockName.put("monsterspawner(dolphin)","52:238");
 		KrimBlockName.put("monsterspawner(fishy)","52:239");
 		KrimBlockName.put("monsterspawner(kitty)","52:240");
 		KrimBlockName.put("monsterspawner(kittybed)","52:241");
 		KrimBlockName.put("monsterspawner(litterbox)","52:242");
 		KrimBlockName.put("monsterspawner(rat)","52:243");
 		KrimBlockName.put("monsterspawner(mouse)","52:244");
 		KrimBlockName.put("monsterspawner(hellrat)","52:245");
 		KrimBlockName.put("monsterspawner(scorpion)","52:246");
 		KrimBlockName.put("monsterspawner(turtle)","52:247");
 		KrimBlockName.put("monsterspawner(crocodile)","52:248");
 		KrimBlockName.put("monsterspawner(ray)","52:249");
 		KrimBlockName.put("monsterspawner(jellyfish)","52:250");
 		KrimBlockName.put("monsterspawner(goat)","52:251");
 		KrimBlockName.put("monsterspawner(snake)","52:252");
 		KrimBlockName.put("monsterspawner(mocegg)","52:253");
 		KrimBlockName.put("monsterspawner(fishbowl)","52:254");
 		KrimBlockName.put("monsterspawner(ostrich)","52:255");
 		KrimBlockName.put("woodenstairs","53");
 		KrimBlockName.put("chest","54");
 		KrimBlockName.put("diamondore","56");
 		KrimBlockName.put("blockofdiamond","57");
 		KrimBlockName.put("diamondblock","57");
 		KrimBlockName.put("craftingtable","58");
 		KrimBlockName.put("farmland","60");
 		KrimBlockName.put("furnace","61");
 		KrimBlockName.put("furnace_on","62");
 		KrimBlockName.put("ladder","65");
 		KrimBlockName.put("track","66");
 		KrimBlockName.put("cobblestonestairs","67");
 		KrimBlockName.put("lever","69");
 		KrimBlockName.put("pressureplate","70");
 		KrimBlockName.put("irondoorblock","71");
 		KrimBlockName.put("woodenpressureplate","72");
 		KrimBlockName.put("redstoneore","73");
 		KrimBlockName.put("redstonetorch_glow","74");
 		KrimBlockName.put("redstonetorch_off","75");
 		KrimBlockName.put("redstonetorch","76");
 		KrimBlockName.put("button","77");
 		KrimBlockName.put("snow","78");
 		KrimBlockName.put("ice","79");
 		KrimBlockName.put("snow","80");
 		KrimBlockName.put("cactus","81");
 		KrimBlockName.put("clay","82");
 		KrimBlockName.put("jukebox","84");
 		KrimBlockName.put("fence","85");
 		KrimBlockName.put("pumpkin","86");
 		KrimBlockName.put("netherrack","87");
 		KrimBlockName.put("soulsand","88");
 		KrimBlockName.put("glowstone","89");
 		KrimBlockName.put("portal","90");
 		KrimBlockName.put("jackolantern","91");
 		KrimBlockName.put("cake","92");
 		KrimBlockName.put("lockedchest","95");
 		KrimBlockName.put("trapdoor","96");
 		KrimBlockName.put("silverfishstone","97");
 		KrimBlockName.put("stonebricks","98");
 		KrimBlockName.put("mossystonebricks","98:1");
 		KrimBlockName.put("crackedstonebricks","98:2");
 		KrimBlockName.put("chiseledstonebricks","98:3");
 		KrimBlockName.put("hugebrownmushroom","99");
 		KrimBlockName.put("hugeredmushroom","100");
 		KrimBlockName.put("ironbars","101");
 		KrimBlockName.put("glasspanel","102");
 		KrimBlockName.put("melon","103");
 		KrimBlockName.put("vines","106");
 		KrimBlockName.put("fencegate","107");
 		KrimBlockName.put("brickstairs","108");
 		KrimBlockName.put("stonebricksatirs","109");
 		KrimBlockName.put("mycelium","110");
 		KrimBlockName.put("lilypad","111");
 		KrimBlockName.put("netherbrick","112");
 		KrimBlockName.put("netherbrickfence","113");
 		KrimBlockName.put("netherbrickstairs","114");
 		KrimBlockName.put("netherwart","115");
 		KrimBlockName.put("enchantmenttable","116");
 		KrimBlockName.put("endportal","119");
 		KrimBlockName.put("endportalframe","120");
 		KrimBlockName.put("whitestone","121");
 		KrimBlockName.put("endstone","121:1");
 		KrimBlockName.put("dragonegg","122");
 		KrimBlockName.put("redstonelamp_off","123");
 		KrimBlockName.put("redstonelamp","124");
 		KrimBlockName.put("Oakwoodslab","126");
 		KrimBlockName.put("Sprucewoodslab","126:1");
 		KrimBlockName.put("Birchwoodslab","126:2");
 		KrimBlockName.put("Junglewoodslab","126:3");
 		KrimBlockName.put("Sandstonestairs","128");
 		KrimBlockName.put("Emeraldore","129");
 		KrimBlockName.put("Tripwirehook","131");
 		KrimBlockName.put("emeraldblock","133");
 		KrimBlockName.put("Sprucewoodstairs","134");
 		KrimBlockName.put("Birchwoodstairs","135");
 		KrimBlockName.put("Junglewoodstairs","136");
 		KrimBlockName.put("Beacon","138");
 		KrimBlockName.put("Cobblestonewall","139");
 		KrimBlockName.put("Mossycobblestonewall","139:1");
 		KrimBlockName.put("Woodenbutton","143");
 		KrimBlockName.put("Anvil","145");
 		KrimBlockName.put("Anvil(Damaged)","145:1");
 		KrimBlockName.put("Anvil(VeryDamaged)","145:2");
 		KrimBlockName.put("TrappedChest","146");
 		KrimBlockName.put("WeightedPressurePlate(Light)","147");
 		KrimBlockName.put("WeightedPressurePlate(Heavy)","148");
 		KrimBlockName.put("RedstoneComparator(Dis)","149");
 		KrimBlockName.put("RedstoneComparator(Act)","150");
 		KrimBlockName.put("DaylightSensor","151");
 		KrimBlockName.put("BlockofRedstone","152");
 		KrimBlockName.put("Redstoneblock","152");
 		KrimBlockName.put("Netherquartzore","153");
 		KrimBlockName.put("Hopper","154");
 		KrimBlockName.put("BlockofQuartz","155");
 		KrimBlockName.put("Quartzstairs","156");
 		KrimBlockName.put("ActivatorRail","157");
 		KrimBlockName.put("Dropper","158");
 		
 		
 		KrimBlockName.put("ironpickaxe","257");
 		KrimBlockName.put("ironaxe","258");
 		KrimBlockName.put("flintandsteel","259");
 		KrimBlockName.put("apple","260");
 		KrimBlockName.put("bow","261");
 		KrimBlockName.put("arrow","262");
 		KrimBlockName.put("coal","263");
 		KrimBlockName.put("charcoal","263:1");
 		KrimBlockName.put("diamond","264");
 		KrimBlockName.put("ironingot","265");
 		KrimBlockName.put("goldingot","266");
 		KrimBlockName.put("ironsword","267");
 		KrimBlockName.put("woodensword","268");
 		KrimBlockName.put("woodenshovel","269");
 		KrimBlockName.put("woodenpickaxe","270");
 		KrimBlockName.put("woodenaxe","271");
 		KrimBlockName.put("stonesword","272");
 		KrimBlockName.put("stoneshovel","273");
 		KrimBlockName.put("stonepickaxe","274");
 		KrimBlockName.put("stoneaxe","275");
 		KrimBlockName.put("diamondsword","276");
 		KrimBlockName.put("diamondspade","277");
 		KrimBlockName.put("diamondpickaxe","278");
 		KrimBlockName.put("diamondaxe","279");
 		KrimBlockName.put("stick","280");
 		KrimBlockName.put("bowl","281");
 		KrimBlockName.put("mushroomsoup","282");
 		KrimBlockName.put("goldensword","283");
 		KrimBlockName.put("goldenspade","284");
 		KrimBlockName.put("goldenpickaxe","285");
 		KrimBlockName.put("goldenaxe","286");
 		KrimBlockName.put("string","287");
 		KrimBlockName.put("feather","288");
 		KrimBlockName.put("gunpowder","289");
 		KrimBlockName.put("woodenhoe","290");
 		KrimBlockName.put("stonehoe","291");
 		KrimBlockName.put("ironhoe","292");
 		KrimBlockName.put("diamondhoe","293");
 		KrimBlockName.put("goldenmhoe","294");
 		KrimBlockName.put("seeds","295");
 		KrimBlockName.put("wheat","296");
 		KrimBlockName.put("bread","297");
 		KrimBlockName.put("leathercap","298");
 		KrimBlockName.put("leathertunic","299");
 		KrimBlockName.put("leathertrousers","300");
 		KrimBlockName.put("leatherboots","301");
 		KrimBlockName.put("chainmailhelmet","302");
 		KrimBlockName.put("chainchestplate","303");
 		KrimBlockName.put("chainmailleggings","304");
 		KrimBlockName.put("chainmailboots","305");
 		KrimBlockName.put("ironhelmet","306");
 		KrimBlockName.put("ironchestplate","307");
 		KrimBlockName.put("ironleggings","308");
 		KrimBlockName.put("ironboots","309");
 		KrimBlockName.put("diamondhelmet","310");
 		KrimBlockName.put("diamondchestplate","311");
 		KrimBlockName.put("diamondleggings","312");
 		KrimBlockName.put("industrialdiamond","313");
 		KrimBlockName.put("goldenhelmet","314");
 		KrimBlockName.put("goldenchestplate","315");
 		KrimBlockName.put("goldenleggings","316");
 		KrimBlockName.put("goldenboots","317");
 		KrimBlockName.put("flint","318");
 		KrimBlockName.put("rawporkchop","319");
 		KrimBlockName.put("cookedporkchop","320");
 		KrimBlockName.put("painting","321");
 		KrimBlockName.put("goldenapple","322");
 		KrimBlockName.put("sign","323");
 		KrimBlockName.put("woodendoor","324");
 		KrimBlockName.put("bucket","325");
 		KrimBlockName.put("bucketofwater","326");
 		KrimBlockName.put("bucketoflava","327");
 		KrimBlockName.put("minecart","328");
 		KrimBlockName.put("saddle","329");
 		KrimBlockName.put("irondoor","330");
 		KrimBlockName.put("redstone","331");
 		KrimBlockName.put("snowball","332");
 		KrimBlockName.put("boat","333");
 		KrimBlockName.put("leather","334");
 		KrimBlockName.put("bucketofmilk","335");
 		KrimBlockName.put("brick","336");
 		KrimBlockName.put("clay","337");
 		KrimBlockName.put("sugarcanes","338");
 		KrimBlockName.put("paper","339");
 		KrimBlockName.put("book","340");
 		KrimBlockName.put("slimeball","341");
 		KrimBlockName.put("chestcart","342");
 		KrimBlockName.put("furnacecart","343");
 		KrimBlockName.put("egg","344");
 		KrimBlockName.put("compass","345");
 		KrimBlockName.put("fishingrod","346");
 		KrimBlockName.put("watch","347");
 		KrimBlockName.put("glowstonedust","348");
 		KrimBlockName.put("rawfish","349");
 		KrimBlockName.put("cookedfish","350");
 		KrimBlockName.put("inksac","351");
 		KrimBlockName.put("rosered","351:1");
 		KrimBlockName.put("cactusgreen","351:2");
 		KrimBlockName.put("cocoabeans","351:3");
 		KrimBlockName.put("lapislazuli","351:4");
 		KrimBlockName.put("purpledye","351:5");
 		KrimBlockName.put("cyandye","351:6");
 		KrimBlockName.put("lightgreydye","351:7");
 		KrimBlockName.put("greydye","351:8");
 		KrimBlockName.put("pinkdye","351:9");
 		KrimBlockName.put("limegreendye","351:10");
 		KrimBlockName.put("dandelionyellow","351:11");
 		KrimBlockName.put("lightbluedye","351:12");
 		KrimBlockName.put("magentadye","351:13");
 		KrimBlockName.put("orangedye","351:14");
 		KrimBlockName.put("bonemeal","351:15");
 		KrimBlockName.put("bone","352");
 		KrimBlockName.put("sugar","353");
 		KrimBlockName.put("cake","354");
 		KrimBlockName.put("bed","355");
 		KrimBlockName.put("redstonerepeater","356");
 		KrimBlockName.put("cookie","357");
 		KrimBlockName.put("map","358");
 		KrimBlockName.put("shears","359");
 		KrimBlockName.put("melon","360");
 		KrimBlockName.put("pumpkinseeds","361");
 		KrimBlockName.put("melonseeds","362");
 		KrimBlockName.put("rawbeef","363");
 		KrimBlockName.put("steak","364");
 		KrimBlockName.put("rawchicken","365");
 		KrimBlockName.put("cookedchicken","366");
 		KrimBlockName.put("rottenflesh","367");
 		KrimBlockName.put("ender pearl","368");
 		KrimBlockName.put("blazerod","369");
 		KrimBlockName.put("ghasttear","370");
 		KrimBlockName.put("goldennugget","371");
 		KrimBlockName.put("netherwart","372");
 		KrimBlockName.put("waterbottle","373");
 		KrimBlockName.put("awkwardpotion","373:16");
 		KrimBlockName.put("thickpotion","373:32");
 		KrimBlockName.put("mundanepotion","373:64");
 		KrimBlockName.put("Regeneration Potion (0:45)","373:8193");	
 		KrimBlockName.put("Swiftness Potion (3:00)","373:8194");	
 		KrimBlockName.put("Fire Resistance Potion (3:00)","373:8195");	
 		KrimBlockName.put("Poison Potion (0:45)","373:8196");	
 		KrimBlockName.put("Healing Potion","373:8197");	
 		KrimBlockName.put("Weakness Potion (1:30)","373:8200");	
 		KrimBlockName.put("Strength Potion (3:00)","373:8201");	
 		KrimBlockName.put("Slowness Potion (1:30)","373:8202");	
 		KrimBlockName.put("Harming Potion","373:8204");	
 		KrimBlockName.put("Regeneration Potion II (0:22)","373:8225");	
 		KrimBlockName.put("Swiftness Potion II (1:30)","373:8226");	
 		KrimBlockName.put("Poison Potion II (0:22)","373:8228");	
 		KrimBlockName.put("Healing Potion II","373:8229");	
 		KrimBlockName.put("Strength Potion II (1:30)","373:8233");	
 		KrimBlockName.put("Harming Potion II","373:8236");	
 		KrimBlockName.put("Regeneration Potion (2:00)","373:8257");	
 		KrimBlockName.put("Swiftness Potion (8:00)","373:8258");	
 		KrimBlockName.put("Fire Resistance Potion (8:00)","373:8259");	
 		KrimBlockName.put("Poison Potion (2:00)","373:8260");	
 		KrimBlockName.put("Weakness Potion (4:00)","373:8264");	
 		KrimBlockName.put("Strength Potion (8:00)","373:8265");	
 		KrimBlockName.put("Slowness Potion (4:00)","373:8266");	
 		KrimBlockName.put("Fire Resistance Splash (2:15)","373:16378");	
 		KrimBlockName.put("Regeneration Splash (0:33)","373:16385");	
 		KrimBlockName.put("Swiftness Splash (2:15)","373:16386");	
 		KrimBlockName.put("Poison Splash (0:33)","373:16388");	
 		KrimBlockName.put("Healing Splash","373:16389");	
 		KrimBlockName.put("Weakness Splash (1:07)","373:16392");	
 		KrimBlockName.put("Strength Splash (2:15)","373:16393");	
 		KrimBlockName.put("Slowness Splash (1:07)","373:16394");	
 		KrimBlockName.put("Harming Splash","373:16396");	
 		KrimBlockName.put("Swiftness Splash II (1:07)","373:16418");	
 		KrimBlockName.put("Poison Splash II (0:16)","373:16420");	
 		KrimBlockName.put("Healing Splash II","373:16421");	
 		KrimBlockName.put("Strength Splash II (1:07)","373:16425");	
 		KrimBlockName.put("Harming Splash II","373:16428");	
 		KrimBlockName.put("Regeneration Splash (1:30)","373:16449");	
 		KrimBlockName.put("Swiftness Splash (6:00)","373:16450");	
 		KrimBlockName.put("Fire Resistance Splash (6:00)","373:16451");	
 		KrimBlockName.put("Poison Splash (1:30)","373:16452");	
 		KrimBlockName.put("Weakness Splash (3:00)","373:16456");	
 		KrimBlockName.put("Strength Splash (6:00)","373:16457");	
 		KrimBlockName.put("Slowness Splash (3:00)","373:16458");	
 		KrimBlockName.put("Regeneration Splash II (0:16)","373:16471");
 		KrimBlockName.put("glassbottle","374");
 		KrimBlockName.put("spidereye","375");
 		KrimBlockName.put("fermentedspidereye","376");
 		KrimBlockName.put("blazepowder","377");
 		KrimBlockName.put("magmacream","378");
 		KrimBlockName.put("brewingstand","379");
 		KrimBlockName.put("cauldron","380");
 		KrimBlockName.put("eyeofender","381");
 		KrimBlockName.put("glisteringmelon","382");
 		KrimBlockName.put("spawncreeper","383:50");
 		KrimBlockName.put("spawnskeleton","383:51");
 		KrimBlockName.put("spawnspider","383:52");
 		KrimBlockName.put("spawnzombie","383:54");
 		KrimBlockName.put("spawnslime","383:55");
 		KrimBlockName.put("spawnghast","383:56");
 		KrimBlockName.put("spawnpigzombie","383:57");
 		KrimBlockName.put("spawnenderman","383:58");
 		KrimBlockName.put("spawncavespider","383:59");
 		KrimBlockName.put("spawnsilverfish","383:60");
 		KrimBlockName.put("spawnblaze","383:61");
 		KrimBlockName.put("spawnmagmacube","383:62");
 		KrimBlockName.put("spawnpig","383:90");
 		KrimBlockName.put("spawnsheep","383:91");
 		KrimBlockName.put("spawncow","383:92");
 		KrimBlockName.put("spawnchicken","383:93");
 		KrimBlockName.put("spawnsquid","383:94");
 		KrimBlockName.put("spawnwolf","383:95");
 		KrimBlockName.put("spawnmooshroom","383:96");
 		KrimBlockName.put("spawnsnowgolem","383:97");
 		KrimBlockName.put("spawnocelot","383:98");
 		KrimBlockName.put("spawnirongolem","383:99");
 		KrimBlockName.put("spawnvillager","383:120");
 		KrimBlockName.put("bottleo´enchanting","384");
 		KrimBlockName.put("firecharge","385");
 		KrimBlockName.put("bookandquill","386");
 		KrimBlockName.put("writtenbook","387");
 		KrimBlockName.put("Emerald","388");
 		KrimBlockName.put("Itemframe","389");
 		KrimBlockName.put("Flowerpot","390");
 		KrimBlockName.put("Carrot","391");
 		KrimBlockName.put("Potatoe","392");
 		KrimBlockName.put("Bakedpotatoe","393");
 		KrimBlockName.put("Poisonedpotatoe","394");
 		KrimBlockName.put("Emptymap","395");
 		KrimBlockName.put("Goldencarrot","396");
 		KrimBlockName.put("SkelletonSkull","397");
 		KrimBlockName.put("Witherhead","397:1");
 		KrimBlockName.put("Zombiehead","397:2");
 		KrimBlockName.put("Head","397:3");
 		KrimBlockName.put("Creeperhead","397:4");
 		KrimBlockName.put("Carrotonastick","398");
 		KrimBlockName.put("Netherstar","399");
 		KrimBlockName.put("Pumpkinpie","400");
 		KrimBlockName.put("FireRocket","401");
 		KrimBlockName.put("Firestar","402");
 		KrimBlockName.put("Enchantedbook","403");
 		KrimBlockName.put("RedstoneComparator","404");
 		KrimBlockName.put("Netherbrick","405");
 		KrimBlockName.put("Netherquartz","406");
 		KrimBlockName.put("TNTCart","407");
 		KrimBlockName.put("HopperCart","408");
 		
 		KrimBlockName.put("Disk C418-13","2256");
 		KrimBlockName.put("Disk C418-Cat","2257");
 		KrimBlockName.put("Disk C418-Blocks","2258");
 		KrimBlockName.put("Disk C418-Chirp","2259");
 		KrimBlockName.put("Disk C418-Far","2260");
 		KrimBlockName.put("Disk C418-Mall","2261");
 		KrimBlockName.put("Disk C418-Mellohi","2262");
 		KrimBlockName.put("Disk C418-Stall","2263");
 		KrimBlockName.put("Disk C418-Strad","2264");
 		KrimBlockName.put("Disk C418-Ward","2265");
 		KrimBlockName.put("Disk 11","2266");
 		KrimBlockName.put("Disk C418-Wait","2267");
 	}
 	
 	public static void put(String a,String b)
 	{
 		names.put(a.toLowerCase().trim().replace(" ", "_"), b);
 	}
 	
 	public static void loadNames(Map<String,String> m)
 	{
 		for(Entry<?, ?> i : m.entrySet())
 		{
 			KrimBlockName.names.put(i.getKey().toString().toLowerCase().trim().replace(" ","_"),(String)i.getValue());
 		}
 	}
 	
 	public static String searchName(String name)
 	{
 		name = name.toLowerCase();
 		for (Map.Entry<String,String> entry : KrimBlockName.names.entrySet())
     	{
 			if(entry.getKey().toLowerCase().contains(name))
 				return entry.getKey();
     	}
 		
 		return "null";
 	}
 	
 	public static String getIdByName(String name)
 	{
 		name = name.trim().toLowerCase();
 		if(KrimBlockName.names.get(name) != null)
 			return KrimBlockName.names.get(name);
 		else
 			return "-1";
 	}
 	
 	public static String getNameById(int type, int typeid)
 	{
 		String strg = "";
 		strg += type;
 		if(typeid > 0)
 			strg += ":"+typeid;
 		
 		return getNameById(strg);
 	}
 	public static String getNameById(String id)
 	{
 		for (Map.Entry<String,String> entry : KrimBlockName.names.entrySet())
     	{
 			if(entry.getValue().trim().equalsIgnoreCase(id.trim()))
 				return entry.getKey();
     	}
 		
 		//Suche Master Block
 		String[] subidTmp = id.split(":");
 		String nid = "";
 		if(subidTmp.length > 1)
 		{
 			nid = subidTmp[0];
 			for (Map.Entry<String,String> entry : KrimBlockName.names.entrySet())
 	    	{
 				if(entry.getValue().trim().equalsIgnoreCase(nid.trim()))
 					return entry.getKey();
 	    	}
 		}
 		return ""+id;
 	}
 	
 	public static String getNameByItemStack(ItemStack i)
 	{
 		String str = getIdByItemStack(i);
 		return getNameById(str);
 	}
 	
 	public static String getIdByItemStack(ItemStack i)
 	{
 		String str = "";
 		str += i.getTypeId();
 		if(i.getDurability() > 0 && i.getType().getMaxDurability() == 0)
 			str += ":"+i.getDurability();
 		return str;
 	}
 	
 	public static ItemStack parseName(String nam)
 	{
 		ItemStack i = new ItemStack(Material.AIR);
 		int n = 0;
 		try
 		{
 			n = Integer.parseInt(nam);
 			i.setTypeId(n);
 		} catch(NumberFormatException e)
 		{
 			
 		}
 		
 		if(n == 0)
 		{
 			try
 			{
 				if(nam.split(":").length > 1)
 				{
 					String[] sp = nam.split(":");
 					//Block mit ID
 					i.setTypeId(Integer.parseInt(sp[0]));
 					i.setDurability((short) Integer.parseInt(sp[1]));
 				} else
 				{
 					nam = KrimBlockName.getIdByName(nam);
 					if(nam.split(":").length > 1)
 					{
 						String[] sp = nam.split(":");
 						//Block mit ID
 						i.setTypeId(Integer.parseInt(sp[0]));
 						i.setDurability((short) Integer.parseInt(sp[1]));
 					} else
 					{
 						i.setTypeId(Integer.parseInt(nam));
 					}
 				}
 			} catch(Exception e)
 			{
 				return null;
 			}
 		}
 		return i;
 	}
 
 	public static String getIdByBlock(Block i) 
 	{
 		String str = "";
 		str += i.getTypeId();
 		if(i.getData() > 0 && i.getType().getMaxDurability() == 0)
 			str += ":"+i.getData();
 		return str;
 	}
 	
 	public static ItemStack getStackByBlock(Block i)
 	{
 		ItemStack s = new ItemStack(Material.AIR);
 		s.setType(i.getType());
 		if(i.getData() > 0 && i.getType().getMaxDurability() == 0)
 			s.setDurability(i.getData());
 		return s;
 	}
 }
