 package mta13438;
 
 public  enum SOUNDS {
 	//Add sounds and path 
 	MENU_MUSIC("assets/Music/MenuBackground_1.wav"),
	DOOR("/assets/soundEffects/door.wav"),
	FOOTSTEP_STONE("/assets/soundEffects/footstep_stone.wav"),
	FOOTSTEP_WATER("/assets/soundEffects/footstep_water.wav"),
	GUILLOTINE_TRAP("/assets/soundEffects/Guillotine_trap.wav"),
	MONSTER1("/assets/soundEffects/monster1.wav"),
	RAT("/assets/soundEffects/Rat.wav"),
	ROTER_TRAP("/assets/soundEffects/Roter_trap.wav"),
	WATERDROP1("/assets/soundEffects/waterdrop1.wav"),
	WATERDROP2("/assets/soundEffects/waterdrop2.wav"),
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
