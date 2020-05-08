 package me.Nekoyoubi.Blessings.Gods;
 
 import java.util.ArrayList;
 
 import org.bukkit.Material;
 
 import me.Nekoyoubi.Blessings.Favor;
 import me.Nekoyoubi.Blessings.God;
 
 public class Athena extends God {
 	public Athena() {
 		colorCode = "&e";
 		displayName = "Athena";
 		msgGiven = colorName()+" smiles fortune upon you.";
 		msgDisappoint = colorName()+" tests your loyalty and faith.";
		msgNull = colorName()+" is simply too busy for you now.";
 		shrineBases = new ArrayList<Material>(2);
 		shrineBases.add(Material.BRICK);
 		shrineBases.add(Material.SMOOTH_BRICK);
 		favors = new ArrayList<Favor>();
 		favors.add(new Favor(75, "give", "player", "351:11", "")); // One yellow dye.
 		favors.add(new Favor(75, "give", "player", "351:7", "")); // One light-grey dye.
 		favors.add(new Favor(75, "give", "player", "351:9", "")); // One pink dye.
 		favors.add(new Favor(100, "give", "player", "45x10", "")); // Ten bricks.
 		favors.add(new Favor(100, "give", "player", "98x10", "")); // Ten smooth bricks.
 		favors.add(new Favor(30, "give", "player", "35:4x10", "")); // Ten yellow wool.
 		favors.add(new Favor(30, "give", "player", "35:8x10", "")); // Ten light-grey wool.
 		favors.add(new Favor(30, "give", "player", "35:6x10", "")); // Ten pink wool.
 		favors.add(new Favor(30, "give", "player", "20x10", "")); // Ten glass blocks.
 		favors.add(new Favor(30, "give", "player", "102x10", "")); // Ten glass panes blocks.
 		favors.add(new Favor(30, "give", "player", "355", "")); // One bed.
 		favors.add(new Favor(30, "give", "player", "306", "")); // An iron helmet.
 		favors.add(new Favor(30, "give", "player", "307", "")); // An iron chest.
 		favors.add(new Favor(30, "give", "player", "308", "")); // A pair of iron pants.
 		favors.add(new Favor(30, "give", "player", "309", "")); // A pair of iron boots.
 		favors.add(new Favor(20, "give", "player", "262x64", "")); // A stack (64) of arrows.
 		favors.add(new Favor(20, "give", "player", "46", "")); // A block of TNT.
 		favors.add(new Favor(10, "give", "player", "310", "")); // A diamond helmet.
 		favors.add(new Favor(10, "give", "player", "311", "")); // A diamond chest.
 		favors.add(new Favor(10, "give", "player", "312", "")); // A pair of diamond pants.
 		favors.add(new Favor(10, "give", "player", "313", "")); // A pair of diamond boots.
 		
 		curses = new ArrayList<Favor>();	
 		curses.add(new Favor(0, "weaken", "player", "50", "")); // Weakens a random item from the player's inventory by 50%.
 		
 	}
 
 }
