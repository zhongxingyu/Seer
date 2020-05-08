 package util.files;
 
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Stack;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import wombat.DocumentManager;
 
 
 public class RecentDocumentManager {
 	
 	private RecentDocumentManager() {}
 
 	static int FileCount = 10;
 	static Stack<File> fileList = new Stack<File>();
 	static JMenu menu;
 	
 	/**
 	 * Log an error.
 	 * @param msg The error.
 	 */
 	public static void addFile(File f) {
 		// Remove then add to the front of the list.
 		fileList.remove(f);
 		fileList.push(f);
 
 		// Old documents fall off.
 		while (fileList.size() > 10)
			fileList.remove(0);
 		
 		// Rebuild the menu.
 		rebuildMenu();
 	}
 	
 	/**
 	 * Rebuild the menu.
 	 */
 	static void rebuildMenu() {
 		if (menu != null) {
 			// Send the file list to all of the listeners.
 			menu.removeAll();
 			JMenuItem item;
 			for (int i = fileList.size() - 1; i >= 0; i--) {
 				final File each = fileList.get(i);
				
				if (!each.exists()) {
					fileList.remove(i);
					continue;
				}
				
 				item = new JMenuItem(each.getPath());
 				item.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						DocumentManager.Open(each);
 					}
 				});
 				menu.add(item);
 			}
 			
 			menu.addSeparator();
 			item = new JMenuItem("Clear recent documents");
 			item.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					fileList.clear();
 					rebuildMenu();
 				}
 			});
 			menu.add(item);
 			
 			// Figure out if it should be enabled or disabled.
 			menu.setVisible(!fileList.isEmpty());
 		}
 	}
 
 	/**
 	 * Build a list that keeps track of recent documents.
 	 * @return
 	 */
 	public static JMenuItem buildRecentDocumentsMenu() {
 		// Build the menu if it hasn't already been.
 		if (menu == null) {
 			menu = new JMenu("Open recent");
 			rebuildMenu();
 		}
 		
 		return menu;
 	}
 
 	/**
 	 * Set the files to a string.
 	 */
 	public static void setFiles(String files) {
 		if (files != null)
 			for (String doc : files.split(";"))
 				if (!doc.trim().isEmpty())
 					fileList.add(new File(doc));
 		
 		rebuildMenu();
 	}
 	
 	/**
 	 * Get the files as a string.
 	 * @return Duh
 	 */
 	public static String getFiles() {
 		StringBuilder sb = new StringBuilder();
 		for (File f : fileList) {
 			sb.append(f.getPath());
 			sb.append(";");
 		}
 		if (sb.length() > 0)
 			sb.delete(sb.length() - 1, sb.length());
 		return sb.toString();
 	}
 }
 
