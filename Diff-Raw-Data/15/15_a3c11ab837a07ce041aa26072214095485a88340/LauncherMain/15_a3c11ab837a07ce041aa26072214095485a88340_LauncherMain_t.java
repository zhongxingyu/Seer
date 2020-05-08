 package com.lordralex.debugmclauncher;
 
import com.lordralex.debugmclauncher.mclauncher.DisplayFrame;
 import com.lordralex.debugmclauncher.panels.IconPanel;
 import com.lordralex.debugmclauncher.panels.InformationPanel;
 import com.lordralex.debugmclauncher.panels.LoginPanel;
 import com.lordralex.debugmclauncher.panels.SystemInformationPanel;
 import com.lordralex.debugmclauncher.utils.OS;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JFrame;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.WindowConstants;
 import javax.swing.border.BevelBorder;
 
 public class LauncherMain extends JFrame {
 
     private static LauncherMain instance;
 
     public LauncherMain() {
         initComponents();
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         loginPanel1 = new LoginPanel();
         systemInformationPanel1 = new SystemInformationPanel();
         iconPanel1 = new IconPanel();
         informationPanel1 = new InformationPanel();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setBackground(new Color(255, 255, 255));
 
         loginPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 
         systemInformationPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
 
         iconPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
         iconPanel1.setPreferredSize(new Dimension(423, 100));
 
         GroupLayout iconPanel1Layout = new GroupLayout(iconPanel1);
         iconPanel1.setLayout(iconPanel1Layout);
         iconPanel1Layout.setHorizontalGroup(
             iconPanel1Layout.createParallelGroup(Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         iconPanel1Layout.setVerticalGroup(
             iconPanel1Layout.createParallelGroup(Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         informationPanel1.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
         informationPanel1.setPreferredSize(new Dimension(469, 304));
 
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(Alignment.TRAILING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(iconPanel1, GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(loginPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
             .addGroup(layout.createSequentialGroup()
                 .addComponent(informationPanel1, GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(systemInformationPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(Alignment.LEADING)
                     .addComponent(informationPanel1, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                     .addComponent(systemInformationPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                     .addComponent(iconPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(loginPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     public static void main(String args[]) {
         try {
             Logger.getLogger("com.lordralex.debugmclauncher").addHandler(new FileHandler("logs.txt", true));
         } catch (IOException ex) {
             Logger.getLogger(LauncherMain.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(LauncherMain.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 instance = new LauncherMain();
                 instance.setVisible(true);
                 instance.informationPanel1.getNewsFeed();
                 instance.systemInformationPanel1.getSystemInfo();
                 instance.update(instance.getGraphics());
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private IconPanel iconPanel1;
     private InformationPanel informationPanel1;
     private LoginPanel loginPanel1;
     private SystemInformationPanel systemInformationPanel1;
     // End of variables declaration//GEN-END:variables
 
     public static LauncherMain getInstance() {
         return instance;
     }
 
     public IconPanel getIconPanel() {
         return iconPanel1;
     }
 
     public LoginPanel getLoginPanel() {
         return loginPanel1;
     }
 
     public void launchMinecraft(String[] args) {
         this.setVisible(false);

        if (loginPanel1.forceUpdate()) {
            DisplayFrame displayFrame = new DisplayFrame();
            displayFrame.setVisible(true);
            displayFrame.downloadFiles();
            displayFrame.setVisible(false);
         }
 
         ArrayList<String> command = new ArrayList<String>();
         command.add("java");
         command.add("-Xmx512M");
         command.add("-Xms128M");
         command.add("-cp");
         command.add("\"%BIN%\\minecraft.jar" + File.pathSeparatorChar
                 + "%BIN%\\lwjgl.jar" + File.pathSeparatorChar
                 + "%BIN%\\lwjgl_util.jar" + File.pathSeparatorChar
                 + "%BIN%\\jinput.jar\"");
         command.add("-Djava.library.path=\"%BIN%\\natives\"");
        command.add("net.minecraft.client.MinecraftApplet");
         command.add(args[0]);
         command.add(args[3]);
 
         File bin = new File(OS.getFolderFile(), "bin");
 
         for (int i = 0; i < command.size(); i++) {
             command.set(i, command.get(i).replace("%BIN%", bin.getPath()));
         }
 
         ProcessBuilder builder = new ProcessBuilder().command(command).inheritIO();
         try {
             Process process = builder.start();
             InputStream error = process.getErrorStream();
             InputStream input = process.getInputStream();
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(error));
             BufferedReader inputReader = new BufferedReader(new InputStreamReader(input));
             EchoThread errorThread = new EchoThread(errorReader);
             EchoThread inputThread = new EchoThread(inputReader);
             errorThread.start();
             inputThread.start();
             process.waitFor();
         } catch (InterruptedException ex) {
             Logger.getLogger(LauncherMain.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(LauncherMain.class.getName()).log(Level.SEVERE, null, ex);
         }
         System.exit(0);
     }
 
     private class EchoThread extends Thread {
 
         BufferedReader input;
 
         public EchoThread(BufferedReader reader) {
             input = reader;
         }
 
         @Override
         public void run() {
             try {
                 String line;
                 while ((line = input.readLine()) != null) {
                     System.out.println(line);
                 }
             } catch (IOException ex) {
                 ex.printStackTrace(System.out);
             }
         }
     }
 }
