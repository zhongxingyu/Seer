 package io.github.harryprotist.block;
 
 import org.bukkit.entity.Player;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.World;
 import org.bukkit.Location;
 
 import java.util.*;
 
 import io.github.harryprotist.Spell;
 
 public abstract class MemBlockFunction extends BlockFunction
 {
 	protected Map<Integer, Integer> mem;
 	protected List<Integer> rargv; // argv, without subbed values
 
 	public MemBlockFunction(ArrayList<Integer> a, Player c, Location l, 
 	Map<Integer, Integer> mem, ArrayList<Integer> script, int i) {
 
 		super(a, c, l);
 
 		this.mem = mem;
 
		rargv = script.subList(i, i + a.size() );
 		if (rargv == null) rargv = new ArrayList<Integer>();
 	}
 }
