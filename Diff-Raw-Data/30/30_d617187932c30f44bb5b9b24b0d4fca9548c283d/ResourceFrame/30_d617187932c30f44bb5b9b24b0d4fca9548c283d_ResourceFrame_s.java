 import java.awt.List;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JButton;
 
 import org.apache.commons.io.FileUtils;
 
 
 public class ResourceFrame extends JFrame {
 
 	private JPanel contentPane;
 	
 	int rspackDetected = 0;
 	
 	File[] listFiles;
 	static HashMap<String,String> dirs = new HashMap<String,String>();
 
 	/**
 	 * Create the frame.
 	 */
 	public ResourceFrame() {
 		setResizable(false);
 		setTitle("TheWerty Mod Installer V.01 - Resource Packs");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 1073, 628);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		
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
 		getResourceFiles(list);
 		
 		JMenuBar menuBar = new JMenuBar();
 		menuBar.setBounds(0, 0, 1057, 21);
 		contentPane.add(menuBar);
 		
 		JMenu typem = new JMenu("Type");
 		menuBar.add(typem);
 		
 		JCheckBoxMenuItem resourceb = new JCheckBoxMenuItem("Resources");
 		resourceb.setSelected(true);
 		typem.add(resourceb);
 		
 		JCheckBoxMenuItem modb = new JCheckBoxMenuItem("Mods");
 		modb.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			 setVisible(false);
 			 MainFrame msf = new MainFrame();
 			 msf.setVisible(true);
 			
 			}
 		});
 		typem.add(modb);
 		
 		JLabel label = new JLabel(this.rspackDetected+" Resource Packs Dectected");
 		label.setBounds(10, 565, 237, 14);
 		contentPane.add(label);
 		
 		JButton installButton = new JButton("Install");
 		installButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				copyFile(list);
 			}
 		});
 		installButton.setBounds(126, 411, 213, 62);
 		contentPane.add(installButton);
 		
 		JLabel label_1 = new JLabel("\t\tInstaller coded by Minestrappolation Team ");
 		label_1.setBounds(471, 435, 206, 14);
 		contentPane.add(label_1);
 		
 		JButton button = new JButton("Install All");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			copyAllFiles(list);			
 			}
 		});
 		button.setBounds(834, 411, 213, 62);
 		contentPane.add(button);
 	
 		
 		
 		
 	}
 	
 	public String readDescription(List list){
 		String line = "";
 		String description = "";
 		File file = new File(dirs.get(list.getSelectedItem())+"/desc.txt");
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
 		if(file.exists()){
 			return description;
 		}else{
 			return "No description provided";
 		}
 		
 	}
 	
 	public void getResourceFiles(List list){
 		String path = "Resource Packs/";
 		String files;
 		File folder = new File(path);
 		folder.mkdir();
 		listFiles = folder.listFiles();
 		if(!folder.exists()){
 			try {
 				folder.createNewFile();
 				
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		System.out.println(folder.getAbsolutePath());
 		for(int i = 0; i < listFiles.length;i++){
 			this.rspackDetected++;
 			if(listFiles[i].isDirectory()){				
 					list.add(listFiles[i].getName());
 					dirs.put(listFiles[i].getName(), listFiles[i].getAbsolutePath());
 		}
 		}
 		
 		
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
 		File file = new File(dirs.get(list.getSelectedItem())+"/"+getModsFileName(list));
 		File filedes = new File(System.getenv("APPDATA")+"/.minecraft"+"/resourcepacks");
 		try { 
 			FileUtils.copyFileToDirectory(file, filedes);
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
 	
 	public void copyAllFiles(List list){
 		
 		try {
 			for(int i =0; i < listFiles.length;i++){		
 				String line = "";
 				String filen = "";
 				File filename = new File(listFiles[i].getAbsoluteFile()+"/filename.txt");
 				FileInputStream fstream = new FileInputStream(filename);
 				DataInputStream data = new DataInputStream(fstream);
 				BufferedReader br = new BufferedReader(new InputStreamReader(data));
 				
 				while((line = br.readLine())!=null){
 					filen = line;
 					break;
 				}
 				File file = new File(dirs.get(listFiles[i].getName())+"/"+filen);
 				File filedes = new File(System.getenv("APPDATA")+"/.minecraft"+"/mods");
 				FileUtils.copyFileToDirectory(file, filedes);
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
 }
