 package net.managers;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 import net.*;
 import state.*;
 
 @SuppressWarnings("serial")
 public class FPManager extends Manager implements ActionListener {
 	GraphicsPanel gPanel;
 	OrderPanel oPanel;
 	
 	public FPManager() {
 		super(ManagerType.Factory);
 				
 		gPanel = new GraphicsPanel(imageMap, inMap);
 		oPanel = new OrderPanel();
 		
 		Dimension full = new Dimension(1024, 768);
 		gPanel.setMaximumSize(full);
 		gPanel.setMinimumSize(full);
 		gPanel.setPreferredSize(full);
 		
 		this.setContentPane(new JPanel(new BorderLayout()));
 		this.add(oPanel, BorderLayout.WEST);
 		this.add(gPanel, BorderLayout.CENTER);
 		this.setVisible(true);
 		this.pack();
 	}
 		
 	public void actionPerformed(ActionEvent ae) {
 		gPanel.inMap = inMap;
 		
 		gPanel.repaint();
 	}
         
         public void playMusic(){
         AudioInputStream ais = null;
         try {
             ais = AudioSystem.getAudioInputStream(new File("sound/underTheSea.wav"));
             Clip clip = AudioSystem.getClip();
             clip.open( ais );
             clip.loop(Clip.LOOP_CONTINUOUSLY);
             clip.start();
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null,"Audio Error");        
        
         }
         }
     
 	public static void main(String[] args) {
 		FPManager m = new FPManager();
 		new Timer(GraphicsPanel.SYNCRATE, m).start();
                 m.playMusic();
 	}
 
 }
