 package net.infinitycoding.carsim;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Canvas extends JPanel {
 	
 	public Canvas(int width, int height)
 	{
 		this.setBounds(0, 0, width, height);
 		
 		JLabel bgImg = new JLabel(new ImageIcon(CarSim.class.getResource("res/test.jpg")));
		bgImg.setBounds(0, 0, width, height);
 		
 		this.add(bgImg);
 		this.setVisible(true);
 	}
 
 }
