 package org.cxt.lt.util;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 
 import org.cxt.lt.LtRobot;
 import org.cxt.lt.OffsetType;
 import org.cxt.lt.Util;
 
 public class UIFlagManager {
 
 	private static final Map<Integer, FlagWrap> sMap = new HashMap<Integer, FlagWrap>();
 
 	public static final int LOGIN_WAIT_FLAG = 1;
 	public static final int SCRIPT_LIB = 2;
 	public static final int COLLECTION = 3;
 	public static final int OPEN_SCRIPT = 4;
 	public static final int DIALOG1 = 5;
 	public static final int DIALOG2 = 6;
 	public static final int DIALOG3 = 7;
 	public static final int DIALOG_MODE_TIPS = 8;
 	public static final int SELECTE_SCRIPT = 9;
 	public static final int S_WORD_START = 10;
 	public static final int SCRIPT_SD_GUNDAM = 11;
 	public static final int SELECTE_SCRIPT2 = 12;
 	public static final int SCRIPT_DISPLAY = 13;
 	public static final int AUTO_LOGIN_PROTOCOL = 14;
 	public static final int SCRIPT_SETTING_NEEDING = 15;
 	public static final int TIPS = 16;
 	public static final int OPEN_SCRIPT_PROTECTOR = 17;
 	public static final int TIPS2 = 18;
 	public static final int AUTO_LOGIN_PROTOCOL2 = 19;
 	public static final int MODE_TIPS = 20;
 	public static final int SD_GUNDAM_EXE_PATH = 21;
 	public static final int MY_COLLECTION_DEFAULT_SCRIPT_OPEN = 22;
 	public static final int WAIT_MY_COLLECTION_UI = 23;
 	public static final int LOGIN_UI = 24;
 
 	static {
 
 		sMap.put(LOGIN_WAIT_FLAG, new FlagWrap(40, 115, 90, 130,
 				OffsetType.SIMPLAY_LOGIN_WINDOW_OFFSET, "LOGIN_WAIT_FLAG"));
 
 		sMap.put(SCRIPT_LIB, new FlagWrap(110, 35, 162, 98,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "SCRIPT_LIB"));
 
 		sMap.put(COLLECTION, new FlagWrap(34, 239, 76, 259,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "COLLECTION"));
 
 		sMap.put(OPEN_SCRIPT, new FlagWrap(633, 142, 682, 170,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "OPEN_SCRIPT"));
 
 		sMap.put(DIALOG1, new FlagWrap(226, 370, 399, 385,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "DIALOG1"));
 
 		sMap.put(DIALOG2, new FlagWrap(571, 411, 603, 425,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "DIALOG2"));
 
 		sMap.put(DIALOG3, new FlagWrap(323, 355, 464, 378,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "DIALOG3"));
 
 		sMap.put(DIALOG_MODE_TIPS, new FlagWrap(293, 276, 530, 294,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "DIALOG_MODE_TIPS"));
 
 		sMap.put(SELECTE_SCRIPT, new FlagWrap(30, 130, 83, 149,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "SELECTE_SCRIPT"));
 
 		sMap.put(SELECTE_SCRIPT2, new FlagWrap(33, 126, 50, 146,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "SELECTE_SCRIPT2"));
 
 		sMap.put(S_WORD_START, new FlagWrap(550, 159, 565, 175,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "S_WORD_START"));
 
 		sMap.put(SCRIPT_SD_GUNDAM, new FlagWrap(337, 226, 394, 246,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "SCRIPT_SD_GUNDAM"));
 
 		sMap.put(SCRIPT_DISPLAY, new FlagWrap(260, 183, 300, 196,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "SCRIPT_DISPLAY"));
 
 		sMap.put(AUTO_LOGIN_PROTOCOL, new FlagWrap(450, 422, 501, 429,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "AUTO_LOGIN_PROTOCOL"));
 
 		sMap.put(SCRIPT_SETTING_NEEDING, new FlagWrap(544, 388, 576, 394,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "SCRIPT_SETTING_NEEDING"));
 
 		sMap.put(TIPS, new FlagWrap(325, 357, 454, 375,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "TIPS"));
 
 		sMap.put(OPEN_SCRIPT_PROTECTOR, new FlagWrap(226, 405, 445, 437,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "OPEN_SCRIPT_PROTECTOR"));
 
 		sMap.put(TIPS2,
 				new FlagWrap(332, 312, 447, 328,
 						OffsetType.SIMPLAY_RED_KILLER_BACKGROUND_SCRIPT_WINDOW,
 						"TIPS2"));
 
 		sMap.put(AUTO_LOGIN_PROTOCOL2, new FlagWrap(464, 381, 497, 387,
 				OffsetType.SIMPLAY_RED_KILLER_BACKGROUND_SCRIPT_WINDOW,
 				"AUTO_LOGIN_PROTOCOL2"));
 
 		sMap.put(MODE_TIPS, new FlagWrap(425, 291, 459, 296,
 				OffsetType.SIMPLAY_RED_KILLER_BACKGROUND_SCRIPT_WINDOW,
 				"MODE_TIPS"));
 
 		sMap.put(SD_GUNDAM_EXE_PATH, new FlagWrap(540, 178, 591, 186,
 				OffsetType.SIMPLAY_RED_KILLER_BACKGROUND_SCRIPT_WINDOW,
 				"SD_GUNDAM_EXE_PATH"));
 
 		sMap.put(MY_COLLECTION_DEFAULT_SCRIPT_OPEN, new FlagWrap(495, 331, 522,
 				343, OffsetType.SIMPLAY_MAIN_WINDOW,
 				"MY_COLLECTION_DEFAULT_SCRIPT_OPEN"));
 
		sMap.put(WAIT_MY_COLLECTION_UI, new FlagWrap(441, 295, 546, 320,
 				OffsetType.SIMPLAY_MAIN_WINDOW, "WAIT_MY_COLLECTION_UI"));
 
 		sMap.put(LOGIN_UI, new FlagWrap(328, 10, 343, 19,
 				OffsetType.SIMPLAY_LOGIN_WINDOW_OFFSET, "LOGIN_UI"));
 	}
 
 	public static FlagWrap getFlagWrap(int aFlag) {
 		return sMap.get(aFlag);
 	}
 
 	public interface Callback {
 		public void onDetectSuccess(int aFlag);
 
 		public void onDetectFail();
 	}
 
 	public static void invorkDetect(int[] aFlagList,
 			UIFlagManager.Callback aCallback) {
 
 		int count = 0;
 		boolean isDetected = false;
 		int detectedFlag = -1;
 
 		do {
 
 			for (int flag : aFlagList) {
 
 				FlagWrap flagWrap = UIFlagManager.getFlagWrap(flag);
 				LT.assertTrue(null != flagWrap, "flag:" + flag);
 
 				System.out.println("try detect " + flagWrap.getFlagKey());
 
 				BufferedImage flagImage = LtRobot.getInstance().screenShot(
 						flagWrap);
 				BufferedImage shotImage = UIFlagManager.getImage(flagWrap
 						.getFlagKey());
 
 //				if (Util.compareImageBinary(flagImage, shotImage)) {
 
 				if (Util.compareImage(flagImage, shotImage)) {
 				
 					isDetected = true;
 					detectedFlag = flag;
 
 					System.out.println(flagWrap.getFlagKey()
 							+ " detect success.");
 
 					LtRobot.getInstance().showShotSuccessRect(flagWrap);
 					LtRobot.getInstance().delay(1000);
 					LtRobot.getInstance().hideRect();
 
 					break;
 
 				}
 
 			}// end of for
 
 			count++;
 
 			if (isDetected) {
 
 				if (null != aCallback) {
 					aCallback.onDetectSuccess(detectedFlag);
 				}
 
 				break;
 
 			} else {
 
 				if (ConfigManager.getInt("TIME_OUT_SECOND") < count) {
 
 					if (null != aCallback) {
 						aCallback.onDetectFail();
 					}
 				}
 
 			}
 
 			LtRobot.getInstance().delay(1000);
 
 		} while (true);
 	}
 
 	public static BufferedImage getImage(String aFlagName) {
 		LT.assertTrue(null != aFlagName);
 
 		File file = new File(ConfigManager.getString("UI_WAIT_FLAG_FOLDER"),
 				aFlagName + ".png");
 
 		System.out.println("load image:" + file.getAbsolutePath());
 
 		LT.assertTrue(file.exists());
 
 		BufferedImage ret = null;
 
 		try {
 			ret = ImageIO.read(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 			LT.assertTrue(false);
 		}
 
 		return ret;
 
 	}
 
 	public static class FlagWrap {
 		private int mLeft;
 		private int mTop;
 		private int mRight;
 		private int mBottom;
 
 		private int mOffsetType;
 
 		private String mFlagKey;
 
 		public FlagWrap(int aLeft, int aTop, int aRight, int aBottom,
 				int aOffsetType, String aFlagKey) {
 			super();
 			mLeft = aLeft;
 			mTop = aTop;
 			mRight = aRight;
 			mBottom = aBottom;
 			mOffsetType = aOffsetType;
 			mFlagKey = aFlagKey;
 		}
 
 		private int getOffsetLeft() {
 			int ret = 0;
 
 			switch (this.getOffsetType()) {
 			case OffsetType.NO_OFFSET: {
 
 			}
 				break;
 
 			case OffsetType.SIMPLAY_LOGIN_WINDOW_OFFSET: {
 				ret = LtRobot.getLoginUILeftTopOffset().x;
 			}
 				break;
 			case OffsetType.SIMPLAY_MAIN_WINDOW: {
 				ret = LtRobot.getLeftTopOffset().x;
 			}
 				break;
 
 			case OffsetType.SIMPLAY_RED_KILLER_BACKGROUND_SCRIPT_WINDOW: {
 				ret = LtRobot.getLeftTopScriptUIOffset().x;
 			}
 
 				break;
 
 			default: {
 				LT.assertTrue(false);
 			}
 				break;
 			}
 
 			return ret;
 		}
 
 		private int getOffsetTop() {
 			int ret = 0;
 
 			switch (this.getOffsetType()) {
 			case OffsetType.NO_OFFSET: {
 
 			}
 				break;
 
 			case OffsetType.SIMPLAY_LOGIN_WINDOW_OFFSET: {
 				ret = LtRobot.getLoginUILeftTopOffset().y;
 			}
 				break;
 
 			case OffsetType.SIMPLAY_MAIN_WINDOW: {
 				ret = LtRobot.getLeftTopOffset().y;
 			}
 				break;
 
 			case OffsetType.SIMPLAY_RED_KILLER_BACKGROUND_SCRIPT_WINDOW: {
 				ret = LtRobot.getLeftTopScriptUIOffset().y;
 			}
 				break;
 
 			default: {
 				LT.assertTrue(false);
 			}
 				break;
 			}
 
 			return ret;
 		}
 
 		public int getOriginLeft() {
 			return this.mLeft;
 		}
 
 		public int getOriginRight() {
 			return this.mRight;
 		}
 
 		public int getOriginTop() {
 			return this.mTop;
 		}
 
 		public int getOriginBottom() {
 			return this.mBottom;
 		}
 
 		public int getLeft() {
 			return mLeft + this.getOffsetLeft();
 		}
 
 		public int getTop() {
 			return mTop + this.getOffsetTop();
 		}
 
 		public int getRight() {
 			return mRight + this.getOffsetLeft();
 		}
 
 		public int getBottom() {
 			return mBottom + this.getOffsetTop();
 		}
 
 		private int getOffsetType() {
 			return mOffsetType;
 		}
 
 		public String getFlagKey() {
 			return mFlagKey;
 		}
 
 		public String toString() {
 
 			return "[" + this.getLeft() + "," + this.getTop() + ","
 					+ this.getRight() + "," + this.getBottom() + "]";
 
 		}
 
 	}
 
 }
