 package app.util;
 
 import static com.qotsa.jni.controller.WinampController.clearPlayList;
 import static com.qotsa.jni.controller.WinampController.getPlayListLength;
 import static com.qotsa.jni.controller.WinampController.nextTrack;
 import static com.qotsa.jni.controller.WinampController.pause;
 import static com.qotsa.jni.controller.WinampController.play;
 import static com.qotsa.jni.controller.WinampController.previousTrack;
 import static com.qotsa.jni.controller.WinampController.resume;
 import static com.qotsa.jni.controller.WinampController.run;
 import static com.qotsa.jni.controller.WinampController.stop;
 
 import java.io.IOException;
 
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.FloatControl;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.Mixer;
 import javax.sound.sampled.Port;
 
 import org.apache.log4j.Logger;
 
 import com.qotsa.exception.InvalidHandle;
 import com.qotsa.jni.controller.WinampController;
 
 public class WinampUtils {
 	private static final Logger log = LogUtils.getLogger(WinampUtils.class);
 	
 	public static final String PLAYER_ACTION_STOP = "stop";
 	public static final String PLAYER_ACTION_PAUSE = "pause";
 	public static final String PLAYER_ACTION_RESUME = "resume";
 	public static final String PLAYER_ACTION_PLAY = "play";
 	public static final String PLAYER_ACTION_RUN = "run";
 	public static final String PLAYER_ACTION_NEXT = "next";
 	public static final String PLAYER_ACTION_PREVIOUS = "previous";
 	
 	public static final String SYNCHRONIZE_MODE_WINAMP = "1";
 	public static final String SYNCHRONIZE_MODE_WEB = "2";
 
 	/**
 	 * do anything follow from command
 	 * 
 	 * @param event
 	 *            of the player action from form
 	 */
 	public static void playerControl(String event) {
 		log.debug("Enter playerControl : "+event);
 		
 		try {
 			
 			if (PLAYER_ACTION_RUN.equalsIgnoreCase(event)) {
 				run();
 			} else if (PLAYER_ACTION_PLAY.equalsIgnoreCase(event)) {
 				play();
 			} else if (PLAYER_ACTION_RESUME.equalsIgnoreCase(event)) {
 				resume();
 			} else if (PLAYER_ACTION_PAUSE.equalsIgnoreCase(event)) {
 				pause();
 			} else if (PLAYER_ACTION_STOP.equalsIgnoreCase(event)) {
 				stop();
 			} else if (PLAYER_ACTION_NEXT.equalsIgnoreCase(event)) {
 				nextTrack();
 			} else if (PLAYER_ACTION_PREVIOUS.equalsIgnoreCase(event)) {
 				previousTrack();
 			}
 
 		} catch (InvalidHandle ihe) {
 			log.error("Error in playerControl", ihe);
 		} catch (Exception e) {
 			log.error("Error in playerControl", e);
 		} finally {
 			log.debug("Quit playerControl");
 		}
 	}
 	
 	/**
 	 * append file to winamp playlist
 	 * @param fileName filename without path 
 	 */
 	public static void appendFileToPlaylist(String fileName) {
 		log.debug("Enter appendFileToPlaylist");
         try {
 //			WinampController.appendToPlayList(FileUtils.ABSOLUTEPATH_THE_GREAN_GANG_RADIO+name);
         	
         	//using command-line to avoid UTF-8 problem of WinampController.appendToPlayList 
         	StringBuilder command = new StringBuilder(); 
         	command.append("cmd /C ");
         	command.append("\"");
         	command.append("\"C:/Program Files (x86)/winamp/winamp.exe\"");
         	command.append(" /add ");
         	command.append("\"" + FileUtils.ABSOLUTEPATH_THE_GREAN_GANG_RADIO + fileName + "\"");
         	command.append("\"");
         	
         	log.debug(command.toString());
         	Process p = Runtime.getRuntime().exec(command.toString());
 //        	p.waitFor();
 		} catch (IOException e) {
 			log.error("Error in onTransfer",e);
 		} finally {
 			log.debug("Quit appendFileToPlaylist");
 		}
 	}
 	
 	/**
 	 * get playing file name from Winamp 
 	 * 
 	 * <BR/><BR />because,
 	 * {@link WinampController#getFileNamePlaying} return absolute path of playing file<BR />
 	 * 
 	 * @return only name of playing file
 	 */
 	public static String getFileNamePlaying() {
		log.debug("Enter getFileNamePlaying");
 		String fileName = null;
 		try {			
 			String fullPathName = WinampController.getFileNamePlaying();			
 			fileName = "".equals(fullPathName) ? "Add music to playlist first" : fullPathName.substring(fullPathName.lastIndexOf('\\')+1);
 //			log.debug(fileName + " is playing.");
 		} catch (InvalidHandle e) {
 			log.error("Error in getFileNamePlaying", e);
 		} finally {
			log.debug("Quit getFileNamePlaying");
 		}
 		return fileName;
 	}
 	
 	/**
 	 * get playlist from webpage and synchronize to winamp's playlist
 	 */
 	public static void synchronizePlaylist(String synchronizeMode) {
 
 	}
 
 	/**
 	 * check web's playlist length & winamp's playlist length for the way to
 	 * sync.
 	 * 
 	 * @return {@link #SYNCHRONIZE_MODE_WINAMP} or {@link #SYNCHRONIZE_MODE_WEB}
 	 * @throws InvalidHandle
 	 */
 	public static String synchronizeChecking() throws InvalidHandle {
 		int winampPlaylistLength = getPlayListLength();
 		int webPlaylistLength = 0; // TODO
 
 		return winampPlaylistLength > webPlaylistLength ? SYNCHRONIZE_MODE_WINAMP : SYNCHRONIZE_MODE_WEB;
 
 	}
 	
 	/**
 	 * clear all music in winamp playlist
 	 */
 	public static void clearWinampPlayList() {
 		log.debug("Enter clearWinampPlayList");
 		try {
 			clearPlayList();
 		} catch (InvalidHandle e) {
 			log.error("Error in clearWinampPlayList", e);
 		} finally {
 			log.debug("Quit clearWinampPlayList");
 		}
 	}
 	
 }
