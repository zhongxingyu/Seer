 package io.github.harryprotist.block;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.World;
 import org.bukkit.Location;
 
 import java.util.*;
 
 import io.github.harryprotist.Spell;
 
 public class MemAdd extends MemBlockFunction
 {
 	private static final int ARGC = 2;
 	private boolean subtract;	
 
 	public MemAdd(ArrayList<Integer> a, Player c, Location l,
 	Map<Integer, Integer> m, ArrayList<Integer> s, int i, boolean sub) {
 
 		super(a, c, l, m, s, i);
 		subtract = sub;
 	}	
 
 	public boolean isValid() {
 		
 		return argv.size() == ARGC;
 	}
 	public int getManaCost() {
 		
 		return 0;
 	}
 	public void runFunction() {
 		
 		int factor = subtract? -1:1;
 
		mem.put( rargv.get(0), factor * argv.get(1) + argv.get(0) );
 	}
 }
