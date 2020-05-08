 package mta13438;
 
 public  enum SOUNDS {
 	//Add sounds and path 
 	MENU_MUSIC("assets/Music/MenuBackground_1.wav"), //will not play ?
 	CELL_DOOR("assets/soundEffects/Cell door open forlbig.wav"),
 	CHAIN_01("assets/soundEffects/chain01.wav"),
 	CHAIN_02("assets/soundEffects/chain02.wav"),
 	CHAIN_03("assets/soundEffects/chain03.wav"),
 	CHAIN_04("assets/soundEffects/chain04.wav"),
 	CHAIN_05("assets/soundEffects/chain05.wav"),
 	CHAIN_06("assets/soundEffects/chain06.wav"),
 	CHAIN_07("assets/soundEffects/chain07.wav"),
 	CHAIN_08("assets/soundEffects/chain08.wav"),
 	CHAIN_09("assets/soundEffects/chain09.wav"),
 	CHAIN_10("assets/soundEffects/chain10.wav"),
 	FOOTSTEP_STONE("assets/soundEffects/footstep_stone.wav"),
 	FOOTSTEP_STONE_02("assets/soundEffects/footstep_stone01.wav"),
 	FOOTSTEP_STONE_03("assets/soundEffects/footstep_stone01.wav"),
 	FOOTSTEP_STONE_04("assets/soundEffects/footstep_stone01.wav"),
 	FOOTSTEP_WATER("assets/soundEffects/footstep_water.wav"),
	DOOR("assets/soundEffects/doorup" +
			".wav"),
	MONSTER1("assets/soundEffects/monster1.wav"),
 	DOOR_CLOSE("assets/soundEffects/forlblig dr luk.wav"),
 	DOOR_OPEN("assets/soundEffects/forlblig dr ben.wav"),
 	RAT_01("assets/soundEffects/forlbelig rotte.wav"),
 	RAT_02("assets/soundEffects/Rat2.wav"),
 	SCARE_01("assets/soundEffects/forlbig scare.wav"),
 	HANDS_ON_WALL_01("assets/soundEffects/handsonwall01.wav"),
 	HANDS_ON_WALL_02("assets/soundEffects/handsonwall02.wav"),
 	HANDS_ON_WALL_03("assets/soundEffects/handsonwall03.wav"),
 	KEYCHAIN_01("assets/soundEffects/keychain01.wav"),
 	MAN_EATEN_BY_WALRUS("assets/soundEffects/ManEatenByWalrus.wav"),
 	MONSTER_CELL_01("assets/soundEffects/tempcell.wav"),
 	MONSTER_CELL_02("assets/soundEffects/monster cell.wav"),
 	WATER_DROP_01("assets/soundEffects/waterdrop01.wav"),
 	WATER_DROP_02("assets/soundEffects/waterdrop02.wav"),
 	HEARTBEAT("assets/soundEffects/Heart.wav"), 
 	TRAP_01("assets/soundEffects/trapsound.wav"),
 	TRAP_02("assets/soundEffects/trap2.wav"),
 	TRAP_ROTER("assets/soundEffects/trapsound.wav"),
 	TRAP_KILL("assets/soundEffects/.wav") //is still missing
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
