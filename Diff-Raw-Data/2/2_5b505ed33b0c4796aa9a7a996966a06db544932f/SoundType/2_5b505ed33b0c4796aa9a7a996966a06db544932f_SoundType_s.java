 package com.secondhand.model.resource;
 	
 public enum SoundType {
 		POWERUP_SOUND("sfx/powerup.wav"),
 		BEEP_SOUND("sfx/beep.wav"),
 		GROW_SOUND("sfx/grow.wav"),
 		OBSTACLE_COLLISION_SOUND("sfx/obstacle_collision.wav"),
 		PLAYER_KILLED_SOUND("sfx/death.wav"),
 		WIN_SOUND("sfx/win.wav"), 
 		HIGHSCORE_SOUND("sfx/high_score_entry.wav");
 		
 		private String path;
		private SoundType(String path){
 			this.path = path;
 		}
 		
 		public String getPath(){
 			return this.path;
 		}
 		
 	}
 
