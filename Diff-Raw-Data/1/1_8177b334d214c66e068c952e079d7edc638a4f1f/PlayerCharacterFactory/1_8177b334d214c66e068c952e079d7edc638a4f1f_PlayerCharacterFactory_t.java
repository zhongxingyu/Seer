 package com.censoredsoftware.Demigods.Engine.PlayerCharacter;
 
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.censoredsoftware.Demigods.API.CharacterAPI;
 import com.censoredsoftware.Demigods.API.DeityAPI;
 import com.censoredsoftware.Demigods.Engine.Deity.Deity;
 import com.censoredsoftware.Demigods.Engine.Demigods;
 
 public class PlayerCharacterFactory
 {
 	// TODO: Make a createCharacter method that can set the meta values on creation as it was before. The way it was no longer worked as expected because I fixed the PlayerCharacterMeta issues so favor and everything actually works and I was too lazy to clean this all up so there are a ton of extra variables and all and whoa this is a really long TODO so I'm just going to keep going for a bit. Nice day, isn't it? I wouldn't know. I'm just stuck in here programming like some sort of animal. Damn. That's what I am. An animal. WHAT HAVE YOU DONE TO ME WORLD?
 	public static PlayerCharacter createCharacter(final OfflinePlayer player, final String charName, final Deity deity, final int favor, final int maxFavor, final int devotion, final int ascensions, final int offense, final int defense, final int stealth, final int support, final int passive, final boolean immortal)
 	{
 		PlayerCharacter character = new PlayerCharacter();
 		character.setPlayer(player);
 		character.setName(charName);
 		character.setDeity(deity);
 		character.setImmortal(immortal);
 		character.setHealth(20);
 		character.setHunger(20);
 		character.setExperience(0);
 		character.setLevel(0);
 		character.setLocation(player.getPlayer().getLocation());
		character.setMeta(createCharacterMeta());
 		PlayerCharacter.save(character);
 		return character;
 	}
 
 	public static PlayerCharacter createCharacter(OfflinePlayer player, String charName, String charDeity)
 	{
 		if(CharacterAPI.getCharByName(charName) == null)
 		{
 			// Create the Character
 			return createCharacter(player, charName, DeityAPI.getDeity(charDeity), 0, 50, 0, 0, 0, 0, 0, 0, 0, true);
 		}
 		return null;
 	}
 
 	public static PlayerCharacterMeta createCharacterMeta()
 	{
 		PlayerCharacterMeta charMeta = new PlayerCharacterMeta();
 		charMeta.setAscensions(Demigods.config.getSettingInt("character.default_ascensions"));
 		charMeta.setDevotion(Demigods.config.getSettingInt("character.default_devotion"));
 		charMeta.setFavor(Demigods.config.getSettingInt("character.default_favor"));
 		charMeta.setMaxFavor(Demigods.config.getSettingInt("character.default_max_favor"));
 		charMeta.setLevel("OFFENSE", Demigods.config.getSettingInt("character.default_offense"));
 		charMeta.setLevel("DEFENSE", Demigods.config.getSettingInt("character.default_defense"));
 		charMeta.setLevel("STEALTH", Demigods.config.getSettingInt("character.default_stealth"));
 		charMeta.setLevel("SUPPORT", Demigods.config.getSettingInt("character.default_support"));
 		charMeta.setLevel("PASSIVE", Demigods.config.getSettingInt("character.default_passive"));
 		charMeta.initializeMaps();
 		PlayerCharacterMeta.save(charMeta);
 		return charMeta;
 	}
 
 	public static PlayerCharacterInventory createPlayerCharacterInventory(PlayerCharacter character)
 	{
 		PlayerInventory inventory = character.getOfflinePlayer().getPlayer().getInventory();
 		PlayerCharacterInventory charInventory = new PlayerCharacterInventory();
 		charInventory.setOwner(character.getId());
 		if(inventory.getHelmet() != null) charInventory.setHelmet(inventory.getHelmet());
 		if(inventory.getChestplate() != null) charInventory.setChestplate(inventory.getChestplate());
 		if(inventory.getLeggings() != null) charInventory.setLeggings(inventory.getLeggings());
 		if(inventory.getBoots() != null) charInventory.setBoots(inventory.getBoots());
 		charInventory.setItems(inventory);
 		PlayerCharacterInventory.save(charInventory);
 		return charInventory;
 	}
 
 	public static PlayerCharacterInventory createEmptyCharacterInventory()
 	{
 		PlayerCharacterInventory charInventory = new PlayerCharacterInventory();
 		charInventory.setHelmet(new ItemStack(Material.AIR));
 		charInventory.setChestplate(new ItemStack(Material.AIR));
 		charInventory.setLeggings(new ItemStack(Material.AIR));
 		charInventory.setBoots(new ItemStack(Material.AIR));
 		PlayerCharacterInventory.save(charInventory);
 		return charInventory;
 	}
 }
