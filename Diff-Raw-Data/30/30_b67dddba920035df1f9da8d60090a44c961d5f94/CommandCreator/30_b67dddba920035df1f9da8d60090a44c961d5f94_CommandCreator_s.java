 package com.example.digitallighterserver;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.example.lightdetector.ColorManager;
 
 import android.graphics.Bitmap;
 import android.graphics.Color;
 
 /**
  * This class creates a bunch of commands from given media.
  * 
  * @author Tomas
  * 
  */
 public class CommandCreator {
 	private ImageMapper imageMapper;
 	private ArrayList<ImageTimePair> buffer;
 	private boolean valid = true;
 	private int frameRate; // images per second
 	private int frameMs; // frame stays for frameMs milliseconds
 	private String ERROR_COMMAND = "0:#FFFFFF";
 	private long playTime;
 	private boolean firstCommandSend;
 	private boolean sendTimeStamp;
 	private int framesCounter;
 
 	/**
 	 * 
 	 * @param mediaPath
 	 *            relative path (to assets folder) to media to be played from
 	 * @param frameRate
 	 */
 	public CommandCreator(String mediaPath, int frameRate) {
 		try {
 			imageMapper = new ImageMapper(mediaPath);
 			this.frameRate = frameRate;
 			frameMs = (int) (1 / (double) frameRate * 1000);
 			reset();
 		} catch (IOException e) {
 			valid = false;
 			e.printStackTrace();
 		}
 	}
 
 	public void reset() {
 		playTime = System.currentTimeMillis() + Configuration.WAIT_BEFORE_PLAYING;
 		buffer = new ArrayList<ImageTimePair>();
 		firstCommandSend = false;
 		sendTimeStamp = true;
 		framesCounter = 0;
 	}
 
 	/**
 	 * This command must be invoke every time there is need for getting new commands
 	 * 
 	 * @param advanceFrames
 	 *            how many frames in advances should be processed into one command
 	 * @return milliseconds of how long this command will be played
 	 */
 	public int nextCommand(int advanceFrames) {
 		buffer.clear();
 		while (!imageMapper.isFinished() && advanceFrames > 0) {
 			// do until there are images left and we have load insufficient
 			// number of frames
 			buffer.add(new ImageTimePair(imageMapper.getNextFrame(), playTime + this.framesCounter * frameMs));
 			advanceFrames--;
 			this.framesCounter++;
 		}
 		if (firstCommandSend)
 			sendTimeStamp = false;
 		return buffer.size() * frameMs;
 	}
 
 	public String getCommand(int tileX, int tileY) {
 		String retval = new String();
 		if (buffer.size() == 0) {
 			retval = ERROR_COMMAND;
 		} else if (tileX >= buffer.get(0).image.getWidth() || tileY >= buffer.get(0).image.getHeight()) {
 			retval = ERROR_COMMAND;
 		} else { // ok
 			for (int i = 0; i < buffer.size(); i++) {
 				String color = ColorManager.getHexColor(buffer.get(i).image.getPixel(tileX, tileY));
 				if (i + 1 < buffer.size() // there is still something to load
 						&& ColorManager.getHexColor(buffer.get(i + 1).image.getPixel(tileX, tileY)).equals(
 								color)) { // and it is the same
 					// color!!!
 				} else { // flush the command
 					if (retval.length() != 0) { // not first bunch command so
 												// add deliminator
 						retval += "|";
 					}
					retval += CommandCreator.createCommand(buffer.get(i).timestamp, color);
 				}
 			}
 			firstCommandSend = true;
 		}
 		return retval;
 	}
 
 	public boolean isFinished() {
 		return (valid) ? imageMapper.isFinished() : true;
 	}

 	static public String createCommand(long atTime, String message) {
		return addTime(message, atTime) + "\n";
 	}
 
	static public String addTime(String message, long time) {
 		String retval = new String(Long.toString(time));
 		retval += ":" + message;
 		return retval;
 	}
 
 	private class ImageTimePair {
 		public Bitmap image;
 		public long timestamp;
 
 		ImageTimePair(Bitmap image, long timestamp) {
 			this.image = image;
 			this.timestamp = timestamp;
 		}
 	}
 }
