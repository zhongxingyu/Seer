 package org.noip.staticattic.GUI.Events;
 
 import java.awt.Color;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.swing.JLabel;
 
 import org.noip.staticattic.GUI.MainWindow;
 import org.noip.staticattic.fileutils.AudioHandler;
 import org.noip.staticattic.fileutils.TextureHandler;
 
 public class ShowTitleScreen extends GUIEvent {
 
 	public ShowTitleScreen(MainWindow main, int delay) {
 		
 		super(main, delay);
 		
 	}
 	
 	@Override
 	public void execute() {
 		
 		super.getMain().mainpanel.setBackground(Color.BLACK);
 		
 		Clip clip = null;
 		
 		try {
 			
 			clip = AudioSystem.getClip();
 			clip.open(AudioHandler.getStartup());
 			
 		} catch (Exception e) {
 			
 			e.printStackTrace();
 			
 		}
 		
 		clip.start();
 		
 		JLabel companylogo = new JLabel(TextureHandler.getCompanyLogo());
 		companylogo.setBounds((int)(super.getMain().screensize.getWidth()/2)-190, (int)(super.getMain().screensize.getHeight()/2)-119, 380, 238);
 		
 		super.getMain().mainpanel.add(companylogo);
 		super.getMain().repaint();
 		
 	}
 
 }
