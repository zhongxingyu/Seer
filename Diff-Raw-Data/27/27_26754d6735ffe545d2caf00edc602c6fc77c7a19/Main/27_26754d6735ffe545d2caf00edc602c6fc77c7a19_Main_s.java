 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 
 public class Main {
 	static String defaultDBPath = "D:\\crawler\\crawler.db";
 	static String defaultSavePath = "D:\\crawler\\sites\\mehrnews";
 	
 	public static void main(String[] args){
 		//***********************
 		//  DataBase File Path
 		//***********************
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Select your database file");
 		FileNameExtensionFilter ff = new FileNameExtensionFilter("*.db", "db");
 		fc.setFileFilter(ff);
 		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		fc.setVisible(true);
 		fc.showOpenDialog(null);
 		File f = fc.getSelectedFile();
 		
 		DataBase db;
 		if(f != null){
 			defaultDBPath = f.getPath();
 		}
 		else{
 			String dbDefaultMessage = "You Did not specify any path for the db file. So we assume that it is located in: " + defaultDBPath + "\n we are going to try to create that file for you if it does not exists!";
 			JOptionPane.showMessageDialog(null, dbDefaultMessage, "Notice", JOptionPane.WARNING_MESSAGE);
 			
 			//create the file if it does not exists
 			String dir = defaultDBPath.substring(0,defaultDBPath.lastIndexOf('\\'));
 			File tempDir = new File(dir);
 			tempDir.mkdirs();
 			try {
 				new File(defaultDBPath).createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		db = new DataBase(defaultDBPath);
 		
 		
 		//***********************
 		//  Save File Path
 		//***********************
 		JFileChooser dc = new JFileChooser();
 		dc.setDialogTitle("Select your saving path");
 		dc.setAcceptAllFileFilterUsed(false);
 		dc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		dc.showOpenDialog(null);
 		File d = dc.getSelectedFile();
 		
 		Mehrnews mn;
 		if(d != null){
 			defaultSavePath = d.getPath();
 		}
 		else{
 			String savePathDefaultMessage = "You Did not specify any saving path. So we assume that the saving path is: " + defaultSavePath + "\n we are going to try to create that directory for you if it does not exists!";
 			JOptionPane.showMessageDialog(null, savePathDefaultMessage, "Notice", JOptionPane.WARNING_MESSAGE);
 			//create the path if it does not exists
 			String dir = defaultSavePath + "\\";
 			File tempDir = new File(dir);
 			tempDir.mkdirs();
 		}
 		mn = new Mehrnews(defaultDBPath, defaultSavePath);
 		
		
 		try {
 			if(!db.tableExists("mehrnews")){
 				mn.createTable();
			}
 			for(int i = 2923; i > 0; i--){
 				try {
 					mn.getListPage(i);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (ClassNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 }
