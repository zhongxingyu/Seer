 package edu.bonn.mobilegaming.geoquest;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.XPath;
 import org.dom4j.io.SAXReader;
 
 import android.content.res.AssetManager;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import edu.bonn.mobilegaming.geoquest.adaptioninterfaces.AdaptionEngineInterface;
 import edu.bonn.mobilegaming.geoquest.contextmanager.xmlTagsContext;
 import edu.bonn.mobilegaming.geoquest.gameaccess.GameDataManager;
 import edu.bonn.mobilegaming.geoquest.ui.abstrakt.UIFactory;
 
 public class GameLoader {
 
 	static final String TAG = "GameLoader";
 
	private static final String AUTOSTART_GAME_NAME = "game";

	private static final CharSequence AUTOSTART_REPO_NAME = "autostart";
 
 	/**
 	 * Points to the currently selected game the user is playing.
 	 */
 	public static String zipfile;
 	public static ExecutorService executor = Executors.newCachedThreadPool();
 
 	/**
 	 * Unzips all files from the locally stored archive ({@code newGameZipFile}
 	 * and stores it in a new directory named after the zip archive name at the
 	 * same place.
 	 * 
 	 * @param gameZipFile
 	 * @return the directory where the game files have been stored
 	 */
 	static void unzipGameArchive(File gameZipFile) {
 		String newGameDirName = gameZipFile.getAbsolutePath().replace(".zip",
 				"");
 		File newGameDir = new File(newGameDirName);
 		if (!newGameDir.exists() || newGameDir.isFile()) {
 			if (newGameDir.isFile())
 				// just in the awkward case that there is a file with the same
 				// name ...
 				newGameDir.delete();
 			newGameDir.mkdir();
 		} else {
 			// clean directory:
 			deleteDir(newGameDir);
 		}
 
 		try {
 			ZipFile zipFile = new ZipFile(gameZipFile);
 			ZipEntry zipEntry;
 			File entryFile;
 			FileOutputStream fos;
 			InputStream entryStream;
 
 			for (Enumeration<? extends ZipEntry> enumeration = zipFile
 					.entries(); enumeration.hasMoreElements();) {
 				zipEntry = enumeration.nextElement();
 
 				// skip files starting with ".":
 				String zipEntryName = zipEntry.getName();
 				String[] zipEntryNameParts = zipEntryName.split("/");
 				if (zipEntryNameParts[zipEntryNameParts.length - 1]
 						.startsWith("."))
 					continue;
 
 				entryFile = new File(newGameDirName + "/" + zipEntry.getName());
 
 				// in case the entry is a directory:
 				if (zipEntryName.endsWith("/")) {
 					if (!entryFile.exists() || !entryFile.isDirectory())
 						entryFile.mkdir();
 					continue; // now it exists that's enough for directories ...
 				}
 
 				File parentDir = getGameDirectory(entryFile);
 				if (!parentDir.exists()) {
 					parentDir.mkdir();
 				}
 
 				fos = new FileOutputStream(entryFile);
 				entryStream = zipFile.getInputStream(zipEntry);
 				byte content[] = new byte[1024];
 				int bytesRead;
 
 				do {
 					bytesRead = entryStream.read(content);
 					if (bytesRead > 0)
 						fos.write(content, 0, bytesRead);
 				} while (bytesRead > 0);
 
 				fos.flush();
 				fos.close();
 
 				// set timestamp of new loaded gamefile to serverside timestamp:
 				if (entryFile.getName().equals("game.xml")) {
 					boolean timeStampOK = entryFile.setLastModified(gameZipFile
 							.lastModified());
 					if (!timeStampOK)
 						Log.e(TAG, "Time stamp of game file for \""
 								+ gameZipFile.getName()
 								+ "\" could not be set.");
 				}
 			}
 		} catch (ZipException e) {
 			Log.d(TAG, "ZipException creating zipfile from " + gameZipFile);
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.d(TAG, "IOException creating zipfile from " + gameZipFile);
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param dir
 	 * @return {@code true} if the give directory an all contained files or
 	 *         subdirectories have been deleted. Otherwise {@code false}.
 	 */
 	private static boolean deleteDir(File dir) {
 		if (!dir.exists() || !dir.isDirectory())
 			return false;
 		boolean deleted = true;
 		File[] filesToDelete = dir.listFiles();
 		for (int i = 0; i < filesToDelete.length; i++) {
 			if (filesToDelete[i].isDirectory())
 				deleted &= deleteDir(filesToDelete[i]);
 			else
 				deleted &= filesToDelete[i].delete();
 		}
 		return deleted;
 	}
 
 	public static String loadTextFile(File gameDir, String relativeResourcePath) {
 		String resourcePath = gameDir.getAbsolutePath() + "/"
 				+ relativeResourcePath;
 		Log.d(TAG + ".loadTextFile()", "Loading text file from " + resourcePath);
 		StringBuffer result = new StringBuffer();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(new File(
 					resourcePath)));
 			String line;
 			while ((line = br.readLine()) != null) {
 				result.append(line);
 			}
 			br.close();
 		} catch (FileNotFoundException e) {
 			Log.w(TAG + ".loadTextFile()", e.toString());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return result.toString();
 	}
 
 	final static int BYTE_SIZE = 1024;
 
 	/**
 	 * downloads the game.
 	 * 
 	 * TODO: check if SD Card is available and accessible. Otherwise display
 	 * error or even switch to "just online" gaming.
 	 */
 	public static void downloadGame(Handler handler, CharSequence repoName,
 			String gameFileName) {
 		URL url = GeoQuestApp.makeGameURL(repoName, gameFileName);
 		File newGameZipFile = new File(
 				GameDataManager.getLocalRepoDir(repoName), gameFileName
 						+ ".zip");
 		InputStream in;
 
 		Log.d(TAG, "start download: '" + url);
 
 		removeOldGameFile(newGameZipFile);
 		FileOutputStream fOutLocal = createFileWriter(newGameZipFile);
 
 		try {
 			in = new BufferedInputStream(url.openStream(), BYTE_SIZE);
 			int lenght = url.openConnection().getContentLength();
 
 			// TODO: care about lenght == -1, i.e. if info not available, send
 			// other msg to handler.
 			Message msg = handler.obtainMessage();
 			msg.what = GeoQuestProgressHandler.MSG_TELL_MAX_AND_TITLE;
 			msg.arg1 = lenght / BYTE_SIZE;
 			msg.arg2 = R.string.start_downloadGame;
 			handler.sendMessage(msg);
 
 			byte by[] = new byte[BYTE_SIZE];
 			int c;
 
 			while ((c = in.read(by, 0, BYTE_SIZE)) != -1) {
 				// TODO check access to SDCard!
 				fOutLocal.write(by, 0, c);
 				// trigger progress bar to proceed:
 				handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_PROGRESS);
 			}
 
 			in.close();
 			fOutLocal.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		Log.d(TAG, "completed download: '" + url);
 		handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_FINISHED);
 
 		GameLoader.unzipGameArchive(newGameZipFile);
 
 		// TODO delete local zipfile
 	}
 
 	public static FileOutputStream createFileWriter(File newGameZipFile) {
 		FileOutputStream fOutLocal = null;
 		try {
 			fOutLocal = new FileOutputStream(newGameZipFile);
 		} catch (FileNotFoundException e) {
 			Log.e(TAG, e.toString());
 		}
 		return fOutLocal;
 	}
 
 	private static void removeOldGameFile(File newGameZipFile) {
 		if (newGameZipFile.exists()) {
 			newGameZipFile.delete();
 		} else {
 			if (!getGameDirectory(newGameZipFile).exists())
 				getGameDirectory(newGameZipFile).mkdirs();
 		}
 	}
 
 	/**
 	 * Starts the game from the given local file.
 	 * 
 	 * @param handler
 	 * @param gameXMLFile
 	 * @param predefinedUIFactories
 	 *            if not empty, the first string in array is taken as name of
 	 *            UIFactory and overrides settings in game xml specification.
 	 */
 	public static void startGame(Handler handler, File gameXMLFile,
 			Class<? extends UIFactory>... predefinedUIFactories) {
 		endLastGame();
 		GeoQuestApp.resetAdaptionEngine();
 		resetContextManager();
 		// expand missions
 		try {
 			Mission.documentRoot = getDocument(gameXMLFile).getRootElement();
 			setGlobalMissionLayout(predefinedUIFactories);
 			setAdaptionType();
 			setGameDuration();
 
 			sendMsgExpandingGame(handler);
 
 			initAdaptionEngine(getGameDirectory(gameXMLFile));
 			Mission firstMission = createMissions(handler);
 			// TODO initHotspots(document.getRootElement());
 			GeoQuestApp.setRunningGameDir(getGameDirectory(gameXMLFile));
 			// Only from now on we can access game ressources.
 
 			if (handler != null)
 				handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_FINISHED);
 
 			if (firstMission != null) {
 				// TODO ReportingService muss jetzt schon gestartet sein!
 				GameSessionManager.setSessionID(Mission.documentRoot
 						.attributeValue("name"));
 				firstMission.startMission();
 			}
 		} catch (Exception e) {
 			Log.e(TAG, "DocumentException while parsing game: " + gameXMLFile);
 			if (handler != null) {
 				Message msg = handler.obtainMessage();
 				msg.what = GeoQuestProgressHandler.MSG_ABORT_BY_ERROR;
 				msg.arg1 = R.string.start_gameFileCouldNotBeParsed;
 				e.printStackTrace();
 				handler.sendMessage(msg);
 			}
 			return;
 		}
 	}
 
 	private static File getGameDirectory(File gameXMLFile) {
 		return gameXMLFile.getParentFile();
 	}
 
 	private static void resetContextManager() {
 		if (GeoQuestActivity.contextManager != null) {
 			GeoQuestActivity.contextManager.resetHistory();
 		}
 	}
 
 	private static void setGameDuration() {
 		String maxDurationStr = Mission.documentRoot
 				.attributeValue(xmlTagsContext.MAX_DURATION.getString());
 		if (maxDurationStr != null) {
 			GeoQuestActivity.contextManager.setMaximalGameDuration(Long
 					.parseLong(maxDurationStr) * 60 * 1000);
 		}
 	}
 
 	private static Document getDocument(File gameXMLFile)
 			throws DocumentException {
 		SAXReader reader = new SAXReader();
 		Document document = reader.read(gameXMLFile);
 		return document;
 	}
 
 	private static Mission createMissions(Handler handler) {
 		@SuppressWarnings("rawtypes")
 		List missionNodes = Mission.documentRoot.selectNodes("child::mission");
 		boolean first = true;
 		Mission firstMission = null;
 		for (@SuppressWarnings("rawtypes")
 		Iterator iterator = missionNodes.iterator(); iterator.hasNext();) {
 			Element missionNode = (Element) iterator.next();
 			String idOfMission = missionNode.attributeValue("id");
 			Mission curMission = Mission.create(idOfMission, null, missionNode,
 					handler);
 			if (first) {
 				firstMission = curMission;
 				first = false;
 			}
 		}
 		return firstMission;
 	}
 
 	private static void sendMsgExpandingGame(Handler handler) {
 		int num_missions = countMissions();
 		if (handler != null) {
 			Message msg = handler.obtainMessage();
 			msg.what = GeoQuestProgressHandler.MSG_TELL_MAX_AND_TITLE;
 			msg.arg1 = num_missions;
 			msg.arg2 = R.string.start_initializeGame;
 			handler.sendMessage(msg);
 		}
 	}
 
 	private static void endLastGame() {
 		GeoQuestApp.getInstance().endGame();
 		GeoQuestApp.getInstance().setInGame(true);
 	}
 
 	private static int countMissions() {
 		XPath xpath1 = Mission.documentRoot.createXPath("count(//mission)");
 		int num_missions = xpath1.numberValueOf(Mission.documentRoot)
 				.intValue();
 		return num_missions;
 	}
 
 	/**
 	 * @param predefinedUIFactoryArray
 	 *            if not empty, the first string in array is taken as name of
 	 *            UIFactory and overrides settings in game xml specification.
 	 */
 	private static void setGlobalMissionLayout(
 			Class<? extends UIFactory>... predefinedUIFactoryArray) {
 		if (predefinedUIFactoryArray.length == 0)
 			UIFactory.selectUIStyle(Mission.documentRoot
 					.attributeValue("uistyle"));
 		else
 			UIFactory.setFactory(predefinedUIFactoryArray[0]);
 
 		// TODO get rid of the rest, i.e. the old html layout mechanism:
 		String layoutAttr = Mission.documentRoot.attributeValue("layout");
 		if (layoutAttr != null && layoutAttr.equals("html")) {
 			Mission.setUseWebLayoutGlobally(true);
 		}
 	}
 
 	private static void setAdaptionType() {
 		String adaptionType = null;
 		if (GeoQuestApp.useAdaptionEngine) {
 			adaptionType = Mission.documentRoot
 					.attributeValue(AdaptionEngineInterface.xmlTagAdaptionType);
 			if (adaptionType == null || (adaptionType.trim()).length() <= 0) {
 				GeoQuestApp.useAdaptionEngine = false;
 			} else {
 				GeoQuestApp.adaptionEngine.setType(adaptionType);
 			}
 		}
 	}
 
 	private static void initAdaptionEngine(File gameDirectory) {
 		if (GeoQuestApp.useAdaptionEngine) {
 			File cFile = new File(gameDirectory.getAbsolutePath()
 					+ "/contextpool.xml");
 			File mFile = new File(gameDirectory.getAbsolutePath()
 					+ "/missionpool.xml");
 			try {
 				// TODO -- Sabine -- runtimeTest löschen
 				// XmlToolsContextPool creator = new XmlToolsContextPool();
 				// creator.runtimeTest();
 				GeoQuestApp.adaptionEngine.createPools(getDocument(cFile));
 				AlternativeMission.setMissionPoolDocument(getDocument(mFile));
 			} catch (Exception e) {
 				GeoQuestApp.useAdaptionEngine = false;
 				Log.d(TAG,
 						"AdaptionEngine was stoped. The file contextpool.xml or missionpool.xml "
 								+ "weren't found or couldn't be parsed.");
 			}
 		}
 	}
 
 	public static boolean existsGameOnClient(String repoName, String gameName) {
 		// TODO: extend to check whether the game is complete (all referred
 		// resources available).
 		File repoDir = new File(Environment.getExternalStorageDirectory(),
 				GameDataManager.getLocalRepoDir(null) + "/" + repoName);
 		if (!repoDir.exists())
 			return false;
 		File gameDir = new File(repoDir, gameName);
 		File gameXMLFile = new File(gameDir, "game.xml");
 		if (!gameDir.exists() || !gameXMLFile.exists())
 			return false;
 		return true;
 	}
 
 	public static boolean loadGameFromAssets() {
 		AssetManager assetManager = GeoQuestApp.getContext().getAssets();
 		String[] assetFiles = null;
 		InputStream is;
 		try {
 			assetFiles = assetManager.list("autostart");
 			if (assetFiles == null || assetFiles.length < 1)
 				return false;
 			else {
 				for (int i = 0; i < assetFiles.length; i++) {
 					// AssetFileDescriptor afd =
 					// assetManager.openFd(assetFiles[0]);
 					// TODO get length and show progress bar
 					is = new BufferedInputStream(assetManager.open("autostart"
 							+ File.separator + assetFiles[i],
 							AssetManager.ACCESS_BUFFER), BYTE_SIZE);
 					File newRepoFile = GameDataManager
							.getLocalRepoDir(AUTOSTART_REPO_NAME);
 					if (!newRepoFile.exists()) {
 						newRepoFile.mkdirs();
 					}
					File newGameZipFile = new File(newRepoFile,
							AUTOSTART_GAME_NAME + ".zip");
 					FileOutputStream fOutLocal = createFileWriter(newGameZipFile);
 
 					// TODO: care about lenght == -1, i.e. if info not
 					// available,
 					// send
 					// other msg to handler.
 					// Message msg = handler.obtainMessage();
 					// msg.what =
 					// GeoQuestProgressHandler.MSG_TELL_MAX_AND_TITLE;
 					// msg.arg1 = lenght / BYTE_SIZE;
 					// msg.arg2 = R.string.start_downloadGame;
 					// handler.sendMessage(msg);
 
 					byte by[] = new byte[BYTE_SIZE];
 					int c;
 
 					while ((c = is.read(by, 0, BYTE_SIZE)) != -1) {
 						// TODO check access to SDCard!
 						fOutLocal.write(by, 0, c);
 						// trigger progress bar to proceed:
 						// handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_PROGRESS);
 					}
 
 					is.close();
 					fOutLocal.close();
 
 					Log.d(TAG, "completed extraction: '" + assetFiles[0]);
 					// handler.sendEmptyMessage(GeoQuestProgressHandler.MSG_FINISHED);
 
 					GameLoader.unzipGameArchive(newGameZipFile);
					
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e(TAG, e.getMessage());
 			return false;
 		}
 		return true;
 	}
 
 }
