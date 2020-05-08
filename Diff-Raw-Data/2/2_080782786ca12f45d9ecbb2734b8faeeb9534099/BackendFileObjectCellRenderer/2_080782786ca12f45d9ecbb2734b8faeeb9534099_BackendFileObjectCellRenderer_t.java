 
 
 package org.vpac.grisu.client.view.swing.files;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 import javax.swing.Icon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.filechooser.FileSystemView;
 
 import org.vpac.grisu.client.model.files.GrisuFileObject;
 import org.vpac.grisu.client.model.files.FileConstants;
 import org.vpac.grisu.client.model.files.FileSystemListFrontend;
 
 public class BackendFileObjectCellRenderer implements ListCellRenderer {
 	
 	private static FileSystemView fsView =	FileSystemView.getFileSystemView();
 	private static Icon folderIcon = fsView.getSystemIcon(new File(System.getProperty("user.home")));
 	//TODO think of something better?
 	private static Icon fileIcon = fsView.getSystemIcon(findFile());
 	
 	private static File findFile() {
 		for ( File file : new File(System.getProperty("user.home")).listFiles() ) {
 			if ( file.isFile() ) return file;
 		}
 //		return new ImageIcon();
 		return null;
 	}
   
 	
 	public Component getListCellRendererComponent(JList list, Object value,
 			int index, boolean isSelected, boolean cellHasFocus) {
 		
 		GrisuFileObject file = (GrisuFileObject)value;
 		FileSystemListFrontend fs = (FileSystemListFrontend)list.getModel();
 
 		JLabel label = null;
 		if ( ! fs.currentDirectoryIsOnRoot() && index == 0) {
 			label = new JLabel("..", folderIcon, SwingConstants.LEADING);
 		} else if ( file.getType() == FileConstants.TYPE_FOLDER ) {
 			String name = file.getName();
 			if ( name != null ) {
 				try {
 					name = URLDecoder.decode(name, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			label = new JLabel(name, folderIcon, SwingConstants.LEADING);
 			
 
 				
 			
 		} else if ( file.getType() == FileConstants.TYPE_FILE ) {
 			String name = file.getName();
 			if ( name != null ) {
 				try {
 					name = URLDecoder.decode(name, "UTF-8");
 				} catch (UnsupportedEncodingException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			long size = file.getSize(false);
 			String sizeString = size+" B";
 			if ( size > 1024*1024 ) 
				sizeString = size/(1024*1024) + " MB";
 			else if ( size > 1024 )
 				sizeString = size/1024 + " KB";
 			
 			label = new JLabel(name+" ("+sizeString+")", fileIcon, SwingConstants.LEADING);
 
 			
 		} else {
 			throw new RuntimeException("File is not a know file type. This should never happen.");
 		}
 		
 		if ( isSelected ) {
 			label.setBackground(Color.lightGray);
 			label.setOpaque(true);
 		} else {
 			label.setOpaque(false);
 		}
 		
 		return label;
 	}
 
 }
