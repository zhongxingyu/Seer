 package fi.mikuz.boarder.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.commons.io.IOUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.Log;
 
 import com.bugsense.trace.BugSenseHandler;
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.StreamException;
 
 import fi.mikuz.boarder.component.soundboard.GraphicalSound;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboard;
 import fi.mikuz.boarder.component.soundboard.GraphicalSoundboardHolder;
 import fi.mikuz.boarder.gui.SoundboardMenu;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class FileProcessor {
 	public static final String TAG = "FileProcessor";
 	
 	private static Object boardSaveFileLock = new Object();
 	
 	private static final String boardSaveFileName = "graphicalBoard";
 	private static final String boardTempSaveFileName = boardSaveFileName + ".tmp";
 	
 	public static GraphicalSoundboardHolder loadGraphicalSoundboardHolder(String boardName) throws IOException {
 		synchronized (boardSaveFileLock) {

 			File boardDir = constructBoardPath(boardName);
 			File boardFile = new File(boardDir, boardSaveFileName);
 			File tmpBoardFile = new File(boardDir, boardTempSaveFileName);

 			try {
 				GraphicalSoundboardHolder holder = loadSerializedBoardV1(boardDir, boardFile);
 				return holder;
 			} catch(StreamException e) {
 				try {
					Log.e(TAG, "Unable to load board \"" + boardName + "\"", e);
 					GraphicalSoundboardHolder tmpHolder = loadSerializedBoardV1(boardDir, tmpBoardFile);
					Log.i(TAG, "Board \"" + boardName + "\" tmp file loaded successfully");
 					return tmpHolder;
 				} catch(StreamException e2) {
					Log.e(TAG, "Unable to load board \"" + boardName + "\" from tmp file", e2);
					GraphicalSoundboardHolder legacyHolder = null;
					try {
						legacyHolder = loadUnlimitedSoundboardBoard(boardDir, boardName);
					} catch (Exception e3) {
						Log.e(TAG, "Unable to load board \"" + boardName + "\" using legacy format");
						return null;
					}
					Log.i(TAG, "Imported board \"" + boardName + "\" using Unlimited Soundboards format");
 					return legacyHolder;
 				}
 			}
 		}
 	}

 	public static GraphicalSoundboardHolder loadSerializedBoardV1(File boardDir, File boardFile) throws IOException {
 		XStream xstream = XStreamUtil.graphicalBoardXStream();
 		GraphicalSoundboardHolder holder = (GraphicalSoundboardHolder) xstream.fromXML(boardFile);
 		changeBoardDirectoryReferences(holder, SoundboardMenu.mLocalBoardDir, boardDir);
 		return holder;
 	}
 	
 	public static GraphicalSoundboardHolder loadUnlimitedSoundboardBoard(File boardDir, String boardName) throws IOException {
 		GraphicalSoundboardHolder holder = new GraphicalSoundboardHolder();
 		GraphicalSoundboard gsb = new GraphicalSoundboard();
 		
 		DataInputStream in = new DataInputStream(new FileInputStream(boardDir + "/graphicalBoard"));
         BufferedReader br = new BufferedReader(new InputStreamReader(in), 8192);
         
 	    String line;
 	    line = br.readLine();
 	    
 	    gsb.setPlaySimultaneously(Boolean.parseBoolean(line.substring(0, line.indexOf("1"))));
 	    gsb.setBoardVolume(Float.valueOf(line.substring(line.indexOf("1") + 3, line.indexOf("2"))).floatValue());
 	    gsb.setUseBackgroundImage(Boolean.parseBoolean(line.substring(line.indexOf("2") + 3, line.indexOf("3"))));
 	    gsb.setBackgroundColor(Integer.valueOf(line.substring(line.indexOf("3") + 3, line.indexOf("4"))).intValue());
 	    File backgroundImagePath = new File(line.substring(line.indexOf("4") + 3, line.indexOf("5")));
 	    if (backgroundImagePath.toString().contains("local/")) {
 	    	backgroundImagePath = new File(SoundboardMenu.mSbDir + "/" + boardName, backgroundImagePath.toString().
 	    			substring(6, backgroundImagePath.toString().length()));
 	    } else if (backgroundImagePath.toString().equals("na")) {
 	    	backgroundImagePath = null;
 	    }
 	    gsb.setBackgroundImagePath(backgroundImagePath);
 	    gsb.setBackgroundX(Float.valueOf(line.substring(line.indexOf("5") + 3, line.indexOf("6"))).floatValue());
 	    gsb.setBackgroundY(Float.valueOf(line.substring(line.indexOf("6") + 3, line.indexOf("7"))).floatValue());
 	    gsb.setBackgroundWidthHeight(null, 
 	    		Float.valueOf(line.substring(line.indexOf("7") + 3, line.indexOf("8"))).floatValue(),
 	    		Float.valueOf(line.substring(line.indexOf("8") + 3, line.indexOf("9"))).floatValue());
 	    gsb.setScreenOrientation(Integer.valueOf(line.substring(line.indexOf("9") + 3, line.indexOf("10"))).intValue());
 	    gsb.setAutoArrange(Boolean.parseBoolean(line.substring(line.indexOf("10") + 4, line.indexOf("11"))));
 	    gsb.setAutoArrangeColumns(Integer.valueOf(line.substring(line.indexOf("11") + 4, line.indexOf("12"))).intValue());
 	    gsb.setAutoArrangeRows(Integer.valueOf(line.substring(line.indexOf("12") + 4, line.length())).intValue());
 
 	    while ((line = br.readLine()) != null)   {
 	    	GraphicalSound sound = new GraphicalSound();
 
 	    	sound.setName(line.substring(0, line.indexOf("1")).replaceAll("lineBreak", "\n"));
 
 	    	File soundPath = new File(line.substring(line.indexOf("1") + 3, line.indexOf("2")));
 	    	if (soundPath.toString().contains("local/")) {
 	    		sound.setPath(new File(
 	    				SoundboardMenu.mSbDir + "/" + boardName, soundPath.toString().substring(6, soundPath.toString().length())));
 	    	} else {
 	    		sound.setPath(soundPath);
 	    	}
 
 	    	sound.setVolumeLeft(Float.valueOf(line.substring(line.indexOf("2") + 3, line.indexOf("3"))).floatValue());
 	    	sound.setVolumeRight(Float.valueOf(line.substring(line.indexOf("3") + 3, line.indexOf("4"))).floatValue());
 	    	sound.setNameFrameX(Float.valueOf(line.substring(line.indexOf("4") + 3, line.indexOf("5"))).floatValue());
 	    	sound.setNameFrameY(Float.valueOf(line.substring(line.indexOf("5") + 3, line.indexOf("6"))).floatValue());
 	    	sound.setHideImageOrText(Integer.valueOf(line.substring(line.indexOf("8") + 3, line.indexOf("9"))));
 
 	    	File imagePath = new File(line.substring(line.indexOf("9") + 3, line.indexOf("10")));
 	    	if (imagePath.toString().contains("local/")) {
 	    		sound.setImagePath(new File(
 	    				SoundboardMenu.mSbDir + "/" + boardName, imagePath.toString().substring(6, imagePath.toString().length())));
 	    	} else {
 	    		sound.setImagePath(imagePath);
 	    	}
 
 	    	sound.setImageX(Float.valueOf(line.substring(line.indexOf("10") + 4, line.indexOf("11"))).floatValue());
 	    	sound.setImageY(Float.valueOf(line.substring(line.indexOf("11") + 4, line.indexOf("12"))).floatValue());
 	    	sound.setImageWidthHeight(null, 
 	    			Float.valueOf(line.substring(line.indexOf("12") + 4, line.indexOf("13"))).floatValue(),
 	    			Float.valueOf(line.substring(line.indexOf("13") + 4, line.indexOf("14"))).floatValue());
 	    	sound.setHideImageOrText(Integer.valueOf(line.substring(line.indexOf("14") + 4, line.indexOf("15"))));
 	    	sound.setNameTextColorInt(Integer.valueOf(line.substring(line.indexOf("15") + 4, line.indexOf("16"))));
 	    	sound.setNameFrameInnerColorInt(Integer.valueOf(line.substring(line.indexOf("16") + 4, line.indexOf("17"))));
 	    	sound.setNameFrameBorderColorInt(Integer.valueOf(line.substring(line.indexOf("17") + 4, line.indexOf("18"))));
 	    	sound.setShowNameFrameInnerPaint(Boolean.parseBoolean(line.substring(line.indexOf("18") + 4, line.indexOf("19"))));
 	    	sound.setShowNameFrameBorderPaint(Boolean.parseBoolean(line.substring(line.indexOf("19") + 4, line.indexOf("20"))));
 	    	sound.setLinkNameAndImage(Boolean.parseBoolean(line.substring(line.indexOf("20") + 4, line.indexOf("21"))));
 	    	sound.setNameSize(Float.valueOf(line.substring(line.indexOf("21") + 4, line.indexOf("22"))));
 	    	sound.setAutoArrangeColumn(Integer.valueOf(line.substring(line.indexOf("22") + 4, line.indexOf("23"))));
 	    	sound.setAutoArrangeRow(Integer.valueOf(line.substring(line.indexOf("23") + 4, line.indexOf("24"))));
 
 	    	File activeImagePath = new File(line.substring(line.indexOf("24") + 4, line.indexOf("25")));
 	    	if (activeImagePath.toString().contains("local/")) {
 	    		sound.setActiveImagePath(new File(
 	    				SoundboardMenu.mSbDir + "/" + boardName, activeImagePath.toString().substring(6, activeImagePath.toString().length())));
 	    	} else {
 	    		sound.setActiveImagePath(activeImagePath);
 	    	}
 	    	sound.setSecondClickAction(Integer.valueOf(line.substring(line.indexOf("25") + 4, line.length())));
 	    	
 	    	if (sound.getImagePath().getAbsolutePath().equals("/")) sound.setImagePath(null);
 	    	if (sound.getActiveImagePath().getAbsolutePath().equals("/")) sound.setActiveImagePath(null);
 
 	    	gsb.addSound(sound);
 	    }
 	    
 	    in.close();
 	    holder.getBoardList().add(gsb);
 	    
 		return holder;
 	}
 	
 	public static void saveGraphicalSoundboardHolder(String boardName, GraphicalSoundboardHolder boardHolder) throws IOException {
 		synchronized (boardSaveFileLock) {
 			File boardDir = constructBoardPath(boardName);
 			File boardFile = new File(boardDir, boardSaveFileName);
 			File tmpBoardFile = new File(boardDir, boardTempSaveFileName);
 			
 			changeBoardDirectoryReferences(boardHolder, boardDir, SoundboardMenu.mLocalBoardDir);
 			
 			boardDir.mkdirs();
 			attemptBackup(tmpBoardFile);
 
 			if (boardDir.exists() == false) {
 				boardDir.mkdirs();
 			}
 			
 			BufferedWriter tmpOut = new BufferedWriter(new FileWriter(tmpBoardFile));
 			
 			XStream xstream = XStreamUtil.graphicalBoardXStream();
 			xstream.toXML(boardHolder, tmpOut);
 			tmpOut.close();
 			
 			BufferedReader in = new BufferedReader(new FileReader(tmpBoardFile));
 			BufferedWriter out = new BufferedWriter(new FileWriter(boardFile));
 			IOUtils.copy(in, out);
 			in.close();
 			out.close();
 			
 			tmpBoardFile.delete();
 		}
 	}
 	
 	public static boolean boardExists(String boardName) {
 		File boardDir = constructBoardPath(boardName);
 		return boardDir.exists();
 	}
 	
 	private static File constructBoardPath(String boardName) {
 		return new File(SoundboardMenu.mSbDir, boardName);
 	}
 	
 	public static void attemptBackup(File backupIn) {
 		if (backupIn.exists()) {
 			try {
 				File backupDir = new File(SoundboardMenu.mBackupDir, backupIn.getParentFile().getName());
 				for (int i = 8; i >= 0; i--) {
 					if (new File(backupDir, "graphicalBoard." + i).exists()) {
 						IOUtils.copy(new FileInputStream(new File(backupDir, "graphicalBoard." + i)), 
 								new FileOutputStream(new File(backupDir, "graphicalBoard." + (i+1))));
 					}
 				}
 				if (!backupDir.exists()) backupDir.mkdirs();
 				File backupOut = new File(backupDir, "graphicalBoard.0");
 				InputStream in = new FileInputStream(backupIn);
 				OutputStream out = new FileOutputStream(backupOut);
 			    IOUtils.copy(in, out);
 			} catch (FileNotFoundException e) {
 				Log.e(TAG, "Failed to backup", e);
 				BugSenseHandler.sendException(e);
 			} catch (IOException e) {
 				Log.e(TAG, "Failed to backup", e);
 				BugSenseHandler.sendException(e);
 			}
 		}
 	}
 	
 	public static void convertGraphicalBoard(Activity activity, String boardName, GraphicalSoundboard gsb) throws IOException {
 		
 		String boardDir = new File(SoundboardMenu.mSbDir, boardName).getAbsolutePath();
 		
 		if (gsb.getBackgroundImagePath() != null) {
 			if (!gsb.getBackgroundImagePath().exists()) {
 				String error = "Background image file doesn't exist\n\nFile: " + gsb.getBackgroundImagePath().getAbsolutePath();
 				notify(activity, error);
 				Log.w(TAG, error);
 			} else if (gsb.getBackgroundImagePath().getAbsolutePath().contains(boardDir) == false) {
 				File outFile = copySoundElement(boardDir, gsb.getBackgroundImagePath());
 				gsb.setBackgroundImagePath(outFile);
 			}
 		}
 		
 		
 		for (GraphicalSound sound : gsb.getSoundList()) {
 			String doesntExist = " doesn't exist\n\nSound:\n" + sound.getName() + "\n\nFile: ";
 			
 			if (BoardLocal.isFunctionSound(sound)) {
 			} else if (!sound.getPath().exists()) {
 				String error = "Sound file" + doesntExist + sound.getPath().getAbsolutePath();
 				notify(activity, error);
 				Log.w(TAG, error);
 			} else if (sound.getPath().getAbsolutePath().contains(boardDir) == false) {
 				File outFile = copySoundElement(boardDir, sound.getPath());
 				sound.setPath(outFile);
 			}
 			
 			if (sound.getImagePath() != null) {
 				if (!sound.getImagePath().exists()) {
 					String error = "Image file" + doesntExist + sound.getImagePath().getAbsolutePath();
 					notify(activity, error);
 					Log.w(TAG, error);
 				} else if (sound.getImagePath().getAbsolutePath().contains(boardDir) == false) {
 					File outFile = copySoundElement(boardDir, sound.getImagePath());
 					sound.setImagePath(outFile);
 				}
 			}
 			
 			if (sound.getActiveImagePath() != null) {
 				if (!sound.getActiveImagePath().exists()) {
 					String error = "Active image file" + doesntExist + sound.getActiveImagePath().getAbsolutePath();
 					notify(activity, error);
 					Log.w(TAG, error);
 				} else if (sound.getActiveImagePath().getAbsolutePath().contains(boardDir) == false) {
 					File outFile = copySoundElement(boardDir, sound.getActiveImagePath());
 					sound.setActiveImagePath(outFile);
 				}
 			}
 		}
 		Log.v(TAG, boardName + " converted");
 	}
 	
 	private static File copySoundElement(String boardDir, File inFile) throws IOException {
 		File outFile = new File(boardDir, inFile.getName());
 
 		InputStream in = new FileInputStream(inFile);
 		OutputStream out = new FileOutputStream(outFile);
 		IOUtils.copy(in, out);
 		Log.v(TAG, "Copied " + inFile.getAbsolutePath() + " to " + outFile);
 		return outFile;
 	}
 	
 	private static void notify(final Activity activity, final String text) {
 		activity.runOnUiThread(new Runnable() {
 		    public void run() {
 		    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
             	builder.setMessage(text);
             	builder.show();
 		    }
 		});
 	}
 	
 	public static void renameBoard(String originalBoardName, String newBoardName) {
 		File oldLocation = new File(SoundboardMenu.mSbDir, originalBoardName);
 		File newLocation = new File(SoundboardMenu.mSbDir, newBoardName);
 		
 		oldLocation.renameTo(newLocation);
 	}
 	
 	public static void duplicateBoard(String originalBoardName) {
 		
 		File newLocation = null;
 		
 		int i = 1;
 		while (true) {
 			StringBuffer duplicateBoardNameBuffer = new StringBuffer();
 			duplicateBoardNameBuffer.append("duplicate").append(i).append("-").append(originalBoardName);
 			newLocation = new File(SoundboardMenu.mSbDir + "/" + duplicateBoardNameBuffer);
 			if (!newLocation.exists()) {
 				break;
 			}
 			i++;
 		}
 		
 		File oldLocation = new File(SoundboardMenu.mSbDir + "/" + originalBoardName);
 		
 		try {
 			copyDirectory(oldLocation, newLocation);
 		} catch (IOException e) {
 			Log.e(TAG, "Unable to duplicate", e);
 		}
 	}
 	
 	private static void changeBoardDirectoryReferences(GraphicalSoundboardHolder holder, File oldLocation, File newLocation) throws IOException {
 		
 		for (GraphicalSoundboard board : holder.getBoardList()) {
 			board.setBackgroundImagePath(replaceBoardPath(board.getBackgroundImagePath(), oldLocation, newLocation));
 			List<GraphicalSound> soundList = new ArrayList<GraphicalSound>();
 			
 			for (GraphicalSound sound : board.getSoundList()) {
 				sound.setPath(replaceBoardPath(sound.getPath(), oldLocation, newLocation));
 				sound.setImagePath(replaceBoardPath(sound.getImagePath(), oldLocation, newLocation));
 				sound.setActiveImagePath(replaceBoardPath(sound.getActiveImagePath(), oldLocation, newLocation));
 				soundList.add(sound);
 			}
 		}
 	}
 	
 	private static File replaceBoardPath(File file, File originalBoard, File newBoard) {
 		if (file == null) return null;
 		String filePath = file.getAbsolutePath();
 		String originalBoardPath = originalBoard.getAbsolutePath();
 		String newBoardPath = newBoard.getAbsolutePath();
 		return new File(filePath.replaceFirst(originalBoardPath, newBoardPath));
 	}
 	
 	public static void delete(File f) throws IOException {
 		  if (f.isDirectory()) {
 		    for (File c : f.listFiles())
 		      delete(c);
 		  }
 		  if (!f.delete())
 		    throw new FileNotFoundException("Failed to delete file: " + f);
 	}
 	
 	private static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
 
 		if (sourceLocation.isDirectory()) {
 			if (!targetLocation.exists()) {
 				targetLocation.mkdir();
 			}
 
 			String[] children = sourceLocation.list();
 			for (int i=0; i<children.length; i++) {
 				copyDirectory(new File(sourceLocation, children[i]),
 						new File(targetLocation, children[i]));
 			}
 		} else {
 			copyFile(sourceLocation, targetLocation);
 		}
 	}
 	
 	private static void copyFile(File sourceLocation, File targetLocation) throws IOException {
 		InputStream in = new FileInputStream(sourceLocation);
 		OutputStream out = new FileOutputStream(targetLocation);
 
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 	
 	public static void saveScreenshot(Context context, Bitmap bitmap, String boardName) {
 		String toastMsg = null;
 		try {
 			SoundboardMenu.mShareDir.mkdirs();
 			File screenshot = new File(SoundboardMenu.mShareDir, boardName + ".png");
 			if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(screenshot))) {
 				toastMsg = "Screenshot saved as " + 
 						SoundboardMenu.mShareDir.getParent() + "/" + SoundboardMenu.mShareDir.getName() + "/" + screenshot.getName();
 	    	} else {
 	    		toastMsg = "Couldn't save screenshot";
 	    	}
 		} catch (FileNotFoundException e) {
 			BugSenseHandler.sendException(e);
 			Log.e(TAG, "Error saving screenshot", e);
 			toastMsg = "Error saving screenshot";
 		}
 		ContextUtils.toast(context, toastMsg);
 	}
 	
 	private int mBoardDirLength;
 	
 	public void zipBoard(String boardName) {
 		
 		try {
 			SoundboardMenu.mShareDir.mkdirs();
 			
 			File inFolder=new File(SoundboardMenu.mSbDir, boardName);
 			File outFile=new File(SoundboardMenu.mShareDir, boardName + ".zip");
 			mBoardDirLength = inFolder.getAbsolutePath().length()-boardName.length();
 		    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFile));
 		    System.out.println("Creating : " + outFile);
 		    zipAddDir(inFolder, out);
 		    out.close();
 		}
 		catch(IOException e) {
 			BugSenseHandler.sendException(e);
 			Log.e(TAG, "Error zipping", e);
 		}
 	}
 	
 	void zipAddDir(File dirObj, ZipOutputStream out) throws IOException {
 		File[] files = dirObj.listFiles();
 		byte[] tmpBuf = new byte[1024];
 
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isDirectory()) {
 				zipAddDir(files[i], out);
 				continue;
 			}
 			FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
 			System.out.println(" Adding: " + files[i].getAbsolutePath().substring(mBoardDirLength));
 			out.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(mBoardDirLength)));
 			int len;
 			while ((len = in.read(tmpBuf)) > 0) {
 				out.write(tmpBuf, 0, len);
 			}
 			out.closeEntry();
 			in.close();
 		}
 	}
 }
