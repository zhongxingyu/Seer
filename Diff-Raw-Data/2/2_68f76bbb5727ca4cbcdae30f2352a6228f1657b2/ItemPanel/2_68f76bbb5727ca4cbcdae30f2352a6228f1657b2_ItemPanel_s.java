 package gui;
 
 import image.SpriteSheet;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.border.StrokeBorder;
 
 @SuppressWarnings("serial")
 public class ItemPanel extends JPanel{
 	
 	private static final int BUTTON_WIDTH = 64;
 	private static final int BUTTON_HEIGHT = 64;
 	
 	public static String currentButton;
 	
 	public ItemPanel(){
 		super.setBorder(new StrokeBorder(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)));
 		setFocusable(false);
 		setPreferredSize(new Dimension(120, YoloRiot.SCREEN_HEIGHT));
 		add(newButton("NEW_TURRET_ANIMATION.png", 32, 0, 64, 64));
 		add(newButton("Wall, side top bot.png", 0, 0, 32, 32));
 	}
 	
 	public void paintComponent(Graphics g){
 		g.setColor(Color.white);
 		g.fillRect(0,0,getWidth(), getHeight());
 	}
 	
 	public JButton newButton(String imageFile, int x, int y, int width, int height){
 		SpriteSheet image = new SpriteSheet(0, 0, 64,64, imageFile);
 		
 		JButton button = new JButton(new ImageIcon(image.getImage(x, y, width, height).getScaledInstance(64, 64, Image.SCALE_FAST)));
 		button.setFocusable(false);
 		button.setName(imageFile);
 		button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
 		
 		button.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.out.println(getWidth()+" "+ getHeight());
 				JButton b = (JButton)(arg0.getSource());
 				currentButton = b.getName();
 			}
 			
 		});
 		
 		return button;
 	}
 	
 	public static int getButtonNum(){
 		if(currentButton.equals("NEW_TURRET_ANIMATION.png")){
 			return 0;
		}else if(currentButton.equals(".png")){
 			return 1;
 		}else{
 			return -1;
 		}
 	}
 	
 	public static Image getImage(){
 		if(currentButton.equals("NEW_TURRET_ANIMATION.png")){
 			return new SpriteSheet(0, 0, 64, 64, "NEW_TURRET_ANIMATION.png").getImage(32, 0, 64, 64).getScaledInstance(64, 64, Image.SCALE_FAST);
 		}else if(currentButton.equals("Wall, side top bot.png")){
 			return new SpriteSheet(0, 0, 64, 64, "Wall, side top bot.png").getImage(0, 0, 32, 32).getScaledInstance(64, 64, Image.SCALE_FAST);
 		}else{
 			return null;
 		}
 	}
 	
 	public static void resetCurrentButton(){
 		currentButton = null;
 	}
 }
