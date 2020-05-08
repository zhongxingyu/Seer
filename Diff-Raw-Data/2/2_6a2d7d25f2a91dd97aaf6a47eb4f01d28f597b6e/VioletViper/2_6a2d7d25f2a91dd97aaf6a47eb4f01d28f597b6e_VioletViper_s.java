 package com.avona.games.towerdefence.enemy;
 
 import com.avona.games.towerdefence.RGB;
 import com.avona.games.towerdefence.V2;
 import com.avona.games.towerdefence.world.World;
 
 public class VioletViper extends Enemy {
 
 	private static final long serialVersionUID = 93877621275472018L;
 
 	public VioletViper(World world, V2 location, int level) {
 		super(world, location, level);
 		this.life = new RGB(50 * level + 10, 0, 50 * level + 10);
		this.life = new RGB(50 * level + 10, 0, 50 * level + 10);
 	}
 
 }
