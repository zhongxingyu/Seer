 package view;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import controller.Controller;
 
 /**
  * 
 * @author Stephen Box
  * June, 2013
  * 
  * This panel rests on the bottom of the game screen and contains the start 
  * new game button.
  */
 public class ControlPanel extends JPanel{
 	
 	private static final long serialVersionUID = 1L;
 
 	public ControlPanel(Controller controller){
 		this.setBackground(Color.GRAY);
 		this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
 		
 		// Create the new game button
 		JButton newGameButton = new JButton("New Game");
 		newGameButton.addActionListener(controller.getStartNewGameListener());
 		
 		// Load, scale, and add the volume icon
 		try {
 			BufferedImage volumePNG = ImageIO.read(getClass().getResource("/resources/volume.png"));
 			Image icon = volumePNG.getScaledInstance(25, 25, Image.SCALE_SMOOTH);
 			JLabel volumeLabel = new JLabel(new ImageIcon(icon));
 			this.add(volumeLabel);
 			
 		} catch (IOException e) {
 			e.toString();
 		}
 		
 		// Create the volume slider
 		JSlider slider = new JSlider(JSlider.HORIZONTAL,-5,6,5);
 		Dimension sliderSize = new Dimension(75,newGameButton.getPreferredSize().height);
 		slider.setPreferredSize(sliderSize);
 		slider.setSize(sliderSize);
 		slider.setPaintTicks(false);
 		slider.setPaintLabels(false);
 		slider.addChangeListener(controller.getSoundSliderListener());
 		
 		// Add the components to the panel
 		this.add(slider);
 		this.add(Box.createHorizontalGlue());
 		this.add(Box.createHorizontalGlue());
 		this.add(newGameButton);
 	}
 }
