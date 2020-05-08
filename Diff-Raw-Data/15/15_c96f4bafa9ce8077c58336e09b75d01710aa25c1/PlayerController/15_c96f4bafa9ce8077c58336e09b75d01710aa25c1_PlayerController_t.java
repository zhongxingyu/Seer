 package br.com.logica;
 

 import android.media.MediaPlayer;
 
 public class PlayerController {
 	
 	public static MediaPlayer Player;
 	public static int musicIndex = 0;
 
 	public static MediaPlayer getPlayer() {
 		return Player;
 	}
 
 	public static void setPlayer(MediaPlayer player) {
 		Player = player;
 	}
 	
 	
 
 }
