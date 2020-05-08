 package com.Jessy1237.renamer;
 
 import javax.swing.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 
 @SuppressWarnings("serial")
 public class Download extends JFrame{
 
 	public JButton Start;
 	public JLabel a;
 	
 	public static String userHome = System.getProperty("user.home");
 	public static File Windir = new File(userHome,
 			"/AppData/roaming/.minecraft/bin");
 	public static File Winfile18 = new File(Windir, "/1.8.jar");
 	public static File Winfile17 = new File(Windir, "/1.7.jar");
 	public static File Winfile1 = new File(Windir, "/1.0.jar");
 	public static File WinfileJar = new File(Windir, "/minecraft.jar");
 	public static File dir = new File(userHome, "/Library/Application Support/minecraft/bin");
 	public static File file18 = new File(dir, "/1.8.jar"); 
 	public static File file17 = new File(dir, "/1.7.jar");
 	public static File file1 = new File(dir, "/1.0.jar");
 	public static File fileJar = new File(dir, "/minecraft.jar");
 	public static String osName = System.getProperty("os.name").toLowerCase();
 	public static String u1 = ("*URL HERE*");
	public static String u18 = ("*URL HERE");
 	public static String u17 = ("*URL HERE*");
 	public static File Dir = new File(userHome, "/McVSTemp");
 	public static File ver = new File(dir, "/version.txt");
 	public static int c = 1;
 	
 	public Download(){
 		this.setTitle("Minecraft Version Switcher: Download Jar");
 		this.setSize(new Dimension(400, 110));
 		this.setResizable(false);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLayout(null);
 		this.setLocationRelativeTo(null);
 		this.Start = new JButton();
 		this.Start.setText("Start");
 		this.Start.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent evt) {
 				a.setEnabled(false);
 		        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 		        try {
 		        	doInBackground();
 				} catch (IOException e) {
 					JOptionPane.showMessageDialog(null, "Error While Downloading file, is your internet working?", "Minecraft Version Switcher", 1);
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		a = new JLabel();
 		a.setText("Click Start, Don't Worry if it freezes it's still downloading!");
 		a.setFont(new Font("Arial", Font.PLAIN, 14));
 		a.setBounds(20, 10, 380, 20);
 		
 		Start.setBounds(140, 40, 100, 30);
 		
 		add(a);
 		add(Start);
 	}
 	
 	        public void doInBackground()throws IOException {
 	        	if(c == 0){
 	        		if(dLoadJars.v.equals("1.0")){
 	        			if(osName.contains("win")){
 	        				loadFile(u1, WinfileJar);
 	        			}
 	        			if(osName.contains("mac")){
 	        				loadFile(u1, fileJar);
 	        			}
 	        		}else{
 	        			if(dLoadJars.v.equals("1.7")){
 	        				if(osName.contains("win")){
 	        					loadFile(u17, WinfileJar);
 	        				}
 	        				if(osName.contains("mac")){
 	        					loadFile(u17, fileJar);
 	        				}
 	        			}else{
 	        				if(dLoadJars.v.equals("1.8")){
 	        					if(osName.contains("win")){
 	        						loadFile(u18, WinfileJar);
 	        					}
 	        					if(osName.contains("mac")){
 	        						loadFile(u18, fileJar);
 	        					}
 	        				}else{
 	        					if(dLoadJars.v.equals("McVS")){
 	        						loadFile(Update.dUrl, new File(userHome, "/Desktop/Minecraft Version Switcher v" + Update.version + ".jar"));
 	        					}
 	        				}
 	        			}
 	        		}
 	        	}else{
 	        		if(c == 1){
 	        			setVisible(false);
 	        			dLoadJars d = new dLoadJars();
 	        			d.setVisible(true);
 	        		}
 	        	}
 	        }
 
 	        public void done() {
 	            Toolkit.getDefaultToolkit().beep();
 	            Start.setEnabled(true);
 	            c = 1;
 	            dLoadJars.v = null;
 	            a.setBounds(170, 10, 80, 20);
 	            Start.setText("Done");
 	            a.setText("Done!");
 	            setCursor(null);
 	        }
 	 
 	 public void loadFile(String remoteFile, File file) throws IOException{
 		BufferedInputStream inputStream = new BufferedInputStream(new URL(remoteFile).openStream());
 		FileOutputStream fos = new FileOutputStream(file);
 		BufferedOutputStream out = new BufferedOutputStream(fos, 1024);
 
 		byte[] buffer = new byte[1024];
 		int bytesRead = 0;
 
 		while((bytesRead = inputStream.read(buffer)) != -1){   
 		   out.write(buffer, 0, bytesRead);
 		}
 	    out.close();
 	    inputStream.close();
 	    Opener.mcver();
 	    done();
 		ver.delete();
 		Dir.delete();
 	 }
 }
