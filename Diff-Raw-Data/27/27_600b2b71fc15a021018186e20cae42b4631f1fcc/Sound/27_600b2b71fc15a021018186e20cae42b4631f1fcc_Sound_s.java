 package com.cg.trashman.object;
 
 import java.applet.Applet;
 import java.applet.AudioClip;
 
 public class Sound {
 	private AudioClip a;
 	private boolean isPlay;
 	public Sound(String url) {
 		a = Applet.newAudioClip(this.getClass().getResource(url));
 		isPlay = false;
 	}
 	public void play() {
 		if(!isPlay) {
 			isPlay = true;
 			a.play();
 		}
 	}
 }
