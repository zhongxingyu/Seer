 package io.github.harryprotist;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.World;
 import org.bukkit.Location;
 
 import io.github.harryprotist.block.BlockFunction;
 import io.github.harryprotist.block.BreakBlock;
 import io.github.harryprotist.block.GiveMana;
 import io.github.harryprotist.block.MakeExplosion;
 import io.github.harryprotist.block.MemAdd;
 import io.github.harryprotist.block.MemBlockFunction;
 import io.github.harryprotist.block.MemSet;
 import io.github.harryprotist.block.PlaceBlock;
 import io.github.harryprotist.block.SetTargetLooking;
 import io.github.harryprotist.block.ShiftLoc;
 import io.github.harryprotist.block.ShootArrow;
 
 import java.util.*;
 import java.io.*;
 
 // this class is the real meat of the plugin
 public class Spell {
 
 	private Spellcraft Plugin;
 
 	private static Map<Integer, Integer> Functions = new HashMap<Integer, Integer>();
 	private static Map<Integer, Integer> Values = new HashMap<Integer, Integer>();
 	public static boolean Load() {
 		
 		BufferedReader file;
 		try { file = new BufferedReader(new FileReader("spell.txt") ); }
 		catch (FileNotFoundException f) { return false; }
 		String line;
 		
 		try {
 		while ( (line = file.readLine()) != null ) {
 
 			String[] data = line.split(" ");
 			if (data.length != 3) return false;
 
 			Integer key;
 			Integer fun;
 			Integer val;
 
 			try {
 				key = new Integer(data[0]);
 				fun = new Integer(data[1]);
 				val = new Integer(data[2]);	
 
 			} catch (NumberFormatException e) {
 				return false;
 			}
 
 			// the values are good, let's add 'em
 			Functions.put	(key, fun);
 			Values.put	(key, val);
 		}
 		}
 		catch (IOException e) {
 			return true;
 		}		
 
 		return true;
 	}
 	public static Integer getFunction(Integer key) {
 		return Functions.get(key);
 	}
 	public static Integer getValue(Integer key) {
 		return ((Values.get(key) == null)? (10):(Values.get(key)));
 	}
 
 	public static String dumpMaps() {
 		String ret = "Functions:\n";
 
 		for (Object s : Functions.keySet().toArray() ) {
 			Integer S = (Integer)s;
 			ret += S.toString() + "\t" + getFunction(S).toString() + "\n";
 		}
 		
 		ret += "\nValues:\n";
 		for (Object s : Values.keySet().toArray() ) {
 			Integer S = (Integer)s;
 			ret += S.toString() + "\t" + getValue(S).toString() + "\n";
 		}	
 
 		return ret;
 	}
 
 	public String dumpScript() {
 
 		if (Script == null) return "";	
 		
 		String ret = "";
 		for (Integer i : Script) {
 
 			ret += i.toString() + " ";	
 		}
 
 		return ret;
 	}
 	public static Spell parseString(String s) {
 
 		ArrayList<Integer> script = new ArrayList<Integer>();
 		for (String ss : s.split(" ")) {
 			
 			script.add(new Integer(ss) );
 		}
 
 		return new Spell(script); 
 	}
 	
 
 	// script is a list of functions
 	private ArrayList<Integer> Script;
 	public Spell(ArrayList<Integer> script) {
 		Script = script;
 	}
 	public Spell(ArrayList<Integer> script, Spellcraft p) {
 		Script = script;
 		Plugin = p;
 	}
 	public Spell(Spell sp, Spellcraft p) {
 		Script = sp.Script;
 		Plugin = p;
 	}
 
 	public int Excecute(int manaSource, Player caster) {
 
 		int manaUsed = 0; 	
 
 		// maps the ID of a block to the value assigned to it
 		Map<Integer, Integer> mem = new HashMap<Integer, Integer>();
 
 		World w = caster.getWorld();
 
 		Location loc = caster.getLocation();
 
 		//caster.sendMessage(dumpScript());
 
 		SPELL:
 		for (int i = 0; i < Script.size();) {
 
 			int cmd = Script.get(i).intValue();
 			int argc = 0;
 			
 			ArrayList<Integer> argv = new ArrayList<Integer>();
 			if (cmd > 0) {
 
 				i++;
 				argc = Script.get(i).intValue();		
 
 				//caster.sendMessage(cmd + " " + argc);
 
 				for (int j = 1; j <= argc; j++) {
 
 					if (mem.containsKey(Script.get(i + j) ) ) {
 						argv.add(mem.get(Script.get(i + j) ) );
 					} else {
 						argv.add(Script.get(i + j));
 					}
 				}
 			}
 
 			//caster.sendMessage(cmd + ", with " + argc + " args");
 			//caster.sendMessage(getFunction(cmd).toString() + " is the corresponding function");
 
 			if (cmd < 0) {
 				
 				//caster.sendMessage((cmd) + " ");
 				i = -(cmd);
 				continue SPELL;
 			}
 
 			if (getFunction(cmd) == null) break;
 			if (getValue(cmd) == null) break;
 
 			BlockFunction function = null;
 
 			SWITCH:
 			switch (getFunction(cmd).intValue()) {
 				case 1: function = new SetTargetLooking(argv, caster, loc);
 				// Sets target to where you're looking
 				break;
 				case 2: 
 					ArrayList<Integer> tmpArgv = new ArrayList<Integer>();
 					tmpArgv.add(new Integer(51));
 					function = new PlaceBlock(tmpArgv, caster, loc);
 				// Sets current block on fire, if it's air
 				break;	
 				case 3: function = new ShiftLoc(argv, caster, loc, 0.0, -1.0, 0.0);
 				// Moves location down 1
 				break;
 				case 4: function = new ShiftLoc(argv, caster, loc, 0.0, 1.0, 0.0);
 				// Moves location up 1
 				break;
 				case 5: function = new ShiftLoc(argv, caster, loc, 1.0, 0.0, 0.0);
 				// Moves location +1 away
 				break;
 				case 6: function = new ShiftLoc(argv, caster, loc, -1.0, 0.0, 0.0);
 				// Moves location -1 away
 				break;	
 				case 7: function = new ShiftLoc(argv, caster, loc, 0.0, 0.0, 1.0);
 				// Moves location +1 right
 				break;
 				case 8: function = new ShiftLoc(argv, caster, loc, 0.0, 0.0, -1.0);
 				// Moves location -1 right
 				break;
 				case 9: function = new MakeExplosion(argv, caster, loc, false);
 				// Takes an arg for explosion power
 				break;
 				case 10: function = new MakeExplosion(argv, caster, loc, true);
 				// same as before, only a FIERY explosion
 				break;
 				case 11: function = new PlaceBlock(argv, caster, loc);
 				// create a block, only it uses a ton of mana, only makes blocks in air
 				break;
 				case 12: function = new BreakBlock(argv, caster, loc);
 				// breaks a block at loc
 				break;
 				case 13: function = new MemSet(argv, caster, loc, mem, Script, i);
 				// sets memory address of arg 1 to the ID of arg 2
 				break;
 				case 14: function = new MemAdd(argv, caster, loc, mem, Script, i, false);
 				// adds arg 2 to mem address of 1st arg
 				break;
 				case 15: function = new MemAdd(argv, caster, loc, mem, Script, i, true);
 				// subs arg 2 from mem address of 1st arg (min 0)
 				break;
 				case 16: 
 					if (argc != 1) break SPELL;
 					if (argv.get(0).intValue() == 0) break SPELL;
 					manaUsed += 1;
 					if (manaSource < manaUsed) return 0;
 				// stops the rune if arg is 0
 				break;
 				case 17: function = new GiveMana(argv, caster, loc, Plugin);
 				// transfer mana to caster
 				break;
 				case 18: function = new ShootArrow(argv, caster, loc);
 				// shoot an arrow towards loc with power argv[0]	
 				break;
 				default:
 					break SPELL;
 			}
 
 			if (function == null || !function.isValid() ) {
 				break SPELL;
 			}
 
 			manaUsed += function.getManaCost();
 			if (manaSource < manaUsed) return 0; 
 
 			function.runFunction();
 
 			i += argc + 1;
 			//caster.sendMessage("Done with " + i + " out of " + Script.size() );
 		}
 
 		return manaSource - manaUsed;	
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
