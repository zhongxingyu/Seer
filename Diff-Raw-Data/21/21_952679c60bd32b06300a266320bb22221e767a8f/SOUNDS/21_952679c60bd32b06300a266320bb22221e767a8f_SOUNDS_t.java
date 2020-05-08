 package mta13438;
 
 public  enum SOUNDS {
	HEARTBEAT("assets/heartbeat.wav"), 
	FOOTSTEPS("assets/footsteps.wav"),
	BREATHING("assets/breathing.wav"),
	TRAPSOUND("assets/trapsound.wav"),
	TRAPKILLSOUND("assets/trapkillsound.wav");
	
	//desc for description
	private final String desc;
	
	SOUNDS(String description){
		desc = description;
	}
	
	public String getPath(){
		return desc;
	}
 }
