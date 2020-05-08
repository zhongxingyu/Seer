 package app.util;
 
 import static com.qotsa.jni.controller.WinampController.getPlayListLength;
 import static com.qotsa.jni.controller.WinampController.pause;
 import static com.qotsa.jni.controller.WinampController.play;
 import static com.qotsa.jni.controller.WinampController.resume;
 import static com.qotsa.jni.controller.WinampController.stop;
 
 import org.apache.log4j.Logger;
 
 import com.qotsa.exception.InvalidHandle;
 
 public class WinampUtils {
 	private static final Logger log = LogUtils.getLogger(WinampUtils.class);
 	
 	private static final String ACTION_STOP = "stop";
 	private static final String ACTION_PAUSE = "pause";
 	private static final String ACTION_RESUME = "resume";
 	private static final String ACTION_PLAY = "play";
 	public static final String SYNCHRONIZE_MODE_WINAMP = "1";
 	public static final String SYNCHRONIZE_MODE_WEB = "2";
 
 	/**
 	 * do anything follow from command
 	 * 
 	 * @param event
 	 *            of the action from form
 	 * @return String of status of winamp
 	 */
 	public static String playerControl(String event) {
 		log.debug("Enter playerControl : "+event);
 		
 		try {
 			if (ACTION_PLAY.equalsIgnoreCase(event)) {
 				play();
 				return "playing";
 			} else if (ACTION_RESUME.equalsIgnoreCase(event)) {
 				resume();
 				return "resumed";
 			} else if (ACTION_PAUSE.equalsIgnoreCase(event)) {
 				pause();
 				return "pause";
 			} else if (ACTION_STOP.equalsIgnoreCase(event)) {
 				stop();
 				return "stopped";
 			}
 
 		} catch (InvalidHandle ihe) {
 			ihe.printStackTrace();
		} finally {
			log.debug("Quit playerControl");
 		}
 		return null;
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
 
 }
