 /*
  * SkillView.java contains the view definition and display methods for the SkillModel and Controller Classes.
  *
  * Author: Santiago Torres
  * Date:   apr 29, 2013
  */
 import java.awt.*;
 import javax.swing.*;
 import javax.imageio.*;
 import java.awt.image.*;
 import java.io.*;
 public class SkillView extends Panel{
 	//private elements
 	private JLabel name;
 	private JLabel level;
 	private JButton Halo;
 	private Box border;
 	public SkillView(String name, String level, String imageFilename){
 		super();
 		BoxLayout theLayout = new BoxLayout(this,BoxLayout.Y_AXIS);
 		BufferedImage image;
 		ImageIcon imageIcon;
 		
 		try{
 			image = ImageIO.read(new File(imageFilename));
 		}catch(IOException exception){
			image = new BufferedImage(40,40,1);
 		}
 		imageIcon = new ImageIcon(image.getScaledInstance(50,50,Image.SCALE_SMOOTH));
 		border = new Box(BoxLayout.Y_AXIS);
 		border.setBorder(BorderFactory.createMatteBorder(3,3,3,3,new Color(200,10,10)));
 		border.setOpaque(true);
 		border.setSize(new Dimension(50,50));
 		border.setBackground(new Color(200,10,50));
 		this.Halo = new JButton(imageIcon);
 		this.Halo.setContentAreaFilled(false);
 		//this.Halo.setBorderPainted(false);
 		this.Halo.setMargin(new Insets(0,0,0,0));
 		this.Halo.setOpaque(true);
 		this.Halo.setBackground(new Color(80,80,80,200));
 		this.Halo.setAlignmentX(this.CENTER_ALIGNMENT);
 		border.add(this.Halo);
 
 	       	this.name = new JLabel(name);
 		this.name.setAlignmentX(this.CENTER_ALIGNMENT);
 		this.level = new JLabel(level);
 		this.level.setAlignmentX(this.CENTER_ALIGNMENT);
 		this.setLayout(theLayout);
 		this.add(this.name);
 		this.add(border);
 		this.add(this.level);	
 		this.validate();
 		this.setVisible(true);
 	}
 
 }
