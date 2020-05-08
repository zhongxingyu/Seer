 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package awana.database;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Renlar <liddev.com>
  */
 public class AwanaDatabase {
 
	private static int backupCutoff = 10;
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String args[]) {
 		LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
 		/* Set the Nimbus look and feel */
 		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
 		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
 		 */
 		try {
 			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					javax.swing.UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(DirectoryPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 		}
 		//</editor-fold>
		backupDatabase();
 
		DatabaseWrapper databaseWrapper = new DatabaseWrapper();
 		Record.loadMasterData(); //do not remove temporary record load fix will be replaced with dynamic loading once variable yml field loading is supproted
 
 		/* Create and display the form */
 		DirectoryPage page;
 		page = new DirectoryPage(databaseWrapper);
 		page.setVisible(true);
 
 		/*create the shutdown hook*/
 		Thread t = new Thread(new Shutdown(page, databaseWrapper));
 		Runtime.getRuntime().addShutdownHook(t);
 	}
 
 	private static void backupDatabase() {
 		renameAndTrimBackups();
 		File directory = new File(DatabaseWrapper.storagePath + "Backup/");
 		ArrayList<Path> paths = new ArrayList<>();
 		try {
 			DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath(DatabaseWrapper.storagePath), "*.db");
 			for (Path p : stream) {
 				paths.add(p);
 				Logger.getGlobal().log(Level.INFO, null, p);
 			}
 			stream.close();
 
 			Files.copy(paths.get(0), FileSystems.getDefault().getPath(directory.getPath() + "/" + "Directory.h2.db.0.back"));
 
 		} catch (IOException ex) {
 			Logger.getGlobal().log(Level.SEVERE, null, ex);
 		}
 	}
 
 	private static void renameAndTrimBackups() {
 		File directory = new File(DatabaseWrapper.storagePath + "Backup/");
 		ArrayList<Path> paths = new ArrayList<>();
 		if (!directory.exists()) {
 			try {
 				Files.createDirectory(directory.toPath());
 			} catch (IOException ex) {
 				Logger.getLogger(AwanaDatabase.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		} else {
 			try {
 				DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystems.getDefault().getPath(DatabaseWrapper.storagePath + "Backup/"), "*.back");
 				for (Path p : stream) {
 					paths.add(p);
 					Logger.getGlobal().log(Level.INFO, p.toString());
 				}
 				stream.close();
 
 				if (!paths.isEmpty()) {
 					for (int i = paths.size(); i > 0; i--) {
 						File file = paths.get(i - 1).toFile();
 						System.out.println(file.getPath());
 						if (i >= backupCutoff) {
 							file.delete();
 						} else {
 							File FileR = FileSystems.getDefault().getPath(file.getPath().substring(0, file.getPath().lastIndexOf("db") + 3).concat(i + ".back")).toFile();
 							file.renameTo(FileR);
 						}
 					}
 				}
 			} catch (IOException ex) {
 				Logger.getGlobal().log(Level.SEVERE, null, ex);
 			}
 		}
 	}
 }
