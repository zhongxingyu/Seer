 package org.ggj2013;
 
 public class Enemy extends Entity {
 
 	enum Size {
 		SMALL, MEDIUM, BIG
 	}
 
 	final Size size;
 
 	public Enemy(String name, Size size) {
 		super(name);
 		this.size = size;
 	}
 
 	public String getSoundName() {
 		switch (size) {
 		case BIG:
 			return SoundPackStandard.ENEMY_BIG;
 		case MEDIUM:
 			return SoundPackStandard.ENEMY_MEDIUM;
 		case SMALL:
 			return SoundPackStandard.ENEMY_SMALL;
 		default:
 			return "";
 		}
 	}
 
	@Override
	public float volume() {
		return 1;
	}

 }
