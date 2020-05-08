 package mta13438;
 
 public  enum SOUNDS {
 	//Add sounds and path 
 	MENU_MUSIC("assets/Music/MenuBackground_1.wav"),
	DOOR("/HorrorGame/assets/soundEffects/door.wav"),
	FOOTSTEP_STONE("/HorrorGame/assets/soundEffects/footstep_stone.wav"),
	FOOTSTEP_WATER("/HorrorGame/assets/soundEffects/footstep_water.wav"),
	GUILLOTINE_TRAP("/HorrorGame/assets/soundEffects/Guillotine_trap.wav"),
	MONSTER1("/HorrorGame/assets/soundEffects/monster1.wav"),
	RAT("/HorrorGame/assets/soundEffects/Rat.wav"),
	ROTER_TRAP("/HorrorGame/assets/soundEffects/Roter_trap.wav"),
	WATERDROP1("/HorrorGame/assets/soundEffects/waterdrop1.wav"),
	WATERDROP2("/HorrorGame/assets/soundEffects/waterdrop2.wav"),
 	;
 	
 	//desc for description
 	private final String desc;
 	
 	//Constructor
 	SOUNDS(String description){
 		desc = description;
 	}
 	//Returns path
 	public String getPath(){
 		return desc;
 	}
 }
