 package core;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 
 import map.DungeonNavigator;
 
 import javazoom.jl.player.Player;
 
 public class Sound implements Runnable, FileLink{
 
 	private static Sound sound;
 	
 	private File musicFile;
 	private Player musicPlayer;
 	private boolean playMusic;
 	private int currentMusic = -1;
 	
 	private File atmoFile;
 	private Player atmoPlayer;
 	private boolean playAtmo;
 	private int currentAtmo = -1;
 	
 	private File soundFile, soundFile2;
 	private Player soundPlayer, soundPlayer2;
 	
 	
 	private Sound(){
 		
 	}
 	
 	public void run(){
 	
 		if(musicPlayer == null)
 			playMusic();
 			
 		if(atmoPlayer == null && GameManager.getInstance().getIngame())
 			playAtmo();
 			
 		if(GameManager.getInstance().mapLoaded)	
 			checkMusicSound();
 	
 	}
 	
 	private void checkMusicSound(){
 		int musicID = -1;
 		int atmoID = -1;
 		
 		//checkMusic
 		if(GameManager.getInstance().getIngame()){
 			if(GameManager.getInstance().overWorld)
 				atmoID = 1;
 			if(GameManager.getInstance().dungeon)
 				atmoID = 2;
 			
 			
 			if(game.objects.Player.getInstance().getAlive() && !GameManager.getInstance().win && !GameManager.getInstance().lose){	
 				if(GameManager.getInstance().overWorld)
 					musicID = 1;
 				if(GameManager.getInstance().dungeon){
 					if(DungeonNavigator.getInstance().getXMap() == 2)
 						musicID = 3;
 					else
 						musicID = 2;
 				}	
 			} else if(GameManager.getInstance().lose)
 				musicID = 4;
 		} else if(GameManager.getInstance().getMenu())
 			musicID = 0;
 		
 		if(musicID != currentMusic && musicPlayer != null){
 			musicPlayer.close();
 			musicPlayer = null;
 		}
 			
 		if(atmoID != currentAtmo && GameManager.getInstance().getIngame() && atmoPlayer != null){
 			atmoPlayer.close();
 			atmoPlayer = null;
 		}
 			
 		
 	}
 
 	public void playMusic(){
 		
 		
 		if(GameManager.getInstance().getIngame()){
 	
 			if(game.objects.Player.getInstance().getAlive() && !GameManager.getInstance().win && !GameManager.getInstance().lose){
 			
 				if(GameManager.getInstance().overWorld){
 					musicFile = MFoverWorld;
 					currentMusic = 1;
 				}
 					
 				
 				if(GameManager.getInstance().dungeon){
 					
 					if(DungeonNavigator.getInstance().getXMap() == 2){
 						musicFile = MFboss;
 						currentMusic = 3;
 					}
 					else{
 						musicFile = MFdungeon;
 						currentMusic = 2;
 					}
 						
 				
 				} 
 				
 			} else if (GameManager.getInstance().lose){
 				musicFile = MFloseState;
 				currentMusic = 4;
 			}
 
 		} else if(GameManager.getInstance().getMenu()){
 			musicFile = MFmainMenu;
 			currentMusic = 0;
 		}
 		
 		
 		if(musicFile != null){
 			try {
 	            FileInputStream fis = new FileInputStream(musicFile);
 	            BufferedInputStream bis = new BufferedInputStream(fis);
 	            musicPlayer = new Player(bis);
 	        }
 	        catch (Exception e) {
 	            System.out.println("Problem playing file " + musicFile);
 	            System.out.println(e);
 	        }
 	        new Thread() {
 	            public void run() {
 	                try { 
 	                	musicPlayer.play();
 	                	musicPlayer = null;
 	                	}
 	                catch (Exception e) { System.out.println(e); }
 	            }
 	        }.start();
 		}
 		 
 	       
 	}
 	
 	public void playAtmo(){
 		
 		if(GameManager.getInstance().overWorld){
 			atmoFile = SFAtmoOverWorld;
 			currentAtmo = 1;
 		}
 			
 		if(GameManager.getInstance().dungeon){
 			atmoFile = SFAtmoDungeon;
 			currentAtmo = 2;
 		}
 			
 		if(atmoFile != null){
 			try {
 	            FileInputStream fis = new FileInputStream(atmoFile);
 	            BufferedInputStream bis = new BufferedInputStream(fis);
 	            atmoPlayer = new Player(bis);
 	        }
 	        catch (Exception e) {
 	            System.out.println("Problem playing file " + atmoFile);
 	            System.out.println(e);
 	        }
 	        new Thread() {
 	            public void run() {
 	                try {
 	                	atmoPlayer.play();
 	                	atmoPlayer = null;
 	                	
 	                	}
 	                catch (Exception e) { System.out.println(e); }
 	            }
 	        }.start();
 		}
 		 
 	}
 	
 	public void playSound(int sound){
 		File tmpFile = null;
 		
 		switch(sound){
 		case 0: tmpFile = SFgetAchieve; break;
 		case 1: tmpFile = SFpunchHit; break;
 		case 3: tmpFile = SFmagicCast; break;
 		case 10:tmpFile = SFgetCoin; break;
 		}
 		
 		if(soundPlayer == null){
 			soundFile = tmpFile;
 			playSoundNow(soundFile, 1);
 		}
 			
 		else if(soundPlayer == null){
 			soundFile2 = tmpFile;
 			playSoundNow(soundFile2, 2);
 		}
 			
 		
 	}
 	
 	private void playSoundNow(File soundFile, int sound){
 		 try {
 			 	if(sound == 1){
 			 		FileInputStream fis = new FileInputStream(soundFile);
 		            BufferedInputStream bis = new BufferedInputStream(fis);
 		            soundPlayer = new Player(bis);
 			 	} else if(sound == 2){
 			 		FileInputStream fis2 = new FileInputStream(soundFile);
 		            BufferedInputStream bis2 = new BufferedInputStream(fis2);
		            soundPlayer = new Player(bis2);
 			 	}
 	            
 	        }
 	        catch (Exception e) {
 	            System.out.println("Problem playing file " + musicFile);
 	            System.out.println(e);
 	        }
 		 
 		 if(sound == 1){
 			 
 			 new Thread() {
 		            public void run() {
 		                try {
 		                	soundPlayer.play();
 		                	soundPlayer = null;
 		                	}
 		                catch (Exception e) { System.out.println(e); }
 		            }
 		        }.start();
 		        
 		 } else if (sound == 2){
 			 
 			 new Thread() {
 		            public void run() {
 		                try {
 		                	soundPlayer2.play();
 		                	soundPlayer2 = null;
 		                	}
 		                catch (Exception e) { System.out.println(e); }
 		            }
 		        }.start();
 		 }
 	       
 	}
 	
 
 	public static Sound getInstance(){
 		if(sound == null)
 			sound = new Sound();
 		return sound;
 
 	}
 	
 }
