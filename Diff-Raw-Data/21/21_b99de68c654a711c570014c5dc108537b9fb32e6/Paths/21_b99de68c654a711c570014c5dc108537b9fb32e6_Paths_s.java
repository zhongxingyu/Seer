 package com.battleguard.scripts.f2prunecrafter.data;
 
 import org.powerbot.script.methods.MethodContext;
 import org.powerbot.script.wrappers.Tile;
 import org.powerbot.script.wrappers.TilePath;
 
 public enum Paths {
 	AIR(new Tile(3184, 3435, 0), new Tile(3185, 3432, 0), new Tile(3181, 3430, 0), 
 			new Tile(3177, 3429, 0), new Tile(3172, 3429, 0), new Tile(3169, 3428, 0), 
 			new Tile(3166, 3426, 0), new Tile(3160, 3420, 0), new Tile(3157, 3419, 0), 
 			new Tile(3153, 3417, 0), new Tile(3148, 3417, 0), new Tile(3144, 3413, 0), 
 			new Tile(3141, 3410, 0), new Tile(3138, 3408, 0), new Tile(3134, 3406, 0), 
 			new Tile(3131, 3403, 0), new Tile(3129, 3404, 0)),
 	MIND(new Tile(2945, 3369, 0), new Tile(2946, 3375, 0), new Tile(2952, 3379, 0), 
 			new Tile(2958, 3381, 0), new Tile(2964, 3384, 0), new Tile(2964, 3390, 0), 
 			new Tile(2965, 3396, 0), new Tile(2965, 3402, 0), new Tile(2965, 3408, 0), 
 			new Tile(2964, 3414, 0), new Tile(2958, 3414, 0), new Tile(2956, 3420, 0), 
 			new Tile(2954, 3426, 0), new Tile(2951, 3432, 0), new Tile(2950, 3438, 0), 
 			new Tile(2949, 3444, 0), new Tile(2949, 3450, 0), new Tile(2949, 3456, 0), 
 			new Tile(2949, 3462, 0), new Tile(2952, 3468, 0), new Tile(2957, 3473, 0), 
 			new Tile(2963, 3476, 0), new Tile(2968, 3481, 0), new Tile(2972, 3486, 0), 
 			new Tile(2976, 3492, 0), new Tile(2977, 3498, 0), new Tile(2977, 3504, 0), 
 			new Tile(2979, 3510, 0), new Tile(2980, 3514, 0)),
 	WATER(new Tile(0,0,0)),
 	EARTH(new Tile(0,0,0)),
 	FIRE(new Tile(3349, 3238, 0), new Tile(3343, 3236, 0), new Tile(3337, 3235, 0), 
 			new Tile(3331, 3233, 0), new Tile(3325, 3233, 0), new Tile(3320, 3238, 0), 
 			new Tile(3315, 3242, 0), new Tile(3313, 3248, 0), new Tile(3311, 3254, 0)),
 	BODY(new Tile(0,0,0));			
 	
 	private final Tile[] tiles;
 	
 	Paths(Tile... tiles) {			
 		this.tiles = tiles;
 	}
 	
 	public TilePath toBank(MethodContext ctx) {
 		return new TilePath(ctx, tiles).reverse();
 	}
 	
	public TilePath toAlter() {
 		return new TilePath(ctx, tiles);
 	}	
 }
