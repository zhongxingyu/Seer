 package common;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 import common.FileManager.FileManager;
 import common.FileManager.ListProcessor;
 
 @SuppressWarnings("all")
 public class GUIMain extends JFrame implements ActionListener {
 
     private ScrollPane consoleOut = new ScrollPane();
     private JPanel infoPanel;
     private JToolBar toolBar;
     private JButton launchMc;
     private JButton credits;
     private JButton update;
     private JPanel webPanel;
     private JButton jButton1;
     private JTextArea consoleOut1 = new JTextArea(30,100);
     
     private boolean canUpdate = false;
     private boolean modsUpdate = false;
     private boolean canLaunch = false;
 	private static final long serialVersionUID = 2696364157973172973L;
 
     public GUIMain() {
     	
         initComponents();
         this.setResizable(false);
     }
     @Override
 	public void actionPerformed(ActionEvent event) 
 	{
 		if(event.getSource() == update)
 		{
 			if(!canUpdate)
 			{
 				update.setEnabled(false);
 				updateCheck();
 				update.setEnabled(true);
 				update.setText("Update Now");
 				if(FileManager.dir.endsWith(".minecraft"))
 				{
 					canUpdate = true;
 				}
 				else
 				{
 					JOptionPane.showMessageDialog(null, "Updater needs to be in .minecraft directory to function");
 					update.setEnabled(false);
 				}
 			}
 			else
 			{	
 				if(!modsUpdate)
 				{
 					update.setEnabled(false);
 					boolean pp = triggerUpdate();
 					modsUpdate = true;
 					update.setEnabled(true);
 					canLaunch = pp;
 				}
 				else
 				{
 					//TODO add logic to start minecraftLauncher.jar
 				}
 			}
 		}
 		if(event.getSource() == credits){
 
 			JOptionPane.showMessageDialog(null, "Made by DarkGuardsman");
 
 		}
 
 		if(event.getSource() == launchMc)
 		{
 				openMC();
 		}
 	}   
     public void openMC()
     {
    	File file = new File(FileManager.updaterDir+"/Launcher.jar");
     	if(file.exists())
     	{
    		this.addToConsole("Attempting to launch Minecraft");
     		try {
    			ProcessBuilder pb = new ProcessBuilder("java","-Xmx1024M", "-Xms512M", "-cp", FileManager.updaterDir+"/Launcher.jar", "net.minecraft.LauncherFrame");
     			Process process = pb.start();
    		} catch (IOException e) {
    			this.addToConsole("Failed to launch Minecraft");
    			e.printStackTrace();
    			}	
     		
     	}
     	else
     	{
    		Download.downloadFile("http://s3.amazonaws.com/MinecraftDownload/launcher/minecraft.jar", FileManager.updaterDir, "/Launcher.jar", "url");
     		if(file.exists())
         	{
     			openMC();
         	}
     		
     	}
     }
     public void updateCheck()
     {
     	//check to make sure we have a base folder system to work out of
     	//Normally will generate files it need if not something is wrong
     	addToConsole("Checking File System");
 		Boolean fileExist = FileManager.rootFileCheck();
     	if(fileExist)
 		{
 
 			if(FileManager.errors.size() > 0)
 			{
 				addToConsole("Main file check Debug:");
 				for(int i = 0; i < FileManager.errors.size(); i++)
 				{
 					addToConsole("   "+FileManager.errors.get(i));
 				}
 			}
 			//Check for the update list that is used to manage mods
 			Boolean uc = FileManager.updateList();
 			if(!uc)
 			{
 				addToConsole("Critical: Failed To Get List");
 				if(new File(FileManager.updaterDir+"/ModList.list").exists())
 				{
 					addToConsole("Defaulting to old list");
 				}
 				else
 				{
 					addToConsole("Critical: No Update List Found.");
 				}
 			}
 		}
 		else
 		{
 			addToConsole("Critical: Main file check failed");
 			if(FileManager.errors.size() > 0)
 			{
 				addToConsole("Main File Check Debug:");
 				for(int i = 0; i < FileManager.errors.size(); i++)
 				{
 					addToConsole("   "+FileManager.errors.get(i));
 				}
 			}
 		}	
     }
     public boolean triggerUpdate()
     {
     	
 			addToConsole("Starting Updater");
 			boolean updatedAll = ListProcessor.ProcessorUpdateList(); //processes the update list and downloads missing mods
 			if(ListProcessor.debug.size() > 0)
 			{
 				addToConsole("Starting Update Processor");
 				for(int i = 0; i < ListProcessor.debug.size(); i++)
 				{
 					addToConsole(ListProcessor.debug.get(i));
 				}
 			}
 			return updatedAll;
     }
     public void addToConsole(String msg)
 	{
     	String content = consoleOut1.getText();
     	consoleOut1.setText(content + "\n"+ msg);
 	}
     private void initComponents() {
 
         update = new JButton();
         webPanel = new JPanel();
         infoPanel = new JPanel();
         toolBar = new JToolBar();
         jButton1 = new JButton();
         launchMc = new JButton();
         credits = new JButton();
         
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Ue Mod Downloader");
         setBackground(new java.awt.Color(0, 75, 223));
         setBounds(new java.awt.Rectangle(0, 0, 200, 520));
         setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         
         update.addActionListener(this);
         update.setText("UpdateCheck");
         
         consoleOut1.setEditable(false);
         consoleOut.add(consoleOut1);
         
         webPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         javax.swing.GroupLayout webPanelLayout = new javax.swing.GroupLayout(webPanel);
         webPanel.setLayout(webPanelLayout);
         webPanelLayout.setHorizontalGroup(
             webPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 469, Short.MAX_VALUE)
         );
         webPanelLayout.setVerticalGroup(
             webPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         infoPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 
         javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
         infoPanel.setLayout(infoPanelLayout);
         infoPanelLayout.setHorizontalGroup(
             infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 159, Short.MAX_VALUE)
         );
         infoPanelLayout.setVerticalGroup(
             infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 418, Short.MAX_VALUE)
         );
 
         toolBar.setRollover(true);
 
         jButton1.addActionListener(this);
         launchMc.addActionListener(this);
         credits.addActionListener(this);        
         
         jButton1.setText("Download Mods");
         jButton1.setFocusable(false);
         jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(jButton1);
 
         launchMc.setText("Launch Minecraft");
         launchMc.setFocusable(false);
         launchMc.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         launchMc.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(launchMc);
 
         credits.setText("Credits");
         credits.setFocusable(false);
         credits.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         credits.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolBar.add(credits);
         
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(webPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(consoleOut, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(update, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(infoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(webPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(consoleOut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(update, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(28, 28, 28))
         );
 
         pack();
     }
 }
