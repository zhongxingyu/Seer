 import java.awt.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import org.apache.commons.io.FileUtils;
 
 
 public class ModFileHandler {
 	
 	static int modFoldersDectected = 0;
 	static int modFilesDetected = 0;
 	
 	static String minecraftDirString = System.getenv("APPDATA")+"/.minecraft";
 	
 	
 	File[] listModFolders;
 	File[] fileToDelete;
 	static HashMap<String,String> dirs = new HashMap<String,String>();
 	static HashMap<String,Boolean> isFile = new HashMap<String, Boolean>();
 	static HashMap<String,String> filesToDelete = new HashMap<String, String>();
 
 
 	public void deleteMod(List list)
 	{
 		File filedes = null;
 		if(isFile.get(list.getSelectedItem()) == true){
 			filedes = new File(minecraftDirString+"/mods"+"/"+list.getSelectedItem());
 			System.out.println("Deleted "+filedes);
 		}else{
 			getModsFileToDelte(list);
 			for(int i = 0; i < fileToDelete.length;i++){
 				filedes = new File(minecraftDirString+"/mods"+"/"+this.fileToDelete[i].getName());
 				filedes.delete();
 				final JFrame popupSuccess = new JFrame();
 				popupSuccess.getContentPane().setLayout(null);
 				popupSuccess.setVisible(true);
 				popupSuccess.setBounds(500, 350, 190, 150);
 				JLabel installed = new JLabel("Deleted "+list.getSelectedItem());
 				installed.setBounds(22, 0, 200, 100);
 				popupSuccess.getContentPane().add(installed);
 				JButton ok = new JButton("Ok");
 				ok.setBounds(42, 60, 95, 35);
 				popupSuccess.getContentPane().add(ok);
 				ok.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						popupSuccess.setVisible(false);
 					}
 				});
 				System.out.println("Deleted "+filedes);
 			}
 			
 		}
 		if(filedes.exists()){
 			filedes.delete();
 		}			
 	}
 
 	public void getModFiles(List list){
 		String path = "Mods/";
 		File folder = new File(path);
 		folder.mkdir();
 		listModFolders = folder.listFiles();
 		if(!folder.exists()){
 			try {
 				folder.createNewFile();
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		System.out.println(folder.getAbsolutePath());
 		for(int i = 0; i < listModFolders.length;i++){
 			if(listModFolders[i].isDirectory()){				
 					list.add(listModFolders[i].getName());
 					dirs.put(listModFolders[i].getName(), listModFolders[i].getAbsolutePath());
 					isFile.put(listModFolders[i].getName(), false);
 					modFoldersDectected++;
 			}else if(listModFolders[i].isFile()){
 				list.add(listModFolders[i].getName());
 				dirs.put(listModFolders[i].getName(), listModFolders[i].getAbsolutePath());
 				isFile.put(listModFolders[i].getName(), true);
 				modFilesDetected++;
 			}
 		}
 		
 		
 	}
 	
 	public String readDescription(List list){
 		String line = "";
 		String description = "";
 		File file = new File(dirs.get(list.getSelectedItem())+"/desc.txt");
 		if(file.exists()){
 			try {		
 				System.out.println(file);
 				FileInputStream fstream = new FileInputStream(file);
 				DataInputStream data = new DataInputStream(fstream);
 				BufferedReader br = new BufferedReader(new InputStreamReader(data));
 				
 				while((line = br.readLine())!=null){
 					description = line;
 				}
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		if(file.exists()){
 			return description;
 		}else{
 			return "No description provided or this mod is not in a mod folder";
 		}
 		
 	}
 	
 	public void getModsFileToDelte(List list){
 		File file = new File(dirs.get(list.getSelectedItem()));
 		file.mkdir();
 		FilenameFilter filterZipsAndJars = new FilenameFilter(){
 
 			@Override
 			public boolean accept(File arg0, String arg1) {
 				if(arg1.endsWith(".zip")){
 					return arg1.endsWith(".zip");
 				}else if(arg1.endsWith(".jar")){
 					return arg1.endsWith(".jar");
 				}else
 					return null != null;
 				
 			}
 			
 		};
 		this.fileToDelete = file.listFiles(filterZipsAndJars);
 		System.out.println(file);
 		
 	}
 		
 	public ImageIcon getLogo(List list){
 		File imgFile = new File(dirs.get(list.getSelectedItem())+"/logo.png");
 		BufferedImage img = null;
 		try{
 			if(imgFile.exists()){
 				img = ImageIO.read(imgFile);
 			}else{
 				img = ImageIO.read(getClass().getResource("nologo.png"));
 			}
 			
 		}catch(IOException e){
 			
 		}
 		return new ImageIcon(img);
 	}
 	
 	public void copyFile(List list){
 		try {
 			File file = new File(dirs.get(list.getSelectedItem()));
 			file.mkdir();
 			System.out.println(file);
 			FilenameFilter filterZipsAndJars = new FilenameFilter(){
 
 				@Override
 				public boolean accept(File arg0, String arg1) {
 					if(arg1.endsWith(".zip")){
 						return arg1.endsWith(".zip");
 					}else if(arg1.endsWith(".jar")){
 						return arg1.endsWith(".jar");
 					}else
 						return null != null;
 					
 				}
 	        	
 	        };
 			File[] filesInFolder = file.listFiles(filterZipsAndJars);
 			File filedes = new File(minecraftDirString+"/mods");
 				File modFileMod = new File(dirs.get(list.getSelectedItem()));
 				File modFolderMod = null;
 				
 				if(isFile.get(list.getSelectedItem()) == true){
 						 modFileMod = new File(dirs.get(list.getSelectedItem()));
 						 FileUtils.copyFileToDirectory(modFileMod, filedes);
 					
 				}else if(isFile.get(list.getSelectedItem()) == false){
 					for(int i = 0; i < filesInFolder.length;i++){
 						System.out.println(filesInFolder[i].getName());
 						 modFolderMod = new File(dirs.get(list.getSelectedItem())+"/"+filesInFolder[i].getName());
 						 FileUtils.copyFileToDirectory(modFolderMod, filedes);
 					}
 					
 				}	
 				
 					
 				
 			
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		final JFrame message = new JFrame();
 		message.getContentPane().setLayout(null);
 		message.setVisible(true);
 		message.setBounds(500, 350, 190, 150);
 		JLabel installed = new JLabel("Installed "+list.getSelectedItem());
 		installed.setBounds(22, 0, 200, 100);
 		message.getContentPane().add(installed);
 		JButton ok = new JButton("Ok");
 		ok.setBounds(42, 60, 95, 35);
 		message.getContentPane().add(ok);
 		ok.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				message.setVisible(false);
 			}
 		});
 		
 	}
 	
 	public void copyAllFiles(List list){
 			
 		try {
 			for(int i =0; i < listModFolders.length;i++){	
 				File filedes = new File(minecraftDirString+"/mods");
 				if(listModFolders[i].isDirectory()){
 					File file = new File(listModFolders[i].getAbsolutePath());
 					file.mkdir();
 					System.out.println("dir: "+file);
 					FilenameFilter filterZipsAndJars = new FilenameFilter(){
 
 						@Override
 						public boolean accept(File arg0, String arg1) {
 							if(arg1.endsWith(".zip")){
 								return arg1.endsWith(".zip");
 							}else if(arg1.endsWith(".jar")){
 								return arg1.endsWith(".jar");
 							}else
 								return null != null;
 							
 						}
 			        	
 			        };
 			        File[] filesInFolder = file.listFiles(filterZipsAndJars);
 			  
 			        for(int f = 0; f < filesInFolder.length;f++){
 			        	File modFolderMod = new File(listModFolders[i].getAbsolutePath()+"/"+filesInFolder[f].getName());
 			        	System.out.println(modFolderMod);
 			        	FileUtils.copyFileToDirectory(modFolderMod, filedes);
 					}
 					
 				}else if(listModFolders[i].isFile()){
 					FileUtils.copyFileToDirectory(listModFolders[i].getAbsoluteFile(), filedes);
 				}
 				
 			}
 			final JFrame message = new JFrame();
 			message.getContentPane().setLayout(null);
 			message.setVisible(true);
 			message.setBounds(500, 350, 190, 150);
 			JLabel installed = new JLabel("Installed all mods!");
 			installed.setBounds(46, 0, 200, 100);
 			message.getContentPane().add(installed);
 			JButton ok = new JButton("Ok");
 			ok.setBounds(42, 60, 95, 35);
 			message.getContentPane().add(ok);
 			ok.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					message.setVisible(false);
 				}
 			});
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void refreshList(List list,boolean refreshFromModFolder){
 		list.clear();
 		getModFiles(list);
 		if(refreshFromModFolder == true){
 			getModsFromModFolder();
 		}
 		
 	}
 	
 	public void getModsFromModFolder(){
 		File modsFolder = new File(minecraftDirString+"/mods");
 		FilenameFilter filterZipsAndJars = new FilenameFilter(){
 
 			@Override
 			public boolean accept(File arg0, String arg1) {
 				if(arg1.endsWith(".zip")){
 					return arg1.endsWith(".zip");
 				}else if(arg1.endsWith(".jar")){
 					return arg1.endsWith(".jar");
 				}else
 					return null != null;
 				
 			}
         	
         };
 		File[] modsFound = modsFolder.listFiles(filterZipsAndJars);
 		for(int i = 0; i < modsFound.length;i++){
 			File fileToCopy = new File(modsFolder+"/"+modsFound[i].getName());
 			try {
 				FileUtils.copyFileToDirectory(fileToCopy, new File("Mods/"));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 }
