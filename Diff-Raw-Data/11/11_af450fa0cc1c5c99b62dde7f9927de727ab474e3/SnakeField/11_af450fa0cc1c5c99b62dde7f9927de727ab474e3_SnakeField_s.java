 package ru.shalicikkomarov;
 
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
import java.io.File;
 import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.imageio.ImageIO;
 import javax.swing.*;
 
 public class SnakeField extends JPanel {
 	private static BufferedImage img;
 	private static BufferedImage gameover;
 	public static BufferedImage imgmouse;
 	private int backWidth, backHeight;
 
 	public static int state[][] = new int[16][12];
 	static Mouse mouse;
 	static Snake snake;
 	static boolean GameOver;
 	public SnakeField() throws IOException {
 		this.setPreferredSize(new Dimension(Cnst.FWIDTH, Cnst.FHEIGHT));
 		img = ResourceLoader.getImage("background.jpg");
 		backWidth = img.getWidth(null);
 		backHeight = img.getHeight(null);
 		imgmouse = ResourceLoader.getImage("spmouse.png");
 		gameover = ResourceLoader.getImage("gameover.jpg");
 		snake = new Snake();
 		GameOver = false;
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 		g.drawImage(img, 0, 0, Cnst.FWIDTH, Cnst.FHEIGHT, 0, 0, backWidth, backHeight, null);
 		g.setColor(Color.BLACK);
 		for(int i = 0; i < 16; i++)
 			for(int j = 0; j < 12; j++) 
 				g.drawRect(i * Cnst.FCELL, j * Cnst.FCELL, Cnst.FCELL, Cnst.FCELL);
 		mouse.render(g, this);
 		GameOver = snake.isOver();
 		if(GameOver) {
 			g.drawImage(gameover, 0, 0, Cnst.FWIDTH, Cnst.FHEIGHT, 0, 0, gameover.getWidth(), gameover.getHeight(), this); 
 			g.setColor(Color.RED);
 			g.drawString("Press ENTER to continue...", 330, 500);
 		}
 		if(mouse.mouseDead)mouse = new Mouse();
 	}
 }
