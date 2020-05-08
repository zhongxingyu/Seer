 package com.example.digitallighter;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import android.graphics.Color;
 import android.view.View;
 
 public class ClientPlayer {
 
 	View background;
 	public static long timeOffset = 0;
 
 	// COMMAND
 	boolean isPlaying = false;
 	BlockingQueue<String> playingQueue;
 
 	public ClientPlayer(View background) {
 		this.background = background;
 		playingQueue = new LinkedBlockingQueue<String>();
 	}
 
 	// ========================================================================================================
 	// ADD ONE COMMAND. COMMAND FORMAT: (color(hex):duration(msec)) EXAMPLE: ("#ff00ff:5")
 	// FOR MULTIPLE COMMANDS CONCATENATE WITH '|' EXAMPLE: ("#ff00ff:5|#ffabcf:7")
 	// ========================================================================================================
 
 	public void addCommand(String command) {
 
 		if (command == null || command.length() == 0)
 			return;
 
 		if (command.equals("CLEAR")) {
 			playingQueue.clear();
 			background.setBackgroundColor(Color.BLACK);
 			return;
 		}
 
 		// GET MULTIPLE COMMANDS AND PUT THEM IN QUEUE
 		
 		if (command.contains("|")) {
 			String[] commands = command.split("\\|");
 			for (String s : commands) {
 				playingQueue.add(s);
 			}
 		} else {
 			playingQueue.add(command);
 		}
 
 		if (!isPlaying) {
 			isPlaying = true;
 			new PlayThread().start();
 		}
 	}
 
 	class PlayThread extends Thread {
 
 		@Override
 		public void run() {
 			while (!playingQueue.isEmpty()) {

 				// GET ONE COMMAND INFO AND REMOVE IT FROM QUEUE
 				String[] parts = playingQueue.poll().split(":");
 				long time = Long.parseLong(parts[0]);
 				final int color = Color.parseColor(parts[1]);
 
 				while (System.currentTimeMillis() + timeOffset < time) {
 
 				}
 
 				// SET PROPER BACKGROUND DEFINED BY COMMAND
 				background.post(new Runnable() {
 					@Override
 					public void run() {
 						background.setBackgroundColor(color);
 					}
 				});
 
 			}
 			isPlaying = false;
 		}
 
 	}
 
 }
