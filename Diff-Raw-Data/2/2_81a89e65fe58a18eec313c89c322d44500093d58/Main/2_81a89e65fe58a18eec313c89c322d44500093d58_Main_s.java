 package org.cxt.lt;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.cxt.lt.util.ConfigManager;
 import org.cxt.lt.util.LT;
 import org.cxt.lt.util.SecureConfigManager;
 import org.cxt.lt.util.UIFlagManager;
 import org.cxt.lt.util.UIFlagManager.FlagWrap;
 
 import com.sun.org.apache.xpath.internal.operations.Lte;
 
 public class Main {
 
 	private static void setCopyBoardText(String aText) {
 		Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
 		setClipboardText(sysc, aText);
 
 	}
 
 	private static void setClipboardText(Clipboard clip, String writeMe) {
 		Transferable tText = new StringSelection(writeMe);
 		clip.setContents(tText, null);
 	}
 
 	private static void waitScript() {
 		BufferedImage greenButtonFlagImage = UIFlagManager
 				.getImage("OPEN_SCRIPT_GREEN_BUTTON2");
 		LT.assertTrue(null != greenButtonFlagImage);
 
 		BufferedImage scriptFlagImage = UIFlagManager
 				.getImage("SCRIPT_OF_RED_KILLER_BACKGROUND");
 		BufferedImage scriptFlagImage2 = UIFlagManager
 				.getImage("SCRIPT_OF_RED_KILLER_BACKGROUND_CLICKED" + "");
 
 		LT.assertTrue(null != scriptFlagImage);
 
 		// int greenButtonLeft = 688;
 		// int greenButtonRight = 717;
 		int scanGreenButtonTopLimit = 170;
 		int scanGreenButtonBottomLimit = 530;
 
 		int scriptShotLeft = 688 - 544;
 		int scriptShotRight = 688 - 217;
 		int scriptShotTopLimit = scanGreenButtonTopLimit
 				+ LtRobot.getLeftTopOffset().y;
 		int scriptShotBottomLimit = scanGreenButtonBottomLimit
 				+ LtRobot.getLeftTopOffset().y;
 
 		for (int top = scriptShotTopLimit; top <= scriptShotBottomLimit; ++top) {
 
 			// detect logic
 
 			FlagWrap scriptShotFlagWrap = new FlagWrap(scriptShotLeft, top,
 					scriptShotRight, top + greenButtonFlagImage.getHeight(),
 					OffsetType.SIMPLAY_MAIN_WINDOW, "");
 
 			BufferedImage scriptShotImage = LtRobot.getInstance().screenShot(
 					scriptShotFlagWrap);
 
 			// Util.saveImageToFile(scriptShotImage,
 			// "C:\\Users\\letme2010\\Desktop\\t\\" + top + ".png");
 
 			{
 				if (1 == ConfigManager.getInt("IS_SAVE_SCRIPT_SCAN_IMAGE")) {
 					Util.saveImageToFile(scriptShotImage,
 							ConfigManager.getString("SHOT_IMAGE_TMP_FOLDER")
 									+ top + ".png");
 				}
 			}
 
 			if (Util.compareImage(scriptShotImage, scriptFlagImage)
 					|| Util.compareImage(scriptShotImage, scriptFlagImage2)) {
 
 				System.out.println("find it.");
 
 				LtRobot.getInstance().leftClickInMainWindow(700,
 						scriptShotFlagWrap.getOriginTop() + 9);
 
 				break;
 			}
 		}
 	}
 
 	@Deprecated
 	public static void robotScreen() {
 
 		BufferedImage image = LtRobot.getInstance().screenShot(
 				new FlagWrap(34, 239, 76, 259, OffsetType.SIMPLAY_MAIN_WINDOW,
 						"SCRIPT_LIB"));
 
 		Util.saveImageToDefaultFile(image);
 	}
 
 	/**
 	 * @param args
 	 * 
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public static void main(String[] args) throws IOException,
 			InterruptedException {
 
 		boolean hasCfgParams = false;
 
 		for (int i = 0, len = args.length; i < len; i++) {
 			String arg = args[i];
 
 			if ("-cfg".equals(arg)) {
 				String configFilePath = args[i + 1];
 				System.out.println("-cfg " + configFilePath);
 				ConfigManager.initConfigFilePath(configFilePath);
 				hasCfgParams = true;
 			}
 		}
 
 		if (!hasCfgParams) {
 			System.err.println("Please execute me by -cfg params");
 			return;
 		}
 
 		SecureConfigManager.initConfigFilePath(ConfigManager
 				.getString("SECURE_KV_FILE_PATH"));
 
 		// kill something...
 
 		Util.exec("taskkill /f /im "
 				+ ConfigManager.getString("SIMPLE_PLAY_PROCESS_NAME"));
 		Thread.sleep(1000);
 
 		Util.exec("taskkill /f /im "
 				+ ConfigManager.getString("RUNNER_PROCESS_NAME"));
 		Thread.sleep(1000);
 
 		Util.exec(ConfigManager.getString("SIMPLE_PLAY_PATH"));
 
 		// execute action.
 
 		UIFlagManager.invorkDetect(new int[] { UIFlagManager.LOGIN_UI },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().delay(1000);
						LtRobot.getInstance().leftClickInLoginUI(143, 13);
 					}
 
 					@Override
 					public void onDetectFail() {
 						// TODO Auto-generated method stub
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(new int[] { UIFlagManager.LOGIN_WAIT_FLAG },
 				new UIFlagManager.Callback() {
 
 					public void onDetectSuccess(int aFlag) {
 						setCopyBoardText(SimplayUserManager.getUserName());
 						LtRobot.getInstance().pressCtrlV();
 						LtRobot.getInstance().delay(500);
 
 						LtRobot.getInstance().pressTab();
 						LtRobot.getInstance().delay(500);
 
 						setCopyBoardText(SecureConfigManager
 								.getString("SIMPLAY_LOGIN_USER_PASSWORD"));
 						LtRobot.getInstance().pressCtrlV();
 						LtRobot.getInstance().delay(500);
 
 						LtRobot.getInstance().pressEnter();
 					}
 
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 
 				});
 
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.WAIT_MY_COLLECTION_UI },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInMainWindow(419, 304);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.MY_COLLECTION_DEFAULT_SCRIPT_OPEN },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInMainWindow(522, 343);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.AUTO_LOGIN_PROTOCOL },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInMainWindow(501, 429);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.SCRIPT_SETTING_NEEDING },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInMainWindow(576, 394);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(new int[] { UIFlagManager.TIPS },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInMainWindow(454, 375);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(new int[] {
 				UIFlagManager.OPEN_SCRIPT_PROTECTOR, UIFlagManager.MODE_TIPS },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 
 						switch (aFlag) {
 						case UIFlagManager.OPEN_SCRIPT_PROTECTOR: {
 							LtRobot.getInstance().leftClickInMainWindow(340,
 									420);
 							break;
 						}
 						case UIFlagManager.MODE_TIPS: {
 							LtRobot.getInstance().leftClickInScriptUI(459, 296);
 
 							LtRobot.getInstance().delay(1000);
 							UIFlagManager
 									.invorkDetect(
 											new int[] { UIFlagManager.OPEN_SCRIPT_PROTECTOR },
 											new UIFlagManager.Callback() {
 
 												@Override
 												public void onDetectSuccess(
 														int aFlag) {
 													LtRobot.getInstance()
 															.leftClickInMainWindow(
 																	340, 420);
 												}
 
 												@Override
 												public void onDetectFail() {
 													LT.assertTrue(false);
 
 												}
 											});
 
 							break;
 						}
 
 						default: {
 							LT.assertTrue(false);
 							break;
 						}
 						}
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.AUTO_LOGIN_PROTOCOL },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInScriptUI(480, 386);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.SCRIPT_SETTING_NEEDING },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInMainWindow(576, 394);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		UIFlagManager.invorkDetect(new int[] { UIFlagManager.TIPS2 },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInScriptUI(447, 328);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		/**
 		 * enter script UI
 		 */
 
 		UIFlagManager.invorkDetect(new int[] {
 				UIFlagManager.SD_GUNDAM_EXE_PATH, UIFlagManager.MODE_TIPS },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 
 						switch (aFlag) {
 						case UIFlagManager.SD_GUNDAM_EXE_PATH: {
 
 							// copy goline path.
 							LtRobot.getInstance().leftClickInScriptUI(574, 183);
 							LtRobot.getInstance().delay(1000);
 							setCopyBoardText(ConfigManager
 									.getString("GOLINE_SD"));
 							LtRobot.getInstance().pressCtrlV();
 							LtRobot.getInstance().pressEnter();
 
 							break;
 						}
 						case UIFlagManager.MODE_TIPS: {
 
 							LtRobot.getInstance().leftClickInScriptUI(459, 296);
 							LtRobot.getInstance().delay(1000);
 
 							UIFlagManager
 									.invorkDetect(
 											new int[] { UIFlagManager.SD_GUNDAM_EXE_PATH },
 											new UIFlagManager.Callback() {
 
 												@Override
 												public void onDetectSuccess(
 														int aFlag) {
 
 													// copy goline path.
 													LtRobot.getInstance()
 															.leftClickInScriptUI(
 																	574, 183);
 													LtRobot.getInstance()
 															.delay(1000);
 													setCopyBoardText(ConfigManager
 															.getString("GOLINE_SD"));
 													LtRobot.getInstance()
 															.pressCtrlV();
 													LtRobot.getInstance()
 															.pressEnter();
 												}
 
 												@Override
 												public void onDetectFail() {
 													LT.assertTrue(false);
 
 												}
 											});
 
 							break;
 						}
 
 						default: {
 							LT.assertTrue(false);
 							break;
 						}
 						}
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		// set password
 		LtRobot.getInstance().leftClickInScriptUI(270, 204);
 		Thread.sleep(500);
 
 		LtRobot.getInstance().rightClickCurrntPosition();
 		Thread.sleep(500);
 
 		LtRobot.getInstance().pressCtrlA();
 		LtRobot.getInstance().pressDelete();
 		Thread.sleep(500);
 
 		setCopyBoardText(SecureConfigManager.getString("SD_LOGIN_PASSWORD"));
 		LtRobot.getInstance().pressCtrlV();
 		Thread.sleep(500);
 
 		// set reconnect
 		LtRobot.getInstance().leftClickInScriptUI(290, 340);
 		LtRobot.getInstance().delay(500);
 		LtRobot.getInstance().leftClickInScriptUI(272, 383);
 		UIFlagManager.invorkDetect(
 				new int[] { UIFlagManager.AUTO_LOGIN_PROTOCOL2 },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInScriptUI(480, 386);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		// select OS
 
 		LtRobot.getInstance().leftClickInScriptUI(516, 365);
 		LtRobot.getInstance().delay(1000);
 		LtRobot.getInstance().leftClickInScriptUI(516, 396);
 
 		LtRobot.getInstance().leftClickInScriptUI(504, 204);
 		LtRobot.getInstance().delay(500);
 		LtRobot.getInstance().leftClickInScriptUI(495, 233);
 
 		UIFlagManager.invorkDetect(new int[] { UIFlagManager.MODE_TIPS },
 				new UIFlagManager.Callback() {
 
 					@Override
 					public void onDetectSuccess(int aFlag) {
 						LtRobot.getInstance().leftClickInScriptUI(459, 296);
 					}
 
 					@Override
 					public void onDetectFail() {
 						LT.assertTrue(false);
 
 					}
 				});
 
 		// save setting
 		LtRobot.getInstance().leftClickInScriptUI(586, 434);
 		LtRobot.getInstance().delay(500);
 		LtRobot.getInstance().pressEnter();
 
 		// start Game
 		LtRobot.getInstance().clickKey(KeyEvent.VK_F10);
 
 		System.out.println("finish.");
 		System.exit(0);
 
 	}
 }
