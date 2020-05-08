 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.List;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.awt.Button;
 import javax.swing.UIManager;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 
 import org.apache.commons.io.FileUtils;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JCheckBoxMenuItem;
 
 
 public class MainFrame extends JFrame {
 
 	private JPanel contentPane;
 	int modFoldersDectected = 0;
 	int modFilesDetected = 0;
 	int installerType = 0;
 	
 	File[] listModFolders;
 	static HashMap<String,String> dirs = new HashMap<String,String>();
 	static HashMap<String,Boolean> isFile = new HashMap<String, Boolean>();
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainFrame frame = new MainFrame();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainFrame() {
 		setResizable(false);
 		setTitle("TheWerty Mod Installer V.01 - Mods");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 1073, 628);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		final JPanel texturePane = new JPanel();
 		texturePane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		texturePane.setLayout(null);
 		
 		final JEditorPane tane = new JEditorPane();
 		tane.setEditable(false);
 		tane.setBounds(126, 218, 921, 166);
 		texturePane.add(tane);
 
 	
 			final JEditorPane descPane = new JEditorPane();
 			descPane.setEditable(false);
 			descPane.setBounds(126, 218, 921, 166);
 			contentPane.add(descPane);
 			
 			final JLabel logo = new JLabel("");
 			logo.setBounds(270, 28, 628, 166);
 			contentPane.add(logo);
 			
 			final List list = new List();
 			list.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					descPane.setText(readDescription(list));
 					logo.setIcon(getLogo(list));
 				}
 			});
 			list.setBounds(10, 28, 110, 522);
 			contentPane.add(list);
 			
 			getModFiles(list);
 			
 			JButton installButton = new JButton("Install");
 			installButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					copyFile(list);
 				}
 			});
 			installButton.setBounds(126, 411, 213, 62);
 			contentPane.add(installButton);
 			
 			JButton allButton = new JButton("Install All");
 			allButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 				copyAllFiles(list);
 				}
 			});
 			allButton.setBounds(834, 411, 213, 62);
 			contentPane.add(allButton);
 			
 			JLabel credits = new JLabel("\t\tInstaller coded by Minestrappolation Team ");
 			credits.setBounds(471, 435, 206, 14);
 			contentPane.add(credits);
 			
 			JLabel debugLabel = new JLabel("");
 			debugLabel.setBounds(10, 565, 237, 14);
 			debugLabel.setText(modFoldersDectected+" Mod Folders Dectected "+modFilesDetected+" Mod Files Dectected");
 			contentPane.add(debugLabel);
 			
 			JMenuBar menuBar = new JMenuBar();
 			menuBar.setBounds(0, 0, 1057, 21);
 			contentPane.add(menuBar);
 			
 			JMenu mnType = new JMenu("Type");
 			menuBar.add(mnType);
 			
 			JCheckBoxMenuItem resourceb = new JCheckBoxMenuItem("Resources");
 			resourceb.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 				ResourceFrame rsf = new ResourceFrame();
 				setVisible(false);
 				rsf.setVisible(true);
 				
 				}
 			});
 			mnType.add(resourceb);
 			
 			JCheckBoxMenuItem modb = new JCheckBoxMenuItem("Mods");
 			mnType.add(modb);
 			modb.setSelected(true);
 			
 			JButton uninstallButton = new JButton("UnInstall");
 			uninstallButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 				deleteMod(list);
 				
 				}
 			});
 			uninstallButton.setBounds(126, 488, 213, 62);
 			contentPane.add(uninstallButton);
 		
 			
 		}
 	
 	public void deleteMod(List list){
 		File filedes;
 		if(isFile.get(list.getSelectedItem()) == true){
 			filedes = new File(System.getenv("APPDATA")+"/.minecraft"+"/mods"+"/"+list.getSelectedItem());
 		}else{
 			filedes = new File(System.getenv("APPDATA")+"/.minecraft"+"/mods"+"/"+getModsFileName(list));
 		}
 		
 		filedes.delete();
 		System.out.println("Deleted "+filedes);
 		
 		
 	}
 		
 		
 		
 	
 	
 	public void getModFiles(List list){
 		String path = "Mods/";
 		String files;
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
 	
 	public String getModsFileName(List list){
 		String line = "";
 		String filen = "";
 		try {
 			File file = new File(dirs.get(list.getSelectedItem())+"/filename.txt");
 			System.out.println(file);
 			FileInputStream fstream = new FileInputStream(file);
 			DataInputStream data = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(data));
 			
 			while((line = br.readLine())!=null){
 				filen = line;
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return filen;
 	}
 		
 	public ImageIcon getLogo(List list){
 		File imgFile = new File(dirs.get(list.getSelectedItem())+"/logo.png");
		File defaultImg = new File("Res/nologo.png");
 		BufferedImage img = null;
 		try{
 			if(imgFile.exists()){
 				img = ImageIO.read(imgFile);
 			}else{
				img = ImageIO.read(defaultImg);
 			}
 			
 		}catch(IOException e){
 			
 		}
 		return new ImageIcon(img);
 	}
 	
 	public void copyFile(List list){
 		File modFileMod = new File(dirs.get(list.getSelectedItem()));
 		File modFolderMod = null;
 		if(isFile.get(list.getSelectedItem()) == true){
 			 modFileMod = new File(dirs.get(list.getSelectedItem()));
 			 System.out.println("Mod selected is a file");
 		}else if(isFile.get(list.getSelectedItem()) == false){
 			 modFolderMod = new File(dirs.get(list.getSelectedItem())+"/"+getModsFileName(list));
 			 System.out.println("Mod selected is contained in a mod folder");
 		}	
 		File filedes = new File(System.getenv("APPDATA")+"/.minecraft"+"/mods");
 		try { 
 			if(isFile.get(list.getSelectedItem()) == true){
 				FileUtils.copyFileToDirectory(modFileMod, filedes);
 			}else if(isFile.get(list.getSelectedItem()) == false){
 				FileUtils.copyFileToDirectory(modFolderMod, filedes);
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
 			
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void copyAllFiles(List list){
 			
 		try {
 			for(int i =0; i < listModFolders.length;i++){	
 				File filedes = new File(System.getenv("APPDATA")+"/.minecraft"+"/mods");
 				if(listModFolders[i].isDirectory()){
 					String line = "";
 					String filen = "";
 					File filename = new File(listModFolders[i].getAbsoluteFile()+"/filename.txt");
 					FileInputStream fstream = new FileInputStream(filename);
 					DataInputStream data = new DataInputStream(fstream);
 					BufferedReader br = new BufferedReader(new InputStreamReader(data));
 					
 					while((line = br.readLine())!=null){
 						filen = line;
 						break;
 					}
 					File file = new File(dirs.get(listModFolders[i].getName())+"/"+filen);
 					FileUtils.copyFileToDirectory(file, filedes);
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
 	
 	public void installAll(List list){
 		for(int i = 0; i < listModFolders.length;i++){
 			copyFile(list);
 		}
 	}
 }
