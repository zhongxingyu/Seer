 package com.herocraftonline.heromagic;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Location;
 import org.bukkit.util.config.Configuration;
 
 public class Spells {
 	private HeroMagic plugin;
 	private Configuration config;
 	public List<Spell> spellList;
 	public List<String> blackList;
 	
 	Spells(HeroMagic instance) {
 		this.plugin = instance;
 		this.config = this.plugin.getConfiguration();
 		this.spellList = new ArrayList<Spell>();
 		this.blackList = new ArrayList<String>();
 		
 		this.config.load();
 		
 		//TODO Add new spells here!
 		spellList.add(new Spell("Blink"));
 		spellList.add(new Spell("Gate"));
 		spellList.add(new Spell("Recall"));
 		spellList.add(new Spell("Mark"));
 		
 		for(Spell spell : spellList) {
 			int coolDown = this.config.getInt("Spells." + spell.getName() + ".CoolDown", 60);
 			String world = this.config.getString("Spells." + spell.getName() + ".Location.World", plugin.getServer().getWorlds().get(0).getName());
 			double x = this.config.getDouble("Spells." + spell.getName() + ".Location.X", 0);
 			double y = this.config.getDouble("Spells." + spell.getName() + ".Location.Y", 0);
 			double z = this.config.getDouble("Spells." + spell.getName() + ".Location.Z", 0);
 			int reagent1 = this.config.getInt("Spells." + spell.getName() + ".Reagent1-ID", 331);
 			int reagent2 = this.config.getInt("Spells." + spell.getName() + ".Reagent2-ID", 0);
 			int reagent1_amount = this.config.getInt("Spells." + spell.getName() + ".Reagent1-Amount", 5);
 			int reagent2_amount = this.config.getInt("Spells." + spell.getName() + ".Reagent2-Amount", 0);
 			String reagent1_name = this.config.getString("Spells." + spell.getName() + ".Reagent1-Name", "Redstone Dust");
 			String reagent2_name = this.config.getString("Spells." + spell.getName() + ".Reagent2-Name", "");
 			spell.setCoolDown(coolDown);
 			spell.setLocation(new Location(this.plugin.getServer().getWorld(world), x, y, z));
 			spell.setReagent1(reagent1);
 			spell.setReagent2(reagent2);
 			spell.setReagent1_amount(reagent1_amount);
 			spell.setReagent2_amount(reagent2_amount);
 			spell.setReagent1_name(reagent1_name);
 			spell.setReagent2_name(reagent2_name);
 		}

		this.blackList = this.config.getStringList("BlackList", this.blackList);
 		save();
 	}
 	
 	public void save() {
 		for(Spell spell : spellList) {
 			this.config.setProperty("Spells." + spell.getName() + ".CoolDown", spell.getCoolDown());
 			String world;
 			if (spell.getLocation().getWorld() != null) world = spell.getLocation().getWorld().getName();
 			else world = plugin.getServer().getWorlds().get(0).getName();
 			this.config.setProperty("Spells." + spell.getName() + ".Location.World", world);
 			this.config.setProperty("Spells." + spell.getName() + ".Location.X", spell.getLocation().getX());
 			this.config.setProperty("Spells." + spell.getName() + ".Location.Y", spell.getLocation().getY());
 			this.config.setProperty("Spells." + spell.getName() + ".Location.Z", spell.getLocation().getZ());
 			this.config.setProperty("Spells." + spell.getName() + ".Reagent1-ID", spell.getReagent1());
 			this.config.setProperty("Spells." + spell.getName() + ".Reagent1-Amount", spell.getReagent1_amount());
 			this.config.setProperty("Spells." + spell.getName() + ".Reagent1-Name", spell.getReagent1_name());
 			this.config.setProperty("Spells." + spell.getName() + ".Reagent2-ID", spell.getReagent2());
 			this.config.setProperty("Spells." + spell.getName() + ".Reagent2-Amount", spell.getReagent2_amount());
 			this.config.setProperty("Spells." + spell.getName() + ".Reagent2-Name", spell.getReagent2_name());
 		}
 		
 		this.config.setProperty("BlackList", this.blackList);
 		this.config.save();
 	}
 	
 	public Spell getSpellByName(String spellName) {
 		for(Spell spell : spellList) {
 			if (spell.getName().equalsIgnoreCase(spellName)) {
 				return spell;
 			}
 		}
 		return null;
 	}
 }
