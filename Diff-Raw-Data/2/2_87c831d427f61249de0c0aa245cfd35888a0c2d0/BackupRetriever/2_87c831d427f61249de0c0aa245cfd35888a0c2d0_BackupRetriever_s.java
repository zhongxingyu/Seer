 package com.Akkad.AndroidBackup;
 
 import java.io.File;
 import java.io.FilenameFilter;
 
 public class BackupRetriever {
 	private static String backupFolderLocation = "/sdcard/AndroidBackup/"; // Hardcoded until a backup folder setting is implemented
 	private static File mfile = new File(backupFolderLocation);
 
 	public static String getBackupFolderLocation() {
 		return backupFolderLocation;
 	}
 
 	public static void setBackupFolderLocation(String backupFolderLocation) {
 		BackupRetriever.backupFolderLocation = backupFolderLocation;
 	}
 
 	public static int getBackupCount(String packageName) {
 		File[] backups = getBackups();
 		int backupCount = 0;
 		for (int i = 0; i < backups.length; i++) {
			if (backups[i].getName().toLowerCase().contains(packageName)) {
 				backupCount++;
 			}
 		}
 		return backupCount;
 	}
 
 	public static File[] getBackupFolderFiles() {
 		return mfile.listFiles();
 	}
 
 	public static File[] getBackups() {
 		return mfile.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String filename) {
 				return filename.endsWith(".information");
 			}
 		});
 	}
 
 }
