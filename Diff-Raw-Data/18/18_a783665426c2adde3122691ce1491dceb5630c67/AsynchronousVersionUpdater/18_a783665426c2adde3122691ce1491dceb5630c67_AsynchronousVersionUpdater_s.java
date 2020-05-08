 package com.aillusions.dictionary.util;
 
 import java.awt.Cursor;
 import java.awt.Frame;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 
 import com.aillusions.dictionary.view.DownloadUpdateDialog;
 
 public class AsynchronousVersionUpdater implements Runnable {
 
 	public static final String CONTENT_LENGTH = "Content-Length";
 	//public static final String COPY_OVERRITE_UPDATE = "cmd.exe /c copy .\\" + "update\\"	+ "unzipped\\*.* .\\ /Y /n";
 	public static final String COPY_OVERRITE_UPDATE = "cmd.exe /c xcopy .\\" + "update\\"	+ "unzipped\\*.* /a /e /k /Y /n";
 	public static final String CMD_RUN_DICT = "cmd.exe /c java -jar Dictionary.jar /n";
 	public static final String UPDATE_ZIP_FOLDER = "http://github.com/downloads/aillusions/Dictionary/";
 	private static final Logger l = Logger
 			.getLogger(AsynchronousVersionUpdater.class);
 	public JFrame currentContainer;
 
 	public Frame getCurrentContainer() {
 		return currentContainer;
 	}
 
 	public void setCurrentContainer(JFrame currentContainer) {
 		this.currentContainer = currentContainer;
 	}
 
 	public void run() {
 
 		VersionChecker vc = new VersionChecker();
 		boolean continueAscting = vc.isNewerVersionAvailable();
 
 		l.log(Priority.INFO, "Newer version available: " + continueAscting + "; current: " + vc.getCurrentVersion() + "; avail: " + vc.getLastAvailableVersion() );
 		String zipName = "Dictionary-" + vc.getLastAvailableVersion() + ".zip";
 
 		if (continueAscting) {
 
 			Object[] options = { "Yes", "No", "Never ask me" };
 			int n = JOptionPane.showOptionDialog(null,
 					"Would you like to update Dictionary now?",
 					"Newer version is available!",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
 			if (n == 0) {
 
 				URLConnection ucn = null;
 
 				try {
 					ucn = new URL(UPDATE_ZIP_FOLDER + zipName).openConnection();
 					// ucn = new
 					// URL("file:///c:/Dictionary1.2.zip").openConnection();
 				} catch (MalformedURLException e) {
 					l.log(Priority.ERROR, e);
 				} catch (IOException e) {
 					l.log(Priority.ERROR, e);
 				}
 				if (ucn == null) {
 					JOptionPane.showMessageDialog(null,
 							"Update temporarily unavailable. Try latter.",
 							"Sorry!", JOptionPane.INFORMATION_MESSAGE);
 					l.log(Priority.WARN, zipName + "ucn == null");
 					return;
 				}
 
 				List value = ucn.getHeaderFields().get(CONTENT_LENGTH);
 
 				int size = -1;
 				if (value != null && !value.isEmpty()) {
 
 					String sLength = (String) value.get(0);
 
 					if (sLength != null) {
 						size = Integer.parseInt(sLength);
 					}
 				}
 
 				try {
 					InputStream isr = ucn.getInputStream();
 
 					File updateDir = new File("./update");
 					if (updateDir.exists()) {
 						Unzip.deleteDirectory(updateDir);
 					}
 
 					updateDir.mkdir();
 
 					File targetFile = new File("update/" + zipName);
 					if (targetFile.exists()) {
 						targetFile.delete();
 					}
 					FileOutputStream fos = new FileOutputStream("update/"
 							+ zipName, false);
 
 					currentContainer.setCursor(Cursor
 							.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
 					DownloadUpdateDialog dud = new DownloadUpdateDialog(
 							currentContainer);
 					dud.setVisible(true);
 
 					l.log(Priority.INFO, "Size of update: " + size);
 
 					byte[] b = new byte[10000];
 					int read = 0;
 					int overalRead = 0;
 
 					while ((read = isr.read(b)) > 0 && !dud.isStoped()) {
 						fos.write(b, 0, read);
 						overalRead += read;
 						if (size > 0) {
 							dud.setValue((overalRead * 100 / size));
 						} else {
 							dud.setValue(50);
 						}
 					}
 
 					isr.close();
 					fos.close();
 					fos = null;
 
 					currentContainer.setCursor(Cursor
 							.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 
 					continueAscting = !dud.isStoped();
 					dud.setVisible(false);
 					dud.dispose();
 					dud = null;
 
 					Unzip unz = new Unzip();
 					unz.unzip("update/" + zipName, "update/unzipped");
 
 				} catch (IOException e) {
 					l.log(Priority.ERROR, e);
 				}
 			} else {
 				continueAscting = false;
 			}
 		}
 
 		if (continueAscting) {
 
 			Object[] options1 = { "Yes", "No" };
 			int n1 = JOptionPane.showOptionDialog(null,
 					"Can we restart Dictionary now?",
 					"Latest version will be installed!",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE, null, options1, options1[0]);
 
 			if (n1 == 0) {
 
 				Runnable rnb = new Runnable() {
 
 					public void run() {
 						try {
 							Runtime.getRuntime().exec(COPY_OVERRITE_UPDATE);
 							Runtime.getRuntime().exec(CMD_RUN_DICT);
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 
 				};
 
 				new Thread(rnb).run();
 				System.exit(0);
 			}
 		}
 	}
 }
