 package se.darkbits.greengrappler;
 
 public class GlobalOptions {
 
 	public enum VibrationType {
 		SIMPLE, PULSATING,
 	}
 
 	public static interface AbstractVibrator {
 		public void vibrate(int aVibrateTime, VibrationType aVibrationTime);
 	}
 
 	public static AbstractVibrator mVibrator = null;
 	private static boolean mPaused = false;
 
 	public static boolean showTouchControls() {
 		return true;
 	}
 
 	public static boolean dialogueAtTop() {
 		return true;
 	}
 
 	public static boolean avoidHeroAtThumbs() {
 		return true;
 	}
 
 	public static void Vibrate(int aVibrateTime, VibrationType aVibrationType) {
 		if (mVibrator != null)
 			mVibrator.vibrate(aVibrateTime, aVibrationType);
 	}
 
 	public static void setPaused(boolean aPaused) {
 		mPaused = aPaused;
 	}
 
 	public static boolean getPaused() {
 		return mPaused;
 	}
 
 	public static boolean showFps() {
 		return false;
 	}
 	
 	public interface ExitCallback {
 		public abstract void exit();
 	}
 	
 	public static ExitCallback myExitCallback = null;
 	
 	public static void exit()
 	{
 		if(myExitCallback != null)
 		{
 			myExitCallback.exit();
 		}
 	}
 }
