 package com.omnidex.move;
 
 import com.omnidex.damage.BasePower;
 import com.omnidex.move.Category;
 import com.omnidex.pokemon.Pokemon;
 import com.omnidex.type.Type;
 
 public enum Move {
 	
 	NONE("(none)", Type.NO_TYPE, 0,0,0,0, Category.STATUS, Target.ANY_ADJACENT),
 	UNKNOWN("(unknown)",Type.NO_TYPE, 0,0,0,0, Category.STATUS, Target.ANY_ADJACENT),
 
 	ABSORB("Absorb", Type.GRASS, 20,100,25,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	ACID("Acid",Type.POISON, 40,100,30,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	ACID_ARMOR("Acid Armor",Type.POISON, 0,-1,40,0, Category.STATUS, Target.SELF),
 	ACID_SPRAY("Acid Spray",Type.POISON, 40,100,20,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	ACROBATICS("Acrobatics",Type.FLYING, 55,100,15,0, Category.PHYSICAL, Target.ANY),
 	ACUPRESSURE("Acupressure",Type.NORMAL, 0,-1,30,0, Category.STATUS, Target.SELF),
 	AERIAL_ACE("Aerial Ace",Type.FLYING, 60,-1,20,0, Category.PHYSICAL, Target.ANY),
 	AEROBLAST("Aeroblast",Type.FLYING, 100,95,5,0, Category.SPECIAL, Target.ANY),
 	AFTER_YOU("After You",Type.NORMAL, 0,-1,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	AGILITY("Agility", Type.PSYCHIC,0,-1,30,0, Category.STATUS, Target.SELF),
 	AIR_CUTTER("Air Cutter", Type.FLYING, 55,95,25,0,Category.SPECIAL, Target.ANY),
 	AIR_SLASH("Air Slash", Type.FLYING, 75,95,20,0,Category.SPECIAL, Target.ANY),
 	ALLY_SWITCH("Ally Switch", Type.PSYCHIC, 0,-1,15,1,Category.STATUS,Target.SELF),
 	AMNESIA("Amnesia", Type.PSYCHIC, 0,-1,20,0,Category.STATUS,Target.SELF),
 	ANCIENTPOWER("AncientPower", Type.ROCK, 60,100,5,0,Category.SPECIAL, Target.ANY_ADJACENT),
 	AQUA_JET("Aqua Jet", Type.WATER, 40,100,20,1,Category.PHYSICAL, Target.ANY_ADJACENT),
 	AQUA_RING("Aqua Ring", Type.WATER, 0,-1,20,0,Category.STATUS,Target.SELF),
 	AQUA_TAIL("Aqua Tail", Type.WATER, 90,90,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ARM_THRUST("Arm Thrust", Type.FIGHTING, 15,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	AROMATHERAPY("Aromatherapy", Type.GRASS, 0,-1,5,0,Category.STATUS,Target.ALL_ALLIES),
 	ASSIST("Assist", Type.NORMAL, 0,-1,20,0, Category.STATUS,Target.SELF), 
 	ASSURANCE("Assurance", Type.DARK, 50,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	ASTONISH("Astonish", Type.GHOST, 30,100,15,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	ATTACK_ORDER("Attack Order", Type.BUG, 90,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	ATTRACT("Attract", Type.NORMAL, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	AURA_SPHERE("Aura Sphere", Type.FIGHTING, 90,-1,20,0, Category.SPECIAL,Target.ANY),
 	AURORA_BEAM("Aurora beam", Type.ICE, 65,100,20,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	AUTOTOMIZE("Autotomize", Type.STEEL, 0,-1,15,0, Category.STATUS,Target.SELF),
 	AVALANCHE("Avalanche", Type.ICE, 60,100,10,-4, Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	BARRAGE("Barrage", Type.NORMAL, 15,85,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	BARRIER("Barrier",Type.PSYCHIC, 0,-1,30,0, Category.STATUS, Target.SELF),
 	BATON_PASS("Baton Pass",Type.NORMAL, 0,-1,40,0, Category.STATUS, Target.SELF),
 	BEAT_UP("Beat Up", Type.DARK, 1,100,10,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	BELLY_DRUM("Belly Drum", Type.NORMAL, 0,-1,10,0,Category.STATUS, Target.SELF),
 	BESTOW("Bestow", Type.NORMAL, 0,-1,15,0, Category.STATUS, Target.ANY_ADJACENT),
 	BIDE("Bide", Type.NORMAL, 1,-1,10,1, Category.PHYSICAL, Target.SELF),
 	BIND("Bind",Type.NORMAL, 15,85,20,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	BITE("Bite", Type.DARK, 60,100,25,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	BLAST_BURN("Blast Burn", Type.FIRE, 150,90,5,0,Category.SPECIAL, Target.ANY_ADJACENT),
 	BLAZE_KICK("Blaze Kick", Type.FIRE, 85,90,10,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	BLIZZARD("Blizzard", Type.ICE, 120,70,5,0,Category.SPECIAL, Target.ALL_ADJACENT_FOES),
 	BLOCK("Block", Type.NORMAL, 0,-1,5,0, Category.STATUS, Target.ANY_ADJACENT),
 	BLUE_FLARE("Blue Flare", Type.FIRE, 130,85,5,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	BODY_SLAM("Body Slam", Type.NORMAL, 85,100,15,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	BOLT_STRIKE("Bolt Strike", Type.ELECTRIC, 130,85,5,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	BONE_CLUB("Bone Club", Type.GROUND, 65,85,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	BONE_RUSH("Bone Rush", Type.GROUND, 25,90,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	BONEMERANG("Bonemerang", Type.GROUND, 50,90,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	BOUNCE("Bounce",Type.FLYING, 85,85,5,0, Category.PHYSICAL,Target.ANY),
 	BRAVE_BIRD("Brave Bird",Type.FLYING, 120,100,15,0, Category.PHYSICAL, Target.ANY),
 	BRICK_BREAK("Brick Break",Type.FIGHTING, 75,100,15,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	BRINE("Brine", Type.WATER,65,100,10,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	BUBBLE("Bubble", Type.WATER, 20,100,30,0,Category.SPECIAL, Target.ALL_ADJACENT_FOES),
 	BUBBLEBEAM("BubbleBeam", Type.WATER, 65,100,20,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	BUG_BITE("Bug Bite", Type.BUG, 60,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	BUG_BUZZ("Bug Buzz", Type.BUG, 90,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	BULK_UP("Bulk Up", Type.FIGHTING, 0,-1,20,0,Category.STATUS,Target.SELF),
 	BULLDOZE("Bulldoze", Type.GROUND, 60,100,20,0,Category.PHYSICAL,Target.ALL_ADJACENT),
 	BULLET_PUNCH("Bullet Punch", Type.STEEL, 40,100,30,1,Category.PHYSICAL,Target.ANY_ADJACENT),
 	BULLET_SEED("Bullet Seed", Type.GRASS, 25,100,30,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	CALM_MIND("Calm Mind", Type.PSYCHIC, 0,-1,20,0, Category.STATUS, Target.SELF),
 	CAMOUFLAGE("Camouflage", Type.NORMAL, 0,-1,20,0, Category.STATUS, Target.SELF),
 	CAPTIVATE("Captivate", Type.NORMAL, 0,100,20,0, Category.STATUS, Target.ALL_ADJACENT_FOES),
 	CHARGE("Charge",Type.ELECTRIC, 0,-1,20,0, Category.STATUS,Target.SELF),
 	CHARGE_BEAM("Charge Beam",Type.ELECTRIC, 50,90,10,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	CHARM("Charm", Type.NORMAL,0,100,20,0, Category.STATUS, Target.ANY_ADJACENT),
 	CHATTER("Chatter", Type.FLYING, 60,100,20,0,Category.SPECIAL, Target.ANY),
 	CHIP_AWAY("Chip Away", Type.NORMAL, 70,100,20,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CIRCLE_THROW("Circle Throw", Type.FIGHTING, 60,90,10,-6,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CLAMP("Clamp", Type.WATER, 35,85,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	CLEAR_SMOG("Clear Smog", Type.POISON, 50,-1,15,0,Category.SPECIAL, Target.ANY_ADJACENT),
 	CLOSE_COMBAT("Close Combat", Type.FIGHTING, 120,100,5,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	COIL("Coil", Type.POISON, 0, -1,20,0,Category.STATUS, Target.SELF),
 	COMET_PUNCH("Comet Punch", Type.NORMAL, 18,85,15,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	CONFUSE_RAY("Confuse Ray", Type.GHOST, 0,100,10,0, Category.STATUS, Target.ANY_ADJACENT),
 	CONFUSION("Confusion", Type.PSYCHIC, 90,100,25,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	CONSTRICT("Constrict", Type.NORMAL, 10,100,35,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	CONVERSION("Conversion", Type.NORMAL, 0,-1,30,0, Category.STATUS, Target.SELF),
 	CONVERSION_2("Conversion 2", Type.NORMAL, 0,-1,30,0, Category.STATUS, Target.ANY_ADJACENT),
 	COPYCAT("Copycat", Type.NORMAL, 0,-1,20,0, Category.STATUS, Target.SELF),
 	COSMIC_POWER("Cosmic Power", Type.PSYCHIC, 0,-1,20,0,Category.STATUS, Target.SELF),
 	COTTON_GUARD("Cotton Guard",Type.GRASS, 0,-1,10,0, Category.STATUS, Target.SELF),
 	COTTON_SPORE("Cotton Spore", Type.GRASS, 0,100,40,0, Category.STATUS, Target.ANY_ADJACENT),
 	COUNTER("Counter",Type.FIGHTING, 1,100,20,-5, Category.PHYSICAL, Target.SELF),
 	COVET("Covet", Type.NORMAL,60,100,40,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 	CRABHAMMER("Crabhammer", Type.WATER, 90,90,10,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CROSS_CHOP("Cross Chop", Type.FIGHTING, 100,80,5,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CROSS_POISON("Cross Poison", Type.POISON, 70,100,20,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CRUNCH("Crunch", Type.DARK, 80,100,15,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CRUSH_CLAW("Crush Claw", Type.NORMAL, 75,95,10,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CRUSH_GRIP("Crush Grip", Type.NORMAL, 1,100,5,0,Category.PHYSICAL, Target.ANY_ADJACENT),
 	CURSE("Curse", Type.GHOST, 0,-1,10,0, Category.STATUS, Target.SELF),
 	CUT("Cut", Type.NORMAL, 50,95,30,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 
 	DARK_PULSE("Dark Pulse", Type.DARK, 80,100,15,0, Category.SPECIAL, Target.ANY_ADJACENT),
 	DARK_VOID("Dark Void", Type.DARK, 0,80,10,0, Category.STATUS, Target.ALL_ADJACENT_FOES),
 	DEFEND_ORDER("Defend Order", Type.BUG, 0,-1,10,0, Category.STATUS, Target.SELF),
     DEFENSE_CURL("Defense Curl", Type.NORMAL, 0,-1,40,0, Category.STATUS,Target.SELF),
     DEFOG("Defog",Type.FLYING, 0,-1,15,0, Category.STATUS, Target.ANY_ADJACENT),
     DESTINY_BOND("Destiny Bond",Type.GHOST, 0,-1,5,0, Category.STATUS, Target.SELF),
     DETECT("Detect", Type.FIGHTING, 0,-1,5,4,Category.STATUS, Target.SELF),
     DIG("Dig", Type.GROUND, 80,100,10,0, Category.PHYSICAL, Target.ANY_ADJACENT),
     DISABLE("Disable", Type.NORMAL, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
     DISCHARGE("Discharge",Type.ELECTRIC, 80,100,15,0, Category.SPECIAL,Target.ALL_ADJACENT),
     DIVE("Dive", Type.WATER, 80,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DIZZY_PUNCH("Dizzy Punch", Type.NORMAL, 70,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DOOM_DESIRE("Doom Desire", Type.STEEL, 140,100,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
     DOUBLE_HIT("Double Hit", Type.NORMAL, 35,90,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DOUBLE_KICK("Double Kick", Type.FIGHTING, 30,100,30,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DOUBLE_TEAM("Double Team", Type.NORMAL, 0,-1,15,0,Category.STATUS,Target.SELF),
     DOUBLE_EDGE("Double-Edge", Type.NORMAL, 120,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DOUBLESLAP("DoubleSlap", Type.NORMAL, 15,85,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DRACO_METEOR("Draco Meteor", Type.DRAGON, 140,90,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
     DRAGON_CLAW("Dragon Claw", Type.DRAGON, 80,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DRAGON_DANCE("Dragon Dance", Type.DRAGON, 0,-1,20,0,Category.STATUS,Target.SELF),
     DRAGON_PULSE("Dragon Pulse", Type.DRAGON, 90,100,10,0,Category.SPECIAL,Target.ANY),
     DRAGON_RAGE("Dragon Rage", Type.DRAGON, 1,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
     DRAGON_RUSH("Dragon Rush", Type.DRAGON, 100,75,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DRAGON_TAIL("Dragon Tail", Type.DRAGON, 60,90,10,-6,Category.PHYSICAL,Target.ANY_ADJACENT),
     DRAGONBREATH("DragonBreath", Type.DRAGON, 60,100,20,0,Category.SPECIAL,Target.ANY_ADJACENT),
     DRAIN_PUNCH("Drain Punch", Type.FIGHTING, 75,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DREAM_EATER("Dream Eater", Type.PSYCHIC, 100,100,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
     DRILL_PECK("Drill Peck", Type.FLYING, 80,100,20,0,Category.PHYSICAL,Target.ANY),
     DRILL_RUN("Drill Run", Type.GROUND, 80,95,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DUAL_CHOP("Dual Chop", Type.DRAGON, 40,90,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
     DYNAMICPUNCH("DynamicPunch", Type.FIGHTING,100,50,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	EARTH_POWER("Earth Power", Type.GROUND, 90,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	EARTHQUAKE("Earthquake", Type.GROUND, 100,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	ECHOED_VOICE("Echoed Voice", Type.NORMAL, 40,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	EGG_BOMB("Egg Bomb", Type.NORMAL, 100,75,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	ELECTRO_BALL("Electro Ball", Type.ELECTRIC, 1,100,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	ELECTROWEB("Electroweb", Type.ELECTRIC, 55,95,15,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	EMBARGO("Embargo", Type.DARK, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	EMBER("Ember",Type.FIRE, 40,100,25,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	ENCORE("Encore", Type.NORMAL, 0,100,5,0,Category.STATUS,Target.ANY_ADJACENT),
 	ENDEAVOR("Endeavor", Type.NORMAL, 1,100,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ENDURE("Endure", Type.NORMAL, 0,-1,10,3,Category.STATUS,Target.SELF),
 	ENERGY_BALL("Energy Ball", Type.GRASS, 80,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	ENTRAINMENT("Entrainment", Type.NORMAL, 0,100,15,0,Category.STATUS, Target.ANY_ADJACENT),
 	ERUPTION("Eruption", Type.FIRE, 1,100,5,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	EXPLOSION("Explosion", Type.NORMAL, 250,100,5,0,Category.PHYSICAL,Target.ALL_ADJACENT),
 	EXTRASENSORY("Extrasensory", Type.PSYCHIC, 80,100,30,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	EXTREMESPEED("ExtremeSpeed", Type.NORMAL, 80,100,5,2,Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	FACADE("Facade", Type.NORMAL, 70,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FAINT_ATTACK("Faint Attack", Type.DARK, 60,-1,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FAKE_OUT("Fake Out", Type.NORMAL, 40,100,30,1, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FAKE_TEARS("Fake Tears", Type.DARK, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	FALSE_SWIPE("False Swipe", Type.NORMAL, 40,100,40,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FEATHERDANCE("FeatherDance", Type.NORMAL, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	FEINT("Feint",Type.NORMAL, 30,100,10,2, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FIERY_DANCE("Fiery Dance",Type.FIRE, 80,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	FINAL_GAMBIT("Final Gambit",Type.FIGHTING, 1,100,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	FIRE_BLAST("Fire Blast",Type.FIRE, 120,85,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	FIRE_FANG("Fire Fang",Type.FIRE, 65,95,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FIRE_PLEDGE("Fire Pledge",Type.FIRE, 50,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	FIRE_PUNCH("Fire Punch",Type.FIRE, 75,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FIRE_SPIN("Fire Spin",Type.FIRE, 35,85,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	FISSURE("Fissure", Type.GROUND,1,1,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FLAIL("Flail", Type.NORMAL, 1,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FLAME_BURST("Flame Burst", Type.FIRE, 70,100,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FLAME_CHARGE("Flame Charge", Type.FIRE, 50,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FLAME_WHEEL("Flame Wheel", Type.FIRE, 60,100,25,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FLAMETHROWER("Flamethrower", Type.FIRE, 95,100,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FLARE_BLITZ("Flare Blitz", Type.FIRE, 120,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FLASH("Flash", Type.NORMAL, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	FLASH_CANNON("Flash Cannon", Type.STEEL, 80,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	FLATTER("Flatter", Type.DARK, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	FLING("Fling",Type.DARK, 1,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	FLY("Fly", Type.FLYING, 90,95,15,0,Category.PHYSICAL,Target.ANY),
 	FOCUS_BLAST("Focus Blast", Type.FIGHTING, 120,70,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FOCUS_ENERGY("Focus Energy", Type.NORMAL, 0,-1,30,0,Category.STATUS,Target.SELF),
 	FOCUS_PUNCH("Focus Punch", Type.FIGHTING, 150,100,20,-3,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FOLLOW_ME("Follow Me", Type.NORMAL, 0,-1,20,3,Category.STATUS,Target.SELF),
 	FORCE_PALM("Force Palm", Type.FIGHTING, 60,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FORESIGHT("Foresight", Type.NORMAL, 0,-1,40,0,Category.STATUS,Target.ANY_ADJACENT),
 	FOUL_PLAY("Foul Play", Type.DARK, 95,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FREEZE_SHOCK("Freeze_Shock", Type.ICE, 140,90,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FRENZY_PLANT("Frenzy Plant", Type.GRASS, 150,90,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FROST_BREATH("Frost Breath", Type.ICE, 40,90,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FRUSTRATION("Frustration", Type.NORMAL, 1,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FURY_ATTACK("Fury Attack", Type.NORMAL, 15,85,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FURY_CUTTER("Fury Cutter", Type.BUG, 20,95,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FURY_SWIPES("Fury Swipes", Type.NORMAL, 18,80,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FUSION_BOLT("Fusion Bolt", Type.ELECTRIC, 100,100,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	FUSION_FLARE("Fusion Flare", Type.FIRE, 100,100,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	FUTURE_SIGHT("Future Sight", Type.PSYCHIC, 100,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 
 	GASTRO_ACID("Gastro Acid", Type.POISON, 0,100,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	GEAR_GRIND("Gear Grind", Type.STEEL, 50,85,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	GIGA_DRAIN("Giga Drain", Type.GRASS, 75,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	GIGA_IMPACT("Giga Impact", Type.NORMAL, 150,90,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	GLACIATE("Glaciate", Type.ICE, 65,95,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	GLARE("Glare",Type.NORMAL, 0,90,30,0, Category.STATUS,Target.ANY_ADJACENT),
 	GRASS_KNOT("Grass Knot",Type.GRASS, 1, 100,20,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	GRASS_PLEDGE("Grass Pledge",Type.GRASS, 50,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	GRASSWHISTLE("GrassWhistle",Type.GRASS, 0,55,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	GRAVITY("Gravity", Type.PSYCHIC,0,-1,5,0, Category.STATUS,Target.ALL),
 	GROWL("Growl", Type.NORMAL, 0,100,40,0, Category.STATUS,Target.ALL_ADJACENT_FOES),
 	GROWTH("Growth", Type.NORMAL, 0,-1,40,0, Category.STATUS,Target.SELF),
 	GRUDGE("Grudge",Type.GHOST, 0,-1,5,0, Category.STATUS,Target.SELF),
 	GUARD_SPLIT("Guard Split",Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	GUARD_SWAP("Guard Swap",Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	GUILLOTINE("Guillotine",Type.NORMAL, 1,1,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	GUNK_SHOT("Gunk Shot",Type.POISON, 120,70,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	GUST("Gust", Type.FLYING, 40,100,35,0,Category.SPECIAL,Target.ANY), 
 	GYRO_BALL("Gyro Ball", Type.STEEL, 1,100,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	HAIL("Hail", Type.ICE, 0,-1,10,0, Category.STATUS,Target.ALL),
 	HAMMER_ARM("Hammer Arm",Type.FIGHTING, 100,90,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	HARDEN("Harden",Type.NORMAL, 0,-1,30,0, Category.STATUS,Target.SELF),
 	HAZE("Haze", Type.ICE, 0,-1,30,0,Category.STATUS,Target.ALL),
 	HEAD_CHARGE("Head Charge", Type.NORMAL, 120,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	HEAD_SMASH("Head Smash", Type.ROCK, 150,80,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	HEADBUTT("Headbutt", Type.NORMAL, 70,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	HEAL_BELL("Heal Bell", Type.NORMAL, 0,-1,5,0,Category.STATUS,Target.ALL_ALLIES),
 	HEAL_BLOCK("Heal Block", Type.PSYCHIC, 0,100,15,0,Category.STATUS,Target.ALL_FOES),
 	HEAL_ORDER("Heal Order", Type.BUG, 0,-1,10,0,Category.STATUS,Target.SELF),
 	HEAL_PULSE("Heal Pulse", Type.PSYCHIC, 0,-1,10,0,Category.STATUS,Target.ANY),
 	HEALING_WISH("Healing Wish", Type.PSYCHIC, 0,-1,10,0,Category.STATUS,Target.SELF),
 	HEART_STAMP("Heart Stamp", Type.PSYCHIC, 60,100,25,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	HEART_SWAP("Heart Swap", Type.PSYCHIC, 0,-1,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	HEAT_CRASH("Heat Crash", Type.FIRE, 1,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	HEAT_WAVE("Heat Wave", Type.FIRE, 100,90,10,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	HEAVY_SLAM("Heavy Slam", Type.STEEL, 1,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	HELPING_HAND("Helping Hand", Type.NORMAL, 0,-1,20,5,Category.STATUS,Target.ANY_ADJACENT),
 	HEX("Hex", Type.GHOST, 50,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HI_JUMP_KICK("Hi Jump Kick", Type.FIGHTING, 130,90,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_BUG("Hidden Power-Bug", Type.BUG, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_DARK("Hidden Power-Dark", Type.DARK, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_DRAGON("Hidden Power-Dragon", Type.DRAGON, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_ELECTRIC("Hidden Power-Electric", Type.ELECTRIC, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_FIGHTING("Hidden Power-Fighting", Type.FIGHTING, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_FIRE("Hidden Power-Fire", Type.FIRE, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_FLYING("Hidden Power-Flying", Type.FLYING, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_GHOST("Hidden Power-Ghost", Type.GHOST, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_GRASS("Hidden Power-Grass", Type.GRASS, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_GROUND("Hidden Power-Ground", Type.GROUND,1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_ICE("Hidden Power-Ice", Type.ICE, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_POISON("Hidden Power-Poison", Type.POISON,1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_PSYCHIC("Hidden Power-Psychic", Type.PSYCHIC, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_ROCK("Hidden Power-Rock", Type.ROCK, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_STEEL("Hidden Power-Steel", Type.STEEL, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HIDDEN_POWER_WATER("Hidden Power-Water", Type.WATER, 1,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HONE_CLAWS("Hone Claws", Type.DARK, 0,-1,15,0, Category.STATUS,Target.SELF),
 	HORN_ATTACK("Horn Attack", Type.NORMAL, 65,100,25,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	HORN_DRILL("Horn Drill", Type.NORMAL, 1,1,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	HORN_LEECH("Horn Leech", Type.GRASS, 75,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	HOWL("Howl",Type.NORMAL, 0,-1,40,0, Category.STATUS,Target.SELF),
 	HURRICANE("Hurricane",Type.FLYING, 120,70,10,0, Category.SPECIAL,Target.ANY),
 	HYDRO_CANNON("Hydro Cannon",Type.WATER, 150,90,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HYDRO_PUMP("Hydro Pump",Type.WATER, 120,80,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HYPER_BEAM("Hyper Beam",Type.NORMAL, 150,90,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	HYPER_FANG("Hyper Fang",Type.NORMAL, 80,90,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	HYPER_VOICE("Hyper Voice",Type.NORMAL, 90,100,10,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	HYPNOSIS("Hypnosis",Type.PSYCHIC, 0,60,20,0, Category.STATUS,Target.ANY_ADJACENT),
 
 	ICE_BALL("Ice Ball", Type.ICE, 30,90,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	ICE_BEAM("Ice Beam",Type.ICE, 95,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	ICE_BURN("Ice Burn", Type.ICE,140,90,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	ICE_FANG("Ice Fang", Type.ICE, 65,95,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ICE_PUNCH("Ice Punch", Type.ICE, 75,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ICE_SHARD("Ice Shard", Type.ICE, 40,100,30,1,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ICICLE_CRASH("Icicle Crash", Type.ICE, 85,90,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ICICLE_SPEAR("Icicle Spear", Type.ICE, 25,100,30,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ICY_WIND("Icy Wind", Type.ICE, 55,95,15,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	IMPRISON("Imprison", Type.PSYCHIC, 0,-1,10,0,Category.STATUS,Target.ALL_FOES),
 	INCINERATE("Incinerate", Type.FIRE, 30,100,15,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	INFERNO("Inferno", Type.FIRE, 100,50,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	
 	INGRAIN("Ingrain", Type.GRASS, 0,-1,20,0,Category.STATUS,Target.SELF),
 	IRON_DEFENSE("Iron Defense", Type.STEEL, 0,-1,15,0,Category.STATUS,Target.SELF),
 	IRON_HEAD("Iron Head", Type.STEEL, 80,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	IRON_TAIL("Iron Tail", Type.STEEL, 100,75,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	JUDGEMENT("Judgement", Type.NORMAL, 100,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	JUMP_KICK("Jump Kick", Type.FIGHTING, 100,95,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	KARATE_CHOP("Karate Chop", Type.FIGHTING, 50,100,25,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	KINESIS("Kinesis", Type.PSYCHIC, 0,80,15,0, Category.STATUS, Target.ANY_ADJACENT),
 	KNOCK_OFF("Knock Off", Type.DARK, 20,100,20,0, Category.PHYSICAL, Target.ANY_ADJACENT),
 
 	LAST_RESORT("Last Resort", Type.NORMAL, 140,100,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	LAVA_PLUME("Lava Plume", Type.FIRE, 80,100,15,0, Category.SPECIAL,Target.ALL_ADJACENT),
 	LEAF_BLADE("Leaf Blade", Type.GRASS, 90,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	LEAF_STORM("Leaf Storm", Type.GRASS, 140,90,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	LEAF_TORNADO("Leaf Tornado", Type.GRASS, 65,90,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	LEECH_LIFE("Leech life", Type.BUG, 20,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	LEECH_SEED("Leech Seed", Type.GRASS, 0,90,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	LEER("Leer",Type.NORMAL, 0,100,30,0, Category.STATUS,Target.ALL_ADJACENT_FOES),
 	LICK("Lick", Type.GHOST, 20,100,30,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	LIGHT_SCREEN("Light Screen", Type.PSYCHIC, 0,-1,30,0,Category.STATUS,Target.ALL_ALLIES),
 	LOCK_ON("Lock-On", Type.NORMAL, 0,-1,5,0,Category.STATUS,Target.ANY_ADJACENT),
 	LOVELY_KISS("Lovely Kiss", Type.NORMAL, 0,75,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	LOW_KICK("Low Kick", Type.FIGHTING, 1,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	LOW_SWEEP("Low Sweep", Type.FIGHTING, 60,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	LUCKY_CHANT("Lucky Chant", Type.NORMAL, 0,-1,30,0,Category.STATUS,Target.ALL_ALLIES),
 	LUNAR_DANCE("Lunar Dance", Type.PSYCHIC, 0,-1,10,0,Category.STATUS,Target.SELF),
 	LUSTER_PURGE("Luster Purge", Type.PSYCHIC, 70,100,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 
 	MACH_PUNCH("Mach Punch", Type.FIGHTING, 40,100,30,1, Category.PHYSICAL,Target.ANY_ADJACENT),
 	MAGIC_COAT("Magic Coat", Type.PSYCHIC, 0,-1,15,4, Category.STATUS,Target.SELF),
 	MAGIC_ROOM("Magic Room", Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.ALL),
 	MAGICAL_LEAF("Magical Leaf", Type.GRASS, 60,-1,20,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	MAGMA_STORM("Magma Storm", Type.FIRE, 120,75,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	MAGNET_BOMB("Magnet Bomb", Type.STEEL, 60,-1,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	MAGNET_RISE("Magnet Rise", Type.ELECTRIC, 0,-1,10,0, Category.STATUS,Target.SELF),
 	MAGNITUDE("Magnitude", Type.GROUND, 1,100,30,0, Category.PHYSICAL,Target.ALL_ADJACENT),
 	ME_FIRST("Me First", Type.NORMAL, 1,-1,20,0, Category.STATUS,Target.ANY_ADJACENT_FOE),
 	MEAN_LOOK("Mean Look", Type.NORMAL, 0,-1,5,0, Category.STATUS,Target.ANY_ADJACENT),
 	MEDIATE("Mediate",Type.PSYCHIC, 0,-1,40,0, Category.STATUS,Target.SELF),
 	MEGA_DRAIN("Mega Drain",Type.GRASS, 40,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	MEGA_KICK("Mega Kick",Type.NORMAL, 120,75,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	MEGA_PUNCH("Mega Punch",Type.NORMAL, 80,85,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	MEGAHORN("Megahorn", Type.BUG,120,85,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	MEMENTO("Memento", Type.DARK, 0,100,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	METAL_BURST("Metal Burst", Type.STEEL, 1,100,10,0,Category.PHYSICAL,Target.SELF),
 	METAL_CLAW("Metal Claw", Type.STEEL, 50,95,35,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	METAL_SOUND("Metal Sound", Type.STEEL, 0,85,40,0,Category.STATUS,Target.ANY_ADJACENT),
 	METEOR_MASH("Meteor Mash", Type.STEEL, 100,85,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	METRONOME("Metronome", Type.NORMAL, 0,-1,10,0,Category.STATUS,Target.SELF),
 	MILK_DRINK("Milk Drink", Type.NORMAL, 0,-1,10,0,Category.STATUS,Target.SELF),
 	MIMIC("Mimic", Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	MIND_READER("Mind Reader", Type.NORMAL, 0,-1,5,0, Category.STATUS,Target.ANY_ADJACENT),
 	MINIMIZE("Minimize", Type.NORMAL, 0,-1,20,0, Category.STATUS,Target.SELF),
 	MIRACLE_EYE("Miracle Eye", Type.PSYCHIC, 0,-1,40,0, Category.STATUS,Target.ANY_ADJACENT),
 	MIRROR_COAT("Mirror Coat", Type.PSYCHIC, 0,100,20,-5, Category.SPECIAL,Target.SELF),
 	MIRROR_MOVE("Mirror Move", Type.FLYING, 0,-1,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	MIRROR_SHOT("Mirror Shot", Type.STEEL, 65,85,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	MIST("Mist",Type.ICE, 0,-1,30,0, Category.STATUS,Target.ALL_ALLIES),
 	MIST_BALL("Mist Ball", Type.PSYCHIC,70,100,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	MOONLIGHT("Moonlight", Type.NORMAL, 0,-1,5,0,Category.STATUS,Target.SELF),
 	MORNING_SUN("Morning Sun", Type.NORMAL, 0,-1,5,0,Category.STATUS,Target.SELF),
 	MUD_BOMB("Mud Bomb", Type.GROUND, 65,85,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	MUD_SHOT("Mud Shot", Type.GROUND, 55,95,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	MUD_SPORT("Mud Sport", Type.GROUND, 0,-1,15,0,Category.STATUS,Target.ALL),
 	MUD_SLAP("Mud Slap", Type.GROUND, 20,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	MUDDY_WATER("Muddy Water", Type.WATER, 95,85,10,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 
 	NASTY_PLOT("Nasty Plot", Type.DARK, 0,-1,20,0, Category.STATUS, Target.SELF),
 	NATURAL_GIFT("Natural Gift", Type.NORMAL, 1,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	NATURE_POWER("Nature Power", Type.NORMAL, 1,-1,20,0, Category.STATUS,Target.SELF),
 	NEEDLE_ARM("Needle_Arm", Type.GRASS, 60,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	NIGHT_DAZE("Night Daze", Type.DARK, 85,95,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	NIGHT_SHADE("Night Shade", Type.GHOST, 1,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	NIGHT_SLASH("Night Slash", Type.DARK, 70,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	NIGHTMARE("Nightmare", Type.GHOST, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 
 	OCTAZOOKA("Octazooka", Type.WATER, 65,85,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	ODOR_SLEUTH("Odor Sleuth", Type.NORMAL, 0,-1,40,0, Category.STATUS,Target.ANY_ADJACENT),
 	OMINOUS_WIND("Ominous Wind", Type.GHOST, 60,100,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	OUTRAGE("Outrage", Type.DRAGON, 120, 100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT_FOE),
 	OVERHEAT("Overheat", Type.FIRE, 140,90,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 
 	PAIN_SPLIT("Pain Split", Type.NORMAL, 0,-1,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	PAY_DAY("Pay Day", Type.NORMAL, 40,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	PAYBACK("Payback",Type.DARK, 50,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	PECK("Peck", Type.FLYING, 35,100,35,0,Category.PHYSICAL,Target.ANY),
 	PERISH_SONG("Perish Song", Type.NORMAL, 0,-1,5,0,Category.STATUS,Target.ALL),
 	PETAL_DANCE("Petal Dance", Type.GRASS, 120,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT_FOE),
 	PIN_MISSLE("Pin Missle", Type.BUG, 14,85,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	PLUCK("Pluck", Type.FLYING, 60,100,20,0,Category.PHYSICAL,Target.ANY),
 	POISON_FANG("Poison Fang", Type.POISON, 50,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	POISON_GAS("Poison Gas", Type.POISON, 0,80,40,0,Category.STATUS,Target.ALL_ADJACENT_FOES),
 	POISON_JAB("Poison Jab", Type.POISON, 80,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	POISON_STING("Poison Sting", Type.POISON, 15,100,35,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	POISON_TAIL("Poison Tail", Type.POISON, 50,100,25,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	POISONPOWDER("PoisonPowder", Type.POISON, 0,75,35,0,Category.STATUS,Target.ANY_ADJACENT),
 	POUND("Pound", Type.NORMAL, 40,100,35,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	POWDER_SNOW("Powder Snow", Type.ICE, 40,100,25,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	POWER_GEM("Power Gem", Type.ROCK, 70,100,20,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	POWER_SWAP("Power Swap", Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	POWER_TRICK("Power Trick", Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	POWER_WHIP("Power Whip", Type.GRASS, 120,85,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	PRESENT("Present", Type.NORMAL, 1,90,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	PROTECT("Protect",Type.NORMAL, 0,-1,10,4, Category.STATUS,Target.SELF),
 	PSYBEAM("Psybeam", Type.PSYCHIC,65,100,20,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	PSYCH_UP("Psych Up", Type.NORMAL, 0,-1,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	PSYCHIC("Psychic", Type.PSYCHIC, 90,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	PSYCHO_BOOST("Psycho Boost", Type.PSYCHIC, 140,90,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	PSYCHO_CUT("Psycho Cut", Type.PSYCHIC, 70,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	PSYCHO_SHIFT("Psycho Shift", Type.PSYCHIC, 0,90,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	PSYSHOCK("Psyshock", Type.PSYCHIC, 80,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	PSYSTRIKE("Psystrike", Type.PSYCHIC, 100,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	PSYWAVE("Psywave", Type.PSYCHIC, 1,80,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	PUNISHMENT("Punishment", Type.DARK, 1,100,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	PURSUIT("Pursuit", Type.DARK, 40,100,20,7,Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	QUASH("Quash", Type.DARK, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	QUICK_ATTACK("Quick Attack",Type.NORMAL, 40,100,30,1, Category.PHYSICAL, Target.ANY_ADJACENT),
 	QUICK_GUARD("Quick Guard",Type.FIGHTING, 0,-1,15,3, Category.STATUS,Target.ALL_ALLIES),
 	QUIVER_DANCE("Quiver Dance",Type.BUG, 0,-1,20,0, Category.STATUS,Target.SELF),
 
 	RAGE("Rage", Type.NORMAL, 20,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	RAGE_POWDER("Rage Powder", Type.BUG, 0,-1,20,3, Category.STATUS,Target.SELF),
 	RAIN_DANCE("Rain Dance", Type.WATER, 0,-1,5,0, Category.STATUS,Target.ALL),
 	RAPID_SPIN("Rapid Spin", Type.NORMAL, 20,100,40,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	RAZOR_LEAF("Razor Leaf", Type.GRASS, 55,95,25,0, Category.PHYSICAL,Target.ALL_ADJACENT_FOES),
 	RAZOR_SHELL("Razor Shell", Type.WATER, 75,95,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	RAZOR_WIND("Razor Wind", Type.NORMAL, 80,100,10,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	RECOVER("Recover", Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.SELF),
 	RECYCLE("Recycle",Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.SELF),
 	REFLECT("Reflect", Type.PSYCHIC,0,-1,20,0, Category.STATUS,Target.ALL_ALLIES),
 	REFLECT_TYPE("Reflect Type", Type.NORMAL, 0,-1,15,0,Category.STATUS,Target.ANY_ADJACENT),
 	REFRESH("Refresh", Type.NORMAL, 0,-1,20,0,Category.STATUS,Target.SELF),
 	RELIC_SONG("Relic Song", Type.NORMAL, 75,100,10,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	REST("Rest", Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.SELF),
 	RETALIATE("Retaliate", Type.NORMAL, 70,100,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	RETURN("Return",Type.NORMAL, 1,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	REVENGE("Revenge",Type.FIGHTING, 60,100,10,-4, Category.PHYSICAL,Target.ANY_ADJACENT),
 	REVERSAL("Reversal",Type.FIGHTING, 1,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROAR("Roar", Type.NORMAL, 0,100,20,-6,Category.STATUS,Target.ANY_ADJACENT),
 	ROAR_OF_TIME("Roar of Time", Type.DRAGON, 150,90,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	ROCK_BLAST("Rock Blast", Type.ROCK, 25,90,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROCK_CLIMB("Rock Climb", Type.NORMAL, 90,85,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	
 	ROCK_POLISH("Rock Polish", Type.ROCK, 0,-1,20,0,Category.STATUS,Target.SELF),
 	ROCK_SLIDE("Rock Slide", Type.ROCK, 75,90,10,0,Category.PHYSICAL,Target.ALL_ADJACENT_FOES),
 	ROCK_SMASH("Rock Smash", Type.FIGHTING, 40,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROCK_THROW("Rock Throw", Type.ROCK, 50,90,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROCK_TOMB("Rock Tomb", Type.ROCK, 50,80,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROCK_WRECKER("Rock Wrecker", Type.ROCK, 150,90,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROLE_PLAY("Role Play", Type.PSYCHIC, 0,-1,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	ROLLING_KICK("Rolling Kick", Type.FIGHTING, 60,85,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROLLOUT("Rollout", Type.ROCK, 30,90,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	ROOST("Roost", Type.FLYING, 0,-1,10,0, Category.STATUS,Target.SELF),
 	ROUND("Round", Type.NORMAL, 60,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 
 	SACRED_FIRE("Sacred Fire", Type.FIRE, 100,95,5,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SACRED_SWORD("Sacred Sword", Type.FIGHTING, 90,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SAFEGUARD("Safeguard", Type.NORMAL, 0,-1,25,0, Category.STATUS,Target.ALL_ALLIES),
 	SAND_TOMB("Sand Tomb", Type.GROUND, 35,85,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SAND_ATTACK("Sand-Attack", Type.GROUND, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	SANDSTORM("Sandstorm", Type.ROCK, 0,-1,10,0, Category.STATUS,Target.ALL),
 	SCALD("Scald",Type.WATER, 80,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	SCARY_FACE("Scary Face",Type.NORMAL, 0,100,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	SCRATCH("Scratch", Type.NORMAL,40,100,35,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SCREECH("Screech", Type.NORMAL, 0,85,40,0,Category.STATUS,Target.ANY_ADJACENT),
 	SEARING_SHOT("Searing Shot", Type.FIRE, 100,100,5,0,Category.SPECIAL,Target.ALL_ADJACENT),
 	SECRET_POWER("Secret Power", Type.NORMAL, 70,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SECRET_SWORD("Secret Sword", Type.FIGHTING,85,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SEED_BOMB("Seed Bomb", Type.GRASS, 80,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SEED_FLARE("Seed Flare", Type.GRASS, 120,85,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SEISMIC_TOSS("Seismic Toss", Type.FIGHTING, 1,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SELFDESTRUCT("Selfdestruct", Type.NORMAL, 200,100,5,0,Category.PHYSICAL,Target.ALL_ADJACENT),
 	SHADOW_BALL("Shadow Ball", Type.GHOST, 80,100,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SHADOW_CLAW("Shadow Claw", Type.GHOST, 70,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SHADOW_FORCE("Shadow Force", Type.GHOST, 120,100,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SHADOW_PUNCH("Shadow Punch", Type.GHOST, 60,-1,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SHADOW_SNEAK("Shadow Sneak", Type.GHOST, 40,100,30,1,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SHARPEN("Sharpen", Type.NORMAL, 0,-1,30,0,Category.STATUS,Target.SELF),
 	SHEER_COLD("Sheer Cold", Type.ICE, 1,-1,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SHELL_SMASH("Shell Smash", Type.NORMAL, 0,-1,15,0,Category.SPECIAL,Target.SELF),
 	SHIFT_GEAR("Shift Gear", Type.STEEL, 0,-1,10,0,Category.STATUS,Target.SELF),
 	SHOCK_WAVE("Shock Wave", Type.ELECTRIC, 60,-1,20,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SIGNAL_BEAM("Signal beam", Type.BUG, 75,100,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SILVER_WIND("Silver Wind", Type.BUG, 60,100,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SIMPLE_BEAM("Simple Beam", Type.NORMAL, 0,100,15,0,Category.STATUS,Target.ANY_ADJACENT),
 	SING("Sing", Type.NORMAL, 0,55,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	SKETCH("Sketch", Type.NORMAL, 0,1,-1,0, Category.STATUS,Target.ANY_ADJACENT),
 	SKILL_SWAP("Skill Swap", Type.PSYCHIC, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	SKULL_BASH("Skull Bash", Type.NORMAL, 100,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SKY_ATTACK("Sky Attack", Type.FLYING, 140,90,5,0, Category.PHYSICAL,Target.ANY),
 	SKY_DROP("Sky Drop", Type.FLYING, 60,100,10,0, Category.PHYSICAL,Target.ANY),
 	SKY_UPPERCUT("Sky Uppercut", Type.FIGHTING, 85,90,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SLACK_OFF("Slack Off", Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.SELF),
 	SLAM("Slam",Type.NORMAL, 80,75,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SLASH("Slash", Type.NORMAL,70,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SLEEP_POWDER("Sleep Powder", Type.GRASS, 0,75,15,0,Category.STATUS,Target.ANY_ADJACENT),
 	SLEEP_TALK("Sleep Talk", Type.NORMAL, 0,-1,10,0,Category.STATUS,Target.SELF),
 	SLUDGE("Sludge", Type.POISON, 65,100,20,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	
 	SLUDGE_BOMB("Sludge Bomb", Type.POISON, 90,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	SLUDGE_WAVE("Sludge Wave", Type.POISON, 95,100,10,0,Category.SPECIAL,Target.ALL_ADJACENT),
 	SMACK_DOWN("Smack Down", Type.ROCK, 50,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SMELLINGSALT("SmellingSalt", Type.NORMAL, 60,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SMOG("Smog", Type.POISON, 20,70,20,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	SMOKESCREEN("SmokeScreen", Type.NORMAL, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	SNARL("Snarl",Type.DARK, 55,95,15,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	SNATCH("Snatch", Type.DARK, 0,-1,10,4,Category.STATUS,Target.SELF),
 	SNORE("Snore", Type.NORMAL, 40,100,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	SOAK("Soak", Type.WATER, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	SOFTBOILED("Softboiled",Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.SELF),
 	SOLARBEAM("SolarBeam",Type.GRASS, 120,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	SONICBOOM("SonicBoom",Type.NORMAL, 1,90,20,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	SPACIAL_REND("Spacial Rend",Type.DRAGON, 100,95,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	SPARK("Spark", Type.ELECTRIC,65,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	SPIDER_WEB("Spider Web", Type.BUG, 0,-1,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	SPIKE_CANNON("Spike Cannon", Type.NORMAL, 20,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SPIKES("Spikes", Type.GROUND, 0,-1,20,0,Category.STATUS,Target.ANY_ADJACENT),
 	SPIT_UP("Spit Up", Type.NORMAL, 1,100,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	SPITE("Spite", Type.GHOST, 0,100,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	SPLASH("Splash", Type.NORMAL, 0,-1,40,0, Category.STATUS,Target.SELF),
 	SPORE("Spore",Type.GRASS, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	STEALTH_ROCK("Stealth Rock",Type.ROCK, 0,-1,20,0, Category.STATUS,Target.ALL_FOES),
 	STEAMROLLER("Steamroller",Type.BUG, 65,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	STEEL_WING("Steel Wing",Type.STEEL, 70,90,25,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	STOCKPILE("Stockpile",Type.NORMAL, 0,-1,20,0, Category.STATUS,Target.SELF),
 	STOMP("Stomp", Type.NORMAL, 65,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	STONE_EDGE("Stone Edge", Type.ROCK, 100,80,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	STORED_POWER("Stored Power", Type.PSYCHIC, 20,100,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	STORM_THROW("Storm Throw", Type.FIGHTING, 40,100,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	STRENGTH("Strength", Type.NORMAL, 80,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	STRING_SHOT("String Shot", Type.BUG, 0,95,40,0,	Category.STATUS,Target.ALL_ADJACENT_FOES),
 	STRUGGLE("Struggle", Type.NORMAL, 50,-1,-1,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	STRUGGLE_BUG("Struggle Bug", Type.BUG, 30,100,20,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	STUN_SPORE("Stun Spore", Type.GRASS, 0,75,30,0,Category.STATUS,Target.ANY_ADJACENT),
 	SUBMISSION("Submission", Type.FIGHTING, 80,80,25,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SUBSTITUTE("Substitute", Type.NORMAL, 0,-1,10,0,Category.STATUS,Target.SELF),
 	SUCKER_PUNCH("Sucker Punch", Type.DARK, 80,100,5,1,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SUNNY_DAY("Sunny Day", Type.FIRE, 0,-1,5,0,Category.STATUS,Target.ALL),
 	SUPER_FANG("Super Fang", Type.NORMAL, 1,90,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SUPERPOWER("Superpower", Type.FIGHTING, 120,100,5,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	SUPERSONIC("Supersonic", Type.NORMAL, 0,55,20,0,Category.STATUS,Target.ANY_ADJACENT),
 	SURF("Surf", Type.WATER, 95,100,15,0, Category.SPECIAL,Target.ALL_ADJACENT),
 	SWAGGER("Swagger", Type.NORMAL, 0,90,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	SWALLOW("Swallow",Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.SELF),
 	SWEET_KISS("Sweet Kiss",Type.NORMAL, 0,75,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	SWEET_SCENT("Sweet Scent",Type.NORMAL, 0,100,20,0, Category.STATUS,Target.ALL_ADJACENT_FOES),
 	SWIFT("Swift", Type.NORMAL, 60,-1,20,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	SWITCHEROO("Switcheroo", Type.DARK, 0,100,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	SWORDS_DANCE("Swords Dance", Type.NORMAL, 0,-1,30,0,Category.STATUS,Target.SELF),
 	SYNCHRONOISE("Synchronoise", Type.PSYCHIC, 70,100,15,0,Category.STATUS,Target.ALL_ADJACENT),
 	SYNTHESIS("Synthesis", Type.GRASS, 0,-1,5,0,Category.STATUS,Target.SELF),
 
 	TACKLE("Tackle", Type.NORMAL, 50,100,35,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	TAIL_GLOW("Tail Glow", Type.BUG, 0,-1,20,0, Category.STATUS,Target.SELF),
 	TAIL_SLAP("Tail Slap",Type.NORMAL, 25,85,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	TAIL_WHIP("Tail Whip",Type.NORMAL, 0,100,30,0, Category.STATUS,Target.ALL_ADJACENT_FOES),
 	TAILWIND("Tailwind", Type.FLYING,0,-1,30,0, Category.STATUS,Target.ALL_ALLIES),
 	TAKE_DOWN("Take Down", Type.NORMAL, 90,85,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	TAUNT("Taunt", Type.DARK, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	TECHNO_BLAST("Techno Blast", Type.NORMAL, 85,100,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	TEETER_DANCE("Teeter Dance", Type.NORMAL, 0,100,20,0, Category.STATUS,Target.ALL_ADJACENT),
 	TELEKINESIS("Telekinesis", Type.PSYCHIC, 0,-1,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	TELEPORT("Teleport", Type.PSYCHIC, 0,-1,20,0, Category.STATUS,Target.SELF),
 	THIEF("Thief",Type.DARK, 40,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	THRASH("Thrash", Type.NORMAL,120,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT_FOE),
 	THUNDER("Thunder", Type.ELECTRIC, 120,70,10,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	THUNDER_FANG("Thunder Fang", Type.ELECTRIC, 65,95,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	THUNDER_WAVE("Thunder Wave", Type.ELECTRIC, 0,100,20,0,Category.STATUS,Target.ANY_ADJACENT),
 	THUNDERBOLT("Thunderbolt", Type.ELECTRIC, 95,100,15,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	THUNDERPUNCH("ThunderPunch", Type.ELECTRIC, 75,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	THUNDERSHOCK("ThunderShock", Type.ELECTRIC, 40,100,30,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	TICKLE("Tickle", Type.NORMAL, 0,100,20,0, Category.STATUS,Target.ANY_ADJACENT),
 	TORMENT("Torment", Type.DARK, 0,100,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	TOXIC("Toxic",Type.POISON, 0,90,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	TOXIC_SPIKES("Toxic Spikes",Type.POISON, 0,-1,20,0, Category.STATUS,Target.ALL_ADJACENT_FOES),
 	TRANSFORM("Transform",Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 	TRI_ATTACK("Tri Attack",Type.NORMAL, 80,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	TRICK("Trick", Type.PSYCHIC, 0,100,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	TRICK_ROOM("Trick Room", Type.PSYCHIC, 0,-1,5,-7,Category.STATUS,Target.ALL),
 	TRIPLE_KICK("Triple Kick", Type.FIGHTING, 10,90,10,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	TRUMP_CARD("Trump Card", Type.NORMAL, 1,-1,5,0,Category.SPECIAL,Target.ANY_ADJACENT),
 	TWINEEDLE("Twineedle", Type.BUG, 25,100,20,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	TWISTER("Twister", Type.DRAGON, 40,100,20,0,Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 
 	UPROAR("Uproar", Type.NORMAL, 90,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT_FOE),
 	U_TURN("U-Turn",Type.BUG, 70,100,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	V_CREATE("V-create", Type.FIRE, 180,95,5,0, Category.PHYSICAL, Target.ANY_ADJACENT), 
 	
 	VACUUM_WAVE("Vacuum Wave", Type.FIGHTING, 40,100,30,1, Category.SPECIAL,Target.ANY_ADJACENT),
 	VENOSHOCK("Venoshock", Type.POISON, 65, 100,10,0,Category.SPECIAL, Target.ANY_ADJACENT),
 	
 	VICEGRIP("ViceGrip", Type.NORMAL, 55, 100,30,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	VINE_WHIP("Vine Whip", Type.GRASS, 35, 100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	
 	VITAL_THROW("Vital Throw", Type.FIGHTING, 70,-1,10,-1, Category.PHYSICAL,Target.ANY_ADJACENT),
 	VOLT_SWITCH("Volt Switch", Type.ELECTRIC, 70,100,20,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	VOLT_TACKLE("Volt Tacke", Type.ELECTRIC, 120,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	WAKE_UP_SLAP("Wake-Up Slap", Type.FIGHTING, 60,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	WATER_GUN("Water Gun", Type.WATER, 40,100,25,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	WATER_PLEDGE("Water Pledge", Type.WATER, 50,100,10,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	WATER_PULSE("Water Pulse", Type.WATER, 60,100,20,0, Category.SPECIAL,Target.ANY),
 	WATER_SPORT("Water Sport", Type.WATER, 0,-1,15,0, Category.STATUS,Target.ALL),
 	WATER_SPOUT("Water Spout", Type.WATER, 1,100,5,0, Category.SPECIAL,Target.ALL_ADJACENT_FOES),
 	WATERFALL("Waterfall", Type.WATER, 80,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	WEATHER_BALL("Weather Ball", Type.NORMAL, 50,100,10,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	WHIRLPOOL("Whirlpool", Type.WATER, 35,85,15,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	WHIRLWIND("Whirlwind", Type.NORMAL, 0,100,20,-6, Category.STATUS,Target.ANY_ADJACENT),
 	WIDE_GUARD("Wide Guard", Type.ROCK, 0,1,10,3, Category.STATUS,Target.ALL_ALLIES),
 	WILD_CHARGE("Wild Charge", Type.ELECTRIC, 90,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	WILL_O_WISP("Will-O-Wisp", Type.FIRE, 0,75,15,0, Category.STATUS,Target.ANY_ADJACENT),
 	WING_ATTACK("Wing Attack", Type.FLYING, 60,100,35,0, Category.PHYSICAL,Target.ANY),
 	WISH("Wish",Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.SELF),
 	WITHDRAW("Withdraw", Type.WATER,0,-1,40,0, Category.STATUS,Target.SELF),
 	WONDER_ROOM("Wonder Room", Type.PSYCHIC, 0,-1,10,-7,Category.STATUS,Target.ALL),
 	WOOD_HAMMER("Wood Hammer", Type.GRASS, 120,100,15,0,Category.PHYSICAL,Target.ANY_ADJACENT),
 	WORK_UP("Work Up", Type.NORMAL, 0,-1,30,0,Category.STATUS,Target.SELF),
 	WORRY_SEED("Worry Seed", Type.GRASS, 0,100,10,0,Category.STATUS,Target.ANY_ADJACENT),
 	WRAP("Wrap", Type.NORMAL, 15,90,20,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 	WRING_OUT("Wring Out", Type.NORMAL, 1,100,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 
 	X_SCISSOR("X-Scissor", Type.BUG, 80,100,15,0, Category.PHYSICAL,Target.ANY_ADJACENT),
 
 	YAWN("Yawn", Type.NORMAL, 0,-1,10,0, Category.STATUS,Target.ANY_ADJACENT),
 
 	ZAP_CANNON("Zap Cannon", Type.ELECTRIC, 120,50,5,0, Category.SPECIAL,Target.ANY_ADJACENT),
 	ZEN_HEADBUTT("Zen Headbutt", Type.PSYCHIC, 80,90,15,0, Category.PHYSICAL,Target.ANY_ADJACENT);
 
 	private String move;
 	private Type type;
 	private int basePower;
 	private int accuracy;
 	private int pp;
 	private int priority;
 	private Target target;
 	private Category category;
 
 	public String toString() {
 		return move;
 	}
 
 	Move(String move, Type type, int basePower, int accuracy, int pp, int priority, Category category, Target target) {
 		this.move = move;
 		this.type = type;
 		this.basePower = basePower;
 		this.category = category;
 		this.target=target;
 		this.accuracy = accuracy;
 		this.pp = pp;
 		this.priority = priority;
 	}
 
 	public static Move findMoveByName(String name) {
 		Move move = null;
 		for (Move m : Move.values()) {
 			if (m.getName().toLowerCase().equals(name.toLowerCase())) {
 				move = m;
 				break;
 			}
 		}
 		return move;
 	}
 
 	public String getName() {
 		return move;
 	}
 
 	public int getAccuracy() {
 		return accuracy;
 	}
 	
 	public Type getType() {
 		return type;
 	}
 
 	public int getBasePower() {
 		return basePower;
 	}
 	
 	public int getBasePower(Pokemon attacker, Pokemon target) {
 		
 		switch(this) {
 			case LOW_KICK:
 			case GRASS_KNOT:
 				return BasePower.LowKickOrGrassKnot(target);
 //			case PSYWAVE:
 //				return BasePower.Psywave(attacker);
 			case FLAIL:
 				return BasePower.Flail(attacker);
 			case REVERSAL:
 				return BasePower.Reversal(attacker);
 			case ERUPTION:
 			case WATER_SPOUT:
 				return BasePower.EruptionOrWaterSpout(attacker);
 		
 			default:
 				return basePower;
 		}
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public Target getTarget() {
 		return target;
 	}
 	
 	public int getPP(){
 		return pp;
 	}
 	
 	public int getPriority() {
 		return priority;
 	}
 	
 	public boolean isPhysical() {
 		return category == Category.PHYSICAL;
 	}
 
 	public boolean isSpecial() {
 		return category == Category.SPECIAL;
 	}
 
 	public boolean isStatus() {
 		return category == Category.STATUS;
 	}
 
 	public boolean isPunch() {
 		switch (this) {
 		case BULLET_PUNCH:
 		case COMET_PUNCH:
 		case DIZZY_PUNCH:
 		case DRAIN_PUNCH:
 		case DYNAMICPUNCH:
 		case FIRE_PUNCH:
 		case FOCUS_PUNCH:
 		case HAMMER_ARM:
 		case ICE_PUNCH:
 		case MACH_PUNCH:
 		case MEGA_PUNCH:
 		case METEOR_MASH:
 		case SHADOW_PUNCH:
 		case SKY_UPPERCUT:
 		case THUNDERPUNCH:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean isSoundBased() {
 		switch (this) {
 		case BUG_BUZZ:
 		case CHATTER:
 		case ECHOED_VOICE:
 		case GRASSWHISTLE:
 		case GROWL:
 		case HEAL_BELL:
 		case HYPER_VOICE:
 		case METAL_SOUND:
 		case PERISH_SONG:
 		case ROAR:
 		case ROUND:
 		case SCREECH:
 		case SING:
         case SNARL:
 		case SNORE:
 		case SUPERSONIC:
 		case UPROAR:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean isHealing() {
 		switch (this) {
 		case HEAL_ORDER:
 		case MILK_DRINK:
 		case MOONLIGHT:
 		case MORNING_SUN:
 		case RECOVER:
 		case REST:
 		case ROOST:
 		case SLACK_OFF:
 		case SOFTBOILED:
 		case SWALLOW:
 		case SYNTHESIS:
 		case WISH:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean usableWhileAsleep() {
 		switch (this) {
 		case SLEEP_TALK:
 		case SNORE:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean usableWhileFrozen() {
 		switch (this) {
 		case FLAME_WHEEL:
 		case SACRED_FIRE:
 		case SCALD:
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	public boolean breaksScreens() {
 		return this == BRICK_BREAK;
 	}
 	
 	public boolean switchAferMove() {
 	    switch (this) {
 	        case BATON_PASS:
 	        case U_TURN:
 	        case VOLT_SWITCH:
 	            return true;
 	        default:
 	            return false;
 	    }
 	}
 
 	public boolean phases() {
 	    switch (this) {
 	        case ROAR:
 	        case WHIRLWIND:
 	        case DRAGON_TAIL:
 	            return true;
 	        default:
 	            return false;
 	    }
 	}
 	
 	public boolean hasRecoil() {
 		switch (this) {
 		case BRAVE_BIRD:
 		case DOUBLE_EDGE:
 		case FLARE_BLITZ:
 		case HEAD_SMASH:
 		case SUBMISSION:
 		case TAKE_DOWN:
 		case VOLT_TACKLE:
 		case WOOD_HAMMER:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
